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

import org.sakaiproject.message.api.MessageChannelEdit;

/**
 * <p>
 * MailArchiveChannelEdit is an editable MailArchiveChannel.
 * </p>
 */
public interface MailArchiveChannelEdit extends MailArchiveChannel, MessageChannelEdit
{
	/**
	 * Set the enabled status of the channe. Disabled channels will not recieve email.
	 * 
	 * @param setting
	 *        The new setting.
	 */
	public void setEnabled(boolean setting);

	/**
	 * Set the open status of the channe. Open channels will recieve email from anyone - otherwise messages will be accepted only from users (based on the main from email address) with add permission.
	 * 
	 * @param setting
	 *        The new setting.
	 */
	public void setOpen(boolean setting);
	
	/**
	 * Set the reply status of the channel. Reply to list channels will have the replys go back to the list. 
	 * 
	 * @param setting
	 *        The new setting.
	 */
	public void setReplyToList(boolean replyToList);
	/**
	 * Set the send status of the channel. 
	 * Those that have this set to false will just archive the messages. 
	 * 
	 * @param setting
	 *        The new setting.
	 */
	public void setSendToList(boolean sendToList);
}
