package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class ResInfoH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			int res = buf.readInt();
			int qty = ply.getResCtrl().getResQty(res);
			SndMsg.resInfo.reset().writeInt(res).writeInt(qty).pack(ply.getSocket()).send();
		}
	}

}
