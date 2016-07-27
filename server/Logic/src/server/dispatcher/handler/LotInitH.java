package server.dispatcher.handler;

import data.global.GlobalConst;
import manager.TimeManager;
import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.TimesCtrl;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class LotInitH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			SndMsg.lotInit.reset()
			.writeInt(2) // size
			.writeInt(1) // type
			.writeInt(GlobalConst.lottery_coin_free_times - ply.getTimesCtrl().getTimes(TimesCtrl.TYPE_4)) // 剩余次数
			.writeInt(ply.bLottery_coin ? 0 : (600000 - ply.lottery_coin_free_recoverT) / 1000) // 剩余时间
			.writeInt(3) // type
			.writeInt(GlobalConst.lottery_credit_free_times - ply.getTimesCtrl().getTimes(TimesCtrl.TYPE_5)) // 剩余次数
			.writeInt(TimeManager.getSurplusMsecOfCurDay()) // 剩余时间
			.packAndSend(ply.getSocket());
		}
	}

}
