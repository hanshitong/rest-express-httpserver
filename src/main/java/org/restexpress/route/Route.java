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

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.RestExpress;
import org.restexpress.common.util.StringUtils;
import org.restexpress.domain.ex.ServerResponse;
import org.restexpress.domain.ex.SessionInfo;
import org.restexpress.intf.AuthRightIntf;
import org.restexpress.intf.AuthorityIntf;
import org.restexpress.intf.SessionIntf;
import org.restexpress.url.UrlMatch;
import org.restexpress.url.UrlMatcher;
import org.restexpress.util.SerializeUtil;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Route is an immutable relationship between a URL pattern and a REST
 * controller.
 * 
 * @author toddf
 * @since May 4, 2010
 */
public abstract class Route
{
	// SECTION: INSTANCE VARIABLES

	private UrlMatcher urlMatcher;
	private Object controller;
	private Method action;
	private HttpMethod method;
	private boolean shouldSerializeResponse = true;
	private String name;
	private String baseUrl;
	private List<String> supportedFormats = new ArrayList<String>();
	private String defaultFormat;
	private Set<String> flags = new HashSet<String>();
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static LocalVariableTableParameterNameDiscoverer localVar = new LocalVariableTableParameterNameDiscoverer();

	// SECTION: CONSTRUCTORS

	/**
	 * @param urlMatcher
	 * @param controller
	 */
	public Route(UrlMatcher urlMatcher, Object controller, Method action, HttpMethod method, boolean shouldSerializeResponse,
		String name, List<String> supportedFormats, String defaultFormat, Set<String> flags, Map<String, Object> parameters,
		String baseUrl)
	{
		super();
		this.urlMatcher = urlMatcher;
		this.controller = controller;
		this.action = action;
		this.action.setAccessible(true);
		this.method = method;
		this.shouldSerializeResponse = shouldSerializeResponse;
		this.name = name;
		this.supportedFormats.addAll(supportedFormats);
		this.defaultFormat = defaultFormat;
		this.flags.addAll(flags);
		this.parameters.putAll(parameters);
		this.baseUrl = baseUrl;
	}

	/**
	 * Answer whether the route contains the given flag.
	 * 
	 * @param flag
	 * @return true if the route contains the given flag.
	 */
	public boolean isFlagged(String flag)
	{
		if (flag == null) return false;

		return flags.contains(flag);
	}

	/**
	 * Answer whether the route contains all the given flags.
	 * 
	 * @param flags
	 * @return true if the route contains all the given flags.
	 */
	public boolean containsAllFlags(String[] flags)
	{
		if (flags == null) return false;

		return this.flags.containsAll(Arrays.asList(flags));
	}

