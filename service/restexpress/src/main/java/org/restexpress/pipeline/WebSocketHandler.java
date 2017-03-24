package org.restexpress.pipeline;

import org.springframework.http.HttpMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.ReferenceCountUtil;

/**
 * 子类可以继承这个类实现业务逻辑处理，在processWebsocketRequest实现自己的业务逻辑.
 * ChannelInboundHandlerAdapter是不会自动释放channelRead里的消息对象的，当是websocket过来的消息，
 * 需要手动释放对象，对于非websocket消息，则直接提交给后续的handler处理，后续的handler负责对象的释放
 * （继承SimpleChannelInboundHandler，这个类在读消息后会释放消息对象）
 * @author hanst
 */
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

		//1.完成websocket前的http握手
	private void processHttpRequest(final ChannelHandlerContext ctx,HttpRequest request){
			HttpHeaders headers = request.headers();
			
			//不是websocket,其他rest请求
			if(!HttpMethod.GET.name().equals(request.getMethod().name())
					|| !"websocket".equalsIgnoreCase(headers.get("Upgrade"))){
				ctx.fireChannelRead(request);
				return;
			}
			
			//http GET和headers['Upgrade']为'websocket'的http请求
			WebSocketServerHandshakerFactory wsShakerFactory = new WebSocketServerHandshakerFactory(
					"ws://"+request.headers().get(HttpHeaders.Names.HOST),
					null,false );
			WebSocketServerHandshaker wsShakerHandler = wsShakerFactory.newHandshaker(request);
			if(null==wsShakerHandler){
				//无法处理的websocket版本
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			}else{
				//向客户端发送websocket握手,完成握手
				//客户端收到的状态是101 sitching protocol
				wsShakerHandler.handshake(ctx.channel(),request);		
			}
			ReferenceCountUtil.safeRelease(request);
	}
		
		//websocket通信
		protected void processWebsocketRequest(ChannelHandlerContext ctx, WebSocketFrame request){		
			if(request instanceof CloseWebSocketFrame){
				ctx.close();
			}else if(request instanceof PingWebSocketFrame){			
				ctx.write(new PongWebSocketFrame(request.content()));  
			} 
		}

		@Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			//虽然是websocket，但在建立websocket连接前，先进行http握手,所以，这时也要处理http请求
			//在http握手完成后，才是websocket下的通信
			if(msg instanceof HttpRequest){
				processHttpRequest(ctx,(HttpRequest)msg);
			}
			else if(msg instanceof WebSocketFrame){
				processWebsocketRequest(ctx,(WebSocketFrame)msg);
				ReferenceCountUtil.safeRelease(msg);
			}
			else //交给pipeline中下一个handler处理
				ctx.fireChannelRead(msg);			
		}

}
