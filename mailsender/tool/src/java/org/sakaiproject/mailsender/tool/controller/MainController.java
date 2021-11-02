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
package org.sakaiproject.mailsender.tool.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.AttachmentException;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.EmailEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;
import org.sakaiproject.util.Web;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ServerConfigurationService configService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private ExternalLogic externalLogic;

    @Autowired
    private ConfigLogic configLogic;

    @Autowired
    private ComposeLogic composeLogic;

    private static final String TEMPLATE_COMPOSE = "compose";
    private static final String REDIRECT_COMPOSE = "redirect:/compose";
    private static final String TEMPLATE_RESULTS = "results";
    private static final String TEMPLATE_OPTIONS = "options";
    private static final String TEMPLATE_PERMISSIONS = "permissions";
            
    private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }

    private void addEmailUsers(String fromEmail, HashMap<String, String> emailusers, List<User> users, Set<String> invalids) {
        for (User user : users) {
            addEmailUser(fromEmail, emailusers, user,invalids);
        }
    }
    private void addEmailUser(String fromEmail, HashMap<String, String> emailusers, User user, Set<String> invalids) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            if (!fromEmail.equals(user.getEmail())) {
            emailusers.put(user.getEmail(), user.getDisplayName());
            }
        } else {
            invalids.add(user.getDisplayName());
        }
    }
    //compile the list of emails to send to
    private HashSet<String> compileEmailList(EmailEntry emailEntry, String fromEmail, HashMap<String, String> emailusers, Set<String> invalids) {
        HashSet<String> badEmails = new HashSet<>();
        if (emailEntry.isAllIds()) {
            try {
                addEmailUsers(fromEmail, emailusers, composeLogic.getUsers(),invalids);
            } catch (IdUnusedException e) {
                log.warn(e.getMessage(), e);
                badEmails.add(e.getMessage());
            }
        } else {
            // check for roles and add users
            emailEntry.getRoleIds().keySet().forEach(roleId -> {
                try {
                    List<User> users = composeLogic.getUsersByRole(roleId);
                    addEmailUsers(fromEmail, emailusers, users,invalids);
                } catch (IdUnusedException e) {
                    log.warn(e.getMessage(), e);
                    badEmails.add(roleId);
                }
            });
            // check for sections and add users
            emailEntry.getSectionIds().keySet().forEach(sectionId -> {
                try {
                    List<User> users = composeLogic.getUsersByGroup(sectionId);
                    addEmailUsers(fromEmail, emailusers, users,invalids);
                } catch (IdUnusedException e) {
                    log.warn(e.getMessage(), e);
                }
            });

            // check for groups and add users
            emailEntry.getGroupIds().keySet().forEach(groupId -> {
                try {
                    List<User> users = composeLogic.getUsersByGroup(groupId);
                    addEmailUsers(fromEmail, emailusers, users,invalids);
                } catch (IdUnusedException e) {
                    log.warn(e.getMessage(), e);
                }
            });
            emailEntry.getUserIds().keySet().stream().map(userId -> {
                return userId;
            }).map(userId -> externalLogic.getUser(userId)).map(user -> {
                return user;
            }).forEachOrdered(user -> {
                addEmailUser(fromEmail, emailusers, user,invalids);
            });
        }
        return badEmails;
    }

   private String compileRecipientList(Map<String, String> recipients, Locale loc) {
        StringBuilder recipientList = new StringBuilder();
        recipientList.append("<br>");
        recipientList.append(messageSource.getMessage("message.sent.to",null,loc));
        Iterator iter = recipients.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)iter.next();
            String email = entry.getKey();
            String name = entry.getValue();
            if (StringUtils.isNotBlank(name)) {
                recipientList.append(name);
            } else {
                recipientList.append(email);
            }
            if (iter.hasNext()) recipientList.append(", ");
        }
        return recipientList.toString();
    }

    private void addToArchive(EmailEntry emailEntry, ConfigEntry config, MultipartFile [] files, String fromString, String subject, String siteId, List<Attachment> attachments) {
        if (emailEntry.getConfig().isAddToArchive()) {
            StringBuilder attachment_info = new StringBuilder("<br/>");
            int i = 1;
            if (files != null && files.length != 0) {
                for (MultipartFile file : files) {
                    if (file.getSize() > 0) {
                        attachment_info.append("<br/>");
                        attachment_info.append("Attachment #").append(i).append(": ").append(Web.escapeHtml(file.getOriginalFilename())).append("(").append(file.getSize()).append(" Bytes)");
                        i++;
                    }
                }
            }
            String emailarchive = "/mailarchive/channel/" + siteId + "/main";
            String content = Web.cleanHtml(emailEntry.getContent()) + attachment_info.toString();
            externalLogic.addToArchive(config, emailarchive, fromString, subject, content, attachments);
        }
    }

    @RequestMapping(value = {"/","/compose"})
    public String showCompose(Model model, @ModelAttribute EmailEntry emailEntry, HttpServletRequest request, HttpServletResponse response) {
        localeResolver(request, response);
        ConfigEntry confi = configLogic.getConfig();
        EmailEntry newEmailEntry = new EmailEntry(confi);
        // get the user then name & email
        User curUser = externalLogic.getCurrentUser();
        String fromEmail = "";
        String fromDisplay = "";
        if (curUser != null) {
            fromEmail = curUser.getEmail();
            fromDisplay = curUser.getDisplayName();
        }
        newEmailEntry.setFrom(fromDisplay + " <" + fromEmail + ">");
        model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
        model.addAttribute("emailEntry", newEmailEntry);
        model.addAttribute("addedTo", externalLogic.isEmailArchiveAddedToSite());
        model.addAttribute("noemail",StringUtils.isAllEmpty(fromEmail));
        model.addAttribute("comp",composeLogic);
        model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
        model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));  
        return TEMPLATE_COMPOSE;
    }

    @RequestMapping(value = {"/options"})
    public String showOptions(Model model, HttpServletRequest request, HttpServletResponse response) {
        localeResolver(request, response);
        ConfigEntry opconf = configLogic.getConfig();
        model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
        model.addAttribute("added",externalLogic.isEmailArchiveAddedToSite());
        model.addAttribute("prefix",configLogic.allowSubjectPrefixChange());
        model.addAttribute("config",opconf);
        model.addAttribute("siteID",externalLogic.getSiteID());
        return TEMPLATE_OPTIONS;
    }

    @RequestMapping(value = {"/options"}, method = RequestMethod.POST)
    public String submitOptions(Model model, HttpServletRequest request, HttpServletResponse response) {
        localeResolver(request, response);
        Locale loc = localeResolver(request, response);
        String cancel= request.getParameter("cancel");
        if (cancel != null && cancel.equals("Cancel")) {
            return REDIRECT_COMPOSE;
        }
        
        ConfigEntry confi = configLogic.getConfig();
        confi.setSendMeACopy(Boolean.parseBoolean(request.getParameter("sendMeACopy")));
        confi.setAddToArchive(Boolean.parseBoolean(request.getParameter("addToArchive")));
        confi.setAppendRecipientList(Boolean.parseBoolean(request.getParameter("appendRecipientList")));
        confi.setReplyTo(request.getParameter("replyto"));
        confi.setDisplayInvalidEmails(Boolean.parseBoolean(request.getParameter("InvalidEm")));
        confi.setDisplayEmptyGroups(Boolean.parseBoolean(request.getParameter("rcpsempty")));
        String reqPrefixType = request.getParameter("prefix");
        String reqSubjPrefix = request.getParameter("subjectPrefix");
        
        if (reqPrefixType != null) {
            if (SubjectPrefixType.custom.name().equals(reqPrefixType)&& reqSubjPrefix == null || reqSubjPrefix.isEmpty()) {
                model.addAttribute("error",messageSource.getMessage("custom_prefix_required",null,loc));
		return TEMPLATE_OPTIONS ;
            } else {
                confi.setSubjectPrefixType(reqPrefixType);
                confi.setSubjectPrefix(reqSubjPrefix);
		configLogic.saveConfig(confi);
            }
	}
        
        configLogic.saveConfig(confi);
        return REDIRECT_COMPOSE;
    }

    @RequestMapping(value = {"/permissions"})
    public String showPermissions(Model model, HttpServletRequest request, HttpServletResponse response) {
        localeResolver(request, response);
        model.addAttribute("notAdmin", externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
        return "permissions";
    }

    @RequestMapping(value = {"/compose"}, method = RequestMethod.POST)
    public String submitCompose(@ModelAttribute EmailEntry emailEntry, @RequestParam(name = "attachment",required = false) MultipartFile [] files, Model model, HttpServletRequest request, HttpServletResponse response) throws AttachmentException, MailsenderException  {
        Locale loc = localeResolver(request,response);
        // get the user then name & email
        User curUser = externalLogic.getCurrentUser(); 
        String fromEmail = "";
        String fromDisplay = "";
        if (curUser != null) {
            fromEmail = curUser.getEmail();
            fromDisplay = curUser.getDisplayName();
        }
        String from = fromDisplay + " <" + fromEmail + ">";
        ConfigEntry config = configLogic.getConfig();
        String reqContent = request.getParameter("editor1");
        String reqSubject = request.getParameter("subject");
        String rcptsall = request.getParameter("rcptsall");
        //Get checkboxes values
        String onlyPlainText = request.getParameter("onlyPlainText");
        String reqSendMeACopy = request.getParameter("smac");
        String reqAddToArchive = request.getParameter("addToArchive");
        String reqAppendRecipientList = request.getParameter("appendRecipientList");

        EmailEntry newEmailEntry = new EmailEntry(config);
        newEmailEntry.setFrom(from);
        if (StringUtils.isNotBlank(reqContent)) { 
            newEmailEntry.setContent(reqContent);
        }
        if (StringUtils.isNotBlank(reqSubject)) { 
            newEmailEntry.setSubject(reqSubject);
        }
        if (StringUtils.isNotBlank(rcptsall)) {
            newEmailEntry.setAllIds(Boolean.parseBoolean(rcptsall));
        }
        config.setOnlyPlainText(Boolean.parseBoolean(onlyPlainText));
        config.setSendMeACopy(Boolean.parseBoolean(reqSendMeACopy));
        config.setAddToArchive(Boolean.parseBoolean(reqAddToArchive));
        config.setAppendRecipientList(Boolean.parseBoolean(reqAppendRecipientList));

        newEmailEntry.setConfig(config);
        String reqOtherRecipients= request.getParameter("otherRecipients");
        if (StringUtils.isNotBlank(reqOtherRecipients)) {
            newEmailEntry.setOtherRecipients(reqOtherRecipients);
        }

        String [] roleNameArray = request.getParameterValues("rolename");
        String [] rolegNameArray = request.getParameterValues("rolegname");
        String [] rolesecNameArray = request.getParameterValues("rolesecname");
        String [] usersNameArray = request.getParameterValues("user");
        Map<String, String> rolesIds = new HashMap<>();
        Map<String, String> sectionsIds = new HashMap<>();
        Map<String, String> groupsIds = new HashMap<>();
        Map<String, String> usersIds = new HashMap<>();

            if (roleNameArray != null) {
                for (String role : roleNameArray) {
                    rolesIds.put(role, role);
                }
                newEmailEntry.setRoleIds(rolesIds);
            }
            if (rolegNameArray != null) {
                for (String group : rolegNameArray) {
                    groupsIds.put(group, group);
                }
                newEmailEntry.setGroupIds(groupsIds);
            }
            if (rolesecNameArray != null) {
                for (String section : rolesecNameArray) {
                    sectionsIds.put(section, section);
                }
                newEmailEntry.setSectionIds(sectionsIds);
            }

        if (usersNameArray != null) {
            for (String user : usersNameArray) {
                usersIds.put(user, user);
            }
            newEmailEntry.setUserIds(usersIds);
        }
        HashMap<String, String> emailusers = new HashMap<>();
        Set<String> invalids = new HashSet<>();
        compileEmailList(newEmailEntry, fromEmail, emailusers, invalids);
        // handle the other recipients
        String otherRecipients = newEmailEntry.getOtherRecipients();
        ArrayList<String> emailOthers = new ArrayList<>();
        if (StringUtils.isNotBlank(otherRecipients)) {
            String [] rcpts = otherRecipients.replace(';', ',').split(",");
            for (String rcpt : rcpts) {
                emailOthers.add(rcpt.trim());
            }
        }

        String [] allowedDomains = StringUtils.split(configService.getString("sakai.mailsender.other.domains"), ",");
        // add other recipients to the message
        for (String email : emailOthers) {
            if (allowedDomains != null && allowedDomains.length > 0) {
                // check each "other" email to ensure it ends with an accepts domain
                for (String domain : allowedDomains) {
                    if (email.endsWith(domain)) {
                        emailusers.put(email, null);
                    } else {
                        invalids.add(email);
                    }
                }
            } else {
                emailusers.put(email, null);
            }
        }
        String content = newEmailEntry.getContent();
        if (newEmailEntry.getConfig().isAppendRecipientList()) {
            content = content + compileRecipientList(emailusers, loc);
        }
        String subjectContent = newEmailEntry.getSubject();
        if (StringUtils.isBlank(subjectContent)) {
            subjectContent = messageSource.getMessage("no.subject", null, loc);
        }

        String subject = ((config.getSubjectPrefix() != null) ? config.getSubjectPrefix() : "") + subjectContent;
        try {
            List<Attachment> attachments = new ArrayList<>();
            if (files != null && files.length != 0) {
                for (MultipartFile mf:files) {
                    // Although JavaDoc says it may contain path, Commons implementation always just returns the filename without the path.
                    String filename = Web.escapeHtml(mf.getOriginalFilename());
                    try {
                        File f = File.createTempFile(filename, null);
                        mf.transferTo(f);
                        Attachment attachment = new Attachment(f, filename);
                        attachments.add(attachment);
                    } catch (IOException ioe) {
                        throw new AttachmentException(ioe.getMessage());
                    }
                }
            }
            // send the message 
            List<String> invlist = new ArrayList<>();
            invlist = externalLogic.sendEmail(config, fromEmail, fromDisplay, emailusers, subject, content, attachments);
            if(invlist.size() > 0){
                invalids.addAll(invlist);
            }
            // append to the email archive
            String siteId = externalLogic.getSiteID();
            String fromString = fromDisplay + " <" + fromEmail + ">";
            addToArchive(newEmailEntry, config, files, fromString, subject, siteId, attachments);
            ArrayList<String> listaus = new ArrayList<>();

            // build output message for results screen
            for (Entry<String, String> entry : emailusers.entrySet()) {
                String compareAddr = null;
                String addrStr = null;
                if (entry.getValue() != null && entry.getValue().trim().length() > 0) {
                    addrStr = entry.getValue();
                    compareAddr = "\"" + entry.getValue() + "\" <" + entry.getKey() + ">";
                } else {
                    addrStr = entry.getKey();
                    compareAddr = entry.getKey();
                }
                if (!invalids.contains(compareAddr)) {
                    listaus.add(addrStr);
                }
            }
            if (config.isSendMeACopy()) {
                listaus.add(0,curUser.getDisplayName());
            }
            model.addAttribute("listaus",listaus);
            model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
        } catch (MailsenderException me) {
            //Print this exception
            log.warn(me.getMessage()); 
            List<Map<String, Object []>> msgs = me.getMessages();
            if (msgs != null) {
                for (Map<String, Object []> msg : msgs) {     
                    for (Map.Entry<String, Object []> e : msg.entrySet()) {
                        model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
                        model.addAttribute("emailEntry", newEmailEntry);
                        model.addAttribute("addedTo", externalLogic.isEmailArchiveAddedToSite());
                        model.addAttribute("error",messageSource.getMessage(e.getKey(),e.getValue(), loc));
                        model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
                        model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
                        model.addAttribute("comp", composeLogic);
                    }
                }
                return TEMPLATE_COMPOSE;
            } else {
                model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
                model.addAttribute("emailEntry", newEmailEntry);
                model.addAttribute("addedTo", externalLogic.isEmailArchiveAddedToSite());
                model.addAttribute("error",messageSource.getMessage("verbatim",new String [] { me.getMessage() }, loc));
                model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
                model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
                model.addAttribute("comp",composeLogic);
                return TEMPLATE_COMPOSE;
            }
        } catch (AttachmentException ae) {
            model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
            model.addAttribute("emailEntry", newEmailEntry);
            model.addAttribute("addedTo", externalLogic.isEmailArchiveAddedToSite());
            model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
            model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
            model.addAttribute("comp",composeLogic);
            return TEMPLATE_COMPOSE;
        } catch (IllegalStateException me) {
            model.addAttribute("notAdmin",externalLogic.isUserAllowedInLocation(externalLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,externalLogic.getCurrentLocationId()));
            model.addAttribute("emailEntry", newEmailEntry);
            model.addAttribute("addedTo", externalLogic.isEmailArchiveAddedToSite());
            model.addAttribute("error", messageSource.getMessage("verbatim", new String [] {me.getMessage()}, loc));
            model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
            model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
            model.addAttribute("comp",composeLogic);
            return TEMPLATE_COMPOSE;
        }
        // Display Users with Bad Emails if the option is turned on.
        boolean showBadEmails = config.isDisplayInvalidEmails();
        model.addAttribute("badem",showBadEmails);
        if (showBadEmails && invalids != null && invalids.size() > 0) {
            model.addAttribute("bademails",messageSource.getMessage("invalid.email.addresses",null, loc));
            model.addAttribute("invemails", invalids);
        }
        return TEMPLATE_RESULTS;
    }
}
