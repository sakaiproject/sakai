/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.sitemanage.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class UserNotificationProviderImpl implements UserNotificationProvider {

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
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService dhs) {
		developerHelperService = dhs;
	}
	
	/** portlet configuration parameter values* */
	/** Resource bundle using current language locale */
	//private static ResourceLoader rb = new ResourceLoader("UserNotificationProvider");

	public void init() {
		//nothing realy to do
		log.info("init()");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void notifyAddedParticipant(boolean newNonOfficialAccount,
			User user, Site site) {
		ResourceLoader rb = new ResourceLoader(user.getId(), "UserNotificationProvider");
		
		String from = serverConfigurationService.getBoolean(NOTIFY_FROM_CURRENT_USER, false)?
				getCurrentUserEmailAddress():getSetupRequestEmailAddress();
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
			// UVa: change Reply-To to be the user adding the new participant
			String replyTo = from;
			String message_subject = productionSiteName + " "
					+ rb.getString("java.sitenoti");
			String content = "";
			StringBuilder buf = new StringBuilder();
			buf.setLength(0);

			// email bnonOfficialAccounteen newly added nonOfficialAccount account
			// and other users
			buf.append(user.getDisplayName() + ":\n\n");
			buf.append(rb.getString("java.following") + " "
					+ productionSiteName + " "
					+ rb.getString("java.simplesite") + "\n");
			buf.append(site.getTitle() + "\n");
			buf.append(rb.getString("java.simpleby") + " ");
			buf.append(userDirectoryService.getCurrentUser().getDisplayName()
					+ ". \n\n");
			if (newNonOfficialAccount) {
				buf.append(serverConfigurationService.getString(
						"nonOfficialAccountInstru", "")
						+ "\n");

				if (nonOfficialAccountUrl != null) {
					buf.append(rb.getString("java.togeta1") + "\n"
							+ nonOfficialAccountUrl + "\n");
					buf.append(rb.getString("java.togeta2") + "\n\n");
				}
				buf.append(rb.getString("java.once") + " " + productionSiteName
						+ ": \n");
				buf.append(rb.getString("java.loginhow1") + " "
						+ productionSiteName + ": " + productionSiteUrl + "\n");
				buf.append(rb.getString("java.loginhow2") + "\n");
				buf.append(rb.getString("java.loginhow3") + "\n");
			} else {
				buf.append(rb.getString("java.tolog") + "\n");
				buf.append(rb.getString("java.loginhow1") + " "
						+ productionSiteName + ": " + productionSiteUrl + "\n");
				buf.append(rb.getString("java.loginhow2") + "\n");
				buf.append(rb.getString("java.loginhow3u") + "\n");
			}
			buf.append(rb.getString("java.tabscreen"));
			content = buf.toString();
			emailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);

		} // if

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNewUserEmail(User user, String newUserPassword,
			Site site) {
		ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
		// set the locale to individual receipient's setting
		rb.setContextLocale(rb.getLocale(user.getId()));
		
		String from = getSetupRequestEmailAddress();
		String productionSiteName = serverConfigurationService.getString(
				"ui.service", "");
		String productionSiteUrl = serverConfigurationService.getPortalUrl();
		
		String newUserEmail = user.getEmail();
		String to = newUserEmail;
		String headerTo = newUserEmail;
        // UVa: change Reply-To to be the From (collab support) address
		String replyTo = from;
		String message_subject = productionSiteName + " "
				+ rb.getString("java.newusernoti");
		String content = "";

		if (from != null && newUserEmail != null) {
			StringBuilder buf = new StringBuilder();
			buf.setLength(0);

			// email body
			buf.append(user.getDisplayName() + ":\n\n");

			buf.append(rb.getString("java.addedto") + " " + productionSiteName
					+ " (" + productionSiteUrl + ") ");
			buf.append(rb.getString("java.simpleby") + " ");
			buf.append(userDirectoryService.getCurrentUser().getDisplayName()
					+ ". \n\n");
			buf.append(rb.getString("java.passwordis1") + "\n"
					+ newUserPassword + "\n\n");
			buf.append(rb.getString("java.passwordis2") + "\n\n");

			content = buf.toString();
			emailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void notifyTemplateUse(Site templateSite, User currentUser, Site site) {
		// send an email to track who are using the template
		String from = getSetupRequestEmailAddress();
		// send it to the email archive of the template site
		// TODO: need a better way to get the email archive address
		//String domain = from.substring(from.indexOf('@'));
		String templateEmailArchive = templateSite.getId() 
			+ "@" + serverConfigurationService.getServerName();
		String to = templateEmailArchive;
		String headerTo = templateEmailArchive;
		String replyTo = templateEmailArchive;
		String message_subject = templateSite.getId() + ": copied by " + currentUser.getDisplayId ();					

		if (from != null && templateEmailArchive != null) {
			StringBuffer buf = new StringBuffer();
			buf.setLength(0);

			// email body
			buf.append("Dear template maintainer,\n\n");
			buf.append("Congratulations!\n\n");
			buf.append("The following user just created a new site based on your template.\n\n");
			buf.append("Template name: " + templateSite.getTitle() + "\n");
			buf.append("User         : " + currentUser.getDisplayName() + " (" 
					+ currentUser.getDisplayId () + ")\n");
			buf.append("Date         : " + new java.util.Date() + "\n");
			buf.append("New site Id  : " + site.getId() + "\n");
			buf.append("New site name: " + site.getTitle() + "\n\n");
			buf.append("Cheers,\n");
			buf.append("Alliance Team\n");
			String content = buf.toString();
			
			emailService.send(from, to, message_subject, content, headerTo, replyTo, null);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void notifySiteCreation(Site site, List notifySites, boolean courseSite, String termTitle, String requestEmail) {
		// send emails
		String id = site.getId();
		String title = site.getTitle();
		
        SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        String dateDisplay = dform.format(new Date());
		
        User currentUser = userDirectoryService.getCurrentUser();
		String currentUserName = currentUser.getDisplayName();
		String currentUserId = currentUser.getId();
		String currentUserEmail = currentUser.getEmail();
		
		ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
		
		String message_subject = courseSite ? rb.getString("java.official") + " "
				+ currentUserName
				+ " " + rb.getString("java.for") + " " + termTitle : rb.getString("java.site.createdBy") + " " + currentUserName;
		
		String from = currentUser.getEmail();
		String to = requestEmail;
		String headerTo = requestEmail;
		String replyTo = currentUserEmail;
		StringBuilder buf = new StringBuilder();
		buf.append("\n" + rb.getString("java.fromwork") + " "
				+ serverConfigurationService.getServerName() + " "
				+ rb.getString("java.supp") + ":\n\n");
		buf.append(courseSite ? rb.getString("java.off") : rb.getString("java.site"));
		buf.append(" '" + title + "' (id " + id
				+ "), " + rb.getString("java.wasset") + " ");
		buf.append(currentUserName + " (" + currentUserId + ", "
				+ rb.getString("java.email2") + " " + replyTo + ") ");
		buf.append(rb.getString("java.on") + " " + dateDisplay);
		if (courseSite)
		{
			buf.append(rb.getString("java.for") + " " + termTitle + ", ");
		}
		if (notifySites!= null)
		{
			int nbr_sections = notifySites.size();
			if (nbr_sections > 1) {
				buf.append(rb.getString("java.withrost") + " "
						+ Integer.toString(nbr_sections) + " "
						+ rb.getString("java.sections") + "\n\n");
			} else {
				buf.append(" " + rb.getString("java.withrost2") + "\n\n");
			}

			for (int i = 0; i < nbr_sections; i++) {
				String course = (String) notifySites.get(i);
				buf.append(rb.getString("java.course2") + " " + course + "\n");
			}
		}
		emailService.send(from, to, message_subject, buf.toString(), headerTo, replyTo, null);
		
		// send a confirmation email to site creator
		from = requestEmail;
		to = currentUserEmail;
		headerTo = currentUserEmail;
		replyTo = serverConfigurationService.getString("setup.request","no-reply@" + serverConfigurationService.getServerName());
		String content = rb.getFormattedMessage("java.siteCreation.confirmation", new Object[]{title, serverConfigurationService.getServerName()});
		content += "\n\n" + buf.toString();
		emailService.send(from, to, message_subject, content, headerTo, replyTo, null);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean notifyCourseRequestAuthorizer(String instructorId, String requestEmail, String replyToEmail, String termTitle, String requestSectionInfo, String siteTitle, String siteId, String additionalInfo, String serverName)
	{
		try {
			User instructor = userDirectoryService.getUserByEid(instructorId);
			
			ResourceLoader rb = new ResourceLoader(instructorId, "UserNotificationProvider");
			
			StringBuffer buf = new StringBuffer();
			
			String to = instructor.getEmail();	
			String from = requestEmail;
			String headerTo = to;
			String replyTo = replyToEmail;
			
			User currentUser = userDirectoryService.getCurrentUser();
			String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
			
			// message subject
			String message_subject = rb.getString("java.sitereqfrom") + " " + currentUserDisplayName + " " + rb.getString("java.for") + " " + termTitle;
			
			buf.append(rb.getString("java.hello") + " \n\n");
			buf.append(rb.getString("java.receiv") + " "
					+ currentUserDisplayName + ", ");
			buf.append(rb.getString("java.who") + "\n");
			if (!termTitle.isEmpty()) {
				buf.append(termTitle + "\n");
			}
			
			// course section information
			buf.append(requestSectionInfo);
			
			buf.append(rb.getString("java.sitetitle") + "\t"
					+ siteTitle + "\n");
			buf.append(rb.getString("java.siteid") + "\t" + siteId + "\n\n");			
			buf.append(rb.getString("java.siteinstr") + "\n" + additionalInfo
					+ "\n\n");
			buf.append(rb.getString("java.according")
					+ " " + currentUserDisplayName + " "
					+ rb.getString("java.record"));
			buf.append(" " + rb.getString("java.canyou") + " "
					+ currentUserDisplayName + " "
					+ rb.getString("java.assoc") + "\n\n");
			buf.append(rb.getString("java.respond") + " "
					+ currentUserDisplayName
					+ rb.getString("java.appoint") + "\n\n");
			buf.append(rb.getString("java.thanks") + "\n");
			buf.append(serverName + " "
					+ rb.getString("java.support"));

			try
			{
				// send the email
				emailService.send(from, to, message_subject, buf.toString(), headerTo, replyTo, null);
				return true;
			}
			catch (Exception ee)
			{
				log.warn(this + " problem occurs with sending course request email to authorizer " + instructorId );
				return false;
			}
		}
		catch (Exception e)
		{
			log.warn(this + " cannot find user " + instructorId);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String notifyCourseRequestSupport(String requestEmail, String serverName, String request, String termTitle, int requestListSize, String requestSectionInfo,
			String officialAccountName, String siteTitle, String siteId, String additionalInfo, boolean requireAuthorizer, String authorizerNotified, String authorizerNotNotified)
	{
		ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
			

		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
			
			
		// To Support
		String from = currentUserEmail;
		String to = requestEmail;
		String headerTo = requestEmail;
		String replyTo = currentUserEmail;
		
		StringBuffer buf = new StringBuffer();
		buf.append(rb.getString("java.to") + "\t\t" + serverName
				+ " " + rb.getString("java.supp") + "\n");
		buf.append("\n" + rb.getString("java.from") + "\t"
				+ currentUserDisplayName + "\n");
		if ("new".equals(request)) {
			buf.append(rb.getString("java.subj") + "\t"
					+ rb.getString("java.sitereq") + "\n");
		} else {
			buf.append(rb.getString("java.subj") + "\t"
					+ rb.getString("java.sitechreq") + "\n");
		}
        SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        String dateDisplay = dform.format(new Date());
        
		buf.append(rb.getString("java.date") + "\t" + dateDisplay + "\n\n");
		if ("new".equals(request)) {
			buf.append(rb.getString("java.approval") + " "
					+ serverName + " "
					+ rb.getString("java.coursesite") + " ");
		} else {
			buf.append(rb.getString("java.approval2") + " "
					+ serverName + " "
					+ rb.getString("java.coursesite") + " ");
		}
		if (!termTitle.isEmpty()) {
			buf.append(termTitle);
		}
		if (requestListSize > 1) {
			buf.append(" " + rb.getString("java.forthese") + " "
					+ requestListSize + " " + rb.getString("java.sections")
					+ "\n\n");
		} else {
			buf.append(" " + rb.getString("java.forthis") + "\n\n");
		}
		// the course section information
		buf.append(requestSectionInfo);
		
		buf.append(rb.getString("java.name") + "\t" + currentUserDisplayName
				+ " (" + officialAccountName + " " + currentUserDisplayId
				+ ")\n");
		buf.append(rb.getString("java.email") + "\t" + replyTo + "\n");
		buf.append(rb.getString("java.sitetitle") + "\t" + siteTitle + "\n");
		buf.append(rb.getString("java.siteid") + "\t" + siteId + "\n\n");
		buf.append(rb.getString("java.siteinstr") + "\n" + additionalInfo
				+ "\n\n");

		if (requireAuthorizer)
		{
			// if authorizer is required
			if (!authorizerNotified.isEmpty()) {
				buf.append(rb.getString("java.authoriz") + " " + authorizerNotified + " " + rb.getString("java.asreq"));
			} 
			if (!authorizerNotNotified.isEmpty()){
				buf.append(rb.getString("java.thesiteemail") + " " + authorizerNotNotified + " " + rb.getString("java.asreq"));
			}
		}
		String content = buf.toString();
		
		// message subject
		String message_subject = rb.getString("java.sitereqfrom") + " " + currentUserDisplayName + " " + rb.getString("java.for") + " " + termTitle;
		
		try
		{
			emailService.send(from, to, message_subject, content, headerTo, replyTo, null);
			return content;
		}
		catch (Exception e)
		{
			log.warn(this + " problem in send site request email to support for " + currentUserDisplayName );
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyCourseRequestRequester(String requestEmail, String supportEmailContent, String termTitle)
	{
		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserId = currentUser!=null?currentUser.getId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
		

		ResourceLoader rb = new ResourceLoader(currentUserId, "UserNotificationProvider");
		
		String from = requestEmail;
		String to = currentUserEmail;
		String headerTo = to;
		String replyTo = to;
		// message subject
		String message_subject = rb.getString("java.sitereqfrom") + " " + currentUserDisplayName + " " + rb.getString("java.for") + " " + termTitle;
		
		StringBuffer buf = new StringBuffer();
		buf.append(rb.getString("java.isbeing") + " ");
		buf.append(rb.getString("java.meantime") + "\n\n");
		buf.append(rb.getString("java.copy") + "\n\n");
		buf.append(supportEmailContent);
		buf.append("\n" + rb.getString("java.wish") + " " + requestEmail);
		emailService.send(from, to, message_subject, buf.toString(), headerTo, replyTo, null);
	}
	/*
	 *  Private methods
	 */
	private String getCurrentUserEmailAddress() {
		User currentUser = userDirectoryService.getCurrentUser();
		String email = currentUser != null ? currentUser.getEmail():null;
		if (email == null || email.length() == 0) {
			email = getSetupRequestEmailAddress();
		}
		return email;
	}
	
	
	private String getSetupRequestEmailAddress() {
		String from = serverConfigurationService.getString("setup.request",
				null);
		if (from == null) {
			from = "postmaster@".concat(serverConfigurationService
					.getServerName());
			log.warn(this + " - no 'setup.request' in configuration, using: "+ from);
		}
		return from;
	}
	
	public void notifySiteImportCompleted(String toEmail, Locale locale, String siteId, String siteTitle){
		if(toEmail != null && !"".equals(toEmail)){
			String headerTo = toEmail;
			String replyTo = toEmail;
			String link = developerHelperService.getLocationReferenceURL(SITE_REF_PREFIX + siteId);
			ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
			String message_subject = rb.getFormattedMessage("java.siteImport.confirmation.subject", new Object[]{siteTitle});
			String message_body = rb.getFormattedMessage("java.siteImport.confirmation", new Object[]{siteTitle, link});
			emailService.send(getSetupRequestEmailAddress(), toEmail, message_subject, message_body, headerTo, replyTo, null);
		}
	}
}
