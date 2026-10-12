package net.j7.commons.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.j7.commons.base.Equals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The Class Obj.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public final class Obj {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(Obj.class);

   /** The Constant EMPTY_OBJECT_ARRAY. */
   private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0]; // Used to call constructor
   private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

   /** The Constant QuotationMarks. */
   private static final String QT = "()";

   /** The Constant Dot. */
   private static final String DT = ".";

   /** The cached methods. */
   private static transient Map<String, Method> cachedMtds = new HashMap<String, Method>(128);

   /** The cached fields. */
   private static transient Map<String, Field> cachedFlds = new HashMap<String, Field>(128);

   /**
    * returns generated "Key".
    *
    * @param classArg the input class
    * @param name the name
    * @return the string
    */
   private final static String key(final Class<?> classArg, final String name, Class[] args) {
      StringBuilder sb =  new StringBuilder(100).append(name).append('@').append(
            classArg != null ? classArg.getName() : null);

      if (null != args)
      for (Class next : args)
		sb.append('*').append(next == null ? '-' : next.getName());

      return sb.toString();
   }

   public static void set(final Object obj, final String str, Object value) {
	      interate(obj, new StringBuilder(str), 1, value);
	   }

   /**
    * Gets the result value.
    *
    * @param obj the obj
    * @param str the str
    * @return the object
    */
   public static Object get(final Object obj, final String str) {
      return interate(obj, new StringBuilder(str), 0, null);
   }

   /**
    * Gets the result (with generics).
    *
    * @param <E> the element type
    * @param obj the obj
    * @param str the str
    * @return the instanceof E<E> object
    */
   public static <E>E getValue(final Object obj, final String str) {
      return (E)get(obj, str);
   }

//   /**
//    * Gets the string value.
//    *
//    * @param obj the obj
//    * @param str the str
//    * @return the string
//    */
//   public static String getString(final Object obj, final String str) {
//      final Object ret = get(obj, str);
//      return ret != null  ? ret.toString() : StringUtils.EMPTY;
//
//   }
//
//   /**
//    * Gets the integer value.
//    *
//    * @param obj the obj
//    * @param str the str
//    * @return the integer
//    */
//   public static int getInteger(final Object obj, final String str) {
//      final Number ret = (Number) get(obj, str);
//      return ret != null  ? ret.intValue() : 0;
//   }
//
//   /**
//    * Gets the double value.
//    *
//    * @param obj the obj
//    * @param str the str
//    * @return the double
//    */
//   public static double getDouble(final Object obj, final String str) {
//      final Number ret = (Number) get(obj, str);
//      return ret != null  ? ret.doubleValue() : 0d;
//   }


   /**
    * Interate method.
    *
    * @param obj the obj
    * @param str the str
    * @return the object
    */
   private static Object interate(final Object obj, final StringBuilder str, int mode, Object toSet) {

      final int idx = str.indexOf(DT);
      String subS;
      if (idx < 0) {
         subS = str.toString();
         str.delete(0, str.length());
      } else {
         subS = str.substring(0, idx);
         str.delete(0, idx + 1);
      }

      boolean isMethod = subS.endsWith(QT);
      boolean lastChunk = str.length()==0;
      boolean setMode = lastChunk && mode == 1;
      if (isMethod) {
    	  subS = subS.substring(0, subS.length() - 2);
      }

      Object res;
      if (setMode) {
    	  res = isMethod ? setMethodValue(obj, subS, toSet) : setFieldValue(obj, subS, toSet);
      } else {
    	  res = isMethod ?  getMethodValue(obj, subS) : getFieldValue(obj, subS);
      }

      if (res != null && !lastChunk) return interate(res, str, mode, toSet);

      return res;
   }

/**
    * Gets the value any.
    *
    * @param o the o
    * @param key the key
    * @return the value any
    */
   private final static Object getFieldValue(final Object o, final String key) {
      return
           o instanceof Map ? ((Map) o).get(key)
         : getFieldValue0(o, key);
   }

   private static Object setFieldValue(Object o, String key, Object toSet) {
		if (o instanceof Map) {
			((Map)o).put(key, toSet);
		} else {
			setFieldValue0(o, key, toSet);
		}
		return null;

	}

   /**
    * Assigned cached method.
    *
    * @param cls the cls
    * @param name the name
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
    * @return the method
    */
   private final static Method assignedMethod(final Class<?> cls, final String name, Class[] args) {
      final Method[] methods = cls.getMethods();

      for (Method method : methods) {
    	  if (isRightMethod(name, method, args)) return method;
	}

      return null;
   }

	public static boolean isRightMethod(final String name, Method method, Class[] args) {
		if (!Equals.equalIgnoreCase(name, method.getName())) return false;
		Class<?>[] mArgs = method.getParameterTypes();
		if (mArgs.length != args.length) return false;
		int j = 0;
		for (Class<?> next : mArgs) {
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
    * @return the method value
    */
   private final static Object getMethodValue(final Object obj, final String name) {

      final Method method = assignedCachedMethod(obj.getClass(), name, EMPTY_CLASS_ARRAY);

      if (method != null)
         try {
            return method.invoke(obj, EMPTY_OBJECT_ARRAY);
         } catch (final Exception e) {
            logger.warn(String.format("Error getting method value (%s,%s):  ",name,obj), e);
         }
      return null;
   }


	private static Object setMethodValue(Object obj, String methodName, Object toSet) {

		  Class[] args = extractArgTypes(toSet, 1);
	      final Method method = assignedCachedMethod(obj.getClass(), methodName, args);

	      if (method != null)
	         try {
	            return method.invoke(obj, new Object[] {toSet});
	         } catch (final Exception e) {
	            logger.warn(String.format("Error setting method value (%s,%s):  ",methodName,obj), e);
	         }
	      return null;

	}

	private static final Class[] extractArgTypes(Object arg, int mode) {
		if (mode == 0) return EMPTY_CLASS_ARRAY;
		Class[] ret = new Class[1];
		ret[0] = arg != null ? arg.getClass() : null;
		return ret;
	}

}
