package obj.ctrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import obj.GameController;
import obj.Player;
import db.DbCenter;
import db.DbLoader;
import db.DbObj;

public final class FrndCtrl implements DbLoader, GameController {
	
	private final static class FrndDBRow extends DbObj {
		long id; long frnd;
	}
	
	private static FrndDBRow _tmpRow;
	
	private final Map<Long, FrndDBRow> _rowMap = new HashMap<Long, FrndDBRow>();
	private final List<FrndDBRow> _emptyRowList = new ArrayList<FrndDBRow>();
	
	private Player ply;
	
	@Override
	public void onceNoticeClient() {
		
	}

	@Override
	public void saveToDB() {
		for (FrndDBRow row : _rowMap.values()) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyFrnd(), row.frnd, row.id));
			}
		}
		for (FrndDBRow row : _emptyRowList) {
			if (row.IsNeedSave()) {
				row.setAlreadySave();
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyFrnd(), 0, row.id));
			}
		}
	}

	@Override
	public void loadFromDB(ByteBuf buf) {
		for (short s = 0, ms = buf.readShort(); s != ms; ++s) {
			_tmpRow = new FrndDBRow();
			_tmpRow.id = buf.readLong();
			_tmpRow.frnd = buf.readLong();
			if (_tmpRow.frnd != 0) {
				_rowMap.put(_tmpRow.frnd, _tmpRow);
			} else {
				_emptyRowList.add(_tmpRow);
			}
		}
	}
	
	public FrndCtrl(Player _ply) {
		ply = _ply;
		if (ply.getFrndCount() != 0) {
			DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyFrnd(), ply.getPlyId(), ply.getFrndCount()), ply.getSocket(), (byte) 5);
		}
	}
}
