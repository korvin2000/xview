/*
 * @File: DateUtils.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #4 $Date: $
 *
 *
 */
package net.j7.commons.utils;

import java.util.Calendar;
import java.util.Date;

import ch.qos.logback.classic.pattern.DateConverter;

/**
 * The Class DateUtils - common methods to work with java.util.Date classes
 *
 * @author  tavrovsa
 * @version $Revision: #4 $
 */
public class DateUtils {

   /** The Constant MILLISECOND. */
   public final static long MILLISECOND = 1l; // ms

   /** The Constant YEAR_START_XX_CENTURY. */
   public static final int YEAR_START_XX_CENTURY = 1900;

   /** The Constant YEAR_START_XXI_CENTURY. */
   public static final int YEAR_START_XXI_CENTURY = 2000;

   public enum TimeUnit {
      Millisecond(MILLISECOND),
      TenthSec(Millisecond.ms() * 100D),
      Second(Millisecond.ms() * 1000D),
      HalfSecond(Second.ms() / 2D),
      Tertia(Second.ms / 60D),
      Minute(Second.ms * 60D),
      HalfMinute(Second.ms * 30D),
      QuarterMinute(Minute.ms / 4D),
      Ke(Second.ms() * 864), // traditional Chinese unit of decimal time
      Hour(Minute.ms() * 60D),
      HalfHour(Minute.ms() * 30D),
      QuarterHour(Hour.ms() / 4D),
      HourAcademic(Minute.ms() * 45D),
      Day(Hour.ms() * 24D),
      HalfDay(Hour.ms() * 12D),
      QuarterDay(Hour.ms() * 6D),
      WorkDay(Hour.ms() * 8D), // 9 to 5, or working time (Eight Hour Day), is the standard work hours for many
                               // employees in europa, usa, australia, new zealand.
      Week(Day.ms() * 7D),
      Fortnight(Day.ms() * 14D),
      GregorianYear(Day.ms * 365.2425), // avg # days in year according to Gregorian calendar
      CommonYear(Day.ms() * 365),
      LeapYear(Day.ms() * 366),
      MonthAverage(GregorianYear.ms / 12.0), // avg # days in month according to Gregorian calendar
      Quarter(MonthAverage.ms() * 3), // 3 months
      DecadeAverage(GregorianYear.ms * 10), // avg # days in decade (10 years) according to Gregorian calendar
      Indiction(GregorianYear.ms * 15), // indiction is any of the years in a 15-year cycle used to date medieval
                                        // documents throughout Europe
      CenturyAverage(DecadeAverage.ms * 10), // avg # days in century (100 years) according to Gregorian calendar
      MilleniumAverage(CenturyAverage.ms * 10); // avg # days in millenium (1000 years) according to Gregorian calendar
      ;

      private final double ms;

      private TimeUnit(double lengthInMilliseconds) {

         this.ms = lengthInMilliseconds;
      }

      /**
       * Returns this time interval in milliseconds.
       */
      public long ms() {

         return (long) ms;
      }
   }

   /**
    * Gets the date only.
    *
    * @param date the date
    * @return the date only
    */
   public final static Date getDateOnly(Date date) {

      return getDateOnly(date, true);
   }

   /**
    * Gets the date only - Returns date without time information.
    *
    * @param date date to be adjusted
    * @param resetMillisecond the reset millisecond
    * @return the date only
    */
   public final static Date getDateOnly(Date date, boolean resetMillisecond) {

      Calendar c = Calendar.getInstance();
      c.setTime(date);
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      if (resetMillisecond)
         c.set(Calendar.MILLISECOND, 0);
      return c.getTime();
   }

   /**
    * Gets the current date.
    *
    * @return Current date (with time part reset to zero).
    */
   public static Date getCurrentDate() {

      Calendar calendar = Calendar.getInstance();

      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      return calendar.getTime();
   }

   /**
    * Returns time without date information.
    *
    * @param date date to be adjusted
    * @return Returns date object without time information
    */
   public static Date getTimeOnly(Date date) {

      if (null == date) {
         date = new Date();
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.YEAR, 0);
      calendar.set(Calendar.MONTH, 0);
      calendar.set(Calendar.DAY_OF_MONTH, 0);

      return calendar.getTime();
   }

