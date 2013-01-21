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
 * ������ʹ�ñ����Ҫʵ�ֵĳ�����
 * 
 * @author MxCc
 */
public abstract class IHandler {
	private Gson gson = new Gson();
	private Session session;

	/**
	 * �� new
	 * SocketServer��ʱ��,�ṩIHandler��ʵ�����class����,���пͻ�������ʱ,��ʹ�÷�����һ��Handler�Ķ���,
	 * ����Session����
	 * 
	 * @param session
	 *            ,��ͻ��˻Ự��Session����
	 */
	public IHandler(Session session) {
		this.session = session;
	}

	/**
	 * ���пͻ�����������ʱ��Session���̵߳���
	 * 
	 * @param ���ݰ�
	 */
	public void onRequest(IPackage ipack) {
		// System.out.println("�յ�������:" + ipack.getBodyString());
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
	 * ���пͻ��������������ͬ���ļ�
	 * 
	 * @param ipack
	 */
	protected void doSyncFileRequest(IPackage ipack) {
		SyncFilePackage fp = (SyncFilePackage) ipack;
		System.out.println(fp.getBodyString());
		try {
			this.syncDataSuccessfully("�ļ���:" + fp.getReadFileName());
		} catch (WritePackageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ���� *ͬ��* ��������ʱ����
	 * 
	 * @param ipack
	 *            SyncDataPackage
	 */
	protected abstract void doSyncDataRequest(IPackage ipack);

	/**
	 * ���� *�첽* ��������ʱ����
	 * 
	 * @param ipack
	 *            AsynDataPackage
	 */
	protected abstract void doAsynDataRequest(IPackage ipack);

	/**
	 * ��������ת������ʱ����,����ʹ�÷�װ�� transmitMsg() ����ʵ�ֿ���ת��.
	 * ��transmitMsg()ִ�����֮��,selfUserID��targetUserID�����ֵ�������
	 * 
	 * @param ipack
	 */
	protected abstract void doChatRequest(IPackage ipack);

	/**
	 * ���� *ͬ��* ����,����֪ͨ�ͻ��˴���ɹ�
	 * 
	 * @param data
	 *            Ҫ���ص�����
	 * @throws WritePackageException
	 *             ���������쳣
	 */
	protected void syncDataSuccessfully(String data)
			throws WritePackageException {
		this.returnData(data, new SyncDataPackage(), true);
	}

	/**
	 * ����������,����֪ͨ�ͻ��� *ͬ��* ����ɹ�
	 * 
	 * @param data
	 *            Ҫ���ص�����
	 * @throws WritePackageException
	 *             ���������쳣
	 */
	protected void syncDataSuccessfully() throws WritePackageException {
		this.returnData(null, new SyncDataPackage(), true);
	}

	/**
	 * ͬ�������ļ� �ɹ�
	 * 
	 * @param file
	 *            �ļ�
	 * @param data
	 *            �ı�����
	 * @throws WritePackageException
	 */
	protected void syncFileSuccessfully(File file, String data)
			throws WritePackageException {
		this.syncFileSuccessfully(file, data, (short) 0);
	}

	/**
	 * ͬ�������ļ� �ɹ�
	 * 
	 * @param file
	 *            �ļ�
	 * @param data
	 *            �ı�����
	 * @param buffSize
	 *            ��������С
	 * @throws WritePackageException
	 */
	protected void syncFileSuccessfully(File file, String data, short buffSize)
			throws WritePackageException {
		if (C.isNull(file)||null==data||(!file.exists())) {
			throw new NullPointerException("Ҫ���͵��ļ�����Ϊ��,���߲�����");
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
	 * ֪ͨ�ͻ��� *ͬ��* ����ʧ��
	 * 
	 * @param data
	 *            ������Ϣ
	 * @throws WritePackageException
	 */
	protected void syncDataError(String data) throws WritePackageException {
		this.returnData(data, new SyncDataPackage(), false);
	}

	/**
	 * ֪ͨ�ͻ��� *ͬ��* ����ʧ��
	 * 
	 * @throws WritePackageException
	 */
	protected void syncDataError() throws WritePackageException {
		this.returnData(null, new SyncDataPackage(), false);
	}

	/****************************************************************************/
	/**
	 * ���� *�첽* ����,����֪ͨ�ͻ��˴���ɹ�
	 * 
	 * @param data
	 *            Ҫ���ص�����
	 * @throws WritePackageException
	 *             ���������쳣
	 */
	protected void asynDataSuccessfully(String data)
			throws WritePackageException {
		this.returnData(data, new AsynDataPackage(), true);
	}

	/**
	 * ����������,����֪ͨ�ͻ��� *�첽* ����ɹ�
	 * 
	 * @param data
	 *            Ҫ���ص�����
	 * @throws WritePackageException
	 *             ���������쳣
	 */
	protected void asynDataSuccessfully() throws WritePackageException {
		this.returnData(null, new AsynDataPackage(), true);
	}

	/**
	 * ֪ͨ�ͻ��� *�첽* ����ʧ��
	 * 
	 * @param data
	 *            ������Ϣ
	 * @throws WritePackageException
	 *             ���������쳣
	 */
	protected void asynDataError(String data) throws WritePackageException {
		this.returnData(data, new AsynDataPackage(), false);
	}

	/**
	 * ֪ͨ�ͻ��� *�첽* ����ʧ��
	 * 
	 * @throws WritePackageException
	 *             ���������쳣
	 */
	protected void asynDataError() throws WritePackageException {
		this.returnData(null, new AsynDataPackage(), false);
	}

	/**
	 * ��ͻ��˷�������
	 * 
	 * @param data
	 *            Ҫ���͵�����
	 * @param ip
	 *            ���ݰ����� *ͬ��* SyncDataPackage ���� *�첽* AsynDataPackage
	 * @param isSuccess
	 *            �ɹ�����ʧ�ܵı�־
	 * @throws WritePackageException
	 *             ���������쳣
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
	 * ���Session����
	 * 
	 * @return Session����
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * ������������ӳɹ�
	 */
	public abstract void onOnline();

	/**
	 * ����������������ӳɹ�
	 */
	public abstract void onReOnline();

	/**
	 * ����������Ͽ�����
	 */
	public abstract void onDisconnect();

	/**
	 * �ȴ����������Ѿ���ʱ,��Ҫ���մ��߳̿ͻ���ʱ����.
	 */
	public abstract void onDownline();

}
