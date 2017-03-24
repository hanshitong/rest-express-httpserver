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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restexpress.Request;
import org.restexpress.common.query.QueryRange;
import org.restexpress.exception.BadRequestException;

/**
 * A factory for RestExpress-Common QueryRange instances, parsing them from a Request.
 * 
 * @author toddf
 * @since Apr 11, 2011
 * @see org.restexpress.common.query.QueryRange
 */
public abstract class QueryRanges
{
	// SECTION: CONSTANTS

	private static final String LIMIT_HEADER_NAME = "limit";
	private static final String OFFSET_HEADER_NAME = "offset";
	private static final String RANGE_HEADER_NAME = "Range";
	private static final String ITEMS_HEADER_REGEX = "items=(\\d+)-(\\d+)";
	private static final Pattern ITEMS_HEADER_PATTERN = Pattern.compile(ITEMS_HEADER_REGEX);

	// SECTION: FACTORY

	/**
	 * Create a QueryRange instance from the current RestExpress request,
	 * providing a default maximum offset if the request contains no range
	 * criteria.
	 * 
	 * @param request the current request
	 * @param limit the default limit, used if the request contains no range criteria
	 * @return a QueryRange instance, defaulting to 0 to (limit - 1). Never null.
	 */
	public static QueryRange parseFrom(Request request, int limit)
	{
		QueryRange range = new QueryRange();
		range.setOffset(0l);
		range.setLimit(limit);
		parseInto(request, range);
		return range;
	}

	/**
	 * Create a QueryRange instance from the current RestExpress request.
	 * 
	 * @param request the current request
	 * @return a QueryRange instance. Never null.
	 */
	public static QueryRange parseFrom(Request request)
	{
		QueryRange range = new QueryRange();
		parseInto(request, range);
		return range;
	}

	private static void parseInto(Request request, QueryRange range)
	{
		String limit = request.getHeader(LIMIT_HEADER_NAME);
		String offset = request.getHeader(OFFSET_HEADER_NAME);

		if (!parseLimitAndOffset(limit, offset, range))
		{
			parseRangeHeader(request, range);
		}
	}

	/**
	 * @param limit
	 * @param offset
	 * @param range
	 * @return
	 */
	private static boolean parseLimitAndOffset(String limit, String offset, QueryRange range)
	{
		boolean hasLimit = false;
		boolean hasOffset = false;

		if (limit != null && !limit.trim().isEmpty())
		{
			hasLimit = true;
			range.setLimit(Integer.parseInt(limit));
		}

		if (offset != null && !offset.trim().isEmpty())
		{
			hasOffset = true;
			range.setOffset(Long.parseLong(offset));
		}
		else if (hasLimit)
		{
			range.setOffset(0l);
		}

		if (hasLimit || hasOffset)
		{
			if (!range.isValid())
			{
				throw new BadRequestException("Invalid 'limit' and 'offset' parameters: limit=" + limit + " offset=" + offset);
			}

			return true;
		}

		return false;
	}

	private static void parseRangeHeader(Request request, QueryRange range)
	{
		String rangeHeader = request.getHeader(RANGE_HEADER_NAME);

		if (rangeHeader != null && !rangeHeader.trim().isEmpty())
		{
			Matcher matcher = ITEMS_HEADER_PATTERN.matcher(rangeHeader);

			if (!matcher.matches())
			{
				throw new BadRequestException("Unparseable 'Range' header.  Expecting items=[start]-[end] was: " + rangeHeader);
			}
			
			try
			{
				range.setOffset(Long.parseLong(matcher.group(1)));
				range.setLimitViaEnd(Long.parseLong(matcher.group(2)));
			}
			catch(IllegalArgumentException e)
			{
				throw new BadRequestException("Invalid 'Range' header.  Expecting 'items=[start]-[end]'  was: " + rangeHeader);
			}

			if (!range.isValid())
			{
				throw new BadRequestException("Invalid 'Range' header.  Expecting 'items=[start]-[end]'  was: " + rangeHeader);
			}
		}
	}
}
