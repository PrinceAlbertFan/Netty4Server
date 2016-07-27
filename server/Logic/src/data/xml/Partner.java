package data.xml;

import java.util.Map;

import data.xml.Resource.ResourceT;

public final class Partner {
	public final static class PartnerT {
		public final ResourceT[][] arrStepEqpt = new ResourceT[12][6]; // 12阶,6装备位
		public final int[][] arrStarCost = new int[4][2]; // 4个星位,碎片数量+钱币消耗
		
		private int synRes;
		private short synResNum;
		
		private short id;
		private short piece;
		private byte star;
		
		public final int getSynRes() { return synRes; }
		public final short getSynResNum() { return synResNum; }
		
		public final short getId() { return id; }
		public final short getPiece() { return piece; }
		public final byte getStar() { return star; }
	}
	
	private static Map<Short, PartnerT> mapPartnT;
	
	public final static PartnerT getPartnerT(short id) {
		return mapPartnT.get(id);
	}
}
