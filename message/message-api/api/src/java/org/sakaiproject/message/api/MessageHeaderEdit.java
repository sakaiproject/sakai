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

package org.sakaiproject.message.api;

import java.util.Collection;

import org.sakaiproject.entity.api.AttachmentContainerEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * MessageHeader is the base Interface for a Sakai Message headers. Header fields common to all message service message headers are defined here.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public interface MessageHeaderEdit extends MessageHeader, AttachmentContainerEdit
{
	/**
	 * Set the date/time the message was sent to the channel.
	 * 
	 * @param date
	 *        The date/time the message was sent to the channel.
	 */
	void setDate(Time date);
	
	/**
	 * Set the message order the message was sent to the channel.
	 * 
	 * @param message order
	 *        The message order the message was sent to the channel.
	 */
	void setMessage_order(Integer message_order);

	/**
	 * Set the User who sent the message to the channel.
	 * 
	 * @param user
	 *        The User who sent the message to the channel.
	 */
	void setFrom(User user);

	/**
	 * Set the draft status of the message.
	 * 
	 * @param draft
	 *        True if the message is a draft, false if not.
	 */
	void setDraft(boolean draft);

	/**
	 * Set these as the message's groups, replacing the access and groups already defined.
	 * 
	 * @param Collection
	 *        groups The colelction of Group objects to use for this message.
	 * @throws PermissionException
	 *         if the end user does not have permission to remove from the groups that would be removed or add to the groups that would be added.
	 */
	void setGroupAccess(Collection groups) throws PermissionException;

	/**
	 * Remove any grouping for this message; the access mode reverts to channel and any groups are removed.
	 * 
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void clearGroupAccess() throws PermissionException;
}
