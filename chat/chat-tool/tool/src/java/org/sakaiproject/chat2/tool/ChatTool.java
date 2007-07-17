/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-util/util/src/java/org/sakaiproject/portal/util/PortalSiteHelper.java $
 * $Id: PortalSiteHelper.java 21708 2007-02-18 21:59:28Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
 
package org.sakaiproject.chat2.tool;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.RoomObserver;
import org.sakaiproject.chat2.model.ChatFunctions;
import org.sakaiproject.chat2.model.PresenceObserver;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.courier.api.CourierService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.DirectRefreshDelivery;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * Chat works by the courier but not the same way as the old message delivery
 * system.  The courier sends messages into the JSF tool.  When the user dials home
 * it then picks up the messages.
 * 
 * COOL TECHNOLOGY ALERT:
 *   TODO: The page that sends the messages is polled, the tool can wait on the
 *   connection until it recieves a message to send or until it times out.  This
 *   would provide instant messaging as opposed to polling.
 * 
 * When entering the tool the first main page calls the enterTool function which
 * redirects the users based upon the tool's preferences: go to select a room or 
 * to a specific tool.
 * 
 * The PresenceObserverHelper is placed on the current room.  This this tool is informed
 * when a user enters and exits the room.  If the tool is forgotten (user clicked
 * the tool refresh button or closed the browser) then eventually the presence service
 * will remove that user from the location... which we are informed.  The problem is
 * that the presence change event doesn't say WHO entered or exited.  Thus... we must wait
 * until there are no users in the location before we know that we shouldn't be observing
 * 
 * 
 * @author andersjb
 *
 */
public class ChatTool implements RoomObserver, PresenceObserver {

   /** Our logger. */
   private static Log logger = LogFactory.getLog(ChatTool.class);
   
   /*  */
   private static final String IFRAME_ROOM_USERS = "Presence";
   
   /* various pages that we can go to within the tool */
   private static final String PAGE_EDIT_A_ROOM = "editRoom";
   private static final String PAGE_LIST_ROOMS = "listRooms";
   private static final String PAGE_ENTER_ROOM = "room";
   private static final String PAGE_ROOM_CONTROL = "roomControl";
   private static final String PAGE_EDIT_ROOM = "editRoom";
   private static final String PAGE_DELETE_ROOM_CONFIRM = "deleteRoomConfirm";
   private static final String PAGE_DELETE_ROOM_MESSAGES_CONFIRM = "deleteRoomMessagesConfirm";
   private static final String PAGE_DELETE_MESSAGE_CONFIRM = "deleteMessageConfirm";
   private static final String PAGE_SYNOPTIC = "synoptic";
   private static final String PAGE_SYNOPTIC_OPTIONS = "synopticOptions";
   
   private static final String PERMISSION_ERROR = "perm_error";
   private static final String PRESENCE_PREPEND = "chat_room_";
   
   private static final int MESSAGEOPTIONS_NULL = -99;
   private static final int MESSAGEOPTIONS_ALL_MESSAGES = -1;
   private static final int MESSAGEOPTIONS_MESSAGES_BY_DATE = 0;
   private static final int MESSAGEOPTIONS_MESSAGES_BY_NUMBER = 1;
   private static final int MESSAGEOPTIONS_NO_MESSAGES = 2;
   
   private static final int DATETIME_DISPLAY_NONE = 0;
   private static final int DATETIME_DISPLAY_TIME = 1;
   private static final int DATETIME_DISPLAY_DATE = 2;
   private static final int DATETIME_DISPLAY_DATETIME = 3;
   
   
   private static final String PARAM_CHANNEL = "channel";
   private static final String PARAM_DAYS = "days";
   private static final String PARAM_ITEMS = "items";
   private static final String PARAM_LENGTH = "length";

   private static final int DEFAULT_DAYS = 10;
   private static final int DEFAULT_ITEMS = 3;
   private static final int DEFAULT_LENGTH = 50;
   
   /* All the managers */
   /**   The work-horse of chat   */
   private ChatManager chatManager;
   
   /**   The tool manager   */
   private ToolManager toolManager;
   
   /** Constructor discovered injected CourierService. */
   protected CourierService m_courierService = null;
   
   
   /* All the private variables */
   /** The current channel the user is in */
   private DecoratedChatChannel currentChannel = null;
   
   /** The current channel the user is editing */
   private DecoratedChatChannel currentChannelEdit = null;
   
   /** The current message the user is deleting */
   private DecoratedChatMessage currentMessage = null;
   
   private DecoratedSynopticOptions currentSynopticOptions = null;
   
   /** The location where the new message text goes */
   private String newMessageText = "";
   
   /** display the time (1), date(2), both(3), or neither(0) */
   private int viewOptions = DATETIME_DISPLAY_DATETIME;
   
