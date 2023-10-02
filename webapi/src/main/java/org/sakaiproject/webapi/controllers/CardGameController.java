/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cardgame.api.CardGameService;
import org.sakaiproject.cardgame.api.model.CardGameStatItem;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.sakaiproject.webapi.beans.CardGameUserRestBean;
import org.sakaiproject.webapi.beans.SimpleGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.sakaiproject.api.privacy.PrivacyManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CardGameController extends AbstractSakaiApiController {


    private static final int MIN_ATTEMPTS_DEFAULT = 5;
    private static final double MIN_HIT_RATIO_DEFAULT = 0.5;
    private static final boolean SHOW_OFFICIAL_PHOTO_DEFAULT = true;
    private static final boolean SKIP_NO_IMAGE_USERS_DEFAULT = true;
    private static final String[] ALLOWED_ROLE_IDS_DEFAULT = new String[] { "access", "Student" };
    private static final String ROSTER_PERM_VIEW_HIDDEN = "roster.viewhidden";
    private static final String ROSTER_PERM_VIEW_ALL_MEMBERS = "roster.viewallmembers";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private CardGameService cardGameService;

    @Autowired
    private PrivacyManager privacyManager;

    @Autowired
    private ProfileImageLogic profileImageLogic;

    private int minAttempts;
    private double minHitRatio;
    private boolean showOfficialPhoto;
    private boolean skipNoImageUsers;


    @PostConstruct
    public void init() {
        minAttempts = serverConfigurationService.getInt("cardgame.minAttempts", MIN_ATTEMPTS_DEFAULT);
        minHitRatio = serverConfigurationService.getDouble("cardgame.minHitRatio", MIN_HIT_RATIO_DEFAULT);
        showOfficialPhoto = serverConfigurationService.getBoolean("cardgame.showOfficialPhoto", SHOW_OFFICIAL_PHOTO_DEFAULT);
        skipNoImageUsers = serverConfigurationService.getBoolean("cardgame.skipNoImageUsers", SKIP_NO_IMAGE_USERS_DEFAULT);
    }

    @GetMapping(value = "/sites/{siteId}/card-game/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConfig(@PathVariable String siteId) {
        checkSakaiSession();
        checkSite(siteId);

        HashMap<String, Object> config = new HashMap<>();
        config.put("minAttempts", minAttempts);
        config.put("minHitRatio", minHitRatio);
        config.put("showOfficialPhoto", showOfficialPhoto);

        return config;
    }

    @GetMapping(value = "/sites/{siteId}/card-game/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CardGameUserRestBean> getUsers(@PathVariable String siteId) {
        Session session = checkSakaiSession();
        Site site = checkSite(siteId);

        String currentUserId = session.getUserId();
        HashMap<String, CardGameStatItem> statItems = cardGameService.findStatItemByPlayerId(currentUserId).stream()
                .collect(Collectors.toMap(statItem -> statItem.getUserId(), statItem -> statItem, (prev, next) -> next, HashMap::new));

        if(!skipNoImageUsers) {
            log.debug("Not skipping any users for having default images");
        }

        Set<User> visibleUsers = userDirectoryService.getUsers(getVisibleUsersIds(currentUserId, site)).stream()
                .filter(Objects::nonNull)
                .filter(user -> {
                    boolean skipUser = skipNoImageUsers && hasDefaultImage(user.getId(), siteId, showOfficialPhoto);
                    if (skipUser) {
                        log.debug("Skipping user[{}] with default image", user.getId());
                    }

                    return !skipUser;
                })
                .sorted(new UserSortNameComparator(true, true))
                .collect(Collectors.toSet());

        return visibleUsers.stream()
                .map(user -> CardGameUserRestBean.of(user, statItems.get(user.getId())))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/sites/{siteId}/card-game/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<SimpleGroup>> getGroups(@PathVariable String siteId) {
        String userId = checkSakaiSession().getUserId();
        Site site = checkSite(siteId);

        return ResponseEntity.ok(getFilteredUserGroups(site, userId));
    }

    @PutMapping(value = "/sites/{siteId}/card-game/users/{userId}/checkResult", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addHitOrMiss(@PathVariable String siteId, @PathVariable String userId,
            @RequestParam(required = true) boolean correct) {
        String currentUserId = checkSakaiSession().getUserId();
        checkSite(siteId);

        if (correct) {
            log.debug("{} made hit  for {}", currentUserId, userId);
            cardGameService.addHit(currentUserId, userId);
        } else {
            log.debug("{} made miss for {}", currentUserId, userId);
            cardGameService.addMiss(currentUserId, userId);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/sites/{siteId}/card-game/users/{userId}/markAsLearned", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> markUserAsLearned(@PathVariable String siteId, @PathVariable String userId) {
        String currentUserId = checkSakaiSession().getUserId();
        checkSite(siteId);

        log.debug("Marking user {} as learned", userId);
        cardGameService.markUserAsLearnedForPlayer(currentUserId, userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/sites/{siteId}/card-game/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> reset() {

        String currentUserId = checkSakaiSession().getUserId();

        log.debug("Resetting game for user {}", currentUserId);
        cardGameService.resetGameForPlayer(currentUserId);

        return ResponseEntity.ok().build();
    }

    private Set<String> getVisibleUsersIds(String userId, Site site) {
        if (StringUtils.isBlank(userId) || site == null) {
            return Collections.emptySet();
        }

        String siteRef = site.getReference();

        Set<Group> userGroups = site.getGroups().stream()
                .filter(group -> group.getMember(userId) != null)
                .collect(Collectors.toSet());

        String[] allowedRoles = getAllowedRoles();

        Predicate<Member> memberFilter = member -> {
            return member != null
                    // Filter for active users
                    && member.isActive()
                    // Filter for allowed roles
                    && ArrayUtils.contains(allowedRoles, member.getRole().getId())
                    // Filter out current user
                    && !StringUtils.equals(member.getUserId(), userId);
        };

        Set<String> userIds;
        if (securityService.unlock(userId, ROSTER_PERM_VIEW_ALL_MEMBERS, siteRef)) {
            userIds = site.getMembers().stream()
                    .filter(memberFilter)
                    .map(Member::getUserId)
                    .collect(Collectors.toSet());

            log.debug("view all; users: {}", userIds.toArray());
            if (!securityService.unlock(userId, ROSTER_PERM_VIEW_HIDDEN, siteRef)) {
                Set<String> hiddenUsersIds = privacyManager.findHidden(siteRef, userIds);
                userIds.removeAll(hiddenUsersIds);
            }
        } else {
            HashMap<String, Set<String>> hiddenUsersByGroup = new HashMap<>();
            for (Group group : userGroups) {
                if (!securityService.unlock(userId, ROSTER_PERM_VIEW_HIDDEN, group.getReference())) {
                    Set<String> groupMemberIds = group.getMembers().stream()
                            .map(Member::getUserId)
                            .collect(Collectors.toSet());
                    Set<String> hiddenUsersIds = privacyManager.findHidden(siteRef, groupMemberIds);
                    hiddenUsersByGroup.put(group.getId(), hiddenUsersIds);
                }
            }

            // Map each group's members to a set of userIds
            userIds = userGroups.stream()
                    .flatMap(group -> group.getMembers().stream()
                            .filter(memberFilter)
                            .filter(member -> {
                                Set<String> hiddenUsers = hiddenUsersByGroup.get(group.getId());
                                return hiddenUsers == null || !hiddenUsers.contains(member.getUserId());
                            })
                            .map(Member::getUserId))
                    .collect(Collectors.toSet());
        }

        return userIds;
    }

    private Set<SimpleGroup> getFilteredUserGroups(Site site, String userId) {
        Set<SimpleGroup> visibleGroups = new HashSet<>();
        if (securityService.unlock(userId, ROSTER_PERM_VIEW_ALL_MEMBERS, site.getReference())) {
            // Get all site groups
            boolean canViewHiddenUsers = securityService.unlock(userId, ROSTER_PERM_VIEW_HIDDEN, site.getReference());

            for (Group group : site.getGroups()) {
                SimpleGroup simpleGroup = new SimpleGroup(group);

                // Remove hidden users if user has no permission to see them (site context)
                if (!canViewHiddenUsers) {
                    Set<String> hiddenUsersIds = privacyManager.findHidden(site.getReference(), simpleGroup.users);
                    simpleGroup.users.removeAll(hiddenUsersIds);
                }

                // Only add groups that (still) have users
                if (!simpleGroup.users.isEmpty()) {
                    visibleGroups.add(simpleGroup);
                }
            }
        } else {
            // Get only site groups that the user is a member of
            Set<Group> userGroups = site.getGroups().stream()
                    .filter(group -> group.getMember(userId) != null)
                    .collect(Collectors.toSet());

            for (Group group : userGroups) {
                SimpleGroup simpleGroup = new SimpleGroup(group);

                // Remove hidden users if user has no permission to see them (group context)
                if (!securityService.unlock(userId, ROSTER_PERM_VIEW_HIDDEN, group.getReference())) {
                    Set<String> hiddenUsersIds = privacyManager.findHidden(site.getReference(), simpleGroup.users);
                    simpleGroup.users.removeAll(hiddenUsersIds);
                }

                // Only add groups that (still) have users
                if (!simpleGroup.users.isEmpty()) {
                    visibleGroups.add(simpleGroup);
                }
            }
        }

        // Remove users without profile image if applicable
        if (skipNoImageUsers) {
            Set<SimpleGroup> emptyGroups = new HashSet<>();

            for (SimpleGroup simpleGroup : visibleGroups) {
                Set<String> userIdsToRemove = new HashSet<>();

                for (String groupUserId : simpleGroup.users) {
                    if (hasDefaultImage(groupUserId, site.getId(), showOfficialPhoto)) {
                        userIdsToRemove.add(groupUserId);
                    }
                }

                simpleGroup.users.removeAll(userIdsToRemove);
                if (simpleGroup.users.isEmpty()) {
                    emptyGroups.add(simpleGroup);
                }
            }

            // Remove groups that are now empty
            visibleGroups.removeAll(emptyGroups);
        }

        // Filter empty groups and return
        return visibleGroups.stream()
                .filter(group -> !group.users.isEmpty())
                .collect(Collectors.toSet());
    }

    private String[] getAllowedRoles() {
        String[] configuredAllowedRoles = serverConfigurationService.getStrings("cardgame.allowedRoles");

        return configuredAllowedRoles != null ? configuredAllowedRoles : ALLOWED_ROLE_IDS_DEFAULT;
    }

    private Boolean hasDefaultImage(String userId, String siteId, boolean official) {
        if (StringUtils.isAnyBlank(userId, siteId)) {
            log.debug("blank userId or siteId");
            return null;
        }

        ProfileImage profileImage;
        if (official) {
            profileImage = profileImageLogic.getOfficialProfileImage(userId, siteId);
        } else {
            profileImage = profileImageLogic.getProfileImage(userId, null, null, ProfileConstants.PROFILE_IMAGE_MAIN, siteId);
        }

        return profileImage.isDefault() || profileImage.isInitials();
    }
}
