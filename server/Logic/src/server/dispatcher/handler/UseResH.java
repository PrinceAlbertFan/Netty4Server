package server.dispatcher.handler;

import data.xml.Resource;
import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.PartnCtrl;
import obj.ctrl.PartnCtrl.PartnDBRow;
import obj.ctrl.ResCtrl;
import obj.ctrl.ResCtrl.ResDBRow;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class UseResH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			int res = buf.readInt();
			int num = buf.readInt();
			int partn = buf.readInt();
			if (partn != 0) {
				PartnDBRow ptnRow = ply.getPartnCtrl().getPartn((short) partn);
				if (ptnRow != null) {
					ResDBRow resRow = ply.getResCtrl().subtrInfactRes(res, num);
					if (resRow != null && resRow.resT.isUsable()
							&& resRow.resT.getUseReward().rewardType == 1
							&& resRow.resT.getUseReward().rewardId == Resource.ID_99905) {
						short oldLv = PartnCtrl.getPartnLv(ptnRow);
						PartnCtrl.addPartnExp(ply, ptnRow, resRow.resT.getUseReward().rewardNum * num, ply.getLevelT().getPartnLvLimit());
						SndMsg.useRes.reset().writeInt(1).writeInt(res)
						.writeInt(ResCtrl.getResCid(resRow))
						.writeInt(ResCtrl.getResQty(resRow)).writeInt(ResCtrl.getResQty(resRow) + num)
						.writeInt(0).writeInt(0).writeInt(0).writeInt(0).writeInt(0).writeInt(0)
						.writeInt(ply.getResCtrl().getExp()).writeInt(ply.getLv()).writeInt(ply.getLv()).writeInt(ply.getResCtrl().getPower())
						.writeInt(1).writeInt(PartnCtrl.getPartnId(ptnRow))
						.writeInt(PartnCtrl.getPartnExp(ptnRow))
						.writeInt(oldLv).writeInt(PartnCtrl.getPartnLv(ptnRow))
						.packAndSend(ply.getSocket());
						ply.getTaskCtrl().refreshTask((byte) 1, 0, num);
					}
				}
			}
		}
	}

}
