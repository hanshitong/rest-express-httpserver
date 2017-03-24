/*
    Copyright 2014, Strategic Gains, Inc.

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
package org.restexpress.serialization.json;

import java.io.IOException;

import org.owasp.encoder.Encode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Output encodes JSON string values to reduce the possibility of XSS (Cross-Site Scripting) attacks.
 * 
 * @author toddf
 * @since Apr 28, 2014
 */
public class JacksonEncodingStringSerializer
extends JsonSerializer<String>
{
	@Override
    public void serialize(String value, JsonGenerator jgen, SerializerProvider provider)
    throws IOException, JsonProcessingException
    {
		jgen.writeString(Encode.forXmlContent(value));
    }
}
