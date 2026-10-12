package net.j7.commons.types;

// TODO: Auto-generated Javadoc
/**
 * Class MutableDouble.
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 */
public final class MutableDouble extends Number implements Comparable<MutableDouble> {

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   /** The value. */
   public double value;

   /**
    * Gets the value.
    *
    * @return the value
    */
   public double getValue() {

      return value;
   }

   /**
    * Sets the value.
    *
    * @param value the value to set
    */
   public void setValue(double value) {

      this.value = value;
   }

   /**
    * Inc.
    *
    * @return the double
    */
   public double inc() {

      return ++value;
   }

   /**
    * Inc.
    *
    * @param increment the increment
    * @return the double
    */
   public double inc(double increment) {

      value += increment;
      return value;
   }

   /**
    * Dec.
    *
    * @param decrement the decrement
    * @return the double
    */
   public double dec(double decrement) {

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

      return (float) value;
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
   public int compareTo(MutableDouble o) {
      return Double.compare(value, (o != null ? o.getValue() : 0D));
   }


   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      long bits = Double.doubleToLongBits(value);
      return (int) (bits ^ (bits >>> 32));
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      return (obj instanceof MutableDouble)
            && (Double.doubleToLongBits(((MutableDouble) obj).value) == Double.doubleToLongBits(value));
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return String.valueOf(value);
   }

}
