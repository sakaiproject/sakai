/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;


public class EventModel implements IModel {
	private static final long	serialVersionUID	= 1L;
	private String				eventId				= "";
	private String				eventName			= "";
	
	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	public EventModel(String eventId, String eventName) {
		this.eventId = eventId;
		this.eventName = eventName;
	}

	public EventModel(EventInfo e) {
		this.eventId = e.getEventId();
		this.eventName = getFacade().getEventRegistryService().getEventName(this.eventId);
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
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}

}