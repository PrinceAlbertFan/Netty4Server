package data.xml;

import java.util.ArrayList;
import java.util.List;

import data.xml.Fort.FortT;

public final class Chapter {
	public final static class ChapterT {
		private final List<FortT> listFortT = new ArrayList<FortT>();
		
		public final List<FortT> getListFortT() { return listFortT; }
	}
	
	private static ChapterT[][] arrArrChapterT;
	
	public final static ChapterT getChapterT(byte chapter, byte hard) {
		return arrArrChapterT[hard][chapter];
	}
}
