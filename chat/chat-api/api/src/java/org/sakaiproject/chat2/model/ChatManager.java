/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
    * Creates a new ChatChannel but doesn't put it in the database.
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
    * gets all the messages from the Channel after the passed date
    * @param channel ChatChannel 
    * @param context Context of channel and messages to return
    * @param date Date that the messages need to be newer than.  All messages will be returned if null
    * @param items The number of messages to return.  All if set to 0
    * @param sortAsc Boolean to sort the records in ascending order
    * @return List of ChatMessages
    */
   public List<ChatMessage> getChannelMessages(ChatChannel channel, String context, Date date, int items, boolean sortAsc) throws PermissionException;
   
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
    * Adds a room listener on the room
    * @param observer RoomObserver the class to observe the room
    * @param roomId the room being observed
    */
   public void addRoomListener(RoomObserver observer, String roomId);

   /**
    * Removes a room listener on the room
    * @param observer RoomObserver the class to stop observing the room
    * @param roomId the room being observed
    */
   public void removeRoomListener(RoomObserver observer, String roomId);
   
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
   
   public List getContextChannels(String context, boolean lazy);
   
   /**
    * Gets the rooms associated with the context
    * If no rooms are found, one is created with the passed title
    * @param contextId Id
    * @param defaultNewTitle String the default name of a new ChatChannel
    * @param placement
    * @return List of ChatChannel
    */
   public List getContextChannels(String contextId, String defaultNewTitle, String placement);
   
   /**
    * Returns the context's default channel, or null if none.
    * @param contextId
    * @param placement
    * @return
    */
   public ChatChannel getDefaultChannel(String contextId, String placement);


   public boolean getCanDelete(ChatMessage message);
   
   public boolean getCanDelete(ChatChannel channel);
   //public boolean getCanDelete(ChatChannel channel, String placementId);
   
   /**
    * Returns whether or not the user has permissions to delete any messages
    */
   public boolean getCanDeleteAnyMessage(String context);
   
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
    * Makes the passed channel the dfault in the channel's context
    * @param channel
    * @param placement
    */
   public void makeDefaultContextChannel(ChatChannel channel, String placement);
   
   /**
    * Returns a Date object that is the offset number of days before the current date
    * @param offset Difference in days from current date
    * @return
    */
   public Date calculateDateByOffset(int offset);
   
   public String getLabel();
   
   /**
    * Insert migrated message data
    * @param sql sql statement to run
    * @param values Object[] of data to bind into the sql statement
    */
   public void migrateMessage(String sql, Object[] values);
   
   /**
    * Get the number of messages in a given chat channel
    * @param channel ChatChannel to find the number of messages
    * @return int the number of messages in the passed channel
    */
   public int countChannelMessages(ChatChannel channel);
   
}
