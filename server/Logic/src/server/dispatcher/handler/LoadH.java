package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.dispatcher.GameHandler;

public final class LoadH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			switch (buf.readByte()) {
			case 0:
				ply.getResCtrl().loadFromDB(buf);
				break;
			case 1:
				ply.getPartnCtrl().loadFromDB(buf);
				break;
			case 2:
				ply.getGodCtrl().loadFromDB(buf);
				break;
			case 3:
				ply.getFortCtrl().loadFromDB(buf);
				break;
			case 4:
				ply.getTaskCtrl().loadFromDB(buf);
				break;
			case 5:
				ply.getFrndCtrl().loadFromDB(buf);
				break;
			case 6:
				ply.getTroopCtrl().loadFromDB(buf);
				break;
			case 7:
				break;
			case 8:
				ply.getTimesCtrl().loadFromDB(buf);
				break;
			default:
				ply.getGuideCtrl().loadFromDB(buf);
				break;
			}
		}
	}

}
