package server.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.ResCtrl;
import obj.ctrl.ResCtrl.ResDBRow;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class SellH extends GameHandler {
	
	private static ResDBRow _resRow;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			int res = buf.readInt();
			int qty = buf.readInt();
			_resRow = ply.getResCtrl().subtrInfactRes(res, qty);
			if (_resRow == null) {
				System.out.println("物品不够，贩卖失败");
				return;
			}
			ply.getResCtrl().addCoin(_resRow.resT.getSellCoin() * qty);
			SndMsg.sell.reset().writeInt(res).writeInt(ResCtrl.getResCid(_resRow))
			.writeInt(ResCtrl.getResQty(_resRow))
			.writeInt(ResCtrl.getResQty(_resRow) + qty)
			.writeInt(ply.getResCtrl().getCoin())
			.pack(ply.getSocket()).send();
		}
	}

}
