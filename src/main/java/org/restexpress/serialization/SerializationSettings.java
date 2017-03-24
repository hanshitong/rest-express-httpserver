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

import java.nio.ByteBuffer;

import io.netty.handler.codec.http.HttpHeaders;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.response.ResponseProcessor;

/**
 * Contains the Media-Type and a reference to the appropriate {@link ResponseProcessor}
 * to perform request-body parsing.
 * 
 * @author toddf
 * @since Jul 23, 2013
 */
public class SerializationSettings
{
	private String mediaType;
	private ResponseProcessor processor;
	
	public SerializationSettings(String mediaType, ResponseProcessor processor)
	{
		super();
		this.mediaType = mediaType;
		this.processor = processor;
	}

	public String getMediaType()
    {
		return mediaType;
    }

	public ResponseProcessor getResponseProcessor()
    {
		return processor;
    }
	
	public <T> T deserialize(Request request, Class<T> type)
	{
		return processor.deserialize(request, type);
	}
	
    public ByteBuffer serialize(Response response)
    {
		if (!response.hasHeader(HttpHeaders.Names.CONTENT_TYPE))
		{
			response.setContentType(mediaType);
		}

		return processor.serialize(response);
    }
}
