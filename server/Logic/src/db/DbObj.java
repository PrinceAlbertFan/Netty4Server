package db;

/**
 * 所有的需要加载数据库信息的类全部继承这个类
 * */
public abstract class DbObj {
	
	protected boolean bNeedSave;
	
	public final boolean IsNeedSave() { return bNeedSave; }
	
	public final void setAlreadySave() { bNeedSave = false; }
	
	public final void setNeedSave() { bNeedSave = true; }
}
