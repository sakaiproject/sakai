/**
 * $Id$
 * $URL$
 * ServerConfigEntityProvider.java - entity-broker - Jul 17, 2008 2:19:03 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Order;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.providers.model.EntityMember;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;

/**
 * This provides access to memberships as entities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class MembershipEntityProvider extends AbstractEntityProvider implements CoreEntityProvider,
RESTful, ActionsExecutable {

    private SiteService siteService;
    private AuthzGroupService authzGroupService;

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private UserEntityProvider userEntityProvider;

    public void setUserEntityProvider(UserEntityProvider userEntityProvider) {
        this.userEntityProvider = userEntityProvider;
    }

    private EmailService emailService;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    private PrivacyManager privacyManager;
    public void setPrivacyManager(PrivacyManager privacyManager){
    	this.privacyManager = privacyManager;
    }
    
    private SecurityService securityService;
    public void setSecurityService(SecurityService securityService){
    	this.securityService = securityService;
    }

    private static UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private static UserAuditRegistration userAuditRegistration;
    public void setUserAuditRegistration(UserAuditRegistration userAuditRegistration) {
        this.userAuditRegistration = userAuditRegistration;
    }

    public static String PREFIX = "membership";

    public String getEntityPrefix() {
        return PREFIX;
    }

    private static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";
    private static final String ADMIN_SITE_ID = "!admin";
    /**
     * SAKAI CONFIG
     * False by default, no admin site changes allowed
     */
    private static final String ADMIN_SITE_CHANGE_ALLOWED = "eb.membership.admin.site.changes.allowed";
    private boolean allowAdminSiteChanges = false;

    public void init() {
        allowAdminSiteChanges = developerHelperService.getConfigurationSetting(ADMIN_SITE_CHANGE_ALLOWED, false);
    }


    /**
     * join/site/siteId or join/siteId Handle the special case of joining a site, using normal
     * create will not work
     */
    @EntityCustomAction(action = "join", viewKey = EntityView.VIEW_NEW)
    public boolean joinCurrentUserToSite(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
            siteId = (String) params.get("siteId");
        } else if ("site".equals(siteId)) {
            siteId = view.getPathSegment(3);
        }
        if (siteId == null) {
            throw new IllegalArgumentException(
                    "siteId must be set in order join sites, set in params or in the URL /join/site/siteId");
        }
        checkSiteSecurity(siteId);
        try {
            siteService.join(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("The siteId provided (" + siteId
                    + ") could not be found: " + e, e);
        } catch (PermissionException e) {
            throw new SecurityException("The current user ("
                    + developerHelperService.getCurrentUserId()
                    + ") does not have permission to join site (" + siteId + "): " + e, e);
        }
        return true;
    }

    /**
     * unjoin/site/siteId or unjoin/siteId Handle the special case of un-joining a site, using
     * normal delete will not work
     */
    @EntityCustomAction(action = "unjoin", viewKey = EntityView.VIEW_NEW)
    public boolean unjoinCurrentUserFromSite(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
            siteId = (String) params.get("siteId");
        } else if ("site".equals(siteId)) {
            siteId = view.getPathSegment(3);
        }

        if (siteId == null) {
            throw new IllegalArgumentException(
                    "siteId must be set in order to unjoin sites, set in params or in the URL /unjoin/site/siteId");
        }
        checkSiteSecurity(siteId);
        try {
            siteService.unjoin(siteId);
            //String user = sessionManager().getCurrentSessionUserId();
            String currentUserEid = userEntityProvider.getCurrentUser(view).getEid(); //userDirectoryService.getCurrentUser().getEid();
            String roleId = siteService.getSite(siteId).getJoinerRole();
            List<String[]> userAuditList = Collections.singletonList(new String[]{siteId,currentUserEid,roleId, UserAuditService.USER_AUDIT_ACTION_REMOVE,userAuditRegistration.getDatabaseSourceKey(),currentUserEid});
            userAuditRegistration.addToUserAuditing(userAuditList);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("The siteId provided (" + siteId
                    + ") could not be found: " + e, e);
        } catch (PermissionException e) {
            throw new SecurityException("The current user ("
                    + developerHelperService.getCurrentUserId()
                    + ") does not have permission to join site (" + siteId + "): " + e, e);
        }
        return true;
    }

    /**
     * Handle the special needs of UX site membership settings, either getting the current list of
     * site memberships via a GET request, or creating a new batch of site memberships via a POST
     * request. In the case of a POST, special HTTP response headers will be used to communicate
     * success or warning conditions to the client.
     */
    @EntityCustomAction(action = "site", viewKey = "")
    public ActionReturn handleSiteMemberships(EntityView view, Map<String, Object> params) {
        if (log.isDebugEnabled())
            log.debug("handleSiteMemberships method=" + view.getMethod() + ", params=" + params);
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
            siteId = (String) params.get("siteId");
            if (siteId == null) {
                throw new IllegalArgumentException(
                        "siteId must be set in order to get site memberships, set in params or in the URL /membership/site/siteId");
            }
        }
        
        String locationReference = "/site/" + siteId;

        Map<String, String> extraResponseHeaders = null;
        if (EntityView.Method.POST.name().equals(view.getMethod())) {
            extraResponseHeaders = createBatchMemberships(view, params, locationReference);
        }

        List<EntityData> l = getEntities(new EntityReference(PREFIX, ""), new Search(
                CollectionResolvable.SEARCH_LOCATION_REFERENCE, locationReference));
        ActionReturn actionReturn = new ActionReturn(l, Formats.JSON);
        if ((extraResponseHeaders != null) && !extraResponseHeaders.isEmpty()) {
            actionReturn.setHeaders(extraResponseHeaders);
        }
        return actionReturn;
    }

    /**
     * Add members to a site.
     * 
     * @param view
     * @param params
     *            request parameters including a list of userSearchValues
     * @param locationReference
     * @return headers containing success or warning messages for the client
     */
    public Map<String, String> createBatchMemberships(EntityView view, Map<String, Object> params,
            String locationReference) {

        SiteGroup sg = findLocationByReference(locationReference);
        String roleId = (String) params.get("memberRole");
        String notificationMessage = (String) params.get("notificationMessage");
        if ((notificationMessage != null) && (notificationMessage.trim().length() == 0)) {
            notificationMessage = null;
        }
        boolean active = true;

        Map<String, String> responseHeaders = new HashMap<String, String>();
        Set<EntityUser> users = new HashSet<EntityUser>();
        Set<String> valuesNotFound = new HashSet<String>();
        Set<String> valuesAlreadyMembers = new HashSet<String>();
        List<String> userSearchValues = getListFromValue(params.get("userSearchValues"));
        for (String userSearchValue : userSearchValues) {
            EntityUser user = userEntityProvider.findUserFromSearchValue(userSearchValue);
            if (user != null) {
                if (sg.site.getUserRole(user.getId()) != null) {
                    valuesAlreadyMembers.add(userSearchValue);
                } else {
                    users.add(user);
                }
            } else {
                valuesNotFound.add(userSearchValue);
            }
        }

        if (!users.isEmpty()) {
            String currentUserEmail = userEntityProvider.getCurrentUser(null).getEmail();
            for (EntityUser user : users) {
                sg.site.addMember(user.getId(), roleId, active, false);
                if (notificationMessage != null) {
                    /**
                     * TODO Should the From address be the site contact or the "setup.request" Sakai
                     * property? TODO We need to retrieve a localized message title and additional
                     * body (if any) instead of hard-coding it. See the new Email Template Service
                     * for a likely approach.
                     */
                    emailService.send(currentUserEmail, user.getEmail(),
                            "New Site Membership Notification", notificationMessage, null, null,
                            null);
                }
            }
            saveSiteMembership(sg.site);
            responseHeaders.put("x-success-count", String.valueOf(users.size()));
        }
        if (!valuesNotFound.isEmpty()) {
            Iterator<String> listIter = valuesNotFound.iterator();
            StringBuilder listString = new StringBuilder(listIter.next());
            while (listIter.hasNext()) {
                listString.append(", ").append(listIter.next());
            }
            responseHeaders.put("x-warning-not-found", listString.toString());
        }
        if (!valuesAlreadyMembers.isEmpty()) {
            Iterator<String> listIter = valuesAlreadyMembers.iterator();
            StringBuilder listString = new StringBuilder(listIter.next());
            while (listIter.hasNext()) {
                listString.append(", ").append(listIter.next());
            }
            responseHeaders.put("x-warning-already-members", listString.toString());
        }
        return responseHeaders;
    }
    
    @EntityCustomAction(action = "fastroles", viewKey = "")
    public List <EntityData> getMembershipRoles(EntityView view, Map<String, Object> params) {
    	//Can be ID or EID
    	String userId = view.getPathSegment(2);
    	//Don't include member details
    	Search s = new Search("includeMemberDetails",false);
    	if (userId != null) {
    		s.addRestriction(new Restriction(CollectionResolvable.SEARCH_USER_REFERENCE,userId));
    	}
        return getEntities(new EntityReference(PREFIX, ""), s);
        
    }

    /**
     * Special handler for JSON uploads of site membership data. Takes a parameter 'json' which
     * should contain json of the form:
     * 
     * [ {"id": "user1", "role": "access"},{"id": "user2","role": "maintain"} ]
     */
    @EntityCustomAction(action = "sitebyjson", viewKey = EntityView.VIEW_NEW)
    public ActionReturn handleSiteJsonUpload(EntityView view, Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("handleSiteJsonUpload method=" + view.getMethod() + ", params=" + params);
        }

        String siteId = view.getPathSegment(2);
        if (siteId == null) {
            siteId = (String) params.get("siteId");
            if (siteId == null) {
                throw new IllegalArgumentException(
                        "siteId must be set in order to get site memberships, set in params or in the URL /membership/site/siteId");
            }
        }
        
        String locationReference = "/site/" + siteId;
        
        String json = (String) params.get("json");
        if (json == null) {
        	throw new IllegalArgumentException(
                        "The membership JSON data must be supplied as a POST parameter named 'json'.");
        }
        
        Map<String, String> extraResponseHeaders = new HashMap<String, String>(3);
        
        ObjectMapper mapper = new ObjectMapper();
        List<JsonUser> memberships;
		
        try {
            memberships = mapper.readValue(json.getBytes(), new TypeReference<List<JsonUser>>() { });
        } catch(JsonParseException jpe) {
            throw new IllegalArgumentException("The supplied JSON was invalid. Have a look at http://www.json.org/.");
        } catch(JsonMappingException jpe) {
            throw new IllegalArgumentException("The supplied JSON was invalid. Take a look at /direct/membership/describe for the correct structure.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read the supplied JSON.");
        }
		
        // Collect the users into roles
        Map<String, List<String>> usersToRoleMap = new HashMap<String, List<String>>();
        for (JsonUser user : memberships) {
            String id = user.getId();
            String role = user.getRole();
            
            if (usersToRoleMap.containsKey(role)) {
                usersToRoleMap.get(role).add(id);
            } else {
                List<String> users = new ArrayList<String>();
                users.add(id);
                usersToRoleMap.put(role, users);
            }
        }
		
        Map<String, Object> localParams = new HashMap<String, Object>(2);
            
        // Compile results from createBatchMemberships. We'll return these in the response.
        int successCount = 0;
        StringBuilder notFoundBuilder = new StringBuilder();
        StringBuilder alreadyMemberBuilder = new StringBuilder();
            
        Iterator<String> memberRoles = usersToRoleMap.keySet().iterator();
        while (memberRoles.hasNext()) {
            String memberRole = memberRoles.next();
            localParams.put("userSearchValues", usersToRoleMap.get(memberRole));
            localParams.put("memberRole", memberRole);
            Map<String, String> response = createBatchMemberships(view, localParams, locationReference);
            
            String successCountString = response.get("x-success-count");
            if (successCountString != null) {
                try {
                    successCount += Integer.parseInt(successCountString);
                } catch (NumberFormatException nfe) {
                    log.error("x-success-count was not a number. successCount was not increased.");
                }
            }
                
            String nf = response.get("x-warning-not-found");
            if (nf != null) {
                notFoundBuilder.append(nf);
                if (memberRoles.hasNext()) {
                    notFoundBuilder.append(", ");
                }
            }
                
            String am = response.get("x-warning-already-members");
            if (am != null) {
                alreadyMemberBuilder.append(am);
                if (memberRoles.hasNext()) {
                    alreadyMemberBuilder.append(", ");
                }
            }
        }
            
        extraResponseHeaders.put("x-success-count", String.valueOf(successCount));
        extraResponseHeaders.put("x-warning-not-found", notFoundBuilder.toString());
        extraResponseHeaders.put("x-warning-already-members", alreadyMemberBuilder.toString());

        return new ActionReturn("", extraResponseHeaders);
    }
 
    @EntityCustomAction(action = "group", viewKey = "")
    public List<EntityData> getGroupMemberships(EntityView view, Map<String, Object> params) {
        String groupId = view.getPathSegment(2);
        List<EntityData> ed = null;

        if (EntityView.Method.GET.name().equals(view.getMethod())) {
            // GET /direct/membership/group/groupid - gets current membership for the given groupid

            if (groupId == null) {
                groupId = (String) params.get("groupId");
                if (groupId == null) {
                    throw new IllegalArgumentException(
                            "groupId must be set in order to get group memberships, set in params or in the URL /membership/group/groupId");
                }
            }
            ed = getEntities(new EntityReference(PREFIX, ""), new Search(
                    CollectionResolvable.SEARCH_LOCATION_REFERENCE, "/group/" + groupId));

        } else if (EntityView.Method.POST.name().equals(view.getMethod())) {
            // POST /direct/membership/group/groupid - update the membership for the given groupid

            String action = params.get("action") != null ? params.get("action").toString() : null;

            if (action == null || "".equals(action)) {
                throw new IllegalArgumentException(
                        "A parameter named 'action' needs to be specified. 'action' can be update, add or remove. Cannot edit group:"
                                + groupId);
            }

            List<String> userIds = params.get("userIds") != null ? Arrays.asList(params.get(
                    "userIds").toString().split(",")) : new ArrayList<String>();
            if (userIds.size() <= 0) {
                throw new IllegalArgumentException(
                        "A list of user ids needs to be specified as a parameter named 'userIds'. Cannot edit group:"
                                + groupId);
            }

            SiteGroup siteGroup = findLocationByReference("/group/" + groupId);
            Site site = siteGroup.site;
            Group group = siteGroup.group;

            if (site == null) {
                throw new IllegalArgumentException("The site for the group (" + groupId
                        + ") could not be found.");
            }
            if (group == null) {
                throw new IllegalArgumentException("The group provided (" + groupId
                        + ") could not be found.");
            }

            checkGroupType(group);

            if (!siteService.allowUpdateSite(site.getId())) {
                throw new SecurityException("This site (" + site.getReference()
                        + ") cannot be updated by the current user.");
            }

            if ("add".equals(action)) {
                // add the list to the existing membership
                for (String user : userIds) {
                    String userId = userEntityProvider.findAndCheckUserId(null, user.trim());
                    if (userId == null) {
                        log.warn("Unable to add user ("+user+") to group ("+group.getId()+") in site ("+site.getId()+"), could not find user record by id or eid");
                        continue;
                    }
                    Member m = site.getMember(userId);
                    if (m == null) {
                        log.warn("Unable to add user ("+user+") to group ("+group.getId()+") in site ("+site.getId()+"), user is not a member of the site (and must be)");
                        continue;
                    }
                    Role role = m.getRole();

                    if (group.getMember(userId) == null && (role != null && role.getId() != null)) {
                        // Every user added via this EB is defined as non-provided
                        try {
                            group.insertMember(userId, role.getId(), m != null ? m.isActive() : true, false);
                        } catch (IllegalStateException e) {
                            log.error(".getGroupMemberships: User with id {} cannot be inserted in group with id {} because the group is locked", userId, group.getId());
                        }
                    }
                }
            } else if ("update".equals(action)) {
                if (!siteService.allowUpdateGroupMembership(site.getId())) {
                    throw new SecurityException("This group (" + groupId + ") in site ("
                            + site.getId() + ") cannot be updated by the current user.");
                }
                // replace the current membership with the provided list
                try {
                    group.deleteMembers();
                } catch (IllegalStateException e) {
                    log.error(".getGroupMemberships: Members from group with id {} cannot be deleted because the group is locked", group.getId());
                }
                for (String user : userIds) {
                    String userId = userEntityProvider.findAndCheckUserId(null, user.trim());
                    if (userId == null) {
                        log.warn("Unable to update user ("+user+") in group ("+group.getId()+") in site ("+site.getId()+"), could not find user record by id or eid");
                        continue;
                    }
                    Member m = site.getMember(userId);
                    Role role = m.getRole();

                    if (group.getMember(userId) == null && (role != null && role.getId() != null)) {
                        // Every user added via this EB is defined as non-provided
                        try {
                            group.insertMember(userId, role.getId(), m != null ? m.isActive() : true,
                                false);
                        } catch (IllegalStateException e) {
                            log.error(".getGroupMemberships: User with id {} cannot be inserted in group with id {} because the group is locked", userId, group.getId());
                        }
                    }
                }
            } else if ("remove".equals(action)) {
                // remove the list from the existing membership
                for (String userId : userIds) {
                    userId = userEntityProvider.findAndCheckUserId(null, userId.trim());
                    if (userId == null) {
                        log.warn("Unable to remove user ("+userId+") from group ("+group.getId()+") in site ("+site.getId()+"), could not find user record by id or eid");
                        continue;
                    }
                    try {
                        group.deleteMember(userId);
                    } catch (IllegalStateException e) {
                        log.error(".getGroupMemberships: User with id {} cannot be deleted from group with id {} because the group is locked", userId, group.getId());
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "A valid value for the parameter named 'action' needs to be specified. 'action' can be update, add or remove. Cannot edit group:"
                                + groupId);
            }

            // save group
            try {
                siteService.save(site);
            } catch (IdUnusedException e) {
                throw new IllegalArgumentException("Cannot find site with given id: "
                        + site.getId() + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                throw new SecurityException(
                        "Current user does not have permission to save this group:" + groupId
                        + " to site:" + site.getId());
            }
            return null;
        }
        return ed;
    }

    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        String[] parts = EntityMember.parseId(id);
        if (parts != null) {
            // TODO check it later when there is an efficient way
            return true;
        }
        return false;
    }

    public Object getEntity(EntityReference ref) {
        if (ref.getId() == null) {
            return new EntityMember();
        }
        String mid = ref.getId();
        String[] parts = EntityMember.parseId(mid);
        if (parts == null) {
            throw new IllegalArgumentException(
                    "Invalid membership id ("
                            + mid
                            + "), should be formed like so: 'userId::site:siteId' or 'userId::group:groupId");
        }
        EntityMember member = getMember(parts[0], parts[1]);
        if (member == null) {
            throw new IllegalArgumentException("Cannot find membership with id: " + mid);
        }
        return member;
    }

    /**
     * Gets the list of all memberships for the current user if no params provided, otherwise gets
     * memberships in a specified location or for a specified user
     */
    public List<EntityData> getEntities(EntityReference ref, Search search) {
        String currentUserId = developerHelperService.getCurrentUserId();
        String userId = null;
        String locationReference = null;
        String roleId = null;
        boolean includeSites = true;
        boolean includeGroups = false;
        //Include details about the membership, has a performance impact
        boolean includeMemberDetails = true;
        
        //SAK-25710 hold a map of each sites type so we can look them up later (entityId, siteType)
        Map<String,String> siteTypes = new HashMap<String,String>();
        
        if (search == null) {
            search = new Search();
        }
        if (!search.isEmpty()) {
            // process the search
            roleId = (String) search.getRestrictionValueByProperties(new String[] { "role",
            "roleId" });
            Restriction userRes = search
                    .getRestrictionByProperty(CollectionResolvable.SEARCH_USER_REFERENCE);
            if (userRes != null) {
                String userRef = userRes.getStringValue();
                userId = EntityReference.getIdFromRef(userRef);
            }
            Restriction locRes = search
                    .getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE);
            if (locRes != null) {
                locationReference = locRes.getStringValue();
            }
            Restriction incSites = search.getRestrictionByProperty("includeSites");
            if (incSites != null) {
                includeSites = incSites.getBooleanValue();
            }
            Restriction incGroups = search.getRestrictionByProperty("includeGroups");
            if (incGroups != null) {
                includeGroups = incGroups.getBooleanValue();
            }
            
            Restriction incMemberDetails = search.getRestrictionByProperty("includeMemberDetails");
            if (incMemberDetails != null) {
                includeMemberDetails = incMemberDetails.getBooleanValue();
            }
        }
        if (locationReference == null && userId == null) {
            // if these are both null then we default to getting memberships for the current user
            if (currentUserId != null) {
                userId = currentUserId;
            }
        }
        if (locationReference == null && userId == null) {
            // fail if there is still nothing to output
            throw new IllegalArgumentException(
                    "There must be a current user logged in "
                            + "OR you must provide a search with the following restrictions (getting all is not supported): "
                            + "siteId, locationReference, groupId AND (optionally) roleId OR userReference, userId, user");
        }
        List<EntityMember> members = new ArrayList<EntityMember>();
        boolean findByLocation = false;
        if (locationReference != null) {
            // get membership for a location
            findByLocation = true;
            members = getMembers(locationReference);
        } else {
            // get membership for
            if (!includeGroups && !includeSites) {
                throw new IllegalArgumentException(
                        "includesSites and includesGroups cannot both be false");
            }
            // find memberships by userId
            userId = userEntityProvider.findAndCheckUserId(userId, null);
            //SAK-22396 if the user is unknown this will be null
            if (userId == null) {
                throw new IllegalArgumentException("unable to find user with id ("+userId+")");
            }

            boolean userCurrent = userId.equals(currentUserId);

            // Is there a faster way to do this? I really truly hope so -AZ
            // Only if you don't care about getMember details -MJ
            List<Site> allUserSites = siteService.getUserSites(false, userId);

            // Filter out sites where the logged in user of EB does not have view roster status.
            List<Site> sites = new ArrayList<Site>();
            for (Site site: allUserSites) {
                if (siteService.allowViewRoster(site.getId())) {
                    sites.add(site);
                }
            }

            if (includeMemberDetails) {
                for (Site site : sites) {
                    Member sm = site.getMember(userId);
                    if (sm != null) {
                        if (includeSites) {
                            EntityMember em = new EntityMember(sm, site.getReference(), null);
                            members.add(em);
                            siteTypes.put(em.getId(), site.getType());
                        }
                        // also check the groups
                        if (includeGroups) {
                            Collection<Group> groups = site.getGroups();
                            for (Group group : groups) {
                                Member gm = group.getMember(userId);
                                if (gm != null) {
                                    members.add(new EntityMember(gm, group.getReference(), null));
                                }
                            }
                        }
                    }
                } 
            }
            else  {
                Map <String, String> userRoles = authzGroupService.getUserRoles(userId, null);
                for (Site site : sites) {
                    EntityMember em = new EntityMember(userId, site.getReference(), userRoles.get(site.getReference()), true, null); 
                    members.add(em);
                }
            }
        }
        ArrayList<EntityMember> sortedMembers = new ArrayList<EntityMember>();
        int count = 0;
        for (EntityMember em : members) {
            // filter out users and roles
            if (count < search.getStart()) {
                continue;
            } else if (search.getLimit() > 0 && count > search.getLimit()) {
                break; // no more, limit reached
            } else {
                // between the start and limit
                if (roleId != null) {
                    if (!roleId.equals(em.getMemberRole())) {
                        continue;
                    }
                }
                if (findByLocation) {
                    if (userId != null) {
                        if (!userId.equals(em.getUserId())) {
                            continue;
                        }
                    }
                }
                sortedMembers.add(em);
            }
            count++;
        }
        // handle the sorting
        Comparator<EntityMember> memberComparator = new EntityMember.MemberSortName(); // default by
        // sortname
        if (search.getOrders().length > 0) {
            Order order = search.getOrders()[0]; // only one sort allowed
            if ("email".equals(order.getProperty())) {
                memberComparator = new EntityMember.MemberEmail();
            } else if ("displayName".equals(order.getProperty())) {
                memberComparator = new EntityMember.MemberDisplayName();
            } else if ("lastLogin".equals(order.getProperty())) {
                memberComparator = new EntityMember.MemberLastLogin();
            }
        }
        Collections.sort(sortedMembers, memberComparator);
        // TODO reverse sorting?

        // now we put the members into entity data objects
        ArrayList<EntityData> l = new ArrayList<EntityData>();
        for (EntityMember em : sortedMembers) {
                    	
            //SAK-25710 add site type as a property
            Map<String,Object> props = new HashMap<String,Object>();
            String siteType = siteTypes.get(em.getId());
            props.put("siteType", siteType);
                        
            EntityData ed = new EntityData(new EntityReference(PREFIX, em.getId()), null, em, props);
            l.add(ed);
        }
        return l;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        SiteGroup sg = null;
        String roleId = null;
        String userId = null;
        boolean active = true;
        if (entity.getClass().isAssignableFrom(Member.class)) {
            // if someone passes in a Member
            Member member = (Member) entity;
            String locationReference = (String) params.get("locationReference");
            if (locationReference == null) {
                throw new IllegalArgumentException(
                        "Cannot create/update a membership entity from Member without a locationReference in the params");
            }
            sg = findLocationByReference(locationReference);
            roleId = member.getRole().getId();
            userId = userEntityProvider.findAndCheckUserId(member.getUserId(), member.getUserEid());
            active = member.isActive();
        } else if (entity.getClass().isAssignableFrom(EntityMember.class)) {
            // if they instead pass in the EntitySite object
            EntityMember em = (EntityMember) entity;
            sg = findLocationByReference(em.getLocationReference());
            roleId = em.getMemberRole();
            if ((em.getUserId() != null) || (em.getUserEid() != null)) {
                userId = userEntityProvider.findAndCheckUserId(em.getUserId(), em.getUserEid());
            }
            active = em.isActive();
        } else {
            throw new IllegalArgumentException(
                    "Invalid entity for create/update, must be Member or EntityMember object");
        }
        if (roleId == null || "".equals(roleId)) {
            roleId = sg.site.getJoinerRole();
        }

        // SAK-21786
        // set the role to the one passed in, if available
        if (params.get("memberRole") != null) {
            roleId = (String) params.get("memberRole");
        }
        // set the active status to the one passed in, if available
        if (params.get("active") != null) {
            active = Boolean.parseBoolean((String) params.get("active"));
        }

        checkSiteSecurity(sg.site.getId());

        String[] userAuditString;
        List<String[]> userAuditList = new ArrayList<>();
        
        // check for a batch add
        String[] userIds = checkForBatch(params, userId);
        String memberId = "";
        String currentUserId = developerHelperService.getCurrentUserId();
        
        // now add all the memberships
        for (int i = 0; i < userIds.length; i++) {
            if (sg.group == null) {
                // site only
                if (userIds[i].equals(currentUserId) && sg.site.isJoinable()) {
                    try {
                        siteService.join(sg.site.getId());
                    } catch (IdUnusedException e) {
                        throw new IllegalArgumentException("Invalid site: " + sg.site.getId() + ":"
                                + e.getMessage(), e);
                    } catch (PermissionException e) {
                        throw new SecurityException("Current user not allowed to join site: "
                                + sg.site.getId() + ":" + e.getMessage(), e);
                    }
                } else {
                    sg.site.addMember(userIds[i], roleId, active, false);
                    saveSiteMembership(sg.site);
                }
                User user = null;
                // Add change to user_audits_log table.
                try {
                    user = userDirectoryService.getUser(userIds[i]);
                }
                catch (UserNotDefinedException e) {
                    log.error(".createEntity: User with id {} doesn't exist", userIds[i]);
                }
                userAuditString = new String[]{sg.site.getId(),user.getEid(), roleId, UserAuditService.USER_AUDIT_ACTION_ADD,
                                               userAuditRegistration.getDatabaseSourceKey(), userDirectoryService.getCurrentUser().getEid()};
                userAuditList.add(userAuditString);
            } else {
                // group and site
                try {
                    sg.group.insertMember(userIds[i], roleId, active, false);
                    saveGroupMembership(sg.site, sg.group);
                } catch (IllegalStateException e) {
                    log.error(".createEntity: User with id {} cannot be inserted in group with id {} because the group is locked", userIds[i], sg.group.getId());
                }
            }
            if (i == 0) {
                EntityMember em = new EntityMember(userIds[0], sg.locationReference, roleId,
                        active, null);
                memberId = em.getId();
            }
        }

        if (userAuditList.size() > 0) {
            userAuditRegistration.addToUserAuditing(userAuditList);
        }

        if (userIds.length > 1) {
            log.info("Batch add memberships: siteId="
                    + ((sg.site == null) ? "none" : sg.site.getId()) + ",groupId="
                    + ((sg.group == null) ? "none" : sg.group.getId()) + ",userIds="
                    + Search.arrayToString(userIds));

            memberId = "batch:" + memberId;
        }
        return memberId;
    }

    public Object getSampleEntity() {
        return new EntityMember();
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        // same operation for updating memberships, maybe we should check if they exist?
        createEntity(ref, entity, params);
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String mid = ref.getId();
        String[] parts = EntityMember.parseId(mid);
        if (parts == null) {
            throw new IllegalArgumentException(
                    "Invalid membership id ("
                            + mid
                            + "), should be formed like so: 'userId::site:siteId' or 'userId::group:groupId");
        }
        String userId = parts[0];
        SiteGroup sg = findLocationByReference(parts[1]);

        String[] userAuditString;
        List<String[]> userAuditList = new ArrayList<>();
        
        // check for a batch
        String[] userIds = checkForBatch(params, userId);
        for (int i = 0; i < userIds.length; i++) {
            if (sg.group == null) {
                // site only
                Site site = sg.site;

                // Add change to user_audits_log table.
                String role = site.getUserRole(userIds[i]).getId();
                String userEid = null;
                try {
                    userEid = userDirectoryService.getUser(userIds[i]).getEid();
                } catch (UserNotDefinedException e) {
                    log.error(".deleteEntity: User with id {} not defined", userIds[i]);
                }

                userAuditString = new String[]{site.getId(), userEid, role, UserAuditService.USER_AUDIT_ACTION_REMOVE,
                                               userAuditRegistration.getDatabaseSourceKey(), userDirectoryService.getCurrentUser().getEid()};
                userAuditList.add(userAuditString);

                site.removeMember(userIds[i]);
                saveSiteMembership(site);

            } else {
                // group and site
                try {
                    sg.group.deleteMember(userIds[i]);
                    saveGroupMembership(sg.site, sg.group);
                } catch (IllegalStateException e) {
                    log.error(".deleteEntity: User with id {} cannot be deleted from group with id {} because the group is locked", userIds[i], sg.group.getId());
                }
            }
        }

        if (userAuditList.size() > 0) {
            userAuditRegistration.addToUserAuditing(userAuditList);
        }

        if (userIds.length > 1) {
            log.info("Batch remove memberships: siteId="
                    + ((sg.site == null) ? "none" : sg.site.getId()) + ",groupId="
                    + ((sg.group == null) ? "none" : sg.group.getId()) + ",userIds="
                    + Search.arrayToString(userIds));
        }
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

    public EntityMember getMember(String userId, String locationReference) {
        EntityMember em = null;
        Member member = null;
        SiteGroup sg = findLocationByReference(locationReference);
        String currentUserId = developerHelperService.getCurrentUserId();
        if (!userId.equals(currentUserId)) {
            isAllowedAccessMembers(sg.site, sg.group);
        }
        boolean viewHidden = securityService.unlock("roster.viewHidden", sg.site.getReference());
        if (sg.group == null) {
            // site only
            member = sg.site.getMember(userId);
        } else {
            // group and site
            member = sg.group.getMember(userId);
            //see if the user has viewHidden permission at the group level too
            viewHidden = viewHidden || securityService.unlock("roster.viewHidden", sg.group.getReference());
        }
        if (member != null && !privacyManager.findHidden(sg.site.getReference(), new HashSet<String>(Arrays.asList(userId))).contains(userId)) {
            EntityUser eu = userEntityProvider.getUserById(userId);
            em = new EntityMember(member, sg.locationReference, eu);
        }
        return em;
    }

    /**
     * @param locationReference
     *            a site ref with an optional group ref (can look like this:
     *            /site/siteid/group/groupId)
     * @return the list of memberships for the given location and role
     */
    public List<EntityMember> getMembers(String locationReference) {
        ArrayList<EntityMember> l = new ArrayList<EntityMember>();
        Set<Member> members = null;
        SiteGroup sg;
        try {
            sg = findLocationByReference(locationReference);
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Could not find the location based on the ref ("+locationReference+"): " + e, locationReference);
        }
       	isAllowedAccessMembers(sg.site, sg.group);
        boolean viewHidden = viewHidden = securityService.unlock("roster.viewHidden", sg.site.getReference());
        Set<String> hiddenUsers = new HashSet<String>();
        if (sg.group == null) {
            // site only
            members = sg.site.getMembers();
        } else {
            // group and site
            members = sg.group.getMembers();
            //see if user has the ability to view hidden at the group level as well
            viewHidden = viewHidden || securityService.unlock("roster.viewHidden", sg.group.getReference());
        }
        if(!siteService.allowViewRoster(sg.site.getId()) && !viewHidden){
        	//add hidden users to set so we can filter them out
        	Set<String> memberIds = new HashSet<String>();
        	for(Member member : members){
        		memberIds.add(member.getUserId());
        	}
        	hiddenUsers = privacyManager.findHidden(sg.site.getReference(), memberIds);
        }
        // filter out possible invalid/orphaned users (SAK-22396, SAK-17498, SAK-23863)
        for (Member member : members) {
            EntityUser eu = userEntityProvider.getUserById(member.getUserId());
            if (eu != null && !hiddenUsers.contains(member.getUserId())) {
                EntityMember em = new EntityMember(member, sg.locationReference, eu);
                l.add(em);
            }
        }
        return l;
    }

    /**
     * Find a site (and optionally group) by reference
     * 
     * @param locationReference
     * @return a Site and optional group
     * @throws IllegalArgumentException
     *             if they cannot be found for this ref
     */
    public SiteGroup findLocationByReference(String locationReference) {
        SiteGroup holder = new SiteGroup(locationReference);
        if (locationReference.contains("/group/")) {
            // group membership
            String groupId = EntityReference.getIdFromRefByKey(locationReference, "group");
            if (groupId == null || "".equals(groupId)) {
                throw new IllegalArgumentException(
                        "locationReferences for groups must be structured like this: /site/siteid/group/groupId or /group/groupId, could not find group in: "
                                + locationReference);
            }
            locationReference = "/group/" + groupId;
            Group group = siteService.findGroup(groupId);
            // an invalid group ID might be passed which results in a null here
            if (group == null) {
                throw new IllegalArgumentException("No group found for id: "+groupId);
            }
            Site site = group.getContainingSite();
            holder.locationReference = locationReference;
            holder.group = group;
            holder.site = site;
        } else if (locationReference.contains("/site/")) {
            // site membership
            String siteId = EntityReference.getIdFromRefByKey(locationReference, "site");
            Site site = getSiteById(siteId);
            holder.site = site;
        } else {
            throw new IllegalArgumentException(
                    "Do not know how to handle this location reference (" + locationReference
                    + "), only can handle site and group references");
        }
        if (holder.site == null) {
            throw new IllegalArgumentException(
                    "Could not find a site/group with the given reference: " + locationReference);
        }
        return holder;
    }

    /**
     * Look for a batch membership operation
     * 
     * @param params
     * @param userId
     * @return
     */
    protected String[] checkForBatch(Map<String, Object> params, String userId) {
        HashSet<String> userIds = new HashSet<String>();
        if (userId != null) {
            userIds.add(userId);
        }
        if (params != null) {
            List<String> batchUserIds = getListFromValue(params.get("userIds"));
            for (String batchUserId : batchUserIds) {
                String uid = userEntityProvider.findAndCheckUserId(batchUserId, null);
                if (uid != null) {
                    userIds.add(uid);
                }
            }

        }
        if (log.isDebugEnabled())
            log.debug("Received userIds=" + userIds);
        return userIds.toArray(new String[userIds.size()]);
    }

    protected List<String> getListFromValue(Object paramValue) {
        List<String> stringList = new ArrayList<String>();
        if (paramValue != null) {
            if (paramValue.getClass().isArray()) {
                stringList = Arrays.asList((String[]) paramValue);
            } else if (paramValue instanceof String) {
                stringList.add((String) paramValue);
            } else if(paramValue.getClass().isInstance(new ArrayList<String>())) {
            	return (List<String>) paramValue;
            }
        }
        return stringList;
    }

    protected String makeRoleId(String currentRoleId, Site site) {
        String roleId = currentRoleId;
        if (roleId == null || "".equals(roleId)) {
            roleId = site.getJoinerRole();
        }
        return roleId;
    }

    /**
     * @param site
     * @param group
     */
    protected void saveGroupMembership(Site site, Group group) {
        try {
            siteService.saveGroupMembership(site);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Invalid site: " + site.getId() + ":"
                    + e.getMessage(), e);
        } catch (PermissionException e) {
            throw new SecurityException("Current user ("
                    + developerHelperService.getCurrentUserId()
                    + ") not allowed to update site group memberships in group: " + group.getId()
                    + " :" + e.getMessage() + ":" + e.getCause(), e);
        }
    }

    /**
     * @param site
     */
    protected void saveSiteMembership(Site site) {
        checkSiteSecurity(site.getId());
        try {
            siteService.saveSiteMembership(site);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Invalid site: " + site.getId() + ":"
                    + e.getMessage(), e);
        } catch (PermissionException e) {
            throw new SecurityException("Current user ("
                    + developerHelperService.getCurrentUserId()
                    + ") not allowed to update site memberships in site: " + site.getId() + " :"
                    + e.getMessage() + ":" + e.getCause(), e);
        }
    }

    protected Site getSiteById(String siteId) {
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Cannot find site by siteId: " + siteId, e);
        }
        return site;
    }

    /**
     * @param site
     *            the site to check perms in
     * @return true if the current user can view this site
     * @throws SecurityException
     *             if not allowed
     */
    protected boolean isAllowedAccessMembers(Site site, Group g) {
        // check if the current user can access this
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("Anonymous users may not view memberships in ("
                    + site.getReference() + ")");
        } else {
            String siteId = site.getId();
            if (siteService.allowViewRoster(siteId)) {
                return true;
            } else if(g != null && Boolean.TRUE.toString().equals(g.getProperties().getProperty(Group.GROUP_PROP_VIEW_MEMBERS))){
            	return true;
            }else{
            	throw new SecurityException("Memberships in this site (" + site.getReference()
                        + ") are not accessible for the current user: " + userReference);
            }
        }
    }

    /**
     * This contains the site and optionally group for a given reference
     * 
     * @author Aaron Zeckoski (azeckoski @ gmail.com)
     */
    public static class SiteGroup {

        public Site site;
        public Group group;
        public String locationReference;

        public SiteGroup(String locationReference) {
            this.locationReference = locationReference;
        }
    }

    /**
     * This adds the users to the group provided in the site provided
     * 
     * @param site
     *            The site which the group belongs
     * @param group
     *            The group to add the users to
     * @param userIds
     *            The list of Uuids to use
     * NOTE: not used?
     */
    protected void addUsersToGroup(Site site, Group group, List<String> userIds) {
        for (String user : userIds) {
            String userId = user.trim();
            Role role = site.getUserRole(userId);
            Member m = site.getMember(userId);

            if (group.getUserRole(userId) == null && role.getId() != null) {
                // Every user added via this EB is defined as non-provided
                try {
                    group.insertMember(userId, role.getId(), m != null ? m.isActive() : true, false);
                } catch (IllegalArgumentException e) {
                    log.error(".addUsersToGroup: User with id {} cannot be inserted in group with id {} because the group is locked", userId, group.getId());
                }
            }
        }
    }

    /**
     * Only handle Site Info type groups.
     * 
     * @param group
     * @throws IllegalArgumentException
     *             if NOT a Site Info type group
     */
    private void checkGroupType(Group group) {
        if (group != null) {
            try {
                if (!group.getProperties().getBooleanProperty(GROUP_PROP_WSETUP_CREATED)) {
                    throw new IllegalArgumentException(
                            "This type of group (Section Info group) should not be edited by this entity provider. Only Site info groups are allowed.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "This type of group (Section Info group) should not be edited by this entity provider. Only Site info groups are allowed.");
            }
        }
    }

    /**
     * SAK-20828 handle low hanging CSRF blocking
     * @param siteId the sakai site id
     * @throws SecurityException if this site cannot be updated via provider
     */
    private void checkSiteSecurity(String siteId) {
        if (!allowAdminSiteChanges && ADMIN_SITE_ID.equals(siteId)) {
            throw new SecurityException("Admin site membership changes are disabled for security protection against CSRF, you must use the sakai admin UI or enable changes in your sakai config file using "+ADMIN_SITE_CHANGE_ALLOWED+"=true");
        }
    }
    
    public static class JsonUser {
    	private String id = "";
    	private String role = "";
    	
    	public JsonUser() {}
    	public JsonUser(String id, String role) {
    		this.id = id;
    		this.role = role;
    	}
    	
    	public String getId() { return id; }
    	public void setId(String id) {
    		this.id = id;
    	}
    	
    	public String getRole() { return role; }
    	public void setRole(String role) {
    		this.role = role;
    	}
    }

}
