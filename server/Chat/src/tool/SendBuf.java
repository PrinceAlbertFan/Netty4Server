package tool;

import java.util.Set;

import io.netty.buffer.ByteBuf;

/**
 * 发送字节流的静态缓存，不要每次都new，需要多少就new多少个出来
 * @author Albert Fan
 * */
public class SendBuf {
	
	/**2字节总长度、1字节总类型、2字节有效包长度、2字节code*/
	protected ByteBuf buf;
	protected int _opcode;
	
	
	public final ByteBuf getBuffer() {
		return buf;
	}
	
	public final int getWriterIndex() {
		return buf.writerIndex();
	}
	
	public final SendBuf reset(int _bufLen) {
		buf = UnpooledBufUtil.dynamicBuf(_bufLen).writerIndex(7)
				.setByte(2, 0).setShort(5, _opcode);
		return this;
	}
	
	public final SendBuf writeBoolean(boolean b) {
		buf.writeBoolean(b);
		return this;
	}
	
	public final SendBuf setBoolean(int idx, boolean b) {
		buf.setBoolean(idx, b);
		return this;
	}
	
	public final SendBuf writeByte(int b) {
		buf.writeByte(b);
		return this;
	}
	
	public final SendBuf SetByte(int idx, int b) {
		buf.setByte(idx, b);
		return this;
	}
	
	public final SendBuf writeBytes(ByteBuf buf0) {
		buf.writeBytes(buf0);
		return this;
	}
	
	public final SendBuf writeBytes(ByteBuf buf0, int idx, int len) {
		buf.writeBytes(buf0, idx, len);
		return this;
	}
	
	public final SendBuf writeShort(int s) {
		buf.writeShort(s);
		return this;
	}
	
	public final SendBuf setShort(int idx, int s) {
		buf.setShort(idx, s);
		return this;
	}
	
	public final SendBuf writeInt(int i) {
		buf.writeInt(i);
		return this;
	}
	
	public final SendBuf setInt(int idx, int i) {
		buf.setInt(idx, i);
		return this;
	}
	
	public final SendBuf writeFloat(float f) {
		buf.writeFloat(f);
		return this;
	}
	
	public final SendBuf writeLong(long l) {
		buf.writeLong(l);
		return this;
	}
	
	public final SendBuf writeDouble(double d) {
		buf.writeDouble(d);
		return this;
	}
	
	public final SendBuf writeString(String str) {
		ByteBufDecoder.writeString(buf, str);
		return this;
	}
	
	/**记录有效数据包的长度， 固定长度无需设置*/
	public final SendBuf last() {
		buf.setShort(3, buf.writerIndex() - 3);
		return this;
	}
	
	/**记录总包长，固定长度并且非群发消息无需设置*/
	public final SendBuf end() {
		buf.setShort(0, buf.writerIndex());
		return this;
	}
	
	/**
	 * 派生类可重写，服务器转发的必须派生
	 * */
	public SendBuf pack(int socket) {
		buf.writeInt(socket);
		return this;
	}
	
	public SendBuf pack(Set<Integer> sockSet) {
		for (Integer sock : sockSet) {
			buf.writeInt(sock);
		}
		return this;
	}
	
	
	/**构造*/
	public SendBuf(int opcode) {
		_opcode = opcode;
	}
}
