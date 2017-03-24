/*
    Copyright 2012, Strategic Gains, Inc.

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
package org.restexpress.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.restexpress.ContentType;
import org.restexpress.serialization.AliasingSerializationProcessor;

/**
 * @author toddf
 * @since May 31, 2012
 */
public class RouteDefaults
{
	private String defaultFormat = ContentType.JSON;
	private Map<String, Class<?>> xmlAliases = new HashMap<String, Class<?>>();
	private String baseUrl;

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public String getDefaultFormat()
	{
		return defaultFormat;
	}

	public void setDefaultFormat(String defaultFormat)
	{
		this.defaultFormat = defaultFormat;
	}
	
	public void addXmlAlias(String elementName, Class<?> classToAlias)
	{
		xmlAliases.put(elementName, classToAlias);
	}

	public void setXmlAliases(AliasingSerializationProcessor processor)
	{
		for (Entry<String, Class<?>> entry : xmlAliases.entrySet())
		{
			processor.alias(entry.getKey(), entry.getValue());
		}
	}
}
