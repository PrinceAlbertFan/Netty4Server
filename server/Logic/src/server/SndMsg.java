package server;

import server.message.*;
import tool.SendBuf;

/**
 * 发送消息的工厂
 * @author Albert Fan
 * */
public final class SndMsg {
	
	/**通知网关登出的包*/
	public static final LogoutMsg logout = new LogoutMsg();
	
	public static final LoginMsg login = new LoginMsg(0, 12);
	
	public static final SendBuf enterGame = new SendBuf(2, 12);
	
	public static final InitDataMsg initData = new InitDataMsg(3, 4096);
	
	public static final PvEStartMsg pveStart = new PvEStartMsg(4, 256);
	
	public static final SendBuf pveResult = new SendBuf(5, 512);
	
	public static final SendBuf pveChapter = new SendBuf(6, 256);
	
	public static final SendBuf setGuide = new SendBuf(7, 8);
	
	public static final SendBuf addGod = new SendBuf(8, 5);
	
	public static final SendBuf lotRes = new SendBuf(9, 256);
	
	public static final SendBuf lotInit = new SendBuf(10, 64);
	
	public static final SendBuf equip = new SendBuf(11, 141);
	
	public static final SendBuf useRes = new SendBuf(12, 128);
	
	public static final SendBuf taskList = new SendBuf(13, 512);
	
	public static final SendBuf taskRwd = new SendBuf(14, 512);
	
	public static final SendBuf upStar = new SendBuf(15, 160);
	
	public static final SendBuf equipAll = new SendBuf(16, 256);
	
	public static final SendBuf upStep = new SendBuf(17, 128);
	
	public static final SendBuf enEquipment = new SendBuf(18, 256);
	
	public static final SendBuf upSkill = new SendBuf(19, 256);
	
	public static final SendBuf partnSp = new SendBuf(20, 12);
	
	public static final SendBuf godUpStar = new SendBuf(21, 64);
	
	public static final SendBuf godUpgrade = new SendBuf(22, 64);
	
	public static final SendBuf sysn = new SendBuf(23, 80);
	
	public static final SendBuf sell = new SendBuf(24, 24);
	
	public static final SendBuf resetInfo = new SendBuf(25, 20);
	
	public static final SendBuf buyPower = new SendBuf(26, 28);
	
	public static final SendBuf buyPartnSp = new SendBuf(27, 20);
	
	public static final SendBuf setIcon = new SendBuf(31, 8);
	
	public static final SendBuf setName = new SendBuf(32, 64);
	
	public static final SendBuf sysnPartn = new SendBuf(33, 80);
	
	public static final SendBuf reqPartnPos = new SendBuf(34, 32);
	
	public static final SendBuf setPartnPos = new SendBuf(35, 8);
	
	public static final SendBuf resInfo = new SendBuf(37, 12);
}
