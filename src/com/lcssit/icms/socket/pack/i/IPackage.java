package com.lcssit.icms.socket.pack.i;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.Charset;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;

public abstract class IPackage {

	protected byte[] body;
	protected int bodyLenght;
	protected byte head, foot;

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
		this.bodyLenght = body.length;
	}

	public byte getHead() {
		return head;
	}

	public void setHead(byte head) {
		this.head = head;
	}

	public byte getFoot() {
		return foot;
	}

	public void setFoot(byte foot) {
		this.foot = foot;
	}

	public int getBodyLenght() {
		return bodyLenght;
	}

	public void setBodyLenght(int bodyLenght) {
		this.bodyLenght = bodyLenght;
	}

	public String getBodyString() {
		return new String(this.getBody(), Charset.forName("UTF-8"));
	}

	public void setBody(String bodyStr) {
		this.setBody(bodyStr.getBytes(Charset.forName("UTF-8")));
	}

	public abstract void read(DataInputStream in)
			throws ReadDataPackageException;

	public abstract void write(DataOutputStream out)
			throws WritePackageException;

}
