/** ****************************************************************************
 * Copyright (c) 2021 Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package org.sakaiproject.resetpass.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.util.DurationFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.LocaleService;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private LocaleService localeService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EventTrackingService eventService;

    @Autowired
    private AccountValidationService avService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private SecurityService securityService;

    private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
    private static final int MAX_PASSWORD_RESET_MINUTES_DEFAULT = 60;
    private static final String SAK_PROP_INVALID_EMAIL_DOMAINS = "resetPass.invalidEmailDomains";

    private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {

        Locale loc = localeService.getLocaleForCurrentSiteAndUser();

        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, loc);
        }

        return loc;
    }

    @RequestMapping(value = "/")
    public String showIndex(Model model, HttpServletRequest req, HttpServletResponse response) {
        
        Locale locale = localeResolver(req, response);
        
        int totalMinutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);
        String explanation = messageSource.getMessage("explanation", new String[]{getFormattedMinutes(totalMinutes, locale)}, locale);
        model.addAttribute("explanation", explanation);
        
        Placement placement = toolManager.getCurrentPlacement();
        model.addAttribute("placement", placement);
        model.addAttribute("uiService", serverConfigurationService.getString("ui.service", "Sakai Based Service"));

        return "form";
    }

    @RequestMapping(value = {"/formsubmit"}, method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    String resetPass(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestString) {

        Locale locale = localeResolver(req, response);

        ObjectMapper objectMapper = MapperFactory.createDefaultJsonMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        String emailSentMessage = messageSource.getMessage("confirm.validate", new String[]{serverConfigurationService.getString("ui.service", "Sakai"), requestString}, locale);

        Placement placement = toolManager.getCurrentPlacement();
        String supportInstructions = placement == null ? "" : placement.getConfig().getProperty("supportInstructions");
        String supportMessage = null;
        String supportEmail = null;

        if (StringUtils.isNotBlank(supportInstructions)) {
            supportMessage = supportInstructions;

        } else if (serverConfigurationService.getString("mail.support", null) != null) {
            supportMessage = messageSource.getMessage("supportMessage", null, locale);
            supportEmail = serverConfigurationService.getString("mail.support", "");
        }

        String errorMsg = this.validateErrors(requestString, locale);
        boolean suppressSend = this.validateExceptions(requestString);

        if (errorMsg != null && !suppressSend) {
            jsonResponse.put("error_msg", errorMsg);
            return jsonResponse.toString();
        }

        if (!suppressSend) {
            processAction(requestString);
        }

        jsonResponse.put("email_sent_msg", emailSentMessage);
        if (supportMessage != null) {
            jsonResponse.put("support_msg", supportMessage);
            if (StringUtils.isNotBlank(supportEmail)) {
                jsonResponse.put("support_mail", supportEmail);
            }
        }

        return jsonResponse.toString();
    }

    private void processAction(String email) {
        Collection<User> users = userDirectoryService.findUsersByEmail(email);
        if (users.isEmpty()) {
            log.warn("user not found, no such email [{}]", email);
            return;
        }

        User user = users.iterator().next();
        eventService.post(eventService.newEvent("user.resetpass", user.getReference(), true));
        avService.sendPasswordReset(user.getId());
    }

    /**
     * Package-private for unit tests.
     */
    String getFormattedMinutes(int totalMinutes, Locale locale) {
        return DurationFormat.formatMinutes(totalMinutes, locale);
    }

    private String validateErrors(String email, Locale locale) {

        String errorMsgs = null;
        log.debug("validating user {}", email);

        if (StringUtils.isBlank(email)) {
            log.debug("no email provided");
            errorMsgs = messageSource.getMessage("noemailprovided", null, locale);
        }

        // Short circuit: domain provided not allowed
        List<String> invalidDomains = serverConfigurationService.getStringList(SAK_PROP_INVALID_EMAIL_DOMAINS, new ArrayList<>());
        String wrongtype = messageSource.getMessage("wrongtype", null, locale);

        if (invalidDomains.stream().anyMatch(d -> email.toLowerCase().contains(d.toLowerCase()))) {
            Placement placement = toolManager.getCurrentPlacement();
            String toolPropWrongType = placement.getConfig().getProperty("wrongtype");

            if (StringUtils.isBlank(toolPropWrongType)) {
                errorMsgs = wrongtype;
            } else {
                errorMsgs = toolPropWrongType;
            }
        }

        // All checks have passed successfully
        return errorMsgs;
    }

    private boolean validateExceptions(String email){

        boolean exceptionMsg = false;

        // User doesn't exist, null out the user and transfer to the next page
        Collection<User> c = this.userDirectoryService.findUsersByEmail(email.trim());

        if (CollectionUtils.isEmpty(c) && StringUtils.isNotBlank(email)) {
            log.debug("no such email: {}", email);
            exceptionMsg = true;

        } else if (c.size() > 1) {
            // Email is tied to more than one user, null out the user and transfer to next page
            log.warn("more than one account with email: {}", email);
            exceptionMsg = true;
        }

        // email belongs to the superuser, null out the user then transfer to the next page
        if (CollectionUtils.isNotEmpty(c)) {
            User user = c.iterator().next();

            if (securityService.isSuperUser(user.getId())) {
                log.warn("attempting to change superuser password");
                exceptionMsg = true;
            }
        }

        return exceptionMsg;
    }
}
