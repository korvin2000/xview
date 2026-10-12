/**
 *
 */
package net.j7.commons.types;

import java.util.Date;

/**
 * jLead immutable date
 * @author u tavrovsa
 *
 */
public final class ImmutableDate extends Date {

	private static final long serialVersionUID = 1L;

	private static final RuntimeException RE = new RuntimeException("Unsupported operation for ImmutableDate class");
	/**
	 * empty constructor
	 */
	public ImmutableDate() {
		super();
	}

	/**
	 * first constructor
	 * @param date
	 */
	public ImmutableDate(long date) {
		super(date);
	}

	/**
	 * second constructor
	 * @param date
	 */
	public ImmutableDate(Date date) {
		this(date != null ? date.getTime() : 0l);
	}

	/* (non-Javadoc)
	 * @see java.util.Date#clone()
	 */
	@Override
	public Object clone() {
		return new ImmutableDate(this.getTime());
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setDate(int)
	 */
	@Override
	public void setDate(int date) {
		throw RE;
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setHours(int)
	 */
	@Override
	public void setHours(int hours) {
		throw RE;
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setMinutes(int)
	 */
	@Override
	public void setMinutes(int minutes) {
		throw RE;
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setMonth(int)
	 */
	@Override
	public void setMonth(int month) {
		throw RE;
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setSeconds(int)
	 */
	@Override
	public void setSeconds(int seconds) {
		throw RE;
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setTime(long)
	 */
	@Override
	public void setTime(long time) {
		throw RE;
	}

	/* (non-Javadoc)
	 * @see java.util.Date#setYear(int)
	 */
	@Override
	public void setYear(int year) {
		throw RE;
	}

}
