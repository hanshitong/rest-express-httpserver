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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.pipeline.MessageObserver;

/**
 * Provides simple System.out.println() details about basic timing.
 * 
 * @author toddf
 * @since Dec 16, 2010
 */
public class SimpleConsoleLogMessageObserver
extends MessageObserver
{
	// SECTION: INSTANCE VARIABLES

	private Map<String, Timer> timers = new ConcurrentHashMap<String, Timer>();

	
	// SECTION: MESSAGE OBSERVER

	@Override
    protected void onReceived(Request request, Response response)
    {
		timers.put(request.getCorrelationId(), new Timer());
    }

	@Override
    protected void onException(Throwable exception, Request request, Response response)
    {
		System.out.println(request.getEffectiveHttpMethod().toString() 
			+ " " 
			+ request.getUrl()
			+ " threw exception: "
			+ exception.getClass().getSimpleName());
		exception.printStackTrace();
    }

	@Override
    protected void onSuccess(Request request, Response response)
    {
    }

	@Override
    protected void onComplete(Request request, Response response)
    {
		Timer timer = timers.remove(request.getCorrelationId());
		if (timer != null) timer.stop();

		StringBuffer sb = new StringBuffer(request.getEffectiveHttpMethod().toString());
		sb.append(" ");
		sb.append(request.getUrl());
		
		if (timer != null)
		{
			sb.append(" responded with ");
			sb.append(response.getResponseStatus().toString());
			sb.append(" in ");
			sb.append(timer.toString());
		}
		else
		{
			sb.append(" responded with ");
			sb.append(response.getResponseStatus().toString());
			sb.append(" (no timer found)");
		}
		
		System.out.println(sb.toString());
    }
	
	
	// SECTION: INNER CLASS
	
	private static class Timer
	{
		private long startMillis = 0;
		private long stopMillis = 0;
		
		public Timer()
		{
			super();
			this.startMillis = System.currentTimeMillis();
		}
		
		public void stop()
		{
			this.stopMillis = System.currentTimeMillis();
		}
		
		public String toString()
		{
			long stopTime = (stopMillis == 0 ? System.currentTimeMillis() : stopMillis);

			return String.valueOf(stopTime - startMillis) + "ms";
		}
	}
}
