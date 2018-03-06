/**
 * Copyright (c) 2013 The Apereo Foundation
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
package org.sakaiproject.userauditservice.impl;

import java.util.Observable;
import java.util.Observer;


import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;

public class UserAuditSiteDeleteListener implements Observer {
	
	protected EventTrackingService eventTrackingService = null;
	protected SiteService siteService = null;
	protected UserAuditRegistration userAuditRegistration = null;
	
	public void init()
	{
		eventTrackingService.addObserver(this);
	}
	
	public void setEventTrackingService(EventTrackingService service)
	{
		eventTrackingService = service;
	}

	public void setSiteService(SiteService service)
	{
		siteService = service;
	}
	
	public void setUserAuditRegistration(UserAuditRegistration service)
	{
		userAuditRegistration = service;
	}
	
	public void update(Observable o, Object arg)
	{
		if (arg instanceof Event)
		{
			Event event = (Event) arg;
			if (event != null && event.getEvent() != null && siteService.SECURE_REMOVE_SITE.equals(event.getEvent()))
			{
				String resource = event.getResource();
				String siteRefRoot = siteService.REFERENCE_ROOT;
				// double check we have a /site/siteId style string
				if (resource.startsWith(siteRefRoot))
				{
					String siteId = resource.substring(siteRefRoot.length()+1);
					userAuditRegistration.deleteUserAuditingFromSite(siteId);
				}
			}
		}
	}
}
