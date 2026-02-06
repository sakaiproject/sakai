/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BulkPermission;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
@Slf4j
public class UIPermissionsManagerImpl implements UIPermissionsManager {

    private static final Predicate<DBMembershipItem> ifChangeSettings = item -> item.getPermissionLevel().getChangeSettings();
    private static final Predicate<DBMembershipItem> ifDeleteAny = item -> item.getPermissionLevel().getDeleteAny();
    private static final Predicate<DBMembershipItem> ifDeleteOwn = item -> item.getPermissionLevel().getDeleteOwn();
    private static final Predicate<DBMembershipItem> ifMarkAsRead = item -> item.getPermissionLevel().getMarkAsRead();
    private static final Predicate<DBMembershipItem> ifModeratePostings = item -> item.getPermissionLevel().getModeratePostings();
    private static final Predicate<DBMembershipItem> ifMovePosting = item -> item.getPermissionLevel().getMovePosting();
    private static final Predicate<DBMembershipItem> ifNewResponse = item -> item.getPermissionLevel().getNewResponse();
    private static final Predicate<DBMembershipItem> ifNewResponseToResponse = item -> item.getPermissionLevel().getNewResponseToResponse();
    private static final Predicate<DBMembershipItem> ifPostToGradebook = item -> item.getPermissionLevel().getPostToGradebook();
    private static final Predicate<DBMembershipItem> ifRead = i -> i.getPermissionLevel().getRead();
    private static final Predicate<DBMembershipItem> ifReviseAny = item -> item.getPermissionLevel().getReviseAny();
    private static final Predicate<DBMembershipItem> ifReviseOwn = item -> item.getPermissionLevel().getReviseOwn();


    @Setter private AuthzGroupService authzGroupService;
    @Setter private DiscussionForumManager forumManager;
    @Setter private MemoryService memoryService;
    @Setter private PermissionLevelManager permissionLevelManager;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private ToolManager toolManager;
    @Setter private UserDirectoryService userDirectoryService;

    private Cache<String, Set<DBMembershipItem>> membershipItemCache;
    private Cache<String, Set<String>> userGroupMembershipCache;

    public void init() {
        log.info("init()");
        userGroupMembershipCache = memoryService.getCache("org.sakaiproject.component.app.messageforums.ui.UIPermissionsManagerImpl.userGroupMembershipCache");
        membershipItemCache = memoryService.getCache("org.sakaiproject.component.app.messageforums.ui.UIPermissionsManagerImpl.membershipItemCache");
        forumManager.setUiPermissionsManager(this);
    }

    @Override
    public boolean isNewForum() {
        if (isSuperUser()) return true;

        Predicate<DBMembershipItem> ifNewForum = item -> item.getPermissionLevel().getNewForum();
        return getAreaItemsByCurrentUser().stream().anyMatch(ifNewForum);
    }

    @Override
    public boolean isChangeSettings(DiscussionForum forum) {
        if (isSuperUser()) return true;
        // if restricted or instructor belongs to group or is forum owner
        String siteId = getContextSiteId();
        if (securityService.unlock(siteService.SECURE_UPDATE_SITE, siteId)
                && (!forum.getRestrictPermissionsForGroups()
                || isInstructorForAllowedGroup(forum.getId(), true, siteId, getCurrentUserId())
                || forumManager.isForumOwner(forum))) {
            return true;
        }

        return getForumItemsByCurrentUser(forum).stream().anyMatch(ifChangeSettings);
    }

    private boolean isInstructorForAllowedGroup(Long forumId, boolean isForum, String siteId, String userId) {
        if (forumId == null || !isInstructor()) return false;

        List<String> groupTitle;
        if (isForum) {
            groupTitle = forumManager.getAllowedGroupForRestrictedForum(forumId, PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR);
        } else {
            groupTitle = forumManager.getAllowedGroupForRestrictedTopic(forumId, PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR);
        }
        try {
            Site site = siteService.getSite(siteId);
            Set<String> groups = getGroupsWithMember(site, userId);
            return groups.stream().map(site::getGroup).anyMatch(g -> groupTitle.contains(g.getTitle()));
        } catch (IdUnusedException iue) {
            log.warn("Could not fetch site {}, {}", siteId, iue.toString());
        }
        return false;
    }

