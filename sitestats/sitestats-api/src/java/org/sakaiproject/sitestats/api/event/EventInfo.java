package org.sakaiproject.sitestats.api.event;

public interface EventInfo {

	public String getEventId();

	public void setEventId(String eventId);

	public String getEventName();

	public void setEventName(String eventName);

	public boolean isSelected();

	public void setSelected(boolean selected);
	
	public boolean isAnonymous();

	public void setAnonymous(boolean anonymous);
}