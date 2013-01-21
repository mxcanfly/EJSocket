package com.lcssit.example;

import org.dom4j.DocumentException;

import com.lcssit.icms.socket.server.SocketServer;

public class JSocketServerExample {
	public static void main(String[] args) {
		try {
			
			new SocketServer(Handler.class);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
}
