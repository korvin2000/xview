package net.j7.commons.types;

import net.j7.commons.utils.BitUtils;

public class BitNumberRange {


	public BitNumberRange() {
		// TODO Auto-generated constructor stub
	}


	public static long value(long rawValue, Range range) {

		int from = range.lowerBound();
		int to = range.upperBound();
		long res = 0;
		long start = 1;
		for (int i = from; i < to; i++) {
			if (BitUtils.get(rawValue, i)) res += start;
			start = start << 1;
		}
		return res;
	}


	public static long set(long rawValue, Range range, long value) {

		int from = range.lowerBound();
		int to = range.upperBound();
		int j = 0;
		for (int i = from; i < to; i++, j++) {
			boolean bVal = BitUtils.get(value, j);
			rawValue = BitUtils.set(rawValue, i, bVal);
		}
		return rawValue;
	}

}
