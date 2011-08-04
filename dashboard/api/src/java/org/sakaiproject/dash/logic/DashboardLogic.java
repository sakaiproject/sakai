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
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.NewsItem;

/**
 * DashboardLogic
 *
 */
public interface DashboardLogic {

	public CalendarItem createCalendarItem(String title, Date calendarTime, String entityReference, String entityUrl, Context context, Realm realm, SourceType sourceType);

	public void createCalendarLinks(CalendarItem calendarItem);

	public NewsItem createNewsItem(String title, Date newsTime, String entityReference, String entityUrl, Context context, Realm realm, SourceType sourceType);
	
	public void createNewsLinks(NewsItem newsItem);

	public void registerEventProcessor(EventProcessor eventProcessor);

	public Context getContext(String contextId);

	public Context createContext(String contextId);

	public Realm getRealm(String contextId);

	public Realm createRealm(String entityReference, String contextId);

	public SourceType getSourceType(String source_type);

	public SourceType createSourceType(String source_type);
	
	// todo:
	// add methods to revise news items, calendar items, news links, calendar links, etc.
	// add methods to delete news items, calendar items, news links, calendar links, etc.
	
	
}
