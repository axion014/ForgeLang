package io.github.axion014.forgelang.core.word;

public abstract class Word {
	public boolean isReturnWord = false;
	@Override
	public String toString() {
		return beforeToString() + _toString() + afterToString();
	}
	protected abstract String _toString();
	public String beforeToString() {
		return isReturnWord ? "(ret " : "";
	}
	public String afterToString() {
		return isReturnWord ? ")" : "";
	}
}
