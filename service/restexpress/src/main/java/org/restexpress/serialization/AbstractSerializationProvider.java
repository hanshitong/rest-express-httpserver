/*
    Copyright 2013, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.restexpress.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;

import org.restexpress.Format;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.common.exception.ConfigurationException;
import org.restexpress.common.util.StringUtils;
import org.restexpress.contenttype.MediaRange;
import org.restexpress.contenttype.MediaTypeParser;
import org.restexpress.exception.BadRequestException;
import org.restexpress.exception.NotAcceptableException;
import org.restexpress.response.ResponseProcessor;
import org.restexpress.response.ResponseWrapper;

/**
 * @author toddf
 * @since Jul 18, 2013
 */
public abstract class AbstractSerializationProvider
implements SerializationProvider
{
	private Map<String, ResponseProcessor> processorsByFormat = new HashMap<String, ResponseProcessor>();
	private Map<String, ResponseProcessor> processorsByMediaType = new HashMap<String, ResponseProcessor>();
	private List<MediaRange> supportedMediaRanges = new ArrayList<MediaRange>();
	private ResponseProcessor defaultProcessor;
	private List<Alias> aliases = new ArrayList<Alias>();

	/**
	 * Add a SerializationProcessor to this SerializationProvider, along with ResponseWrapper to use
	 * to alter/format responses.
	 * 
	 * @param processor
	 * @param wrapper
	 */
	public void add(SerializationProcessor processor, ResponseWrapper wrapper)
	{
		add(processor, wrapper, false);
	}

	/**
	 * Add a SerializationProcessor to this SerializationProvider, along with ResponseWrapper to use
	 * to alter/format responses.  If isDefault is true, this SerializationProcessor is used when
	 * Content-Type negotiation fails or format is not specified in the URL.
	 * 
	 * @param processor
	 * @param wrapper
	 * @param isDefault
	 */
	public void add(SerializationProcessor processor, ResponseWrapper wrapper, boolean isDefault)
	{
		addMediaRanges(processor.getSupportedMediaRanges());
		ResponseProcessor responseProcessor = new ResponseProcessor(processor, wrapper);
		assignAliases(responseProcessor);

		for (String format : processor.getSupportedFormats())
		{
			if (processorsByFormat.containsKey(format))
			{
				throw new ConfigurationException("Duplicate supported format: " + format);
			}

			processorsByFormat.put(format, responseProcessor);
		}
		
		for (MediaRange mediaRange : processor.getSupportedMediaRanges())
		{
			String mediaType = mediaRange.asMediaType();
			
			if (!processorsByMediaType.containsKey(mediaType))
			{
				processorsByMediaType.put(mediaRange.asMediaType(), responseProcessor);
			}
		}
		
		if (isDefault)
		{
			defaultProcessor = responseProcessor;
		}
	}

	@Override
	public void alias(String name, Class<?> type)
	{
		Alias a = new Alias(name, type);
		if (!aliases.contains(a))
		{
			aliases.add(a);
		}
		
		assignAlias(a);
	}

	@Override
	public void setDefaultFormat(String format)
	{
		ResponseProcessor processor = processorsByFormat.get(format);
		
		if (processor == null)
		{
			throw new RuntimeException("No serialization processor found for requested response format: " + format);
		}
		
		defaultProcessor = processor;
	}
	
	/**
	 * Provided for testing so that UTs can specify and format and compare the resolver-based results.
	 * 
	 * @param format
	 * @return
	 */
	public SerializationProcessor getSerializer(String format)
	{
		ResponseProcessor p = processorsByFormat.get(format);
		
		if (p != null)
		{
			return p.getSerializer();
		}
		
		return null;
	}

	@Override
	public SerializationSettings resolveRequest(Request request)
	{
		ResponseProcessor processor = null;
	    String format = request.getFormat();
	    String bestMatch = null;

		if (format != null)
		{
			processor = processorsByFormat.get(format);

			if (processor == null)
			{
				throw new NotAcceptableException(format);
			}
		}

		if (processor == null)
		{
			List<MediaRange> requestedMediaRanges = MediaTypeParser.parse(request.getHeader(HttpHeaders.Names.CONTENT_TYPE));
			bestMatch = MediaTypeParser.getBestMatch(supportedMediaRanges, requestedMediaRanges);
	
			if (bestMatch != null)
			{
				processor = processorsByMediaType.get(bestMatch);
			}
		}
		
		if (processor == null)
		{
			processor = defaultProcessor;
		}

		return new SerializationSettings((bestMatch == null ? request.getHeader(HttpHeaders.Names.CONTENT_TYPE) : bestMatch), processor);
	}

	@Override
    public SerializationSettings resolveResponse(Request request, Response response, boolean shouldForce)
    {
		String bestMatch = null;
		ResponseProcessor processor = null;
		String format = request.getFormat();
		//add by hanst,默认json格式
		if (format == null) 
			format = Format.JSON; 

		if (exceptionOccurredBeforeRouteResolution(format, response))
		{
			format = parseFormatFromUrl(request.getUrl());
		}

		if (format != null)
		{
			processor = processorsByFormat.get(format);

			if (processor != null)
			{
				bestMatch = processor.getSupportedMediaRanges().get(0).asMediaType();
			}
			else if (!shouldForce)
			{
				throw new BadRequestException("Requested representation format not supported: " + format 
					+ ". Supported formats: " + StringUtils.join(", ", processorsByFormat.keySet()));
			}
		}

		if (processor == null)
		{
			List<MediaRange> requestedMediaRanges = MediaTypeParser.parse(request.getHeader(HttpHeaders.Names.ACCEPT));
			bestMatch = MediaTypeParser.getBestMatch(supportedMediaRanges, requestedMediaRanges);
	
			if (bestMatch != null)
			{
				processor = processorsByMediaType.get(bestMatch);
			}
			else if (!shouldForce && !requestedMediaRanges.isEmpty())
			{
				throw new NotAcceptableException("Supported Media Types: " + StringUtils.join(", ", supportedMediaRanges));
			}
		}
		
		if (processor == null)
		{
			processor = defaultProcessor;
			bestMatch = processor.getSupportedMediaRanges().get(0).asMediaType();
		}
		
		return new SerializationSettings(bestMatch, processor);
    }


	// SECTION: CONVENIENCE/SUPPORT

	private void addMediaRanges(List<MediaRange> mediaRanges)
    {
		if (mediaRanges == null) return;
		
		for (MediaRange mediaRange : mediaRanges)
		{
			if (!supportedMediaRanges.contains(mediaRange))
			{
				supportedMediaRanges.add(mediaRange);
			}
		}
    }

	private void assignAlias(Alias a)
    {
		for (ResponseProcessor processor : processorsByFormat.values())
		{
			if (Aliasable.class.isAssignableFrom(processor.getSerializer().getClass()))
			{
				((Aliasable)processor.getSerializer()).alias(a.name, a.type);
			}
		}
    }

	private void assignAliases(ResponseProcessor processor)
    {
		if (Aliasable.class.isAssignableFrom(processor.getClass()))
		{
			Aliasable p = (Aliasable) processor;

			for (Alias a : aliases)
			{
				p.alias(a.name, a.type);
			}
		}
    }

	private boolean exceptionOccurredBeforeRouteResolution(String format, Response response)
    {
	    return format == null && response.hasException();
    }

	private String parseFormatFromUrl(String url)
    {
		int queryDelimiterIndex = url.indexOf('?');
		String path = (queryDelimiterIndex > 0 ? url.substring(0, queryDelimiterIndex) : url);
    	int formatDelimiterIndex = path.lastIndexOf('.');
    	return (formatDelimiterIndex > 0 ? path.substring(formatDelimiterIndex + 1) : null);
    }


	// SECTION: INNER CLASS

	private static class Alias
	{
		private String name;
		private Class<?> type;
		
		public Alias(String name, Class<?> type)
		{
			super();
			this.name = name;
			this.type = type;
		}
		
		@Override
		public boolean equals(Object that)
		{
			if (that == null) return false;

			if (this.getClass().isAssignableFrom(that.getClass()))
			{
				return equals((Alias) that);
			}

			return false;
		}
		
		public boolean equals(Alias that)
		{
			if (that == null) return false;

			if (this.name.equals(that.name) && this.type.equals(that.type))
			{
				return true;
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return this.getClass().hashCode() + name.hashCode() + type.hashCode();
		}
	}
}
