package org.restexpress.route;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restexpress.common.exception.ConfigurationException;
import org.restexpress.domain.metadata.RouteMetadata;
import org.restexpress.domain.metadata.UriMetadata;
import org.restexpress.settings.RouteDefaults;
import org.restexpress.url.UrlPattern;
import org.restexpress.util.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Builds a route for a single URI.  If a URI is given with no methods or actions, the builder
 * creates routes for the GET, POST, PUT, and DELETE HTTP methods for the given URI.
 * 
 * @author toddf
 * @since 2010
 */
public abstract class RouteBuilder
{
	// SECTION: CONSTANTS

	static final String DELETE_ACTION_NAME = "delete";
	static final String GET_ACTION_NAME = "read";
	static final String POST_ACTION_NAME = "create";
	static final String PUT_ACTION_NAME = "update";
	static final List<HttpMethod> DEFAULT_HTTP_METHODS = Arrays.asList(new HttpMethod[] {GET, POST, PUT, DELETE});
	static final Map<HttpMethod, String> ACTION_MAPPING = new HashMap<HttpMethod, String>();	
	static final AntPathMatcher antPathMatcher = new AntPathMatcher();

	static
	{
		ACTION_MAPPING.put(DELETE, DELETE_ACTION_NAME);
		ACTION_MAPPING.put(GET, GET_ACTION_NAME);
		ACTION_MAPPING.put(POST, POST_ACTION_NAME);
		ACTION_MAPPING.put(PUT, PUT_ACTION_NAME);
	}

	
	// SECTION: INSTANCE VARIABLES

	private String uri;
	private List<HttpMethod> methods = new ArrayList<HttpMethod>();
	private List<String> supportedFormats = new ArrayList<String>();
	private String defaultFormat = null;
	private Map<HttpMethod, String> actionNames = new HashMap<HttpMethod, String>();
	private Object controller;
	private boolean shouldSerializeResponse = true;
	private String name;
	private String baseUrl;
	private Set<String> flags = new HashSet<String>();
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	/**
	 * Create a RouteBuilder instance for the given URI pattern. URIs that match the pattern
	 * will map to methods on the POJO controller.
	 * 
	 * @param uri a URI pattern
	 * @param controller the POJO service controller.
	 */
	public RouteBuilder(String uri, Object controller, RouteDefaults defaults)
	{
		super();
		this.uri = uri;
		this.controller = controller;
		applyDefaults(defaults);
	}

	/**
	 * Map a service method name (action) to a particular HTTP method (e.g. GET, POST, PUT, DELETE, HEAD, OPTIONS)
	 * 
	 * @param action the name of a method within the service POJO.
	 * @param method the HTTP method that should invoke the service method.
	 * @return the RouteBuilder instance.
	 */
	public RouteBuilder action(String action, HttpMethod method)
	{
		if (!actionNames.containsKey(method))
		{
			actionNames.put(method, action);
		}

		if (!methods.contains(method))
		{
			methods.add(method);
		}

		return this;
	}
	
