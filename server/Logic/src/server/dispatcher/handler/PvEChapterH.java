package server.dispatcher.handler;

import data.xml.Chapter;
import data.xml.Chapter.ChapterT;
import data.xml.Fort.FortT;
import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.FortCtrl;
import obj.ctrl.FortCtrl.FortDBRow;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class PvEChapterH extends GameHandler {

	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			byte chapter = (byte) buf.readInt();
			byte hard = (byte) buf.readInt();
			ChapterT chapterT = Chapter.getChapterT(chapter, hard);
			if (chapterT == null) {
				System.out.println("没有该关卡信息，chapter：" + chapter + "，hard：" + hard);
				return;
			}
			SndMsg.pveChapter.reset().writeInt(1)
			.writeInt(chapter).writeInt(hard)
			.writeInt(chapterT.getListFortT().size());
			for (FortT fortT : chapterT.getListFortT()) {
				SndMsg.pveChapter.writeInt(fortT.getId());
				FortDBRow row = ply.getFortCtrl().getDBRow(fortT.getId());
				if (row != null) {
					SndMsg.pveChapter.writeInt(FortCtrl.getFortStar(row))
					.writeInt(FortCtrl.getFortTimes(row));
				} else {
					SndMsg.pveChapter.writeInt(0).writeInt(0);
				}
			}
			SndMsg.pveChapter.writeInt(0) // 奖励进度
			.packAndSend(ply.getSocket());
		}
	}

}
