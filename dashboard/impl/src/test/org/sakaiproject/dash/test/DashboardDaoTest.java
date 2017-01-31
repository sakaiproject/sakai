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
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.*;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.dao.impl.DashboardDaoImpl;
import org.sakaiproject.dash.logic.TaskLock;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;

/**
 * This class really only tests the HSQLDB impl. But that provides a baseline for testing 
 * of the logic methods.
 */
@ContextConfiguration(classes = {DashboardDaoTest.class})
@Configuration
public class DashboardDaoTest extends AbstractJUnit4SpringContextTests {

    @Autowired
	protected DashboardDao dao;

	@Bean
	public DataSource dataSource() {
		jdbcDataSource ds = new jdbcDataSource();
		ds.setUser("sa");
		ds.setPassword("");
		ds.setDatabase("jdbc:hsqldb:mem:dash");
		return ds;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean
	public DashboardDaoImpl dashboardDao() {
		DashboardDaoImpl dao = new DashboardDaoImpl();
		dao.setServerConfigurationService(serverConfigurationService());
		dao.setJdbcTemplate(jdbcTemplate());
        dao.init();
		return dao;
	}

	@Bean(name = {"mockServerConfigurationService"})
	public ServerConfigurationService serverConfigurationService() {
		ServerConfigurationService serverConfigurationService = Mockito.mock(ServerConfigurationService.class);
		when(serverConfigurationService.getString(
				eq("vendor@org.sakaiproject.db.api.SqlService"))).thenReturn("hsqldb");
		when(serverConfigurationService.getString(
				eq("vendor@org.sakaiproject.db.api.SqlService"), any())).thenReturn("hsqldb");
		when(serverConfigurationService.getBoolean("auto.ddl", true)).thenReturn(true);
		return serverConfigurationService;
	}

	protected static AtomicInteger counter = new AtomicInteger(999);

	private static final long ONE_MINUTE = 1000L * 60L;
	private static final long ONE_HOUR = ONE_MINUTE * 60L;
	private static final long ONE_DAY = ONE_HOUR * 24L;
	private static final long ONE_WEEK = ONE_DAY * 7L;
	
	private static final long TIME_DELTA = 1000L * 60L * 1L;


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
	@Test
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
		
		Assert.assertTrue(saved);
		
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);

		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
		Assert.assertEquals(calendarTimeLabelKey,calendarItem.getCalendarTimeLabelKey());
		Assert.assertEquals(title, calendarItem.getTitle());
		Assert.assertEquals(entityReference, calendarItem.getEntityReference());
		Assert.assertEquals(subtype, calendarItem.getSubtype());
		
		Assert.assertEquals(calendarTime.getTime(), calendarItem.getCalendarTime().getTime());
		//Assert.assertTrue(calendarTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
		//Assert.assertTrue(calendarTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
		
		Assert.assertNotNull(calendarItem.getContext());
		Assert.assertEquals(contextId, calendarItem.getContext().getContextId());
		Assert.assertEquals(contextTitle, calendarItem.getContext().getContextTitle());
		Assert.assertEquals(contextUrl, calendarItem.getContext().getContextUrl());
		
		Assert.assertNotNull(calendarItem.getSourceType());
		Assert.assertEquals(sourceTypeIdentifier, calendarItem.getSourceType().getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType, CalendarItem and CalendarLink. It then verifies that the 
     * retrieved items have the same attribute values as the items that were saved.
     */
	@Test
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
		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		Assert.assertTrue(calendarItem.getId().longValue() > 0L);
		Assert.assertEquals(entityReference, calendarItem.getEntityReference());
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		dao.addPerson(person);
		person = dao.getPersonBySakaiId(sakaiId);
		
		CalendarLink link = new CalendarLink(person, calendarItem, context, false, false);
		boolean linkSaved = dao.addCalendarLink(link);
		Assert.assertTrue(linkSaved);
		
		boolean saved = false;
		boolean hidden = false;
		
		List<CalendarItem> items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		Assert.assertNotNull(items);
		Assert.assertTrue(items.size() > 0);
		
		boolean foundItem = false;
		for(CalendarItem item : items) {
			if(item.getEntityReference().equals(entityReference)) {
				// we have found the one and only link for this user to this item
				foundItem = true;
				Assert.assertEquals(title, item.getTitle());
				Assert.assertNotNull(item.getContext());
				Assert.assertEquals(contextId,item.getContext().getContextId());
				Assert.assertNotNull(item.getSourceType());
				Assert.assertEquals(sourceTypeIdentifier,item.getSourceType().getIdentifier());
				Assert.assertEquals(calendarTime.getTime(), item.getCalendarTime().getTime());
				//Assert.assertTrue(calendarTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
				//Assert.assertTrue(calendarTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
				break;
			}
		}
		Assert.assertTrue(foundItem);
	}

