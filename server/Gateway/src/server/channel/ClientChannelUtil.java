package server.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

import tool.ByteBufListener;
import tool.UnpooledBufUtil;
import manager.MessageManager;
import manager.TimeManager;


public final class ClientChannelUtil {
	
	public final static class ClientChannel {
		private Channel channel;
		private int id;
		private int heartbeatTime;
		
		private short msgSize, msgOpcode; // for decode
		
		public void decode(ByteBuf buf) {
			msgSize = (short) (buf.writerIndex() + 2);
			msgOpcode = buf.getShort(0);
			ServerChannelUtil.send(UnpooledBufUtil.dynamicBuf().writeShort(msgSize + 4).writeInt(id).writeBytes(buf),
					MessageManager.getServerId(msgOpcode, msgSize));
			buf.release();
		}
		
		public final void send(ByteBuf buf) {
			channel.writeAndFlush(buf.retain()).addListener(ByteBufListener.getListener(buf));
		}
		
		public final int getId() {
			return id;
		}
		
		public final void checkHeartbeat() {
			if ((heartbeatTime += TimeManager.dt) > HEARTBEAT_T) {
				channel.close();
			}
		}
		
		public final void resetHeartbeat() {
			heartbeatTime = 0;
		}
		
		public ClientChannel(Channel _channel) {
			channel = _channel;
			id = Integer.parseUnsignedInt(channel.id().asShortText(), 16);
			//System.out.println("接入socket:" + id);
			heartbeatTime =  HEARTBEAT_T - LOGIN_TIMEOUT;
		}
	}
	
	private static final ConcurrentHashMap<Channel, ClientChannel> mapClieChannel;
	private static final ConcurrentHashMap<Integer, ClientChannel> mapClieSockChannel;
	
	private static final int HEARTBEAT_T, LOGIN_TIMEOUT;
	
	static {
		mapClieChannel = new ConcurrentHashMap<Channel, ClientChannel>();
		mapClieSockChannel = new ConcurrentHashMap<Integer, ClientChannel>();
		HEARTBEAT_T = 300000;
		LOGIN_TIMEOUT = 3000;
	}
	
	public static void onUpdate() {
		for (ClientChannel clientChannel : mapClieChannel.values()) {
			//clientChannel.checkHeartbeat();
		}
	}
	
	public static void send(ByteBuf buf, int id) {
		ClientChannel client = mapClieSockChannel.get(id);
		if (client != null) {
			client.send(buf);
		}
	}
	
	public static void close(int id) {
		ClientChannel client = mapClieSockChannel.get(id);
		if (client != null) {
			client.channel.close();
		}
	}
	
	public final static ClientChannel getClientChannel(Channel channel) {
		return mapClieChannel.get(channel);
	}
	
	public final static ClientChannel getClientChannel(int id) {
		return mapClieSockChannel.get(id);
	}
	
	public final static void putClientChannel(Channel channel) {
		ClientChannel clientChannel = new ClientChannel(channel);
		mapClieChannel.put(channel, clientChannel);
		mapClieSockChannel.put(clientChannel.getId(), clientChannel);
		System.out.println("连接客户端：" + channel.remoteAddress());
	}
	
	public final static void removeClientChannel(Channel channel) {
		ClientChannel clientChannel = mapClieChannel.remove(channel);
		mapClieSockChannel.remove(clientChannel.getId());
		System.out.println("断开客户端：" + channel.remoteAddress());
		// 通知所有连接的服务器
		ServerChannelUtil.noticeClientLogoutToAllServers(clientChannel.id);
	}
}
