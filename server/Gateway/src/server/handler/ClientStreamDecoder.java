package server.handler;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public final class ClientStreamDecoder extends ByteToMessageDecoder {
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		int readableBytes = in.readableBytes();
		while (readableBytes > 3) {
			short msgBytes = in.getShort(in.readerIndex());
			if (msgBytes < 4 || msgBytes > 256) {
				System.out.println("非法的消息包长度：" + msgBytes + "，客户端：" + ctx.channel().remoteAddress());
				ctx.close();
				return;
			}
			if (msgBytes > readableBytes) {
				break;
			}
			out.add(in.skipBytes(2).readSlice(msgBytes - 2).retain());
			readableBytes -= msgBytes;
		}
	}

}