	/**
	 * Set the base URL that is associated with this route.  By default
	 * the route will inherit the base URL from the RestExpress server and
	 * is used when retrieving the URL pattern for a route in order to create
	 * a Location or other hypermedia link.
	 *  
	 * @param baseUrl protocol://host:port to use as a base URL in links.
	 * @return the RouteBuilder instance.
	 */
	public RouteBuilder baseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
		return this;
	}
	
	/**
	 * Defines HTTP methods that the route will support (e.g. GET, PUT, POST, DELETE, OPTIONS, HEAD, PATCH).
	 * This utilizes the default HTTP method to service action mapping (e.g. GET maps to read(), PUT to update(), etc.).
	 * 
	 * @param methods the HTTP methods supported by the route.
	 * @return the RouteBuilder instance.
	 */
	public RouteBuilder method(HttpMethod... methods)
	{
		for (HttpMethod method : methods)
		{
			if (!this.methods.contains(method))
			{
				this.methods.add(method);
			}
		}

		return this;
	}

	/**
	 * Turns off serialization of the response--returns the response body as pain text.
	 * 
	 * @return the RouteBuilder instance.
	 */
	public RouteBuilder noSerialization()
	{
		this.shouldSerializeResponse = false;
		return this;
	}

	/**
	 * Turns on response serialization (the default) so the response body will be serialized
	 * (e.g. into JSON or XML).
	 * 
	 * @return the RouteBuilder instance.
	 */
	public RouteBuilder performSerialization()
	{
		this.shouldSerializeResponse = true;
		return this;
	}

	/**
	 * Give the route a known name to facilitate retrieving the route by name.  This facilitates
	 * using the route URI pattern to create Link instances via LinkUtils.asLinks().
	 * 
	 * The name must be unique for each URI pattern.
	 * 
	 * @param name the given name of the route for later retrieval.
	 * @return the RouteBuilder instance.
	 */
	public RouteBuilder name(String name)
	{
		this.name = name;
		return this;
	}

	/**
	 * 
	 * @param format
	 * @return
	 * @deprecated
	 */
	public RouteBuilder format(String format)
	{
		if (!supportedFormats.contains(format))
		{
			supportedFormats.add(format);
		}
		
		return this;
	}

	/**
	 * 
	 * @param format
	 * @return
	 * @deprecated
	 */
	public RouteBuilder defaultFormat(String format)
	{
		this.defaultFormat = format;
		return this;
	}

	/**
	 * Flags are boolean settings that are created at route definition time.
	 * These flags can be used to pass booleans to preprocessors, controllers, or postprocessors.  An example might be:
	 * flag(NO_AUTHORIZATION), which might inform an authorization preprocessor to skip authorization for this route.
	 * 
	 * @param flagValue the name of the flag.
	 * @return this RouteBuilder to facilitate method chaining.
	 */
	public RouteBuilder flag(String flagValue)
	{
		flags.add(flagValue);
		return this;
	}

	/**
	 * Parameters are named settings that are created at route definition time. These parameters
	 * can be used to pass data to subsequent preprocessors, controllers, or postprocessors.  This is a way to pass data
	 * from a route definition down to subsequent controllers, etc.  An example might be: setParameter("route", "read_foo")
	 * setParameter("permission", "view_private_data"), which might inform an authorization preprocessor of what permission
	 * is being requested on a given resource.
	 * 
	 * @param name the name of the parameter.
	 * @param value an object that is the parameter value.
	 * @return this RouteBuilder to facilitate method chaining.
	 */
	public RouteBuilder parameter(String name, Object value)
	{
		parameters.put(name, value);
		return this;
	}

	/**
	 * NOT IMPLEMENTED.
	 * 
	 * @return
	 * @deprecated
	 */
	public RouteBuilder useStreamingMultipartUpload()
	{
		// TODO: complete supportMultipart()
		return this;
	}
	
	/**
	 * NOT IMPLEMENTED.
	 * 
	 * @return
	 * @deprecated
	 */
	public RouteBuilder useStreamingDownload()
	{
		// TODO: complete useStreamingdownload()
		return this;
	}
	
	
	// SECTION - BUILDER

	/**
	 * Build the Route instances.  The last step in the Builder process.
	 * 
	 * @return a List of Route instances.
	 */
	public List<Route> build() 
	{
//		if (methods.isEmpty())
//		{
//			methods = DEFAULT_HTTP_METHODS;
//		}
//
//		List<Route> routes = new ArrayList<Route>();
//		String pattern = toRegexPattern(uri);
//		
//		for (HttpMethod method : methods)
//		{
//			String actionName = actionNames.get(method);
//
//			if (actionName == null)
//			{
//				actionName = ACTION_MAPPING.get(method);
//
//				if (actionName == null)
//				{
//					actionName = method.name().toLowerCase();
//				}
//			}
//			
//			Method action = determineActionMethod(controller, actionName);
//			routes.add(newRoute(pattern, controller, action, method, shouldSerializeResponse, name, supportedFormats, defaultFormat, flags, parameters, baseUrl));
//		}
//		
//		return routes;
		
		//上面是旧的实现
		if (methods.isEmpty())
		{
			methods = DEFAULT_HTTP_METHODS;
		}
		List<Route> routes = new ArrayList<Route>();
		String pattern = toRegexPattern(uri);
		Method action = determineActionMethod(controller, pattern);		
		//method都传get,不使用http的请求方式映射方法
		RequestMapping rm = action.getAnnotation(RequestMapping.class);
		HttpMethod method = null;
		if (rm.method() != null && rm.method().length > 0 && rm.method()[0] == RequestMethod.POST)
			method = DEFAULT_HTTP_METHODS.get(DEFAULT_HTTP_METHODS.indexOf(HttpMethod.POST));
		else
			method = DEFAULT_HTTP_METHODS.get(DEFAULT_HTTP_METHODS.indexOf(HttpMethod.GET));
		
		routes.add(newRoute(pattern, controller, action, method, shouldSerializeResponse, 
				name, supportedFormats, defaultFormat, flags, parameters, baseUrl));
		return routes;
	}

	protected abstract String toRegexPattern(String uri);
	
	
	// SECTION: CONSOLE
	
	public RouteMetadata asMetadata()
	{
		RouteMetadata metadata = new RouteMetadata();
		metadata.setName(name);
		metadata.setSerialized(shouldSerializeResponse);
		metadata.setDefaultFormat(defaultFormat);
		metadata.addAllSupportedFormats(supportedFormats);
		metadata.setBaseUrl(baseUrl);
		
		UriMetadata uriMeta = new UriMetadata(uri);
		List<Route> routes = build();

		for (Route route : routes)
		{
			uriMeta.addAllParameters(route.getUrlParameters());			
			metadata.addMethod(route.getMethod().name());
		}

		metadata.setUri(uriMeta);
		return metadata;
	}

	
	// SECTION: UTILITY - SUBSCLASSES

	/**
     * @param pattern
     * @param controller
     * @param action
     * @param method
     * @param shouldSerializeResponse
     * @param name
	 * @param supportedFormats 
	 * @param defaultFormat
	 * @param flags
	 * @param parameters
	 * @param baseUrl
     * @return
     */
    protected abstract Route newRoute(String pattern, Object controller, Method action,
    	HttpMethod method, boolean shouldSerializeResponse,
    	String name, List<String> supportedFormats, String defaultFormat, Set<String> flags,
    	Map<String, Object> parameters, String baseUrl);


	// SECTION: UTILITY - PRIVATE

	/**moidfy by hanst
	 * 注意这里并非使用http请求的方式PUT,GET等对应到pojo里的update,read等方法，而是允许使用别名和映射，因此使用的是
	 * url去匹配RequestMapping指定的url,actionName是uri
	 * 
	 * Attempts to find the actionName on the controller, assuming a signature of actionName(Request, Response), 
	 * and returns the action as a Method to be used later when the route is invoked.
	 * 
	 * @param controller a pojo that implements a method named by the action, with Request and Response as parameters.
	 * @param actionName the name of a method on the given controller pojo.
	 * @return a Method instance referring to the action on the controller.
	 * @throws ConfigurationException if an error occurs.
	 */
	protected Method determineActionMethod(Object controller, String actionName)
	{		
		try
		{
			//原来实现
			//return controller.getClass().getMethod(actionName, Request.class, Response.class);
			Class cls = controller.getClass();
			Method[] methods = cls.getDeclaredMethods();
			int dup = 0;
			Method result = null;			
			RequestMapping an = (RequestMapping) cls.getAnnotation(RequestMapping.class);
    		String uri = (an == null || an.value()[0].length() == 0)?StringUtil.getMaping(cls.getName()):an.value()[0];    		
			for(Method m: methods){
				if (m.getModifiers() != 1) continue;				 
				String url = StringUtil.getRequestMapping(m, uri); 			
				UrlPattern urlPattern = new UrlPattern(url); 
				//在main构建里，如果有re
				if (urlPattern.matches(actionName) || antPathMatcher.match(url, actionName)){
					result = m;
					dup++;
				}
			}
			if (dup > 1)
				throw new ConfigurationException(new StringBuilder(controller.getClass().getName()).
						append("不能有多个").append(actionName).append("重复映射").toString());
			if (result == null)
				throw new ConfigurationException(new StringBuilder(controller.getClass().getName()).
						append("没有找到").append(actionName).append("映射的方法").toString());
			return result;
		}
		catch (Exception e)
		{
			throw new ConfigurationException(e);
		}
	}

	/**
     * @param defaults
     */
    protected void applyDefaults(RouteDefaults defaults)
    {
    	if (defaults == null) return;

    	defaultFormat(defaults.getDefaultFormat());
    	baseUrl(defaults.getBaseUrl());
    }
}
