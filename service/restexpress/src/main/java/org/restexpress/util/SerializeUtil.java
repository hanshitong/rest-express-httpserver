package org.restexpress.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeUtil {
	private static String DATE_TIME_PATTERN2 = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat dateTimeSdf2 = new SimpleDateFormat(DATE_TIME_PATTERN2);
	private static final Logger logger = LoggerFactory
			 .getLogger(SerializeUtil.class); 
	 
	public static ObjectMapper get(){
		return getObjectMapper().get(); 
	}
	
	//注意日期格式序列化和反序列化的时候yyyy-MM-dd HH:mm:ss,如果不同，请创建自己的objectMapper
    private static ThreadLocal<ObjectMapper> getObjectMapper(){  
    	return	new ThreadLocal<ObjectMapper>(){
    		@Override 
    		protected ObjectMapper initialValue() {
    			return createObjectMapper();
    		}
    	};
    }
    
    private static ObjectMapper createObjectMapper(SimpleDateFormat des,SimpleDateFormat ser){
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	objectMapper.setDateFormat(dateTimeSdf2);
    	//因为objectmapper默认是使用UTC时间，但是我们创建的时间是使用默认时区
    	objectMapper.setTimeZone(TimeZone.getDefault());
   	 	return objectMapper;
    }
    
    /**
     * 默认使用yyyy-MM-dd HH:mm:ss序列化和反序列化日期
     * @return
     */
    private static ObjectMapper createObjectMapper(){    	 
   	 	return createObjectMapper(dateTimeSdf2,dateTimeSdf2);
    }
    
}
