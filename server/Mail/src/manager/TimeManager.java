package manager;

import java.util.Date;

import data.PlyMail;

public final class TimeManager {
	
	private static final int DUE_TIME = 86400000;
	
	private static final byte GAME_LOOP_SLEEP_MILLISECONDS = 40;
	private static final byte GAME_LOOP_SLEEP_MILLISECONDS_DOUBLE = 80;
	
	private static long lastTimestamp, curTimestamp;
	
	private static long time;
	/**
	 * 全局时间戳
	 * */
	public static int dt;
	
	public final static long getTime() { return time; }
	public final static long getDueTime() { return time + DUE_TIME; }
	
	public static void onUpdate() throws Exception {
		
		curTimestamp = System.currentTimeMillis();
		dt = (int) (curTimestamp - lastTimestamp);
		lastTimestamp = curTimestamp;
		
		time += dt;
		PlyMail.mainUpdate();
		
		if ((dt = GAME_LOOP_SLEEP_MILLISECONDS_DOUBLE - dt) > GAME_LOOP_SLEEP_MILLISECONDS) {
			dt = GAME_LOOP_SLEEP_MILLISECONDS;
		} else if (dt < 1) {
			dt = 1;
		}
		Thread.sleep(dt);
	}
	
	public static void init() throws Exception {
		time = new Date().getTime();
		lastTimestamp = System.currentTimeMillis();
		Thread.sleep(GAME_LOOP_SLEEP_MILLISECONDS);
	}
}
