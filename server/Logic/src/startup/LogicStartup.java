package startup;

import obj.Player;
import server.LogicServer;
import server.dispatcher.RecvMsgDispatcher;
import load.XmlLoader;
import manager.TimeManager;
import db.DbCenter;

public final class LogicStartup {
	
	private static volatile boolean _run = true;
	
	public final static void exit() {
		_run = false;
	}
	
	public static void main(String[] args) throws Exception {
		
		XmlLoader.load();
		DbCenter.start();
		TimeManager.init();
		LogicServer.start();
		RecvMsgDispatcher.init();
		
		while (_run) {
			TimeManager.onUpdate();
		}
		Player.close();
		DbCenter.close();
		System.exit(0);
	}
}
