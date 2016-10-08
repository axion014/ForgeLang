package io.github.axion014.forgelang.analyze.word;

public class OmniStr extends Value {
	public String value;
	public int pos;
	public OmniStr next;
	private static OmniStr strings;
	public OmniStr(String value) {
		this.value = value;
		length = value.length() + 2;
		pos = strings != null ? strings.pos + 1 : 1;
		next = strings;
		strings = this;
	}
	@Override
	public String _toString() {
		return "\"" + value + "\"";
	}
}
