/*
 * @File: CommonUtils.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #1 $Date: $
 *
 *
 */
package net.j7.commons.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;


/**
 * The Class CommonUtils with common simple, but useful methods
 * all methods in this class compatible with java 1.5 and higher
 * @author  tavrovsa
 */
@SuppressWarnings("rawtypes")
public class CommonUtils {

   /**
    * Checks if an Object (eG. String, List, Map, Array) is empty or null
    * @param obj the Object
    * @return true, if is empty
    */

   public final static boolean isEmpty(final Object obj) {

      return (null == obj)
            ? true
            : (obj instanceof CharSequence)
                  ? ((CharSequence) obj).length() == 0
                  : (obj instanceof Iterable)
                        ? (obj instanceof Collection ? ((Collection) obj).isEmpty() : !((Iterable) obj).iterator().hasNext())
                        : (obj instanceof Map)
                              ? ((Map) obj).size() == 0
                              : (obj instanceof Object[])
                                    ? ((Object[]) obj).length == 0
                                    : false;
   }



   /**
    * Gets the first element from the Iterable object
    * @param Iterable<E> lst
    * @return first <E> object
    * @throws Exception
    */
   public static <E>E getFirstElement(Iterable<E> lst) throws Exception {
       if (lst != null)
       for (E property : lst)
           return property;

       return null;
   }

   /**
    * Gets the first element from the Map<?, E> object
    * @param Map<?, E> mapObj
    * @return first <E> object
    * @throws Exception
    */
   public static <E>E getFirstElement(Map<?, E> mapObj) throws Exception {
     if ( mapObj != null )
     for (Map.Entry<?, E> entry : mapObj.entrySet())  return entry.getValue();

       return null;
   }

  /**
   * Nvl function. Same as in Oracle SQL. NVL lets you replace null (returned
   * as a blank) with a string in the results of a query. If expr1 is null,
   * then NVL returns expr2. If expr1 is not null, then NVL returns expr1.
   *
   * @param expr1
   * @param expr2
   *
   * @return return (null != expr1) ? expr1 : expr2;
   */
   public static <E>E nvl(E expr1, E expr2) {
     return (null != expr1) ? expr1 : expr2;
   }

  /**
   * Nvl2 function. Same as in Oracle SQL. NVL2 lets you determine the value
   * returned by a query based on whether a specified expression is null or
   * not null. If expr1 is not null, then NVL2 returns expr2. If expr1 is
   * null, then NVL2 returns expr3.
   *
   * @param expr1
   * @param expr2
   * @param expr3
   *
   * @return return (null != expr1) ? expr2 : expr3;
   */
   public static <E>E nvl2(Object expr1, E expr2, E expr3) {
     return (null != expr1) ? expr2 : expr3;
   }

   /**
    * Method for Fast Typed Arrays Copying
    * @param T[] original - input array source
    * @return <T> T[] - output copied array
    */
   @SuppressWarnings("unchecked")
   public final static <T>T[] arrayCopy(final T[] original) {

       Class<?>	arrayType	= original.getClass().getComponentType();
       T[]			copy		= (T[]) java.lang.reflect.Array.newInstance(arrayType, original.length);

       System.arraycopy(original, 0, copy, 0, original.length);

       return copy;
   }

}
