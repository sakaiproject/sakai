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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.PollRolePerms;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class ExternalLogicImpl implements ExternalLogic {

	 private static final String
	 	/* Email template constants */
	 	EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION = "polls.notifyDeletedOption",
	 	FILE_NOTIFY_DELETED_OPTION_TEMPLATE = "org/sakaiproject/poll/templates/notifyDeletedOption.xml",
	 	
	 	/* Other constants */
	 	USER_ADMIN_ID = "admin",
	 	USER_ADMIN_EID = "admin";
	 
	private static final String USER_ENTITY_PREFIX = "/user/";
	
	/**
	 * Injected services
	 */
	
    private LearningResourceStoreService learningResourceStoreService;
    
	public void setLearningResourceStoreService(
			LearningResourceStoreService learningResourceStoreService) {
		this.learningResourceStoreService = learningResourceStoreService;
	}
    
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

    private EmailService emailService;
    public void setEmailService(EmailService emailService) {
    	this.emailService = emailService;
    }
    
    private EmailTemplateService emailTemplateService;
    public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
    	this.emailTemplateService = emailTemplateService;
    }

    private ArrayList<String> emailTemplates;
    public void setEmailTemplates(ArrayList<String> emailTemplates) {
    	this.emailTemplates = emailTemplates;
    }
    
    private EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService ets) {
        eventTrackingService = ets;
    }
    
    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager fm) {
        functionManager = fm;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}


    private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	private UserTimeService userTimeService;
	public void setUserTimeService(UserTimeService userTimeService) {
		this.userTimeService = userTimeService;
	}


	private String fromEmailAddress;
	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}
	
	private String replyToEmailAddress;
	public void setReplyToEmailAddress(String replyToEmailAddress) {
		this.replyToEmailAddress = replyToEmailAddress;
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

	public String getCurrentuserReference() {
		return developerHelperService.getCurrentUserReference();
	} 

	public String getUserEidFromId(String userId) {
		try {
			return userDirectoryService.getUserEid(userId);
		} catch (UserNotDefinedException e) {
			log.debug("Looked up non-existant user id: "+userId, e);
		}
		
		return null;
	}
	
	public String getCurrentLocationReference() {
		log.debug("getCurrentLocationReference");
        return developerHelperService.getCurrentLocationReference();
	}

	public boolean isAllowedInLocation(String permission, String locationReference, String userReference) {
		log.debug("isAllowed in location( " + permission + " , " + locationReference + " , " + userReference);
		return developerHelperService.isUserAllowedInEntityReference(userReference, permission, locationReference);
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference) {
		log.debug("isAllowed in location( " + permission + " , " + locationReference);
		return isAllowedInLocation(permission, locationReference, developerHelperService.getCurrentUserReference());
	}

    private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
   
    public void init() {
    	log.info("init()");
    	
    	// this is set by injection
    	emailTemplateService.processEmailTemplates(emailTemplates);
    }
    
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
		return userTimeService.getLocalTimeZone();
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
			log.error(e.getMessage(), e);
		}
		

		
		return null;
	}


	public boolean isRoleAllowedInRealm(String roleId, String realmId, String permission) {
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(realmId);
			Role role = group.getRole(roleId);
			return  role.isAllowed(permission);
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}


	public String getSiteTile(String siteId) {
		Site site;
		
		try {
			site = siteService.getSite(siteId);
			return site.getTitle();
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
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
		Set<Entry<String, PollRolePerms>> entrySet = permMap.entrySet(); 
		for (Iterator<Entry<String, PollRolePerms>> i = entrySet.iterator(); i.hasNext();)
		{	
			Entry<String, PollRolePerms> entry = i.next(); 
			String key = entry.getKey();
			Role role = authz.getRole(key);
			//try {
			  PollRolePerms rp = (PollRolePerms) entry.getValue();
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
			log.error(e.getMessage(), e);
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

	public boolean userIsViewingAsRole() {
		String effectiveRole = securityService.getUserEffectiveRole(developerHelperService.getCurrentLocationReference());
		if (effectiveRole != null)
					return true;
		
		return false;
	}

	public void notifyDeletedOption(List<String> userEids, String siteTitle, String pollQuestion) {
		if (siteTitle == null)
			throw new IllegalArgumentException("Site title cannot be null");
		else if (pollQuestion == null)
			throw new IllegalArgumentException("Poll Question cannot be null");
		
		Map<String, String> replacementValues = new HashMap<String, String>();

		String from = (fromEmailAddress == null || fromEmailAddress.equals("")) ?
					serverConfigurationService.getString("setup.request", "no-reply@" + serverConfigurationService.getServerName()) : fromEmailAddress;
					
		for (String userEid : userEids) {
			User user = null;
			try {
				user = userDirectoryService.getUserByEid(userEid);
				replacementValues.put("localSakaiName",
						developerHelperService.getConfigurationSetting("ui.service", "Sakai"));
				replacementValues.put("recipientFirstName",user.getFirstName());
				replacementValues.put("recipientDisplayName", user.getDisplayName());
				replacementValues.put("pollQuestion", pollQuestion);
				replacementValues.put("siteTitle", siteTitle); 

				RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser(EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION,
						user.getReference(), replacementValues);
				
				if (template == null)
					return;
					
				String
					content = template.getRenderedMessage(),
					subject = template.getRenderedSubject();
				
				emailService.send(from, user.getEmail(), subject, content, user.getEmail(), from,
						null);
			} catch (UserNotDefinedException e) {
				log.warn("Attempted to send email to unknown user (eid): '"+userEid+"'", e);
			}
		}
	}
	
	


	public ToolSession getCurrentToolSession() {
		return sessionManager.getCurrentToolSession();
	}
	
	public boolean isResultsChartEnabled() {
		return serverConfigurationService.getBoolean("poll.results.chart.enabled", false);
	}
	
	public boolean isShowPublicAccess() {
		return serverConfigurationService.getBoolean("poll.allow.public.access", false);
	}
	
	public boolean isMobileBrowser() {
		Session session = sessionManager.getCurrentSession();
		if (session.getAttribute("is_wireless_device") != null && ((Boolean) session.getAttribute("is_wireless_device")).booleanValue()) {
			return true;
		}
		return false;
		
	}
	
	
	public List<String> getPermissionKeys() {
		
		String[] perms = new String[]{
				PollListManager.PERMISSION_VOTE,
			    PollListManager.PERMISSION_ADD,
			    PollListManager.PERMISSION_EDIT_OWN,
			    PollListManager.PERMISSION_EDIT_ANY,
			    PollListManager.PERMISSION_DELETE_OWN,
			    PollListManager.PERMISSION_DELETE_ANY
		}; 
		List<String> ret = Arrays.asList(perms);
		return ret;
	}

    private LRS_Statement getStatementForUserVotedInPoll(String text, Vote vote) {
    	LRS_Actor student = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        String url = serverConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + "/poll", "voted-in-poll");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User voted in a poll");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User voted in a poll with text:" + text + "; their vote was option: " + vote.getPollOption());
        lrsObject.setDescription(descMap);
        return new LRS_Statement(student, verb, lrsObject);
    }

    private LRS_Statement getStatementForUserEditPoll(String text, boolean newPoll) {
    	LRS_Actor student = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        String url = serverConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + "/poll", newPoll ? "new-poll" : "updated-poll");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User " + (newPoll ? "created" : "updated") + " a poll");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User " + (newPoll ? "created" : "updated") + " a poll with text:" + text);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(student, verb, lrsObject);
    }

    /**
     * @see org.sakaiproject.poll.logic.ExternalLogic#registerStatement(java.lang.String, org.sakaiproject.poll.model.Vote)
     */
    @Override
    public void registerStatement(String pollText, Vote vote) {
        if (null != learningResourceStoreService) {
            LRS_Statement statement = getStatementForUserVotedInPoll(pollText, vote);
            Event event = eventTrackingService.newEvent("poll.vote", "vote", null, true, NotificationService.NOTI_OPTIONAL, statement);
            eventTrackingService.post(event);
        }
    }

    /**
     * @see org.sakaiproject.poll.logic.ExternalLogic#registerStatement(java.lang.String, boolean)
     */
    @Override
    public void registerStatement(String pollText, boolean newPoll) {
        if (null != learningResourceStoreService) {
            LRS_Statement statement = getStatementForUserEditPoll(pollText, newPoll);
            Event event = eventTrackingService.newEvent("poll.edit", "edit poll", null, true, NotificationService.NOTI_OPTIONAL, statement);
            eventTrackingService.post(event);
        }
    }

}
