package com.lcssit.icms.socket.helper;

import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.pack.ChatPackage;
import com.lcssit.icms.socket.pool.SessionPool;
import com.lcssit.icms.socket.session.Session;

public class SocketHerlper {
	private static SocketHerlper helper;
	public static SocketHerlper getInstance(){
		if (C.isNull(helper)) {
			helper=new SocketHerlper();
		}
		return helper;
	}
	private SocketHerlper(){}
	
	/**
	 * 转发聊天信息
	 * @param cp ChatPackage
	 */
	public boolean transmitMsg(ChatPackage cp,String targetSessionID){
		if (C.isNull(cp)||C.isNull(targetSessionID)) {
			throw new NullPointerException();
		}
		boolean isSend=false;
		if (C.isNotNull(targetSessionID)) {
			int selfUserID=cp.getSelfUserID();
			//角色互转,发送给客户端的时候,客户端声明的自己的ID换为对方的ID  对方的ID换为自己的ID
			cp.setSelfUserID(cp.getTargetUserID());
			cp.setTargetUserID(selfUserID);
			try {
				Session targetSession=SessionPool.getInstance().get(targetSessionID);//取得对方客户端的Session对象
				if (C.isNotNull(targetSession)) {
					targetSession.writePackage(cp);//发送
					isSend=true;
				}
			} catch (WritePackageException e) {
				e.printStackTrace();
			}
		}
		return isSend;
	}
	
	/**
	 * 根据SessionId销毁一个Session 该session的hander会收到销毁事件
	 * @param sessionId
	 */
	public void recoverSession(String sessionId){
		SessionPool.getInstance().get(sessionId).recover();
	}
//	
//	/**
//	 * 向客户端发送文件
//	 * @param file 文件对象
//	 * @param cmd 附带的命令 可以为空 不能为 null
// 	 * @param sessionId 客户端的SessionId
//	 * @throws WritePackageException
//	 */
//	public void sendFile(File file,String cmd,String sessionId) throws WritePackageException{
//		if (C.isNull(file)||C.isNull(cmd)) {
//			throw new NullPointerException();
//		}
//		SyncFilePackage fp=new SyncFilePackage();
//		fp.setBody(cmd);
//		fp.setWriteFile(file);
//		SessionPool.getInstance().get(sessionId).writePackage(fp);;
//	}
	
}
