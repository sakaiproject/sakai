/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.groupmanager.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.groupmanager.constants.GroupManagerConstants;
import org.sakaiproject.groupmanager.form.MainForm;
import org.sakaiproject.groupmanager.service.SakaiService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {

    @Inject
    private MessageSource messageSource;

    @Autowired
    private SakaiService sakaiService;

    @RequestMapping(value = {"/", "/index"})
    public String showIndex(Model model, HttpServletRequest request, HttpServletResponse response) {
        log.debug("showIndex()");
        
        final Locale locale = sakaiService.getLocaleForCurrentSiteAndUser();    
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, locale);

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Group members for each group, separated by comma
        Map<String, String> groupMemberMap = new HashMap<String, String>();
        // Joinable sets for each group
        Map<String, String> groupJoinableSetMap = new HashMap<String, String>();
        // Joinable set open dates for each joinable group
        Map<String, String> joinableSetOpenDateMap = new HashMap<String, String>();
        // Joinable set close dates for each joinable group
        Map<String, String> joinableSetCloseDateMap = new HashMap<String, String>();
        // In order to display or not the related columns
        boolean anyJoinableSetDate = false;
        // Joinable sets size for each group
        Map<String, String> groupJoinableSetSizeMap = new HashMap<String, String>();
        // Group restrictions for each group
        Map<String, String> groupRestrictionsMap = new HashMap<String, String>();
        // List of groups of the site, excluding the ones which GROUP_PROP_WSETUP_CREATED property is false.
        List<Group> groupList = site.getGroups().stream().filter(group -> group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED) != null && Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED)).booleanValue()).collect(Collectors.toList());
        // Sort the group list by title.
        Collections.sort(groupList, new GroupTitleComparator());

        // Control the groups that are locked by entities
        boolean anyGroupLocked = false;
        List<String> lockedGroupList = new ArrayList<>();
        List<String> lockedForDeletionGroupList = new ArrayList<>();
        Map<String, Map<String, List<String>>> lockedGroupsEntityMap = new HashMap<>();

        // For each group of the site, get the members separated by comma, the joinable sets and the size of the joinable sets.
        for (Group group: groupList) {
            boolean groupLocked = false;
            // Get the group members separated by comma
            StringJoiner stringJoiner = new StringJoiner(", ");
            List<User> groupMemberList = new ArrayList<User>();
            group.getMembers().forEach(member -> {
                Optional<User> memberUserOptional = sakaiService.getUser(member.getUserId());
                if (memberUserOptional.isPresent()) {
                    groupMemberList.add(memberUserOptional.get());
                }
            });
            Collections.sort(groupMemberList, new UserSortNameComparator());
            groupMemberList.forEach(u -> stringJoiner.add(u.getDisplayName()));
            groupMemberMap.put(group.getId(), stringJoiner.toString());
            // Get the joinable sets and add them to the Map
            String joinableSetName = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
            groupJoinableSetMap.put(group.getId(), joinableSetName);
            // Get the datetimes associated to each joinable set
            String joinableSetOpenDate = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_OPEN_DATE);
            String joinableSetCloseDate = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE);
            // Convert from UTC to user's timezone & lang format. Save into map used to fill the table
            joinableSetOpenDate = sakaiService.dateFromUtcToUserTimeZone(joinableSetOpenDate, true);
            joinableSetOpenDateMap.put(joinableSetName, joinableSetOpenDate);
            // Same for close date
            joinableSetCloseDate = sakaiService.dateFromUtcToUserTimeZone(joinableSetCloseDate, true);
            joinableSetCloseDateMap.put(joinableSetName, joinableSetCloseDate);
            // Is there any date?
            if (anyJoinableSetDate == false && (joinableSetOpenDate != null || joinableSetCloseDate != null)) {
                anyJoinableSetDate = true;
            }
            // Get the max number of users who can join each joinable set group
            groupJoinableSetSizeMap.put(group.getId(), group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX));
            
            // Get group restrictions if this group is part of a joinable set
            String joinableSetRestrictions = "";
            if (joinableSetName != null) {
                String allowedGroups = group.getProperties().getProperty(GroupManagerConstants.GROUP_PROP_JOINABLE_ALLOWED_GROUPS);
                if (allowedGroups != null && !allowedGroups.trim().isEmpty()) {
                    // Find the names of the restricted groups
                    List<String> restrictedGroupNames = new ArrayList<>();
                    String[] restrictedGroupIds = allowedGroups.split(",");
                    for (String restrictedGroupId : restrictedGroupIds) {
                        Optional<Group> restrictedGroup = site.getGroups().stream()
                            .filter(g -> g.getId().equals(restrictedGroupId.trim()))
                            .findFirst();
                        if (restrictedGroup.isPresent()) {
                            restrictedGroupNames.add(restrictedGroup.get().getTitle());
                        }
                    }
                    joinableSetRestrictions = String.join(", ", restrictedGroupNames);
                }
            }
            groupRestrictionsMap.put(group.getId(), joinableSetRestrictions);

            // Check if the group is locked for modify or all
            if (RealmLockMode.ALL.equals(group.getRealmLock()) || RealmLockMode.MODIFY.equals(group.getRealmLock())) {
                lockedGroupList.add(group.getId());
                groupLocked = true;
            }

            // Check if the group is locked for deletion
            if (RealmLockMode.ALL.equals(group.getRealmLock()) || RealmLockMode.DELETE.equals(group.getRealmLock())) {
                lockedForDeletionGroupList.add(group.getId());
                groupLocked = true;
            }

            // If the group is locked, provide information about the entities that are locking the group.
            if (groupLocked) {
                anyGroupLocked = true;
                Map<String, List<String>> entityMap = sakaiService.getGroupLockingEntities(group);

                if (!entityMap.containsKey("assignments")) {
                    entityMap.put("assignments", new ArrayList<>());
                }
                if (!entityMap.containsKey("assessments")) {
                    entityMap.put("assessments", new ArrayList<>());
                }
                if (!entityMap.containsKey("joinableSets")) {
                    entityMap.put("joinableSets", new ArrayList<>());
                }

                lockedGroupsEntityMap.put(group.getId(), entityMap);
            }

        }

        // Check for groups that are being used as restrictions in joinable sets
        for (Group group : groupList) {
            String groupId = group.getId();

            Map<String, List<String>> entityMap = lockedGroupsEntityMap.getOrDefault(groupId, new HashMap<>());

            if (!entityMap.containsKey("joinableSets")) {
                entityMap.put("joinableSets", new ArrayList<>());
            }

            boolean isUsedInJoinableSet = site.getGroups().stream()
                .anyMatch(g -> {
                    String allowedGroups = g.getProperties().getProperty(GroupManagerConstants.GROUP_PROP_JOINABLE_ALLOWED_GROUPS);
                    return allowedGroups != null && Arrays.asList(allowedGroups.split(",")).contains(groupId);
                });

            if (isUsedInJoinableSet) {
                // Mark as locked for deletion
                if (!lockedForDeletionGroupList.contains(groupId)) {
                    lockedForDeletionGroupList.add(groupId);
                }

                anyGroupLocked = true;
                List<String> joinableSetTitles = entityMap.get("joinableSets");

                // Find which joinable sets are using this group as restriction
                site.getGroups().stream()
                    .filter(g -> {
                        String allowedGroups = g.getProperties().getProperty(GroupManagerConstants.GROUP_PROP_JOINABLE_ALLOWED_GROUPS);
                        return allowedGroups != null && Arrays.asList(allowedGroups.split(",")).contains(groupId);
                    })
                    .map(g -> g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET))
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(joinableSetTitles::add);
            }

            lockedGroupsEntityMap.put(groupId, entityMap);
        }

        // Add attributes to the model
        model.addAttribute("groupList", groupList);
        model.addAttribute("lockedGroupList", lockedGroupList);
        model.addAttribute("lockedForDeletionGroupList", lockedForDeletionGroupList);
        model.addAttribute("anyGroupLocked", anyGroupLocked);
        model.addAttribute("lockedGroupsEntityMap", lockedGroupsEntityMap);
        model.addAttribute("groupMemberMap", groupMemberMap);
        model.addAttribute("groupJoinableSetMap", groupJoinableSetMap);
        model.addAttribute("joinableSetOpenDateMap", joinableSetOpenDateMap);
        model.addAttribute("joinableSetCloseDateMap", joinableSetCloseDateMap);
        model.addAttribute("anyJoinableSetDate", anyJoinableSetDate);
        model.addAttribute("groupJoinableSetSizeMap", groupJoinableSetSizeMap);
        model.addAttribute("groupRestrictionsMap", groupRestrictionsMap);
        model.addAttribute("mainForm", new MainForm());
        log.debug("Listing {} groups for the site {}.", groupList.size(), site.getId());

        return GroupManagerConstants.INDEX_TEMPLATE;
    }

    @PostMapping(value = "/removeGroups")
    public String removeGroups(@ModelAttribute MainForm deleteGroupsForm, Model model, HttpServletRequest request, HttpServletResponse response) {
        log.debug("removeGroups called with the following groups {}.", deleteGroupsForm.getDeletedGroupList());

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent() || deleteGroupsForm.getDeletedGroupList() == null) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Control if any group has been deleted
        boolean anyGroupDeleted = false;

        // Check if any groups to delete are being used as restrictions in joinable sets
        List<Group> groupsUsedInJoinableSets = new ArrayList<>();
        for (String deletedGroupId : deleteGroupsForm.getDeletedGroupList()) {
            // Check if this group is referenced in any joinable set's allowed groups property
            boolean isUsedInJoinableSet = site.getGroups().stream()
                .anyMatch(group -> {
                    String allowedGroups = group.getProperties().getProperty(GroupManagerConstants.GROUP_PROP_JOINABLE_ALLOWED_GROUPS);
                    return allowedGroups != null && Arrays.asList(allowedGroups.split(",")).contains(deletedGroupId);
                });

            if (isUsedInJoinableSet) {
                sakaiService.findGroupById(deletedGroupId).ifPresent(groupsUsedInJoinableSets::add);
            }
        }

        // If groups are being used in joinable sets, show error and return
        if (!groupsUsedInJoinableSets.isEmpty()) {
            String groups = String.join(", ", groupsUsedInJoinableSets.stream().map(g -> g.getTitle()).collect(Collectors.toList()));
            model.addAttribute("errorMessage", messageSource.getMessage("index.error.cantDeleteGroupsUsedInJoinableSets", new Object[] {groups}, sakaiService.getCurrentUserLocale()));
            return showIndex(model, request, response);
        }

        // For each group, try to delete it from the site
        List<Group> lockedGroups = new ArrayList<>(deleteGroupsForm.getDeletedGroupList().size());
        for (String deletedGroupId : deleteGroupsForm.getDeletedGroupList()) {
            log.debug("Deleting the group {}.", deletedGroupId);
            Optional<Group> groupOptional = sakaiService.findGroupById(deletedGroupId);
            if (groupOptional.isPresent()) {
                // Check if group is locked first, if it's locked don't attempt deletion, just skip to the next group
                Group group = groupOptional.get();
                if (RealmLockMode.ALL.equals(group.getRealmLock()) || RealmLockMode.DELETE.equals(group.getRealmLock())) {
                    lockedGroups.add(group);
                    continue;
                }
                try {
                    String groupId = group.getId();
                    site.deleteGroup(group);
                    anyGroupDeleted = true;
                    sakaiService.postEvent(SamigoConstants.AUTHZ_GROUP_DELETED, groupId);
                } catch (AuthzRealmLockException e) { // This exception is not thrown in the event that the group list UI is stale; see SAK-49139
                    log.error("The group {} is locked and cannot be deleted.", deletedGroupId);
                }
            }
        }

        if (anyGroupDeleted) {
            sakaiService.saveSite(site);
        }

        // If any groups selected to be deleted could not be due to locks, populate an error message indicating which groups and why
        if (!lockedGroups.isEmpty()) {
            String groups = String.join(", ", lockedGroups.stream().map(g -> g.getTitle()).collect(Collectors.toList()));
            model.addAttribute("errorMessage", messageSource.getMessage("index.error.cantDeleteLockedGroup", new Object[] {groups}, sakaiService.getCurrentUserLocale()));
            return showIndex(model, request, response);
        }

        // Return to the list of groups after deleting them.
        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

}
