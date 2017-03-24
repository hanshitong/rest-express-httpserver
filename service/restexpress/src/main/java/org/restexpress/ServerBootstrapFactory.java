/*
    Copyright 2015, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.restexpress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

/**
 * @author toddf
 * @since Jul 10, 2015
 */
public class ServerBootstrapFactory
{
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	/**
	 * 
	 * @param ioMainThreadCount <= 0, 则取netty默认io线程数
	 * @param ioSubThreadCount > 0 ,main sub reactor主从多线程模型, = 0, netty多线程模型  
	 * @return
	 */
	public ServerBootstrap newServerBootstrap(int ioMainThreadCount,int ioSubThreadCount)
    {
		if (ioMainThreadCount <= 0)
			ioMainThreadCount = Runtime.getRuntime().availableProcessors() * 2;
		if (Epoll.isAvailable())
		{
			System.out.println("use epoll");
			return newEpollServerBootstrap(ioMainThreadCount,ioSubThreadCount);
		}

		return newNioServerBootstrap(ioMainThreadCount,ioSubThreadCount);
    }

	public void shutdownGracefully(boolean shouldWait)
    {
		Future<?> workerFuture = null;
		if (workerGroup != null)
			workerFuture = workerGroup.shutdownGracefully();
		Future<?> bossFuture = bossGroup.shutdownGracefully();

		if (shouldWait)
		{
			if (workerFuture != null)
				workerFuture.awaitUninterruptibly();
			bossFuture.awaitUninterruptibly();
		}
    }

	private ServerBootstrap newNioServerBootstrap(int ioMainThreadCount,int ioSubThreadCount)
    {
	    if (ioSubThreadCount > 0)
		{
			bossGroup = new NioEventLoopGroup(ioMainThreadCount);
			workerGroup = new NioEventLoopGroup(ioSubThreadCount);
			return new ServerBootstrap()
					.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class);
		}
		else
		{
			bossGroup = new NioEventLoopGroup();
			return new ServerBootstrap()
					.group(bossGroup).channel(NioServerSocketChannel.class);
		}
    }

	private ServerBootstrap newEpollServerBootstrap(int ioMainThreadCount,int ioSubThreadCount)
    {
	    if (ioSubThreadCount > 0)
	    {
	    	bossGroup = new EpollEventLoopGroup(ioMainThreadCount);
	    	workerGroup = new EpollEventLoopGroup(ioSubThreadCount);
	    	 return new ServerBootstrap()
	    		    	.group(bossGroup, workerGroup)
	    		    	.channel(EpollServerSocketChannel.class);
	    }
	    else
	    {
	    	bossGroup = new EpollEventLoopGroup();
	    	 return new ServerBootstrap()
	    		    	.group(bossGroup)
	    		    	.channel(EpollServerSocketChannel.class); 
	    }
    }
}
