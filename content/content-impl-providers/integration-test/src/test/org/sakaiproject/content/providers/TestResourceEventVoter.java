/**
 * Copyright (c) 2003-2008 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.providers;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class TestResourceEventVoter extends SakaiTestBase
{
	private ResourceEventVoter voter;
	private ResourceEventVoterHelper helper;
	private ContentHostingService chs;
	private Event event;
	private UsageSessionService usageSessionService;
	private SessionManager sessionManager;
	private Session session;
	private TimeService timeService;

	private String delayId;

	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(TestResourceEventVoter.class))
		{
			protected void setUp() throws Exception
			{
				oneTimeSetup();
			}

			protected void tearDown() throws Exception
			{
				oneTimeTearDown();
			}
		};
		return setup;
	}

	@Override
	protected void setUp() throws Exception
	{
		String runAs = "admin";
		voter = (ResourceEventVoter) getService(ResourceEventVoter.class.getName());
		helper = (ResourceEventVoterHelper) getService(ResourceEventVoterHelper.class.getName());
		chs = (ContentHostingService) getService(ContentHostingService.class.getName());
		timeService = (TimeService) getService(TimeService.class.getName());
		usageSessionService = (UsageSessionService) getService(UsageSessionService.class.getName());
		usageSessionService.startSession(runAs, "localhost", "resourceVoter-integrationTest");

		sessionManager = (SessionManager) getService("org.sakaiproject.tool.api.SessionManager");
		session = sessionManager.getCurrentSession();
		session.setUserEid(runAs);
		session.setUserId(runAs);

		event = new ResourceEventVoterHelper.ReEvent("content.revise", true, 1,
				"/content/group/5c5fdcef-dbb2-415b-9714-b031e4e18bb8/quotes.txt", null);
	}

	@Override
	protected void tearDown() throws Exception
	{
		if (delayId != null)
			helper.deleteDelay(delayId);
	}

	public void testExecute() throws Exception
	{
		// create a delay that is arbitrarily in the future.
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MINUTE, 5);
		Time releaseDate = timeService.newTime(cal);
		delayId = helper.createDelay(event, "admin", releaseDate);

		voter.execute(delayId);
	}
}
