package org.sakaiproject.dash.test;

import java.util.Date;
import java.util.List;

import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * @author jimeng
 *
 */
public class DashboardLogicTest extends AbstractTransactionalSpringContextTests {
	
	public DashboardLogicTest() {
		super();
		this.setDependencyCheck(false);
	}

	public void testAddCalendarLinks() {
		String sakaiUserIs;
		String contextId;
	}

	public void testAddNewsLinks() {
		String sakaiUserId;
		String contextId;
	}

	public void testCreateCalendarItem() {
		String title; 
		Date calendarTime;
		String entityReference;
		String entityUrl;
		Context context;
		SourceType sourceType;
	}

	public void testCreateCalendarLinks() {
		CalendarItem calendarItem;
	}

	public void testCreateContext() {
		String contextId;
	}

	public void testCreateNewsItem() {
		String title;
		Date newsTime;
		String entityReference;
		String entityUrl;
		Context context;
		SourceType sourceType;
	}

	public void testCreateNewsLinks() {
		NewsItem newsItem;
	}

	public void testCreateSourceType() {
		String identifier;
		String accessPermission;
	}

	public void testGetCalendarItem() {
		long id;
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

	public void testRemoveNewsItem() {
		String entityReference;
	}

	public void testRemoveCalendarLinksString() {
		String entityReference;
	}

	public void testRemoveCalendarLinksStringString() {
		String sakaiUserId;
		String contextId;
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
