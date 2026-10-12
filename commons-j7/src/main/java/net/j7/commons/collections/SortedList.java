package net.j7.commons.collections;

import java.util.*;
/**
 * This class is a List implementation which sorts the elements using the
 * comparator specified when constructing a new instance.
 *
 * @param <E>
 */
public class SortedList<E> extends LinkedList<E> {
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Comparator used to sort the list.
     */
    private Comparator<? super E> comparator = null;
    /**
     * Construct a new instance with the list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     */
    private SortedList() {
    }

    /**
     * Construct a new instance using the given comparator.
     *
     * @param comparator
     */
    public SortedList(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    /**
     * Construct a new instance using the given comparator.
     *
     * @param comparator
     */
    public SortedList(Collection<? extends E> paramCollection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        addAll(paramCollection);
    }
    /**
     * Add a new entry to the list. The insertion point is calculated using the
     * comparator.
     *
     * @param element
     */
    @Override
    public boolean add(E element) {
        int insertionPoint = Collections.binarySearch(this, element, comparator);
        super.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, element);
        return true;
    }
    /**
     * Adds all elements in the specified collection to the list. Each element
     * will be inserted at the correct position to keep the list sorted.
     *
     * @param paramCollection
     */
    @Override
    public boolean addAll(Collection<? extends E> paramCollection) {
        boolean result = false;
        if (paramCollection.size() > 4) {
            result = super.addAll(paramCollection);
            Collections.sort(this, comparator);
        }
        else {
            for (E paramT:paramCollection) {
                result |= add(paramT);
            }
        }
        return result;
    }
    /**
     * Check, if this list contains the given Element. This is faster than the
     * {@link #contains(Object)} method, since it is based on binary search.
     *
     * @param paramT
     * @return <code>true</code>, if the element is contained in this list;
     * <code>false</code>, otherwise.
     */
    public boolean containsElement(E paramT) {
        return (Collections.binarySearch(this, paramT, comparator) > -1);
    }



}