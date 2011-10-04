/**
 * 
 */
package org.sakaiproject.dash.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.entity.EntityLinkStrategy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * This class really only tests the HSQLDB impl. But that provides a baseline for testing 
 * of the logic methods.
 */
public class DashboardDaoTest extends AbstractTransactionalSpringContextTests {
	
	protected DashboardDao dao;
	
	protected static AtomicInteger counter = new AtomicInteger(999);

	private static final long ONE_DAY = 1000L * 60L * 60L * 24L;
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		boolean saved = dao.addCalendarItem(calendarItem);
		
		assertTrue(saved);
		
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(calendarTimeLabelKey,calendarItem.getCalendarTimeLabelKey());
		assertEquals(title, calendarItem.getTitle());
		assertEquals(entityUrl, calendarItem.getEntityUrl());
		assertEquals(entityReference, calendarItem.getEntityReference());
		
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		
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
		for(CalendarItem item : items) {
			if(item.getEntityReference().equals(entityReference)) {
				// we have found the one and only link for this user to this item
				foundItem = true;
				assertEquals(title, item.getTitle());
				assertEquals(entityUrl,item.getEntityUrl());
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL);
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			entityReference, entityUrl, context, sourceType);
		boolean saved = dao.addNewsItem(newsItem);
		
		assertTrue(saved);
		
		newsItem = dao.getNewsItem(entityReference);

		assertNotNull(newsItem);
		assertNotNull(newsItem.getId());
		
		assertEquals(title, newsItem.getTitle());
		assertEquals(entityUrl, newsItem.getEntityUrl());
		assertEquals(entityReference, newsItem.getEntityReference());

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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		NewsItem newsItem = new NewsItem(title, eventTime,
			entityReference, entityUrl, context, sourceType);
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
		
		boolean saved = false;
		boolean hidden = false;
		
		List<NewsItem> items = dao.getNewsItems(sakaiId, contextId, saved, hidden);
		assertNotNull(items);
		assertTrue(items.size() > 0);
		
		boolean foundItem = false;
		for(NewsItem item : items) {
			if(item.getEntityReference().equals(entityReference)) {
				// we have found the one and only link for this user to this item
				foundItem = true;
				assertEquals(title, item.getTitle());
				assertEquals(entityUrl,item.getEntityUrl());
				assertNotNull(item.getContext());
				assertEquals(contextId,item.getContext().getContextId());
				assertNotNull(item.getSourceType());
				assertEquals(sourceTypeIdentifier,item.getSourceType().getIdentifier());
				assertEquals(eventTime.getTime(), item.getNewsTime().getTime());
				break;
			}
		}
		assertTrue(foundItem);
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

    /**
     * This method actually depends on being able to save and retrieve SourceType objects. 
     * It then verifies that the retrieved items have the same attribute values as the 
     * items that were saved.
     */
	public void testAddSourceType() {
		String identifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(identifier,accessPermission, EntityLinkStrategy.ACCESS_URL);
		boolean saved = dao.addSourceType(sourceType);
		assertTrue(saved);
		
		sourceType = dao.getSourceType(identifier);
		assertNotNull(sourceType);
		assertNotNull(sourceType.getId());
		assertEquals(identifier,sourceType.getIdentifier());
		assertEquals(accessPermission,sourceType.getAccessPermission());
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		boolean removed = dao.deleteCalendarItem(calendarItem.getId());
		assertTrue(removed);
		calendarItem = dao.getCalendarItem(calendarItem.getId());
		assertNull(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		assertNull(calendarItem);		
	}

	public void testDeleteCalendarLinksLong() {
		Date calendarTime = new Date(System.currentTimeMillis() + ONE_DAY);
		String calendarTimeLabelKey = getUniqueIdentifier();
		String title = getUniqueIdentifier();
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(title, calendarItem.getTitle());
		assertEquals(entityUrl, calendarItem.getEntityUrl());
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(title, calendarItem.getTitle());
		assertEquals(entityUrl, calendarItem.getEntityUrl());
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
		String contextId;
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
		String entityUrl = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		dao.addContext(context);
		context = dao.getContext(contextId);
		
		String sourceTypeIdentifier = getUniqueIdentifier();
		String accessPermission = getUniqueIdentifier();
		SourceType sourceType = new SourceType(sourceTypeIdentifier, accessPermission, EntityLinkStrategy.ACCESS_URL );
		dao.addSourceType(sourceType);
		sourceType = dao.getSourceType(sourceTypeIdentifier);

		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		boolean saved = dao.addCalendarItem(calendarItem);
		calendarItem = dao.getCalendarItem(entityReference, calendarTimeLabelKey);
		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		String newTitle = getUniqueIdentifier();
		Date newTime = new Date(System.currentTimeMillis() + (2L * ONE_DAY));
		
		boolean updated = dao.updateCalendarItem(calendarItem.getId(), newTitle, newTime);
		assertTrue(updated);
		
		calendarItem = dao.getCalendarItem(calendarItem.getId());

		assertNotNull(calendarItem);
		assertNotNull(calendarItem.getId());
		
		assertEquals(newTitle, calendarItem.getTitle());
		
		assertEquals(newTime.getTime(), calendarItem.getCalendarTime().getTime());
		//assertTrue(newTime.getTime() + TIME_DELTA > calendarItem.getCalendarTime().getTime());
		//assertTrue(newTime.getTime() - TIME_DELTA < calendarItem.getCalendarTime().getTime());
	}

	public void testUpdateCalendarItemTime() {
		Long id;
		Date newTime;
	}

	public void testUpdateCalendarItemTitle() {
		Long id;
		String newTitle;
	}

	public void testUpdateNewsItemTitle() {
		Long id;
		String newTitle;
	}

	protected String getUniqueIdentifier() {
		return "unique-identifier-" + counter.incrementAndGet();
	}

	public void setDashboardDao(DashboardDao dashboardDao) {
		this.dao = dashboardDao;
		
	}
}
