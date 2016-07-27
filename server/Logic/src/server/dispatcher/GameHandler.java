package server.dispatcher;

import obj.Player;
import io.netty.buffer.ByteBuf;

/**
 * 此类是游戏调度器的消息分派超类，被用于继承，
 * 继承它的类名全部用大写H后缀，用以与基类(GameHandler)区分
 * @author Albert Fan
 */
public abstract class GameHandler {
	
	protected static Player ply;
	
	/**
	 * 对buf进行解析和操作，然后分派消息类型给Dispatcher
	 * @param buf
	 * @param ply
	 * @return 一切顺利返回ture
	 **/
	public abstract void handle(ByteBuf buf, int socket);
}
