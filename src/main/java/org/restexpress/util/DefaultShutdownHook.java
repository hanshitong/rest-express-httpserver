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
package org.restexpress.util;

import org.restexpress.RestExpress;

/**
 * @author toddf
 * @since Feb 1, 2011
 */
public class DefaultShutdownHook
extends Thread
{
	private RestExpress server;
	
	public DefaultShutdownHook(RestExpress server)
	{
		super();
		this.server = server;
	}

	public void run()
	{
		System.out.println(server.getName() + " server detected JVM shutdown...");
		server.shutdown();
	}
}
