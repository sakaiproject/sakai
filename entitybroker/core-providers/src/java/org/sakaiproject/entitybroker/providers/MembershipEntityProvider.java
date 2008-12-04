/**
 * $Id: MembershipEntityProvider.java 51727 2008-09-03 09:00:03Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/impl/src/java/org/sakaiproject/entitybroker/providers/MembershipEntityProvider.java $
 * ServerConfigEntityProvider.java - entity-broker - Jul 17, 2008 2:19:03 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
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
import org.sakaiproject.entitybroker.providers.model.EntityMember;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;


/**
 * This provides access to memberships as entities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class MembershipEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful, ActionsExecutable {

    private static Log log = LogFactory.getLog(MembershipEntityProvider.class);

    private SiteService siteService;
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

    public static String PREFIX = "membership";
    public String getEntityPrefix() {
        return PREFIX;
    }
    
    /**
     * Handle the special needs of UX site membership settings, either getting the current
     * list of site memberships via a GET request, or creating a new batch of site memberships
     * via a POST request. In the case of a POST, special HTTP response headers will be
     * used to communicate success or warning conditions to the client.
     * @param view
     * @param params
     * @return
     */
    @EntityCustomAction(action="site",viewKey="")
    public ActionReturn handleSiteMemberships(EntityView view, Map<String, Object> params) {
        if (log.isDebugEnabled()) log.debug("handleSiteMemberships method=" + view.getMethod() + ", params=" + params);
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
            siteId = (String) params.get("siteId");
            if (siteId == null) {
                throw new IllegalArgumentException("siteId must be set in order to get site memberships, set in params or in the URL /membership/site/siteId");
            }
        }
        String locationReference = "/site/" + siteId;
        
        Map<String, String> extraResponseHeaders = null;
        if (EntityView.Method.POST.name().equals(view.getMethod())) {
            extraResponseHeaders = createBatchMemberships(view, params, locationReference);
        }
        
        List<EntityData> l = getEntities(new EntityReference(PREFIX,""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE, locationReference));
        ActionReturn actionReturn = new ActionReturn(l, Formats.JSON);
        if ((extraResponseHeaders != null) && !extraResponseHeaders.isEmpty()) {
            actionReturn.setHeaders(extraResponseHeaders);
        }
        return actionReturn;
    }

    /**
     * Add members to a site.
     * @param view
     * @param params request parameters including a list of userSearchValues
     * @param locationReference
     * @return headers containing success or warning messages for the client
     */
    public Map<String, String> createBatchMemberships(EntityView view, Map<String, Object> params, String locationReference) {
        SiteGroup sg = findLocationByReference(locationReference);
        String roleId = (String)params.get("memberRole");
        String notificationMessage = (String)params.get("notificationMessage");
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
            for (EntityUser user : users) {
                sg.site.addMember(user.getId(), roleId, active, false);
                if (notificationMessage != null) {
                    /**
                     * TODO Should the From address be the site contact or the "setup.request" Sakai property?
                     * TODO We need to retrieve a localized message title and additional body (if any) instead of hard-coding it.
                     * TODO Any special boilerplate to include site URL or title or description?
                     */
                    
                    emailService.send("setup.request@example.org", user.getEmail(), 
                        "New Site Membership Notification", notificationMessage, null, null, null);
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

    @EntityCustomAction(action="group",viewKey=EntityView.VIEW_LIST)
    public List<?> getGroupMemberships(EntityView view, Map<String, Object> params) {
        String groupId = view.getPathSegment(2);
        if (groupId == null) {
            groupId = (String) params.get("groupId");
            if (groupId == null) {
                throw new IllegalArgumentException("groupId must be set in order to get group memberships, set in params or in the URL /membership/group/groupId");
            }
        }
        List<?> l = getEntities(new EntityReference(PREFIX,""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/group/" + groupId));
        return l;
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
            throw new IllegalArgumentException("Invalid membership id (" + mid + "), should be formed like so: 'userId::site:siteId' or 'userId::group:groupId");
        }
        EntityMember member = getMember(parts[0], parts[1]);
        if (member == null) {
            throw new IllegalArgumentException("Cannot find membership with id: " + mid);
        }
        return member;
    }

    /**
     * Gets the list of all memberships for the current user if no params provided,
     * otherwise gets memberships in a specified location or for a specified user
     */
    public List<EntityData> getEntities(EntityReference ref, Search search) {
        String currentUserId = developerHelperService.getCurrentUserId();
        String userId = null;
        String locationReference = null;
        String roleId = null;
        boolean includeSites = true;
        boolean includeGroups = false;
        if (search == null) {
            search = new Search();
        }
        if (! search.isEmpty()) {
            // process the search
            roleId = (String) search.getRestrictionValueByProperties(new String[] {"role","roleId"});
            Restriction userRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_USER_REFERENCE);
            if (userRes != null) {
                String userRef = userRes.getStringValue();
                userId = EntityReference.getIdFromRef(userRef);
            }
            Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE);
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
        }
        if (locationReference == null && userId == null) {
            // if these are both nul then we default to getting memberships for the current user
            if (currentUserId != null) {
                userId = currentUserId;
            }
        }
        if (locationReference == null && userId == null) {
            // fail if there is still nothing to output
            throw new IllegalArgumentException("There must be a current user logged in " +
                    "OR you must provide a search with the following restrictions (getting all is not supported): " +
                    "siteId, locationReference, groupId AND (optionally) roleId OR userReference, userId, user");
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
                throw new IllegalArgumentException("includesSites and includesGroups cannot both be false");
            }
            // find memberships by userId
            userId = userEntityProvider.findAndCheckUserId(userId, null);
            boolean userCurrent = userId.equals(currentUserId);
            // Is there a faster way to do this? I really truly hope so -AZ
            try {
                if (!userCurrent) {
                    developerHelperService.setCurrentUser("/user/" + userId);
                }
                List<Site> sites = siteService.getSites(SelectionType.ACCESS, null, null, null, null, null);
                for (Site site : sites) {
                    Member sm = site.getMember(userId);
                    if (sm != null) {
                        if (includeSites) {
                            members.add( new EntityMember(sm, site.getReference(), null) );
                        }
                        // also check the groups
                        if (includeGroups) {
                            Collection<Group> groups = site.getGroups();
                            for (Group group : groups) {
                                Member gm = group.getMember(userId);
                                if (gm != null) {
                                    members.add( new EntityMember(gm, group.getReference(), null) );
                                }
                            }
                        }
                    }
                }
            } finally {
                if (!userCurrent) {
                    developerHelperService.restoreCurrentUser();
                }
            }
        }
        ArrayList<EntityMember> sortedMembers = new ArrayList<EntityMember>();
        int count = 0;
        for (EntityMember em : members) {
            // filter out users and roles
            if (count < search.getStart()) {
                continue;
            } else if ( search.getLimit() > 0 && count > search.getLimit() ) {
                break; // no more, limit reached
            } else {
                // between the start and limit
                if (roleId != null) {
                    if (! roleId.equals(em.getMemberRole())) {
                        continue;
                    }
                }
                if (findByLocation) {
                    if (userId != null) {
                        if (! userId.equals(em.getUserId())) {
                            continue;
                        }
                    }
                }
                sortedMembers.add(em);
            }
            count++;
        }
        // handle the sorting
        Comparator<EntityMember> memberComparator = new EntityMember.MemberSortName(); // default by sortname
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
            EntityData ed = new EntityData(new EntityReference(PREFIX,em.getId()), null, em);
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
                throw new IllegalArgumentException("Cannot create/update a membership entity from Member without a locationReference in the params");
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
            throw new IllegalArgumentException("Invalid entity for create/update, must be Member or EntityMember object");
        }
        if (roleId == null || "".equals(roleId)) {
            roleId = sg.site.getJoinerRole();
        }

        // check for a batch add
        String[] userIds = checkForBatch(params, userId);
        // now add all the memberships
        String memberId = "";
        for (int i = 0; i < userIds.length; i++) {
            if (sg.group == null) {
                // site only
                sg.site.addMember(userIds[i], roleId, active, false);
                saveSiteMembership(sg.site);
            } else {
                // group and site
                sg.group.addMember(userIds[i], roleId, active, false);
                saveGroupMembership(sg.site, sg.group);
            }
            if (i == 0) {
                EntityMember em = new EntityMember(userIds[0], sg.locationReference, roleId, active, null);
                memberId = em.getId();
            }
        }
        if (userIds.length > 1) {
            log.info("Batch add memberships: siteId="+((sg.site == null) ? "none" : sg.site.getId())+",groupId="+((sg.group == null) ? "none" : sg.group.getId())
                    +",userIds=" + Search.arrayToString(userIds));

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
            throw new IllegalArgumentException("Invalid membership id (" + mid + "), should be formed like so: 'userId::site:siteId' or 'userId::group:groupId");
        }
        String userId = parts[0];
        SiteGroup sg = findLocationByReference(parts[1]);
        // check for a batch
        String[] userIds = checkForBatch(params, userId);
        for (int i = 0; i < userIds.length; i++) {
            if (sg.group == null) {
                // site only
                sg.site.removeMember(userIds[i]);
                saveSiteMembership(sg.site);
            } else {
                // group and site
                sg.group.removeMember(userIds[i]);
                saveGroupMembership(sg.site, sg.group);
            }
        }
        if (userIds.length > 1) {
            log.info("Batch remove memberships: siteId="+((sg.site == null) ? "none" : sg.site.getId())+",groupId="+((sg.group == null) ? "none" : sg.group.getId())
                    +",userIds=" + Search.arrayToString(userIds));
        }
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }


    public EntityMember getMember(String userId, String locationReference) {
        EntityMember em = null;
        Member member = null;
        SiteGroup sg = findLocationByReference(locationReference);
        isAllowedAccessMembers(sg.site);
        if (sg.group == null) {
            // site only
            member = sg.site.getMember(userId);
        } else {
            // group and site
            member = sg.group.getMember(userId);
        }
        if (member != null) {
            EntityUser eu = userEntityProvider.getUserById(userId);
            em = new EntityMember(member, sg.locationReference, eu);
        }
        return em;
    }

    /**
     * @param locationReference a site ref with an optional group ref (can look like this: /site/siteid/group/groupId)
     * @return the list of memberships for the given location and role
     */
    public List<EntityMember> getMembers(String locationReference) {
        ArrayList<EntityMember> l = new ArrayList<EntityMember>();
        Set<Member> members = null;
        SiteGroup sg = findLocationByReference(locationReference);
        isAllowedAccessMembers(sg.site);
        if (sg.group == null) {
            // site only
            members = sg.site.getMembers();
        } else {
            // group and site
            members = sg.group.getMembers();
        }
        for (Member member : members) {
            EntityUser eu = userEntityProvider.getUserById(member.getUserId());
            EntityMember em = new EntityMember(member, sg.locationReference, eu);
            l.add(em);
        }
        return l;
    }

    /**
     * Find a site (and optionally group) by reference
     * @param locationReference
     * @return a Site and optional group
     * @throws IllegalArgumentException if they cannot be found for this ref
     */
    public SiteGroup findLocationByReference(String locationReference) {
        SiteGroup holder = new SiteGroup(locationReference);
        if (locationReference.contains("/group/")) {
            // group membership
            String groupId = EntityReference.getIdFromRefByKey(locationReference, "group");
            if (groupId == null || "".equals(groupId)) {
                throw new IllegalArgumentException("locationReferences for groups must be structured like this: /site/siteid/group/groupId or /group/groupId, could not find group in: " + locationReference);
            }
            locationReference = "/group/" + groupId;
            Group group = siteService.findGroup(groupId);
            Site site = group.getContainingSite();
            holder.locationReference = locationReference;
            holder.group = group;
            holder.site = site;
        } else if (locationReference.contains("/site/")) {
            // site membership
            EntityReference ref = new EntityReference(locationReference);
            String siteId = ref.getId();
            Site site = getSiteById(siteId);
            holder.site = site;
        } else {
            throw new IllegalArgumentException("Do not know how to handle this location reference ("+locationReference+"), only can handle site and group references");
        }
        if (holder.site == null) {
            throw new IllegalArgumentException("Could not find a site/group with the given reference: " + locationReference);
        }
        return holder;
    }


    /**
     * Look for a batch membership operation
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
        if (log.isDebugEnabled()) log.debug("Received userIds=" + userIds);
        return userIds.toArray(new String[userIds.size()]);
    }
    
    protected List<String> getListFromValue(Object paramValue) {
        List<String> stringList = new ArrayList<String>();
    	if (paramValue != null) {
    	    if (paramValue.getClass().isArray()) {
    	        stringList = Arrays.asList((String[])paramValue);
    	    } else if (paramValue instanceof String) {
    	        stringList.add((String)paramValue);
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
            throw new IllegalArgumentException("Invalid site: " + site.getId() + ":" + e.getMessage(), e);
        } catch (PermissionException e) {
            throw new SecurityException("Current user not allowed to update site group memberships in group: " + group.getId() + ":" + e.getMessage(), e);
        }
    }

    /**
     * @param site
     */
    protected void saveSiteMembership(Site site) {
        try {
            siteService.saveSiteMembership(site);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Invalid site: " + site.getId() + ":" + e.getMessage(), e);
        } catch (PermissionException e) {
            throw new SecurityException("Current user not allowed to update site memberships in site: " + site.getId() + ":" + e.getMessage(), e);
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
     * @param site the site to check perms in
     * @return true if the current user can view this site
     * @throws SecurityException if not allowed
     */
    protected boolean isAllowedAccessMembers(Site site) {
        // check if the current user can access this
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
        	throw new SecurityException("Anonymous users may not view memberships in ("+site.getReference()+")");
        } else {
        	String siteId = site.getId();
        	if (siteService.allowViewRoster(siteId)) {
        		return true;
        	} else {
        		throw new SecurityException("Memberships in this site ("+site.getReference()+") are not accessible for the current user: " + userReference);            	
        	}
        }
    }

    /**
     * This contains the site and optionally group for a given reference
     * 
     * @author Aaron Zeckoski (azeckoski @ gmail.com)
     */
    public class SiteGroup {
        public Site site;
        public Group group;
        public String locationReference;
        public SiteGroup(String locationReference) {
            this.locationReference = locationReference;
        }
    }

}
