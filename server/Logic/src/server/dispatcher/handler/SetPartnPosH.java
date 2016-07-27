package server.dispatcher.handler;

import data.global.GloTmpVal;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class SetPartnPosH extends GameHandler {
	
	private static final short[] _arrPartn = new short[4];
	private static short _god;
	private static byte _troop;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			_troop = (byte) buf.readInt();
			_god = (short) buf.readInt();
			GloTmpVal.tmpB = (byte) buf.readInt(); // size
//			if (GloTmpVal.tmpB < 0 || GloTmpVal.tmpB > 4) {
//				System.out.println("错误的PVE出战英雄数量：" + GloTmpVal.tmpB);
//				return false;
//			}
			for (GloTmpVal._tmpB = 0; GloTmpVal._tmpB != GloTmpVal.tmpB; ++GloTmpVal._tmpB) {
				_arrPartn[GloTmpVal._tmpB] = (short) buf.readInt();
			}
			ply.getTroopCtrl().setTroop(_troop, _god, GloTmpVal.tmpB, _arrPartn);
			SndMsg.setPartnPos.reset().writeInt(0).packAndSend(socket);
		}
	}

}
