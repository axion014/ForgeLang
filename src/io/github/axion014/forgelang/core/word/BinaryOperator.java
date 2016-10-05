package io.github.axion014.forgelang.core.word;

public abstract class BinaryOperator extends Word {
	public BinaryOperatorType type;
	public Word left;
	public Word right;
	@Override
	public String _toString() {
		return "(" + left.toString() + type.content + right.toString() + ")";
	}
}
