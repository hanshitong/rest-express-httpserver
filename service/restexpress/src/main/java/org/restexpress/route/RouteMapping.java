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
package org.restexpress.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.url.UrlMatch;

/**
 * Contains the routes for a given service implementation. Sub-classes will
 * implement the initialize() method which calls map() to specify how URL
 * request will be routed to the underlying controllers.
 * 
 * @author toddf
 * @since May 21, 2010
 */
public class RouteMapping
{
	// SECTION: INSTANCE VARIABLES

	private Map<HttpMethod, List<Route>> routes;
	private List<Route> deleteRoutes = new ArrayList<Route>();
	private List<Route> getRoutes = new ArrayList<Route>();
	private List<Route> postRoutes = new ArrayList<Route>();
	private List<Route> putRoutes = new ArrayList<Route>();
	private List<Route> optionRoutes = new ArrayList<Route>();
	private List<Route> headRoutes = new ArrayList<Route>();

	private Map<String, Map<HttpMethod, Route>> routesByName = new HashMap<String, Map<HttpMethod, Route>>();
	private Map<String, List<Route>> routesByPattern = new LinkedHashMap<String, List<Route>>();

	// SECTION: CONSTRUCTOR

	public RouteMapping()
	{
		super();
		routes = new HashMap<HttpMethod, List<Route>>();		
		routes.put(HttpMethod.DELETE, deleteRoutes);
		//modify by hanst get,post 都映射到get里去，因为方法实现的时候不区分get post
		routes.put(HttpMethod.GET, getRoutes);
		routes.put(HttpMethod.POST, getRoutes);
//		routes.put(HttpMethod.POST, postRoutes);
		routes.put(HttpMethod.PUT, putRoutes);
		routes.put(HttpMethod.HEAD, headRoutes);
		routes.put(HttpMethod.OPTIONS, optionRoutes);
	}


	// SECTION: UTILITY - PUBLIC

	/**
	 * Return a list of Route instances for the given HTTP method. The returned
	 * list is immutable.
	 * 
	 * @param method
	 *            the HTTP method (GET, PUT, POST, DELETE) for which to retrieve
	 *            the routes.
	 */
	public List<Route> getRoutesFor(HttpMethod method)
	{
		List<Route> routesFor = routes.get(method);

		if (routesFor == null)
		{
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(routesFor);
	}

	/**
	 * Attempts to match the path and method to an appropriate Route, returning an
	 * Action instance if a match is found.  Returns null if no match is found.
	 * 
	 * @param method
	 *            the HTTP method (GET, PUT, POST, DELETE) for which to retrieve
	 *            the routes.
	 * @param path the path portion of the url to match.
	 * @return a new Action or null, if the path/method combination don't match.
	 */
	public Action getActionFor(HttpMethod method, String path)
	{
		for (Route route : routes.get(method))
		{
			UrlMatch match = route.match(path);

			if (match != null)
			{
				return new Action(route, match);
			}
		}
		
		return null;
	}

	/**
	 * Returns a list of Route instances that the given path resolves to.
	 * 
	 * @param path the path portion of the URL (e.g. after the domain and port).
	 * @return A list of Route instances matching the given path. Never null.
	 */
	public List<Route> getMatchingRoutes(String path)
	{
		for (List<Route> patternRoutes : routesByPattern.values())
		{
			if (patternRoutes.get(0).match(path) != null)
			{
				return Collections.unmodifiableList(patternRoutes);
			}
		}
		
		return Collections.emptyList();
	}

	/**
	 * Returns the supported HTTP methods for the given URL path.
	 * 
	 * @param path the path portion of the URL (e.g. after the domain and port).
	 * @return A list of appropriate HTTP methods for the given path. Never null.
	 */
	public List<HttpMethod> getAllowedMethods(String path)
	{
		List<Route> matchingRoutes = getMatchingRoutes(path);
		
		if (matchingRoutes.isEmpty()) return Collections.emptyList();

		List<HttpMethod> methods = new ArrayList<HttpMethod>();

		for (Route route : matchingRoutes)
		{
			methods.add(route.getMethod());
		}
		
		return methods;
	}

	/**
	 * Return a Route by the name and HttpMethod provided in DSL. Returns null
	 * if no route found.
	 * 
	 * @param name
	 * @return
	 */
	public Route getNamedRoute(String name, HttpMethod method)
	{
		Map<HttpMethod, Route> routesByMethod = routesByName.get(name);

		if (routesByMethod == null)
		{
			return null;
		}

		return routesByMethod.get(method);
	}


	// SECTION: UTILITY

	/**
	 * @param route
	 */
	public void addRoute(Route route)
	{
		List<Route> list = routes.get(route.getMethod());

		if (list == null)
		{
			list = new ArrayList<Route>();
			routes.put(route.getMethod(), list);
		}

		list.add(route);
		addByPattern(route);

		if (route.hasName())
		{
			addNamedRoute(route);
		}
	}


	// SECTION: UTILITY - PRIVATE

	private void addNamedRoute(Route route)
	{
		Map<HttpMethod, Route> routesByMethod = routesByName.get(route.getName());

		if (routesByMethod == null)
		{
			routesByMethod = new HashMap<HttpMethod, Route>();
			routesByName.put(route.getName(), routesByMethod);
		}

		routesByMethod.put(route.getMethod(), route);
	}
	
	private void addByPattern(Route route)
	{
		List<Route> urlRoutes = routesByPattern.get(route.getPattern());
		
		if (urlRoutes == null)
		{
			urlRoutes = new ArrayList<Route>();
			routesByPattern.put(route.getPattern(), urlRoutes);
		}
		
		urlRoutes.add(route);
	}
}
