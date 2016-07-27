package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import obj.GameController;
import obj.Player;
import obj.ctrl.ResCtrl.ResDBRow;
import server.SndMsg;
import tool.SendBuf;
import data.xml.God;
import data.xml.God.GodT;
import data.xml.Level;
import data.xml.Level.GodLevelT;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class GodCtrl implements DbLoader, GameController {
	
	public final static class GodDBRow extends DbObj {
		long id; GodT godT; GodLevelT lvT;
		short lv; byte star; short mood;
	}
	
	/**邪神表，主键是邪神编号*/
	private final Map<Short, GodDBRow> _rowMap = new HashMap<Short, GodDBRow>();
	
	private Player ply;
	
	public final GodDBRow getGod(short id) { return _rowMap.get(id); }
	
	public short checkCountOfGodStarForTask(byte star, short num) {
		short count = 0;
		for (GodDBRow row : _rowMap.values()) {
			if (row.star >= star && ++count == num) {
				return count;
			}
		}
		return count;
	}
	
	public short checkCountOfGodLvForTask(short lv, short num) {
		short count = 0;
		for (GodDBRow row : _rowMap.values()) {
			if (row.lv >= lv && ++count == num) {
				return count;
			}
		}
		return count;
	}
	
	/**邪神升级*/
	public void upgrade(short id) {
		GodDBRow row = _rowMap.get(id);
		if (row != null && row.lv != ply.getLv()
				&& ply.getResCtrl().subtrSoul(row.lvT.getSoul())
				&& ply.getResCtrl().subtrCoin(row.lvT.getCoin())) {
			row.setNeedSave();
			row.lvT = Level.getGodLevelT(++row.lv);
			if (row.lv % 10 == 0) {
				ply.getTaskCtrl().refreshTask((byte) 110, row.lv, 1);
			}
			SndMsg.godUpgrade.reset().writeBoolean(true).writeInt(0)
			.writeInt(ply.getResCtrl().getCoin());
			_writeRowInfo(SndMsg.godUpgrade, row);
			SndMsg.godUpgrade.writeInt(ply.getResCtrl().getSoul())
			.packAndSend(ply.getSocket());
		}
	}
	
	/**邪神升星*/
	public void upStar(short id) {
		GodDBRow row = _rowMap.get(id);
		if (row != null && row.star != 3) {
			ResDBRow resRow = ply.getResCtrl().subtrInfactRes(row.godT.getSynRes(), row.godT.arrStarCost[row.star]);
			if (resRow != null) {
				row.setNeedSave();
				++row.star;
				ply.getTaskCtrl().refreshTask((byte) 109, row.star, 1);
				SndMsg.godUpStar.reset().writeBoolean(true).writeInt(0)
				.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + row.godT.arrStarCost[row.star - 1]);
				_writeRowInfo(SndMsg.godUpStar, row);
				SndMsg.godUpStar.packAndSend(ply.getSocket());
			}
		}
	}
	
	/**添加邪神*/
	public void addGod(short id) {
		GodDBRow row = _rowMap.get(id);
		if (row == null) {
			GodT godT = God.getGodT(id);
			if (godT != null && ply.getResCtrl().subtrInfactRes(godT.getSynRes(), godT.arrStarCost[0]) != null) {
				row = new GodDBRow();
				row.id = DbCenter.nextPlyGodId();
				row.godT = godT;
				row.lvT = Level.getGodLevelT(row.lv = 1);
				row.star = row.godT.getStar();
				row.mood = 300;
				DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyGod(), row.id, ply.getPlyId(), id, row.star));
				_rowMap.put(id, row);
				ply.setGodCount((short) _rowMap.size());
				SndMsg.addGod.reset().writeBoolean(true).packAndSend(ply.getSocket());
			}
		}
	}
	
	private void _writeRowInfo(SendBuf buf, GodDBRow row) {
		buf.writeInt(row.godT.getId())
		.writeInt(row.star)
		.writeInt(row.mood - 300)
		.writeInt(row.lv);
	}
	
	@Override
	public void onceNoticeClient() {
		//System.out.println("邪神数量:" + ply.getGodCount());
		SndMsg.initData.writeInt(ply.getGodCount());
		for (GodDBRow row : _rowMap.values()) {
			_writeRowInfo(SndMsg.initData, row);
		}
	}

	@Override
	public void saveToDB() {
		for (GodDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyGod(),
						row.lv, row.star, row.mood, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		GodDBRow row;
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			row = new GodDBRow();
			row.id = buf.readLong();
			row.godT = God.getGodT(buf.readShort());
			row.lvT = Level.getGodLevelT(row.lv = buf.readShort());
			row.star = buf.readByte();
			row.mood = buf.readShort();
			_rowMap.put(row.godT.getId(), row);
		}
	}
	
	public GodCtrl(Player _ply) {
		ply = _ply;
		if (ply.getGodCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyGod(), ply.getPlyId(), ply.getGodCount()), ply.getSocket(), (byte) 2);
		}/* else {
			DBRow row = new DBRow();
			row.id = DbCenter.nextPlyGodId();
			row.godT = God.getGodT((short) 1001);
			row.lv = 1;
			row.star = row.godT.getStar();
			row.mood = 300;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyGod(), row.id, ply.getPlyId(), row.godT.getId(), row.star));
			_rowMap.put(row.godT.getId(), row);
			
			ply.setGodCount((short) _rowMap.size());
		}*/
	}
}
