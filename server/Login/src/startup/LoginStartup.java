package startup;

import db.DbCenter;
import server.LoginServer;

public final class LoginStartup {
	
	public static void main(String[] args) throws Exception {
		
		DbCenter.start();
		LoginServer.start();
	}
}
