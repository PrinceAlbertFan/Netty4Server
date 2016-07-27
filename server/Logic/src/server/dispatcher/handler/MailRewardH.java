package server.dispatcher.handler;

import data.global.GloTmpVal;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class MailRewardH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			System.out.println("收到领取邮件附件消息");
			GloTmpVal._tmpI = buf.readInt();
			GloTmpVal.tmpI = ply.getResCtrl().addRes(GloTmpVal._tmpI, buf.readInt());
			SndMsg.resInfo.reset().writeInt(GloTmpVal._tmpI).writeInt(GloTmpVal.tmpI).pack(ply.getSocket()).send();
		}
	}

}