	/**
	 * Answer whether the route contains any of the given flags.
	 * 
	 * @param flags
	 * @return true if the route contains any of the given flags.
	 */
	public boolean containsAnyFlags(String[] flags)
	{
		if (flags == null) return false;

		for (String flag : flags)
		{
			if (isFlagged(flag))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Answer whether the route contains the given parameter.
	 * 
	 * @param name
	 * @return true if the route contains the given parameter. Otherwise false.
	 */
	public boolean hasParameter(String name)
	{
		return (getParameter(name) != null);
	}

	/**
	 * Retrieve a parameter value by name from the route.
	 * 
	 * @param name
	 * @return the parameter value or null if the name is not found.
	 */
	public Object getParameter(String name)
	{
		return parameters.get(name);
	}

	public Method getAction()
	{
		return action;
	}
	
	public Object getController()
	{
		return controller;
	}
	
	public HttpMethod getMethod()
	{
		return method;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean hasName()
	{
		return (getName() != null && !getName().trim().isEmpty());
	}

	public String getBaseUrl()
	{
		return (baseUrl == null ? StringUtils.EMPTY_STRING : baseUrl);
	}

	/**
	 * Returns the base URL + the URL pattern.  Useful in creating links.
	 * 
	 * @return a string URL pattern containing the base URL.
	 */
	public String getFullPattern()
	{
		return getBaseUrl() + getPattern();
	}

	/**
	 * Returns the URL pattern without any '.{format}' at the end.  In essence, a 'short' URL pattern.
	 * 
	 * @return a URL pattern
	 */
	public String getPattern()
	{
		return urlMatcher.getPattern();
	}
	
	public boolean shouldSerializeResponse()
	{
		return shouldSerializeResponse;
	}

	/**
	 * @deprecated
	 */
    public Collection<String> getSupportedFormats()
    {
	    return Collections.unmodifiableList(supportedFormats);
    }
	
	/**
	 * @deprecated
	 */
	public boolean hasSupportedFormats()
	{
		return (!supportedFormats.isEmpty());
	}
	
	/**
	 * @deprecated
	 */
	public void addAllSupportedFormats(List<String> formats)
	{
		supportedFormats.addAll(formats);
	}
	
	public void addSupportedFormat(String format)
	{
		if (!supportsFormat(format))
		{
			supportedFormats.add(format);
		}
	}

	/**
	 * @deprecated
	 */
	public boolean supportsFormat(String format)
	{
		return supportedFormats.contains(format);
	}
	
	/**
	 * @deprecated
	 */
	public String getDefaultFormat()
	{
		return defaultFormat;
	}
	
	/**
	 * @deprecated
	 */
	public boolean hasDefaultFormat()
	{
		return defaultFormat != null;
	}

	public UrlMatch match(String url)
	{
		return urlMatcher.match(url);
	}
	
	public List<String> getUrlParameters()
	{
		return urlMatcher.getParameterNames();
	}

	/**
	 * modify by hanst,修改这个类，参考springmvc实现参数的定义与自动注入,目前只支持
	 * Byte,Integer,Double,String,Short,Long,Date(默认格式是yyyy-MM-dd HH:mm:ss),还有会话Session的注入，
	 * 还有text/json,application/json形式的body方式传参
	 * 
	 * 形式1，get
	 * http://xxxx/param1=value1&param2=value2
	 * 
	 * 形式2, 普通表单方式提交 post,body为普通的key=value,&分割的参数形式
	 * http://www.xxxx.com/xxx
	 * body:  param3=value3&param4=value4
	 * 
	 * 形式3 application/json text/json 方式
	 * http://www.xxxx.com/xxx
	 * 注意，如果使用这种方式提交，参数除了框架注入的参数(request,response,会话对象)外，只支持一个自定义参数
	 * body:  {param3: "value3","param4":"value4"} 或者 [{id: 123, name: "abc"}]
	 * 
	 * 后续可以添加注入个复杂类，类似springmvc
	 * 如果对端使用的netty把对象编码后过来，这里不需要解码对象为map再转成对象，效率低，直接使用getBody函数即可
	 * @param request
	 * @param response
	 * @return
	 */
	
	@SuppressWarnings(value={"rawtypes","unchecked"})
	public Object invoke(Request request, Response response)
	{
		//因为只支持get,post
		if (method.compareTo(HttpMethod.POST) == 0 && method.compareTo(request.getEffectiveHttpMethod()) != 0)
			return new ServerResponse(405,"http请求类型不匹配,请使用" + method.name());
		
		//对路径进行统一会话校验，如果需要权限，则用户至少需要登录
		SessionInfo sessionInfo = null;
		SessionIntf si = null;		
		try
        {
			if (request.getPath().indexOf(RestExpress.getConfig().getPermissionCheck()) >= 0)
			{			
				if (si == null){
					HashMap<String,String> privPath = RestExpress.getConfig().getSessionByPath();
					if (privPath != null)
						for(String path: privPath.keySet())
							if (request.getPath().indexOf(path) >=0){
								si = (SessionIntf) RestExpress.getSpringCtx().getBean(privPath.get(path));
								break;
							}
				}
				if (si == null)
					return new ServerResponse(500,"需要实现SessionIntf以获取会话信息Spring服务",request.getUrl());
				if (sessionInfo == null)
					sessionInfo = si.getSession(request);
				if (sessionInfo == null)
					return new ServerResponse(403,"未登录或会话已超时",request.getUrl());			
				ServerResponse sr = (si.checkSession(sessionInfo));
				if (sr != null)
					return sr;
			}
			
			String[] paramNames = localVar.getParameterNames(action);
			Map<String,Object> postValue = getPostValue(request,paramNames);
						
			//传过来的可能比action目标申明的方法参数少（有默认参数值）
			Class<?>[] types = action.getParameterTypes();									
			Object[] values = new Object[types.length]; 
			Annotation[][] paramsAn = action.getParameterAnnotations();
			int i = 0;			 
			for(Class cls: types){					
				Annotation paramAn = null;
				String paramName = null;				
				if (paramsAn[i].length > 0){
					paramAn = paramsAn[i][0];	
					if ((paramAn instanceof RequestParam) && ((RequestParam) paramAn).value().length() > 0)
						paramName = ((RequestParam) paramAn).value();
				}			
				//参数名，先用RequestParam里的别名，如果没有定义别名，则使用参数本身名字
				if (paramName == null)
					paramName = paramNames[i];
				
//				Object value = null;
				
				//参数值先从get里取，取不到再从post里获取,因此如果post，get都传了相同参数，以post为准
				Object value = postValue.get(paramName);	
				if ((paramAn instanceof RequestParam)){
					if (value==null){
						if (((RequestParam)paramAn).required())					   
						   return new ServerResponse(400,paramName + "参数不能为null");
						else{
							values[i++] = null;
							continue;
						}
					}					   	
				}
				try{
					if (cls.equals(org.restexpress.Request.class)) 
					  values[i] = request;
					else if (cls.equals(org.restexpress.Response.class))
						values[i] = response;
					else if (cls.equals(String.class)) //utf-8解码了
						values[i] = (String)value;
					else if (cls.equals(Integer.class)){
						if (value instanceof String)
							values[i] = Integer.parseInt((String)value);
						else	
							values[i] = (Integer)value;
					}
					else if (cls.equals(Long.class)){
						if (value instanceof String)
							values[i] = Long.parseLong((String)value);
						else
							values[i] = (Long)value;
					}
					else if (cls.equals(Byte.class)){	
						if (value instanceof String)
							values[i] = Byte.parseByte((String)value);
						else
							values[i] = (Byte)value;
					}
					else if (cls.equals(Short.class)){
						if (value instanceof String)
							values[i] = Short.parseShort((String)value);
						else
							values[i] = (Short)value;
					}
					else if (cls.equals(Double.class)){
						if (value instanceof String)
							values[i] = Double.parseDouble((String)value);
						else
							values[i] = (Double)value;
					}
					else if (cls.equals(Date.class))
						values[i] = (value != null && value.toString().length() > 0) ?sdf.parse(value.toString()):null;	
					else if (SessionInfo.class.isAssignableFrom(cls)){
						//有些接口不是以/priv/开头的,但是需要用户回话信息做特殊判断，这里需要重新获取会话(如果有的话)
						if (sessionInfo == null){ 	
							SessionInfo obj = (SessionInfo)cls.newInstance();  //这里多创建了一个实例,能否避免?
							si = (SessionIntf) RestExpress.getSpringCtx().getBean(obj.getSessionImpl());
							obj = null;
							sessionInfo = si.getSession(request);
//							HashMap<String,String> privPath = RestExpress.getConfig().getSessionByClass();
//								if (privPath != null)
//									for(String className: privPath.keySet())
//										if (cls.getName().equals(className)){
//											si = (SessionIntf) RestExpress.getSpringCtx().getBean(privPath.get(className));
//											sessionInfo = si.getSession(request);
//											break;
//										}
							 
						}
						values[i] = sessionInfo;
					}
						//注入一个会话信息对象
//					else if (SessionIntf.class.isAssignableFrom(cls)){						 
//						if (si == null)
//							si = RestExpress.getSpringCtx().getBean(SessionIntf.class);			
//						if (si == null)
//							return new ServerResponse(500,"需要实现SessionIntf以获取会话信息Spring服务",request.getUrl());
//						if (sessionInfo == null)
//							sessionInfo = si.getSession(request);						
//						if (sessionInfo != null){
//							ServerResponse sr = si.checkSession(sessionInfo);
//						 	if (sr != null)
//						 		return sr;
//						}
//						values[i] = sessionInfo;
//					}
					//权限信息
//					else if (cls.equals(AuthorityIntf.class)){
//						AuthorityIntf auth = RestExpress.getSpringCtx().getBean(AuthorityIntf.class);
//						if (auth == null)
//							return new ServerResponse(500,"需要实现AuthorityIntf以获取权限信息Spring服务",request.getUrl());
//						if (si == null)
//							si = RestExpress.getSpringCtx().getBean(SessionIntf.class);			
//						if (si == null)
//							return new ServerResponse(500,"需要实现SessionIntf以获取会话信息Spring服务",request.getUrl());
//						if (sessionInfo == null)
//							sessionInfo = si.getSession(request);
//						auth.setSessionInfo(sessionInfo);
//						
//						//下面这个并非一定要的，所以可空
//						AuthRightIntf auth2 = RestExpress.getSpringCtx().getBean(AuthRightIntf.class);
//						if (auth2 != null){
//							auth.setAuthRightIntf(auth2);
//							auth2.setSessionInfo(sessionInfo);
//						}
//																		
//						values[i] = auth;
//					}
//					else if (cls.equals(AuthRightIntf.class)){
//						AuthRightIntf auth = RestExpress.getSpringCtx().getBean(AuthRightIntf.class);
//						if (auth == null)
//							return new ServerResponse(500,"需要实现AuthRightIntf以获取权限信息Spring服务",request.getUrl());
//						if (si == null)
//							si = RestExpress.getSpringCtx().getBean(SessionIntf.class);			
//						if (si == null)
//							return new ServerResponse(500,"需要实现SessionIntf以获取会话信息Spring服务",request.getUrl());
//						if (sessionInfo == null)
//							sessionInfo = si.getSession(request);
//						auth.setSessionInfo(sessionInfo);
//						values[i] = auth;
//					}					
					else if (!cls.isPrimitive()){ //注入一个复杂对象							
						values[i] = SerializeUtil.get().readValue(
								SerializeUtil.get().writeValueAsBytes(postValue),cls);
					}
				}catch(NumberFormatException | ParseException e){					
					return new ServerResponse(400,new StringBuilder("参数").append(paramName).
							append("值:").append(request.getHeader(paramName)).append("无法转成:").
							append(cls.getName()).toString());					 
				}								
				i++;					 
			}
	        return action.invoke(controller,values);	 
	         
	        //原来实现
//	        return action.invoke(controller,request,response);
        }
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
			return new ServerResponse(500,"服务器繁忙，稍后再试");
			//下面原来的实现
//			Throwable cause = e.getCause();
//			
//			if (RuntimeException.class.isAssignableFrom(cause.getClass()))
//			{
//				throw (RuntimeException) e.getCause();
//			}
//			else
//			{
//				throw new RuntimeException(cause);
//			}
		}
        catch (Exception e)
        {
        	e.printStackTrace();
        	return new ServerResponse(500,"服务器繁忙，稍后再试");
        	//下面原来的实现
//        	throw new ServiceException(e);
        }
	}

	/**
	 * 返回get里的参数和post里的参数，如果get 和post都出现，以post为准
	 * @param request
	 * @return
	 */
	private Map<String, Object> getPostValue(Request request,String[] paramNames) {
		boolean post = (request.getEffectiveHttpMethod().equals(HttpMethod.POST));
		String contentType = request.getHeader(HttpHeaders.Names.CONTENT_TYPE);
		boolean isjson = false;
		if (contentType != null){ 
			contentType = contentType.toLowerCase();
		    isjson = contentType.indexOf("text/json") >= 0 || contentType.indexOf("application/json") >= 0;
		}
		HashMap<String, Object> postValue = new HashMap<>();
		
		//从url串里获取参数
		if (!post)
			for(String key : request.getQueryStringMap().keySet())
			try {
				postValue.put(key,URLDecoder.decode(request.getQueryStringMap().get(key),ContentType.ENCODING));
			} catch (UnsupportedEncodingException e1) {
				 
			}
		//再从url path里获取一些参数值
		for(String key: paramNames)
			postValue.put(key,request.getHeader(key));
		 
		if (post){			
			if (isjson){ // application/json text/json 方式
				try{			
//					String value = URLDecoder.decode(request.getBody().toString(ContentType.CHARSET),ContentType.ENCODING);
					HashMap value = request.getBodyAs(HashMap.class);
					postValue.putAll(value);
				}catch(Exception e){
					e.printStackTrace();
					return null;
				}
			}
			else{	// application/x-www-form-urlencoded,一般表单提交方式,title=test&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3
				Map<String, List<String>> tmp =  request.getBodyFromUrlFormEncoded();
				if (tmp != null){					 					 
					for(String key: tmp.keySet()){
						List<String> value = tmp.get(key);
						postValue.put(key,value.get(0));	//这里我们只取第一个值，所以不支持数组的方式					 
					}
				}				
			}
		}
		return postValue;
	}

	/**
	 * 仅限于pojo对象，比如实体,bean等
	 * @param request
	 * @param cls
	 * @param paramNames
	 * @return
	 */
	private Object getComplexObjectValue(Request request, Class cls,Map<String,List<String>> postValue) {
		Object ins = null;
		Method[] ms = null;
		try{
			ins = cls.newInstance();
			ms = cls.getDeclaredMethods();			
		}catch(Exception e){
			//忽略不能注入复杂对象的错误
			return null;
		} 
		
		boolean fillOnce = false;
		if (ms != null){
			//url里?后面显示传的
			Set<String> params = new HashSet<>(request.getQueryStringMap().keySet());			
			//url path里传的,个人觉得还不如把整个的参数合并到request,不知道restexpress为何分开这2个部分参数			
			params.addAll(this.urlMatcher.getParameterNames());
			//post方式传的
			if (postValue != null)
				params.addAll(postValue.keySet());			
			for(Method m: ms)
				for(String str: params)
					if (("set" + str.substring(0,1).toUpperCase() + str.substring(1)).equals(m.getName()))
					try{	
						String value = request.getHeader(str); 					
						if (value == null && postValue != null)
						{
							List<String> list = postValue.get(str);
							//因为只取第一个，所以不能以传数组的方式传递参数
							if (list != null && list.size() > 0)
								value = URLDecoder.decode(list.get(0),"utf-8");
						}
						Class[] types = m.getParameterTypes(); 
						if (types.length == 1){
							if (types[0].equals(String.class))  
								m.invoke(ins,value);
							else if (types[0].equals(Integer.class))
								m.invoke(ins,Integer.parseInt(value));
							else if (types[0].equals(Long.class))
								m.invoke(ins,Long.valueOf(value));
							else if (types[0].equals(Byte.class))	
								m.invoke(ins,Byte.valueOf(value));
							else if (types[0].equals(Short.class))
								m.invoke(ins,Short.valueOf(value));
							else if (types[0].equals(Double.class))
								m.invoke(ins,Double.valueOf(value));
							else if (types[0].equals(Date.class))
								m.invoke(ins,sdf.parse(value));							
							fillOnce = true;
						}
					}catch(Exception e){
						
					}	
		}
		return fillOnce?ins:null;
	}
	
	public static void main(String[] args){
		 
	}
}
