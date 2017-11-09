package org.restexpress.intf.impl;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.RestExpress;
import org.restexpress.domain.ex.SessionInfo;
import org.restexpress.exception.BadRequestException;
import org.restexpress.exception.ForbiddenException;
import org.restexpress.exception.ServiceException;
import org.restexpress.intf.HttpRequestParameterParseIntf;
import org.restexpress.intf.SessionIntf;
import org.restexpress.util.SerializeUtil;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.netty.handler.codec.http.HttpHeaders;

/**
 *  
 * 实现类似springmvc controller那样申明的参数的形式 
 * 额外还有回话的注入，权限注入或验证等
	 * modify by hanst,修改这个类，参考springmvc实现参数的定义与自动注入,目前只支持
	 * Byte,Integer,Double,String,Short,Long,Date(默认格式是yyyy-MM-dd HH:mm:ss),还有会话Session的注入，
	 * 还有application/json形式的body方式传参
	 * 
	 * 形式1，get
	 * http://xxxx/param1=value1&param2=value2
	 * 
	 * 形式2, 普通表单方式提交 post,body为普通的key=value,&分割的参数形式
	 * http://www.xxxx.com/xxx
	 * body:  param3=value3&param4=value4
	 * 
	 * 形式3 application/json  方式
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
public class SpringMVCHttpRequestParameterParseImpl implements HttpRequestParameterParseIntf {
	private static LocalVariableTableParameterNameDiscoverer localVar = new LocalVariableTableParameterNameDiscoverer();

	@Override
	public Object[] parse(Method action, Request request,Response response) throws ServiceException{
		String contentType = request.getHeader(HttpHeaders.Names.CONTENT_TYPE);
		boolean isjson = false;
		if (contentType != null){ 
			isjson = contentType.toLowerCase().indexOf("application/json") >= 0;
		}
		
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
					throw new ServiceException("需要实现SessionIntf以获取会话信息Spring服务");
				if (sessionInfo == null)
					sessionInfo = si.getSession(request);
				if (sessionInfo == null)
					throw new ForbiddenException("未登录或会话已超时");			
			}
			
			String[] paramNames = localVar.getParameterNames(action);
			Map<String,Object> postValue = getPostValue(request,paramNames,request.isMethodPost(),isjson);
						
			//传过来的可能比action目标申明的方法参数少（有默认参数值）
			Class[] types = action.getParameterTypes();	
			Type[] gtype = action.getGenericParameterTypes();
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
				
				Object value = postValue.get(paramName);	
				if ((paramAn instanceof RequestParam)){
					if (value==null){
						if (((RequestParam)paramAn).required())					   
							throw new BadRequestException(paramName + "参数不能为null");
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
					else if (SessionInfo.class.isAssignableFrom(cls)){
						//有些接口不是以/priv/开头的,但是需要用户回话信息做特殊判断，这里需要重新获取会话(如果有的话)
						if (sessionInfo == null){ 	
							SessionInfo obj = (SessionInfo)cls.newInstance();  //这里多创建了一个实例,能否避免?
							si = (SessionIntf) RestExpress.getSpringCtx().getBean(obj.getSessionImpl());
							obj = null;
							sessionInfo = si.getSession(request);
						}
						values[i] = sessionInfo;
					}
						 				
					else if (!cls.isPrimitive()){ //注入一个复杂对象	 
						if (isjson){
//							values[i] = request.getBodyAs(cls);
							JavaType jt = TypeFactory.defaultInstance().constructType(gtype[i]);
							values[i] = SerializeUtil.get().readValue(request.getBodyAsStream(),jt);
						}
						else
							values[i] = SerializeUtil.get().convertValue(postValue,cls);
					}
				}catch(NumberFormatException  e){					
					throw new BadRequestException(new StringBuilder("参数").append(paramName).
							append("值:").append(request.getHeader(paramName)).append("无法转成:").
							append(cls.getName()).toString());					 
				}								
				i++;					 
			}
	        return values;	 
        }
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ServiceException(e);
		}
         
	}

	/**
	 * 返回get里的参数和post里的参数，如果get 和post都出现，以post为准,如果是application/json  text/json
	 * 方式提交，return 空的参数体
	 * @param request
	 * @return
	 */
	private Map<String, Object> getPostValue(Request request,String[] paramNames,boolean post,boolean isjson) {
		HashMap<String, Object> postValue = new HashMap<>();
		if (post && isjson) return postValue;
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
		 
		if (post && !isjson ){			
			Map<String, List<String>> tmp =  request.getBodyFromUrlFormEncoded();
			if (tmp != null){					 					 
				for(String key: tmp.keySet()){
					List<String> value = tmp.get(key);
					postValue.put(key,value.get(0));	//这里我们只取第一个值，所以不支持数组的方式					 
				}
			}
		}
		return postValue;
	}
}
