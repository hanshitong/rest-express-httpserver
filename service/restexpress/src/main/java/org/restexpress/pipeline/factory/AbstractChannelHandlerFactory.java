package org.restexpress.pipeline.factory;

import org.restexpress.RestExpress;
import io.netty.channel.ChannelPipeline;

/**
 * 为ServerChannel的pipeline生成ChannelHandler的工厂抽象类
 * @author hanst
 *
 */
public abstract class AbstractChannelHandlerFactory {
	public abstract void build(ChannelPipeline pipeline,RestExpress restExpress,Object ...args);
	public abstract String getDesc();
}
