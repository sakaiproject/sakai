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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.groupmanager.constants.GroupManagerConstants;
import org.sakaiproject.groupmanager.form.GroupForm;
import org.sakaiproject.groupmanager.service.SakaiService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteGroupHelper;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.comparator.UserSortNameComparator;

@Slf4j
@Controller
public class GroupController {

    @Inject
    private MessageSource messageSource;

    @Autowired
    private SakaiService sakaiService;

    @RequestMapping(value = "/group")
    public String showGroup(Model model, 
    		@RequestParam(required=false) String groupId,
    		@RequestParam(required=false) String filterByGroupId,
    		@RequestParam(required=false) String currentTitle,
    		@RequestParam(required=false) String currentDescription ) {
        log.debug("showGroup called with groupId {}.", groupId);

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // The form values which are optional.
        GroupForm groupForm = new GroupForm();
        groupForm.setGroupTitle(StringUtils.isNotBlank(currentTitle) ? currentTitle : StringUtils.EMPTY);
        groupForm.setGroupDescription(StringUtils.isNotBlank(currentDescription) ? currentDescription : StringUtils.EMPTY);
        groupForm.setJoinableSetName(StringUtils.EMPTY);
        groupForm.setJoinableSetNumOfMembers(String.valueOf(1));
        groupForm.setGroupAllowPreviewMembership(false);
        groupForm.setGroupUnjoinable(false);

        // The list of sections assigned to the group, only for existing groups.
        List<Member> sectionProvidedUsers = new ArrayList<>();
        // The list of roles assigned to the group, only for existing groups.
        List<String> roleProviderList = new ArrayList<String>();
        // The list of members assigned to the group, only for existing groups.
        List<String> currentGroupMembers = new ArrayList<String>();
        // List of joinable sets that can be assigned to the group
        List<String> joinableSetList = new ArrayList<String>();
        // Full list of the site members.
        List<User> siteMemberList = new ArrayList<User>();
        // Group and Section lists for the group filter.
        List<Group> groupList = new ArrayList<Group>();
        List<Group> sectionList = new ArrayList<Group>();

        // Filter by groups or sections
        site.getGroups().forEach(group -> {
            String wsetupCreated = group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED);
            if (StringUtils.isNotBlank(wsetupCreated) && Boolean.valueOf(wsetupCreated)) {
                groupList.add(group);
            } else {
                sectionList.add(group);
            }
        });

        // Selected group as a filter, display the members of that group only.
        Group filterGroup = null;
        Optional<Group> optionalFilterGroup = sakaiService.findGroupById(filterByGroupId);
        if (StringUtils.isNotEmpty(filterByGroupId) && optionalFilterGroup.isPresent()) {
            filterGroup = optionalFilterGroup.get();
        }
        // Set the filtered group id in the form 
        groupForm.setFilterByGroupId(optionalFilterGroup.isPresent() ? filterByGroupId : StringUtils.EMPTY);

        // The group exists so we should edit it
        if (StringUtils.isNotBlank(groupId)) {
            Optional<Group> optionalGroup = sakaiService.findGroupById(groupId);
            if (optionalGroup.isPresent()) {
                Group group = optionalGroup.get();
                if (RealmLockMode.ALL.equals(group.getRealmLock()) || RealmLockMode.MODIFY.equals(group.getRealmLock())) {
                    log.error("The user {} is trying to modify the locked group {}, returning to main.", sakaiService.getCurrentUserId(), groupId);
                    return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
                }

                // After finding the group, assign all the existing values to the form.
                groupForm.setGroupId(groupId);
                groupForm.setGroupTitle(group.getTitle());
                groupForm.setGroupDescription(group.getDescription());
                groupForm.setGroupAllowViewMembership(group.getProperties().get(Group.GROUP_PROP_VIEW_MEMBERS) != null && Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_VIEW_MEMBERS)).booleanValue());
                String roleProviderId = group.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
                // Get the roles currently assigned to the group.
                roleProviderList = StringUtils.isNotBlank(roleProviderId) ? (ArrayList<String>) SiteGroupHelper.unpack(roleProviderId) : new ArrayList<String>();

                for (Group section : sectionList) {
                    if (roleProviderList.contains(section.getTitle())) {
                        sectionProvidedUsers.addAll(section.getMembers());
                    }
                }

