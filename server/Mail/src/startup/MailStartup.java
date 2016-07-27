package startup;

import server.MailServer;
import manager.TimeManager;
import data.PlyMail;
import db.DbCenter;

public final class MailStartup {
	
	private static volatile boolean _run = true;
	
	public final static void exit() {
		_run = false;
	}
	
	public static void main(String[] args) throws Exception {
		
		DbCenter.start();
		TimeManager.init();
		MailServer.start();
		
		while (_run) {
			TimeManager.onUpdate();
		}
		
		PlyMail.close();
		DbCenter.close();
		System.exit(0);
	}
}
