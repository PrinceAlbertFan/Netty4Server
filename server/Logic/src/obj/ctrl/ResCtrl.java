package obj.ctrl;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import data.global.GlobalConst;
import data.xml.Resource;
import data.xml.Resource.ResourceT;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;
import obj.GameController;
import obj.Player;
import server.SndMsg;

public final class ResCtrl implements DbLoader, GameController {
	
	public final static class ResDBRow extends DbObj {
		public ResourceT resT;
		long id;
		int qty;
		short cid;
	}
	
	//public final static ResourceT getResT(ResDBRow row) { return row.resT; }
	public final static int getResQty(ResDBRow row) { return row.qty; }
	public final static short getResCid(ResDBRow row) { return row.cid; }
	
	/**资源表，主键是资源编号*/
	private final Map<Integer, ResDBRow> _rowMap = new HashMap<Integer, ResDBRow>();
	
	private Player ply;
	private int coin, credit, power, exp, soul;
	private short partnSp;
	
	private short infactResCount;
	
	private boolean bCoin, bCredit, bPower, bExp, bSoul, bPartnSp;
	
	public final int getCoin() { return coin; }
	public final int addCoin(int _coin) { bCoin = true; return coin += _coin; }
	public boolean subtrCoin(int _coin) {
		//System.out.println("扣除：" + _coin);
		if ((_coin = coin - _coin) >= 0) {
			coin = _coin;
			return bCoin = true;
		}
		return false;
	}
	
	public final int getCredit() { return credit; }
	public final int addCredit(int _credit) { bCredit = true; return credit += _credit; }
	public boolean subtrCredit(int _credit) {
		if ((_credit = credit - _credit) >= 0) {
			credit = _credit;
			return bCredit = true;
		}
		return false;
	}
	
	public final int getPower() { return power; }
	public final int addPower(int _power) { bPower = true; return power += _power; }
	public final void addOnePower() { ++power; bPower = true; }
	public boolean subtrPower(int _power) {
		if ((_power = power - _power) >= 0) {
			power = _power;
			return bPower = true;
		}
		return false;
	}
	
	public final int getExp() { return exp; }
	public void addExp(int _exp) {
		if (ply.getLevelT() != null) {
			exp += _exp; bExp = true;
			if (exp >= ply.getLevelT().getExp()) {
				_exp = exp - ply.getLevelT().getExp();
				exp = 0;
				ply.upgrade();
				addExp(_exp);
			}
		}
	}
	
	public final int getSoul() { return soul; }
	public final int addSoul(int _soul) { bSoul = true; return soul += _soul; }
	public boolean subtrSoul(int _soul) {
		if ((_soul = soul - _soul) >= 0) {
			soul = _soul;
			return bSoul = true;
		}
		return false;
	}
	
	public final short getPartnSp() { return partnSp; }
	public final int addPartnSp(int _sp) { bPartnSp = true; return partnSp += _sp; }
	public final void addOnePartnSp() { ++partnSp; bPartnSp = true; }
	public final void subtrOnePartnSp() { --partnSp; bPartnSp = true; }
	
	/**获得资源数量*/
	public int getResQty(int res) {
		switch (res) {
		case Resource.ID_99901:	return coin;
		case Resource.ID_99902:	return credit;
		case Resource.ID_99903:	return power;
		case Resource.ID_99904: return exp;
		case Resource.ID_99906: return soul;
		case Resource.ID_99907: return partnSp;
		default:	break;
		}
		ResDBRow row = _rowMap.get(res);
		return row != null ? row.qty : 0;
	}
	
	/**添加资源，返回剩余数量*/
	public int addRes(int res, int qty) {
		switch (res) {
		case Resource.ID_99901:	return addCoin(qty);
		case Resource.ID_99902:	return addCredit(qty);
		case Resource.ID_99903:	return addPower(qty);
		case Resource.ID_99904: addExp(qty); return exp;
		case Resource.ID_99906: return addSoul(qty);
		case Resource.ID_99907: return addPartnSp(qty);
		default:	break;
		}
		return addInfactRes(res, qty).qty;
	}
	
