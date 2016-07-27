package server.message;

import obj.Player;
import tool.SendBuf;

public final class PvEStartMsg extends SendBuf {
	
	public void send(Player ply) {
		
		this.reset().writeInt(4)
		.packAndSend(ply.getSocket());
	}
	
	public PvEStartMsg(int opcode, int dataLen) {
		super(opcode, dataLen);
	}

}
