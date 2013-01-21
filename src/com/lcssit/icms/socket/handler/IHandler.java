package com.lcssit.icms.socket.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.helper.C;
import com.lcssit.icms.socket.pack.AsynDataPackage;
import com.lcssit.icms.socket.pack.ChatPackage;
import com.lcssit.icms.socket.pack.IDataPackage;
import com.lcssit.icms.socket.pack.SyncDataPackage;
import com.lcssit.icms.socket.pack.SyncFilePackage;
import com.lcssit.icms.socket.pack.i.IPackage;
import com.lcssit.icms.socket.session.Session;

/**
 * 此类是使用本框架要实现的抽象类
 * 
 * @author MxCc
 */
public abstract class IHandler {
	private Gson gson = new Gson();
	private Session session;

	/**
	 * 在 new
	 * SocketServer的时候,提供IHandler的实现类的class对象,当有客户端请求时,会使用反射获得一个Handler的对象,
	 * 并将Session传入
	 * 
	 * @param session
	 *            ,与客户端会话的Session对象
	 */
	public IHandler(Session session) {
		this.session = session;
	}

	/**
	 * 当有客户端数据请求时被Session的线程调用
	 * 
	 * @param 数据包
	 */
	public void onRequest(IPackage ipack) {
		// System.out.println("收到的数据:" + ipack.getBodyString());
		switch (ipack.getHead()) {
		case SyncDataPackage.HEAD:
			this.doSyncDataRequest(ipack);
			break;
		case AsynDataPackage.HEAD:
			this.doAsynDataRequest(ipack);
			break;
		case ChatPackage.HEAD:
			this.doChatRequest(ipack);
			break;
		case SyncFilePackage.HEAD:
			this.doSyncFileRequest(ipack);
			break;
		}
	}

	/**
	 * 当有客户端向服务器发送同步文件
	 * 
	 * @param ipack
	 */
	protected void doSyncFileRequest(IPackage ipack) {
		SyncFilePackage fp = (SyncFilePackage) ipack;
		System.out.println(fp.getBodyString());
		try {
			this.syncDataSuccessfully("文件名:" + fp.getReadFileName());
		} catch (WritePackageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 当有 *同步* 数据请求时调用
	 * 
	 * @param ipack
	 *            SyncDataPackage
	 */
	protected abstract void doSyncDataRequest(IPackage ipack);

	/**
	 * 当有 *异步* 数据请求时调用
	 * 
	 * @param ipack
	 *            AsynDataPackage
	 */
	protected abstract void doAsynDataRequest(IPackage ipack);

	/**
	 * 当有聊天转发请求时调用,可以使用封装的 transmitMsg() 方法实现快速转发.
	 * 在transmitMsg()执行完毕之后,selfUserID与targetUserID里面的值将会调换
	 * 
	 * @param ipack
	 */
	protected abstract void doChatRequest(IPackage ipack);

	/**
	 * 返回 *同步* 数据,并且通知客户端处理成功
	 * 
	 * @param data
	 *            要返回的数据
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	protected void syncDataSuccessfully(String data)
			throws WritePackageException {
		this.returnData(data, new SyncDataPackage(), true);
	}

	/**
	 * 不返回数据,但是通知客户端 *同步* 处理成功
	 * 
	 * @param data
	 *            要返回的数据
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	protected void syncDataSuccessfully() throws WritePackageException {
		this.returnData(null, new SyncDataPackage(), true);
	}

	/**
	 * 同步返回文件 成功
	 * 
	 * @param file
	 *            文件
	 * @param data
	 *            文本数据
	 * @throws WritePackageException
	 */
	protected void syncFileSuccessfully(File file, String data)
			throws WritePackageException {
		this.syncFileSuccessfully(file, data, (short) 0);
	}

	/**
	 * 同步返回文件 成功
	 * 
	 * @param file
	 *            文件
	 * @param data
	 *            文本数据
	 * @param buffSize
	 *            缓冲区大小
	 * @throws WritePackageException
	 */
	protected void syncFileSuccessfully(File file, String data, short buffSize)
			throws WritePackageException {
		if (C.isNull(file)||null==data||(!file.exists())) {
			throw new NullPointerException("要发送的文件对象为空,或者不存在");
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mark", "");
		map.put("success", "true");
		if (C.isNotNull(data)) {
			map.put("result", data);
		}
		SyncFilePackage fp = new SyncFilePackage();
		fp.setBody(gson.toJson(map));
		if (buffSize > 0) {
			fp.setBuffSize(buffSize);
		}
		fp.setWriteFile(file);
		this.session.writePackage(fp);
	}

	/**
	 * 通知客户端 *同步* 处理失败
	 * 
	 * @param data
	 *            错误信息
	 * @throws WritePackageException
	 */
	protected void syncDataError(String data) throws WritePackageException {
		this.returnData(data, new SyncDataPackage(), false);
	}

	/**
	 * 通知客户端 *同步* 处理失败
	 * 
	 * @throws WritePackageException
	 */
	protected void syncDataError() throws WritePackageException {
		this.returnData(null, new SyncDataPackage(), false);
	}

	/****************************************************************************/
	/**
	 * 返回 *异步* 数据,并且通知客户端处理成功
	 * 
	 * @param data
	 *            要返回的数据
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	protected void asynDataSuccessfully(String data)
			throws WritePackageException {
		this.returnData(data, new AsynDataPackage(), true);
	}

	/**
	 * 不返回数据,但是通知客户端 *异步* 处理成功
	 * 
	 * @param data
	 *            要返回的数据
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	protected void asynDataSuccessfully() throws WritePackageException {
		this.returnData(null, new AsynDataPackage(), true);
	}

	/**
	 * 通知客户端 *异步* 处理失败
	 * 
	 * @param data
	 *            错误信息
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	protected void asynDataError(String data) throws WritePackageException {
		this.returnData(data, new AsynDataPackage(), false);
	}

	/**
	 * 通知客户端 *异步* 处理失败
	 * 
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	protected void asynDataError() throws WritePackageException {
		this.returnData(null, new AsynDataPackage(), false);
	}

	/**
	 * 向客户端发送数据
	 * 
	 * @param data
	 *            要发送的数据
	 * @param ip
	 *            数据包对象 *同步* SyncDataPackage 或者 *异步* AsynDataPackage
	 * @param isSuccess
	 *            成功或者失败的标志
	 * @throws WritePackageException
	 *             返回数据异常
	 */
	private void returnData(String data, IDataPackage ip, boolean isSuccess)
			throws WritePackageException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mark", "");
		map.put("success", isSuccess ? "true" : "false");
		if (C.isNotNull(data)) {
			map.put("result", data);
		}
		ip.full(gson.toJson(map));
		session.writePackage(ip);
	}

	/**
	 * 获得Session对象
	 * 
	 * @return Session对象
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * 当与服务器连接成功
	 */
	public abstract void onOnline();

	/**
	 * 当与服务器重新连接成功
	 */
	public abstract void onReOnline();

	/**
	 * 当与服务器断开连接
	 */
	public abstract void onDisconnect();

	/**
	 * 等待重新连接已经超时,将要回收此线程客户端时调用.
	 */
	public abstract void onDownline();

}
