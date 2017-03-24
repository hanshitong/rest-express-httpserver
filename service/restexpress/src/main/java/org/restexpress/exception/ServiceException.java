/*
 * Copyright 2009, Strategic Gains, Inc.
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

package org.restexpress.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.restexpress.Response;

/**
 * @author toddf
 * @since Nov 20, 2009
 */
public class ServiceException
extends RuntimeException
{
	private static final long serialVersionUID = 1810995969641082808L;
	private static final HttpResponseStatus STATUS = HttpResponseStatus.INTERNAL_SERVER_ERROR;

	private UUID id;
	private HttpResponseStatus httpStatus;
	private Map<String, String> headers;

	
	// SECTION: CONSTRUCTORS

	public ServiceException()
	{
		this((String) null);
	}

	public ServiceException(HttpResponseStatus status)
	{
		this(status, (String) null);
	}

	/**
	 * @param message
	 */
	public ServiceException(String message)
	{
		this(STATUS, message);
	}

	public ServiceException(HttpResponseStatus status, String message)
	{
		super(message);
		initialize(status);
	}

	/**
	 * @param cause
	 */
	public ServiceException(Throwable cause)
	{
		this(STATUS, cause);
	}

	public ServiceException(HttpResponseStatus status, Throwable cause)
	{
		super(cause);
		initialize(status);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceException(String message, Throwable cause)
	{
		this(STATUS, message, cause);
	}

	/**
	 * @param internalServerError
	 * @param message
	 * @param cause
	 */
	public ServiceException(HttpResponseStatus status, String message, Throwable cause)
	{
		super(message, cause);
		initialize(status);
	}

	
	// SECTION: ACCESSORS - PUBLIC

	public HttpResponseStatus getHttpStatus()
	{
		return httpStatus;
	}

	public UUID getId()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return String.format("%s (%s): %s, %s", 
			getClass().getSimpleName(),
			getId().toString(),
			getHttpStatus().toString(),
			getLocalizedMessage());
	}

	/**
	 * Adds headers, etc. to the reponse, if required for the exception.
	 */
	public void augmentResponse(Response response)
	{
		if (hasHeaders())
		{
			for (Entry<String, String> header : headers.entrySet())
			{
				response.addHeader(header.getKey(), header.getValue());
			}
		}
	}

	public void setHeader(String name, String value)
	{
		if (headers == null)
		{
			headers = new HashMap<String, String>();
		}
		
		headers.put(name, value);
	}
	
	public boolean hasHeaders()
	{
		return (headers != null && !headers.isEmpty());
	}

	public String getHeader(String name)
	{
		return (headers == null ? null : headers.get(name));
	}

	
	// SECTION: MUTATORS - PRIVATE

	private void initialize(HttpResponseStatus status)
    {
	    setHttpStatus(status);
		initializeId();
    }

	private void setHttpStatus(HttpResponseStatus status)
	{
		this.httpStatus = status;
	}
	
	private void initializeId()
	{
		this.id = UUID.randomUUID();
	}

	
	// SECTION: CONVENIENCE - STATIC

	public static boolean isAssignableFrom(Throwable exception)
	{
		return ServiceException.class.isAssignableFrom(exception.getClass());
	}

}
