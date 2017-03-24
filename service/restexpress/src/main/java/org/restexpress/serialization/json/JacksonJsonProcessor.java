/*
 * Copyright 2010-2013, Strategic Gains, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.restexpress.serialization.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.restexpress.ContentType;
import org.restexpress.Format;
import org.restexpress.common.util.StringUtils;
import org.restexpress.serialization.DeserializationException;
import org.restexpress.serialization.SerializationException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.strategicgains.util.date.DateAdapterConstants;

/**
 * A SerializationProcessor to handle JSON input/output. It anticipates ISO
 * 8601-compatible time points for date instances and outputs dates as ISO 8601
 * time points.
 * <p/>
 * This serialization processor also, by default, outbound HTML-encodes all strings to
 * help protect from cross-site scripting (XSS) attacks. The default behavior may be
 * turned off by calling new JacksonJsonProcessor(false) or using your own SimpleModule
 * or ObjectMapper instance.
 * 
 * @author toddf
 * @since Mar 16, 2010
 */
public class JacksonJsonProcessor
extends JsonSerializationProcessor
{
	private static final byte[] EMPTY_STRING_BYTES = StringUtils.EMPTY_STRING.getBytes(ContentType.CHARSET);
	private ObjectMapper mapper;
	private boolean shouldOutboundEncode;

	public JacksonJsonProcessor()
	{
		this(true);
	}

	public JacksonJsonProcessor(boolean shouldOutboundEncode)
	{
		this(Format.JSON, shouldOutboundEncode);
	}

	public JacksonJsonProcessor(String format)
	{
		this(format, true);
	}

	public JacksonJsonProcessor(String format, boolean shouldOutboundEncode)
	{
		super(format);
		this.shouldOutboundEncode = shouldOutboundEncode;
		SimpleModule module = new SimpleModule();
		initializeModule(module);
	}

	public JacksonJsonProcessor(SimpleModule module)
	{
		initialize(module);
	}

	public JacksonJsonProcessor(ObjectMapper mapper)
	{
		super();
		this.mapper = mapper;
	}

	private void initialize(SimpleModule module)
	{
		this.mapper = new ObjectMapper();
		mapper.registerModule(module);
		initializeMapper(mapper);
	}

	/**
	 * Template method for sub-classes to augment the module with desired
	 * serializers and/or deserializers.  Sub-classes should call super()
	 * to get default settings.
	 * 
	 * @param module a SimpleModule
	 */
	protected void initializeModule(SimpleModule module)
    {
		module
			.addSerializer(Date.class, new JacksonTimepointSerializer())
			.addDeserializer(Date.class, new JacksonTimepointDeserializer());

		if (shouldOutboundEncode)
		{
			module.addSerializer(String.class, new JacksonEncodingStringSerializer());
		}

		initialize(module);
    }

	/**
	 * Template method for sub-classes to augment the mapper with desired
	 * settings.  Sub-classes should call super() to get default settings.
	 * 
	 * @param module a SimpleModule
	 */
	protected void initializeMapper(ObjectMapper mapper)
    {
		mapper
//			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

			// Ignore additional/unknown properties in a payload.
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			
			// Only serialize populated properties (do no serialize nulls)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			
			// Use fields directly.
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			
			// Ignore accessor and mutator methods (use fields per above).
			.setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
			.setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
			.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE)
			
			// Set default date output format.
//			.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			.setDateFormat(new SimpleDateFormat(DateAdapterConstants.TIME_POINT_OUTPUT_FORMAT));
    }

	@Override
	public <T> T deserialize(String string, Class<T> type)
	{
		try
		{
			return (string == null || string.trim().isEmpty() ? null : mapper.readValue(string, type));
		}
		catch (JsonProcessingException e)
		{
			throw new DeserializationException(e);
		}
		catch (IOException e)
		{
			throw new DeserializationException(e);
		}
	}

	@Override
	public <T> T deserialize(ByteBuf buffer, Class<T> type)
	{
		try
		{
			
			return (buffer == null || buffer.readableBytes() == 0 ? null : mapper.readValue(new InputStreamReader(new ByteBufInputStream(buffer), ContentType.CHARSET), type));
		}
		catch (JsonProcessingException e)
		{
			throw new DeserializationException(e);
		}
		catch (IOException e)
		{
			throw new DeserializationException(e);
		}
	}

	@Override
	public ByteBuffer serialize(Object object)
	{
		try
		{
			if (object == null)
			{
				return ByteBuffer.wrap(EMPTY_STRING_BYTES);
			}


			ByteArrayOutputStream b = new ByteArrayOutputStream();
			mapper.writeValue(b, object);
			return ByteBuffer.wrap(b.toByteArray());
		}
		catch (IOException e)
		{
			throw new SerializationException(e);
		}
	}
}
