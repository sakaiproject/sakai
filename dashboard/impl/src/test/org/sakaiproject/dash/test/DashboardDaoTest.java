/**
 * 
 */
package org.sakaiproject.dash.test;

import java.util.Date;

import org.sakaiproject.dash.dao.DashboardDao;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.SourceType;

import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * 
 *
 */
public class DashboardDaoTest extends AbstractTransactionalSpringContextTests {
	
	protected DashboardDao dao;
	
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

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */

	public void testAddCalendarItem() {
		CalendarItem calendarItem;
		
		assertTrue(true);
	}

	public void testAddCalendarLink() {
		CalendarLink calendarLink;
	}

	public void testAddContext() {
		Context context;
	}

	public void testAddNewsItem() {
		NewsItem newsItem;
	}

	public void testAddNewsLink() {
		NewsLink newsLink;
	}

	public void testAddPerson() {
		Person person;
	}

	public void testAddSourceType() {
		SourceType identifier;
	}

	public void testDeleteCalendarItem() {
		Long id;
	}

	public void testDeleteCalendarLinksLong() {
		Long calendarItemId;
	}

	public void testDeleteCalendarLinksLongLong() {
		Long personId;
		Long contextId;
	}

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
		long id;
	}

	public void testGetCalendarItemString() {
		String entityReference;
	}

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
		Long id;
		String newTitle;
		Date newTime;
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

	

}
