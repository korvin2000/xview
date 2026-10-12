package net.j7.commons.collections;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.j7.commons.base.NotNull;

/**
 * The Class Maps.
 *  Factory class and common methods for Map classes
 * @author  tavrovsa
 */
public final class Maps {

	/**
	 * New map (not synchronized).
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the map
	 */
	public static final <K, V> Map<K, V> newMap() {
		return new HashMap<K, V>();
	}

	/**
	 * New map (not synchronized).
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param size the size
	 * @return the map
	 */
	public static final <K, V> Map<K, V> newMap(int size) {
		return new HashMap<K, V>(size);
	}

	/**
	 * New synchronized map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the map
	 */
	public static final <K, V> Map<K, V> newSynchronizedMap() {
		return new Hashtable<K, V>();
	}

	  /**
    * New synchronized map.
    *
    * @param <K> the key type
    * @param <V> the value type
    * @return the map
    */
   public static final <K, V> Map<K, V> newSynchronizedMap(int initialSize) {
      return new Hashtable<K, V>(initialSize);
   }

	/**
	 * New concurrent map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the concurrent map
	 */
	public static final <K, V> ConcurrentMap<K, V> newConcurrentMap() {
		return new ConcurrentHashMap<K, V>();
	}


	 /**
    * Returns Not null Map (Null object Pattern)
  	 *
  	 * @param <K> the key type
  	 * @param <V> the value type
  	 * @param map the map
  	 * @return the map
  	 */
  	public static final <K, V> Map<K, V>  notNull(Map<K, V> map) {
	      return NotNull.map(map);
	   }

}
