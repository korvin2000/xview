/*
 * @File: BitUtils.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #2 $Date: $
 *
 *
 */
package net.j7.commons.utils;

import net.j7.commons.types.BitNumber;

/**
 * Utils to work with bit mask
 *
 * @author  tavrovsa
 * @version $Revision: #2 $
 * @see BitNumber
 */
public class BitUtils {

   private static final long int_bit_size = Integer.SIZE;

   private static final long long_bit_size = Integer.SIZE;

   /**
    * Sets the N'th bit in the mask, starting from position 0
    *
    * @param mask the mask
    * @param bitPosition the bit position (0 <= position < 32)
    * @param value the value
    * @return the int
    */
   public final static int set(int mask, int bitPosition, boolean value) {

      int bitNum = 1 << (bitPosition % int_bit_size);
      return value
            ? mask | bitNum
            : mask - (mask & bitNum);
   }

   /**
    * Gets the N'th bit in the mask, starting from position 0
    *
    * @param mask the mask
    * @param bitPosition the bit position (0 <= position < 32)
    * @return true, if successful
    */
   public final static boolean get(int mask, int bitPosition) {
      int bitNum = 1 << (bitPosition % int_bit_size);
      return (mask & bitNum) == bitNum;
   }

   /**
    * Sets the N'th bit in the mask, starting from position 0
    *
    * @param mask the mask
    * @param bitPosition the bit position (0 <= position < 64)
    * @param value the value
    * @return the int
    */
   public final static long set(long mask, int bitPosition, boolean value) {

      long bitNum = 1L << (bitPosition % long_bit_size);
      return value
            ? mask | bitNum
            : mask - (mask & bitNum);
   }

   /**
    * Gets the N'th bit in the mask, starting from position 0
    *
    * @param mask the mask
    * @param bitPosition the bit position (0 <= position < 64)
    * @return true, if successful
    */
   public final static boolean get(long mask, int bitPosition) {

      long bitNum = 1L << (bitPosition % long_bit_size);
      return (mask & bitNum) == bitNum;
   }

   /**
   * Counts number of 1 bits in a 32 bit unsigned number.<br>
   * example: countBitsSet(37) -> should returns 3<br>
   * 37 (100101 = 1+4+32) -> 3 bits set
   * is about 6 times slower, then
   * @param value unsigned 32 bit number whose bits you wish to count.
   * @return number of 1 bits in x.
   * @see google: Implementation of Brian Kernighan�s Algorithm.<br>
   * about 6 time slower, then Hamming Weight implementation
   */
   public final static int countOfBitsSet_2(int value) {
      int count = 0;
      while ( value != 0 )
         {
         // The result of this operation is to subtract off
         // the least significant non-zero bit. This can be seen
         // from noting that subtracting 1 from any number causes
         // all bits up to and including the least significant
         // non-zero bit to be complemented.
         //
         // For example:
         // 10101100 x
         // 10101011 x-1
         // 10101000 (x - 1) & x
         value &= value - 1;
         count++;
         }
      return count;
   }

   /**
    * Find out number of bits needed to represent a positive integer
    *
   * @param value unsigned 32 bit int value
   * @return number of bits needed to store a value
    */
   public final static int countBitsNeeded(int value) {
      return Integer.SIZE-Integer.numberOfLeadingZeros(value);
   }

   /**
   * Counts number of 1 bits in a 32 bit unsigned number.<br>
   * example: countBitsSet(37) -> should returns 3<br>
   * 37 (100101 = 1+4+32) -> 3 bits set
   *
   * @param x unsigned 32 bit number whose bits you wish to count.
   * @return number of 1 bits in x.
   * @see google: Implementation of 'Hamming Weight', 'popcount' or 'sideways addition'.<br>
   * about 6 time faster, then Brian Kernighan�s implementation
   */
   public final static  int countOfBitsSet(int value) {
      value = value - ((value >> 1) & 0x55555555);
      value = (value & 0x33333333) + ((value >> 2) & 0x33333333);
      return (((value + (value >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
   }


   /**
    * Checking the value of a flag, using bitwise AND operation<br>
    * Example:<br>
    * checkBitMaskFlag(5,4) = true<br>
    * checkBitMaskFlag(5,2) = false<br>
    * checkBitMaskFlag(5,1) = true<br>
    *
    * @param value the value
    * @param mask the mask
    * @return true, if successful
    */
   public static boolean checkBitMaskFlag(int value, int maskFlag) {
      return (value & maskFlag) == maskFlag;
   }


}
