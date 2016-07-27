package server.dispatcher.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import data.xml.Drop.DropT;
import data.xml.Drop;
import data.xml.Fort;
import data.xml.Resource;
import data.xml.Fort.FortT;
import data.xml.Fort.BattleT;
import io.netty.buffer.ByteBuf;
import obj.Player;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class PvEStartH extends GameHandler {
	
	private static Iterator<Map.Entry<Integer, Integer>> _tmpMapDropIter;
	private static Entry<Integer, Integer> _tmpMapDropEntry;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			ply.leaveBattle();
			int btl_id = buf.readInt();// map
			if (!ply.getFortCtrl().isCanOpenFort(btl_id)) {
				System.out.println("关卡剩余次数不足");
				return;
			}
			buf.readInt();// hard
			
			FortT fortT = Fort.getFortT(btl_id);
			if (ply.getResCtrl().getPower() < fortT.getPower()) {
				System.out.println("体力值不够，无法进入PVE，当前体力值：" + ply.getResCtrl().getPower()
						+ "，需要体力值：" + fortT.getPower());
				return;
			}
			SndMsg.pveStart.reset()
			.writeInt(fortT.getListBattleT().size());
			for (BattleT bt : fortT.getListBattleT()) {
				SndMsg.pveStart.writeInt(bt.getBtlNo());
				if (bt.isHasDrop()) {
					for (DropT dropT : bt.listDropId) {
						Drop.make(ply, dropT);
					}
				}
				if (bt.isHasBossDrop()) {
					for (DropT dropT : bt.listBossDropId) {
						Drop.make(ply, dropT);
					}
				}
				Integer coin = Drop._mapDropMake.remove(Resource.ID_99901);
				SndMsg.pveStart.writeInt(coin != null ? coin : 0) // 金币
				.writeBoolean(bt.isHasBossDrop())
				.writeInt(Drop._mapDropMake.size()); // List<ItemInfo> items
				_tmpMapDropIter = Drop._mapDropMake.entrySet().iterator();
				while (_tmpMapDropIter.hasNext()) {
					_tmpMapDropEntry = _tmpMapDropIter.next();
					SndMsg.pveStart.writeInt(_tmpMapDropEntry.getKey()).writeInt(0) // cid
					.writeInt(_tmpMapDropEntry.getValue()).writeInt(0); // oldvalue
				}
				Drop._mapDropMake.clear();
			}
			SndMsg.pveStart.packAndSend(ply.getSocket());
		}
	}

}
