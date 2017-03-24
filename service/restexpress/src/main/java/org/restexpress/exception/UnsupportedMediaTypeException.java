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
package org.restexpress.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author toddf
 * @since Jul 18, 2013
 */
public class UnsupportedMediaTypeException
extends ServiceException
{
    private static final long serialVersionUID = -7886837335939319210L;
	private static final HttpResponseStatus STATUS = HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE;

	public UnsupportedMediaTypeException()
    {
	    super(STATUS);
    }

	public UnsupportedMediaTypeException(String message, Throwable cause)
    {
	    super(STATUS, message, cause);
    }

	public UnsupportedMediaTypeException(String message)
    {
	    super(STATUS, message);
    }

	public UnsupportedMediaTypeException(Throwable cause)
    {
	    super(STATUS, cause);
    }
}
