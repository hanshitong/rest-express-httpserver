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

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author toddf
 * @since Nov 20, 2009
 */
public class BadRequestException
extends ServiceException
{
    private static final long serialVersionUID = 1322585725650252682L;

	public BadRequestException()
	{
		super(HttpResponseStatus.BAD_REQUEST);
	}

	/**
	 * @param message
	 */
	public BadRequestException(String message)
	{
		super(HttpResponseStatus.BAD_REQUEST, message);
	}

	/**
	 * @param cause
	 */
	public BadRequestException(Throwable cause)
	{
		super(HttpResponseStatus.BAD_REQUEST, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BadRequestException(String message, Throwable cause)
	{
		super(HttpResponseStatus.BAD_REQUEST, message, cause);
	}
}
