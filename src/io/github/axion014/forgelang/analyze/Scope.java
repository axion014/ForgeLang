package io.github.axion014.forgelang.analyze;

import java.util.*;

import io.github.axion014.forgelang.analyze.word.*;

public class Scope implements Iterable<Word> {
	/**
	 * スコープ内で定義された変数。
	 */
	public SymbolOriginal variables = null;
	private List<Word> body = new LinkedList<>();
	
	public List<Word> getExprs() {
		return body;
	}
	
	public void freeze() {
		body = Collections.unmodifiableList(body);
	}
	
	Symbol findVariable(String name) {
		for (SymbolOriginal v = variables; v != null; v = v.next)
			if (name.equals(v.name) && v.scope == this) return new Symbol(v);
		return null;
	}
	
	boolean isLocalVariable(String name) {
		for (SymbolOriginal v = variables; v != null; v = v.next)
			if (name.equals(v.name) && v.scope == this) return true;
		return false;
	}

	@Override
	public Iterator<Word> iterator() {
		return body.iterator();
	}
}
