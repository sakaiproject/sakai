/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;


public class EventModel implements IModel {
	private static final long	serialVersionUID	= 1L;
	private String				eventId				= "";
	private String				eventName			= "";

	public EventModel(String eventId, String eventName) {
		this.eventId = eventId;
		this.eventName = eventName;
	}

	public EventModel(EventInfo e) {
		this.eventId = e.getEventId();
		this.eventName = e.getEventName();
	}

	public Object getObject() {
		return getEventId() + " + " + getEventName();
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
		if(ReportManager.WHAT_EVENTS_ALLEVENTS.equals(eventName)){
			return (String) new ResourceModel("all").getObject();
		}else{
			return eventName;
		}
	}

	public void detach() {
		eventId = null;
		eventName = null;
	}

}