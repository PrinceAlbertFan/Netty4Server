package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.dispatcher.GameHandler;

public final class AddGodH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			ply.getGodCtrl().addGod((short) buf.readInt());
		}
	}

}
