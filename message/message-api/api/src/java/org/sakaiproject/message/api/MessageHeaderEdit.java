/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.message.api;

import org.sakaiproject.entity.api.AttachmentContainerEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
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
	 * Add a Group to the list of groups for this message.
	 * 
	 * @param group
	 *        The Group to add to those for this message.
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void addGroup(Group group) throws PermissionException;

	/**
	 * Remove this Group from the list of groups for this message.
	 * 
	 * @param group
	 *        The Group to remove from those for this message.
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void removeGroup(Group group) throws PermissionException;

	/**
	 * Set the access mode for the message - how we compute who has access to the message.
	 * 
	 * @param access
	 *        The MessageAccess access mode for the message.
	 */
	void setAccess(MessageAccess access);
}
