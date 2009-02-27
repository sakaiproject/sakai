package org.sakaiproject.sitemanage.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.xerces.util.DOMUtil;



public class ETSUserNotificationProviderImpl implements UserNotificationProvider {
	
	private static Log M_log = LogFactory.getLog(ETSUserNotificationProviderImpl.class);
	
	private static String NOTIFY_ADDED_PARTICIPANT ="sitemange.notifyAddedParticipant";

	private static String NOTIFY_NEW_USER ="sitemanage.notifyNewUserEmail"; 
	
	private static final String ADMIN = "admin";
	
	private EmailService emailService; 
	
	
	
	public void setEmailService(EmailService es) {
		emailService = es;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService scs) {
		serverConfigurationService = scs;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService uds) {
		userDirectoryService = uds;
	}
	
	private EmailTemplateService emailTemplateService;
	public void setEmailTemplateService(EmailTemplateService ets) {
		emailTemplateService = ets;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager s) {
		this.sessionManager = s;
	}

	public void init() {
		//nothing realy to do
		M_log.info("init()");
		
		
		//do we need to load data?
		Map<String, String> replacementValues = new HashMap<String, String>();
		
		// put placeholders for replacement values 
		replacementValues.put("userName", "");
        replacementValues.put("userEid", "");
        replacementValues.put("localSakaiName", "");
        replacementValues.put("currentUserName", "");
        replacementValues.put("currentUserDisplayName", "");
        replacementValues.put("localSakaiURL", "");
        replacementValues.put("siteName", "");
        replacementValues.put("productionSiteName", "");
        replacementValues.put("newNonOfficialAccount", "");
        replacementValues.put("newPassword", "");
        replacementValues.put("productionSiteName", "");
        
		if (emailTemplateService.getRenderedTemplateForUser(this.NOTIFY_ADDED_PARTICIPANT, "/user/admin", replacementValues) == null) 
			loadAddedParticipantMail();
		else 
			M_log.info("templates for " + NOTIFY_ADDED_PARTICIPANT + " exist");
		
		if (serverConfigurationService.getBoolean("auto.ddl", false)) {
			if (emailTemplateService.getRenderedTemplateForUser(NOTIFY_NEW_USER, "/user/admin", replacementValues) == null) 
				loadNewUserMail();
			else 
				M_log.info("templates for " + NOTIFY_NEW_USER + " exist");
			
			if (emailTemplateService.getRenderedTemplateForUser(this.NOTIFY_ADDED_PARTICIPANT, "/user/admin", replacementValues) == null) 
				loadAddedParticipantMail();
			else 
				M_log.info("templates for " + NOTIFY_NEW_USER + " exist");
			
		}
	}
	
	public void notifyAddedParticipant(boolean newNonOfficialAccount,
			User user, String siteTitle) {
		
		String from = getSetupRequestEmailAddress();
		//we need to get the template
		


		if (from != null) {
			String productionSiteName = serverConfigurationService.getString(
					"ui.service", "");
			String productionSiteUrl = serverConfigurationService
					.getPortalUrl();
			String nonOfficialAccountUrl = serverConfigurationService.getString(
					"nonOfficialAccount.url", null);
			String emailId = user.getEmail();
			String to = emailId;
			String headerTo = emailId;
			String replyTo = emailId;
			Map<String, String> rv = new HashMap<String, String>();
			rv.put("productionSiteName", productionSiteName);

			
			String content = "";
			/*
			 * $userName
			 * $localSakaiName
			 * $currentUserName
			 * $localSakaiUrl
			 */
			 Map<String, String> replacementValues = new HashMap<String, String>();
	            replacementValues.put("userName", user.getDisplayName());
	            replacementValues.put("userEid", user.getEid());
	            replacementValues.put("localSakaiName",serverConfigurationService.getString(
	    				"ui.service", ""));
	            replacementValues.put("currentUserName",userDirectoryService.getCurrentUser().getDisplayName());
	            replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
	            replacementValues.put("siteName", siteTitle);
	            replacementValues.put("productionSiteName", productionSiteName);
	            replacementValues.put("newNonOfficialAccount", new Boolean(newNonOfficialAccount).toString());
	         
	            M_log.debug("getting template: sitemange.notifyAddedParticipant");
	            RenderedTemplate template = null;
	           try { 
				template = emailTemplateService.getRenderedTemplateForUser(NOTIFY_ADDED_PARTICIPANT, user.getReference(), replacementValues); 
				if (template == null)
					return;	
	           }
	           catch (Exception e) {
	        	   e.printStackTrace();
	           }
			List headers = new ArrayList();
			headers.add("Precedence: bulk");
			
			content = template.getRenderedMessage();	
			emailService.send(from, to, template.getRenderedSubject(), content, headerTo,
					replyTo, headers);

		} // if

	}

