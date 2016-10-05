package io.github.axion014.forgelang.core;

import java.util.List;

import io.github.axion014.forgelang.core.word.OmniStr;
import io.github.axion014.forgelang.core.word.Word;

public interface DestinationWriter {
	void writeCode(List<Word> exprs, OmniStr strings, StringBuilder assembly);
}
