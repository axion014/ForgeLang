package io.github.axion014.forgelang.core.word;

import java.util.HashMap;
import java.util.Map;

import io.github.axion014.forgelang.core.exception.NotOperatorException;

public enum BinaryOperatorType {
	SGN("=", 1), ADD("+", 2), SUB("-", 2), MUL("*", 3), DIV("/", 3), EQU("==", 1), LSR("<", 2), GRT(">", 2);
	public int length;
	public int priority;
	public String content;
	private static final Map<String, BinaryOperatorType> TYPE_FROM_STR = new HashMap<>();

	static {
		for (BinaryOperatorType type : BinaryOperatorType.values()) {
			TYPE_FROM_STR.put(type.content, type);
		}
	}

	BinaryOperatorType(String content, int priority) {
		this.content = content;
		length = content.length();
		this.priority = priority;
	}

	public static BinaryOperatorType from(String name) throws NotOperatorException {
		BinaryOperatorType t = TYPE_FROM_STR.get(name);
		if (t == null) if (t == null) throw new NotOperatorException("Invaild operator \"" + name + "\"");
		return t;
	}

	public static boolean isExist(String name) {
		return TYPE_FROM_STR.containsKey(name);
	}
}
