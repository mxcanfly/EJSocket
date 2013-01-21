package com.lcssit.icms.socket.pack;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.pack.i.IPackage;

/**
 * �������ݰ�,�ͻ��˻�ÿ��һ��ʱ�䷢��һ��,����ά���ͻ��˵�����״̬
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
