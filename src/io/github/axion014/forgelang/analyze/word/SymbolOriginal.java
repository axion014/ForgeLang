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
		pos = env.scope.variables != null ? env.scope.variables.pos + 1 : 1;
		next = env.scope.variables;
		env.scope.variables = this;
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
}
