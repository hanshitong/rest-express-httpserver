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

import org.restexpress.serialization.Deserializer;


/**
 * Represents 2xx HTTP status responses: success.
 * 
 * @author toddf
 * @since Mar 22, 2011
 */
public class SuccessResult
extends Result
{
	private static final String STATUS_SUCCESS = "success";

	private Object data;

	public SuccessResult()
	{
	}

    public SuccessResult(Integer httpStatusResponseCode, Object data)
    {
	    super(httpStatusResponseCode, STATUS_SUCCESS);
		this.data = data;
    }

	public Object getData()
    {
    	return data;
    }
	
	public static SuccessResult fromJson(String json, Deserializer deserializer, Class<?> dataType)
	{
		String jsonData = null;
		
		if (json.matches("\"data\":\\[.*\\]"))
		{
			jsonData = "array";
		}
		else if (json.matches("\"data\":\\{.*\\}"))
		{
			jsonData = "object";
		}
		else if (json.matches("\"data\".?:.?\".*\""))
		{
			jsonData = "string";
		}

		return new SuccessResult(1, jsonData);
	}
}
