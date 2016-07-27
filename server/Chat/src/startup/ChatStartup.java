package startup;

import server.ChatServer;

public final class ChatStartup {
	
	public static void main(String[] args) throws Exception {
		
		ChatServer.start();
	}
}
