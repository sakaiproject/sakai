/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/

package org.sakaiproject.signup.tool.entityproviders;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.sakaiproject.signup.restful.SignupEvent;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>
 * This class will provides user session cache for restful events.
 * </P>
 * 
 * @author Peter Liu
 */
public class SignupRESTfulSessionManager {

	private static final String USER_SESSION_DATA = "signup.user.session.data";

	/* 5 minutes */
	private static final long TIME_INTERVAL_TO_REFRESH = 5 * 60 * 1000;

	SignupRESTfulSessionManager() {
	}

	public UserSignupSessionData getStoredUserSessionData() {
		return (UserSignupSessionData) SessionManager.getCurrentSession().getAttribute(USER_SESSION_DATA);
	}

	public SiteEventsCache getSiteEventsCache(String siteId) {
		if (getStoredUserSessionData() == null)
			return null;

		return getStoredUserSessionData().get(siteId);
	}

	public void StoreSiteEventsData(String siteId, List<SignupEvent> events, String viewRange) {
		SiteEventsCache siteEventsCache = new SiteEventsCache(siteId, events, viewRange);
		UserSignupSessionData userDataMap = getStoredUserSessionData();
		if (userDataMap == null) {
			userDataMap = new UserSignupSessionData();
		}
		userDataMap.add(siteId, siteEventsCache);
		storeUserDataInSession(userDataMap);
	}

	public void StoreSiteEventsData(String siteId, SiteEventsCache cache) {
		UserSignupSessionData userDataMap = getStoredUserSessionData();
		if (userDataMap == null) {
			userDataMap = new UserSignupSessionData();
		}
		userDataMap.add(siteId, cache);
		storeUserDataInSession(userDataMap);
	}

	public boolean isUpTodateDataAvailable(String siteId) {
		if (this.getSiteEventsCache(siteId) != null && !isRefresh(getSiteEventsCache(siteId))) {
			return true;
		}

		return false;
	}

	public boolean isSameViewRange(String siteId, String viewRange) {
		SiteEventsCache sec = this.getSiteEventsCache(siteId);
		if (sec != null && sec.getViewRange().equals(viewRange)) {
			return true;
		}
		return false;
	}

	public SignupEvent getExistedSignupEventInCache(String siteId, Long eventId) {
		if (this.getSiteEventsCache(siteId) != null) {
			getSiteEventsCache(siteId).getSignupEvent(eventId);
			return getSiteEventsCache(siteId).getSignupEvent(eventId);
		}
		return null;
	}

	public void updateSiteEventsCache(String siteId, SignupEvent event) {
		SiteEventsCache siteECache = getSiteEventsCache(siteId);
		if (siteECache == null) {
			return; // don't update, force it to get a new list later.
		}

		if (event == null) {
			/* clean up and force it to get a new list later due to event-gone. */
			cleanSiteEventsCache(siteId);
			return;
		} else {
			siteECache.updateSignupEvent(event);
		}

		StoreSiteEventsData(siteId, siteECache);
	}

	private void storeUserDataInSession(UserSignupSessionData userSessionData) {
		SessionManager.getCurrentSession().setAttribute(USER_SESSION_DATA, userSessionData);
	}

	private void cleanSiteEventsCache(String siteId) {
		UserSignupSessionData userSnData = getStoredUserSessionData();
		userSnData.remove(siteId);
		storeUserDataInSession(userSnData);

	}

	private boolean isRefresh(SiteEventsCache cache) {
		Date current = new Date();
		return (current.getTime() - cache.getLastUpdatedTime().getTime() > TIME_INTERVAL_TO_REFRESH);
	}

	class UserSignupSessionData {
		private Hashtable<String, SiteEventsCache> table = new Hashtable<String, SiteEventsCache>();

		public UserSignupSessionData() {
		}

		public void add(String siteId, SiteEventsCache eCache) {
			this.table.put(siteId, eCache);
		}

		public SiteEventsCache get(String siteId) {
			return this.table.get(siteId);
		}

		public void remove(String siteId) {
			this.table.remove(siteId);
		}

		public void update(String siteId, SiteEventsCache eCache) {
			this.table.put(siteId, eCache);
		}

	}

	class SiteEventsCache {

		private Date lastUpdatedTime;

		private String siteId;

		private List<SignupEvent> events;

		private String viewRange = "";

		SiteEventsCache(String siteId, List<SignupEvent> events, String viewRange) {
			this.siteId = siteId;
			this.events = events;
			this.viewRange = viewRange;
			lastUpdatedTime = new Date();
		}

		SiteEventsCache(String siteId, SignupEvent event) {
			this.siteId = siteId;
			events = new ArrayList<SignupEvent>();
			events.add(event);
			lastUpdatedTime = new Date();
		}

		public SignupEvent getSignupEvent(Long eventId) {
			SignupEvent event = null;
			if (this.events != null) {
				for (SignupEvent e : events) {
					if (e.getEventId().equals(eventId)) {
						event = e;
						break;
					}

				}
			}
			return event;
		}

		public void updateSignupEvent(SignupEvent event) {
			if (this.events != null) {
				for (int i = 0; i < events.size(); i++) {
					if (events.get(i).getEventId().equals(event.getEventId())) {
						events.remove(i);
						events.add(i, event);
						break;
					}
				}
			}
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		public List<SignupEvent> getEvents() {
			return events;
		}

		public void setEvents(List<SignupEvent> events) {
			this.events = events;
		}

		public Date getLastUpdatedTime() {
			return lastUpdatedTime;
		}

		public void setLastUpdatedTime(Date lastUpdatedTime) {
			this.lastUpdatedTime = lastUpdatedTime;
		}

		public String getViewRange() {
			return viewRange;
		}

		public void setViewRange(String viewRange) {
			this.viewRange = viewRange;
		}

	}

}
