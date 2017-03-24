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
package org.restexpress.response;

import org.restexpress.Response;
import org.restexpress.domain.ErrorResult;

/**
 * @author toddf
 * @since Oct 8, 2013
 */
public class ErrorResponseWrapper
implements ResponseWrapper
{
	@Override
	public Object wrap(Response response)
	{
		if (addsBodyContent(response))
		{
			return ErrorResult.fromResponse(response);
		}
		
		return response.getBody();
	}

	@Override
	public boolean addsBodyContent(Response response)
	{
		if (response.hasException())
		{
			return true;
		}

		int code = response.getResponseStatus().code();

		if (code >= 400 && code < 600)
		{
			return true;
		}
		
		return false;
	}
}
