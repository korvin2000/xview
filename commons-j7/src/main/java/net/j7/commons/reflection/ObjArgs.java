package net.j7.commons.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.j7.commons.base.Equals;
import net.j7.commons.strings.StringUtils;
import net.j7.commons.types.MutableInteger;


// TODO: Auto-generated Javadoc
/**
 * The Class Obj.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public final class ObjArgs {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(ObjArgs.class);

   /** The Constant EMPTY_OBJECT_ARRAY. */
   private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0]; // Used to call constructor

   /** The Constant EMPTY_CLASS_ARRAY. */
   private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

   /** The Constant Dot. */
   private static final String DT = ".";

   /** The cached methods. */
   private static transient Map<String, Method> cachedMtds = new HashMap<String, Method>(128);

   /** The cached fields. */
   private static transient Map<String, Field> cachedFlds = new HashMap<String, Field>(128);

   private static final Pattern pHasArgs = Pattern.compile("\\(\\s*(\\s*\\?\\s*\\,?\\s*)*\\)");
   private static final Pattern pArgMatcher = Pattern.compile("\\?\\s*(?=[,\\)])");

   /**
    * Sets the.
    *
    * @param obj the obj
    * @param str the str
    * @param values the values
    */
   public static void set(final Object obj, final String str, Object... values) {
	      interate(obj, preProcess(str), 1, values, MutableInteger.valueOf(0));
	}

   public static void setValue(final Object obj, final String str, Object value) {
	      interate(obj, preProcess(str), 1, new Object[] {value}, MutableInteger.valueOf(0));
   }

   /**
    * Call.
    *
    * @param obj the obj
    * @param str the str
    * @param values the values
    */
   public static void call(final Object obj, final String str, Object... values) {
	   set(obj, str, values);
   }


   /**
    * Gets the.
    *
    * @param obj the obj
    * @param str the str
    * @return the object
    */
   public static Object get(final Object obj, final String str) {
	   return get(obj, str, EMPTY_OBJECT_ARRAY);
   }

   /**
    * Gets the result value.
    *
    * @param obj the obj
    * @param str the str
    * @param args the args
    * @return the object
    */
   public static Object get(final Object obj, final String str, Object... args) {
      return interate(obj, preProcess(str), 0, args, MutableInteger.valueOf(0));
   }

   /**
    * Gets the value.
    *
    * @param <E> the element type
    * @param obj the obj
    * @param str the str
    * @return the value
    */
   public static <E>E getValue(final Object obj, final String str) {
	   return getValue(obj, str, EMPTY_OBJECT_ARRAY);
   }

   /**
    * Gets the result (with generics).
    *
    * @param <E> the element type
    * @param obj the obj
    * @param str the str
    * @param args the args
    * @return the instanceof E<E> object
    */
   public static <E>E getValue(final Object obj, final String str, Object... args) {
      return (E)interate(obj, preProcess(str), 0, args, MutableInteger.valueOf(0));
   }

   private static StringBuilder preProcess(String str) {

	   if (null != str && !str.isEmpty()) {
		   str = str.replaceAll("\\[", "\\.\\[");
	   }

	   StringBuilder sb = new StringBuilder(str);
	   return sb;
   }

