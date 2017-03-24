package org.restexpress.pipeline.factory;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.restexpress.RestExpress;
import org.restexpress.intf.SpringInitCompleteAware;
import org.restexpress.pipeline.WebSocketHandler;
import io.netty.channel.ChannelPipeline;

/**
 * http server且支持websocket,WebSocketHandler只是实现了握手，心跳，业务逻辑需要自己继承WebSocketHandler
 * 并且实现processWebsocketRequest方法,可以再次构造一些
 * @author hanst
 *
 */
public class HttpServerWebSocketChannelHandlerFactory extends HttpServerChannelHandlerFactory implements SpringInitCompleteAware {
	 
	private WebSocketHandler perhandler = null;
	
	public static final String HANDLER_NAME_WEBSOCKETHANDLER = "websockethandler";
	
	@Override 
	public void execute(){
		super.execute();
		try{
			perhandler = RestExpress.getSpringCtx().getBean(WebSocketHandler.class);
			if (perhandler.isSharable())									
				System.out.println("用户自定义了sharable websocket handler:" + perhandler.getClass().getName());
			else{
				perhandler = null;
				System.out.println("用户自定义了非sharable websocket handler:" + perhandler.getClass().getName());
			}
		}catch(Exception e){ 
			System.out.println("用户没有自定义的websocket handler");
			perhandler = new WebSocketHandler();
		}
	}
	
	@Override
	public void build(ChannelPipeline pipeline,RestExpress restExpress,Object ...args) {
		super.build(pipeline, restExpress, args);		 
		
		//如果是用户自定义的，根据用户决定，是否放业务线程池执行,如果用户自己实现，需要注意线程安全	
		if (perhandler != null) 
			pipeline.addBefore(restExpress.getDefaultEventExecutorGroup(), 
					HttpServerChannelHandlerFactory.HANDLER_NAME_REQHANDLER,
					HANDLER_NAME_WEBSOCKETHANDLER,perhandler); 		
		else{ 
			WebSocketHandler handler = RestExpress.getSpringCtx().getBean(WebSocketHandler.class);
			pipeline.addBefore(restExpress.getDefaultEventExecutorGroup(), 
					HttpServerChannelHandlerFactory.HANDLER_NAME_REQHANDLER,
					HANDLER_NAME_WEBSOCKETHANDLER,handler);
		}		 				 		 
	}
	
	@Override
	public String getDesc() {
		return "http server with websocket but no ssl support";
	}
}
