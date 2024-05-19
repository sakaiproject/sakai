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

package org.sakaiproject.announcement.cover;

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;

/**
 * <p>
 * AnnouncementService is a static Cover for the {@link org.sakaiproject.announcement.api.AnnouncementService AnnouncementService}; see that interface for usage details.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 * @deprecated
 */
@Deprecated
public class AnnouncementService
{
	public static final String MOD_DATE = org.sakaiproject.announcement.api.AnnouncementService.MOD_DATE;
	public static final String RELEASE_DATE = org.sakaiproject.announcement.api.AnnouncementService.RELEASE_DATE;
	public static final String RETRACT_DATE = org.sakaiproject.announcement.api.AnnouncementService.RETRACT_DATE;
	public static final String ASSIGNMENT_REFERENCE = org.sakaiproject.announcement.api.AnnouncementService.ASSIGNMENT_REFERENCE;
	/** Events **/
    public static final String EVENT_ANNC_UPDATE_TITLE = org.sakaiproject.announcement.api.AnnouncementService.EVENT_ANNC_UPDATE_TITLE;
    public static final String EVENT_ANNC_UPDATE_ACCESS = org.sakaiproject.announcement.api.AnnouncementService.EVENT_ANNC_UPDATE_ACCESS;
    public static final String EVENT_ANNC_UPDATE_AVAILABILITY = org.sakaiproject.announcement.api.AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY;
	public static final String EVENT_AVAILABLE_ANNC = org.sakaiproject.announcement.api.AnnouncementService.EVENT_AVAILABLE_ANNC;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.announcement.api.AnnouncementService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.announcement.api.AnnouncementService) ComponentManager
						.get(org.sakaiproject.announcement.api.AnnouncementService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.announcement.api.AnnouncementService) ComponentManager
					.get(org.sakaiproject.announcement.api.AnnouncementService.class);
		}
	}

	private static org.sakaiproject.announcement.api.AnnouncementService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.announcement.api.AnnouncementService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.announcement.api.AnnouncementService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ANNC_ROOT = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_ROOT;

	public static java.lang.String SECURE_ANNC_READ = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_READ;

	public static java.lang.String SECURE_ANNC_ADD = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_ADD;

	public static java.lang.String SECURE_ANNC_REMOVE_OWN = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_REMOVE_OWN;

	public static java.lang.String SECURE_ANNC_REMOVE_ANY = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_REMOVE_ANY;

	public static java.lang.String SECURE_ANNC_UPDATE_OWN = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_UPDATE_OWN;

	public static java.lang.String SECURE_ANNC_UPDATE_ANY = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_UPDATE_ANY;

	public static java.lang.String SECURE_ANNC_READ_DRAFT = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ANNC_READ_DRAFT;

	public static java.lang.String SECURE_READ = org.sakaiproject.announcement.api.AnnouncementService.SECURE_READ;

	public static java.lang.String SECURE_ADD = org.sakaiproject.announcement.api.AnnouncementService.SECURE_ADD;

	public static java.lang.String SECURE_REMOVE_OWN = org.sakaiproject.announcement.api.AnnouncementService.SECURE_REMOVE_OWN;

	public static java.lang.String SECURE_REMOVE_ANY = org.sakaiproject.announcement.api.AnnouncementService.SECURE_REMOVE_ANY;

	public static java.lang.String SECURE_UPDATE_OWN = org.sakaiproject.announcement.api.AnnouncementService.SECURE_UPDATE_OWN;

	public static java.lang.String SECURE_UPDATE_ANY = org.sakaiproject.announcement.api.AnnouncementService.SECURE_UPDATE_ANY;

	public static java.lang.String SECURE_READ_DRAFT = org.sakaiproject.announcement.api.AnnouncementService.SECURE_READ_DRAFT;

	public static java.lang.String REF_TYPE_CHANNEL = org.sakaiproject.announcement.api.AnnouncementService.REF_TYPE_CHANNEL;

	public static java.lang.String REF_TYPE_MESSAGE = org.sakaiproject.announcement.api.AnnouncementService.REF_TYPE_MESSAGE;

	/**
	 * A (AnnouncementChannel) cover for getChannel() to return a specific announcement channel.
	 * @param param0  The channel reference string.
	 * @return the AnnouncementChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public static org.sakaiproject.announcement.api.AnnouncementChannel getAnnouncementChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getAnnouncementChannel(param0);
	}

	/**
	 * A (AnnouncementChannel) cover for addChannel() to add a new announcement channel.
	 * @param param0  The channel reference string.
	 * @return The newly created channel.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public static org.sakaiproject.announcement.api.AnnouncementChannelEdit addAnnouncementChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.addAnnouncementChannel(param0);
	}

	/**
	 * check permissions for getChannel().
	 * @param param0  The channel reference string.
	 * @return true if the user is allowed to getChannel(channelId), false if not.
	 */
	public static boolean allowGetChannel(java.lang.String param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return false;

		return service.allowGetChannel(param0);
	}

	/**
	 * check permissions for addChannel().
	 * @param param0  The channel reference string.
	 * @return true if the user is allowed to addChannel(channelId), false if not.
	 */
	public static boolean allowAddChannel(java.lang.String param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return false;

		return service.allowAddChannel(param0);
	}

	/**
	 * Add a new channel. Must commitEdit() to make official, or cancelEdit() when done!
	 * @param The channel reference string.
	 * @return The newly created channel, locked for update.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public static org.sakaiproject.message.api.MessageChannelEdit addChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.addChannel(param0);
	}

	/**
	 * check permissions for editChannel()
	 * @param param0  The channel reference string.
	 * @return true if the user is allowed to update the channel, false if not.
	 */
	public static boolean allowEditChannel(java.lang.String param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return false;

		return service.allowEditChannel(param0);
	}

	/**
	 * Return a specific channel, as specified by channel id, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
	 * @param param0 The channel reference string.
	 * @return the Channel that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to edit the channel.
	 * @exception InUseException
	 *            if the channel is locked for edit by someone else.
	 */
	public static org.sakaiproject.message.api.MessageChannelEdit editChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.editChannel(param0);
	}

	/**
	 * Commit the changes made to a MessageChannelEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * @param param0 The MessageChannelEdit object to commit.
	 */
	public static void commitChannel(org.sakaiproject.message.api.MessageChannelEdit param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return;

		service.commitChannel(param0);
	}

	/**
	 * Cancel the changes made to a MessageChannelEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * @param param0  The MessageChannelEdit object to cancel.
	 */
	public static void cancelChannel(org.sakaiproject.message.api.MessageChannelEdit param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return;

		service.cancelChannel(param0);
	}

	/**
	 * Check permissions for removeChannel(). 
	 * @param param0  The channel reference string.
	 * @return true if the user is allowed to removeChannel(), false if not.
	 */
	public static boolean allowRemoveChannel(java.lang.String param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveChannel(param0);
	}

	/**
	 * Remove a channel - it must be locked from editChannel().
	 * @param param0  The MessageChannelEdit object of the channel to remove.
	 * @exception PermissionException
	 *            if the user does not have permission to remove a channel.
	 */
	public static void removeChannel(org.sakaiproject.message.api.MessageChannelEdit param0)
			throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return;

		service.removeChannel(param0);
	}

	/**
	 * Access the internal reference which can be used to access the channel from within the system.
	 * @param param0 The context string.
	 * @param param1 The channel id.
	 * @return The the internal reference which can be used to access the channel from within the system.
	 */
	public static java.lang.String channelReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.channelReference(param0, param1);
	}

	/**
	 * Access the internal reference which can be used to access the message from within the system.
	 * @param param0 The context string.
	 * @param param1 The channel id.
	 * @param param2 The message id.
	 * @return The the internal reference which can be used to access the message from within the system.
	 */
	public static java.lang.String messageReference(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.messageReference(param0, param1, param2);
	}

	/**
	 * Access the internal reference which can be used to access the message from within the system.
	 * @param param0  The channel reference string
	 * @param param1  The message id.
	 * @return The the internal reference which can be used to access the message from within the system.
	 */
	public static java.lang.String messageReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.messageReference(param0, param1);
	}

	/**
	 * Cancel the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
	 * @param param0  The MessageEdit object to cancel.
	 */
	public static void cancelMessage(org.sakaiproject.message.api.MessageEdit param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return;

		service.cancelMessage(param0);
	}

	/**
	 * Access a list of messages in the channel, that are after the date, limited to just the n latest messages, ordered as specified, including drafts if specified. Channel read permission is required, unless pubViewOnly is selected - draft read on the
	 * channel is required to see drafts.
	 * @param param0 channel reference string
	 * @param param1 if null, no date limit, else limited to only messages after this date.
	 * @param param2 if 0, no count limit, else limited to only the latest this number of messages.
	 * @param param3 if true, sort oldest first, else sort latest first.
	 * @param param4 if true, include drafts (if the user has draft permission), else leave them out.
	 * @param param5 if true, include only messages marked pubview, else include any.
	 * @return A list of Message objects that meet the criteria; may be empty
	 * @exception PermissionException
	 *            If the current user does not have channel read permission.
	 */
	public static java.util.List getMessages(java.lang.String param0, org.sakaiproject.time.api.Time param1, int param2,
			boolean param3, boolean param4, boolean param5) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getMessages(param0, param1, param2, param3, param4, param5);
	}

	
	/**
	 * Access a list of channel ids that are defined related to the context.
	 * @param param0  The context in which to search
	 * @return A List (String) of channel id for channels withing the context.
	 */
	public static java.util.List getChannelIds(java.lang.String param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getChannelIds(param0);
	}

	/**
	 * Get a message, given a reference. This call avoids the need to have channel security, as long as the user has permissions to the message. 
	 * @param param0 The message reference
	 * @return The message.
	 * @exception IdUnusedException
	 *            If this reference does not identify a message.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the message.
	 */
	public static org.sakaiproject.message.api.Message getMessage(org.sakaiproject.entity.api.Reference param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getMessage(param0);
	}

	/**
	 * Return a specific channel. 
	 * @param param0 The channel reference.
	 * @return the MessageChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for any channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public static org.sakaiproject.message.api.MessageChannel getChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getChannel(param0);
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2,
			java.lang.String param3, java.util.Map param4, java.util.HashMap param5, java.util.Set param6)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.merge(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.lang.String getLabel()
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getLabel();
	}

	
	public static java.lang.String archive(java.lang.String param0, org.w3c.dom.Document param1, java.util.Stack param2,
			java.lang.String param3, java.util.List param4)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.archive(param0, param1, param2, param3, param4);
	}


    /**
     * Get a summary of an Announcement Channel
     * @param param0 channel reference string.
     * @param param1  Maximum number of items to return
     * @param param2  Maximum number of days to peer back
     * @return The Map containing the Summary
     * @exception IdUsedException
     *            if the id is not unique.
     * @exception IdInvalidException
     *            if the id is not made up of valid characters.
     * @exception PermissionException
     *            if the user does not have permission to add a channel.
     */
	public static java.util.Map getSummary( java.lang.String param0, int param1, int param2)
								throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
										 org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getSummary(param0, param1, param2);
	}
	 
	/**
	* Get announcement entity reference for given context
	* @param param0 announcement context (site-id)
	* @return announcement entity reference
	*/
	public static Reference getAnnouncementReference(String param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getAnnouncementReference(param0);
	}
	
	/**
	* Get URL to access the announcement rss feed
	* @param param0 The announcement entity reference
	* @return URL for announcement rss feed
	*/
	public static String getRssUrl(Reference param0)
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;

		return service.getRssUrl(param0);
	}
	
	/**
	 * Determine if message viewable based on release/retract dates (if set)
	 * @param param0 AnnouncementMessage
	 * @return boolean
	 */
	public static boolean isMessageViewable(AnnouncementMessage param0) 
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return false;

		return service.isMessageViewable(param0);
	}
	
	/**
	 * clears the message cache for this channel
	 * @param channelRef
	 */
	public static void clearMessagesCache(String channelRef){
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service != null){
			service.clearMessagesCache(channelRef);
		}
	}
	
	/**
	 * Return a list of messages from the provided channel (merged flag returns merged messages)
	 * @param channelReference
	 *        Channel's reference String
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @param order
	 *        Order of messages, ascending if true, descending if false
	 * @param merged
	 * 		  flag to include merged channel messages, true returns ALL messages including merged sites/channels
	 * @return a list of Message objects or specializations of Message objects (may be empty).
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 * @exception NullPointerException
	 */
	public static  java.util.List getMessages(String channelReference, Filter filter, boolean order, boolean merged) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException, NullPointerException
	{
		org.sakaiproject.announcement.api.AnnouncementService service = getInstance();
		if (service == null) return null;
		
		return service.getMessages(channelReference,filter, order, merged);	
	}
	
}
