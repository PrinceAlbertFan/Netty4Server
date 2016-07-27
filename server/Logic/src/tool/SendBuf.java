package tool;

import server.connect.GatewayConnection;
import io.netty.buffer.ByteBuf;

/**
 * 发送字节流的静态缓存，不要每次都new，需要多少就new多少个出来
 * @author Albert Fan
 * */
public class SendBuf {
	
	/**2字节总长度、1字节总类型、2字节有效包长度、2字节code*/
	protected ByteBuf buf;
	
	protected int _opcode;
	protected int _bufLen; // 临时记录总长度，正式版可删除
	
	
	public final ByteBuf getBuffer() {
		return buf;
	}
	
	public final int getWriterIndex() {
		return buf.writerIndex();
	}
	
	public final SendBuf reset() {
		buf = UnpooledBufUtil.dynamicBuf(_bufLen).writeShort(_bufLen).writeByte(0).writeShort(_bufLen - 7).writeShort(_opcode);
		//buf.writerIndex(7);
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
	
	public final void send() {
//		if (_bufLen < buf.writerIndex()/* || _bufLen - buf.writerIndex() > 64*/) {
//			System.out.println("send ["+ buf.getShort(5) + "]: initSize:" + _bufLen + " infactSize:" + buf.writerIndex());
//		}
		GatewayConnection.send(buf);
	}
	
	/**
	 * 单对象封包并派送
	 * */
	public void packAndSend(int socket) {
//		if (_bufLen < buf.writerIndex()/* || _bufLen - buf.writerIndex() > 64*/) {
//			System.out.println("send ["+ buf.getShort(5) + "]: initSize:" + _bufLen + " infactSize:" + buf.writerIndex());
//		}
		GatewayConnection.send(buf.setShort(3, buf.writerIndex() - 3).writeInt(socket).setShort(0, buf.writerIndex()));
	}
	
//	/**
//	 * 只能用于对整个包不作改动的情况
//	 * */
//	public void send(int socket) {
//		GatewayConnection.send(buf.setInt(buf.writerIndex() - 4, socket));
//	}
	
	/**构造*/
	public SendBuf(int opcode, int dataLen) {
//		buf = Unpooled.buffer(_bufLen = dataLen + 7).writeShort(_bufLen)
//				.writeByte(0).writeShort(dataLen).writeShort(_opcode = opcode).writerIndex(_bufLen);
		_opcode = opcode;
		_bufLen = dataLen + 7;
	}
}
