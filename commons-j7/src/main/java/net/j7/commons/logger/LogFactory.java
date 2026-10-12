/*
 * @File: LogFactory.java
 *
 *
 * @Author: Alexander Tawrowski
 *
 * @Version $Revision: #1 $Date: $
 *
 */

package net.j7.commons.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * <b>A factory for creating wrapped logger instances. (reflection-based)</b><br><br>
 *
 * LogFactory purpose to find the most suitable Logger available in the classpath.<br>
 * <b>Advantages:</b> It's not necessary to explicitly add logger dependencies in every module/project.<br>
 * This logger wrapper hasn't any direct dependencies to concrete logger implementations.<br> It's uses reflection mechanism to work with a concrete logger implementation.<br>
 * <br>To use you should add this lightweight single-class logger wrapper to your project.<br>
 *
 * <br><u>Logger priorities:</u><br>
 * <b>CrefoLogger > Log4j v.2 > SL4J > Log4j > Java std. Logger</b>
 * <br>CrefoLogger - has highest prio and Java Logger lower prio respectively.
 * <br><br>
 * Is reflection slower as direct call ? Yes. Direct method call is about 2 times faster.<br>
 * Does reflection impact logger performance? Not really. Impact is negligible.<br>
 * <br><u>Typical example:</u><br>To generate 100 Mb log it will cause additional 3 ms delay (i7 3.2 Ghz CPU).
 * <br>In other words, to cause 1 second delay about 31 Gb log data should be written.<br>
 * <br>
 * Crefosystem writes up to (maximal) 300-350 Mb data to product log / pro day<br>
 * To write continuously, sequentially 31 Gb of data to typical HDD -> OS needs ca. 9 minutes. <br>
 * If you will write the same amount of data using logger wrapper instead of calling logger direct <br>-> whole 31 Gb of data will be written only 1 second later in comparison to normal logger.
 * <br>
 * <br><u>Performance considerations (JVM 7):</u><br><br>
 * Direct method call : 100%<br>
 * Object Proxy  call : 165% (65 % slower)<br>
 * Via Reflection call: 194% (94 % slower)<br>
 *
 * @author  <a href="A.Tavrovschi@verband.creditreform.de">Alexander Tawrowski</a>
 *
 * @see <a href="http://blog.takipi.com/779236-java-logging-statements-1313-github-repositories-error-warn-or-fatal/">The Average Java Log Level Distribution</a>
 * @see <a href="http://blog.takipi.com/is-standard-java-logging-dead-log4j-vs-log4j2-vs-logback-vs-java-util-logging/">Loggers comparision and statistics</a>
 * @see <a href="http://www.cowtowncoder.com/blog/archives/2007/02/entry_32.html">Reflection considered slow -- but how slow is slow?</a>
 */
public class LogFactory {

	private static final java.util.logging.Logger intLogger = java.util.logging.Logger.getLogger(LogFactory.class.getName());

	static boolean DOUBLE_LOGGING = true;

	final static LogFinder[] finders = new LogFinder[] { CrefoLogFinder.instance, Log4j2Finder.instance, Slf4jFinder.instance, Log4jFinder.instance, JavaLogFinder.instance };

	private static LogFinder findLogger() {
		for (LogFinder next : finders) {
			if (next.present()) return next;
		}
   	    return null;
	}

	private static LogFinder logger;

	static {
		logger = findLogger();
		intLogger.info(String.format("Used logger: >>> %s <<<", logger.getClass().getSimpleName()));
	}

	public static Logger getLogger() {
		return getLogger(((new Throwable()).getStackTrace()[1]).getClass()) ;
	}

	public final static Logger getLogger(Class<?> clz) {
		return getLogger(clz, null);
	};

	public final static Logger getLogger(Class<?> clz, String revision) {
		return logger.getInstance(clz, revision);
	};

	/**
	 * Checks if is double logging. If enabled - all outputs to non standard logger are extra logged to java logger (console)<br>
	 * default: <b>enabled</b>
	 * @return true, if double logging enabled.
	 */
	public static boolean isDoubleLogging() {
		return DOUBLE_LOGGING;
	}