    @Override
    public boolean isNewTopic(DiscussionForum forum) {
        if (isSuperUser()) return true;
        String siteId = getContextSiteId();
        if (securityService.unlock(siteService.SECURE_UPDATE_SITE, siteId)
                && forum.getRestrictPermissionsForGroups()
                && isInstructorForAllowedGroup(forum.getId(), true, siteId, getCurrentUserId())) {
            return true;
        }
        Predicate<DBMembershipItem> ifNewTopic = item -> item.getPermissionLevel().getNewTopic();
        return getForumItemsByCurrentUser(forum).stream().anyMatch(ifNewTopic);
    }

    @Override
    public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum) {
        return isNewResponse(topic, forum, getCurrentUserId(), getContextId());
    }

    @Override
    public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        if (forum != null
                && !forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {
            return getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifNewResponse);
        }
        return false;
    }

    @Override
    public boolean isNewResponseToResponse(DiscussionTopic topic, DiscussionForum forum) {
        return isNewResponseToResponse(topic, forum, getCurrentUserId(), getContextId());
    }

    @Override
    public boolean isNewResponseToResponse(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        if (forum != null
                && !forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {
            return getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifNewResponseToResponse);
        }
        return false;
    }

    @Override
    public boolean isMovePostings(DiscussionTopic topic, DiscussionForum forum) {
        if (checkBaseConditions(topic, forum)) return true;

        if (forum != null
                && !forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {

            return getTopicItemsByCurrentUser(topic).stream().anyMatch(ifMovePosting.or(ifReviseAny).or(ifReviseOwn));
        }
        return false;
    }

    @Override
    public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum) {
        return isChangeSettings(topic, forum, getCurrentUserId());
    }

    @Override
    public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum, String userId) {
        if (isSuperUser(userId)) return true;
        String siteId = getContextSiteId();

        if (securityService.unlock(userId, siteService.SECURE_UPDATE_SITE, siteId)
                && ((!forum.getRestrictPermissionsForGroups() && !topic.getRestrictPermissionsForGroups())
                || (forum.getRestrictPermissionsForGroups() && isInstructorForAllowedGroup(forum.getId(), true, siteId, userId))
                || (topic.getRestrictPermissionsForGroups() && isInstructorForAllowedGroup(topic.getId(), false, siteId, userId)))) {
            return true;
        }
        // if owner then allow change of settings on the topic or on forum.
        if (forumManager.isTopicOwner(topic, userId)) return true;
        return getTopicItemsByUser(topic, userId).stream().anyMatch(ifChangeSettings);
    }

    @Override
    public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum) {
        return isPostToGradebook(topic, forum, getCurrentUserId());
    }

    @Override
    public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum, String userId) {
        return isPostToGradebook(topic, forum, userId, getContextId());
    }

    @Override
    public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        if (!forum.getDraft() && !topic.getDraft()) {
            return getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifPostToGradebook);
        }
        return false;
    }

    @Override
    public boolean isRead(DiscussionTopic topic, DiscussionForum forum) {
        return isRead(topic, forum, getCurrentUserId());
    }

    @Override
    public boolean isRead(DiscussionTopic topic, DiscussionForum forum, String userId) {
        String contextId = getContextId();
        if (StringUtils.isBlank(contextId)) {
            // not sure if this is even needed as other places don't do this
            contextId = forumManager.getContextForForumById(forum.getId());
        }
        return isRead(topic, forum, userId, contextId);
    }

    @Override
    public boolean isRead(DiscussionTopic topic, DiscussionForum forum, String userId, String siteId) {

        if (userId == null) {
            userId = sessionManager.getCurrentSessionUserId();
        }

        if (checkBaseConditions(topic, forum, userId, "/site/" + siteId)) return true;
        if (forum.getDraft() || topic.getDraft()) return false;

        List<DBMembershipItem> items = getTopicItemsByUser(topic, userId, siteId);
        return items.stream().anyMatch(ifRead);
    }

    @Override
    public boolean isRead(Long topicId, Boolean isTopicDraft, Boolean isForumDraft, String userId, String siteId) {
        if (checkBaseConditions(null, null, userId, "/site/" + siteId)) return true;
        if (isForumDraft || isTopicDraft) return false;

        DiscussionTopic topic = forumManager.getTopicById(topicId);
        return getTopicItemsByUser(topic, userId, siteId).stream().anyMatch(ifRead);
    }

    @Override
    public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum) {
        return isReviseAny(topic, forum, getCurrentUserId(), getContextId());
    }

    @Override
    public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        return (forum.getDraft() == null || !forum.getDraft())
                && !isLocked(forum)
                && (topic.getDraft() == null || !topic.getDraft())
                && !isLocked(topic)
                && getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifReviseAny);
    }

    @Override
    public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum) {
        return isReviseOwn(topic, forum, getCurrentUserId(), getContextId());
    }

    @Override
    public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        if (isLocked(topic)) return false;

        if (!forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {
            return getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifReviseOwn);
        }
        return false;
    }

    @Override
    public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum) {
        return isDeleteAny(topic, forum, getCurrentUserId(), getContextId());
    }

    @Override
    public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        if (isLocked(topic)) return false;

        if (!forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {
            return getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifDeleteAny);
        }
        return false;
    }

    @Override
    public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum) {
        return isDeleteOwn(topic, forum, getCurrentUserId(), getContextId());
    }

    @Override
    public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
        if (checkBaseConditions(topic, forum, userId, contextId)) return true;

        if (isLocked(topic)) return false;

        if (!forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {
            return getTopicItemsByUser(topic, userId, contextId).stream().anyMatch(ifDeleteOwn);
        }
        return false;
    }

    @Override
    public boolean isMarkAsRead(DiscussionTopic topic, DiscussionForum forum) {
        if (checkBaseConditions(topic, forum)) return true;

        if (isLocked(topic)) return false;

        if (!forum.getDraft()
                && !isLocked(forum)
                && !topic.getDraft()
                && !isLocked(topic)) {
            return getTopicItemsByCurrentUser(topic).stream().anyMatch(ifMarkAsRead);
        }
        return false;

    }

    @Override
    public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum) {
        return isModeratePostings(topic, forum, getCurrentUserId());
    }

    @Override
    public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum, String userId) {
        return isModeratePostings(topic, forum, userId, getContextId());
    }

    @Override
    public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum, String userId, String siteId) {
        // NOTE: the forum or topic being locked should not affect a user's ability to moderate,
        // so logic related to the locked status was removed
        if (checkBaseConditions(topic, forum, userId, "/site/" + siteId)) return true;

        return ((forum.getDraft() == null || !forum.getDraft())
                && (topic.getDraft() == null || !topic.getDraft())
                && getTopicItemsByUser(topic, userId, siteId).stream().anyMatch(ifModeratePostings));
    }

    public boolean isModeratePostings(Long topicId, Boolean isForumLocked, Boolean isForumDraft, Boolean isTopicLocked, Boolean isTopicDraft, String userId, String siteId) {
        if (checkBaseConditions(null, null, userId, "/site/" + siteId)) return true;
        DiscussionTopic topic = forumManager.getTopicById(topicId);
        return !isForumDraft && !isTopicDraft && getTopicItemsByUser(topic, userId, siteId).stream().anyMatch(ifModeratePostings);
    }

    @Override
    public boolean isIdentifyAnonAuthors(DiscussionTopic topic) {
        String currentUserId = getCurrentUserId();

        if (isSuperUser(currentUserId)) return true;

        Predicate<DBMembershipItem> ifIdentifyANonAuthors = i -> i.getPermissionLevel().getIdentifyAnonAuthors();
        return getTopicItemsByUser(topic, currentUserId, getContextId()).stream().anyMatch(ifIdentifyANonAuthors);
    }

    @Override
    public List<String> getCurrentUserMemberships() {
        return getCurrentUserMemberships(getContextId());
    }

    @Override
    public List<String> getCurrentUserMemberships(String siteId) {
        List<String> userMemberships = new ArrayList<>();
        // first, add the user's role
        String currentUserRole = getCurrentUserRole(siteId);
        if (StringUtils.isNotBlank(currentUserRole)) {
            userMemberships.add(currentUserRole);
        }
        // now, add any groups the user is a member of
        try {
            Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
            Set<String> groups = getGroupsWithMember(site, getCurrentUserId());
            groups.stream().map(site::getGroup).filter(Objects::nonNull).map(Group::getTitle).forEach(userMemberships::add);
        } catch (IdUnusedException iue) {
            log.warn("Could not fetch site {}, {}", siteId, iue.toString());
        }

        return userMemberships;
    }

    private List<DBMembershipItem> getAreaItemsByCurrentUser() {
        List<DBMembershipItem> areaItems = new ArrayList<>();

        Set<DBMembershipItem> areaMemberships = getAreaMemberships(getContextId());
        areaItems.add(forumManager.getDBMember(areaMemberships, getCurrentUserRole(), MembershipItem.TYPE_ROLE));

        // for group awareness
        String siteId = getContextId();
        try {
            Site currentSite = siteService.getSite(siteId);
            getGroupsWithMember(currentSite, getCurrentUserId()).stream().map(currentSite::getGroup)
                    .map(g -> forumManager.getDBMember(areaMemberships, g.getTitle(), MembershipItem.TYPE_GROUP))
                    .forEach(areaItems::add);
        } catch (IdUnusedException iue) {
            log.warn("Could not fetch site {}, {}", siteId, iue.toString());
        }

        return areaItems;
    }

    @Override
    public Set<DBMembershipItem> getAreaItemsSet(Area area) {
        Set<DBMembershipItem> areaItems = new HashSet<>();
        Set<DBMembershipItem> allAreaSet = getAreaMemberships(getContextId());

        Predicate<DBMembershipItem> ifSameArea = item -> ((DBMembershipItemImpl) item).getArea() != null
                && area.getId() != null
                && area.getId().equals(((DBMembershipItemImpl) item).getArea().getId());
        allAreaSet.stream().filter(ifSameArea).forEach(areaItems::add);
        return areaItems;
    }

    private List<DBMembershipItem> getForumItemsByCurrentUser(DiscussionForum forum) {
        List<DBMembershipItem> forumItems = new ArrayList<>();

        Set<DBMembershipItem> forumItemsInThread = getForumMemberships(forum.getArea());
        Set<DBMembershipItem> thisForumItemSet = new HashSet<>();

        Predicate<DBMembershipItem> ifSameForum = item -> ((DBMembershipItemImpl)item).getForum() != null
                && forum.getId() != null
                && forum.getId().equals(((DBMembershipItemImpl)item).getForum().getId());
        forumItemsInThread.stream().filter(ifSameForum).forEach(thisForumItemSet::add);

        if (thisForumItemSet.isEmpty() && forum.getTopicsSet() == null && ".anon".equals(forum.getCreatedBy()) && forumManager.getAnonRole()) {
            forum.getMembershipItemSet().stream().filter(item -> ".anon".equals(item.getName())).forEach(thisForumItemSet::add);
        }

        forumItems.add(forumManager.getDBMember(thisForumItemSet, getCurrentUserRole(), MembershipItem.TYPE_ROLE));

        //  for group awareness
        String siteId = getContextId();
        try {
            Site site = siteService.getSite(siteId);
            Set<String> groups = getGroupsWithMember(site, getCurrentUserId());

            if (groups != null) {
                groups.stream().map(site::getGroup)
                        .map(g -> forumManager.getDBMember(thisForumItemSet, g.getTitle(), MembershipItem.TYPE_GROUP))
                        .filter(Objects::nonNull)
                        .forEach(forumItems::add);
            }
        } catch (IdUnusedException iue) {
            log.warn("Could not fetch site {} when attempting to add group information for forum {}, {}", siteId, forum.getId(), iue.toString());
        }
        return forumItems;
    }

    public Set<DBMembershipItem> getForumItemsSet(DiscussionForum forum) {
        Set<DBMembershipItem> forumItems = new HashSet<>();
        Set<DBMembershipItem> allForumSet = getForumMemberships(forum.getArea());
        Predicate<DBMembershipItem> ifSameForum = item -> ((DBMembershipItemImpl) item).getForum() != null
                && forum.getId() != null
                && forum.getId().equals(((DBMembershipItemImpl) item).getForum().getId());
        allForumSet.stream().filter(ifSameForum).forEach(forumItems::add);
        return forumItems;
    }

    private Area getTopicForumArea(DiscussionTopic topic) {
        return topic.getBaseForum() != null ? topic.getBaseForum().getArea() : topic.getOpenForum().getArea();
    }
    
    private List<DBMembershipItem> getTopicItemsByCurrentUser(DiscussionTopic topic) {
        return getTopicItemsByUser(topic, getCurrentUserId());
    }

    private List<DBMembershipItem> getTopicItemsByUser(DiscussionTopic topic, String userId) {
        return getTopicItemsByUser(topic, userId, getContextId());
    }

    private List<DBMembershipItem> getTopicItemsByUser(DiscussionTopic topic, String userId, String siteId) {
        List<DBMembershipItem> topicItems = new ArrayList<>();

        Set<DBMembershipItem> topicItemsInThread = getTopicMemberships(getTopicForumArea(topic));
        Set<DBMembershipItem> thisTopicItemSet = new HashSet<>();

        Predicate<DBMembershipItem> ifTopicIsNonNullAndEqualsTopicId = item -> ((DBMembershipItemImpl) item).getTopic() != null
                && ((DBMembershipItemImpl) item).getTopic().getId().equals(topic.getId());
        topicItemsInThread.stream().filter(ifTopicIsNonNullAndEqualsTopicId).forEach(thisTopicItemSet::add);

        topicItems.add(forumManager.getDBMember(thisTopicItemSet, getUserRole(siteId, userId), MembershipItem.TYPE_ROLE, "/site/" + siteId));

        //for group awareness
        try {
            Site currentSite = siteService.getSite(siteId);
            Set<String> groups = getGroupsWithMember(currentSite, userId);
            if (groups != null) {
                groups.stream().map(currentSite::getGroup)
                        .map(g -> forumManager.getDBMember(thisTopicItemSet, g.getTitle(), MembershipItem.TYPE_GROUP, "/site/" + siteId))
                        .filter(Objects::nonNull)
                        .forEach(topicItems::add);
            }
        } catch (Exception iue) {
            log.warn("Could not fetch site {} when attempting to add group information for topic {}, {}", siteId, topic.getId(), iue.toString());
        }

        return topicItems;
    }



    @Override
    public Set<DBMembershipItem> getTopicItemsSet(DiscussionTopic topic) {
        Set<DBMembershipItem> topicItems = new HashSet<>();
        Set<DBMembershipItem> allTopicSet = getTopicMemberships(getTopicForumArea(topic));
        Predicate<DBMembershipItem> ifSameTopic = item -> ((DBMembershipItemImpl) item).getTopic() != null
                && topic.getId() != null
                && topic.getId().equals(((DBMembershipItemImpl) item).getTopic().getId());
        allTopicSet.stream().filter(ifSameTopic).forEach(topicItems::add);
        return topicItems;
    }

    @Override
    public BulkPermission getBulkPermissions(DiscussionTopic topic, DiscussionForum forum) {
        BulkPermission permission = new BulkPermission();

        String userId = getCurrentUserId();
        String siteId = getContextId();

        boolean ifBaseConditions = checkBaseConditions(topic, forum, userId, "/site/" + siteId);
        if (ifBaseConditions) {
            permission.setAllPermissions(true);
            return permission;
        }

        boolean ifTopicOwner = topic != null && forumManager.isTopicOwner(topic, userId);
        boolean ifLockedTopic = isLocked(topic);
        boolean ifLockedForum = isLocked(forum);
        boolean ifDraftTopic = topic != null && (topic.getDraft() != null && topic.getDraft());
        boolean ifDraftForum = forum != null && (forum.getDraft() != null && forum.getDraft());

        Collection<DBMembershipItem> topicItemsByUser = getTopicItemsByUser(topic, userId);

        permission.setChangeSettings(ifTopicOwner || topicItemsByUser.stream().anyMatch(ifChangeSettings));
        permission.setDeleteAny(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifDeleteAny));
        permission.setDeleteOwn(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifDeleteOwn));
        permission.setMarkAsRead(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifMarkAsRead));
        permission.setModeratePostings(!ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifModeratePostings));
        permission.setMovePostings(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifMovePosting));
        permission.setNewResponse(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifNewResponse));
        permission.setNewResponseToResponse(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifNewResponseToResponse));
        permission.setPostToGradebook(!ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifPostToGradebook));
        permission.setRead(!ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifRead));
        permission.setReviseAny(!ifLockedTopic && !ifLockedForum && !ifDraftForum && !ifDraftTopic && topicItemsByUser.stream().anyMatch(ifReviseAny));
        permission.setReviseOwn(!ifLockedTopic && (!ifLockedForum && !ifDraftForum && !ifDraftTopic) && topicItemsByUser.stream().anyMatch(ifReviseOwn));

        return permission;
    }

    @Override
    public BulkPermission getBulkPermissions(DiscussionForum forum) {
        BulkPermission permission = new BulkPermission();
        permission.setChangeSettings(isChangeSettings(forum));
        permission.setNewTopic(isNewTopic(forum));
        return permission;
    }

    public boolean isInstructor() {
        return isInstructor(userDirectoryService.getCurrentUser());
    }

    private boolean isInstructor(User user) {
        if (user != null) return securityService.unlock(user, "site.upd", getContextSiteId());
        return false;
    }

    private String getContextSiteId() {
        return ("/site/" + getContextId());
    }

    private String getCurrentUserId() {
        if (TestUtil.isRunningTests()) return "test-user";
        String userId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(userId) && getAnonRole()) return ".anon";
        return userId;
    }

    private String getCurrentUserRole() {
        return getCurrentUserRole(getContextId());
    }

    private String getCurrentUserRole(String siteId) {
        if (authzGroupService.getUserRole(getCurrentUserId(), "/site/" + siteId) == null
                && sessionManager.getCurrentSessionUserId() == null
                && getAnonRole(siteId)) {
            return ".anon";
        }
        return authzGroupService.getUserRole(getCurrentUserId(), "/site/" + siteId);
    }

    private String getUserRole(String siteId, String userId) {
        String userRole = authzGroupService.getUserRole(userId, "/site/" + siteId);

        // if user role is still null at this point, check for .anon
        if (userRole == null && userId == null && getAnonRole("/site/" + siteId)) {
            return ".anon";
        }

        return userRole;
    }

    public boolean getAnonRole() {
        return forumManager.getAnonRole();
    }

    public boolean getAnonRole(String contextSiteId) {
        return forumManager.getAnonRole(contextSiteId);
    }

    private String getContextId() {
        if (TestUtil.isRunningTests()) return "test-context";
        return toolManager.getCurrentPlacement().getContext();
    }

    private boolean isSuperUser() {
        return isSuperUser(getCurrentUserId());
    }


    private boolean isSuperUser(String userId) {
        return securityService.isSuperUser(userId);
    }


    private boolean checkBaseConditions(DiscussionTopic topic, DiscussionForum forum) {
        return checkBaseConditions(topic, forum, getCurrentUserId(), getContextId());
    }


    private boolean checkBaseConditions(DiscussionTopic topic, DiscussionForum forum, String userId, String contextSiteId) {
        if (isSuperUser(userId)) return true;

        // if restricted and belongs to group
        return (forum != null && forum.getRestrictPermissionsForGroups() && isInstructorForAllowedGroup(forum.getId(), true, contextSiteId, userId))
                || (topic != null && topic.getRestrictPermissionsForGroups() && isInstructorForAllowedGroup(topic.getId(), false, contextSiteId, userId));
    }

    private boolean isLockedAfterClose(DiscussionForum forum) {
        if (forum == null) return false;
        if (!Boolean.TRUE.equals(forum.getAvailabilityRestricted())) return false;
        if (!Boolean.TRUE.equals(forum.getLockedAfterClosed())) return false;
        Date closeDate = forum.getCloseDate();
        return closeDate != null && closeDate.before(new Date());
    }

    private boolean isLockedAfterClose(DiscussionTopic topic) {
        if (topic == null) return false;
        if (!Boolean.TRUE.equals(topic.getAvailabilityRestricted())) return false;
        if (!Boolean.TRUE.equals(topic.getLockedAfterClosed())) return false;
        Date closeDate = topic.getCloseDate();
        return closeDate != null && closeDate.before(new Date());
    }

    private boolean isLocked(DiscussionForum forum) {
        if (forum == null) return true;
        if (Boolean.TRUE.equals(forum.getLocked())) return true;
        return isLockedAfterClose(forum);
    }

    private boolean isLocked(DiscussionTopic topic) {
        if (topic == null) return true;
        if (Boolean.TRUE.equals(topic.getLocked())) return true;
        return isLockedAfterClose(topic);
    }

    public void clearMembershipsFromCacheForArea(Area area) {
        if (area == null || area.getId() == null) return;
        String areaId = area.getId().toString();
        membershipItemCache.remove("area_" + areaId);
        membershipItemCache.remove("forum_" + areaId);
        membershipItemCache.remove("topic_" + areaId);
    }

    private Set<DBMembershipItem> getTopicMemberships(Area area) {
        if (area == null) return Collections.emptySet();
        String topicCacheKey = "topic_" + area.getId();
        Set<DBMembershipItem> cachedTopicMemberships = membershipItemCache.get(topicCacheKey);
        if (cachedTopicMemberships == null) {
            cachedTopicMemberships = new HashSet<>(permissionLevelManager.getAllMembershipItemsForTopicsForSite(area.getId()));
            membershipItemCache.put(topicCacheKey, cachedTopicMemberships);
        }
        return cachedTopicMemberships;
    }

    private Set<DBMembershipItem> getForumMemberships(Area area) {
        if (area == null) return Collections.emptySet();
        String forumCacheKey = "forum_" + area.getId();
        Set<DBMembershipItem> cachedForumMemberships = membershipItemCache.get(forumCacheKey);
        if (cachedForumMemberships == null) {
            cachedForumMemberships = new HashSet<>(permissionLevelManager.getAllMembershipItemsForForumsForSite(area.getId()));
            membershipItemCache.put(forumCacheKey, cachedForumMemberships);
        }
        return cachedForumMemberships;
    }

    private Set<DBMembershipItem> getAreaMemberships(String siteId) {
        if (StringUtils.isNotBlank(siteId)) {
            Area area = forumManager.getDiscussionForumArea(siteId);
            if (area != null) {
                String areaSiteCacheKey = "area_" + area.getId();
                Set<DBMembershipItem> cachedAreaMemberships = membershipItemCache.get(areaSiteCacheKey);
                if (cachedAreaMemberships == null) {
                    cachedAreaMemberships = area.getMembershipItemSet();
                    membershipItemCache.put(areaSiteCacheKey, cachedAreaMemberships);
                }
                return cachedAreaMemberships;
            }
        }
        return Collections.emptySet();
    }

    public Set<String> getGroupsWithMember(Site site, String userId) {
        Set<String> groupIds = new HashSet<>();
        if (site != null && StringUtils.isNotBlank(userId)) {
            String cacheKey = site.getReference() + "/" + userId;
            Set<String> cachedGroupIds = userGroupMembershipCache.get(cacheKey);
            if (cachedGroupIds == null) {
                Collection<Group> groups = site.getGroupsWithMember(userId);
                groupIds = groups.stream().map(Group::getId).collect(Collectors.toSet());
                userGroupMembershipCache.put(cacheKey, Collections.unmodifiableSet(groupIds));
            } else {
                groupIds.addAll(cachedGroupIds);
            }
        }
        return groupIds;
    }
}
