package org.restexpress.pipeline.factory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.restexpress.RestExpress;
import org.restexpress.intf.SpringInitCompleteAware;
import org.restexpress.pipeline.DefaultRequestHandler;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 标准的http server,含文件上传和下载,不支持ssl,注意最后一个urlreqhandler,可能是在一个用户(业务)线程池里
 * 运行的，并非一定在I/O线程池里执行,取决于用户对executorThreadCount的设置
 * @see SSL支持类 HttpServerSSLChannelHandlerFactory
 * @author hanst
 *
 */
public class HttpServerChannelHandlerFactory extends AbstractChannelHandlerFactory implements SpringInitCompleteAware {
	public static final String HANDLER_NAME_REQHANDLER = "httpRestHandler";
	private volatile DefaultRequestHandler perhandler = null;
	private static AtomicBoolean userDefineReq = new AtomicBoolean(true);

	@Override
	public void execute() {		
		try{
			//只要是sharable,返回perhandler就不会null,但是没有初始化完
			perhandler = RestExpress.getSpringCtx().getBean(DefaultRequestHandler.class);
			if (perhandler.isSharable())									
				System.out.println("用户自定义了sharable"+ HANDLER_NAME_REQHANDLER  +":" + perhandler.getClass().getName());
			else{
				perhandler = null;
				System.out.println("用户自定义了非sharable"+ HANDLER_NAME_REQHANDLER +":" + perhandler.getClass().getName());
			}
		}catch(Exception e){
			perhandler = new DefaultRequestHandler(); //默认的DefaultRequestHandler是sharable的
			userDefineReq.set(false);
			System.out.println("用户没有自定义的" + HANDLER_NAME_REQHANDLER);
		}		
	}
	
	@Override
	public void build(ChannelPipeline pipeline,RestExpress restExpress,Object ...args) {
				
		// Inbound handlers
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("inflater", new HttpContentDecompressor());

				// Outbound handlers
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("chunkWriter", new ChunkedWriteHandler());
				pipeline.addLast("deflater", new HttpContentCompressor());

				// Aggregator MUST be added last, otherwise results are not correct
				pipeline.addLast("aggregator", new HttpObjectAggregator(restExpress.getServerSettings().getMaxContentSize()));	
				
				//这个可能看用户配置
				pipeline.addLast("idle", new IdleStateHandler(restExpress.getReadIdleTimeOut(),
						restExpress.getWriteIdleTimeOut(),restExpress.getBothIdleTimeOut(), TimeUnit.SECONDS));
				
				if (restExpress.getDefaultEventExecutorGroup() != null)
				{			
					//用户自己定义的RequestHandler,放在线程池里					
					pipeline.addLast(restExpress.getDefaultEventExecutorGroup(),HANDLER_NAME_REQHANDLER,
							restExpress.buildRequestHandler(perhandler));					
				}
				else
				{				    
					pipeline.addLast(HANDLER_NAME_REQHANDLER,restExpress.buildRequestHandler(perhandler));					 
				}	
				
	}

	@Override
	public String getDesc() {
		return "http server with no ssl,websocket support.";
	}

	@Override
	public int getOrder() {		
		return 0;
	}
 
}
