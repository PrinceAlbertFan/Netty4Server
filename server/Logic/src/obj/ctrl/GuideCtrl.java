package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

import obj.GameController;
import obj.Player;
import server.SndMsg;
import db.DbCenter;
import db.DbLoader;

public final class GuideCtrl implements DbLoader, GameController {
	
//	private final static class DBRow extends DbObj {
//		long id; short guide;
//	}
	
	/**引导列表*/
	//private final Map<Short, DBRow> _rowMap = new HashMap<Short, DBRow>();
	private final Set<Short> _setGuide = new HashSet<Short>();
	
	private Player ply;
	
	public void addGuide(short id) {
		if (_setGuide.add(id)) {
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyGuide(), DbCenter.nextPlyGuideId(), ply.getPlyId(), id));
			ply.setGuideCount((short) _setGuide.size());
		}
	}
	
	@Override
	public void onceNoticeClient() {
		SndMsg.initData.writeInt(ply.getGuideCount());
//		for (DBRow row : _rowMap.values()) {
//			SndMsg.initData.writeInt(row.guide);
//		}
		for (Short s : _setGuide) {
			SndMsg.initData.writeInt(s);
		}
	}

	@Override
	public void saveToDB() {
		// do nothing
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			_setGuide.add(buf.readShort());
		}
		ply.refreshDynamicData();
		ply.loadOver();
	}
	
	public GuideCtrl(Player _ply) {
		ply = _ply;
		if (ply.getGuideCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyGuide(), ply.getPlyId(), ply.getGuideCount()), ply.getSocket(), (byte) 10);
		} else {
			ply.loadOver();
		}
	}
}