    /**
     * This method actually depends on being able to save and retrieve Context objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	@Test
	public void testAddContext() {
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		
		boolean saved = dao.addContext(context);
		Assert.assertTrue(saved);
		
		context = dao.getContext(contextId);		
		Assert.assertNotNull(context);
		Assert.assertNotNull(context.getId());
		Assert.assertEquals(contextId, context.getContextId());
		Assert.assertEquals(contextTitle, context.getContextTitle());
		Assert.assertEquals(contextUrl, context.getContextUrl());
	}

	
    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and NewsItem. It then verifies that the retrieved items 
     * have the same attribute values as the items that were saved.
     */
	@Test
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
		
		Assert.assertTrue(saved);
		
		newsItem = dao.getNewsItem(entityReference);

		Assert.assertNotNull(newsItem);
		Assert.assertNotNull(newsItem.getId());
		
		Assert.assertEquals(title, newsItem.getTitle());
		Assert.assertEquals(entityReference, newsItem.getEntityReference());
		Assert.assertEquals(labelKey, newsItem.getNewsTimeLabelKey());
		Assert.assertEquals(subtype, newsItem.getSubtype());

		Assert.assertEquals(eventTime.getTime(), newsItem.getNewsTime().getTime());
		//Assert.assertTrue(eventTime.getTime() + TIME_DELTA > newsItem.getNewsTime().getTime());
		//Assert.assertTrue(eventTime.getTime() - TIME_DELTA < newsItem.getNewsTime().getTime());
		
		Assert.assertNotNull(newsItem.getContext());
		Assert.assertEquals(contextId, newsItem.getContext().getContextId());
		Assert.assertEquals(contextTitle, newsItem.getContext().getContextTitle());
		Assert.assertEquals(contextUrl, newsItem.getContext().getContextUrl());
		
		Assert.assertNotNull(newsItem.getSourceType());
		Assert.assertEquals(sourceTypeIdentifier, newsItem.getSourceType().getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType, NewsItem and newsLink. It then verifies that the retrieved 
     * items have the same attribute values as the items that were saved.
     */
	@Test
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
		Assert.assertTrue(savedLink);
		
//		boolean saved = false;
//		boolean hidden = false;
//		
//		List<NewsItem> items = dao.getNewsItems(sakaiId, contextId, saved, hidden);
//		Assert.assertNotNull(items);
//		Assert.assertTrue(items.size() > 0);
//		
//		boolean foundItem = false;
//		for(NewsItem item : items) {
//			if(item.getEntityReference().equals(entityReference)) {
//				// we have found the one and only link for this user to this item
//				foundItem = true;
//				Assert.assertEquals(title, item.getTitle());
//				Assert.assertNotNull(item.getContext());
//				Assert.assertEquals(contextId,item.getContext().getContextId());
//				Assert.assertNotNull(item.getSourceType());
//				Assert.assertEquals(sourceTypeIdentifier,item.getSourceType().getIdentifier());
//				Assert.assertEquals(eventTime.getTime(), item.getNewsTime().getTime());
//				break;
//			}
//		}
//		Assert.assertTrue(foundItem);
	}

    /**
     * This method actually depends on being able to save and retrieve Person objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	@Test
	public void testAddPerson() {
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		
		boolean saved = dao.addPerson(person);
		Assert.assertTrue(saved);
		
		person = dao.getPersonBySakaiId(sakaiId);
		Assert.assertNotNull(person);
		Assert.assertNotNull(person.getId());
		Assert.assertEquals(sakaiId,person.getSakaiId());
		Assert.assertEquals(userId,person.getUserId());
	}
	
	@Test
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
		Assert.assertNotNull(sourceType);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		Assert.assertNotNull(context);
		
		RepeatingCalendarItem repeatingCalendarItem = new RepeatingCalendarItem(title, firstTime, lastTime, 
				timeLabel, entityReference, subtype, context, sourceType, frequency, maxCount);
		boolean savedItem = dao.addRepeatingCalendarItem(repeatingCalendarItem);
		
		Assert.assertTrue(savedItem);
		
		RepeatingCalendarItem repeatingCalendarItem1 = dao.getRepeatingCalendarItem(entityReference, timeLabel);
		
		Assert.assertNotNull(repeatingCalendarItem1);
		Assert.assertNotNull(repeatingCalendarItem1.getId());
		Assert.assertEquals(title, repeatingCalendarItem1.getTitle());
		Assert.assertEquals(entityReference, repeatingCalendarItem1.getEntityReference());
		Assert.assertEquals(subtype, repeatingCalendarItem1.getSubtype());
		Assert.assertEquals(frequency, repeatingCalendarItem1.getFrequency());
		Assert.assertEquals(timeLabel, repeatingCalendarItem1.getCalendarTimeLabelKey());
		Assert.assertEquals(maxCount,repeatingCalendarItem1.getMaxCount());
		
		Assert.assertNotNull(repeatingCalendarItem1.getFirstTime());
		Assert.assertEquals(firstTime.getTime(),  repeatingCalendarItem1.getFirstTime().getTime());
		Assert.assertNotNull(repeatingCalendarItem1.getLastTime());
		Assert.assertEquals(lastTime.getTime(), repeatingCalendarItem1.getLastTime().getTime());
		
		Assert.assertNotNull(repeatingCalendarItem1.getContext());
		Assert.assertEquals(contextId, repeatingCalendarItem1.getContext().getContextId());
		
		Assert.assertNotNull(repeatingCalendarItem1.getSourceType());
		Assert.assertEquals(identifier1, repeatingCalendarItem1.getSourceType().getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve SourceType objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	@Test
	public void testAddSourceType() {
		String identifier = getUniqueIdentifier();
		SourceType sourceType = new SourceType(identifier);
		boolean saved = dao.addSourceType(sourceType);
		Assert.assertTrue(saved);
		
		sourceType = dao.getSourceType(identifier);
		Assert.assertNotNull(sourceType);
		Assert.assertNotNull(sourceType.getId());
		Assert.assertEquals(identifier,sourceType.getIdentifier());

		String identifier1 = getUniqueIdentifier();
		SourceType sourceType1 = new SourceType(identifier1);
		boolean saved1 = dao.addSourceType(sourceType1);
		Assert.assertTrue(saved1);
		
		sourceType1 = dao.getSourceType(identifier1);
		Assert.assertNotNull(sourceType1);
		Assert.assertNotNull(sourceType1.getId());
		Assert.assertEquals(identifier1,sourceType1.getIdentifier());
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and CalendarItem. It then deletes the CalendarItem and verifies 
	 * that the deleted item can not longer be retrieved.
     */
	@Test
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
		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		boolean removed = dao.deleteCalendarItem(calendarItem.getId());
		Assert.assertTrue(removed);
		calendarItem = dao.getCalendarItem(calendarItem.getId());
		Assert.assertNull(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		Assert.assertNull(calendarItem);		
	}

