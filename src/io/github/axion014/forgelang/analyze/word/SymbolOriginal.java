package io.github.axion014.forgelang.analyze.word;

import io.github.axion014.forgelang.analyze.Compiler;
import io.github.axion014.forgelang.analyze.Scope;

public class SymbolOriginal extends Symbol {

	public SymbolOriginal next;
	public Scope scope;

	public SymbolOriginal(String name, String type, Compiler env) {
		this.name = name;
		this.type = type;
		length = name.length();
		scope = env.scope;
		next = scope.variables;
		scope.variables = this;
	}

	@Override
	public SymbolOriginal asParam() {
		isparam = true;
		return this;
	}

	@Override
	public String _toString() {
		return type + " " + super._toString();
	}

	@Override
	public int pos() {
		int pos = 1;
		for (SymbolOriginal v = scope.variables; !v.name.equals(name); v = v.next)
			if (v.isparam == isparam) pos++;
		return pos;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SymbolOriginal && ((SymbolOriginal) obj).name.equals(name)
				&& ((SymbolOriginal) obj).scope == scope;
	}
}