                // Add members to the membership selector only if they were not provided by a role or a section
                for (Member member : group.getMembers()) {
                    if (!roleProviderList.contains(member.getRole().getId()) && sectionProvidedUsers.stream().noneMatch(m -> m.getUserId().equals(member.getUserId()))) {
                        currentGroupMembers.add(member.getUserId());
                    }
                }

                // Add the current members to the existing roles.
                roleProviderList.addAll(currentGroupMembers);

                // Set the list with the roles and the users in the form of an existing group.
                groupForm.setGroupMembers(roleProviderList);

                String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
                if (StringUtils.isNotBlank(joinableSet)) {
                    groupForm.setJoinableSetName(joinableSet);
                    groupForm.setJoinableSetNumOfMembers(StringUtils.isNotBlank(group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX)) ? group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_MAX) : String.valueOf(0));
                    groupForm.setGroupAllowPreviewMembership(Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW)));
                    groupForm.setGroupUnjoinable(Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE)));
                }
            }
        }

        // Get all the joinable sets from all the site groups.
        site.getGroups().forEach(group -> {
            String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
            if (StringUtils.isNotBlank(joinableSet) && !joinableSetList.contains(joinableSet)) {
                joinableSetList.add(joinableSet);
            }
        });
        Collections.sort(joinableSetList);

        // For every member of the site or the filtered group, add it to the selector except if they were provided by a role.
        for (Member member : filterGroup == null ? site.getMembers() : filterGroup.getMembers()) {
            if (!roleProviderList.contains(member.getRole().getId()) && sectionProvidedUsers.stream().noneMatch(m -> m.getUserId().equals(member.getUserId()))) {
                Optional<User> memberUserOptional = sakaiService.getUser(member.getUserId());
                if (memberUserOptional.isPresent()) {
                    siteMemberList.add(memberUserOptional.get());
                }
            }
        }
        //Sort the members of the site by sort name.
        Collections.sort(siteMemberList, new UserSortNameComparator());

        // Add the attributes to the model
        model.addAttribute("groupForm", groupForm);
        model.addAttribute("siteRoleList", site.getRoles().stream().filter(role -> !role.getId().startsWith(".")).collect(Collectors.toList()));
        model.addAttribute("joinableSetList", joinableSetList);
        model.addAttribute("siteMemberList", siteMemberList);
        model.addAttribute("groupList", groupList);
        model.addAttribute("sectionList", sectionList);        

        return GroupManagerConstants.GROUP_TEMPLATE;
    }

    @PostMapping(value = "/saveGroup")
    public String saveGroup(@ModelAttribute GroupForm groupForm, Model model) {
        log.debug("saveGroup called, saving a group with title {}", groupForm.getGroupTitle());

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Variable definition
        Locale userLocale = sakaiService.getCurrentUserLocale();
        String groupId = groupForm.getGroupId();
        String groupTitle = groupForm.getGroupTitle();
        String groupDescription = groupForm.getGroupDescription();
        String filterByGroupId = groupForm.getFilterByGroupId();
        String joinableSetName = groupForm.getJoinableSetName();
        // The group can be new or existing.
        Group group = null;
        List<Member> currentGroupMembers = null;
        List<String> selectedProviderIdList = new ArrayList<String>();
        List<String> addedGroupMemberList = new ArrayList<String>();
        // Build the section list.
        List<Group> sectionList = site.getGroups().stream()
            .filter(
                section -> section.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED) == null || 
                !Boolean.valueOf(section.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED)).booleanValue()
            ).collect(Collectors.toList());

        //Ensure the group title is provided
        if (StringUtils.isBlank(groupTitle)) {
            model.addAttribute("errorMessage", messageSource.getMessage("groups.error.providetitle", null, userLocale));
            return showGroup(model, groupId, filterByGroupId, groupTitle, groupDescription);
        }

        //Ensure the group title is shorter than the maximum allowed
        if (groupTitle.length() > 99) {
            model.addAttribute("errorMessage", messageSource.getMessage("groups.error.titlelength", null, userLocale));
            return showGroup(model, groupId, filterByGroupId, groupTitle, groupDescription);
        }

        // If a joinable set is selected, validate the maximum number of members.
        if (StringUtils.isNoneBlank(joinableSetName, groupForm.getJoinableSetNumOfMembers())) {
            try {
                int joinableSetMaxMembers = Integer.parseInt(groupForm.getJoinableSetNumOfMembers());
                if (joinableSetMaxMembers <= 0 || joinableSetMaxMembers > 999) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException  e) {
                model.addAttribute("errorMessage", messageSource.getMessage("groups.error.maxmembers", null, userLocale));
                return showGroup(model, groupId, filterByGroupId, groupTitle, groupDescription);                
            }
        }

        //Ensure the group title is not in use by other groups.
        if (site.getGroups().stream().anyMatch(g -> !g.getId().equals(groupId) && groupTitle.equalsIgnoreCase(g.getTitle()))) {
            model.addAttribute("errorMessage", messageSource.getMessage("groups.error.sametitle", null, userLocale));
            return showGroup(model, groupId, filterByGroupId, groupTitle, groupDescription);
        }

        // If the group already exists, get it from the site and delete all the members.
        if (StringUtils.isNotBlank(groupId)) {
            group = site.getGroup(groupId);
            // Save the current members first
            currentGroupMembers = new ArrayList<Member>(group.getMembers());
            try {
                group.deleteMembers();
            } catch (AuthzRealmLockException e) {
                log.error("The members of the group {} cannot be deleted because the group is locked.", groupId);
            }
        }

        // If the group is new, create a new one.
        if (group == null) {
            group = site.addGroup();
            currentGroupMembers = new ArrayList<Member>();
            group.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
        }

        // Set the title, description and properties of the group.
        group.setTitle(groupTitle);
        group.setDescription(groupForm.getGroupDescription());
        group.getProperties().addProperty(Group.GROUP_PROP_VIEW_MEMBERS, String.valueOf(groupForm.isGroupAllowViewMembership()));

        // Assign or delete the joinable set
        if (StringUtils.isNotBlank(joinableSetName)) {
            group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, joinableSetName);
            group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_MAX, groupForm.getJoinableSetNumOfMembers());
            group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW, String.valueOf(groupForm.isGroupAllowPreviewMembership()));
            group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE, String.valueOf(groupForm.isGroupUnjoinable()));
        } else {
            group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET);
            group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_MAX);
            group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW);
            group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE);
        }

        // Assign roles or members to the groups.
        for (String selectedGroupMember : groupForm.getGroupMembers()) {
            // If the selected member is a role, add all the members of this role to the group
            if (site.getRoles().stream().anyMatch(s -> selectedGroupMember.equals(s.getId()))) {
                selectedProviderIdList.add(selectedGroupMember);
                for (String memberId : site.getUsersHasRole(selectedGroupMember)) {
                    Member member = site.getMember(memberId);
                    if (member != null) {
                        group.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), false);
                        addedGroupMemberList.add(member.getUserId());
                    }
                }
            } else if (sectionList.stream().anyMatch(s -> selectedGroupMember.equals(s.getTitle()))) {
                // If the selected member is a section, add all the members of this section to the group
                selectedProviderIdList.add(selectedGroupMember);
                Group section = sectionList.stream().filter(s -> selectedGroupMember.equals(s.getTitle())).findAny().orElse(null);
                if (section != null) {
                    for (Member member : section.getMembers()) {
                        group.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), false);
                        addedGroupMemberList.add(member.getUserId());
                    }
                }
            } else {
                // If the selected member is not a role, add it as individual member.                
                Member member = site.getMember(selectedGroupMember);
                if (member != null) {
                    group.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), false);
                    addedGroupMemberList.add(member.getUserId());
                }
            }
        }

        // Update the GROUP_PROP_ROLE_PROVIDERID property of the group with the selected roles.
        if (!selectedProviderIdList.isEmpty()) {
            group.getProperties().addProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID, SiteGroupHelper.pack(selectedProviderIdList));
        } else {
            group.getProperties().removeProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
        }

        sakaiService.saveSite(site);

        //Post Add and Remove events for each added/removed user.
        if (sakaiService.getBooleanProperty(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false)) {
            // Post an event for each individual member added
            for (String addedUserId : addedGroupMemberList) {
                if (currentGroupMembers.stream().noneMatch(member -> addedUserId.equals(member.getUserId()))) {
                    sakaiService.postEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_ADD, addedUserId);
                }
            }

            // Post an event for each individual member removed
            currentGroupMembers.forEach(currentMember -> {
                if (!addedGroupMemberList.contains(currentMember.getUserId())) {
                    // an event for each individual member remove
                    sakaiService.postEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_REMOVE, currentMember.getUserId());
                }
            });
        }

        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

}
