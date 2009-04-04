/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;

public class ExternalLogicImpl implements ExternalLogic {

	 private static Log log = LogFactory.getLog(ExternalLogicImpl.class);
	
	private static final String USER_ENTITY_PREFIX = "/user/";
	
	/**
	 * Injected services
	 */
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	
    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }
	
    private EntityManager entityManager;
    public void setEntityManager(EntityManager em) {
        entityManager = em;
    }

    private EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService ets) {
        eventTrackingService = ets;
    }
    
    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager fm) {
        functionManager = fm;
    }
    
	private TimeService timeService;
	public void setTimeService(TimeService ts) {
		timeService = ts;
	}

    
    /**
     * Methods
     */
	public String getCurrentLocationId() {
		return developerHelperService.getCurrentLocationId();
	}

	public boolean isUserAdmin(String userId) {
		return developerHelperService.isUserAdmin(USER_ENTITY_PREFIX + userId);
	}

	public boolean isUserAdmin() {
		return isUserAdmin(getCurrentUserId());
	}

	public String getCurrentUserId() {
		return developerHelperService.getCurrentUserId();
	}

	public String getCurrentLocationReference() {
		return developerHelperService.getCurrentLocationReference();
	}

	public boolean isAllowedInLocation(String permission, String locationReference, String userReference) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference) {
		return isAllowedInLocation(permission, locationReference, developerHelperService.getCurrentUserReference());
	}

    private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
    @SuppressWarnings("unchecked")
    public List<String> getSitesForUser(String userId, String permission) {
        log.debug("userId: " + userId + ", permission: " + permission);

        List<String> l = new ArrayList<String>();

        // get the groups from Sakai
        Set<String> authzGroupIds = 
           authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
        Iterator<String> it = authzGroupIds.iterator();
        while (it.hasNext()) {
           String authzGroupId = it.next();
           Reference r = entityManager.newReference(authzGroupId);
           if (r.isKnownType()) {
              // check if this is a Sakai Site or Group
              if (r.getType().equals(SiteService.APPLICATION_ID)) {
                 String type = r.getSubType();
                 if (SAKAI_SITE_TYPE.equals(type)) {
                    // this is a Site
                    String siteId = r.getId();
                    l.add(siteId);
                 }
              }
           }
        }

        if (l.isEmpty()) log.info("Empty list of siteIds for user:" + userId + ", permission: " + permission);
        return l;
     }


	public void postEvent(String eventId, String reference, boolean modify) {
		 eventTrackingService.post(eventTrackingService.newEvent(eventId, reference, modify));
		
	}

	public void registerFunction(String function) {
		functionManager.registerFunction(function);
		
	}

	public TimeZone getLocalTimeZone() {
		return timeService.getLocalTimeZone();
	}
	
}
