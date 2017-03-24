/*
 * Copyright 2010, eCollege, Inc.  All rights reserved.
 */
package org.restexpress.response;

import io.netty.channel.ChannelHandlerContext;
import org.restexpress.Request;
import org.restexpress.Response;

/**
 * @author toddf
 * @since Aug 26, 2010
 */
public interface HttpResponseWriter
{
	public void write(ChannelHandlerContext ctx, Request request, Response response);
}
