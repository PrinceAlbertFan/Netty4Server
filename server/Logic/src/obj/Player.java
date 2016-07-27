package obj;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import obj.ctrl.*;
import server.SndMsg;
import tool.ByteBufDecoder;
import data.global.GloTmpVal;
import data.global.GlobalConst;
import data.xml.Level;
import data.xml.Level.LevelT;
import db.DbCenter;
import db.DbObj;
import manager.TimeManager;

public final class Player implements GameController {
	
	private final static class DBRow extends DbObj {
		long id;
		byte acc_type;
		long acc_id;
		String name; int icon;
		short lv; byte vip_lv;
		short partn_count; short res_count; short god_count;
		short fort_count; short task_count; short troop_count;
		short frnd_count; short times_count; short guide_count;
		long logout_t;
	}
	
	/**Socket连接表，主键是链接号*/
	private static final Map<Integer, Player> _sockMap 	= new HashMap<Integer, Player>();
	/**账号连接表，主键是ply_id*/
	private static final Map<Long, Player> _plyMap 		= new HashMap<Long, Player>();
	
	/**离线客户缓存*/
	private static final Map<Long, Player> _memPlyMap	= new HashMap<Long, Player>();
	
	private static Iterator<Map.Entry<Integer, Player>> _sockMapIter;
	
	public final static int getPlayerCount() { return _sockMap.size(); }
	public final static Player getPlayerBySocket(int socket) { return _sockMap.get(socket); }
	//public final static Map<Integer, Player> 	getPlySockMap() 	{ return _sockMap; }
	//public final static Map<Long, Player> 		getPlyIdMap() 		{ return _plyMap; }
	
	/**副本中获取列表(id,num)*/
	public final Map<Integer, Integer> _mapDropMake = new HashMap<Integer, Integer>();
	/**每天各种重置当前次数*/
	public final byte[] _arrResetTimes = new byte[17];
	
	private ResCtrl resCtrl;
	private PartnCtrl partnCtrl;
	private GodCtrl godCtrl;
	private FortCtrl fortCtrl;
	private TaskCtrl taskCtrl;
	private GuideCtrl guideCtrl;
	private TroopCtrl troopCtrl;
	private TimesCtrl timesCtrl;
	private FrndCtrl frndCtrl;
	
	private DBRow dbRow;
	
	private LevelT lvT;
	
	private int socket, savaDBTime, powerRecoverT, partnSpRecoverT;
	
	public int lottery_coin_free_recoverT;
	public boolean bLottery_coin = true;
	
	public boolean isLoadEnd, isNeedSnd;
	
	public static void mainUpdate() {
		_sockMapIter = _sockMap.entrySet().iterator();
		while (_sockMapIter.hasNext()) {
			_sockMapIter.next().getValue().onUpdate();
		}
	}
	
	public static void login(ByteBuf buf, int socket) {
		if (!_sockMap.containsKey(socket)) {
			long ply_id = buf.readLong();
			Player ply = _memPlyMap.remove(ply_id);
			if (ply != null) {
				ply.socket = socket;
				//ply.setSocket(socket);
				_plyMap.put(ply_id, ply);
			} else {
				ply = _plyMap.get(ply_id);
				if (ply == null) {
					ply = new Player();
					ply.dbRow = new DBRow();
					ply.dbRow.id = ply_id;
					ply.socket = socket;
					ply.dbRow.acc_type = buf.readByte();
					ply.dbRow.acc_id = buf.readLong();
					ply.dbRow.name = ByteBufDecoder.readString(buf);
					ply.dbRow.icon = buf.readInt();
					ply.dbRow.lv = buf.readShort();
					ply.lvT = Level.getLevelT(ply.dbRow.lv);
					ply.dbRow.vip_lv = buf.readByte();
					ply.dbRow.partn_count = buf.readShort();
					ply.dbRow.res_count = buf.readShort();
					ply.dbRow.god_count = buf.readShort();
					ply.dbRow.fort_count = buf.readShort();
					ply.dbRow.task_count = buf.readShort();
					ply.dbRow.troop_count = buf.readShort();
					ply.dbRow.frnd_count = buf.readShort();
					ply.dbRow.times_count = buf.readShort();
					ply.dbRow.guide_count = buf.readShort();
					ply.dbRow.logout_t = buf.readLong();
					if (ply.dbRow.logout_t == 0) {
						ply.dbRow.logout_t = TimeManager.getGmtMsecTimestamp();
					}
					_plyMap.put(ply_id, ply);
				} else { // 异地登陆
					System.out.println("异地登陆用户：" + ply.getPlyId());
					// 通知网关
					SndMsg.logout.send(ply.socket);
					_sockMap.remove(ply.socket);
					ply.socket = socket;
					//ply.setSocket(socket);
				}
			}
			_sockMap.put(ply.socket, ply);
//			if (!ply.isLoadEnd) {
//			} else {
//				buf.skipBytes(15 + buf.getShort(buf.readerIndex() + 9));
//			}
			ply.enterGame();
		}
	}
	
