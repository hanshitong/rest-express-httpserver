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
package org.restexpress.exception;

/**
 * @author toddf
 * @since Apr 8, 2011
 */
public class ExceptionUtils
{
	// Prevents instantiation.
	private ExceptionUtils() {}
	
	/**
	 * Traverses throwable.getCause() up the chain until the root cause is found.
	 * 
	 * @param throwable
	 * @return the root cause.  Never null, unless throwable is null.
	 */
	public static Throwable findRootCause(Throwable throwable)
	{
		Throwable cause = throwable;
		Throwable rootCause = null;
		
		while (cause != null)
		{
			rootCause = cause;
			cause = cause.getCause();
		}
		
		return rootCause;
	}
}
