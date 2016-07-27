package server.handler;

import java.util.List;

import server.channel.ServerChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public final class ServerStreamDecoder extends ByteToMessageDecoder {
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		int readableBytes = in.readableBytes();
		while (readableBytes > 4) {
			short msgBytes = in.getShort(in.readerIndex());
			if (msgBytes < 5 || msgBytes > 4096) {
				System.out.println("错误的消息包长度：" + msgBytes + "，服务器编号：" + ServerChannelUtil.getServerChannel(ctx.channel()).getId()
						+ "丢弃可读字节数：" + readableBytes);
				// 为了安全起见，这里只能作全部丢弃处理 // ctx.close();
				in.skipBytes(readableBytes); // in.readerIndex(in.writerIndex());
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