/**
    * Gets the string value.
    *
    * @param obj the obj
    * @param str the str
    * @return the string
    */
   public static boolean asBoolean(final Object obj, final String str) {
      final Boolean ret = getValue(obj, str);
      return ret != null  ? ret.booleanValue() : false;

   }

   /**
    * Gets the string value.
    *
    * @param obj the obj
    * @param str the str
    * @return the string
    */
   public static String getString(final Object obj, final String str) {
      final Object ret = get(obj, str);
      return ret != null  ? ret.toString() : StringUtils.EMPTY;

   }

   /**
    * Gets the integer value.
    *
    * @param obj the obj
    * @param str the str
    * @return the integer
    */
   public static int getInteger(final Object obj, final String str) {
      final Number ret = (Number) get(obj, str);
      return ret != null  ? ret.intValue() : 0;
   }

   /**
    * Gets the double value.
    *
    * @param obj the obj
    * @param str the str
    * @return the double
    */
   public static double getDouble(final Object obj, final String str) {
      final Number ret = (Number) get(obj, str);
      return ret != null  ? ret.doubleValue() : 0d;
   }

   /**
    * returns generated "Key".
    *
    * @param classArg the input class
    * @param name the name
    * @param args the args
    * @return the string
    */
   private final static String key(final Class<?> classArg, final String name, Class[] args) {
      StringBuilder sb =  new StringBuilder  (100).append(name).append('@').append(
            classArg != null ? classArg.getName() : null);

      if (null != args)
      for (Class next : args)
		sb.append('*').append(next == null ? '-' : next.getName());

      return sb.toString();
   }

   /**
    * Interate method.
    *
    * @param obj the obj
    * @param str the str
    * @param mode the mode
    * @param toSet the to set
    * @return the object
    */
   private static Object interate(final Object obj, final StringBuilder str, int mode, Object[] toSet, MutableInteger offset) {

      final int idx = str.indexOf(DT);
      String subS;
      if (idx < 0) {
         subS = str.toString();
         str.delete(0, str.length());
      } else {
         subS = str.substring(0, idx);
         str.delete(0, idx + 1);
      }
      Matcher argSigMatcher = pHasArgs.matcher(subS);
      boolean isMethod = argSigMatcher.find();
      boolean lastChunk = str.length()==0;
      boolean setMode = lastChunk && mode == 1;
      int argsLength = 0;

      if (isMethod) {
    	  argsLength =   countArgs(argSigMatcher, subS);
    	  subS = argSigMatcher.replaceFirst("");
      }
      //isMethod = isMethod || (lastChunk && size(toSet, offset.intValue()) > 1);

      Object res;


      if (isMethod) {
    	  if (setMode && argsLength == 0) argsLength = 1;
    	  Object[] methodArgs = args(toSet, offset.intValue(), argsLength);
    	  offset.inc(argsLength);
    	  res = setOrGetMethod(obj, subS, methodArgs);
      } else {

    	  if (setMode) {
    		 res = setFieldValue(obj, subS, toSet, offset);
    	  } else {
    		  res = getFieldValue(obj, subS, toSet, offset);
    	  }
      }

      if (res != null && !lastChunk) return interate(res, str, mode, toSet, offset);

      return res;
   }

	private static Object[] args(Object[] src, int offset, int length) {
			if (length < 0) length = 0;
			if (offset < 0) offset = 0;
			Object[] dest = new Object[length];
			if (offset >= src.length) return dest;
			int maxLength = src.length - offset;
			if (maxLength < 0) maxLength = 0;
			System.arraycopy(src, offset, dest, 0, length < maxLength ? length : maxLength);
	return dest;
}

	public static int countArgs(Matcher matcherRoot, String s) {
		String args = matcherRoot.group();
		Matcher matcher = pArgMatcher.matcher(args);
		int x = 0;
		while (matcher.find()) {
			 x++;
	    }
		return x;
	}

	/**
	 * Gets the.
	 *
	 * @param arr the arr
	 * @param at the at
	 * @return the object
	 */
	private static Object get(Object[] arr, int at) {
		return arr != null && arr.length > at  && at < arr.length ? arr[at] : null;
	}

/**
    * Gets the value any.
    *
    * @param o the o
    * @param key the key
    * @return the value any
    */
   private final static Object getFieldValue(final Object o, String key, Object[] toSet, MutableInteger mInt) {

	   if (o instanceof Map) return ((Map) o).get(key);
	   if (o.getClass().isArray()) {
		   if ("length".equals(key)) return ((Object[])o).length;

		   int index = checkIndex(key, toSet, mInt);
		   if (-1 != index) return get((Object[])o, index);
	   }

       return  getFieldValue0(o, key);
   }

   private static int checkIndex(String key, Object[] toSet, MutableInteger mInt) {
	   if (key.startsWith("[") && key.endsWith("]")) {
		   key = key.substring(1, key.length()-1).trim();
		   int index = 0;
		   if ("?".equals(key)) {
			   index = asNum(get(toSet, mInt.intValue()));
			   mInt.inc();
		   } else {
			   index = asNum(key);
		   }
		   return index;
	   } else {
		   return -1;
	   }
   }


	private static int asNum(Object obj) {
		String key = obj!=null ? obj.toString() : null;
		if (key != null && !StringUtils.isEmpty(key))
		try {
			return Integer.parseInt(key);
		} catch (Exception e) {
			// ignore it
		}
		return 0;
	}