	@Test
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
		boolean saved1 = dao.addCalendarItem(calendarItem);
		Assert.assertTrue(saved1);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
		Assert.assertNotNull(calendarItem);
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		dao.addPerson(person);
		person = dao.getPersonBySakaiId(sakaiId);
		
		CalendarLink link = new CalendarLink(person, calendarItem, context, false, false);
		boolean linkSaved = dao.addCalendarLink(link);
		Assert.assertTrue(linkSaved);
		
		boolean showFuture = true;
		boolean showPast = true;
		boolean saved = false;
		boolean hidden = false;
		
		List<CalendarItem> items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		Assert.assertNotNull(items);
		Assert.assertTrue(items.size() > 0);
		
		boolean foundItem = false;
		List<Long> calendarItemIds = new ArrayList<Long>();
		for(CalendarItem item : items) {
			if(item.getEntityReference().equals(entityReference)) {
				// we have found the one and only link for this user to this item
				foundItem = true;
				calendarItemIds.add(item.getId());
				boolean deleted = dao.deleteCalendarLinks(item.getId());
				Assert.assertTrue(deleted);
			}
		}
		Assert.assertTrue(foundItem);
		Assert.assertTrue(calendarItemIds.size() > 0);
		
		items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		
		Assert.assertTrue(items.size() == 0);
	}

	@Test
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
		Assert.assertTrue(linkSaved);
		
		boolean showFuture = true;
		boolean showPast = true;
		boolean saved = false;
		boolean hidden = false;
		
		List<CalendarItem> items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		Assert.assertNotNull(items);
		Assert.assertTrue(items.size() > 0);
		
		boolean deleted = dao.deleteCalendarLinks(person.getId(), context.getId());
		Assert.assertTrue(deleted);
		items = dao.getCalendarItems(sakaiId, contextId, saved, hidden);
		
		Assert.assertTrue(items.size() == 0);
	}

    /**
     * This method actually depends on being able to save and retrieve objects of type
     * Context, SourceType and NewsItem. It then deletes the NewsItem and verifies 
	 * that the deleted item can not longer be retrieved.
     */
	@Test
	public void testDeleteNewsItem() {
		Long id;
	}

	@Test
	public void testDeleteNewsLinksLong() {
		Long newsItemId;
	}

	@Test
	public void testDeleteNewsLinksLongLong() {
		Long personId;
		Long contextId;
	}

	@Test
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
		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
		Assert.assertEquals(title, calendarItem.getTitle());
		Assert.assertEquals(entityReference, calendarItem.getEntityReference());
		Assert.assertEquals(calendarTime.getTime(), calendarItem.getCalendarTime().getTime());
		
		Assert.assertNotNull(calendarItem.getContext());
		Assert.assertEquals(contextId, calendarItem.getContext().getContextId());
		Assert.assertEquals(contextTitle, calendarItem.getContext().getContextTitle());
		Assert.assertEquals(contextUrl, calendarItem.getContext().getContextUrl());
		
		Assert.assertNotNull(calendarItem.getSourceType());
		Assert.assertEquals(sourceTypeIdentifier, calendarItem.getSourceType().getIdentifier());
	}

	@Test
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
		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
		Assert.assertEquals(title, calendarItem.getTitle());
		Assert.assertEquals(entityReference, calendarItem.getEntityReference());
		Assert.assertEquals(calendarTime.getTime(), calendarItem.getCalendarTime().getTime());
		
		Assert.assertNotNull(calendarItem.getContext());
		Assert.assertEquals(contextId, calendarItem.getContext().getContextId());
		Assert.assertEquals(contextTitle, calendarItem.getContext().getContextTitle());
		Assert.assertEquals(contextUrl, calendarItem.getContext().getContextUrl());
		
		Assert.assertNotNull(calendarItem.getSourceType());
		Assert.assertEquals(sourceTypeIdentifier, calendarItem.getSourceType().getIdentifier());	}

	@Test
	public void testGetCalendarItems() {
		String sakaiUserId;
		String contextId;
	}

	@Test
	public void testGetCalendarItemsByContext() {
		String contextId;
	}

	@Test
	public void testGetContextLong() {
		long id;
	}

	@Test
	public void testGetContextString() {
		String contextId;
	}

	@Test
	public void testGetNewsItemString() {
		String entityReference;
	}

	@Test
	public void testGetNewsItemLong() {
		long id;
	}

	@Test
	public void testGetNewsItems() {
		String sakaiUserId;
		String contextId;
	}

	@Test
	public void testGetNewsItemsByContext() {

	}

	@Test
	public void testGetPersonBySakaiId() {
		String sakaiId;
	}

	@Test
	public void testGetSourceType() {
		String identifier;
	}

	@Test
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
		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