	/**
	 * Sets the double logging.<br> If enabled - outputs to non standard logger are extra logged to java logger (console)
	 *
	 * @param enabled the new double logging
	 */
	public static void setDoubleLogging(boolean enabled) {
		DOUBLE_LOGGING = enabled;
	}


	static abstract class LogFinder {
		Class<?> logFactoyCls;
		Method newLoggerMethod;

		boolean baseClassPresent(String clsName, String method, Class<?>[] params) {
			try {
				logFactoyCls = Class.forName(clsName);
				newLoggerMethod = logFactoyCls != null ? logFactoyCls.getMethod(method, params) : null;
				if (null != newLoggerMethod) {
					newLoggerMethod.setAccessible(true);
					return true;
				}
			} catch (ReflectiveOperationException | SecurityException | LinkageError e) {
				intLogger.log(Level.FINE, format("Error instantiating logger: {}", clsName), e);
			}
			intLogger.info(format("Can't find logger: {0}",clsName));
			return false;
		}

		abstract boolean checkMethods() throws ReflectiveOperationException, SecurityException;

		abstract boolean present();

		boolean checkPresent(String clsName, String method, Class<?>[] params) {
			if (baseClassPresent(clsName, method, params)) {
				try {
					return checkMethods();
				} catch (ReflectiveOperationException | SecurityException e) {
					intLogger.log(Level.WARNING, format("Error initializing: {}",logFactoyCls), e);
				}
			};
			return false;
		}

		Object createLogger(Object... args) {
			try {
				return newLoggerMethod.invoke(null, args);
			} catch (ReflectiveOperationException | IllegalArgumentException | LinkageError  e) {
				if (intLogger.isLoggable(Level.INFO)) {
					intLogger.log(Level.INFO, "Can't instantiate object instance: {0}.{1}", new Object[] {logFactoyCls, newLoggerMethod});
					intLogger.log(Level.FINE, "Exception: ", e);
				}
				return null;
			}
		}

		abstract Logger instance(Class<?> clz, String revision);

		Logger getInstance(Class<?> clz, String revision) {
			Logger primary = instance(clz, revision);
			return DOUBLE_LOGGING ? new DoubleLogger(primary, JavaLogFinder.instance.instance(clz, revision)) : primary;
		}
	}

	static class JavaLogFinder extends LogFinder {

		static JavaLogFinder instance = new JavaLogFinder();

		boolean checkMethods() throws ReflectiveOperationException { return true; };

		boolean present() { return true; }

		Logger instance(Class<?> clz, String revision) {
			return new JavaLogger(java.util.logging.Logger.getLogger(clz.getName()));
		}

		@Override
		Logger getInstance(Class<?> clz, String revision) {
			return instance(clz, revision);
		}
	}

	static class CrefoLogFinder extends LogFinder {

		static CrefoLogFinder instance = new CrefoLogFinder();

		private final static Class<?>[] params = new Class[] {Class.class, String.class};

		boolean checkMethods() throws ReflectiveOperationException, SecurityException {
			Object inst = createLogger(getClass(), "");
			if (null != inst) CrefoLogger.init(inst.getClass());
			return inst != null;
		}

		boolean present() {
			return checkPresent("de.creditreform.crefosystem.logging.LoggerFactory", "getLogger", params);
		}

		Logger instance(Class<?> clz, String revision)  { return new CrefoLogger(createLogger(clz, revision)); }

	}

	static class Slf4jFinder extends LogFinder {

		static Slf4jFinder instance = new Slf4jFinder();

		private final static Class<?>[] params = new Class[] {Class.class};

		boolean checkMethods() throws ReflectiveOperationException {
			Object logger = createLogger(getClass());
			if (null != logger) Slf4Logger.init(logger.getClass());
			return logger != null;
		}

		boolean present() {
			return checkPresent("org.slf4j.LoggerFactory", "getLogger", params);
		}

