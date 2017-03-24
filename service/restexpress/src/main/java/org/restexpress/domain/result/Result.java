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


/**
 * Generic JSEND-style wrapper for responses.
 * 
 * @author toddf
 * @since Jan 11, 2011
 */
public abstract class Result
{
	private Integer code;
	private String status;

	public Result()
	{
	}

	public Result(Integer responseCode, String status)
	{
		super();
		this.code = responseCode;
		this.status = status;
	}

	public Integer getCode()
	{
		return code;
	}

	public String getStatus()
    {
    	return status;
    }
}
