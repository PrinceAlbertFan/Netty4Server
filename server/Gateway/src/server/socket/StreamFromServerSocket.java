package server.socket;

import server.handler.ServerStreamDecoder;
import server.handler.ServerStreamDispatchH;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class StreamFromServerSocket {
	
	public void startup(int port) throws Exception {
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(parentGroup, childGroup).channel(NioServerSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
		.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
		.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
		.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
		.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(2048, 2048, 32768))//8192, 8192, 131072
		.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
		.childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) {
				channel.pipeline().addLast(new ServerStreamDecoder(), new ServerStreamDispatchH());
			}
		}).bind(port).sync();
	}
}
