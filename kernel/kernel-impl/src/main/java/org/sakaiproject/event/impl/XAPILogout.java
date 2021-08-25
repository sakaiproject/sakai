package org.sakaiproject.event.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService.EventWrapper;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.XAPIFactory;

public class XAPILogout extends XAPIFactory{

	@Override
	public LRS_Object getEventObject(Event event, Map<String, EventWrapper> xAPIEvent, String url) {
        LRS_Object object = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            /*
             * NOTE: use the following terms "view", "add", "edit", "delete"
             */
            
            if (xAPIEvent.containsKey(e)) {
            	object = new LRS_Object (/*serverConfigurationService.getPortalUrl()*/url + "/logout", xAPIEvent.get(e).getObject());
            }
        }
        return object;
	}
}