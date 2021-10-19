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
package org.sakaiproject.accountvalidator.tool.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONObject;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.logic.ValidationException;
import org.sakaiproject.accountvalidator.model.ValidationClaim;
import org.sakaiproject.accountvalidator.tool.constants.AccountValidatorConstants;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private ValidationLogic validationLogic;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeveloperHelperService developerHelperService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private UsageSessionService usageSessionService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    protected SiteService siteService;

    @Autowired
    protected AuthzGroupService authzGroupService;

    private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }

    // REQUEST ACCOUNT

    @RequestMapping(value = {"/requestAccount"}, method = RequestMethod.GET)
    public String requestAccountIndex(Model model, HttpServletRequest request, HttpServletResponse response, 
        @RequestParam(value="tokenId", required = false) String tokenId) {

        Locale userLocale = localeResolver(request, response);

        model.addAttribute("uiService", serverConfigurationService.getString("ui.service", "Sakai"));

        if (tokenId == null || StringUtils.isBlank(tokenId)) {
            log.debug("Valid token not found");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noCode", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);
        
        if (va == null) {
            log.debug("Not a valid validation code");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{tokenId}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;  
        } else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT)) {
            // this form is not appropriate, no such validation of the required account status
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.alreadyValidated", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_EXPIRED.equals(va.getStatus())) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.alreadyValidated", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        User u = null;

        try {
            u = userDirectoryService.getUser(va.getUserId());
            log.debug("user ID: "+u.getEid());
        } catch (UserNotDefinedException e){
            log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
            model.addAttribute("warnMsg", messageSource.getMessage("validate.userNotDefined", new String[]{serverConfigurationService.getString("ui.service", "Sakai")}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        model.addAttribute("eid", u.getEid());
        // for predefined info
        model.addAttribute("firstName", u.getFirstName());
        model.addAttribute("surName", u.getLastName());

        boolean passwordPolicyEnabled = userDirectoryService.getPasswordPolicy() != null;
        String passwordPolicyEnabledJavaScript = "VALIDATOR.isPasswordPolicyEnabled = " + Boolean.toString(passwordPolicyEnabled) + ";";
        model.addAttribute("passwordPolicyEnabled", passwordPolicyEnabledJavaScript);
 
        // If we have some terms, get the user to accept them.
        String accountValidatorTerms = serverConfigurationService.getString("account-validator.terms");
        if (!StringUtils.isBlank(accountValidatorTerms)) {
            model.addAttribute("termsText", accountValidatorTerms);
            String terms = "<label id='termsLink' class='linkStyle' onclick='showModal()'>" + messageSource.getMessage("terms.link", null, userLocale)+"</label>";
            model.addAttribute("termsLabel", terms);
        }
        return AccountValidatorConstants.REQUEST_ACCOUNT_TEMPLATE;
    }

    @RequestMapping(value = {"/requestAccount"}, method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String requestAccountFormData(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestForm,
        @RequestParam(value="tokenId", required = false) String tokenId) throws JsonProcessingException { 

        Locale userLocale = localeResolver(req, response);
        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code (POST)");
            return null;  
        } else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(va).readValue(requestForm);
            return validateAccount(va, userLocale).toJSONString();
        }
    }

    // PASSWORD RESET

    @RequestMapping(value = {"/passwordReset"}, method = RequestMethod.GET)
    public String passwordResetIndex(Model model, HttpServletRequest request, HttpServletResponse response, 
        @RequestParam(value="tokenId", required = false) String tokenId) {

        Locale userLocale = localeResolver(request, response);

        model.addAttribute("uiService", serverConfigurationService.getString("ui.service", "Sakai"));
        int resetMinutes = serverConfigurationService.getInt(AccountValidatorConstants.MAX_PASSWORD_RESET_MINUTES, AccountValidatorConstants.MAX_PASSWORD_RESET_MINUTES_DEFAULT);
        String expirationTime = getFormattedMinutes(resetMinutes, userLocale);

        if (tokenId == null || StringUtils.isBlank(tokenId)) {
            log.debug("Valid token not found");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noCode", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{tokenId}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET)) {
            // this form is not appropriate, no such validation of the required account status
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.alreadyValidated", null, userLocale));
            model.addAttribute("requestPass", addResetPassLink(model, va, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_EXPIRED.equals(va.getStatus())) {
            /*If accountValidator.maxPasswordResetMinutes is configured, 
             *we give them an approrpiate message, otherwise we give them the default*/
            if (resetMinutes > 0) {
                model.addAttribute("warnMsg",  messageSource.getMessage("msg.expiredValidationRealTime", new String[]{expirationTime}, userLocale));
                return AccountValidatorConstants.ERROR_TEMPLATE;
            } else {
                //give them the default
                model.addAttribute("warnMsg",  messageSource.getMessage("msg.alreadyValidated", new String[]{va.getValidationToken()}, userLocale));
                model.addAttribute("requestPass", addResetPassLink(model, va, userLocale));
                return AccountValidatorConstants.ERROR_TEMPLATE;
            }
        } else if (sendLegacyLinksEnabled()) {
            return AccountValidatorConstants.VALIDATE_REDIRECT+tokenId;
        } else {
            /* Password resets should go quickly. If it takes longer than accountValidator.maxPasswordResetMinutes, 
             * it could be an intruder who stumbled on an old validation token. */
            if (va.getAccountStatus() != null)  {
                //get the time limit and convert to millis
                long maxMillis = resetMinutes * 60 * 1000;
                //the time when the validation token was sent to the email server
                long sentTime = va.getValidationSent().getTime();

                if (System.currentTimeMillis() - sentTime > maxMillis) {
                    //it's been too long, so invalide the token and stop the user
                    va.setStatus(ValidationAccount.STATUS_EXPIRED);
                    model.addAttribute("warnMsg",  messageSource.getMessage("msg.expiredValidationRealTime", new String[]{expirationTime}, userLocale));
                    model.addAttribute("requestPass", addResetPassLink(model, va, userLocale));
                    return AccountValidatorConstants.ERROR_TEMPLATE;                    
                }
            }
        }

        User u = null;

        try {  
            u = userDirectoryService.getUser(va.getUserId());
            log.debug("user ID: "+u.getEid());
        } catch (UserNotDefinedException e){
            log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
            model.addAttribute("warnMsg", messageSource.getMessage("validate.userNotDefined", new String[]{serverConfigurationService.getString("ui.service", "Sakai")}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        model.addAttribute("expirationTime", messageSource.getMessage("validate.expirationtime", new String[]{expirationTime}, userLocale));
        model.addAttribute("eid", u.getEid());

        boolean passwordPolicyEnabled = userDirectoryService.getPasswordPolicy() != null;
        String passwordPolicyEnabledJavaScript = "VALIDATOR.isPasswordPolicyEnabled = " + Boolean.toString(passwordPolicyEnabled) + ";";
        model.addAttribute("passwordPolicyEnabled", passwordPolicyEnabledJavaScript);

        return AccountValidatorConstants.PASSWORD_RESET_TEMPLATE;
    }

    @RequestMapping(value = {"/passwordReset"}, method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String passwordResetFormData(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestForm,
        @RequestParam(value="tokenId", required = false) String tokenId) throws JsonProcessingException { 

        Locale userLocale = localeResolver(req, response);
        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code (POST)");
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(va).readValue(requestForm);
            return validateAccount(va,userLocale).toJSONString();
        }
    }

    // NEW USER

    @RequestMapping(value = {"/newUser"}, method = RequestMethod.GET)
    public String newUserIndex(Model model, HttpServletRequest request, HttpServletResponse response, 
        @RequestParam(value="tokenId", required = false) String tokenId) {

        Locale userLocale = localeResolver(request, response);
        String uiService = serverConfigurationService.getString("ui.service", "Sakai");

        if (tokenId == null || StringUtils.isBlank(tokenId)) {
            log.debug("Valid token not found");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noCode", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{tokenId}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_NEW) && !va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_EXISITING) && !va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE)) {
            // this form is not appropriate, no such validation of the required account status
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.alreadyValidated", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_EXPIRED.equals(va.getStatus())) {
            model.addAttribute("warnMsg",  messageSource.getMessage("msg.alreadyValidated", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (sendLegacyLinksEnabled()) {
            return AccountValidatorConstants.VALIDATE_REDIRECT+tokenId;
        } 

        User u = null;

        try {  
            u = userDirectoryService.getUser(va.getUserId());
        } catch (UserNotDefinedException e){
            log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
            model.addAttribute("warnMsg", messageSource.getMessage("validate.userNotDefined", new String[]{uiService}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        //Do not display sites and other welcome page information if user is updating userid
        if(va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE)) {
            String submitUpdate = messageSource.getMessage("submit.update", null, userLocale);
            model.addAttribute("accountTitle", submitUpdate);
            model.addAttribute("eid", va.getEid());
            model.addAttribute("submitBtn", submitUpdate);
        } else {
            model.addAttribute("accountTitle", messageSource.getMessage("activateAccount.title", null, userLocale));
            model.addAttribute("eid", u.getDisplayId());
            model.addAttribute("wait1", messageSource.getMessage("validate.wait.newUser.1", null, userLocale));
            String linkText = messageSource.getMessage("validate.wait.newUser.2", new String[]{uiService}, userLocale);
            String transferMembershipsURL = getViewURL("transferMemberships", va);
            String labelLink = "<a href="+transferMembershipsURL+">"+linkText+"</a>";
            model.addAttribute("wait2", labelLink);
            model.addAttribute("addDetailsSub", messageSource.getMessage("submit.new.account", null, userLocale));
            model.addAttribute("submitBtn", messageSource.getMessage("submit.new.account", null, userLocale));
            //we need to know which sites they're a member of:
            Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(va.getUserId()), "site.visit", null);
            List<String> existingSites = new ArrayList<>();
            List<String> siteTitles = new ArrayList<>();

            for (String groupRef : groups) {
                String groupId = EntityReference.getIdFromRef(groupRef);
                if (!existingSites.contains(groupId)) {
                    log.debug("groupId is " + groupId);
                    try {
                        Site s = siteService.getSite(groupId);
                        siteTitles.add(s.getTitle());
                        existingSites.add(groupId);
                    } catch (IdUnusedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            model.addAttribute("siteTitles", siteTitles);
            model.addAttribute("welcome", messageSource.getMessage(existingSites.size() >= 2 ? "validate.welcome.plural" : "validate.welcome.single", new String[]{uiService}, userLocale));
        }
        // model.addAttribute("welcome2", messageSource.getMessage("validate.welcome2", new String[]{u.getDisplayId()}, userLocale));
        // We would gladly accept an alternate method of doing this, however
        // this seems to be the only way to pass the enabled, disabled value from the server into the JavaScript
        boolean passwordPolicyEnabled = (userDirectoryService.getPasswordPolicy() != null);
        String passPolicyEnabledJavaScript = "VALIDATOR.isPasswordPolicyEnabled = " + Boolean.toString(passwordPolicyEnabled) + ";";
        model.addAttribute("passwordPolicyEnabled", passPolicyEnabledJavaScript);

        log.debug("account status: " + va.getAccountStatus());
        boolean renderPassBox = false;

        if (ValidationAccount.ACCOUNT_STATUS_NEW == va.getAccountStatus()) {
            log.debug("this is a new account render the second password box");
            renderPassBox = true;
        }

        model.addAttribute("renderPassBox", renderPassBox);
        // If we have some terms, get the user to accept them.
        String accountValidatorTerms = serverConfigurationService.getString("account-validator.terms");
        if (!StringUtils.isBlank(accountValidatorTerms)) {
            model.addAttribute("termsText", accountValidatorTerms);
            String terms = "<label id='termsLink' class='linkStyle' onclick='showModal()'>" + messageSource.getMessage("terms.link", null, userLocale)+"</label>";
            model.addAttribute("termsLabel", terms);
        }

        return AccountValidatorConstants.NEW_USER_TEMPLATE;
    }

    @RequestMapping(value = {"/newUser"}, method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String newUserFormData(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestForm,
        @RequestParam(value="tokenId", required = false) String tokenId) throws JsonProcessingException { 

        Locale userLocale = localeResolver(req, response);
        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code (POST)");
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(va).readValue(requestForm);
            return validateAccount(va, userLocale).toJSONString();
        }
    }

    // TRANSFER MEMBERSHIPS

    @RequestMapping(value = {"/transferMemberships"}, method = RequestMethod.GET)
    public String transferMembershipsIndex(Model model, HttpServletRequest request, HttpServletResponse response, 
        @RequestParam(value="tokenId", required = false) String tokenId) {

        Locale userLocale = localeResolver(request, response);

        String uiService = serverConfigurationService.getString("ui.service", "Sakai");
        model.addAttribute("uiService", uiService);

        if (tokenId == null || StringUtils.isBlank(tokenId)) {
            log.debug("Valid token not found");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noCode", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{tokenId}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_NEW) && !va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_EXISITING)) {
            // this form is not appropriate, no such validation of the required account status
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.alreadyValidated", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }
        else if (sendLegacyLinksEnabled()) {
            return AccountValidatorConstants.VALIDATE_REDIRECT+tokenId;
        }

        User u = null;

        try {  
            u = userDirectoryService.getUser(va.getUserId());
        } catch (UserNotDefinedException e) {
            log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
            model.addAttribute("warnMsg", messageSource.getMessage("validate.userNotDefined", new String[]{uiService}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }
        
        //we need to know which sites they're a member of:
        Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(va.getUserId()), "site.visit", null);
        List<String> existingSites = new ArrayList<>();
        List<String> siteTitles = new ArrayList<>();

        for (String groupRef : groups) {
            String groupId = EntityReference.getIdFromRef(groupRef);
            if (!existingSites.contains(groupId)) {
                log.debug("groupId is " + groupId);
                try {
                    Site s = siteService.getSite(groupId);
                    siteTitles.add(s.getTitle());
                    existingSites.add(groupId);
                } catch (IdUnusedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        model.addAttribute("siteTitles", siteTitles);

        String welcomeMessage = existingSites.size() == 1 ? "validate.welcome.single" : "validate.welcome.plural";
        model.addAttribute("welcome", messageSource.getMessage(welcomeMessage, new String[]{uiService}, userLocale));
        //produce a link to switch to the new user view
        String linkText = messageSource.getMessage("validate.wait.transfer.2", new String[]{u.getDisplayId()}, userLocale);
        String activationURL = getViewURL("newUser", va);
        String labelLink = "<a href="+activationURL+">"+linkText+"</a>";
        model.addAttribute("labelLink", labelLink);

        model.addAttribute("userId", u.getDisplayId());
        model.addAttribute("requestPass", addResetPassLink(model, va, userLocale));

        return AccountValidatorConstants.TRANSFER_MEMBERSHIPS_TEMPLATE;
    } 

    @RequestMapping(value = {"/transferMemberships"}, method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String transferMembershipsFormData(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestForm,
        @RequestParam(value="tokenId", required = false) String tokenId) throws JsonProcessingException { 

        Locale userLocale = localeResolver(req, response);
        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);
        
        if (va == null) {
            log.debug("Not a valid validation code (POST)");
            return null;
        } else {
            ValidationClaim vc = new ValidationClaim();
            vc.setValidationToken(tokenId);
            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(vc).readValue(requestForm);
            return claimAccount(vc, userLocale).toJSONString();
        }
    }

    // VALIDATE CLASSIC

    @RequestMapping(value = {"/validate"}, method = RequestMethod.GET)
    public String validateIndex(Model model, HttpServletRequest request, HttpServletResponse response, 
        @RequestParam(value="tokenId", required = false) String tokenId) {

        Locale userLocale = localeResolver(request, response);

        String uiService = serverConfigurationService.getString("ui.service", "Sakai");
        model.addAttribute("uiService", uiService);

        if (tokenId == null || StringUtils.isBlank(tokenId)) {
            log.debug("Valid token not found");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noCode", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (!sendLegacyLinksEnabled()) {
            log.debug("Legacy links were disabled");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.obsoleteLink", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        if (va == null) {
            log.debug("Not a valid validation code");
            model.addAttribute("warnMsg", messageSource.getMessage("msg.noSuchValidation", new String[]{tokenId}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus())) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.alreadyValidated", null, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else if (validationLogic.isTokenExpired(va)) {
            model.addAttribute("warnMsg", messageSource.getMessage("msg.expiredValidation", new String[]{va.getValidationToken()}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        } else {
            /*
            * If we're dealing with password resets, they should go quickly. If it takes longer than
            * accountValidator.maxPasswordResetMinutes, it could be an intruder who stumbled upon the validation
            * token from an intercepted email, and we should stop them.
            * Note that there already exists a quartz job to expire the validation tokens, but using a quartz job
            * means that tokens would only be invalidated when the job runs. So here we check in real-time
            * */
            if (va.getAccountStatus() != null && va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET)) {
                int minutes = serverConfigurationService.getInt(AccountValidatorConstants.MAX_PASSWORD_RESET_MINUTES, AccountValidatorConstants.MAX_PASSWORD_RESET_MINUTES_DEFAULT);
                //get the time limit and convert to millis
                long maxMillis = minutes * 60 * 1000;
                //the time when the validation token was sent to the email server
                long sentTime = va.getValidationSent().getTime();

                if (System.currentTimeMillis() - sentTime > maxMillis) {
                    //it's been too long, so invalidate the token and stop the user
                    va.setStatus(ValidationAccount.STATUS_EXPIRED);
                    model.addAttribute("error", messageSource.getMessage("msg.expiredValidation", new String[]{va.getValidationToken()}, userLocale));
                    return AccountValidatorConstants.ERROR_TEMPLATE;
                }
            }
        }

        if (!serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false)) {
            Integer accountStatus = va.getAccountStatus();
            if (accountStatus != null) {
                if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET)) {
                    return AccountValidatorConstants.PASSWORD_RESET_TEMPLATE;
                } else {
                    return AccountValidatorConstants.NEW_USER_TEMPLATE;
                }
            }
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        User u = null;

        try {  
            u = userDirectoryService.getUser(va.getUserId());        
            //user who added this person (not used)
            //User addedBy = u.getCreatedBy();
            model.addAttribute("email", u.getEmail());

            //is this a password reset?
            boolean isReset = false;
            if (va.getAccountStatus() == ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET) {
                isReset = true;
            }

            String[] args = new String[]{uiService};

            if (!isReset) {
                model.addAttribute("welcome1", messageSource.getMessage("validate.welcome1", null, userLocale));
                model.addAttribute("welcome", messageSource.getMessage("validate.welcome", args, userLocale));
                model.addAttribute("imnew", messageSource.getMessage("validate.imnew", args, userLocale));
                model.addAttribute("alreadyhave", messageSource.getMessage("validate.alreadyhave", args, userLocale));
            } else {
                model.addAttribute("welcome1", messageSource.getMessage("validate.welcome1.reset", args, userLocale));
                model.addAttribute("welcome", messageSource.getMessage("validate.welcome.reset", args, userLocale));
                model.addAttribute("imnew", messageSource.getMessage("validate.oneaccount", args, userLocale));
                model.addAttribute("alreadyhave", messageSource.getMessage("validate.alreadyhave.reset", args, userLocale));
            }

            //we need to know what sites their a member of:
            Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(va.getUserId()), "site.visit", null);
            List<String> existingSites = new ArrayList<>();
            List<String> siteTitles = new ArrayList<>();

            for (String groupRef : groups) {
                String groupId = EntityReference.getIdFromRef(groupRef);
                if (!existingSites.contains(groupId)) {
                    log.debug("groupId is " + groupId);
                    try {
                        Site s = siteService.getSite(groupId);
                        siteTitles.add(s.getTitle());
                        existingSites.add(groupId);
                    } catch (IdUnusedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            model.addAttribute("siteTitles", siteTitles);

            if (isReset) {
                model.addAttribute("claim", messageSource.getMessage("validate.reset", args, userLocale));
                model.addAttribute("addDetailsSub", messageSource.getMessage("submit.new.reset", null, userLocale));
            } else {
                model.addAttribute("claim", messageSource.getMessage("validate.claim", args, userLocale));
                model.addAttribute("addDetailsSub",  messageSource.getMessage("submit.new.account", null, userLocale));
            }

            model.addAttribute("eid", u.getDisplayId());
            // for predefined info
            model.addAttribute("firstName", u.getFirstName());
            model.addAttribute("surName", u.getLastName());

            log.debug("account status: " + va.getAccountStatus());  

            boolean renderPassBox = false;
            if (ValidationAccount.ACCOUNT_STATUS_NEW == va.getAccountStatus() || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == va.getAccountStatus()
                    || ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == va.getAccountStatus()) {
                log.debug("this is a new account render the second password box");
                renderPassBox = true;            
            }
            model.addAttribute("renderPassBox", renderPassBox);

            // If we have some terms, get the user to accept them.
            String accountValidatorTerms = serverConfigurationService.getString("account-validator.terms");
            if (!StringUtils.isBlank(accountValidatorTerms)) {
                model.addAttribute("termsText", accountValidatorTerms);
                String terms = "<label id='termsLink' class='linkStyle' onclick='showModal()'>" + messageSource.getMessage("terms.link", null, userLocale)+"</label>";
                model.addAttribute("termsLabel", terms);
            }

            //the claim form
            if (!isReset) {
                model.addAttribute("loginexisting", messageSource.getMessage("validate.loginexisting", args, userLocale));
            } else {
                model.addAttribute("loginexisting", messageSource.getMessage("validate.loginexisting.reset", args, userLocale));
            }
       
        } catch (UserNotDefinedException e) {
            log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
            model.addAttribute("warnMsg", messageSource.getMessage("validate.userNotDefined", new String[]{uiService}, userLocale));
            return AccountValidatorConstants.ERROR_TEMPLATE;
        }

        return AccountValidatorConstants.VALIDATE_TEMPLATE;
    }

    // ValidateAccount
    @RequestMapping(value = {"/validate"}, method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String validateFormData1(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestForm,
        @RequestParam(value="tokenId", required = false) String tokenId) throws JsonProcessingException { 

        Locale userLocale = localeResolver(req, response);
        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code (POST)");
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(va).readValue(requestForm);
            return validateAccount(va, userLocale).toJSONString();
        }
    }

    // ClaimAccount
    @RequestMapping(value = {"/claim"}, method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String validateFormData(HttpServletRequest req, HttpServletResponse response, Model model, @RequestBody String requestForm,
        @RequestParam(value="tokenId", required = false) String tokenId) throws JsonProcessingException { 

        Locale userLocale = localeResolver(req, response);
        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(tokenId);

        if (va == null) {
            log.debug("Not a valid validation code (POST)");
            return null;
        } else {
            ValidationClaim vc = new ValidationClaim();
            vc.setValidationToken(tokenId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(vc).readValue(requestForm);
            return claimAccount(vc, userLocale).toJSONString();
        }
    }

    //TODO the logic should be moved to a service method
    private JSONObject validateAccount(ValidationAccount va, Locale userLocale) {
        log.debug("Validate Account");
        JSONObject jsonResponse = new JSONObject();

        List<String> userReferences = new ArrayList<String>();
        String formEid = StringUtils.trimToEmpty(va.getEid());
        String formFirstName = StringUtils.trimToEmpty(va.getFirstName());
        String formSurname = StringUtils.trimToEmpty(va.getSurname());
        String formPw1 = StringUtils.trimToEmpty(va.getPassword());
        String formPw2 = StringUtils.trimToEmpty(va.getPassword2());
        boolean formTerms = va.getTerms();

        if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus()) || ValidationAccount.STATUS_EXPIRED.equals(va.getStatus())) {
            return null;
        }

        log.debug("Validating va: " + va.getId() + " for user: " + va.getUserId());

        int accountStatus = va.getAccountStatus();
        //names are required in all cases except password resets
        if (ValidationAccount.ACCOUNT_STATUS_NEW == accountStatus || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == accountStatus || ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == accountStatus) {

            if (formFirstName.isEmpty()) {  
                jsonResponse.put("error", messageSource.getMessage("firstname.required", null, userLocale));
                return jsonResponse;
            }
            if (formSurname.isEmpty()) {  
                jsonResponse.put("error", messageSource.getMessage("lastname.required", null, userLocale));
                return jsonResponse;
            }
        }

        log.debug(formFirstName + " " + formSurname);
        log.debug("this is an new va?: " + va.getAccountStatus());
        try {
            String userId = EntityReference.getIdFromRef(va.getUserId());
            //we need permission to edit this user

                //if this is an existing user did the password match?
            if (ValidationAccount.ACCOUNT_STATUS_EXISITING == va.getAccountStatus() && !validateLogin(userId, formPw1)) {
                jsonResponse.put("error", messageSource.getMessage("validate.invalidPassword", null, userLocale));
                return jsonResponse;
            }

            securityService.pushAdvisor(new SecurityAdvisor() {
                public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function, String reference) {
                    if (function.equals(UserDirectoryService.SECURE_UPDATE_USER_ANY)) {
                        return SecurityAdvisor.SecurityAdvice.ALLOWED;
                    } else {
                        return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
                    }
                }
            });

            if (validationLogic.isTokenExpired(va)) {
                return null;
            }
            if(va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE)) {
                boolean isSuccess = userDirectoryService.updateUserId(userId,formEid);
                va.setEid(formEid);
                if(!isSuccess) {
                    jsonResponse.put("error", messageSource.getMessage("msg.errUpdate.userId", new String[]{formEid}, userLocale));
                    return jsonResponse;
                }
            }

            UserEdit u = userDirectoryService.editUser(userId);

            if (sendLegacyLinksEnabled() || ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET != accountStatus) {
                //We always can change names if legacy links is enabled. Otherwise in the new forms, we can't change names during password resets
                u.setFirstName(formFirstName);
                u.setLastName(formSurname);
                va.setFirstName(formFirstName);
                va.setSurname(formSurname);
            }

            //if this is a new account set the password
            if (ValidationAccount.ACCOUNT_STATUS_NEW == accountStatus || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == accountStatus || ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == accountStatus || ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == accountStatus) {
                if (formPw1 == null || !formPw1.equals(formPw2)) {
                    //Abandon the edit
                    userDirectoryService.cancelEdit(u);
                    jsonResponse.put("error", messageSource.getMessage("validate.passNotMatch", null, userLocale));
                    return jsonResponse;
                }

                // SAK-22427
                if (userDirectoryService.getPasswordPolicy() != null) {
                        UserDirectoryService.PasswordRating rating = userDirectoryService.validatePassword(formPw1, u);
                        if (UserDirectoryService.PasswordRating.FAILED.equals(rating)) {
                            userDirectoryService.cancelEdit(u);
                            jsonResponse.put("error", messageSource.getMessage("validate.password.fail", null, userLocale));
                            return jsonResponse;
                        }
                }

                u.setPassword(formPw1);

                // Do they have to accept terms and conditions.
                if (!StringUtils.isBlank(serverConfigurationService.getString("account-validator.terms"))) {
                    //terms and conditions are only relevant for new accounts (unless we're using the legacy links)
                    boolean checkTerms = ValidationAccount.ACCOUNT_STATUS_NEW == accountStatus || sendLegacyLinksEnabled();
                    if (checkTerms) {
                        // Check they accepted the terms.
                        if (formTerms) {
                            u.getPropertiesEdit().addProperty("TermsAccepted", "true");
                        } else {
                            userDirectoryService.cancelEdit(u);
                            jsonResponse.put("error", messageSource.getMessage("validate.acceptTerms", null, userLocale));
                            return jsonResponse;
                        }
                    }
                }
            }

            userDirectoryService.commitEdit(u);

            //update the Validation object
            va.setValidationReceived(new Date());
            va.setStatus(ValidationAccount.STATUS_CONFIRMED);
            log.debug("Saving now ...");

            //post an event
            developerHelperService.fireEvent("accountvalidation.validated", u.getReference());

            validationLogic.save(va);

            userReferences.add(userDirectoryService.userReference(va.getUserId()));

            //log the user in
            log.debug("- log the user in");
            Evidence e = new ExternalTrustedEvidence(u.getEid());
            try {
                Authentication a = authenticationManager.authenticate(e);
                log.debug("authenticated " + a.getEid() + "(" + a.getUid() + ")");
                log.debug("reg: " + httpServletRequest.getRemoteAddr());
                log.debug("user agent: " + httpServletRequest.getHeader("user-agent"));
                usageSessionService.login(a , httpServletRequest);
            } catch (AuthenticationException e1) {
                log.error(e1.getMessage(), e1);
            }
        } catch (UserNotDefinedException | UserPermissionException | UserLockedException | UserAlreadyDefinedException e) {
                log.error(e.getMessage(), e);
        } finally {
                securityService.popAdvisor();
                jsonResponse.put("success", "success");
        }
        // validationLogic.
        // Send password reset acknowledgement email for password reset scenarios
        if (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == accountStatus) {

            String supportEmail = serverConfigurationService.getString("mail.support");
            Map<String, Object> replacementValues = new HashMap<>();
            replacementValues.put("emailSupport", supportEmail);
            emailTemplateService.sendRenderedMessages(AccountValidatorConstants.TEMPLATE_KEY_ACKNOWLEDGE_PASSWORD_RESET, userReferences, replacementValues, supportEmail, supportEmail);
        }

        return jsonResponse;
    }

    private JSONObject claimAccount(ValidationClaim vc, Locale userLocale) {

        log.debug("claim account!");
        JSONObject jsonResponse = new JSONObject();
        String loginFailed = messageSource.getMessage("validate.loginFailed", null, userLocale);
        //does the userName password match?
        /*ValidationClaim vc = null;
        for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
          String key = (String) it.next();
          vc = (ValidationClaim) delivered.get(key);
        }   */
        if (vc == null) {
            jsonResponse.put("error", loginFailed);
            return jsonResponse;
        }

        log.debug(vc.getUserEid() + ": " + vc.getPassword1());
        User u = userDirectoryService.authenticate(vc.getUserEid(), vc.getPassword1());

        if (u == null) {
            log.warn("authentification failed for " + vc.getUserEid());
            jsonResponse.put("error", loginFailed);
            return jsonResponse;
        }

        ValidationAccount va = validationLogic.getVaLidationAcountBytoken(vc.getValidationToken());

        if (va == null) {
            log.warn("Couldn't obtain a ValidationAccount object for token: " + vc.getValidationToken());
            jsonResponse.put("error", loginFailed);
            return jsonResponse;
        }

        //With sendLegacyLinks disabled, the option to transfer memberships is not available for password resets
        if (!serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false) && (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == va.getStatus() || ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT == va.getStatus())) {
            log.warn("Was attempting to transfer memberships for a ValidationAccount of status " + va.getStatus());
            jsonResponse.put("error", loginFailed);
            return jsonResponse;
        }

        String oldUserRef = userDirectoryService.userReference(va.getUserId());

        // don't let them claim the account if their validation token is expired
        if (validationLogic.isTokenExpired(va)) {
            // a TargettedMessage will be displayed by ValidationProducer
            jsonResponse.put("error", messageSource.getMessage("msg.expiredValidation", new String[]{vc.getValidationToken()}, userLocale));
            return jsonResponse;
        }

        //Try set up the ussersession
        authenticateUser(vc, oldUserRef);

        //we can't merge an account into itself
        if (u.getId().equals(va.getUserId())) {
            log.warn("using the same accounts for validation!"); 
            jsonResponse.put("error", messageSource.getMessage("validate.sameAccount", null, userLocale));
            return jsonResponse;
        }

        try {
            validationLogic.mergeAccounts(va.getUserId(), u.getReference());
            //delete the token
            validationLogic.deleteValidationAccount(va);

            authenticateUser(vc, oldUserRef);
            jsonResponse.put("success", "success");
            return jsonResponse;
        } catch (ValidationException e2) {
            log.error(e2.getMessage(), e2);
        }

        jsonResponse.put("error", loginFailed);
        return jsonResponse;
    }

    private void authenticateUser(ValidationClaim vc, String oldUserRef) {
            //log the user in
            Evidence e = new IdPwEvidence(vc.getUserEid(), vc.getPassword1(), httpServletRequest.getRemoteAddr());
            try {
                Authentication a = authenticationManager.authenticate(e);
                log.debug("authenticated " + a.getEid() + "(" + a.getUid() + ")");
                log.debug("reg: " + httpServletRequest.getRemoteAddr());
                log.debug("user agent: " + httpServletRequest.getHeader("user-agent"));
                if (usageSessionService.login(a, httpServletRequest)) {
                    log.debug("logged in!");
                }
                //post an event
                developerHelperService.fireEvent("accountvalidation.merge", oldUserRef);
            } catch (AuthenticationException e1) {
                log.error(e1.getMessage(), e1);
            }
    }

    private boolean validateLogin(String userId, String password) {
        try {
            User u = userDirectoryService.authenticate(userDirectoryService.getUserEid(userId), password);
            if (u != null) {
                return true;
            }
        } catch (UserNotDefinedException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private String getFormattedMinutes(int totalMinutes, Locale userLocale) {
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

    private boolean sendLegacyLinksEnabled() {
        return serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false);
    }

    private String getViewURL(String view, ValidationAccount va) {
        String serverUrl = serverConfigurationService.getServerUrl();
        return serverUrl + "/accountvalidator/" + view + "?tokenId=" + va.getValidationToken();
    }

    private String addResetPassLink(Model model, ValidationAccount va, Locale userLocale) {

        if (model == null || va == null) {
            // enforce method contract
            throw new IllegalArgumentException("null passed to addResetPassLink()");
        }
        //the url to reset-pass - assume it's on the gateway. Otherwise, we don't render a link and we log a warning

        String url = null;
        try {
            //get the link target
            url = getPasswordResetUrl();
        }
        catch (IllegalArgumentException e) {
            log.warn("Couldn't create a link to reset-pass; no instance of reset-pass found on the gateway");
        }

        if (url != null) {
            
            //add a label
            String requestLabel = messageSource.getMessage("validate.requestanother.label", null, userLocale);
            //add the link to reset-pass
            String requestAnother = null;
            if (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == va.getAccountStatus()) {
                requestAnother = messageSource.getMessage("validate.requestanother.reset", null, userLocale);
            } else {
                requestAnother = messageSource.getMessage("validate.requestanother", null, userLocale);
            }
            String requestURL = "<p>"+requestLabel+"&nbsp;<a href="+url+">" +requestAnother+ "</a></p>";
            return requestURL;
        }
        //else - there is no reset pass instance on the gateway, but the user sees an appropriate message regardless (handled by a targetted message)
        return null;
    }

    private String getPasswordResetUrl() {
        // Has a password reset url been specified in sakai.properties? If so, it rules.
        String passwordResetUrl = serverConfigurationService.getString("login.password.reset.url", null);

        if(passwordResetUrl == null) {
            // No explicit password reset url. Try and locate the tool on the gateway page.
            // If it has been  installed we'll use it.
            String gatewaySiteId = serverConfigurationService.getGatewaySiteId();
            Site gatewaySite = null;
            try {
                gatewaySite = siteService.getSite(gatewaySiteId);
                ToolConfiguration resetTC = gatewaySite.getToolForCommonId("sakai.resetpass");
                if(resetTC != null) {
                    passwordResetUrl = resetTC.getContainingPage().getUrl();
                }
            } catch (IdUnusedException e) {
                log.warn("No " + gatewaySiteId + " site found whilst building password reset url, set password.reset.url" +
                        " or create " + gatewaySiteId + " and add password reset tool.");
            }
        }
        return passwordResetUrl;
    }
}