	public static void logout(Player ply) {
		_sockMap.remove(ply.socket);
		_memPlyMap.put(ply.dbRow.id, _plyMap.remove(ply.dbRow.id));
		ply.leaveGame();
	}
	
	public static void dayRefresh() {
		for (Player ply : _memPlyMap.values()) {
			ply.refreshAllData();
		}
		for (Player ply : _sockMap.values()) {
			ply.refreshAllData();
		}
	}
	
	public static void close() {
		if (!_memPlyMap.isEmpty()) {
			for (Player ply : _memPlyMap.values()) {
				ply.saveToDB();
			}
			_memPlyMap.clear();
		}
		if (!_sockMap.isEmpty()) {
			for (Player ply : _sockMap.values()) {
				ply.leaveGame();
			}
			_plyMap.clear();
			_sockMap.clear();
		}
	}
	
	public void onUpdate() {
		if ((savaDBTime += TimeManager.dt) > 600000) { // 10分钟(可修改)存库一次
			savaDBTime = 0;
			saveToDB();
		}
		if (resCtrl.getPower() < lvT.getPowerLimit()
				&& (powerRecoverT += TimeManager.dt) > GlobalConst.power_recover_time) {
			powerRecoverT = 0;
			resCtrl.addOnePower();
		}
		if (resCtrl.getPartnSp() < GlobalConst.hero_skill_max
				&& (partnSpRecoverT += TimeManager.dt) > GlobalConst.hero_skill_CD) {
			partnSpRecoverT = 0;
			resCtrl.addOnePartnSp();
		}
		if (!bLottery_coin && (lottery_coin_free_recoverT += TimeManager.dt) > GlobalConst.lottery_coin_cutdown) {
			lottery_coin_free_recoverT = 0;
			bLottery_coin = true;
		}
	}
	
	public final ResCtrl getResCtrl() { return resCtrl; }
	public final PartnCtrl getPartnCtrl() { return partnCtrl; }
	public final GodCtrl getGodCtrl() { return godCtrl; }
	public final FortCtrl getFortCtrl() { return fortCtrl; }
	public final TaskCtrl getTaskCtrl() { return taskCtrl; }
	public final GuideCtrl getGuideCtrl() { return guideCtrl; }
	public final TroopCtrl getTroopCtrl() { return troopCtrl; }
	public final TimesCtrl getTimesCtrl() { return timesCtrl; }
	public final FrndCtrl getFrndCtrl() { return frndCtrl; }
	
	public final LevelT getLevelT() { return lvT; }
	
	public final int getPartnSpRecoverSurplusSecond() { return (GlobalConst.hero_skill_CD - partnSpRecoverT) / 1000;}
	
	public final long getPlyId() { return dbRow.id; }
	public final void setPlyId(long id) { dbRow.id = id; }
	
	public final void setAccType(byte acc_type) { dbRow.acc_type = acc_type; }
	
	public final void setAccId(long acc_id) { dbRow.acc_id = acc_id; }
	
	public final String getName() { return dbRow.name; }
	public final void setName(String name) {
		if (!dbRow.name.equals(name)) {
			dbRow.setNeedSave();
			SndMsg.setName.reset().writeString(dbRow.name = name).packAndSend(socket);
		}
	}
	
	public final int getIcon() { return dbRow.icon; }
	public void setIcon(int icon) {
		if (dbRow.icon != icon) {
			dbRow.setNeedSave();
			SndMsg.setIcon.reset().writeInt(dbRow.icon = icon).pack(socket).send();
		}
	}
	
