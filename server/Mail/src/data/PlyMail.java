package data;

import global.GloConst;
import io.netty.buffer.ByteBuf;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import server.connect.GatewayConnection;
import tool.ByteBufDecoder;
import tool.ClieSendBuf;
import tool.ServSendBuf;
import tool.UnpooledBufUtil;
import manager.TimeManager;
import db.DbCenter;


public final class PlyMail {
	
	private static PlyMail _tmpPlyMail;
	private static ByteBuf _tmpBuf;
	private static long _id;
	private static String _cont;
	private static int _socket, _resId;
	private static short _mail;
	
	private static final BlockingQueue<ByteBuf> recvBufQue = new LinkedBlockingQueue<ByteBuf>(1024);
	
	private static final ClieSendBuf clieSndBuf = new ClieSendBuf(36, 50);
	private static final ServSendBuf servSndBuf = new ServSendBuf(36, 30);
	
	/**Socket连接表，主键是链接号*/
	private static final Map<Integer, PlyMail> _sockMap = new HashMap<Integer, PlyMail>();
	/**账号连接表，主键是ply_id*/
	private static final Map<Long, PlyMail> _plyIdMap = new HashMap<Long, PlyMail>();
	
	private static final String emptyStr = "";
	
	public static void mainUpdate() {
		while ((_tmpBuf = recvBufQue.poll()) != null) {
			_socket = _tmpBuf.readInt();
			switch (_tmpBuf.readShort()) { // opcode
			case 1001: // 收到单份邮件
				_id = _tmpBuf.readLong();
				_mail = _tmpBuf.readShort();
				_cont = ByteBufDecoder.readString(_tmpBuf);
				_resId = _tmpBuf.readInt();
				collectMail(_id, _mail, _cont, _resId, _tmpBuf.readInt());
				break;
			case 36: // 领取邮件
				takeMail(_tmpBuf.readLong(), _socket);
				break;
			case 1: // 登出
				remove(_socket);
				break;
			default: // code = 0, 登入
				_id = _tmpBuf.readLong();
				PlyMail.put(_id, _socket, _tmpBuf.readShort());
				break;
			}
			UnpooledBufUtil.reclaimBuf(_tmpBuf);
		}
		for (PlyMail plyMail : _plyIdMap.values()) {
			plyMail.onUpdate();
		}
	}
	
	public static void captureMsg(ByteBuf buf) {
		try {
			recvBufQue.put(buf); // 阻塞
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void _collectMailTo(PlyMail plyMail, short mail, String cont, int res_id, int res_qty, long due_t) {
		if (!plyMail._emptyList.isEmpty()) {
			_tmpRow = plyMail._emptyList.remove(plyMail._emptyList.size() - 1);
			_tmpRow.bNeedSave = true;
		} else {
			_tmpRow = new DBRow();
			plyMail._mailMap.put(_tmpRow.id = DbCenter.nextPlyMailId(), _tmpRow);
			DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyMail(), _tmpRow.id, plyMail.ply_id, mail, cont, res_id, res_qty, due_t));
			plyMail.setMailCount((short) plyMail._mailMap.size());
		}
		_tmpRow.mail = mail;
		_tmpRow.cont = cont;
		_tmpRow.res_id = res_id;
		_tmpRow.res_qty = res_qty;
		_tmpRow.due_t = due_t;
	}
	
