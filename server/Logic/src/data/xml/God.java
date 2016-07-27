package data.xml;

import java.util.Map;

public final class God {
	public final static class GodT {
		public final short[] arrStarCost = new short[3];
		
		private int synRes;
		//private short synResNum;
		
		private short id;
		private byte star;
		
		public final int getSynRes() { return synRes; }
		//public final short getSynResNum() { return synResNum; }
		
		public final short getId() { return id; }
		public final byte getStar() { return star; }
	}
	
	private static Map<Short, GodT> mapGodT;
	
	public final static GodT getGodT(short id) {
		return mapGodT.get(id);
	}
}
