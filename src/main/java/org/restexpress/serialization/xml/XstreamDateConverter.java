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
package org.restexpress.serialization.xml;

import java.text.ParseException;
import java.util.Date;

import com.strategicgains.util.date.DateAdapter;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * @author toddf
 * @since Dec 16, 2010
 */
public class XstreamDateConverter
implements SingleValueConverter
{
	private DateAdapter adapter;

	public XstreamDateConverter()
	{
		this(new DateAdapter());
	}

	public XstreamDateConverter(DateAdapter adapter)
	{
		super();
		this.adapter = adapter;
	}

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class aClass)
    {
	    return Date.class.isAssignableFrom(aClass);
    }

    @Override
    public Object fromString(String value)
    {
	    try
        {
	        return adapter.parse(value);
        }
        catch (ParseException e)
        {
	        e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public String toString(Object date)
    {
	    return adapter.format((Date) date);
    }
}
