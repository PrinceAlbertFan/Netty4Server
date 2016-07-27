package server;

import java.io.FileReader;
import java.util.Properties;

import server.socket.StreamFromClientSocket;
import server.socket.StreamFromServerSocket;

public final class GatewayServer {
	
	public static void start() throws Exception {
		
		Properties properties = new Properties();
		FileReader reader = new FileReader("config/config.properties");
		properties.load(reader);
		reader.close();
		int port1 = Integer.parseInt(properties.getProperty("PORT_OF_CLIENT_CONNECTION"));
		int port2 = Integer.parseInt(properties.getProperty("PORT_OF_SERVER_CONNECTION"));
		properties.clear();
		
		new StreamFromServerSocket().startup(port2);
		new StreamFromClientSocket().startup(port1);
		
		System.out.println("网关服务器启动。");
	}
}
