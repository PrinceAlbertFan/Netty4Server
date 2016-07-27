package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import manager.TimeManager;
import obj.GameController;
import obj.Player;
import obj.ctrl.ResCtrl.ResDBRow;
import server.SndMsg;
import tool.SendBuf;
import data.global.GloTmpVal;
import data.xml.Resource;
import data.xml.Task;
import data.xml.Task.TaskReward;
import data.xml.Task.TaskT;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class TaskCtrl implements DbLoader, GameController {
	
	/**未完成*/
	private static final byte TASK_FLAG_0 = 0;
	/**已完成，未领奖*/
	private static final byte TASK_FLAG_1 = 1;
	/**已完成，已领奖*/
	private static final byte TASK_FLAG_2 = 2;
	/**永久完成，已领奖，已销毁*/
	private static final byte TASK_FLAG_3 = 3;
	/**永久完成，已领奖，未销毁，待后续*/
	private static final byte TASK_FLAG_4 = 4;
	
	private final static class TaskDBRow extends DbObj {
		long id; TaskT taskT; byte type; int cond; byte flag;
	}
	
	private static final List<TaskDBRow> _tmpListTask = new ArrayList<TaskDBRow>();
	private static final List<TaskT> _tmpListTaskT = new ArrayList<TaskT>();
	
	/**所有的任务编号表*/
	private final Set<Integer> _setTask = new HashSet<Integer>();
	/**当前任务表*/
	private final Map<Integer, TaskDBRow> _rowMap = new HashMap<Integer, TaskDBRow>();
	/**当前未完成任务*/
	private final Map<Long, TaskDBRow> _mapRefreshTask = new HashMap<Long, TaskDBRow>();
	
	private Player ply;
	private boolean bUnfull;
	
	private void _addNewTask(TaskT taskT) {
		TaskDBRow _row = new TaskDBRow();
		_row.id = DbCenter.nextPlyTaskId();
		_row.taskT = taskT;
		_row.type = taskT.getType();
		DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyTask(),
				_row.id, ply.getPlyId(), taskT.getId(), taskT.getType(), 0, TASK_FLAG_0));
		_mapRefreshTask.put((long) taskT.getCondition() * 1000 + taskT.getType(), _row);
		_rowMap.put(taskT.getId(), _row);
		_setTask.add(taskT.getId());
		if (_row.type > 100) {
			switch (_row.type) {
			case 101:
				if (ply.getLv() >= taskT.getCondition()) {
					refreshTask(_row.type, taskT.getCondition(), 1);
				}
				break;
			case 102:
			case 103:
			case 104:
				if (ply.getFortCtrl().isCompleteFort(taskT.getCondition())) {
					refreshTask(_row.type, taskT.getCondition(), 1);
				}
				break;
			case 105:
				GloTmpVal._tmpS = ply.getPartnCtrl().checkCountOfPartnLvForTask((short) taskT.getCondition(), (short) taskT.getConditionNum());
				if (GloTmpVal._tmpS != 0) {
					refreshTask(_row.type, taskT.getCondition(), GloTmpVal._tmpS);
				}
				break;
			case 106:
				GloTmpVal._tmpS = ply.getPartnCtrl().checkCountOfPartnStepForTask((byte) (taskT.getCondition() - 1), (short) taskT.getConditionNum());
				if (GloTmpVal._tmpS != 0) {
					refreshTask(_row.type, taskT.getCondition(), GloTmpVal._tmpS);
				}
				break;
			case 107:
				GloTmpVal._tmpS = ply.getPartnCtrl().checkCountOfPartnStarForTask((byte) (taskT.getCondition() - 1), (short) taskT.getConditionNum());
				if (GloTmpVal._tmpS != 0) {
					refreshTask(_row.type, taskT.getCondition(), GloTmpVal._tmpS);
				}
				break;
			case 109:
				GloTmpVal._tmpS = ply.getGodCtrl().checkCountOfGodStarForTask((byte) taskT.getCondition(), (short) taskT.getConditionNum());
				if (GloTmpVal._tmpS != 0) {
					refreshTask(_row.type, taskT.getCondition(), GloTmpVal._tmpS);
				}
				break;
			case 110:
				GloTmpVal._tmpS = ply.getGodCtrl().checkCountOfGodLvForTask((short) taskT.getCondition(), (short) taskT.getConditionNum());
				if (GloTmpVal._tmpS != 0) {
					refreshTask(_row.type, taskT.getCondition(), GloTmpVal._tmpS);
				}
				break;
			case 121:
				GloTmpVal._tmpS = ply.getFortCtrl().checkCountOfFortStarForTask((byte) taskT.getCondition(), (short) taskT.getConditionNum());
				if (GloTmpVal._tmpS != 0) {
					refreshTask(_row.type, taskT.getCondition(), GloTmpVal._tmpS);
				}
				break;
			default:
				break;
			}
		}
	}
	
	private void _destoryTask(TaskDBRow row) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyTask(), 0, TASK_FLAG_3, row.id));
		_rowMap.remove(row.taskT.getId());
	}
	
	private void _taskListSnd(SendBuf buf) {
		GloTmpVal.tmpI = buf.getWriterIndex();
		buf.writeInt(GloTmpVal.tmpS = 0);
		for (TaskDBRow row : _rowMap.values()) {
			if (row.flag < 2) {
				++GloTmpVal.tmpS;
				buf.writeInt(row.taskT.getId()).writeInt(row.cond);
			}
		}
		buf.setInt(GloTmpVal.tmpI, GloTmpVal.tmpS)
		.packAndSend(ply.getSocket());
	}
	
	private void _taskRwdSndAdd(TaskT taskT) {
		GloTmpVal._tmpS = ply.getLv();
		SndMsg.taskRwd.reset().writeInt(taskT.getId()).writeInt(GloTmpVal.tmpS = 0);
		GloTmpVal.tmpI = 0;
		for (TaskReward rwd : taskT.listRwd) {
			if (rwd.rwdType == 1) {
				switch (rwd.rwdId) {
				case Resource.ID_99904:
					ply.getResCtrl().addExp(rwd.rwdNum);
					break;
				case Resource.ID_99901:
					++GloTmpVal.tmpS;
					SndMsg.taskRwd.writeInt(901).writeInt(ply.getResCtrl().getCoin()).writeInt(ply.getResCtrl().addCoin(rwd.rwdNum));
					break;
				case Resource.ID_99902:
					++GloTmpVal.tmpS;
					SndMsg.taskRwd.writeInt(902).writeInt(ply.getResCtrl().getCredit()).writeInt(ply.getResCtrl().addCredit(rwd.rwdNum));
					break;
				case Resource.ID_99903:
					ply.getResCtrl().addPower(rwd.rwdNum);
//					++GlobTempValue.tempShort;
//					SndMsg.taskRwd.writeInt(903).writeInt(ply.getResCtrl().getPower()).writeInt(ply.getResCtrl().addPower(rwd.rwdNum));
					break;
				default:
					if (rwd.rwdId < 99801) {
						++GloTmpVal.tmpI;
					}
					break;
				}
			}
		}
		SndMsg.taskRwd.setInt(11, GloTmpVal.tmpS);
		SndMsg.taskRwd.writeInt(GloTmpVal.tmpI);
		for (TaskReward rwd : taskT.listRwd) {
			if (rwd.rwdType == 1 && rwd.rwdId < 99801) {
				ResDBRow resRow = ply.getResCtrl().addInfactRes(rwd.rwdId, rwd.rwdNum);
				SndMsg.taskRwd.writeInt(rwd.rwdId).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty - rwd.rwdNum);
				
			}
		}
		SndMsg.taskRwd.writeInt(ply.getResCtrl().getExp()).writeInt(GloTmpVal._tmpS).writeInt(ply.getLv()).writeInt(ply.getResCtrl().getPower());
	}
	
	public void refreshTask(byte type, int condition, int addCondNum) {
		TaskDBRow row = _mapRefreshTask.get(GloTmpVal.tmpL = condition * 1000 + type);
		if (row != null) {
			if ((addCondNum += row.cond) > row.taskT.getConditionNum()) {
				addCondNum = row.taskT.getConditionNum();
			}
			row.setNeedSave();
			if (addCondNum == row.taskT.getConditionNum()) {
				_mapRefreshTask.remove(GloTmpVal.tmpL);
				row.flag = TASK_FLAG_1;
			}
			row.cond = addCondNum;
		}
	}
	
	public void finishTask(int id) {
		TaskDBRow row = _rowMap.get(id);
		if (row != null && row.flag == TASK_FLAG_1) {
			// 获取奖励
			_taskRwdSndAdd(row.taskT);
			row.setNeedSave();
			if (row.type < 100) {
				row.flag = TASK_FLAG_2;
			} else if (row.taskT.isHasNextTask()) {
				row.flag = TASK_FLAG_3;
				for (TaskT taskT : row.taskT.getListNextTask()) {
					if (ply.getLv() >= taskT.getLvlimit()) {
						_addNewTask(taskT);
					} else {
						row.flag = TASK_FLAG_4;
					}
				}
				if (row.flag == TASK_FLAG_3) {
					_destoryTask(row);
					ply.setTaskCount((short) _setTask.size());
				} else if (_setTask.size() != ply.getTaskCount()) {
					ply.setTaskCount((short) _setTask.size());
				}
			} else {
				row.flag = TASK_FLAG_4;
			}
			_taskListSnd(SndMsg.taskRwd);
		}
	}
	
	public void checkAllTask() {
		if (bUnfull) {
			bUnfull = false;
			for (TaskT taskT : Task.getListInitTaskT()) {
				if (!_setTask.contains(taskT.getId())) {
					if (ply.getLv() < taskT.getLvlimit()) {
						bUnfull = true;
					} else {
						_addNewTask(taskT);
					}
				}
			}
		}
		for (TaskDBRow row : _rowMap.values()) {
			if (row.flag == TASK_FLAG_4 && row.taskT.isHasNextTask()) {
				_tmpListTask.add(row);
			}
		}
		if (!_tmpListTask.isEmpty()) {
			boolean isChangeFlag;
			for (TaskDBRow row : _tmpListTask) {
				isChangeFlag = true;
				for (TaskT taskT : row.taskT.getListNextTask()) {
					if (!_setTask.contains(taskT.getId())) {
						if (ply.getLv() < taskT.getLvlimit()) {
							isChangeFlag = false;
						} else {
							_addNewTask(taskT);
						}
					}
				}
				if (isChangeFlag) {
					_destoryTask(row);
				}
			}
			_tmpListTask.clear();
		}
		if (_setTask.size() != ply.getTaskCount()) {
			ply.setTaskCount((short) _setTask.size());
		}
	}
	
	public void refreshAllTask() {
		for (TaskDBRow row : _rowMap.values()) {
			if (row.type < 100) {
				row.setNeedSave();
				row.cond = 0;
				if (row.flag != TASK_FLAG_0) {
					row.flag = TASK_FLAG_0;
					_mapRefreshTask.put((long) row.taskT.getCondition() * 1000 + row.type, row);
				}
			}
		}
	}
	
	@Override
	public void onceNoticeClient() {
		_taskListSnd(SndMsg.taskList.reset());
	}

	@Override
	public void saveToDB() {
		for (TaskDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyTask(), row.cond, row.flag, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		TaskDBRow row;
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			GloTmpVal.tmpL = buf.readLong();
			_setTask.add(GloTmpVal.tmpI = buf.readInt());
			if ((GloTmpVal.tmpB = buf.getByte(buf.readerIndex() + 5)) != TASK_FLAG_3) {
				row = new TaskDBRow();
				row.id = GloTmpVal.tmpL;
				row.taskT = Task.getTaskT(GloTmpVal.tmpI);
				row.type = buf.readByte();
				row.cond = buf.readInt();
				buf.skipBytes(1);
				if ((row.flag = GloTmpVal.tmpB) == TASK_FLAG_0) {
					_mapRefreshTask.put((long) row.taskT.getCondition() * 1000 + row.type, row);
				}
				_rowMap.put(row.taskT.getId(), row);
			} else {
				buf.skipBytes(6);
			}
		}
		checkAllTask();
		//**以下可能临时代码为了应付策划随时修改添加新任务类型的操作，上线前可去掉**//
		for (Integer taskId : _setTask) {
			if (!_rowMap.containsKey(taskId)) {
				TaskT taskT = Task.getTaskT(taskId);
				if (taskT.isHasNextTask()) {
					for (TaskT t : taskT.getListNextTask()) {
						if (!_setTask.contains(t.getId()) && ply.getLv() >= t.getLvlimit()) {
							_tmpListTaskT.add(t);
						}
					}
				}
			}
		}
		if (!_tmpListTaskT.isEmpty()) {
			for (TaskT taskT : _tmpListTaskT) {
				_addNewTask(taskT);
			}
			_tmpListTaskT.clear();
			ply.setTaskCount((short) _setTask.size());
		}
	}
	
	public TaskCtrl(Player _ply) {
		ply = _ply;
		if (bUnfull = ply.getTaskCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyTask(), ply.getPlyId(), ply.getTaskCount()), ply.getSocket(), (byte) 4);
			if (ply.getLogoutT() < TimeManager.getPassedGmtMsecOfCurDay()) {
				refreshAllTask();
			}
		} else {
			for (TaskT taskT : Task.getListInitTaskT()) {
				if (ply.getLv() >= taskT.getLvlimit()) {
					_addNewTask(taskT);
				} else {
					bUnfull = true;
				}
			}
			ply.setTaskCount((short) _setTask.size());
		}
	}
}
