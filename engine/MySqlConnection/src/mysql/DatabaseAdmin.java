package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 这个类是数据库中心的连接与操作，进行读写分离
 * @Author:Albert Fan
 * */
public class DatabaseAdmin {
	
//	/**
//	 * 发生异常时，用于判定是否连接超时还是错误的SQL语句
//	 * */
//	private static boolean bException;
	
	/**连接对象*/
	private Connection con;
	
	/**用于读*/
	private Statement readStmt;
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
		if (reconnect()) {
			try {
				writeStmt.executeUpdate(sql);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
	}
	
	/**
	 * 查询：
	 * 返回数据集，使用完毕需要清理
	 * */
	public ResultSet executeQuery(String sql) {
		if (reconnect()) {
			try {
				return readStmt.executeQuery(sql);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
		return null;
	}
	
	/**
	 * 根据条件获得某个列数值long类型数据
	 * */
	public long executeQueryLongFieldValue(String sql) {
		if (reconnect()) {
			try {
				ResultSet res = readStmt.executeQuery(sql);
				long value = res.next() ? res.getLong(1) : 0;
				res.close();
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值int类型数据
	 * */
	public int executeQueryIntFieldValue(String sql) {
		if (reconnect()) {
			try {
				ResultSet res = readStmt.executeQuery(sql);
				int value = res.next() ? res.getInt(1) : 0;
				res.close();
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值short类型数据
	 * */
	public short executeQueryShortFieldValue(String sql) {
		if (reconnect()) {
			try {
				ResultSet res = readStmt.executeQuery(sql);
				short value = res.next() ? res.getShort(1) : 0;
				res.close();
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值byte类型数据
	 * */
	public byte executeQueryByteFieldValue(String sql) {
		if (reconnect()) {
			try {
				ResultSet res = readStmt.executeQuery(sql);
				byte value = res.next() ? res.getByte(1) : 0;
				res.close();
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
		return 0;
	}
	
	/**
	 * 根据条件获得某个列数值String类型数据
	 * */
	public String executeQueryStringFieldValue(String sql) {
		if (reconnect()) {
			try {
				ResultSet res = readStmt.executeQuery(sql);
				String value = res.next() ? res.getString(1) : null;
				res.close();
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Maybe err[sql]:" + sql);
			}
		} else {
			System.out.println("Reconnect DB failed...[sql]:" + sql);
		}
		return null;
	}
	
	/**
	 * 获得临时查询对象，使用完毕需要释放
	 * 适用于其他多线程
	 * */
	public Statement getNewStatement() {
		if (reconnect()) {
			try {
				return con.createStatement();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("getNewStatement() failed...");
			}
		} else {
			System.out.println("getNewStatement() Reconnect DB failed...");
		}
		return null;
	}
	
	/**
	 * 重连数据库
	 * */
	public boolean reconnect() {
		if (con != null) {
			return true;
		}
		System.out.println("Reconnect for DB timeout...");
		try {
			Class.forName("com.mysql.jdbc.Driver"); // 加载驱动程序
	        con = DriverManager.getConnection("jdbc:mysql://" + mIp + ":" + mPort + "/" + mDbName, mUserName, mPassword); // 连接MySQL数据库
	        if (!con.isClosed()) {
	        	readStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        	writeStmt = con.createStatement();
	        	System.out.println("Succeeded connecting to the Database:" + mDbName);
			} else {
				System.out.println("Failed connecting to the Database:" + mDbName);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
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
				readStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
				readStmt.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			writeStmt = null;
			readStmt = null;
			con = null;
		}
	}
	
	/**
	 * 创建DB中表
	 * */
	public void createTable(String sql, String tbName) {
		try {
			ResultSet rs = readStmt.executeQuery(String.format("select count(*) from information_schema.tables where table_schema='%s' and table_name='%s'", mDbName, tbName));
			rs.next();
			if (rs.getInt(1) == 0) { // 没有表，则创建
				writeStmt.executeUpdate(sql);
				System.out.println("Failed finding from the Database, create new Table:" + tbName);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
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
