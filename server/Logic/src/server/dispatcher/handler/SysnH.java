package server.dispatcher.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import data.xml.Resource;
import data.xml.Resource.ResourceT;
import io.netty.buffer.ByteBuf;
import obj.Player;
import obj.ctrl.ResCtrl;
import obj.ctrl.ResCtrl.ResDBRow;
import server.SndMsg;
import server.dispatcher.GameHandler;

public final class SysnH extends GameHandler {
	
	private static Iterator<Map.Entry<Integer, Short>> _iter;
	private static Entry<Integer, Short> _entry;
	private static ResDBRow _resRow;
	
	@Override
	public void handle(ByteBuf buf, int socket) {
		if ((ply = Player.getPlayerBySocket(socket)) != null) {
			ResourceT resT = Resource.getResourceT(buf.readInt());
			if (resT == null || !resT.isSysnable()) {
				System.out.println("合成目标为空");
				return;
			}
			if (!ply.getResCtrl().subtrCoin(resT.getSysnT().coin_cost)) {
				System.out.println("钱币不够，合成失败");
				return;
			}
			SndMsg.sysn.reset().writeInt(resT.getId()).writeInt(ply.getResCtrl().getCoin()).writeInt(resT.getSysnT().mapSysn.size() + 1);
			_iter = resT.getSysnT().mapSysn.entrySet().iterator();
			while (_iter.hasNext()) {
				_entry = _iter.next();
				_resRow = ply.getResCtrl().subtrInfactRes(_entry.getKey(), _entry.getValue());
				if (_resRow == null) {
					System.out.println("合成物不够，合成失败");
					return;
				}
				SndMsg.sysn.writeInt(_entry.getKey()).writeInt(ResCtrl.getResCid(_resRow))
				.writeInt(ResCtrl.getResQty(_resRow)).writeInt(ResCtrl.getResQty(_resRow) + _entry.getValue());
			}
			_resRow = ply.getResCtrl().addInfactRes(resT.getId(), 1);
			SndMsg.sysn.writeInt(resT.getId()).writeInt(ResCtrl.getResCid(_resRow))
			.writeInt(ResCtrl.getResQty(_resRow)).writeInt(ResCtrl.getResQty(_resRow) - 1)
			.packAndSend(ply.getSocket());
		}
	}

}
