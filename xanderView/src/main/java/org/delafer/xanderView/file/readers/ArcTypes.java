package org.delafer.xanderView.file.readers;

import java.util.HashMap;
import java.util.Map;

public enum ArcTypes {
	ZIP("zip"),
	RAR("rar"),
	SEVENZIP("7z"),
	JZIP("jzip");

	static Map<String, ArcTypes> map = new HashMap<String, ArcTypes>(4);
	static {
		for (ArcTypes next : ArcTypes.values()) { map.put(next.type, next); }
	}

	public static ArcTypes from(String ext) {
		if (ext == null) return null;
		return map.get(ext.toLowerCase());
	}

	public static boolean isArcType(String ext) {
		return from(ext) != null;
	}

	String type;
	ArcTypes(String arg) {
		this.type = arg;
	}
}
