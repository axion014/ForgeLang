package io.github.axion014.forgelang.core.word;

import io.github.axion014.forgelang.core.Snippet;

public class OmniInt extends Value {
	public int value;
	public OmniInt(int value) {
		this.value = value;
		length = Snippet.stringSize(value);
	}
	@Override
	public String _toString() {
		return Integer.valueOf(value).toString();
	}
}
