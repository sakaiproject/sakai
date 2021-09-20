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
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.Period;

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

    private Locale userLocale;
    private static final String SECURE_UPDATE_USER_ANY = org.sakaiproject.user.api.UserDirectoryService.SECURE_UPDATE_USER_ANY;
    private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
    private static final int MAX_PASSWORD_RESET_MINUTES_DEFAULT = 60;
    private static final String SAK_PROP_INVALID_EMAIL_DOMAINS = "resetPass.invalidEmailDomains";

    private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {

        String userId = sessionManager.getCurrentSessionUserId();
        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);

        return loc;
    }

    @RequestMapping(value = "/")
    public String showIndex(Model model, HttpServletRequest req, HttpServletResponse response) {
        
        userLocale = localeResolver(req, response);
        
        int totalMinutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);
        String explanation = messageSource.getMessage("explanation", new String[]{getFormattedMinutes(totalMinutes)}, userLocale);
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

        userLocale = localeResolver(req, response);
        JSONObject jsonResponse = new JSONObject();

        boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
        String emailSentMessage = null;

        if (validatingAccounts) {
            emailSentMessage = messageSource.getMessage("confirm.validate", new String[]{serverConfigurationService.getString("ui.service", "Sakai"), requestString}, userLocale);
        } else {
            emailSentMessage = messageSource.getMessage("confirm", new String[]{requestString}, userLocale);
        }

        Placement placement = toolManager.getCurrentPlacement();
        String supportInstructions = placement == null ? "" : placement.getConfig().getProperty("supportInstructions");
        String supportMessage = null;
        String supportEmail = null;

        if (StringUtils.isNotBlank(supportInstructions)) {
            supportMessage = supportInstructions;

        } else if (serverConfigurationService.getString("mail.support", null) != null) {
            supportMessage = messageSource.getMessage("supportMessage", null, userLocale);
            supportEmail = serverConfigurationService.getString("mail.support", "");
        }

        String exceptionMessage = messageSource.getMessage("confirm.validate", new String[]{serverConfigurationService.getString("ui.service", "Sakai"), requestString}, userLocale);
        String errorMsg = this.validateErrors(requestString);
        boolean exceptionMsg = this.validateExceptions(requestString);

        if (errorMsg == null && !exceptionMsg) {
            jsonResponse.put("email_sent_msg", emailSentMessage);
            processAction(requestString);
            
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

        return jsonResponse.toJSONString();
    }

    private void processAction(String email) {
        //siteManage.validateNewUsers = false use the classic method:
        boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
        if (!validatingAccounts) {
            resetPassClassic(email);
        } else {
            // SAK-26189 record event in similar way to resetPassClassic()
            Collection<User> users = userDirectoryService.findUsersByEmail(email);
            User user = (User) users.iterator().next();
            String userId = user.getId();
            eventService.post(eventService.newEvent("user.resetpass", user.getReference() , true));

            if (!validationLogic.isAccountValidated(userId)) {
                log.debug("account is not validated");
                //its possible that the user has an outstanding Validation
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

                if (va == null ) {
                    //the account is validated we need to send a password reset
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
        }
    }

    /*
     * The classic method that mails a new password
     * template is constructed from strings in resource bundle
     */
    private void resetPassClassic(String email) {

        log.info("getting password for " + email);
        String from = serverConfigurationService.getString("setup.request", null);

        if (from == null) {

            log.warn(this + " - no 'setup.request' in configuration");
            from = "postmaster@".concat(serverConfigurationService.getServerName());
        }

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
            User user = (User) users.iterator().next();
            UserEdit userE = userDirectoryService.editUser(user.getId());
            String pass = getRandPass();
            userE.setPassword(pass);
            userDirectoryService.commitEdit(userE);

            //securityService.popAdvisor(sa);
            String productionSiteName = serverConfigurationService.getString("reset-pass.productionSiteName", "");

            if(productionSiteName == null || "".equals(productionSiteName)){
                productionSiteName = serverConfigurationService.getString("ui.service", "");
            }

            StringBuffer buff = new StringBuffer();
            buff.setLength(0);
            buff.append(messageSource.getMessage("mailBodyPre",new String[]{userE.getDisplayName()}, userLocale));
            buff.append(messageSource.getMessage("mailBody1",new String[]{productionSiteName, serverConfigurationService.getPortalUrl()}, userLocale));
            buff.append(messageSource.getMessage("mailBody2",new String[]{userE.getEid()}, userLocale));
            buff.append(messageSource.getMessage("mailBody3",new String[]{pass}, userLocale));

            if (serverConfigurationService.getString("mail.support", null) != null ){
                buff.append(messageSource.getMessage("mailBody4",new String[]{serverConfigurationService.getString("mail.support")}, userLocale));
            }

            log.debug(messageSource.getMessage("mailBody1",new String[]{productionSiteName}, userLocale));
            buff.append(messageSource.getMessage("mailBodySalut", null, userLocale));
            buff.append(messageSource.getMessage("mailBodySalut1",new String[]{productionSiteName}, userLocale));

            String body = buff.toString();

            List<String> headers = new ArrayList<String>();
            headers.add("Precedence: bulk");

            emailService.send(from,email,"mailSubject", body, email, null, headers);

            log.info("New password emailed to: " + userE.getEid() + " (" + userE.getId() + ")");
            eventService.post(eventService.newEvent("user.resetpass", userE.getReference() , true));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            securityService.popAdvisor(sa);
        }
    }

    private String getRandPass() {
            // set password to a random positive number
            Random generator = new Random(System.currentTimeMillis());
            Integer num = generator.nextInt(Integer.MAX_VALUE);
            if (num < 0) num = num * -1;
            return num.toString();
    }

    private String getFormattedMinutes(int totalMinutes) {
        // Create a joda time period (takes milliseconds)
        Period period = new Period(totalMinutes*60*1000);
        // format the period for the locale
        /* 
         * Covers English, Danish, Dutch, French, German, Japanese, Portuguese, and Spanish.
         * To translate into others, see http://joda-time.sourceforge.net/apidocs/org/joda/time/format/PeriodFormat.html#wordBased(java.util.Locale)
         * (ie. put the properties mentioned in http://joda-time.sourceforge.net/apidocs/src-html/org/joda/time/format/PeriodFormat.html#line.94 into the classpath resource bundle)
         */
        PeriodFormatter periodFormatter = PeriodFormat.wordBased(userLocale);
        return periodFormatter.print(period);
    }

    private String validateErrors(String email) {

        String errorMsgs = null;
        log.debug("validating user " + email);

        if (StringUtils.isBlank(email)) {
            log.debug("no email provided");
            errorMsgs = messageSource.getMessage("noemailprovided", null, userLocale);
        }

        // Short circuit: domain provided not allowed
        List<String> invalidDomains = serverConfigurationService.getStringList(SAK_PROP_INVALID_EMAIL_DOMAINS, new ArrayList<>());
        String wrongtype = messageSource.getMessage("wrongtype", null, userLocale);

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

        // User doesn't exist, null out the user and transfer to next page
        Collection<User> c = this.userDirectoryService.findUsersByEmail(email.trim());

        if (CollectionUtils.isEmpty(c) && StringUtils.isNotBlank(email)) {
            log.debug("no such email: {}", email);
            exceptionMsg = true;

        } else if (c.size() > 1) {
            // Email is tied to more than one user, null out the user and transfer to next page
            log.warn("more than one account with email: {}", email);
            exceptionMsg = true;
        }

        // Email belongs to super user, null out the user and transfer to next page
        if (CollectionUtils.isNotEmpty(c)) {
            User user = (User) c.iterator().next();

            if (securityService.isSuperUser(user.getId())) {
                log.warn("tryng to change superuser password");
                exceptionMsg = true;
            }
        }

        return exceptionMsg;
    }
}
