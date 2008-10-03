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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
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


    public static String PREFIX = "membership";
    public String getEntityPrefix() {
        return PREFIX;
    }

    @EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
    public List<?> getSiteMemberships(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
            siteId = (String) params.get("siteId");
            if (siteId == null) {
                throw new IllegalArgumentException("siteId must be set in order to get site memberships, set in params or in the URL /membership/site/siteId");
            }
        }
        List<?> l = getEntities(new EntityReference(PREFIX,""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/site/" + siteId));
        return l;
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
    @SuppressWarnings("unchecked")
    public List<?> getEntities(EntityReference ref, Search search) {
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
            if (roleId == null || "".equals(roleId)) {
                roleId = sg.site.getJoinerRole();
            }
            userId = userEntityProvider.findAndCheckUserId(member.getUserId(), member.getUserEid());
            active = member.isActive();
        } else if (entity.getClass().isAssignableFrom(EntityMember.class)) {
            // if they instead pass in the EntitySite object
            EntityMember em = (EntityMember) entity;
            sg = findLocationByReference(em.getLocationReference());
            roleId = em.getMemberRole();
            if (roleId == null || "".equals(roleId)) {
                roleId = sg.site.getJoinerRole();
            }
            userId = userEntityProvider.findAndCheckUserId(em.getUserId(), em.getUserEid());
        } else {
            throw new IllegalArgumentException("Invalid entity for create/update, must be Member or EntityMember object");
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
            log.info("Batch add memberships: siteId="+sg.site.getId()+",groupId="+sg.group == null ? "none" : sg.group.getId()
                    +",roleId="+roleId+",active="+active+", userIds=" + Search.arrayToString(userIds));
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
            log.info("Batch remove memberships: siteId="+sg.site.getId()+",groupId="+sg.group == null ? "none" : sg.group.getId()
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
    @SuppressWarnings("unchecked")
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
            Object uids = params.get("userIds");
            if (uids != null && uids.getClass().isArray()) {
                String[] batchUserIds = (String[]) uids;
                if (batchUserIds.length > 0) {
                    for (int i = 0; i < batchUserIds.length; i++) {
                        String uid = userEntityProvider.findAndCheckUserId(batchUserIds[i], null);
                        if (uid != null) {
                            userIds.add(uid);
                        }
                    }
                }
            }
        }
        return userIds.toArray(new String[userIds.size()]);
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
            if (! siteService.allowViewRoster(site.getId())) {
                throw new SecurityException("Memberships in this site ("+site.getReference()+") are not accessible for the current user: " + userReference);
            }
        }
        return true;
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
