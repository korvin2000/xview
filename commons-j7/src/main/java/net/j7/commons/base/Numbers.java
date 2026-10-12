package net.j7.commons.base;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import net.j7.commons.strings.StringUtils;

@SuppressWarnings("rawtypes")
public class Numbers {

	   /**
	    * Parse input string and returns not null Long value
	    * @param val - input value
	    * @return not null Long value
	    */
	   public static Long stringAsLong(String val) {

	      Long ret = null;
	      if (!StringUtils.empty(val))
	      try {
	         ret =  Long.parseLong(val);
	      } catch (Exception e) {

	         try {
	            ret =  Double.valueOf(Double.parseDouble(val.trim())).longValue();
	         } catch (Exception e2) {}

	      }

	      return ret != null ? ret : Long.valueOf(0l);

	   }


	   /**
	    * Parse input string and returns not null Integer value
	    * @param val - input value
	    * @return not null Integer value
	    */
	   public static Integer stringAsInteger(String val) {

	      Integer ret = null;
	      if (!StringUtils.empty(val))
	      try {
	         ret =  Integer.parseInt(val);
	      } catch (Exception e) {

	         try {
	            ret =  Double.valueOf(Double.parseDouble(val.trim())).intValue();
	         } catch (Exception e2) {}

	      }

	      return ret != null ? ret : Integer.valueOf(0);

	   }

	   /**
	    * Parse input string and returns not null Double value
	    * @param val - input value
	    * @return not null Double value
	    */
	   public static Double stringAsDouble(String val) {

	      Double ret = null;
	      if (!StringUtils.empty(val))
	      try {
	         ret =  Double.parseDouble(val);
	      } catch (Exception e) {

	         try {
	            ret =  Double.valueOf(Double.parseDouble(val.trim()));
	         } catch (Exception e2) {}

	      }

	      return ret != null ? ret : Double.valueOf(0d);

	   }



	   /**
	    * Checks if a given Numeric value is integer number value (Mathematics, not computer science sense)
	    * e.G. isValueInteger(100d) = true, isValueInteger(3.5) = false;
	    *
	    * @param number the number
	    * @return true, if is value integer
	    * @see isValueFloat
	    */
	   public static boolean isValueInteger(Number number) {
	      return number != null ? ((number.doubleValue()) == (double)number.longValue()) : false;
	   }


	   /**
	    * Checks if a given Numeric value is float number value (Mathematics, not computer science sense)
	    * e.G. isValueFloat(100d) = false, isValueFloat(3.5) = true;
	    *
	    * @param number the number
	    * @return true, if is value integer
	    * @see isValueInteger
	    */
	   public static boolean isValueFloat(Number number) {
	      return number != null ? ((number.doubleValue()) != (double)number.longValue()) : false;
	   }

	   /* ******************************************************************* *
	    * ################################################################### *
	    * #############   methods to work with primitive int types ########## *
	    * ################################################################### *
	    * ******************************************************************* * */

	   /**
	    * Maximal value.
	    * @param valOne the first value
	    * @param valTwo the second value
	    *
	    * @return maximal value as int
	    */
	   public final static int max(int valOne, int valTwo) {
	      return (valOne>valTwo) ? valOne : valTwo;
	   }

	   /**
	    * Minimal value.
	    * @param valOne the first value
	    * @param valTwo the second value
	    *
	    * @return minimal value as int
	    */
	   public final static int min(int valOne, int valTwo) {
	      return (valOne<valTwo) ? valOne : valTwo;
	   }

	    /**
	     * return Absolute value
	     * @param value as int
	     * @return absolute value
	     */
	    public final static int abs(int value) {
	        return (value >= 0) ? value : -value;
	    }

	    /**
	     * return positive / >= 0 value
	     * @param value as int
	     * @return positive value
	     */
	    public static int positive(int value) {
	        return (value >= 0) ? value : 0;
	    }

	   /* ******************************************************************* *
	    * ################################################################### *
	    * ############   methods to work with primitive long types ########## *
	    * ################################################################### *
	    * ******************************************************************* * */

	   /**
	    * Maximal value.
	    * @param val1 the first value
	    * @param val2 the second value
	    *
	    * @return the int
	    */
	   public final static long maxLong(long val1, long val2) {
	      return (val1>val2) ? val1 : val2;
	   }

	   /**
	    * Minimal value.
	    * @param val1 the first value
	    * @param val2 the second value
	    *
	    * @return the int
	    */
	   public final static long minLong(long val1, long val2) {
	      return (val1<val2) ? val1 : val2;
	   }

	    /**
	     * return Absolute value
	     * @param value as long
	     * @return absolute value as long
	     */
	    public final static long absLong(long value) {
	        return (value >= 0l) ? value : -value;
	    }

	    /**
	     * return positive / >= 0 value
	     * @param value as long
	     * @return positive value as long
	     */
	    public static long positiveLong(long value) {
	        return (value >= 0l) ? value : 0l;
	    }

	   /* ******************************************************************* *
	    * ################################################################### *
	    * ###########   methods to work with primitive double types ######### *
	    * ################################################################### *
	    * ******************************************************************* * */

	   /**
	    * Maximal double value.
	    * @param valOne the first value
	    * @param valTwo the second value
	    *
	    * @return the int
	    */
	   public final static double maxDouble(double valOne, double valTwo) {
	      return (valOne>valTwo) ? valOne : valTwo;
	   }

	   /**
	    * Minimal value.
	    * @param valOne the first value
	    * @param valTwo the second value
	    * @return the double
	    */
	   public final static double minDouble(double valOne, double valTwo) {
	      return (valOne<valTwo) ? valOne : valTwo;
	   }

