package net.j7.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.j7.commons.base.NotNull;

/**
 * Factory class and common methods for java Collections classes
 * The Class Datasets for Collections
 * @author  tavrovsa
 */
public class Datasets {

	/**
	 * New collection (not synchronized).
	 *
	 * @param <V> the value type
	 * @return the collection
	 */
	public static final <V> Collection<V> newCollection() {
		return new ArrayList<V>();
	}

	/**
	 * New collection (not synchronized).
	 *
	 * @param <V> the value type
	 * @param size the size
	 * @return the collection
	 */
	public static final <V> Collection<V> newCollection(int size) {
		return new ArrayList<V>(size);
	}

	/**
	 * New synchronized collection.
	 *
	 * @param <V> the value type
	 * @return the collection
	 */
	public static final <V> Collection<V> newSynchronizedCollection() {
		return Collections.synchronizedList(new ArrayList<V>());
	}


	 /**
  	 * Returns Not null Collection (Null object Pattern).
  	 *
  	 * @param <V> the value type
  	 * @param lst the lst
  	 * @return the collection
  	 */
  	public static <V> Collection<V> notNull(Collection<V> lst) {
	      return NotNull.collection(lst);
	   }

}
