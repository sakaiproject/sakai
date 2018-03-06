/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.event.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * Implementation of ActivityService
 *
 */
@Slf4j
public class ActivityServiceImpl implements ActivityService, Observer {
	private Cache<String, Long> userActivityCache = null;
	private final String USER_ACTIVITY_CACHE_NAME = "org.sakaiproject.event.api.ActivityService.userActivityCache";
		
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserActive(String userId) {
		if(userActivityCache.containsKey(userId)){
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getActiveUsers(List<String> userIds) {
		
		List<String> activeUsers = new ArrayList<String>();
		for(String userId: userIds) {
			if(isUserActive(userId)){
				activeUsers.add(userId);
			}
		}
		return activeUsers;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public Long getLastEventTimeForUser(String userId) {
		return (Long)userActivityCache.get(userId);
	}

	/**
 	 * {@inheritDoc}
 	 */
	public Map<String, Long> getLastEventTimeForUsers(List<String> userIds) {
		
		 Map<String, Long> times = new HashMap<String, Long>();
		 for(String userId: userIds) {
			 if(isUserActive(userId)) {
				 times.put(userId, getLastEventTimeForUser(userId));
			 }
		 }
		 return times;
	}

	
	/**
	 * Update the cache with the observed Event
	 */
	public void update(Observable obs, Object o) {
		if(o instanceof Event){
			Event e = (Event) o;
			
			String userId = e.getUserId();
			
			//If userId is blank, get it from the sessionId.
			//I believe this is always the case though? - sswinsburg.
			if(StringUtils.isBlank(userId)) {
				log.debug("No userId for event, getting from the UsageSession instead: " + e.getEvent());
				
				UsageSession session = usageSessionService.getSession(e.getSessionId());
				if(session != null) {
					userId = session.getUserId();
				}
				
				//if still blank, give up.
				if(StringUtils.isBlank(userId)) {
					log.debug("Couldn't get a userId for event, cannot update cache - skipping: " + e.getEvent());
					return;
				}
			}
			
			//if event is logout, remove entry from cache, otherwise add to cache
			if(StringUtils.equals(e.getEvent(), UsageSessionService.EVENT_LOGOUT)) {
				userActivityCache.remove(userId);
				log.debug("Removed from user activity cache: " + userId);
			} else {
				userActivityCache.put(userId, Long.valueOf(new Date().getTime()));
				log.debug("Added to user activity cache: " + userId);
			}
		}
	}
	
	
	public void init() {
		
		//add event observer
		eventTrackingService.addPriorityObserver(this);
		
		//setup cache
		userActivityCache = memoryService.newCache(USER_ACTIVITY_CACHE_NAME);
	}
	
	public void destroy() {
		eventTrackingService.deleteObserver(this);
	}

	private MemoryService memoryService;
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
	
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}
	
	private UsageSessionService usageSessionService;
	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}
}
