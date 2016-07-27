package server.dispatcher.handler;

import data.global.GloTmpVal;
import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.PartnCtrl;
import obj.ctrl.TroopCtrl;
import obj.ctrl.TroopCtrl.TroopDBRow;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class ApplyPartnPosH extends GameHandler {
	
	private static TroopDBRow _troopRow;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			_troopRow = ply.getTroopCtrl().getTroop(GloTmpVal.tmpB = (byte) buf.readInt());
			SndMsg.reqPartnPos.reset().writeInt(GloTmpVal.tmpB);
			if (_troopRow != null) {
				SndMsg.reqPartnPos.writeInt(TroopCtrl.getTroopGodId(_troopRow)).writeInt(_troopRow.partnNum);
				GloTmpVal._tmpB = 0;
				while (GloTmpVal._tmpB != _troopRow.partnNum) {
					SndMsg.reqPartnPos.writeInt(PartnCtrl.getPartnId(_troopRow.arrPartn[GloTmpVal._tmpB++]));
				}
			} else {
				SndMsg.reqPartnPos.writeLong(0);
			}
			SndMsg.reqPartnPos.packAndSend(ply.getSocket());
		}
	}

}
