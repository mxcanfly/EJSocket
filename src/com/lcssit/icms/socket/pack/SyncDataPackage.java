package com.lcssit.icms.socket.pack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;

public class SyncDataPackage extends IDataPackage {

	public static final byte HEAD = 0x38, FOOT = 0x38;

	public SyncDataPackage() {
		this.setHead(SyncDataPackage.HEAD);
		this.setFoot(SyncDataPackage.FOOT);
	}

	@Override
	public void read(DataInputStream in) throws ReadDataPackageException {
		try {
			this.setBodyLenght(in.readInt());
			this.body = new byte[this.bodyLenght];
			in.readFully(this.body);
		} catch (IOException e) {
			throw new ReadDataPackageException(e.getMessage()
					+ "填充数据包失败,流读取异常.");
		}
	}

	@Override
	public void full(String bodyStr) {
		this.setBody(bodyStr);
	}

	@Override
	public void write(DataOutputStream out) throws WritePackageException {
		try {
			out.writeInt(this.getBodyLenght());
			out.write(this.getBody());
		} catch (Exception e) {
			throw new WritePackageException(e.getMessage() + "发送数据包异常,可能是连接中断.");
		}
	}

}
