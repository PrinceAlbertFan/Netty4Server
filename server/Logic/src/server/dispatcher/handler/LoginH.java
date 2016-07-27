package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import server.dispatcher.GameHandler;

public final class LoginH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		Player.login(buf, socket);
	}

}
