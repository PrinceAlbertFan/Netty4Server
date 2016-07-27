package manager;

import server.channel.ClientChannelUtil;
import server.channel.ServerChannelUtil;

public final class TimeManager {
	
	private static final byte GAME_LOOP_SLEEP_MILLISECONDS = 40;
	private static final byte GAME_LOOP_SLEEP_MILLISECONDS_DOUBLE = 80;
	
	private static long lastTimestamp, curTimestamp;
	
	/**
	 * 全局时间戳
	 * */
	public static int dt;
	
	
	public static void onUpdate() throws Exception {
		
		curTimestamp = System.currentTimeMillis();
		dt = (int) (curTimestamp - lastTimestamp);
		lastTimestamp = curTimestamp;
		
		//ServerChannelUtil.onUpdate();
		//ClientChannelUtil.onUpdate();
		
		if ((dt = GAME_LOOP_SLEEP_MILLISECONDS_DOUBLE - dt) > GAME_LOOP_SLEEP_MILLISECONDS) {
			dt = GAME_LOOP_SLEEP_MILLISECONDS;
		} else if (dt < 1) {
			dt = 1;
		}
		Thread.sleep(dt);
	}
	
	public static void init() throws Exception {
		lastTimestamp = System.currentTimeMillis();
		Thread.sleep(GAME_LOOP_SLEEP_MILLISECONDS);
	}
}
