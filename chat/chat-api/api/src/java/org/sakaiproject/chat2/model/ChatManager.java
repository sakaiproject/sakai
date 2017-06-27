/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 
package org.sakaiproject.chat2.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.exception.PermissionException;

/**
 * 
 * @author andersjb
 *
 */
public interface ChatManager extends EntitySummary {

   /** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
   static final String APPLICATION_ID = "sakai:chat";
   
   /** This string starts the references to resources in this service. */
   public static final String REFERENCE_ROOT = "/chat";
   
   public static final String REF_TYPE_CHANNEL = "channel";
   
   public static final String CHAT = "chat";
   
   /** The Reference type for a messgae. */
   public static final String REF_TYPE_MESSAGE = "msg";

   public static final String CHAT_TOOL_ID = "sakai.chat";
   
   /**
    * Creates a new ChatChannel but doesn't put it in the database. To persist the channel, call updateChannel().
    * @param context Id of what the channel is linked to
    * @param title String the title of the channel
    * @param placementDefaultChannel boolean to set this as the default channel in the context
    * @param checkAuthz boolean indicating if we should check for authorization before creating the channel
    * @param placement String id of the tool placement
    * @return ChatChannel the new un-saved channel
    */
   public ChatChannel createNewChannel(String context, String title, boolean placementDefaultChannel, boolean checkAuthz, String placement) throws PermissionException;
   
   /**
    * updates the channel back into the database
    * @param channel ChatChannel
    * @param checkAuthz boolean indicating if we should check for authorization before updating
    */
   public void updateChannel(ChatChannel channel, boolean checkAuthz) throws PermissionException;
   
   /**
    * deletes the channel from the database.  It also removes the ChatMessages
    * @param channel
    */
   public void deleteChannel(ChatChannel channel) throws PermissionException;

   /**
    * gets one chat room
    * @param chatChannelId Id
    * @return ChatChannel
    */
   public ChatChannel getChatChannel(String chatChannelId);

   /**
    * gets all the messages from the Channel after the passed date,
    * limited to returning the the default maximum number of messages (100),
    * use {@link #getChannelMessagesCount(ChatChannel, String, Date)} to find the total number of messages
    * 
    * @param channel the ChatChannel to get messages for
    * @param context [OPTIONAL] Context of channel and messages to return (only used if the channel is null)
    * @param date [OPTIONAL] Date that the messages need to be newer than, includes all messages if null
    * @param start The item to start on (supports paging)
    * @param max The maximum number of items to return, uses the default maximum if above the default max or < 0, returns none if set to 0
    * @param sortAsc Boolean to sort the records in ascending order
    * @return List of ChatMessages
    */
   public List<ChatMessage> getChannelMessages(ChatChannel channel, String context, Date date, int start, int max, boolean sortAsc) throws PermissionException;

   /**
    * Gets the count of all the messages from the Channel after the passed date
    * 
    * @param channel the ChatChannel to get messages for
    * @param context [OPTIONAL] Context of channel and messages to return (only used if the channel is null)
    * @param date [OPTIONAL] Date that the messages need to be newer than, includes all messages if null
    * @return the count of ChatMessages
    */
   public int getChannelMessagesCount(ChatChannel channel, String context, Date date);
   
   /**
    * Get the number of messages in a given chat channel
    * @param channel ChatChannel to find the number of messages
    * @return int the number of messages in the passed channel
    * @deprecated use {@link #getChannelMessagesCount(ChatChannel, String, Date)}
    */
   public int countChannelMessages(ChatChannel channel);

   /**
    * creates an unsaved Chat Message
    * @param ChatChannel the channel that the new message will be in
    * @param String  the owner of the message
    * @return ChatMessage
    */
   public ChatMessage createNewMessage(ChatChannel channel, String owner) throws PermissionException;
   
   /**
    * saves a Chat Message
    * @param ChatMessage the message to update
    */
   public void updateMessage(ChatMessage message);
   
   /**
    * delete a Chat Message
    * @param ChatMessage the message to delete
    */
   public void deleteMessage(ChatMessage message) throws PermissionException;
   