/**
    * Sets the field value.
    *
    * @param o the o
    * @param key the key
    * @param toSet the to set
    * @return the object
    */
   private static Object setFieldValue(Object o, String key, Object[] toSet, MutableInteger offset) {
	   if (o instanceof Map) {
			((Map)o).put(key, get(toSet, offset.intValue()));
		}
		if (o.getClass().isArray()) {
			 int index = checkIndex(key, toSet, offset);
			 if (-1 != index) {
				 ((Object[])o)[index] = get(toSet, offset.intValue());
			 }
		}
		else {
			setFieldValue0(o, key, get(toSet, offset.intValue()));
		}
		return null;

	}

   /**
    * Assigned cached method.
    *
    * @param cls the cls
    * @param name the name
    * @param args the args
    * @return the method
    */
   private final static synchronized Method assignedCachedMethod(final Class<?> cls, final String name, Class[] args) {

      final String key = key(cls, name, args);
      Method ret = cachedMtds.get(key);

      if (null == ret) {
         ret = assignedMethod(cls, name, args);

         try {
         if (ret != null && !ret.isAccessible()) ret.setAccessible(true);
         } catch (final Exception e) {};

         cachedMtds.put(key, ret);
      }

      return ret;
   }

   /**
    * Assigned cached field.
    *
    * @param cls the cls
    * @param name the name
    * @return the field
    */
   private static synchronized Field assignedCachedField(final Class<?> cls, final String name) {
      final String key = key(cls, name, null);
      Field ret = cachedFlds.get(key);

      if (null == ret) {
         ret = assignedField(cls, name);

         try {
         if (ret != null && !ret.isAccessible()) ret.setAccessible(true);
         } catch (final Exception e) {};

         cachedFlds.put(key, ret);
      }

      return ret;
   }

   /**
    * Assigned method.
    *
    * @param cls the cls
    * @param name the name
    * @param args the args
    * @return the method
    */
   private final static Method assignedMethod(final Class<?> cls, final String name, Class[] args) {
      final Method[] methods = cls.getMethods();

      for (Method method : methods) {
    	  if (isRightMethod(name, method, args)) return method;
	}

      return null;
   }

   private final static HashMap<Class, Class> autoConv;
   static {
	   autoConv = new HashMap<Class, Class>(8, 1f);
	   autoConv.put(int.class, Integer.class);
	   autoConv.put(long.class, Long.class);
	   autoConv.put(byte.class, Byte.class);
	   autoConv.put(short.class, Short.class);
	   autoConv.put(float.class, Float.class);
	   autoConv.put(double.class, Double.class);
	   autoConv.put(boolean.class, Boolean.class);
	   autoConv.put(char.class, Character.class);
   }

	/**
	 * Checks if is right method.
	 *
	 * @param name the name
	 * @param method the method
	 * @param args the args
	 * @return true, if is right method
	 */
	public static boolean isRightMethod(final String name, Method method, Class[] args) {
		if (!Equals.equalIgnoreCase(name, method.getName())) return false;
		Class<?>[] mArgs = method.getParameterTypes();
		if (mArgs.length != args.length) return false;
		int j = 0;
		for (Class<?> next : mArgs) {
			Class<?> next2 = autoConv.get(next);
			if (null != next2) next = next2;
			Class clz = args[j++];
			if (clz != null && !next.isAssignableFrom(clz)) return false;
		}
		return true;
	}

   /**
    * Gets the field by name.
    *
    * @param cls  the cls
    * @param name the name
    * @return the field by name
    */
   private static Field assignedField(final Class<?> cls, final String name) {

      if (cls == null || cls.isPrimitive())
         return null;

      Field field = null;
      try {
         field = cls.getDeclaredField(name);
      } catch (final Exception e) {
      }

      return field != null ? field : assignedField(cls.getSuperclass(), name);
   }

   /**
    * Gets the field value.
    *
    * @param obj the obj
    * @param name the name
    * @return the field value
    */
   private final static Object getFieldValue0(final Object obj, final String name) {
      final Field field = assignedCachedField(obj.getClass(), name);
      if (field != null && !field.isAccessible())
         field.setAccessible(true);
      Object value = null;
      if (field != null)
         try {
            value = field.get(obj);
         } catch (final Exception e) {
            logger.warn(String.format("Error getting field value (%s,%s): ", name, obj), e);
         }
      return value;

   }

   /**
    * Sets the field value0.
    *
    * @param obj the obj
    * @param name the name
    * @param toSet the to set
    */
   private final static void setFieldValue0(final Object obj, final String name, Object toSet) {
	      final Field field = assignedCachedField(obj.getClass(), name);
	      if (field != null && !field.isAccessible())
	         field.setAccessible(true);
	      if (field != null)
	         try {
	        	 field.set(obj, toSet);
	         } catch (final Exception e) {
	            logger.warn(String.format("Error setting field value (%s,%s): ", name, obj), e);
	         }
	   }

   /**
    * Gets the method value.
    *
    * @param obj the obj
    * @param name the name
    * @param args the args
    * @return the method value
    */
   private final static Object setOrGetMethod(final Object obj, final String name, Object[] args) {
	  Class[] clsArgs = extractArgTypes(args);
      final Method method = assignedCachedMethod(obj.getClass(), name, clsArgs);

      if (method != null)
         try {
            return method.invoke(obj, args);
         } catch (final Exception e) {
            logger.warn(String.format("Error calling method (%s,%s):  ",name,obj), e);
         }
      return null;
   }


	/**
	 * Extract arg types.
	 *
	 * @param args the args
	 * @return the class[]
	 */
	private final static Class[] extractArgTypes(final Object[] args) {
		if (null == args) return EMPTY_CLASS_ARRAY;
		int len;
		final Class[] r = new Class[(len = args.length)];

		while (len-->0)
			r[len] = args[len] != null ? args[len].getClass() : null;
		return r;
	}


}
