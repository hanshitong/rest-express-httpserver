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
package org.restexpress.domain.result;

import org.restexpress.exception.ServiceException;

/**
 * An error result.  In essence, equivalent to a non-2xx error, but not including 500 (internal server) errors.
 *
 * @author toddf
 * @since Mar 22, 2011
 */
public class ErrorResult
extends MessageResult
{
	private static final String STATUS_ERROR = "error";

	public ErrorResult()
	{
	}

    public ErrorResult(Integer httpStatusResponseCode, ServiceException exception)
    {
	    super(httpStatusResponseCode, STATUS_ERROR, exception.getMessage());
    }
}
