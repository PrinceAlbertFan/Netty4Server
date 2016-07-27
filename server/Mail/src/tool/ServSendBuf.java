package tool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 发送字节流的静态缓存，不要每次都new，需要多少就new多少个出来
 * @author Albert Fan
 * */
public class ServSendBuf {
	
	/**2字节总长度、1字节总类型、2字节有效包长度、2字节code*/
	protected ByteBuf buf;
	protected int _opcode;
	protected int _bufLen;
	
	public final ByteBuf getBuffer() {
		return buf;
	}
	
	public final int getWriterIndex() {
		return buf.writerIndex();
	}
	
	public final ServSendBuf setWriterIndex(int idx) {
		buf.writerIndex(idx);
		return this;
	}
	
	public final ServSendBuf reset(int socket) {
		buf = UnpooledBufUtil.dynamicBuf(_bufLen).writerIndex(11)
				.setByte(2, 1).setInt(5, socket).setShort(9, _opcode);
		//buf.writerIndex(11).setInt(5, socket);
		return this;
	}
	
	public final ServSendBuf reset() {
		buf.writerIndex(11);
		return this;
	}
	
	public final ServSendBuf writeBoolean(boolean b) {
		buf.writeBoolean(b);
		return this;
	}
	
	public final ServSendBuf setBoolean(int idx, boolean b) {
		buf.setBoolean(idx, b);
		return this;
	}
	
	public final ServSendBuf writeByte(int b) {
		buf.writeByte(b);
		return this;
	}
	
	public final ServSendBuf SetByte(int idx, int b) {
		buf.setByte(idx, b);
		return this;
	}
	
	public final ServSendBuf writeBytes(ByteBuf buf0) {
		buf.writeBytes(buf0);
		return this;
	}
	
	public final ServSendBuf writeBytes(ByteBuf buf0, int idx, int len) {
		buf.writeBytes(buf0, idx, len);
		return this;
	}
	
	public final ServSendBuf writeShort(int s) {
		buf.writeShort(s);
		return this;
	}
	
	public final ServSendBuf setShort(int idx, int s) {
		buf.setShort(idx, s);
		return this;
	}
	
	public final ServSendBuf writeInt(int i) {
		buf.writeInt(i);
		return this;
	}
	
	public final ServSendBuf setInt(int idx, int i) {
		buf.setInt(idx, i);
		return this;
	}
	
	public final ServSendBuf writeFloat(float f) {
		buf.writeFloat(f);
		return this;
	}
	
	public final ServSendBuf writeLong(long l) {
		buf.writeLong(l);
		return this;
	}
	
	public final ServSendBuf writeDouble(double d) {
		buf.writeDouble(d);
		return this;
	}
	
	public final ServSendBuf writeString(String str) {
		ByteBufDecoder.writeString(buf, str);
		return this;
	}
	
	/**记录有效数据包的长度， 固定长度无需设置*/
	public final ServSendBuf last() {
		buf.setShort(3, buf.writerIndex() - 3);
		return this;
	}
	
	/**记录总包长*/
	public final ServSendBuf end() {
		buf.setShort(0, buf.writerIndex());
		return this;
	}
	
	/**
	 * 派生类可重写，服务器转发的必须派生
	 * */
	public ServSendBuf pack(short... servIds) {
		for (short servId : servIds) {
			buf.writeShort(servId);
		}
		return this;
	}
	
	/**构造*/
	public ServSendBuf(int opcode, int dataLen) {
		_opcode = opcode;
		_bufLen = dataLen;
		//buf = Unpooled.buffer(dataLen).writerIndex(11).setByte(2, 1).setShort(9, opcode);
	}
}
