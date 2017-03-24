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
package org.restexpress.plugin;

import org.restexpress.RestExpress;

/**
 * @author toddf
 * @since Jul 20, 2011
 */
public interface Plugin
{
	/**
	 * Called to register this plugin with the RestExpress server.
	 * Within this method, pre/post-processors can be created, as
	 * well as routes injected.
	 * 
	 * @param server
	 * @return a Plugin reference so plugin commands can be chained.
	 */
	public Plugin register(RestExpress server);
	
	/**
	 * Called during RestExpress.bind(), after all resources have been allocated
	 * to enable this plugin to access such details as route metadata, etc.
	 * 
	 * @param server the fully-bound RestExpress server.
	 */
	public void bind(RestExpress server);
	
	/**
	 * Called during RestExpress.shutdown() to release resources held by this plugin.
	 */
	public void shutdown(RestExpress server);
}
