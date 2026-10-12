package net.j7.commons.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public final class Metrics {

	private static final String TIME = "Time: ";

	public long start;
	int counter;


	private static transient boolean disabled = false;
	private static transient PrintStream  stream = System.out;
	private static transient HashMap<Object, Long> times = new HashMap<Object, Long>();
	private Metrics() {
		restart();
	}

	public final static Metrics start() {
		return new Metrics();
	}


	public final void measure() {
		this.measure(TIME);
	}

	public final void restart() {
		this.start = System.currentTimeMillis();
		counter = 0;
	}

	public final void measure(String text) {
		if (disabled) return ;
		long startPrev = this.start;
		this.start = System.currentTimeMillis();
		counter++;

		StringBuilder sb = new StringBuilder();
		sb.append(counter).append(") ");
		sb.append(text).append(": ").append((start - startPrev)).append(" ms");
		stream.println(sb);

	}

	public final static void enable(boolean state) {
		Metrics.disabled = !state;
	}

	public final static void usePrintStream(PrintStream  prnStream) {
		Metrics.stream = prnStream;
	}



	public static void measureStart(Object id) {
		times.put(id, System.currentTimeMillis());
	}

	public static void measureStop(Object id) {
		measureStop(id, null);
	}

	public static void measureContinue(Object id) {
		measureContinue(id, null);
	}

	public static void measureContinue(Object id, String label) {
		Long endTime = System.currentTimeMillis();
		Long startTime = times.put(id, endTime);
		measureInt(id, label, startTime, endTime);
	}

	public static void measureStop(Object id, String label) {
		Long startTime = times.remove(id);
		Long endTime = System.currentTimeMillis();
		measureInt(id, label, startTime, endTime);
	}

	private static void measureInt(Object id, String label, Long startTime, Long endTime) {
		if (null != startTime) {
			StringBuilder sb = new StringBuilder();
			if (label != null)
				sb.append(label);
			else {
				sb.append("Measured [").append(id.toString()).append("] = ");
			}
			sb.append((endTime - startTime)).append(" ms");
			stream.println(sb);
		}
	}

}
