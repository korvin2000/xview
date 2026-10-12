package net.j7.commons.types;

/**
 * Class MutableFloat.
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 */
public final class MutableFloat extends Number implements Comparable<MutableFloat> {

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   /** The value. */
   public float value;

   /**
    * Gets the value.
    *
    * @return the value
    */
   public float getValue() {

      return value;
   }

   /**
    * Sets the value.
    *
    * @param value the value to set
    */
   public void setValue(float value) {

      this.value = value;
   }

   /**
    * Inc.
    *
    * @return the double
    */
   public float inc() {

      return ++value;
   }

   /**
    * Inc.
    *
    * @param increment the increment
    * @return the double
    */
   public float inc(float increment) {

      value += increment;
      return value;
   }

   /**
    * Dec.
    *
    * @param decrement the decrement
    * @return the double
    */
   public double dec(float decrement) {

      value -= decrement;
      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#doubleValue()
    */
   @Override
   public double doubleValue() {

      return (double)value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#floatValue()
    */
   @Override
   public float floatValue() {

      return  value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#intValue()
    */
   @Override
   public int intValue() {

      return (int) value;
   }

   /* (non-Javadoc)
    * @see java.lang.Number#longValue()
    */
   @Override
   public long longValue() {

      return (long) value;
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(MutableFloat o) {
      return Float.compare(value, (o != null ? o.getValue() : 0F));
   }


   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return Float.floatToIntBits(value);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      return (obj instanceof MutableFloat)
            && (Float.floatToIntBits(((MutableFloat) obj).value) == Float.floatToIntBits(value));
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return String.valueOf(value);
   }

}
