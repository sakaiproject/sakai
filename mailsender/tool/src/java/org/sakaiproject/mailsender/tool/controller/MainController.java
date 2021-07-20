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
package org.sakaiproject.mailsender.tool.controller;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
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
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Controller
public class MainController {
    
    public static final String EMAIL_SENT = "emailSent";
    public static final String EMAIL_FAILED = "emailFailed";
    public static final String EMAIL_CANCELLED = "emailCancelled";

    private Map<String, MultipartFile> multipartMap;
    private MessageSource messageSource;
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
    
    
    private EmailEntry emailEntry;
      
   
    
     private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }
    private void addEmailUsers(String fromEmail, HashMap<String, String> emailusers,
			List<User> users)
	{
		for (User user : users)
		{
			addEmailUser(fromEmail, emailusers, user);
		}
	}
    
    private void addEmailUser(String fromEmail, HashMap<String, String> emailusers,
			User user)
	{
		if (!fromEmail.equals(user.getEmail()))
		{
			emailusers.put(user.getEmail(), user.getDisplayName());
		}
	}

    private HashSet<String> compileEmailList(String fromEmail, HashMap<String, String> emailusers)
	{
		HashSet<String> badEmails = new HashSet<String>();
		
			try
			{
				addEmailUsers(fromEmail, emailusers, composeLogic.getUsers());
			}
			catch (IdUnusedException e)
			{
				log.warn(e.getMessage(), e);
				badEmails.add(e.getMessage());
			}
		
                return badEmails;
        }
    @RequestMapping(value = {"/","/compose"})
    public String showCompose(Model model, HttpServletRequest request, HttpServletResponse response){
       try {
           
        Locale loc =localeResolver(request,response);
        // get the user then name & email
        User curUser = externalLogic.getCurrentUser();
       
        String fromEmail = "";
        String fromDisplay = "";
        if (curUser != null) {
            fromEmail = curUser.getEmail();
            fromDisplay = curUser.getDisplayName();
        }
        String from = fromDisplay + " <" + fromEmail + ">";
        ConfigEntry confi=configLogic.getConfig();
        model.addAttribute("username",from);
        model.addAttribute("added",externalLogic.isEmailArchiveAddedToSite());
        model.addAttribute("config",confi);
        //model.addAttribute("roles",composeLogic.getEmailRoles());
        //model.addAttribute("userl",composeLogic.getUsersByRole("access").);
        model.addAttribute("comp",composeLogic);
        model.addAttribute("comp1",composeLogic);
        //System.out.println(composeLogic.getEmailRoles().size()+"tete");
        model.addAttribute("siteID",externalLogic.getSiteID());
        model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
        model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
        List<EmailRole> list= composeLogic.getEmailRoles();
        for(EmailRole hola :list ){
           System.out.println(hola.getRoleId());
           for(User u:composeLogic.getUsersByRole(hola.getRoleId())){
               System.out.println(u.getDisplayId());
           }
       }
        
          System.out.println("grupos");
        List<EmailRole> lista = composeLogic.getEmailGroups();
        for(EmailRole holas :lista ){
           System.out.println(holas.getRoleId());
           for(User u:composeLogic.getUsersByGroup(holas.getRoleId())){
               System.out.println(u.getDisplayId());
           }
       }
        
        HashMap<String, String> emailusers= new HashMap<>();
        compileEmailList(fromEmail, emailusers);
        
            //externalLogic.sendEmail(configLogic.getConfig(), fromEmail, from, emailusers, from, from,null);
        } catch (GroupNotDefinedException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IdUnusedException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "compose";
    }

     @RequestMapping(value = {"/options"})
    public String showOptions(Model model, HttpServletRequest request, HttpServletResponse response) {
               
        Locale loc=localeResolver(request,response);
        
        ConfigEntry opconf =configLogic.getConfig();
        //opconf.setSendMeACopy(true);
        System.out.println(opconf.getReplyTo()+"  yeye");
        model.addAttribute("added",externalLogic.isEmailArchiveAddedToSite());
        model.addAttribute("prefix",configLogic.allowSubjectPrefixChange());
        model.addAttribute("config",opconf);
        model.addAttribute("siteID",externalLogic.getSiteID());
        System.out.println(opconf.getSubjectPrefixType()+"  te "+opconf.getSubjectPrefix());

        return "options";
    }
    
    @RequestMapping(value = {"/options"}, method = RequestMethod.POST)
    public String submitOptions(Model model, HttpServletRequest request, HttpServletResponse response) {
               
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale locale = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, locale);
        
        
        ConfigEntry confi=configLogic.getConfig();
        System.out.println((request.getParameterValues("replyto").toString()));
        confi.setSendMeACopy(Boolean.parseBoolean(request.getParameter("sendMeACopy")));
        confi.setAddToArchive(Boolean.parseBoolean(request.getParameter("addToArchive")));
        System.out.println(Boolean.parseBoolean(request.getParameter("addToArchive")));
        confi.setAppendRecipientList(Boolean.parseBoolean(request.getParameter("appendRecipientList")));
        confi.setReplyTo(request.getParameter("replyto"));
        confi.setDisplayInvalidEmails(Boolean.parseBoolean(request.getParameter("InvalidEm")));
        confi.setDisplayEmptyGroups(Boolean.parseBoolean(request.getParameter("rcpsempty")));
        if(request.getParameter("prefix").equals("custom")||!request.getParameter("subjectPrefix").isEmpty()){
            confi.setSubjectPrefixType(request.getParameter("prefix"));
            confi.setSubjectPrefix(request.getParameter("subjectPrefix"));}
        else{
            confi.setSubjectPrefixType(request.getParameter("prefix"));
        }
            
        System.out.println(request.getParameter("prefix")+" aanuel"+request.getParameter("prefix"));
        
        System.out.println(configLogic.saveConfig(confi));
        
        return "redirect:/compose";
    }
    
    
    @RequestMapping(value = {"/permissions"})
    public String showPermissions(Model model, HttpServletRequest request, HttpServletResponse response) {
               
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale locale = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, locale);

        return "permissions";
    }
    
   @RequestMapping(value = {"/compose"}, method = RequestMethod.POST)
    public String submitCompose(Model model, HttpServletRequest request, HttpServletResponse response) {
                     
       try {        
        Locale loc=localeResolver(request,response);
        // get the user then name & email
        User curUser = externalLogic.getCurrentUser();
       
        String fromEmail = "";
        String fromDisplay = "";
        if (curUser != null) {
            fromEmail = curUser.getEmail();
            fromDisplay = curUser.getDisplayName();
        }
        
        if (fromEmail == null || fromEmail.trim().length() == 0)
		{
			messageSource.getMessage("no.from.address",null ,loc);
			return EMAIL_FAILED;
		}
        String from = fromDisplay + " <" + fromEmail + ">";
        ConfigEntry confi=configLogic.getConfig();
        EmailEntry emailEntry= new EmailEntry(confi);
 
        
        
           System.out.println(emailEntry.getRoleIds().size()+"yeaaahhhh");
        
        if(!Boolean.parseBoolean(request.getParameter("rcptsall"))){
                        
        }else{
        emailEntry.setAllIds(Boolean.parseBoolean(request.getParameter("rcptsall")));
        
        }
        
        System.out.println(emailEntry.getAttachments().size()+"nanotete");
        HashMap<String, String> emailusers= new HashMap<>();
        compileEmailList(fromEmail, emailusers);
        String HTML=request.getParameter("editor1");
        String subject=request.getParameter("subject");
        System.out.println(request.getParameter("attachmentArea")+"jajajajjajaja");
        List<Attachment> attachments = new ArrayList<Attachment>();
            if (multipartMap != null && !multipartMap.isEmpty()) {
                for (Entry<String, MultipartFile> entry : multipartMap.entrySet()) {
                    MultipartFile mf = entry.getValue();
                    // Although JavaDoc says it may contain path, Commons implementation always just
                    // returns the filename without the path.
                    String filename = Web.escapeHtml(mf.getOriginalFilename());
                    File f = File.createTempFile(filename, null);
                    mf.transferTo(f);
                    Attachment attachment = new Attachment(f, filename);
                    attachments.add(attachment);
		    }
            }
           externalLogic.sendEmail(configLogic.getConfig(), fromEmail, fromDisplay, emailusers ,subject, HTML ,attachments);
           
        } catch (AttachmentException ex) {System.out.println(ex);
          //  Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MailsenderException ex) {System.out.println(ex);
            //Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "redirect:/compose";
        
   /* public @ResponseBody
    ResponseEntity<String> EmailSubmit(HttpServletRequest req, Model model, @RequestBody String requestString, HttpServletResponse response) throws JsonProcessingException, ParseException {
        ResponseEntity<String> responseBody;

        String userId = sessionManager.getCurrentSessionUserId();
        Locale loc = localeResolver(req, response);
        JSONObject jsonResponse = new JSONObject();
        
            String jsonParam = requestString;
            if (StringUtils.isBlank(jsonParam)) {
                jsonParam = "[]";
            }
            EmailEntry emailEntry=new EmailEntry(ConfigEntry.DEFAULT_CONFIG);
            
            ObjectMapper objectMapper = new ObjectMapper();
           
            String jsonString = objectMapper.writeValueAsString(emailEntry);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonString);

            JSONObject modifiedJson = (JSONObject) parser.parse(jsonParam);

            Set<Object> modifiedJsonKeys = modifiedJson.keySet();
            modifiedJsonKeys.forEach(dynamicKey -> {
                Object line = modifiedJson.get(dynamicKey);
                json.put(dynamicKey, line);
            });

            emailEntry = objectMapper.readValue(json.toJSONString(), EmailEntry.class);
        return responseBody = ResponseEntity.status(HttpStatus.OK).body(jsonResponse.toJSONString());
   */ }
}