		Logger instance(Class<?> clz, String revision) { return new Slf4Logger(createLogger(clz)); }
	}

	static class Log4jFinder extends LogFinder {

		static Log4jFinder instance = new Log4jFinder();

		private final static Class<?>[] params = new Class[] {Class.class};

		boolean checkMethods() throws ReflectiveOperationException {
			Object logger = createLogger(getClass());
			if (null != logger) Log4jLogger.init(logger.getClass());
			return logger != null;
		}

		boolean present() {
			return checkPresent("org.apache.log4j.Logger", "getLogger", params);
		}

		Logger instance(Class<?> clz, String revision) { return new Log4jLogger(createLogger(clz)); }
	}

	static class Log4j2Finder extends LogFinder {

		static Log4j2Finder instance = new Log4j2Finder();

		private final static Class<?>[] params = new Class[] {Class.class};

		boolean checkMethods() throws ReflectiveOperationException {
			Object logger = createLogger(getClass());
			if (null != logger) Log4j2Logger.init(logger.getClass());
			return logger != null;
		}

		boolean present() {
			return checkPresent("org.apache.logging.log4j.LogManager", "getLogger", params);
		}

		Logger instance(Class<?> clz, String revision) { return new Log4j2Logger(createLogger(clz)); }
	}

	public interface Logger {

		void info(Object msg);
		void info(Object msg, Throwable t);
		void info(Object msg, Throwable t, Object... args);
		void info(Object msg, Object... args);

		void warn(Object msg);
		void warn(Object msg, Throwable t);
		void warn(Object msg, Throwable t, Object... args);
		void warn(Object msg, Object... args);


		void error(Object msg);
		void error(Object msg, Throwable t);
		void error(Object msg, Throwable t, Object... args);
		void error(Object msg, Object... args);

		void debug(Object msg);
		void debug(Object msg, Throwable t);
		void debug(Object msg, Throwable t, Object... args);
		void debug(Object msg, Object... args);

		void trace(Object msg);
		void trace(Object msg, Throwable t);
		void trace(Object msg, Throwable t, Object... args);
		void trace(Object msg, Object... args);

		boolean isDebugEnabled();
		boolean isWarnEnabled();
		boolean isErrorEnabled();
		boolean isInfoEnabled();
		boolean isTraceEnabled();

	}


	static abstract class BaseLogger implements Logger {

		static final Class<?>[] PT_OBJ_THR = new Class[] {Object.class, Throwable.class };
		static final Class<?>[] PT_STR_THR = new Class[] {String.class, Throwable.class };

		Object logger;

		static Method getMethod(Class<?> clz, String name, Class<?>[] parameterTypes) throws ReflectiveOperationException, SecurityException  {
			Method m = null;
				m = parameterTypes != null ? clz.getMethod(name, parameterTypes) : clz.getMethod(name);
				m.setAccessible(true);
			return m;
		}

		boolean getBool(Method m) {
			Boolean b = null;
			try {
				b = (Boolean)m.invoke(logger);
			} catch (ReflectiveOperationException e) {
				intLogger.log(Level.SEVERE, "Unsupported or unknown logger implementation", e);
			}
			return Boolean.TRUE.equals(b);
		}

		final void invoke(final Method method,final  Object... args) {
			try {
				method.invoke(logger, args);
			} catch (ReflectiveOperationException | IllegalArgumentException e) {
				intLogger.log(Level.SEVERE, "", e);
			}
		}

		static String str(Throwable t) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			return sw.toString();
		}

