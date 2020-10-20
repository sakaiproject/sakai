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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.groupmanager.constants.GroupManagerConstants;
import org.sakaiproject.groupmanager.form.AutoGroupsForm;
import org.sakaiproject.groupmanager.service.SakaiService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.user.api.User;

@Slf4j
@Controller
public class AutoGroupsController {   

    @Inject
    private MessageSource messageSource;

    @Autowired
    private SakaiService sakaiService;

    @RequestMapping(value = "/autogroups")
    public String showStep1(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm) {
        log.debug("showStep1() called with values {}", autoGroupsForm);

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Fill the model attributes.
        model.addAttribute("siteRoleList", site.getRoles().stream().filter(role -> !role.getId().startsWith(".")).collect(Collectors.toList()));
        model.addAttribute("autoGroupsForm", autoGroupsForm == null ? new AutoGroupsForm() : autoGroupsForm);
        return GroupManagerConstants.AUTO_GROUPS_STEP1_TEMPLATE;
    }

    @PostMapping(value = "/autogroups/submitStep1")
    public String submitStep1(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm) {
        log.debug("submitStep1() called with values {}.", autoGroupsForm);
        
        // Display an error if the selected role list is empty.
        if (autoGroupsForm.getSelectedRoleList() == null || autoGroupsForm.getSelectedRoleList().isEmpty()) {
            model.addAttribute("step1ErrorMessage", messageSource.getMessage("autogroups.step1.error.emptyroles", null, sakaiService.getCurrentUserLocale()));
            return showStep1(model, autoGroupsForm);
        }

        return showStep2(model, autoGroupsForm);
    }
    
