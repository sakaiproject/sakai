package org.sakaiproject.sitestats.impl.event;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;


public class EventInfoImpl implements EventInfo, Serializable {
	private static final long	serialVersionUID	= 1L;
	private Log					LOG					= LogFactory.getLog(EventInfoImpl.class);
	private String				eventId;
	private String				eventName;
	private boolean				selected;
	private boolean				anonymous;
	
	public EventInfoImpl(String eventId) {
		this.eventId = eventId.trim();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#getEventId()
	 */
	public String getEventId() {
		return eventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#setEventId(java.lang.String)
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId.trim();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#getEventName()
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#setEventName(java.lang.String)
	 */
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#isSelected()
	 */
	public boolean isSelected() {
		return selected;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#setSelected(boolean)
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}	

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#isAnonymous()
	 */
	public boolean isAnonymous() {
		return anonymous;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#setAnonymous(boolean)
	 */
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
