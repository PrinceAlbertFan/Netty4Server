package db;

import io.netty.buffer.ByteBuf;

/**
 * 所有的需要加载数据库信息的类全部实现loadFromDB这个接口
 * */
public interface DbLoader {
	
	public void loadFromDB(ByteBuf buf);
}
