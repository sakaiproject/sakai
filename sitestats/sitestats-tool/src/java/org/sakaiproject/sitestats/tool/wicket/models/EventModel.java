/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;


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
		this.eventName = Locator.getFacade().getEventRegistryService().getEventName(this.eventId);
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