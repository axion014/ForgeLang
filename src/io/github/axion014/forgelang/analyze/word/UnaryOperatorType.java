package io.github.axion014.forgelang.analyze.word;

import java.util.HashMap;
import java.util.Map;

import io.github.axion014.forgelang.analyze.exception.NotOperatorException;

public enum UnaryOperatorType {
	INC("++", 1), DEC("--", 1);
	
	public int length;
	public int priority;
	public String content;
	private static final Map<String, UnaryOperatorType> TYPE_FROM_STR = new HashMap<>();

	static {
		for (UnaryOperatorType type : UnaryOperatorType.values()) {
			TYPE_FROM_STR.put(type.content, type);
		}
	}

	UnaryOperatorType(String content, int priority) {
		this.content = content;
		length = content.length();
		this.priority = priority;
	}

	public static UnaryOperatorType from(String name) throws NotOperatorException {
		UnaryOperatorType t = TYPE_FROM_STR.get(name);
		if (t == null) throw new NotOperatorException("Invaild operator \"" + name + "\"");
		return t;
	}

	public static boolean isExist(String name) {
		return TYPE_FROM_STR.containsKey(name);
	}
}
