/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-util/util/src/java/org/sakaiproject/portal/util/PortalSiteHelper.java $
 * $Id: PortalSiteHelper.java 21708 2007-02-18 21:59:28Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 
package org.sakaiproject.chat2.tool;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
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
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
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
import org.sakaiproject.util.Web;
import org.sakaiproject.util.FormattedText;

/**
 *
 * This class is used in two ways:
 *  1) It is a JSF backing bean, with session scope. A new one is created
 *    every time the user enters the main chat tool. This includes a refresh.
 *  2) It is an observer. It is put on a list of observers for the room
 *    by AddRoomListener. 
 * This double existence is messy, because JSF manages beans, but
 * if JSF replaces or finishes the bean, there is no automatic way for
 * it to get removed from the list of observers. 
 *   Getting it off the list of observers is kind of tricky, because
 * there's no direct way to know when the instance is no longer valid.
 * Since a refresh generates a new instance, we have to get rid of the
 * old one, or the list of observers keeps growing. We assume there's
 * only one per session, so we keep track of the current instance in
 * a hash by session ID, and kill the old one before adding this one.
 *    (Actually, it's a double hash, by room ID and session ID. I thought
 *    it was possible to have two rooms active per session. That's not
 *    so clear, but the current code seems safe.)
 * The other way an instance can become invalid is if the session
 * goes away. So in userLeft we check whether the current session is
 * still alive, and if not, remove the instance.
 *   This bookkeeping is done in SetCurrentChannel and 
 * ResetCurrentChannel, so they should be the only code to call
 * AddRoomListener and RemoveRoomListener.
 *
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
 * to a specific tool. NB: each time enterTool is called, we are dealing
 * with a new instance.
 * 
 * The PresenceObserverHelper is placed on the current room.  This this tool is informed
 * when a user enters and exits the room.  
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
   
   private static final int DATETIME_DISPLAY_NONE = 0x00;
   private static final int DATETIME_DISPLAY_TIME = 0x01;
   private static final int DATETIME_DISPLAY_DATE = 0x02;
   private static final int DATETIME_DISPLAY_DATETIME = 0x03;
   private static final int DATETIME_DISPLAY_ID   = 0x04;
   
   
   private static final String PARAM_CHANNEL = "channel";
   private static final String PARAM_DAYS = "days";
   private static final String PARAM_ITEMS = "items";
   private static final String PARAM_LENGTH = "length";

   private static final int DEFAULT_DAYS = 10;
   private static final int DEFAULT_ITEMS = 3;
   private static final int DEFAULT_LENGTH = 50;
   
   private static final int CHAT_SESSION_TIMEOUT = 300*1000;
   
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
   
   /** display the time (1), date(2), both(3), neither(0), or uniqueid(4) */
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
   
   /** Double map by room ID and then Session ID
    ** Used to keep track of instances of this tool so
    ** we can find dead ones and remove them from the
    ** list of observers
    **/

   private static Map toolsBySessionId = new HashMap();
   private static Map<String,Long> timeouts = new HashMap();
   
   /**
    * This is called from the first page to redirect the user to the proper view.
    * This is the first call after JSF creates a new instance, so initialization is 
    *   done here.
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
      
      ChatChannel defaultChannel = getChatManager().getDefaultChannel(placement.getContext(), placement.getId());
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
    * @return List of String display names
    */
   public List<String> getUsersInCurrentChannel()
   {
      List<String> userList = new ArrayList<String>();
      if(!refreshPresence()) {
         return userList;
      }
      
      
      presenceChannelObserver.updatePresence();
      
      // put into context a list of sessions with chat presence
      String location = presenceChannelObserver.getLocation();

      // get the current presence list (User objects) for this page
      List<User> users = presenceChannelObserver.getPresentUsers();
      
   // is the current user running under an assumed (SU) user id?
      String asName = null;
      String myUserId = null;
      try
      {
         UsageSession usageSession = UsageSessionService.getSession();
         if (usageSession != null)
         {
            // this is the absolutely real end-user id, even if running as another user
            myUserId = usageSession.getUserId();

            // this is the user id the current user is running as
            String sessionUserId = SessionManager.getCurrentSessionUserId();

            // if different
            if (!myUserId.equals(sessionUserId))
            {
               asName = UserDirectoryService.getUser(sessionUserId).getDisplayName();
            }
         }
      }
      catch (Throwable any)
      {
      }
      
      for (Iterator<User> i = users.iterator(); i.hasNext();)
      {
         User u = (User) i.next();
         String displayName = u.getDisplayName();

         // adjust if this is the current user running as someone else
         if ((asName != null) && (u.getId().equals(myUserId)))
         {
            displayName += " (" + asName + ")";
         }

         userList.add(Web.escapeHtml(displayName));
      }
      
      
      return userList;
   }
   
   protected boolean refreshPresence() {

	  if(getCurrentChannel() != null) {
         return true;
      }
      return false;
   }
   

   //********************************************************************
   // Interface Implementations
   
   static void setTimeout(String address, Long timeout)
   {
       synchronized(timeouts) {
		   if (timeout != null)
		       timeouts.put(address, timeout);
		   else
		       timeouts.remove(address);
       }
   }

   /**
    * {@inheritDoc}
    * in the context of the event manager thread
    */
   public void receivedMessage(String roomId, Object message)
   {
      if(currentChannel != null && currentChannel.getChatChannel().getId().equals(roomId)) {
		  String address = sessionId + roomId;
		  Long timeout = timeouts.get(address);
		  if (SessionManager.getSession(sessionId) == null ||
		      (timeout != null && 
		       (timeout + CHAT_SESSION_TIMEOUT) < System.currentTimeMillis())) {
		      logger.debug("received msg expired session " + sessionId + " " + currentChannel);
		      resetCurrentChannel(currentChannel, true);
		      m_courierService.clear(address);
		      setTimeout(address, null);
		  } else {
		      m_courierService.deliver(new ChatDelivery(address, "Monitor", message, placementId, false, getChatManager()));
		      if (timeout == null)
			  setTimeout(address, System.currentTimeMillis());
		  }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void roomDeleted(String roomId)
   {
      if(currentChannel != null && currentChannel.getChatChannel().getId().equals(roomId)) {
		  resetCurrentChannel(currentChannel, true);
		  m_courierService.clear(sessionId+roomId);
		  setTimeout(sessionId+roomId, null);
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
    * In addition to refreshing the list, this code takes care of
    * removing instances associated with processes that are now dead.
    * Despite the user argument, the API doesn't tell us what user
    * left. So the best we can do is check ourself, and rely on the
    * fact that this will be called once for each user who has an
    * observer in the room observer list, so the user who left
    * should catch himself.
    */
   
   // new impl that counts the number of system in the location
   public void userLeft(String location, String user)
   {
       if (currentChannel != null && SessionManager.getSession(sessionId) == null) {
		   resetCurrentChannel(currentChannel, true);
		   m_courierService.clear(sessionId+location);
		   setTimeout(sessionId+location, null);
       }
       else
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
      channel.setPlacementDefaultChannel(true);
      getChatManager().makeDefaultContextChannel(channel, getToolManager().getCurrentPlacement().getId());
      return PAGE_LIST_ROOMS;
   }
   
   public String processActionAddRoom()
   {
      try {
         ChatChannel newChannel = getChatManager().createNewChannel(getContext(), "", false, true, getToolManager().getCurrentPlacement().getId());
         currentChannelEdit = new DecoratedChatChannel(this, newChannel, true);
         
         //init the filter param
         if (currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_ALL) ||
        		 currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER) ||
               currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME) ||
               currentChannelEdit.getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) {
            currentChannelEdit.setFilterParamLast(currentChannelEdit.getChatChannel().getNumberParam());
            currentChannelEdit.setFilterParamPast(currentChannelEdit.getChatChannel().getTimeParam());
            currentChannelEdit.setFilterParamNone(0);
         }
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
         message.setBody( FormattedText.convertPlaintextToFormattedText(newMessageText));
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
                  org.sakaiproject.util.Web.escapeUrl(getPermissionsMessage()) +
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
	   String filter = getCurrentChannel().getChatChannel().getFilterType();
	   if(filter.equals(ChatChannel.FILTER_ALL)){
		   setMessageOptions(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES));
	   }else if(filter.equals(ChatChannel.FILTER_BY_NUMBER)){
		   setMessageOptions(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_NUMBER));
	   }else if(filter.equals(ChatChannel.FILTER_BY_TIME)){
		   setMessageOptions(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_DATE));
	   }else if(filter.equals(ChatChannel.FILTER_NONE)){
		   setMessageOptions(Integer.toString(MESSAGEOPTIONS_NO_MESSAGES));
	   }
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
      chatChannel.setFilterParamNone(0);
      chatChannel.setFilterParamLast(chatChannel.getChatChannel().getNumberParam());
      chatChannel.setFilterParamPast(chatChannel.getChatChannel().getTimeParam());

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
      
      //set default number and time values based on the decordatedChannel class
      channel.setNumberParam(getCurrentChannelEdit().getFilterParamLast());
      channel.setTimeParam(getCurrentChannelEdit().getFilterParamPast());
      
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
            
            if (getCurrentChannel() != null && getCurrentChannel().getChatChannel().getId().equals(channel.getId())) {
               setCurrentChannel(new DecoratedChatChannel(this, channel));
            }
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
        DecoratedChatMessage msg = getCurrentMessage();
        if (msg != null)
               getChatManager().deleteMessage(msg.getChatMessage());
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

   /**
    * This allows us to change/give the courier address.
    * We want the courier to respond to the chat room.
    * @return String
    */
   public String getCourierPresenceString() {
      return "/courier/" + getToolString() + "-presence";
   }
   
   /**
    * Check for add/edit/del perms on the channel
    * @return
    */
   public boolean getCanManageTool()
   {
      boolean any = getCanCreateChannel() || 
         getCanEditChannel(null) || 
         getCanRemoveChannel(null);
      return any;
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

      // if changing to the same channel, nothing to do
      // this is a fairly expensive operation, so it's worth optimizing out

      if (this.currentChannel != null && channel != null &&
	   this.currentChannel.getChatChannel().getId().equals(
	   channel.getChatChannel().getId()))
    	  return;

      // turn off observation for the old channel
      if(presenceChannelObserver != null){
		  // need to save location, as we're about to clear current channel
		  String address = sessionId+currentChannel.getChatChannel().getId();
		  resetCurrentChannel(this.currentChannel, true);
		  m_courierService.clear(address);
		  setTimeout(address, null);
      }
      
      this.currentChannel = channel;
      
      // turn on observation for the new channel
      // the problem is that we have already have an observer
      // in this room for this session, because JSF tends to
      // generate new instances when you wouldn't expect it.
      // so look up the current channel and session in the
      // hash and remember the old instance if there was one
      // add the current instance to the hash in its place

     if (channel != null) {

		 String channelId = channel.getChatChannel().getId();
	
		 ChatTool oldTool = null;
	
		 synchronized(toolsBySessionId) {
		     Map tools = (Map)toolsBySessionId.get(channelId);
		     if (tools == null) {
				 // no entry for this chat room, make one
				 tools = new HashMap();
				 toolsBySessionId.put(channelId, tools);
		     } else {
				 // there is an entry for this chat room
				 // see if an instance for this session
				 oldTool = (ChatTool)tools.get(sessionId);
		     }
		     
		     // either way, there's now a hash for this
		     // chat room, so put this instance in it,
		     // replacing the old entry if there was one
		     tools.put(sessionId, this);
		 }
	
		 if (oldTool != null) {
		     // there was another instance for this session
		     // kill it. pass false, since we already handled
		     // the hash table.
		     oldTool.resetCurrentChannel(channel, false);
		 }
		 
		 // now do stuff for the new instance. It's already in the hash.
	     // place a presence observer on this tool.
		 presenceChannelObserver = new PresenceObserverHelper(this, channelId);
	         
		 // hmmmm.... should this all be under the synchronize?
         getChatManager().addRoomListener(this, channelId);
         
         presenceChannelObserver.updatePresence();
      }
   }

    // this removes the current channel but doesn't add a new one. sort of
    // half of setCurrentChannel.
    protected void resetCurrentChannel(DecoratedChatChannel oldChannel, Boolean removeFromHash) {
      String channelId = oldChannel.getChatChannel().getId();

      if(presenceChannelObserver != null) {
         presenceChannelObserver.endObservation();
         presenceChannelObserver.removePresence();
         getChatManager().removeRoomListener(this, channelId);
      }
      presenceChannelObserver = null;
      currentChannel = null;
      
      if (removeFromHash) {
		  synchronized(toolsBySessionId) {
		      Map tools = (Map)toolsBySessionId.get(channelId);
		      if (tools != null) {
			  tools.remove(sessionId);
			  if (tools.size() == 0)
			      toolsBySessionId.remove(tools);
		      }
		  }
      }
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
            if(message==null) return null;            
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
      int val = Integer.parseInt(getViewOptions());
      return ((val & DATETIME_DISPLAY_DATE) == DATETIME_DISPLAY_DATE);
   }
   
   public boolean getDisplayTime()
   {
      int val = Integer.parseInt(getViewOptions());
      return ((val & DATETIME_DISPLAY_TIME) == DATETIME_DISPLAY_TIME);
   }
   
   public boolean getDisplayId()
   {
      int val = Integer.parseInt(getViewOptions());
      return ((val & DATETIME_DISPLAY_ID) == DATETIME_DISPLAY_ID);
   }
   
   public boolean getCanRenderAllMessages() {
      return (!getCanRenderMessageOptions() &&
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_ALL)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_ALL_MESSAGES);
   }

   public boolean getCanRenderDateMessages() {
      return (!getCanRenderMessageOptions() && 
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_MESSAGES_BY_DATE);
   }
   
   public boolean getCanRenderNumberMessages() {
      return (!getCanRenderMessageOptions() && 
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_MESSAGES_BY_NUMBER);
   }
   
   public boolean getCanRenderNoMessages() {
      return (!getCanRenderMessageOptions() && 
         getCurrentChannel().getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_NO_MESSAGES);
   }
   
   public List getMessageOptionsList() {
	   List<SelectItem> messageOptions = new ArrayList<SelectItem>();
	   int numberParam = getCurrentChannel().getChatChannel().getNumberParam();
	   int timeParam = getCurrentChannel().getChatChannel().getTimeParam();
	   
	   if(getCanRenderMessageOptions()){

		   SelectItem item1 = new SelectItem(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES), getMessageFromBundle("allMessages"));
		   SelectItem item2 = new SelectItem(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_NUMBER), getCustomOptionText(ChatChannel.FILTER_BY_NUMBER, numberParam));
		   SelectItem item3 = new SelectItem(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_DATE), getCustomOptionText(ChatChannel.FILTER_BY_TIME, timeParam));
		   SelectItem item4 = new SelectItem(Integer.toString(MESSAGEOPTIONS_NO_MESSAGES), getCustomOptionText(ChatChannel.FILTER_NONE, 0));

		   messageOptions.add(item1);
		   messageOptions.add(item2);
		   messageOptions.add(item3);
		   messageOptions.add(item4);
	   }else{
		   String filter = getCurrentChannel().getChatChannel().getFilterType();
		   SelectItem item = null;
		   if(filter.equals(ChatChannel.FILTER_ALL)){
			   item = new SelectItem(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES), getMessageFromBundle("allMessages"));
		   }else if(filter.equals(ChatChannel.FILTER_BY_NUMBER)){
			   item = new SelectItem(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_NUMBER), getCustomOptionText(ChatChannel.FILTER_BY_NUMBER, numberParam));
		   }else if(filter.equals(ChatChannel.FILTER_BY_TIME)){
			   item = new SelectItem(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_DATE), getCustomOptionText(ChatChannel.FILTER_BY_TIME, timeParam));
		   }else if(filter.equals(ChatChannel.FILTER_NONE)){
			   item = new SelectItem(Integer.toString(MESSAGEOPTIONS_NO_MESSAGES), getCustomOptionText(ChatChannel.FILTER_NONE, 0));
		   }
		   messageOptions.add(item);
	   }
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
   
   protected int countChannelMessages(ChatChannel channel) {
      return getChatManager().countChannelMessages(channel);
   }
   
   public List getRoomMessages()
   {
      Date xDaysOld = null;
      int maxMessages = 0;

      if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_DATE) {
    	  int x = getCurrentChannel().getChatChannel().getTimeParam();
         xDaysOld = getChatManager().calculateDateByOffset(x);
         maxMessages = ChatChannel.MAX_MESSAGES;
      }
      else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_NUMBER) {
    	  int x = getCurrentChannel().getChatChannel().getNumberParam();
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
      if(getChatManager() == null){
    	  return null;
      }else{
    	  Date date = getChatManager().calculateDateByOffset(dso.getDays());
    	  return getMessages(getContext(), date, dso.getItems(), false);
      }
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
      return (getChatManager() == null) ? false : getChatManager().isMaintainer();
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
   
   protected String getAllMessagesText() {
	      return getMessageFromBundle("allMessages");
   }
   
   public String getViewingChatRoomText() {
      String title = null;
      if (getCurrentChannel() != null && getCurrentChannel().getChatChannel() != null)
      {
         title = getCurrentChannel().getChatChannel().getTitle();
      }
      return getMessageFromBundle("viewingChatRoomText", new Object[]{title});
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
      return getChatManager().getContextChannels(getContext(), getMessageFromBundle("default_new_channel_title"), getToolManager().getCurrentPlacement().getId());
   }
   
   
   /**
    * gets the number of channels in this site
    * @return int
    */
   public int getSiteChannelCount() {
      return getChatManager().getContextChannels(getContext(), getMessageFromBundle("default_new_channel_title"), getToolManager().getCurrentPlacement().getId()).size();
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
   
   public String getSessionId() {
      return sessionId;
   }

   public void validatePositiveNumber(FacesContext context, UIComponent component, Object value){
	    if (value != null)
	    {
	
		    if ((Integer) value < 0)
		    {
		      FacesMessage message = new FacesMessage(getMessageFromBundle("neg_num_error",null));
		      message.setSeverity(FacesMessage.SEVERITY_WARN);
		      throw new ValidatorException(message);
		    }
	
	    }
   }
   
}
