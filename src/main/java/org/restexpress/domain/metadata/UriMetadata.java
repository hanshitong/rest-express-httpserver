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
package org.restexpress.domain.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author toddf
 * @since Feb 14, 2011
 */
public class UriMetadata
{
	private String pattern;
	private List<String> parameters;

	public UriMetadata(String uri)
	{
		super();
		this.pattern = uri;
	}

	public String getPattern()
	{
		return pattern;
	}
	
	public List<String> getParameters()
	{
		return parameters;
	}
	
	public void addParameter(String parameter)
	{
		if (parameters == null)
		{
			parameters = new ArrayList<String>();
		}

		if (!parameters.contains(parameter))
		{
			parameters.add(parameter);
		}
	}
	
	public void addAllParameters(Collection<String> parameters)
	{
		if (parameters == null) return;

		for (String parameter : parameters)
		{
			addParameter(parameter);
		}
	}
}
