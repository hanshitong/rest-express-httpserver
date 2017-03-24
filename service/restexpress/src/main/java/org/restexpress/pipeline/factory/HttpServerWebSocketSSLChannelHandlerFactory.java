package org.restexpress.pipeline.factory;

import java.util.concurrent.atomic.AtomicBoolean;

import org.restexpress.RestExpress;
import org.restexpress.pipeline.WebSocketHandler;
import io.netty.channel.ChannelPipeline;

/**
 * http server且支持websocket,WebSocketHandler只是实现了握手，心跳，业务逻辑需要自己继承WebSocketHandler
 * 并且实现processWebsocketRequest方法,可以再次构造一些
 * @author hanst
 *
 */
public class HttpServerWebSocketSSLChannelHandlerFactory extends HttpServerSSLChannelHandlerFactory {	
	private static AtomicBoolean userDefineReq = new AtomicBoolean(true);
	private static WebSocketHandler perhandler = null;
	public static final String HANDLER_NAME_WEBSOCKETHANDLER = "websockethandler";
	
	@Override
	public void build(ChannelPipeline pipeline,RestExpress restExpress,Object ...args) {
		super.build(pipeline, restExpress, args);
		//如果是用户自定义的，根据用户决定，是否放业务线程池执行,如果用户自己实现，需要注意线程安全	
		try{
			WebSocketHandler handler = perhandler; 
			if (handler == null && userDefineReq.get() == true){ 
				handler = restExpress.getSpringCtx().getBean(WebSocketHandler.class);
				if (handler.isSharable()){					
					perhandler = handler;  
				}
			}				 
			pipeline.addBefore(restExpress.getDefaultEventExecutorGroup(), 
					HttpServerChannelHandlerFactory.HANDLER_NAME_REQHANDLER,
					HANDLER_NAME_WEBSOCKETHANDLER,handler != null?handler:new WebSocketHandler());			 
		}catch(Exception e){	
			userDefineReq.set(false);
			pipeline.addBefore(HttpServerChannelHandlerFactory.HANDLER_NAME_REQHANDLER,
					HANDLER_NAME_WEBSOCKETHANDLER,new WebSocketHandler());
		}		 
	}
	
	@Override
	public String getDesc() {
		return "http server with websocket and ssl support";
	}
}
