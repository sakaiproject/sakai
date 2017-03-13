/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.util.Observable;
import java.util.Observer;

/**
 * This observer watches for user.add and user.upd events to invalidate the Authn cache
 * 
 * @author dhorwitz
 *
 */
public class AuthnCacheWatcher implements Observer {

	private static final Logger log = LoggerFactory.getLogger(AuthnCacheWatcher.class);
	//Copied from DbUserService as they are private
	private static final String EIDCACHE = "eid:";
	private static final String IDCACHE = "id:";
	private AuthenticationCache authenticationCache;
	private UserDirectoryService userDirectoryService;
	private EventTrackingService eventTrackingService;
	private EntityManager entityManager;
    private MemoryService memoryService;
	private Cache userCache = null;
	
    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

	public void setUserCache(Cache userCache) {
		this.userCache = userCache;
	}


	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}


	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}


	public void setAuthenticationCache(AuthenticationCache authenticationCache) {
		this.authenticationCache = authenticationCache;
	}


	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}


	public void init() {
		log.info("init()");
        if (userCache == null) { // this is the user id->eid mapping cache
            userCache = memoryService.getCache("org.sakaiproject.user.api.UserDirectoryService");
        }
		eventTrackingService.addObserver(this);
	}
	
	public void destroy() {
        if (!ComponentManager.hasBeenClosed()) {
            eventTrackingService.deleteObserver(this);
        }
	}
	
	public void update(Observable arg0, Object arg) {
		// arg is Event
		if (!(arg instanceof Event))
			return;
		Event event = (Event) arg;
		
		
		// check the event function against the functions we have notifications watching for
		String function = event.getEvent();
		
		//we err on the side of caution here in checking all events that might invalidate the data in the cache -DH
		if (UserDirectoryService.SECURE_ADD_USER.equals(function) || UserDirectoryService.SECURE_UPDATE_USER_OWN_PASSWORD.equals(function)
				|| UserDirectoryService.SECURE_UPDATE_USER_ANY.equals(function) || UserDirectoryService.SECURE_UPDATE_USER_OWN.equals(function)) {
			
			//we need the userId
			Reference ref = entityManager.newReference(event.getResource());
			
			// look for group reference. Need to replace it with parent site reference
			String refId = ref.getId();
			try {
				String eid = userDirectoryService.getUserEid(refId);
				log.debug("removing " + eid + " from cache");
				authenticationCache.removeAuthentification(eid);
				userCache.remove(EIDCACHE + eid);
				userCache.remove(IDCACHE + refId);
			} catch (UserNotDefinedException e) {
				//not sure how we'd end up here
				log.warn(e.getMessage(), e);
			}
			
		} 

	}

}
