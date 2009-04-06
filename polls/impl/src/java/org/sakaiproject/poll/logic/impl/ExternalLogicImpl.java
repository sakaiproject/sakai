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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.poll.model.PollRolePerms;

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

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
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


	public List<String> getRoleIdsInRealm(String realmId) {
		AuthzGroup group;
		
		try {
			group = authzGroupService.getAuthzGroup(realmId);
			List<String> ret = new ArrayList<String>();
			Set<Role> roles = group.getRoles();
			Iterator<Role> i = roles.iterator();
			while (i.hasNext()) {
				Role role = (Role)i.next();
				ret.add(role.getId());
			}
			return ret;
		} catch (GroupNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		return null;
	}


	public boolean isRoleAllowedInRealm(String roleId, String realmId, String permission) {
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(realmId);
			Role role = group.getRole(roleId);
			return  role.isAllowed(permission);
		} catch (GroupNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}


	public String getSiteTile(String locationReference) {
		Site site;
		
		try {
			site = siteService.getSite(locationReference);
			return site.getTitle();
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}

	public void setToolPermissions(Map<String, PollRolePerms> permMap,
			String locationReference) throws SecurityException, IllegalArgumentException {
		
		AuthzGroup authz = null;
		try {
			 authz = authzGroupService.getAuthzGroup(locationReference);
		}
		catch (GroupNotDefinedException e) {
			
			throw new IllegalArgumentException(e);
			
		}
		for (Iterator<String> i = permMap.keySet().iterator(); i.hasNext();)
		{	
			String key = (String) i.next();
			Role role = authz.getRole(key);
			//try {
			  PollRolePerms rp = (PollRolePerms) permMap.get(key);
			  if (rp.add != null )
				  setFunc(role,PollListManager.PERMISSION_ADD,rp.add);
			  if (rp.deleteAny != null )
				  setFunc(role,PollListManager.PERMISSION_DELETE_ANY, rp.deleteAny);
			  if (rp.deleteOwn != null )
				  setFunc(role,PollListManager.PERMISSION_DELETE_OWN,rp.deleteOwn);
			  if (rp.editAny != null )
				  setFunc(role,PollListManager.PERMISSION_EDIT_ANY,rp.editAny);
			  if (rp.editOwn != null )
				  setFunc(role,PollListManager.PERMISSION_EDIT_OWN,rp.editOwn);
			  if (rp.vote != null )
				  setFunc(role,PollListManager.PERMISSION_VOTE,rp.vote);
			  
			  log.info(" Key: " + key + " Vote: " + rp.vote + " New: " + rp.add );
			/*}
			  catch(Exception e)
			{
			log.error(" ClassCast Ex PermKey: " + key);
				e.printStackTrace();
				return "error";
			}*/
		}
		try {
			authzGroupService.save(authz);
		}
		catch (GroupNotDefinedException e) {
			throw new IllegalArgumentException(e);
		}
		catch (AuthzPermissionException e) {
			throw new SecurityException(e);
		}
		
	}

	
	public Map<String, PollRolePerms> getRoles(String locationReference)
	{
		log.debug("Getting permRoles");
		Map<String, PollRolePerms>  perms = new HashMap<String, PollRolePerms>();
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(locationReference);
			Set<Role> roles = group.getRoles();
			Iterator<Role> i = roles.iterator();
			
			while (i.hasNext())
			{
				Role role = (Role)i.next();
				String name = role.getId();
				log.debug("Adding element for " + name); 
				perms.put(name, new PollRolePerms(name, 
						role.isAllowed(PollListManager.PERMISSION_VOTE),
						role.isAllowed(PollListManager.PERMISSION_ADD),
						role.isAllowed(PollListManager.PERMISSION_DELETE_OWN),
						role.isAllowed(PollListManager.PERMISSION_DELETE_ANY),
						role.isAllowed(PollListManager.PERMISSION_EDIT_OWN),
						role.isAllowed(PollListManager.PERMISSION_EDIT_ANY)
						));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return perms;
	}

	
	private void setFunc(Role role, String function, Boolean allow)
	{
		
			//m_log.debug("Setting " + function + " to " + allow.toString() + " for " + rolename + " in /site/" + ToolManager.getCurrentPlacement().getContext());
			if (allow.booleanValue())
				role.allowFunction(function);
			else
				role.disallowFunction(function);
			
	}

	public String getSiteRefFromId(String siteId) {
		return siteService.siteReference(siteId);
	} 

	
}
