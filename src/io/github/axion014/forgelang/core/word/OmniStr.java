package io.github.axion014.forgelang.core.word;

import io.github.axion014.forgelang.core.Compiler;

public class OmniStr extends Value {
	public String value;
	public int pos;
	public OmniStr next;
	public OmniStr(String value, Compiler env) {
		this.value = value;
		length = value.length() + 2;
		pos = env.strings != null ? env.strings.pos + 1 : 1;
		next = env.strings;
		env.strings = this;
	}
	@Override
	public String _toString() {
		return "\"" + value + "\"";
	}
}
