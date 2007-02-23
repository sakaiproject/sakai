package org.sakaiproject.chat2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.metaobj.shared.model.Id;


/**
 * 
 * @author andersjb
 *
 */
public interface ChatManager {

   /** The key to the tool placement setting for where to go when entering the tool */
   public static String TOOL_INITIAL_VIEW_SETTING = "chatInitialView";
   
   /** The TOOL_INITIAL_VIEW_SETTING placement config is set to this if there is none.  This
    * specifies that the room selection window is presented at first */
   public static String INITIAL_VIEW_SELECT_ROOM = "____SELECT____ROOM____";

   /**
    * Creates a new ChatChannel but doesn't put it in the database.
    * @param context Id of what the channel is linked to
    * @param title String the title of the channel
    * @return ChatChannel the new un-saved channel
    */
   public ChatChannel createNewChannel(String context, String title);
   
   /**
    * updates the channel back into the database
    * @param channel ChatChannel
    */
   public void updateChannel(ChatChannel channel);
   
   /**
    * deletes the channel from the database.  It also removes the ChatMessages
    * @param channel
    */
   public void deleteChannel(ChatChannel channel);

   /**
    * gets one chat room
    * @param chatChannelId Id
    * @return ChatChannel
    */
   public ChatChannel getChatChannel(Id chatChannelId);

   /**
    * gets all the messages from the Channel
    * @param channel ChatChannel 
    * @return List of ChatMessages
    */
   public List getChannelMessages(ChatChannel channel);
   
   /**
    * creates an unsaved Chat Message
    * @param ChatChannel the channel that the new message will be in
    * @param String  the owner of the message
    * @return ChatMessage
    */
   public ChatMessage createNewMessage(ChatChannel channel, String owner);
   
   /**
    * saves a Chat Message
    * @param ChatMessage the message to update
    */
   public void updateMessage(ChatMessage message);
   
   /**
    * delete a Chat Message
    * @param ChatMessage the message to delete
    */
   public void deleteMessage(ChatMessage message);
   
   /**
    * gets the message with the id
    * @param chatMessageId Id
    * @return ChatMessage
    */
   public ChatMessage getMessage(Id chatMessageId);
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
    * @param contextId Id
    * @param defaultNewTitle String the default name of a new ChatChannel
    * @return List of ChatChannel
    */
   public List getContextChannels(String contextId, String defaultNewTitle);


   public Map getAuthorizationsMap();
   public boolean getCanDelete(ChatMessage chatMessage);
   public boolean getCanDelete(ChatMessage message, String placementId);
   public boolean isMaintaner();
   
}
