package com.lcssit.example;

import java.io.File;

import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.handler.IHandler;
import com.lcssit.icms.socket.pack.AsynDataPackage;
import com.lcssit.icms.socket.pack.SyncDataPackage;
import com.lcssit.icms.socket.pack.i.IPackage;
import com.lcssit.icms.socket.session.Session;

public class Handler extends IHandler {

	public Handler(Session session) {
		super(session);
	}
	
	@Override
	protected void doSyncDataRequest(IPackage ipack) {
		SyncDataPackage dp = (SyncDataPackage) ipack;
		System.out.println(dp.getBodyString());
		try {
			this.syncFileSuccessfully(new File("d:\\jdk-7u4-windows-x64.exe"), "haha");
		} catch (WritePackageException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doAsynDataRequest(IPackage ipack) {
		AsynDataPackage dp = (AsynDataPackage) ipack;
	}
	
	@Override
	protected void doChatRequest(IPackage ipack) {
	}

	@Override
	public void onOnline() {
		System.out.println("有客户端连接..");
		this.getSession().setFileTempCatchDir("./temp2/");
	}

	@Override
	public void onReOnline() {
		System.out.println("有客户端重新连接..");
	}

	@Override
	public void onDisconnect() {
		System.out.println("有客户端断开..");
		//System.out.println("onDisconnect"+UserOnlineTable.getInstance().getUserIDBySessionID(this.getSession().getSessionID()));
	}

	@Override
	public void onDownline() {
		System.out.println("有客户端下线..");
		//System.out.println("onDownline"+UserOnlineTable.getInstance().getUserIDBySessionID(this.getSession().getSessionID()));
	}
}
