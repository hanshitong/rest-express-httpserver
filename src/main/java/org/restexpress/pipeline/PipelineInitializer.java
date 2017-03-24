/*
 * Copyright 2010, eCollege, Inc.  All rights reserved.
 */
package org.restexpress.pipeline;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.restexpress.RestExpress;
import org.restexpress.pipeline.factory.AbstractChannelHandlerFactory;

/**
 * Provides a tiny DSL to define the pipeline features.
 *
 * @author toddf
 * @since Aug 27, 2010
 */
public class PipelineInitializer
extends ChannelInitializer<SocketChannel>
{
	 
	private AbstractChannelHandlerFactory channelHandlerFactory = null;
	private RestExpress restExpress;

	// SECTION: CONSTRUCTORS

	public RestExpress getRestExpress() {
		return restExpress;
	}

	public PipelineInitializer setRestExpress(RestExpress restExpress) {
		this.restExpress = restExpress;
		return this;
	}

	public PipelineInitializer()
	{
		super();
	}


	// SECTION: CHANNEL PIPELINE FACTORY

	@Override
	public void initChannel(SocketChannel ch) throws Exception
	{
		ChannelPipeline pipeline = ch.pipeline();
		channelHandlerFactory.build(pipeline,restExpress);	 
	}

	public AbstractChannelHandlerFactory getChannelHandlerFactory() {
		return channelHandlerFactory;
	}

	public PipelineInitializer setChannelHandlerFactory(AbstractChannelHandlerFactory channelHandlerFactory) {
		this.channelHandlerFactory = channelHandlerFactory;
		return this;
	}

	 
}