//		String newTitle = getUniqueIdentifier();
//		Date newTime = new Date(System.currentTimeMillis() + (2L * ONE_DAY));
		
//		boolean updated = dao.updateCalendarItem(calendarItem.getId(), newTitle, newTime);
//		Assert.assertTrue(updated);
//		
//		calendarItem = dao.getCalendarItem(calendarItem.getId());
//
//		Assert.assertNotNull(calendarItem);
//		Assert.assertNotNull(calendarItem.getId());
//		
//		Assert.assertEquals(newTitle, calendarItem.getTitle());
//		
//		Assert.assertEquals(newTime.getTime(), calendarItem.getCalendarTime().getTime());
		//Assert.assertTrue(newTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
		//Assert.assertTrue(newTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
	}

	@Test
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
		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		Assert.assertNotNull(calendarItem);
		Assert.assertNotNull(calendarItem.getId());
		Assert.assertEquals(subtype1, calendarItem.getSubtype());
		
//		String subtype2 = getUniqueIdentifier();
//		
//		boolean updated = dao.updateCalendarItemSubtype(calendarItem.getId(), subtype2);
//		Assert.assertTrue(updated);
//		
//		CalendarItem revisedCalendarItem = dao.getCalendarItem(calendarItem.getId());
//
//		Assert.assertNotNull(revisedCalendarItem);
//		Assert.assertNotNull(revisedCalendarItem.getId());
//		
//		Assert.assertEquals(subtype2, revisedCalendarItem.getSubtype());
//		
	}

	@Test
	public void testUpdateCalendarItemTime() {
		Long id;
		Date newTime;
	}

	@Test
	public void testUpdateCalendarItemTitle() {
		Long id;
		String newTitle;
	}
	
	@Test
	public void testUpdateContextTitle() {
		String contextId = getUniqueIdentifier();
		String contextTitle1 = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle1, contextUrl );
		
		boolean saved = dao.addContext(context);
		Assert.assertTrue(saved);
		
		context = dao.getContext(contextId);		
		Assert.assertNotNull(context);
		Assert.assertNotNull(context.getId());
		Assert.assertEquals(contextId, context.getContextId());
		Assert.assertEquals(contextTitle1, context.getContextTitle());
		Assert.assertEquals(contextUrl, context.getContextUrl());
		
		String contextTitle2 = getUniqueIdentifier();
		boolean updated = dao.updateContextTitle(contextId, contextTitle2);
		
		Assert.assertTrue(updated);
		Context revisedContext = dao.getContext(contextId);	
		
		Assert.assertNotNull(revisedContext);
		Assert.assertNotNull(revisedContext.getId());
		Assert.assertEquals(contextId, revisedContext.getContextId());
		Assert.assertEquals(contextTitle2, revisedContext.getContextTitle());
		Assert.assertEquals(contextUrl, revisedContext.getContextUrl());
	}

	@Test
	public void testUpdateNewsItemTitle() {
		Long id;
		String newTitle;
	}

	@Test
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
		
		Assert.assertTrue(saved);
		
		NewsItem savedItem = dao.getNewsItem(entityReference);
		Assert.assertNotNull(savedItem);
		Assert.assertNotNull(savedItem.getId());
		Assert.assertTrue(savedItem.getId().longValue() >= 0L);
		
		Date newTime = new Date();
		String newGroupingIdentifier = getUniqueIdentifier();
		dao.updateNewsItemTime(savedItem.getId(), newTime, newGroupingIdentifier);
		NewsItem revisedItem = dao.getNewsItem(savedItem.getId());
		Assert.assertNotNull(revisedItem);
		Assert.assertEquals(newTime.getTime(), revisedItem.getNewsTime().getTime());
		
		
	}

	@Test
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
		
		Assert.assertTrue(saved);
		
		NewsItem savedItem = dao.getNewsItem(entityReference);
		Assert.assertNotNull(savedItem);
		Assert.assertNotNull(savedItem.getId());
		Assert.assertTrue(savedItem.getId().longValue() >= 0L);
		Assert.assertEquals(labelKey1, savedItem.getNewsTimeLabelKey());

		String labelKey2 = getUniqueIdentifier();
		Assert.assertFalse(labelKey1.equals(labelKey2));

