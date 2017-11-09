package org.restexpress.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.restexpress.common.exception.ConfigurationException;

public class Configuration extends Environment {
	private boolean useTcpNoDelay = true;
	private int soLinger = -1; // disabled by default
	private int receiveBufferSize = 262140; // Java default
	private int sendBufferSize = 8096;
	private int connectTimeoutMillis = 10000;
	private boolean keepAlive = true;
	private boolean reuseAddress = true;
	private int maxContentSize = 25600; //netty default
	
	//取决于你的服务，如果你的服务都是异步的，则可以设置为0，如果不是，加上服务部署3个，
	//服务连接的数据库支持300个连接，则300/3 = 100个
	private int executorThreadCount = 100;
	
	//如果url路径匹配下面则进行权限检查，只检查是否需要登录
	private String permissionCheck = "/priv/";
	
	private HashMap<String,String> sessionByPath;
	private HashMap<String,String> sessionByClass;

	private String stopCommandStr = "stop";
	
	private static final String nettyConfigFile = "netty.properties";
	
	private String httpRequestHandler = "org.restexpress.pipeline.DefaultRequestHandler"; 
	
	private String springConfigFile = null;
	
	private int listenPort = 9091;
	
	private int shutdownPort = 9092;
	private int ioThreadCount = Runtime.getRuntime().availableProcessors() * 2;
	private int backLog = 1024;
	
	private long readIdleTimeOut = 0;
	private long writeIdleTimeOut = 0;
	private long bothIdleTimeOut = 0;
	private int ioRation = 50;
	private int ioSubThreadCount = 0;
	private String requestToken = "ticket";
	
	public HashMap<String, String> getSessionByPath() {
		return sessionByPath;
	}
	
	//固定写死加载的netty配置文件
	@Override
	protected void load(String filename)
			throws ConfigurationException, FileNotFoundException, IOException{
		super.load(nettyConfigFile);
	}

