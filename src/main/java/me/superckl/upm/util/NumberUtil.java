package me.superckl.upm.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NumberUtil {

	private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
	static {
		NumberUtil.suffixes.put(1_000L, "k");
		NumberUtil.suffixes.put(1_000_000L, "M");
		NumberUtil.suffixes.put(1_000_000_000L, "G");
		NumberUtil.suffixes.put(1_000_000_000_000L, "T");
		NumberUtil.suffixes.put(1_000_000_000_000_000L, "P");
		NumberUtil.suffixes.put(1_000_000_000_000_000_000L, "E");
	}

	public static String format(final long value) {
		//Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
		if (value == Long.MIN_VALUE) return NumberUtil.format(Long.MIN_VALUE + 1);
		if (value < 0) return "-" + NumberUtil.format(-value);
		if (value < 1000) return Long.toString(value); //deal with easy case

		final Entry<Long, String> e = NumberUtil.suffixes.floorEntry(value);
		final Long divideBy = e.getKey();
		final String suffix = e.getValue();

		final long truncated = value / (divideBy / 10); //the number part of the output times 10
		final boolean hasDecimal = truncated < 100 && truncated / 10d != truncated / 10;
		return hasDecimal ? truncated / 10d + suffix : truncated / 10 + suffix;
	}

	public static double bigDecimalDivide(final long value, final long divisor) {
		return new BigDecimal(value).divide(new BigDecimal(divisor), RoundingMode.DOWN).doubleValue();
	}

}
