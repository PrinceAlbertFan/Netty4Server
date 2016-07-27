package db;

import java.io.FileReader;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mysql.DatabaseAdmin;

public final class DbCenter {
	
	private static final DatabaseAdmin gameDBA;
	
	// SQL队列
	private static final BlockingQueue<String> sqlSaveQue;
	
	private static final Thread dbSaveTd;
	
	private static long insert_ply_mail_id;
	
	private static String sql_update_ply;
	private static String sql_insert_ply_mail;
	private static String sql_update_ply_mail;
	private static String sql_select_ply_mail;
	private static String sql_select_empty_mail;
	
	private static volatile boolean _dbRun;
	
	static {
		gameDBA = new DatabaseAdmin();
		sqlSaveQue = new LinkedBlockingQueue<String>(1024);
		dbSaveTd = new Thread(new Runnable() {
			@Override
			public void run() {
				//System.out.println("DBSaveThread Start...");
				for (;;) {
					if (_dbRun) {
						try {
							//gameDBA.executeUpdate(sqlQue.poll(10000, TimeUnit.MILLISECONDS)); // 超时为10秒(可修改)
							gameDBA.executeUpdate(sqlSaveQue.take());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						while (!sqlSaveQue.isEmpty()) {
							gameDBA.executeUpdate(sqlSaveQue.poll());
						}
						_dbRun = true;
						break;
					}
				}
				//System.out.println("DBSaveThread End...");
			}
		});
	}
	
	public static void executeUpdate(String sql) {
		if (!sqlSaveQue.add(sql)) {
			System.out.println("sqlSaveQue已满，执行阻塞添加");
			try {
				sqlSaveQue.put(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public final static ResultSet executeQuery(String sql) {
		return gameDBA.executeQuery(sql);
	}
	
	public final static long executeQueryLongFieldValue(String sql) {
		return gameDBA.executeQueryLongFieldValue(sql);
	}
	
	public final static long nextPlyMailId() { return ++insert_ply_mail_id; }
	
	public final static String getSqlUpdatePly() { return sql_update_ply; }
	public final static String getSqlInsertPlyMail() { return sql_insert_ply_mail; }
	public final static String getSqlUpdatePlyMail() { return sql_update_ply_mail; }
	public final static String getSqlSelectPlyMail() { return sql_select_ply_mail; }
	public final static String getSqlSelectEmptyMail() { return sql_select_empty_mail; }
	
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
		if (!gameDBA.connect(DB_IP, DB_PORT, DB_NAME, DB_ACCOUNT, DB_PASSWORD)) {
			System.out.println("用户数据库连接失败。");
			properties.clear();
			System.exit(0);
		}
		System.out.println("用户数据库连接成功。");
		sql_update_ply = "update ply set %s=%s where id=%d limit 1";
		// 创建邮件表(手游通用)
		gameDBA.createTable("create table ply_mail("
				+ "id bigint unsigned not null,"
				+ "ply_id bigint unsigned not null,"
				+ "mail smallint unsigned not null,"
				+ "cont varchar(20) not null,"
				+ "res_id int unsigned not null,"
				+ "res_qty int unsigned not null,"
				+ "due_t bigint unsigned not null,"
				+ "primary key(id))", "ply_mail");
		insert_ply_mail_id = gameDBA.getLongFieldMaxValue("ply_mail", "id");
		sql_insert_ply_mail = "insert into ply_mail value(%d,%d,%d,'%s',%d,%d,%d)";
		sql_update_ply_mail = "update ply_mail set mail=%d,cont='%s',res_id=%d,res_qty=%d,due_t=%d where id=%d limit 1";
		sql_select_ply_mail = "select id,mail,cont,res_id,res_qty,due_t from ply_mail where ply_id=%d limit %d";
		sql_select_empty_mail = "select id from ply_mail where ply_id=% and (mail=0 or due_t<%d) limit 1";
		// 启动DB线程
		_dbRun = true;
		dbSaveTd.setName("dbSaveThread");
		dbSaveTd.start();
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
			gameDBA.executeUpdate(sqlSaveQue.poll());
		}
		gameDBA.close();
	}
}
