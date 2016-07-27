package server.message;

import obj.Player;
import tool.SendBuf;

public final class InitDataMsg extends SendBuf {
	
	public void send(Player ply) {
		this.reset()
		.writeInt(ply.getResCtrl().getCoin())
		.writeInt(ply.getResCtrl().getPower())
		.writeInt(ply.getResCtrl().getCredit())
		.writeInt(ply.getLv())
		.writeInt(ply.getResCtrl().getExp())
		.writeInt(ply.getVipLv())
		.writeInt(ply.getIcon()) // 头像
		.writeString(ply.getName())
		.writeInt(ply.getResCtrl().getPartnSp()) // skillPoint
		.writeInt(ply.getPartnSpRecoverSurplusSecond()); // skill_point_recover_second
		ply.getPartnCtrl().onceNoticeClient(); // List<HeroInfo> heros;
		ply.getResCtrl().onceNoticeClient(); // List<ItemInfo> items;
		this.writeInt(ply.getFortCtrl().getCurComFort()); // map_id;
		ply.getGodCtrl().onceNoticeClient(); // List<DevilInfo> devils;
		this.writeInt(ply.getResCtrl().getSoul()) // soul
		.writeInt(0) // union_id
		.writeInt(0) // union_job
		.writeString("") // union_name
		.writeInt(0) // arena_coin
		.writeInt(0) // pvp_coin
		.writeInt(0) // union_coin
		.writeInt(0) // expedition_coin
		.writeInt(0) // boss_coin
		.writeString("") // chat_group
		.writeInt(0); // chat_times
		ply.getFortCtrl().onceNoticeClient(); // List<int> map_ids
		ply.getGuideCtrl().onceNoticeClient(); // List<TutorialInfo> tutorials
		this.packAndSend(ply.getSocket());
	}

	public InitDataMsg(int opcode, int dataLen) {
		super(opcode, dataLen);
	}

}
