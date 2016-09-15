package org.sakaiproject.sitemanage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;



public class ETSUserNotificationProviderImpl implements UserNotificationProvider {
	
	private static final Logger M_log = LoggerFactory.getLogger(ETSUserNotificationProviderImpl.class);
	
	private static final String NOTIFY_ADDED_PARTICIPANT ="sitemange.notifyAddedParticipant";

	private static final String NOTIFY_NEW_USER ="sitemanage.notifyNewUserEmail"; 
	
	private static final String NOTIFY_TEMPLATE_USE = "sitemanage.notifyTemplateUse";
	
	// to send an email to course authorizer based on course site request
	private static final String NOTITY_COURSE_REQUEST_AUTHORIZER = "sitemanage.notifyCourseRequestAuthorizer";
	
	// to send an email to course site requestor
	private static final String NOTIFY_COURSE_REQUEST_REQUESTER = "sitemanage.notifyCourseRequestRequester";
	
	// to send an email to support team about course request
	private static final String NOTIFY_COURSE_REQUEST_SUPPORT = "sitemanage.notifyCourseRequestSupport";
	
	private static final String NOTIFY_SITE_CREATION = "sitemanage.notifySiteCreation";
	
	private static final String NOTIFY_SITE_CREATION_CONFIRMATION = "sitemanage.notifySiteCreation.confirmation";
	
	private static final String ADMIN = "admin";
	
