package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class GuideH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			ply.getGuideCtrl().addGuide((short) buf.readInt());
			SndMsg.setGuide.reset().writeInt(0).packAndSend(socket);
		}
	}

}
