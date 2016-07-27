package server.message;

import obj.Player;
import tool.SendBuf;

public final class LoginMsg extends SendBuf {
	
	public void send(Player ply) {
		this.reset()
		//.writeLong(ply.getPlyId())
		//.writeString("") //token
		.writeInt(1)//服务器list长度
		.writeInt(1)//list为1，服务器编号
		//.writeString("")//account
		.packAndSend(ply.getSocket());
	}
	
	public LoginMsg(int opcode, int dataLen) {
		super(opcode, dataLen);
	}
}
