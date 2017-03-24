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
package org.restexpress.serialization.xml;

import java.util.Arrays;
import java.util.List;

import org.restexpress.ContentType;
import org.restexpress.Format;
import org.restexpress.common.util.StringUtils;
import org.restexpress.contenttype.MediaTypeParser;
import org.restexpress.serialization.AbstractSerializationProcessor;
import org.restexpress.serialization.AliasingSerializationProcessor;

/**
 * @author toddf
 * @since Jul 18, 2013
 */
public abstract class XmlSerializationProcessor
extends AbstractSerializationProcessor
implements AliasingSerializationProcessor
{
	private static final String SUPPORTED_MEDIA_TYPES = StringUtils.join(",",
		ContentType.XML,
		"text/xml; charset=" + ContentType.ENCODING);

	public XmlSerializationProcessor()
	{
		this(Arrays.asList(Format.XML));
	}
	
	public XmlSerializationProcessor(String format)
	{
		this(Arrays.asList(format));
	}

	public XmlSerializationProcessor(List<String> supportedFormats)
	{
		super(supportedFormats, MediaTypeParser.parse(SUPPORTED_MEDIA_TYPES));
	}
}
