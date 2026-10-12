/*
 * @File: NaturalOrderComparator.java
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
package net.j7.commons.strings;

import java.util.Comparator;

/**
 * A sorting comparator to sort strings numerically, ie [1, 2, 10], as opposed to [1, 10, 2].
 *
 * @param <E> the element type
 * @author  tavrovsa
 */
public class NaturalOrderComparator implements Comparator<String> {

    /**  Lazy-loaded Singleton, by Bill Pugh **/
    private static class Holder {
        private final static Comparator<String> INSTANCE = new NaturalOrderComparator();
    }

    public static Comparator<String> instance() {
        return Holder.INSTANCE;
    }

   /**
    * Instantiates a new natural order comparator.
    */
   private NaturalOrderComparator() {
      // no-arg constructor
   }

   /**
    * Compare right.
    *
    * @param a the a
    * @param b the b
    * @return the int
    */
   int compareRight(String a, String b) {

      int bias = 0;
      int ia = 0;
      int ib = 0;

      // The longest run of digits wins. That aside, the greatest
      // value wins, but we can't know that it will until we've scanned
      // both numbers to know that they have the same magnitude, so we
      // remember it in BIAS.
      for (;; ia++, ib++) {
         char ca = charAt(a, ia);
         char cb = charAt(b, ib);

         if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
            return bias;
         } else if (!Character.isDigit(ca)) {
            return -1;
         } else if (!Character.isDigit(cb)) {
            return +1;
         } else if (ca < cb) {
            if (bias == 0) {
               bias = -1;
            }
         } else if (ca > cb) {
            if (bias == 0)
               bias = +1;
         } else if (ca == 0 && cb == 0) {
            return bias;
         }
      }
   }

   /* (non-Javadoc)
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(String o1, String o2) {

      String a = o1 != null
            ? o1.toLowerCase()
            : StringUtils.EMPTY;
      String b = o2 != null
            ? o2.toLowerCase()
            : StringUtils.EMPTY;

      int ia = 0, ib = 0;
      int nza = 0, nzb = 0;
      char ca, cb;
      int result;

      while (true) {
         // only count the number of zeroes leading the last number compared
         nza = nzb = 0;

         ca = charAt(a, ia);
         cb = charAt(b, ib);

         // skip over leading spaces or zeros
         while (Character.isSpaceChar(ca) || ca == '0') {
            if (ca == '0') {
               nza++;
            } else {
               // only count consecutive zeroes
               nza = 0;
            }

            ca = charAt(a, ++ia);
         }

         while (Character.isSpaceChar(cb) || cb == '0') {
            if (cb == '0') {
               nzb++;
            } else {
               // only count consecutive zeroes
               nzb = 0;
            }

            cb = charAt(b, ++ib);
         }

         // process run of digits
         if (Character.isDigit(ca) && Character.isDigit(cb)) {
            if ((result = compareRight(a.substring(ia), b.substring(ib))) != 0) {
               return result;
            }
         }

         if (ca == 0 && cb == 0) {
            // The strings compare the same. Perhaps the caller
            // will want to call strcmp to break the tie.
            return nza - nzb;
         }

         if (ca < cb) {
            return -1;
         } else if (ca > cb) {
            return +1;
         }

         ++ia;
         ++ib;
      }
   }

   /**
    * gets a char at given position
    *
    * @param s the string
    * @param i the position
    * @return the char
    */
   static char charAt(String s, int i) {

      if (i >= s.length()) {
         return 0;
      } else {
         return s.charAt(i);
      }
   }

}
