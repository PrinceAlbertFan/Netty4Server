package data.xml;

import java.util.Map;

public final class Monster {
	public final static class MonsterT {
		private int id;
		private boolean bBoss;
		
		public final int getId() { return id; }
		public final boolean isBoss() { return bBoss; }
	}
	
	private static Map<Integer, MonsterT> _mapMonsterT;
	
	public final static MonsterT getMonsterT(int id) {
		return _mapMonsterT.get(id);
	}
}
