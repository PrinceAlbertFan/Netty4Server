package server.dispatcher.handler;

import data.global.GloTmpVal;
import data.xml.ResetTimes;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class BuyPowerH extends GameHandler {
	
	private static int[] _tmpData;
	private static byte times;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			times = ply._arrResetTimes[0];
			_tmpData = ResetTimes.data[0][times < 30 ? times + 1 : 30];
			GloTmpVal._tmpI = ply.getResCtrl().subtrRes(_tmpData[0], _tmpData[1]);
			if (GloTmpVal._tmpI == -1) {
				System.out.println("资源不足，购买体力失败");
				return;
			}
			if (times < 30) {
				++ply._arrResetTimes[0];
			}
			GloTmpVal.tmpI = ply.getResCtrl().addRes(_tmpData[2], _tmpData[3]);
			SndMsg.buyPower.reset()
			.writeInt(_tmpData[0] % 1000).writeInt(GloTmpVal._tmpI + _tmpData[1]).writeInt(GloTmpVal._tmpI)
			.writeInt(_tmpData[2] % 1000).writeInt(GloTmpVal.tmpI - _tmpData[3]).writeInt(GloTmpVal.tmpI)
			.pack(ply.getSocket()).send();
		}
	}

}
