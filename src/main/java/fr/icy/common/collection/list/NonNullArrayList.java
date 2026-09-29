/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.icy.common.collection.list;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A customized implementation of {@link ArrayList} that does not allow storing
 * or processing null elements. Any attempt to add, set, or process null values
 * will result in a {@link NullPointerException}.
 *
 * @param <E> the type of elements maintained by this list
 *
 * @author Thomas Musset
 */
@ApiStatus.Experimental
public class NonNullArrayList<E> extends ArrayList<E> {
    /**
     * Constructs a new {@code NonNullArrayList} with the specified initial capacity.
     * This list does not allow null elements.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public NonNullArrayList(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs a new {@code NonNullArrayList} with the default initial capacity.
     * This list does not allow null elements.
     */
    public NonNullArrayList() {
        super();
    }

    /**
     * Checks if the specified object is present in the list.
     * This method overrides the {@code contains} method in {@link ArrayList}.
     *
     * @param o the object to be checked for presence in the list; can be {@code null}
     * @return {@code true} if the list contains the specified object, and it is non-null;
     *         {@code false} otherwise
     */
    @Contract(pure = true)
    @Override
    public boolean contains(final @Nullable Object o) {
        if (o == null)
            return false;
        return super.contains(o);
    }

    /**
     * Returns the index of the first occurrence of the specified object in this list,
     * or -1 if the object is not present. This method overrides the {@code indexOf}
     * method in {@link ArrayList}.
     *
     * @param o the object to search for in the list; may be {@code null}
     * @return the index of the first occurrence of the specified object in the list,
     *         or -1 if the object is not found or if {@code null} is passed
     */
    @Override
    public @Range(from = -1, to = Integer.MAX_VALUE) int indexOf(final @Nullable Object o) {
        if (o == null)
            return -1;
        return super.indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified object in this list,
     * or -1 if the object is not present. This method overrides the {@code lastIndexOf}
     * method in {@link ArrayList}.
     *
     * @param o the object to search for in the list; may be {@code null}
     * @return the index of the last occurrence of the specified object in the list,
     *         or -1 if the object is not found or if {@code null} is passed
     */
    @Override
    public @Range(from = -1, to = Integer.MAX_VALUE) int lastIndexOf(final @Nullable Object o) {
        if (o == null)
            return -1;
        return super.lastIndexOf(o);
    }

    /**
     * Returns an array containing all the elements in this list in a proper sequence
     * (from first to last element).
     *
     * @return a non-null array containing all the elements in this list;
     *         the runtime type of the returned array is {@code Object[]}
     */
    @Override
    public @NonNull Object @NonNull [] toArray() {
        return super.toArray();
    }

    /**
     * Converts the elements in this list to an array of the specified runtime type.
     * The length of the returned array is equal to the size of the list, and all elements
     * in the array are guaranteed to be non-null.
     *
     * @param <T> the component type of the array
     * @param a the array into which the elements of this list are to be stored if it is
     *          large enough; otherwise, a new array of the same runtime type is allocated
     *          for this purpose
     * @return an array containing all the elements in this list; the runtime type of the
     *         returned array is that of the specified array
     * @throws ArrayStoreException if the runtime type of the specified array is not a supertype
     *                             of the runtime type of every element in this list
     * @throws NullPointerException if the provided array is null
     */
    @Override
    public <T> @NonNull T @NonNull [] toArray(final @NonNull T @NonNull [] a) {
        return super.toArray(a);
    }

    /**
     * Retrieves the element at the specified position in this list.
     *
     * @param index the index of the element to be retrieved; must be within the
     *              range {@code 0} (inclusive) to {@code size() - 1} (inclusive)
     * @return the non-null element at the specified position in the list
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Contract(pure = true)
    @Override
    public @NonNull E get(final @Range(from = 0, to = Integer.MAX_VALUE) int index) throws IndexOutOfBoundsException {
        return super.get(index);
    }

    /*@Override
    public E getFirst() {
        return super.getFirst();
    }*/

    /*@Override
    public E getLast() {
        return super.getLast();
    }*/

    /**
     * Replaces the element at the specified position in this list with the specified element.
     * This method overrides the {@code set} method in {@link ArrayList}.
     *
     * @param index the index of the element to replace; must be within the range {@code 0}
     *              (inclusive) to {@code size() - 1} (inclusive)
     * @param element the non-null element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     * @throws NullPointerException if the specified element is {@code null}
     */
    @Override
    public @NonNull E set(final @Range(from = 0, to = Integer.MAX_VALUE) int index, final @NonNull E element) throws IndexOutOfBoundsException, NullPointerException {
        Objects.requireNonNull(element, "Cannot add null element.");
        return super.set(index, element);
    }

    /**
     * Adds the specified non-null element to the list. This method overrides the {@code add} method
     * in {@link ArrayList} and ensures that null elements cannot be added.
     *
     * @param e the non-null element to be added to the list
     * @return {@code true} if the list is modified as a result of this operation, {@code false} otherwise
     * @throws NullPointerException if the specified element is {@code null}
     */
    @Override
    public boolean add(final @NonNull E e) throws NullPointerException {
        Objects.requireNonNull(e, "Cannot add null element.");
        return super.add(e);
    }

    /**
     * Inserts the specified non-null element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent elements
     * to the right (adds one to their indices). This method overrides the {@code add}
     * method in {@link ArrayList} and ensures that null elements cannot be added.
     *
     * @param index the index at which the specified element is to be inserted; must be
     *              within the range {@code 0} (inclusive) to {@code size()} (inclusive)
     * @param element the non-null element to be inserted at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     * @throws NullPointerException if the specified element is {@code null}
     */
    @Override
    public void add(final @Range(from = 0, to = Integer.MAX_VALUE) int index, final @NonNull E element) throws IndexOutOfBoundsException, NullPointerException {
        Objects.requireNonNull(element, "Cannot add null element.");
        super.add(index, element);
    }

    /*@Override
    public void addFirst(E element) {
        super.addFirst(element);
    }*/

    /*@Override
    public void addLast(E element) {
        super.addLast(element);
    }*/

    /*@Override
    public E removeFirst() {
        return super.removeFirst();
    }*/

    /*@Override
    public E removeLast() {
        return super.removeLast();
    }*/

    /**
     * Removes the specified object from this list if it is present and non-null.
     * This method overrides the {@code remove} method in {@link ArrayList}.
     *
     * @param o the object to be removed from the list; can be {@code null}
     * @return {@code true} if the list is modified as a result of this operation, and
     *         the specified object is non-null and present; {@code false} otherwise
     */
    @Override
    public boolean remove(final @Nullable Object o) {
        if (o == null)
            return false;
        return super.remove(o);
    }

    /**
     * Adds all the elements in the specified collection to this collection.
     * The behavior of this operation is undefined if the specified collection
     * is modified while the operation is in progress.
     *
     * @param c the collection containing elements to be added to this collection
     * @return true if this collection changed as a result of the call
     * @throws NullPointerException if the specified collection contains one or more null elements
     */
    @Override
    public boolean addAll(final @NonNull Collection<? extends @NonNull E> c) throws NullPointerException {
        if (containsNull(c))
            throw new NullPointerException("Collection contains at least one null element.");
        else
            return super.addAll(c);
    }

    /**
     * Inserts all the elements in the specified collection into this list, starting
     * at the specified position. Shifts the elements currently at that position
     * (if any) and any subsequent elements to the right (increases their indices).
     * The new elements will appear in this list in the order that they are returned
     * by the specified collection's iterator.
     *
     * @param index the index at which to insert the first element from the specified
     *              collection
     * @param c the collection containing elements to be added to this list; must not
     *          contain null elements
     * @return true if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null or contains null elements
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    @Override
    public boolean addAll(final @Range(from = 0, to = Integer.MAX_VALUE) int index, final @NonNull Collection<? extends @NonNull E> c) throws NullPointerException, IndexOutOfBoundsException {
        if (containsNull(c))
            throw new NullPointerException("Collection contains at least one null element.");
        else
            return super.addAll(index, c);
    }

    /**
     * Removes from this collection all of its elements that are contained
     * in the specified collection.
     *
     * @param c the collection containing elements to be removed from this collection
     * @return {@code true} if this collection changed as a result of the call
     * @throws NullPointerException if the specified collection is null or contains null elements
     */
    @Override
    public boolean removeAll(final @NonNull Collection<?> c) throws NullPointerException {
        if (containsNull(c))
            throw new NullPointerException("Collection contains at least one null element.");
        else
            return super.removeAll(c);
    }

    /**
     * Retains only the elements in this collection that are contained in the specified
     * collection. In other words, removes from this collection all of its elements that
     * are not contained in the specified collection. This method will throw a
     * NullPointerException if the specified collection contains any null elements.
     *
     * @param c the collection containing elements to be retained in this collection
     * @return true if this collection changed as a result of the call
     * @throws NullPointerException if the specified collection is null or contains null elements
     */
    @Override
    public boolean retainAll(final @NonNull Collection<?> c) {
        if (containsNull(c))
            throw new NullPointerException("Collection contains at least one null element.");
        else
            return super.retainAll(c);
    }

    /**
     * Checks if the current collection contains all elements of the specified collection.
     *
     * @param c the collection whose elements are to be checked for containment in the current collection
     * @return true if the current collection contains all elements of the specified collection, false otherwise
     */
    @Override
    public boolean containsAll(@NotNull final Collection<?> c) {
        if (containsNull(c))
            return false;
        return super.containsAll(c);
    }

    /**
     * Checks if the given collection contains a null element.
     *
     * @param c the collection to check for null elements; must not be null
     * @return true if the collection contains at least one null element; false otherwise
     * @throws NullPointerException if the provided collection is null
     */
    @Contract(pure = true)
    protected final boolean containsNull(final @NonNull Collection<?> c) throws NullPointerException {
        Objects.requireNonNull(c, "Collection is null.");
        try {
            return c.contains(null);
        }
        catch (final NullPointerException e) {
            return false;
        }
    }
}
