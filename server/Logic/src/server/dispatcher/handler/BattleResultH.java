package server.dispatcher.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import data.xml.Fort.FortT;
import data.xml.Resource;
import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.PartnCtrl;
import obj.ctrl.PartnCtrl.PartnDBRow;
import obj.ctrl.TroopCtrl.TroopDBRow;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class BattleResultH extends GameHandler {
	
	private static Iterator<Map.Entry<Integer, Integer>> _tmpMapDropIter;
	private static Entry<Integer, Integer> _tmpMapDropEntry;
	private static final int[] _arrLv = new int[4];
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			int mapId = buf.readInt(); //win
			//System.out.println("mapId:" + mapId);
			int isWin = buf.readInt(); //win
			//System.out.println("isWin:" + isWin);
			int star = buf.readInt(); //star
			if (star < 0) {
				System.out.println("star不能为负数");
				return;
			}
			//System.out.println("star:" + star);
			//buf.readInt();// 波数
			//for();list<int>
			
			FortT fortT = ply.getFortCtrl().refreshFort(mapId, (byte) star);
			
			short oldLv = ply.getLv();
			if (isWin != 0) {
				ply.getResCtrl().addExp(fortT.getExp());
				ply.getResCtrl().subtrPower(fortT.getPower());
			}
			byte size = 0;
			TroopDBRow troopRow = ply.getTroopCtrl().getTroop((byte) 1);
			for (PartnDBRow partnRow : troopRow.arrPartn) {
				if (partnRow != null) {
					_arrLv[size++] = PartnCtrl.getPartnLv(partnRow);
					PartnCtrl.addPartnExp(ply, partnRow, fortT.getPartnExp(), ply.getLevelT().getPartnLvLimit());
				} else {
					break;
				}
			}
			SndMsg.pveResult.reset().writeInt(isWin);
			if (isWin != 0) {
				Integer coin = ply._mapDropMake.remove(Resource.ID_99901);
				int addCoin = coin != null ? coin : 0;
				SndMsg.pveResult.writeInt(addCoin); // coin
				if (addCoin != 0) {
					ply.getResCtrl().addCoin(addCoin);
				}
			} else {
				SndMsg.pveResult.writeInt(0);
			}
			SndMsg.pveResult.writeInt(fortT.getNextId())
			.writeInt(ply.getResCtrl().getExp())
			.writeInt(oldLv)
			.writeInt(ply.getLv())
			.writeInt(ply.getResCtrl().getPower())
			.writeInt(size);
			size = 0;
			for (PartnDBRow row : troopRow.arrPartn) {
				if (row != null) {
					SndMsg.pveResult.writeInt(PartnCtrl.getPartnId(row))
					.writeInt(PartnCtrl.getPartnExp(row))
					.writeInt(_arrLv[size++])
					.writeInt(PartnCtrl.getPartnLv(row));
				} else {
					break;
				}
			}
			if (isWin != 0) {
				SndMsg.pveResult.writeInt(ply._mapDropMake.size()); // itemList
				_tmpMapDropIter = ply._mapDropMake.entrySet().iterator();
				while (_tmpMapDropIter.hasNext()) {
					_tmpMapDropEntry = _tmpMapDropIter.next();
					SndMsg.pveResult.writeInt(_tmpMapDropEntry.getKey()).writeInt(0) // cid
					.writeInt(_tmpMapDropEntry.getValue()).writeInt(0); // oldvalue
					ply.getResCtrl().addRes(_tmpMapDropEntry.getKey(), _tmpMapDropEntry.getValue());
				}
				if (fortT.getType() == 1) {
					switch (fortT.getHard()) {
					case 1: ply.getTaskCtrl().refreshTask((byte) 2, 0, 1); break;
					case 2: ply.getTaskCtrl().refreshTask((byte) 3, 0, 1); break;
					default:ply.getTaskCtrl().refreshTask((byte) 4, 0, 1); break;
					}
				}
			} else {
				SndMsg.pveResult.writeInt(0);
			}
			ply._mapDropMake.clear();
			SndMsg.pveResult.packAndSend(ply.getSocket());
		}
	}

}
