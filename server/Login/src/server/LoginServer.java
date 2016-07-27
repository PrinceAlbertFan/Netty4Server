package server;

import java.io.FileReader;
import java.util.Properties;

import server.connect.GatewayConnection;

public final class LoginServer {
	
	public static void start() throws Exception {
		Properties properties = new Properties();
		FileReader reader = new FileReader("config/config.properties");
		properties.load(reader);
		reader.close();
		String ip = properties.getProperty("GATEWAY_IP");
		int port = Integer.valueOf(properties.getProperty("GATEWAY_PORT"));
		properties.clear();
		
		GatewayConnection.startup(ip, port);
		
		System.out.println("登陆服务器启动。");
	}
}
