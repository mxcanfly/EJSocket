package com.lcssit.icms.socket.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lcssit.icms.socket.helper.C;
import com.lcssit.icms.socket.session.Session;

public class SessionPool {

	private SessionPool(){}
	private static SessionPool cctp;
	private Map<String, Session> map = new HashMap<String, Session>();

	public static SessionPool getInstance() {
		if (C.isNull(cctp)) {
			cctp = new SessionPool();
		}
		return cctp;
	}

	public List<Session> getAll() {
		List<Session> list = new ArrayList<Session>();
		Iterator<Session> it=map.values().iterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	public synchronized Session get(String key) {
		return map.get(key);
	}

	public synchronized void add(String sessionID,Session c) {
		map.put(sessionID, c);
	}

	public synchronized void remove(String key) {
		map.remove(key);
	}

	public int getSize() {
		return map.size();
	}
}
