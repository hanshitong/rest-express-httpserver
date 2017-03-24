/*
 * Copyright 2009-2012, Strategic Gains, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restexpress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.restexpress.domain.metadata.RouteMetadata;
import org.restexpress.domain.metadata.ServerMetadata;
import org.restexpress.exception.DefaultExceptionMapper;
import org.restexpress.exception.ExceptionMapping;
import org.restexpress.exception.ServiceException;
import org.restexpress.intf.SpringInitCompleteAware;
import org.restexpress.intf.SystemStatIntf;
import org.restexpress.pipeline.DefaultRequestHandler;
import org.restexpress.pipeline.MessageObserver;
import org.restexpress.pipeline.PipelineInitializer;
import org.restexpress.pipeline.Postprocessor;
import org.restexpress.pipeline.Preprocessor;
import org.restexpress.pipeline.WebSocketHandler;
import org.restexpress.pipeline.factory.AbstractChannelHandlerFactory;
import org.restexpress.pipeline.factory.HttpServerChannelHandlerFactory;
import org.restexpress.plugin.Plugin;
import org.restexpress.response.DefaultHttpResponseWriter;
import org.restexpress.route.RouteBuilder;
import org.restexpress.route.RouteDeclaration;
import org.restexpress.route.RouteResolver;
import org.restexpress.route.parameterized.ParameterizedRouteBuilder;
import org.restexpress.route.regex.RegexRouteBuilder;
import org.restexpress.serialization.DefaultSerializationProvider;
import org.restexpress.serialization.SerializationProvider;
import org.restexpress.settings.RouteDefaults;
import org.restexpress.settings.ServerSettings;
import org.restexpress.settings.SocketSettings;
import org.restexpress.util.Callback;
import org.restexpress.util.Configuration;
import org.restexpress.util.DefaultShutdownHook;
import org.restexpress.util.Environment;
import org.restexpress.util.StringUtil;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Primary entry point to create a RestExpress service. All that's required is a
 * RouteDeclaration. By default: port is 8081, serialization format is JSON,
 * supported formats are JSON and XML.
 *
 * @author toddf
 */
public class RestExpress
{
//	static
//	{
//		ResourceLeakDetector.setLevel(Level.DISABLED);
//	}
	
    private static final ChannelGroup allChannels = new DefaultChannelGroup("RestExpress", GlobalEventExecutor.INSTANCE);	
	
	public static final String DEFAULT_NAME = "RestExpress";
	public static final int DEFAULT_PORT = 8081;

	private static SerializationProvider DEFAULT_SERIALIZATION_PROVIDER = null;

	private SocketSettings socketSettings = new SocketSettings();
	private ServerSettings serverSettings = new ServerSettings();
	private RouteDefaults routeDefaults = new RouteDefaults();
	private boolean enforceHttpSpec = false;
	private boolean useSystemOut;
	private ServerBootstrapFactory bootstrapFactory = new ServerBootstrapFactory();

	private List<MessageObserver> messageObservers = new ArrayList<MessageObserver>();
	private List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();
	private List<Postprocessor> postprocessors = new ArrayList<Postprocessor>();
	private List<Postprocessor> finallyProcessors = new ArrayList<Postprocessor>();
	private ExceptionMapping exceptionMap = new DefaultExceptionMapper();
	private List<Plugin> plugins = new ArrayList<Plugin>();
	private RouteDeclaration routeDeclarations = new RouteDeclaration();
	 
	private SerializationProvider serializationProvider = null;
	
	//add by hanst
	private static ClassPathXmlApplicationContext springCtx = null;
	private static Configuration config = null;
	private static String SHUT_DOWN_COMMAND = null;

	//如果自定义了序列化，也需要读这个参数,默认不转义,可以做成配置或启动参数的形式
	private static boolean shouldOutboundEncode = false;
	
	private DefaultEventExecutorGroup defaultEventExecutorGroup = null;

	/**
	 * Change the default behavior for serialization.
	 * If no SerializationProcessor is set, default of DefaultSerializationProcessor is used,
	 * which uses Jackson for JSON, XStream for XML.
	 * 
	 * @param provider a SerializationProvider instance.
	 * @deprecated use setDefaultSerializationProvider()
	 */
	public static void setSerializationProvider(SerializationProvider provider)
	{
		setDefaultSerializationProvider(provider);
	}

	/**
	 * @return the default serialization provider.
	 * @deprecated Use getDefaultSerializationProvider()
	 */
	public static SerializationProvider getSerializationProvider()
	{
		return getDefaultSerializationProvider();
	}

	/**
	 * Change the default behavior for serialization.
	 * If no SerializationProvider is set, default of DefaultSerializationProvider is used,
	 * which uses Jackson for JSON, XStream for XML.
	 * 
	 * @param provider a SerializationProvider instance.
	 */
	public static void setDefaultSerializationProvider(SerializationProvider provider)
	{
		DEFAULT_SERIALIZATION_PROVIDER = provider;
	}

