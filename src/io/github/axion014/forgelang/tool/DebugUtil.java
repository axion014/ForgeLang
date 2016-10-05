package io.github.axion014.forgelang.tool;

import io.github.axion014.forgelang.tool.throwablefunction.ThrowableRunnable;
import io.github.axion014.forgelang.tool.throwablefunction.ThrowableSupplier;

public class DebugUtil {

	private DebugUtil() {}
	
	public static <T extends Throwable> void benchMark(ThrowableRunnable<T> target) throws T {
		benchMark(target, "Execution time");
	}
	
	public static <T extends Throwable> void benchMark(ThrowableRunnable<T> target, String comment) throws T {
		long start = System.nanoTime();
		target.run();
		long end = System.nanoTime() - start;
		System.out.println(comment + ": " + DebugUtil.autoformat(end / 10000000000.0, "s"));
	}
	
	public static <T, E extends Throwable> T benchMark(ThrowableSupplier<T, E> target) throws E {
		return benchMark(target, "Execution time");
	}
	
	public static <T, E extends Throwable> T benchMark(ThrowableSupplier<T, E> target, String comment) throws E {
		long start = System.nanoTime();
		T result = target.get();
		long end = System.nanoTime() - start;
		System.out.println(comment + ": " + DebugUtil.autoformat(end / 10000000000.0, "s"));
		return result;
	}

	public static String autoformat(final double number, final String unit) {
		return autoformat(number, unit, 2);
	}

	public static String autoformat(final double number, String unit, final int scale) {
		if (number >= 10e24) unit = String.format("%." + Integer.toString(scale) + "f", number / 10e24) + "Y" + unit;
		else if (number >= 10e21)
			unit = String.format("%." + Integer.toString(scale) + "f", number / 10e21) + "Z" + unit;
		else if (number >= 10e18)
			unit = String.format("%." + Integer.toString(scale) + "f", number / 10e18) + "E" + unit;
		else if (number >= 10e15)
			unit = String.format("%." + Integer.toString(scale) + "f", number / 10e15) + "P" + unit;
		else if (number >= 10e12)
			unit = String.format("%." + Integer.toString(scale) + "f", number / 10e12) + "T" + unit;
		else if (number >= 10e9) unit = String.format("%." + Integer.toString(scale) + "f", number / 10e9) + "G" + unit;
		else if (number >= 10e6) unit = String.format("%." + Integer.toString(scale) + "f", number / 10e6) + "M" + unit;
		else if (number >= 10e3) unit = String.format("%." + Integer.toString(scale) + "f", number / 10e3) + "k" + unit;
		else if (number >= 10e0) unit = String.format("%." + Integer.toString(scale) + "f", number) + unit;
		else if (number >= 10e-2 && unit == "m")
			unit = String.format("%." + Integer.toString(scale) + "f", number * 10e2) + "c" + unit;
		else if (number >= 10e-3)
			unit = String.format("%." + Integer.toString(scale) + "f", number * 10e3) + "m" + unit;
		else if (number >= 10e-6)
			unit = String.format("%." + Integer.toString(scale) + "f", number * 10e6) + "μ" + unit;
		else if (number >= 10e-9)
			unit = String.format("%." + Integer.toString(scale) + "f", number * 10e9) + "n" + unit;
		else unit = String.format("%." + Integer.toString(scale) + "f", number * 10e12) + "p" + unit;
		return unit;
	}
}
