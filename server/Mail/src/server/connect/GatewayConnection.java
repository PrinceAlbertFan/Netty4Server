package server.connect;

import java.net.InetSocketAddress;

import server.handler.MailMsgDecoder;
import server.handler.MailMsgHandler;
import tool.ByteBufListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public final class GatewayConnection {
	
	private static Channel _channel;
	
	public final static void writeAndFlush(ByteBuf buf) {
		_channel.writeAndFlush(buf.retain()).addListener(ByteBufListener.getListener(buf));
	}
	
	public final static void write(ByteBuf buf) {
		_channel.write(buf.retain()).addListener(ByteBufListener.getListener(buf));
	}
	
	public final static void flush() {
		_channel.flush();
	}
	
	public static void startup(String ip, int port) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
		.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
		.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(512, 512, 8192))
		.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
		//.option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
		//.handler(new MailHandler())
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) {
				(_channel = channel).pipeline().addLast(new MailMsgDecoder(), new MailMsgHandler());
			}
		})
		.connect(new InetSocketAddress(ip, port)).sync();
	}
}
