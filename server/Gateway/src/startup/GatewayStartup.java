package startup;

import server.GatewayServer;
import manager.MessageManager;
import manager.TimeManager;

public final class GatewayStartup {
	
	public static void main(String[] args) throws Exception {
		
		TimeManager.init();
		MessageManager.init();
		GatewayServer.start();
		
		for (;;) {
			TimeManager.onUpdate();
		}
	}
}