   /** display all messages (-1), past 3 days (0) */
   private int messageOptions = MESSAGEOPTIONS_NULL;
   
   /** The id of the session. needed for adding messages to the courier because that runs in the notification thread */
   private String sessionId = "";
   
   /** The id of the placement of this sakai tool.  the jsf tool bean needs this for passing to the delivery  */
   private String placementId = "";
   
   /** Mapping the color of each message */
   private ColorMapper colorMapper = new ColorMapper();
   
   /** the worksite the tool is in */
   private Site worksite = null;
   
   private String toolContext = null;
   
   /*  error conditions */
   /** an error that could display on the select a chat room page */
   private boolean selectedRoomNotAvailable = false;
   
   /** Allows us to see who is in the current room */
   private PresenceObserverHelper presenceChannelObserver = null;
   
   
   /**
    * This is called from the first page to redirect the user to the proper view.
    * If the tool is set to go to the select a chat room view and there are multiple chat
    * rooms, then it will go to the select a room page.  If the user is to select a room and
    * there is only one room, then it will go to that room.
    * 
    * @return String
    */
   public String getEnterTool() {
      
      // "inject" a CourierService
      m_courierService = org.sakaiproject.courier.cover.CourierService.getInstance();
      
      Session session = SessionManager.getCurrentSession();
      
      sessionId = session.getId();
      String url = PAGE_ENTER_ROOM;
      
      Placement placement = getToolManager().getCurrentPlacement();
      placementId = placement.getId();
      
      //Really onl calling this just to make sure a room gets created
      List rooms = getSiteChannels();
      
      ChatChannel defaultChannel = getChatManager().getDefaultChannel(placement.getContext());
      setCurrentChannel(new DecoratedChatChannel(this, defaultChannel));
         
      // if there is no room selected to enter then go to select a room
      if(currentChannel == null)
         url = PAGE_LIST_ROOMS;
         
      
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      
      HttpServletRequest req = (HttpServletRequest) context.getRequest();
      req.setAttribute(Tool.NATIVE_URL, null); //signal to WrappedRequest that we want the Sakai managed
      setToolContext(req.getContextPath());
      req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
      
      
      try {
           context.redirect(url);
      }
      catch (IOException e) {
           throw new RuntimeException("Failed to redirect to " + url, e);
      }
      return "";
   }
   
   /**
    * this gets the users in the current channel.  It should be called often
    * to refresh the presence of the channel
    * @return List of Sakai User(?)
    */
   public List getUsersInCurrentChannel()
   {
      if(!refreshPresence()) {
         return new ArrayList();
      }
      
      presenceChannelObserver.updatePresence();
      
      // put into context a list of sessions with chat presence
      String location = presenceChannelObserver.getLocation();

      // get the current presence list (User objects) for this page
      List users = presenceChannelObserver.getPresentUsers();
      
      return users;
   }
   
   protected boolean refreshPresence() {
      if (getCurrentChannel() == null) {
         ChatChannel defaultChannel = getChatManager().getDefaultChannel(
               getToolManager().getCurrentPlacement().getContext());
         setCurrentChannel(new DecoratedChatChannel(this, defaultChannel));
      }
      if(getCurrentChannel() != null) {
         // place a presence observer on this tool.
         presenceChannelObserver = new PresenceObserverHelper(this,
               getCurrentChannel().getChatChannel().getId());
         
         getChatManager().addRoomListener(this, getCurrentChannel().getChatChannel().getId());
         return true;
         //presenceChannelObserver.updatePresence();
      }
      return false;
   }
   

   //********************************************************************
   // Interface Implementations
   
   /**
    * {@inheritDoc}
    * in the context of the event manager thread
    */
   public void receivedMessage(String roomId, Object message)
   {
      if(currentChannel != null && currentChannel.getChatChannel().getId().equals(roomId)) {
         m_courierService.deliver(new ChatDelivery(sessionId+roomId, "Monitor", message, placementId, false, getChatManager()));
      }
   }

