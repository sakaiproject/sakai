/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.*;
import org.sakaiproject.time.api.Time;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * GenericMessageService is the base interface for the different specific Sakai communications service such as Chat, Announcements, etc.
 * </p>
 * <p>
 * The service manages a set of message channels, each containing a set of messages.
 * </p>
 * <p>
 * Channels and Messages can be worked with through the MessageChannel and Message APIs and their extensions.
 * </p>
 */
public interface MessageService extends EntityProducer, EntitySummary
{
	/** Security function / event for reading channel / message. */
	public static final String SECURE_READ = "read";

	/** Security function / event for adding channel / message. */
	public static final String SECURE_ADD = "new";

	/** Event for adding channel / message. */
	public static final String SECURE_CREATE = "create";

	/** Security function / event for removing one's own message. */
	public static final String SECURE_REMOVE_OWN = "delete.own";

	/** Security function / event for removing anyone's message or channel. */
	public static final String SECURE_REMOVE_ANY = "delete.any";

	/** Security function / event for updating one's own message or the channel. */
	public static final String SECURE_UPDATE_OWN = "revise.own";

	/** Security function / event for updating any message. */
	public static final String SECURE_UPDATE_ANY = "revise.any";

	/** Security function / event for accessing someone elses draft. */
	public static final String SECURE_READ_DRAFT = "read.drafts";

	/** Security function giving the user permission to all groups, if granted to at the channel or site level. */
	public static final String SECURE_ALL_GROUPS = "all.groups";

	/** The Reference type for a channel. */
	public static final String REF_TYPE_CHANNEL = "channel";

	/** The Reference type for a messgae. */
	public static final String REF_TYPE_MESSAGE = "msg";

	/**
	 * Return a list of all the defined channels.
	 * 
	 * @return a list of MessageChannel (or extension) objects (may be empty).
	 * @deprecated since 8 April 2014 (Sakai 10), this is not useful (why would you want all channels in the system) and would perform very badly, use getChannelIds(String context) OR getChannel(String ref) instead
	 */
	public List<MessageChannel> getChannels();

	/**
	 * check permissions for getChannel().
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to getChannel(channelId), false if not.
	 */
	public boolean allowGetChannel(String ref);

	/**
	 * Return a specific channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the MessageChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for any channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public MessageChannel getChannel(String ref) throws IdUnusedException, PermissionException;

	/**
	 * Find the channel, in cache or info store - cache it if newly found.
	 * 
	 * Warning: No check is made on channel permissions -- caller should filter for public messages
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The channel, if found.
	 */
	public MessageChannel getChannelPublic(String ref);
	
	/**
	 * check permissions for addChannel().
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to addChannel(channelId), false if not.
	 */
	public boolean allowAddChannel(String ref);

	/**
	 * Add a new channel. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel, locked for update.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public MessageChannelEdit addChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException;

	/**
	 * check permissions for editChannel()
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to update the channel, false if not.
	 */
	public boolean allowEditChannel(String ref);

	/**
	 * Return a specific channel, as specified by channel id, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the Channel that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to edit the channel.
	 * @exception InUseException
	 *            if the channel is locked for edit by someone else.
	 */
	public MessageChannelEdit editChannel(String ref) throws IdUnusedException, PermissionException, InUseException;

	/**
	 * Commit the changes made to a MessageChannelEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The MessageChannelEdit object to commit.
	 */
	public void commitChannel(MessageChannelEdit edit);

	/**
	 * Cancel the changes made to a MessageChannelEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The MessageChannelEdit object to cancel.
	 */
	public void cancelChannel(MessageChannelEdit edit);

	/**
	 * Check permissions for removeChannel().
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to removeChannel(), false if not.
	 */
	public boolean allowRemoveChannel(String ref);

	/**
	 * Remove a channel - it must be locked from editChannel().
	 * 
	 * @param channel
	 *        The channel to remove.
	 * @exception PermissionException
	 *            if the user does not have permission to remove a channel.
	 */
	public void removeChannel(MessageChannelEdit channel) throws PermissionException;

	/**
	 * Access the internal reference which can be used to access the channel from within the system.
	 * 
	 * @param context
	 *        The context.
	 * @param id
	 *        The channel id.
	 * @return The the internal reference which can be used to access the channel from within the system.
	 */
	public String channelReference(String context, String id);

	/**
	 * Access the internal reference which can be used to access the message from within the system.
	 * 
	 * @param context
	 *        The context.
	 * @param channelId
	 *        The channel id.
	 * @param id
	 *        The message id.
	 * @return The the internal reference which can be used to access the message from within the system.
	 */
	public String messageReference(String context, String channelId, String id);

	/**
	 * Access the internal reference which can be used to access the message from within the system.
	 * 
	 * @param channelRef
	 *        The channel reference.
	 * @param id
	 *        The message id.
	 * @return The the internal reference which can be used to access the message from within the system.
	 */
	public String messageReference(String channelRef, String id);

	/**
	 * Get a message, given a reference. This call avoids the need to have channel security, as long as the user has permissions to the message.
	 * 
	 * @param ref
	 *        The message reference
	 * @return The message.
	 * @exception IdUnusedException
	 *            If this reference does not identify a message.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the message.
	 */
	public Message getMessage(Reference ref) throws IdUnusedException, PermissionException;

	/**
	 * Cancel the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The MessageEdit object to cancel.
	 */
	public void cancelMessage(MessageEdit edit);

	/**
	 * Access a list of messages in the channel, that are after the date, limited to just the n latest messages, ordered as specified, including drafts if specified. Channel read permission is required, unless pubViewOnly is selected - draft read on the
	 * channel is required to see drafts.
	 * 
	 * @param afterDate
	 *        if null, no date limit, else limited to only messages after this date.
	 * @param limitedToLatest
	 *        if 0, no count limit, else limited to only the latest this number of messages.
	 * @param ascending
	 *        if true, sort oldest first, else sort latest first.
	 * @param includeDrafts
	 *        if true, include drafts (if the user has draft permission), else leave them out.
	 * @param pubViewOnly
	 *        if true, include only messages marked pubview, else include any.
	 * @return A list of Message objects that meet the criteria; may be empty
	 * @exception PermissionException
	 *            If the current user does not have channel read permission.
	 */
	public List<Message> getMessages(String channelRef, Time afterDate, int limitedToLatest, boolean ascending, boolean includeDrafts,
			boolean pubViewOnly) throws PermissionException;

	/**
	 * Access a list of channel ids that are defined related to the context.
	 * 
	 * @param context
	 *        The context in which to search
	 * @return A List (String) of channel id for channels withing the context.
	 */
	public List<String> getChannelIds(String context);

        /**
         * Get a summary of an Announcement Channel
         * 
         * @param ref
         *        The channel reference.
         * @param items
         *        Maximum number of items to return
         * @param days
         *        Maximum number of days to peer back
         * @return The Map containing the Summary
         * @exception IdUsedException
         *            if the id is not unique.
         * @exception IdInvalidException
         *            if the id is not made up of valid characters.
         * @exception PermissionException
         *            if the user does not have permission to add a channel.
         */
        public Map getSummary(String ref, int items, int days) throws IdUsedException, IdInvalidException, PermissionException;

} // MessageService

