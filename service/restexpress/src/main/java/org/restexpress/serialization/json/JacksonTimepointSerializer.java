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
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.strategicgains.util.date.DateAdapter;
import com.strategicgains.util.date.TimestampAdapter;

/**
 * @author toddf
 * @since Dec 16, 2010
 */
public class JacksonTimepointSerializer
extends JsonSerializer<Date>
{
	private DateAdapter adapter;

	public JacksonTimepointSerializer()
	{
		//modify by hanst,使用符合我们自己习惯的日期输出格式
		this(new ZhCnTimeAdapter());
		//this(new TimestampAdapter());
	}

	public JacksonTimepointSerializer(DateAdapter adapter)
	{
		super();
		this.adapter = adapter;
	}

	@Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider sp)
    throws IOException, JsonProcessingException
    {
		gen.writeString(adapter.format(date));
    }
}
