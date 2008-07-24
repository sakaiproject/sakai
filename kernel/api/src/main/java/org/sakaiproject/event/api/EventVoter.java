package org.sakaiproject.event.api;

/**
 * Event voters can decide whether an event should be allowed to propagated by the system. If a
 * voter votes against an event (returns false), the event is discarded by the system. If an event
 * is to be used in a different way (delayed, etc), it is up to the voter to handle that.
 */
public interface EventVoter
{
	/**
	 * Notify method to allow a voter to chime in on the propagation of an event.
	 * 
	 * @param event
	 * @return
	 */
	boolean vote(Event event);
}
