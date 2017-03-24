/*
 * Copyright 2009, Strategic Gains, Inc.
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
package org.restexpress.pipeline;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.exception.DefaultExceptionMapper;
import org.restexpress.exception.ExceptionMapping;
import org.restexpress.exception.ExceptionUtils;
import org.restexpress.exception.ServiceException;
import org.restexpress.response.HttpResponseWriter;
import org.restexpress.route.Action;
import org.restexpress.route.RouteResolver;
import org.restexpress.serialization.SerializationProvider;
import org.restexpress.serialization.SerializationSettings;
import org.restexpress.util.HttpSpecification;

/**
 * @author toddf
 * @since Nov 13, 2009
 */
@Sharable
public class DefaultRequestHandler
extends SimpleChannelInboundHandler<FullHttpRequest>
{
    //SECTION: CONSTANTS
    private static final AttributeKey<MessageContext> CONTEXT_KEY = AttributeKey.valueOf("context");

	// SECTION: INSTANCE VARIABLES

	private RouteResolver routeResolver;
	private SerializationProvider serializationProvider;
	private HttpResponseWriter responseWriter;
	private List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();
	private List<Postprocessor> postprocessors = new ArrayList<Postprocessor>();
	private List<Postprocessor> finallyProcessors = new ArrayList<Postprocessor>();
	private ExceptionMapping exceptionMap = new DefaultExceptionMapper();
	private List<MessageObserver> messageObservers = new ArrayList<MessageObserver>();
	private boolean shouldEnforceHttpSpec = true;


	// SECTION: CONSTRUCTORS

	public DefaultRequestHandler(RouteResolver routeResolver, SerializationProvider serializationProvider,
		HttpResponseWriter responseWriter, boolean enforceHttpSpec)
	{
		super();
		this.routeResolver = routeResolver;
		this.serializationProvider = serializationProvider;
		setResponseWriter(responseWriter);
		this.shouldEnforceHttpSpec = enforceHttpSpec;
	}

	//add by hanst
	public DefaultRequestHandler(){
		super();
	}
	
	//add by hanst
	public void setRouteResolver(RouteResolver routeResolver) {
		this.routeResolver = routeResolver;
	}

	//add by hanst
	public void setSerializationProvider(SerializationProvider serializationProvider) {
		this.serializationProvider = serializationProvider;
	}

	//add by hanst
	public void setShouldEnforceHttpSpec(boolean shouldEnforceHttpSpec) {
		this.shouldEnforceHttpSpec = shouldEnforceHttpSpec;
	}

	// SECTION: MUTATORS

	public void addMessageObserver(MessageObserver... observers)
	{
		for (MessageObserver observer : observers)
		{
			if (!messageObservers.contains(observer))
			{
				messageObservers.add(observer);
			}
		}
	}

	public <T extends Throwable, U extends ServiceException> DefaultRequestHandler mapException(Class<T> from, Class<U> to)
	{
		exceptionMap.map(from, to);
		return this;
	}

	public DefaultRequestHandler setExceptionMap(ExceptionMapping map)
	{
		this.exceptionMap = map;
		return this;
	}

	public HttpResponseWriter getResponseWriter()
	{
		return this.responseWriter;
	}

	public void setResponseWriter(HttpResponseWriter writer)
	{
		this.responseWriter = writer;
	}


	// SECTION: SIMPLE-CHANNEL-UPSTREAM-HANDLER

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest event)
	throws Exception
	{
		MessageContext context = createInitialContext(ctx, event);

		try
		{
			processRequest(ctx, context);
		}
		catch(Throwable t)
		{
			handleRestExpressException(ctx, t);
		}
		finally
		{
			notifyComplete(context);
		}
	}

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        ctx.flush();
        super.channelReadComplete(ctx);
    }

	private void processRequest(ChannelHandlerContext ctx, MessageContext context)
	throws Throwable
	{
		notifyReceived(context);
		resolveRoute(context);
		resolveResponseProcessor(context);
		invokePreprocessors(preprocessors, context.getRequest());
		Object result = context.getAction().invoke(context.getRequest(), context.getResponse());

		if (result != null)
		{
			context.getResponse().setBody(result);
		}

		invokePostprocessors(postprocessors, context.getRequest(), context.getResponse());
		serializeResponse(context, false);
		enforceHttpSpecification(context);
		invokeFinallyProcessors(finallyProcessors, context.getRequest(), context.getResponse());
		writeResponse(ctx, context);
		notifySuccess(context);
	}

	private void resolveResponseProcessor(MessageContext context)
    {
		SerializationSettings s = serializationProvider.resolveResponse(context.getRequest(), context.getResponse(), false);
		context.setSerializationSettings(s);
    }

	/**
     * @param context
     */
    private void enforceHttpSpecification(MessageContext context)
    {
    	if (shouldEnforceHttpSpec)
    	{
    		HttpSpecification.enforce(context.getResponse());
    	}
    }

	private void handleRestExpressException(ChannelHandlerContext ctx, Throwable cause)
	throws Exception
	{
		MessageContext context = (MessageContext) ctx.attr(CONTEXT_KEY).get();
		Throwable rootCause = mapServiceException(cause);

		if (rootCause != null) // was/is a ServiceException
		{
			context.setHttpStatus(((ServiceException) rootCause).getHttpStatus());

			if (ServiceException.class.isAssignableFrom(rootCause.getClass()))
			{
				((ServiceException) rootCause).augmentResponse(context.getResponse());
			}
		}
		else
		{
			rootCause = ExceptionUtils.findRootCause(cause);
			context.setHttpStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}

		context.setException(rootCause);
		notifyException(context);
		serializeResponse(context, true);
		invokeFinallyProcessors(finallyProcessors, context.getRequest(), context.getResponse());
		writeResponse(ctx, context);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable)
	throws Exception
	{
		try
		{
			MessageContext messageContext = (MessageContext) ctx.attr(CONTEXT_KEY).get();

			if (messageContext != null)
			{
				messageContext.setException(throwable.getCause()!=null?throwable.getCause():throwable);
				notifyException(messageContext);
			}
		}
		catch(Throwable t)
		{
			System.err.print("DefaultRequestHandler.exceptionCaught() threw an exception.");
			t.printStackTrace();
		}
		finally
		{
			ctx.channel().close();
		}
	}
	
	public void userEventTriggered(ChannelHandlerContext ctx,Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
          IdleStateEvent e = (IdleStateEvent) evt;
          if (e.state() == IdleState.READER_IDLE) {
              ctx.close();
          } else if (e.state() == IdleState.WRITER_IDLE || e.state() == IdleState.ALL_IDLE) {
        	  ctx.flush();
              ctx.close();
          }
      }
	}

	private MessageContext createInitialContext(ChannelHandlerContext ctx, FullHttpRequest httpRequest)
	{
		Request request = createRequest(httpRequest, ctx);
		Response response = createResponse();
		MessageContext context = new MessageContext(request, response);
		ctx.attr(CONTEXT_KEY).set(context);
		return context;
	}

	private void resolveRoute(MessageContext context)
    {
	    Action action = routeResolver.resolve(context.getRequest());
		context.setAction(action);
    }


    private void notifyReceived(MessageContext context)
    {
    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onReceived(context.getRequest(), context.getResponse());
    	}
    }

    private void notifyComplete(MessageContext context)
    {
    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onComplete(context.getRequest(), context.getResponse());
    	}
    }

	// SECTION: UTILITY -- PRIVATE

    private void notifyException(MessageContext context)
    {
    	Throwable exception = context.getException();

    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onException(exception, context.getRequest(), context.getResponse());
    	}
    }

    private void notifySuccess(MessageContext context)
    {
    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onSuccess(context.getRequest(), context.getResponse());
    	}
    }

	public void addPreprocessor(Preprocessor handler)
	{
		if (!preprocessors.contains(handler))
		{
			preprocessors.add(handler);
		}
	}

	public void addPostprocessor(Postprocessor handler)
	{
		if (!postprocessors.contains(handler))
		{
			postprocessors.add(handler);
		}
	}

	public void addFinallyProcessor(Postprocessor handler)
	{
		if (!finallyProcessors.contains(handler))
		{
			finallyProcessors.add(handler);
		}
	}

    private void invokePreprocessors(List<Preprocessor> processors, Request request)
    {
		for (Preprocessor handler : processors)
		{
			handler.process(request);
		}

		request.getBody().resetReaderIndex();
    }

    private void invokePostprocessors(List<Postprocessor> processors, Request request, Response response)
    {
		for (Postprocessor handler : processors)
		{
			handler.process(request, response);
		}
    }

    private void invokeFinallyProcessors(List<Postprocessor> processors, Request request, Response response)
    {
		for (Postprocessor handler : processors)
		{
			try
			{
				handler.process(request, response);
			}
			catch(Throwable t)
			{
				t.printStackTrace(System.err);
			}
		}
    }

	/**
	 * Uses the exceptionMap to map a Throwable to a ServiceException, if possible.
	 *
	 * @param cause
	 * @return Either a ServiceException or the root cause of the exception.
	 */
	private Throwable mapServiceException(Throwable cause)
    {
		if (ServiceException.isAssignableFrom(cause))
		{
			return cause;
		}

		return exceptionMap.getExceptionFor(cause);
    }

    private Request createRequest(FullHttpRequest request, ChannelHandlerContext context)
    {
    	try
    	{
	    	return new Request((InetSocketAddress) context.channel().remoteAddress(), request, routeResolver, serializationProvider);
    	}
    	catch(Throwable t)
    	{
        	return new Request(request, routeResolver, serializationProvider);
    	}
    }

    private Response createResponse()
    {
    	return new Response();
    }

    private void writeResponse(ChannelHandlerContext ctx, MessageContext context)
    {
    	getResponseWriter().write(ctx, context.getRequest(), context.getResponse());
    }

	private void serializeResponse(MessageContext context, boolean force)
	{
		Response response = context.getResponse();

		if (HttpSpecification.isContentTypeAllowed(response))
		{
			SerializationSettings settings = null;

			if (response.hasSerializationSettings())
			{
				settings = response.getSerializationSettings();
			}
			else if (force)
			{
				settings = serializationProvider.resolveResponse(context.getRequest(), response, force);
			}

			if (settings != null)
			{
				if (response.isSerialized())
				{
					ByteBuffer serialized = settings.serialize(response);

					if (serialized != null)
					{
                        response.setBody(Unpooled.wrappedBuffer(serialized));

						if (!response.hasHeader(HttpHeaders.Names.CONTENT_TYPE))
						{
							response.setContentType(settings.getMediaType());
						}
					}
				}
			}

			if (!response.hasHeader(HttpHeaders.Names.CONTENT_TYPE))
			{
				response.setContentType(ContentType.TEXT_PLAIN);
			}
		}
	}
}
