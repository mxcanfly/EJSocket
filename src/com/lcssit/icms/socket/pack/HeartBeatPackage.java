package com.lcssit.icms.socket.pack;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.pack.i.IPackage;

/**
 * 心跳数据包,客户端会每隔一定时间发送一个,用来维护客户端的在线状态
 * @author MxCc
 */
public class HeartBeatPackage extends IPackage {

	public static final byte HEAD = 0x00, FOOT = 0x00;
	
	public HeartBeatPackage(){
		this.setHead(HeartBeatPackage.HEAD);
		this.setFoot(HeartBeatPackage.FOOT);
	}
	
	@Override
	public void read(DataInputStream in) throws ReadDataPackageException {
	}

	@Override
	public void write(DataOutputStream out) throws WritePackageException {
	}

}
