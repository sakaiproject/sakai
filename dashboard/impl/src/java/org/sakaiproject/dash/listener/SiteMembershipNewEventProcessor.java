/********************************************************************************** 
 * $URL: $ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.SiteService;

/**
 * 
 * THIS MAY BE MOVED TO THE site-manage PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 */
public class SiteMembershipNewEventProcessor implements EventProcessor {

	private static Logger logger = LoggerFactory.getLogger(SiteMembershipNewEventProcessor.class);
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
	 */
	public String getEventIdentifer() {

		// please refer to SakaiProxy instead of SiteService here
		return SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
		}
		String context = event.getContext();
		// here is the format of resource string. We need to parse it to get uid, role, provide, group id separately
		//uid=8f6f32b0-2a25-4661-9be8-6cda2b479e14;role=access;active=true;provided=false
		String uid = null;
		String role = null;
		String active = null;
		String provided = null;
		String resource = event.getResource();
		String parts[] = resource.split(";");
		if( parts != null) {
			for(String pair : parts) {
				String entries[] = pair.split("=");
				if (entries != null)
				{
					if ("uid".equals(entries[0]))
					{
						uid = entries[1];
					}
					else if ("role".equals(entries[0]))
					{
						role = entries[1];
					}
					else if ("active".equals(entries[0]))
					{
						active = entries[1];
					}
					else if ("provided".equals(entries[0]))
					{
						provided = entries[1];
					}
				}
			}
		}
		
		// now we have user id and active status for the context. This method will then call a 
		// method in DashboardLogic to add links for this user to dashboard items related
		// to this site. The implementation of the new DashboardLogic method will iterate
		// through all CalendarItems and NewsItems related to that site, and add a link if 
		// the user has that permission. 
		// Is that right? 
		if (uid != null && active != null && active.equals("true"))
		{
			dashboardLogic.addCalendarLinks(uid, context);
			dashboardLogic.addNewsLinks(uid, context);
		}
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
