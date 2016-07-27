package db;

import io.netty.buffer.ByteBuf;

import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import server.dispatcher.RecvMsgDispatcher;
import tool.ByteBufDecoder;
import tool.UnpooledBufUtil;
import mysql.DBReadAdmin;
import mysql.DBWriteAdmin;
//import mysql.DatabaseAdmin;

/**
 * DB中心
 * */
public final class DbCenter {
	
	/**sql队列超时时间*/
	private static final int DB_QUE_TIMEOUT = 600000;
	/**数据库连接超时临界值*/
	private static final int DB_TIMEOUT_WAIT = 25200000; // mySql默认8小时，所以设定为7小时
	
	private final static class LoadSql {
		String sql; int socket; byte type;
		LoadSql(String _sql, int _sock, byte _type) { sql = _sql; socket = _sock; type = _type; }
	}
	
	private static final DBWriteAdmin gameSaveDBA;
	private static final DBReadAdmin gameLoadDBA;
	//private static final DatabaseAdmin gameDBA;
	
	// SQL队列
	private static final BlockingQueue<String> sqlSaveQue;
	private static final BlockingQueue<LoadSql> sqlLoadQue;
	
	private static final Thread dbSaveTd;
	private static final Thread dbLoadTd;
	
	//private static LoadSql _tmpLoadSql;
	
	private static long insert_ply_partn_id;
	private static long insert_ply_res_id;
	private static long insert_ply_god_id;
	private static long insert_ply_fort_id;
	private static long insert_ply_guide_id;
	private static long insert_ply_troop_id;
	private static long insert_ply_task_id;
	private static long insert_ply_frnd_id;
	private static long insert_ply_times_id;
	
	private static String sql_insert_ply_partn;
	private static String sql_insert_ply_res;
	private static String sql_insert_ply_god;
	private static String sql_insert_ply_fort;
	private static String sql_insert_ply_guide;
	private static String sql_insert_ply_troop;
	private static String sql_insert_ply_task;
	private static String sql_insert_ply_frnd;
	private static String sql_insert_ply_times;
	
	private static String sql_update_ply, sql_save_ply;
	private static String sql_update_ply_partn;
	private static String sql_update_ply_res;
	private static String sql_update_ply_god;
	private static String sql_update_ply_fort;
	//private static String sql_update_ply_guide;
	private static String sql_update_ply_troop;
	private static String sql_update_ply_task;
	private static String sql_update_ply_frnd;
	private static String sql_update_ply_times;
	
	private static String sql_select_ply_partn;
	private static String sql_select_ply_res;
	private static String sql_select_ply_god;
	private static String sql_select_ply_fort;
	private static String sql_select_ply_guide;
	private static String sql_select_ply_troop;
	private static String sql_select_ply_task;
	private static String sql_select_ply_frnd;
	private static String sql_select_ply_times;
	
	private static /*volatile */boolean _dbRun;
	
