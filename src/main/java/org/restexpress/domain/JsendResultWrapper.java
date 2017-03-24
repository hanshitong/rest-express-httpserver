/*
    Copyright 2011, Strategic Gains, Inc.

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
package org.restexpress.domain;

import org.restexpress.Response;
import org.restexpress.exception.ExceptionUtils;
import org.restexpress.exception.ServiceException;

/**
 * Generic JSEND-style wrapper for responses.  Differs from the JSEND recommendation as follows:</br>
 * <p/>
 * 1. Always includes the HTTP response status code.<br/>
 * 2. Error status illustrates a non-2xx and non-500 response (e.g. validation errors causing a 400, Bad Request).</br>
 * 3. Fail status is essentially a 500 (internal server) error.<br/>
 * 
 * @author toddf
 * @since Jan 11, 2011
 */
public class JsendResultWrapper
{
	private static final String STATUS_SUCCESS = "success";
	private static final String STATUS_ERROR = "error";
	private static final String STATUS_FAIL = "fail";

	private int code;
	private String status;
	private String message;
	private Object data;

	public JsendResultWrapper(int httpResponseCode, String status, String errorMessage, Object data)
	{
		super();
		this.code = httpResponseCode;
		this.status = status;
		this.message = errorMessage;
		this.data = data;
	}

	public int getCode()
    {
    	return code;
    }
	
	public String getMessage()
	{
		return message;
	}

	public String getStatus()
    {
    	return status;
    }

	public Object getData()
    {
    	return data;
    }
	
	
	// SECTION: FACTORY
	
	public static JsendResultWrapper fromResponse(Response response)
	{
		if (response.hasException())
		{
			Throwable exception = response.getException();
			Throwable rootCause = ExceptionUtils.findRootCause(exception);
			String message = (rootCause != null ? rootCause.getMessage() : exception.getMessage());
			String causeName = (rootCause != null ? rootCause.getClass().getSimpleName() : exception.getClass().getSimpleName());

			if (ServiceException.isAssignableFrom(exception))
			{
				return new JsendResultWrapper(response.getResponseStatus().code(), STATUS_ERROR, message, causeName);
			}

			return new JsendResultWrapper(response.getResponseStatus().code(), STATUS_FAIL, message, causeName);
		}
		else
		{
			int code = response.getResponseStatus().code();

			if (code >= 400 && code < 500)
			{
				return new JsendResultWrapper(response.getResponseStatus().code(), STATUS_ERROR, null, response.getBody());
			}

			if (code >= 500 && code < 600)
			{
				return new JsendResultWrapper(response.getResponseStatus().code(), STATUS_FAIL, null, response.getBody());
			}
		}

		return new JsendResultWrapper(response.getResponseStatus().code(), STATUS_SUCCESS, null, response.getBody());
	}
}
