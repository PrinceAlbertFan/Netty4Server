package server.handler;

import data.PlyMail;
import startup.MailStartup;
import tool.UnpooledBufUtil;
import global.GloConst;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class MailMsgHandler extends ChannelInboundHandlerAdapter {
	
	private ByteBuf byteBuf;
	
//	@Override
//	public final void channelRegistered(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void channelUnregistered(ChannelHandlerContext ctx) {
//	}
	
	@Override
	public final void channelActive(ChannelHandlerContext ctx) {
		System.out.println("邮件服连接网关成功。");
		ctx.writeAndFlush(ctx.alloc().buffer(5).writeShort(5).writeByte(-1).writeShort(GloConst.SERVER_ID));
	}
	
	@Override
	public final void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("邮件服断开网关。");
		MailStartup.exit();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		byteBuf = (ByteBuf) msg;
		PlyMail.captureMsg(UnpooledBufUtil.dynamicBuf(byteBuf.writerIndex()).writeBytes(byteBuf));
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
