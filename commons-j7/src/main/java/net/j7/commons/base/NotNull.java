package net.j7.commons.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.j7.commons.strings.StringUtils;

/**
 * The Class NotNull
 * Null object Pattern -  Avoid null references by providing a default object.<br>
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Wiki: "Null object Pattern"</a>
 * @author  tavrovsa
 */
@SuppressWarnings({"unchecked","rawtypes"})
public final class NotNull {


   /** The technical logger to use. */
   private static final Logger logger = LoggerFactory.getLogger(NotNull.class);

	public static final Class  [] EMPTY_CLASS_ARRAY 	= new Class [0];    // Used to lookup Constructor
    public static final Object [] EMPTY_OBJECT_ARRAY 	= new Object [0];


	/** The Constant zLong. */
	private static final transient Long 	zLong 		= Long.valueOf(0l);

	/** The Constant zInt. */
	private static final transient Integer	zInt		= Integer.valueOf(0);

	/** The Constant zDouble. */
	private static final transient Double 	zDouble		= Double.valueOf(0d);

	/** The Constant zFloat. */
	private static final transient Float 	zFloat 		= Float.valueOf(0f);

	/** The Constant zBoolean. */
	private static final transient Boolean zBoolean 	= Boolean.valueOf(false);

	/** The Constant zByte. */
	private static final transient Byte 	zByte 		= Byte.valueOf((byte)0);

	/** The Constant zShort. */
	private static final transient Short 	zShort 		= Short.valueOf((short)0);

	/** The Constant BigDecimal */
	private static final transient BigDecimal 	zBDec	= BigDecimal.valueOf(0);

	/**
	 * If an input argument is NULL returns not null empty Set<E> back
	 */
	public static final <E> Set<E> set(Set<E> s) {
		return null!=s ? s : Collections.EMPTY_SET;
	}

	/**
	 * If an input argument is NULL returns not null empty List<E> back
	 *
	 */
	public static final <E> List<E> list(List<E> s) {
		return null!=s ? s : Collections.EMPTY_LIST;
	}

	/**
	 * If an input argument is NULL returns not null empty Map<K, V> back
	 *
	 */
	public static final <K, V> Map<K, V> map(Map<K, V> s) {
		return null!=s ? s : Collections.EMPTY_MAP;
	}

	/**
	 * If an input argument is NULL returns not null empty Iterator<E> back
	 *
	 */
	public static final <E>Iterator<E> iterator(Iterator<E> s) {
		return null!=s ? s : new EmptyIterator<E>();
	}

	/**
	 * If an input argument is NULL returns not null empty Collection<E> back
	 */
	public static final  <E> Collection<E> collection(Collection<E> s) {
		return null!=s ? s : Collections.EMPTY_LIST;
	}

	/**
	 * If an input argument is NULL returns not null empty Iterable<E> back
	 */
	public static final  <E>Iterable<E> iterable(Iterable<E> s) {
		return null!=s ? s : Collections.EMPTY_LIST;
	}

	/**
	 * If an input argument is NULL returns not null empty String back
	 */
	public static final String string(String text) {
		return text != null ? text : StringUtils.EMPTY;
	}

	/**
	 * If an input argument is NULL returns current Date instance (new instance of a Date())
	 */
	public static final Date date(Date date) {
		return date != null ? date : new Date();
	}

	/**
	 * If an input argument is NULL returns not null empty array of objects back
	 */
	public static final <E>E[] array(E[] array) {
		return array != null ? array : (E[])EMPTY_OBJECT_ARRAY;
	}

	/**
	 * If an input argument is NULL returns Long(0) back
	 *
	 */
	public static final Long number(Long n) {
		return n != null ? n : zLong;
	}

	/**
	 * If an input argument is NULL returns Integer(0) back
	 */
	public static final Integer number(Integer n) {
		return n != null ? n : zInt;
	}

	/**
	 * If an input argument is NULL returns Double(0) back
	 */
	public static final Double number(Double n) {
		return n != null ? n : zDouble;
	}

	/**
	 * If an input argument is NULL returns Float(0) back
	 */
	public static final Float number(Float n) {
		return n != null ? n : zFloat;
	}

	/**
	 * If an input argument is NULL returns Boolean.FALSE back
	 */
	public static final Boolean number(Boolean n) {
		return n != null ? n : zBoolean;
	}


