package server;

import java.io.FileReader;
import java.util.Properties;

import data.global.GloTmpVal;
import server.connect.GatewayConnection;

public final class LogicServer {
	
	public static void start() throws Exception {
		Properties properties = new Properties();
		FileReader reader = new FileReader("config/config.properties");
		properties.load(reader);
		reader.close();
		String ip = properties.getProperty("GATEWAY_IP");
		int port = Integer.valueOf(properties.getProperty("GATEWAY_PORT"));
		GloTmpVal.tmpI = Integer.valueOf(properties.getProperty("SERVER_ID"));
		properties.clear();
		
		GatewayConnection.startup(ip, port);
		
		System.out.println("逻辑服务器启动。");
	}
}
