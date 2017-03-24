/*
 * Copyright 2010-2012, Strategic Gains, Inc.
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

import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.restexpress.ContentType;
import org.restexpress.common.util.StringUtils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.strategicgains.util.date.DateAdapterConstants;

/**
 * A SerializationProcessor to handle JSON input/output using GSON. It anticipates ISO
 * 8601-compatible time points for date instances and outputs dates as ISO 8601
 * time points.
 * <p/>
 * This serialization processor also, by default, outbound HTML-encodes all strings to
 * help protect from cross-site scripting (XSS) attacks. The default behavior may be
 * turned off by calling new GsonJsonProcessor(false) or using your own Gson instance.
 * 
 * @author toddf
 * @since Mar 16, 2010
 */
public class GsonJsonProcessor
extends JsonSerializationProcessor
{
	private static final byte[] EMPTY_STRING_BYTES = StringUtils.EMPTY_STRING.getBytes(ContentType.CHARSET);

	private Gson gson;

	public GsonJsonProcessor()
	{
		this(true);
	}

	public GsonJsonProcessor(boolean shouldOutboundEncode)
	{
		super();
		GsonBuilder builder = new GsonBuilder()
		    .disableHtmlEscaping()
		    .registerTypeAdapter(Date.class, new GsonTimepointSerializer())
		    .setDateFormat(DateAdapterConstants.TIMESTAMP_OUTPUT_FORMAT)
		    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

		if (shouldOutboundEncode)
		{
			builder.registerTypeAdapter(String.class, new GsonEncodingStringSerializer());
		}

		gson = builder.create();
	}

	public GsonJsonProcessor(Gson gson)
	{
		super();
		this.gson = gson;
	}


	// SECTION: SERIALIZATION PROCESSOR

	@Override
	public <T> T deserialize(String string, Class<T> type)
	{
		return gson.fromJson((String) string, type);
	}

	@Override
	public <T> T deserialize(ByteBuf buffer, Class<T> type)
	{
    	return gson.fromJson(new InputStreamReader(new ByteBufInputStream(buffer), ContentType.CHARSET), type);
	}

	@Override
	public ByteBuffer serialize(Object object)
	{
		if (object == null)
		{
			return ByteBuffer.wrap(EMPTY_STRING_BYTES);
		}

		// TODO: Determine why this doesn't work...
//		ByteArrayOutputStream b = new ByteArrayOutputStream();
//		gson.toJson(object, new BufferedWriter(new OutputStreamWriter(b, ContentType.CHARSET)));
//		return ByteBuffer.wrap(b.toByteArray());
		return ByteBuffer.wrap(gson.toJson(object).getBytes(ContentType.CHARSET));
	}
}
