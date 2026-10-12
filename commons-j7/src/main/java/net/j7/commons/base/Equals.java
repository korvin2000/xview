package net.j7.commons.base;


import java.io.Serializable;
import java.util.*;

import net.j7.commons.strings.StringUtils;

/**
 * The Class Equals.
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final  class Equals {

    /** The Constant FAUTE. */
    private static final int FAUTE = 1000;

   /** The EOA. */
   private static double EOA = 0.0001d; //error of approximation


   /**
    * Checks if two Objects are equal.
    *
    * @param o1 the o1
    * @param o2 that Object
    * @return <code>true</code> if aThis and aThat are equal, <code>false</code> otherwise.
    */
   public final static boolean equal(Object o1, Object o2)
   {
      return o1==o2 ? true : o1 != null ? o1.equals(o2) : false;
   }

   /**
    * Are equal for Double and Float values.
    *
    * @param d1 the d1
    * @param d2 the d2
    * @return true, if successful
    */
   public final static boolean equal(Double d1, Double d2) {
      return null != d1 && null != d2 ? Numbers.absDouble(d1 - d2) < EOA : d1 == d2;
   }

   /**
    * Are equal for Integer values.
    *
    * @param l1 the l1
    * @param l2 the l2
    * @return true, if successful
    */
   public final static boolean equal(Long l1, Long l2) {
      return l1 != null && l2 != null ? l1.longValue() == l2.longValue() : l1 == l2;
   }

   /**
    * Are equal for Dates.
    *
    * @param d1 the a this
    * @param d2 the a that
    * @return true, if successful
    */
   public final static boolean equal(Date d1, Date d2) {
      return null != d1 && null != d2 ? Numbers.absLong(d1.getTime() - d2.getTime()) < FAUTE : d1 == d2; //interval < 1 sec

   }

   /**
    * Checks if two Arrays are equal.
    * @param o1 this Arrays of objects
    * @param o2 that Arrays of objects
    * @return <code>true</code> if aThis and aThat are equal, <code>false</code> otherwise.
    */
   public final static boolean equal(Object[] o1, Object[] o2)
   {
      return Arrays.equals(o1, o2);
   }


   /**
    * Checks if two Map's.
    *
    * @param m1 the a this
    * @param m2 the a that
    * @return true, if successful
    */
   public final static boolean equal(Map m1, Map m2) {
      if (null == m1) return (null == m2);
      if (m1==m2) return true;
      if (m1.size() != m2.size()) return false;
      boolean ret = true;

      Iterator it = m1.entrySet().iterator();
      while (it.hasNext() && ret)
      {
         Map.Entry entry = (Map.Entry) it.next();
         ret = equal(entry.getValue(), m2.get(entry.getKey()));
      }
      return ret;
   }


   /**
    * Equal.
    *
    * @param s1 the s1
    * @param s2 the s2
    * @return true, if successful
    */
   public static boolean equal(Set s1, Set s2) {
      return s1 == s2 ? true : s1 == null || s2 == null ? false :
      s1.size() == s2.size() && s1.containsAll( s2 );
}


   /**
    * Equal.
    *
    * @param i1 the i1
    * @param i2 the i2
    * @return true, if successful
    */
   public final static boolean equal(Collection i1, Collection i2) {
      if (i1==i2) return true;
      if (null == i1 || null == i2) return i1 == i2;
      if (i1.size() != i2.size()) return false;

      final Iterator it1 = i1.iterator();
      final Iterator it2 = i2.iterator();
      boolean r1, r2, ret = true;

      do {
         r1 = it1.hasNext();
         r2 = it2.hasNext();
         ret = ret && (r1 == r2) && (r1 ? equal(it1.next(), it2.next()) : true);
      } while (ret && r1);

      return ret;
   }

   /**
    * Checks if two Iterable objects, such as Collections, Lists, Sets are equal.
    *
    * @param i1 the a this
    * @param i2 the a that
    *
    * @return true, if successful
    */
   public final static boolean equal(Iterable i1, Iterable i2) {

      if (i1==i2) return true;
      if (null == i1 || null == i2) return i1 == i2;

      final Iterator it1 = i1.iterator();
      final Iterator it2 = i2.iterator();
      boolean r1, r2, ret = true;

      do {
         r1 = it1.hasNext();
         r2 = it2.hasNext();
         ret = ret && (r1 == r2) && (r1 ? equal(it1.next(), it2.next()) : true);
      } while (ret && r1);

      return ret;

   }


   /**
    * Compare id.
    *
    * @param s1 the s1
    * @param s2 the s2
    * @return true, if successful
    */
   public final static boolean compareId(Serializable s1, Serializable s2) {
      return (s1 != null) ? s1.equals(s2) : false;
   }

   /**
    * Equals method for Strings or CharSequence objects.
    *
    * @param s1 the a this
    * @param s2 the a that
    * @return true, if successful
    */
   public final static boolean equal(CharSequence s1, CharSequence s2) {
      return s1==s2 ? true : s1 != null ? s1.equals(s2) : false;
    }

   /**
    * Equal.
    *
    * @param s1 the s1
    * @param s2 the s2
    * @return true, if successful
    */
   public final static boolean equal(String s1, String s2) {
      return StringUtils.compare(s1, s2);
    }

   /**
    * Equal ignore case.
    * @param s1 the s1
    * @param s2 the s2
    * @return true, if successful
    */
   public final static boolean equalIgnoreCase(String s1, String s2) {
      return StringUtils.compareIgnoreCase(s1, s2);
    }

    /**
     * Equal for arrays with numbers (Double, Float, Integer, Long, etc).
     *
     * @param n1 the a this
     * @param n2 the a that
     * @return true, if successful
     */
   public final static boolean equal(Number[] n1, Number[] n2) {
      if (null == n1) return (null == n2);

      if (n1.length != n2.length) return false;
      boolean ret = true;
      for (int i = 0; ret && i < n1.length; i++)
                     ret = equal(n1[i], n2[i]);
      return ret;
   }

   /**
    * Checks if two Objects are equal.
    * @param check if <code>true</code> check is performed, otherwise <code>false</code> is returned without a check
    * @param aThis this Object
    * @param aThat that Object
    * @return <code>true</code> if aThis and aThat are equal, <code>false</code> otherwise.
    */
   static public boolean equal(boolean check, Object aThis, Object aThat)
   {
      return check && equal(aThis, aThat);
   }

   /**
    * Equal.
    *
    * @param arg1 the arg1
    * @param arg2 the arg2
    * @return true, if successful
    */
   public final static boolean equal(double[] arg1, double[] arg2) {
      return Arrays.equals(arg1, arg2);
   }

   /**
    * Equal.
    *
    * @param arg1 the arg1
    * @param arg2 the arg2
    * @return true, if successful
    */
   public final static boolean equal(float[] arg1, float[] arg2) {
      return Arrays.equals(arg1, arg2);
   }

   /**
    * Equal.
    *
    * @param arg1 the arg1
    * @param arg2 the arg2
    * @return true, if successful
    */
   public final static boolean equal(long[] arg1, long[] arg2) {
      return Arrays.equals(arg1, arg2);
   }

   /**
    * Equal.
    *
    * @param arg1 the arg1
    * @param arg2 the arg2
    * @return true, if successful
    */
   public final static boolean equal(int[] arg1, int[] arg2) {
      return Arrays.equals(arg1, arg2);
   }

   /**
    * Equal.
    *
    * @param arg1 the arg1
    * @param arg2 the arg2
    * @return true, if successful
    */
   public final static boolean equal(boolean[] arg1, boolean[] arg2) {
      return Arrays.equals(arg1, arg2);
   }



}
