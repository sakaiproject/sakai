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

package org.sakaiproject.dash.logic;

import java.util.Date;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.NewsItem;

/**
 * DashboardLogic
 *
 */
public interface DashboardLogic {

	public CalendarItem createCalendarItem(String entityReference, String context);

	public void createCalendarLinks(CalendarItem calendarItem);

	public NewsItem createNewsItem(String entityReference, Date newsTime, String context);

	public void createNewsLinks(NewsItem newsItem);

	public void registerEventProcessor(EventProcessor eventProcessor);
	
}
