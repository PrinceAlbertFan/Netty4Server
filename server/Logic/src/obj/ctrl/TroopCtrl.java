package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import obj.GameController;
import obj.Player;
import obj.ctrl.GodCtrl.GodDBRow;
import obj.ctrl.PartnCtrl.PartnDBRow;
import data.global.GloTmpVal;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class TroopCtrl implements DbLoader, GameController {
	
	public final static class TroopDBRow extends DbObj {
		long id;
		GodDBRow god;
		public final PartnDBRow[] arrPartn = new PartnDBRow[4];
		public byte partnNum;
		byte troop;
	}
	
	public final static short getTroopGodId(TroopDBRow row) {
		return row.god != null ? row.god.godT.getId() : 0;
	}
	
	/**布阵表，主键是troopId*/
	private final Map<Byte, TroopDBRow> _rowMap = new HashMap<Byte, TroopDBRow>();
	
	private Player ply;
	
	public final TroopDBRow getTroop(byte troop) {
		return _rowMap.get(troop);
	}
	
	/**布阵*/
	public void setTroop(byte troop, short god, byte size, short... ids) {
		TroopDBRow row = _rowMap.get(troop);
		if (row != null) {
			if (row.god != null) {
				if (row.god.godT.getId() != god) {
					row.setNeedSave();
					row.god = god != 0 ? ply.getGodCtrl().getGod(god) : null;
				}
			} else if (god != 0) {
				row.setNeedSave();
				row.god = ply.getGodCtrl().getGod(god);
			}
			if (row.partnNum == size) {
				while (size-- != 0) {
					if (row.arrPartn[size].partnT.getId() != ids[size]) {
						row.setNeedSave();
						row.arrPartn[size] = ply.getPartnCtrl().getPartn(ids[size]);
					}
				}
			} else {
				row.setNeedSave();
				if (size > row.partnNum) {
					for (GloTmpVal.tmpB = 0; GloTmpVal.tmpB != row.partnNum; ++GloTmpVal.tmpB) {
						if (row.arrPartn[GloTmpVal.tmpB].partnT.getId() != ids[GloTmpVal.tmpB]) {
							row.arrPartn[GloTmpVal.tmpB] = ply.getPartnCtrl().getPartn(ids[GloTmpVal.tmpB]);
						}
					}
					do {
						row.arrPartn[GloTmpVal.tmpB] = ply.getPartnCtrl().getPartn(ids[GloTmpVal.tmpB]);
					} while (++GloTmpVal.tmpB != size);
				} else {
					for (GloTmpVal.tmpB = 0; GloTmpVal.tmpB != size; ++GloTmpVal.tmpB) {
						if (row.arrPartn[GloTmpVal.tmpB].partnT.getId() != ids[GloTmpVal.tmpB]) {
							row.arrPartn[GloTmpVal.tmpB] = ply.getPartnCtrl().getPartn(ids[GloTmpVal.tmpB]);
						}
					}
					do {
						row.arrPartn[GloTmpVal.tmpB] = null;
					} while (++GloTmpVal.tmpB != row.partnNum);
				}
				row.partnNum = size;
			}
		} else {
			row = new TroopDBRow();
			row.id = DbCenter.nextPlyTroopId();
			row.troop = troop;
			if (god != 0) {
				row.god = ply.getGodCtrl().getGod(god);
			}
			row.partnNum = size;
			while (size-- != 0) {
				row.arrPartn[size] = ply.getPartnCtrl().getPartn(ids[size]);
			}
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyTroop(), row.id, ply.getPlyId(), troop, god,
					row.arrPartn[0] != null ? row.arrPartn[0].partnT.getId() : 0,
					row.arrPartn[1] != null ? row.arrPartn[1].partnT.getId() : 0,
					row.arrPartn[2] != null ? row.arrPartn[2].partnT.getId() : 0,
					row.arrPartn[3] != null ? row.arrPartn[3].partnT.getId() : 0));
			_rowMap.put(troop, row);
			ply.setTroopCount((short) _rowMap.size());
		}
	}
	
	@Override
	public void onceNoticeClient() {
		
	}

	@Override
	public void saveToDB() {
		for (TroopDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyTroop(),
						row.god != null ? row.god.godT.getId() : 0,
						row.arrPartn[0] != null ? row.arrPartn[0].partnT.getId() : 0,
						row.arrPartn[1] != null ? row.arrPartn[1].partnT.getId() : 0,
						row.arrPartn[2] != null ? row.arrPartn[2].partnT.getId() : 0,
						row.arrPartn[3] != null ? row.arrPartn[3].partnT.getId() : 0, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		TroopDBRow row;
		for (short id, s = 0, ms = buf.readShort(); s != ms; ++s) {
			row = new TroopDBRow();
			row.id = buf.readLong();
			row.troop = buf.readByte();
			if ((id = buf.readShort()) != 0) {
				row.god = ply.getGodCtrl().getGod(id);
			}
			if ((id = buf.readShort()) != 0) {
				row.arrPartn[0] = ply.getPartnCtrl().getPartn(id);
				++row.partnNum;
				if ((id = buf.readShort()) != 0) {
					row.arrPartn[1] = ply.getPartnCtrl().getPartn(id);
					++row.partnNum;
					if ((id = buf.readShort()) != 0) {
						row.arrPartn[2] = ply.getPartnCtrl().getPartn(id);
						++row.partnNum;
						if ((id = buf.readShort()) != 0) {
							row.arrPartn[3] = ply.getPartnCtrl().getPartn(id);
							++row.partnNum;
						}
					} else { buf.skipBytes(2); }
				} else { buf.skipBytes(4); }
			} else { buf.skipBytes(6); }
			_rowMap.put(row.troop, row);
		}
	}
	
	public TroopCtrl(Player _ply) {
		ply = _ply;
		if (ply.getTroopCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyTroop(), ply.getPlyId(), ply.getTroopCount()), ply.getSocket(), (byte) 6);
		} else {
			TroopDBRow row = new TroopDBRow();
			row.id = DbCenter.nextPlyTroopId();
			row.troop = 1;
			row.partnNum = 0;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyTroop(), row.id, ply.getPlyId(), row.troop, 0, 0, 0, 0, 0));
			_rowMap.put(row.troop, row);
			ply.setTroopCount((short) _rowMap.size());
		}
	}
}
