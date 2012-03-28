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
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;

/**
 * 
 *
 */
public class DashboardDaoMock implements DashboardDao {
	
	protected AtomicLong idSequence = new AtomicLong(0L);
	
	protected Map<String,Context> contextId2contextMap = new HashMap<String,Context>(); 
	protected Map<Long,Context> id2contextMap = new HashMap<Long,Context>();

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addAvailabilityCheck(org.sakaiproject.dash.model.AvailabilityCheck)
	 */
	public boolean addAvailabilityCheck(AvailabilityCheck availabilityCheck) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addCalendarItem(org.sakaiproject.dash.model.CalendarItem)
	 */
	public boolean addCalendarItem(CalendarItem calendarItem) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addCalendarLink(org.sakaiproject.dash.model.CalendarLink)
	 */
	public boolean addCalendarLink(CalendarLink calendarLink) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addContext(org.sakaiproject.dash.model.Context)
	 */
	public boolean addContext(Context context) {
		boolean success = false;
		context.setId(this.idSequence.incrementAndGet());
		contextId2contextMap.put(context.getContextId(), context);
		Context c1 = contextId2contextMap.get(context.getContextId());
		if(c1 != null) {
			id2contextMap.put(c1.getId(), c1);
			Context c2 = id2contextMap.get(c1.getId());
			if(c2 != null) {
				success = true;
			}
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addNewsItem(org.sakaiproject.dash.model.NewsItem)
	 */
	public boolean addNewsItem(NewsItem newsItem) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addNewsLink(org.sakaiproject.dash.model.NewsLink)
	 */
	public boolean addNewsLink(NewsLink newsLink) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addPerson(org.sakaiproject.dash.model.Person)
	 */
	public boolean addPerson(Person person) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addRepeatingCalendarItem(org.sakaiproject.dash.model.RepeatingCalendarItem)
	 */
	public boolean addRepeatingCalendarItem(
			RepeatingCalendarItem repeatingCalendarItem) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addSourceType(org.sakaiproject.dash.model.SourceType)
	 */
	public boolean addSourceType(SourceType sourceType) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSourceType(long)
	 */
	public SourceType getSourceType(long sourceTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSourceType(java.lang.String)
	 */
	public SourceType getSourceType(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItem(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public CalendarItem getCalendarItem(String entityReference,
			String calendarTimeLabelKey, Integer sequenceNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItems(java.lang.String)
	 */
	public List<CalendarItem> getCalendarItems(String entityReference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItems(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public List<CalendarItem> getCalendarItems(String sakaiUserId,
			String contextId, boolean saved, boolean hidden) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getFutureCalendarItems(java.lang.String, java.lang.String)
	 */
	public List<CalendarItem> getFutureCalendarItems(String sakaiUserId,
			String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getPastCalendarItems(java.lang.String, java.lang.String)
	 */
	public List<CalendarItem> getPastCalendarItems(String sakaiUserId,
			String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItemsByContext(java.lang.String)
	 */
	public List<CalendarItem> getCalendarItemsByContext(String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarLink(long)
	 */
	public CalendarLink getCalendarLink(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getFutureCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId,
			String contextId, boolean hidden) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getPastCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId,
			String contextId, boolean hidden) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getStarredCalendarLinks(java.lang.String, java.lang.String)
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId,
			String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getContext(long)
	 */
	public Context getContext(long id) {
		
		return this.id2contextMap.get(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getContext(java.lang.String)
	 */
	public Context getContext(String contextId) {
		
		return this.contextId2contextMap.get(contextId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getMOTD(java.lang.String)
	 */
	public List<NewsItem> getMOTD(String motdContextId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getPersonBySakaiId(java.lang.String)
	 */
	public Person getPersonBySakaiId(String sakaiId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRealm(long)
	 */
	public Realm getRealm(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItem(java.lang.String)
	 */
	public NewsItem getNewsItem(String entityReference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItems(java.lang.String, java.lang.String, int)
	 */
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId,
			int collapseCount) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItems(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId,
			boolean saved, boolean hidden) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItemsByContext(java.lang.String)
	 */
	public List<NewsItem> getNewsItemsByContext(String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItem(long)
	 */
	public NewsItem getNewsItem(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsLink(long)
	 */
	public NewsLink getNewsLink(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCurrentNewsLinks(java.lang.String, java.lang.String)
	 */
	public List<NewsLink> getCurrentNewsLinks(String sakaiId, String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getStarredNewsLinks(java.lang.String, java.lang.String)
	 */
	public List<NewsLink> getStarredNewsLinks(String sakaiId, String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getHiddenNewsLinks(java.lang.String, java.lang.String)
	 */
	public List<NewsLink> getHiddenNewsLinks(String sakaiId, String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItem(long)
	 */
	public CalendarItem getCalendarItem(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#countNewsLinksByGroupId(java.lang.String, java.lang.String)
	 */
	public int countNewsLinksByGroupId(String sakaiUserId, String groupId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsLinksByGroupId(java.lang.String, java.lang.String, int, int)
	 */
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRepeatingCalendarItem(java.lang.String, java.lang.String)
	 */
	public RepeatingCalendarItem getRepeatingCalendarItem(
			String entityReference, String calendarTimeLabelKey) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRepeatingCalendarItems()
	 */
	public List<RepeatingCalendarItem> getRepeatingCalendarItems() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getLastIndexInSequence(java.lang.String, java.lang.String)
	 */
	public int getLastIndexInSequence(String entityReference,
			String calendarTimeLabelKey) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getFutureSequenceNumbers(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public SortedSet<Integer> getFutureSequenceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteAvailabilityChecks(java.lang.String)
	 */
	public boolean deleteAvailabilityChecks(String entityReference) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteAvailabilityChecksBeforeTime(java.util.Date)
	 */
	public boolean deleteAvailabilityChecksBeforeTime(Date time) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarItem(java.lang.Long)
	 */
	public boolean deleteCalendarItem(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLink(java.lang.Long, java.lang.Long)
	 */
	public boolean deleteCalendarLink(Long personId, Long calendarItemId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLinks(java.lang.Long)
	 */
	public boolean deleteCalendarLinks(Long calendarItemId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLinks(java.lang.Long, java.lang.Long)
	 */
	public boolean deleteCalendarLinks(Long personId, Long contextId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsItem(java.lang.Long)
	 */
	public boolean deleteNewsItem(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLink(java.lang.Long, java.lang.Long)
	 */
	public boolean deleteNewsLink(Long personId, Long newsItemId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLinks(java.lang.Long)
	 */
	public boolean deleteNewsLinks(Long newsItemId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLinks(java.lang.Long, java.lang.Long)
	 */
	public boolean deleteNewsLinks(Long personId, Long contextId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addEvent(java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean addEvent(Date eventDate, String event, String itemRef,
			String contextId, String sessionId, String eventCode) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItem(java.lang.Long, java.lang.String, java.util.Date)
	 */
	public boolean updateCalendarItem(Long id, String newTitle, Date newTime) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemTime(java.lang.Long, java.util.Date)
	 */
	public boolean updateCalendarItemTime(Long id, Date newTime) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemTitle(java.lang.Long, java.lang.String)
	 */
	public boolean updateCalendarItemTitle(Long id, String newTitle) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean updateCalendarItemsLabelKey(String entityReference,
			String oldLabelKey, String newLabelKey) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTitle(java.lang.Long, java.lang.String, java.util.Date, java.lang.String, java.lang.String)
	 */
	public boolean updateNewsItemTitle(Long id, String newTitle,
			Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemsLabelKey(String entityReference,
			String oldLabelKey, String newLabelKey) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getAvailabilityChecksBeforeTime(java.util.Date)
	 */
	public List<AvailabilityCheck> getAvailabilityChecksBeforeTime(Date time) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSakaIdsForUserWithCalendarLinks(java.lang.String)
	 */
	public Set<String> getSakaIdsForUserWithCalendarLinks(String entityReference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSakaiIdsForUserWithNewsLinks(java.lang.String)
	 */
	public Set<String> getSakaiIdsForUserWithNewsLinks(String entityReference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarLink(long, long)
	 */
	public CalendarLink getCalendarLink(long calendarItemId, long personId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarLink(org.sakaiproject.dash.model.CalendarLink)
	 */
	public boolean updateCalendarLink(CalendarLink link) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateContextTitle(java.lang.String, java.lang.String)
	 */
	public boolean updateContextTitle(String contextId, String newContextTitle) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsLink(long, long)
	 */
	public NewsLink getNewsLink(long newsItemId, long personId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsLink(org.sakaiproject.dash.model.NewsLink)
	 */
	public boolean updateNewsLink(NewsLink link) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTime(java.lang.Long, java.util.Date, java.lang.String)
	 */
	public boolean updateNewsItemTime(Long id, Date newTime,
			String newGroupingIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemLabelKey(java.lang.Long, java.lang.String, java.lang.String)
	 */
	public boolean updateNewsItemLabelKey(Long id, String labelKey,
			String newGroupingIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemSubtype(java.lang.Long, java.lang.String, java.util.Date, java.lang.String, java.lang.String)
	 */
	public boolean updateNewsItemSubtype(Long id, String newSubtype,
			Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemSubtype(java.lang.Long, java.lang.String)
	 */
	public boolean updateCalendarItemSubtype(Long id, String newSubtype) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemTime(java.lang.String, java.lang.String, java.lang.Integer, java.util.Date)
	 */
	public boolean updateCalendarItemTime(String entityReference,
			String labelKey, Integer sequenceNumber, Date newDate) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemFrequency(java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemFrequency(String entityReference,
			String frequency) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemsSubtype(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemsSubtype(String entityReference,
			String labelKey, String newSubtype) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemTime(java.lang.String, java.util.Date, java.util.Date)
	 */
	public boolean updateRepeatingCalendarItemTime(String entityReference,
			Date newFirstTime, Date newLastTime) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemTitle(java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemTitle(String entityReference,
			String newTitle) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getConfigProperty(java.lang.String)
	 */
	public Integer getConfigProperty(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#setConfigProperty(java.lang.String, java.lang.Integer)
	 */
	public void setConfigProperty(String propertyName, Integer propertyValue) {
		// TODO Auto-generated method stub

	}

	public boolean updateSourceType(SourceType sourceType) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<CalendarItem> getCalendarItems(
			RepeatingCalendarItem repeatingEvent) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean updateCalendarItem(CalendarItem calendarItem) {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<String> listUsersWithLinks(CalendarItem calendarItem) {
		// TODO Auto-generated method stub
		return null;
	}

}
