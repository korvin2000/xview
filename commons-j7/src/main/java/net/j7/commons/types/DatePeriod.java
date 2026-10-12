package net.j7.commons.types;


import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



/**
 * The usefull Class DatePeriod to work with some time period (Start Date < ....  < Finish Date) and not with single date only
 * @author  tavrovsa
 */
public class DatePeriod implements Comparable<DatePeriod>, Cloneable, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8712335658535556694L;

	/** The from. */
	private Date from;

	/** The to. */
	private Date to;

	/** The date_format. */
	private static DateFormat date_format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

	/**
	 * Instantiates a new date period.
	 */
	public DatePeriod() {
	}

	/**
	 * Instantiates a new date period.
	 *
	 * @param fromDate the from date
	 * @param fromOffsetDays the from offset days
	 * @param toDate the to date
	 * @param toOffsetDays the to offset days
	 */
	public DatePeriod(Date fromDate,int fromOffsetDays, Date toDate, int toOffsetDays) {
		final Date fromOffs = getDateWithOffset(fromDate, fromOffsetDays);
		final Date toOffs = getDateWithOffset(toDate, toOffsetDays);
		checkBounds(fromOffs, toOffs);
		this.from = fromOffs;
		this.to = toOffs;
	}


	/**
	 * Instantiates a new date period.
	 *
	 * @param fromDate the from date
	 * @param toDate the to date
	 */
	public DatePeriod(Date fromDate, Date toDate) {
		checkBounds(fromDate, toDate);
		this.from = fromDate;
		this.to = toDate;
	}

	/**
	 * New period.
	 *
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @return the date period
	 */
	public static DatePeriod newPeriod(Date fromDate, Date toDate) {
		return new DatePeriod(fromDate, toDate);
	}

//	public static DatePeriod newPeriod(String fromDate, String toDate) {
//		return newPeriod(Dates.get(fromDate), Dates.get(toDate));
//	}

	/**
 * New limited periond, without begin/start date, but with close/finish date.
 *
 * @param closeDate the close date
 * @return the date period
 */
	public static DatePeriod newLimitedPeriond(Date closeDate) {
		return new DatePeriod(null, closeDate);
	}

	/**
	 * New infinite period, without finish / end date.
	 *
	 * @param startDate the start date
	 * @return the date period
	 */
	public static DatePeriod newInfinitePeriod(Date startDate) {
		return new DatePeriod(startDate, null);
	}

	/**
	 * New unrestricted period.
	 *
	 * @return the date period
	 */
	public static DatePeriod newUnrestrictedPeriod() {
		return new DatePeriod();
	}

	/**
	 * Checks for start date.
	 *
	 * @return true, if successful
	 */
	public boolean hasStartDate() {
		return from != null;
	}

	/**
	 * Checks for end date.
	 *
	 * @return true, if successful
	 */
	public boolean hasEndDate() {
		return to != null;
	}

	/**
	 * Gets the min date.
	 *
	 * @return the min date
	 */
	public Date getMinDate() {
		return from != null ? from : to;
	}

	/**
	 * Gets the max date.
	 *
	 * @return the max date
	 */
	public Date getMaxDate() {
		return to != null ? to : from;
	}

	/**
	 * Checks if is unrestricted.
	 *
	 * @return true, if is unrestricted
	 */
	public boolean isUnrestricted() {
		return from == null && to == null;
	}

	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public Date getStartDate() {
		return from;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate the new start date
	 */
	public void setStartDate(Date startDate) {
		checkBounds(startDate, to);
		this.from = startDate;
	}

	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public Date getEndDate() {
		return to;
	}

	/**
	 * Sets the end date.
	 *
	 * @param toDate the new end date
	 */
	public void setEndDate(Date toDate) {
		checkBounds(from, toDate);
		this.to = toDate;
	}

	/**
	 * Contains.
	 *
	 * @param period the period
	 * @return true, if successful
	 */
	public boolean contains(DatePeriod period) {
		if (period==null || period.isUnrestricted()) return isUnrestricted();

		if (from != null && from.after(period.getMinDate())) return false;
		if (to != null && to.before(period.getMaxDate())) return false;

		return true;
	}

	/**
	 * Contains.
	 *
	 * @param date the date
	 * @return true, if successful
	 */
	public boolean contains(Date date) {
		if (date==null) return from == null && to == null;

		if (from != null && from.after(date)) return false;
		if (to != null && to.before(date)) return false;

		return true;


	}

    /**
     * Tests if begin date is reached.
     *
     * @param beginDate the begin date
     * @return true, if is begin date reached
     */
	public boolean isBeginDateReached(Date beginDate) {
		if (beginDate == null) return  from == null && to == null;
		Date minDate = getMinDate();
		return minDate != null ? minDate.before(beginDate) : true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DatePeriod o) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int r =  ((from == null) ? 0 : from.hashCode());
		return 31 * r + ((to == null) ? 0 : to.hashCode());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DatePeriod)) return false;

		DatePeriod o = (DatePeriod) obj;

		return
		(from == o.from ? true : from != null ? from.equals(o.from) : false)
		&&
		(to == o.to ? true : to != null ? to.equals(o.to) : false);


	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String fromDate = from != null ? date_format.format(from) : "";
		String toDate = to != null ? date_format.format(to) : "";
		return "DatePeriod [" + (fromDate != null ? "from=" + fromDate + ", " : "...,")
				+ (toDate != null ? "to=" + toDate : "...") + "]";
	}


	/**
	 * Check bounds.
	 *
	 * @param from the from
	 * @param to the to
	 */
	private static void checkBounds(Date from, Date to) {
		if (from != null && to != null && from.after(to))
			throw new IllegalArgumentException("start/from date must be always before end/to date: ["+from+" < "+to+"]");
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		return new DatePeriod(from, to);
	}


	/**
	 * Gets the date with offset.
	 *
	 * @param date the date
	 * @param days the days
	 * @return the date with offset
	 */
	public static final Date getDateWithOffset(Date date, int days) {
		if (date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}

}

