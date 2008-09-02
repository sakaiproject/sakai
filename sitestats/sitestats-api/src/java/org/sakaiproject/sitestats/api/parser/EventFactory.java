package org.sakaiproject.sitestats.api.parser;

import org.sakaiproject.sitestats.api.event.EventInfo;


public interface EventFactory{

	public EventInfo createEvent(String eventId);
}
