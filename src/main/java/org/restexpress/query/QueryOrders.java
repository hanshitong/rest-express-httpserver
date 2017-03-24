/*
 * Copyright 2011, Strategic Gains, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restexpress.query;

import java.util.Arrays;
import java.util.List;

import org.restexpress.Request;
import org.restexpress.common.query.QueryOrder;
import org.restexpress.common.util.StringUtils;
import org.restexpress.exception.BadRequestException;

/**
 * A factory for RestExpress-Common QueryOrder instances, parsing them from a Request.
 * 
 * @author toddf
 * @since Apr 12, 2011
 * @see org.restexpress.common.query.QueryOrder
 */
public abstract class QueryOrders
{
	private static final String SORT_HEADER_NAME = "sort";
	private static final String SORT_SEPARATOR = "\\|";

	/**
	 * Create a QueryOrder instance from the RestExpress request.
	 * Assumes all resource properties can be sorted-on.
	 * 
	 * @param request the current request
	 * @return a QueryOrder instance
	 */
	public static QueryOrder parseFrom(Request request)
	{
		return parseFrom(request, (List<String>) null);
	}

	/**
	 * Create a QueryOrder instance from the RestExpress request, setting the
	 * properties on which the resource can be sorted.
	 * 
	 * @param request the current request
	 * @param allowedProperties an array of property names on which the resource can be sorted.
	 * @return a QueryOrder instance
	 */
	public static QueryOrder parseFrom(Request request, String... allowedProperties)
	{
		return parseFrom(request, Arrays.asList(allowedProperties));
	}

	/**
	 * Create a QueryOrder instance from the RestExpress request, setting the
	 * properties on which the resource can be sorted.
	 * 
	 * @param request the current request
	 * @param allowedProperties a list of property names on which the resource can be sorted.
	 * @return a QueryOrder instance
	 */
	public static QueryOrder parseFrom(Request request, List<String> allowedProperties)
	{
		String sortString = request.getHeader(SORT_HEADER_NAME);

		if (sortString == null || sortString.trim().isEmpty())
		{
			return new QueryOrder();
		}
		
		String[] strings = sortString.split(SORT_SEPARATOR);
		enforceAllowedProperties(allowedProperties, strings);

		return new QueryOrder(strings);
	}

	private static void enforceAllowedProperties(List<String> allowedProperties, String[] requestedProperties)
    {
		if (requestedProperties == null) return;

	    if (allowedProperties != null)
		{
			int i = 0;

			while (i < requestedProperties.length)
			{
				String requested = requestedProperties[i++];

				for (String allowed : allowedProperties)
				{
					if (requested.endsWith(allowed))
					{
						return;
					}
				}

				throw new BadRequestException(requested
					+ " is not a supported sort field. Supported sort fields are: "
					+ StringUtils.join(", ", allowedProperties));
			}
		}
    }
}
