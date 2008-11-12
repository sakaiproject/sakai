/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.sakaiproject.sitestats.api.event.EventInfo;


public class EventModel implements IModel {
	private static final long	serialVersionUID	= 1L;
	String						eventId				= "";
	String						eventName			= "";

	public EventModel(EventInfo e) {
		eventId = e.getEventId();
		eventName = e.getEventName();
	}

	public Object getObject() {
		return eventId + " + " + eventName;
	}

	public void setObject(Object object) {
		if(object instanceof String){
			String[] str = ((String) object).split(" \\+ ");
			eventId = str[0];
			eventName = str[1];
		}
	}

	public String getEventId() {
		return eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void detach() {
		eventId = null;
		eventName = null;
	}

}