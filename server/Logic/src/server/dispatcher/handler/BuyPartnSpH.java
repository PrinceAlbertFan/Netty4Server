package server.dispatcher.handler;

import data.global.GloTmpVal;
import data.xml.ResetTimes;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class BuyPartnSpH extends GameHandler {
	
	private static int[] _tmpData;
	private static byte times;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			times = ply._arrResetTimes[4];
			_tmpData = ResetTimes.data[4][times < 30 ? times + 1 : 30];
			GloTmpVal._tmpI = ply.getResCtrl().subtrRes(_tmpData[0], _tmpData[1]);
			if (GloTmpVal._tmpI == -1) {
				System.out.println("资源不足，购买英雄技能点失败");
				return;
			}
			if (times < 30) {
				++ply._arrResetTimes[4];
			}
			SndMsg.buyPartnSp.reset()
			.writeInt(_tmpData[0] % 1000).writeInt(GloTmpVal._tmpI + _tmpData[1]).writeInt(GloTmpVal._tmpI)
			.writeInt(ply.getResCtrl().addRes(_tmpData[2], _tmpData[3]))
			.pack(ply.getSocket()).send();
		}
	}

}
