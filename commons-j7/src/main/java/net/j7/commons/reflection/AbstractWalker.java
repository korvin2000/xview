package net.j7.commons.reflection;

import java.text.*;
import java.util.*;
@SuppressWarnings("unchecked")
public class AbstractWalker {


	protected static final String BASE_MODEL = "base";
	public static final String CLASS_NAME = "class";
	protected static final char DOT = '.';

	protected final static transient Set<Class> IGNR;

	protected static final int MAX_RECURSION = 1024;

	protected final static Integer FIELD_WRONG 	= Integer.valueOf(0);
	protected final static Integer FIELD_BRIDGE = Integer.valueOf(1);
	protected final static Integer FIELD_LAST 	= Integer.valueOf(2);

	static {
		IGNR = Collections.synchronizedSet(new HashSet<Class>(25));
		// add common final/immutable classes (String and TypeWrappers)
		IGNR.add(String.class);
		IGNR.add(Byte.class);
		IGNR.add(Short.class);
		IGNR.add(Integer.class);
		IGNR.add(Long.class);
		IGNR.add(Float.class);
		IGNR.add(Double.class);
		IGNR.add(Character.class);
		IGNR.add(Boolean.class);
		IGNR.add(DecimalFormat.class);
		IGNR.add(NumberFormat.class);
		IGNR.add(DateFormat.class);
		IGNR.add(SimpleDateFormat.class);
		IGNR.add(Calendar.class);
		IGNR.add(Locale.class);
		IGNR.add(StringBuilder.class);
		IGNR.add(StringBuffer.class);
		IGNR.add(java.util.Date.class);
		IGNR.add(java.math.BigDecimal.class);
		IGNR.add(java.math.BigInteger.class);
		IGNR.add(java.sql.Timestamp.class);
		IGNR.add(java.sql.Date.class);
		IGNR.add(java.sql.Time.class);
	}

	protected static final boolean instanceOf(Class baseClazz, Class implClass) {
		try {
			baseClazz.asSubclass(implClass);
			return true;
		} catch (ClassCastException cce) {
			return false;
		}
	}

//	public static void main(String[] args) {
//		System.out.println(instanceOf(Long.class, Object.class));
//	}
}
