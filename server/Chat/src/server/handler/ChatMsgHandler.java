package server.handler;

import java.util.HashMap;
import java.util.Map;

import tool.ByteBufDecoder;
import tool.ByteBufListener;
import tool.SendBuf;
import global.GlobalTemporaryVariable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class ChatMsgHandler extends ChannelInboundHandlerAdapter {
	
	private final static class Ply {
		int sock; String name; byte vipLv;
	}
	
	private static final SendBuf wordChat = new SendBuf(28);
	private static final SendBuf whisper = new SendBuf(29);
	private static final Map<Integer, Ply> _mapSockPly = new HashMap<Integer, Ply>();
	private static Ply _ply, _toPly;
	
	private ByteBuf byteBuf;
	private int socket;
	//short opcode;
	
//	@Override
//	public final void channelRegistered(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void channelUnregistered(ChannelHandlerContext ctx) {
//	}
	
	@Override
	public final void channelActive(ChannelHandlerContext ctx) {
		System.out.println("聊天服连接网关成功。");
		ctx.writeAndFlush(ctx.alloc().buffer(5).writeShort(5).writeByte(-1).writeShort(GlobalTemporaryVariable.tempInt));
	}
	
	@Override
	public final void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("聊天服断开网关。");
		System.exit(0);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		socket = (byteBuf = (ByteBuf) msg).readInt();
//		if (byteBuf.refCnt() != 1) {
//			System.out.println("refCnt:" + byteBuf.refCnt());
//		}
		switch (byteBuf.readShort()) { // opcode
		case 28:
			if ((_ply = _mapSockPly.get(socket)) != null) {
				ctx.writeAndFlush(wordChat.reset(_mapSockPly.size() * 4 + 256).writeInt(socket).writeString(_ply.name).writeInt(_ply.vipLv)
						.writeBytes(byteBuf).last().pack(_mapSockPly.keySet()).end().getBuffer().retain())
						.addListener(ByteBufListener.getListener(wordChat.getBuffer()));
				//System.out.println("send28, socket:" + socket);
			}
			break;
		case 29:
			if ((_ply = _mapSockPly.get(socket)) != null) {
				if ((_toPly = _mapSockPly.get(byteBuf.readInt())) != null) {
					ctx.write(whisper.reset(256).writeInt(socket).writeString(_ply.name).writeInt(_ply.vipLv)
							.writeBytes(byteBuf).last().pack(_toPly.sock).end().getBuffer().retain())
							.addListener(ByteBufListener.getListener(whisper.getBuffer()));
					ctx.writeAndFlush(whisper.reset(256).writeInt(0).writeString(_toPly.name).writeInt(_toPly.vipLv)
							.writeBytes(byteBuf).last().pack(socket).end().getBuffer().retain())
							.addListener(ByteBufListener.getListener(whisper.getBuffer()));
				} else {
					ctx.writeAndFlush(whisper.reset(17).writeInt(0).writeShort(0).last().pack(socket).end().getBuffer().retain())
					.addListener(ByteBufListener.getListener(whisper.getBuffer()));
				}
			}
			break;
		case 30:
			break;
		case 1: // 登出
			_mapSockPly.remove(socket);
			//System.out.println("_mapSockPly.remove:" + socket);
			break;
		default: // code = 0, 登入
			_ply = new Ply();
			_ply.sock = socket;
			_ply.name = ByteBufDecoder.readString(byteBuf);
			// _ply.socket = socket;
			_mapSockPly.put(socket, _ply);
			//System.out.println("_mapSockPly.put:" + socket);
			break;
		}
		byteBuf.release();
	}
	
//	@Override
//	public final void channelReadComplete(ChannelHandlerContext ctx) {
//		ctx.flush();
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
