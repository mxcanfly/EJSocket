package com.lcssit.icms.socket.pack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.pack.i.IPackage;

/**
 * 聊天数据包
 * @author MxCc
 */
public class ChatPackage extends IPackage {

	public static final byte HEAD = 0x39, FOOT = 0x39;
	private int targetUserID;
	private int selfUserID;
	private int msgType;
	private byte[] userName;
	private int userNameLength;
	
	public ChatPackage(){
		this.setHead(ChatPackage.HEAD);
		this.setFoot(ChatPackage.FOOT);
	}

	@Override
	public void read(DataInputStream in) throws ReadDataPackageException {
		try {
			this.setBodyLenght(in.readInt());
			this.setSelfUserID(in.readInt());
			this.setUserNameLength(in.readInt());
			this.userName=new byte[this.getUserNameLength()];
			in.readFully(this.userName);
			this.setTargetUserID(in.readInt());
			this.setMsgType(in.readInt());
			this.body = new byte[this.bodyLenght];
			in.readFully(this.body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReadDataPackageException(e.getMessage()
					+ "填充数据包失败,流读取异常.");
		}
	}

	public void full(String bodyStr, int selfUserID,String selfUserName,int targetUserID,int msgType) {
		this.setBody(bodyStr);
		this.setTargetUserID(targetUserID);
		this.setSelfUserID(selfUserID);
		this.setUserName(selfUserName);
		this.setUserNameLength(this.userName.length);
		this.setMsgType(msgType);
	}

	@Override
	public void write(DataOutputStream out) throws WritePackageException {
		try {
			out.writeInt(this.bodyLenght);
			out.writeInt(this.selfUserID);
			out.writeInt(this.userNameLength);
			out.write(this.userName);
			out.writeInt(this.targetUserID);
			out.writeInt(this.msgType);
			out.write(this.body);
		} catch (Exception e) {
			throw new WritePackageException(e.getMessage() + "发送数据包异常,可能是连接中断.");
		}
	}

	public int getTargetUserID() {
		return targetUserID;
	}

	public void setTargetUserID(int targetUserID) {
		this.targetUserID = targetUserID;
	}

	public int getSelfUserID() {
		return selfUserID;
	}

	public void setSelfUserID(int selfUserID) {
		this.selfUserID = selfUserID;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getUserName() {
		return new String(this.userName, Charset.forName("UTF-8"));
	}

	public void setUserName(String selfUserName) {
		this.userName = selfUserName.getBytes(Charset.forName("UTF-8"));
	}

	public int getUserNameLength() {
		return userNameLength;
	}

	public void setUserNameLength(int selfUserNameLength) {
		this.userNameLength = selfUserNameLength;
	}
}
