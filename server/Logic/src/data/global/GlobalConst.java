package data.global;

public final class GlobalConst {
	
	/**玩家初始-金币*/
	public static int init_coin;
	/**玩家初始-钻石*/
	public static int init_credit;
	/**玩家初始-体力*/
	public static int init_power;
	/**体力回复时间（每1点需要时间）*/
	public static int power_recover_time;
	/**金币抽奖1次-价格*/
	public static int lottery_coin_1_price;
	/**钻石抽奖1次-价格*/
	public static int lottery_credit_1_price;
	/**金币抽奖10次-价格*/
	public static int lottery_coin_10_price;
	/**钻石抽奖10次-价格*/
	public static int lottery_credit_10_price;
	/**金币抽奖每次-赠送物品ID*/
	public static int lottery_coin_item_id;
	/**钻石抽奖每次-赠送物品ID*/
	public static int lottery_credit_item_id;
	/**金币免费抽奖倒计时*/
	public static int lottery_coin_cutdown;
	/**英雄技能点，每点恢复时间*/
	public static int hero_skill_CD;
	/**技能点上限*/
	public static short hero_skill_max;
	/**英雄技能-等级初始系数*/
	public static final short[] arr_skill_lv = new short[4];
	/**金币抽奖每日免费次数*/
	public static byte lottery_coin_free_times = 5;
	/**钻石抽奖每日免费次数*/
	public static byte lottery_credit_free_times = 1;
}