	@Override
	protected void fillValues(Properties p) throws ConfigurationException {
		String value = p.getProperty("useTcpNoDelay");
		if (value != null){
			if (value.trim().equals("true"))
				useTcpNoDelay = true;
			else if (value.trim().equals("false"))
				useTcpNoDelay = false;
		}
		value = p.getProperty("soLinger");
		if (value != null)
		try{
			soLinger = Integer.parseInt(value);
		}catch(Exception e){			
		}
		value = p.getProperty("receiveBufferSize");
		if (value != null)
		try{
			if (Integer.parseInt(value) > 0)
				receiveBufferSize = Integer.parseInt(value);
		}catch(Exception e){			
		}	
		
		value = p.getProperty("sendBufferSize");
		if (value != null)
		try{
			if (Integer.parseInt(value) > 0)
				sendBufferSize = Integer.parseInt(value);
		}catch(Exception e){			
		}
		
		String sessionStr = p.getProperty("sessionByPath");
		if (sessionStr != null){
			sessionByPath = new HashMap<>();
			String[] str = sessionStr.split(";");
			for(String s: str)
				sessionByPath.put(s.substring(0,s.indexOf("=")),s.substring(s.indexOf("=")+1));
		}
		
		sessionStr = p.getProperty("sessionByClass");
		if (sessionStr != null){
			sessionByClass = new HashMap<>();
			String[] str = sessionStr.split(";");
			for(String s: str)
				sessionByClass.put(s.substring(0,s.indexOf("=")),s.substring(s.indexOf("=")+1));
		}
		
		
		value = p.getProperty("permissionCheck");
		if (value != null){
			value = value.trim();
			if (value.length() > 2 && value.startsWith("/") && value.endsWith("/"))
				permissionCheck = value;
		}
		value = p.getProperty("connectTimeoutMillis");
		if (value != null){
			value = value.trim();
			try{
				if (Integer.parseInt(value) > 0)
					connectTimeoutMillis = Integer.parseInt(value);
			}catch(Exception e){				
			}
		}
		
		value = p.getProperty("keepAlive");
		if (value != null){
			if (value.trim().equals("true"))
				keepAlive = true;
			else if (value.trim().equals("false"))
				keepAlive = false;
		}
		value = p.getProperty("reuseAddress");
		if (value != null){
			if (value.trim().equals("true"))
				reuseAddress = true;
			else if (value.trim().equals("false"))
				reuseAddress = false;
		}
		
		value = p.getProperty("maxContentSize");
		if (value != null){
			value = value.trim();
			try{
				if (Integer.parseInt(value) > 0)
					maxContentSize = Integer.parseInt(value);
			}catch(Exception e){				
			}
		}
		 
		 
		value = p.getProperty("executorThreadCount");
		if (value != null){
			value = value.trim();
			try{				
				executorThreadCount = Integer.parseInt(value);
			}catch(Exception e){				
			}
		} 	
		value = p.getProperty("stopCommandStr");
		if (value != null && value.trim().length() > 0)
			stopCommandStr = value;
		
		value = p.getProperty("httpRequestHandler");
		if (value != null && value.trim().length() > 0)
			httpRequestHandler = value;
		
		value = p.getProperty("listenPort");
		if (value != null){
			value = value.trim();
			try{				
				listenPort = Integer.parseInt(value);
			}catch(Exception e){				
			}
		} 
		
		value = p.getProperty("shutdownPort");
		if (value != null){
			value = value.trim();
			try{				
				shutdownPort = Integer.parseInt(value);
			}catch(Exception e){				
			}
		} 
		
		value = p.getProperty("ioThreadCount");
		if (value != null){
			value = value.trim();
			try{				
				ioThreadCount = Integer.parseInt(value);
			}catch(Exception e){				
			}
		}
		
		value = p.getProperty("ioSubThreadCount");
		if (value != null){
			value = value.trim();
			try{				
				ioSubThreadCount = Integer.parseInt(value);
			}catch(Exception e){				
			}
		}
			
		value = p.getProperty("backLog");
		if (value != null){
			value = value.trim();
			try{				
				backLog = Integer.parseInt(value);
			}catch(Exception e){				
			}
		}
		
		value = p.getProperty("readIdleTimeOut");
		if (value != null){
			value = value.trim();
			try{				
				readIdleTimeOut = Long.parseLong(value);
			}catch(Exception e){				
			}
		}
		
		value = p.getProperty("writeIdleTimeOut");
		if (value != null){
			value = value.trim();
			try{				
				writeIdleTimeOut = Long.parseLong(value);
			}catch(Exception e){				
			}
		} 
		
		value = p.getProperty("bothIdleTimeOut");
		if (value != null){
			value = value.trim();
			try{				
				bothIdleTimeOut = Long.parseLong(value);
			}catch(Exception e){				
			}
		}
		
		value = p.getProperty("ioRation");
		if (value != null){
			value = value.trim();
			try{				
				ioRation  = Integer.parseInt(value);
			}catch(Exception e){				
			}
			if (ioRation <= 0 || ioRation > 100)
				ioRation = 50;
		}
		
		value = p.getProperty("requestToken");
		if (value != null){
			requestToken = value.trim();
		}
				
		springConfigFile = p.getProperty("springConfigFile");		 
	}
	
	public boolean isUseTcpNoDelay() {
		return useTcpNoDelay;
	}

	public int getSoLinger() {
		return soLinger;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public int getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public int getMaxContentSize() {
		return maxContentSize;
	}

	public String getPermissionCheck() {
		return permissionCheck;
	}

	public int getExecutorThreadCount() {
		return executorThreadCount;
	}

	public String getStopCommandStr() {
		return stopCommandStr;
	}

	public void setStopCommandStr(String stopCommandStr) {
		this.stopCommandStr = stopCommandStr;
	}

	public String getHttpRequestHandler() {
		return httpRequestHandler;
	}

	public String getSpringConfigFile() {
		return springConfigFile;
	}

	public int getListenPort() {
		return listenPort;
	}

	public int getShutdownPort() {
		return shutdownPort;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public int getIoThreadCount() {		
		return ioThreadCount ;
	}

	public int getBackLog() {
		return backLog;
	}

	/**
	 * 单位  秒,通道readIdleTimeOut秒没有数据读后，关闭
	 * @return
	 */
	public long getReadIdleTimeOut() {
		return readIdleTimeOut;
	}

	/**
	 * 单位  秒通道writeIdleTimeOut秒没有数据写后，关闭
	 * @return
	 */
	public long getWriteIdleTimeOut() {
		return writeIdleTimeOut;
	}

	/**
	 * 单位  秒 通道writeIdleTimeOut秒没有数据读写后，关闭
	 * @return
	 */
	public long getBothIdleTimeOut() {
		return bothIdleTimeOut;
	}

	public int getIoRation() {
		return ioRation;
	}

	public int getIoSubThreadCount() {
		return ioSubThreadCount ;
	}

	public HashMap<String,String> getSessionByClass() {
		return sessionByClass;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

}
