/*
 * Copyright 2010-2014, Strategic Gains, Inc.  All rights reserved.
 */
package org.restexpress.response;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.restexpress.ContentType;
import org.restexpress.Parameters;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.util.HttpSpecification;

/**
 * @author toddf
 * @since Aug 26, 2010
 */
public class DefaultHttpResponseWriter
implements HttpResponseWriter
{
	@Override
	public void write(ChannelHandlerContext ctx, Request request,
	    Response response)
	{
		// The DefaultHttpResponseWriter will include the provided response body (if provided),
		// else the default empty body (from the DefaultFullHttpResponse class) will be included.
		FullHttpResponse httpResponse = response.hasBody()
		    && HttpSpecification.isContentAllowed(response)
		    ? new DefaultFullHttpResponse(request.getHttpVersion(),
		        getHttpResponseStatusFrom(request, response),
		        getResponseBodyByteBuf(response))
		    : new DefaultFullHttpResponse(request.getHttpVersion(),
		        getHttpResponseStatusFrom(request, response));
		addHeaders(response, httpResponse);

		if (request.isKeepAlive())
		{
			// Add 'Content-Length' header only for a keep-alive connection.
			if (HttpSpecification.isContentLengthAllowed(response))
			{
				httpResponse.headers().set(CONTENT_LENGTH, String.valueOf(httpResponse.content().readableBytes()));
			}

			// Support "Connection: Keep-Alive" for HTTP 1.0 requests.
			if (request.isHttpVersion1_0())
			{
				httpResponse.headers().add(CONNECTION, "Keep-Alive");
			}

			enforceEmptyHeadResponseBody(request, httpResponse);
			ctx.channel().write(httpResponse)
			    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		}
		else
		{
			httpResponse.headers().set(CONNECTION, "close");
			enforceEmptyHeadResponseBody(request, httpResponse);

			// Close the connection as soon as the message is sent.
			ctx.channel().write(httpResponse)
			    .addListener(ChannelFutureListener.CLOSE);
		}
	}

	private ByteBuf getResponseBodyByteBuf(Response response)
	{
		// If the response body contains a ByteBuf, the
		// DefaultHttpResponseWriter will use it, else it is assumed that the
		// body is a string.
		return ByteBuf.class.isAssignableFrom(response.getBody().getClass())
		    ? Unpooled.wrappedBuffer((ByteBuf) response.getBody()) : Unpooled
		        .wrappedBuffer(response.getBody().toString()
		            .getBytes(ContentType.CHARSET));
	}

	private HttpResponseStatus getHttpResponseStatusFrom(Request request,
	    Response response)
	{
		return request.getHeader(Parameters.Query.IGNORE_HTTP_STATUS) == null
		    ? response.getResponseStatus() : HttpResponseStatus.OK;
	}

	/**
	 * @param response
	 * @param httpResponse
	 */
	private void addHeaders(Response response, HttpResponse httpResponse)
	{
		for (String name : response.getHeaderNames())
		{
			for (String value : response.getHeaders(name))
			{
				httpResponse.headers().add(name, value);
			}
		}
	}

	/**
	 * Clear out the content for HEAD calls.
	 * 
	 * @param request
	 * @param httpResponse
	 */
	private void enforceEmptyHeadResponseBody(Request request, FullHttpResponse httpResponse)
	{
		if (request.getHttpMethod() == HttpMethod.HEAD)
		{
			httpResponse.content().clear();
		}
	}
}
