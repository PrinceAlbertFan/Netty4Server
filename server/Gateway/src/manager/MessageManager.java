package manager;

import global.GloConst;

public final class MessageManager {
	
	public static final short SERVER_NUM = 7; // 服务器数量
	
	//public static final short SERVER_ID_ERROR = 0;
	
	private static final short MSG_COUNT = 200; // 消息种类数量
	
	private static final short[] serverIdRelateMsgOpcode;
	private static final short[] minMsgSizeRelateMsgOpcode;
	private static final short[] maxMsgSizeRelateMsgOpcode;
	
	static {
		serverIdRelateMsgOpcode = new short[MSG_COUNT];
		minMsgSizeRelateMsgOpcode = new short[MSG_COUNT];
		maxMsgSizeRelateMsgOpcode = new short[MSG_COUNT];
	}
	
	/**
	 * 返回消息所流向的服务器号
	 * */
	public final static short getServerId(short msgOpcode, short msgSize) {
//		return msgOpcode >= 0 && msgOpcode < MSG_COUNT && msgSize >= minMsgSizeRelateMsgOpcode[msgOpcode] && msgSize <= maxMsgSizeRelateMsgOpcode[msgOpcode]
//				? serverIdRelateMsgOpcode[msgOpcode] : 0;
		if (msgOpcode >= 0 && msgOpcode < MSG_COUNT && msgSize >= minMsgSizeRelateMsgOpcode[msgOpcode] && msgSize <= maxMsgSizeRelateMsgOpcode[msgOpcode]) {
			//System.out.println("收到正确消息：" + msgOpcode + "，长度：" + msgSize);
			return serverIdRelateMsgOpcode[msgOpcode];
		}
		System.out.println("收到错误消息，code：" + msgOpcode + "，size：" + msgSize);
		return 0;
	}
	
	private final static void _registMsg(int opcode, int minMsgSize, int maxMsgSize, short servId) {
		serverIdRelateMsgOpcode[opcode] = servId;
		minMsgSizeRelateMsgOpcode[opcode] = (short) minMsgSize;
		maxMsgSizeRelateMsgOpcode[opcode] = (short) maxMsgSize;
	}
	
	public static void init() {
		_registMsg(0, 10, 62, GloConst.LOGIN_SERVER_ID); // 登录
		_registMsg(2, 4, 4, GloConst.MAIN_SERVER_ID); // 进入游戏
		_registMsg(3, 4, 4, GloConst.MAIN_SERVER_ID); // 初始数据
		_registMsg(4, 12, 36, GloConst.MAIN_SERVER_ID); // 申请进入单人PVE副本
		_registMsg(5, 20, 128, GloConst.MAIN_SERVER_ID); // 战斗结算
		_registMsg(6, 12, 12, GloConst.MAIN_SERVER_ID); // 申请PVE关卡信息
		_registMsg(7, 8, 12, GloConst.MAIN_SERVER_ID); // 引导
		_registMsg(8, 8, 8, GloConst.MAIN_SERVER_ID); // 添加邪神
		_registMsg(9, 8, 8, GloConst.MAIN_SERVER_ID); // 抽卡
		_registMsg(10, 4, 4, GloConst.MAIN_SERVER_ID); // 抽卡初始化信息
		_registMsg(11, 12, 12, GloConst.MAIN_SERVER_ID); // 穿装备
		_registMsg(12, 16, 16, GloConst.MAIN_SERVER_ID); // 使用道具
		_registMsg(13, 4, 4, GloConst.MAIN_SERVER_ID); // 任务列表
		_registMsg(14, 8, 8, GloConst.MAIN_SERVER_ID); // 领取任务奖励
		_registMsg(15, 8, 8, GloConst.MAIN_SERVER_ID); // 伙伴升星
		_registMsg(16, 8, 8, GloConst.MAIN_SERVER_ID); // 一键装备
		_registMsg(17, 8, 8, GloConst.MAIN_SERVER_ID); // 伙伴升阶
		_registMsg(18, 12, 12, GloConst.MAIN_SERVER_ID); // 强化
		_registMsg(19, 12, 12, GloConst.MAIN_SERVER_ID); // 英雄技能升级
		_registMsg(20, 4, 4, GloConst.MAIN_SERVER_ID); // 请求英雄技能点和刷新剩余秒数
		_registMsg(21, 8, 8, GloConst.MAIN_SERVER_ID); // 邪神升星
		_registMsg(22, 8, 8, GloConst.MAIN_SERVER_ID); // 邪神升级
		_registMsg(23, 8, 8, GloConst.MAIN_SERVER_ID); // 装备合成
		_registMsg(24, 12, 12, GloConst.MAIN_SERVER_ID); // 卖物品
		_registMsg(25, 8, 8, GloConst.MAIN_SERVER_ID); // 请求购买重置信息
		_registMsg(26, 4, 4, GloConst.MAIN_SERVER_ID); // 购买体力
		_registMsg(27, 4, 4, GloConst.MAIN_SERVER_ID); // 购买技能点
		_registMsg(28, 7, 256, GloConst.CHAT_SERVER_ID); // 世界聊
		_registMsg(29, 11, 256, GloConst.CHAT_SERVER_ID); // 私聊
		_registMsg(30, 4, 4, GloConst.CHAT_SERVER_ID); // 公会聊
		_registMsg(31, 8, 8, GloConst.MAIN_SERVER_ID); // 修改头像
		_registMsg(32, 7, 64, GloConst.MAIN_SERVER_ID); // 修改昵称
		_registMsg(33, 8, 8, GloConst.MAIN_SERVER_ID); // 合成英雄
		_registMsg(34, 8, 8, GloConst.MAIN_SERVER_ID); // 请求出战英雄列表
		_registMsg(35, 16, 32, GloConst.MAIN_SERVER_ID); // 设置出战英雄
		_registMsg(36, 12, 12, GloConst.MAIL_SERVER_ID); // 邮件领取
		_registMsg(37, 8, 8, GloConst.MAIL_SERVER_ID); // 请求资源数量
	}
}
