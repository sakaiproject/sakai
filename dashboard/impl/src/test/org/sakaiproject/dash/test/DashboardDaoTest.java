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

package org.sakaiproject.dash.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * This class really only tests the HSQLDB impl. But that provides a baseline for testing 
 * of the logic methods.
 */
public class DashboardDaoTest extends AbstractTransactionalSpringContextTests {
	
	protected DashboardDao dao;
	
	protected static AtomicInteger counter = new AtomicInteger(999);

	private static final long ONE_MINUTE = 1000L * 60L;
	private static final long ONE_HOUR = ONE_MINUTE * 60L;
	private static final long ONE_DAY = ONE_HOUR * 24L;
	private static final long ONE_WEEK = ONE_DAY * 7L;
	
	private static final long TIME_DELTA = 1000L * 60L * 1L;



	public DashboardDaoTest() {
		super();
		this.setDependencyCheck(false);
	}
	
    protected String[] getConfigLocations() {
		 // point to the needed spring config files, must be on the classpath
		 // (add pack/src/webapp/WEB-INF to the build path in Eclipse),
		 // they also need to be referenced in the project.xml file
		 return new String[] {"test.xml"};
	}
    
    protected final void onSetUp() throws Exception {
    	
    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and CalendarItem. It then verifies that the retrieved items 
     * have the same attribute values as the items that were saved.
     */
	public void testAddCalendarItem() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String subtype = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, subtype, null, null);
		boolean saved = dao.addCalendarItem(calendarItem);
		
		assertTrue(saved);
		
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		//System.out.println("calendarItem == " + calendarItem);

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(calendarTimeLabelKey,calendarItem.getCalendarTimeLabelKey());
		assertEquals(title, calendarItem.getTitle());
		assertEquals(entityReference, calendarItem.getEntityReference());
		assertEquals(subtype, calendarItem.getSubtype());
		
		assertEquals(calendarTime.getTime(), calendarItem.getCalendarTime().getTime());
		//assertTrue(calendarTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
		//assertTrue(calendarTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
		
		assertNotNull(calendarItem.getContext());
		assertEquals(contextId, calendarItem.getContext().getContextId());
		assertEquals(contextTitle, calendarItem.getContext().getContextTitle());
		assertEquals(contextUrl, calendarItem.getContext().getContextUrl());
		
		assertNotNull(calendarItem.getSourceType());
		assertEquals(sourceTypeIdentifier, calendarItem.getSourceType().getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType, CalendarItem and CalendarLink. It then verifies that the 
     * retrieved items have the same attribute values as the items that were saved.
     */
	public void testAddCalendarLink() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		dao.addPerson(person);
		person = dao.getPersonBySakaiId(sakaiId);
		
		CalendarLink link = new CalendarLink(person, calendarItem, context, false, false);
		boolean linkSaved = dao.addCalendarLink(link);
		assertTrue(linkSaved);
		
		boolean saved = false;
		boolean hidden = false;
		
		List<CalendarItem> items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		assertNotNull(items);
		assertTrue(items.size() > 0);
		
		boolean foundItem = false;
		for(CalendarItem item : items) {
			if(item.getEntityReference().equals(entityReference)) {
				// we have found the one and only link for this user to this item
				foundItem = true;
				assertEquals(title, item.getTitle());
				assertNotNull(item.getContext());
				assertEquals(contextId,item.getContext().getContextId());
				assertNotNull(item.getSourceType());
				assertEquals(sourceTypeIdentifier,item.getSourceType().getIdentifier());
				assertEquals(calendarTime.getTime(), item.getCalendarTime().getTime());
				//assertTrue(calendarTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
				//assertTrue(calendarTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
				break;
			}
		}
		assertTrue(foundItem);
	}

    /**
     * This method actually depends on being able to save and retrieve Context objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	public void testAddContext() {
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		
		boolean saved = dao.addContext(context);
		assertTrue(saved);
		
		context = dao.getContext(contextId);		
		assertNotNull(context);
		assertNotNull(context.getId());
		assertEquals(contextId, context.getContextId());
		assertEquals(contextTitle, context.getContextTitle());
		assertEquals(contextUrl, context.getContextUrl());
	}

	
    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and NewsItem. It then verifies that the retrieved items 
     * have the same attribute values as the items that were saved.
     */
	public void testAddNewsItem() {
		Date eventTime = new Date(System.currentTimeMillis() - ONE_DAY);
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String labelKey = getUniqueIdentifier();
		String subtype = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			labelKey, entityReference, context, sourceType, subtype);
		boolean saved = dao.addNewsItem(newsItem);
		
