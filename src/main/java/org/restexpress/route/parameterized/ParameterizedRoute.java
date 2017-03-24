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
package org.restexpress.route.parameterized;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.route.Route;
import org.restexpress.url.UrlMatch;
import org.restexpress.url.UrlPattern;


/**
 * @author toddf
 * @since Jan 7, 2011
 */
public class ParameterizedRoute
extends Route
{
	private UrlPattern[] aliases;

	/**
     * @param urlMatcher
     * @param controller
     * @param action
     * @param method
     * @param shouldSerializeResponse
     * @param name
     */
    public ParameterizedRoute(UrlPattern urlMatcher, Object controller, Method action, HttpMethod method, boolean shouldSerializeResponse,
    	String name, List<String> supportedFormats, String defaultFormat, Set<String> flags, Map<String, Object> parameters, String baseUrl)
    {
	    super(urlMatcher, controller, action, method, shouldSerializeResponse, name, supportedFormats, defaultFormat, flags, parameters, baseUrl);
    }

    /**
     * @param urlPattern
     * @param controller
     * @param action
     * @param method
     * @param shouldSerializeResponse
     * @param name
     */
    public ParameterizedRoute(String urlPattern, Object controller, Method action, HttpMethod method, boolean shouldSerializeResponse,
    	String name, List<String> supportedFormats, String defaultFormat, Set<String> flags, Map<String, Object> parameters, String baseUrl)
    {
	    this(new UrlPattern(urlPattern), controller, action, method, shouldSerializeResponse, name, supportedFormats, defaultFormat, flags, parameters, baseUrl);
    }

    public void addAliases(List<String> uris)
    {
    	if (uris == null) return;
    	
    	aliases = new UrlPattern[uris.size()];
    	int i = 0;

    	for (String uri : uris)
    	{
    		aliases[i++] = new UrlPattern(uri);
    	}
    }

    @Override
    public UrlMatch match(String url)
    {
    	UrlMatch match = super.match(url);
    	
    	if (match == null && aliases != null)
    	{
    		for (UrlPattern alias : aliases)
    		{
    			match = alias.match(url);
    			
    			if (match != null)
    			{
    				break;
    			}
    		}
    	}

    	return match;
    }
}