	/**添加非属性资源，返回ResDBRow*/
	public ResDBRow addInfactRes(int res, int qty) {
		ResDBRow row = _rowMap.get(res);
		if (row != null) {
			row.setNeedSave();
			row.qty += qty;
		} else {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(res);
			row.qty = qty;
			if (row.resT.getType() < 100) {
				row.cid = infactResCount++;
			}
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), res, qty));
			_rowMap.put(res, row);
			ply.setResCount((short) _rowMap.size());
		}
		return row;
	}
	
	/**扣除资源(通用)，返回剩余数量，-1为扣除失败*/
	public int subtrRes(int res, int qty) {
		switch (res) {
		case Resource.ID_99901:	return subtrCoin(qty) ? coin : -1;
		case Resource.ID_99902:	return subtrCredit(qty) ? credit : -1;
		case Resource.ID_99903:	return subtrPower(qty) ? power : -1;
		case Resource.ID_99906: return subtrSoul(qty) ? soul : -1;
		default:	break;
		}
		ResDBRow row = _rowMap.get(res);
		if (row != null) {
			qty = row.qty - qty;
			if (qty >= 0) {
				row.setNeedSave();
				return row.qty = qty;
			}
		} else {
			row = new ResDBRow();
			row.resT = Resource.getResourceT(res);
			if (row.resT.getType() < 100) {
				row.cid = infactResCount++;
			}
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(),
					row.id = DbCenter.nextPlyResId(), ply.getPlyId(), res, 0));
			_rowMap.put(res, row);
			ply.setResCount((short) _rowMap.size());
		}
		return -1;
	}
	
	/**扣除非属性资源，返回ResDBRow，扣除失败返回null*/
	public ResDBRow subtrInfactRes(int res, int qty) {
		ResDBRow row = _rowMap.get(res);
		if (row != null) {
			qty = row.qty - qty;
			if (qty >= 0) {
				row.setNeedSave();
				row.qty = qty;
				return row;
			}
		}
		return null;
	}
	
	@Override
	public void onceNoticeClient() {
		SndMsg.initData.writeInt(infactResCount);
		for (ResDBRow row : _rowMap.values()) {
			if (row.resT.getType() < 100) {
				SndMsg.initData.writeInt(row.resT.getId())
				.writeInt(row.cid)
				.writeInt(row.qty)
				.writeInt(row.qty);
			}
		}
	}

	@Override
	public void saveToDB() {
		ResDBRow _row;
		if (bCoin) {
			bCoin = false;
			_row = _rowMap.get(Resource.ID_99901);
			_row.setNeedSave();
			_row.qty = coin;
		}
		if (bCredit) {
			bCredit = false;
			_row = _rowMap.get(Resource.ID_99902);
			_row.setNeedSave();
			_row.qty = credit;
		}
		if (bPower) {
			bPower = false;
			_row = _rowMap.get(Resource.ID_99903);
			_row.setNeedSave();
			_row.qty = power;
		}
		if (bExp) {
			bExp = false;
			_row = _rowMap.get(Resource.ID_99904);
			_row.setNeedSave();
			_row.qty = exp;
		}
		if (bSoul) {
			bSoul = false;
			_row = _rowMap.get(Resource.ID_99906);
			_row.setNeedSave();
			_row.qty = soul;
		}
		if (bPartnSp) {
			bPartnSp = false;
			_row = _rowMap.get(Resource.ID_99907);
			_row.setNeedSave();
			_row.qty = partnSp;
		}
		for (ResDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyRes(), row.qty, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		ResDBRow row;
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			row = new ResDBRow();
			row.id = buf.readLong();
			row.resT = Resource.getResourceT(buf.readInt());
			row.qty = buf.readInt();
			if (row.resT.getType() < 100) {
				row.cid = infactResCount++;
			}
			_rowMap.put(row.resT.getId(), row);
		}
		row = _rowMap.get(Resource.ID_99901);
		if (row != null) {
			coin = row.qty;
		} else {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99901);
			coin = row.qty = GlobalConst.init_coin;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
		}
		row = _rowMap.get(Resource.ID_99902);
		if (row != null) {
			credit = row.qty;
		} else {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99902);
			credit = row.qty = GlobalConst.init_credit;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
		}
		row = _rowMap.get(Resource.ID_99903);
		if (row != null) {
			power = row.qty;
		} else {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99903);
			power = row.qty = GlobalConst.init_power;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
		}
		row = _rowMap.get(Resource.ID_99904);
		if (row != null) {
			exp = row.qty;
		} else {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99904);
			//exp = row.qty = 0;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
		}
		row = _rowMap.get(Resource.ID_99906);
		if (row != null) {
			soul = row.qty;
		} else {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99906);
			//soul = row.qty = 0;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
		}
		row = _rowMap.get(Resource.ID_99907);
		if (row == null) {
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99907);
			row.qty = GlobalConst.hero_skill_max;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
		}
		partnSp = (short) row.qty;
		if (ply.getResCount() != _rowMap.size()) {
			ply.setResCount((short) _rowMap.size());
		}
	}

	public ResCtrl(Player _ply) {
		ply = _ply;
		
		if (ply.getResCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyRes(), ply.getPlyId(), ply.getResCount()), ply.getSocket(), (byte) 0);
		} else {
			ResDBRow row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99901);
			coin = row.qty = GlobalConst.init_coin;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
			
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99902);
			credit = row.qty = GlobalConst.init_credit;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
			
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99903);
			power = row.qty = GlobalConst.init_power;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
			
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99904);
			//exp = row.qty = 0;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
			
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99906);
			//soul = row.qty = 0;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
			
			row = new ResDBRow();
			row.id = DbCenter.nextPlyResId();
			row.resT = Resource.getResourceT(Resource.ID_99907);
			row.qty = partnSp = GlobalConst.hero_skill_max;
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyRes(), row.id, ply.getPlyId(), row.resT.getId(), row.qty));
			_rowMap.put(row.resT.getId(), row);
			
			ply.setResCount((short) _rowMap.size());
		}
	}
}
