package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 这个类是数据库中心的连接与操作，只能读
 * @Author:Albert Fan
 * */
public class DBReadAdmin {
	/**连接对象*/
	private Connection con;
	
	/**用于读*/
	private Statement readStmt;
	
	private String mIp;
	private String mPort;
	private String mDbName;
	private String mUserName;
	private String mPassword;
	
	/**
	 * 查询：
	 * 返回数据集，使用完毕需要清理
	 * */
	public ResultSet executeQuery(String sql) {
		try {
			return readStmt.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
		return null;
	}
	
	/**
	 * 根据条件获得某个列数值long类型数据
	 * */
	public long executeQueryLongFieldValue(String sql) {
		try {
			ResultSet res = readStmt.executeQuery(sql);
			long value = res.next() ? res.getLong(1) : 0;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值int类型数据
	 * */
	public int executeQueryIntFieldValue(String sql) {
		try {
			ResultSet res = readStmt.executeQuery(sql);
			int value = res.next() ? res.getInt(1) : 0;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值short类型数据
	 * */
	public short executeQueryShortFieldValue(String sql) {
		try {
			ResultSet res = readStmt.executeQuery(sql);
			short value = res.next() ? res.getShort(1) : 0;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值byte类型数据
	 * */
	public byte executeQueryByteFieldValue(String sql) {
		try {
			ResultSet res = readStmt.executeQuery(sql);
			byte value = res.next() ? res.getByte(1) : 0;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值String类型数据
	 * */
	public String executeQueryStringFieldValue(String sql) {
		try {
			ResultSet res = readStmt.executeQuery(sql);
			String value = res.next() ? res.getString(1) : null;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe err[sql]:" + sql);
		}
		return null;
	}
	
	/**
	 * 用于快要连接超时前激活一下Sql语句
	 * */
	public void activateStatement() {
		try {
			readStmt.executeQuery("select 1").close();
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
//	        	readStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
	        	readStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
				readStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
				readStmt.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			readStmt = null;
			con = null;
		}
	}
	
	/**
	 * 获得某个列long类型数据的最大值
	 * */
	public long getLongFieldMaxValue(String tableName, String fieldName) {
		try {
			ResultSet res = readStmt.executeQuery("select max(" + fieldName +") from " + tableName);
			long value = res.next() ? res.getLong(1) : 0;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 获得某个列int类型数据的最大值
	 * */
	public int getIntFieldMaxValue(String tableName, String fieldName) {
		try {
			ResultSet res = readStmt.executeQuery("select max(" + fieldName +") from " + tableName);
			int value = res.next() ? res.getInt(1) : 0;
			res.close();
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
