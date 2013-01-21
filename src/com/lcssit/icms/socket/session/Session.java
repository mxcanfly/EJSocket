package com.lcssit.icms.socket.session;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.handler.IHandler;
import com.lcssit.icms.socket.helper.C;
import com.lcssit.icms.socket.pack.AsynDataPackage;
import com.lcssit.icms.socket.pack.ChatPackage;
import com.lcssit.icms.socket.pack.SyncFilePackage;
import com.lcssit.icms.socket.pack.HeartBeatPackage;
import com.lcssit.icms.socket.pack.SyncDataPackage;
import com.lcssit.icms.socket.pack.i.IPackage;
import com.lcssit.icms.socket.pool.SessionPool;

public class Session extends Thread {

	public static final int SESSIONID_LENGTH = 36;
	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private String sessionID;
	private IHandler handler;
	private boolean isConned = false;//��ͻ��˵�����״̬

	private int sessionTimeOut = 20;//�ͻ��˶��߳�ʱʱ�� ����
	private int statusRefreshInterval = 100;//-�Ͽ���,ÿ�����ٺ����ж�һ���Ƿ����ӳɹ�
	
	private int waitTime = 0;//�Ͽ�״̬��,��ǰ�ȴ��ͻ��˵���ʱ��,�������Ӻ�����.

	private int fileCacheSize=0;
	private int readSleepTime=0;
	private int writeSleepTime;
	private String fileTempCatchDir;

	public Session(int sessionTimeOut, int statusRefreshInterval) {
		this.sessionTimeOut=sessionTimeOut;
		this.statusRefreshInterval=statusRefreshInterval;
	}

	public void registerHandler(IHandler handler) {
		this.handler = handler;
	}

	/**
	 * ��ܷ��������ܳ���,���е����ݶ����ڴ˴�����,ͬ������.
	 * @param ip
	 * @throws WritePackageException
	 */
	public synchronized void writePackage(IPackage ip)
			throws WritePackageException {
		try {
			outputStream.writeByte(ip.getHead());
			ip.write(this.outputStream);
			outputStream.writeByte(ip.getFoot());
		} catch (Exception e) {
			throw new WritePackageException(e.getMessage() + "�������ݰ��쳣,�����������ж�.");
		}
	}

	@Override
	public void run() {
		byte head;
		IPackage pack = null;
		while (true) {
			// ����Ͽ�����,��ȴ�
			while (!this.isConned()) {
				if ((this.waitTime += this.statusRefreshInterval) >= this.sessionTimeOut) {
					break;
				}// System.out.println("�Ѿ��ȴ�"+this.sessionID+"����"+this.waitTime/1000+"��");
				this.sleep(this.statusRefreshInterval);
			}
			// ����ȴ���ʱ,������.
			if (this.waitTime >= this.sessionTimeOut) {
				this.recover();
				break;
			}
			try {
				head = inputStream.readByte();//��ȡ��ͷ
			} catch (IOException e) {
				this.onSocketDisconnect();
				continue;
			}
			switch (head) {
				case SyncDataPackage.HEAD:
					pack = new SyncDataPackage();
					break;
				case AsynDataPackage.HEAD:
					pack = new AsynDataPackage();
					break;
				case ChatPackage.HEAD:
					pack = new ChatPackage();
					break;
				case HeartBeatPackage.HEAD:
					pack=new HeartBeatPackage();
					break;
				case SyncFilePackage.HEAD:
					pack=new SyncFilePackage();
					((SyncFilePackage)pack).setBuffSize(this.fileCacheSize);
					((SyncFilePackage)pack).setReadSleepTime(this.readSleepTime);
					((SyncFilePackage)pack).setCacheFileDir(this.fileTempCatchDir);
					((SyncFilePackage)pack).setWriteSleepTime(this.writeSleepTime);
					break;
			}
			if (C.isNotNull(pack)) {
				pack.setHead(head);
				try {
					pack.read(inputStream);//���ð��Լ���ʵ�ֶ�ȡ����
					pack.setFoot(inputStream.readByte());//��ȡ��β
				} catch (ReadDataPackageException e1) {
					e1.printStackTrace();
					System.out.println("������ʧ��..��ͻ��˶Ͽ�,�ȴ��Է�����..");
					this.onSocketDisconnect();
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("��ȡ��βʧ��..");
					this.onSocketDisconnect();
					continue;
				}
				if (pack.getHead() == pack.getFoot()) {//ͷβһֱ���֪ͨHandler
					handler.onRequest(pack);
				}else{
					try {
						inputStream.reset();
					} catch (IOException e) {
						System.out.println("inupt�����������쳣");
					}
				}
			}
		}
	}

	/**
	 * ���յ�ǰ�߳�
	 */
	public void recover() {
		// socket
		this.handler.onDownline();//֪ͨHandler,���߳�Ҫ���л���.
		try {
			this.outputStream.close();
			this.inputStream.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.outputStream = null;
		this.inputStream = null;
		this.socket = null;
		this.handler = null;
		System.gc();
		// pool
		SessionPool.getInstance().remove(this.getSessionID());
		System.out.println("�ͻ���:" + this.getSessionID() + "�ȴ���ʱ,֪ͨJre����,��ǰ��������:"+ SessionPool.getInstance().getSize());

	}

	private void onSocketDisconnect() {
		this.setConned(false);
		this.handler.onDisconnect();
		System.out.println("�ͻ���:"+this.getSessionID()+"����,�ȴ��Է���������..");
	}

	private void sleep(int s) {
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("�ͻ���:" + this.getSessionID() + " ���̱߳�����.......");
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public DataInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(DataInputStream inputStream) {
		this.inputStream = inputStream;
	}

	public DataOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(DataOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public boolean isConned() {
		return isConned;
	}

	public void setConned(boolean isConned) {
		if (isConned == true) {
			this.waitTime = 0;
		}
		this.isConned = isConned;
	}

	public IHandler getHandler() {
		return handler;
	}

	
	
	
	public int getFileCacheSize() {
		return fileCacheSize;
	}

	public void setFileCacheSize(int fileCacheSize) {
		this.fileCacheSize = fileCacheSize;
	}

	public int getReadSleepTime() {
		return readSleepTime;
	}

	public void setReadSleepTime(int readSleepTime) {
		this.readSleepTime = readSleepTime;
	}

	public int getWriteSleepTime() {
		return writeSleepTime;
	}

	public void setWriteSleepTime(int writeSleepTime) {
		this.writeSleepTime = writeSleepTime;
	}

	public String getFileTempCatchDir() {
		return fileTempCatchDir;
	}

	public void setFileTempCatchDir(String fileTempCatchDir) {
		this.fileTempCatchDir = fileTempCatchDir;
	}
	
	
}