//		String newGroupingIdentifier = getUniqueIdentifier();
//		dao.updateNewsItemLabelKey(savedItem.getId(), labelKey2, newGroupingIdentifier);
//		NewsItem revisedItem = dao.getNewsItem(savedItem.getId());
//		Assert.assertNotNull(revisedItem);
//		Assert.assertEquals(labelKey2, revisedItem.getNewsTimeLabelKey());
//		
		
	}

	@Test
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
		
		Assert.assertTrue(saved);
		
		NewsItem savedItem = dao.getNewsItem(entityReference);
		Assert.assertNotNull(savedItem);
		Assert.assertNotNull(savedItem.getId());
		Assert.assertTrue(savedItem.getId().longValue() >= 0L);
		Assert.assertEquals(subtype1, savedItem.getSubtype());

		String subtype2 = getUniqueIdentifier();
		Assert.assertFalse(subtype1.equals(subtype2));

//		Date newNewsTime = new Date();
//		
//		String newLabelKey = getUniqueIdentifier();
//		String newGroupingIdentifier = getUniqueIdentifier();
//		dao.updateNewsItemSubtype(savedItem.getId(), subtype2, newNewsTime, newLabelKey, newGroupingIdentifier);
//		NewsItem revisedItem = dao.getNewsItem(savedItem.getId());
//		Assert.assertNotNull(revisedItem);
//		Assert.assertEquals(subtype2, revisedItem.getSubtype());
//		Assert.assertEquals(newNewsTime, revisedItem.getNewsTime());
//		Assert.assertEquals(newLabelKey, revisedItem.getNewsTimeLabelKey());
//		
	}
	
	@Test
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
		Assert.assertNotNull(sourceType);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		Assert.assertNotNull(context);
		
		RepeatingCalendarItem repeatingCalendarItem = new RepeatingCalendarItem(title, firstTime, lastTime, 
				timeLabel, entityReference, subtype1, context, sourceType, frequency, maxCount);
		boolean savedItem = dao.addRepeatingCalendarItem(repeatingCalendarItem);
		
		Assert.assertTrue(savedItem);
		
		RepeatingCalendarItem repeatingCalendarItem1 = dao.getRepeatingCalendarItem(entityReference, timeLabel);
		
		Assert.assertNotNull(repeatingCalendarItem1);
		Assert.assertNotNull(repeatingCalendarItem1.getId());
		
