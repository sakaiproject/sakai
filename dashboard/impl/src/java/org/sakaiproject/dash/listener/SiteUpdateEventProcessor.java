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

import org.apache.log4j.Logger;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.SiteService;

/**
 * This is the event handler for site.upd event
 * if the site becomes unpublished, it will remove all news/calendar links associated with the site
 * if the site becomes published, it will add links for all existing NewsItem/CalendarItem for all eligible users within the site 
 */
public class SiteUpdateEventProcessor implements EventProcessor {

	private static Logger logger = Logger.getLogger(SiteUpdateEventProcessor.class);
	
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

		return SiteService.SECURE_UPDATE_SITE;
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
		String context = event.getContext();
		
		// check whether the site is publish or not
		if (!sakaiProxy.isSitePublished(context))
		{
			logger.info(this + " process Event start removing News links and Calendar links for site " + context);
			
			// if not published, remove all news links and calendar links
			dashboardLogic.removeNewsLinksByContext(context);
			dashboardLogic.removeCalendarLinksByContext(context);

			logger.info(this + " process Event end removing News links and Calendar links for site " + context);
		}
		else
		{
			logger.info(this + " process Event start adding News links and Calendar links for site " + context);
			
			// if published, add all news links and calendar links
			dashboardLogic.addNewsLinksByContext(context);
			dashboardLogic.addCalendarLinksByContext(context);

			logger.info(this + " process Event end adding News links and Calendar links for site " + context);
			
		}
		
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
