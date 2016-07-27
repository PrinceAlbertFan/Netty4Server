package server.dispatcher.handler;

import data.global.GloTmpVal;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.dispatcher.GameHandler;

public final class UpSkillH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			GloTmpVal._tmpS = (short) buf.readInt();
			ply.getPartnCtrl().upSkill(GloTmpVal._tmpS, (byte) buf.readInt());
		}
	}

}
