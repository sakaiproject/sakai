/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.mailarchive.api;

import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.MessageService;

/**
 * <p>
 * MailArchiveService is the extension to MessageService configured for the Mail Archive.
 * </p>
 * <p>
 * Channels are MailArchiveChannels, and Messages are MailArchiveMessages with MailArchiveMessageHeaders.
 * </p>
 * <p>
 * Security in the mail archive service, in addition to that defined in the channels, include:
 * <ul>
 * <li>mailarchive.channel.add</li>
 * </ul>
 * </p>
 * <li>mailarchive.channel.remove</li>
 * </ul>
 * </p>
 * <p>
 * Usage Events are generated:
 * <ul>
 * <li>mailarchive.channel.add - mail archive channel resource id</li>
 * <li>mailarchive.channel.remove - mail archive channel resource id</li>
 * </ul>
 * </p>
 */
public interface MailArchiveService extends MessageService
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:mailarchive";

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = "/mailarchive";

	/** Property for a mail channel indicating if the channel has replys going back to the list (user settable) [Boolean] */
	static final String PROP_MAIL_CHANNEL_REPLY_LIST = "CHEF:mail-channel-reply-list";

	/** Property for a mail channel indicating if the channel only archives (user settable) [Boolean] */
	static final String PROP_MAIL_CHANNEL_SENDTO_LIST = "CHEF:mail-channel-sendto-list";
	
	/** Property for a message channel indicating if the channel is 'enabled' (user settable) [Boolean] */
	static final String PROP_MAIL_CHANNEL_OPEN = "CHEF:mail-channel-open";
	
	/** Security lock / event root for generic message events to make it a mail event. */
	public static final String SECURE_MAIL_ROOT = "mail.";

	/** Security lock / event for reading channel / message. */
	public static final String SECURE_MAIL_READ = SECURE_MAIL_ROOT + SECURE_READ;

	/** Security lock / event for adding channel / message. */
	public static final String SECURE_MAIL_ADD = SECURE_MAIL_ROOT + SECURE_ADD;

	/** Security lock / event for removing one's own message. */
	public static final String SECURE_MAIL_REMOVE_OWN = SECURE_MAIL_ROOT + SECURE_REMOVE_OWN;

	/** Security lock / event for removing anyone's message or channel. */
	public static final String SECURE_MAIL_REMOVE_ANY = SECURE_MAIL_ROOT + SECURE_REMOVE_ANY;

	/** Security lock / event for updating one's own message or the channel. */
	public static final String SECURE_MAIL_UPDATE_OWN = SECURE_MAIL_ROOT + SECURE_UPDATE_OWN;

	/** Security lock / event for updating any message. */
	public static final String SECURE_MAIL_UPDATE_ANY = SECURE_MAIL_ROOT + SECURE_UPDATE_ANY;

	/** Security lock / event for accessing someone elses draft. */
	public static final String SECURE_MAIL_READ_DRAFT = SECURE_MAIL_ROOT + SECURE_READ_DRAFT;

	/** Message header that indicates the original outer-envelope Content-Type of an archived message */
	public static final String HEADER_OUTER_CONTENT_TYPE = "X-Content-Type-Outer-Envelope";

	/**
	 * Message header that indicates the Content-Type of the message body of an archived message - this may be different from the original outer-envelope Content-Type (outer might be multipart)
	 */
	public static final String HEADER_INNER_CONTENT_TYPE = "X-Content-Type-Message-Body";
	
	// Common mail headers -- always use a case-insensitive test against these
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_RETURN_PATH  = "Return-Path";
	public static final String HEADER_SUBJECT = "Subject";
	public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

	/**
	 * A (MailArchiveChannel) cover for getChannel() to return a specific mail archive group.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the MailArchiveChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a mail archive channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public MailArchiveChannel getMailArchiveChannel(String ref) throws IdUnusedException, PermissionException;

	/**
	 * A (MailArchiveChannel) cover for add() to add a new announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created group.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a group.
	 */
	public MailArchiveChannelEdit addMailArchiveChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException;
}
