package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import manager.TimeManager;
import obj.GameController;
import obj.Player;
import server.SndMsg;
import data.xml.Fort;
import data.xml.Fort.FortT;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class FortCtrl implements DbLoader, GameController {
	
	public final static class FortDBRow extends DbObj {
		long id; FortT fortT; byte star; short times;
	}
	
	public final static byte getFortStar(FortDBRow row) { return row.star; }
	public final static short getFortTimes(FortDBRow row) { return row.times; }
	
	/**关卡表*/
	private final Map<Integer, FortDBRow> _rowMap = new HashMap<Integer, FortDBRow>();
	
	private Player ply;
	
	private int curComFort;
	
	public final FortDBRow getDBRow(int id) { return _rowMap.get(id); }
	public final int getCurComFort() { return curComFort; }
	
	public short checkCountOfFortStarForTask(byte chapter, short num) {
		short count = 0;
		for (FortDBRow row : _rowMap.values()) {
			if (row.fortT.getType() == 1 && row.fortT.getHard() == 1
					&& row.fortT.getChapter() == chapter && (count += row.star) >= num) {
				return num;
			}
		}
		return count;
	}
	
	/**刷新关卡，返回关卡模版*/
	public FortT refreshFort(int id, byte star) {
		FortDBRow row = _rowMap.get(id);
		if (row == null) {
			row = new FortDBRow();
			row.id = DbCenter.nextPlyFortId();
			row.fortT = Fort.getFortT(id);
			row.star = star;
			if (row.star != 0) {
				row.times = 1;
				Fort.make(ply, row.fortT);
				if (row.fortT.getType() == 1 && row.fortT.getHard() == 1) {
					curComFort = row.fortT.getId();
					ply.getTaskCtrl().refreshTask((byte) 121, row.fortT.getChapter(), star);
				}
			}
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyFort(),
					row.id, ply.getPlyId(), id, star, row.times));
			_rowMap.put(id, row);
			ply.setFortCount((short) _rowMap.size());
		} else if (star > row.star) {
			row.setNeedSave();
			if (row.star == 0) {
				Fort.make(ply, row.fortT);
				if (row.fortT.getType() == 1 && row.fortT.getHard() == 1) {
					curComFort = row.fortT.getId();
					ply.getTaskCtrl().refreshTask((byte) 121, row.fortT.getChapter(), star);
				}
			} else if (row.fortT.getType() == 1 && row.fortT.getHard() == 1) {
				ply.getTaskCtrl().refreshTask((byte) 121, row.fortT.getChapter(), star - row.star);
			}
			row.star = star;
			++row.times;
		} else if (star != 0) {
			row.setNeedSave();
			++row.times;
		}
		return row.fortT;
	}
	
	/**是否还可继续通关*/
	public boolean isCanOpenFort(int id) {
		FortDBRow row = _rowMap.get(id);
		return row == null || row.times != row.fortT.getTimes();
	}
	
	/**是否通关*/
	public boolean isCompleteFort(int id) {
		FortDBRow row = _rowMap.get(id);
		return row != null && row.star != 0;
	}
	
	public void refreshAllFort() {
		for (FortDBRow row : _rowMap.values()) {
			if (row.times != 0) {
				row.setNeedSave();
				row.times = 0;
			}
		}
	}
	
	@Override
	public void onceNoticeClient() {
		SndMsg.initData.writeInt(ply.getFortCount());
		for (FortDBRow row : _rowMap.values()) {
			SndMsg.initData.writeInt(row.fortT.getId());
		}
	}

	@Override
	public void saveToDB() {
		for (FortDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyFort(),
						row.star, row.times, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		FortDBRow row;
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			row = new FortDBRow();
			row.id = buf.readLong();
			row.fortT = Fort.getFortT(buf.readInt());
			row.star = buf.readByte();
			row.times = buf.readShort();
			if (row.fortT.getType() == 1 && row.fortT.getHard() == 1 && row.star != 0) {
				curComFort = row.fortT.getId();
			}
			_rowMap.put(row.fortT.getId(), row);
		}
	}
	
	public FortCtrl(Player _ply) {
		ply = _ply;
		if (ply.getFortCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyFort(), ply.getPlyId(), ply.getFortCount()), ply.getSocket(), (byte) 3);
			if (ply.getLogoutT() < TimeManager.getPassedGmtMsecOfCurDay()) {
				refreshAllFort();
			}
		} else {
			curComFort = 1;
//			FortDBRow row = new FortDBRow();
//			row.id = DbCenter.nextPlyFortId();
//			row.fortT = Fort.getFortT(1);
//			row.star = 1;
//			curComFort = row.fortT.getId();
//			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyFort(),
//					row.id, ply.getPlyId(), row.fortT.getId(), row.star));
//			_rowMap.put(row.fortT.getId(), row);
//			
//			ply.setFortCount((short) _rowMap.size());
		}
	}
}
