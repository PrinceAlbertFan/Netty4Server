package data.xml;

import java.util.HashMap;
import java.util.Map;

public final class Resource {
	
	/**金币*/
	public static final int ID_99901 = 99901;
	/**钻石*/
	public static final int ID_99902 = 99902;
	/**体力*/
	public static final int ID_99903 = 99903;
	/**领主经验*/
	public static final int ID_99904 = 99904;
	/**英雄经验*/
	public static final int ID_99905 = 99905;
	/**邪灵*/
	public static final int ID_99906 = 99906;
	/**英雄技能点*/
	public static final int ID_99907 = 99907;
	
	
	public final static class EquipT {
		public short lvLimit;
		public int cost_base;
		public int cost_add;
		public byte max_en_num;
	}
	public final static class UseReward {
		public int rewardId;
		public int rewardNum;
		public byte rewardType;
	}
	public final static class SysnT {
		public final Map<Integer, Short> mapSysn = new HashMap<Integer, Short>();
		public int coin_cost;
	}
	public final static class ResourceT {
		private EquipT equipT;
		private UseReward useReward;
		private SysnT sysnT;
		
		private int id;
		private int sellCoin;
		
		private short type;
		
		//private byte quality;
		
		//private boolean isSeller;
		public final EquipT getEquipT() { return equipT; }
		public final UseReward getUseReward() { return useReward; }
		public final SysnT getSysnT() { return sysnT; }
		
		public final int getId() { return id; }
		public final int getSellCoin() { return sellCoin; }
		public final short getType() { return type; }
		
		public final boolean isUsable() { return useReward != null; }
		public final boolean isSysnable() { return sysnT != null; }
	}
	
	private static Map<Integer, ResourceT> mapResT;
	
	public final static ResourceT getResourceT(int key) {
		return mapResT.get(key);
	}
}