		assertTrue(saved);
		
		newsItem = dao.getNewsItem(entityReference);

		assertNotNull(newsItem);
		assertNotNull(newsItem.getId());
		
		assertEquals(title, newsItem.getTitle());
		assertEquals(entityReference, newsItem.getEntityReference());
		assertEquals(labelKey, newsItem.getNewsTimeLabelKey());
		assertEquals(subtype, newsItem.getSubtype());

		assertEquals(eventTime.getTime(), newsItem.getNewsTime().getTime());
		//assertTrue(eventTime.getTime() + TIME_DELTA > newsItem.getNewsTime().getTime());
		//assertTrue(eventTime.getTime() - TIME_DELTA < newsItem.getNewsTime().getTime());
		
		assertNotNull(newsItem.getContext());
		assertEquals(contextId, newsItem.getContext().getContextId());
		assertEquals(contextTitle, newsItem.getContext().getContextTitle());
		assertEquals(contextUrl, newsItem.getContext().getContextUrl());
		
		assertNotNull(newsItem.getSourceType());
		assertEquals(sourceTypeIdentifier, newsItem.getSourceType().getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType, NewsItem and newsLink. It then verifies that the retrieved 
     * items have the same attribute values as the items that were saved.
     */
	public void testAddNewsLink() {
		Date eventTime = new Date(System.currentTimeMillis() - ONE_DAY);
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			null, entityReference, context, sourceType, null);
		dao.addNewsItem(newsItem);
		newsItem = dao.getNewsItem(entityReference);
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		dao.addPerson(person);
		person = dao.getPersonBySakaiId(sakaiId);
		
		NewsLink link = new NewsLink(person, newsItem, context, false, false);
		boolean savedLink = dao.addNewsLink(link);
		assertTrue(savedLink);
		
//		boolean saved = false;
//		boolean hidden = false;
//		
//		List<NewsItem> items = dao.getNewsItems(sakaiId, contextId, saved, hidden);
//		assertNotNull(items);
//		assertTrue(items.size() > 0);
//		
//		boolean foundItem = false;
//		for(NewsItem item : items) {
//			if(item.getEntityReference().equals(entityReference)) {
//				// we have found the one and only link for this user to this item
//				foundItem = true;
//				assertEquals(title, item.getTitle());
//				assertNotNull(item.getContext());
//				assertEquals(contextId,item.getContext().getContextId());
//				assertNotNull(item.getSourceType());
//				assertEquals(sourceTypeIdentifier,item.getSourceType().getIdentifier());
//				assertEquals(eventTime.getTime(), item.getNewsTime().getTime());
//				break;
//			}
//		}
//		assertTrue(foundItem);
	}

