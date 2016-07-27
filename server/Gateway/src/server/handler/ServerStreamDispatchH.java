package server.handler;

import server.channel.ServerChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class ServerStreamDispatchH extends ChannelInboundHandlerAdapter {
//	@Override
//	public final void channelRegistered(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void channelUnregistered(ChannelHandlerContext ctx) {
//	}
	
	@Override
	public final void channelActive(ChannelHandlerContext ctx) {
		ServerChannelUtil.putServerChannel(ctx.channel());
		System.out.println("新连接服务器：" + ctx.channel().remoteAddress().toString());
	}
	
	@Override
	public final void channelInactive(ChannelHandlerContext ctx) {
		ServerChannelUtil.removeServerChannel(ctx.channel());
		System.out.println("断开服务器：" + ctx.channel().remoteAddress().toString());
	}
	
	@Override
	public final void channelRead(ChannelHandlerContext ctx, Object msg) {
		//ystem.out.println("channelRead ctx:" + ctx.channel().id());
		ServerChannelUtil.getServerChannel(ctx.channel()).decode((ByteBuf) msg);
	}
	
//	@Override
//	public final void channelReadComplete(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
//	}
//	@Override
//	public final void channelWritabilityChanged(ChannelHandlerContext ctx) {
//	}
	@Override
	public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	}
}
