package db;

import java.io.FileReader;
import java.util.Properties;

import mysql.DatabaseAdmin;

public final class DbCenter {
	
	public static final DatabaseAdmin gameDBA;
	
	private static int insert_guild_id;
	
	private static String sql_insert_guild;
	private static String sql_insert_guild_member;
	
	static {
		gameDBA = new DatabaseAdmin();
	}
	
	public final static int nextGuildId() {
		return ++insert_guild_id;
	}
	
	public final static String getSqlInsertGuild() {
		return sql_insert_guild;
	}
	
	public final static String getSqlInsertGuildMember() {
		return sql_insert_guild_member;
	}
	
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
		// 创建公会表
		gameDBA.createTable("create table guild("
				+ "id int unsigned not null,"
				+ "name char(20) not null,"
				+ "lv smallint unsigned not null,"
				+ "exp int unsigned not null,"
				+ "cost_exp int unsigned not null,"
				+ "join_lv smallint unsigned not null,"
				+ "join_type tinyint unsigned not null,"
				+ "member_count smallint unsigned not null,"
				+ "memo char(200) not null,"
				+ "primary key(id))", "guild");
		insert_guild_id = gameDBA.getIntFieldMaxValue("guild", "id");
		short lv = 1;
		int exp = 0;
		int cost_exp = 0;
		short join_lv = 1;
		byte join_type = 1;
		short member_count = 1;
		String memo = "";
		sql_insert_guild = "insert into guild value(%d,'%s'"
				+ String.format(",%d,%d,%d,%d,%d,%d,'%s')", lv, exp, cost_exp, join_lv, join_type, member_count, memo);
		// 创建公会成员表
		gameDBA.createTable("create table guild_member("
				+ "id bigint unsigned not null,"
				+ "guild_id int unsigned not null,"
				+ "post tinyint unsigned not null,"
				+ "rec_t bigint unsigned not null,"
				+ "guild_exp int unsigned not null,"
				+ "primary key(id))", "guild_member");
		int guild_exp = 0;
		sql_insert_guild_member = "insert into guild_member value(%d,%d,%d,%d"
				+ String.format(",%d)", guild_exp);
	}
}
