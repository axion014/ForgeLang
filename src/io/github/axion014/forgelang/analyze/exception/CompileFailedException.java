package io.github.axion014.forgelang.analyze.exception;

public class CompileFailedException extends Exception {
	public int line;

	public CompileFailedException(Throwable cause) {
		super(cause);
	}

	public CompileFailedException(String message) {
		super(message);
	}

	public CompileFailedException(String message, int line) {
		this(message);
		this.line = line;
	}
}