    /**
     * This method actually depends on being able to save and retrieve Person objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	public void testAddPerson() {
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		
		boolean saved = dao.addPerson(person);
		assertTrue(saved);
		
		person = dao.getPersonBySakaiId(sakaiId);
		assertNotNull(person);
		assertNotNull(person.getId());
		assertEquals(sakaiId,person.getSakaiId());
		assertEquals(userId,person.getUserId());
	}
	
	public void testAddRepeatingCalendarItem() {
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String subtype = getUniqueIdentifier();
		String frequency = RepeatingCalendarItem.REPEATS_DAILY;
		String timeLabel = getUniqueIdentifier();
		Date firstTime = new Date();
		Date lastTime = new Date(firstTime.getTime() + ONE_WEEK + ONE_HOUR);
		int maxCount = 5;
		
		String identifier1 = getUniqueIdentifier();
		SourceType sourceType = new  SourceType(identifier1);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(identifier1);
		assertNotNull(sourceType);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		assertNotNull(context);
		
		RepeatingCalendarItem repeatingCalendarItem = new RepeatingCalendarItem(title, firstTime, lastTime, 
				timeLabel, entityReference, subtype, context, sourceType, frequency, maxCount);
		boolean savedItem = dao.addRepeatingCalendarItem(repeatingCalendarItem);
		
		assertTrue(savedItem);
		
		RepeatingCalendarItem repeatingCalendarItem1 = dao.getRepeatingCalendarItem(entityReference, timeLabel);
		
		assertNotNull(repeatingCalendarItem1);
		assertNotNull(repeatingCalendarItem1.getId());
		assertEquals(title, repeatingCalendarItem1.getTitle());
		assertEquals(entityReference, repeatingCalendarItem1.getEntityReference());
		assertEquals(subtype, repeatingCalendarItem1.getSubtype());
		assertEquals(frequency, repeatingCalendarItem1.getFrequency());
		assertEquals(timeLabel, repeatingCalendarItem1.getCalendarTimeLabelKey());
		assertEquals(maxCount,repeatingCalendarItem1.getMaxCount());
		
		assertNotNull(repeatingCalendarItem1.getFirstTime());
		assertEquals(firstTime.getTime(),  repeatingCalendarItem1.getFirstTime().getTime());
		assertNotNull(repeatingCalendarItem1.getLastTime());
		assertEquals(lastTime.getTime(), repeatingCalendarItem1.getLastTime().getTime());
		
		assertNotNull(repeatingCalendarItem1.getContext());
		assertEquals(contextId, repeatingCalendarItem1.getContext().getContextId());
		
		assertNotNull(repeatingCalendarItem1.getSourceType());
		assertEquals(identifier1, repeatingCalendarItem1.getSourceType().getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve SourceType objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	public void testAddSourceType() {
		String identifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(identifier);
		boolean saved = dao.addSourceType(sourceType);
		assertTrue(saved);
		
		sourceType = dao.getSourceType(identifier);
		assertNotNull(sourceType);
		assertNotNull(sourceType.getId());
		assertEquals(identifier,sourceType.getIdentifier());

		String identifier1 = getUniqueIdentifier();
		SourceType sourceType1 = new SourceType(identifier1);
		boolean saved1 = dao.addSourceType(sourceType1);
		assertTrue(saved1);
		
		sourceType1 = dao.getSourceType(identifier1);
		assertNotNull(sourceType1);
		assertNotNull(sourceType1.getId());
		assertEquals(identifier1,sourceType1.getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and CalendarItem. It then deletes the CalendarItem and verifies 
	 * that the deleted item can not longer be retrieved.
     */
	public void testDeleteCalendarItem() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		boolean removed = dao.deleteCalendarItem(calendarItem.getId());
		assertTrue(removed);
		calendarItem = dao.getCalendarItem(calendarItem.getId());
		assertNull(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		assertNull(calendarItem);		
	}

	public void testDeleteCalendarLinksLong() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		dao.addPerson(person);
		person = dao.getPersonBySakaiId(sakaiId);
		
		CalendarLink link = new CalendarLink(person, calendarItem, context, false, false);
		boolean linkSaved = dao.addCalendarLink(link);
		assertTrue(linkSaved);
		
		boolean showFuture = true;
		boolean showPast = true;
		boolean saved = false;
		boolean hidden = false;
		
		List<CalendarItem> items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		assertNotNull(items);
		assertTrue(items.size() > 0);
		
		boolean foundItem = false;
		List<Long> calendarItemIds = new ArrayList<Long>();
		for(CalendarItem item : items) {
			if(item.getEntityReference().equals(entityReference)) {
				// we have found the one and only link for this user to this item
				foundItem = true;
				calendarItemIds.add(item.getId());
				boolean deleted = dao.deleteCalendarLinks(item.getId());
				assertTrue(deleted);
			}
		}
		assertTrue(foundItem);
		assertTrue(calendarItemIds.size() > 0);
		
