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
package org.restexpress;


/**
 * Constants for built-in RestExpress parameters on routes.
 * 
 * @author toddf
 * @since Jan 19, 2011
 */
public abstract class Parameters
{
	public static final class Cache
	{
		public static final String MAX_AGE = "max.age";
	}

	public static final class Query
	{
		// This allows a POST request to function as another 'effective' HTTP method.
		public static final String METHOD_TUNNEL = "_method";

		// Specify the response format (json, xml, etc.) via the query string instead of the URL path.
		public static final String FORMAT = "format";

		// Force the service to return a 200 OK response.
		public static final String IGNORE_HTTP_STATUS = "_ignore_http_status";
	}
	
	private Parameters()
	{
		// prevents instantiation.
	}
}
