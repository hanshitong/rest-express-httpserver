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


/**
 * Defines an implementation for a user-defined mapping from Throwables to ServiceExceptions so clients
 * can utilize Libraries that throw checked exceptions and map those into a ServiceException that will
 * return an HTTP status code.
 * 
 * @author toddf
 * @since Oct 13, 2010
 */
public interface ExceptionMapping
{
	public <T extends Throwable, U extends ServiceException> void map(Class<T> inExceptionClass, Class<U> outExceptionClass);
	public ServiceException getExceptionFor(Throwable throwable);
}
