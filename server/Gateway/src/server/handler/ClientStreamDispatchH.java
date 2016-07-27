package server.handler;

import server.channel.ClientChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class ClientStreamDispatchH extends ChannelInboundHandlerAdapter {
//	@Override
//	public final void channelRegistered(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void channelUnregistered(ChannelHandlerContext ctx) {
//	}
	
	@Override
	public final void channelActive(ChannelHandlerContext ctx) {
		ClientChannelUtil.putClientChannel(ctx.channel());
	}
	
	@Override
	public final void channelInactive(ChannelHandlerContext ctx) {
		ClientChannelUtil.removeClientChannel(ctx.channel());
	}
	
	@Override
	public final void channelRead(ChannelHandlerContext ctx, Object msg) {
		ClientChannelUtil.getClientChannel(ctx.channel()).decode((ByteBuf) msg);
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
