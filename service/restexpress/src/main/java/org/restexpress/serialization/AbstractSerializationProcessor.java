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
import java.util.Collections;
import java.util.List;

import org.restexpress.contenttype.MediaRange;
import org.restexpress.contenttype.MediaTypeParser;

/**
 * @author toddf
 * @since Jul 18, 2013
 */
public abstract class AbstractSerializationProcessor
implements SerializationProcessor
{
	private List<String> supportedFormats = new ArrayList<String>();
	private List<MediaRange> supportedMediaRanges = new ArrayList<MediaRange>();

	public AbstractSerializationProcessor()
	{
		super();
	}

	public AbstractSerializationProcessor(List<String> supportedFormats, List<MediaRange> supportedMediaRanges)
	{
		super();
		setSupportedFormats(supportedFormats);
		setSupportedMediaRanges(supportedMediaRanges);
	}
	
	public AbstractSerializationProcessor addSupportedFormat(String format)
	{
		if (!supportedFormats.contains(format))
		{
			supportedFormats.add(format);
		}

		return this;
	}

	@Override
    public List<String> getSupportedFormats()
    {
		return Collections.unmodifiableList(supportedFormats);
    }

	public void setSupportedFormats(List<String> supportedFormats)
	{
		this.supportedFormats.clear();
		this.supportedFormats.addAll(supportedFormats);
	}

	/**
	 * Parse a media type string and add all the parse results to the supported media types
	 * of this SerializationProcessor.
	 * 
	 * @param mediaTypes a Media Types string, containing possibly more-than one media type
	 */
	public void addSupportedMediaTypes(String mediaTypes)
	{
		addSupportedMediaRanges(MediaTypeParser.parse(mediaTypes));
	}

	public void addSupportedMediaRange(MediaRange mediaRange)
	{
		if (!supportedMediaRanges.contains(mediaRange))
		{
			supportedMediaRanges.add(mediaRange);
		}
	}

	public void addSupportedMediaRanges(List<MediaRange> mediaRanges)
	{
		for (MediaRange mediaRange : mediaRanges)
		{
			addSupportedMediaRange(mediaRange);
		}
	}

	@Override
    public List<MediaRange> getSupportedMediaRanges()
    {
	    return supportedMediaRanges;
    }

	public void setSupportedMediaRanges(List<MediaRange> mediaRanges)
	{
		supportedMediaRanges.clear();
		supportedMediaRanges.addAll(mediaRanges);
	}
}
