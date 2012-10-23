package org.sakaiproject.sitemanage.api;

import java.util.List;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;

public interface UserNotificationProvider {

	/**
	 * Configuration parameter for sakai.properties.
	 * Should the added participant emails come from the current user.
	 * The new account emails shouldn't use the current user as we don't want bounces
	 * containing passwords going back to the user who added it.
	 */
	public static final String NOTIFY_FROM_CURRENT_USER = "sitemanage.notifyFromCurrentUser";

	/**
	 * Send an email to newly added user informing password
	 * 
	 * @param newnonOfficialAccount
	 * @param emailId
	 * @param userName
	 * @param siteTitle
	 */
	public void notifyNewUserEmail(User user, String newUserPassword, String siteTitle);

	/**
	 * send email notification to added participant
	 */
	public void notifyAddedParticipant(boolean newNonOfficialAccount, User user, String siteTitle); 
	
	/**
	 * send email notification to template contact people about template usage
	 * @param templateSiteId
	 * @param templateUser
	 * @param templateSiteTitle
	 * @param targetSite
	 */
	public void notifyTemplateUse(Site templateSite, User templateUser,	Site targetSite);
	
	/**
	 * notification for site creation
	 * @param site
	 * @param notifySites
	 * @param courseSite
	 * @param termTitle
	 * @param requestEmail
	 */
	public void notifySiteCreation(Site site, List notifySites, boolean courseSite, String termTitle, String requestEmail);
	
	/**
	 * send course site request information to course authorizer
	 * return true if such email sent successfully; false otherwise
	 * @param instructorId
	 * @param requestEmail
	 * @param replyToEmail
	 * @param termTitle
	 * @param requestSectionInfo
	 * @param siteTitle
	 * @param siteId
	 * @param additionalInfo
	 * @param serverName
	 * @return
	 */
	public boolean notifyCourseRequestAuthorizer(String instructorId, String requestEmail, String replyToEmail, String termTitle, String requestSectionInfo, String siteTitle, String siteId, String additionalInfo, String serverName);
	
	/**
	 * notify support team about course creation
	 * @param requestEmail
	 * @param serverName
	 * @param request
	 * @param termTitle
	 * @param requestListSize
	 * @param requestSectionInfo
	 * @param officialAccountName
	 * @param siteTitle
	 * @param siteId
	 * @param additionalInfo
	 * @param requireAuthorizer
	 * @param authorizerNotified
	 * @param authorizerNotNotified
	 * @return
	 */
	public String notifyCourseRequestSupport(String requestEmail, String serverName, String request, String termTitle, int requestListSize, String requestSectionInfo,
			String officialAccountName, String siteTitle, String siteId, String additionalInfo, boolean requireAuthorizer, String authorizerNotified, String authorizerNotNotified);

	/**
	 * notify course site requester
	 * @param requestEmail
	 * @param supportEmailContent
	 * @param termTitle
	 */
	public void notifyCourseRequestRequester(String requestEmail, String supportEmailContent, String termTitle);
}
