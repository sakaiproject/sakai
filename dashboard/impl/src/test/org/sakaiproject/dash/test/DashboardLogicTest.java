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

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardCommonLogicImpl;
import org.sakaiproject.dash.logic.DashboardConfigImpl;
import org.sakaiproject.dash.logic.DashboardLogicImpl;
import org.sakaiproject.dash.mock.DashboardDaoMock;
import org.sakaiproject.dash.mock.MockTransactionManager;
import org.sakaiproject.dash.mock.SakaiProxyMock;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 
 *
 */
public class DashboardLogicTest
{

	protected static AtomicInteger counter = new AtomicInteger(999);
	
	private static final long ONE_MINUTE = 1000L * 60L;
	private static final long ONE_HOUR = ONE_MINUTE * 60L;
	private static final long ONE_DAY = ONE_HOUR * 24L;
	private static final long ONE_WEEK = ONE_DAY * 7L;

	protected SakaiProxy sakaiProxy;
	protected DashboardCommonLogic dashboardCommonLogic;

	/**
	 * @param sakaiProxy the sakaiProxy to set
	 */
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}

	/**
	 * @param dashboardCommonLogic the dashboardCommonLogic to set
	 */
	public void setDashboardLogic(DashboardCommonLogic dashboardCommonLogic) {
		this.dashboardCommonLogic = dashboardCommonLogic;
	}

	@Test
	public void testAddCalendarLinks() {
		String sakaiUserId;
		String contextId;
		
		// user belongs to first context and not second
		// add items to both contexts
		// confirm that user can see items in first context and not second
		// add user to second context
		// confirm that user can see items in first *AND* second context
		
	}

	@Test
	public void testAddNewsLinks() {
		String sakaiUserId;
		String contextId;

		// user belongs to first context and not second
		// add items to both contexts
		// confirm that user can see items in first context and not second
		// add user to second context
		// confirm that user can see items in first *AND* second context

	}

	@Test
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

	@Test
	public void testCreateCalendarLinks() {
		CalendarItem calendarItem;
		
		// add a calendar item
		// supply a small roster for a context by way of the SakaiProxyMock
		// call DashboardCommonLogic.createCalendarLinks(calendarItem)
		// confirm that the calendar links were created for the item
	}

	@Test
	public void testCreateContext() {
		this.sakaiProxy = new SakaiProxyMock();
		DashboardDao dao = new DashboardDaoMock();
		this.dashboardCommonLogic = new DashboardCommonLogicImpl();
		PlatformTransactionManager txManager = new MockTransactionManager();
		DashboardLogicImpl dashboardLogic = new DashboardLogicImpl(txManager );
		dashboardLogic.setDao(dao);
		DashboardConfig dashboardConfig = new DashboardConfigImpl();
		dashboardLogic.setDashboardConfig(dashboardConfig);
		dashboardLogic.setSakaiProxy(sakaiProxy);
		((DashboardCommonLogicImpl) this.dashboardCommonLogic).setDashboardLogic(dashboardLogic );
		((DashboardCommonLogicImpl) this.dashboardCommonLogic).setSakaiProxy(sakaiProxy);
		((DashboardCommonLogicImpl) this.dashboardCommonLogic).setDao(dao);
		
		String validContextId = SakaiProxyMock.VALID_SITE_ID;
		
		// create and save a Context object for a valid siteId
		Context validContext = this.dashboardCommonLogic.createContext(validContextId);
		Assert.assertNotNull(validContext);
		// retrieve the Context object
		Context c1 = this.dashboardCommonLogic.getContext(validContextId);
		// confirm that its properties are correct
		Assert.assertNotNull(c1);
		Assert.assertEquals(validContextId, c1.getContextId());
		
		String bogusContextId = SakaiProxyMock.BOGUS_SITE_ID;
		
		// try creating and saving a Context object for an invalid siteId
		Context bogusContext = this.dashboardCommonLogic.createContext(bogusContextId);
		
		Assert.assertNull(bogusContext);
		
		Context c2 = this.dashboardCommonLogic.getContext(bogusContextId);
		
		Assert.assertNull(c2);
	}

	@Test
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

	@Test
	public void testCreateNewsLinks() {
		NewsItem newsItem;
		
		// add a news item
		// supply a small roster for a context by way of the SakaiProxyMock
		// call DashboardCommonLogic.createNewsLinks(newsItem)
		// confirm that the news links were created for the item
	}

	@Test
	public void testCreateSourceType() {
		String identifier;

		// create and save a SourceType object
		// retrieve the SourceType object
		// confirm that its properties are correct
	}

	@Test
	public void testGetCalendarItem() {
		long id;
		
		// add a calendar item
		// retrieve calendar item  
		// confirm that it can be retrieved by its id
	}

	@Test
	public void testGetCalendarItemsString() {
		String sakaiUserId;
	}

	@Test
	public void testGetCalendarItemsStringString() {
		String sakaiUserId;
		String contextId;
	}

	@Test
	public void testGetContext() {
		String contextId;
	}

	@Test
	public void testGetNewsItem() {
		long id;
	}

	@Test
	public void testGetNewsItemsString() {
		String sakaiUserId;
	}

	@Test
	public void testGetNewsItemsStringString() {
		String sakaiUserId;
		String contextId;
	}

	@Test
	public void testGetSourceType() {
		String identifier;
	}

	@Test
	public void testRegisterEventProcessor() {
		EventProcessor eventProcessor;
	}

	@Test
	public void testRemoveCalendarItem() {
		String entityReference;
	}

	@Test
	public void testRemoveCalendarLinksString() {
		String entityReference;
	}

	@Test
	public void testRemoveCalendarLinksStringString() {
		String sakaiUserId;
		String contextId;
	}

	@Test
	public void testRemoveNewsItem() {
		String entityReference;
	}

	@Test
	public void testRemoveNewsLinksString() {
		String entityReference;
	}

	@Test
	public void testRemoveNewsLinksStringString() {
		String sakaiUserId;
		String contextId;
	}

	@Test
	public void testReviseCalendarItem() {
		String entityReference;
		String newTitle;
		Date newTime;
	}

	@Test
	public void testReviseCalendarItemTime() {
		String entityReference;
		Date newTime;
	}

	@Test
	public void testReviseCalendarItemTitle() {
		String entityReference;
		String newTitle;
	}

	@Test
	public void testReviseNewsItemTitle() {
		String entityReference;
		String newTitle;
	}
	
	@Test
	public void testGenerateCalendarItems() {
		
		String title = getUniqueIdentifier();
		String entityReference = getUniqueIdentifier();
		String frequency = RepeatingCalendarItem.REPEATS_DAILY;
		String timeLabel = getUniqueIdentifier();
		Date firstTime = new Date();
		Date lastTime = new Date(firstTime.getTime() + ONE_WEEK + ONE_HOUR);
		int maxCount = 5;
		
		String identifier1 = getUniqueIdentifier();
		SourceType sourceType = new  SourceType(identifier1);
		
		String contextId = getUniqueIdentifier();
		String contextTitle = getUniqueIdentifier();
		String contextUrl = getUniqueIdentifier();
		Context context = new Context(contextId, contextTitle, contextUrl );
		
		RepeatingCalendarItem repeatingCalendarItem = new RepeatingCalendarItem(title, firstTime, lastTime, timeLabel, entityReference, null, context, sourceType, frequency, maxCount);
		
		//List<CalendarItem> items = repeatingCalendarItem.generateCalendarItems(lastTime);
		//assertNotNull(items);
		//assertFalse(items.isEmpty());
		
	}
	
	protected String getUniqueIdentifier() {
		return "unique-identifier-" + counter.incrementAndGet();
	}


}
