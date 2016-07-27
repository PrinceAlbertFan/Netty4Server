package server.channel;

import java.util.concurrent.ConcurrentHashMap;

import tool.ByteBufListener;
import tool.UnpooledBufUtil;
import manager.MessageManager;
import global.GloConst;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public final class ServerChannelUtil {
	
	public final static class ServerChannel {
		private Channel channel;
		private short id;
		
		private int sliceSize; // for decode
		
		public void decode(ByteBuf buf) {
			switch (buf.readByte()) {
			case 0: // 流向客户端消息(网关向客户机分发消息)
				 // 获取有效数据长度(包括长度自身)，里头包括opcode等会直接推送给客户端
				buf.skipBytes(sliceSize = buf.getShort(1)); // 根据总包长获取有效字节流
				do { // 把实际有效的数据流推送给每个客户机
					ClientChannelUtil.send(UnpooledBufUtil.dynamicBuf(sliceSize).writeBytes(buf, 1, sliceSize), buf.readInt());
				} while (buf.isReadable());
				break;
			case 1: // 流向服务器消息(网关向服务器分发消息)
				//System.out.println("socket:" + buf.getShort(5));
				//System.out.println("code:" + buf.getShort(7));
				buf.skipBytes(sliceSize = buf.getShort(1));
				//System.out.println("size:" + sliceSize);
				do { // 把实际有效的数据流推送给每个服务器
					ServerChannelUtil.send(UnpooledBufUtil.dynamicBuf(sliceSize).writeBytes(buf, 1, sliceSize), buf.readShort());
				} while (buf.isReadable());
				break;
			case 2: // 某客户端被Game踢除
				// 注销(GameServer通知GateServer),该玩家被GameServer主动剔除,该玩家已经不在GameServer中了
				ClientChannelUtil.close(buf.readInt());
				break;
			case -1: // 服务器注册
				registServerChannel(this, buf.readShort());
				break;
			default: // ???
				byte type = buf.getByte(buf.readerIndex() - 1);
				System.out.println("错误的type:" + type);
				registServerChannel(this, buf.readShort());
				break;
			}
			//UnpooledBufUtil.reclaimBuf(buf);
			buf.release();
		}
		
		public final void send(ByteBuf buf) {
			channel.writeAndFlush(buf.retain()).addListener(ByteBufListener.getListener(buf));
		}
		
		public final short getId() {
			return id;
		}
		
		public final void setId(short _id) {
			id = _id;
		}
		
		public ServerChannel(Channel _channel) {
			channel = _channel;
		}
	}
	
	private static final ConcurrentHashMap<Channel, ServerChannel> mapServChannel;
	private static final ServerChannel[] arrServChannel;
	
	static {
		mapServChannel = new ConcurrentHashMap<Channel, ServerChannel>();
		arrServChannel = new ServerChannel[MessageManager.SERVER_NUM];
	}
	
//	public static void onUpdate() {
//		for (ServerChannel serverChannel : mapCtxServChannel.values()) {
//			serverChannel.flush();
//		}
//	}
	
	public static void send(ByteBuf buf, short id) {
		ServerChannel server = arrServChannel[id];
		if (server != null) {
			server.send(buf);
		}
	}
	
	public static void noticeClientLogoutToAllServers(int clieId) {
		ServerChannel server = arrServChannel[GloConst.MAIN_SERVER_ID];
		if (server != null) {
			server.send(UnpooledBufUtil.dynamicBuf().writeShort(8).writeInt(clieId).writeShort(1));
		}
		if ((server = arrServChannel[GloConst.LOGIN_SERVER_ID]) != null) {
			server.send(UnpooledBufUtil.dynamicBuf().writeShort(8).writeInt(clieId).writeShort(1));
		}
		if ((server = arrServChannel[GloConst.BATTLE_SERVER_ID]) != null) {
			server.send(UnpooledBufUtil.dynamicBuf().writeShort(8).writeInt(clieId).writeShort(1));
		}
		if ((server = arrServChannel[GloConst.CHAT_SERVER_ID]) != null) {
			server.send(UnpooledBufUtil.dynamicBuf().writeShort(8).writeInt(clieId).writeShort(1));
		}
		if ((server = arrServChannel[GloConst.GUILD_SERVER_ID]) != null) {
			server.send(UnpooledBufUtil.dynamicBuf().writeShort(8).writeInt(clieId).writeShort(1));
		}
		if ((server = arrServChannel[GloConst.MAIL_SERVER_ID]) != null) {
			server.send(UnpooledBufUtil.dynamicBuf().writeShort(8).writeInt(clieId).writeShort(1));
		}
	}
	
	public final static ServerChannel getServerChannel(Channel channel) {
		return mapServChannel.get(channel);
	}
	
	public final static void putServerChannel(Channel channel) {
		ServerChannel serverChannel = new ServerChannel(channel);
		mapServChannel.put(channel, serverChannel);
	}
	
	public final static void registServerChannel(ServerChannel serverChannel, short id) {
		if (arrServChannel[id] == null) {
			serverChannel.setId(id);
			arrServChannel[id] = serverChannel;
			if (arrServChannel[GloConst.MAIN_SERVER_ID] != null) {
				if (id == GloConst.CHAT_SERVER_ID) {
					// 告诉聊天服现有的在线玩家
				} else if (id == GloConst.GUILD_SERVER_ID) {
					
				}
			}
			System.out.println("服务器：[" + id + "]注册成功。");
		} else {
			System.out.println("重复的服务器：[" + id + "]，注册失败。");
		}
	}
	
	public final static void removeServerChannel(Channel channel) {
		ServerChannel serverChannel = mapServChannel.remove(channel);
		if (serverChannel.equals(arrServChannel[serverChannel.getId()])) {
			arrServChannel[serverChannel.getId()] = null;
			System.out.println("服务器：[" + serverChannel.getId() + "]已注销。");
		}
	}
}
