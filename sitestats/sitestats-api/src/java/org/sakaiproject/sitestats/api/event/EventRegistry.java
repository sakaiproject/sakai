package org.sakaiproject.sitestats.api.event;

import java.util.List;


public interface EventRegistry {

	/**
	 * Get the statisticable event registry.
	 * @return A list of all tool events definition.
	 * @see org.sakaiproject.sitestats.api.ToolInfo
	 */
	public List<ToolInfo> getEventRegistry();
	
	/** Check whether EventRegistry has expired. This means that getEventRegistry() will be called again, when needed. */
	public boolean isEventRegistryExpired();
	
	/** Get the event name (localized) for the specified event id. */
	public String getEventName(String eventId);
}
