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
package org.sakaiproject.sitemanage.api;

import java.util.List;
import java.util.Locale;

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
	 * Entity prefix for sites
	 */
	public static final String SITE_REF_PREFIX	= "/site/";

	/**
	 * Send an email to newly added user informing them of their password.
	 * 
	 * @param newUser The newly created user.
	 * @param newUserPassword The password for the newly created user.
	 * @param site The site in which the new user was created.
	 */
	public void notifyNewUserEmail(User newUser, String newUserPassword, Site site);

	/**
	 * Send email notification to added participant indicating they have been added to a site.
	 * 
	 * @param nonOfficialAccount <code>true</code> if the added user is a guest user rather than an official one.
	 * @param user The user who was newly added to the site.
	 * @param site The site to which the user was added as a participant.
	 */
	public void notifyAddedParticipant(boolean nonOfficialAccount, User user, Site site); 
	
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
	
	/**
	 * Notifies user when the course site import completed
	 * @param toEmail
	 * @param locale
	 * @param siteId
	 * @param siteTitle
	 */
	public void notifySiteImportCompleted(String toEmail, Locale locale, String siteId, String siteTitle);
}
