package net.j7.commons.ui;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;

public final class StackTrace {

	private static final String COLOR_TAT = "\tat ";



	private static final String RED = "\033[31m";
	 private static final String GREEN = "\033[32m";
	 private static final String CYAN = "\033[36m";
	 private static final String YELLOW = "\033[33m";
	 private static final String WHITE = "\033[37m";
	 private static final String NORMAL = "\033[0m";

	static PrintStream outputStream = System.out;

	private static transient ConcurrentHashMap<String, StackMetadata> map = new ConcurrentHashMap<String, StackMetadata>();

	static int maxLines = 50;

	private final static boolean skip(StackTraceElement elm) {
		String str = elm.getClassName();
		return str.startsWith("java.lang.")||str.startsWith("sun.")||str.startsWith("com.sun.")||str.startsWith("sun.");
	}

	private static final String getAsString(StackTraceElement[] el) {

		StringBuilder sb = new StringBuilder(64 * maxLines);

		for (StackTraceElement stackTraceElement : el)
			sb.append(stackTraceElement.toString());

		return sb.toString();
	}

	public final static synchronized int print() {
		return print0(new Throwable(), 1);
	}

	public final static synchronized int print(Throwable t) {
		return print0(t, 0);
	}

	public final static synchronized int print0(Throwable t, int skip) {

		PrintStream s = StackTrace.outputStream;

        StackTraceElement[] trace = t.getStackTrace();
        if (skip > 0) {
        	int len = max(trace.length-skip,0);
        	StackTraceElement[] newTrace = new StackTraceElement[len];
        	System.arraycopy(trace, skip, newTrace, 0, len);
        	trace = newTrace;
        }

        String key = getAsString(trace);


        StackMetadata data = map.get(key);

        if (data != null) {
        	data.inc();
        	s.println(" Same exception [id="+data.getId()+"] was thrown "+data.getCount()+" time(s)");
        	return data.getId();
        }

        data = new StackMetadata(trace);

        map.put(key, data);

        int lCnt = 1;
		s.println("jLead stack trace [id="+data.getId()+"]");

        for (int i=0; i < trace.length; i++) {
        	if (lCnt>maxLines) return data.getId();
        	if (/*i==0 ||*/ skip(trace[i])) continue ;
            s.println(COLOR_TAT + trace[i]);
            lCnt++;
        }

        final Throwable ourCause = t.getCause();
        if (ourCause != null)
            printStackTraceAsCause(ourCause, s, trace, lCnt);

        return data.getId();
	}

	   private static int max(int i, int j) {
		return i > j ? i : j;
	}

	/**
     * Print our stack trace as a cause for the specified stack trace.
     */
    private static void printStackTraceAsCause(Throwable ourC, PrintStream s, StackTraceElement[] causedTrace, int lCnt)
    {

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = ourC.getStackTrace();
        int m = trace.length-1, n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        s.println("Caused by: " + ourC);
        lCnt++;

        for (int i=0; i <= m; i++) {
        	if (lCnt>maxLines) return ;
        	if (skip(trace[i])) continue ;
        	s.println(COLOR_TAT + trace[i]);
        	 lCnt++;
        }

        if (framesInCommon != 0) {
            s.println("\t... " + framesInCommon + " more");
            lCnt++;
        }

        // Recurse if we have a cause
        Throwable ourCause = ourC.getCause();
        if (ourCause != null)
        	printStackTraceAsCause(ourCause, s, trace, lCnt);
    }

    public static class StackMetadata {

    	StackTraceElement[] stackTrace;
    	int count;
    	int id;
    	private static transient volatile int idGen;
		/**
		 * @param stackTrace
		 * @param count
		 */
		public StackMetadata(StackTraceElement[] stackTrace) {
			this.stackTrace = stackTrace;
			idGen++;
			this.id = idGen;
			this.count = 1;
		}

		public void inc() {
			count++;
		}

		public StackTraceElement[] getStackTrace() {
			return stackTrace;
		}

		public int getCount() {
			return count;
		}

		public int getId() {
			return id;
		}

    }

	public static void setOutputStream(PrintStream outputStream) {
		StackTrace.outputStream = outputStream;
	}

}
