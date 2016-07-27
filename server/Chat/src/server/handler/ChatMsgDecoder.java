package server.handler;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public final class ChatMsgDecoder extends ByteToMessageDecoder {
	
	private int readableBytes;
	private short recordBytes;
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		for (readableBytes = in.readableBytes();
				readableBytes > 7 && (recordBytes = in.getShort(in.readerIndex())) <= readableBytes;
				readableBytes -= recordBytes) {
			out.add(in.skipBytes(2).readSlice(recordBytes - 2).retain());
		}
	}

}
