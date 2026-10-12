package net.j7.commons.collections;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EmptyStackException;
@SuppressWarnings({"unchecked", "rawtypes"})
public final class FastStack<T> implements Serializable, Cloneable {
	private static final long serialVersionUID = -5734444108109419771L;

	private T[] contents;

	  private int size;

	  private int initialSize;

	  /**
	   * Creates a new empty stack.
	   */
	  public FastStack() {
	    this.initialSize = 6;
	  }

	  /**
	   * Creates a new empty stack with the specified initial storage size.
	   *
	   * @param size
	   *          the initial storage elements.
	   */
	  public FastStack(int size) {
	    this.initialSize = Math.max(1, size);
	  }

	  /**
	   * Returns <code>true</code> if the stack is empty, and <code>false</code>
	   * otherwise.
	   *
	   * @return A boolean.
	   */
	  public boolean isEmpty() {
	    return this.size == 0;
	  }

	  /**
	   * Returns the number of elements in the stack.
	   *
	   * @return The element count.
	   */
	  public int size() {
	    return this.size;
	  }

	  /**
	   * Pushes an object onto the stack.
	   *
	   * @param o
	   *          the object.
	   */
	  public void push(T o) {
	    if (this.contents == null) {
	      this.contents = (T[])new Object[this.initialSize];
	      this.contents[0] = o;
	      this.size = 1;
	      return;
	    }

	    final int oldSize = this.size;
	    this.size += 1;
	    if (this.contents.length == this.size) {
	      // grow ..
	      final T[] newContents =(T[]) new Object[this.size + this.initialSize];
	      System.arraycopy(this.contents, 0, newContents, 0, this.size);
	      this.contents = newContents;
	    }
	    this.contents[oldSize] = o;
	  }

	  /**
	   * Returns the object at the top of the stack without removing it.
	   *
	   * @return The object at the top of the stack.
	   */
	  public T peek() {
	    if (this.size == 0) {
	      throw new EmptyStackException();
	    }
	    return this.contents[this.size - 1];
	  }

	  /**
	   * Removes and returns the object from the top of the stack.
	   *
	   * @return The object.
	   */
	  public T pop() {
	    if (this.size == 0) {
	      throw new EmptyStackException();
	    }
	    this.size -= 1;
	    final T retval = this.contents[this.size];
	    this.contents[this.size] = null;
	    return retval;
	  }

	  /**
	   * Returns a clone of the stack.
	   *
	   * @return A clone.
	   */
	  public Object clone() {
	    try {
	      FastStack stack = (FastStack) super.clone();
	      if (this.contents != null) {
	        stack.contents = (Object[]) this.contents.clone();
	      }
	      return stack;
	    } catch (CloneNotSupportedException cne) {
	      throw new IllegalStateException("Clone not supported? Why?");
	    }
	  }

	  /**
	   * Clears the stack.
	   */
	  public void clear() {
	    this.size = 0;
	    if (this.contents != null) {
	      Arrays.fill(this.contents, null);
	    }
	  }

	  /**
	   * Returns the item at the specified slot in the stack.
	   *
	   * @param index
	   *          the index.
	   *
	   * @return The item.
	   */
	  public Object get(final int index) {
	    if (index >= this.size) {
	      throw new IndexOutOfBoundsException();
	    }
	    return this.contents[index];
	  }
	}
