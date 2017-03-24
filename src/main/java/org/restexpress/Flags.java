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
 * Constants for built-in RestExpress flag values on routes.
 * 
 * @author toddf
 * @since Jan 19, 2011
 */
public abstract class Flags
{
	public static final class Cache
	{
		public static final String DONT_CACHE = "no.caching";
	}
	
	/**
	 * Use these flags on routes where the security (e.g. preprocessors) is altered.
	 * For instance, when all routes are behind authentication and authorization,
	 * some routes may be made public (such as OAuth2 authentication routes).
	 *  
	 * @author toddf
	 * @since Mar 1, 2013
	 */
	public static final class Auth
	{
		public static final String PUBLIC_ROUTE = "not.secured";
		public static final String NO_AUTHENTICATION = "no.authentication";
		public static final String NO_AUTHORIZATION = "no.authorization";
	}
	
	private Flags()
	{
		// prevents instantiation.
	}
}
