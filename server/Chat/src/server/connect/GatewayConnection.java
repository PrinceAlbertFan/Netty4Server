package server.connect;

import java.net.InetSocketAddress;

import server.handler.ChatMsgDecoder;
import server.handler.ChatMsgHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public final class GatewayConnection {
	
	public void startup(String ip, int port) throws Exception {
		
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
		.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
		.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(256, 256, 4096))
		.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
		//.option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) {
				channel.pipeline().addLast(new ChatMsgDecoder(), new ChatMsgHandler());
			}
		})
		//.handler(new ChatHandler())
		.connect(new InetSocketAddress(ip, port)).sync();
	}
}