   /**
    * {@inheritDoc}
    */
   public void roomDeleted(String roomId)
   {
      if(currentChannel != null && currentChannel.getChatChannel().getId().equals(roomId)) {
         resetCurrentChannel(currentChannel);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void userJoined(String location, String user)
   {
      m_courierService.deliver(new DirectRefreshDelivery(sessionId+location, "Presence"));
   }

   /**
    * {@inheritDoc}
    * If a user left the location and there are no more users then 
    * something happened and this is a stray presence observer.  When there 
    * are no users in a presence then the observer should be removed because 
    * this tool isn't updating the presence any longer thus isn't in the location
    * thus doesn't need to observe the presence of the room.
    */
   public void userLeft(String location, String user)
   {
      if(presenceChannelObserver != null && presenceChannelObserver.getPresentUsers().size() == 0) {
         presenceChannelObserver.endObservation();
         getChatManager().removeRoomListener(this, currentChannel.getChatChannel().getId());
         presenceChannelObserver = null;
      } else
         m_courierService.deliver(new DirectRefreshDelivery(sessionId+location, "Presence"));
   }


   
   //********************************************************************
   // Tool Process Actions
   
   /**
    * resets various variables for the tool
    */
   private void clearToolVars()
   {
      selectedRoomNotAvailable = false;
   }
   
   /**
    * Make sure that the channel has a title when saving, the title isn't 
    * too long, and the description isn't too long
    * @param channel
    * @return Returns if the channel validates
    */
   protected boolean validateChannel(ChatChannel channel) {      
      boolean validates = true;
      if (channel.getTitle() == null || channel.getTitle().length() == 0) {
         
         setErrorMessage("editRoomForm:title", "title_required", new String[] {});
         validates = false;
      }
      if (channel.getTitle() != null && channel.getTitle().length() > 64) {
         
         setErrorMessage("editRoomForm:title", "title_too_long", new String[] {Integer.toString(64)});
         validates = false;
      }
      if (channel.getDescription() != null && channel.getDescription().length() > 255) {
         
         setErrorMessage("editRoomForm:desc", "desc_too_long", new String[] {Integer.toString(255)});
         validates = false;
      }
      
      if (!validates)
         setErrorMessage("validation_error", new String[] {});
      
      return validates;
   }
   
   /**
    * When the user wants to cancel changing a room
    * @return String next page
    */
   public String processActionCancelChangeChannel()
   {
      clearToolVars();
      
      return PAGE_ENTER_ROOM;
   }
   
   public String processActionSetAsDefaultRoom(DecoratedChatChannel decoChannel) {
      ChatChannel channel = decoChannel.getChatChannel();
      channel.setContextDefaultChannel(true);
      getChatManager().makeDefaultContextChannel(channel);
      return PAGE_LIST_ROOMS;
   }
   
   public String processActionAddRoom()
   {
      try {
         ChatChannel newChannel = getChatManager().createNewChannel(getContext(), "", false, true);
         currentChannelEdit = new DecoratedChatChannel(this, newChannel, true);
         
         //init the filter param
         if (currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER) ||
               currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME) ||
               currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) {
            currentChannelEdit.setFilterParamLast(currentChannelEdit.getChatChannel().getFilterParam());
            currentChannelEdit.setFilterParamPast(currentChannelEdit.getChatChannel().getFilterParam());
            currentChannelEdit.setFilterParamNone(0);
         }
         //return "";
         return PAGE_EDIT_A_ROOM;
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL});
         return "";
      }
   }
   
   public String processActionSubmitMessage()
   {
      try {
         ChatMessage message = getChatManager().createNewMessage(
               getCurrentChannel().getChatChannel(), SessionManager.getCurrentSessionUserId());
         message.setBody(newMessageText);
         if (!newMessageText.equals("")) {
            newMessageText = "";
            getChatManager().updateMessage(message);
            getChatManager().sendMessage(message);
         }
         return PAGE_ROOM_CONTROL;
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_NEW});
         return "";
      }
   }
   
   public String processActionResetMessage()
   {
      newMessageText = "";
      return PAGE_ROOM_CONTROL;
   }
   public String processActionPermissions()
   {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

      try {
          String url = "sakai.permissions.helper.helper/tool?" +
                  "session." + PermissionsHelper.DESCRIPTION + "=" +
                  getPermissionsMessage() +
                  "&session." + PermissionsHelper.TARGET_REF + "=" +
                  getWorksite().getReference() +
                  "&session." + PermissionsHelper.PREFIX + "=" +
                  getChatFunctionPrefix();

          context.redirect(url);
      }
      catch (IOException e) {
          throw new RuntimeException("Failed to redirect to helper", e);
      }
      return null;
   }
   
  
   public String processActionSynopticOptions() {
      DecoratedSynopticOptions dso = lookupSynopticOptions();
      setCurrentSynopticOptions(dso);
      return PAGE_SYNOPTIC_OPTIONS;
   }

   public String processActionSynopticOptionsSave() {
      DecoratedSynopticOptions dso = getCurrentSynopticOptions();
      
      Placement placement = getToolManager().getCurrentPlacement();
      if (placement != null)
      {
         //placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));
         placement.getPlacementConfig().setProperty(PARAM_DAYS, Integer.toString(dso.getDays()));
         placement.getPlacementConfig().setProperty(PARAM_ITEMS, Integer.toString(dso.getItems()));
         placement.getPlacementConfig().setProperty(PARAM_LENGTH, Integer.toString(dso.getChars()));
         placement.save();
      }
      setCurrentSynopticOptions(null);
      return PAGE_SYNOPTIC;
   }
   
   public String processActionSynopticOptionsCancel() {
      setCurrentSynopticOptions(null);
      return PAGE_SYNOPTIC;
   }
   
   
   public String processActionBackToRoom() {
      return PAGE_ENTER_ROOM;
   }
   
   public String getChatFunctionPrefix()
   {
      return ChatFunctions.CHAT_FUNCTION_PREFIX;
   }
   
   public String getPermissionsMessage() {
      return getMessageFromBundle("perm_description", new Object[]{
            getToolManager().getCurrentTool().getTitle(), getWorksite().getTitle()});
  }

   public Site getWorksite() {
       if (worksite == null) {
           try {
               worksite = SiteService.getSite(getToolManager().getCurrentPlacement().getContext());
           }
           catch (IdUnusedException e) {
               throw new RuntimeException(e);
           }
       }
       return worksite;
   }
   
   
   
   //********************************************************************
   // Channel Process Actions
   
   /**
    * Sets the current room and goes to the room page
    * @param chatChannel
    * @return String selects a new room to go into
    */
   protected String processActionEnterRoom(DecoratedChatChannel chatChannel)
   {
      setCurrentChannel(chatChannel);
      setMessageOptions(Integer.toString(MESSAGEOPTIONS_NULL));
      return PAGE_ENTER_ROOM;
   }
   
   public String processActionListRooms() {
      return PAGE_LIST_ROOMS;
   }
   
   /**
    * Sets the current room and goes to edit that room
    * @param chatChannel
    * @return String goes to the edit room view
    */
   protected String processActionEditRoom(DecoratedChatChannel chatChannel)
   {
      //Init the filter param here
      if (chatChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER) ||
            chatChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME) ||
            chatChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) {
         chatChannel.setFilterParamLast(chatChannel.getChatChannel().getFilterParam());
         chatChannel.setFilterParamPast(chatChannel.getChatChannel().getFilterParam());
         chatChannel.setFilterParamNone(0);
      }

      setCurrentChannelEdit(chatChannel);
      return PAGE_EDIT_ROOM;
   }

   /**
    * Sets the current room to null
    * @return String goes to the edit room view
    */
   public String processActionEditRoomSave()
   {
      //Set the filter param here
      ChatChannel channel = getCurrentChannelEdit().getChatChannel();
      boolean directEdit = getCurrentChannelEdit().isDirectEdit();
      
      if (channel.getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) {
         channel.setFilterParam(getCurrentChannelEdit().getFilterParamLast());
      }
      else if (channel.getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
         channel.setFilterParam(getCurrentChannelEdit().getFilterParamPast());
      }
      else if (channel.getFilterType().equals(ChatChannel.FILTER_NONE)) {
         channel.setFilterParam(0);
      }
      String retView = PAGE_LIST_ROOMS;
      
      if (directEdit)
         retView = PAGE_ENTER_ROOM;
      else
         retView = PAGE_LIST_ROOMS;
      
      if (validateChannel(channel))
         try {
            getChatManager().updateChannel(channel, true);
            
            if (getCurrentChannel().getChatChannel().getId().equals(channel.getId())) {
               setCurrentChannel(new DecoratedChatChannel(this, channel));
            }
            //setCurrentChannel(channel);
            setCurrentChannelEdit(null);
            
         }
         catch (PermissionException e) {
            setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL});
            return "";
         }
      else {
         //Message should get set in the validateChannel method
         //setErrorMessage(VALIDATION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_DELETE_PREFIX});
         retView = "";
      }
      return retView;
   }
   
   /**
    * Sets the current room to null
    * @return String goes to the edit room view
    */
   public String processActionEditRoomCancel()
   {
      boolean directEdit = getCurrentChannelEdit().isDirectEdit();
      setCurrentChannelEdit(null);
      
      if (directEdit)
         return PAGE_ENTER_ROOM;
      else
         return PAGE_LIST_ROOMS;
      
   }
   
   /**
    * Sets the current room and goes to confirm deleting the room
    * @param chatChannel
    * @return String goes to the delete room confirmation page
    */
   protected String processActionDeleteRoomConfirm(DecoratedChatChannel chatChannel)
   {
      setCurrentChannelEdit(chatChannel);
      return PAGE_DELETE_ROOM_CONFIRM;
   }
   
   
   /**
    * deletes the current room and all it's messages
    * @return String goes to the select a room page
    */
   public String processActionDeleteRoom()
   {
      try {
         getChatManager().deleteChannel(currentChannelEdit.getChatChannel());
         setCurrentChannelEdit(null);
         return PAGE_LIST_ROOMS;
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL});
         return "";
      }
   }
   
   
   /**
    * cancels the deletion of a room via the confirmation
    * @return String goes to the select a room page
    */
   public String processActionDeleteRoomCancel()
   {
      setCurrentChannelEdit(null);
      return PAGE_LIST_ROOMS;
   }
   
   
   // *************
   
   /**
    * Sets the current room and goes to confirm deleting the room's messages
    * @param chatChannel
    * @return String goes to the delete room messages confirmation page
    */
   protected String processActionDeleteRoomMessagesConfirm(DecoratedChatChannel chatChannel)
   {
      setCurrentChannelEdit(chatChannel);
      return PAGE_DELETE_ROOM_MESSAGES_CONFIRM;
   }
   
   
   /**
    * deletes the current room's messages
    * @return String goes to the select a room page
    */
   public String processActionDeleteRoomMessages()
   {
      try {
         getChatManager().deleteChannelMessages(currentChannelEdit.getChatChannel());
         setCurrentChannelEdit(null);
         return PAGE_LIST_ROOMS;
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_DELETE_ANY});
         return "";
      }
   }
   
   
   /**
    * cancels the deletion of a room's messages via the confirmation
    * @return String goes to the select a room page
    */
   public String processActionDeleteRoomMessagesCancel()
   {
      setCurrentChannelEdit(null);
      return PAGE_LIST_ROOMS;
   }
   
   // ********************************************************************
   // Message Process Actions
   
   
   /**
    * Deletes the current message
    * @return String goes to the room's main page
    */
   public String processActionDeleteMessage()
   {
      try {
         getChatManager().deleteMessage(getCurrentMessage().getChatMessage());
         return PAGE_ENTER_ROOM;
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_DELETE_PREFIX});
         return "";
      }
   }
   
   /**
    * Deletes the specified message
    * @param message
    * @return String goes to the delete message page
    */
   protected String processActionDeleteMessageConfirm(DecoratedChatMessage message)
   {
      setCurrentMessage(message);
      //getChatManager().deleteMessage(message);
      return PAGE_DELETE_MESSAGE_CONFIRM;
   }
   
   /**
    * Cancels the delete of the current message
    * @return String goes to the room's main page
    */
   public String processActionDeleteMessageCancel()
   {
      setCurrentMessage(null);
      return PAGE_ENTER_ROOM;
   }

   
   //********************************************************************
   // Getters and Setters

   /**
    * This allows us to change/give the courier address.
    * We want the courier to respond to the chat room.
    * @return String
    */
   public String getCourierString() {
      return "/courier/" + getCurrentChatChannelId() + "/" + getToolString();
   }
   
   public boolean getCanManageTool()
   {
      return getMaintainer();
   }
   
   
   /**
    * gets the channel id of the current channel.  If there isn't one then 
    * give a blank string
    * @return ChatManager
    */
   public String getCurrentChatChannelId() {
      if(currentChannel == null)
         return "";
      return currentChannel.getChatChannel().getId();
   }
   
   /**
    * gets the current channel
    * @return ChatChannel
    */
   public DecoratedChatChannel getCurrentChannel()
   {
      return currentChannel;
   }
   
   /**
    * Implements a change of the chat room.  It removes presence from the prior room,
    *  adds observation of the new room, and then becomes present in the new room
    * @param channel
    */
   public void setCurrentChannel(DecoratedChatChannel channel)
   {
      if(presenceChannelObserver != null) {
         presenceChannelObserver.endObservation();
         presenceChannelObserver.removePresence();
         getChatManager().removeRoomListener(this, channel.getChatChannel().getId());
      }
      presenceChannelObserver = null;
      
      this.currentChannel = channel;

      if(channel != null) {
         // place a presence observer on this tool.
         presenceChannelObserver = new PresenceObserverHelper(this,
                  channel.getChatChannel().getId());
         
         getChatManager().addRoomListener(this, channel.getChatChannel().getId());
         
         presenceChannelObserver.updatePresence();
      }
   }
   
   protected void resetCurrentChannel(DecoratedChatChannel oldChannel) {
      if(presenceChannelObserver != null) {
         presenceChannelObserver.endObservation();
         presenceChannelObserver.removePresence();
         getChatManager().removeRoomListener(this, oldChannel.getChatChannel().getId());
      }
      presenceChannelObserver = null;
      
      this.currentChannel = null;
   }
   
   /**
    * @return the currentChannelEdit
    */
   public DecoratedChatChannel getCurrentChannelEdit() {
      return currentChannelEdit;
   }

   /**
    * @param currentChannelEdit the currentChannelEdit to set
    */
   public void setCurrentChannelEdit(DecoratedChatChannel currentChannelEdit) {
      this.currentChannelEdit = currentChannelEdit;
   }

   /**
    * @return the currentMessage
    */
   public DecoratedChatMessage getCurrentMessage() {
      DecoratedChatMessage tmpCurrent = null;
      if (currentMessage == null) {         
         String messageId = (String)SessionManager.getCurrentToolSession().getAttribute("current_message");
         if(messageId != null) {
            ChatMessage message = getChatManager().getMessage(messageId);
            tmpCurrent = new DecoratedChatMessage(this, message);
            return tmpCurrent;
         }
      }
      return currentMessage;
   }

   /**
    * @param currentMessage the currentMessage to set
    */
   public void setCurrentMessage(DecoratedChatMessage currentMessage) {
      this.currentMessage = currentMessage;
   }

   public DecoratedSynopticOptions lookupSynopticOptions() {
      DecoratedSynopticOptions dso = new DecoratedSynopticOptions();
      Placement placement = getToolManager().getCurrentPlacement();
      try {
         dso.setDays(Integer.parseInt(placement.getPlacementConfig().getProperty(PARAM_DAYS)));
      } catch (NumberFormatException e) {
         dso.setDays(DEFAULT_DAYS);
         logger.debug("Can't get tool property for synoptic chat.  Using default option");
      }
      try {
         dso.setItems(Integer.parseInt(placement.getPlacementConfig().getProperty(PARAM_ITEMS)));
      } catch (NumberFormatException e) {
         dso.setItems(DEFAULT_ITEMS);
         logger.debug("Can't get tool property for synoptic chat.  Using default option");
      }
      try {
         dso.setChars(Integer.parseInt(placement.getPlacementConfig().getProperty(PARAM_LENGTH)));
      } catch (NumberFormatException e) {
         dso.setChars(DEFAULT_LENGTH);
         logger.debug("Can't get tool property for synoptic chat.  Using default option");
      }
      return dso;
   }
   /**
    * @return the synopticOptions
    */
   public DecoratedSynopticOptions getCurrentSynopticOptions() {
      return currentSynopticOptions;
   }

   /**
    * @param synopticOptions the synopticOptions to set
    */
   public void setCurrentSynopticOptions(DecoratedSynopticOptions currentSynopticOptions) {
      this.currentSynopticOptions = currentSynopticOptions;
   }

   /**
    * This creates select items out of the channels available to the tool
    * @return List of SelectItem
    */
   public List getChatRoomsSelectItems()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      
      for(Iterator i = getSiteChannels().iterator(); i.hasNext(); ) {
         ChatChannel channel = (ChatChannel)i.next();
         items.add(createSelect(channel.getId(), channel.getTitle()));
      }
      
      return items;
   }
   
   
   /**
    * gets the tool decorated channels 
    * @return
    */
   public List getChatChannels()
   {
      List<DecoratedChatChannel> items = new ArrayList<DecoratedChatChannel>();
      
      for(Iterator i = getSiteChannels().iterator(); i.hasNext(); ) {
         ChatChannel channel = (ChatChannel)i.next();
         items.add(new DecoratedChatChannel(this, channel));
      }
      
      return items;
   }
   
   /**
    * gets the chatManager
    * @return ChatManager
    */
   public ChatManager getChatManager() {
      return chatManager;
   }
   
   public String getViewOptions() {
      return Integer.toString(viewOptions);
   }
   public void setViewOptions(String d) {
      viewOptions = Integer.parseInt(d);
   }
   
   protected int initMessageOptions() {
      
      int result = MESSAGEOPTIONS_ALL_MESSAGES;
      if (getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_ALL)) {
         result = MESSAGEOPTIONS_ALL_MESSAGES;
      }
      else if (getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) {
         result = MESSAGEOPTIONS_MESSAGES_BY_NUMBER;
      }
      else if (getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
         result = MESSAGEOPTIONS_MESSAGES_BY_DATE;
      }
      else if (getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) {
         result = MESSAGEOPTIONS_NO_MESSAGES;
      }
      return result;
   }
   
   /**
    * @return the messageOptions
    */
   public String getMessageOptions() {
      if (messageOptions == MESSAGEOPTIONS_NULL)
         messageOptions = initMessageOptions();
      return Integer.toString(messageOptions);
   }

   /**
    * @param messageOptions the messageOptions to set
    */
   public void setMessageOptions(String messageOptions) {
      this.messageOptions = Integer.parseInt(messageOptions);
   }

   public String getNewMessageText() {
      return newMessageText;
   }

   public void setNewMessageText(String newMessageText) {
      this.newMessageText = newMessageText;
   }

   /**
    * Sets the chatManager
    * @param chatManager ChatManager
    */
   public void setChatManager(ChatManager chatManager) {
      this.chatManager = chatManager;
   }


   public ToolManager getToolManager() {
      return toolManager;
   }

   public void setToolManager(ToolManager toolManager) {
      this.toolManager = toolManager;
   }

   public ColorMapper getColorMapper() {
      return colorMapper;
   }

   public void setColorMapper(ColorMapper colorMapper) {
      this.colorMapper = colorMapper;
   }
   
   public boolean getDisplayDate()
   {
      //2
      int val = Integer.parseInt(getViewOptions());
      return ((val & 2) == 2);
      //return true;
   }
   
   public boolean getDisplayTime()
   {
      //1
      int val = Integer.parseInt(getViewOptions());
      return ((val & 1) == 1);
      //return true;
   }
   
   public boolean getCanRenderAllMessages() {
      return getCanRenderMessageOptions() ||
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_ALL);
   }

   public boolean getCanRenderDateMessages() {
      return !getCanRenderMessageOptions() && 
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME);
   }
   
   public boolean getCanRenderNumberMessages() {
      return !getCanRenderMessageOptions() && 
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER);
   }
   
   public boolean getCanRenderNoMessages() {
      return !getCanRenderMessageOptions() && 
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE);
   }
   
   public List getMessageOptionsList() {
      List<SelectItem> messageOptions = new ArrayList<SelectItem>();
      String filterType = getCurrentChannel().getChatChannel().getFilterType();
      int filterParam = getCurrentChannel().getChatChannel().getFilterParam();
      SelectItem item = new SelectItem(getCustomOptionValue(filterType), getCustomOptionText(filterType, filterParam));
      messageOptions.add(item);
      
      return messageOptions;
   }
   
   /**
    * Determins if the message option display dropdown gets rendered or not
    * @return
    */
   public boolean getCanRenderMessageOptions() {
      return getCurrentChannel().getChatChannel().isEnableUserOverride();
   }
   
   protected String getCustomOptionValue(String filterType) {
      int val = MESSAGEOPTIONS_MESSAGES_BY_DATE;
      //String filterType = getCurrentChannel().getChatChannel().getFilterType(); 
      if (filterType.equals(ChatChannel.FILTER_BY_TIME)) {
         val = MESSAGEOPTIONS_MESSAGES_BY_DATE;
      }
      else if (filterType.equals(ChatChannel.FILTER_BY_NUMBER)) {
         val = MESSAGEOPTIONS_MESSAGES_BY_NUMBER;
      }
      else if (filterType.equals(ChatChannel.FILTER_NONE)) {
         val = MESSAGEOPTIONS_NO_MESSAGES;
      }
      //val = getCurrentChannel().getChatChannel().getFilterParam();
      return Integer.toString(val);
   }
   
   protected String getCustomOptionText(String filterType, int filterParam) {
      //int x = 3;
      String result = getPastXDaysText(filterParam);
      
      //x= getCurrentChannel().getChatChannel().getFilterParam();
      //String filterType = getCurrentChannel().getChatChannel().getFilterType(); 
      if (filterType.equals(ChatChannel.FILTER_BY_TIME)) {
         result = getPastXDaysText(filterParam);
      }
      else if (filterType.equals(ChatChannel.FILTER_BY_NUMBER)) {
         result = getPastXMessagesText(filterParam);
      }
      else if (filterType.equals(ChatChannel.FILTER_NONE)) {
         result = getNoMessagesText();
      }
      return result;
   }
   
   public boolean getSoundAlert()
   {
      return true;
   }
   
   public List getRoomMessages()
   {
      Date xDaysOld = null;
      int maxMessages = 0;
      int x = getCurrentChannel().getChatChannel().getFilterParam();
      if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_DATE) {
         xDaysOld = getChatManager().calculateDateByOffset(x);
         maxMessages = ChatChannel.MAX_MESSAGES;
      }
      else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_NUMBER) {
         maxMessages = x;
      }
      else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_ALL_MESSAGES) {
         maxMessages = ChatChannel.MAX_MESSAGES;
      }
      else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_NO_MESSAGES) {
         maxMessages = 0;
      }
      return getMessages(getContext(), xDaysOld, maxMessages, true);
   }
   
   public List getSynopticMessages()
   {
      DecoratedSynopticOptions dso = lookupSynopticOptions();
      Date date = getChatManager().calculateDateByOffset(dso.getDays());
      return getMessages(getContext(), date, dso.getItems(), false);
   }
   
   /**
    * 
    * @param context
    * @param limitDate
    * @param numMessages
    * @param sortAsc
    * @return
    */
   protected List getMessages(String context, Date limitDate, int numMessages, boolean sortAsc)
   {
      List messages = new ArrayList();
      try {
         ChatChannel channel = (currentChannel==null) ? null : currentChannel.getChatChannel();
         messages = getChatManager().getChannelMessages(channel, context, limitDate, numMessages, sortAsc);
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_READ});
      }
      
      List<DecoratedChatMessage> decoratedMessages = new ArrayList<DecoratedChatMessage>();
      
      for(Iterator i = messages.iterator(); i.hasNext(); ) {
         ChatMessage message = (ChatMessage)i.next();
         
         DecoratedChatMessage decoratedMessage = new DecoratedChatMessage(this, message);
         
         decoratedMessages.add(decoratedMessage);
      }
      return decoratedMessages;
   }
   
   public boolean getCanRemoveMessage(ChatMessage message)
   {
      return getChatManager().getCanDelete(message);
   }

   public boolean getCanRemoveChannel(ChatChannel channel)
   {
      return getChatManager().getCanDelete(channel);
   }

   public boolean getCanRemoveChannelMessages(ChatChannel channel)
   {
      return getChatManager().getCanDeleteAnyMessage();
   }
   
   public boolean getCanEditChannel(ChatChannel channel)
   {
      return getChatManager().getCanEdit(channel);
   }
   
   public boolean getCanCreateChannel()
   {
      return getChatManager().getCanCreateChannel();
   }

   public boolean getCanRead(ChatChannel channel)
   {
      return getChatManager().getCanReadMessage(channel);
   }
   
   public boolean getMaintainer()
   {
      return getChatManager().isMaintainer();
   }
   
   public String getMessageOwnerDisplayName(ChatMessage message)
   {
      User sender = null;
      try {
         sender = UserDirectoryService.getUser(message.getOwner());
      } catch(UserNotDefinedException e) {
         logger.error(e);
         return message.getOwner();
      }
      return sender.getDisplayName();
   }

   protected String getPastXDaysText(int x) {
      return getMessageFromBundle("past_x_days", new Object[]{x});
   }
   
   protected String getPastXMessagesText(int x) {
      return getMessageFromBundle("past_x_messages", new Object[]{x});
   }

   protected String getNoMessagesText() {
      return getMessageFromBundle("shownone");
   }
   
   public String getViewingChatRoomText() {
      return getMessageFromBundle("viewingChatRoomText", new Object[]{getCurrentChannel().getChatChannel().getTitle()});
   }
   
   private void setErrorMessage(String errorMsg, Object[] extras)
   {
      setErrorMessage(null, errorMsg, extras);
   }
   
   private void setErrorMessage(String field, String errorMsg, Object[] extras)
   {
     logger.debug("setErrorMessage(String " + errorMsg + ")");
     FacesContext.getCurrentInstance().addMessage(field,
         new FacesMessage(getMessageFromBundle(errorMsg, extras)));
   }
   
   public String getServerUrl() {
      return ServerConfigurationService.getServerUrl();
   }
   
   //********************************************************************
   // Utilities

   /**
    * Gets the id of the tool we are in
    * @return String
    */
   public String getToolString() {
      return placementId;
   }   
   
   /**
    * Gets the id of the site we are in
    * @return String
    */
   protected String getContext() {
      return getToolManager().getCurrentPlacement().getContext();
   }
   
   /**
    * Returns the frame identifier for resizing
    * @return
    */
   public String getFramePlacementId() {
      return Validator.escapeJavascript("Main" + getToolManager().getCurrentPlacement().getId());
   }
   
   
   /**
    * gets the channels in this site
    * @return List of ChatChannel
    */
   protected List getSiteChannels() {
      return getChatManager().getContextChannels(getContext(), getMessageFromBundle("default_new_channel_title"));
   }
   
   
   /**
    * gets the number of channels in this site
    * @return int
    */
   public int getSiteChannelCount() {
      return getChatManager().getContextChannels(getContext(), getMessageFromBundle("default_new_channel_title")).size();
   }
   
   
   
   //********************************************************************
   // Common Utilities
   
   private ResourceLoader toolBundle;

   public SelectItem createSelect(Object id, String description) {
      SelectItem item = new SelectItem(id, description);
      return item;
   }

   public String getMessageFromBundle(String key, Object[] args) {
      return MessageFormat.format(getMessageFromBundle(key), args);
   }
/*
   public FacesMessage getFacesMessageFromBundle(String key, Object[] args) {
      return new FacesMessage(getMessageFromBundle(key, args));
   }*/

   public String getMessageFromBundle(String key) {
      if (toolBundle == null) {
         String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
         toolBundle = new ResourceLoader(bundle);
      /*   Locale requestLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
         if (requestLocale != null) {
            toolBundle = ResourceBundle.getBundle(
                  bundle, requestLocale);
         }
         else {
            toolBundle = ResourceBundle.getBundle(bundle);
         }*/
      }
      return toolBundle.getString(key);
   }

   public String getToolContext() {
      return toolContext;
   }

   public void setToolContext(String toolContext) {
      this.toolContext = toolContext;
   }
   
   
}
