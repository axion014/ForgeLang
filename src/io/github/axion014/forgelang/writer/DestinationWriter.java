package io.github.axion014.forgelang.writer;

import io.github.axion014.forgelang.analyze.Scope;

public interface DestinationWriter {
	void writeCode(Scope global, StringBuilder assembly);
}
