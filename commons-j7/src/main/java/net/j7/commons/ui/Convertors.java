package net.j7.commons.ui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Convertors {

	private static final long POWER = 10l;

	public static final long SIZE_BYTE = 1l;
	public static final long SIZE_KILOBYTE = SIZE_BYTE << POWER;
	public static final long SIZE_MEGABYTE = SIZE_KILOBYTE << POWER;
	public static final long SIZE_GIGABYTE = SIZE_MEGABYTE << POWER;
	public static final long SIZE_TERABYTE = SIZE_GIGABYTE << POWER;
	public static final long SIZE_PETABYTE = SIZE_TERABYTE << POWER;
	public static final long SIZE_EXABYTE = SIZE_PETABYTE << POWER;

	private static final transient DecimalFormat floatFormat		= new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.US));

	public static void main(String[] args) {
		System.out.println(floatFormat.format(1d));
		System.out.println(floatFormat.format(10000d));
		System.out.println(floatFormat.format(10000.35d));
		System.out.println(floatFormat.format(0.3d));
		System.out.println(floatFormat.format(100.01d));
		System.out.println(floatFormat.format(100.5d));
		System.out.println(floatFormat.format(100.555d));
		System.out.println(floatFormat.format(0.01d));
	}
	
   public enum TimeUnit {
	      Millisecond(1l),
	      Second(Millisecond.ms() * 1000l),
	      Minute(Second.ms() * 60l),
	      Hour(Minute.ms() * 60l),
	      Day(Hour.ms() * 24l),
	      ;

	      long ms; TimeUnit(long msArg) { ms = msArg; }
	      public long ms() { return ms; }
	   }

	private final static TimeUnit[] STU = new TimeUnit[] {TimeUnit.Day, TimeUnit.Hour, TimeUnit.Minute, TimeUnit.Second};

	public static enum ByteUnit{Byte(0), KB(1), MB(2), GB(3), TB(4), PB(5), EB(6);

	long size, multiply;
	private ByteUnit(long arg){
		multiply = arg;
		size = SIZE_BYTE << (POWER * multiply);
	}
	long getBytes() { return size; }
	long getMultiply() { return multiply; }
	String getUnit() { return toString(); }

	};


	public static float convert(float value, ByteUnit from, ByteUnit to) {
		if (from.equals(to)) return value;
		long dif = from.getMultiply() - to.getMultiply();
		return dif > 0 ? value * (float)  (SIZE_BYTE << (POWER * dif)) : value / (float)  (SIZE_BYTE << (- POWER * dif));

	}

	public static String autoSize(long size) {
		StringBuilder sb = new StringBuilder();
		ByteUnit unit = null;
		if (size < SIZE_MEGABYTE) {
			if (size < SIZE_KILOBYTE) {
				unit = ByteUnit.Byte;
			} else {
				unit = ByteUnit.KB;
			}
		} else {
			if (size < SIZE_GIGABYTE) {
				unit = ByteUnit.MB;
			} else
			if (size < SIZE_TERABYTE)
			{
				unit = ByteUnit.GB;
			} else
			if (size < SIZE_PETABYTE)
			{
				unit = ByteUnit.TB;
			} else
			if (size < SIZE_EXABYTE)
			{
				unit = ByteUnit.PB;
			} else
				unit = ByteUnit.EB;
		}

		sb.append(unit == ByteUnit.Byte ? size : floatFormat.format(convert(size, ByteUnit.Byte, unit)));
		sb.append(' ');
		sb.append(unit.getUnit());

		return sb.toString();
	}

	public static String formatValue(double val) {
		return floatFormat.format(val);
	}


	   public static String getIntervalAsText(long ms) {

		StringBuilder sb = new StringBuilder();
		for (TimeUnit unit : STU) {
			if (ms > unit.ms()) {
				long clv = ms / unit.ms();
				if (sb.length()>0) sb.append(' ');
				sb.append(clv).append(' ').append(unit.name());
				if (clv>1) sb.append('s');

				ms = ms - (clv * unit.ms());
			}
		}
		return sb.toString();
	   }
}
