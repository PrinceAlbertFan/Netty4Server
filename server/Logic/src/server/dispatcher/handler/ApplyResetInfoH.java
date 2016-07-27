package server.dispatcher.handler;

import data.xml.ResetTimes;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class ApplyResetInfoH extends GameHandler {
	
	private static int[] _tmpData;
	private static byte type, times;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			type = (byte) buf.readInt();
			times = ply._arrResetTimes[type];
			if (times < 30) {
				++times;
			}
			_tmpData = ResetTimes.data[type][times];
			SndMsg.resetInfo.reset()
			.writeInt(times).writeInt(_tmpData[0] % 1000).writeInt(_tmpData[1]).writeInt(type)
			.pack(ply.getSocket()).send();
		}
	}

}
