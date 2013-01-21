package com.lcssit.icms.socket.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.dom4j.DocumentException;

import com.lcssit.icms.socket.config.ConfigManager;
import com.lcssit.icms.socket.exception.NoReadSessionIDException;
import com.lcssit.icms.socket.exception.ReflectionHandlerException;
import com.lcssit.icms.socket.handler.IHandler;
import com.lcssit.icms.socket.helper.C;
import com.lcssit.icms.socket.pool.SessionPool;
import com.lcssit.icms.socket.session.Session;

/**
 * Socket�ļ����߳�,ÿһ���µ��������󴴽�һ���߳���ά��.ά������״̬,�Ͽ���ʱʱ��. 
 * @author MxCc
 */
public class SocketServer extends Thread {

	public int port;
	public int socketSoTimeout;//Socket�������ʱ��
	public int sessionTimeOut;//��ͻ��˶Ͽ�������ʱʱ��
	public int statusRefreshInterval;//ˢ������״̬���
	
	private Class<? extends IHandler> clasz;

	public SocketServer(Class<? extends IHandler> clasz)
			throws NumberFormatException, DocumentException {
		this.clasz = clasz;
		
		this.port = ConfigManager.getInstance().getPort();
		this.socketSoTimeout=ConfigManager.getInstance().getSocketSoTimeout();//Socket�������ʱ��
		this.sessionTimeOut = ConfigManager.getInstance().getSessionTimeOut();//��ȡ�ͻ��˳�ʱ����
		this.statusRefreshInterval = ConfigManager.getInstance().getStatusRefreshInterval();//��ȡ���ߺ��ж�����״̬�������
		this.start();
		System.out.println("�������Ѿ���...");
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		ServerSocket ss = null;
		Socket client = null;
		DataInputStream in = null;
		DataOutputStream out = null;
		Session session = null;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (true) {
			try {
				client = ss.accept();
				client.setSoTimeout(socketSoTimeout);//�����������ʱ��

				in = new DataInputStream(client.getInputStream());
				out = new DataOutputStream(client.getOutputStream());

				String sessionID = this.readSessionID(in);//��ȡ�ͻ���ע���SessionID 36λUUID
				this.writeSessionTimeOut(out, sessionTimeOut);//��ͻ��˷��ͻỰ��ʱʱ��
				boolean isReConn = false;
				//������¿ͻ���,��ôע���SessionID����������ڻỰ��,����Ƕ�������,��Ϊnull.�¿ͻ���Ϊnewһ��Session�߳�,�����������µ�Socket�������ø�Session,����������Ϊ���ӳɹ�״̬.
				if (C.isNull(SessionPool.getInstance().get(sessionID))) {
					session = new Session(sessionTimeOut,statusRefreshInterval);
					session.setSessionID(sessionID);
					session.registerHandler(this.newHanlder(session));
					SessionPool.getInstance().add(session.getSessionID(),session);
				} else {
					isReConn = true;
					session = SessionPool.getInstance().get(sessionID);
				}
				session.setSocket(client);
				session.setInputStream(in);
				session.setOutputStream(out);
				session.setConned(true);
				if (isReConn) {
					session.getHandler().onReOnline();//�������ӳɹ��ص�
				}else{
					session.start();//�����߳�
					session.getHandler().onOnline();//���ӳɹ��ص�
				}
				System.out.println("�ͻ���:" + sessionID+ (isReConn ? "����" : "����") + "��,��ǰ��������:"+ SessionPool.getInstance().getSize());
			} catch (SocketException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (NoReadSessionIDException e) {
				e.printStackTrace();
				continue;
			} catch (ReflectionHandlerException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	/**
	 * ��ȡSessionID
	 * @param in
	 * @return 36λUUID�ַ���
	 * @throws NoReadSessionIDException
	 */
	private String readSessionID(DataInputStream in)
			throws NoReadSessionIDException {
		try {
			byte[] buff = new byte[Session.SESSIONID_LENGTH];
			in.readFully(buff);
			return new String(buff);
		} catch (Exception e) {
			throw new NoReadSessionIDException(e.getMessage()
					+ "��ȡ�ͻ���SessionIDʧ��..");
		}
	}
	
	/**
	 * ��ͻ��˷���,��������Socket�������ʱ�� ����
	 * @param out �����
	 * @param timeout Socket�������ʱ�� ����
	 * @throws IOException д��ʧ�� 
	 */
	private void writeSessionTimeOut(DataOutputStream out,int timeout) throws IOException{
		out.writeInt(timeout);
	}

	/**
	 * ͨ�������ȡHandler�Ķ���
	 * @param s Session
	 * @return IHandler��ʵ��
	 * @throws ReflectionHandlerException
	 */
	private IHandler newHanlder(Session s) throws ReflectionHandlerException {

		IHandler handler = null;
		Constructor<? extends IHandler> con = null;
		try {
			con = clasz.getConstructor(Session.class);
			handler = (IHandler) con.newInstance(s);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReflectionHandlerException(e.getMessage()
					+ "�ṩ��Handler���ʼ��ʵ��ʧ��,�������û�м̳���IHandler������.");
		}
		return handler;
	}
}
