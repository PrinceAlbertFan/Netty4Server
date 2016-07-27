package data.xml;

public final class Level {
	public final static class LevelT {
		private int exp;
		//private short lv;
		private short powerLimit;
		private short partnLvLimit;
		
		public final int getExp() { return exp; }
		//public final short getLv() { return lv; }
		public final short getPowerLimit() { return powerLimit; }
		public final short getPartnLvLimit() { return partnLvLimit; }
	}
	public final static class PartnLevelT {
		public final int[] arrSklUpCost = new int[4];
		private int exp;
		//private short lv;
		public final int getExp() { return exp; }
		//public final short getLv() { return lv; }
	}
	public final static class GodLevelT {
		private int coin;
		private int soul;
		
		public final int getCoin() { return coin; }
		public final int getSoul() { return soul; }
	}
	
	private static LevelT[] arrLevelT;
	private static PartnLevelT[] arrPartnLevelT;
	private static GodLevelT[] arrGodLevelT;
	
	public final static LevelT getLevelT(short lv) {
		return arrLevelT[lv];
	}
	
	public final static PartnLevelT getPartnLevelT(short lv) {
		return arrPartnLevelT[lv];
	}
	
	public final static GodLevelT getGodLevelT(short lv) {
		return arrGodLevelT[lv];
	}
}
