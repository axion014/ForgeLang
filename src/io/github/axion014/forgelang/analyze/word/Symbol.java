package io.github.axion014.forgelang.analyze.word;

import io.github.axion014.forgelang.analyze.exception.CompilerInternalException;

public class Symbol extends Value {
	public String name;
	public String type;
	public boolean isparam;
	public UnaryOperator after = null;
	private SymbolOriginal original;
	public UnaryOperator before = null;

	Symbol() {} // do nothing

	public Symbol(SymbolOriginal original) {
		this.original = original;
		name = original.name;
		type = original.type;
		length = original.length;
		isparam = original.isparam;
	}

	@Override
	public String beforeToString() {
		return (before != null ? before.toString() : "") + super.beforeToString();
	}

	@Override
	public String _toString() {
		return name;
	}

	@Override
	public String afterToString() {
		return (after != null ? after.toString() : "") + super.afterToString();
	}

	public SymbolOriginal asParam() {
		throw new CompilerInternalException("can't set parameter flag to clone symbol");
	}

	public int pos() {
		return original.pos();
	}
}
