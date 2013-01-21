package com.lcssit.icms.socket.helper;

import java.util.List;
import java.util.Map;

public class C {
	/**
	 * 判断对象是否为Null，若为Null则返回true否则返回false<br>
	 * 若判断对象为String类型，则在判断是否为Null或""<br>
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Object obj) {
		boolean flag = true;
		if (obj instanceof String) {
			String new_name = (String) obj;
			flag = (new_name == null || "".equals(new_name.trim()));
		} else if (obj instanceof List) {
			List<?> new_name = (List<?>) obj;
			flag = (new_name == null || new_name.isEmpty());
		} else if (obj instanceof Map) {
			Map<?, ?> new_name = (Map<?, ?>) obj;
			flag = (new_name == null || new_name.isEmpty());
		} else {
			flag = (obj == null);
		}
		return flag;
	}

	public static boolean isNotNull(Object obj) {
		return !isNull(obj);
	}
}
