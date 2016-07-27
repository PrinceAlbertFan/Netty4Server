package server.handler;

import server.dispatcher.RecvMsgDispatcher;
import startup.LogicStartup;
import tool.UnpooledBufUtil;
import data.global.GloTmpVal;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class LogicMsgHandler extends ChannelInboundHandlerAdapter {
	ByteBuf byteBuf;
	
//	@Override
//	public final void channelRegistered(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void channelUnregistered(ChannelHandlerContext ctx) {
//	}
	
	@Override
	public final void channelActive(ChannelHandlerContext ctx) {
		System.out.println("逻辑服连接网关成功。");
		ctx.writeAndFlush(ctx.alloc().buffer(5).writeShort(5).writeByte(-1).writeShort(GloTmpVal.tmpI));
	}
	
	@Override
	public final void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("逻辑服断开网关。");
		LogicStartup.exit();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		byteBuf = (ByteBuf) msg;
		RecvMsgDispatcher.captureMsg(UnpooledBufUtil.dynamicBuf(byteBuf.writerIndex()).writeBytes(byteBuf));
		byteBuf.release();
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
