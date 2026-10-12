package net.j7.commons.types;

import java.io.Serializable;

public class CompositeValue<K,V> implements Serializable {

	private static final long serialVersionUID = -8111676726840981251L;

	K key;
	V value;

	public CompositeValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

}
