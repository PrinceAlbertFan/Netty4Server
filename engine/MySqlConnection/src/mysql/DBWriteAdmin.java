package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 这个类是数据库中心的连接与操作，只能写
 * @Author:Albert Fan
 * */
public class DBWriteAdmin {
	/**连接对象*/
	private Connection con;
	
	/**用于写*/
	private Statement writeStmt;
	
	private String mIp;
	private String mPort;
	private String mDbName;
	private String mUserName;
	private String mPassword;
	
	
	/**
	 * 添加、修改、删除
	 * */
	public void executeUpdate(String sql) {
		try {
			writeStmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
	}
	
	/**
	 * 获得临时查询对象，使用完毕需要释放
	 * 适用于其他多线程
	 * */
	public Statement getNewStatement() {
		try {
			return con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("getNewStatement() failed...");
		}
		return null;
	}
	
	/**
	 * 用于快要连接超时前激活一下Sql语句
	 * */
	public void activateStatement() {
		try {
			writeStmt.executeQuery("select 1").close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("activateStatement() failed...");
		}	
	}
	
//	/**
//	 * 重连数据库
//	 * */
//	public boolean reconnect() {
//		if (con != null) {
//			return true;
//		}
//		System.out.println("Reconnect for DB timeout...");
//		try {
//			Class.forName("com.mysql.jdbc.Driver"); // 加载驱动程序
//	        con = DriverManager.getConnection("jdbc:mysql://" + mIp + ":" + mPort + "/" + mDbName, mUserName, mPassword); // 连接MySQL数据库
//	        if (!con.isClosed()) {
//	        	writeStmt = con.createStatement();
//	        	System.out.println("Succeeded connecting to the Database:" + mDbName);
//			} else {
//				System.out.println("Failed connecting to the Database:" + mDbName);
//				return false;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
	
	/**
	 * 连接数据库,如果没有该DB,则自动创建DB
	 * */
	public boolean connect(String ip, String port, String dbName, String userName, String password) {
		if (con != null) {
			return true;
		}
		try {
			Class.forName("com.mysql.jdbc.Driver"); // 加载驱动程序
	        con = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName, userName, password); // 连接MySQL数据库
	        if (!con.isClosed()) {
	        	writeStmt = con.createStatement();
	        	System.out.println("Succeeded connecting to the Database:" + dbName);
			} else {
				System.out.println("Failed connecting to the Database:" + dbName);
				return false;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Failed connecting to the Database, create new Database:" + dbName);
			// 没有该DB,要创建该DB
			try {
				con = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port, userName, password);
				//con.createStatement().executeUpdate("create database " + dbName + " default charset utf8 collate utf8_general_ci");
				con.createStatement().executeUpdate("create database " + dbName + " default character set utf8 collate utf8_general_ci");
				con = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName, userName, password);
				writeStmt = con.createStatement();
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
		}
		mIp = ip;
		mPort = port;
		mDbName = dbName;
		mUserName = userName;
		mPassword = password;
		return true;
	}
	
	/**
	 * 对象不再需要的时候，请显示调用
	 * */
	public void close() {
		if (con != null) {
			try {
				writeStmt.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			writeStmt = null;
			con = null;
		}
	}
	
	/**
	 * 创建DB中表
	 * */
	public void createTable(String sql, String tbName) {
		try {
			Statement readStmt = con.createStatement();
			ResultSet rs = readStmt.executeQuery(String.format("select count(*) from information_schema.tables where table_schema='%s' and table_name='%s'", mDbName, tbName));
			rs.next();
			if (rs.getInt(1) == 0) { // 没有表，则创建
				writeStmt.executeUpdate(sql);
				System.out.println("Failed finding from the Database, create new Table:" + tbName);
			}
			rs.close();
			readStmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
