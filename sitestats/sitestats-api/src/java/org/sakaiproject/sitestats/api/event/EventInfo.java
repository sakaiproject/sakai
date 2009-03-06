package org.sakaiproject.sitestats.api.event;

import java.io.Serializable;

public class EventInfo implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private String				eventId;
	private boolean				selected;
	private boolean				anonymous;

	public EventInfo(String eventId) {
		this.eventId = eventId.trim();
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId.trim();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}	

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null || !(arg0 instanceof EventInfo))
			return false;
		else {
			EventInfo other = (EventInfo) arg0;
			return getEventId().equals(other.getEventId());
		}
	}
	
	@Override
	public int hashCode() {
		return getEventId().hashCode();
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("	-> EventInfo: "+getEventId()+" ["+isSelected()+"]\n");
		return buff.toString();
	}
	
}