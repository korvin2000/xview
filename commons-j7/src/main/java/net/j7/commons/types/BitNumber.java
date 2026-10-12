/*
 * @File: BitNumber.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #3 $Date: $
 *
 *
 */
package net.j7.commons.types;

import java.util.*;

import net.j7.commons.utils.BitUtils;

/**
 * The Class BitNumber to work with bit masks.
 *
 * @author  tavrovsa
 * @version $Revision: #3 $
 * @see BitUtils
 */
public class BitNumber extends Number implements Comparable<Object>, java.io.Serializable {

   /** The Constant LONG_BIT_SIZE. */
   static final long LONG_BIT_SIZE = Long.SIZE; // Long.SIZE since java 1.5

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 6803026912588528196L;

   /** The bits. */
   long[] bits;

   /** The enabled. */
   private boolean enabled;

   private int length;

   /**
    * Instantiates a new bit number.
    */
   public BitNumber() {

      this(0l, LONG_BIT_SIZE, false);
   }

   /**
    * Instantiates a new bit number.
    *
    * @param valueLong the value long
    */
   public BitNumber(long valueLong) {

      this(valueLong, LONG_BIT_SIZE, true);
   }

   /**
    * Instantiates a new bit number.
    *
    * @param bits the bits
    */
   public BitNumber(String bits) {

      this(0l, bits.length(), true);
      for (int i = 0; i < bits.length(); i++)
         if ('1' == bits.charAt(i))
            this.setBit(i, true);
   }

   /**
    * Instantiates a new bit number.
    *
    * @param valueLong the value long
    * @param bitsCnt the bits cnt
    * @param state the state
    */
   public BitNumber(long valueLong, long bitsCnt, boolean state) {
      initBits(valueLong, bitsCnt);
      enabled = state;
   }

	private void initBits(long valueLong, long bitsCnt) {
		long dimension = getLongOrder(bitsCnt);

	      bits = new long[(int) dimension];
	      bits[0] = valueLong;
	      length = 0;

	}

   public void clear() {
	   initBits(0l, LONG_BIT_SIZE);
   }

   /**
    * Gets the long order.
    *
    * @param bitNum the bit num
    * @return the long order
    */
   private int getLongOrder(long bitNum) {

	  if (this.length < bitNum) this.length = (int)bitNum;

      long dimension = (bitNum - 1) / LONG_BIT_SIZE + 1;

      if (dimension < 1) {
         dimension = 1;
      }

      return (int) dimension;
   }

   /**
    * Ensure capasity.
    *
    * @param elements the elements
    */
   private void ensureCapasity(int elements) {

      if (elements >= bits.length) {
         long[] newBits = new long[elements + 1];

         System.arraycopy(bits, 0, newBits, 0, bits.length);
         bits = newBits;
      }
   }

   /**
    * Sets the bit.
    *
    * @param bitPosition the bit position
    * @param value the value
    */
   public void setBit(long bitPosition, boolean value) {

      int whichLong = getLongOrder(bitPosition + 1) - 1;

      ensureCapasity(whichLong);

      long bitNum = (long) 1 << (bitPosition % LONG_BIT_SIZE);

      bits[whichLong] = value
            ? bits[whichLong] | bitNum
            : bits[whichLong] - (bits[whichLong] & bitNum);
   }

   /**
    * Gets the bit.
    *
    * @param bitPosition the bit position
    * @return the bit
    */
   public boolean getBit(long bitPosition) {

      if (bitPosition < 0) {
         return false;
      }

      int whichLong = getLongOrder(bitPosition + 1) - 1;

      if (whichLong >= bits.length) {
         return false;
      }

      long bitNum = (long) 1 << (bitPosition % LONG_BIT_SIZE);

      return (bits[whichLong] & bitNum) == bitNum;
   }

   /**
    * Gets the first element.
    *
    * @return the first element
    */
   private Long getFirstElement() {

      return new Long(bits[0]);
   }

   /* (non-Javadoc)
    * @see java.lang.Number#doubleValue()
    */
   @Override
   public double doubleValue() {

      return getFirstElement().doubleValue();
   }

   /* (non-Javadoc)
    * @see java.lang.Number#floatValue()
    */
   @Override
   public float floatValue() {

      return getFirstElement().floatValue();
   }

