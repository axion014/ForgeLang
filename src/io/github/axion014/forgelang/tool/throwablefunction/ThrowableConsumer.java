package io.github.axion014.forgelang.tool.throwablefunction;

public interface ThrowableConsumer<T, E extends Throwable> {
	void accept(T t) throws E;
}
