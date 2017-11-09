package org.restexpress.intf;

import java.lang.reflect.Method;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.exception.ServiceException;

public interface HttpRequestParameterParseIntf {
	//从客户端http请求request里解析目标方法action需要的参数,并按顺序以数组方式返回
	Object[] parse(Method action,Request request,Response response) throws Exception;
}
