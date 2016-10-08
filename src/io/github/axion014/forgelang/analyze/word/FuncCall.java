package io.github.axion014.forgelang.analyze.word;

import java.util.List;

public class FuncCall extends Value {
	public String name;
	public List<Word> args;
	public int retnum;

	@Override
	public String _toString() {
		StringBuilder ret = new StringBuilder(name);
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