	public void notifyNewUserEmail(User user, String newUserPassword,
			String siteTitle) {
		
		
		String from = getSetupRequestEmailAddress();
		String productionSiteName = serverConfigurationService.getString(
				"ui.service", "");
		String productionSiteUrl = serverConfigurationService.getPortalUrl();
		
		String newUserEmail = user.getEmail();
		String to = newUserEmail;
		String headerTo = newUserEmail;
		String replyTo = newUserEmail;
		
		
		
		
		 
		String content = "";

		
	
		
		if (from != null && newUserEmail != null) {
			/*
			 * $userName
			 * $localSakaiName
			 * $currentUserName
			 * $localSakaiUrl
			 */
			 Map<String, String> replacementValues = new HashMap<String, String>();
	            replacementValues.put("userName", user.getDisplayName());
	            replacementValues.put("localSakaiName",serverConfigurationService.getString(
	    				"ui.service", ""));
	            replacementValues.put("currentUserName",userDirectoryService.getCurrentUser().getDisplayName());
	            replacementValues.put("userEid", userDirectoryService.getCurrentUser().getEid());
	            replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
	            replacementValues.put("newPassword",newUserPassword);
	            replacementValues.put("siteName", siteTitle);
	            replacementValues.put("productionSiteName", productionSiteName);
	        RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser(this.NOTIFY_NEW_USER, user.getReference(), replacementValues);    		
	    	if (template == null)
				return;
	        content = template.getRenderedMessage();
			
			String message_subject = template.getRenderedSubject();
			List headers = new ArrayList();
			headers.add("Precedence: bulk");
			emailService.send(from, to, message_subject, content, headerTo,
					replyTo, headers);
		}
	}

	/*
	 *  Private methods
	 */
	
	private String getSetupRequestEmailAddress() {
		String from = serverConfigurationService.getString("setup.request",
				null);
		if (from == null) {
			M_log.warn(this + " - no 'setup.request' in configuration");
			from = "postmaster@".concat(serverConfigurationService
					.getServerName());
		}
		return from;
	}



	private void loadAddedParticipantMail() {
		try {
			//we need a user session to avoind potential NPE's
			Session sakaiSession = sessionManager.getCurrentSession();
			sakaiSession.setUserId(ADMIN);
		    sakaiSession.setUserEid(ADMIN);
			InputStream in = ETSUserNotificationProviderImpl.class.getClassLoader().getResourceAsStream("notifyAddedParticipants.xml");
			Document document = new SAXBuilder(  ).build(in);
			List it = document.getRootElement().getChildren("emailTemplate");
			
			for (int i =0; i < it.size(); i++) {
				Element xmlTemplate = (Element)it.get(i);
				xmlToTemplate(xmlTemplate, this.NOTIFY_ADDED_PARTICIPANT);
			}
			sakaiSession.setUserId(null);
		    sakaiSession.setUserEid(null);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private void loadNewUserMail() {
		try {
			//we need a user session to avoind potential NPE's
			Session sakaiSession = sessionManager.getCurrentSession();
			sakaiSession.setUserId(ADMIN);
		    sakaiSession.setUserEid(ADMIN);
			InputStream in = ETSUserNotificationProviderImpl.class.getClassLoader().getResourceAsStream("notifyNewuser.xml");
			Document document = new SAXBuilder(  ).build(in);
			List it = document.getRootElement().getChildren("emailTemplate");
			
			for (int i =0; i < it.size(); i++) {
				Element xmlTemplate = (Element)it.get(i);
				xmlToTemplate(xmlTemplate, this.NOTIFY_NEW_USER);
			}
			sakaiSession.setUserId(null);
		    sakaiSession.setUserEid(null);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private void xmlToTemplate(Element xmlTemplate, String key) {
		String subject = xmlTemplate.getChildText("subject");
		String body = xmlTemplate.getChildText("message");
		String locale = xmlTemplate.getChildText("locale");
		M_log.info("subject: " + subject);
		
		EmailTemplate template = new EmailTemplate();
		template.setSubject(subject);
		template.setMessage(body);
		template.setLocale(locale);
		template.setKey(key);
		template.setOwner("admin");
		template.setLastModified(new Date());
		
		this.emailTemplateService.saveTemplate(template);
	}



	
}
