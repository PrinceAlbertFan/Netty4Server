package data.xml;

import java.util.ArrayList;
import java.util.List;

import data.global.GloTmpVal;
import data.global.GlobalConst;
import obj.Player;
import obj.ctrl.TimesCtrl;
import server.SndMsg;

public final class Lot {
	
	public static final byte STORE_ID_1 	= 0; // 金币普通卡库
	public static final byte STORE_ID_2 	= 1; // 钻石普通卡库
	public static final byte STORE_ID_10 	= 2; // 金币特殊卡库，金币每普通抽卡9次后，使用一次金币特殊卡库
	public static final byte STORE_ID_20 	= 3; // 钻石特殊卡库，钻石每普通抽卡9次后，使用一次钻石特殊卡库
	public static final byte STORE_ID_11 	= 4; // 金币首次单抽卡库（首次单抽走该卡库，随后走普通卡库）
	public static final byte STORE_ID_21 	= 5; // 钻石首次单抽卡库（首次单抽走该卡库，随后走普通卡库）
	public static final byte STORE_ID_12 	= 6; // 金币首次十连特殊卡库（第一次十连抽，其中9次走普通卡库，1次走首抽特殊卡库）
	public static final byte STORE_ID_22 	= 7; // 钻石首次十连特殊卡库（第一次十连抽，其中9次走普通卡库，1次走首抽特殊卡库）
	
	public static final byte STORE_ID_NUM	= 8;
	
	public final static class Loot {
		private int lootId;
		private int qtyBase;
		private int qtyRnd;
		private int qty = 1;
		private int prob;
		private byte type;
		
		public final int getRootId() { return lootId; }
		//public final int getQtyBase() { return qtyBase; }
		//public final int getQtyRnd() { return qtyRnd; }
		public final int getRndQty() { return qty = (int) (Math.random() * qtyRnd) + qtyBase; }
		public final int getQty() { return qty; }
		public final int getProb() { return prob; }
		public final byte getType() { return type; }
	}
	public final static class LootGrp {
		public final List<Loot> listLoot = new ArrayList<Loot>();
		private int totalProb;
		
		public final int getTotalProb() { return totalProb; }
	}
	
	private static LootGrp[][] arrArrLootGrp;
	
	private static LootGrp _tmpLootGrp;
	private static Loot _tmpLoot;
	
	private static final Loot[] _arrLoot = new Loot[10];
	
