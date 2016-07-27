package load;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import data.global.GlobalConst;
import data.xml.*;
import data.xml.Chapter.ChapterT;
import data.xml.Drop.*;
import data.xml.Fort.*;
import data.xml.God.GodT;
import data.xml.Level.*;
import data.xml.Lot.*;
import data.xml.Monster.MonsterT;
import data.xml.Partner.PartnerT;
import data.xml.Resource.*;
import data.xml.Task.TaskReward;
import data.xml.Task.TaskT;

/**
 * 加载游戏配置
 * */
public final class XmlLoader {
	
	private static final String XML_PATH = "xml/";
	
	public static void load() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc;
		NodeList list;
		Element elem;
		Field field, field0, field1, field2, field3, field4, field5, field6;
		int i, max;
		// 加载等级表
		doc = db.parse(new File(XML_PATH + "Level.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		LevelT[] arrLevelT = new LevelT[max + 2];
		PartnLevelT[] arrPartnLevelT = new PartnLevelT[max + 2];
		field0 = PartnLevelT.class.getDeclaredField("exp");			field0.setAccessible(true);
		field1 = LevelT.class.getDeclaredField("exp");				field1.setAccessible(true);
		field2 = LevelT.class.getDeclaredField("powerLimit");		field2.setAccessible(true);
		field3 = LevelT.class.getDeclaredField("partnLvLimit");		field3.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			LevelT levelT = new LevelT();
			PartnLevelT partnLevelT = new PartnLevelT();
			short lv = Short.valueOf(elem.getAttributeNode("zdLv").getValue());
			field0.setInt(partnLevelT, Integer.valueOf(elem.getAttributeNode("heroExp").getValue()));
			field1.setInt(levelT, Integer.valueOf(elem.getAttributeNode("zdExp").getValue()));
			field2.setShort(levelT, Short.valueOf(elem.getAttributeNode("powerLimit").getValue()));
			field3.setShort(levelT, Short.valueOf(elem.getAttributeNode("heroLvLimit").getValue()));
			arrLevelT[lv] = levelT;
			arrPartnLevelT[lv] = partnLevelT;
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field = Level.class.getDeclaredField("arrLevelT");
		field.setAccessible(true);
		field.set(Level.class, arrLevelT);
		field.setAccessible(false);
		field = Level.class.getDeclaredField("arrPartnLevelT");
		field.setAccessible(true);
		field.set(Level.class, arrPartnLevelT);
		field.setAccessible(false);
		doc = db.parse(new File(XML_PATH + "God_UpGrade.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		GodLevelT[] arrGodLevelT = new GodLevelT[max + 2];
		field0 = GodLevelT.class.getDeclaredField("coin");		field0.setAccessible(true);
		field1 = GodLevelT.class.getDeclaredField("soul");		field1.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			GodLevelT lvT = new GodLevelT();
			field0.setInt(lvT, Integer.valueOf(elem.getAttributeNode("coin").getValue()));
			field1.setInt(lvT, Integer.valueOf(elem.getAttributeNode("piece").getValue()));
			arrGodLevelT[Short.valueOf(elem.getAttributeNode("level").getValue())] = lvT;
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field = Level.class.getDeclaredField("arrGodLevelT");
		field.setAccessible(true);
		field.set(Level.class, arrGodLevelT);
		field.setAccessible(false);
		
		// 加载技能等级花费表
		doc = db.parse(new File(XML_PATH + "Hero_Skill_up.xml"));
		list = doc.getElementsByTagName("table");
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			PartnLevelT pt = arrPartnLevelT[Short.valueOf(elem.getAttributeNode("lv").getValue())];
			pt.arrSklUpCost[0] = Integer.valueOf(elem.getAttributeNode("skill1").getValue());
			pt.arrSklUpCost[1] = Integer.valueOf(elem.getAttributeNode("skill2").getValue());
			pt.arrSklUpCost[2] = Integer.valueOf(elem.getAttributeNode("skill3").getValue());
			pt.arrSklUpCost[3] = Integer.valueOf(elem.getAttributeNode("skill4").getValue());
		}

		// 加载资源表
		doc = db.parse(new File(XML_PATH + "item.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Integer, ResourceT> mapResT = new HashMap<Integer, ResourceT>(max);
		field0 = ResourceT.class.getDeclaredField("id");			field0.setAccessible(true);
		field1 = ResourceT.class.getDeclaredField("type");			field1.setAccessible(true);
		field2 = ResourceT.class.getDeclaredField("sellCoin");		field2.setAccessible(true);
		field3 = ResourceT.class.getDeclaredField("useReward");		field3.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			ResourceT resT = new ResourceT();
			field0.setInt(resT, Integer.valueOf(elem.getAttributeNode("id").getValue()));
			field1.setShort(resT, Short.valueOf(elem.getAttributeNode("type").getValue()));
			field2.setInt(resT, Integer.valueOf(elem.getAttributeNode("sellcoin").getValue()));
			String str = elem.getAttributeNode("reward").getValue();
			if (!str.equals("[]")) {
				String[] strs = str.replace("[", "").replace("]", "").split(",");
				UseReward rwd = new UseReward();
				rwd.rewardType = Byte.valueOf(strs[0]);
				rwd.rewardId = Integer.valueOf(strs[1]);
				rwd.rewardNum = Integer.valueOf(strs[2]);
				field3.set(resT, rwd);
			}
			mapResT.put(resT.getId(), resT);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field = Resource.class.getDeclaredField("mapResT");
		field.setAccessible(true);
		field.set(Resource.class, mapResT);
		field.setAccessible(false);
		
		// 加载装备表
		doc = db.parse(new File(XML_PATH + "equipment.xml"));
		list = doc.getElementsByTagName("table");
		field0 = ResourceT.class.getDeclaredField("equipT");	field0.setAccessible(true);
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			ResourceT resT = mapResT.get(Integer.valueOf(elem.getAttributeNode("id").getValue()));
			if (resT != null) {
				EquipT equipT = new EquipT();
				equipT.lvLimit = Short.valueOf(elem.getAttributeNode("lv").getValue());
				field0.set(resT, equipT);
			}
		}
		field0.setAccessible(false);
		
		// 加载装备强化表
		doc = db.parse(new File(XML_PATH + "equipment_enhance.xml"));
		list = doc.getElementsByTagName("table");
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			ResourceT resT = mapResT.get(Integer.valueOf(elem.getAttributeNode("id").getValue()));
			if (resT != null) {
				resT.getEquipT().cost_base = Integer.valueOf(elem.getAttributeNode("cost_coin_b").getValue());
				resT.getEquipT().cost_add = Integer.valueOf(elem.getAttributeNode("cost_coin_add").getValue());
				resT.getEquipT().max_en_num = Byte.valueOf(elem.getAttributeNode("max_en_num").getValue());
			}
		}
		
		// 加载合成表
		doc = db.parse(new File(XML_PATH + "Equip_Sys.xml"));
		list = doc.getElementsByTagName("table");
		field0 = ResourceT.class.getDeclaredField("sysnT");	field0.setAccessible(true);
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			ResourceT resT = mapResT.get(Integer.valueOf(elem.getAttributeNode("to_id").getValue()));
			if (resT != null) {
				SysnT sysnT = new SysnT();
				sysnT.coin_cost = Integer.valueOf(elem.getAttributeNode("cost_coin").getValue());
				String[] strs = elem.getAttributeNode("data").getValue().replace("[", "").replace("]", "").split(";");
				for (String str : strs) {
					String[] strs1 = str.split(",");
					int id = Integer.valueOf(strs1[0]);
					short num = Short.valueOf(strs1[1]);
					Short s = sysnT.mapSysn.get(id);
					if (s == null) {
						sysnT.mapSysn.put(id, num);
					} else {
						sysnT.mapSysn.put(id, (short) (s + num));
					}
				}
				field0.set(resT, sysnT);
			}
		}
		field0.setAccessible(false);
		
		// 加载伙伴表
		doc = db.parse(new File(XML_PATH + "role.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Short, PartnerT> mapPartnT = new HashMap<Short, PartnerT>(max);
		field0 = PartnerT.class.getDeclaredField("id");			field0.setAccessible(true);
		field1 = PartnerT.class.getDeclaredField("star");		field1.setAccessible(true);
		field2 = PartnerT.class.getDeclaredField("piece");		field2.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			PartnerT partnT = new PartnerT();
			field0.setShort(partnT, Short.valueOf(elem.getAttributeNode("id").getValue()));
			field1.setByte(partnT, (byte) (Integer.valueOf(elem.getAttributeNode("star").getValue()) - 1));
			field2.setShort(partnT, Short.valueOf(elem.getAttributeNode("get_piece").getValue()));
			String[] strs = elem.getAttributeNode("equi_list").getValue().replace("[", "").replace("]", "").split(";");
			int step = 0, idx;
			for (String str : strs) {
				String[] strs1 = str.split(",");
				idx = 0;
				for (String s : strs1) {
					partnT.arrStepEqpt[step][idx++] = mapResT.get(Integer.valueOf(s));
				}
				++step;
			}
			mapPartnT.put(partnT.getId(), partnT);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field = Partner.class.getDeclaredField("mapPartnT");
		field.setAccessible(true);
		field.set(Partner.class, mapPartnT);
		field.setAccessible(false);
		
		// 加载伙伴合成表
		doc = db.parse(new File(XML_PATH + "hero_sys.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		field0 = PartnerT.class.getDeclaredField("synRes");			field0.setAccessible(true);
		field1 = PartnerT.class.getDeclaredField("synResNum");		field1.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			PartnerT partnT = Partner.getPartnerT(Short.valueOf(elem.getAttributeNode("to_id").getValue()));
			if (partnT != null) {
				field0.setInt(partnT, Short.valueOf(elem.getAttributeNode("for_id").getValue()));
				field1.setByte(partnT, Byte.valueOf(elem.getAttributeNode("for_num").getValue()));
			}
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		
		// 加载伙伴升星表
		doc = db.parse(new File(XML_PATH + "hero_star.xml"));
		list = doc.getElementsByTagName("table");
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			int cut_peice = Integer.valueOf(elem.getAttributeNode("cut_peice").getValue());
			if (cut_peice != 0) {
				PartnerT partnT = Partner.getPartnerT(Short.valueOf(elem.getAttributeNode("id").getValue()));
				int star_idx = Integer.valueOf(elem.getAttributeNode("star").getValue()) -1;
				partnT.arrStarCost[star_idx][0] = cut_peice;
				partnT.arrStarCost[star_idx][1] = Integer.valueOf(elem.getAttributeNode("cut_coin").getValue());
			}
		}
		
		// 加载怪物表
		doc = db.parse(new File(XML_PATH + "master.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Integer, MonsterT> mapMonsterT = new HashMap<Integer, MonsterT>(max);
		field0 = MonsterT.class.getDeclaredField("id");			field0.setAccessible(true);
		field1 = MonsterT.class.getDeclaredField("bBoss");		field1.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			MonsterT monsterT = new MonsterT();
			field0.setInt(monsterT, Integer.valueOf(elem.getAttributeNode("id").getValue()));
			field1.setBoolean(monsterT, Byte.valueOf(elem.getAttributeNode("isboss").getValue()) != 0);
			mapMonsterT.put(monsterT.getId(), monsterT);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field = Monster.class.getDeclaredField("_mapMonsterT");
		field.setAccessible(true);
		field.set(Monster.class, mapMonsterT);
		field.setAccessible(false);
		
		// 加载掉落表
		doc = db.parse(new File(XML_PATH + "Drop.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Integer, DropT> mapDropT = new HashMap<Integer, DropT>(max);
		field0 = DropT.class.getDeclaredField("id");		field0.setAccessible(true);
		field1 = DropT.class.getDeclaredField("minLv");		field1.setAccessible(true);
		field2 = DropT.class.getDeclaredField("maxLv");		field2.setAccessible(true);
		field3 = DropT.class.getDeclaredField("type");		field3.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			byte type = Byte.valueOf(elem.getAttributeNode("type").getValue());
			DropT dt = type == 1 ? new NoamalDropT() : new SpecialDropT();
			field3.setByte(dt, type);
			field0.setInt(dt, Integer.valueOf(elem.getAttributeNode("id").getValue()));
			field1.setShort(dt, Short.valueOf(elem.getAttributeNode("min_lv").getValue()));
			field2.setShort(dt, Short.valueOf(elem.getAttributeNode("max_lv").getValue()));
			mapDropT.put(dt.getId(), dt);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field4 = RateT.class.getDeclaredField("rate");		field4.setAccessible(true);
		field5 = RateT.class.getDeclaredField("num");		field5.setAccessible(true);
		field6 = ItemRateT.class.getDeclaredField("id");	field6.setAccessible(true);
		field0 = DropRateT.class.getDeclaredField("dropT");	field0.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			DropT dt = mapDropT.get(Integer.valueOf(elem.getAttributeNode("id").getValue()));
			double rate = Double.valueOf(elem.getAttributeNode("rate").getValue());
			String[] strs1 = elem.getAttributeNode("list").getValue().split(";");
			if (dt.isNormal()) {
				NoamalDropT ndt = (NoamalDropT) dt;
				for (String str : strs1) {
					String[] str2 = str.split(",");//id,num,rate
					ItemRateT itemRateT = new ItemRateT();
					field4.setDouble(itemRateT, Double.valueOf(str2[2]) / rate);
					field5.setInt(itemRateT, Integer.valueOf(str2[1]));
					field6.setInt(itemRateT, Integer.valueOf(str2[0]));
					ndt.listItemT.add(itemRateT);
				}
			} else {
				SpecialDropT sdt = (SpecialDropT) dt;
				for (String str : strs1) {
					String[] str2 = str.split(",");//id,num,rate
					DropRateT dropRateT = new DropRateT();
					field4.setDouble(dropRateT, Double.valueOf(str2[2]) / rate);
					field5.setInt(dropRateT, Integer.valueOf(str2[1]));
					field0.set(dropRateT, mapDropT.get(Integer.valueOf(str2[0])));
					sdt.listDropT.add(dropRateT);
				}
			}
		}
		field4.setAccessible(false);
		field5.setAccessible(false);
		field6.setAccessible(false);
		field0.setAccessible(false);
		field = Drop.class.getDeclaredField("_mapDropT");
		field.setAccessible(true);
		field.set(Drop.class, mapDropT);
		field.setAccessible(false);
		
		// 加载关卡场景表
		doc = db.parse(new File(XML_PATH + "battle.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Integer, FortT> mapFortT = new HashMap<Integer, FortT>();
		field0 = FortT.class.getDeclaredField("id");			field0.setAccessible(true);
		field1 = BattleT.class.getDeclaredField("btl_no");		field1.setAccessible(true);
		field2 = BattleT.class.getDeclaredField("bDrop");		field2.setAccessible(true);
		field3 = BattleT.class.getDeclaredField("bBossDrop");	field3.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			int id = Integer.valueOf(elem.getAttributeNode("battle_id").getValue());
			FortT fortT = mapFortT.get(id);
			if (fortT == null) {
				fortT = new FortT();
				field0.setInt(fortT, id);
				mapFortT.put(id, fortT);
			}
			BattleT bt = new BattleT();
			field1.setByte(bt, Byte.valueOf(elem.getAttributeNode("battle_count").getValue()));
			short dropTimes = Short.valueOf(elem.getAttributeNode("channel_enemys").getValue());
			String[] strs = elem.getAttributeNode("reward_drop_enemys").getValue().split(";");
			for (short s = 0; s != dropTimes; ++s) {
				for (String str : strs) {
					bt.listDropId.add(mapDropT.get(Integer.valueOf(str)));
				}
			}
			dropTimes = Short.valueOf(elem.getAttributeNode("channel_enemys_boss").getValue());
			strs = elem.getAttributeNode("reward_drop_enemys_boss").getValue().split(";");
			for (short s = 0; s != dropTimes; ++s) {
				for (String str : strs) {
					bt.listBossDropId.add(mapDropT.get(Integer.valueOf(str)));
				}
			}
			strs = elem.getAttributeNode("enemys").getValue().split(";");
			for (String str : strs) {
				MonsterT mst = Monster.getMonsterT(Integer.valueOf(str));
				if (mst != null) {
					if (!mst.isBoss()) {
						if (!bt.isHasDrop()) {
							field2.setBoolean(bt, true);
						}
					} else {
						if (!bt.isHasBossDrop()) {
							field3.setBoolean(bt, true);
						}
					}
				}
			}
			fortT.getListBattleT().add(bt);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field = Fort.class.getDeclaredField("mapFortT");
		field.setAccessible(true);
		field.set(Fort.class, mapFortT);
		field.setAccessible(false);
		
		// 加载关卡主表
		doc = db.parse(new File(XML_PATH + "battle_Desc.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		field0 = FortT.class.getDeclaredField("prevId");		field0.setAccessible(true);
		field1 = FortT.class.getDeclaredField("nextId");		field1.setAccessible(true);
		field2 = FortT.class.getDeclaredField("exp");			field2.setAccessible(true);
		field3 = FortT.class.getDeclaredField("partnExp");		field3.setAccessible(true);
		field4 = FortT.class.getDeclaredField("power");			field4.setAccessible(true);
		field = FortT.class.getDeclaredField("hard");			field.setAccessible(true);
		field5 = FortT.class.getDeclaredField("type");			field5.setAccessible(true);
		field6 = FortT.class.getDeclaredField("chapter");		field6.setAccessible(true);
		Field field7 = FortT.class.getDeclaredField("times");	field7.setAccessible(true);
		byte maxChapter = 0, maxHard = 0;
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			int nextId = Integer.valueOf(elem.getAttributeNode("id").getValue());
			FortT fortT = Fort.getFortT(nextId);
			if (fortT != null) {
				int prevId = Integer.valueOf(elem.getAttributeNode("open_need_battleid").getValue().split(";")[0]);
				if (prevId != 0) {
					FortT ft = Fort.getFortT(prevId);
					field1.setInt(ft, nextId);
					field0.setInt(fortT, prevId);
				}
				field2.setShort(fortT, Short.valueOf(elem.getAttributeNode("team_EXP").getValue()));
				field3.setShort(fortT, Short.valueOf(elem.getAttributeNode("hero_EXP").getValue()));
				field4.setInt(fortT, Integer.valueOf(elem.getAttributeNode("power").getValue()));
				field.setByte(fortT, Byte.valueOf(elem.getAttributeNode("difficulty_lv").getValue()));
				field5.setByte(fortT, Byte.valueOf(elem.getAttributeNode("game_type").getValue()));
				field6.setByte(fortT, Byte.valueOf(elem.getAttributeNode("chapter").getValue()));
				field7.setShort(fortT, Short.valueOf(elem.getAttributeNode("times").getValue()));
				String[] strs = elem.getAttributeNode("first_drop").getValue().split(";");
				for (String str : strs) {
					String[] strs1 = str.split(",");//type,id,num
					if (strs1.length == 3) {
						FirstDropT dropT = new FirstDropT();	
						dropT.type = Byte.valueOf(strs1[0]);
						dropT.id = Integer.valueOf(strs1[1]);
						dropT.num = Integer.valueOf(strs1[2]);
						fortT.listFirstDropT.add(dropT);
					}
				}
				if (fortT.getType() == 1) {
					if (fortT.getHard() > maxHard) {
						maxHard = fortT.getHard();
					}
					if (fortT.getChapter() > maxChapter) {
						maxChapter = fortT.getChapter();
					}
				}
			}
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field4.setAccessible(false);
		field.setAccessible(false);
		field5.setAccessible(false);
		field6.setAccessible(false);
		field7.setAccessible(false);
		
		// 关卡分类（PVE章节）
		ChapterT[][] arrArrChapterT = new ChapterT[maxHard + 1][maxChapter + 1];
		for (FortT fortT : mapFortT.values()) {
			if (fortT.getType() == 1) {
				ChapterT ct = arrArrChapterT[fortT.getHard()][fortT.getChapter()];
				if (ct == null) {
					ct = new ChapterT();
					arrArrChapterT[fortT.getHard()][fortT.getChapter()] = ct;
				}
				ct.getListFortT().add(fortT);
			}
		}
		field = Chapter.class.getDeclaredField("arrArrChapterT");
		field.setAccessible(true);
		field.set(Chapter.class, arrArrChapterT);
		field.setAccessible(false);
		
		// 加载邪神表
		doc = db.parse(new File(XML_PATH + "God_Initial.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Short, GodT> mapGodT = new HashMap<Short, GodT>(max);
		field0 = GodT.class.getDeclaredField("id");			field0.setAccessible(true);
		field1 = GodT.class.getDeclaredField("star");		field1.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			GodT godT = new GodT();
			field0.setShort(godT, Short.valueOf(elem.getAttributeNode("id").getValue()));
			field1.setByte(godT, Byte.valueOf(elem.getAttributeNode("start_star").getValue()));
			mapGodT.put(godT.getId(), godT);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field = God.class.getDeclaredField("mapGodT");
		field.setAccessible(true);
		field.set(God.class, mapGodT);
		field.setAccessible(false);
		
		doc = db.parse(new File(XML_PATH + "God_Sys.xml"));
		list = doc.getElementsByTagName("table");
		field0 = GodT.class.getDeclaredField("synRes");			field0.setAccessible(true);
		//field1 = GodT.class.getDeclaredField("synResNum");		field1.setAccessible(true);
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			GodT godT = mapGodT.get(Short.valueOf(elem.getAttributeNode("target_id").getValue()));
			if (godT != null) {
				field0.setInt(godT, Integer.valueOf(elem.getAttributeNode("piece_id").getValue()));
				//field1.setShort(godT, Short.valueOf(elem.getAttributeNode("piece").getValue()));
				godT.arrStarCost[0] = Short.valueOf(elem.getAttributeNode("piece").getValue());
			}
		}
		field0.setAccessible(false);
		//field1.setAccessible(false);
		doc = db.parse(new File(XML_PATH + "God_StarSkill.xml"));
		list = doc.getElementsByTagName("table");
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			byte star = Byte.valueOf(elem.getAttributeNode("star").getValue());
			if (star < 3) {
				GodT godT = mapGodT.get(Short.valueOf(elem.getAttributeNode("god_id").getValue()));
				if (godT != null) {
					godT.arrStarCost[star] = Short.valueOf(elem.getAttributeNode("cost").getValue());
				}
			}
		}
		
		// 加载抽奖表
		doc = db.parse(new File(XML_PATH + "store.xml"));
		list = doc.getElementsByTagName("table");
		LootGrp[][] arrArrLootGrp = new LootGrp[arrLevelT.length][Lot.STORE_ID_NUM];
		for (i = 1, max = arrLevelT.length; i != max; ++i) {
			for (int j = 0; j != Lot.STORE_ID_NUM; ++j) {
				arrArrLootGrp[i][j] = new LootGrp();
			}
		}
		field0 = Loot.class.getDeclaredField("lootId");			field0.setAccessible(true);
		field1 = Loot.class.getDeclaredField("qtyBase");		field1.setAccessible(true);
		field2 = Loot.class.getDeclaredField("qtyRnd");			field2.setAccessible(true);
		field3 = Loot.class.getDeclaredField("prob");			field3.setAccessible(true);
		field4 = Loot.class.getDeclaredField("type");			field4.setAccessible(true);
		field = LootGrp.class.getDeclaredField("totalProb");	field.setAccessible(true);
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			Loot lt = new Loot();
			String[] ss = elem.getAttributeNode("item").getValue().split(",");
			field0.setInt(lt, Integer.valueOf(ss[1]));
			field1.setInt(lt, Integer.valueOf(elem.getAttributeNode("min_number").getValue()));
			field2.setInt(lt, Integer.valueOf(elem.getAttributeNode("max_number").getValue()) - field1.getInt(lt) + 1);
			field3.setInt(lt, Integer.valueOf(elem.getAttributeNode("weight").getValue()));
			field4.setByte(lt, Byte.valueOf(ss[0]));
			short min_lv = Short.valueOf(elem.getAttributeNode("min_lv").getValue());
			short max_lv = Short.valueOf(elem.getAttributeNode("max_lv").getValue());
			byte store_id = -1;
			switch (Byte.valueOf(elem.getAttributeNode("store_id").getValue())) {
			case 1: store_id = Lot.STORE_ID_1; break;
			case 2: store_id = Lot.STORE_ID_2; break;
			case 10: store_id = Lot.STORE_ID_10; break;
			case 20: store_id = Lot.STORE_ID_20; break;
			case 11: store_id = Lot.STORE_ID_11; break;
			case 21: store_id = Lot.STORE_ID_21; break;
			case 12: store_id = Lot.STORE_ID_12; break;
			case 22: store_id = Lot.STORE_ID_22; break;
			default:	break;
			}
			if (store_id != -1) {
				for (int k = 1, kmax = arrLevelT.length; k != kmax; ++k) {
					if (k >= min_lv) {
						if (k <= max_lv) {
							LootGrp lootGrp = arrArrLootGrp[k][store_id];
							lootGrp.listLoot.add(lt);
							field.setInt(lootGrp, lt.getProb() + lootGrp.getTotalProb());
						} else {
							break;
						}
					} 
				}
			}
		}
		field.setAccessible(false);
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field4.setAccessible(false);
		field = Lot.class.getDeclaredField("arrArrLootGrp");
		field.setAccessible(true);
		field.set(Lot.class, arrArrLootGrp);
		field.setAccessible(false);
		
		// 加载任务表
		doc = db.parse(new File(XML_PATH + "task_list.xml"));
		list = doc.getElementsByTagName("table");
		max = list.getLength();
		Map<Integer, TaskT> mapTaskT = new HashMap<Integer, TaskT>(max);
		field0 = TaskT.class.getDeclaredField("id");			field0.setAccessible(true);
		field1 = TaskT.class.getDeclaredField("prevId");		field1.setAccessible(true);
		field2 = TaskT.class.getDeclaredField("condition");		field2.setAccessible(true);
		field3 = TaskT.class.getDeclaredField("condNum");		field3.setAccessible(true);
		field4 = TaskT.class.getDeclaredField("lvLimit");		field4.setAccessible(true);
		field5 = TaskT.class.getDeclaredField("type");			field5.setAccessible(true);
		for (i = 0; i != max; ++i) {
			elem = (Element)list.item(i);
			TaskT taskT = new TaskT();
			field0.setInt(taskT, Integer.valueOf(elem.getAttributeNode("id").getValue()));
			String str = elem.getAttributeNode("open_task").getValue();
			field1.setInt(taskT, !str.equals("#") ? Integer.valueOf(str) : 0);
			str = elem.getAttributeNode("object").getValue();
			field2.setInt(taskT, str.equals("#") ? 0 : Integer.valueOf(str));
			field3.setInt(taskT, Integer.valueOf(elem.getAttributeNode("number").getValue()));
			str = elem.getAttributeNode("open_level").getValue();
			field4.setShort(taskT, str.equals("#") ? 0 : Short.valueOf(str));
			field5.setByte(taskT, Byte.valueOf(elem.getAttributeNode("type").getValue()));
			TaskReward rwd = new TaskReward();
			String[] strs = elem.getAttributeNode("reward_1").getValue().split(",");
			rwd.rwdType = Byte.valueOf(strs[0]);
			rwd.rwdId = Integer.valueOf(strs[1]);
			rwd.rwdNum = Integer.valueOf(strs[2]);
			taskT.listRwd.add(rwd);
			str = elem.getAttributeNode("reward_2").getValue();
			if (!str.equals("#")) {
				strs = str.split(",");
				rwd = new TaskReward();
				rwd.rwdType = Byte.valueOf(strs[0]);
				rwd.rwdId = Integer.valueOf(strs[1]);
				rwd.rwdNum = Integer.valueOf(strs[2]);
				taskT.listRwd.add(rwd);
				str = elem.getAttributeNode("reward_3").getValue();
				if (!str.equals("#")) {
					strs = str.split(",");
					rwd = new TaskReward();
					rwd.rwdType = Byte.valueOf(strs[0]);
					rwd.rwdId = Integer.valueOf(strs[1]);
					rwd.rwdNum = Integer.valueOf(strs[2]);
					taskT.listRwd.add(rwd);
				}
			}
			mapTaskT.put(taskT.getId(), taskT);
		}
		field0.setAccessible(false);
		field1.setAccessible(false);
		field2.setAccessible(false);
		field3.setAccessible(false);
		field4.setAccessible(false);
		field5.setAccessible(false);
		field6 = TaskT.class.getDeclaredField("listNextTask");	field6.setAccessible(true);
		List<TaskT> listInitTaskT = new ArrayList<TaskT>();
		for (TaskT taskT : mapTaskT.values()) {
			if (taskT.getPrevId() != 0) {
				TaskT tt = mapTaskT.get(taskT.getPrevId());
				if (!tt.isHasNextTask()) {
					field6.set(tt, new ArrayList<TaskT>());
				}
				tt.getListNextTask().add(taskT);
			} else {
				listInitTaskT.add(taskT);
			}
		}
		field6.setAccessible(false);
		field = Task.class.getDeclaredField("mapTaskT");
		field.setAccessible(true);
		field.set(Task.class, mapTaskT);
		field.setAccessible(false);
		field = Task.class.getDeclaredField("listInitTaskT");
		field.setAccessible(true);
		field.set(Task.class, listInitTaskT);
		field.setAccessible(false);
		
		// 加载全局变量
		doc = db.parse(new File(XML_PATH + "Global_Value.xml"));
		list = doc.getElementsByTagName("table");
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			switch (Short.valueOf(elem.getAttributeNode("id").getValue())) {
			case 18: GlobalConst.init_coin = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 19: GlobalConst.init_credit = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 20: GlobalConst.init_power = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 29: GlobalConst.power_recover_time = Integer.valueOf(elem.getAttributeNode("Value").getValue()) * 1000; break;
			case 2: GlobalConst.lottery_coin_1_price = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 7: GlobalConst.lottery_credit_1_price = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 6: GlobalConst.lottery_coin_10_price = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 10: GlobalConst.lottery_credit_10_price = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 3: GlobalConst.lottery_coin_item_id = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 8: GlobalConst.lottery_credit_item_id = Integer.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 88: GlobalConst.hero_skill_max = Short.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 68: GlobalConst.hero_skill_CD = Integer.valueOf(elem.getAttributeNode("Value").getValue()) * 1000; break;
			case 21: GlobalConst.arr_skill_lv[0] = Short.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 22: GlobalConst.arr_skill_lv[1] = Short.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 23: GlobalConst.arr_skill_lv[2] = Short.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 24: GlobalConst.arr_skill_lv[3] = Short.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 5: GlobalConst.lottery_coin_free_times = Byte.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 9: GlobalConst.lottery_credit_free_times = Byte.valueOf(elem.getAttributeNode("Value").getValue()); break;
			case 4: GlobalConst.lottery_coin_cutdown = Integer.valueOf(elem.getAttributeNode("Value").getValue()) * 1000; break;
			default: break;
			}
		}
		
		// 加载购买重置信息表
		doc = db.parse(new File(XML_PATH + "Reset_times.xml"));
		list = doc.getElementsByTagName("table");
		for (i = 0, max = list.getLength(); i != max; ++i) {
			elem = (Element)list.item(i);
			byte times = Byte.valueOf(elem.getAttributeNode("times").getValue());
			// 购买体力花费
			String[] strs = elem.getAttributeNode("power_cost").getValue().split(",");
			ResetTimes.data[0][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[0][times][1] = Integer.valueOf(strs[2]);
			ResetTimes.data[0][times][2] = Resource.ID_99903;
			ResetTimes.data[0][times][3] = 120;
			// 精英副本重置花费
			strs = elem.getAttributeNode("battle_jy_cost").getValue().split(",");
			ResetTimes.data[1][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[1][times][1] = Integer.valueOf(strs[2]);
			// 噩梦副本重置花费
			strs = elem.getAttributeNode("battle_em_cost").getValue().split(",");
			ResetTimes.data[2][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[2][times][1] = Integer.valueOf(strs[2]);
			// 点金石次数花费
			strs = elem.getAttributeNode("coin_cost").getValue().split(",");
			ResetTimes.data[3][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[3][times][1] = Integer.valueOf(strs[2]);
			// 技能点购买重置花费
			strs = elem.getAttributeNode("skill_cost").getValue().split(",");
			ResetTimes.data[4][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[4][times][1] = Integer.valueOf(strs[2]);
			ResetTimes.data[4][times][2] = Resource.ID_99907;
			ResetTimes.data[4][times][3] = 10;
			// 世界BOSS挑战重置花费
			strs = elem.getAttributeNode("world_boss_cost").getValue().split(",");
			ResetTimes.data[5][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[5][times][1] = Integer.valueOf(strs[2]);
			// 竞技场挑战重置花费
			strs = elem.getAttributeNode("arena_cost").getValue().split(",");
			ResetTimes.data[6][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[6][times][1] = Integer.valueOf(strs[2]);
			// 商店刷新重置花费
			strs = elem.getAttributeNode("random_shop_cost").getValue().split(",");
			ResetTimes.data[7][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[7][times][1] = Integer.valueOf(strs[2]);
			// 竞技场商城刷新花费
			strs = elem.getAttributeNode("arena_shop_cost").getValue().split(",");
			ResetTimes.data[8][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[8][times][1] = Integer.valueOf(strs[2]);
			// 世界BOSS商城刷新花费
			strs = elem.getAttributeNode("world_boss_shop_cost").getValue().split(",");
			ResetTimes.data[9][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[9][times][1] = Integer.valueOf(strs[2]);
			// 公会商城刷新花费
			strs = elem.getAttributeNode("consortia_shop_cost").getValue().split(",");
			ResetTimes.data[10][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[10][times][1] = Integer.valueOf(strs[2]);
			// 远征商城刷新花费
			strs = elem.getAttributeNode("expedition_shop_cost").getValue().split(",");
			ResetTimes.data[11][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[11][times][1] = Integer.valueOf(strs[2]);
			// 公会值1次金币贡献所需金币
			strs = elem.getAttributeNode("consortia_value_coin_cost").getValue().split(",");
			ResetTimes.data[12][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[12][times][1] = Integer.valueOf(strs[2]);
			// 公会值钻石1贡献所需钻石
			strs = elem.getAttributeNode("consortia_value_diamond1_cost").getValue().split(",");
			ResetTimes.data[13][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[13][times][1] = Integer.valueOf(strs[2]);
			// 公会值钻石2贡献所需钻石
			strs = elem.getAttributeNode("consortia_value_diamond2_cost").getValue().split(",");
			ResetTimes.data[14][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[14][times][1] = Integer.valueOf(strs[2]);
			// 公会图腾金币充能
			strs = elem.getAttributeNode("consortia_totem_coin_cost").getValue().split(",");
			ResetTimes.data[15][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[15][times][1] = Integer.valueOf(strs[2]);
			// 公会图腾钻石1充能
			strs = elem.getAttributeNode("consortia_totem_diamond1_cost").getValue().split(",");
			ResetTimes.data[16][times][0] = Integer.valueOf(strs[1]);
			ResetTimes.data[16][times][1] = Integer.valueOf(strs[2]);
		}
	}
}
