/*
    Copyright 2012, Strategic Gains, Inc.

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
package org.restexpress.response;

import java.nio.ByteBuffer;
import java.util.List;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.contenttype.MediaRange;
import org.restexpress.serialization.SerializationProcessor;

/**
 * @author toddf
 * @since May 14, 2012
 */
public class ResponseProcessor
{
	private SerializationProcessor serializer;
	private ResponseWrapper wrapper;

	public ResponseProcessor(SerializationProcessor serializer, ResponseWrapper wrapper)
	{
		super();
		this.serializer = serializer;
		this.wrapper = wrapper;
	}
	
	public SerializationProcessor getSerializer()
	{
		return serializer;
	}
	
	public ResponseWrapper getWrapper()
	{
		return wrapper;
	}
	
	public List<MediaRange> getSupportedMediaRanges()
	{
		return serializer.getSupportedMediaRanges();
	}
	
	public <T> T deserialize(Request request, Class<T> type)
	{
		return serializer.deserialize(request.getBody(), type);
	}

	public ByteBuffer serialize(Response response)
	{
		Object wrapped = wrapper.wrap(response);
		
		if (wrapped != null)
		{
			return serializer.serialize(wrapped);
		}
		
		return null;
	}
}
