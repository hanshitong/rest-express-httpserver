package org.restexpress.intf.impl;

import java.lang.reflect.Method;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.intf.HttpRequestParameterParseIntf;

/**
 * rest-express的默认实现，所有参数在request里，不做任何处理，在应用的接口里自己自由获取
 * 而且顺序是固定的action先申明Request,Response
 * @author hanst
 *
 */
public class DefaultHttpRequestParameterParseImpl implements HttpRequestParameterParseIntf {

	@Override
	public Object[] parse(Method action, Request request,Response response) {
		return new Object[]{request,response};
	}

}
