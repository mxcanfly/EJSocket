package com.lcssit.example;

import java.util.ArrayList;
import java.util.List;

import com.lcssit.icms.socket.helper.C;

public class UserOnlineTable {
	private UserOnlineTable(){}
	private static UserOnlineTable uot;
	public static UserOnlineTable getInstance() {
		if (C.isNull(uot)) {
			uot = new UserOnlineTable();
		}
		return uot;
	}
	
	private List<UserEntity> list=new ArrayList<UserEntity>();
	
	public void addUser(int userId,String sessionID,String userIP){
		UserEntity ue=new UserEntity();
		ue.setUserId(userId);
		ue.setSessionID(sessionID);
		ue.setUserIP(userIP);
		list.add(ue);
	}
	
	/**
	 * 根据SessionID获取UserID
	 * @param sessionID
	 * @return
	 */
	public Integer getUserIdBySessionID(String sessionID){
		for (UserEntity ue : this.list) {
			if (ue.getSessionID().equals(sessionID)) {
				return ue.getUserId();
			}
		}
		return null;
	}
	
	/**
	 * 根据UserID获取SessionID
	 * @param userId
	 * @return
	 */
	public String getSessionIDByUserID(Integer userId){
		for (UserEntity ue : this.list) {
			if (ue.getUserId().equals(userId)) {
				return ue.getSessionID();
			}
		}
		return null;
	}
	
	/**
	 * 根据UserId获取UserIP
	 * @param userId
	 * @return
	 */
	public String getUserIPByUserId(Integer userId){
		for (UserEntity ue : this.list) {
			if (ue.getUserId().equals(userId)) {
				return ue.getUserIP();
			}
		}
		return null;
	}
	
	/**
	 * 根据SessionID获取UserIP
	 * @param sessionID
	 * @return
	 */
	public String getUserIPBySessionID(String sessionID){
		for (UserEntity ue : this.list) {
			if (ue.getSessionID().equals(sessionID)) {
				return ue.getUserIP();
			}
		}
		return null;
	}
	
	
	class UserEntity{
		private Integer userId;
		private String sessionID;
		private String userIP;
		public Integer getUserId() {
			return userId;
		}
		public void setUserId(Integer userId) {
			this.userId = userId;
		}
		public String getSessionID() {
			return sessionID;
		}
		public void setSessionID(String sessionID) {
			this.sessionID = sessionID;
		}
		public String getUserIP() {
			return userIP;
		}
		public void setUserIP(String userIP) {
			this.userIP = userIP;
		}
	}
}


