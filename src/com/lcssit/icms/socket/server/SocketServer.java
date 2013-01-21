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
 * Socket的监听线程,每一个新的连接请求创建一个线程类维护.维护连接状态,断开超时时间. 
 * @author MxCc
 */
public class SocketServer extends Thread {

	public int port;
	public int socketSoTimeout;//Socket最大阻塞时间
	public int sessionTimeOut;//与客户端断开后的最大超时时间
	public int statusRefreshInterval;//刷新连接状态间隔
	
	private Class<? extends IHandler> clasz;

	public SocketServer(Class<? extends IHandler> clasz)
			throws NumberFormatException, DocumentException {
		this.clasz = clasz;
		
		this.port = ConfigManager.getInstance().getPort();
		this.socketSoTimeout=ConfigManager.getInstance().getSocketSoTimeout();//Socket最大阻塞时间
		this.sessionTimeOut = ConfigManager.getInstance().getSessionTimeOut();//读取客户端超时配置
		this.statusRefreshInterval = ConfigManager.getInstance().getStatusRefreshInterval();//读取断线后判断连接状态间隔配置
		this.start();
		System.out.println("服务器已经动...");
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
				client.setSoTimeout(socketSoTimeout);//设置最大阻塞时间

				in = new DataInputStream(client.getInputStream());
				out = new DataOutputStream(client.getOutputStream());

				String sessionID = this.readSessionID(in);//读取客户端注册的SessionID 36位UUID
				this.writeSessionTimeOut(out, sessionTimeOut);//向客户端发送会话超时时间
				boolean isReConn = false;
				//如果是新客户端,那么注册的SessionID将不会存在于会话池,如果是断线重练,则不为null.新客户端为new一个Session线程,断线重连则将新的Socket对象设置给Session,并且设置其为连接成功状态.
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
					session.getHandler().onReOnline();//重新连接成功回调
				}else{
					session.start();//启动线程
					session.getHandler().onOnline();//连接成功回调
				}
				System.out.println("客户端:" + sessionID+ (isReConn ? "重连" : "连接") + "上,当前在线人数:"+ SessionPool.getInstance().getSize());
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
	 * 读取SessionID
	 * @param in
	 * @return 36位UUID字符串
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
					+ "读取客户端SessionID失败..");
		}
	}
	
	/**
	 * 向客户端发送,服务器的Socket最大阻塞时间 毫秒
	 * @param out 输出流
	 * @param timeout Socket最大阻塞时间 毫秒
	 * @throws IOException 写入失败 
	 */
	private void writeSessionTimeOut(DataOutputStream out,int timeout) throws IOException{
		out.writeInt(timeout);
	}

	/**
	 * 通过反射获取Handler的对象
	 * @param s Session
	 * @return IHandler的实例
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
					+ "提供的Handler类初始化实例失败,该类可能没有继承自IHandler抽象类.");
		}
		return handler;
	}
}
