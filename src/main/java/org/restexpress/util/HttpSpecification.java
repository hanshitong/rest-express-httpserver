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
package org.restexpress.util;

import static io.netty.handler.codec.http.HttpHeaders.Names.ALLOW;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.restexpress.Response;
import org.restexpress.exception.HttpSpecificationException;

/**
 * Verifies the response contents prior to writing it to the output stream to
 * ensure that it conforms to the HTTP 1.1. specification.
 * 
 * @author toddf
 * @since Mar 3, 2011
 */
public final class HttpSpecification
{
	private HttpSpecification()
	{
		// prevents instantiation
	}

	// SECTION: SPECIFICATION ENFORCEMENT

	public static void enforce(Response response)
	{
		int status = response.getResponseStatus().code();

		if (is1xx(status))
		{
			enforce1xx(response);
		}
		else
			switch (status)
			{
				case 204:
					enforce204(response);
					break;
				case 304:
					enforce304(response);
					break;
				case 405:
					enforce405(response);
					break;
				default:
					break;
			}
	}

	// SECTION: SPECIFICATION TESTING

	/**
	 * There must be a Content-Type, except when the Status is 1xx, 204 or 304,
	 * in which case there must be none given.
	 * 
	 * @param response
	 * @return
	 */
	public static boolean isContentTypeAllowed(Response response)
	{
		return isContentAllowed(response);
	}

	/**
	 * There must not be a Content-Length header when the Status is 1xx, 204 or
	 * 304.
	 * 
	 * @param response
	 * @return
	 */
	public static boolean isContentLengthAllowed(Response response)
	{
		return isContentAllowed(response);
	}

	public static boolean isContentAllowed(Response response)
	{
		HttpResponseStatus status = response.getResponseStatus();
		return !(HttpResponseStatus.NO_CONTENT.equals(status)
		    || HttpResponseStatus.NOT_MODIFIED.equals(status)
		    || is1xx(status.code()));
	}

	// SECTION: UTILITY - PRIVATE

	private static boolean is1xx(int status)
	{
		return ((100 <= status) && (status <= 199));
	}

	/**
	 * Responses 1xx, 204 (No Content) and 304 (Not Modified) must not return a
	 * response body.
	 * 
	 * @param response
	 */
	private static void enforce1xx(Response response)
	{
		ensureNoBody(response);
		ensureNoContentType(response);
		ensureNoContentLength(response);
	}

	/**
	 * Responses 1xx, 204 (No Content) and 304 (Not Modified) must not return a
	 * response body.
	 * 
	 * @param response
	 */
	private static void enforce204(Response response)
	{
		ensureNoBody(response);
		ensureNoContentType(response);
		ensureNoContentLength(response);
	}

	/**
	 * Responses 1xx, 204 (No Content) and 304 (Not Modified) must not return a
	 * response body.
	 * 
	 * @param response
	 */
	private static void enforce304(Response response)
	{
		ensureNoBody(response);
		ensureNoContentType(response);
		ensureNoContentLength(response);
	}

	private static void enforce405(Response response)
	{
		ensureAllowHeader(response);
	}

	private static void ensureNoBody(Response response)
	{
		if (response.hasBody())
		{
			throw new HttpSpecificationException("HTTP 1.1 specification: must not contain response body with status: " + response.getResponseStatus());
		}
	}

	/**
	 * @param response
	 */
	private static void ensureNoContentLength(Response response)
	{
		if (response.getHeader(CONTENT_LENGTH) != null)
		{
			throw new HttpSpecificationException("HTTP 1.1 specification: must not contain Content-Length header for status: " + response.getResponseStatus());
		}
	}

	/**
	 * @param response
	 */
	private static void ensureNoContentType(Response response)
	{
		if (response.getHeader(CONTENT_TYPE) != null)
		{
			throw new HttpSpecificationException("HTTP 1.1 specification: must not contain Content-Type header for status: " + response.getResponseStatus());
		}
	}

	private static void ensureAllowHeader(Response response)
	{
		if (response.getHeader(ALLOW) == null)
		{
			throw new HttpSpecificationException("HTTP 1.1 specification: must contain Allow header for status: " + response.getResponseStatus());
		}
	}
}
