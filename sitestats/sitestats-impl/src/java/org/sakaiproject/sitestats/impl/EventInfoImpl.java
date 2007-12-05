package org.sakaiproject.sitestats.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitestats.api.EventInfo;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;


public class EventInfoImpl implements EventInfo {
	private String			bundleName	= "org.sakaiproject.sitestats.impl.bundle.Messages";
	private ResourceLoader	msgs		= new ResourceLoader(bundleName);
	private Log				LOG			= LogFactory.getLog(EventInfoImpl.class);
	private String			eventId;
	private String			eventName;
	private boolean			selected;
	
	public EventInfoImpl(String eventId) {
		this.eventId = eventId;
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
		this.eventId = eventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventInfo#getEventName()
	 */
	public String getEventName() {
		try{
			eventName = msgs.getString(getEventId().trim());
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
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null || !(arg0 instanceof EventInfo))
			return false;
		else {
			EventInfo other = (EventInfo) arg0;
			return getEventId().equals(other.getEventId());
		}
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("	-> EventInfo: "+getEventId()+" ("+getEventName()+") ["+isSelected()+"]\n");
		return buff.toString();
	}
	
}
