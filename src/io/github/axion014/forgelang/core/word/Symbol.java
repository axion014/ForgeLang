package io.github.axion014.forgelang.core.word;

public class Symbol extends Value {
	public String name;
	public String type;
	public int pos;
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
		pos = original.pos;
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

	public Symbol asParam() {
		return new Symbol(original.asParam());
	}
}
