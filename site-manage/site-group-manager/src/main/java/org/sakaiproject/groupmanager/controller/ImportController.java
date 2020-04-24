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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.groupmanager.constants.GroupManagerConstants;
import org.sakaiproject.groupmanager.service.SakaiService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.user.api.User;

@Slf4j
@Controller
public class ImportController {

    private final String BULK_LINE_DELIMITER = "\r\n";
    private final String BULK_FIELD_DELIMITER = ",";

    @Inject
    private MessageSource messageSource;

    @Autowired
    private SakaiService sakaiService;

    @RequestMapping(value = "/import")
    public String showImport(Model model) {
        log.debug("showImport()");
        return GroupManagerConstants.IMPORT_TEMPLATE;
    }

    @PostMapping(value = "/importGroups", consumes = "multipart/form-data")
    public String showImportGroups(@RequestParam(required=false) String groupUploadedText, Model model, HttpServletRequest req) {
        log.debug("showImportGroups called with value {}", groupUploadedText);

        // Variable definition
        Locale userLocale = sakaiService.getCurrentUserLocale();
        Map<String, List<String>> importedGroupMap = new HashMap<String, List<String>>();
        String uploadedText = StringUtils.EMPTY;
        String groupFileUploadedText = StringUtils.EMPTY;

        //Check the uploaded file and contents.
        FileItem uploadedFileItem = (FileItem) req.getAttribute("groupUploadFile");
        if (uploadedFileItem.getSize() > 0) {
            try (Scanner scanner = new Scanner(uploadedFileItem.getInputStream(), StandardCharsets.UTF_8.name())) {
                groupFileUploadedText = scanner.useDelimiter("\\A").next();
            } catch (Exception e) {
                log.error("The file {} provided is not valid.", uploadedFileItem.getName());
            }
        }

        // Check if both options are blank and return an error message
        if (StringUtils.isAllBlank(groupUploadedText, groupFileUploadedText)) {
            return returnImportError(model, "import.error.inputrequired", userLocale);
        }        

        // Process the submitted texts, combine the uploaded and the file into one String
        uploadedText = String.format("%s\r\n%s", groupUploadedText, groupFileUploadedText);

        String[] lineArray = uploadedText.split(BULK_LINE_DELIMITER);
        for (String line : lineArray) {

            if (StringUtils.isBlank(line)) {
                continue;
            }

            String[] lineContentArray = line.split(BULK_FIELD_DELIMITER);

            //Each line must contain a groupTitle and a userEid
            if (lineContentArray.length == 2) {
                String groupTitle = StringUtils.trimToNull(lineContentArray[0]);
                String userEid = StringUtils.trimToNull(lineContentArray[1]);

                if (StringUtils.isAnyBlank(groupTitle, userEid)) {
                    // One of the items of the line is blank, redirect to the import form again displaying an error. 
                    return returnImportError(model, "import.error.wrongformat", userLocale);
                }

                if (groupTitle.length() > 99) {
                    // One of the items of the line has more than 99 characters, redirect to the import form again displaying an error. 
                    return returnImportError(model, "import.error.titlelength", userLocale);
                }

                if (importedGroupMap.get(groupTitle) != null) {
                    // If the map contains an entry for that group, add the user to the list
                    importedGroupMap.get(groupTitle).add(userEid);
                } else {
                    // If the map does not contain an entry for that group, create a list and add the member to the list
                    List<String> newUserlist = new ArrayList<String>();
                    newUserlist.add(userEid);
                    importedGroupMap.put(groupTitle, newUserlist);
                }
            } else {
                // One line does not contain two items, redirect to the import form again displaying an error.
                return returnImportError(model, "import.error.wrongformat", userLocale);
            }
        }

        //Redirect to the confirmation page once the map are correct
        return showImportConfirmation(model, importedGroupMap);
    }
    