	private static final String SITE_IMPORT_EMAIL_TEMPLATE_FILE_NAME 		= "notifySiteImportConfirmation.xml";
   private static final String SITE_IMPORT_EMAIL_TEMPLATE_KEY 				= "sitemanage.siteImport.Confirmation";
   private static final String SITE_IMPORT_EMAIL_TEMPLATE_VAR_WORKSITE 	= "worksiteName";
   private static final String SITE_IMPORT_EMAIL_TEMPLATE_VAR_LINK 		= "linkToWorksite";
   private static final String SITE_IMPORT_EMAIL_TEMPLATE_VAR_INSTITUTION 	= "institution";
   private static final String SAK_PROP_UI_INSTITUTION						= "ui.institution";
	
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
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService dhs) {
		this.developerHelperService = dhs;
	}

	public void init() {
		//nothing realy to do
		M_log.info("init()");

    	loadTemplate("notifyAddedParticipants.xml", NOTIFY_ADDED_PARTICIPANT);
    	loadTemplate("notifyNewuser.xml", NOTIFY_NEW_USER);
    	loadTemplate("notifyTemplateUse.xml", NOTIFY_TEMPLATE_USE);
    	loadTemplate("notifyCourseRequestAuthorizer.xml", NOTITY_COURSE_REQUEST_AUTHORIZER);
    	loadTemplate("notifyCourseRequestRequester.xml", NOTIFY_COURSE_REQUEST_REQUESTER);
    	loadTemplate("notifyCourseRequestSupport.xml", NOTIFY_COURSE_REQUEST_SUPPORT);
    	loadTemplate("notifySiteCreation.xml", NOTIFY_SITE_CREATION);
    	loadTemplate("notifySiteCreationConfirmation.xml", NOTIFY_SITE_CREATION_CONFIRMATION);
			
        loadTemplate(SITE_IMPORT_EMAIL_TEMPLATE_FILE_NAME, SITE_IMPORT_EMAIL_TEMPLATE_KEY);
			
	}
	
	public void notifyAddedParticipant(boolean newNonOfficialAccount,
			User user, Site site) {
		
		String from = serverConfigurationService.getBoolean(NOTIFY_FROM_CURRENT_USER, false)?
				getCurrentUserEmailAddress():getSetupRequestEmailAddress();
		//we need to get the template
		


		if (from != null) {
			String productionSiteName = serverConfigurationService.getString(
					"ui.service", "");
			String emailId = user.getEmail();
			String to = emailId;
			String headerTo = emailId;
			String replyTo = from;
			Map<String, String> rv = new HashMap<>();
			rv.put("productionSiteName", productionSiteName);

			 Map<String, String> replacementValues = new HashMap<>();
			 replacementValues.put("userName", user.getDisplayName());
			 replacementValues.put("userEid", user.getEid());
			 replacementValues.put("localSakaiName", productionSiteName);
			 replacementValues.put("currentUserName",userDirectoryService.getCurrentUser().getDisplayName());
			 replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
			 String nonOfficialAccountUrl = serverConfigurationService.getString("nonOfficialAccount.url", null);
			 replacementValues.put("hasNonOfficialAccountUrl", nonOfficialAccountUrl!=null?Boolean.TRUE.toString().toLowerCase():Boolean.FALSE.toString().toLowerCase());
			 replacementValues.put("nonOfficialAccountUrl",nonOfficialAccountUrl);
			 replacementValues.put("siteName", site.getTitle());
			 replacementValues.put("productionSiteName", productionSiteName);
			 replacementValues.put("newNonOfficialAccount", Boolean.toString(newNonOfficialAccount).toLowerCase());
			 replacementValues.put("xloginText", serverConfigurationService.getString("xlogin.text", "Login"));
			 replacementValues.put("loginText", serverConfigurationService.getString("login.text", "Login"));
			 replacementValues.put("siteUrl", site.getUrl());
			 
			 // send email
			 emailTemplateServiceSend(NOTIFY_ADDED_PARTICIPANT, null, user, from, to, headerTo, replyTo, replacementValues);
				

		} // if

	}

	public void notifyNewUserEmail(User user, String newUserPassword,
			Site site) {
		
		
		String from = getSetupRequestEmailAddress();
		String productionSiteName = serverConfigurationService.getString(
				"ui.service", "");
		String newUserEmail = user.getEmail();
		String to = newUserEmail;
		String headerTo = newUserEmail;
		String replyTo = from;

		if (from != null && newUserEmail != null) {
			/*
			 * $userName
			 * $localSakaiName
			 * $currentUserName
			 * $localSakaiUrl
			 */
			Map<String, String> replacementValues = new HashMap<>();
			replacementValues.put("userName", user.getDisplayName());
			replacementValues.put("localSakaiName",serverConfigurationService.getString("ui.service", ""));
			replacementValues.put("currentUserName",userDirectoryService.getCurrentUser().getDisplayName());
			replacementValues.put("userEid", user.getEid());
			replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
			replacementValues.put("newPassword",newUserPassword);
			replacementValues.put("siteName", site.getTitle());
			replacementValues.put("productionSiteName", productionSiteName);

			// send email
			emailTemplateServiceSend(NOTIFY_NEW_USER, null, user, from, to, headerTo, replyTo, replacementValues);
		}
	}
	
	public void notifyTemplateUse(Site templateSite, User currentUser, Site site) {
		// send an email to track who are using the template
		String from = getSetupRequestEmailAddress();
		// send it to the email archive of the template site
		// TODO: need a better way to get the email archive address
		//String domain = from.substring(from.indexOf('@'));
		String templateEmailArchive = templateSite.getId() + "@" + serverConfigurationService.getServerName();
		String to = templateEmailArchive;
		String headerTo = templateEmailArchive;
		String replyTo = templateEmailArchive;

		if (from != null && templateEmailArchive != null) {
			Map<String, String> replacementValues = new HashMap<>();
			replacementValues.put("templateSiteTitle", templateSite.getTitle());
			replacementValues.put("templateSiteId", templateSite.getId());
			replacementValues.put("currentUserDisplayName", currentUser.getDisplayName());
			replacementValues.put("currentUserDisplayId", currentUser.getDisplayId());
			SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
			dform.applyPattern("yyyy-MM-dd HH:mm:ss");
			String dateDisplay = dform.format(new Date());
			replacementValues.put("currentDate", dateDisplay);
			replacementValues.put("newSiteId", site.getId());
			replacementValues.put("newSiteTitle", site.getTitle());
			
			emailTemplateServiceSend(NOTIFY_TEMPLATE_USE, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
		}
	}
	
	public boolean notifyCourseRequestAuthorizer(String instructorId, String requestEmail, String replyToEmail, String termTitle, String requestSectionInfo, String siteTitle, String siteId, String additionalInfo, String serverName)
	{
		try {
			User instructor = userDirectoryService.getUserByEid(instructorId);
			String to = instructor.getEmail();	
			String from = requestEmail;
			String headerTo = to;
			String replyTo = replyToEmail;
			User currentUser = userDirectoryService.getCurrentUser();
			String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
			String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
			
			
			Map<String, String> replacementValues = new HashMap<>();
			replacementValues.put("currentUserDisplayName", currentUserDisplayName);
			replacementValues.put("currentUserDisplayId", currentUserDisplayId);
			replacementValues.put("termTitle", termTitle);
			replacementValues.put("requestSectionInfo", requestSectionInfo);
			replacementValues.put("siteTitle", siteTitle);
			replacementValues.put("siteId", siteId);
			replacementValues.put("specialInstruction", additionalInfo);
			replacementValues.put("serverName", serverName);
			
			return emailTemplateServiceSend(NOTITY_COURSE_REQUEST_AUTHORIZER, null, instructor, from, to, headerTo, replyTo, replacementValues) != null? true:false;
			
		}
		catch (Exception e)
		{
			M_log.warn(this + " cannot find user " + instructorId, e);
			return false;
		}
	}
	
	public String notifyCourseRequestSupport(String requestEmail, String serverName, String request, String termTitle, int requestListSize, String requestSectionInfo,
			String officialAccountName, String siteTitle, String siteId, String additionalInfo, boolean requireAuthorizer, String authorizerNotified, String authorizerNotNotified)
	{
		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
			
			
		// To Support
		String from = currentUserEmail;
		String to = requestEmail;
		String headerTo = requestEmail;
		String replyTo = currentUserEmail;
		
		Map<String, String> replacementValues = new HashMap<>();
		replacementValues.put("currentUserDisplayName", currentUserDisplayName);
		replacementValues.put("currentUserDisplayId", currentUserDisplayId);
		replacementValues.put("termTitle", termTitle);
		replacementValues.put("requestSectionInfo", requestSectionInfo);
		replacementValues.put("requestListSize", String.valueOf(requestListSize));
		replacementValues.put("siteTitle", siteTitle);
		replacementValues.put("siteId", siteId);
		replacementValues.put("specialInstruction", additionalInfo);
		replacementValues.put("serverName", serverName);
		
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        String dateDisplay = dform.format(new Date());
		replacementValues.put("dateDisplay", dateDisplay);
		
		replacementValues.put("requireAuthorizer", String.valueOf(requireAuthorizer));
		replacementValues.put("authorizerNotified", authorizerNotified);
		replacementValues.put("authorizerNotNotified", authorizerNotNotified);
		
		try
		{
			return emailTemplateServiceSend(NOTIFY_COURSE_REQUEST_SUPPORT, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
		}
		catch (Exception e)
		{
			M_log.warn(this + " problem in send site request email to support for " + currentUserDisplayName, e );
			return "";
		}
	}
	
	public void notifyCourseRequestRequester(String requestEmail, String supportEmailContent, String termTitle)
	{
		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
		
		String from = requestEmail;
		String to = currentUserEmail;
		String headerTo = to;
		String replyTo = to;
		Map<String, String> replacementValues = new HashMap<>();
		replacementValues.put("currentUserDisplayName", currentUserDisplayName);
		replacementValues.put("currentUserDisplayId", currentUserDisplayId);
		replacementValues.put("currentUserEmail", currentUserEmail);
		replacementValues.put("termTitle", termTitle);
		replacementValues.put("supportEmailContent", supportEmailContent);
		replacementValues.put("requestEmail", requestEmail);
		
		emailTemplateServiceSend(NOTIFY_COURSE_REQUEST_REQUESTER, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
	}
	
	public void notifySiteCreation(Site site, List notifySites, boolean courseSite, String termTitle, String requestEmail) {
		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
		
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        String dateDisplay = dform.format(new Date());
		
		String from = currentUserEmail;
		String to = requestEmail;
		String headerTo = requestEmail;
		String replyTo = currentUserEmail;
		Map<String, String> replacementValues = new HashMap<>();
		replacementValues.put("currentUserDisplayName", currentUserDisplayName);
		replacementValues.put("currentUserDisplayId", currentUserDisplayId);
		replacementValues.put("currentUserEmail", currentUserEmail);
		replacementValues.put("dateDisplay", dateDisplay);
		replacementValues.put("termTitle", termTitle);
		replacementValues.put("serviceName", serverConfigurationService.getString( "ui.service", serverConfigurationService.getServerName() ) );
		replacementValues.put("siteTitle", site!=null?site.getTitle():"");
		replacementValues.put("siteId", site!=null?site.getId():"");
		replacementValues.put("courseSite", String.valueOf(courseSite));
		
		StringBuilder buf = new StringBuilder();
		if (notifySites!= null)
		{
			int nbr_sections = notifySites.size();
			replacementValues.put("numSections", String.valueOf(nbr_sections));
			for (int i = 0; i < nbr_sections; i++) {
				String course = (String) notifySites.get(i);
				buf.append( course ).append("\n");
			}
		}
		else
		{
			replacementValues.put("numSections", "0");
		}
		replacementValues.put("sections", buf.toString());
		
		emailTemplateServiceSend(NOTIFY_SITE_CREATION, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
		
		// send a confirmation email to site creator
		from = requestEmail;
		to = currentUserEmail;
		headerTo = currentUserEmail;
		replyTo = serverConfigurationService.getString("setup.request","no-reply@" + serverConfigurationService.getServerName());
		emailTemplateServiceSend(NOTIFY_SITE_CREATION_CONFIRMATION, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
		
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

	@SuppressWarnings("unchecked")
	private void loadTemplate(String templateFileName, String templateRegistrationString) 
	{
		M_log.info(this + " loading template " + templateFileName);
		//we need a user session to avoid potential NPE's
		Session sakaiSession = sessionManager.getCurrentSession();
		try {
			sakaiSession.setUserId(ADMIN);
		    sakaiSession.setUserEid(ADMIN);
			InputStream in = ETSUserNotificationProviderImpl.class.getClassLoader().getResourceAsStream(templateFileName);
			Document document = new SAXBuilder(  ).build(in);
			List<Element> it = document.getRootElement().getChildren("emailTemplate");
			
			for( Element xmlTemplate : it ) {
				xmlToTemplate(xmlTemplate, templateRegistrationString);
			}
		} catch (IOException | JDOMException e) {
			M_log.warn(e.getMessage());
		} 
		finally
		{
			sakaiSession.setUserId(null);
		    sakaiSession.setUserEid(null);
		}
	}

	private void xmlToTemplate(Element xmlTemplate, String key) {
		String subject = xmlTemplate.getChildText("subject");
		String body = xmlTemplate.getChildText("message");
		String locale = xmlTemplate.getChildText("locale");
		String versionString = xmlTemplate.getChildText("version");
		Locale loc = null;
		
		if (locale != null && !"".equals(locale)) {
			loc = LocaleUtils.toLocale(locale);
		}
		
		
		if (!emailTemplateService.templateExists(key, loc))
		{
			EmailTemplate template = new EmailTemplate();
			template.setSubject(subject);
			template.setMessage(body);
			template.setLocale(locale);
			template.setKey(key);
			template.setVersion(1);
			template.setOwner("admin");
			template.setLastModified(new Date());
			this.emailTemplateService.saveTemplate(template);
			M_log.info(this + " user notification template " + key + " added");
		}
		else
		{
			EmailTemplate existingTemplate = this.emailTemplateService.getEmailTemplate(key, new Locale(locale));
			String oVersionString = existingTemplate.getVersion() != null ? existingTemplate.getVersion().toString():null;
			if ((oVersionString == null && versionString != null) || (oVersionString != null && versionString != null && !oVersionString.equals(versionString)))
			{
				existingTemplate.setSubject(subject);
				existingTemplate.setMessage(body);
				existingTemplate.setLocale(locale);
				existingTemplate.setKey(key);
				existingTemplate.setVersion(Integer.valueOf(versionString));
				existingTemplate.setOwner("admin");
				existingTemplate.setLastModified(new Date());
				this.emailTemplateService.updateTemplate(existingTemplate);
			M_log.info(this + " user notification template " + key + " updated to newer version");
			}
		}
			
	}

	private String getCurrentUserEmailAddress() {
		User currentUser = userDirectoryService.getCurrentUser();
		String email = currentUser != null ? currentUser.getEmail():null;
		if (email == null || email.length() == 0) {
			email = getSetupRequestEmailAddress();
		}
		return email;
	}
	
	/**
	 * use EmailTemplateService to send email
	 * @param templateName
	 * @param user
	 * @param from
	 * @param to
	 * @param headerTo
	 * @param replyTo
	 * @param replacementValues
	 * @return the email content
	 */
	private String emailTemplateServiceSend(String templateName, Locale locale, User user, String from, String to, String headerTo, String replyTo, Map<String, String> replacementValues) {
		M_log.debug("getting template: " + templateName);
		RenderedTemplate template;
		try { 
			if (locale == null)
			{
				// use user's locale
				template = emailTemplateService.getRenderedTemplateForUser(templateName, user!=null?user.getReference():"", replacementValues);
			}
			else
			{
				// use local
				template = emailTemplateService.getRenderedTemplate(templateName, locale, replacementValues);
			}
			if (template != null)
			{
				List<String> headers = new ArrayList<>();
				headers.add("Precedence: bulk");
				
				String content = template.getRenderedMessage();	
				emailService.send(from, to, template.getRenderedSubject(), content, headerTo, replyTo, headers);
				return content;
			}
       }
       catch (Exception e) {
    	   M_log.warn(this + e.getMessage());
    	   return null;
       }
       return null;
	}
	
	public void notifySiteImportCompleted(String toEmail, String siteId, String siteTitle){
		if(toEmail != null && !"".equals(toEmail)){
			
			// Create the map of replacement values
			Map<String, String> replacementValues = new HashMap<>();
			replacementValues.put(SITE_IMPORT_EMAIL_TEMPLATE_VAR_WORKSITE, siteTitle);
			replacementValues.put(SITE_IMPORT_EMAIL_TEMPLATE_VAR_LINK, developerHelperService.getLocationReferenceURL(SITE_REF_PREFIX + siteId));
			replacementValues.put(SITE_IMPORT_EMAIL_TEMPLATE_VAR_INSTITUTION, serverConfigurationService.getString(SAK_PROP_UI_INSTITUTION));
			
			// Use the email template service to send the email
			String headerTo = toEmail;
			String replyTo = toEmail;
			emailTemplateServiceSend(SITE_IMPORT_EMAIL_TEMPLATE_KEY, Locale.ENGLISH, userDirectoryService.getCurrentUser(), getSetupRequestEmailAddress(),
					toEmail, headerTo, replyTo, replacementValues);
		}
	}
}
