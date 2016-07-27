package db;

import java.io.FileReader;
import java.util.Properties;

import mysql.DatabaseAdmin;

public final class DbCenter {
	
	public static final DatabaseAdmin accDBA, gameDBA;
	
	private static long insert_acc_id;
	private static long insert_ply_id;
	
	private static String sql_insert_ply;
	private static String sql_select_ply;
	
	static {
		accDBA = new DatabaseAdmin();
		gameDBA = new DatabaseAdmin();
	}
	
	public final static long nextAccId() { return ++insert_acc_id; }
	public final static long nextPlyId() { return ++insert_ply_id; }
	
	public final static String getSqlInsertPly() { return sql_insert_ply; }
	public final static String getSqlSelectPly() { return sql_select_ply; }
	
	
	public static void start() throws Exception {
		Properties properties = new Properties();
		FileReader reader = new FileReader("config/config.properties");
		properties.load(reader);
		reader.close();
		String DB_IP = properties.getProperty("ACC_DB_IP");
		String DB_PORT = properties.getProperty("ACC_DB_PORT");
		String DB_NAME = properties.getProperty("ACC_DB_NAME");
		String DB_ACCOUNT = properties.getProperty("ACC_DB_ACCOUNT");
		String DB_PASSWORD = properties.getProperty("ACC_DB_PASSWORD");
		if (!accDBA.connect(DB_IP, DB_PORT, DB_NAME, DB_ACCOUNT, DB_PASSWORD)) {
			System.out.println("账号数据库连接失败。");
			properties.clear();
			System.exit(0);
		}
		System.out.println("账号数据库连接成功。");
		DB_IP = properties.getProperty("GAME_DB_IP");
		DB_PORT = properties.getProperty("GAME_DB_PORT");
		DB_NAME = properties.getProperty("GAME_DB_NAME");
		DB_ACCOUNT = properties.getProperty("GAME_DB_ACCOUNT");
		DB_PASSWORD = properties.getProperty("GAME_DB_PASSWORD");
		properties.clear();
		if (!gameDBA.connect(DB_IP, DB_PORT, DB_NAME, DB_ACCOUNT, DB_PASSWORD)) {
			System.out.println("用户数据库连接失败");
			accDBA.close();
			System.exit(0);
		}
		System.out.println("用户数据库连接成功。");
		// 创建账号表
		accDBA.createTable("create table account("
				+ "id bigint unsigned not null,"
				+ "acc_name varchar(20) not null,"
				+ "password varchar(32) not null,"
				+ "primary key(id))", "account");
		insert_acc_id = accDBA.getLongFieldMaxValue("account", "id");
		// 创建用户角色表
		gameDBA.createTable("create table ply("
				+ "id bigint unsigned not null,"
				+ "acc_type tinyint unsigned not null,"
				+ "acc_id bigint unsigned not null,"
				+ "name char(20) not null,"
				+ "icon int unsigned not null,"
				//+ "gender tinyint unsigned not null,"
				+ "lv smallint unsigned not null,"
				+ "vip_lv tinyint unsigned not null,"
				+ "partn_count smallint unsigned not null,"
				+ "res_count smallint unsigned not null,"
				+ "god_count smallint unsigned not null,"
				+ "fort_count smallint unsigned not null,"
				+ "task_count smallint unsigned not null,"
				+ "troop_count smallint unsigned not null,"
				+ "frnd_count smallint unsigned not null,"
				+ "times_count smallint unsigned not null,"
				+ "guide_count smallint unsigned not null,"
				+ "mail_count smallint unsigned not null,"
				+ "logout_t bigint unsigned not null,"
				//+ "memo char(200) not null,"
				+ "primary key(id))", "ply");
		insert_ply_id = gameDBA.getLongFieldMaxValue("ply", "id");
		// 设置默认值
		int icon = 1;
		short lv = 1;
		byte vip_lv = 0;
		long logout_t = 0;
		sql_insert_ply = "insert into ply value(%d,%d,%d,'%s'"
				+ String.format(",%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d)",
						icon, lv, vip_lv, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, logout_t);
		sql_select_ply = "select * from ply where acc_type=%d and acc_id=%d limit 1";
	}
}
