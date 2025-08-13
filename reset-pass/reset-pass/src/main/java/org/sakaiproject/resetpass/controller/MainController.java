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
import java.util.Optional;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.Period;

import org.sakaiproject.serialization.MapperFactory;
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

import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.site.api.SiteService;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EventTrackingService eventService;

    @Autowired
    private ValidationLogic validationLogic;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SiteService siteService;

    private static final String SECURE_UPDATE_USER_ANY = org.sakaiproject.user.api.UserDirectoryService.SECURE_UPDATE_USER_ANY;
    private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
    private static final int MAX_PASSWORD_RESET_MINUTES_DEFAULT = 60;
    private static final String SAK_PROP_INVALID_EMAIL_DOMAINS = "resetPass.invalidEmailDomains";

    private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {

        String userId = sessionManager.getCurrentSessionUserId();
        Placement placement = toolManager.getCurrentPlacement();
        String context = placement != null ? placement.getContext() : null;

        Locale loc = Optional.ofNullable(context)
                .filter(StringUtils::isNotBlank)
                .flatMap(c -> siteService.getSiteLocale(c))
                .orElseGet(() -> StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault());

        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);

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

        boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", false);
        model.addAttribute("validatingAccounts", validatingAccounts);

        return "form";
    }

    @RequestMapping(value = {"/formsubmit"}, method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    String resetPass(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestString) {

        Locale locale = localeResolver(req, response);

        ObjectMapper objectMapper = MapperFactory.createDefaultJsonMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();

        boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
        String emailSentMessage = null;

        if (validatingAccounts) {
            emailSentMessage = messageSource.getMessage("confirm.validate", new String[]{serverConfigurationService.getString("ui.service", "Sakai"), requestString}, locale);
        } else {
            emailSentMessage = messageSource.getMessage("confirm", new String[]{requestString}, locale);
        }

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

        String exceptionMessage = messageSource.getMessage("confirm.validate", new String[]{serverConfigurationService.getString("ui.service", "Sakai"), requestString}, locale);
        String errorMsg = this.validateErrors(requestString, locale);
        boolean exceptionMsg = this.validateExceptions(requestString);

        if (errorMsg == null && !exceptionMsg) {
            jsonResponse.put("email_sent_msg", emailSentMessage);
            processAction(requestString, locale);
            
            if (supportMessage != null){
                jsonResponse.put("support_msg", supportMessage);
                jsonResponse.put("support_mail", supportEmail);
            }

        } else if (exceptionMsg){
            jsonResponse.put("exception_msg", exceptionMessage);
            
            if (supportMessage != null){
                jsonResponse.put("support_msg", supportMessage);
                jsonResponse.put("support_mail", supportEmail);
            }

        } else {
            jsonResponse.put("error_msg", errorMsg);
        }

        return jsonResponse.toString();
    }

    private void processAction(String email, Locale locale) {
        //siteManage.validateNewUsers = false use the classic method:
        boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
        if (!validatingAccounts) {
            resetPassClassic(email, locale);
        } else {
            // record event in a similar way to resetPassClassic()
            Collection<User> users = userDirectoryService.findUsersByEmail(email);
            if (!users.isEmpty()) {
                User user = users.iterator().next();
                String userId = user.getId();
                eventService.post(eventService.newEvent("user.resetpass", user.getReference(), true));

                if (!validationLogic.isAccountValidated(userId)) {
                    log.debug("account is not validated");
                    // it is possible that the user has an outstanding Validation
                    ValidationAccount va = validationLogic.getVaLidationAcountByUserId(userId);
                    if (va == null) {
                        //we need to validate the account.
                        log.debug("This is a legacy user to validate!");
                        validationLogic.createValidationAccount(userId, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
                    } else {
                        log.debug("resending validation");
                        validationLogic.resendValidation(va.getValidationToken());
                    }

                } else {
                    //there may be a pending VA that needs to be verified
                    ValidationAccount va = validationLogic.getVaLidationAcountByUserId(userId);

                    if (va == null) {
                        // the account is validated need to send a password reset
                        log.info("no account found!");
                        validationLogic.createValidationAccount(userId, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
                    } else if (va.getValidationReceived() == null) {
                        log.debug("no response on validation!");
                        validationLogic.resendValidation(va.getValidationToken());
                    } else {
                        log.debug("creating a new validation for password reset");
                        validationLogic.createValidationAccount(userId, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
                    }
                }
            } else {
                log.warn("user not found, no such email [{}]", email);
            }
        }
    }

    /*
     * The classic method that mails a new password
     * template is constructed from strings in the resource bundle
     */
    private void resetPassClassic(String email, Locale locale) {

        log.debug("getting password for {}", email);
        String from = serverConfigurationService.getSmtpFrom();

        //now we need to reset the password
        SecurityAdvisor sa = (String userId, String function, String reference) -> {
            if (SECURE_UPDATE_USER_ANY.equals(function)) {
                return SecurityAdvice.ALLOWED;
            }
            return SecurityAdvice.PASS;
        };

        try {
            securityService.pushAdvisor(sa);
            Collection<User> users = userDirectoryService.findUsersByEmail(email);
            if (!users.isEmpty()) {
                User user = users.iterator().next();
                UserEdit userE = userDirectoryService.editUser(user.getId());
                String pass = getRandPass();
                userE.setPassword(pass);
                userDirectoryService.commitEdit(userE);

                String productionSiteName = serverConfigurationService.getString("reset-pass.productionSiteName", "");

                if (productionSiteName == null || productionSiteName.isEmpty()) {
                    productionSiteName = serverConfigurationService.getString("ui.service", "");
                }

                StringBuilder buff = new StringBuilder();
                buff.setLength(0);
                buff.append(messageSource.getMessage("mailBodyPre", new String[]{userE.getDisplayName()}, locale));
                buff.append(messageSource.getMessage("mailBody1", new String[]{productionSiteName, serverConfigurationService.getPortalUrl()}, locale));
                buff.append(messageSource.getMessage("mailBody2", new String[]{userE.getEid()}, locale));
                buff.append(messageSource.getMessage("mailBody3", new String[]{pass}, locale));

                if (serverConfigurationService.getString("mail.support", null) != null) {
                    buff.append(messageSource.getMessage("mailBody4", new String[]{serverConfigurationService.getString("mail.support")}, locale));
                }

                log.debug(messageSource.getMessage("mailBody1", new String[]{productionSiteName}, locale));
                buff.append(messageSource.getMessage("mailBodySalut", null, locale));
                buff.append(messageSource.getMessage("mailBodySalut1", new String[]{productionSiteName}, locale));

                String body = buff.toString();

                List<String> headers = new ArrayList<>();
                headers.add("Precedence: bulk");

                emailService.send(from, email, "mailSubject", body, email, null, headers);

                log.info("New password emailed to: {} ({})", userE.getEid(), userE.getId());
                eventService.post(eventService.newEvent("user.resetpass", userE.getReference(), true));
            } else {
                log.warn("user not found, no such email [{}]", email);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            securityService.popAdvisor(sa);
        }
    }

    private String getRandPass() {
        // set the password to a random positive number
        return Integer.toString(new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE));
    }

    private String getFormattedMinutes(int totalMinutes, Locale locale) {
        // Create a joda time period (takes milliseconds)
        Period period = new Period(totalMinutes * 60 * 1000L);
        // format the period for the locale
        /* 
         * Covers English, Danish, Dutch, French, German, Japanese, Portuguese, and Spanish.
         * To translate into others, see http://joda-time.sourceforge.net/apidocs/org/joda/time/format/PeriodFormat.html#wordBased(java.util.Locale)
         * (ie. put the properties mentioned in http://joda-time.sourceforge.net/apidocs/src-html/org/joda/time/format/PeriodFormat.html#line.94 into the classpath resource bundle)
         */
        PeriodFormatter periodFormatter = PeriodFormat.wordBased(locale);
        return periodFormatter.print(period);
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
