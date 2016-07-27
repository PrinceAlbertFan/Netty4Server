package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class EnterGameH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			//ByteBufDecoder.readString(buf); // account
			//buf.readInt(); // serverId
			if (ply.isLoadEnd) {
				SndMsg.enterGame.reset().writeLong(ply.getPlyId()).pack(ply.getSocket()).send();
				//System.out.println("初始信息正常发送");
			} else {
				ply.isNeedSnd = true;
			}
		}
	}
	
}
