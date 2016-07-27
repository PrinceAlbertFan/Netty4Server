package server.dispatcher.handler;

import data.xml.Lot;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.dispatcher.GameHandler;

public final class LotResH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			switch (buf.readInt()) {
			case 1:
				Lot.make(ply, true, true);
				ply.getTaskCtrl().refreshTask((byte) 10, 0, 1);
				break;
			case 2:
				Lot.make(ply, false, true);
				ply.getTaskCtrl().refreshTask((byte) 10, 0, 10);
				break;
			case 3:
				Lot.make(ply, true, false);
				ply.getTaskCtrl().refreshTask((byte) 10, 0, 1);
				break;
			default:
				Lot.make(ply, false, false);
				ply.getTaskCtrl().refreshTask((byte) 10, 0, 10);
				break;
			}
		}
	}

}
