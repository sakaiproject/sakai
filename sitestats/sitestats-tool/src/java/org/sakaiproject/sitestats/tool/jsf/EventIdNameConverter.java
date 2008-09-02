package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;


public class EventIdNameConverter extends CharacterConverter {
	/** Statistics Manager object */
	private EventRegistryService	ers	= (EventRegistryService) ComponentManager.get(EventRegistryService.class.getName());

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String eventName = null;
		if(value == null || value.equals("")){
			eventName = "";
		}else{
			if(value instanceof String)
				eventName = ers.getEventName((String) value);
			eventName = super.getAsString(context, component, (Object) eventName);
		}

		return eventName;
	}

}
