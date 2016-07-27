package server;

import global.GlobalTemporaryVariable;

import java.io.FileReader;
import java.util.Properties;

import server.connect.GatewayConnection;

public final class ChatServer {
	
	public static void start() throws Exception {
		
		Properties properties = new Properties();
		FileReader reader = new FileReader("config/config.properties");
		properties.load(reader);
		reader.close();
		String ip = properties.getProperty("GATEWAY_IP");
		int port = Integer.valueOf(properties.getProperty("GATEWAY_PORT"));
		GlobalTemporaryVariable.tempInt = Integer.valueOf(properties.getProperty("SERVER_ID"));
		properties.clear();
		
		new GatewayConnection().startup(ip, port);
		
		System.out.println("聊天服务器启动。");
	}
}
