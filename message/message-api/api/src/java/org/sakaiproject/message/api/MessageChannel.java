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

// import
import java.util.Collection;
import java.util.List;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.w3c.dom.Element;

/**
 * <p>
 * MessageChannel is the base interface for Sakai communications service message channels. Extensions to this interface configure types of communications channels (Chat, Announcements, etc.)
 * </p>
 * <p>
 * Channels contains collections of messages, each Message (or extension) object. Each chat channel has a unique channel id (read only), and is a Sakai Resource.
 * </p>
 * <p>
 * The chat channel can be asked:
 * <ul>
 * <li>for an iterator on the messages, with a filter</li>
 * <li>to find a specific message</li>
 * <li>to add a new message</li>
 * <li>to update an existing message</li>
 * <li>to remove an existing message</li>
 * </ul>
 * </p>
 * <p>
 * The chat channel can be subscribed to providing notification when:
 * <ul>
 * <li>a new message has been posted</li>
 * <li>a message has been changed</li>
 * <li>a message has been removed</li>
 * <li>the channel has been removed</li>
 * </ul>
 * </p>
 * <p>
 * Security on the channel include:
 * <ul>
 * <li>message.channel.read</li>
 * <li>message.channel.remove.any</li>
 * <li>message.channel.remove.own</li>
 * <li>message.channel.post</li>
 * </ul>
 * Security Roles for the channel include:
 * <ul>
 * <li>message.member: read, remove.own, post</li>
 * <li>message.administrator: chat.member, remove.any</li>
 * </ul>
 * </p>
 * <p>
 * Event handling is defined in the specific extension classes.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public interface MessageChannel<T extends Message> extends Entity
{
	/**
	 * Access the context of the resource.
	 * 
	 * @return The context.
	 */
	String getContext();

	/**
	 * check permissions for getMessages() or getMessage().
	 * 
	 * @return true if the user is allowed to get messages from this channel, false if not.
	 */
	boolean allowGetMessages();

	/**
	 * Get the number of messages in this particular channel.
	 *
	 * @return The count.
	 */

	int getCount() throws PermissionException;

        /**
	 * Get the number of messages in this particular channel if the filter
	 * were applied.
	 *
	 * @param filter
	 *      A filtering object to accept messages, or null if no filtering is desired.
	 *
	 * @return The count.
	 */
	int getCount(Filter filter) throws PermissionException;

	/**
	 * Return a list of all or filtered messages in the channel. The order in which the messages will be found in the iteration is by date, oldest first if ascending is true, newest first if ascending is false.
	 * 
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @param ascending
	 *        Order of messages, ascending if true, descending if false
	 * @return a list of channel Message objects or specializations of Message objects (may be empty).
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 */
	List<T> getMessages(Filter filter, boolean ascending) throws PermissionException;

        /**
	 * Return a list of all or filtered messages in the channel. The order in which the 
	 * messages will be found in the iteration is by date, oldest first if 
	 * ascending is true, newest first if ascending is false.  See getMessagesSearch()
	 * for detail on the possible differences between search and filter retrievals.
	 * 
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @param ascending
	 *        Order of messages, ascending if true, descending if false
	 * @param pages
	 *        An indication of the range of messages we are looking for
	 * @return a list of channel Message objects or specializations of Message objects (may be empty).
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 */
	List<T> getMessages(Filter filter, boolean ascending, PagingPosition pages) throws PermissionException;

	/**
	 * Return a list of all public messages in the channel. 
	 * The order in which the messages will be found in the iteration is by date, 
	 * oldest first if ascending is true, newest first if ascending is false.
	 * 
	 * @param filter
	 *        Optional additional filtering object to accept messages, or null
	 * @param ascending
	 *        Order of messages, ascending if true, descending if false
	 * @return a list of channel Message objects or specializations of Message objects (may be empty).
	 */
	List<T> getMessagesPublic(Filter filter, boolean ascending);

	/**
	 * Return a specific channel message, as specified by message name.
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the Message that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to read the message.
	 */
	Message getMessage(String messageId) throws IdUnusedException, PermissionException;

	/**
	 * check permissions for editMessage()
	 * 
	 * @param id
	 *        The message id.
	 * @return true if the user is allowed to update the message, false if not.
	 */
	boolean allowEditMessage(String messageId);

	/**
	 * Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the Message that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to edit the message.
	 * @exception InUseException
	 *            if the message is locked for edit by someone else.
	 */
	MessageEdit editMessage(String messageId) throws IdUnusedException, PermissionException, InUseException;
	
	/**
	 * Commit the changes made to a MessageEdit object for announcement reorder, and release the lock. The MessageEdit is disabled, and not to be used after this call. If the message is in a form that the user has no permission to store, a PermissionException is thrown, and the
	 * edit is canceled.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 */
	void commitMessage_order(MessageEdit edit);

	/**
	 * Commit the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call. If the message is in a form that the user has no permission to store, a PermissionException is thrown, and the
	 * edit is canceled.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 */
	void commitMessage(MessageEdit edit);

	/**
	 * Commit the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call. If the message is in a form that the user has no permission to store, a PermissionException is thrown, and the
	 * edit is canceled.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 * @param priority
	 *        The notification priority for this commit.
	 */
	void commitMessage(MessageEdit edit, int priority);

	/**
	 * Commit the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call. If the message is in a form that the user has no permission to store, a PermissionException is thrown, and the
	 * edit is canceled. Used when a scheduled notification is made for this message.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 * @param priority
	 *        The notification priority for this commit.
	 * @param invokee
	 * 		  The id for the object to be called when the scheduled notification fires.
	 */
	void commitMessage(MessageEdit edit, int priority, String invokee);

	/**
	 * Cancel the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 */
	void cancelMessage(MessageEdit edit);

	/**
	 * check permissions for addMessage().
	 * 
	 * @return true if the user is allowed to addMessage(...), false if not.
	 */
	boolean allowAddMessage();
	
	/**
	 * check permission for adding draft message and modifying it afterwards.
	 */
	boolean allowAddDraftMessage();

	/**
	 * Check if the user has permission to add a channel-wide (not grouped) message.
	 * 
	 * @return true if the user has permission to add a channel-wide (not grouped) message.
	 */
	boolean allowAddChannelMessage();

	/**
	 * Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @return The newly added message, locked for update.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 */
	MessageEdit addMessage() throws PermissionException;

	/**
	 * check permissions for removeMessage().
	 * 
	 * @param message
	 *        The message from this channel to remove.
	 * @return true if the user is allowed to removeMessage(...), false if not.
	 */
	boolean allowRemoveMessage(Message message);

	/**
	 * Remove a message from the channel based on message id
	 * 
	 * @param messageId
	 *        The messageId for the message of the channel to remove.
	 * @exception PermissionException
	 *            if the user does not have permission to remove the message.
	 */
	void removeMessage(String messageId) throws PermissionException;

	/**
	 * Remove a message from the channel - it must be locked from editMessage().
	 * 
	 * @param message
	 *        The message from this channel to remove.
	 * @exception PermissionException
	 *            if the user does not have permission to remove the message.
	 */
	void removeMessage(MessageEdit message) throws PermissionException;

	/**
	 * Merge in a new message as defined in the xml. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param el
	 *        The message information in XML in a DOM element.
	 * @return The newly added message, locked for update.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 * @exception IdUsedException
	 *            if the user id is already used.
	 */
	MessageEdit mergeMessage(Element el) throws PermissionException, IdUsedException;

	/**
	 * Get the collection of Groups defined for the context of this channel that the end user has add message permissions in.
	 * 
	 * @return The Collection (Group) of groups defined for the context of this channel that the end user has add message permissions in, empty if none.
	 */
	Collection<Group> getGroupsAllowAddMessage();

	/**
	 * Get the collection of Group defined for the context of this channel that the end user has get message permissions in.
	 * 
	 * @return The Collection (Group) of groups defined for the context of this channel that the end user has get message permissions in, empty if none.
	 */
	Collection<Group> getGroupsAllowGetMessage();

	/**
	 * Get the collection of Group defined for the context of this channel that the end user has remove message permissions in.
	 * 
	 * @param own
	 *        true if the message is the user's own, false if it is someone else's.
	 * @return The Collection (Group) of groups defined for the context of this channel that the end user has get message permissions in, empty if none.
	 */
	Collection<Group> getGroupsAllowRemoveMessage(boolean own);
}
