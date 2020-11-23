/**
 * 
 */
package icy.common;

/**
 * Collapsible interface for collapsible event used by UpdateEventHandler.<br>
 * As we use HashMap to store these events, so we rely on Object.equals(..) and
 * Object.hashcode() implementation for these events.
 * 
 * @author Stephane
 */
public interface CollapsibleEvent
{
    /**
     * Collapse current object/event with specified one.
     * 
     * @return <code>false</code> if collapse operation failed (object are not 'equals')
     * @param event event
     */
    public boolean collapse(CollapsibleEvent event);

    /**
     * @return Returns <code>true</code> if the current event is equivalent to the specified one.<br>
     * We want event to override {@link Object#equals(Object)} method as we use an HashMap to store
     * these event
     * in the {@link UpdateEventHandler} class.
     * @param event event
     */
    public boolean equals(Object event);

    /**
     * @return Returns hash code for current event. It should respect the default {@link Object#hashCode()}
     * contract.
     */
    public int hashCode();
}
