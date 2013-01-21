package com.lcssit.icms.socket.config;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ConfigManager {

	private static ConfigManager instance;

	public static ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	private SAXReader reader = new SAXReader();

	private ConfigManager() {
		reader = new SAXReader();
	}

	public String getValue(String name) throws DocumentException {
		String value = null;
		Document doc = this.getDocument();
		@SuppressWarnings("unchecked")
		List<Element> list = doc.getRootElement().elements();
		for (Element element : list) {
			if (element.attributeValue("name").equals(name)) {
				value = element.attributeValue("value");
			}
		}
		return value;
	}

	private Document getDocument() throws DocumentException {
		return reader.read(this.getPath());
	}

	private String getPath() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL sourceUrl = loader.getResource("");
		String path = null;
		try {
			path = URLDecoder.decode(sourceUrl.getFile(), "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return path.replaceFirst("/", "") + "config.xml";
	}

	public int getPort() throws NumberFormatException, DocumentException {
		return new Integer(this.getValue("port"));
	}

	public int getSessionTimeOut() throws NumberFormatException,
			DocumentException {
		return new Integer(this.getValue("session_time_out"));
	}

	public int getSocketSoTimeout() throws NumberFormatException,
			DocumentException {
		return new Integer(this.getValue("socket_so_timeout"));
	}

	public int getStatusRefreshInterval() throws NumberFormatException, DocumentException {
		return new Integer(this.getValue("status_refresh_interval"));
	}
}
