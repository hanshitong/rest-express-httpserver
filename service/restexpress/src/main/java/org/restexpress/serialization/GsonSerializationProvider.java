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

import org.restexpress.response.JsendResponseWrapper;
import org.restexpress.serialization.json.GsonJsonProcessor;
import org.restexpress.serialization.xml.XstreamXmlProcessor;


/**
 * A serialization provider that uses Gson for JSON and XStream for XML serialization/deserialization.
 * 
 * @author toddf
 * @since Jul 18, 2013
 */
public class GsonSerializationProvider
extends AbstractSerializationProvider
{
	public GsonSerializationProvider()
    {
		super();
		add(new GsonJsonProcessor(), new JsendResponseWrapper(), true);
		add(new XstreamXmlProcessor(), new JsendResponseWrapper());
    }
}
