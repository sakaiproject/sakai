package org.sakaiproject.sitestats.api.event;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;

public class EventInfo implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private transient Log		LOG					= LogFactory.getLog(EventInfo.class);
	private String				eventId;
	private String				eventName;
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

	public String getEventName() {
		try{
			EventRegistryService M_ers = (EventRegistryService) ComponentManager.get(EventRegistryService.class);
			eventName = M_ers.getEventName(getEventId());
		}catch(RuntimeException e){
			eventName = getEventId().trim();
			LOG.info("No translation found for eventId: " + eventId.trim() + ". Please specify it in sitestats/sitestats-impl/impl/src/bundle/org/sakaiproject/sitestats/impl/bundle/");
		}
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
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
		buff.append("	-> EventInfo: "+getEventId()+" ("+getEventName()+") ["+isSelected()+"]\n");
		return buff.toString();
	}
	
}