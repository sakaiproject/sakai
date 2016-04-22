/********************************************************************************** 
 * $URL: $ 
 * $Id: SiteUpdateEventProcessor.java 313377 2012-06-22 20:18:27Z jimeng@umich.edu $ 
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
 * This is the event handler for site.unpublish event
 * it will remove links for all existing NewsItem/CalendarItem for all eligible users within the site 
 */
public class SiteUnpublishEventProcessor implements EventProcessor {

	private static Logger logger = LoggerFactory.getLogger(SiteUnpublishEventProcessor.class);
	
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

		return SiteService.EVENT_SITE_UNPUBLISH;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
		}
		
		// get site id
		String site_reference = event.getResource();
		if (site_reference != null && site_reference.contains(SiteService.REFERENCE_ROOT))
		{
			// get the substring of site id
			String context = site_reference.substring(SiteService.REFERENCE_ROOT.length() + 1);
		
			logger.info(this + " process Event start removing News links and Calendar links for site " + context);
			
			// if unpublished, remove all news links and calendar links
			dashboardLogic.modifyLinksByContext(context, dashboardLogic.TYPE_NEWS, false/*removing*/);
			dashboardLogic.modifyLinksByContext(context, dashboardLogic.TYPE_CALENDAR, false/*removing*/);
			
			logger.info(this + " process Event end removing News links and Calendar links for site " + context);
		}
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
