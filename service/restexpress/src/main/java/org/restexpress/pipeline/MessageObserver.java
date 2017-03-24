/*
    Copyright 2010, Strategic Gains, Inc.

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
package org.restexpress.pipeline;

import org.restexpress.Request;
import org.restexpress.Response;

/**
 * @author toddf
 * @since Dec 15, 2010
 */
public class MessageObserver
{
	/**
	 * Sent when a message is received, after the request and response are created.
	 * Useful for initiating start timers, etc.
	 * 
     * @param request
     * @param response
     */
    protected void onReceived(Request request, Response response)
    {
	    // default behavior is to do nothing.
    }

	/**
	 * Sent when an exception occurs in a route, but before the response is written.
	 * 
     * @param exception
     * @param request
     * @param response
     */
    protected void onException(Throwable exception, Request request, Response response)
    {
	    // default behavior is to do nothing.
    }

	/**
	 * Sent after a response is successfully written.
	 * 
     * @param request
     * @param response
     */
    protected void onSuccess(Request request, Response response)
    {
	    // default behavior is to do nothing.
    }

	/**
	 * Sent after either an exception or successful response is written from a route.
	 * 
     * @param request
     * @param response
     */
    protected void onComplete(Request request, Response response)
    {
	    // default behavior is to do nothing.
    }
}
