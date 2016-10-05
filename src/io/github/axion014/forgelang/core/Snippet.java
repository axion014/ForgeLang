package io.github.axion014.forgelang.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axion014.forgelang.core.exception.MatchingFailedException;
import io.github.axion014.forgelang.core.word.*;
import io.github.axion014.forgelang.tool.throwablefunction.*;

public class Snippet {

	private final static int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999,
			java.lang.Integer.MAX_VALUE};

	// Requires positive x
	public static int stringSize(int x) {
		for (int i = 0;; i++)
			if (x <= sizeTable[i]) return i + 1;
	}

	public static int lengthOf(Word word) {
		if (word instanceof OmniInt || word instanceof OmniStr) {
			return ((Value) word).length;
		} else if (word instanceof BinaryOperator) {
			return ((BinaryOperator) word).type.length;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static Object valueOf(Value value) {
		if (value instanceof OmniInt) {
			return ((OmniInt) value).value;
		} else if (value instanceof OmniStr) {
			return ((OmniStr) value).value;
		} else {
			throw new IllegalArgumentException();
		}
	}

	static void printLengthOfnoln(Word word) {
		System.out.print("Length of ");
		if (word instanceof BinaryOperator) {
			System.out.print(((BinaryOperator) word).type.content);
		} else if (word instanceof Value) {
			System.out.print(valueOf((Value) word));
		}
		System.out.print(": " + lengthOf(word));
	}

	public static void printLengthOf(Word word) {
		printLengthOfnoln(word);
		System.out.println();
	}

	public static void printLengthOfWithLineNumber(Word word, Class<?> thisClass) {
		printLengthOfnoln(word);
		System.out.println("  (" + thisClass.getCanonicalName() + ":" + getLineNumber(thisClass) + ")");
	}

	public static void printWithLineNumber(String message, Class<?> thisClass) {
		System.out.print(message);
		System.out.println("  (" + thisClass.getCanonicalName() + ":" + getLineNumber(thisClass) + ")");
	}

	public static int getLineNumber(Class<?> targetClass) {
		try {
			throw new Exception();
		} catch (Exception ex) {
			StackTraceElement[] elements = ex.getStackTrace();
			for (StackTraceElement element : elements) {
				if (element != null && targetClass.getCanonicalName()
					.equals(element.getClassName())) { return element.getLineNumber(); }
			}
		}
		return -1;
	}

	public static <E extends Throwable> void doIfMatch(Pattern pattern, String dest, ThrowableConsumer<String, E> task)
			throws E {
		Matcher m = pattern.matcher(dest);
		if (m.find()) task.accept(m.group());
	}

	public static <E extends Throwable, R> R doIfMatch(Pattern pattern, String dest,
			ThrowableFunction<String, R, E> task) throws E, MatchingFailedException {
		Matcher m = pattern.matcher(dest);
		if (m.find()) return task.apply(m.group());
		throw new MatchingFailedException();
	}

	public static <E extends Throwable, F extends Throwable> void doIfMatchElse(Pattern pattern, String dest,
			ThrowableConsumer<String, E> thentask, ThrowableRunnable<F> elsetask) throws E, F {
		Matcher m = pattern.matcher(dest);
		if (m.find()) {
			thentask.accept(m.group());
		} else {
			elsetask.run();
		}
	}

	public static <E extends Throwable, R, F extends Throwable> R doIfMatchElse(Pattern pattern, String dest,
			ThrowableFunction<String, R, E> thentask, ThrowableSupplier<R, F> elsetask)
			throws E, F {
		Matcher m = pattern.matcher(dest);
		if (m.find()) return thentask.apply(m.group());
		return elsetask.get();
	}
}
