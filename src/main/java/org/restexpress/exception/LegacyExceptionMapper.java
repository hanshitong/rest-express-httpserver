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
package org.restexpress.exception;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a user-defined mapping from Throwables to ServiceExceptions so clients can utilize Libraries that
 * throw checked exceptions and map those into a ServiceException that will return an HTTP status code.
 * 
 * @author toddf
 * @since Oct 13, 2010
 * @deprecated Use DefaultExceptionMapper
 */
public class LegacyExceptionMapper
implements ExceptionMapping
{
	private Map<Class<? extends Throwable>, Class<? extends ServiceException>> exceptions =
		new HashMap<Class<? extends Throwable>, Class<? extends ServiceException>>();
	
	public <T extends Throwable, U extends ServiceException> void map(Class<T> inExceptionClass, Class<U> outExceptionClass)
	{
		exceptions.put(inExceptionClass, outExceptionClass);
	}

	public ServiceException getExceptionFor(Throwable throwable)
	{
		Class<?> mapped = exceptions.get(throwable.getClass());
		
		if (mapped != null)
		{
			try
            {
	            Constructor<?> constructor = mapped.getConstructor(String.class, Throwable.class);
	            return (ServiceException) constructor.newInstance(throwable.getMessage(), throwable);
            }
            catch (Exception e)
            {
	            e.printStackTrace();
            }
		}
		
		return null;
	}
}
