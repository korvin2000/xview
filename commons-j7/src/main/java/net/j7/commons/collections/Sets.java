package net.j7.commons.collections;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import net.j7.commons.base.NotNull;

/**
 * Factory class and common methods for java Sets
 * The Class Sets.
 * @author  tavrovsa
 */
public final class Sets {

	/**
	 * New set (not synchronized).
	 *
	 * @param <V> the value type
	 * @return the sets the
	 */
	public static final <V> Set<V> newSet() {
		return new HashSet<V>();
	}

	/**
	 * New set of given size(not synchronized).
	 *
	 * @param <V> the value type
	 * @param size the size
	 * @return the sets the
	 */
	public static final <V> Set<V> newSet(int size) {
		return new HashSet<V>(size);
	}

	/**
	 * New synchronized set.
	 *
	 * @param <V> the value type
	 * @return the sets the
	 */
	public static final <V> Set<V> newSynchronizedSet() {
		return Collections.synchronizedSet(new HashSet<V>());
	}

	/**
	 * New synchronized set.
	 *
	 * @param <V> the value type
	 * @param size the size
	 * @return the sets the
	 */
	public static final <V> Set<V> newSynchronizedSet(int size) {
		return Collections.synchronizedSet(new HashSet<V>(size));
	}

	/**
	 * New sorted set.
	 *
	 * @param <V> the value type
	 * @return the sets the
	 */
	public static final <V> Set<V> newSortedSet() {
		return new TreeSet<V>();
	}

	/**
	 * New synchronized sorted set.
	 *
	 * @param <V> the value type
	 * @return the sets the
	 */
	public static final <V> Set<V> newSynchronizedSortedSet() {
		return Collections.synchronizedSet(new TreeSet<V>());
	}

	/**
	 * New concurrent set.
	 *
	 * @param <V> the value type
	 * @return the sets the
	 */
	public static final <V> Set<V> newConcurrentSet() {
		return new CopyOnWriteArraySet<V>();
	}

	/**
	 * Returns Not null Set (Null object Pattern)
	 *
	 * @param <V> the value type
	 * @param lst the lst
	 * @return the sets the
	 */
	public static final <V> Set<V> notNull(Set<V> lst) {
	      return NotNull.set(lst);
	}

//	public static final <V> Set<V> newIdentitySet() {
//		return new IdentityHashSet<V>();
//	}
}
