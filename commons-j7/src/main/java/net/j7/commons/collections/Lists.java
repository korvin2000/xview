package net.j7.commons.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import net.j7.commons.base.NotNull;


/**
 * Factory class and common methods for java Lists classes
 * @author  tavrovsa
 */
public final class Lists {

	/**
	 * New list (not synchronized).
	 *
	 * @param <V> the value type
	 * @return the list
	 */
	public static final <V> List<V> newList() {
		return new ArrayList<V>();
	}


	  /**
    * New list (not synchronized).
    *
    * @param <V> the value type
    * @return the list
    */
   public static final <V> List<V> newList(int initialSize) {
      return new ArrayList<V>(initialSize);
   }


	/**
	 * New synchronized list.
	 *
	 * @param <V> the value type
	 * @return the list
	 */
	public static final <V> List<V> newSynchronizedList() {
		return new Vector<V>();
	}


	  /**
    * New synchronized list.
    *
    * @param <V> the value type
    * @return the list
    */
   public static final <V> List<V> newSynchronizedList(int initialSize) {
      return new Vector<V>(initialSize);
   }

	/**
	 * Returns Not null List (Null object Pattern)
	 *
	 * @param <V> the value type
	 * @param lst the lst
	 * @return the list
	 */
	public static <V> List<V> notNull(List<V> lst) {
		return NotNull.list(lst);
	}


	  /**
    * New concurrent list - A thread-safe variant of ArrayList in which all mutative
    * operations (add, set, and so on) are implemented by making a fresh copy of the
    * underlying array.
    *
    * @param <V> the value type
    * @return the sets the
    */
   public static final <V> List<V> newConcurrentList() {
      return new CopyOnWriteArrayList<V>();
   }


}
