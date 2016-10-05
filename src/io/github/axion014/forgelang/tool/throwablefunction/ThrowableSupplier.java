package io.github.axion014.forgelang.tool.throwablefunction;

public interface ThrowableSupplier<T, E extends Throwable> {
	T get() throws E;
}