	public static boolean make(Player ply, boolean bOnce, boolean bCommon) {
		if (bOnce) { // 抽1次
			if (bCommon) { // 金币抽
				if (ply.bLottery_coin && ply.getTimesCtrl().getTimes(TimesCtrl.TYPE_4) != GlobalConst.lottery_coin_free_times) {
					ply.bLottery_coin = false;
					ply.getTimesCtrl().addOneTimes(TimesCtrl.TYPE_4);
				} else if (!ply.getResCtrl().subtrCoin(GlobalConst.lottery_coin_1_price)) {
					return false;
				}
				ply.getResCtrl().addRes(GlobalConst.lottery_coin_item_id, 1);
				GloTmpVal._tmpI = ply.getTimesCtrl().addOneTimes(TimesCtrl.TYPE_0);
				_tmpLootGrp = arrArrLootGrp[ply.getLv()][GloTmpVal._tmpI != 1 ? (GloTmpVal._tmpI % 10 != 0 ? STORE_ID_1 : STORE_ID_10) : STORE_ID_11];
			} else { // 钻石抽
				if (ply.getTimesCtrl().getTimes(TimesCtrl.TYPE_5) != GlobalConst.lottery_credit_free_times) {
					ply.getTimesCtrl().addOneTimes(TimesCtrl.TYPE_5);
				} else if (!ply.getResCtrl().subtrCredit(GlobalConst.lottery_credit_1_price)) {
					return false;
				}
				ply.getResCtrl().addRes(GlobalConst.lottery_credit_item_id, 1);
				GloTmpVal._tmpI = ply.getTimesCtrl().addOneTimes(TimesCtrl.TYPE_1);
				_tmpLootGrp = arrArrLootGrp[ply.getLv()][GloTmpVal._tmpI != 1 ? (GloTmpVal._tmpI % 10 != 0 ? STORE_ID_2 : STORE_ID_20) : STORE_ID_21];
			}
			GloTmpVal.tmpI = (int) (Math.random() * _tmpLootGrp.getTotalProb());
			for (Loot loot : _tmpLootGrp.listLoot) {
				if (GloTmpVal.tmpI < loot.getProb()) {
					_tmpLoot = loot;
					break;
				} else {
					GloTmpVal.tmpI -= loot.getProb();
				}
			}
			if (_tmpLoot.getType() == 1) {
				ply.getResCtrl().addRes(_tmpLoot.getRootId(), _tmpLoot.getRndQty());
			} else {
				ply.getPartnCtrl().addPartn((short) _tmpLoot.getRootId());
			}
			SndMsg.lotRes.reset().writeInt(1)
			.writeInt(_tmpLoot.getType()).writeInt(_tmpLoot.getRootId()).writeInt(_tmpLoot.getQty())
			.writeInt(ply.getResCtrl().getCoin()).writeInt(ply.getResCtrl().getCredit())
			.packAndSend(ply.getSocket());
		} else { // 抽10次
			if (bCommon) { // 金币抽
				if (!ply.getResCtrl().subtrCoin(GlobalConst.lottery_coin_10_price)) {
					return false;
				}
				ply.getResCtrl().addRes(GlobalConst.lottery_coin_item_id, 10);
				GloTmpVal._tmpI = ply.getTimesCtrl().addOneTimes(TimesCtrl.TYPE_2);
				_tmpLootGrp = arrArrLootGrp[ply.getLv()][GloTmpVal._tmpI != 1 ? STORE_ID_10 : STORE_ID_12];
			} else { // 钻石抽
				if (!ply.getResCtrl().subtrCredit(GlobalConst.lottery_credit_10_price)) {
					return false;
				}
				ply.getResCtrl().addRes(GlobalConst.lottery_credit_item_id, 10);
				GloTmpVal._tmpI = ply.getTimesCtrl().addOneTimes(TimesCtrl.TYPE_3);
				_tmpLootGrp = arrArrLootGrp[ply.getLv()][GloTmpVal._tmpI != 1 ? STORE_ID_20 : STORE_ID_22];
			}
			GloTmpVal.tmpI = (int) (Math.random() * _tmpLootGrp.getTotalProb());
			for (Loot loot : _tmpLootGrp.listLoot) {
				if (GloTmpVal.tmpI < loot.getProb()) {
					if (loot.getType() == 1) {
						ply.getResCtrl().addRes(loot.getRootId(), loot.getRndQty());
					} else {
						ply.getPartnCtrl().addPartn((short) loot.getRootId());
					}
					_arrLoot[9] = loot;
					break;
				} else {
					GloTmpVal.tmpI -= loot.getProb();
				}
			}
			GloTmpVal.tmpB = 0;
			_tmpLootGrp = bCommon ? arrArrLootGrp[ply.getLv()][STORE_ID_1] : arrArrLootGrp[ply.getLv()][STORE_ID_2];
			do {
				GloTmpVal.tmpI = (int) (Math.random() * _tmpLootGrp.getTotalProb());
				for (Loot loot : _tmpLootGrp.listLoot) {
					if (GloTmpVal.tmpI < loot.getProb()) {
						if (loot.getType() == 1) {
							ply.getResCtrl().addRes(loot.getRootId(), loot.getRndQty());
						} else {
							ply.getPartnCtrl().addPartn((short) loot.getRootId());
						}
						_arrLoot[GloTmpVal.tmpB++] = loot;
						break;
					} else {
						GloTmpVal.tmpI -= loot.getProb();
					}
				}
			} while (GloTmpVal.tmpB != 9);
			SndMsg.lotRes.reset().writeInt(10);
			for (Loot loot : _arrLoot) {
				SndMsg.lotRes.writeInt(loot.getType()).writeInt(loot.getRootId()).writeInt(loot.getQty());
			}
			SndMsg.lotRes.writeInt(ply.getResCtrl().getCoin()).writeInt(ply.getResCtrl().getCredit())
			.packAndSend(ply.getSocket());
		}
		return true;
	}
}
