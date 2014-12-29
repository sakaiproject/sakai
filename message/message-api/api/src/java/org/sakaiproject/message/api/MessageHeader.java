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
import java.util.Stack;

import org.sakaiproject.entity.api.AttachmentContainer;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * MessageHeader is the base Interface for a Sakai Message headers. Header fields common to all message service message headers are defined here.
 * </p>
 */
public interface MessageHeader extends AttachmentContainer
{
	/**
	 * <p>
	 * MessageAccess enumerates different access modes for the message: channel-wide or grouped.
	 * </p>
	 */
	public class MessageAccess
	{
		private final String m_id;

		private MessageAccess(String id)
		{
			m_id = id;
		}

		public String toString()
		{
			return m_id;
		}

		static public MessageAccess fromString(String access)
		{
			// if (PUBLIC.m_id.equals(access)) return PUBLIC;
			if (CHANNEL.m_id.equals(access)) return CHANNEL;
			if (GROUPED.m_id.equals(access)) return GROUPED;
			return null;
		}

		/** public access to the message: pubview */
		// public static final MessageAccess PUBLIC = new MessageAccess("public");
		/** channel (site) level access to the message */
		public static final MessageAccess CHANNEL = new MessageAccess("channel");

		/** grouped access; only members of the getGroup() groups (authorization groups) have access */
		public static final MessageAccess GROUPED = new MessageAccess("grouped");
	}

	/**
	 * Access the unique (within the channel) message id.
	 * 
	 * @return The unique (within the channel) message id.
	 */
	String getId();

	/**
	 * Access the date/time the message was sent to the channel.
	 * 
	 * @return The date/time the message was sent to the channel.
	 */
	Time getDate();
	
	/**
	 * Access the message order of the message was sent to the channel.
	 * 
	 * @return The message order of the message was sent to the channel.
	 */
	Integer getMessage_order();

	/**
	 * Access the User who sent the message to the channel.
	 * 
	 * @return The User who sent the message to the channel.
	 */
	User getFrom();

	/**
	 * Access the draft status of the message.
	 * 
	 * @return True if the message is a draft, false if not.
	 */
	boolean getDraft();

	/**
	 * Access the groups defined for this message.
	 * 
	 * @return A Collection (String) of group refs (authorization group ids) defined for this message; empty if none are defined.
	 */
	Collection<String> getGroups();

	/**
	 * Access the groups, as Group objects, defined for this message.
	 * 
	 * @return A Collection (Group) of group objects defined for this message; empty if none are defined.
	 */
	Collection<Group> getGroupObjects();

	/**
	 * Access the access mode for the message - how we compute who has access to the message.
	 * 
	 * @return The MessageAccess access mode for the message.
	 */
	MessageAccess getAccess();

	/**
	 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
	Element toXml(Document doc, Stack stack);
}
