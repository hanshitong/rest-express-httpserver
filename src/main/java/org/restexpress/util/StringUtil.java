package org.restexpress.util;

import java.lang.reflect.Method;

import org.springframework.web.bind.annotation.RequestMapping;

public class StringUtil {
	public static String getMaping(String name) {
		String value = name.substring(name.lastIndexOf(".")+1).toLowerCase();
		int pos = value.indexOf("controller");
		if (pos < 0)
			return value;
		else
			return "/" + value.substring(0,pos);
	}

	public static String getRequestMapping(Method m, String baseUri) {
		RequestMapping rm = m.getAnnotation(RequestMapping.class);
		StringBuilder finalUri = new StringBuilder(baseUri);	 
		// 方法名上有映射标注
		if (rm != null && rm.value()[0].length() > 0)
			finalUri.append(rm.value()[0]);
		else
			// 否则方法上没有映射标注,或者标注为"",则直接默认是方法名小写
			finalUri.append("/").append(m.getName().toLowerCase());		 
		return finalUri.append(".{format}").toString();
	}

}
