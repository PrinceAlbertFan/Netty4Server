package server.message;

import server.connect.GatewayConnection;
import tool.UnpooledBufUtil;

public final class LogoutMsg {
	
	public void send(int socket) {
		GatewayConnection.send(UnpooledBufUtil.dynamicBuf().writeShort(7).writeByte(2).writeInt(socket));
	}
}