    @RequestMapping(value = "/importConfirmation")
    public String showImportConfirmation(Model model, Map<String, List<String>> importedGroupMap) {
        log.debug("showImportConfirmation() called with {} items to import.", importedGroupMap.entrySet().size());

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // List of groups of the site, excluding the ones which GROUP_PROP_WSETUP_CREATED property is false.
        List<Group> groupList = (List<Group>) site.getGroups().stream().filter(group -> group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED) != null && Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED)).booleanValue()).collect(Collectors.toList());
        // Variable definition that will be sent to the model
        Map<String, Boolean> importedGroups = new HashMap<String, Boolean>();
        Map<String, String> nonExistingMemberMap = new HashMap<String, String>();
        Map<String, String> nonMemberMap = new HashMap<String, String>();
        Map<String, String> existingMemberMap = new HashMap<String, String>();
        Map<String, String> newMemberMap = new HashMap<String, String>();
        boolean membershipErrors = false;

        //For each entry we must process the members and catalogue them
        for (Entry<String, List<String>> importedGroup : importedGroupMap.entrySet()) {
            StringJoiner nonExistingUsers = new StringJoiner(", ");
            StringJoiner nonMemberUsers = new StringJoiner(", ");
            StringJoiner existingMembers = new StringJoiner(", ");
            StringJoiner newMembers = new StringJoiner(", ");

            String groupTitle = importedGroup.getKey();
            Optional<Group> existingGroupOptional = groupList.stream().filter(g -> groupTitle.equalsIgnoreCase(g.getTitle())).findAny();

            // The UI shows a message if the group already exists.
            importedGroups.put(groupTitle, Boolean.valueOf(existingGroupOptional.isPresent()));
            List<String> importedGroupUsers = importedGroup.getValue();

            //For every imported user, check if exists, if is member of the site, if already a member or is new. 
            for (String userEid : importedGroupUsers) {
                Optional<User> userOptional = sakaiService.getUserByEid(userEid);
                
                // The user doesn't exist in Sakai.
                if (!userOptional.isPresent()) {
                    nonExistingUsers.add(userEid);
                    membershipErrors = true;
                    continue;
                }

                User user = userOptional.get();
                String userDisplayName = String.format("%s (%s)", user.getDisplayName(), user.getEid());

                //The user is not a member of the site.
                if (site.getMember(user.getId()) == null) {
                    nonMemberUsers.add(userDisplayName);
                    membershipErrors = true;
                    continue;
                }

                if (existingGroupOptional.isPresent()) {
                    Group existingGroup = existingGroupOptional.get();
                    if (existingGroup.getMember(user.getId()) != null) {
                        //The user is already member of the group
                        existingMembers.add(userDisplayName);
                    } else {
                        //The user is not member of the group
                        newMembers.add(userDisplayName);
                    }
                } else {
                    //The group does not exist so the user is new to that group
                    newMembers.add(userDisplayName);
                }
            }

            //Add the catalogued users to the maps.
            nonExistingMemberMap.put(groupTitle, nonExistingUsers.toString());
            nonMemberMap.put(groupTitle, nonMemberUsers.toString());
            existingMemberMap.put(groupTitle, existingMembers.toString());
            newMemberMap.put(groupTitle, newMembers.toString());
        }

        //Fill the model
        model.addAttribute("importedGroups", importedGroups);
        model.addAttribute("membershipErrors", membershipErrors);
        model.addAttribute("nonExistingMemberMap", nonExistingMemberMap);
        model.addAttribute("nonMemberMap", nonMemberMap);
        model.addAttribute("existingMemberMap", existingMemberMap);    
        model.addAttribute("newMemberMap", newMemberMap);
        //Serialize as json the group map to send it back to the controller after the confirmation.
        ObjectMapper objectMapper = new ObjectMapper();
        String importedGroupMapJson = StringUtils.EMPTY;
        try {
                importedGroupMapJson = objectMapper.writer().writeValueAsString(importedGroupMap);
        } catch (JsonProcessingException e) {
            log.error("Fatal error serializing the imported group map.", e);
        }
        model.addAttribute("importedGroupMap", importedGroupMapJson);

        return GroupManagerConstants.IMPORT_CONFIRMATION_TEMPLATE;
    }

    @PostMapping(value = "/confirmImport")
    public String showConfirmImport(@RequestParam("importedGroupMap") String importedGroupJson, Model model) {

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent() || StringUtils.isBlank(importedGroupJson)) {
            log.error("Group import failed because the site is not valid or no groups have been provided.");
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        List<Group> groupList = (List<Group>) site.getGroups().stream().filter(group -> group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED) != null && Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED)).booleanValue()).collect(Collectors.toList());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> importedGroupMap = null;
        try {
            importedGroupMap = objectMapper.readValue(importedGroupJson, new TypeReference<HashMap<String, List<String>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Fatal error processing the imported group map, aborting the process.", e);
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        for (Entry<String, List<String>> importedGroup : importedGroupMap.entrySet()) {
            String groupTitle = importedGroup.getKey();
            List<String> groupUserList = importedGroup.getValue();
            //Check if the group already exists or create a new one.
            Optional<Group> groupOptional = groupList.stream().filter(g -> groupTitle.equalsIgnoreCase(g.getTitle())).findAny();
            Group group = groupOptional.isPresent() ? groupOptional.get() : site.addGroup();
            if (!groupOptional.isPresent()) {
                group.setTitle(groupTitle);
                group.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
            }

            for (String userEid : groupUserList) {

                //Get the Sakai user...despite the fact the user has been validated in the confirmation.
                Optional<User> userOptional = sakaiService.getUserByEid(userEid);
                if (!userOptional.isPresent()) {
                    log.error("Fatal error adding user {} in bulk, the user does not exist.", userEid);
                    continue;
                }

                //Get the Site member...despite the fact the member has been validated in the confirmation.
                Member member = site.getMember(userOptional.get().getId());
                if (member == null) {
                    log.error("Fatal error adding user {} in bulk, the member does not belong to the site.", userEid);
                    continue;
                }

                //Add the user to the group.
                group.addMember(member.getUserId(), member.getRole().getId(), member.isActive(), false);
                
                //Post Add events for each added user.
                if (sakaiService.getBooleanProperty(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false)) {
                    sakaiService.postEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_ADD, member.getUserId());
                }

            }
        }

        //After all the group changes, save the site.
        sakaiService.saveSite(site);

        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

    private String returnImportError(Model model, String message, Locale userLocale) {
        model.addAttribute("errorMessage", messageSource.getMessage(message, null, userLocale));
        return showImport(model);
    }

}
