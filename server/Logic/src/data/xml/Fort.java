package data.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import obj.Player;
import data.xml.Drop.DropT;

public final class Fort {
	public final static class BattleT {
		public final List<DropT> listDropId = new ArrayList<DropT>();
		public final List<DropT> listBossDropId = new ArrayList<DropT>();
		private byte btl_no;
		private boolean bDrop;
		private boolean bBossDrop;
		
		public final byte getBtlNo() { return btl_no; }
		public final boolean isHasDrop() { return bDrop; }
		public final boolean isHasBossDrop() { return bBossDrop; }
	}
	
	public final static class FirstDropT {
		public int id;
		public int num;
		public byte type;
	}
	
	public final static class FortT {
		
		public final List<FirstDropT> listFirstDropT = new ArrayList<FirstDropT>();
		private final List<BattleT> listBattleT = new ArrayList<BattleT>();
		
		private int id;
		
		private int prevId;
		private int nextId;
		
		private int power;
		private short exp;
		private short partnExp;
		private short times;
		private byte hard;
		private byte type;
		private byte chapter;
		
		public final List<BattleT> getListBattleT() { return listBattleT; }
		public final int getId() { return id; }
		public final int getPrevId() { return prevId; }
		public final int getNextId() { return nextId; }
		
		public final int getPower() { return power; }
		public final short getExp() { return exp; }
		public final short getPartnExp() { return partnExp; }
		public final short getTimes() { return times; }
		public final byte getHard() { return hard; }
		public final byte getType() { return type; }
		public final byte getChapter() { return chapter; }
	}
	
	private static Map<Integer, FortT> mapFortT;
	
	public final static FortT getFortT(int id) {
		return mapFortT.get(id);
	}
	
	/**首胜奖励*/
	public static void make(Player ply, FortT fortT) {
		for (FirstDropT dropT : fortT.listFirstDropT) {
			if (dropT.type == 1) {
				ply._mapDropMake.put(dropT.id, dropT.num);
			} else if (dropT.type == 2) {
				ply.getPartnCtrl().addPartn((short) dropT.id);
			} else {
				ply.getGodCtrl().addGod((short) dropT.id);
			}
		}
		if (fortT.getType() == 1) {
			ply.getTaskCtrl().refreshTask((byte) (101 + fortT.getHard()), fortT.getId(), 1);
		}
	}
}
