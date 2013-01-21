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
	 * ת��������Ϣ
	 * @param cp ChatPackage
	 */
	public boolean transmitMsg(ChatPackage cp,String targetSessionID){
		if (C.isNull(cp)||C.isNull(targetSessionID)) {
			throw new NullPointerException();
		}
		boolean isSend=false;
		if (C.isNotNull(targetSessionID)) {
			int selfUserID=cp.getSelfUserID();
			//��ɫ��ת,���͸��ͻ��˵�ʱ��,�ͻ����������Լ���ID��Ϊ�Է���ID  �Է���ID��Ϊ�Լ���ID
			cp.setSelfUserID(cp.getTargetUserID());
			cp.setTargetUserID(selfUserID);
			try {
				Session targetSession=SessionPool.getInstance().get(targetSessionID);//ȡ�öԷ��ͻ��˵�Session����
				if (C.isNotNull(targetSession)) {
					targetSession.writePackage(cp);//����
					isSend=true;
				}
			} catch (WritePackageException e) {
				e.printStackTrace();
			}
		}
		return isSend;
	}
	
	/**
	 * ����SessionId����һ��Session ��session��hander���յ������¼�
	 * @param sessionId
	 */
	public void recoverSession(String sessionId){
		SessionPool.getInstance().get(sessionId).recover();
	}
//	
//	/**
//	 * ��ͻ��˷����ļ�
//	 * @param file �ļ�����
//	 * @param cmd ���������� ����Ϊ�� ����Ϊ null
// 	 * @param sessionId �ͻ��˵�SessionId
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
