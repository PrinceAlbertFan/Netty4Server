package tool;

//import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 发送字节流的静态缓存，不要每次都new，需要多少就new多少个出来
 * @author Albert Fan
 * */
public class ClieSendBuf {
	
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
	
	public final ClieSendBuf reset() {
		buf = UnpooledBufUtil.dynamicBuf(_bufLen).writeShort(_bufLen)
				.writeByte(0).writeShort(_bufLen - 7).writeShort(_opcode);
		return this;
	}
	
	public final ClieSendBuf writeBoolean(boolean b) {
		buf.writeBoolean(b);
		return this;
	}
	
	public final ClieSendBuf setBoolean(int idx, boolean b) {
		buf.setBoolean(idx, b);
		return this;
	}
	
	public final ClieSendBuf writeByte(int b) {
		buf.writeByte(b);
		return this;
	}
	
	public final ClieSendBuf SetByte(int idx, int b) {
		buf.setByte(idx, b);
		return this;
	}
	
	public final ClieSendBuf writeBytes(ByteBuf buf0) {
		buf.writeBytes(buf0);
		return this;
	}
	
	public final ClieSendBuf writeBytes(ByteBuf buf0, int idx, int len) {
		buf.writeBytes(buf0, idx, len);
		return this;
	}
	
	public final ClieSendBuf writeShort(int s) {
		buf.writeShort(s);
		return this;
	}
	
	public final ClieSendBuf setShort(int idx, int s) {
		buf.setShort(idx, s);
		return this;
	}
	
	public final ClieSendBuf writeInt(int i) {
		buf.writeInt(i);
		return this;
	}
	
	public final ClieSendBuf setInt(int idx, int i) {
		buf.setInt(idx, i);
		return this;
	}
	
	public final ClieSendBuf writeFloat(float f) {
		buf.writeFloat(f);
		return this;
	}
	
	public final ClieSendBuf writeLong(long l) {
		buf.writeLong(l);
		return this;
	}
	
	public final ClieSendBuf writeDouble(double d) {
		buf.writeDouble(d);
		return this;
	}
	
	public final ClieSendBuf writeString(String str) {
		ByteBufDecoder.writeString(buf, str);
		return this;
	}
	
	/**记录有效数据包的长度， 固定长度无需设置*/
	public final ClieSendBuf last() {
		buf.setShort(3, buf.writerIndex() - 3);
		return this;
	}
	
	/**记录总包长，固定长度并且非群发消息无需设置*/
	public final ClieSendBuf end() {
		buf.setShort(0, buf.writerIndex());
		return this;
	}
	
	/**
	 * 派生类可重写，服务器转发的必须派生
	 * */
	public ClieSendBuf pack(int socket) {
		buf.writeInt(socket);
		return this;
	}
	
//	public SendBuf pack(Set<Integer> sockSet) {
//		for (Integer sock : sockSet) {
//			buf.writeInt(sock);
//		}
//		return this;
//	}
	
	
	/**构造*/
	public ClieSendBuf(int opcode, int dataLen) {
//		buf = Unpooled.buffer(dataLen + 7).writeShort(dataLen + 7)
//				.writeByte(0).writeShort(dataLen).writeShort(opcode).writerIndex(dataLen + 7);
		_opcode = opcode;
		_bufLen = dataLen + 7;
	}
}
