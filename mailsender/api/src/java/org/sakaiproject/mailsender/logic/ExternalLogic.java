/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic;

import java.util.List;
import java.util.Map;

import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.mailsender.AttachmentException;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * This is the interface for logic which is external to our app logic
 */
public interface ExternalLogic
{
	String NO_LOCATION = "noLocationAvailable";

	String EVENT_EMAIL_SEND = "msnd.email.send";

	// permissions
	String PERM_ADMIN = "mailtool.admin";
	String PERM_SEND = "mailtool.send";

	/**
	 * @return the current sakai user id (not username)
	 */
	String getCurrentUserId();

	/**
	 * Get details for the current user
	 *
	 * @return
	 */
	User getCurrentUser();

	/**
	 * Get the display name for a user by their unique id
	 *
	 * @param userId
	 *            the current sakai user id (not username)
	 * @return display name (probably firstname lastname) or "----------" (10 hyphens) if none found
	 */
	String getUserDisplayName(String userId);

	/**
	 * Get details for a user
	 *
	 * @param userId
	 * @return
	 */
	User getUser(String userId);

	/**
	 * @param locationId
	 *            a unique id which represents the current location of the user (entity reference)
	 * @return the title for the context or "--------" (8 hyphens) if none found
	 */
	String getCurrentSiteTitle();

	/**
	 * Get the current site's details
	 *
	 * @return
	 */
	Site getCurrentSite();

	/**
	 * Get the site id for the current site
	 *
	 * @return
	 */
	String getSiteID();

	/**
	 * Get the realm id for the current site
	 *
	 * @return
	 */
	String getSiteRealmID();

	/**
	 * Get the type of the current site
	 *
	 * @return
	 */
	String getSiteType();

	/**
	 * Check if this user has site update access
	 *
	 * @param userId
	 *            the internal user id (not username)
	 * @param locationId
	 *            a unique id which represents the current location of the user (entity reference)
	 * @return true if the user has site update access, false otherwise
	 */
	boolean isUserSiteAdmin(String userId, String locationId);

	/**
	 * Check if this user has super admin access
	 *
	 * @param userId
	 *            the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	boolean isUserAdmin(String userId);

	/**
	 * Check if a user has a specified permission within a context, primarily a convenience method
	 * and passthrough
	 *
	 * @param userId
	 *            the internal user id (not username)
	 * @param permission
	 *            a permission string constant
	 * @param locationId
	 *            a unique id which represents the current location of the user (entity reference)
	 * @return true if allowed, false otherwise
	 */
	boolean isUserAllowedInLocation(String userId, String permission, String locationId);

	/**
	 * Check if the email archive tool is added to the current site
	 *
	 * @return true if email archive tool exists, false otherwise
	 */
	boolean isEmailArchiveAddedToSite();

	/**
	 * Send email to a list of users. After validation, if there are any valid email addresses
	 * available, the email is sent to those recipients and the invalid recipients are returned to
	 * the caller.
	 *
	 * @param config
	 * @param fromEmail
	 * @param fromName
	 * @param to
	 *            Map of email address <address, display name>
	 * @param subject
	 * @param content
	 * @param attachments
	 * @return List of email addresses that were found to be invalid.
	 * @throws AttachmentException
	 */
	List<String> sendEmail(ConfigEntry config, String fromEmail, String fromName,
			Map<String, String> to, String subject, String content,
			List<Attachment> attachments) throws MailsenderException, AttachmentException;

	/**
	 * Append email to Email Archive
	 *
	 * @param config The config used by the user, can be <code>null</code>.
	 * @param channelRef The Email Archive channel reference to add the email to.
	 * @param sender The email sender (eg John Smith &lt;john.smith@example.com&gt;)
	 * @param subject The Subject of the email.
	 * @param body The body of the email message.
	 * @param attachments A list of attachments, can be <code>null</code>.
	 * @return true if success
	 */
	boolean addToArchive(ConfigEntry config, String channelRef, String sender, String subject,
			String body, List<Attachment> attachments);

	/**
	 * @return the current location id of the current user
	 */
	String getCurrentLocationId();

	/**
	 * Get a list of the permission keys for the tool
	 *
	 * @return List of the permission keys
	 */
	List<String> getPermissionKeys();
}
