package org.sakaiproject.calendar.impl;


/**
 * This allows elements in the cache to be updated.
 * @author buckett
 *
 */
public interface ElementRefresher {

	/**
	 * Updates an object in the cache.
	 * @param element The element that is a little old.
	 * @return The updated object for the cache, if null is returned the element is removed from the cache.
	 */
	public Object updateElement(Object key, Object value);

}