	private static void collectMail(long ply_id, short mail, String cont, int res_id, int res_qty) {
		_tmpPlyMail = _plyIdMap.get(ply_id);
		if (_tmpPlyMail != null) {
			_collectMailTo(_tmpPlyMail, mail, cont, res_id, res_qty, TimeManager.getDueTime());
			if (_tmpPlyMail.socket != 0) {
				clieSndBuf.reset().writeInt(1).writeLong(_tmpRow.id).writeInt(mail)
				.writeInt(res_id).writeInt(res_qty)
				.writeLong(_tmpRow.due_t).writeString(cont)
				.last().pack(_tmpPlyMail.socket).end();
				GatewayConnection.writeAndFlush(clieSndBuf.getBuffer());
			}
		} else {
			_id = DbCenter.executeQueryLongFieldValue(String.format(DbCenter.getSqlSelectEmptyMail(), ply_id, TimeManager.getTime()));
			if (_id != 0) {
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyMail(),
						mail, cont, res_id, res_qty, TimeManager.getDueTime(), _id));
			} else {
				DbCenter.executeUpdate(String.format(DbCenter.getSqlInsertPlyMail(),
						DbCenter.nextPlyMailId(), ply_id, mail, cont, res_id, res_qty, TimeManager.getDueTime()));
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(), "mail_count", "mail_count+1", ply_id));
			}
		}
	}
	
	private static void takeMail(long id, int socket) {
		_tmpPlyMail = _sockMap.get(socket);
		if (_tmpPlyMail != null) {
			_tmpRow = _tmpPlyMail._mailMap.get(id);
			if (_tmpRow != null && _tmpRow.mail != 0) {
				if (_tmpRow.due_t > TimeManager.getTime()) {
					// 通知MainServer领取附件
					servSndBuf.reset(socket).writeInt(_tmpRow.res_id).writeInt(_tmpRow.res_qty)
					.last().pack(GloConst.MAIN_SERVER_ID).end();
					GatewayConnection.write(servSndBuf.getBuffer());
				}
				_tmpPlyMail._setDueMail(_tmpRow);
				clieSndBuf.reset().writeInt(0).writeLong(id).last().pack(socket).end();
				GatewayConnection.writeAndFlush(clieSndBuf.getBuffer());
			}
		}
	}
	
	private static void remove(int sock) {
		_tmpPlyMail = _sockMap.remove(sock);
		if (_tmpPlyMail != null) {
			_tmpPlyMail.socket = 0;
			//System.out.println("one PlyMail removed [sock]:" + sock);
		}
	}
	
	public static void put(long plyId, int sock, short count) {
		_tmpPlyMail = _plyIdMap.get(plyId);
		if (_tmpPlyMail != null) {
			_tmpPlyMail.socket = sock;
			//System.out.println("old PlyMail activated [sock]:" + sock);
		} else {
			_plyIdMap.put(plyId, _tmpPlyMail = new PlyMail(plyId, sock, count));
			_tmpPlyMail.loadFromDB();
			//System.out.println("new PlyMail created [sock]:" + sock);
		}
		_sockMap.put(sock, _tmpPlyMail);
		_tmpPlyMail.onceNoticeClient();
	}
	
	public static void close() {
		if (!_plyIdMap.isEmpty()) {
			for (PlyMail plyMail : _plyIdMap.values()) {
				plyMail.saveToDB();
			}
			_plyIdMap.clear();
		}
		if (!_sockMap.isEmpty()) {
			_sockMap.clear();
		}
	}
	
	private final static class DBRow {
		long id; short mail; String cont; int res_id; int res_qty; long due_t;
		boolean bNeedSave;
	}
	
	private static DBRow _tmpRow;
	private static ResultSet _tmpRs;
	//private static short _tmpS;
	private static boolean _tmpBool;
	
	private final Map<Long, DBRow> _mailMap = new HashMap<Long, DBRow>();
	private final List<DBRow> _emptyList = new ArrayList<DBRow>();
	
	private long ply_id;
	private int socket, saveT;
	private short mail_count;
	
	private void onUpdate() {
		if ((saveT += TimeManager.dt) > 600000) { 
			saveT = 0;
			if (socket == 0) {
				for (DBRow row : _mailMap.values()) {
					if (row.mail != 0 && row.due_t < TimeManager.getTime()) {
						_setDueMail(row);
					}
				}
			} else {
				_tmpBool = false;
				for (DBRow row : _mailMap.values()) {
					if (row.mail != 0 && row.due_t < TimeManager.getTime()) {
						_setDueMail(row);
						clieSndBuf.reset().writeInt(0).writeLong(row.id)
						.last().pack(socket).end();
						GatewayConnection.write(clieSndBuf.getBuffer());
						_tmpBool = true;
					}
				}
				if (_tmpBool) {
					GatewayConnection.flush();
				}
			}
			saveToDB();
		}
	}
	
	private void _setDueMail(DBRow row) {
		row.bNeedSave = true;
		row.mail = 0;
		row.cont = emptyStr;
		row.res_qty = row.res_id = 0;
		row.due_t = 0;
		_emptyList.add(row);
	}
	
	private void setMailCount(short count) {
		DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePly(), "mail_count", String.valueOf(mail_count = count), ply_id));
	}
	
	private void saveToDB() {
		for (DBRow row : _mailMap.values()) {
			if (row.bNeedSave) {
				row.bNeedSave = false;
				DbCenter.executeUpdate(String.format(DbCenter.getSqlUpdatePlyMail(),
						row.mail, row.cont, row.res_id, row.res_qty, row.due_t, row.id));
			}
		}
	}
	
	private void loadFromDB() {
		if (mail_count != 0) {
			_tmpRs = DbCenter.executeQuery(String.format(DbCenter.getSqlSelectPlyMail(), ply_id, mail_count));
			try {
				while (_tmpRs.next()) {
					_tmpRow = new DBRow();
					_tmpRow.id = _tmpRs.getLong(1);
					_tmpRow.mail = _tmpRs.getShort(2);
					if (_tmpRow.mail == 0) {
						_emptyList.add(_tmpRow);
					} else {
						_tmpRow.due_t = _tmpRs.getLong(6);
						if (_tmpRow.due_t > TimeManager.getTime()) {
							_tmpRow.cont = _tmpRs.getString(3);
							_tmpRow.res_id = _tmpRs.getInt(4);
							_tmpRow.res_qty = _tmpRs.getInt(5);
						} else {
							_setDueMail(_tmpRow);
						}
					}
					_mailMap.put(_tmpRow.id, _tmpRow);
				}
				_tmpRs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			_collectMailTo(this, (short) 1, emptyStr, 99902, 50, TimeManager.getDueTime());
		}
	}
	
	private void onceNoticeClient() {
		boolean bool = false;
		for (DBRow row : _mailMap.values()) {
			if (row.mail != 0) {
				clieSndBuf.reset().writeInt(1).writeLong(row.id).writeInt(row.mail)
				.writeInt(row.res_id).writeInt(row.res_qty)
				.writeLong(row.due_t).writeString(row.cont)
				.last().pack(socket).end();
				GatewayConnection.write(clieSndBuf.getBuffer());
				bool = true;
			}
		}
		if (bool) {
			GatewayConnection.flush();
		}
	}
	
	private PlyMail(long plyId, int sock, short count) {
		ply_id = plyId;
		socket = sock;
		mail_count = count;
	}
}
