package io.github.axion014.forgelang.analyze.word;

import java.util.List;

import io.github.axion014.forgelang.analyze.Compiler;
import io.github.axion014.forgelang.analyze.Scope;
import io.github.axion014.forgelang.analyze.exception.CompileFailedException;

public class Func extends Value {
	public String name;
	public List<Symbol> params;
	public Func next;
	public Scope scope;

	public Func(String name, Compiler env) throws CompileFailedException {
		this.name = name;
		length = name.length();
		next = env.functions;
		Scope parent = env.scope;
		scope = new Scope(() -> {
			params = env.getParams();
			return env.readBlock(true);
		}, env);
		env.scope = parent;
		env.functions = this;
	}

	@Override
	public String _toString() {
		StringBuilder ret = new StringBuilder(name);
		ret.append("(");
		for (Symbol param : params) {
			ret.append(param.toString());
			ret.append(",");
		}
		ret.deleteCharAt(ret.length() - 1);
		ret.append(")\n");
		for (Word expr : scope) {
			ret.append("\t");
			ret.append(expr.toString());
			ret.append("\n");
		}
		ret.deleteCharAt(ret.length() - 1);
		return ret.toString();
	}
}