   /**
    * delete all Chat Messages in a given channel
    * @param ChatChannel the channel to delete all messages from
    */
   public void deleteChannelMessages(ChatChannel channel) throws PermissionException;
 
   /**
    * gets the message with the id
    * @param chatMessageId Id
    * @return ChatMessage
    */
   public ChatMessage getMessage(String chatMessageId);
   
   /**
    * sends the message out to the other clients
    * @param entry ChatMessage
    */
   public void sendMessage(ChatMessage entry);
   
   /**
    * gets the rooms associated with the context
    * @param context Site the channel is in
    * @param lazy boolean to load the messages lazily or not
    * @return List of ChatChannel
    */
   public List<ChatChannel> getContextChannels(String context, boolean lazy);
   
   /**
    * Gets the rooms associated with the context
    * If no rooms are found, one is created with the passed title
    * @param contextId Id
    * @param defaultNewTitle String the default name of a new ChatChannel
    * @param placement
    * @return List of ChatChannel
    */
   public List<ChatChannel> getContextChannels(String contextId, String defaultNewTitle, String placement);
   
   /**
    * Returns the context's default channel, or null if none.
    * @param contextId
    * @param placement
    * @return the Channel
    */
   public ChatChannel getDefaultChannel(String contextId, String placement);

   /**
    * Returns whether or not the user has permissions to delete this message
    */
   public boolean getCanDelete(ChatMessage message);

   /**
    * Returns whether or not the user has permissions to delete this channel
    */
   public boolean getCanDelete(ChatChannel channel);
   
   /**
    * Returns whether or not the user has permissions to delete any messages in this context
    */
   public boolean getCanDeleteAnyMessage(String context);
   
   /**
    * Returns whether or not the user has permissions to edit this channel
    */
   public boolean getCanEdit(ChatChannel channel);
 
   /**
    * Returns whether or not the user has permissions to create a new channel in the given context
    */
   public boolean getCanCreateChannel(String context);
   
   /**
    * Returns whether or not the user can read messages in this channel
    */
   public boolean getCanReadMessage(ChatChannel channel);

   /**
    * Returns whether or not the user can post messages in this channel
    */
   public boolean getCanPostMessage(ChatChannel channel);
   
   /**
    * Returns whether or not the user is a site maintainer in this context
    */
   public boolean isMaintainer(String context);
   
   /**
    * Makes the passed channel the default in the channel's context
    * @param channel
    * @param placement
    */
   public void makeDefaultContextChannel(ChatChannel channel, String placement);
   
   /**
    * Returns a Date object that is the offset number of days before the current date
    * @param offset Difference in days from current date
    * @return a Date
    */
   public Date calculateDateByOffset(int offset);
 
   /**
    * Returns label used for entity producer
    */
   public String getLabel();
   
   /**
    * Insert migrated message data
    * @param sql sql statement to run
    * @param values Object[] of data to bind into the sql statement
    */
   public void migrateMessage(String sql, Object[] values);

    /**
     *
     * @return the max number of messages that are returned from storage
     */
   public int getMessagesMax();
   
   /**
    * Get all online users in given siteId and chat channel id 
    * 
    * @param siteId
    * @param channelId
    * @return
    */
   public List<SimpleUser> getPresentUsers(String siteId, String channelId);
   
   /**
    * - Update heartbeat for current user
    * - Get undelivered (latest) messages
    * - Get online users
    * - Get removed messages
    * 
    * @param siteId
    * @param channelId
    * @param sessionKey
    * @return
    */
   public Map<String,Object> handleChatData(String siteId, String channelId, String sessionKey);
   
   /**
    * Get pollInterval (in milliseconds) from properties
    * @return
    */
   public int getPollInterval();
   
   /**
    * Get session key (ussage_session_id:session_user_id) from current session
    * @return
    */
   public String getSessionKey();
   
   /**
    * Get different date strings based on given ChatMessage::messageDate
    * @param msg
    * @return
    */
   public MessageDateString getMessageDateString(ChatMessage msg);
   
   /**
    * Get user timezone from preferencesService.
    * This method is almost a duplicate from BasicTimeService.getUserTimezoneLocale. 
    * Would be great if the preferencesService returns it directly
    * @return
    */
   public String getUserTimeZone();
   
   
}
