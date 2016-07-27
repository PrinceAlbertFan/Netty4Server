package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import obj.GameController;
import obj.Player;
import obj.ctrl.ResCtrl.ResDBRow;
import server.SndMsg;
import tool.SendBuf;
import data.global.GloTmpVal;
import data.global.GlobalConst;
import data.xml.Level;
import data.xml.Level.PartnLevelT;
import data.xml.Partner;
import data.xml.Partner.PartnerT;
import data.xml.Resource.EquipT;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class PartnCtrl implements DbLoader, GameController {
	
	public final static class PartnDBRow extends DbObj {
		long id;
		PartnerT partnT; PartnLevelT lvT;
		short lv; int exp; byte star; byte step;
		
		final short[] arrSkl = new short[4];
		final byte[] arrSlot = new byte[6];
	}
	
	public final static short getPartnId(PartnDBRow row) { return row.partnT.getId(); }
	public final static short getPartnLv(PartnDBRow row) { return row.lv; }
	public final static int getPartnExp(PartnDBRow row) { return row.exp; }
	
	
	public static void addPartnExp(Player ply, PartnDBRow row, int exp, short lvLimit) {
		if (row.lvT != null && row.lv < lvLimit) {
			row.exp += exp; row.setNeedSave();
			if (row.exp >= row.lvT.getExp()) {
				exp = row.exp - row.lvT.getExp();
				row.exp = 0;
				row.lvT = Level.getPartnLevelT(++row.lv);
				if (row.lv % 10 == 0) {
					ply.getTaskCtrl().refreshTask((byte) 105, row.lv, 1);
				}
				addPartnExp(ply, row, exp, lvLimit);
			}
		}
	}
	
	/**伙伴表，主键是伙伴编号*/
	private final Map<Short, PartnDBRow> _rowMap = new HashMap<Short, PartnDBRow>();
	
	private Player ply;
	
	public final PartnDBRow getPartn(short id) {
		return _rowMap.get(id);
	}
	
	public short checkCountOfPartnLvForTask(short lv, short num) {
		short count = 0;
		for (PartnDBRow row : _rowMap.values()) {
			if (row.lv >= lv && ++count == num) {
				return count;
			}
		}
		return count;
	}
	
	public short checkCountOfPartnStepForTask(byte step, short num) {
		short count = 0;
		for (PartnDBRow row : _rowMap.values()) {
			if (row.step >= step && ++count == num) {
				return count;
			}
		}
		return count;
	}
	
	public short checkCountOfPartnStarForTask(byte star, short num) {
		short count = 0;
		for (PartnDBRow row : _rowMap.values()) {
			if (row.star >= star && ++count == num) {
				return count;
			}
		}
		return count;
	}
	
	/**装备强化*/
	public void enhanceEquipment(short id, byte pos) {
		PartnDBRow row = _rowMap.get(id);
		if (row != null && row.arrSlot[pos] != 0) {
			EquipT equipT = row.partnT.arrStepEqpt[row.step][pos].getEquipT();
			if (row.arrSlot[pos] - 1 != equipT.max_en_num && ply.getResCtrl().subtrCoin(equipT.cost_base + equipT.cost_add * (row.arrSlot[pos] - 1))) {
				row.setNeedSave();
				++row.arrSlot[pos];
				SndMsg.enEquipment.reset().writeBoolean(true).writeInt(0).writeInt(ply.getResCtrl().getCoin());
				_writeRowInfo(SndMsg.enEquipment, row);
				SndMsg.enEquipment.packAndSend(ply.getSocket());
			}
		}
	}
	
	/**一键穿装*/
	public void equipAll(short id) {
		PartnDBRow row = _rowMap.get(id);
		if (row != null) {
			SndMsg.equipAll.reset().writeBoolean(true).writeInt(0);
			GloTmpVal.tmpI = SndMsg.equipAll.getWriterIndex();
			SndMsg.equipAll.writeInt(GloTmpVal.tmpB = 0);
			ResDBRow resRow;
			row.setNeedSave();
			if (row.arrSlot[0] == 0
					 && row.lv >= row.partnT.arrStepEqpt[row.step][0].getEquipT().lvLimit
					 && (resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][0].getId(), 1)) != null) {
				row.arrSlot[0] = 1;
				++GloTmpVal.tmpB;
				SndMsg.equipAll.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
			}
			if (row.arrSlot[1] == 0
					 && row.lv >= row.partnT.arrStepEqpt[row.step][1].getEquipT().lvLimit
					 && (resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][1].getId(), 1)) != null) {
				row.arrSlot[1] = 1;
				++GloTmpVal.tmpB;
				SndMsg.equipAll.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
			}
			if (row.arrSlot[2] == 0
					 && row.lv >= row.partnT.arrStepEqpt[row.step][2].getEquipT().lvLimit
					 && (resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][2].getId(), 1)) != null) {
				row.arrSlot[2] = 1;
				++GloTmpVal.tmpB;
				SndMsg.equipAll.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
			}
			if (row.arrSlot[3] == 0
					&& row.lv >= row.partnT.arrStepEqpt[row.step][3].getEquipT().lvLimit
					&& (resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][3].getId(), 1)) != null) {
				row.arrSlot[3] = 1;
				++GloTmpVal.tmpB;
				SndMsg.equipAll.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
			}
			if (row.arrSlot[4] == 0
					 && row.lv >= row.partnT.arrStepEqpt[row.step][4].getEquipT().lvLimit
					 && (resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][4].getId(), 1)) != null) {
				row.arrSlot[4] = 1;
				++GloTmpVal.tmpB;
				SndMsg.equipAll.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
			}
			if (row.arrSlot[5] == 0
					 && row.lv >= row.partnT.arrStepEqpt[row.step][5].getEquipT().lvLimit
					 && (resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][5].getId(), 1)) != null) {
				row.arrSlot[5] = 1;
				++GloTmpVal.tmpB;
				SndMsg.equipAll.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
			}
			SndMsg.equipAll.setInt(GloTmpVal.tmpI, GloTmpVal.tmpB);
			_writeRowInfo(SndMsg.equipAll, row);
			SndMsg.equipAll.packAndSend(ply.getSocket());
		}
	}
	
	/**
	 * 穿装备
	 * */
	public void equip(short id, byte pos) {
		PartnDBRow row = _rowMap.get(id);
		if (row != null && row.arrSlot[pos] == 0 && row.lv >= row.partnT.arrStepEqpt[row.step][pos].getEquipT().lvLimit) {
			ResDBRow resRow = ply.getResCtrl().subtrInfactRes(row.partnT.arrStepEqpt[row.step][pos].getId(), 1);
			if (resRow != null) {
				row.arrSlot[pos] = 1;
				row.setNeedSave();
				SndMsg.equip.reset().writeBoolean(true).writeInt(0)
				.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + 1);
				_writeRowInfo(SndMsg.equip, row);
				SndMsg.equip.packAndSend(ply.getSocket());
			}
		}
	}
	
	/**技能升级*/
	public void upSkill(short id, byte pos) {
		if (ply.getResCtrl().getPartnSp() != 0) {
			PartnDBRow row = _rowMap.get(id);
			if (row != null && row.arrSkl[pos] != 0 && row.arrSkl[pos] < row.lv - 1 + GlobalConst.arr_skill_lv[pos]
					&& ply.getResCtrl().subtrCoin(Level.getPartnLevelT(row.arrSkl[pos]).arrSklUpCost[pos])) {
				ply.getResCtrl().subtrOnePartnSp();
				row.setNeedSave();
				++row.arrSkl[pos];
				SndMsg.upSkill.reset().writeBoolean(true).writeInt(0).writeInt(ply.getResCtrl().getCoin());
				_writeRowInfo(SndMsg.upSkill, row);
				SndMsg.upSkill.writeInt(ply.getResCtrl().getPartnSp()).packAndSend(ply.getSocket());
			}
		}
	}
	
	/**升阶
	 * */
	public void upStep(short id) {
		PartnDBRow row = _rowMap.get(id);
		if (row != null && row.arrSlot[0] != 0 && row.arrSlot[1] != 0 && row.arrSlot[2] != 0
				&& row.arrSlot[3] != 0 && row.arrSlot[4] != 0 && row.arrSlot[5] != 0) {
			EquipT equipT;
			GloTmpVal._tmpI = 0;
			if (row.arrSlot[0] != 1) {
				equipT = row.partnT.arrStepEqpt[row.step][0].getEquipT();
				for (int i = 0, max = row.arrSlot[0] - 1; i != max; ++i) {
					GloTmpVal._tmpI += equipT.cost_base + equipT.cost_add * i;
				}
			}
			if (row.arrSlot[1] != 1) {
				equipT = row.partnT.arrStepEqpt[row.step][1].getEquipT();
				for (int i = 0, max = row.arrSlot[1] - 1; i != max; ++i) {
					GloTmpVal._tmpI += equipT.cost_base + equipT.cost_add * i;
				}
			}
			if (row.arrSlot[2] != 1) {
				equipT = row.partnT.arrStepEqpt[row.step][2].getEquipT();
				for (int i = 0, max = row.arrSlot[2] - 1; i != max; ++i) {
					GloTmpVal._tmpI += equipT.cost_base + equipT.cost_add * i;
				}
			}
			if (row.arrSlot[3] != 1) {
				equipT = row.partnT.arrStepEqpt[row.step][3].getEquipT();
				for (int i = 0, max = row.arrSlot[3] - 1; i != max; ++i) {
					GloTmpVal._tmpI += equipT.cost_base + equipT.cost_add * i;
				}
			}
			if (row.arrSlot[4] != 1) {
				equipT = row.partnT.arrStepEqpt[row.step][4].getEquipT();
				for (int i = 0, max = row.arrSlot[4] - 1; i != max; ++i) {
					GloTmpVal._tmpI += equipT.cost_base + equipT.cost_add * i;
				}
			}
			if (row.arrSlot[5] != 1) {
				equipT = row.partnT.arrStepEqpt[row.step][5].getEquipT();
				for (int i = 0, max = row.arrSlot[5] - 1; i != max; ++i) {
					GloTmpVal._tmpI += equipT.cost_base + equipT.cost_add * i;
				}
			}
			if (GloTmpVal._tmpI != 0) {
				ply.getResCtrl().addCoin(GloTmpVal._tmpI);
			}
			row.setNeedSave();
			row.arrSlot[5] = row.arrSlot[4] = row.arrSlot[3] = row.arrSlot[2] = row.arrSlot[1] = row.arrSlot[0] = 0;
			switch (++row.step) {
			case 1: row.arrSkl[1] = 1; break;
			case 3: row.arrSkl[2] = 1; break;
			case 6: row.arrSkl[3] = 1; break;
			default: break;
			}
			ply.getTaskCtrl().refreshTask((byte) 106, row.step + 1, 1);
			SndMsg.upStep.reset().writeInt(GloTmpVal._tmpI); // 返回钱币
			_writeRowInfo(SndMsg.upStep, row);
			SndMsg.upStep.packAndSend(ply.getSocket());
		}
	}
	
	/**升星
	 * */
	public void upStar(short id) {
		PartnDBRow row = _rowMap.get(id);
		if (row != null && row.star != 4
				&& ply.getResCtrl().subtrCoin(row.partnT.arrStarCost[row.star][1])) {
			ResDBRow resRow = ply.getResCtrl().subtrInfactRes(row.partnT.getSynRes(), row.partnT.arrStarCost[row.star][0]);
			if (resRow != null) {
				row.setNeedSave();
				++row.star;
				ply.getTaskCtrl().refreshTask((byte) 107, row.star + 1, 1);
				SndMsg.upStar.reset().writeBoolean(true).writeInt(0)
				.writeInt(ply.getResCtrl().getCoin())
				.writeInt(resRow.resT.getId()).writeInt(resRow.cid).writeInt(resRow.qty)
				.writeInt(resRow.qty + row.partnT.arrStarCost[row.star][0]);
				_writeRowInfo(SndMsg.upStar, row);
				SndMsg.upStar.packAndSend(ply.getSocket());
			}
		}
	}
	
	/**添加英雄，如果有该英雄，则自动转化成碎片*/
	public void addPartn(short id) {
		PartnDBRow row = _rowMap.get(id);
		if (row != null) {
			ply.getResCtrl().addRes(row.partnT.getSynRes(), row.partnT.getPiece());
		} else {
			row = new PartnDBRow();
			row.id = DbCenter.nextPlyPartnId();
			row.partnT = Partner.getPartnerT(id);
			row.lvT = Level.getPartnLevelT(row.lv = 1);
			row.star = row.partnT.getStar();
			row.arrSkl[0] = 1;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyPartn(), row.id, ply.getPlyId(), id, row.star));
			_rowMap.put(id, row);
			ply.setPartnCount((short) _rowMap.size());
		}
	}
	
	/**合成英雄*/
	public void sysnPartn(short id) {
		if (!_rowMap.containsKey(id)) { 
			PartnerT PartnT = Partner.getPartnerT(id);
			ResDBRow resRow = ply.getResCtrl().subtrInfactRes(PartnT.getSynRes(), PartnT.getSynResNum());
			if (resRow != null) {
				PartnDBRow row = new PartnDBRow();
				row.id = DbCenter.nextPlyPartnId();
				row.partnT = PartnT;
				row.lvT = Level.getPartnLevelT(row.lv = 1);
				row.star = row.partnT.getStar();
				row.arrSkl[0] = 1;
				DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyPartn(), row.id, ply.getPlyId(), id, row.star));
				_rowMap.put(id, row);
				ply.setPartnCount((short) _rowMap.size());
				
				SndMsg.sysnPartn.reset().writeBoolean(true).writeInt(0)
				.writeInt(PartnT.getSynRes()).writeInt(resRow.cid).writeInt(resRow.qty).writeInt(resRow.qty + PartnT.getSynResNum());
				this._writeRowInfo(SndMsg.sysnPartn, row);
				SndMsg.sysnPartn.packAndSend(ply.getSocket());
			}
		}
	}
	
	private void _writeRowInfo(SendBuf buf, PartnDBRow row) {
		buf.writeInt(row.partnT.getId())
		.writeInt(row.lv)
		.writeInt(row.step + 1)
		.writeInt(row.star + 1)
		//.writeInt(row.pos - 1)
		.writeInt(row.exp)
		.writeInt(4) // List<int> skill_level size
		.writeInt(row.arrSkl[0])
		.writeInt(row.arrSkl[1])
		.writeInt(row.arrSkl[2])
		.writeInt(row.arrSkl[3]);
		GloTmpVal.tmpI = buf.getWriterIndex();
		buf.writeInt(GloTmpVal.tmpB = 0);
		if (row.arrSlot[0] != 0) {
			++GloTmpVal.tmpB;
			buf.writeInt(row.partnT.arrStepEqpt[row.step][0].getId()).writeInt(0).writeInt(row.arrSlot[0] - 1);
		}
		if (row.arrSlot[1] != 0) {
			++GloTmpVal.tmpB;
			buf.writeInt(row.partnT.arrStepEqpt[row.step][1].getId()).writeInt(1).writeInt(row.arrSlot[1] - 1);
		}
		if (row.arrSlot[2] != 0) {
			++GloTmpVal.tmpB;
			buf.writeInt(row.partnT.arrStepEqpt[row.step][2].getId()).writeInt(2).writeInt(row.arrSlot[2] - 1);
		}
		if (row.arrSlot[3] != 0) {
			++GloTmpVal.tmpB;
			buf.writeInt(row.partnT.arrStepEqpt[row.step][3].getId()).writeInt(3).writeInt(row.arrSlot[3] - 1);
		}
		if (row.arrSlot[4] != 0) {
			++GloTmpVal.tmpB;
			buf.writeInt(row.partnT.arrStepEqpt[row.step][4].getId()).writeInt(4).writeInt(row.arrSlot[4] - 1);
		}
		if (row.arrSlot[5] != 0) {
			++GloTmpVal.tmpB;
			buf.writeInt(row.partnT.arrStepEqpt[row.step][5].getId()).writeInt(5).writeInt(row.arrSlot[5] - 1);
		}
		buf.setInt(GloTmpVal.tmpI, GloTmpVal.tmpB);
	}

	@Override
	public void onceNoticeClient() {
		SndMsg.initData.writeInt(ply.getPartnCount());
		for (PartnDBRow row : _rowMap.values()) {
			_writeRowInfo(SndMsg.initData, row);
		}
	}
	
	@Override
	public void saveToDB() {
		for (PartnDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyPartn(),
						row.lv, row.exp, row.star, row.step, row.arrSkl[0], row.arrSkl[1], row.arrSkl[2], row.arrSkl[3],
						row.arrSlot[0], row.arrSlot[1], row.arrSlot[2], row.arrSlot[3], row.arrSlot[4], row.arrSlot[5], row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		PartnDBRow row;
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			row = new PartnDBRow();
			row.id = buf.readLong();
			row.partnT = Partner.getPartnerT(buf.readShort());
			row.lvT = Level.getPartnLevelT(row.lv = buf.readShort());
			row.exp = buf.readInt();
			row.star = buf.readByte();
			row.step = buf.readByte();
			row.arrSkl[0] = buf.readShort();
			row.arrSkl[1] = buf.readShort();
			row.arrSkl[2] = buf.readShort();
			row.arrSkl[3] = buf.readShort();
			row.arrSlot[0] = buf.readByte();
			row.arrSlot[1] = buf.readByte();
			row.arrSlot[2] = buf.readByte();
			row.arrSlot[3] = buf.readByte();
			row.arrSlot[4] = buf.readByte();
			row.arrSlot[5] = buf.readByte();
			_rowMap.put(row.partnT.getId(), row);
		}
//		this._setPartnPos(_rowMap.get((short) 101), (byte) 0, (byte) 1);
//		this._setPartnPos(_rowMap.get((short) 202), (byte) 0, (byte) 2);
//		this._setPartnPos(_rowMap.get((short) 301), (byte) 0, (byte) 3);
//		this._setPartnPos(_rowMap.get((short) 307), (byte) 0, (byte) 4);
	}
	
	public PartnCtrl(Player _ply) {
		ply = _ply;
		if (ply.getPartnCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyPartn(), ply.getPlyId(), ply.getPartnCount()), ply.getSocket(), (byte) 1);
		} else {
			PartnDBRow row = new PartnDBRow();
			row.id = DbCenter.nextPlyPartnId();
			row.partnT = Partner.getPartnerT((short) 101);
			row.lvT = Level.getPartnLevelT(row.lv = 1);
			row.star = row.partnT.getStar(); row.arrSkl[0] = 1;
			//row.pos = 1;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyPartn(), row.id, ply.getPlyId(), row.partnT.getId(), row.star));
			_rowMap.put(row.partnT.getId(), row);
			
//			row = new PartnDBRow();
//			row.id = DbCenter.nextPlyPartnId();
//			row.partnT = Partner.getPartnerT((short) 202);
//			row.lvT = Level.getPartnLevelT(row.lv = 1);
//			row.star = row.partnT.getStar(); row.arrSkl[0] = 1;
//			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyPartn(), row.id, ply.getPlyId(), row.partnT.getId(), row.star));
//			_rowMap.put(row.partnT.getId(), row);
//			
//			row = new PartnDBRow();
//			row.id = DbCenter.nextPlyPartnId();
//			row.partnT = Partner.getPartnerT((short) 301);
//			row.lvT = Level.getPartnLevelT(row.lv = 1);
//			row.star = row.partnT.getStar(); row.arrSkl[0] = 1;
//			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyPartn(), row.id, ply.getPlyId(), row.partnT.getId(), row.star));
//			_rowMap.put(row.partnT.getId(), row);
//			
//			row = new PartnDBRow();
//			row.id = DbCenter.nextPlyPartnId();
//			row.partnT = Partner.getPartnerT((short) 307);
//			row.lvT = Level.getPartnLevelT(row.lv = 1);
//			row.star = row.partnT.getStar(); row.arrSkl[0] = 1;
//			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyPartn(), row.id, ply.getPlyId(), row.partnT.getId(), row.star));
//			_rowMap.put(row.partnT.getId(), row);
			
			ply.setPartnCount((short) _rowMap.size());
//			ply.setPartnCount((short) 1);
		}
	}
}