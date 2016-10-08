package io.github.axion014.forgelang.writer;

import java.util.List;

import io.github.axion014.forgelang.analyze.word.Word;

public interface DestinationWriter {
	void writeCode(List<Word> exprs, StringBuilder assembly);
}
