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
package org.restexpress.response;

import org.restexpress.Response;

/**
 * Leaves the response alone, returning it without wrapping it at all, unless
 * there is an exception. If there is an exception, the exception is wrapped in
 * a serializable Error instance.
 * 
 * @author toddf
 * @since Feb 10, 2011
 */
public class RawResponseWrapper
implements ResponseWrapper
{
	@Override
	public Object wrap(Response response)
	{
		if (!response.hasException())
		{
			return response.getBody();
		}

		return response.getException().getMessage();
	}

	@Override
	public boolean addsBodyContent(Response response)
	{
		return false;
	}
}
