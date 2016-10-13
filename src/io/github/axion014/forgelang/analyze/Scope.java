package io.github.axion014.forgelang.analyze;

import java.util.Iterator;
import java.util.List;

import io.github.axion014.forgelang.analyze.word.SymbolOriginal;
import io.github.axion014.forgelang.analyze.word.Word;
import io.github.axion014.forgelang.tool.throwablefunction.ThrowableSupplier;

public class Scope implements Iterable<Word> {
	/**
	 * スコープ内で定義された変数。
	 */
	public SymbolOriginal variables = null;
	public final Word[] body;

	public <E extends Throwable> Scope(ThrowableSupplier<List<Word>, E> bodyfunc, Compiler env) throws E {
		env.scope = this;
		body = bodyfunc.get().toArray(new Word[0]);
	}
	
	public Scope() {body = new Word[0];}

	@Override
	public Iterator<Word> iterator() {
		return new Iterator<Word>() {
			private int pos = 0;

			@Override
			public boolean hasNext() {
				return body.length > pos;
			}

			@Override
			public Word next() {
				Word ret = body[pos];
				pos++;
				return ret;
			}
		};
	}
}