		static String str(Object o) {return o != null ? o instanceof Throwable ? str((Throwable)o) : o.toString() : null; }
		static String str(Object... o) {
			if (o==null || o.length==0) return "";
			StringBuilder r = new StringBuilder();
			for (Object next : o) {
				if (r.length()>0) r.append(' ');
				r.append(str(next));
			}
			return r.toString();
		}

	};


	static class JavaLogger extends BaseLogger {

//		FINEST  -> TRACE
//		 FINER   -> DEBUG / **TRACE**
//		 FINE    -> DEBUG
//		 INFO    -> INFO
//		 WARNING -> WARN
//		 SEVERE  -> ERROR

		private java.util.logging.Logger log;

		private JavaLogger (Object logger) {
			this.log = (java.util.logging.Logger )logger;
		}

		public void info(Object msg) {
			if (isInfoEnabled()) log.info(str(msg));
		}

		public void info(Object msg, Throwable t) {
			if (isInfoEnabled()) log.log(Level.INFO, str(msg), t);
		}

		public void info(Object msg, Throwable t, Object... args) {
			if (isInfoEnabled()) log.log(Level.INFO, format(str(msg), args), t);
		}

		public void info(Object msg, Object... args) {
			if (isInfoEnabled()) log.info(format(str(msg), args));
		}

		public void warn(Object msg) {
			if (isWarnEnabled()) log.warning(str(msg));
		}

		public void warn(Object msg, Throwable t) {
			if (isWarnEnabled()) log.log(Level.WARNING, str(msg), t);
		}

		public void warn(Object msg, Throwable t, Object... args) {
			if (isWarnEnabled()) log.log(Level.WARNING, format(str(msg), args), t);
		}

		public void warn(Object msg, Object... args) {
			if (isWarnEnabled()) log.warning(format(str(msg), args));
		}

		public void error(Object msg) {
			if (isErrorEnabled()) log.severe(str(msg));
		}

		public void error(Object msg, Throwable t) {
			if (isErrorEnabled()) log.log(Level.SEVERE, str(msg), t);
		}

		public void error(Object msg, Throwable t, Object... args) {
			if (isErrorEnabled()) log.log(Level.SEVERE, format(str(msg), args), t);
		}

		public void error(Object msg, Object... args) {
			if (isErrorEnabled()) log.severe(format(str(msg), args));
		}

		public void debug(Object msg) {
			if (isDebugEnabled()) log.fine(str(msg));
		}

		public void debug(Object msg, Throwable t) {
			if (isDebugEnabled()) log.log(Level.FINE, str(msg), t);
		}

		public void debug(Object msg, Throwable t, Object... args) {
			if (isDebugEnabled()) log.log(Level.FINE, format(str(msg), args), t);
		}

		public void debug(Object msg, Object... args) {
			if (isDebugEnabled()) log.fine(format(str(msg), args));
		}

		public void trace(Object msg) {
			if (isTraceEnabled()) log.finest(str(msg));
		}

		public void trace(Object msg, Throwable t) {
			if (isTraceEnabled()) log.log(Level.FINEST, str(msg), t);
		}

		public void trace(Object msg, Throwable t, Object... args) {
			if (isTraceEnabled()) log.log(Level.FINEST, format(str(msg), args), t);
		}

		public void trace(Object msg, Object... args) {
			if (isTraceEnabled()) log.finest(format(str(msg), args));
		}

		public boolean isDebugEnabled() {
			return  log.isLoggable(Level.FINE);
		}

		public boolean isWarnEnabled() {
			return log.isLoggable(Level.WARNING);
		}

		public boolean isErrorEnabled() {
			return log.isLoggable(Level.SEVERE);
		}

		public boolean isInfoEnabled() {
			return log.isLoggable(Level.INFO);
		}

		public boolean isTraceEnabled() {
			return log.isLoggable(Level.FINEST);
		}

	}

	 static class AbstractReflectionLogger extends BaseLogger {

		Method infoMethod, warnMethod, errMethod, debugMethod, traceMethod;
		boolean isDebug, isWarn, isError, isInfo, isTrace;

		public void info(Object msg) {
			invoke(infoMethod, msg, null);
		}

		public void info(Object msg, Throwable t) {
			invoke(infoMethod, msg, t);
		}

		public void info(Object msg, Throwable t, Object... args) {
			invoke(infoMethod, format(str(msg), args), t);
		}

		public void info(Object msg, Object... args) {
			invoke(infoMethod, format(str(msg), args), null);
		}

		public void warn(Object msg) {
			invoke(warnMethod, msg, null);
		}

		public void warn(Object msg, Throwable t) {
			invoke(warnMethod, msg, t);
		}

		public void warn(Object msg, Throwable t, Object... args) {
			invoke(warnMethod, format(str(msg), args), t);
		}

		public void warn(Object msg, Object... args) {
			invoke(warnMethod, format(str(msg), args), null);
		}

		public void error(Object msg) {
			invoke(errMethod, msg, null);
		}

		public void error(Object msg, Throwable t) {
			invoke(errMethod, msg, t);
		}

		public void error(Object msg, Throwable t, Object... args) {
			invoke(errMethod, format(str(msg), args), t);
		}

		public void error(Object msg, Object... args) {
			invoke(errMethod, format(str(msg), args), null);
		}

		public void debug(Object msg) {
			invoke(debugMethod, msg, null);
		}

		public void debug(Object msg, Throwable t) {
			invoke(debugMethod, msg, t);
		}

		public void debug(Object msg, Throwable t, Object... args) {
			invoke(debugMethod, format(str(msg), args), t);
		}

		public void debug(Object msg, Object... args) {
			invoke(debugMethod, format(str(msg), args), null);
		}

		public void trace(Object msg) {
			invoke(traceMethod, msg, null);
		}

		public void trace(Object msg, Throwable t) {
			invoke(traceMethod, msg, t);
		}

		public void trace(Object msg, Throwable t, Object... args) {
			invoke(traceMethod, format(str(msg), args), t);
		}

		public void trace(Object msg, Object... args) {
			invoke(traceMethod, format(str(msg), args), null);
		}

		public boolean isDebugEnabled() {return isDebug;}

		public boolean isWarnEnabled() {return isWarn;}

		public boolean isErrorEnabled() {return isError;}

		public boolean isInfoEnabled() {return isInfo;}

		public boolean isTraceEnabled() {return isTrace;}

	}

	static class CrefoLogger extends AbstractReflectionLogger {

		static Method mtdInfo, mtdWarn, mtdErr, mtdDebug, mtdTrace;
		static Method debug, info, trace;

		private CrefoLogger(Object logObj) {
			logger = logObj;
			infoMethod = mtdInfo;
			warnMethod = mtdWarn;
			errMethod = mtdErr;
			debugMethod = mtdDebug;
			traceMethod = mtdTrace;

			isTrace = getBool(trace);
			isDebug = getBool(debug);
			isInfo = getBool(info);
			isWarn = true;
			isError = true;
		}

		static void init(Class<?> clz) throws ReflectiveOperationException, SecurityException{
			mtdInfo = getMethod(clz, "info", PT_OBJ_THR);
			mtdWarn = getMethod(clz, "warn", PT_OBJ_THR);
			mtdErr = getMethod(clz, "error", PT_OBJ_THR);
			mtdDebug = getMethod(clz, "debug", PT_OBJ_THR);
			mtdTrace = getMethod(clz, "trace", PT_OBJ_THR);

			trace = getMethod(clz, "isTraceEnabled", null);
			debug = getMethod(clz, "isDebugEnabled", null);
			info = getMethod(clz, "isInfoEnabled", null);
		}

	}

	static class Slf4Logger extends AbstractReflectionLogger {

		static Method mtdInfo, mtdWarn, mtdErr, mtdDebug, mtdTrace;
		static Method debug, info, warn, error, trace;

		private Slf4Logger(Object logObj) {
			logger = logObj;
			infoMethod = mtdInfo;
			warnMethod = mtdWarn;
			errMethod = mtdErr;
			debugMethod = mtdDebug;
			traceMethod = mtdTrace;

			isTrace = getBool(trace);
			isDebug = getBool(debug);
			isInfo = getBool(info);
			isWarn = getBool(warn);
			isError = getBool(error);
		}

		public static void init(Class<?> clz)  throws ReflectiveOperationException, SecurityException {
			mtdInfo = getMethod(clz, "info", PT_STR_THR);
			mtdWarn = getMethod(clz, "warn", PT_STR_THR);
			mtdErr = getMethod(clz, "error", PT_STR_THR);
			mtdDebug = getMethod(clz, "debug", PT_STR_THR);
			mtdTrace = getMethod(clz, "trace", PT_STR_THR);

			trace = getMethod(clz, "isTraceEnabled", null);
			debug = getMethod(clz, "isDebugEnabled", null);
			info = getMethod(clz, "isInfoEnabled", null);
			warn = getMethod(clz, "isWarnEnabled", null);
			error = getMethod(clz, "isErrorEnabled", null);
		}

		public void info(Object msg) {
			invoke(infoMethod, str(msg), null);
		}

		public void info(Object msg, Throwable t) {
			invoke(infoMethod, str(msg), t);
		}

		public void warn(Object msg) {
			invoke(warnMethod, str(msg), null);
		}

		public void warn(Object msg, Throwable t) {
			invoke(warnMethod, str(msg), t);
		}

		public void error(Object msg) {
			invoke(errMethod, str(msg), null);
		}

		public void error(Object msg, Throwable t) {
			invoke(errMethod, str(msg), t);
		}

		public void debug(Object msg) {
			invoke(debugMethod, str(msg), null);
		}

		public void debug(Object msg, Throwable t) {
			invoke(debugMethod, str(msg), t);
		}

		public void trace(Object msg) {
			invoke(traceMethod, str(msg), null);
		}

		public void trace(Object msg, Throwable t) {
			invoke(traceMethod, str(msg), t);
		}

	}

	static class Log4jLogger extends AbstractReflectionLogger {

		static Method mtdInfo, mtdWarn, mtdErr, mtdDebug, mtdTrace;
		static Method debug, info, trace;

		private Log4jLogger(Object logObj) {
			logger = logObj;
			infoMethod = mtdInfo;
			warnMethod = mtdWarn;
			errMethod = mtdErr;
			debugMethod = mtdDebug;
			traceMethod = mtdTrace;

			isTrace = getBool(trace);
			isDebug = getBool(debug);
			isInfo = getBool(info);
			isWarn = true;
			isError = true;
		}

		static void init(Class<?> clz) throws ReflectiveOperationException, SecurityException{
			mtdInfo = getMethod(clz, "info", PT_OBJ_THR);
			mtdWarn = getMethod(clz, "warn", PT_OBJ_THR);
			mtdErr = getMethod(clz, "error", PT_OBJ_THR);
			mtdDebug = getMethod(clz, "debug", PT_OBJ_THR);
			mtdTrace = getMethod(clz, "trace", PT_OBJ_THR);

			trace = getMethod(clz, "isTraceEnabled", null);
			debug = getMethod(clz, "isDebugEnabled", null);
			info = getMethod(clz, "isInfoEnabled", null);
		}

	}

	static class Log4j2Logger extends AbstractReflectionLogger {

		static Method mtdInfo, mtdWarn, mtdErr, mtdDebug, mtdTrace;
		static Method debug, info, warn, error, trace;

		private Log4j2Logger(Object logObj) {
			logger = logObj;
			infoMethod = mtdInfo;
			warnMethod = mtdWarn;
			errMethod = mtdErr;
			debugMethod = mtdDebug;
			traceMethod = mtdTrace;

			isTrace = getBool(trace);
			isDebug = getBool(debug);
			isInfo = getBool(info);
			isWarn = getBool(warn);
			isError = getBool(error);
		}

		static void init(Class<?> clz) throws ReflectiveOperationException, SecurityException{
			mtdInfo = getMethod(clz, "info", PT_OBJ_THR);
			mtdWarn = getMethod(clz, "warn", PT_OBJ_THR);
			mtdErr = getMethod(clz, "error", PT_OBJ_THR);
			mtdDebug = getMethod(clz, "debug", PT_OBJ_THR);
			mtdTrace = getMethod(clz, "trace", PT_OBJ_THR);

			trace = getMethod(clz, "isTraceEnabled", null);
			debug = getMethod(clz, "isDebugEnabled", null);
			info = getMethod(clz, "isInfoEnabled", null);
			warn = getMethod(clz, "isWarnEnabled", null);
			error = getMethod(clz, "isErrorEnabled", null);
		}

	}

	final static class DoubleLogger implements Logger {

		private final Logger x, y;

		DoubleLogger(Logger first, Logger second) { this.x = first; this.y = second; }

		public void info(Object msg) { x.info(msg); y.info(msg); }

		public void info(Object msg, Throwable t) { x.info(msg, t); y.info(msg, t);}

		public void info(Object msg, Throwable t, Object... args) { x.info(msg, t, args); y.info(msg, t, args); }

		public void info(Object msg, Object... args) { x.info(msg, args); y.info(msg, args); }

		public void warn(Object msg) { x.warn(msg); y.warn(msg); }

		public void warn(Object msg, Throwable t) { x.warn(msg, t); y.warn(msg, t); }

		public void warn(Object msg, Throwable t, Object... args) { x.warn(msg, t, args); y.warn(msg, t, args);}

		public void warn(Object msg, Object... args) { x.warn(msg, args); y.warn(msg, args); }

		public void error(Object msg) { x.error(msg); y.error(msg); }

		public void error(Object msg, Throwable t) { x.error(msg, t); y.error(msg, t); }

		public void error(Object msg, Throwable t, Object... args) { x.error(msg, t, args); y.error(msg, t, args); }

		public void error(Object msg, Object... args) { x.error(msg, args); y.error(msg, args); }

		public void debug(Object msg) { x.debug(msg); y.debug(msg); }

		public void debug(Object msg, Throwable t) { x.debug(msg, t); y.debug(msg, t); }

		public void debug(Object msg, Throwable t, Object... args) { x.debug(msg, t, args); y.debug(msg, t, args); }

		public void debug(Object msg, Object... args) { x.debug(msg, args); y.debug(msg, args); }

		public void trace(Object msg) { x.trace(msg); y.trace(msg); }

		public void trace(Object msg, Throwable t) { x.trace(msg, t); y.trace(msg, t); }

		public void trace(Object msg, Throwable t, Object... args) { x.trace(msg, t, args); y.trace(msg, t, args); }

		public void trace(Object msg, Object... args) { x.trace(msg, args); y.trace(msg, args); }

		public boolean isDebugEnabled() { return x.isDebugEnabled() || y.isDebugEnabled(); }

		public boolean isWarnEnabled() { return x.isWarnEnabled() || y.isWarnEnabled(); }

		public boolean isErrorEnabled() { return x.isErrorEnabled() || y.isErrorEnabled(); }

		public boolean isInfoEnabled() { return x.isInfoEnabled() || y.isInfoEnabled(); }

		public boolean isTraceEnabled() { return x.isTraceEnabled() || y.isTraceEnabled(); }

		public String toString() { return x.toString(); }


	}

	private static String preformat(String s, int max) {
		if (s==null || s.isEmpty()) return s;
		if (!s.contains("{}") && s.indexOf('\'') < 0) return s;
		char ch, lch = (char)0;
		int j = 0;
		boolean q = false;
		StringBuilder sb = new StringBuilder(s.length()+3);
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			if (ch == '}' && lch == '{' ) { sb.append(j); if (j<max) j++; }
			else if (ch == '\'') { q =! q; }
			else if (q) { q =! q; sb.append(lch); }

			sb.append(ch);lch = ch;
		}
		if (q) sb.append(lch);

		return sb.toString();
	}

	public static String format(String pattern, Object arg) {
		return format(pattern, new Object[] {arg});
	}

	public static String format(String pattern, Object[] args) {
		if (null == pattern || args == null || pattern.isEmpty() || args.length == 0) return pattern;
		return new MessageFormat(preformat(pattern, args.length-1)).format(args);
	}

}
