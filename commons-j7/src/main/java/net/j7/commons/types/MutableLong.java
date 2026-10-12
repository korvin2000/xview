package net.j7.commons.types;

/**
 * Class MutableLong.
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 */
public final class MutableLong extends Number implements Comparable<MutableLong> {



private static final long serialVersionUID = 1L;

   /** The value. */
   public long value;

   public MutableLong() {
	   super();
   }

   public MutableLong(long value) {
	    super();
		this.value = value;
	}

   /**
    * Gets the value.
    *
    * @return the value
    */
   public long getValue() {

      return value;
   }

   /**
    * Sets the value.
    *
    * @param value the value to set
    */
   public void setValue(long value) {

      this.value = value;
   }

   /**
    * Increment value.
    *
    * @return the long
    */
   public long inc() {

      return ++value;
   }

   /**
    * Increment value.
    *
    * @param increment the increment
    * @return the long
    */
   public long inc(long increment) {

      value += increment;
      return value;
   }

   public long dec(long decrement) {

      value -= decrement;
      return value;
   }

   @Override
   public double doubleValue() {

      return value;
   }

   @Override
   public float floatValue() {

      return value;
   }

   @Override
   public int intValue() {

      return (int) value;
   }

   @Override
   public long longValue() {

      return value;
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(MutableLong o) {
      long thisVal = this.value;
      long anotherVal = (o != null ? o.getValue() : 0l);
      return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
  }


   @Override
   public int hashCode() {

      return (int) (value ^ (value >>> 32));
   }

   @Override
   public boolean equals(Object obj) {

      if (obj instanceof MutableLong) {
         return value == ((MutableLong) obj).longValue();
      }
      return false;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return String.valueOf(value);
   }

}
