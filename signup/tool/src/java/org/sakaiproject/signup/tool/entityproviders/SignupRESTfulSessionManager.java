/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.entityproviders;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.restful.SignupEvent;
import org.sakaiproject.signup.restful.SignupParticipant;
import org.sakaiproject.signup.restful.SignupTimeslotItem;
import org.sakaiproject.signup.tool.util.Utilities;
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
		return (UserSignupSessionData) SessionManager.getCurrentSession().getAttribute(
				USER_SESSION_DATA);
	}

	public SignupEventsCache getSignupEventsCache(String uniqueId) {
		if (uniqueId == null || getStoredUserSessionData() == null)
			return null;

		return getStoredUserSessionData().get(uniqueId);
	}

	/**
	 * Store the data in the cache
	 * 
	 * @param uniqueId
	 *            can be unique siteId or userId
	 * @param events
	 *            a list of SignupEvent objects
	 * @param viewRange
	 *            a numeric string
	 */
	public void StoreSignupEventsData(String uniqueId, List<SignupEvent> events, String viewRange) {
		SignupEventsCache signupEventsCache = new SignupEventsCache(uniqueId, events, viewRange);
		UserSignupSessionData userDataMap = getStoredUserSessionData();
		if (userDataMap == null) {
			userDataMap = new UserSignupSessionData();
		}
		userDataMap.add(uniqueId, signupEventsCache);
		storeUserDataInSession(userDataMap);
	}

	public void StoreSignupEventsData(String uniqueId, SignupEventsCache cache) {
		UserSignupSessionData userDataMap = getStoredUserSessionData();
		if (userDataMap == null) {
			userDataMap = new UserSignupSessionData();
		}
		userDataMap.add(uniqueId, cache);
		storeUserDataInSession(userDataMap);
	}

	public boolean isUpTodateDataAvailable(String uniqueId) {
		if (this.getSignupEventsCache(uniqueId) != null && !isRefresh(getSignupEventsCache(uniqueId))) {
			return true;
		}

		return false;
	}

	/**
	 * See if the view range is the same for the data set
	 * 
	 * @param id
	 *            can be unique siteId or userId
	 * @param viewRange
	 *            numeric number string
	 * @return boolean value
	 */
	public boolean isSameViewRange(String id, String viewRange) {
		SignupEventsCache sec = this.getSignupEventsCache(id);
		if (sec != null && sec.getViewRange().equals(viewRange)) {
			return true;
		}
		return false;
	}

	public SignupEvent getExistedSignupEventInCache(String uniqueId, Long eventId) {
		if (this.getSignupEventsCache(uniqueId) != null) {
			// getSiteEventsCache(siteId).getSignupEvent(eventId);
			return getSignupEventsCache(uniqueId).getSignupEvent(eventId);
		}
		return null;
	}

	public void updateSignupEventsCache(String uniqueId, SignupEvent event) {
		SignupEventsCache eCache = getSignupEventsCache(uniqueId);
		if (eCache == null) {
			return; // don't update, force it to get a new list later.
		}

		if (event == null) {
			/* clean up and force it to get a new list later due to event-gone. */
			cleanSignupEventsCache(uniqueId);
			return;
		} else {
			eCache.updateSignupEvent(event);
		}

		StoreSignupEventsData(uniqueId, eCache);
	}
	
	public void updateMySignupEventsCache(String userId, SignupEvent event,String userAction) {
		SignupEventsCache eCache = getSignupEventsCache(userId);
		if (eCache == null) {
			return; // don't update, force it to get a new list later.
		}

		if (event == null) {
			/* clean up and force it to get a new list later due to event-gone. */
			cleanSignupEventsCache(userId);
			return;
		} else {
			if(SignupEvent.USER_CANCEL_SIGNUP.equals(userAction))
				eCache.removeEvent(event);
			else if (SignupEvent.USER_SIGNUP.equals(userAction)){
				populateMySignedUpEventData(event, userId);
				eCache.addEvent(event);
			}
		}

		StoreSignupEventsData(userId, eCache);
	}
	
	private void populateMySignedUpEventData(SignupEvent event, String userId){
		List<SignupTimeslotItem> tsList = event.getSignupTimeSlotItems();
		if(tsList !=null){
			for (SignupTimeslotItem ts : tsList) {
				List<SignupParticipant> attList = ts.getAttendees();
				if(attList !=null){
					for (SignupParticipant p : attList) {
						if(p.getAttendeeUserId().equals(userId)){
							event.setMyStartTime(ts.getStartTime());
							event.setMyEndTime(ts.getEndTime());
							event.setAvailableStatus(Utilities.rb.getString("event.youSignedUp"));
							return;
						}
					}
				}
			}
		}
	}

	private void storeUserDataInSession(UserSignupSessionData userSessionData) {
		SessionManager.getCurrentSession().setAttribute(USER_SESSION_DATA, userSessionData);
	}

	private void cleanSignupEventsCache(String uniqueId) {
		UserSignupSessionData userSnData = getStoredUserSessionData();
		userSnData.remove(uniqueId);
		storeUserDataInSession(userSnData);

	}

	private boolean isRefresh(SignupEventsCache cache) {
		Date current = new Date();
		return (current.getTime() - cache.getLastUpdatedTime().getTime() > TIME_INTERVAL_TO_REFRESH);
	}

	class UserSignupSessionData {
		private Hashtable<String, SignupEventsCache> table = new Hashtable<String, SignupEventsCache>();

		public UserSignupSessionData() {
		}

		public void add(String uniqueId, SignupEventsCache eCache) {
			this.table.put(uniqueId, eCache);
		}

		public SignupEventsCache get(String uniqueId) {
			return this.table.get(uniqueId);
		}

		public void remove(String uniqueId) {
			this.table.remove(uniqueId);
		}

		public void update(String uniqueId, SignupEventsCache eCache) {
			this.table.put(uniqueId, eCache);
		}

	}

	class SignupEventsCache {

		private Date lastUpdatedTime;

		private String uniqueId;

		private List<SignupEvent> events;

		private String viewRange = "";

		SignupEventsCache(String id, List<SignupEvent> events, String viewRange) {
			this.uniqueId = id;
			this.events = events;
			this.viewRange = viewRange;
			lastUpdatedTime = new Date();
		}

		SignupEventsCache(String id, SignupEvent event) {
			this.uniqueId = id;
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
		
		public void removeEvent(SignupEvent event){
			if (this.events != null) {
				for (int i = 0; i < events.size(); i++) {
					if (events.get(i).getEventId().equals(event.getEventId())) {
						events.remove(i);
						break;
					}
				}
			}
		}
		
		public void addEvent(SignupEvent event){
			if (this.events != null){
				events.add(event);
			}
		}

		public String getUniqueId() {
			return uniqueId;
		}

		public void setUniqueId(String uniqueId) {
			this.uniqueId = uniqueId;
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
