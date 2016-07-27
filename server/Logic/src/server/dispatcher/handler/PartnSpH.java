package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class PartnSpH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			SndMsg.partnSp.reset()
			.writeInt(ply.getResCtrl().getPartnSp())
			.writeInt(ply.getPartnSpRecoverSurplusSecond())
			.pack(ply.getSocket()).send();
		}
	}

}
