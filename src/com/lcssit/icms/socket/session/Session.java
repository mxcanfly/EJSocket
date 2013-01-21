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
	private boolean isConned = false;//与客户端的连接状态

	private int sessionTimeOut = 20;//客户端断线超时时间 分钟
	private int statusRefreshInterval = 100;//-断开后,每过多少毫秒判断一次是否连接成功
	
	private int waitTime = 0;//断开状态下,当前等待客户端的总时间,重新连接后清零.

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
	 * 框架发送数据总出口,所有的数据都会在此处发送,同步方法.
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
			throw new WritePackageException(e.getMessage() + "发送数据包异常,可能是连接中断.");
		}
	}

	@Override
	public void run() {
		byte head;
		IPackage pack = null;
		while (true) {
			// 如果断开连接,则等待
			while (!this.isConned()) {
				if ((this.waitTime += this.statusRefreshInterval) >= this.sessionTimeOut) {
					break;
				}// System.out.println("已经等待"+this.sessionID+"重连"+this.waitTime/1000+"秒");
				this.sleep(this.statusRefreshInterval);
			}
			// 如果等待超时,则销毁.
			if (this.waitTime >= this.sessionTimeOut) {
				this.recover();
				break;
			}
			try {
				head = inputStream.readByte();//读取包头
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
					pack.read(inputStream);//调用包自己的实现读取数据
					pack.setFoot(inputStream.readByte());//读取包尾
				} catch (ReadDataPackageException e1) {
					e1.printStackTrace();
					System.out.println("填充包体失败..与客户端断开,等待对方重连..");
					this.onSocketDisconnect();
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("读取包尾失败..");
					this.onSocketDisconnect();
					continue;
				}
				if (pack.getHead() == pack.getFoot()) {//头尾一直则调通知Handler
					handler.onRequest(pack);
				}else{
					try {
						inputStream.reset();
					} catch (IOException e) {
						System.out.println("inupt流数据排列异常");
					}
				}
			}
		}
	}

	/**
	 * 回收当前线程
	 */
	public void recover() {
		// socket
		this.handler.onDownline();//通知Handler,此线程要进行回收.
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
		System.out.println("客户端:" + this.getSessionID() + "等待超时,通知Jre回收,当前在线人数:"+ SessionPool.getInstance().getSize());

	}

	private void onSocketDisconnect() {
		this.setConned(false);
		this.handler.onDisconnect();
		System.out.println("客户端:"+this.getSessionID()+"掉线,等待对方重新连接..");
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
		System.out.println("客户端:" + this.getSessionID() + " 的线程被回收.......");
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
