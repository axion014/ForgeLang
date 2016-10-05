package io.github.axion014.forgelang.tool.throwablefunction;

@FunctionalInterface
public interface ThrowableRunnable<T extends Throwable> {
	public abstract void run() throws T;
}
