package org.sakaiproject.event.api;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.LearningResourceStoreService.EventWrapper;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;

public class XAPIFactory{
	public LRS_Verb getEventVerb(Event event, Map<String, EventWrapper> xAPIEvent) {
        LRS_Verb verb = null;
        if (event != null) {
        	String e = StringUtils.lowerCase(event.getEvent());
            if (xAPIEvent.containsKey(e)) {
                verb = new LRS_Verb(xAPIEvent.get(e).getVerb());
            }
        }
        return verb;
	}
	
	public LRS_Object getEventObject(Event event, Map<String, EventWrapper> xAPIEvent, String url) {
        LRS_Object object = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            /*
             * NOTE: use the following terms "view", "add", "edit", "delete"
             */
            
            if (xAPIEvent.containsKey(e)) {
            	object = new LRS_Object (/*serverConfigurationService.getPortalUrl()*/url + event.getResource(), xAPIEvent.get(e).getObject());
            }
        }
        return object;
	}
	
	public String getEventOrigin(Event event, Map<String, EventWrapper> xAPIEvent) {
        String origin = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            
            if (xAPIEvent.containsKey(e)) {
            	origin = xAPIEvent.get(e).getOrigin();
            } else {
                origin = e;
            }
        }
        return origin;
	}
}