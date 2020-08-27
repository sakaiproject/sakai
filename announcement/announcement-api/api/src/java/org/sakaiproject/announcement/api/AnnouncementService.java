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

package org.sakaiproject.announcement.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.message.api.MessageService;

/**
 * <p>
 * AnnouncementService is the extension to MessageService configured for Announcements.
 * </p>
 * <p>
 * MessageChannels are AnnouncementMessageChannels, and Messages are AnnouncementMessages with AnnouncementMessageHeaders.
 * </p>
 * <p>
 * Security in the announcement service, in addition to that defined in the channels, include:
 * <ul>
 * <li>announcement.channel.add</li>
 * </ul>
 * </p>
 * <li>announcement.channel.remove</li>
 * </ul>
 * </p>
 * <p>
 * Usage Events are generated:
 * <ul>
 * <li>announcement.channel.add - announcement channel resource id</li>
 * <li>announcement.channel.remove - announcement channel resource id</li>
 * </ul>
 * </p>
 */
public interface AnnouncementService extends MessageService
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:announcement";

	/** The Reference type for an announcement */
	public static final String REF_TYPE_ANNOUNCEMENT = "announcement";
	
    /** The property name of the channel to use instead of the site channel */
    public final static String ANNOUNCEMENT_CHANNEL_PROPERTY = "channel";

	/** The Reference type for an announcement rss feed */
	public static final String REF_TYPE_ANNOUNCEMENT_RSS = "rss";
	
	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + REF_TYPE_ANNOUNCEMENT;

	/** Security lock / event root for generic message events to make it a mail event. */
	public static final String SECURE_ANNC_ROOT = "annc.";

	/** Security lock / event for reading channel / message. */
	public static final String SECURE_ANNC_READ = SECURE_ANNC_ROOT + SECURE_READ;

	/** Security lock / event for adding channel / message. */
	public static final String SECURE_ANNC_ADD = SECURE_ANNC_ROOT + SECURE_ADD;

	/** Security lock / event for removing one's own message. */
	public static final String SECURE_ANNC_REMOVE_OWN = SECURE_ANNC_ROOT + SECURE_REMOVE_OWN;

	/** Security lock / event for removing anyone's message or channel. */
	public static final String SECURE_ANNC_REMOVE_ANY = SECURE_ANNC_ROOT + SECURE_REMOVE_ANY;

	/** Security lock / event for updating one's own message or the channel. */
	public static final String SECURE_ANNC_UPDATE_OWN = SECURE_ANNC_ROOT + SECURE_UPDATE_OWN;

	/** Security lock / event for updating any message. */
	public static final String SECURE_ANNC_UPDATE_ANY = SECURE_ANNC_ROOT + SECURE_UPDATE_ANY;

	/** Security lock / event for accessing someone elses draft. */
	public static final String SECURE_ANNC_READ_DRAFT = SECURE_ANNC_ROOT + SECURE_READ_DRAFT;

	/** Security function giving the user permission to all groups, if granted to at the channel or site level. */
	public static final String SECURE_ANNC_ALL_GROUPS = SECURE_ANNC_ROOT + SECURE_ALL_GROUPS;

	/** release date property names for announcements	 */
	public static final String RELEASE_DATE = "releaseDate";
	
	/** retract date property names for announcements	 */
	public static final String RETRACT_DATE = "retractDate";
	
	/** modified date property names for announcements	 */
	public static final String MOD_DATE = "modDate";
	
	/** assignment reference property for announcements       */
    public static final String ASSIGNMENT_REFERENCE = "assignmentReference";
	
    /** Event for updating announcement title **/
    public static final String EVENT_ANNC_UPDATE_TITLE = SECURE_ANNC_ROOT + "revise.title";
   
    /** Event for updating announcement access **/
    public static final String EVENT_ANNC_UPDATE_ACCESS = SECURE_ANNC_ROOT + "revise.access";
    
    /** Event for updating announcement availability **/
    public static final String EVENT_ANNC_UPDATE_AVAILABILITY = SECURE_ANNC_ROOT + "revise.availability";
    
    
	/**
	 * A (AnnouncementChannel) cover for getChannel() to return a specific announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the AnnouncementChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public AnnouncementChannel getAnnouncementChannel(String ref) throws IdUnusedException, PermissionException;

	/**
	 * A (AnnouncementChannel) cover for addChannel() to add a new announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public AnnouncementChannelEdit addAnnouncementChannel(String ref) throws IdUsedException, IdInvalidException,
			PermissionException;

	/**
	* Get announcement entity reference for given context
	* @param context announcement context (site-id)
	* @return announcement entity reference
	*/
	public Reference getAnnouncementReference(String context);
	
	/**
	* Get URL to access the announcement rss feed
	* @param ref The announcement entity reference
	* @return URL for announcement rss feed
	*/
	public String getRssUrl(Reference ref);
	
	/**
	 * Determine if message viewable based on release/retract dates (if set)
	 * @param AnnouncementMessage
	 * @return boolean
	 */
	public boolean isMessageViewable(AnnouncementMessage message);
	
	/**
	 * clears the message cache for this channel
	 * @param channelRef
	 */
	public void clearMessagesCache(String channelRef);
	
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
	public List getMessages(String channelReference, Filter filter, boolean order, boolean merged) throws IdUnusedException, PermissionException, NullPointerException;

	public Map<String, List<AnnouncementMessage>> getAllAnnouncementsForCurrentUser();
}