   /* (non-Javadoc)
    * @see java.lang.Number#intValue()
    */
   @Override
   public int intValue() {

      return getFirstElement().intValue();
   }

   /* (non-Javadoc)
    * @see java.lang.Number#longValue()
    */
   @Override
   public long longValue() {

      return getFirstElement().longValue();
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(Object arg0) {

      return this.equals(arg0)
            ? 0
            : -1;
   }

   /**
    * Returns a hash code value for the array.
    *
    * @param array the array to create a hash code value for
    * @return a hash code value for the array
    */
   private static int hashCode(long[] array) {

      final int prime = 31;

      if (array == null) {
         return 0;
      }

      int result = 1;

      for (int index = 0; index < array.length; index++) {
         result = prime * result + (int) (array[index] ^ (array[index] >>> 32));
      }

      return result;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      int result = 1;

      result = 31 + hashCode(bits);

      return result;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final BitNumber other = (BitNumber) obj;

      if (!Arrays.equals(bits, other.bits)) {
         return false;
      }

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      StringBuffer sb = new StringBuffer(bits.length * 64);
      boolean first = false;

      for (int i = bits.length - 1; i >= 0; i--) {
         int size = (int) (LONG_BIT_SIZE - 1);

         for (int j = size; j >= 0; j--) {
            boolean bit = getBit(i * LONG_BIT_SIZE + j);

            if (bit && !first) {
               first = true;
            }

            if (first) {
               sb.insert(0, getBit(i * LONG_BIT_SIZE + j)
                     ? '1'
                     : '0');
            }
         }
      }

      return sb.toString();
   }

   /**
    * Enabled bits.
    *
    * @return the int
    */
   public int enabledBits() {

      boolean first = false;
      int totalBits = 0;
      for (int i = bits.length - 1; i >= 0; i--) {
         int size = (int) (LONG_BIT_SIZE - 1);

         for (int j = size; j >= 0; j--) {
            boolean bit = getBit(i * LONG_BIT_SIZE + j);

            if (bit && !first) {
               first = true;
            }

            if (first) {
               if (getBit(i * LONG_BIT_SIZE + j))
                  totalBits++;
            }
         }
      }

      return totalBits;
   }

   /**
    * Checks if is enabled.
    *
    * @return the enabled
    */
   public final boolean isEnabled() {

      return enabled;
   }

   /**
    * Sets the enabled.
    *
    * @param enabled the enabled to set
    */
   public final void setEnabled(boolean enabled) {

      this.enabled = enabled;
   }

   public static BitNumber getInstance(EnumSet<?> set) {
	   BitNumber ret =  new BitNumber();
	   if (set == null || set.isEmpty()) return ret;
	   for (Enum<?> next : set) {
		ret.setBit(next.ordinal(), true);
	   }
	   return ret;
   }

   public int size() {
	   return (toString()).length();
   }

   public int length() {
	   return size();
   }

   public <T extends Enum<T>> EnumSet<T> getEnumSet(Class<T> enumClass) {
	   if (enumClass == null) return null;
	   EnumSet<T> set = EnumSet.noneOf(enumClass);
	   T[] possibleValues = enumClass.getEnumConstants();
	   Map<Integer, T> map = new HashMap<Integer, T>(possibleValues.length);
	   for (T next : possibleValues) {
		   map.put(next.ordinal(), next);
	   }
	   int count = size();
	   for (int i = 0; i < count; i++) {
		   if (this.getBit(i)) {
			   T toAdd = map.get(i);
			   if (toAdd != null) set.add(toAdd);
		   }
	   }
	   return set;
   }

//   enum Alex {A,B,C,D,E,F, I, K,L,M,R,O,P,Q};
//   Alex a = Alex.A;
//   public static void main(String[] args) {
//
//	   EnumSet<Alex> bs = EnumSet.of(Alex.P, Alex.A, Alex.E, Alex.D);
//
//	   	BitNumber bbb = BitNumber.getInstance(bs);
//
//	   	EnumSet<Alex> bs2 = bbb.getEnumSet(Alex.class);
//	   	System.out.println(bs);
//	   	System.out.println(bs2);
//
//}

}
