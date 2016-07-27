package data.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Task {
	public final static class TaskReward {
		public int rwdId;
		public int rwdNum;
		public byte rwdType;
	}
	public final static class TaskT {
		public final List<TaskReward> listRwd = new ArrayList<TaskReward>();
		private List<TaskT> listNextTask;
		
		private int id;
		private int prevId;
		private int condition;
		private int condNum;
		private short lvLimit;
		private byte type;
		
		public final List<TaskT> getListNextTask() { return listNextTask; }
		public final int getId() { return id; }
		public final int getPrevId() { return prevId; }
		public final int getCondition() { return condition; }
		public final int getConditionNum() { return condNum; }
		public final short getLvlimit() { return lvLimit; }
		public final byte getType() { return type; }
		public final boolean isHasNextTask() { return listNextTask != null; }
	}
	
	private static List<TaskT> listInitTaskT;
	private static Map<Integer, TaskT> mapTaskT;
	
	public final static List<TaskT> getListInitTaskT() {
		return listInitTaskT;
	}
	
	public final static TaskT getTaskT(int id) {
		return mapTaskT.get(id);
	}
}
