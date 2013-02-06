package org.sakaiproject.calendar.impl;

/**
 * This is a Mock ElementRefresher that means we can test the caches outside 
 * of a full sakai framework.
 *
 */
public class MockExternalCalendarElementRefresher implements ElementRefresher {

	public Object updateElement(Object key, Object value) {
		// Just return the current value.
		return value;
	}

}