	/**
	 * Get the default serialization provider for RestExpress. If the value is
	 * unset DefaultSerializationProcessor is set as the default and returned.
	 * Otherwise, the previously-set value for the default is returned.
	 * 
	 * @return the default serialization provider.
	 */
	public static SerializationProvider getDefaultSerializationProvider()
	{
		if (DEFAULT_SERIALIZATION_PROVIDER == null)
		{
			DEFAULT_SERIALIZATION_PROVIDER = new DefaultSerializationProvider(isShouldOutboundEncode());
		}

		return DEFAULT_SERIALIZATION_PROVIDER;
	}

	/**
	 * Change the serialization provider for this server instance.
	 * If no SerializationProcessor is set, default of DefaultSerializationProcessor is used,
	 * which uses Jackson for JSON, XStream for XML.
	 * 
	 * @param provider a SerializationProvider instance.
	 * @return this RestExpress server instance.
	 */
	public RestExpress serializationProvider(SerializationProvider provider)
	{
		this.serializationProvider = provider;
		return this;
	}

	/**
	 * Get the serialization provider for this server instance. If none has
	 * been set, it is set to the default serialization processor and returned.
	 * Otherwise, the setting for this server is returned.
	 * 
	 * @return the SerializationProvider for this instance, or the default.
	 */
	public SerializationProvider serializationProvider()
	{
		if (serializationProvider == null)
		{
			serializationProvider(getDefaultSerializationProvider());
		}

		return serializationProvider;
	}

	/**
	 * Create a new RestExpress service. By default, RestExpress uses port 8081.
	 * Supports JSON, and XML, providing JSEND-style wrapped responses. And
	 * displays some messages on System.out. These can be altered with the
	 * setPort(), noJson(), noXml(), noSystemOut(), and useRawResponses() DSL
	 * modifiers, respectively, as needed.
	 * 
	 * <p/>
	 * The default input and output format for messages is JSON. To change that,
	 * use the setDefaultFormat(String) DSL modifier, passing the format to use
	 * by default. Make sure there's a corresponding SerializationProcessor for
	 * that particular format. The Format class has the basics.
	 * 
	 * <p/>
	 * This DSL was created as a thin veneer on Netty functionality. The bind()
	 * method simply builds a Netty pipeline and uses this builder class to
	 * create it. Underneath the covers, RestExpress uses Google GSON for JSON
	 * handling and XStream for XML processing. However, both of those can be
	 * swapped out using the putSerializationProcessor(String,
	 * SerializationProcessor) method, creating your own instance of
	 * SerializationProcessor as necessary.
	 */
	public RestExpress() throws Exception
	{
		super();
		setName(DEFAULT_NAME);
		useSystemOut();
		//add by hanst
		init(null);
	}
	
	//add by hanst
	public RestExpress(String[] args) throws Exception
	{
		super();
		setName(DEFAULT_NAME);
		useSystemOut();		
		init(args);
	}

