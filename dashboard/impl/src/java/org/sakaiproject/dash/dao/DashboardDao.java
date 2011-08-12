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

package org.sakaiproject.dash.dao;

import java.util.List;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.dash.model.Thing;

/**
 * DAO interface for our project
 * 
 *
 */
public interface DashboardDao {

	/**
	 * Gets a single Thing from the db
	 * 
	 * @return an item or null if no result
	 */
	public Thing getThing(long id);
	
	/**
	 * Get all Things
	 * @return a list of items, an empty list if no items
	 */
	public List<Thing> getThings();
		
	/**
	 * Add a new Thing record to the database. Only the name property is actually used.
	 * @param t	Thing
	 * @return	true if success, false if not
	 */
	public boolean addThing(Thing t);
	
	
	public boolean addCalendarItem(CalendarItem calendarItem);
	
	public boolean addCalendarLink(CalendarLink calendarLink);
	
	public boolean addContext(Context context);
	
	public boolean addNewsItem(NewsItem newsItem);
	
	public boolean addNewsLink(NewsLink newsLink);
	
	public boolean addPerson(Person person);
	
	public boolean addRealm(Realm realm);
	
	public boolean addSourceType(SourceType identifier);
	
	public SourceType getSourceType(String identifier);

	public CalendarItem getCalendarItem(String entityReference);

	public Context getContext(long id);
	
	public Context getContext(String contextId);
	
	public Person getPersonBySakaiId(String sakaiId);

	public Realm getRealm(long id);
	
	public Realm getRealm(String realmId);




	
}
