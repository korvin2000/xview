package net.j7.commons.types;

import java.io.Serializable;

import net.j7.commons.base.Equals;

public class DoubleValue<K,V> implements Serializable {

	private static final long serialVersionUID = 3210145873853860486L;

	private K one;
	private V two;

	public DoubleValue(K one, V two) {
		this.one = one;
		this.two = two;
	}

	public DoubleValue() {
	}

	public K getOne() {
		return one;
	}

	public void setOne(K one) {
		this.one = one;
	}

	public V getTwo() {
		return two;
	}

	public void setTwo(V two) {
		this.two = two;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = ((one == null) ? 0 : one.hashCode());
		result = prime * result + ((two == null) ? 0 : two.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DoubleValue)) return false;
		DoubleValue<?,?> other = (DoubleValue<?,?>) obj;
		if (!Equals.equal(one, other.one)) return false;
		if (!Equals.equal(two, other.two)) return false;

		return true;
	}

	@Override
	public String toString() {
		return String.format("DoubleValue [%s, %s]", one, two);
	}

}