	/**
	 * If an input argument is NULL returns BigDecimal(0) back
	 */
	public static final BigDecimal number(BigDecimal n) {
		return n != null ? n : zBDec;
	}

	/**
	 * If an input argument is NULL returns Byte(0) back
	 */
	public static final Byte numberByte(Byte n) {
      return n != null ? n : zByte;
   }

	/**
	 * If an input argument is NULL returns Short(0) back
	 */
	public static final Short numberShort(Short n) {
      return n != null ? n : zShort;
   }


	/**
	 * Returns first not null object, if such exists<br>
	 * or null otherwise
	 *
	 */
	public static <E>E firstNotNull(E... objects) {
		if (objects != null)
		for (E object : objects) {
			if (object != null) return object;
		}
		return null;
	}

	/**
	 * Returns first not null object, if such exists<br>
	 * or null otherwise
	 *
	 */
	public static <E>E firstNotNull(Iterable<E> objects) {
		if (objects != null)
		for (E object : objects) {
			if (object != null) return object;
		}
		return null;
	}

	/**
	 * Returns not null instance of an object of a given class/interface<br>
	 * Such java class should be either interface or an class with default empty constructor
	 *
	 */
	public static final <E>E object(E obj, Class<E> classId) {
		if (obj != null) return obj;
		try {
			return classId.newInstance();
		} catch (Exception e) {

			if (classId.isInterface()) {
				return (E)Proxy.newProxyInstance(classId.getClassLoader(),new Class[] { classId }, new DummyInvocationHandler());
			}

	        obj = newInstanceByPrivateConstr0(classId);
	        if (null != obj) return obj;

	        logger.error(String.format("Can't find any empty even private constructor for a given class %s", classId),e);
		}
		return null;
	}

	private static <E> E newInstanceByPrivateConstr0(Class<E> classId) {
		try {
			Constructor<E> constr = classId.getDeclaredConstructor(EMPTY_CLASS_ARRAY);
			if (null != constr) {
				constr.setAccessible(true);
				return constr.newInstance(EMPTY_OBJECT_ARRAY);
			}
		} catch (Exception ignore) {
			/* ignore */
		}
		return null;
	}

	/**
	 * The Class EmptyIterator.
	 *
	 * @param <E> the element type
	 */
	protected final static class EmptyIterator<E>  implements Iterator<E> {

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {return false;}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public E next() {return null;}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {}
	}

	protected final static class DummyInvocationHandler implements InvocationHandler{
		  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		    return null;
		  }
	}


   /**
    * Checks if collection is not empty and contains at least 1 not null object
    *
    */
   public final static boolean isNonEmptyCollection(Collection lst) {
		if (!isEmptyCollection(lst))
		for (Object next : lst) {
			if (next != null) return true;
		}
		return false;
	}

   /**
    * Checks if array is not empty and contains at least 1 not null object
    *
    */
   public final static boolean isNonEmptyArray(Object[] arr) {
		if (arr != null && arr.length > 0)
		for (Object next : arr) {
			if (next != null) return true;
		}
		return false;
	}


   /**
    * Checks if is empty collection.
    *
    * @param lst the lst
    * @return true, if is empty collection
    */
   public final static boolean isEmptyCollection(Collection lst) {
		return null == lst || 0 == lst.size();
	}

	/**
	 * Checks if is empty map.
	 *
	 * @param map the map
	 * @return true, if is empty map
	 */
	public final static boolean isEmptyMap(Map map) {
		return null == map || 0 == map.size();
	}

	/**
	 * Checks if is empty set.
	 *
	 * @param set the set
	 * @return true, if is empty set
	 */
	public final static boolean isEmptySet(Set set) {
		return isEmptyCollection(set);
	}

	/**
	 * Checks if is empty list.
	 *
	 * @param lst the lst
	 * @return true, if is empty list
	 */
	public final static boolean isEmptyList(List lst) {
		return isEmptyCollection(lst);
	}


	public static final class NullObject {

		public static final transient NullObject instance = new NullObject();

		public static boolean isNull(Object obj) {
			return (obj == NullObject.instance) ? true : false;
		}

		public static boolean notNull(Object obj) {
			return (obj == NullObject.instance) ? false : true;
		}

		public static <E>E convert(E obj) {
			return (obj == NullObject.instance) ? null : obj;
		}

	}

}
