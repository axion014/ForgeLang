package io.github.axion014.forgelang.analyze.word;

import io.github.axion014.forgelang.tool.Snippet;

public class FLInt extends Value {
	public int value;
	public FLInt(int value) {
		this.value = value;
		length = Snippet.stringSize(value);
	}
	@Override
	public String _toString() {
		return Integer.valueOf(value).toString();
	}
}
