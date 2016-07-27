package data.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import obj.Player;

public final class Drop {
	public static class RateT {
		double rate;
		int num;
	}
	public final static class ItemRateT extends RateT {
		int id;
	}
	public static class DropT {
		private int id;
		//private int rate;
		private short minLv;
		private short maxLv;
		private byte type;
		
		public final int getId() { return id; }
		//public final int getRate() { return rate; }
		public final boolean checkLv(short lv) { return lv >= minLv && lv <= maxLv; }
		public final boolean isNormal() { return type == 1; }
	}
	public final static class DropRateT  extends RateT {
		public NoamalDropT dropT;
	}
	
	public final static class NoamalDropT extends DropT {
		public final List<ItemRateT> listItemT = new ArrayList<ItemRateT>();
	}
	
	public final static class SpecialDropT extends DropT {
		public final List<DropRateT> listDropT = new ArrayList<DropRateT>();
	}
	
	public final static Map<Integer, Integer> _mapDropMake = new HashMap<Integer, Integer>();
	
	private static Map<Integer, DropT> _mapDropT;
	
//	private static Iterator<Map.Entry<Integer, Integer>> _tmpMapDropIter;
//	private static Entry<Integer, Integer> _tmpMapDropEntry;
	//private static DropT _tmpDropT;
	
	private static void _make(Player ply, NoamalDropT dropT) {
		if (dropT.checkLv(ply.getLv())) {
			for (ItemRateT itemT : dropT.listItemT) {
				if (Math.random() < itemT.rate) {
					Integer i = ply._mapDropMake.get(itemT.id);
					if (i == null) {
						ply._mapDropMake.put(itemT.id, itemT.num);
					} else {
						ply._mapDropMake.put(itemT.id, i + itemT.num);
					}
					i = _mapDropMake.get(itemT.id);
					if (i == null) {
						_mapDropMake.put(itemT.id, itemT.num);
					} else {
						_mapDropMake.put(itemT.id, i + itemT.num);
					}
				}
			}
		}
	}
	
	public static void make(Player ply,  DropT _dropT) {
		if (_dropT.isNormal()) {
			_make(ply, (NoamalDropT) _dropT);
		} else {
			SpecialDropT sDropT = (SpecialDropT) _dropT;
			for (DropRateT dropT : sDropT.listDropT) {
				if (Math.random() < dropT.rate) {
					byte b = 0;
					do {
						_make(ply, dropT.dropT);
					} while (++b != dropT.num);
				}
			}
		}
//		_tmpMapDropIter = ply._mapDropMake.entrySet().iterator();
//		while (_tmpMapDropIter.hasNext()) {
//			_tmpMapDropEntry = _tmpMapDropIter.next();
//			ply.getResCtrl().addRes(_tmpMapDropEntry.getKey(), _tmpMapDropEntry.getValue());
//		}
//		if (!_mapDropMake.isEmpty()) {
//			_mapDropMake.clear();
//		}
	}
}
