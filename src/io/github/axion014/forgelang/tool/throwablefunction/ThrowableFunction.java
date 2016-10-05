package io.github.axion014.forgelang.tool.throwablefunction;

public interface ThrowableFunction<T, R, E extends Throwable> {
	R apply(T t) throws E;
}