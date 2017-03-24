/*
    Copyright 2010-2013, Strategic Gains, Inc.

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
import java.text.ParseException;
import java.util.Date;

import org.restexpress.serialization.DeserializationException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.strategicgains.util.date.DateAdapter;
import com.strategicgains.util.date.Iso8601TimepointAdapter;

/**
 * @author toddf
 * @since Dec 16, 2010
 */
public class JacksonTimepointDeserializer
extends JsonDeserializer<Date>
{
	private DateAdapter adapter;

	public JacksonTimepointDeserializer()
	{
		this(new Iso8601TimepointAdapter());
	}

	public JacksonTimepointDeserializer(DateAdapter adapter)
	{
		super();
		this.adapter = adapter;
	}

	@Override
    public Date deserialize(JsonParser parser, DeserializationContext context)
    throws IOException, JsonProcessingException
    {
		try
        {
	        return adapter.parse(parser.getText());
        }
        catch (ParseException e)
        {
        	throw new DeserializationException(e);
        }
    }
}
