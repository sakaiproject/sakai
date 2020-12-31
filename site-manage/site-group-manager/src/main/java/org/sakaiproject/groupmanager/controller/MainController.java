/****************************************************************************** 
* Copyright (c) 2020 Apereo Foundation

* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*          http://opensource.org/licenses/ecl2

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 ******************************************************************************/
package org.sakaiproject.groupmanager.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.groupmanager.constants.GroupManagerConstants;
import org.sakaiproject.groupmanager.form.MainForm;
import org.sakaiproject.groupmanager.service.SakaiService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.sakaiproject.util.comparator.UserSortNameComparator;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private SakaiService sakaiService;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private PreferencesService preferencesService;

    @RequestMapping(value = {"/", "/index"})
    public String showIndex(Model model, HttpServletRequest request, HttpServletResponse response) {
        log.debug("showIndex()");
        
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale locale = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
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
        // Joinable sets size for each group
        Map<String, String> groupJoinableSetSizeMap = new HashMap<String, String>();
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
            groupJoinableSetMap.put(group.getId(), group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET));
            // Get the joinable sets and add them to the Map
            groupJoinableSetSizeMap.put(group.getId(), group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX) != null ? group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX) : null);

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
                lockedGroupsEntityMap.put(group.getId(), sakaiService.getGroupLockingEntities(group));
            }

        }

        // Add attributes to the model
        model.addAttribute("groupList", groupList);
        model.addAttribute("lockedGroupList", lockedGroupList);
        model.addAttribute("lockedForDeletionGroupList", lockedForDeletionGroupList);
        model.addAttribute("anyGroupLocked", anyGroupLocked);
        model.addAttribute("lockedGroupsEntityMap", lockedGroupsEntityMap);
        model.addAttribute("groupMemberMap", groupMemberMap);
        model.addAttribute("groupJoinableSetMap", groupJoinableSetMap);
        model.addAttribute("groupJoinableSetSizeMap", groupJoinableSetSizeMap);
        model.addAttribute("mainForm", new MainForm());
        log.debug("Listing {} groups for the site {}.", groupList.size(), site.getId());

        return GroupManagerConstants.INDEX_TEMPLATE;
    }

    @PostMapping(value = "/removeGroups")
    public String removeGroups(@ModelAttribute MainForm deleteGroupsForm, Model model) {
        log.debug("removeGroups called with the following groups {}.", deleteGroupsForm.getDeletedGroupList());

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent() || deleteGroupsForm.getDeletedGroupList() == null) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Control if any group has been deleted
        boolean anyGroupDeleted = false;

        // For each group, try to delete it from the site
        for (String deletedGroupId : deleteGroupsForm.getDeletedGroupList()) {
            log.debug("Deleting the group {}.", deletedGroupId);
            Optional<Group> groupOptional = sakaiService.findGroupById(deletedGroupId);
            if (groupOptional.isPresent()) {
                try {
                    site.deleteGroup(groupOptional.get());
                    anyGroupDeleted=true;
                } catch (AuthzRealmLockException e) {
                    log.error("The group {} is locked and cannot be deleted.", deletedGroupId);
                }
            }
        }

        if (anyGroupDeleted) {
            sakaiService.saveSite(site);
        }

        //Return to the list of groups after deleting them.
        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

}
