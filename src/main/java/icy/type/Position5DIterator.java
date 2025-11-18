/**
 * 
 */
package icy.type;

import java.util.NoSuchElementException;

import icy.type.point.Point5D;

/**
 * Position 5D iterator.
 * 
 * @author Stephane
 */
public interface Position5DIterator
{
    /**
     * Reset iterator to initial position.
     */
    public void reset() throws InterruptedException;

    /**
     * Pass to the next element.
     * 
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public void next() throws NoSuchElementException, InterruptedException;

    /**
     * Returns <i>true</i> if the iterator has no more elements.
     */
    public boolean done();

    /**
     * @return the current position of the iterator
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public Point5D get() throws NoSuchElementException;

    /**
     * @return the current position X of the iterator
     */
    public int getX() throws NoSuchElementException;

    /**
     * @return the current position Y of the iterator
     */
    public int getY() throws NoSuchElementException;;

    /**
     * @return the current position Z of the iterator
     */
    public int getZ() throws NoSuchElementException;;

    /**
     * @return the current position T of the iterator
     */
    public int getT() throws NoSuchElementException;;

    /**
     * @return the current position C of the iterator
     */
    public int getC() throws NoSuchElementException;;
}
