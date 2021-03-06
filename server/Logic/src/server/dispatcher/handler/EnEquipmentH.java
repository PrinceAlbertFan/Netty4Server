package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.dispatcher.GameHandler;

public final class EnEquipmentH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			short id = (short) buf.readInt();
			ply.getPartnCtrl().enhanceEquipment(id, (byte) buf.readInt());
		}
	}

}
