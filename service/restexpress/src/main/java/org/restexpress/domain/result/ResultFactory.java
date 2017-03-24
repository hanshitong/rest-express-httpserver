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

import org.restexpress.Response;
import org.restexpress.exception.ServiceException;

/**
 * Generates JSEND-style results for a given response.
 * 
 * @author toddf
 * @since Jan 11, 2011
 */
public class ResultFactory
{
	public ResultFactory()
	{
		super();
	}


	// SECTION: FACTORY

	public Result fromResponse(Response response)
	{
		Integer httpResponseCode = response.getResponseStatus().code();

		if (!response.hasException())
		{
			return new SuccessResult(httpResponseCode, response.getBody());
		}

		Throwable exception = response.getException();

		if (ServiceException.isAssignableFrom(exception))
		{
			return new ErrorResult(httpResponseCode, (ServiceException) exception);
		}

		return new FailResult(httpResponseCode, exception);
	}
}