	/**
	 * 
	 * @param command start/*
	 * @param listenPort 一个整数，侦听端口
	 * @param shutdownPort   一个整数，侦听端口,关闭应用使用
	 * @param threadCount 线程数
	 * @throws Exception 
	 */
	private void init(String[] args) throws Exception {
		config = Environment.load(new String[]{}, Configuration.class);
		if (args == null || args.length < 1) //默认启动
		{
			args = new String[]{"start",Integer.toString(config.getListenPort()),
					Integer.toString(config.getExecutorThreadCount()),Integer.toString(config.getShutdownPort())};
		}
		
		String command = args[0];
		if (command.equals("start") && args.length < 4){
			System.out.println("启动应用需要4个参数start listenPort threadCount shutdownPort ");
			System.out.println("停止应用需要2个参数"+  config.getStopCommandStr() +" shutdownPort ");
			return;
		}
		
		SHUT_DOWN_COMMAND = config.getStopCommandStr();
		String listenPort = args[1];
		if (command.equals(SHUT_DOWN_COMMAND)){
			int port = -1;
			try {
				port = Integer.parseInt(listenPort);
				System.out.println("正在关闭侦听端口:" + port + "的应用");
				shutdown(port);				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return;
		}
		
		int port = config.getListenPort();
		try {
			port = Integer.parseInt(listenPort);
			System.out.println("侦听端口:" + port);
		} catch (Exception e) {
			System.out.println(listenPort + "不是整数，端口使用默认值:" + port);
		}
		int threadCount = config.getExecutorThreadCount();
		try {
			threadCount = Integer.parseInt(args[2]);
			System.out.println("worker线程数:" + threadCount);
		} catch (Exception e) {
			System.out.println(args[2] + "不是整数(可以为0)，线程数目使用默认值:" + threadCount);
		}
		int ioMainThreadCount = config.getIoThreadCount(); 
		int ioSubThreadCount = config.getIoSubThreadCount();
		System.out.println("io main线程数:" + ioMainThreadCount);
		System.out.println("io sub线程数:" + ioSubThreadCount);
		setPort(port).setExecutorThreadCount(threadCount).
		    setIoMainThreadCount(ioMainThreadCount).setIoSubThreadCount(ioSubThreadCount)
			.setConnectTimeoutMillis(config.getConnectTimeoutMillis())
			.setKeepAlive(config.isKeepAlive()).setMaxContentSize(config.getMaxContentSize())
			.setReceiveBufferSize(config.getReceiveBufferSize())
			.setReuseAddress(config.isReuseAddress()).setSoLinger(config.getSoLinger())
			.setUseTcpNoDelay(config.isUseTcpNoDelay()).setSendBufferSize(config.getSendBufferSize()).
			setBackLog(config.getBackLog());			
		springCtx = new ClassPathXmlApplicationContext(config.getSpringConfigFile());
		//必须要先执行初始化spring后的事件，才执行defineRoutes,否则defineRoutes开始接收请求的时候，有东西没初始化完
		execSpringInitFinish();
		defineRoutes(this);
		//mapExceptions();
				bind();
//			    server.awaitShutdown();
		int shutdownPort = config.getShutdownPort();
		try{
			shutdownPort = Integer.parseInt(args[3]);
			System.out.println("关闭应用" + SHUT_DOWN_COMMAND + " " + shutdownPort);
		}catch(Exception e){
			System.out.println(args[3] + "不是整数，停止服务侦听端口使用默认值:" + shutdownPort);
		}
		registShutdownServer(shutdownPort);
	}

	private RestExpress setIoSubThreadCount(int ioSubThreadCount) {
		serverSettings.setIoSubThreadCount(ioSubThreadCount); 
		return this;
	}

	/*
	 * 向应用的侦听端口发送一个stop命令，应用收到stop命令执行后shutdownMonitor方法
	 */
	private static void shutdown(int port) {
		DataOutputStream dos = null; 
		try{
			Socket client = new Socket("127.0.0.1", port); 
			 dos = new DataOutputStream(client.getOutputStream());
			 dos.writeBytes(SHUT_DOWN_COMMAND);
			 dos.close();
			 client.close();
        } catch (Exception e) {
        	System.out.println("连接端口失败:" + port);
            return;
        }		
	}

	private void registShutdownServer(final int shutdownPort) {
        new Thread(new Runnable() {			
			@Override
			public void run() {				 
				ServerSocket myserver = null;
		        BufferedReader br = null;
		        try {
		        	myserver = new ServerSocket();
		        	myserver.bind(new InetSocketAddress("127.0.0.1", shutdownPort));
		        } catch (Exception e) {
		        	System.out.println("绑定端口失败:" + shutdownPort);
		            return;
		        }
		        
		        while(true)
		            try {
		                Socket sock = myserver.accept();//这里会阻塞，直到收到命令
		                sock.setSoTimeout(0); //本地通信设置较短时间
		                br = new BufferedReader(new InputStreamReader(sock.getInputStream()));		                
		                String readContent = br.readLine();
		                // 判断收到信息是否是停止标志,可以在这里获取更多信息		                
		                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
		                if (SHUT_DOWN_COMMAND.equals(readContent)) {		                	
		                	os.write("stop server..\r\n");		                	
		        			doShutdown(); 
		        			br.close();
		        			sock.close();
		        			myserver.close();		        		
		                    return;
		                }
		                if ("nettyconfig".equals(readContent)) {                    
		                	os.write("connectTimeout:" + config.getConnectTimeoutMillis()/1000);
		                	os.write("\r\n");
		                	os.write("receiveBufferSize:" + config.getReceiveBufferSize());
		                	os.write("\r\n");
		                	os.write("sendBufferSize:" + config.getSendBufferSize());
		                	os.write("\r\n");
		                	os.write("maxContentSize:" + config.getMaxContentSize());
		                	os.write("\r\n");
		                	os.write("useTcpNoDelay:" + config.isUseTcpNoDelay());
		                	os.write("\r\n");
		                	os.write("executorThreadCount:" + config.getExecutorThreadCount());
		                	os.write("\r\n");
		                	os.write("keepAlive:" + config.isKeepAlive());
		                	os.write("\r\n");
		                	os.write("permissionCheck:" + config.getPermissionCheck());
		                	os.write("\r\n");
		                	os.write("listen Port:" + config.getListenPort());
		                	os.write("\r\n");
		                	os.write("shutdown Port:" + config.getShutdownPort());
		                	os.write("\r\n");
		                	os.write("ioThreadCount:" + config.getIoThreadCount());
		                	os.write("\r\n");
		                	os.write("backLog:" + config.getBackLog());
		                	os.write("\r\n");
		                	os.write("soLinger:" + config.getSoLinger());
		                	os.write("\r\n");
		                }
		                else{		                	
		                	Map<String,SystemStatIntf> com = getSpringCtx().getBeansOfType(SystemStatIntf.class);
		                	
		                	if (readContent.equals("help")){
		                		for(String key: com.keySet()){			                		
		                			SystemStatIntf ssi = com.get(key);
		                			for(int i = 0; i < ssi.getCommand().length; i++){
		                				os.write(ssi.getCommand()[i] + ": " + ssi.commandDesc()[i]);
		                				os.write("\r\n");
		                			}
		                			os.write("\r\n");
		                		}
		                		os.write("nettyconfig: server startup param");	
		                	}
		                	else{				                	
		                		boolean found = false;
		                		for(String key: com.keySet()){
		                			SystemStatIntf ssi = com.get(key);
		                			for(int i = 0; i < ssi.getCommand().length; i++)
		                				if (readContent.equals(ssi.getCommand()[i])){
		                					os.write(ssi.getStat(readContent));
		                					os.write("\r\n");
		                					found = true;
		                				}
		                		}
		                		if (!found)
		                			os.write("unknown command:" + readContent + ",type help!");
		                	}
		                }
		                os.flush();
	                	os.close();
		                br.close();               		             
		        } catch (Exception e) {
		        	System.out.println(e.getMessage());
		        }
			}
		}).start();
    }

	private void doShutdown() {
		System.out.println("应用程序准备停止");
		shutdown(true);
		springCtx.destroy();		
	}
	
	/**
     * @param server
     * @param config
	 * @throws Exception 
	 * 
     */
    private void defineRoutes(RestExpress server) throws Exception
    {
		// This route supports GET, POST, PUT, DELETE echoing the 'echo' query-string parameter in the response.
    	// GET and DELETE are also supported but require an 'echo' header or query-string parameter.
//		server.uri("/echo/{delay_ms}", config.getEchoController());
		
		//text/plain格式	
		//.noSerialization();
		//使用格式进行输出xml/json
		//.performSerialization()

		// Waits the delay_ms number of milliseconds and responds with a 200.
		// Supports GET, PUT, POST, DELETE methods.
//		server.uri("/success/{delay_ms}.{format}", 
//				((Controller)config.getSuccessController().getClass().getAnnotations()[0]).);

		// Waits the delay_ms number of milliseconds and responds with the
		// specified HTTP response code.
		// Supports GET, PUT, POST, DELETE methods.
//		server.uri("/status/{delay_ms}/{http_response_code}.{format}", config.getStatusController());
    	
    	//从spring里加载
    	Map<String, Object> map = springCtx.getBeansWithAnnotation(Controller.class);
//    	Map<String, ControllerInterface> map = springCtx.getBeansOfType(ControllerInterface.class);
    	
    	for(String key: map.keySet()){
    		Object intf = map.get(key);
    		Class cls = intf.getClass();
    		RequestMapping an = (RequestMapping) cls.getAnnotation(RequestMapping.class);    		
    		String uri = (an == null || an.value()[0].length() == 0)?StringUtil.getMaping(cls.getName()):an.value()[0];
    		//只映射本类的方法，不包含父类的方法
    		Method[] ms = cls.getDeclaredMethods();
    		for(Method m: ms){
    			if (m.getModifiers() != 1) continue;  
    			String url = StringUtil.getRequestMapping(m, uri);
    			server.uri(url, intf);
    			System.out.println("map { " + url + " } to " + cls.getName() + "." + m.getName());
    		}
    	}
    }

	public String getBaseUrl()
	{
		return routeDefaults.getBaseUrl();
	}

	public RestExpress setBaseUrl(String baseUrl)
	{
		routeDefaults.setBaseUrl(baseUrl);
		return this;
	}

	/**
	 * Get the name of this RestExpress service.
	 *
	 * @return a String representing the name of this service suite.
	 */
	public String getName()
	{
		return serverSettings.getName();
	}

	/**
	 * Set the name of this RestExpress service suite.
	 *
	 * @param name
	 *            the name.
	 * @return the RestExpress instance to facilitate DSL-style method chaining.
	 */
	public RestExpress setName(String name)
	{
		serverSettings.setName(name);
		return this;
	}
	
	private RestExpress setBackLog(int backLog) {
		serverSettings.setBackLog(backLog);
		return this;
	}

	public int getPort()
	{
		return serverSettings.getPort();
	}

	public RestExpress setPort(int port)
	{
		serverSettings.setPort(port);
		return this;
	}
	
	public ServerSettings getServerSettings(){
		return serverSettings;
	}

	public String getHostname()
	{
		return serverSettings.getHostname();
	}

	public boolean hasHostname()
	{
		return serverSettings.hasHostname();
	}

	/**
	 * Set the hostname or IP address that the server will listen on.
	 * 
	 * @param hostname hostname or IP address.
	 */
	public void setHostname(String hostname)
	{
		serverSettings.setHostname(hostname);
	}

	public RestExpress addMessageObserver(MessageObserver observer)
	{
		if (!messageObservers.contains(observer))
		{
			messageObservers.add(observer);
		}

		return this;
	}

	public List<MessageObserver> getMessageObservers()
	{
		return Collections.unmodifiableList(messageObservers);
	}

	/**
	 * Add a Preprocessor instance that gets called before an incoming message
	 * gets processed. Preprocessors get called in the order in which they are
	 * added. To break out of the chain, simply throw an exception.
	 *
	 * @param processor
	 * @return
	 */
	public RestExpress addPreprocessor(Preprocessor processor)
	{
		if (!preprocessors.contains(processor))
		{
			preprocessors.add(processor);
		}

		return this;
	}

	public List<Preprocessor> getPreprocessors()
	{
		return Collections.unmodifiableList(preprocessors);
	}

	/**
	 * Add a Postprocessor instance that gets called after an incoming message is
	 * processed. A Postprocessor is useful for augmenting or transforming the
	 * results of a controller or adding headers, etc. Postprocessors get called
	 * in the order in which they are added.
	 * Note however, they do NOT get called in the case of an exception or error
	 * within the route.
	 * 
	 * @param processor
	 * @return
	 */
	public RestExpress addPostprocessor(Postprocessor processor)
	{
		if (!postprocessors.contains(processor))
		{
			postprocessors.add(processor);
		}

		return this;
	}

	public List<Postprocessor> getPostprocessors()
	{
		return Collections.unmodifiableList(postprocessors);
	}

	/**
	 * Add a Postprocessor instance that gets called right before the serialized
	 * message is sent to the client, or in a finally block after the message is
	 * processed, if an error occurs.  Finally processors are Postprocessor instances
	 * that are guaranteed to run even if an error is thrown from the controller
	 * or somewhere else in the route.  A Finally Processor is useful for adding
	 * headers or transforming results even during error conditions. Finally
	 * processors get called in the order in which they are added.
	 * 
	 * If an exception is thrown during finally processor execution, the finally processors
	 * following it are executed after printing a stack trace to the System.err stream.
	 * 
	 * @param processor
	 * @return RestExpress for method chaining.
	 */
	public RestExpress addFinallyProcessor(Postprocessor processor)
	{
		if (!finallyProcessors.contains(processor))
		{
			finallyProcessors.add(processor);
		}

		return this;
	}

	public List<Postprocessor> getFinallyProcessors()
	{
		return Collections.unmodifiableList(finallyProcessors);
	}

	public boolean shouldUseSystemOut()
	{
		return useSystemOut;
	}

	public RestExpress setUseSystemOut(boolean useSystemOut)
	{
		this.useSystemOut = useSystemOut;
		return this;
	}

	public RestExpress setEnforceHttpSpec(boolean enforceHttpSpec)
	{
		this.enforceHttpSpec = enforceHttpSpec;
		return this;
	}

	public RestExpress enforceHttpSpec()
	{
		setEnforceHttpSpec(true);
		return this;
	}

	public RestExpress useSystemOut()
	{
		setUseSystemOut(true);
		return this;
	}

	public RestExpress noSystemOut()
	{
		setUseSystemOut(false);
		return this;
	}

	public boolean useTcpNoDelay()
	{
		return socketSettings.useTcpNoDelay();
	}

	public RestExpress setUseTcpNoDelay(boolean useTcpNoDelay)
	{
		socketSettings.setUseTcpNoDelay(useTcpNoDelay);
		return this;
	}

	public boolean useKeepAlive()
	{
		return serverSettings.isKeepAlive();
	}

	public RestExpress setKeepAlive(boolean useKeepAlive)
	{
		serverSettings.setKeepAlive(useKeepAlive);
		return this;
	}

	public boolean shouldReuseAddress()
	{
		return serverSettings.isReuseAddress();
	}

	public RestExpress setReuseAddress(boolean reuseAddress)
	{
		serverSettings.setReuseAddress(reuseAddress);
		return this;
	}

	public int getSoLinger()
	{
		return socketSettings.getSoLinger();
	}

	public RestExpress setSoLinger(int soLinger)
	{
		socketSettings.setSoLinger(soLinger);
		return this;
	}

	public int getReceiveBufferSize()
	{
		return socketSettings.getReceiveBufferSize();
	}
	
	public int getSendBufferSize()
	{
		return socketSettings.getSendBufferSize();
	}
	

	public RestExpress setReceiveBufferSize(int receiveBufferSize)
	{
		socketSettings.setReceiveBufferSize(receiveBufferSize);
		return this;
	}
	
	public RestExpress setSendBufferSize(int sendBufferSize)
	{
		socketSettings.setSendBufferSize(sendBufferSize);
		return this;
	}

	public int getConnectTimeoutMillis()
	{
		return socketSettings.getConnectTimeoutMillis();
	}

	public RestExpress setConnectTimeoutMillis(int connectTimeoutMillis)
	{
		socketSettings.setConnectTimeoutMillis(connectTimeoutMillis);
		return this;
	}

	/**
	 * @param elementName
	 * @param theClass
	 * @return
	 */
	public RestExpress alias(String elementName, Class<?> theClass)
	{
		routeDefaults.addXmlAlias(elementName, theClass);
		return this;
	}

	public <T extends Exception, U extends ServiceException> RestExpress mapException(
	    Class<T> from, Class<U> to)
	{
		exceptionMap.map(from, to);
		return this;
	}

	public RestExpress setExceptionMap(ExceptionMapping mapping)
	{
		this.exceptionMap = mapping;
		return this;
	}


	/**
	 * Set the number of NIO/HTTP-handling worker threads.  This
	 * value controls the number of simultaneous connections the
	 * application can handle.
	 * 
	 * The default (if this value is not set, or set to zero) is
	 * the Netty default, which is 2 times the number of processors
	 * (or cores).
	 * 
	 * @param value the number of desired NIO worker threads.
	 * @return the RestExpress instance.
	 */
	public RestExpress setIoMainThreadCount(int value)
	{
		serverSettings.setIoMainThreadCount(value);
		return this;
	}

	/**
	 * Returns the number of background request-handling (executor) threads.
	 *
	 * @return the number of executor threads.
	 */
	public int getExecutorThreadCount()
	{
		return serverSettings.getExecutorThreadPoolSize();
	}

	/**
	 * Set the number of background request-handling (executor) threads.
	 * This value controls the number of simultaneous blocking requests that
	 * the server can handle.  For longer-running requests, a higher number
	 * may be indicated.
	 * 
	 * For VERY short-running requests, a value of zero will cause no
	 * background threads to be created, causing all processing to occur in
	 * the NIO (front-end) worker thread.
	 * 
	 * @param value the number of executor threads to create.
	 * @return the RestExpress instance.
	 */
	public RestExpress setExecutorThreadCount(int value)
	{
		serverSettings.setExecutorThreadPoolSize(value);
		return this;
	}

	/**
	 * Set the maximum length of the content in a request. If the length of the content exceeds this value,
	 * the server closes the connection immediately without sending a response.
	 * 
	 * @param size the maximum size in bytes.
	 * @return the RestExpress instance.
	 */
	public RestExpress setMaxContentSize(int size)
	{
		serverSettings.setMaxContentSize(size);
		return this;
	}

	/**
	 * Can be called after routes are defined to augment or get data from
	 * all the currently-defined routes.
	 * 
	 * @param callback a Callback implementor.
	 */
	public void iterateRouteBuilders(Callback<RouteBuilder> callback)
	{
		routeDeclarations.iterateRouteBuilders(callback);
	}

	public Channel bind()
	{
		Channel channel = bind((getPort() > 0 ? getPort() : DEFAULT_PORT));		
		return channel; 
	}
	
	private void execSpringInitFinish() {
		Map<String, ApplicationContextAware> awares = springCtx
				.getBeansOfType(ApplicationContextAware.class);
		for(String key: awares.keySet())
			awares.get(key).setApplicationContext(springCtx);
		
		Map<String, SpringInitCompleteAware> results = springCtx
				.getBeansOfType(SpringInitCompleteAware.class);
		List<SpringInitCompleteAware> lists = new ArrayList<>(results.size());
		for (String key : results.keySet())
			lists.add(results.get(key));
		Collections.sort(lists, new Comparator<SpringInitCompleteAware>() {
			@Override
			public int compare(SpringInitCompleteAware o1,
					SpringInitCompleteAware o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
		for (SpringInitCompleteAware sic : lists) {
			try {
				sic.execute();
			} catch (Exception e) {
				System.out.println("Spring初始化完毕后执行" + sic.getClass().getName()
						+ "错误:" + e.getMessage());
			}
		}

	}
	
	public DefaultEventExecutorGroup getDefaultEventExecutorGroup(){
		if (getExecutorThreadCount() > 0 && defaultEventExecutorGroup == null)
			defaultEventExecutorGroup = new DefaultEventExecutorGroup(getExecutorThreadCount());
		return defaultEventExecutorGroup;
	}
	/**
	 * Build a default request handler. Used instead of bind() so it may be used
	 * injected into any existing Netty pipeline.
	 * 
	 * @return ChannelHandler
	 */
	
	
	
	/**
	 *  用户自定义 ---
	 *      是
	 *          shareable? 
	 *                    是    从spring加载用户定义的RequestHandler后，保存到perhandler返回
	 *                    否    从spring加载用户定义的RequestHandler后, 返回
	 *      否    shareable?
	 *                    是    创建DefaultRequestHandler,保存到到perhandler返回
	 *                    否    创建DefaultRequestHandler返回
	 *             
	 * @return
	 *
	*/
	public ChannelHandler buildRequestHandler(DefaultRequestHandler requestHandler)
	{
		// shareable 初始化后，直接返回
		if (requestHandler == null)
			// 用户自定义，且非shareable情况
			requestHandler = getSpringCtx().getBean(DefaultRequestHandler.class);

		// 还没有初始化，则初始化requestHandler
		if (requestHandler.getResponseWriter() == null) {
			requestHandler.setResponseWriter(new DefaultHttpResponseWriter());
			requestHandler.setRouteResolver(createRouteResolver());
			requestHandler.setShouldEnforceHttpSpec(enforceHttpSpec);
			requestHandler.setSerializationProvider(serializationProvider());
			// Add MessageObservers to the request handler here, if desired...
			requestHandler.addMessageObserver(messageObservers.toArray(new MessageObserver[0]));
			requestHandler.setExceptionMap(exceptionMap);
			// Add pre/post processors to the request handler here...
			addPreprocessors(requestHandler);
			addPostprocessors(requestHandler);
			addFinallyProcessors(requestHandler);
		}
		return requestHandler; 
	}

	/**
	 * The last call in the building of a RestExpress server, bind() causes
	 * Netty to bind to the listening address and process incoming messages.
	 *
	 * @return Channel
	 */
	public Channel bind(int port)
	{
		setPort(port);

		if (hasHostname())
		{
			return bind(new InetSocketAddress(getHostname(), port));
		}

		return bind(new InetSocketAddress(port));
	}

	/**
	 * Bind to a particular hostname or IP address and port.
	 * 
	 * @param hostname
	 * @param port
	 * @return
	 */
	public Channel bind(String hostname, int port)
	{
		setPort(port);
		return bind(new InetSocketAddress(hostname, port));
	}

	public Channel bind(InetSocketAddress ipAddress)
	{
		ServerBootstrap bootstrap = bootstrapFactory.newServerBootstrap(serverSettings.getIoMainThreadCount(),
				serverSettings.getIoSubThreadCount());
		AbstractChannelHandlerFactory channelHandlerFactory = null;
		try{
			channelHandlerFactory = springCtx.getBean(AbstractChannelHandlerFactory.class);
		}catch(Exception e){
			channelHandlerFactory = new HttpServerChannelHandlerFactory();
			System.out.println(e.getMessage());
		}
		System.out.println(String.format("channel handler factory:%s",channelHandlerFactory.getClass().getName()));
		System.out.println(String.format("channel handler desc:%s",channelHandlerFactory.getDesc()));
		bootstrap.childHandler(new PipelineInitializer().setChannelHandlerFactory(channelHandlerFactory)
				.setRestExpress(this));
		setIoRation(bootstrap);
		setBootstrapOptions(bootstrap);

		// Bind and start to accept incoming connections.
		if (shouldUseSystemOut())
		{
			System.out.println(getName() + " server listening on port " + ipAddress.toString());
		}

		Channel channel = bootstrap.bind(ipAddress).channel();
		allChannels.add(channel);

		bindPlugins();
		return channel;
	}

	//TODO: netty5 不需要这么麻烦
	private void setIoRation(ServerBootstrap bootstrap) {
		if (bootstrap.group() != null){
			Iterator<EventExecutor> it = bootstrap.group().iterator();
			while(it.hasNext()){
				EventExecutor ee = it.next(); 
//				if (ee instanceof EpollEventLoop) 这个访问不了?
//					((EpollEventLoop)ee).setIoRatio(config.getIoRation());
				if (ee instanceof NioEventLoop)
					((NioEventLoop)ee).setIoRatio(config.getIoRation());
			}
		}
		if (bootstrap.childGroup() != null){
			Iterator<EventExecutor> it = bootstrap.childGroup().iterator();
			while(it.hasNext()){
				EventExecutor ee = it.next(); 
				if (ee instanceof NioEventLoop)
					((NioEventLoop)ee).setIoRatio(config.getIoRation());
			}
		}	
	}

	private void setBootstrapOptions(ServerBootstrap bootstrap)
	{
		bootstrap.option(ChannelOption.SO_KEEPALIVE, useKeepAlive());
		bootstrap.option(ChannelOption.SO_BACKLOG, serverSettings.getBackLog());
	    bootstrap.option(ChannelOption.TCP_NODELAY, useTcpNoDelay());		
		bootstrap.option(ChannelOption.SO_REUSEADDR, shouldReuseAddress());
		bootstrap.option(ChannelOption.SO_LINGER, getSoLinger());
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeoutMillis());
		bootstrap.option(ChannelOption.SO_RCVBUF, getReceiveBufferSize());
		bootstrap.option(ChannelOption.SO_SNDBUF, getSendBufferSize());
		bootstrap.option(ChannelOption.MAX_MESSAGES_PER_READ, Integer.MAX_VALUE);
		//I/O 线程使用，池化direct buffer,这个版本先不用direct 内存,使用websocket用非heap内存分配模型有内存泄露
		bootstrap.option(ChannelOption.ALLOCATOR,new PooledByteBufAllocator(false));
		
		//非I/O 线程使用，池化heap buffer,在4.0.36netty,使用websocket用非heap内存分配模型有内存泄露
		bootstrap.childOption(ChannelOption.ALLOCATOR,new PooledByteBufAllocator(false));
		
	    bootstrap.childOption(ChannelOption.MAX_MESSAGES_PER_READ, Integer.MAX_VALUE);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, getReceiveBufferSize());
//		bootstrap.childOption(ChannelOption.SO_SNDBUF, getSendBufferSize());
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, shouldReuseAddress());
//		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, useKeepAlive());
	}

	/**
	 * Used in main() to install a default JVM shutdown hook and shut down the
	 * server cleanly. Calls shutdown() when JVM termination detected. To
	 * utilize your own shutdown hook(s), install your own shutdown hook(s) and
	 * call shutdown() instead of awaitShutdown().
	 */
	public void awaitShutdown()
	{
		Runtime.getRuntime().addShutdownHook(new DefaultShutdownHook(this));
		boolean interrupted = false;

		do
		{
			try
			{
				Thread.sleep(300);
			}
			catch (InterruptedException e)
			{
				interrupted = true;
			}
		}
		while (!interrupted);
	}

	/**
	 * Releases all resources associated with this server so the JVM can
	 * shutdown cleanly. Call this method to finish using the server. To utilize
	 * the default shutdown hook in main() provided by RestExpress, call
	 * awaitShutdown() instead.
	 * <p/>
	 * Same as shutdown(false);
	 */
	public void shutdown()
	{
		shutdown(false);
	}

	/**
	 * Releases all resources associated with this server so the JVM can
	 * shutdown cleanly. Call this method to finish using the server. To utilize
	 * the default shutdown hook in main() provided by RestExpress, call
	 * awaitShutdown() instead.
	 * 
	 * @param shouldWait true if shutdown() should wait for the shutdown of each thread group.
	 */
	public void shutdown(boolean shouldWait)
	{
		ChannelGroupFuture channelFuture = allChannels.close();
		bootstrapFactory.shutdownGracefully(shouldWait);
		channelFuture.awaitUninterruptibly();
		shutdownPlugins();
	}

	/**
	 * @return
	 */
	private RouteResolver createRouteResolver()
	{
		return new RouteResolver(routeDeclarations.createRouteMapping(routeDefaults));
	}

	/**
	 * Retrieve metadata about the routes in this RestExpress server.
	 *
	 * @return ServerMetadata instance.
	 */
	public ServerMetadata getRouteMetadata()
	{
		ServerMetadata m = new ServerMetadata();
		m.setName(getName());
		m.setPort(getPort());
		// TODO: create a good substitute for this...
		// m.setDefaultFormat(getDefaultFormat());
		// m.addAllSupportedFormats(getResponseProcessors().keySet());
		m.addAllRoutes(routeDeclarations.getMetadata());
		return m;
	}

	/**
	 * Retrieve the named routes in this RestExpress server, creating a Map of
	 * them by name, with the value portion being populated with the URL
	 * pattern. Any '.{format}' portion of the URL pattern is omitted.
	 * <p/>
	 * If the Base URL is set, it is included in the URL pattern.
	 * <p/>
	 * Only named routes are included in the output.
	 *
	 * @return a Map of Route Name/URL pairs.
	 */
	public Map<String, String> getRouteUrlsByName()
	{
		final Map<String, String> urlsByName = new HashMap<String, String>();

		iterateRouteBuilders(new Callback<RouteBuilder>()
		{
			@Override
			public void process(RouteBuilder routeBuilder)
			{
				RouteMetadata route = routeBuilder.asMetadata();

				if (route.getName() != null)
				{
					urlsByName.put(route.getName(), getBaseUrl()
					    + route.getUri().getPattern().replace(".{format}", ""));
				}
			}
		});

		return urlsByName;
	}

	public RestExpress registerPlugin(Plugin plugin)
	{
		if (!plugins.contains(plugin))
		{
			plugins.add(plugin);
			plugin.register(this);
		}

		return this;
	}

	private void bindPlugins()
	{
		for (Plugin plugin : plugins)
		{
			plugin.bind(this);
		}
	}

	private void shutdownPlugins()
	{
		for (Plugin plugin : plugins)
		{
			plugin.shutdown(this);
		}
	}

	/**
	 * @param requestHandler
	 */
	private void addPreprocessors(DefaultRequestHandler requestHandler)
	{
		for (Preprocessor processor : getPreprocessors())
		{
			requestHandler.addPreprocessor(processor);
		}
	}

	/**
	 * @param requestHandler
	 */
	private void addPostprocessors(DefaultRequestHandler requestHandler)
	{
		for (Postprocessor processor : getPostprocessors())
		{
			requestHandler.addPostprocessor(processor);
		}
	}

	/**
	 * @param requestHandler
	 */
	private void addFinallyProcessors(DefaultRequestHandler requestHandler)
	{
		for (Postprocessor processor : getFinallyProcessors())
		{
			requestHandler.addFinallyProcessor(processor);
		}
	}

	// SECTION: ROUTE CREATION

	public ParameterizedRouteBuilder uri(String uriPattern, Object controller)
	{
		return routeDeclarations.uri(uriPattern, controller, routeDefaults);
	}

	public RegexRouteBuilder regex(String uriPattern, Object controller)
	{
		return routeDeclarations.regex(uriPattern, controller, routeDefaults);
	}

	/**
	 * 实现ApplicationContextAware获得spring context
	 * @return
	 */
	 
	public static ClassPathXmlApplicationContext getSpringCtx() {
		return springCtx;
	}

	public static Configuration getConfig() {
		return config;
	}

	//unit second
	public long getReadIdleTimeOut() {
		return config.getReadIdleTimeOut();
	}

	public long getWriteIdleTimeOut() {
		return config.getWriteIdleTimeOut();
	}

	public long getBothIdleTimeOut() {
		return config.getBothIdleTimeOut();
	}

	public static boolean isShouldOutboundEncode() {
		return shouldOutboundEncode;
	}

	public static void setShouldOutboundEncode(boolean value) {
		shouldOutboundEncode = value;
	}

}
