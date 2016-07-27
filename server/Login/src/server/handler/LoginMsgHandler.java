package server.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import tool.ByteBufDecoder;
import tool.ByteBufListener;
import tool.UnpooledBufUtil;
import db.DbCenter;
import global.GloConst;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class LoginMsgHandler extends ChannelInboundHandlerAdapter {
	
	private static final Set<Integer> _setSock = new HashSet<Integer>();
	
	private static ResultSet rs;
	private static ByteBuf buf;
	
	private static String name;
	private static String acc_name;
	private static String password;
	
	private static long ply_id;
	private static long logout_t;
	private static long acc_id;
	
	private static int icon;
	private static int socket;
	
	private static short lv;
	private static short partn_count;
	private static short res_count;
	private static short god_count;
	private static short fort_count;
	private static short task_count;
	private static short troop_count;
	private static short frnd_count;
	private static short times_count;
	private static short guide_count;
	private static short mail_count;
	
	private static byte vip_lv;
	private static byte acc_type;
	
	ByteBuf byteBuf;
	
//	@Override
//	public final void channelRegistered(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void channelUnregistered(ChannelHandlerContext ctx) {
//	}
	
	@Override
	public final void channelActive(ChannelHandlerContext ctx) {
		System.out.println("登陆服连接网关成功。");
		ctx.writeAndFlush(ctx.alloc().buffer(5).writeShort(5).writeByte(-1).writeShort(GloConst.SERVER_ID));
	}
	
	@Override
	public final void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("登陆服断开网关。");
		System.exit(0);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		socket = (byteBuf = (ByteBuf) msg).readInt();
		switch (byteBuf.readShort()) { // opcode
		case 0:
			if (_setSock.add(socket)) {
				System.out.println("Login<<[socket:" + socket + "]");
				acc_type = 0;
				acc_name = ByteBufDecoder.readString(byteBuf);
				password = ByteBufDecoder.readString(byteBuf);
				acc_id = DbCenter.accDBA.executeQueryLongFieldValue(
						String.format("select id from account where acc_name='%s' and password='%s' limit 1", acc_name, password));
				if (acc_id == 0) {
					acc_id = DbCenter.nextAccId();
					DbCenter.accDBA.executeUpdate(String.format("insert into account value(%d,'%s','%s')", acc_id, acc_name, password));
				}
				rs = DbCenter.gameDBA.executeQuery(
						String.format(DbCenter.getSqlSelectPly(), acc_type, acc_id));
				try {
					if (rs.next()) {
						ply_id = rs.getLong(1);
						name = rs.getString(4);
						icon = rs.getInt(5);
						lv = rs.getShort(6);
						vip_lv = rs.getByte(7);
						partn_count = rs.getShort(8);
						res_count = rs.getShort(9);
						god_count = rs.getShort(10);
						fort_count = rs.getShort(11);
						task_count = rs.getShort(12);
						troop_count = rs.getShort(13);
						frnd_count = rs.getShort(14);
						times_count = rs.getShort(15);
						guide_count = rs.getShort(16);
						mail_count = rs.getShort(17);
						logout_t = rs.getLong(18);
						rs.close();
					} else {
						rs.close();
						logout_t = 0;
						icon = 1;
						lv = 1;
						vip_lv = 0;
						mail_count = guide_count = times_count = frnd_count = troop_count = task_count = fort_count = god_count = res_count = partn_count = 0;
						ply_id = DbCenter.nextPlyId();
						StringBuffer sb = new StringBuffer("游客");
						String num = String.valueOf(ply_id);
						int len = 10 - num.length();
						if (len > 0) {
							do {
								sb.append("0");
							} while (--len != 0);
						}
						name = sb.toString() + num;
						DbCenter.gameDBA.executeUpdate(String.format(DbCenter.getSqlInsertPly(),
								ply_id, acc_type, acc_id, name));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// 发给Main
				buf = UnpooledBufUtil.dynamicBuf();
				buf.writerIndex(2).writeByte(1).writerIndex(5)
				.writeInt(socket).writeShort(0)
				.writeLong(ply_id).writeByte(acc_type).writeLong(acc_id);
				ByteBufDecoder.writeString(buf, name);
				buf.writeInt(icon).writeShort(lv).writeByte(vip_lv)
				.writeShort(partn_count).writeShort(res_count).writeShort(god_count)
				.writeShort(fort_count).writeShort(task_count).writeShort(troop_count)
				.writeShort(frnd_count).writeShort(times_count).writeShort(guide_count)
				.writeLong(logout_t).setShort(3, buf.writerIndex() - 3)
				.writeShort(GloConst.LOGIC_SERVER_ID).setShort(0, buf.writerIndex());
				ctx.write(buf.retain()).addListener(ByteBufListener.getListener(buf));
				// 发给MAIL
				buf = UnpooledBufUtil.dynamicBuf();
				buf.writerIndex(2).writeByte(1).writerIndex(5)
						.writeInt(socket).writeShort(0)
						.writeLong(ply_id).writeShort(mail_count).setShort(3, buf.writerIndex() - 3)
						.writeShort(GloConst.MAIL_SERVER_ID).setShort(0, buf.writerIndex());
				ctx.write(buf.retain()).addListener(ByteBufListener.getListener(buf));
				// 发给CHAT
				buf = UnpooledBufUtil.dynamicBuf();
				buf.writerIndex(2).writeByte(1).writerIndex(5).writeInt(socket).writeShort(0);
				ByteBufDecoder.writeString(buf, name);
				buf.setShort(3, buf.writerIndex() - 3)
				.writeShort(GloConst.CHAT_SERVER_ID).setShort(0, buf.writerIndex());
				ctx.writeAndFlush(buf.retain()).addListener(ByteBufListener.getListener(buf));  
			}
			break;
		default:
			if (_setSock.remove(socket)) {
				System.out.println("socket:[" + socket + "]>>Logout");
			}
			break;
		}
		byteBuf.release();
	}
	
//	@Override
//	public final void channelReadComplete(ChannelHandlerContext ctx) {
//	}
//	@Override
//	public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
//	}
//	@Override
//	public final void channelWritabilityChanged(ChannelHandlerContext ctx) {
//	}
	@Override
	public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	}
}
