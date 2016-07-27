package obj.ctrl;

import java.util.HashMap;
import java.util.Map;

import manager.TimeManager;
import io.netty.buffer.ByteBuf;
import obj.GameController;
import obj.Player;
import data.global.GloTmpVal;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class TimesCtrl implements DbLoader, GameController {
	/**金币抽总次数*/
	public static final short TYPE_0 = 101;
	/**钻石抽总次数*/
	public static final short TYPE_1 = 102;
	/**金币10抽总次数*/
	public static final short TYPE_2 = 103;
	/**钻石10抽总次数*/
	public static final short TYPE_3 = 104;
	
	/**金币抽奖当日免费次数*/
	public static final short TYPE_4 = 107;
	/**钻石抽奖当日免费次数*/
	public static final short TYPE_5 = 108;
	
	private final static class TimesDBRow extends DbObj {
		long id; int times;
	}
	
	private static TimesDBRow _tmpRow;
	
	private Map<Short, TimesDBRow> _rowMap = new HashMap<Short, TimesDBRow>();
	private Player ply;
	
	private TimesDBRow _getTimesDBRow(short type) {
		if ((_tmpRow = _rowMap.get(type)) == null) {
			_tmpRow = new TimesDBRow();
			_tmpRow.id = DbCenter.nextPlyTimesId();
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyTimes(), _tmpRow.id, ply.getPlyId(), type, 0));
			_rowMap.put(type, _tmpRow);
			ply.setTimesCount((short) _rowMap.size());
		}
		return _tmpRow;
	}
	
	public final int getTimes(short type) {
		return _getTimesDBRow(type).times;
	}
	
	public final int addOneTimes(short type) {
		_getTimesDBRow(type).setNeedSave();
		return ++_tmpRow.times;
	}
	
	private final void _resetTimes(short type) {
		_getTimesDBRow(type).setNeedSave();
		_tmpRow.times = 0;
	}
	
	public void refreshAllTimes() {
		_resetTimes(TYPE_4);
		_resetTimes(TYPE_5);
	}
	
	@Override
	public void onceNoticeClient() {
		
	}

	@Override
	public void saveToDB() {
		for (TimesDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyTimes(), row.times, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			_tmpRow = new TimesDBRow();
			_tmpRow.id = buf.readLong();
			GloTmpVal._tmpS = buf.readShort();
			_tmpRow.times = buf.readInt();
			_rowMap.put(GloTmpVal._tmpS, _tmpRow);
		}
	}
	
	public TimesCtrl(Player _ply) {
		ply = _ply;
		if (ply.getTimesCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyTimes(), ply.getPlyId(), ply.getTimesCount()), ply.getSocket(), (byte) 8);
			if (ply.getLogoutT() < TimeManager.getPassedGmtMsecOfCurDay()) {
				refreshAllTimes();
			}
		}
	}
}
