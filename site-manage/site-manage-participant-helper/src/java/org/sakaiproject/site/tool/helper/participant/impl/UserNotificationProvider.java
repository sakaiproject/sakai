package org.sakaiproject.site.tool.helper.participant.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import uk.org.ponder.messageutil.MessageLocator;


public class UserNotificationProvider {
	
	private  Log M_log = LogFactory.getLog(UserNotificationProvider.class);	
	
	private  EmailService emailService;
	
	private  ServerConfigurationService serverConfigurationService;
	
	private  UserDirectoryService userDirectoryService;

	public  MessageLocator messageLocator;
	
	public void init() {
		//nothing realy to do
		M_log.info("UserNotificationProvider:init()");
	}
	
	public  void notifyAddedParticipant(boolean newNonOfficialAccount,
			User user, String siteTitle) {
		
		String from = getSetupRequestEmailAddress();
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
			String message_subject = productionSiteName + " "
					+ getMessage("java.sitenoti");
			String content = "";
			StringBuilder buf = new StringBuilder();
			buf.setLength(0);

			// email bnonOfficialAccounteen newly added nonOfficialAccount account
			// and other users
			buf.append(user.getDisplayName() + ":\n\n");
			buf.append(getMessage("java.following") + " "
					+ productionSiteName + " "
					+ getMessage("java.simplesite") + "\n");
			buf.append(siteTitle + "\n");
			buf.append(getMessage("java.simpleby") + " ");
			buf.append(userDirectoryService.getCurrentUser().getDisplayName()
					+ ". \n\n");
			if (newNonOfficialAccount) {
				buf.append(serverConfigurationService.getString(
						"nonOfficialAccountInstru", "")
						+ "\n");

				if (nonOfficialAccountUrl != null) {
					buf.append(getMessage("java.togeta1") + "\n"
							+ nonOfficialAccountUrl + "\n");
					buf.append(getMessage("java.togeta2") + "\n\n");
				}
				buf.append(getMessage("java.once") + " " + productionSiteName
						+ ": \n");
				buf.append(getMessage("java.loginhow1") + " "
						+ productionSiteName + ": " + productionSiteUrl + "\n");
				buf.append(getMessage("java.loginhow2") + "\n");
				buf.append(getMessage("java.loginhow3") + "\n");
			} else {
				buf.append(getMessage("java.tolog") + "\n");
				buf.append(getMessage("java.loginhow1") + " "
						+ productionSiteName + ": " + productionSiteUrl + "\n");
				buf.append(getMessage("java.loginhow2") + "\n");
				buf.append(getMessage("java.loginhow3u") + "\n");
			}
			buf.append(getMessage("java.tabscreen"));
			content = buf.toString();
			emailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);

		} // if

	}

	public  void notifyNewUserEmail(User user, String newUserPassword,
			String siteTitle) {
		
		String from = getSetupRequestEmailAddress();
		String productionSiteName = serverConfigurationService.getString(
				"ui.service", "");
		String productionSiteUrl = serverConfigurationService.getPortalUrl();
		
		String newUserEmail = user.getEmail();
		String to = newUserEmail;
		String headerTo = newUserEmail;
		String replyTo = newUserEmail;
		String message_subject = productionSiteName + " "
				+ getMessage("java.newusernoti");
		String content = "";

		if (from != null && newUserEmail != null) {
			StringBuilder buf = new StringBuilder();
			buf.setLength(0);

			// email body
			buf.append(user.getDisplayName() + ":\n\n");

			buf.append(getMessage("java.addedto") + " " + productionSiteName
					+ " (" + productionSiteUrl + ") ");
			buf.append(getMessage("java.simpleby") + " ");
			buf.append(userDirectoryService.getCurrentUser().getDisplayName()
					+ ". \n\n");
			buf.append(getMessage("java.passwordis1") + "\n"
					+ newUserPassword + "\n\n");
			buf.append(getMessage("java.passwordis2") + "\n\n");

			content = buf.toString();
			emailService.send(from, to, message_subject, content, headerTo,
					replyTo, null);
		}
	}

	/*
	 *  Private methods
	 */
	
	private  String getSetupRequestEmailAddress() {
		String from = serverConfigurationService.getString("setup.request",
				null);
		if (from == null) {
			M_log.warn("UserNotificationProvider:getSetupRequestEmailAddress - no 'setup.request' in configuration");
			from = "postmaster@".concat(serverConfigurationService
					.getServerName());
		}
		return from;
	}
	
	/**
	 * return string based on locale bundle file
	 * @param id
	 * @return
	 */
	private  String getMessage(String id)
	{
		return messageLocator.getMessage(id);
	}

	public  void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public  void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public  void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public  void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
}