	    /**
	     * return Absolute value
	     * @param value as int
	     * @return absolute value as double
	     */
	    public final static double absDouble(double value) {
	        return (value >= 0d) ? value : -value;
	    }

	    /**
	     * return positive / >= 0 value
	     * @param value as int
	     * @return positive value as double
	     */
	    public static double positiveDouble(double value) {
	        return (value >= 0d) ? value : 0d;
	    }

	   /* ******************************************************************* *
	    * ################################################################### *
	    * ###########        O T H E R     M E T H O D S            ######### *
	    * ################################################################### *
	    * ******************************************************************* * */

	    /**
	     * Gets the result as Double value.
	     * @param Object val - Number, Boolean, String, Date or BOBase
	     * @return the result as Long value
	     */
	    public final static Double getAsDouble(final Object val) {
	      Double   res   = null;

	        if (val instanceof Number)
	            res   = Double.valueOf(((Number) val).doubleValue());
	         else
	        if (val instanceof Boolean)
	            res   = Double.valueOf(((Boolean) val).booleanValue() == true ? 1d : 0d);
	         else
	        if (val instanceof String) try
	            {
	               res   = Double.valueOf(((String) val).trim());
	            } catch (Exception e) { /* ignore it */}
	            else
	        if (val instanceof Date)
	            res   = Double.valueOf(((Date) val).getTime());

	        return res;
	    }


	    /**
	     * Gets the result as long primitive value.
	     * @param Object val - Number, Boolean or String
	     * @return the result as lLong value
	     */
	    public final static double getAsDoubleP(final Object val) {
	      Double   result   = getAsDouble(val);
	        return (result != null)  ? result.doubleValue() : 0d;
	    }

	    /**
	     * Gets the result as Long value.
	     * @param Object val - Number, Boolean, String, Date or BOBase
	     * @return the result as Long value
	     */
	    public final static Long getAsLong(final Object val) {
	        final Double ret = getAsDouble(val);
	        return ret != null ? Long.valueOf(ret.longValue()) : null;
	    }

	    /**
	     * Gets the result as long primitive value.
	     * @param Object val - Number, Boolean or String
	     * @return the result as lLong value
	     */
	    public final static long getAsLongP(final Object val) {
	       final Double ret = getAsDouble(val);
	         return ret != null ? ret.longValue() : 0l;
	    }

	    /**
	     * Gets the result as Integer value.
	     * @param Object val - Number, Boolean or String
	     * @return the result as Integer value
	     */
	    public final static Integer getResultAsInteger(Object val) {
	      final Double ret = getAsDouble(val);
	        return ret != null ? Integer.valueOf(ret.intValue()) : null;
	    }

	    public final static int getResultAsInt(Object val) {
	      final Double ret = getAsDouble(val);
	        return ret != null ? ret.intValue() : 0;
	    }

	   /**
	    * Gets the double value of first element in argument list.
	    * @param double value if list is not empty, else 0.
	    * @return double value of first element in argument list.
	    */
	   public static double getResultAsDouble(List result) {
	      double res = 0d;
	      if (result.size()>0) {
	         Object val = result.get(0);
	         res = getAsDoubleP(val);
	      }
	      return res;
	   }

	   /**
	    * Gets the long value of first element in argument list.
	    *
	    * @param result the result
	    * @return long value of first element in argument list.
	    */
	   public static long getResultAsLong(List result) {
	      long res = 0;
	      if (result.size()>0) {
	         Object val = result.get(0);
	         res = getAsLongP(val);
	      }
	      return res;
	   }

	   /**
	    * Gets the int value of first element in argument list.
	    * @param int value if list is not empty, else 0.
	    * @return int value of first element in argument list.
	    */
	   public static int getResultAsInt(List result) {
	      return (int)getResultAsLong(result);
	   }


	   /**
	    * Round double value with precision 2
	    *
	    * @param val the value to round
	    * @return the double
	    */
	   public static double round(double val) {
	      return round(val, 2);
	   }

	   /**
	    * Multi-Round double value with precision 2
	    * Multi-Round:  Note that this is the
	    * rounding mode that minimizes cumulative error when applied
	    * repeatedly over a sequence of calculations
	    * @param val - input Value
	    * @return
	    */
	   public static double multiRound(double val) {
	      return multiRound(val, 2);
	   }

	   /**
	    * Round double value with given precision
	    *
	    * @param val - input Value
	    * @param precision
	    * @return
	    */
	   public static double round(double val, int precision)
	   {
	      if (! Double.isNaN(val))
	      {
	         boolean negative = val < 0d;

	         BigDecimal db = BigDecimal.valueOf(negative ? -val : val);
	         db = db.setScale(precision, BigDecimal.ROUND_HALF_UP);

	         return negative ? -db.doubleValue() : db.doubleValue();
	      }

	      return 0d;
	   }

	   /**
	    * Multi-Round double value with given precision
	    * Multi-Round:  Note that this is the
	     * rounding mode that minimizes cumulative error when applied
	     * repeatedly over a sequence of calculations
	     *
	    * @param val - input Value
	    * @param precision
	    * @return
	    */
	   public static double multiRound(double val, int precision)
	   {
	      if (! Double.isNaN(val))
	      {
	         boolean negative = val < 0d;

	         BigDecimal db = BigDecimal.valueOf(negative ? -val : val);
	         db = db.setScale(precision, BigDecimal.ROUND_HALF_EVEN);

	         return negative ? -db.doubleValue() : db.doubleValue();
	      }

	      return 0d;
	   }

	}