		items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		
		assertTrue(items.size() == 0);
	}

	public void testDeleteCalendarLinksLongLong() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		dao.addPerson(person);
		person = dao.getPersonBySakaiId(sakaiId);
		
		CalendarLink link = new CalendarLink(person, calendarItem, context, false, false);
		boolean linkSaved = dao.addCalendarLink(link);
		assertTrue(linkSaved);
		
		boolean showFuture = true;
		boolean showPast = true;
		boolean saved = false;
		boolean hidden = false;
		
		List<CalendarItem> items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		assertNotNull(items);
		assertTrue(items.size() > 0);
		
		boolean deleted = dao.deleteCalendarLinks(person.getId(), context.getId());
		assertTrue(deleted);
		items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		
		assertTrue(items.size() == 0);
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and NewsItem. It then deletes the NewsItem and verifies 
	 * that the deleted item can not longer be retrieved.
     */
	public void testDeleteNewsItem() {
		Long id;
	}

	public void testDeleteNewsLinksLong() {
		Long newsItemId;
	}

	public void testDeleteNewsLinksLongLong() {
		Long personId;
		Long contextId;
	}

	public void testGetCalendarItemLong() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(title, calendarItem.getTitle());
		assertEquals(entityReference, calendarItem.getEntityReference());
		assertEquals(calendarTime.getTime(), calendarItem.getCalendarTime().getTime());
		
		assertNotNull(calendarItem.getContext());
		assertEquals(contextId, calendarItem.getContext().getContextId());
		assertEquals(contextTitle, calendarItem.getContext().getContextTitle());
		assertEquals(contextUrl, calendarItem.getContext().getContextUrl());
		
		assertNotNull(calendarItem.getSourceType());
		assertEquals(sourceTypeIdentifier, calendarItem.getSourceType().getIdentifier());
	}

	public void testGetCalendarItemString() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(title, calendarItem.getTitle());
		assertEquals(entityReference, calendarItem.getEntityReference());
		assertEquals(calendarTime.getTime(), calendarItem.getCalendarTime().getTime());
		
		assertNotNull(calendarItem.getContext());
		assertEquals(contextId, calendarItem.getContext().getContextId());
		assertEquals(contextTitle, calendarItem.getContext().getContextTitle());
		assertEquals(contextUrl, calendarItem.getContext().getContextUrl());
		
		assertNotNull(calendarItem.getSourceType());
		assertEquals(sourceTypeIdentifier, calendarItem.getSourceType().getIdentifier());	}

	public void testGetCalendarItems() {
		String sakaiUserId;
		String contextId;
	}

	public void testGetCalendarItemsByContext() {
		String contextId;
	}

	public void testGetContextLong() {
		long id;
	}

	public void testGetContextString() {
		String contextId;
	}

	public void testGetNewsItemString() {
		String entityReference;
	}

	public void testGetNewsItemLong() {
		long id;
	}

	public void testGetNewsItems() {
		String sakaiUserId;
		String contextId;
	}

	public void testGetNewsItemsByContext() {

	}

	public void testGetPersonBySakaiId() {
		String sakaiId;
	}

	public void testGetSourceType() {
		String identifier;
	}

	public void testUpdateCalendarItem() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
//		String newTitle = getUniqueIdentifier();
//		Date newTime = new Date(System.currentTimeMillis() + (2L * ONE_DAY));
		
//		boolean updated = dao.updateCalendarItem(calendarItem.getId(), newTitle, newTime);
//		assertTrue(updated);
//		
//		calendarItem = dao.getCalendarItem(calendarItem.getId());
//
//		assertNotNull(calendarItem);
//		assertNotNull(calendarItem.getId());
//		
//		assertEquals(newTitle, calendarItem.getTitle());
//		
//		assertEquals(newTime.getTime(), calendarItem.getCalendarTime().getTime());
		//assertTrue(newTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
		//assertTrue(newTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
	}

	public void testUpdateCalendarItemSubtype() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String subtype1 = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, subtype1, null, null);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		assertEquals(subtype1, calendarItem.getSubtype());
		
