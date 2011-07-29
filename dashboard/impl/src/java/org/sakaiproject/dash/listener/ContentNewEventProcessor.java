/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.listener;

import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.event.api.Event;

/**
 * 
 *
 */
public class ContentNewEventProcessor implements EventProcessor {
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
	 */
	@Override
	public String getEventIdentifer() {
		return SakaiProxy.EVENT_CONTENT_NEW;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	@Override
	public void processEvent(Event event) {
		NewsItem newsItem = dashboardLogic.createNewsItem(event.getResource(), event.getEventTime(), event.getContext());
		dashboardLogic.createNewsLinks(newsItem);
	}
	
	public void init() {
		this.dashboardLogic.registerEventProcessor(this);
	}

}
