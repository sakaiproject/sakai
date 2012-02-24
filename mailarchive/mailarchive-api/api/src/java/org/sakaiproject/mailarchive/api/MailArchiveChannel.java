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

import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * MailArchiveChannel is the extension to the MessageChannel interface for a Sakai Mail Archive service. Messages in the MailArchiveChannel are MailArchiveMessages with MailArchiveMessageHeaders.
 * </p>
 * <p>
 * Security on the channel include:
 * <ul>
 * <li>mailarchive.channel.read</li>
 * <li>mailarchive.channel.remove.any</li>
 * <li>mailarchive.channel.remove.own</li>
 * <li>mailarchive.channel.post</li>
 * </ul>
 * Security Roles for the channel include:
 * <ul>
 * <li>mailarchive.member: read, remove.own, post</li>
 * <li>mailarchive.administrator: mailarchive.member, remove.any</li>
 * </ul>
 * </p>
 * <p>
 * Usage Events generated:
 * <ul>
 * <li>mailarchive.channel.read - mailarchive message resource id</li>
 * <li>mailarchive.channel.remove.any - mailarchive message resource id</li>
 * <li>mailarchive.channel.remove.own - mailarchive message resource id</li>
 * <li>mailarchive.channel.post - mailarchive message resource id</li>
 * </p>
 */
public interface MailArchiveChannel extends MessageChannel
{
	/**
	 * A (MailArchiveMessage) cover for getMessage to return a specific mail archive group message, as specified by message id.
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the MailArchiveMessage that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to read the message.
	 */
	public MailArchiveMessage getMailArchiveMessage(String messageId) throws IdUnusedException, PermissionException;

	/**
	 * a (MailArchiveMessage) cover for addMessage to add a new message to this channel.
	 * 
	 * @param subject
	 *        The message header subject.
	 * @param fromAddress
	 *        The mail from: address from the message.
	 * @param dateSent
	 *        The date: sent from the message.
	 * @param mailHeaders
	 *        The full set of mail headers from the message.
	 * @param attachments
	 *        The message header attachments, a vector of Reference objects.
	 * @param body
	 *        The message body.- body[0] is plain/text; body[1] is html/text
	 * @return The newly added message.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 */
	public MailArchiveMessage addMailArchiveMessage(String subject, String fromAddress, Time dateSent, List mailHeaders,
			List attachments, String[] body) throws PermissionException;

	/** @return true if the channel enabled, false if not. */
	public boolean getEnabled();

	/** @return true if the channel is open to messages from outside the membership, false if not. */
	public boolean getOpen();
	
	/** @return <code>true</code> if the channel sets the reply to address back to the channel */
	public boolean getReplyToList();
		
	/** @return <code>true</code> if the channel sets the value to send only to the archive */
	public boolean getSendToList();
	/**
	 * check permissions for addMessage() for the given user.
	 * 
	 * @param user
	 *        The user.
	 * @return true if the specified user is allowed to addMessage(...), false if not.
	 */
	public boolean allowAddMessage(User user);
}
