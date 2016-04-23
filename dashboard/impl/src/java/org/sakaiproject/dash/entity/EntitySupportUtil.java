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

package org.sakaiproject.dash.entity;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.util.ResourceLoader;

/**
 * the util class to be used by various EntitySupport classes
 *
 */
public class EntitySupportUtil{
	
	private Logger logger = LoggerFactory.getLogger(EntitySupportUtil.class);
	
	static ResourceLoader rl = new ResourceLoader("dash_entity");
	
	protected static DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	public void init() {
		logger.info("init()");
	}
	
	/**
	 * update NewsItem time and title related to an event
	 * @param event
	 */
	public static void updateNewsItemTimeTitle(Event event) {
		NewsItem nItem = dashboardLogic.getNewsItem(event.getResource());
		if (nItem != null)
		{
			Date newTime = event.getEventTime();
			if ((newTime.getTime() - nItem.getNewsTime().getTime()) > 1000)
			{
				// set values on the item to trigger calculation of new grouping identifier
				String newLabelKey = "dash.updated";
				nItem.setNewsTime(newTime);
				nItem.setNewsTimeLabelKey(newLabelKey);
				// if this is not an update within object creation
				dashboardLogic.reviseNewsItemTitle(event.getResource(), nItem.getTitle(), newTime, newLabelKey, nItem.getGroupingIdentifier());
			}
		}
	}
	
}
