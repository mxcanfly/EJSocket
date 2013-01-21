package com.lcssit.icms.socket.pack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.lcssit.icms.socket.exception.ReadDataPackageException;
import com.lcssit.icms.socket.exception.WritePackageException;
import com.lcssit.icms.socket.helper.C;
import com.lcssit.icms.socket.pack.i.IPackage;

public class SyncFilePackage extends IPackage
{
	public static final byte HEAD = 0x27, FOOT = 0x27;
	
	private long readFileLength;
	private String readFileName;
	private File writeFile;
	private File readFile;
	private int buffSize=1024;
	private String tmpCacheDir="./temp/";
	private int readSleepTime=0;
	private int writeSleepTime=0;

	
	public SyncFilePackage() {
		this.setHead(SyncFilePackage.HEAD);
		this.setFoot(SyncFilePackage.FOOT);
	}
	
	@Override
	public void read(DataInputStream in) throws ReadDataPackageException {
		try {
			
			this.bodyLenght=in.readInt();
			this.body=new byte[this.bodyLenght];
			in.readFully(this.body);
			
			int fileNameArrLength = in.readInt();//1.读取文件名字节数组长度
			byte[] fileNameArr=new byte[fileNameArrLength];
			in.readFully(fileNameArr);//2.读取文件名字节数组
			this.readFileName=new String(fileNameArr,Charset.forName("UTF-8"));//还原成字符串
			this.readFileLength=in.readLong();//3.读取文件总长度
			this.readFile=createFile();
			FileOutputStream fout = new FileOutputStream(this.readFile);
			byte[] buff=new byte[buffSize];
			long readCompleteLength=0;
			int rate=0;
			//4.读取文件
			while(readCompleteLength<this.readFileLength){
				if (this.readFileLength-readCompleteLength<buff.length) {
					buff=new byte[(int) (this.readFileLength-readCompleteLength)];
				}
				in.readFully(buff);
				fout.write(buff);
				readCompleteLength+=buff.length;
				if (this.readSleepTime>0) {
					try {
						Thread.sleep(this.readSleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if ((int)((double)readCompleteLength/this.readFileLength*100)>rate) {
					rate=(int)((double)readCompleteLength/this.readFileLength*100);
					if (rate%5==0) {
						System.out.println("接收文件进度:"+rate+"%");
					}
				}
				
			}
			fout.close();
		} catch (IOException e) {
			throw new ReadDataPackageException("接收文件失败");
		}
	}

	private File createFile() {
		File tempDir=new File(this.tmpCacheDir);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		return new File(this.tmpCacheDir+readFileName);
	}

	@Override
	public void write(DataOutputStream out) throws WritePackageException {
		try {
			
			if (C.isNull(this.body)) {
				throw new NullPointerException();
			}
			out.writeInt(this.getBody().length);
			out.write(this.body);
			
			byte[] fileNameArr=this.writeFile.getName().toLowerCase().getBytes(Charset.forName("UTF-8"));
			out.writeInt(this.writeFile.getName().getBytes().length);//1.写入文件名字节数组长度
			out.write(fileNameArr);//2.写入文件名字节数组
			long fileLength=this.writeFile.length();
			
			out.writeLong(fileLength);//3.写入文件长度
			FileInputStream fin=new FileInputStream(this.writeFile);
			byte[] buff=new byte[buffSize];
			long writeCompleteLength=0;
			
			int rate=0;
			
			//4.发送文件
			while(writeCompleteLength<fileLength){
				if (fileLength-writeCompleteLength<buff.length) {
					buff=new byte[(int) (fileLength-writeCompleteLength)];
				}
				fin.read(buff);
				out.write(buff);
				writeCompleteLength+=buff.length;
				if (this.writeSleepTime>0) {
					try {
						Thread.sleep(this.writeSleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if ((int)((double)writeCompleteLength/fileLength*100)>rate) {
					rate=(int)((double)writeCompleteLength/fileLength*100);
					if (rate%5==0) {
						System.out.println("发送文件进度:"+rate+"%");
					}
				}
			}
			fin.close();
		} catch (FileNotFoundException e) {
			throw new WritePackageException("无法读取本地文件");
		} catch (IOException e) {
			throw new WritePackageException("本地文件或向流写入文件失败");
		} 
		
	}

	public void setWriteFile(File writeFile) {
		this.writeFile = writeFile;
	}

	public String getReadFileName() {
		return readFileName;
	}

	public int getBuffSize() {
		return buffSize;
	}

	public File getReadFile() {
		return readFile;
	}

	public void setBuffSize(int buffSize) {
		if (buffSize>0) {
			this.buffSize = buffSize;
		}
	}

	public int getReadSleepTime() {
		return readSleepTime;
	}

	public void setReadSleepTime(int readSleepTime) {
		if (readSleepTime>0) {
			this.readSleepTime = readSleepTime;
		}
	}
	
	public void setCacheFileDir(String path){
		if (C.isNotNull(path)) {
			this.tmpCacheDir=path;
		}
	}

	public int getWriteSleepTime() {
		return writeSleepTime;
	}

	public void setWriteSleepTime(int writeSleepTime) {
		if (writeSleepTime>0) {
			this.writeSleepTime = writeSleepTime;
		}
	}
}

