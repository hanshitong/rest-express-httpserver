/*
    Copyright 2013, Strategic Gains, Inc.

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
package org.restexpress.serialization;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.response.ResponseWrapper;

/**
 * @author toddf
 * @since Jul 18, 2013
 */
public interface SerializationProvider
extends Aliasable
{
	public void add(SerializationProcessor processor, ResponseWrapper wrapper);
	public void add(SerializationProcessor processor, ResponseWrapper wrapper, boolean isDefault);
	public void setDefaultFormat(String format);
	public SerializationSettings resolveRequest(Request request);
    public SerializationSettings resolveResponse(Request request, Response response, boolean shouldForce);
}
