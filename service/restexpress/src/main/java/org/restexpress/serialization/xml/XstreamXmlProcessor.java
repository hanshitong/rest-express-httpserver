/*
 * Copyright 2010, Strategic Gains, Inc.
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

package org.restexpress.serialization.xml;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.restexpress.ContentType;
import org.restexpress.Format;
import org.restexpress.common.util.StringUtils;
import org.restexpress.domain.JsendResultWrapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * The operation of these SerializationProcessor methods MUST match the behavior
 * of those in the DefaultJsonProcessor (and any other serialization processors).
 * 
 * @author toddf
 * @since Mar 16, 2010
 */
public class XstreamXmlProcessor
extends XmlSerializationProcessor
{
	private static final byte[] EMPTY_STRING_BYTES = StringUtils.EMPTY_STRING.getBytes(ContentType.CHARSET);

	private XStream xstream;
	private Map<Class<?>, String> aliases = new HashMap<Class<?>, String>();
	private boolean shouldAutoAlias = true;
	
	public XstreamXmlProcessor()
	{
		this(Format.XML);
	}

	public XstreamXmlProcessor(String format)
	{
		this(new XStream(), format);
		xstream.registerConverter(new XstreamTimestampConverter());
		xstream.alias("list", ArrayList.class);
		xstream.alias("response", JsendResultWrapper.class);
	}

	public XstreamXmlProcessor(XStream xstream, String format)
	{
		super(format);
		this.xstream = xstream;
		shouldAutoAlias = false;
	}
	
	protected XStream getXStream()
	{
		return this.xstream;
	}
	
	
	// SECTION: XML NAME ALIASING

	@Override
	public void alias(String name, Class<?> type)
	{
		xstream.alias(name, type);
	}
	
	public void registerConverter(SingleValueConverter converter)
	{
		xstream.registerConverter(converter);
	}


	// SECTION: SERIALIZATION PROCESSOR

	@Override
	public ByteBuffer serialize(Object object)
	{
		if (object == null)
		{
			return ByteBuffer.wrap(EMPTY_STRING_BYTES);
		}

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		xstream.toXML(object, b);
		return ByteBuffer.wrap(b.toByteArray());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialize(String xml, Class<T> type)
	{
		if (xml == null || xml.trim().isEmpty()) return null;

		if (shouldAutoAlias)
		{
			addAliasIfNecessary(type);
		}

		return (T) xstream.fromXML(xml);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialize(ByteBuf xml, Class<T> type)
	{
		if (!xml.isReadable()) return null;

		return (T) xstream.fromXML(new ByteBufInputStream(xml));
	}

	private void addAliasIfNecessary(Class<?> type)
	{
		if (!aliases.containsKey(type))
		{
			String name = type.getSimpleName().trim();
			
			if ("[]".equals(name) || "".equals(name))
			{
				return;
			}
			
			xstream.alias(name, type);
		}
	}
}
