package io.github.axion014.forgelang.analyze.word;

public class UnaryOperator extends Word {
	public UnaryOperatorType type;
	@Override
	public String _toString() {
		return type.content;
	}
}
