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

import java.util.List;

import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.Request;
import org.restexpress.exception.MethodNotAllowedException;
import org.restexpress.exception.NotFoundException;
import org.restexpress.util.Resolver;

/**
 * @author toddf
 * @since May 4, 2010
 */
public class RouteResolver
implements Resolver<Action>
{
	private RouteMapping routeMapping;
	
	public RouteResolver(RouteMapping routes)
	{
		super();
		this.routeMapping = routes;
	}
	
	public Route getNamedRoute(String name, HttpMethod method)
	{
		return routeMapping.getNamedRoute(name, method);
	}
	
	@Override
	public Action resolve(Request request)
	{
		Action action = routeMapping.getActionFor(request.getEffectiveHttpMethod(), request.getPath());
		
		if (action != null) return action;

		List<HttpMethod> allowedMethods = routeMapping.getAllowedMethods(request.getPath());

		if (allowedMethods != null && !allowedMethods.isEmpty())
		{
			throw new MethodNotAllowedException(request.getUrl(), allowedMethods);
		}

		throw new NotFoundException("Unresolvable URL: " + request.getUrl());
	}
}