   /**
    * Gets the end of month.
    *
    * @param date to get the end of month for
    * @return last day of month.
    */
   public static Date getEndOfMonth(Date date) {

      if (null == date) {
         date = new Date();
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      calendar.add(Calendar.MONTH, 1);
      calendar.add(Calendar.DATE, -1);

      return calendar.getTime();
   }

   /**
    * Gets the begin of month.
    *
    * @param date to get the begin of month for
    * @return first day of month.
    */
   public static Date getBeginOfMonth(Date date) {

      if (null == date) {
         date = new Date();
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      return calendar.getTime();
   }

   /**
    * Gets the date only (without time).
    *
    * @return the date only
    */
   public final static Date getDateOnly() {

      Calendar c = Calendar.getInstance();
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTime();
   }

   /**
    * Gets the date time.
    *
    * @return the date time
    */
   public final static Date getDateTime() {

      return new Date();
   }

   /**
    * Min.
    *
    * @param dates the dates
    * @return the date
    */
   public final static Date min(Date... dates) {

      Date min = null;
      Date curr;
      if (dates != null)
         for (int i = 0; i < dates.length; i++) {
            curr = dates[i];
            if (i == 0)
               min = curr;
            else if (curr != null) {
               if (min == null)
                  min = curr;
               else if (min.after(curr))
                  min = curr;
            }
         }
      return min;
   }

   /**
    * Max.
    *
    * @param dates the dates
    * @return the date
    */
   public final static Date max(Date... dates) {

      Date max = null;
      Date curr;
      if (dates != null)
         for (int i = 0; i < dates.length; i++) {
            curr = dates[i];
            if (i == 0)
               max = curr;
            else if (curr != null) {
               if (max == null)
                  max = curr;
               else if (max.before(curr))
                  max = curr;
            }
         }
      return max;
   }

   /**
    * Convert minutes to milliseconds.
    *
    * @param minutes as long
    * @return milliseconds as the long
    */
   public static long minutesToMs(long minutes) {

      return minutes * TimeUnit.Minute.ms();
   }

   /**
    * Convert milliseconds to minutes.
    *
    * @param ms the ms
    * @return minutes as long
    */
   public static long msToMinutes(long ms) {

      return ms / TimeUnit.Minute.ms();
   }

   /**
    * Compares two dates and returns difference (date1 - date2) in passed units.
    *
    * @param date1 - first date to compare. Cannot be null
    * @param date2 - second date to compare. Cannot be null
    * @param unit - Time unit for difference. Possible values: <code>Calendar.HOUR</code>, <code>Calendar.MINUTE</code>,
    *        <code>Calendar.SECOND</code>. Passing units <code>Calendar.DAY_OF_MONTH</code>,
    *        <code>Calendar.DAY_OF_WEEK</code>, <code>Calendar.DAY_OF_WEEK_IN_MONTH</code>,
    *        <code>Calendar.DAY_OF_YEAR</code> causes return of date difference in days.
    * @return date difference
    */
   public static int getDifference(Date date1, Date date2, int unit) {

      if (date1 == null)
         throw new IllegalArgumentException("Passed date (arg 1) cannot be null");
      if (date2 == null)
         throw new IllegalArgumentException("Passed date (arg 2) cannot be null");

      long difference = date1.getTime() - date2.getTime();
      switch (unit) {
      case Calendar.HOUR:
         return (int) Math.round((double) difference / TimeUnit.Hour.ms());

      case Calendar.MINUTE:
         return (int) Math.round((double) difference / TimeUnit.Minute.ms());

      case Calendar.SECOND:
         return (int) Math.round((double) difference / TimeUnit.Second.ms());

      case Calendar.DAY_OF_MONTH:
      case Calendar.DAY_OF_WEEK:
      case Calendar.DAY_OF_WEEK_IN_MONTH:
      case Calendar.DAY_OF_YEAR:
         return Math.round(difference / TimeUnit.Day.ms());

      default:
         // return difference in ms
         return (int) difference;
      }
   }

   /**
    * Adds the specified (signed) amount of time to the given time field, based on the calendar's rules to the passed
    * date.
    *
    * @param date - date to change.
    * @param field - the time field. Use Calendar constants.
    * @param amount - the amount of date or time to be added to the field.
    * @return new date object
    */
   public static Date addTime(Date date, int field, int amount) {

      if (date == null)
         throw new IllegalArgumentException("Passed date cannot be null");

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(field, amount);
      return calendar.getTime();
   }

   /**
    * Correct date.
    *
    * @param date the date
    * @return the date
    */
   public final static Date correctDate(Date date) {

      if (null == date)
         return null;
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int year = cal.get(Calendar.YEAR);
      int shift = year > 100
            ? 0
            : year < 30
                  ? 2000
                  : 1900;

      if (shift > 0)
         cal.add(Calendar.YEAR, shift);
      return cal.getTime();
   }

   /**
    * Return Date object containing date and time truncated to hour.
    *
    * @param date - date to truncate
    * @return Date object containing date and time truncated to hour
    */
   public static Date truncateToHour(Date date) {

      java.util.Calendar calendar = java.util.Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(java.util.Calendar.MINUTE, 0);
      calendar.set(java.util.Calendar.SECOND, 0);
      calendar.set(java.util.Calendar.MILLISECOND, 0);

      return calendar.getTime();
   }

   /**
    * Gets the first week which ends in specified month.
    *
    * @param month initial month
    * @return first day of first week of the month
    */
   public static Date getFirstMonthWeek(Date month) {

      // Get last day of month
      Calendar firstWeek = Calendar.getInstance();
      firstWeek.setTime(month);
      firstWeek.set(Calendar.DATE, 1);
      firstWeek.set(Calendar.HOUR, 0);
      firstWeek.set(Calendar.MINUTE, 0);
      firstWeek.set(Calendar.SECOND, 0);
      firstWeek.set(Calendar.MILLISECOND, 0);
      firstWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

      return firstWeek.getTime();
   }

   /**
    * Method to get day number of week. 0 - Monday. ... 6 - Sunday.
    *
    * @param start current date.
    * @param day the day
    * @return day of week
    */
   public static Date getDayOfWeek(Date start, int day) {

      if (day == 0)
         return start;
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(start);

      calendar.add(Calendar.DAY_OF_YEAR, day);
      return calendar.getTime();
   }

   /**
    * Gets the week.
    *
    * @param date the date
    * @return week start and finish date.
    */
   private final static Date[] getWeek(Date date) {

      Date[] period = new Date[2];
      if (date == null) {
         return period;
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);

      int day = calendar.get(Calendar.DAY_OF_WEEK);
      if (day == Calendar.SUNDAY) {
         period[1] = calendar.getTime();
         calendar.add(Calendar.DAY_OF_WEEK, -6);
         period[0] = calendar.getTime();
      } else {
         calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
         period[0] = calendar.getTime();
         calendar.add(Calendar.DAY_OF_WEEK, 6);
         period[1] = calendar.getTime();
      }

      return period;
   }

   /**
    * Gets the begin of week.
    *
    * @param date to get the begin of week for
    * @return start week date.
    */
   public static Date getBeginOfWeek(Date date) {

      return getWeek(date)[0];
   }

   /**
    * Gets the end of week.
    *
    * @param date to get the end of week for
    * @return end week date.
    */
   public static Date getEndOfWeek(Date date) {

      return getWeek(date)[1];
   }

   /**
    * Return month count.
    *
    * @param startDate bounds start
    * @param endDate bounds end
    * @return count of months. (if start month == end month, count = 1)
    */
   public static int getMonthCount(Date startDate, Date endDate) {

      int count = 1;
      Calendar calendar = Calendar.getInstance();

      calendar.setTime(endDate);
      count += calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
      calendar.setTime(startDate);
      count -= calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

      return count;
   }

   /**
    * Adds some days to date.
    *
    * @param date input
    * @param count of days to be added
    * @return Date with specified amount of days added.
    */
   public static Date addDays(Date date, int count) {

      if (null == date) {
         date = new Date();
      }

      Date ret = date;
      if (0 != count) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         calendar.add(Calendar.DATE, count);

         ret = calendar.getTime();
      }

      return ret;
   }

   /**
    * Adds some minutes to date.
    *
    * @param date initial date, may be null, then current time is used
    * @param count count of minutes to add
    * @return Returns date which is count minutes later than passed in date
    */
   public static Date addMinutes(Date date, int count) {

      if (null == date) {
         date = new Date();
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(Calendar.MINUTE, count);

      return calendar.getTime();
   }

   /**
    * Adds some week to date.
    *
    * @param date the date
    * @param count the count
    * @return Date with specified amount of weeks added.
    */
   public static Date addWeek(Date date, int count) {

      if (null == date) {
         date = new Date();
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(Calendar.WEEK_OF_YEAR, count);

      return calendar.getTime();
   }

   /**
    * Adds some months to date.
    *
    * @param date the date
    * @param count the count
    * @return Date with specified amount of months added.
    */
   public static Date addMonth(Date date, int count) {

      if (null == date) {
         date = new Date();
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      calendar.add(Calendar.MONTH, count);

      return calendar.getTime();
   }


   /**
    * Returns week start date.
    *
    * @param date the date
    * @return week start date
    */
   public static Date getWeekStart(Date date) {

      if (date == null) {
         return null;
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      int day = calendar.get(Calendar.DAY_OF_WEEK);
      if (day == Calendar.SUNDAY) {
         calendar.add(Calendar.DAY_OF_WEEK, -6);
         return calendar.getTime();
      }

      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      return calendar.getTime();
   }

}
