package org.sakaiproject.dash.test;

import java.util.Date;

import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * 
 *
 */
public class DashboardLogicTest extends AbstractTransactionalSpringContextTests 
{
	protected SakaiProxy sakaiProxy;
	
	public DashboardLogicTest() {
		super();
		this.setDependencyCheck(false);
	}

	public void testAddCalendarLinks() {
		String sakaiUserId;
		String contextId;
		
		// user belongs to first context and not second
		// add items to both contexts
		// confirm that user can see items in first context and not second
		// add user to second context
		// confirm that user can see items in first *AND* second context
		
	}

	public void testAddNewsLinks() {
		String sakaiUserId;
		String contextId;

		// user belongs to first context and not second
		// add items to both contexts
		// confirm that user can see items in first context and not second
		// add user to second context
		// confirm that user can see items in first *AND* second context

	}

	public void testCreateCalendarItem() {
		String title; 
		Date calendarTime;
		String entityReference;
		String entityUrl;
		Context context;
		SourceType sourceType;
		
		// add a calendar item
		// retrieve calendar item  
		// confirm that its properties are correct
	}

	public void testCreateCalendarLinks() {
		CalendarItem calendarItem;
		
		// add a calendar item
		// supply a small roster for a context by way of the SakaiProxyMock
		// call DashboardLogic.createCalendarLinks(calendarItem)
		// confirm that the calendar links were created for the item
	}

	public void testCreateContext() {
		String contextId;
		
		// create and save a Context object
		// retrieve the Context object
		// confirm that its properties are correct
	}

	public void testCreateNewsItem() {
		String title;
		Date newsTime;
		String entityReference;
		String entityUrl;
		Context context;
		SourceType sourceType;
		
		// add a news item
		// retrieve news item  
		// confirm that its properties are correct
	}

	public void testCreateNewsLinks() {
		NewsItem newsItem;
		
		// add a news item
		// supply a small roster for a context by way of the SakaiProxyMock
		// call DashboardLogic.createNewsLinks(newsItem)
		// confirm that the news links were created for the item
	}

	public void testCreateSourceType() {
		String identifier;
		String accessPermission;

		// create and save a SourceType object
		// retrieve the SourceType object
		// confirm that its properties are correct
	}

	public void testGetCalendarItem() {
		long id;
		
		// add a calendar item
		// retrieve calendar item  
		// confirm that it can be retrieved by its id
	}

	public void testGetCalendarItemsString() {
		String sakaiUserId;
	}

	public void testGetCalendarItemsStringString() {
		String sakaiUserId;
		String contextId;
	}

	public void testGetContext() {
		String contextId;
	}

	public void testGetNewsItem() {
		long id;
	}

	public void testGetNewsItemsString() {
		String sakaiUserId;
	}

	public void testGetNewsItemsStringString() {
		String sakaiUserId;
		String contextId;
	}

	public void testGetSourceType() {
		String identifier;
	}

	public void testRegisterEventProcessor() {
		EventProcessor eventProcessor;
	}

	public void testRemoveCalendarItem() {
		String entityReference;
	}

	public void testRemoveCalendarLinksString() {
		String entityReference;
	}

	public void testRemoveCalendarLinksStringString() {
		String sakaiUserId;
		String contextId;
	}

	public void testRemoveNewsItem() {
		String entityReference;
	}

	public void testRemoveNewsLinksString() {
		String entityReference;
	}

	public void testRemoveNewsLinksStringString() {
		String sakaiUserId;
		String contextId;
	}

	public void testReviseCalendarItem() {
		String entityReference;
		String newTitle;
		Date newTime;
	}

	public void testReviseCalendarItemTime() {
		String entityReference;
		Date newTime;
	}

	public void testReviseCalendarItemTitle() {
		String entityReference;
		String newTitle;
	}

	public void testReviseNewsItemTitle() {
		String entityReference;
		String newTitle;
	}

}