	static {
		gameSaveDBA = new DBWriteAdmin();
		gameLoadDBA = new DBReadAdmin();
		//gameDBA = new DatabaseAdmin();
		sqlSaveQue = new LinkedBlockingQueue<String>(1024);
		sqlLoadQue = new LinkedBlockingQueue<LoadSql>(1024);
		dbSaveTd = new Thread(new Runnable() {
			@Override
			public void run() {
				//System.out.println("DBSaveThread Start...");
//				for (;;) {
//					if (_dbRun) {
//						try {
//							//gameDBA.executeUpdate(sqlQue.poll(10000, TimeUnit.MILLISECONDS)); // 超时为10秒(可修改)
//							gameDBA.executeUpdate(sqlSaveQue.take());
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					} else {
//						while (!sqlSaveQue.isEmpty()) {
//							gameDBA.executeUpdate(sqlSaveQue.poll());
//						}
//						_dbRun = true;
//						break;
//					}
//				}
				String sql = null;
				int dbTimeoutRecord = 0; // 数据库连接超时记录
				try {
					while (_dbRun) {
						sql = sqlSaveQue.poll(DB_QUE_TIMEOUT, TimeUnit.MILLISECONDS);
						if (sql != null) {
							dbTimeoutRecord = 0;
							gameSaveDBA.executeUpdate(sql);
						} else {
							dbTimeoutRecord += DB_QUE_TIMEOUT;
							if (dbTimeoutRecord > DB_TIMEOUT_WAIT) {
								dbTimeoutRecord = 0;
								gameSaveDBA.activateStatement();
								System.out.println("DBSaveStatement Timeout Reset...");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("sqlSaveQue.poll failed..[sql]:" + sql);
				}
				//System.out.println("DBSaveThread End...");
			}
		});
		dbLoadTd = new Thread(new Runnable() {
			@Override
			public void run() {
				//System.out.println("DBLoadThread Start...");
				LoadSql loadSql;
				ResultSet rs;
				ResultSetMetaData rsmd;
				ByteBuf buf;
				int[] types = new int[32];
				int dbTimeoutRecord = 0; // 数据库连接超时记录
				int capacity;
				for (short s, max, rowCount; ;) {
					try {
						loadSql = sqlLoadQue.poll(DB_QUE_TIMEOUT, TimeUnit.MILLISECONDS);
						if (loadSql != null) {
							dbTimeoutRecord = 0;
							rs = gameLoadDBA.executeQuery(loadSql.sql);
							if (rs.last()) {
								rowCount = (short) rs.getRow();
								rs.first();
								max = (short) (rsmd = rs.getMetaData()).getColumnCount();
								s = 1; capacity = 0;
								do {
									switch (types[s] = rsmd.getColumnType(s)) {
									case Types.SMALLINT:	capacity += 2; break;
									case Types.TINYINT:		capacity += 1; break;
									case Types.BIGINT:		capacity += 8; break;
									case Types.INTEGER:		capacity += 4; break;
									default: // Types.CHAR
										capacity += 32;	break;
									}
								} while (++s <= max);
								buf = UnpooledBufUtil.dynamicBuf(capacity * rowCount + 10)
										.writeInt(loadSql.socket).writeShort(RecvMsgDispatcher.MSG_MAX_IDX)
										.writeByte(loadSql.type).writeShort(rowCount);
								do {
									s = 1;
									do {
										switch (types[s]) {
										case Types.SMALLINT:	buf.writeShort(rs.getShort(s)); break;
										case Types.TINYINT:		buf.writeByte(rs.getByte(s)); 	break;
										case Types.BIGINT:		buf.writeLong(rs.getLong(s)); 	break;
										case Types.INTEGER:		buf.writeInt(rs.getInt(s)); 	break;
										default: // Types.CHAR
											ByteBufDecoder.writeString(buf, rs.getString(s));	break;
										}
									} while (++s <= max);
								} while (rs.next());
								RecvMsgDispatcher.captureMsg(buf);
							}
							rs.close();
						} else {
							dbTimeoutRecord += DB_QUE_TIMEOUT;
							if (dbTimeoutRecord > DB_TIMEOUT_WAIT) {
								dbTimeoutRecord = 0;
								gameLoadDBA.activateStatement();
								System.out.println("DBLoadStatement Timeout Reset...");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//System.out.println("DBLoadThread End...");
			}
		});
	}
	
	public static void executeUpdate(String sql) {
		if (!sqlSaveQue.offer(sql)) {
			System.out.println("sqlSaveQue已满，执行阻塞添加");
			try {
				sqlSaveQue.put(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public final static void executeQuery(String sql, int socket, byte type) {
		if (!sqlLoadQue.offer(new LoadSql(sql, socket, type))) {
			System.out.println("sqlLoadQue已满，执行阻塞添加");
			try {
				sqlLoadQue.put(new LoadSql(sql, socket, type));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
//	public final static void executeQuery(DbLoader loader, String sql) {
//		loader.loadFromDB(gameDBA.executeQuery(sql));
//	}
	
	public final static long nextPlyPartnId() { return ++insert_ply_partn_id; }
	public final static long nextPlyResId() { return ++insert_ply_res_id; }
	public final static long nextPlyGodId() { return ++insert_ply_god_id; }
	public final static long nextPlyFortId() { return ++insert_ply_fort_id; }
	public final static long nextPlyGuideId() { return ++insert_ply_guide_id; }
	public final static long nextPlyTroopId() { return ++insert_ply_troop_id; }
	public final static long nextPlyTaskId() { return ++insert_ply_task_id; }
	public final static long nextPlyFrndId() { return ++insert_ply_frnd_id; }
	public final static long nextPlyTimesId() { return ++insert_ply_times_id; }
	
	public final static String getSqlInsertPlyPartn() { return sql_insert_ply_partn; }
	public final static String getSqlInsertPlyRes() { return sql_insert_ply_res; }
	public final static String getSqlInsertPlyGod() { return sql_insert_ply_god; }
	public final static String getSqlInsertPlyFort() { return sql_insert_ply_fort; }
	public final static String getSqlInsertPlyGuide() { return sql_insert_ply_guide; }
	public final static String getSqlInsertPlyTroop() { return sql_insert_ply_troop; }
	public final static String getSqlInsertPlyTask() { return sql_insert_ply_task; }
	public final static String getSqlInsertPlyFrnd() { return sql_insert_ply_frnd; }
	public final static String getSqlInsertPlyTimes() { return sql_insert_ply_times; }
	
	public final static String getSqlUpdatePly() { return sql_update_ply; }
	public final static String getSqlSavePly() { return sql_save_ply; }
	public final static String getSqlUpdatePlyPartn() { return sql_update_ply_partn; }
	public final static String getSqlUpdatePlyRes() { return sql_update_ply_res; }
	public final static String getSqlUpdatePlyGod() { return sql_update_ply_god; }
	public final static String getSqlUpdatePlyFort() { return sql_update_ply_fort; }
	//public final static String getSqlUpdatePlyGuide() { return sql_update_ply_guide; }
	public final static String getSqlUpdatePlyTroop() { return sql_update_ply_troop; }
	public final static String getSqlUpdatePlyTask() { return sql_update_ply_task; }
	public final static String getSqlUpdatePlyFrnd() { return sql_update_ply_frnd; }
	public final static String getSqlUpdatePlyTimes() { return sql_update_ply_times; }
	
	public final static String getSqlSelectPlyPartn() { return sql_select_ply_partn; }
	public final static String getSqlSelectPlyRes() { return sql_select_ply_res; }
	public final static String getSqlSelectPlyGod() { return sql_select_ply_god; }
	public final static String getSqlSelectPlyFort() { return sql_select_ply_fort; }
	public final static String getSqlSelectPlyGuide() { return sql_select_ply_guide; }
	public final static String getSqlSelectPlyTroop() { return sql_select_ply_troop; }
	public final static String getSqlSelectPlyTask() { return sql_select_ply_task; }
	public final static String getSqlSelectPlyFrnd() { return sql_select_ply_frnd; }
	public final static String getSqlSelectPlyTimes() { return sql_select_ply_times; }
	
	
	public static void start() throws Exception {
		Properties properties = new Properties();
		FileReader reader = new FileReader("config/config.properties");
		properties.load(reader);
		reader.close();
		String DB_IP = properties.getProperty("GAME_DB_IP");
		String DB_PORT = properties.getProperty("GAME_DB_PORT");
		String DB_NAME = properties.getProperty("GAME_DB_NAME");
		String DB_ACCOUNT = properties.getProperty("GAME_DB_ACCOUNT");
		String DB_PASSWORD = properties.getProperty("GAME_DB_PASSWORD");
		properties.clear();
		if (!gameSaveDBA.connect(DB_IP, DB_PORT, DB_NAME, DB_ACCOUNT, DB_PASSWORD)) {
			System.out.println("用户数据库连接失败。");
			System.exit(0);
		}
		if (!gameLoadDBA.connect(DB_IP, DB_PORT, DB_NAME, DB_ACCOUNT, DB_PASSWORD)) {
			System.out.println("用户数据库连接失败。");
			System.exit(0);
		}
		System.out.println("用户数据库连接成功。");
		sql_update_ply = "update ply set %s=%d where id=%d limit 1";
		sql_save_ply = "update ply set name='%s',icon=%d where id=%d limit 1";
		// 创建伙伴表
		gameSaveDBA.createTable("create table ply_partn("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "partn smallint unsigned not null,"
				+ "lv smallint unsigned not null,"
				+ "exp int unsigned not null,"
				+ "star tinyint unsigned not null,"
				+ "step tinyint unsigned not null,"
				+ "skl0 smallint unsigned not null,"
				+ "skl1 smallint unsigned not null,"
				+ "skl2 smallint unsigned not null,"
				+ "skl3 smallint unsigned not null,"
				+ "slot0 tinyint unsigned not null,"
				+ "slot1 tinyint unsigned not null,"
				+ "slot2 tinyint unsigned not null,"
				+ "slot3 tinyint unsigned not null,"
				+ "slot4 tinyint unsigned not null,"
				+ "slot5 tinyint unsigned not null,"
				+ "primary key(id))", "ply_partn");
		insert_ply_partn_id = gameLoadDBA.getLongFieldMaxValue("ply_partn", "id");
		sql_insert_ply_partn = "insert into ply_partn value(%d,%d,%d,1,0,%d,0,1,0,0,0,0,0,0,0,0,0)";
		sql_update_ply_partn = "update ply_partn set lv=%d,exp=%d,star=%d,step=%d,skl0=%d,skl1=%d,skl2=%d,skl3=%d,slot0=%d,slot1=%d,slot2=%d,slot3=%d,slot4=%d,slot5=%d where id=%d limit 1";
		sql_select_ply_partn = "select id,partn,lv,exp,star,step,skl0,skl1,skl2,skl3,slot0,slot1,slot2,slot3,slot4,slot5 from ply_partn where ply_id=%d limit %d";
		// 创建资源表
		gameSaveDBA.createTable("create table ply_res("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "res int unsigned not null,"
				+ "qty int unsigned not null,"
				+ "primary key(id))", "ply_res");
		insert_ply_res_id = gameLoadDBA.getLongFieldMaxValue("ply_res", "id");
		sql_insert_ply_res = "insert into ply_res value(%d,%d,%d,%d)";
		sql_update_ply_res = "update ply_res set qty=%d where id=%d limit 1";
		sql_select_ply_res = "select id,res,qty from ply_res where ply_id=%d limit %d";
		// 创建邪神表
		gameSaveDBA.createTable("create table ply_god("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "god smallint unsigned not null,"
				+ "lv smallint unsigned not null,"
				+ "star tinyint unsigned not null,"
				//+ "skl0 tinyint unsigned not null,"
				//+ "skl1 tinyint unsigned not null,"
				//+ "skl2 tinyint unsigned not null,"
				+ "mood smallint unsigned not null,"
				+ "primary key(id))", "ply_god");
		insert_ply_god_id = gameLoadDBA.getLongFieldMaxValue("ply_god", "id");
		sql_insert_ply_god = "insert into ply_god value(%d,%d,%d,1,%d,300)";
		sql_update_ply_god = "update ply_god set lv=%d,star=%d,mood=%d where id=%d limit 1";
		sql_select_ply_god = "select id,god,lv,star,mood from ply_god where ply_id=%d limit %d";
		// 创建关卡表
		gameSaveDBA.createTable("create table ply_fort("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "fort int unsigned not null,"
				+ "star tinyint unsigned not null,"
				+ "times smallint unsigned not null,"
				+ "primary key(id))", "ply_fort");
		insert_ply_fort_id = gameLoadDBA.getLongFieldMaxValue("ply_fort", "id");
		sql_insert_ply_fort = "insert into ply_fort value(%d,%d,%d,%d,%d)";
		sql_update_ply_fort = "update ply_fort set star=%d,times=%d where id=%d limit 1";
		sql_select_ply_fort = "select id,fort,star,times from ply_fort where ply_id=%d limit %d";
		// 创建布阵表
		gameSaveDBA.createTable("create table ply_troop("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "troop tinyint unsigned not null,"
				+ "pos_god smallint unsigned not null,"
				+ "pos0 smallint unsigned not null,"
				+ "pos1 smallint unsigned not null,"
				+ "pos2 smallint unsigned not null,"
				+ "pos3 smallint unsigned not null,"
				+ "primary key(id))", "ply_troop");
		insert_ply_troop_id = gameLoadDBA.getLongFieldMaxValue("ply_troop", "id");
		sql_insert_ply_troop = "insert into ply_troop value(%d,%d,%d,%d,%d,%d,%d,%d)";
		sql_update_ply_troop = "update ply_troop set pos_god=%d,pos0=%d,pos1=%d,pos2=%d,pos3=%d where id=%d limit 1";
		sql_select_ply_troop = "select id,troop,pos_god,pos0,pos1,pos2,pos3 from ply_troop where ply_id=%d limit %d";
		// 创建引导表
		gameSaveDBA.createTable("create table ply_guide("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "guide smallint unsigned not null,"
				+ "primary key(id))", "ply_guide");
		insert_ply_guide_id = gameLoadDBA.getLongFieldMaxValue("ply_guide", "id");
		sql_insert_ply_guide = "insert into ply_guide value(%d,%d,%d)";
		sql_select_ply_guide = "select guide from ply_guide where ply_id=%d limit %d";
		// 创建任务表
		gameSaveDBA.createTable("create table ply_task("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "task int unsigned not null,"
				+ "type tinyint unsigned not null,"
				+ "cond int unsigned not null,"
				+ "flag tinyint unsigned not null,"
				+ "primary key(id))", "ply_task");
		insert_ply_task_id = gameLoadDBA.getLongFieldMaxValue("ply_task", "id");
		sql_insert_ply_task = "insert into ply_task value(%d,%d,%d,%d,%d,%d)";
		sql_update_ply_task = "update ply_task set cond=%d,flag=%d where id=%d limit 1";
		sql_select_ply_task = "select id,task,type,cond,flag from ply_task where ply_id=%d limit %d";
		// 创建好友表
		gameSaveDBA.createTable("create table ply_frnd("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "frnd bigint unsigned not null,"
				+ "primary key(id))", "ply_frnd");
		insert_ply_frnd_id = gameLoadDBA.getLongFieldMaxValue("ply_frnd", "id");
		sql_insert_ply_frnd = "insert into ply_frnd value(%d,%d,%d)";
		sql_update_ply_frnd = "update ply_frnd set frnd=%d where id=%d limit 1";
		sql_select_ply_frnd = "select id,frnd from ply_frnd where ply_id=%d limit %d";
		// 创建记录次数表
		gameSaveDBA.createTable("create table ply_times("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "type smallint unsigned not null,"
				+ "times int unsigned not null,"
				+ "primary key(id))", "ply_times");
		insert_ply_times_id = gameLoadDBA.getLongFieldMaxValue("ply_times", "id");
		sql_insert_ply_times = "insert into ply_times value(%d,%d,%d,%d)";
		sql_update_ply_times = "update ply_times set times=%d where id=%d limit 1";
		sql_select_ply_times = "select id,type,times from ply_times where ply_id=%d limit %d";
		// 启动DB线程
		_dbRun = true;
		dbSaveTd.setName("dbSaveThread");
		dbSaveTd.start();
		dbLoadTd.setName("dbLoadThread");
		dbLoadTd.start();
	}
	
	public static void close() throws Exception {
		_dbRun = false;
		byte b = 0;
		// 操作一些DB语句
		while (!_dbRun) {
			Thread.sleep(250);
			if (++b == 10) {
				//System.out.println("DbSaveThread blocking, break for exit...");
				break;
			}
		}
		while (!sqlSaveQue.isEmpty()) {
			gameSaveDBA.executeUpdate(sqlSaveQue.poll());
		}
		gameSaveDBA.close();
		gameLoadDBA.close();
	}
}
