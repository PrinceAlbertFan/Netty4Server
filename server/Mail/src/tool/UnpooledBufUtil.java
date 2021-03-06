package tool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class UnpooledBufUtil {
	
	private static final BlockingQueue<ByteBuf> unpooledBufQue = new LinkedBlockingQueue<ByteBuf>(1024);
	
	
	public static ByteBuf dynamicBuf(int capacity) {
		ByteBuf _buf = unpooledBufQue.poll();
		return _buf != null
				? (_buf.capacity() < capacity ? _buf.capacity(capacity) : _buf)
				: Unpooled.buffer(capacity);
	}
	
	public final static void reclaimBuf(ByteBuf buf) {
		if (!unpooledBufQue.offer(buf.clear())) { 
			System.out.println("unpooledBufQue长度不足");
		}
	}
}
