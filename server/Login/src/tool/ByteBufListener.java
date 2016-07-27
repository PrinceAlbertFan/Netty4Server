package tool;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public final class ByteBufListener implements ChannelFutureListener {
	
	private static final BlockingQueue<ByteBufListener> listenerQue = new LinkedBlockingQueue<ByteBufListener>(64);
	
	
	public static ChannelFutureListener getListener(ByteBuf buf) {
		ByteBufListener _listener = listenerQue.poll();
		return _listener != null ? _listener._setByteBuf(buf) : new ByteBufListener(buf);
	}
	
	private ByteBuf _buf;
	
	@Override
	public void operationComplete(ChannelFuture future) {
		if (future.isSuccess()) {
			UnpooledBufUtil.reclaimBuf(_buf);
			if (!listenerQue.offer(this)) {
				System.out.println("listenerQue长度不足");
			}
		} else {
			future.cause().printStackTrace();
		}
	}
	
	private final ChannelFutureListener _setByteBuf(ByteBuf buf) {
		_buf = buf;
		return this;
	}
	
	private ByteBufListener(ByteBuf buf) {
		_buf = buf;
	}
}
