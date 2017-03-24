/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.restexpress.url;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;

/**
 * Splits an HTTP query string into a path string and key-value parameter pairs.
 * This parser is for one time use only. Create a new instance for each URI:
 * 
 * <pre>
 * {@link QueryStringParser} parser = new {@link QueryStringParser}("/hello?recipient=hello%20world&x=1;y=2");
 * assert parser.getPath().equals("/hello");
 * assert parser.getParameters().get("recipient").get(0).equals("hello%20world");
 * assert parser.getParameters().get("x").get(0).equals("1");
 * assert parser.getParameters().get("y").get(0).equals("2");
 * </pre>
 * 
 * This parser can also decode the content of an HTTP POST request whose content
 * type is <tt>application/x-www-form-urlencoded</tt>:
 * 
 * <pre>
 * {@link QueryStringParser} parser = new {@link QueryStringParser}("recipient=hello%20world&x=1;y=2", false);
 * ...
 * </pre>
 * 
 * But will not URL decode the individual values.
 * 
 * <h3>HashDOS vulnerability fix</h3>
 * 
 * As a workaround to the <a href=
 * "http://events.ccc.de/congress/2011/Fahrplan/attachments/2007_28C3_Effective_DoS_on_web_application_platforms.pdf"
 * >HashDOS</a> vulnerability, the decoder limits the maximum number of decoded
 * key-value parameter pairs, up to {@literal 1024} by default, and you can
 * configure it when you construct the decoder by passing an additional integer
 * parameter.
 * <p/>
 * Note that no exception is thrown if the number of parameters is exceeded. This process simply stops adding
 * parameters over the limit.
 * 
 * <p>
 * Based on Netty {@link QueryStringDecoder} with URL decoding removed for
 * selective query-string parameter decoding.
 * </p>
 * 
 * @see QueryStringDecoder
 * @see QueryStringEncoder
 */
public class QueryStringParser
{
	private static final int DEFAULT_MAX_PARAMS = 1024;

	private final String uri;
	private final boolean hasPath;
	private final int maxParams;
	private String path;
	private Map<String, List<String>> params;
	private int nParams;

	/**
	 * Creates a new parser for the given uri.
	 */
	public QueryStringParser(String uri)
	{
		this(uri, true);
	}

	/**
	 * Creates a new decoder that decodes the specified URI encoded in the
	 * specified charset.
	 */
	public QueryStringParser(String uri, boolean hasPath)
	{
		this(uri, hasPath, DEFAULT_MAX_PARAMS);
	}
	
	public QueryStringParser(URI uri)
	{
		this(uri, DEFAULT_MAX_PARAMS);
	}

	/**
	 * Creates a new decoder that decodes the specified URI encoded in the
	 * specified charset.
	 */
	public QueryStringParser(String uri, boolean hasPath, int maxParams)
	{
		if (uri == null)
		{
			throw new NullPointerException("uri");
		}
		if (maxParams <= 0)
		{
			throw new IllegalArgumentException("maxParams: " + maxParams
			    + " (expected: a positive integer)");
		}

		// http://en.wikipedia.org/wiki/Query_string
		this.uri = uri.replace(';', '&');
		this.maxParams = maxParams;
		this.hasPath = hasPath;
	}

	/**
	 * Creates a new decoder that decodes the specified URI encoded in the
	 * specified charset.
	 */
	public QueryStringParser(URI uri, int maxParams)
	{
		if (uri == null)
		{
			throw new NullPointerException("uri");
		}
		if (maxParams <= 0)
		{
			throw new IllegalArgumentException("maxParams: " + maxParams
			    + " (expected: a positive integer)");
		}

		String rawPath = uri.getRawPath();
		if (rawPath != null)
		{
			hasPath = true;
		}
		else
		{
			rawPath = "";
			hasPath = false;
		}
		// Also take care of cut of things like "http://localhost"
		String newUri = rawPath + "?" + uri.getRawQuery();

		// http://en.wikipedia.org/wiki/Query_string
		this.uri = newUri.replace(';', '&');
		this.maxParams = maxParams;

	}

	/**
	 * Returns the decoded path string of the URI.
	 */
	public String getPath()
	{
		if (path == null)
		{
			if (!hasPath)
			{
				return path = "";
			}

			int pathEndPos = uri.indexOf('?');
			if (pathEndPos < 0)
			{
				path = uri;
			}
			else
			{
				return path = uri.substring(0, pathEndPos);
			}
		}
		return path;
	}

	/**
	 * Returns the decoded key-value parameter pairs of the URI.
	 */
	public Map<String, List<String>> getParameters()
	{
		if (params == null)
		{
			if (hasPath)
			{
				int pathLength = getPath().length();
				if (uri.length() == pathLength)
				{
					return Collections.emptyMap();
				}
				parseParams(uri.substring(pathLength + 1));
			}
			else
			{
				if (uri.length() == 0)
				{
					return Collections.emptyMap();
				}
				parseParams(uri);
			}
		}
		return params;
	}

	private void parseParams(String s)
	{
		Map<String, List<String>> params = this.params = new LinkedHashMap<String, List<String>>();
		nParams = 0;
		String name = null;
		int pos = 0; // Beginning of the unprocessed region
		int i; // End of the unprocessed region
		char c = 0; // Current character
		for (i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if (c == '=' && name == null)
			{
				if (pos != i)
				{
					name = s.substring(pos, i);
				}
				pos = i + 1;
			}
			else if (c == '&')
			{
				if (name == null && pos != i)
				{
					// We haven't seen an `=' so far but moved forward.
					// Must be a param of the form '&a&' so add it with
					// an empty value.
					if (!addParam(params, s.substring(pos, i), ""))
					{
						return;
					}
				}
				else if (name != null)
				{
					if (!addParam(params, name, s.substring(pos, i)))
					{
						return;
					}
					name = null;
				}
				pos = i + 1;
			}
		}

		if (pos != i)
		{ // Are there characters we haven't dealt with?
			if (name == null)
			{ // Yes and we haven't seen any `='.
				if (!addParam(params, s.substring(pos, i), ""))
				{
					return;
				}
			}
			else
			{ // Yes and this must be the last value.
				if (!addParam(params, name, s.substring(pos, i)))
				{
					return;
				}
			}
		}
		else if (name != null)
		{ // Have we seen a name without value?
			if (!addParam(params, name, ""))
			{
				return;
			}
		}
	}

	private boolean addParam(Map<String, List<String>> params, String name, String value)
	{
		if (nParams >= maxParams)
		{
			return false;
		}

		List<String> values = params.get(name);
		if (values == null)
		{
			values = new ArrayList<String>(1); // Often there's only 1 value.
			params.put(name, values);
		}
		values.add(value);
		nParams++;
		return true;
	}
}
