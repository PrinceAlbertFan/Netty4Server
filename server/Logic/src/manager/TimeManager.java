package manager;

import java.text.SimpleDateFormat;
import java.util.Date;

import obj.Player;
import server.dispatcher.RecvMsgDispatcher;

public final class TimeManager {
	
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Date date = new Date();
	
	private static final int OUR_GMT_MILLISECONDS = 28800000; // Chinese
	
	private static final byte GAME_LOOP_SLEEP_MILLISECONDS = 40;
	private static final byte GAME_LOOP_SLEEP_MILLISECONDS_DOUBLE = 80;
	
	private static long lastTimestamp, curTimestamp;
	private static int msgCount, calMsgMax;
	
	//************************************************************************
	/* 全局时间                                                          									*
	/************************************************************************/
	private static long passedGmtMsecOfCurDay; // 当前天所经历的格林威治毫秒数
	private static int passedGmtSecOfCurDay; // 当前天所经历的格林威治秒数
	private static int passedMsecOfCurMin; // 当前分钟所经历的毫秒数(0~60000)
	private static short passedMinOfCurDay; // 当天所经历的分钟数(0~1440)
	private static short curYear; // 当前的年(1900~???)
	private static byte curMonth; // 当前年的月(1~12)
	private static byte curDate; // 当前月的日期(1~31)
	private static byte curDay; // 当前的星期(1~7)
	
	/**
	 * 全局时间戳
	 * */
	public static int dt;
	
	public final static int getSurplusMsecOfCurDay() {
		return 86400 - passedMinOfCurDay * 60;
	}
	
	public final static long getPassedGmtMsecOfCurDay() {
		return passedGmtMsecOfCurDay;
	}
	
	public final static long getMsecTimestamp() { // 返回的本地时间(毫秒)
		return passedGmtMsecOfCurDay + passedMinOfCurDay * 60000 + passedMsecOfCurMin;
	}
	
	public final static long getGmtMsecTimestamp() { // 返回的格林威治时间(毫秒)
		//return getMsecTimestamp() - OUR_GMT_MILLISECONDS;
		return passedGmtMsecOfCurDay + passedMinOfCurDay * 60000 + passedMsecOfCurMin - OUR_GMT_MILLISECONDS;
	}
	
	public final static String getLocalTimeString() { //2016-01-22 18:06:11
		date.setTime(getGmtMsecTimestamp());
		return dateFmt.format(date);
	}
	
	public static void onUpdate() throws Exception {
		curTimestamp = System.currentTimeMillis();
		dt = (int) (curTimestamp - lastTimestamp);
		lastTimestamp = curTimestamp;
		
		if ((passedMsecOfCurMin += dt) > 60000) {
			passedMsecOfCurMin -= 60000;
			if (passedMinOfCurDay != 1439) {
				++passedMinOfCurDay;
			} else {
				passedMinOfCurDay = 0;
			}
			//System.out.println("当前：" + passedMinOfCurDay / 60 + "时" + passedMinOfCurDay % 60 + "分");
			switch (passedMinOfCurDay) {
			case 0://AM:00:00
				passedGmtMsecOfCurDay += 86400000;//1440*60*1000
				passedGmtSecOfCurDay += 86400;//1440*60
				if (curDay != 7) {
					++curDay;
				} else {
					curDay = 1;
				}
				passDayRefresh();
				if (++curDate > 28) {
					switch (curMonth) {
					case 4:case 6:case 9:case 11:
						if (curDate == 31) {
							curDate = 1;
							++curMonth;
							passMonthRefresh();
						}
						break;
					case 2:
						curDate = (byte) new Date(new Date().getTime() + 3600000).getDate();
						if (curDate == 1) {
							++curMonth;
							passMonthRefresh();
						}
						break;
					case 12:
						if (curDate == 32) {
							curDate = 1;
							curMonth = 1;
							++curYear;
							passMonthRefresh();
						}
						break;
					default:
						if (curDate == 32) {
							curDate = 1;
							++curMonth;
							passMonthRefresh();
						}
						break;
					}
				}
				break;
			case 210:// AM:3:30
				break ;
			case 300:// AM:5:00
				//passDayRefresh(); // 凌晨5点刷新当日数据
				break;
			case 720:// AM:12:00
				break;
			case 840:// AM:14:00
				break;
			case 690://AM:11:30
				//上午某某活动开启
				break;/////////////////////////测试开始////////////////////////////
			case 1050://PM:17:30
				//下午某某活动开启
				break;
			case 1080://PM:18:00
				break;
			case 1200://PM:20:00
				break;
			case 1260:// PM:21:00
				break;
			case 1380:// PM:23:00
				break;
			default:
				break;
			}
//			if (checkOnlineNum != 4) {
//				++checkOnlineNum;
//			} else {
//				checkOnlineNum = 0;
//				// 记录在线人数表
//				dbWorker.dbUpdate(String.format("insert into online value(default,%d,'%s')", GameSession.getConnectSize(), getLocalTimeString()));
//			}
		}
		
		msgCount = 0;
		calMsgMax = Player.getPlayerCount();
		do {
			if (!RecvMsgDispatcher.decodeNextMsg()) {
				break;
			}
		} while (++msgCount < calMsgMax);
		Player.mainUpdate();
		
		if ((dt = GAME_LOOP_SLEEP_MILLISECONDS_DOUBLE - dt) > GAME_LOOP_SLEEP_MILLISECONDS) {
			dt = GAME_LOOP_SLEEP_MILLISECONDS;
		} else if (dt < 1) {
			dt = 1;
		}
		Thread.sleep(dt);
	}
	
	private static void passDayRefresh() {
		Player.dayRefresh();
//		dbWorker.dbUpdatePromptly("update player set subsist_idx=0,subsist_state=0,sign_state=0");
	}
	
	private static void passMonthRefresh() {
		// 刷新签到
//		for (GameSession session : GameServer.getSockMap().values()) {
//			session.getPlayer().setSignIndex((byte) 0);
//		}
//		dbWorker.dbUpdatePromptly("update player set sign_idx=0");
	}
	
	public static void init() throws Exception {
		// 时间初始化
		Date date = new Date();
		passedGmtMsecOfCurDay = date.getTime();
		passedGmtSecOfCurDay = (int) (passedGmtMsecOfCurDay / 1000);
		passedMsecOfCurMin = (int) (passedGmtMsecOfCurDay % 60000);
		passedGmtSecOfCurDay = passedGmtSecOfCurDay / 60 + 8 * 60;//Chinese Gmt:8 * 60
		passedMinOfCurDay = (short) (passedGmtSecOfCurDay % 1440);
		passedGmtSecOfCurDay = (passedGmtSecOfCurDay - passedMinOfCurDay) * 60;
		passedGmtMsecOfCurDay = (long) passedGmtSecOfCurDay * 1000;
		curYear = (short) (date.getYear() + 1900);
		curMonth = (byte) (date.getMonth() + 1);
		curDate = (byte) date.getDate();
		curDay = (byte) date.getDay();
		// 时间戳初始化
		lastTimestamp = System.currentTimeMillis();
		Thread.sleep(GAME_LOOP_SLEEP_MILLISECONDS);
	}
}
