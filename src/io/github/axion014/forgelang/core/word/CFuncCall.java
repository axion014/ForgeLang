package io.github.axion014.forgelang.core.word;

import java.util.List;

public class CFuncCall extends Value {
	public String name;
	public List<Word> args;
	
	@Override
	public String _toString() {
		StringBuilder ret = new StringBuilder("c.");
		ret.append(name);
		ret.append("(");
		for (Word arg : args) {
			ret.append(arg.toString());
			ret.append(",");
		}
		ret.deleteCharAt(ret.length() - 1);
		ret.append(")");
		return ret.toString();
	}
}