//		String subtype2 = getUniqueIdentifier();
//		
//		boolean updated = dao.updateCalendarItemSubtype(calendarItem.getId(), subtype2);
//		assertTrue(updated);
//		
//		CalendarItem revisedCalendarItem = dao.getCalendarItem(calendarItem.getId());
//
//		assertNotNull(revisedCalendarItem);
//		assertNotNull(revisedCalendarItem.getId());
//		
//		assertEquals(subtype2, revisedCalendarItem.getSubtype());
//		
	}

	public void testUpdateCalendarItemTime() {
		Long id;
		Date newTime;
	}

	public void testUpdateCalendarItemTitle() {
		Long id;
		String newTitle;
	}
	
	public void testUpdateContextTitle() {
		String contextId = getUniqueIdentifier();
		String contextTitle1 = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle1, contextUrl );
		
		boolean saved = dao.addContext(context);
		assertTrue(saved);
		
		context = dao.getContext(contextId);		
		assertNotNull(context);
		assertNotNull(context.getId());
		assertEquals(contextId, context.getContextId());
		assertEquals(contextTitle1, context.getContextTitle());
		assertEquals(contextUrl, context.getContextUrl());
		
		String contextTitle2 = getUniqueIdentifier();
		boolean updated = dao.updateContextTitle(contextId, contextTitle2);
		
		assertTrue(updated);
		Context revisedContext = dao.getContext(contextId);	
		
		assertNotNull(revisedContext);
		assertNotNull(revisedContext.getId());
		assertEquals(contextId, revisedContext.getContextId());
		assertEquals(contextTitle2, revisedContext.getContextTitle());
		assertEquals(contextUrl, revisedContext.getContextUrl());
	}

	public void testUpdateNewsItemTitle() {
		Long id;
		String newTitle;
	}

	public void testUpdateNewsItemTime() {
		Date eventTime = new Date(System.currentTimeMillis() - ONE_DAY);
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			null, entityReference, context, sourceType, null);
		boolean saved = dao.addNewsItem(newsItem);
		
		assertTrue(saved);
		
		NewsItem savedItem = dao.getNewsItem(entityReference);
		assertNotNull(savedItem);
		assertNotNull(savedItem.getId());
		assertTrue(savedItem.getId().longValue() >= 0L);
		
		Date newTime = new Date();
		String newGroupingIdentifier = getUniqueIdentifier();
		dao.updateNewsItemTime(savedItem.getId(), newTime, newGroupingIdentifier);
		NewsItem revisedItem = dao.getNewsItem(savedItem.getId());
		assertNotNull(revisedItem);
		assertEquals(newTime.getTime(), revisedItem.getNewsTime().getTime());
		
		
	}

	public void testUpdateNewsItemLabelKey() {
		Date eventTime = new Date(System.currentTimeMillis() - ONE_DAY);
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String labelKey1 = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			labelKey1, entityReference, context, sourceType, null);
		boolean saved = dao.addNewsItem(newsItem);
		
		assertTrue(saved);
		
		NewsItem savedItem = dao.getNewsItem(entityReference);
		assertNotNull(savedItem);
		assertNotNull(savedItem.getId());
		assertTrue(savedItem.getId().longValue() >= 0L);
		assertEquals(labelKey1, savedItem.getNewsTimeLabelKey());

		String labelKey2 = getUniqueIdentifier();
		assertFalse(labelKey1.equals(labelKey2));

//		String newGroupingIdentifier = getUniqueIdentifier();
//		dao.updateNewsItemLabelKey(savedItem.getId(), labelKey2, newGroupingIdentifier);
//		NewsItem revisedItem = dao.getNewsItem(savedItem.getId());
//		assertNotNull(revisedItem);
//		assertEquals(labelKey2, revisedItem.getNewsTimeLabelKey());
//		
		
	}

	public void testUpdateNewsItemSubtype() {
		Date eventTime = new Date(System.currentTimeMillis() - ONE_DAY);
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String subtype1 = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			null, entityReference, context, sourceType, subtype1);
		boolean saved = dao.addNewsItem(newsItem);
		
		assertTrue(saved);
		
		NewsItem savedItem = dao.getNewsItem(entityReference);
		assertNotNull(savedItem);
		assertNotNull(savedItem.getId());
		assertTrue(savedItem.getId().longValue() >= 0L);
		assertEquals(subtype1, savedItem.getSubtype());

		String subtype2 = getUniqueIdentifier();
		assertFalse(subtype1.equals(subtype2));

