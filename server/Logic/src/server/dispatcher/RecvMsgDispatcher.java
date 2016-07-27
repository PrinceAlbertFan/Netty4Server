package server.dispatcher;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import server.dispatcher.handler.*;
import tool.UnpooledBufUtil;

public final class RecvMsgDispatcher {
	
	private static final short RECV_MSG_MAX = 128;
	public static final short MSG_MAX_IDX = RECV_MSG_MAX - 1;
	
	private static final BlockingQueue<ByteBuf> recvBufQue = new LinkedBlockingQueue<ByteBuf>(1024);
	
	// 根据游戏设计决定长度，暂定为128
	private static final GameHandler[] arrH = new GameHandler[RECV_MSG_MAX];
	
	// 以下静态常驻内存，省去函数内部多字节引用的压栈和出栈销毁开销
	private static ByteBuf _recvBuf;
	private static int _socket;
	
	/**
	 * 捕获新的消息，方法中调用(尽量不使用会阻塞的put)
	 **/
	public final static void captureMsg(ByteBuf byteBuf) {
		if (!recvBufQue.offer(byteBuf)) {
			System.out.println("recvBufQue长度不足，执行阻塞添加");
			try {
				recvBufQue.put(byteBuf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 此函数在主循环调用，因此队列不用take(阻塞)，
	 * 没有消息处理，则返回false
	 **/
	public static boolean decodeNextMsg() {
		if ((_recvBuf = recvBufQue.poll()) == null) {
			return false;
		}
		_socket = _recvBuf.readInt();
		//System.out.println("收到code:" + _recvBuf.getShort(_recvBuf.readerIndex()));
		arrH[_recvBuf.readShort()].handle(_recvBuf, _socket);
		UnpooledBufUtil.reclaimBuf(_recvBuf);
		return true;
	}
	
	/**
	 * 以下数组下标代表opcode，请根据顺序排列，相当于外部逻辑模块的ServerType。
	 * */
	public static void init() {
		arrH[0] = new LoginH();
		arrH[1] = new LogoutH();
		arrH[2] = new EnterGameH();
		arrH[3] = new InitDataH();
		arrH[4] = new PvEStartH();
		arrH[5] = new BattleResultH();
		arrH[6] = new PvEChapterH();
		arrH[7] = new GuideH();
		arrH[8] = new AddGodH();
		arrH[9] = new LotResH();
		arrH[10] = new LotInitH();
		arrH[11] = new EquipH();
		arrH[12] = new UseResH();
		arrH[13] = new TaskListH();
		arrH[14] = new TaskRwdH();
		arrH[15] = new UpStarH();
		arrH[16] = new EquipAllH();
		arrH[17] = new UpStepH();
		arrH[18] = new EnEquipmentH();
		arrH[19] = new UpSkillH();
		arrH[20] = new PartnSpH();
		arrH[21] = new GodUpStarH();
		arrH[22] = new GodUpgradeH();
		arrH[23] = new SysnH();
		arrH[24] = new SellH();
		arrH[25] = new ApplyResetInfoH();
		arrH[26] = new BuyPowerH();
		arrH[27] = new BuyPartnSpH();
		arrH[31] = new SetIconH();
		arrH[32] = new SetNameH();
		arrH[33] = new SysnPartnH();
		arrH[34] = new ApplyPartnPosH();
		arrH[35] = new SetPartnPosH();
		arrH[36] = new MailRewardH();
		arrH[37] = new ResInfoH();
		
		arrH[MSG_MAX_IDX] = new LoadH();
	}
}