	public final short getLv() { return dbRow.lv; }
	public void upgrade() {
		lvT = Level.getLevelT(++dbRow.lv);
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(), "lv", dbRow.lv, dbRow.id));
		taskCtrl.checkAllTask();
		if (dbRow.lv % 5 == 0) {
			taskCtrl.refreshTask((byte) 101, dbRow.lv, 1);
		}
	}
	
	public final short getVipLv() { return dbRow.vip_lv; }
	public final void setVipLv(byte vip_lv) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"vip_lv", dbRow.vip_lv = vip_lv, dbRow.id));
	}
	
	public final short getPartnCount() { return dbRow.partn_count; }
	public final void setPartnCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"partn_count", dbRow.partn_count = count, dbRow.id));
	}
	
	public final short getResCount() { return dbRow.res_count; }
	public final void setResCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"res_count", dbRow.res_count = count, dbRow.id));
	}
	
	public final short getGodCount() { return dbRow.god_count; }
	public final void setGodCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"god_count", dbRow.god_count = count, dbRow.id));
	}
	
	public final short getFortCount() { return dbRow.fort_count; }
	public final void setFortCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"fort_count", dbRow.fort_count = count, dbRow.id));
	}
	
	public final short getTaskCount() { return dbRow.task_count; }
	public final void setTaskCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"task_count", dbRow.task_count = count, dbRow.id));
	}
	
	public final short getTroopCount() { return dbRow.troop_count; }
	public final void setTroopCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"troop_count", dbRow.troop_count = count, dbRow.id));
	}
	
	public final short getFrndCount() { return dbRow.frnd_count; }
	public final void setFrndCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"frnd_count", dbRow.frnd_count = count, dbRow.id));
	}
	
	public final short getTimesCount() { return dbRow.times_count; }
	public final void setTimesCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"times_count", dbRow.times_count = count, dbRow.id));
	}
	
	public final short getGuideCount() { return dbRow.guide_count; }
	public final void setGuideCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"guide_count", dbRow.guide_count = count, dbRow.id));
	}
	
	public final long getLogoutT() { return dbRow.logout_t; }
	public final void setLogoutT(long logout_t) { dbRow.logout_t = logout_t; }
	
	public final int getSocket() { return socket; }
	//public final void setSocket(int sock) { socket = sock; }
	
	public void enterGame() {
		if (!isLoadEnd) {
			System.out.println("开始加载：ply_id:" + dbRow.id);
			resCtrl = new ResCtrl(this);
			partnCtrl = new PartnCtrl(this);
			godCtrl = new GodCtrl(this);
			fortCtrl = new FortCtrl(this);
			taskCtrl = new TaskCtrl(this);
			frndCtrl = new FrndCtrl(this);
			troopCtrl = new TroopCtrl(this); // troopCtrl必须在partnCtrl之后new
			timesCtrl = new TimesCtrl(this);
			guideCtrl = new GuideCtrl(this); // guideCtrl必须放最后new
		} else {
			refreshDynamicData();
		}
		//onceNoticeClient();
		SndMsg.login.send(this);
	}
	
	public void leaveGame() {
		leaveBattle();
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(),
				"logout_t", dbRow.logout_t = TimeManager.getGmtMsecTimestamp(), dbRow.id));
		saveToDB();
	}
	
	public void leaveBattle() {
		if (!_mapDropMake.isEmpty()) {
			System.out.println("强制退出副本，清理战场战利品");
			_mapDropMake.clear();
		}
	}
	
	public void refreshDynamicData() {
		GloTmpVal.tmpL = TimeManager.getGmtMsecTimestamp() - dbRow.logout_t;
		if (resCtrl.getPower() < lvT.getPowerLimit()) {
			GloTmpVal.tmpI = (int) (GloTmpVal.tmpL / GlobalConst.power_recover_time);
			if (resCtrl.getPower() + GloTmpVal.tmpI < lvT.getPowerLimit()) {
				resCtrl.addPower(GloTmpVal.tmpI);
				powerRecoverT = (int) (GloTmpVal.tmpL % GlobalConst.power_recover_time);
			} else {
				resCtrl.addPower(lvT.getPowerLimit() - resCtrl.getPower());
			}
		}
		if (resCtrl.getPartnSp() < GlobalConst.hero_skill_max) {
			GloTmpVal.tmpI = (int) (GloTmpVal.tmpL / GlobalConst.hero_skill_CD);
			if (resCtrl.getPartnSp() + GloTmpVal.tmpI < GlobalConst.hero_skill_max) {
				resCtrl.addPartnSp(GloTmpVal.tmpI);
				partnSpRecoverT = (int) (GloTmpVal.tmpL % GlobalConst.hero_skill_CD);
			} else {
				resCtrl.addPartnSp(GlobalConst.hero_skill_max - resCtrl.getPartnSp());
			}
		}
		if (!bLottery_coin) {
			if (GloTmpVal.tmpL > GlobalConst.lottery_coin_cutdown
					|| GloTmpVal.tmpL + lottery_coin_free_recoverT > GlobalConst.lottery_coin_cutdown) {
				lottery_coin_free_recoverT = 0;
				bLottery_coin = true;
			} else {
				lottery_coin_free_recoverT += GloTmpVal.tmpL;
			}
		}
	}
	
	public void refreshAllData() {
		Arrays.fill(_arrResetTimes, (byte) 0);
		taskCtrl.refreshAllTask();
		fortCtrl.refreshAllFort();
		timesCtrl.refreshAllTimes();
	}
	
	@Override
	public void onceNoticeClient() {
		SndMsg.initData.send(this);
		//resCtrl.onceNoticeClient();
		//partnCtrl.onceNoticeClient();
	}
	
	@Override
	public void saveToDB() {
		if (dbRow.IsNeedSave()) {
			dbRow.setAlreadySave();
			DbCenter.executeUpdate(String.format(DbCenter.getSqlSavePly(),
					dbRow.name, dbRow.icon, dbRow.id));
		}
		resCtrl.saveToDB();
		partnCtrl.saveToDB();
		godCtrl.saveToDB();
		fortCtrl.saveToDB();
		taskCtrl.saveToDB();
		frndCtrl.saveToDB();
		//guideCtrl.saveToDB();
		troopCtrl.saveToDB();
		timesCtrl.saveToDB();
	}
	
	public void loadOver() {
		isLoadEnd = true;
		if (isNeedSnd) {
			isNeedSnd = false;
			SndMsg.enterGame.reset().writeLong(dbRow.id).pack(socket).send();
			System.out.println("初始信息延迟发送");
		}
	}
}
