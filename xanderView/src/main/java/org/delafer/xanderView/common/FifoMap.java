package org.delafer.xanderView.common;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class FifoMap<K,V> extends LinkedHashMap<K,V> {
	private final int capacity;

	public FifoMap(int capacity) {
		super(capacity, 0.75f, false);
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
		boolean remove = size() > capacity;
		if (remove) dispose(eldest.getKey(), eldest.getValue());
		return remove;
	}

	public abstract void dispose(K key, V value);
}