    @RequestMapping(value = "/autogroups/step2")
    public String showStep2(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm) {
        log.debug("showStep2() called with values {}", autoGroupsForm);

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Build the section list.
        List<Group> sectionList = site.getGroups().stream()
            .filter(
                group -> group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED) == null || 
                !Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED)).booleanValue()
            ).collect(Collectors.toList());

        //Fill the model attributes.
        model.addAttribute("autoGroupsForm", autoGroupsForm);
        model.addAttribute("sectionList", sectionList);
        return GroupManagerConstants.AUTO_GROUPS_STEP2_TEMPLATE;
    }

    @PostMapping(value = "/autogroups/submitStep2")
    public String submitStep2(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm, @RequestParam(required=false) String wizardAction) {
        log.debug("submitStep2() called with values {} and action {}.", autoGroupsForm, wizardAction);

        // If the submit was done by a back button or link, redirect to the previous step with the wizard values.
        if (StringUtils.isNotBlank(wizardAction)) {
            switch (wizardAction) {
                case GroupManagerConstants.WIZARD_BACK_ACTION:
                case GroupManagerConstants.WIZARD_STEP1_ACTION:
                default:
                    return showStep1(model, autoGroupsForm);
            }
        }

        // Clean the list if the 'dont use sections' option has been selected
        if (GroupManagerConstants.SECTIONS_OPTION_DONT_USE_SECTIONS == autoGroupsForm.getSectionsOption()) {
            autoGroupsForm.setSelectedSectionList(new ArrayList<String>());
            autoGroupsForm.setUseManuallyAddedUsers(false);
        }

        // Display an error if 'use sections' option has been selected and the list is empty.
        if (GroupManagerConstants.SECTIONS_OPTION_USE_SECTIONS == autoGroupsForm.getSectionsOption() && autoGroupsForm.getSelectedSectionList().isEmpty() && !autoGroupsForm.isUseManuallyAddedUsers()) {
            model.addAttribute("step2ErrorMessage", messageSource.getMessage("autogroups.step2.error.emptysections", null, sakaiService.getCurrentUserLocale()));
            return showStep2(model, autoGroupsForm);
        }

        // At least one of the lists, roles or sections, should have a value, perform a validation.
        if (autoGroupsForm.getSelectedRoleList().isEmpty() && autoGroupsForm.getSelectedSectionList().isEmpty() && !autoGroupsForm.isUseManuallyAddedUsers()) {
            model.addAttribute("step2ErrorMessage", messageSource.getMessage("autogroups.step2.error.emptylists", null, sakaiService.getCurrentUserLocale()));
            return showStep2(model, autoGroupsForm);
        }

        // Avoid duplicates
        autoGroupsForm.setSelectedRoleList(autoGroupsForm.getSelectedRoleList().stream().distinct().collect(Collectors.toList()));
        autoGroupsForm.setSelectedSectionList(autoGroupsForm.getSelectedSectionList().stream().distinct().collect(Collectors.toList()));

        return showStep3(model, autoGroupsForm);
    }

    @RequestMapping(value = "/autogroups/step3")
    public String showStep3(Model model, AutoGroupsForm autoGroupsForm) {
        log.debug("showStep3() called with values {}", autoGroupsForm);

        List<String> selectedSectionIdList = autoGroupsForm.getSelectedSectionList();
        // Build the section object list, we need the objects to display the section names.
        List<Group> selectedSectionList = new ArrayList<Group>();
        for (String groupId : selectedSectionIdList) {
            Optional<Group> groupOptional = sakaiService.findGroupById(groupId);
            if (groupOptional.isPresent()) {
                selectedSectionList.add(groupOptional.get());
            }
        }

        // Fill the model attributes
        model.addAttribute("autoGroupsForm", autoGroupsForm);
        model.addAttribute("selectedSectionList", selectedSectionList);
        return GroupManagerConstants.AUTO_GROUPS_STEP3_TEMPLATE;
    }

    @PostMapping(value = "/autogroups/submitStep3")
    public String submitStep3(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm, @RequestParam(required=false) String wizardAction) {
        log.debug("submitStep3() called with values {} and action {}.", autoGroupsForm, wizardAction);

        // If the submit was done by a back button, redirect to the previous step with the wizard values.
        if (StringUtils.isNotBlank(wizardAction)) {
            switch (wizardAction) {
                case GroupManagerConstants.WIZARD_BACK_ACTION:
                case GroupManagerConstants.WIZARD_STEP2_ACTION:
                    return showStep2(model, autoGroupsForm);
                case GroupManagerConstants.WIZARD_STEP1_ACTION:
                default:
                    return showStep1(model, autoGroupsForm);
            }
        }

        // Variable definition
        Locale userLocale = sakaiService.getCurrentUserLocale();

        // Avoid duplicates
        autoGroupsForm.setSelectedRoleList(autoGroupsForm.getSelectedRoleList().stream().distinct().collect(Collectors.toList()));
        autoGroupsForm.setSelectedSectionList(autoGroupsForm.getSelectedSectionList().stream().distinct().collect(Collectors.toList()));

        // Check the options and perform the validations considering the options
        if (GroupManagerConstants.STRUCTURE_CONFIGURATION_MIXTURE == autoGroupsForm.getStructureConfigurationOption()) {
            boolean titleError = false;
            boolean numberError = false;
            String numberErrorMessage = StringUtils.EMPTY;

            if (GroupManagerConstants.SPLIT_OPTIONS_BY_GROUP == autoGroupsForm.getSplitOptions()) {
                titleError = StringUtils.isBlank(autoGroupsForm.getGroupTitleByGroup());
                numberError = autoGroupsForm.getGroupNumberByGroup() <= 0 || autoGroupsForm.getGroupNumberByGroup() > 999;
                numberErrorMessage = messageSource.getMessage("autogroups.step3.group.number.required", null, userLocale);
            } else {
                titleError = StringUtils.isBlank(autoGroupsForm.getGroupTitleByUser());
                numberError = autoGroupsForm.getGroupNumberByUser() <= 0 || autoGroupsForm.getGroupNumberByUser() > 999;
                numberErrorMessage = messageSource.getMessage("autogroups.step3.userspergroup.number.required", null, userLocale);
            }

            // Validate the title is not empty.
            if (titleError) {
                model.addAttribute("splitOptionsError", messageSource.getMessage("autogroups.step3.group.title.required", null, userLocale));
                return showStep3(model, autoGroupsForm);
            }

            // Validate the number of groups is inside the range.
            if (numberError) {
                model.addAttribute("splitOptionsError", numberErrorMessage);
                return showStep3(model, autoGroupsForm);
            }

        }

        return showStep4(model, autoGroupsForm);
    }

    @RequestMapping(value = "/autogroups/step4")
    public String showStep4(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm) {
        log.debug("showStep4() called with values {}", autoGroupsForm);

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Avoid duplicates
        autoGroupsForm.setSelectedRoleList(autoGroupsForm.getSelectedRoleList().stream().distinct().collect(Collectors.toList()));
        autoGroupsForm.setSelectedSectionList(autoGroupsForm.getSelectedSectionList().stream().distinct().collect(Collectors.toList()));

        // Get the data from the previous steps.
        List<String> selectedRoleList = autoGroupsForm.getSelectedRoleList();
        List<String> selectedSectionList = autoGroupsForm.getSelectedSectionList();
        int sectionsOption = autoGroupsForm.getSectionsOption();
        boolean useManuallyAddedUsers = autoGroupsForm.isUseManuallyAddedUsers();
        int structureConfigurationOption = autoGroupsForm.getStructureConfigurationOption();
        int splitOption = autoGroupsForm.getSplitOptions();

        // The map of the groups that will be created with the list of members.
        Map<String, List<String>> autoGroupsMap = new TreeMap<String, List<String>>();
        // The list of members that will compose the auto groups.
        List<Member> filteredMembers = new ArrayList<Member>();
        // We need the user objects to display the user name or the eid.
        Map<String, User> userMap = new HashMap<String, User>();
        // We need the member objects to display the member role.
        Map<String, String> userRoleMap = new HashMap<String, String>();
        // We need the section title of the member
        Map<String, String> sectionMemberMap = new HashMap<String, String>();

        // Remove any potential empty id
        selectedRoleList.removeAll(Arrays.asList(StringUtils.EMPTY, null));
        selectedSectionList.removeAll(Arrays.asList(StringUtils.EMPTY, null));

        // Build the list of users that will compose the groups.
        // If the user decided to not use sections, pick all the members of the site. 
        // If the user decided to use sections, pick all the members of the site that belongs to those sections.
        if (GroupManagerConstants.SECTIONS_OPTION_DONT_USE_SECTIONS == sectionsOption) {
            // Do not filter by section, use all the members of the site filtering by the selected roles
            filteredMembers = site.getMembers().stream().filter(m -> selectedRoleList.contains(m.getRole().getId())).collect(Collectors.toList());
        } else {
            // Pick the members that belong to the selected sections and have the selected roles.
            for (String sectionId : selectedSectionList) {
                Optional<Group> existingGroupOptional = sakaiService.findGroupById(sectionId);
                if (!existingGroupOptional.isPresent()) {
                    continue;
                }

                Group existingGroup = existingGroupOptional.get();

                for (Member member : existingGroup.getMembers()) {
                    if (selectedRoleList.contains(member.getRole().getId())) {
                        String currentSections = sectionMemberMap.get(member.getUserId());
                        // Append any section that the user belongs to
                        if (StringUtils.isBlank(currentSections)) {
                            currentSections = existingGroup.getTitle();
                            filteredMembers.add(member);
                        } else {
                            currentSections += ", " + existingGroup.getTitle();
                        }
                        sectionMemberMap.put(member.getUserId(), currentSections);
                    }
                }
            }

            if (useManuallyAddedUsers) {
                // Add non-provided members filtering by the selected roles.
                List<Member> nonProvidedSiteMembers = site.getMembers().stream().filter(m -> !m.isProvided() && selectedRoleList.contains(m.getRole().getId())).collect(Collectors.toList());
                filteredMembers.addAll(nonProvidedSiteMembers);
            }

        }

        // Fill the user and the member objects to display the names and the eid in the UI.
        for (Member member : filteredMembers) {
            userRoleMap.put(member.getUserId(), member.getRole().getId());
            Optional<User> userOptional = sakaiService.getUser(member.getUserId());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                userMap.put(user.getId(), user);
            }
        }

        if (GroupManagerConstants.STRUCTURE_CONFIGURATION_PURE == structureConfigurationOption) {
            // Pure configuration, create one group per role.
            // Create a group per role using the filtered list of the previous step.
            for (String roleId : selectedRoleList) {
                List<String> usersWithRoleList = new ArrayList<String>();
                List<Member> membersWithRole = filteredMembers.stream().filter(member -> roleId.equals(member.getRole().getId())).collect(Collectors.toList());
                membersWithRole.forEach(member -> { usersWithRoleList.add(member.getUserId()); });
                autoGroupsMap.put(roleId, usersWithRoleList);
            }
        } else {
            // Mixture configuration

            // Shuffle the list first
            Collections.shuffle(filteredMembers);

            if (GroupManagerConstants.SPLIT_OPTIONS_BY_GROUP == splitOption) {
                // Split by group
                String groupPrefix = autoGroupsForm.getGroupTitleByGroup();
                int groupNumber = autoGroupsForm.getGroupNumberByGroup();
                int groupSize = filteredMembers.size() / groupNumber;
                int remainingUsers = filteredMembers.size() % groupNumber;

                // Use one, two or three digits to represent the suffix depending on the size of the number of groups, %01d %02d or %03d
                String indexFormat = String.format("%%0%dd", String.valueOf(groupNumber).length());
                for (int groupIndex = 1 ; groupIndex <= groupNumber; groupIndex++) {
                    String groupTitle = String.format("%s-"+indexFormat, groupPrefix, groupIndex);
                    List<String> randomMemberList = new ArrayList<String>();
                    List<Member> memberSubList = filteredMembers.stream().limit(groupSize).collect(Collectors.toList());
                    filteredMembers.removeAll(memberSubList);

                    // Distribute the rest of the users in the first groups, we can take the first as they are already randomized. 
                    if (groupIndex <= remainingUsers && !filteredMembers.isEmpty()) {
                        memberSubList.add(filteredMembers.get(0));
                        filteredMembers.remove(0);
                    }

                    // If there are remaining members, assign them to the last group.
                    if (groupIndex == groupNumber && !filteredMembers.isEmpty()) {
                        memberSubList.addAll(filteredMembers);
                    }
                    memberSubList.forEach(member -> { randomMemberList.add(member.getUserId()); });
                    autoGroupsMap.put(groupTitle, randomMemberList);
                }
            } else {
                // Split by number of users.
                String groupPrefix = autoGroupsForm.getGroupTitleByUser();
                int groupNumber = autoGroupsForm.getGroupNumberByUser();
                List<List<Member>> partitionedMemberList = ListUtils.partition(filteredMembers, groupNumber);
                int groupIndex = 1;
                // Use one, two or three digits to represent the suffix depending on the size of the number of groups, %01d %02d or %03d
                String indexFormat = String.format("%%0%dd", String.valueOf(partitionedMemberList.size()).length());
                for (List<Member> groupMembers : partitionedMemberList) {
                    String groupTitle = String.format("%s-"+indexFormat, groupPrefix, groupIndex);
                    groupIndex++;
                    List<String> randomMemberList = new ArrayList<String>();
                    groupMembers.forEach(member -> { randomMemberList.add(member.getUserId()); });
                    autoGroupsMap.put(groupTitle, randomMemberList);
                }
            }
        }

        // Fill the model attributes.
        model.addAttribute("autoGroupsForm", autoGroupsForm);
        model.addAttribute("autoGroupsMap", autoGroupsMap);
        model.addAttribute("userMap", userMap);
        model.addAttribute("userRoleMap", userRoleMap);
        model.addAttribute("sectionMemberMap", sectionMemberMap);
        //Serialize as json the group map to send it back to the controller after the confirmation.
        ObjectMapper objectMapper = new ObjectMapper();
        String autoGroupsMapJson = StringUtils.EMPTY;
        try {
            autoGroupsMapJson = objectMapper.writer().writeValueAsString(autoGroupsMap);
        } catch (JsonProcessingException e) {
            log.error("Fatal error serializing the auto groups map.", e);
        }
        model.addAttribute("serializedAutoGroupsMap", autoGroupsMapJson);

        return GroupManagerConstants.AUTO_GROUPS_STEP4_TEMPLATE;
    }

    @PostMapping(value="/autogroups/confirmAutoGroups")
    public String confirmAutoGroups(Model model, @ModelAttribute AutoGroupsForm autoGroupsForm, @RequestParam(required=false) String serializedAutoGroupsMap, @RequestParam(required=false) String wizardAction) {
        log.debug("confirmAutoGroups() called with values {} and action {}.", autoGroupsForm, wizardAction);

        // If the submit was done by a back button, redirect to the previous step with the wizard values.
        if (StringUtils.isNotBlank(wizardAction)) {
            switch (wizardAction) {
                case GroupManagerConstants.WIZARD_BACK_ACTION:
                case GroupManagerConstants.WIZARD_STEP3_ACTION:
                    return showStep3(model, autoGroupsForm);
                case GroupManagerConstants.WIZARD_STEP2_ACTION:
                    return showStep2(model, autoGroupsForm);
                case GroupManagerConstants.WIZARD_STEP1_ACTION:
                default:
                    return showStep1(model, autoGroupsForm);
            }
        }

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        if (StringUtils.isBlank(serializedAutoGroupsMap)) {
            log.error("The auto groups map is empty, aborting the confirmation.");
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        // Deserialize the autogroups map and create the site groups with the members.
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> autoGroupsMap = null;
        try {
            autoGroupsMap = objectMapper.readValue(serializedAutoGroupsMap, new TypeReference<HashMap<String, List<String>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Fatal error deserializing the auto groups map, aborting the process", e);
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        for (Entry<String, List<String>> autoGroup : autoGroupsMap.entrySet()) {
            Group newGroup = site.addGroup();
            newGroup.setTitle(autoGroup.getKey());
            newGroup.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
            newGroup.getProperties().addProperty(Group.GROUP_PROP_VIEW_MEMBERS, String.valueOf(autoGroupsForm.isAllowViewMembership()));
            for (String userId : autoGroup.getValue()) {
                Member member = site.getMember(userId);
                if (member != null) {
                    newGroup.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), false);
                    //Post Add and Remove events for each added/removed user.
                    if (sakaiService.getBooleanProperty(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false)) {
                        sakaiService.postEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_ADD, member.getUserId());
                    }
                }
            }
        }

        sakaiService.saveSite(site);

        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

}
