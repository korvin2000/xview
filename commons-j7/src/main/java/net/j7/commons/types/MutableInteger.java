package net.j7.commons.types;


/**
 * Class MutableInteger.
 *
 * @author  tavrovsa
 * @version $Revision: #2 $
 */
public final class MutableInteger extends Number implements Comparable<MutableInteger> {


   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 8344612245264468312L;


   /** The value. */
   public int value;

   public MutableInteger() {
      this(0);
   }

   public MutableInteger(int value) {
      super();
      this.value = 0;
   }


   /**
    * Gets the value.
    *
    * @return the value
    */
   public int getValue() {

      return value;
   }

   /**
    * Sets the value.
    *
    * @param value the value to set
    */
   public void setValue(int value) {
      this.value = value;
   }

   /**
    * Inc.
    *
    * @return the int
    */
   public int inc() {

      return ++value;
   }

   /**
    * Inc.
    *
    * @param increment the increment
    * @return the int
    */
   public int inc(int increment) {

      value += increment;
      return value;
   }

   /**
    * Dec.
    *
    * @param decrement the decrement
    * @return the int
    */
   public int dec(int decrement) {

      value -= decrement;
      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#doubleValue()
    */
   @Override
   public double doubleValue() {

      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#floatValue()
    */
   @Override
   public float floatValue() {

      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#intValue()
    */
   @Override
   public int intValue() {

      return value;
   }

   public static MutableInteger valueOf(int i) {
     return new MutableInteger(i);
   }

   public static MutableInteger valueOf(String s) throws NumberFormatException {
       return MutableInteger.valueOf(Integer.parseInt(s, 10));
   }

   /* (non-Javadoc)
    * @see java.lang.Number#longValue()
    */
   @Override
   public long longValue() {

      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (obj instanceof MutableInteger) {
         return value == ((MutableInteger) obj).intValue();
      }
      return false;
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(MutableInteger o) {
      int tmpVal = this.value;
      int anotherVal = (o != null ? o.getValue() : 0);
      return (tmpVal<anotherVal ? -1 : (tmpVal==anotherVal ? 0 : 1));
   }


   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return String.valueOf(value);
   }

}
