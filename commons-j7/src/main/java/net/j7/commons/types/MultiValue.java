package net.j7.commons.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultiValue<T> implements Serializable {

	private static final long serialVersionUID = 1855219667148309393L;

	private T svalue;
	private List<T> mvalue;


	public MultiValue() {
	}

	public MultiValue<T> addValue(T value) {
		if (null == svalue) {
			svalue = value;
		} else {
			if (null == mvalue) mvalue = new ArrayList<T>(4);
			mvalue.add(value);
		}
		return this;
	}


	@Override
	public String toString() {
		T value = getValue();
		return value != null ? value.toString() : null;
	}

	public T getValue() {
		return svalue;
	}

	public T getValue(int at) {
		return at <= 0 ? svalue : mvalue != null && at <= mvalue.size() ? mvalue.get(--at) : null;
	}

	public int size() {
		return mvalue != null ? mvalue.size()+1 : svalue == null ? 0 : 1;
	}

}