//		String subtype2 = getUniqueIdentifier();
//		boolean updated = dao.updateRepeatingCalendarItemsSubtype(entityReference, timeLabel, subtype2);
//		
//		Assert.assertTrue(updated);
//		
//		RepeatingCalendarItem repeatingCalendarItem2 = dao.getRepeatingCalendarItem(entityReference, timeLabel);
//		
//		Assert.assertNotNull(repeatingCalendarItem2);
//		Assert.assertNotNull(repeatingCalendarItem2.getId());
//		
//		Assert.assertEquals(subtype2, repeatingCalendarItem2.getSubtype());

	}
	
	@Test
	public void testDeleteCalendarItemsWithoutLinks() {
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		Assert.assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		Assert.assertNotNull(person);
		
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
			Assert.assertTrue(saved);
			calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
			Assert.assertNotNull(calendarItem);
			Assert.assertNotNull(calendarItem.getId());
			
			if(i % 2 == 1 || i % 2 == -1) {
				CalendarLink link = new CalendarLink(person,calendarItem,context,false,false);
				boolean linkSaved = dao.addCalendarLink(link);
				Assert.assertTrue(linkSaved);
				link = dao.getCalendarLink(calendarItem.getId(), person.getId());
				Assert.assertNotNull(link);
				itemsWithLinks++;
			}
			totalItems++;
		}
		
		List<CalendarItem> before = dao.getCalendarItems(entityReference);
		Assert.assertNotNull(before);
		Assert.assertEquals(totalItems, before.size());
		
		boolean linksDeleted = dao.deleteCalendarItemsWithoutLinks();
		Assert.assertTrue(linksDeleted);
		
		List<CalendarItem> after = dao.getCalendarItems(entityReference);
		Assert.assertNotNull(after);
		Assert.assertEquals(itemsWithLinks, after.size());
		
	}
	
	@Test
	public void testDeleteCalendarLinksBefore() {
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		
		Assert.assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		Assert.assertNotNull(person);
		
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
			Assert.assertTrue(saved);
			calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey, null);
			Assert.assertNotNull(calendarItem);
			Assert.assertNotNull(calendarItem.getId());
			
			boolean hidden = i % 2 == 1;
			boolean sticky = i % 3 == 1;
			CalendarLink link = new CalendarLink(person,calendarItem,context,hidden,sticky);
			boolean linkSaved = dao.addCalendarLink(link);
			Assert.assertTrue(linkSaved);
			link = dao.getCalendarLink(calendarItem.getId(), person.getId());
			Assert.assertNotNull(link);

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
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());
		
		links = dao.getStarredCalendarLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(stickyCount, links.size());
		
		links = dao.getFutureCalendarLinks(sakaiId, contextId, true);
		Assert.assertNotNull(links);
		Assert.assertEquals(hiddenCount, links.size());
		
		expectedCount = expectedCount - notStickyNotHiddenCount1;
		dao.deleteCalendarLinksBefore(deleteDate1, false, false);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());

		expectedCount = expectedCount - stickyNotHiddenCount1;
		dao.deleteCalendarLinksBefore(deleteDate1, true, false);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());
		
		links = dao.getStarredCalendarLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(stickyCount - stickyNotHiddenCount1, links.size());
		
		dao.deleteCalendarLinksBefore(deleteDate1, false, true);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, true);
		Assert.assertNotNull(links);
		Assert.assertEquals(hiddenCount - hiddenNotStickyCount1, links.size());

		dao.deleteCalendarLinksBefore(deleteDate1, true, true);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, true);
		Assert.assertNotNull(links);
		Assert.assertEquals(hiddenCount - hiddenNotStickyCount1 - stickyAndHiddenCount1, links.size());
		
		expectedCount = expectedCount - stickyNotHiddenCount2;
		dao.deleteCalendarLinksBefore(deleteDate2, true, false);
		links = dao.getFutureCalendarLinks(sakaiId, contextId, false);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());	
		
	}

	@Test
	public void testDeleteNewsItemsWithoutLinks() {
		
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		
		Assert.assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		Assert.assertNotNull(person);
		
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
			Assert.assertTrue(saved);
			newsItem = dao.getNewsItem(entityReference);
			Assert.assertNotNull(newsItem);
			Assert.assertNotNull(newsItem.getId());
			
			if(i % 2 == 1 || i % 2 == -1) {
				NewsLink link = new NewsLink(person,newsItem,context,false,false);
				boolean linkSaved = dao.addNewsLink(link);
				Assert.assertTrue(linkSaved);
				link = dao.getNewsLink(newsItem.getId(), person.getId());
				Assert.assertNotNull(link);
				itemsWithLinks++;
			}
			totalItems++;
		}
		
		List<NewsItem> before = dao.getNewsItemsByContext(context.getContextId());
		Assert.assertNotNull(before);
		Assert.assertEquals(totalItems, before.size());
		
		boolean linksDeleted = dao.deleteNewsItemsWithoutLinks();
		Assert.assertTrue(linksDeleted);
		
		List<NewsItem> after = dao.getNewsItemsByContext(context.getContextId());
		Assert.assertNotNull(after);
		Assert.assertEquals(itemsWithLinks, after.size());
	}
	
	@Test
	public void testDeleteNewsLinksBefore() {
		String sakaiId = getUniqueIdentifier();
		String userId = getUniqueIdentifier();
		Person person = new Person(sakaiId, userId);
		boolean personSaved = dao.addPerson(person);
		
		Assert.assertTrue(personSaved);
		person = dao.getPersonBySakaiId(sakaiId);
		Assert.assertNotNull(person);
		
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
			Assert.assertTrue(saved);
			newsItem = dao.getNewsItem(entityReference);
			Assert.assertNotNull(newsItem);
			Assert.assertNotNull(newsItem.getId());
			
			boolean hidden = i % 2 == 1;
			boolean sticky = i % 3 == 1;
			NewsLink link = new NewsLink(person,newsItem,context,hidden,sticky);
			boolean linkSaved = dao.addNewsLink(link);
			Assert.assertTrue(linkSaved);
			link = dao.getNewsLink(newsItem.getId(), person.getId());
			Assert.assertNotNull(link);

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
		Assert.assertNotNull(items);
		Assert.assertEquals(totalCount, items.size());
		
		int expectedCount = totalCount - hiddenCount;
		List<NewsLink> links = dao.getCurrentNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());
		
		links = dao.getStarredNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(stickyCount, links.size());
		
		links = dao.getHiddenNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(hiddenCount, links.size());
		
		expectedCount = expectedCount - notStickyNotHiddenCount1;
		dao.deleteNewsLinksBefore(deleteDate1, false, false);
		links = dao.getCurrentNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());

		expectedCount = expectedCount - stickyNotHiddenCount1;
		dao.deleteNewsLinksBefore(deleteDate1, true, false);
		links = dao.getCurrentNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());
		
		links = dao.getStarredNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(stickyCount - stickyNotHiddenCount1, links.size());
		
		dao.deleteNewsLinksBefore(deleteDate1, false, true);
		links = dao.getHiddenNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(hiddenCount - hiddenNotStickyCount1, links.size());

		dao.deleteNewsLinksBefore(deleteDate1, true, true);
		links = dao.getHiddenNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(hiddenCount - hiddenNotStickyCount1 - stickyAndHiddenCount1, links.size());
		
		expectedCount = expectedCount - stickyNotHiddenCount2;
		dao.deleteNewsLinksBefore(deleteDate2, true, false);
		links = dao.getCurrentNewsLinks(sakaiId, contextId);
		Assert.assertNotNull(links);
		Assert.assertEquals(expectedCount, links.size());	
	}

	@Test
	public void testAddTaskLock() {
		// boolean addTaskLock(TaskLock taskLock)
		boolean saved = false;
		try {
			String task = getUniqueIdentifier();
			boolean hasLock = false;
			String serverId = this.getUniqueIdentifier();
			Date lastUpdate = new Date();
			Date claimTime = new Date();
			TaskLock taskLock = new TaskLock(task, serverId , claimTime, hasLock, lastUpdate);
			
			saved = dao.addTaskLock(taskLock);
		} catch(Throwable t) {
			Assert.fail("Failure while trying to create and save a task-lock");
		}
		
		Assert.assertTrue(saved);
	}
	
	@Test
	public void testGetTaskLocks() {
		String task00 = getUniqueIdentifier();
		
		List<TaskLock> locks = dao.getTaskLocks(task00);
		Assert.assertNotNull(locks);
		Assert.assertEquals(0, locks.size());
		
		String serverId00 = this.getUniqueIdentifier();
		Date time00 = new Date();
		TaskLock taskLock00 = new TaskLock(task00, serverId00 , time00, false, time00);
		
		boolean saved00 = dao.addTaskLock(taskLock00);
		Assert.assertTrue(saved00);
		
		List<TaskLock> locks01 = dao.getTaskLocks(task00);
		Assert.assertNotNull(locks01);
		Assert.assertEquals(1, locks01.size());
		
		String task01 = getUniqueIdentifier();
		Date time01 = new Date();
		TaskLock taskLock01 = new TaskLock(task01, serverId00, time01 , false, time01);
		
		boolean saved01 = dao.addTaskLock(taskLock01);
		Assert.assertTrue(saved01);
		
		List<TaskLock> locks02 = dao.getTaskLocks(task01);
		Assert.assertNotNull(locks02);
		Assert.assertEquals(1, locks02.size());
		
		String serverId02 = getUniqueIdentifier();
		Date time02 = new Date();
		TaskLock taskLock02 = new TaskLock(task00, serverId02, time02, false, time02);
		
		boolean saved02 = dao.addTaskLock(taskLock02);
		Assert.assertTrue(saved02);

		List<TaskLock> locks03 = dao.getTaskLocks(task00);
		Assert.assertNotNull(locks03);
		Assert.assertEquals(2, locks03.size());

	}
	 
	@Test
	public void testDeleteTaskLocks() {
		// boolean deleteTaskLocks(String task)
		
		String task00 = getUniqueIdentifier();
		String serverId00 = this.getUniqueIdentifier();
		Date time00 = new Date();
		TaskLock taskLock00 = new TaskLock(task00, serverId00 , time00, false, time00);
		
		boolean saved00 = dao.addTaskLock(taskLock00);
		Assert.assertTrue(saved00);
		
		String task01 = getUniqueIdentifier();
		Date time01 = new Date();
		TaskLock taskLock01 = new TaskLock(task01, serverId00, time01 , false, time01);
		
		boolean saved01 = dao.addTaskLock(taskLock01);
		Assert.assertTrue(saved01);
				
		String serverId02 = getUniqueIdentifier();
		Date time02 = new Date();
		TaskLock taskLock02 = new TaskLock(task00, serverId02, time02, false, time02);
		
		boolean saved02 = dao.addTaskLock(taskLock02);
		Assert.assertTrue(saved02);

		List<TaskLock> locks00 = dao.getTaskLocks(task00);
		Assert.assertNotNull(locks00);
		Assert.assertEquals(2, locks00.size());

		List<TaskLock> locks01 = dao.getTaskLocks(task01);
		Assert.assertNotNull(locks01);
		Assert.assertEquals(1, locks01.size());
		
		boolean deleted00 = dao.deleteTaskLocks(task00);
		Assert.assertTrue(deleted00);
		
		List<TaskLock> locks02 = dao.getTaskLocks(task00);
		Assert.assertNotNull(locks02);
		Assert.assertEquals(0, locks02.size());

		List<TaskLock> locks03 = dao.getTaskLocks(task01);
		Assert.assertNotNull(locks03);
		Assert.assertEquals(1, locks03.size());
		
		boolean deleted01 = dao.deleteTaskLocks(task00);
		Assert.assertTrue(deleted01);
		
		boolean deleted02 = dao.deleteTaskLocks(task01);
		Assert.assertTrue(deleted02);
		
		List<TaskLock> locks04 = dao.getTaskLocks(task00);
		Assert.assertNotNull(locks04);
		Assert.assertEquals(0, locks04.size());

		List<TaskLock> locks05 = dao.getTaskLocks(task01);
		Assert.assertNotNull(locks05);
		Assert.assertEquals(0, locks05.size());
		
	}
	
	@Test
	public void testUpdateTaskLockStringStringDate() {
		// boolean updateTaskLock(String task, String serverId, Date lastUpdate)
		String task = getUniqueIdentifier();
		boolean hasLock = false;
		String serverId = this.getUniqueIdentifier();
		Date time00 = new Date();
		TaskLock taskLock = new TaskLock(task, serverId , time00, hasLock, time00);
		
		boolean saved = dao.addTaskLock(taskLock);
		Assert.assertTrue(saved);
		
		List<TaskLock> locks00 = dao.getTaskLocks(task);
		Assert.assertNotNull(locks00);
		Assert.assertEquals(1, locks00.size());
		
		TaskLock savedLock = locks00.get(0);
		Assert.assertNotNull(savedLock);
		Assert.assertNotNull(savedLock.getId());
		
		Assert.assertEquals(time00.getTime(), savedLock.getClaimTime().getTime());
		Assert.assertEquals(time00.getTime(), savedLock.getLastUpdate().getTime());
		
		long tasklockId = savedLock.getId().longValue();
		Date time01 = new Date(time00.getTime() + 10000L);
		
		boolean updated = dao.updateTaskLock(task, serverId, time01);
		
		Assert.assertTrue(updated);
		
		List<TaskLock> locks01 = dao.getTaskLocks(task);
		Assert.assertNotNull(locks01);
		Assert.assertEquals(1, locks01.size());
		
		TaskLock updatedLock = locks01.get(0);
		Assert.assertNotNull(updatedLock);
		Assert.assertNotNull(updatedLock.getId());
		Assert.assertEquals(tasklockId, updatedLock.getId().longValue());
		Assert.assertFalse(updatedLock.isHasLock());
		
		Assert.assertEquals(time00.getTime(), updatedLock.getClaimTime().getTime());
		Assert.assertEquals(time01.getTime(), updatedLock.getLastUpdate().getTime());
		
	}
	
	@Test
	public void testUpdateTaskLockLongBooleanDate() {
		// boolean updateTaskLock(long id, boolean hasLock, Date lastUpdate)
		String task = getUniqueIdentifier();
		boolean hasLock = false;
		String serverId = this.getUniqueIdentifier();
		Date time00 = new Date();
		TaskLock taskLock = new TaskLock(task, serverId , time00, hasLock, time00);
		
		boolean saved = dao.addTaskLock(taskLock);
		Assert.assertTrue(saved);
		
		List<TaskLock> locks00 = dao.getTaskLocks(task);
		Assert.assertNotNull(locks00);
		Assert.assertEquals(1, locks00.size());
		
		TaskLock savedLock = locks00.get(0);
		Assert.assertNotNull(savedLock);
		Assert.assertNotNull(savedLock.getId());
		Assert.assertFalse(savedLock.isHasLock());
		
		Assert.assertEquals(time00.getTime(), savedLock.getClaimTime().getTime());
		Assert.assertEquals(time00.getTime(), savedLock.getLastUpdate().getTime());
		
		long tasklockId = savedLock.getId().longValue();
		Date time01 = new Date(time00.getTime() + 10000L);
		
		boolean updated = dao.updateTaskLock(tasklockId, true, time01);
		
		Assert.assertTrue(updated);
		
		List<TaskLock> locks01 = dao.getTaskLocks(task);
		Assert.assertNotNull(locks01);
		Assert.assertEquals(1, locks01.size());
		
		TaskLock updatedLock = locks01.get(0);
		Assert.assertNotNull(updatedLock);
		Assert.assertNotNull(updatedLock.getId());
		Assert.assertEquals(tasklockId, updatedLock.getId().longValue());
		Assert.assertTrue(updatedLock.isHasLock());
		
		Assert.assertEquals(time00.getTime(), updatedLock.getClaimTime().getTime());
		Assert.assertEquals(time01.getTime(), updatedLock.getLastUpdate().getTime());
	}

	protected String getUniqueIdentifier() {
		return "unique-identifier-" + counter.incrementAndGet();
	}

	public void setDashboardDao(DashboardDao dashboardDao) {
		dao = dashboardDao;
		
	}
}