//		Date newNewsTime = new Date();
//		
//		String newLabelKey = getUniqueIdentifier();
//		String newGroupingIdentifier = getUniqueIdentifier();
//		dao.updateNewsItemSubtype(savedItem.getId(), subtype2, newNewsTime, newLabelKey, newGroupingIdentifier);
//		NewsItem revisedItem = dao.getNewsItem(savedItem.getId());
//		assertNotNull(revisedItem);
//		assertEquals(subtype2, revisedItem.getSubtype());
//		assertEquals(newNewsTime, revisedItem.getNewsTime());
//		assertEquals(newLabelKey, revisedItem.getNewsTimeLabelKey());
//		
	}
	
	public void testUpdateRepeatingCalendarItemSubtype() {
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String subtype1 = getUniqueIdentifier();
		String frequency = RepeatingCalendarItem.REPEATS_DAILY;
		String timeLabel = getUniqueIdentifier();
		Date firstTime = new Date();
		Date lastTime = new Date(firstTime.getTime() + ONE_WEEK + ONE_HOUR);
		int maxCount = 5;
		
		String identifier1 = getUniqueIdentifier();
		SourceType sourceType = new  SourceType(identifier1);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(identifier1);
		assertNotNull(sourceType);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		assertNotNull(context);
		
		RepeatingCalendarItem repeatingCalendarItem = new RepeatingCalendarItem(title, firstTime, lastTime, 
				timeLabel, entityReference, subtype1, context, sourceType, frequency, maxCount);
		boolean savedItem = dao.addRepeatingCalendarItem(repeatingCalendarItem);
		
		assertTrue(savedItem);
		
		RepeatingCalendarItem repeatingCalendarItem1 = dao.getRepeatingCalendarItem(entityReference, timeLabel);
		
		assertNotNull(repeatingCalendarItem1);
		assertNotNull(repeatingCalendarItem1.getId());
		
//		String subtype2 = getUniqueIdentifier();
//		boolean updated = dao.updateRepeatingCalendarItemsSubtype(entityReference, timeLabel, subtype2);
//		
//		assertTrue(updated);
//		
//		RepeatingCalendarItem repeatingCalendarItem2 = dao.getRepeatingCalendarItem(entityReference, timeLabel);
//		
//		assertNotNull(repeatingCalendarItem2);
//		assertNotNull(repeatingCalendarItem2.getId());
//		
//		assertEquals(subtype2, repeatingCalendarItem2.getSubtype());

	}
	
	public void testDeleteCalendarItemsWithoutLinks() {
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		assertNotNull(person);
		
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		int totalItems = 0;
		int itemsWithLinks = 0;
		
		for(int i = -7; i <= 7; i++) {

			String calendarTimeLabelKey = getUniqueIdentifier();
			Date calendarTime = new Date(System.currentTimeMillis() + (i * ONE_DAY));
			CalendarItem calendarItem = new CalendarItem(title, calendarTime,
					calendarTimeLabelKey, entityReference, context, sourceType, null, null, null);
			boolean saved = dao.addCalendarItem(calendarItem);
			assertTrue(saved);
			calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
			assertNotNull(calendarItem);
			assertNotNull(calendarItem.getId());
			
			if(i % 2 == 1 || i % 2 == -1) {
				CalendarLink link = new CalendarLink(person,calendarItem,context,false,false);
				boolean linkSaved = dao.addCalendarLink(link);
				assertTrue(linkSaved);
				link = dao.getCalendarLink(calendarItem.getId(), person.getId());
				assertNotNull(link);
				itemsWithLinks++;
			}
			totalItems++;
		}
		
		List<CalendarItem> before = dao.getCalendarItems(entityReference);
		assertNotNull(before);
		assertEquals(totalItems, before.size());
		
		boolean linksDeleted = dao.deleteCalendarItemsWithoutLinks();
		assertTrue(linksDeleted);
		
		List<CalendarItem> after = dao.getCalendarItems(entityReference);
		assertNotNull(after);
		assertEquals(itemsWithLinks, after.size());
		
	}
	
	public void testDeleteCalendarLinksBefore() {
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		
		assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		assertNotNull(person);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);
		
		String subtype = getUniqueIdentifier();

		int totalCount = 0;
		int hiddenCount = 0;
		int stickyCount = 0;
		
		int stickyAndHiddenCount1 = 0;
		int hiddenNotStickyCount1 = 0;
		int stickyNotHiddenCount1 = 0;
		int notStickyNotHiddenCount1 = 0;
		
		int stickyAndHiddenCount2 = 0;
		int hiddenNotStickyCount2 = 0;
		int stickyNotHiddenCount2 = 0;
		
		Date deleteDate1 = new Date(System.currentTimeMillis() + 4 * ONE_DAY);
		Date deleteDate2 = new Date(System.currentTimeMillis() + 6 * ONE_DAY);
		
		
		for(int i = 0; i <= 10; i++) {
			String title = getUniqueIdentifier();
			String entityReference = getUniqueIdentifier();
			
			String calendarTimeLabelKey = getUniqueIdentifier();
			Date calendarTime = new Date(System.currentTimeMillis() + (i * ONE_DAY));
			CalendarItem calendarItem = new CalendarItem(title, calendarTime,
					calendarTimeLabelKey, entityReference, context, sourceType, subtype, null, null);
			boolean saved = dao.addCalendarItem(calendarItem);
			assertTrue(saved);
			calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
			assertNotNull(calendarItem);
			assertNotNull(calendarItem.getId());
			
			boolean hidden = i % 2 == 1;
			boolean sticky = i % 3 == 1;
			CalendarLink link = new CalendarLink(person,calendarItem,context,hidden,sticky);
			boolean linkSaved = dao.addCalendarLink(link);
			assertTrue(linkSaved);
			link = dao.getCalendarLink(calendarItem.getId(), person.getId());
			assertNotNull(link);

			totalCount++;
			if(hidden) {
				hiddenCount++;
			}
			if(sticky) {
				stickyCount++;
			}
			if(calendarTime.before(deleteDate1)) {
				if(hidden) {
					if(sticky) {
						stickyAndHiddenCount1++;
					} else {
						hiddenNotStickyCount1++;
					}
				} else if(sticky) {
					stickyNotHiddenCount1++;
				} else {
					notStickyNotHiddenCount1++;
				}
			}  
			if(calendarTime.before(deleteDate2)) {
				if(hidden) {
					if(sticky) {
						stickyAndHiddenCount2++;
					} else {
						hiddenNotStickyCount2++;
					}
				} else if(sticky) {
					stickyNotHiddenCount2++;
				}
			}
		}
		
		int expectedCount = totalCount - hiddenCount;
		List<CalendarLink> links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());
		
		links = dao.getStarredCalendarLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(stickyCount, links.size());
		
		links = dao.getFutureCalendarLinks(sakaiId, contextId, true);
		assertNotNull(links);
		assertEquals(hiddenCount, links.size());
		
		expectedCount = expectedCount - notStickyNotHiddenCount1;
		dao.deleteCalendarLinksBefore(deleteDate1, false, false);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());

		expectedCount = expectedCount - stickyNotHiddenCount1;
		dao.deleteCalendarLinksBefore(deleteDate1, true, false);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());
		
		links = dao.getStarredCalendarLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(stickyCount - stickyNotHiddenCount1, links.size());
		
		dao.deleteCalendarLinksBefore(deleteDate1, false, true);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, true);
		assertNotNull(links);
		assertEquals(hiddenCount - hiddenNotStickyCount1, links.size());

		dao.deleteCalendarLinksBefore(deleteDate1, true, true);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, true);
		assertNotNull(links);
		assertEquals(hiddenCount - hiddenNotStickyCount1 - stickyAndHiddenCount1, links.size());
		
		expectedCount = expectedCount - stickyNotHiddenCount2;
		dao.deleteCalendarLinksBefore(deleteDate2, true, false);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());	
		
	}

	public void testDeleteNewsItemsWithoutLinks() {
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		
		assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		assertNotNull(person);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);
		
		String subtype = getUniqueIdentifier();

		int totalItems = 0;
		int itemsWithLinks = 0;
		
		for(int i = -7; i <= 7; i++) {
			String title = getUniqueIdentifier();
			String entityReference = getUniqueIdentifier();
			
			String newsTimeLabelKey = getUniqueIdentifier();
			Date newsTime = new Date(System.currentTimeMillis() + (i * ONE_DAY));
			NewsItem newsItem = new NewsItem(title, newsTime,
					newsTimeLabelKey, entityReference, context, sourceType, subtype);
			boolean saved = dao.addNewsItem(newsItem);
			assertTrue(saved);
			newsItem = dao.getNewsItem(entityReference);
			assertNotNull(newsItem);
			assertNotNull(newsItem.getId());
			
			if(i % 2 == 1 || i % 2 == -1) {
				NewsLink link = new NewsLink(person,newsItem,context,false,false);
				boolean linkSaved = dao.addNewsLink(link);
				assertTrue(linkSaved);
				link = dao.getNewsLink(newsItem.getId(), person.getId());
				assertNotNull(link);
				itemsWithLinks++;
			}
			totalItems++;
		}
		
		List<NewsItem> before = dao.getNewsItemsByContext(context.getContextId());
		assertNotNull(before);
		assertEquals(totalItems, before.size());
		
		boolean linksDeleted = dao.deleteNewsItemsWithoutLinks();
		assertTrue(linksDeleted);
		
		List<NewsItem> after = dao.getNewsItemsByContext(context.getContextId());
		assertNotNull(after);
		assertEquals(itemsWithLinks, after.size());
	}
	
	public void testDeleteNewsLinksBefore() {
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		
		assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		assertNotNull(person);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);
		
		String subtype = getUniqueIdentifier();

		int totalCount = 0;
		int hiddenCount = 0;
		int stickyCount = 0;
		
		int stickyAndHiddenCount1 = 0;
		int hiddenNotStickyCount1 = 0;
		int stickyNotHiddenCount1 = 0;
		int notStickyNotHiddenCount1 = 0;
		
		int stickyAndHiddenCount2 = 0;
		int hiddenNotStickyCount2 = 0;
		int stickyNotHiddenCount2 = 0;
		
		Date deleteDate1 = new Date(System.currentTimeMillis() + 4 * ONE_DAY);
		Date deleteDate2 = new Date(System.currentTimeMillis() + 6 * ONE_DAY);
		
		
		for(int i = 0; i <= 10; i++) {
			String title = getUniqueIdentifier();
			String entityReference = getUniqueIdentifier();
			
			String newsTimeLabelKey = getUniqueIdentifier();
			Date newsTime = new Date(System.currentTimeMillis() + (i * ONE_DAY));
			NewsItem newsItem = new NewsItem(title, newsTime,
					newsTimeLabelKey, entityReference, context, sourceType, subtype);
			boolean saved = dao.addNewsItem(newsItem);
			assertTrue(saved);
			newsItem = dao.getNewsItem(entityReference);
			assertNotNull(newsItem);
			assertNotNull(newsItem.getId());
			
			boolean hidden = i % 2 == 1;
			boolean sticky = i % 3 == 1;
			NewsLink link = new NewsLink(person,newsItem,context,hidden,sticky);
			boolean linkSaved = dao.addNewsLink(link);
			assertTrue(linkSaved);
			link = dao.getNewsLink(newsItem.getId(), person.getId());
			assertNotNull(link);

			totalCount++;
			if(hidden) {
				hiddenCount++;
			}
			if(sticky) {
				stickyCount++;
			}
			if(newsTime.before(deleteDate1)) {
				if(hidden) {
					if(sticky) {
						stickyAndHiddenCount1++;
					} else {
						hiddenNotStickyCount1++;
					}
				} else if(sticky) {
					stickyNotHiddenCount1++;
				} else {
					notStickyNotHiddenCount1++;
				}
			}  
			if(newsTime.before(deleteDate2)) {
				if(hidden) {
					if(sticky) {
						stickyAndHiddenCount2++;
					} else {
						hiddenNotStickyCount2++;
					}
				} else if(sticky) {
					stickyNotHiddenCount2++;
				}
			}
		}
		
		List<NewsItem> items = dao.getNewsItemsByContext(context.getContextId());
		assertNotNull(items);
		assertEquals(totalCount, items.size());
		
		int expectedCount = totalCount - hiddenCount;
		List<NewsLink> links = dao.getCurrentNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());
		
		links = dao.getStarredNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(stickyCount, links.size());
		
		links = dao.getHiddenNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(hiddenCount, links.size());
		
		expectedCount = expectedCount - notStickyNotHiddenCount1;
		dao.deleteNewsLinksBefore(deleteDate1, false, false);
		links = dao.getCurrentNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());

		expectedCount = expectedCount - stickyNotHiddenCount1;
		dao.deleteNewsLinksBefore(deleteDate1, true, false);
		links = dao.getCurrentNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());
		
		links = dao.getStarredNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(stickyCount - stickyNotHiddenCount1, links.size());
		
		dao.deleteNewsLinksBefore(deleteDate1, false, true);
		links = dao.getHiddenNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(hiddenCount - hiddenNotStickyCount1, links.size());

		dao.deleteNewsLinksBefore(deleteDate1, true, true);
		links = dao.getHiddenNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(hiddenCount - hiddenNotStickyCount1 - stickyAndHiddenCount1, links.size());
		
		expectedCount = expectedCount - stickyNotHiddenCount2;
		dao.deleteNewsLinksBefore(deleteDate2, true, false);
		links = dao.getCurrentNewsLinks(sakaiId, contextId);
		assertNotNull(links);
		assertEquals(expectedCount, links.size());	
}



	protected String getUniqueIdentifier() {
		return "unique-identifier-" + counter.incrementAndGet();
	}

	public void setDashboardDao(DashboardDao dashboardDao) {
		this.dao = dashboardDao;
		
	}
}
