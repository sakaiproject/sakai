/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.BaseCsvFileProcessor;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.ProcessorState;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserProcessor extends AbstractUserProcessor {

    private static String NOTIFY_NEW_USER = "sitemanage.notifyNewUserEmail";

    @Data
    class SisUser {
        private String userName;
        private String lastName;
        private String firstName;
        private String emailAddress;
        private String password;
        private String userType;
        private String userId;
        private String property1;
        private String property2;
        private String property3;
        private String property4;
        private String property5;
    }

    public String getProcessorTitle() {
        return "SIS User Processor";
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        SisUser user = new SisUser();
        user.setUserName(data[0]);
        user.setLastName(data[1]);
        user.setFirstName(data[2]);
        user.setEmailAddress(data[3]);
        user.setPassword(data[4]);
        user.setUserType(data[5]);
        user.setUserId(data[6]);

        if (data.length > 7) {
            user.setProperty1(data[7]);
        }
        if (data.length > 8) {
            user.setProperty2(data[8]);
        }
        if (data.length > 9) {
            user.setProperty3(data[9]);
        }
        if (data.length > 10) {
            user.setProperty4(data[10]);
        }
        if (data.length > 11) {
            user.setProperty5(data[11]);
        }
        processSisUser(user, state);
    }

    private void processSisUser(SisUser user, ProcessorState state) throws Exception {
        User u = null;

        // if we have a user id assume we are looking user up by this id
        if (StringUtils.isNotBlank(user.getUserId())) {
            // Lookup by user id
            try {
                u = userDirectoryService.getUser(user.getUserId());
            } catch (UserNotDefinedException ex) {
                log.debug("User not found with id: ", user.getUserId());
            }
        } else {
            // Lookup by EID
            try {
                u = userDirectoryService.getUserByEid(user.getUserName());
            } catch (UserNotDefinedException ex) {
                log.debug("User not found with eid: ", user.getUserName());
            }
        }

        if (u == null) {
            // Add new if not found
            if (StringUtils.isBlank(user.getUserId())) {
                user.setUserId(null);
            }
            if (generatePassword) {
                user.setPassword(generatePassword());
            }
            User newUser = userDirectoryService.addUser(user.getUserId(), user.getUserName(), user.getFirstName(), user.getLastName(), user.getEmailAddress(), user.getPassword(), user.getUserType(), null);
            updateExtraProperties(user, newUser);

            notifyNewUserEmail(newUser, user.getPassword());
            state.incrementInsertCnt();
        } else if (updateAllowed) {
            // Not new but update allowed
            UserEdit ue = userDirectoryService.editUser(u.getId());

            ue.setFirstName(user.getFirstName());
            ue.setLastName(user.getLastName());
            ue.setEid(user.getUserName());
            ue.setEmail(user.getEmailAddress());
            ue.setType(user.getUserType());
            updateExtraPropertiesWithEdit(user, ue);


            if (updatePassword) {
                ue.setPassword(user.getPassword());
            }

            userDirectoryService.commitEdit(ue);

            state.incrementUpdateCnt();
        } else {
            // ignore
            state.incrementIgnoreCnt();
        }
    }

    protected String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(9);
    }

    protected void updateExtraPropertiesWithEdit(SisUser sisUser, UserEdit ue) throws UserNotDefinedException, UserPermissionException, UserLockedException, UserAlreadyDefinedException {

        String[] propertyNames = serverConfigurationService.getStrings("user.sis.property");

        if (propertyNames != null && propertyNames.length > 0) {
            if (StringUtils.isNotBlank(sisUser.getProperty1())) {
                ue.getProperties().addProperty(propertyNames[0], sisUser.getProperty1());
            }

            if (propertyNames.length > 1) {
                if (StringUtils.isNotBlank(sisUser.getProperty2())) {
                    ue.getProperties().addProperty(propertyNames[1], sisUser.getProperty2());
                }
            }
            if (propertyNames.length > 2) {
                if (StringUtils.isNotBlank(sisUser.getProperty3())) {
                    ue.getProperties().addProperty(propertyNames[2], sisUser.getProperty3());
                }
            }
            if (propertyNames.length > 3) {
                if (StringUtils.isNotBlank(sisUser.getProperty4())) {
                    ue.getProperties().addProperty(propertyNames[3], sisUser.getProperty4());
                }
            }
            if (propertyNames.length > 4) {
                if (StringUtils.isNotBlank(sisUser.getProperty5())) {
                    ue.getProperties().addProperty(propertyNames[4], sisUser.getProperty5());
                }
            }
        }
    }

    protected void updateExtraProperties(SisUser sisUser, User user) throws UserNotDefinedException, UserPermissionException, UserLockedException, UserAlreadyDefinedException {
        UserEdit ue = userDirectoryService.editUser(user.getId());
        updateExtraPropertiesWithEdit(sisUser, ue);
        userDirectoryService.commitEdit(ue);
    }

    public void notifyNewUserEmail(User user, String newUserPassword) {
        if (!userEmailNotification) {
            return;
        }

        String from = "\"" +
                serverConfigurationService.getString("ui.institution", "Sakai") +
                " <no-reply@" +
                serverConfigurationService.getServerName() +
                ">\"";

        String productionSiteName = serverConfigurationService.getString("ui.service", "Sakai");

        String newUserEmail = user.getEmail();

        if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(newUserEmail)) {
            Map<String, String> replacementValues = new HashMap<>();
            replacementValues.put("userName", user.getDisplayName());
            replacementValues.put("userEid", user.getEid());
            replacementValues.put("localSakaiName", serverConfigurationService.getString("ui.service", "Sakai"));
            replacementValues.put("currentUserName", userDirectoryService.getCurrentUser().getDisplayName());
            replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
            replacementValues.put("newPassword", newUserPassword);
            replacementValues.put("productionSiteName", productionSiteName);
            RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser(NOTIFY_NEW_USER, user.getReference(), replacementValues);
            if (template != null) {
                String content = template.getRenderedMessage();

                String message_subject = template.getRenderedSubject();
                List<String> headers = Arrays.asList("Precedence: bulk");
                emailService.send(from, newUserEmail, message_subject, content, newUserEmail, newUserEmail, headers);
            }
        }
    }
}
