/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-util/util/src/java/org/sakaiproject/portal/util/PortalSiteHelper.java $
 * $Id: PortalSiteHelper.java 21708 2007-02-18 21:59:28Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 
package org.sakaiproject.chat2.tool;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatFunctions;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.DateFormatterUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

@Slf4j
public class ChatTool {
   /*  */
   private static final String IFRAME_ROOM_USERS = "Presence";
   
   /* various pages that we can go to within the tool */
   private static final String PAGE_EDIT_A_ROOM = "editRoom";
   private static final String PAGE_LIST_ROOMS = "listRooms";
   private static final String PAGE_ENTER_ROOM = "room";
   private static final String PAGE_EDIT_ROOM = "editRoom";
   private static final String PAGE_DELETE_ROOM_CONFIRM = "deleteRoomConfirm";
   private static final String PAGE_DELETE_ROOM_MESSAGES_CONFIRM = "deleteRoomMessagesConfirm";
   private static final String PAGE_DELETE_MESSAGE_CONFIRM = "deleteMessageConfirm";
   private static final String PAGE_SYNOPTIC = "synoptic";
   private static final String PAGE_SYNOPTIC_OPTIONS = "synopticOptions";
   
   private static final String PERMISSION_ERROR = "perm_error";
   
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
   
   private static final String HIDDEN_START_ISO_DATE = "chatStartDateISO8601";
   private static final String HIDDEN_END_ISO_DATE = "chatEndDateISO8601";
   
   /* All the managers */
   /**   The work-horse of chat   */
   private ChatManager chatManager;
   
   /**   The tool manager   */
   private ToolManager toolManager;
   
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
   
   /** The id of the placement of this sakai tool.  the jsf tool bean needs this for passing to the delivery  */
   private String placementId = "";
   
   /** the worksite the tool is in */
   private Site worksite = null;
   
   private String toolContext = null;
   
   // Used for fetching user's default language locale
   ResourceLoader rl = new ResourceLoader();
   
   protected void setupTool() {
      
      Placement placement = getToolManager().getCurrentPlacement();
      placementId = placement.getId();
      
      // Really only calling this just to make sure a room gets created
      getSiteChannels();
      
      ChatChannel defaultChannel = getChatManager().getDefaultChannel(placement.getContext(), placement.getId());
      setCurrentChannel(new DecoratedChatChannel(this, defaultChannel));
         
      return;
   }
   
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
      
	  setupTool();
	   
      // if there is no room selected to enter then go to select a room
      String url = PAGE_ENTER_ROOM;

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
   
   public int getPollInterval() {
	   return getChatManager().getPollInterval();
   }
   
   //********************************************************************
   // Tool Process Actions
   
   /**
    * resets various variables for the tool
    */
   private void clearToolVars()
   {
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

      if(channel.getFilterType().equals(ChatChannel.FILTER_BY_NUMBER) && (channel.getFilterParam() > chatManager.getMessagesMax() || channel.getFilterParam() < 1)){
    	  setErrorMessage("editRoomForm:filterParam_last", "show_last_range_error", new String[] {Integer.toString(1),Integer.toString(chatManager.getMessagesMax())});
    	  validates = false;
      }      
      
      if (log.isDebugEnabled()) log.debug("chat start ("+channel.getStartDate()+") and end ("+channel.getEndDate()+") dates");
      // validate the dates
      if (channel.getStartDate() != null && channel.getEndDate() != null) {
          // check the dates are valid
          if (channel.getStartDate().after(channel.getEndDate())) {
              setErrorMessage("editRoomForm:startDate", "custom_date_error_order", new Object[] {channel.getStartDate(), channel.getEndDate()});
              validates = false;
          }
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
   
   @SuppressWarnings("unchecked")
   public String processActionPermissions()
   {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      ToolSession toolSession = SessionManager.getCurrentToolSession();

      try {
          String url = "sakai.permissions.helper.helper/tool?" +
                  "session." + PermissionsHelper.DESCRIPTION + "=" +
                  org.sakaiproject.util.Web.escapeUrl(getPermissionsMessage()) +
                  "&session." + PermissionsHelper.TARGET_REF + "=" +
                  getWorksite().getReference() +
                  "&session." + PermissionsHelper.PREFIX + "=" +
                  getChatFunctionPrefix();

          // Set permission descriptions
          if (toolSession != null) {
        	  ResourceLoader pRb = new ResourceLoader("permissions");
        	  HashMap<String, String> pRbValues = new HashMap<String, String>();
        	  for (Iterator<Entry<String, String>> mapIter = pRb.entrySet().iterator();mapIter.hasNext();)
        	  {
        		  Entry<String, String> entry = mapIter.next();
        		  pRbValues.put(entry.getKey(), entry.getValue());
        	  }

        	  toolSession.setAttribute("permissionDescriptions", pRbValues); 
          }

          // Invoke Permissions helper
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
	   DecoratedChatChannel dChannel = getCurrentChannel();
	   String filter = dChannel != null ? dChannel.getChatChannel().getFilterType() : "";
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
   
   private String getFormattedDate(Date d){
	   ZonedDateTime ldt = ZonedDateTime.ofInstant(d.toInstant(), ZoneId.of(chatManager.getUserTimeZone()));
	   Locale locale = rl.getLocale();
	   DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
	   
	   return ldt.format(dtf);
   }

   /**
    * @return the translated message based on the current date settings for this channel
    */
   public String getDatesMessage() {
       String msg = null;
       if (this.currentChannel != null) {
           if (this.currentChannel.getStartDate() != null && this.currentChannel.getEndDate() != null) {
               msg = getMessageFromBundle("custom_date_display", 
                       new Object[] {getFormattedDate(this.currentChannel.getStartDate()), 
                    		   getFormattedDate(this.currentChannel.getEndDate())}
               );
           } else if (this.currentChannel.getStartDate() != null) {
               msg = getMessageFromBundle("custom_date_display_start", 
                       new Object[] {getFormattedDate(this.currentChannel.getStartDate()), ""}
               );
           } else if (this.currentChannel.getEndDate() != null) {
               msg = getMessageFromBundle("custom_date_display_end", 
                       new Object[] {"", getFormattedDate(this.currentChannel.getEndDate())}
               );
           }
       }
       return msg;
   }

   /**
    * @return true if chat posting is restricted by configured dates or false otherwise
    */
   public boolean isDatesRestricted() {
       boolean restricted = false;
       if (this.currentChannel != null) {
           Date today = new Date();
           Date start = this.currentChannel.getStartDate();
           if (start == null) {
               start = today;
           }
           Date end = this.currentChannel.getEndDate();
           if (end == null) {
               end = today;
           }
           if ( today.before(start) || today.after(end) ) {
               // today is outside the configured dates so posting restricted by dates
               restricted = true;
           }
       }
       return restricted;
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
       DecoratedChatChannel dChannel = getCurrentChannelEdit();
       if (dChannel != null)
       {
           ChatChannel channel = dChannel.getChatChannel();
           boolean directEdit = dChannel.isDirectEdit();     

           //set default number and time values based on the decordatedChannel class
           channel.setNumberParam(dChannel.getFilterParamLast());
           channel.setTimeParam(dChannel.getFilterParamPast());

           if (channel.getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) {
        	   channel.setFilterParam(dChannel.getFilterParamLast());
           }
           else if (channel.getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
               int pPast = dChannel.getFilterParamPast();
               if (pPast > 999) {
                   pPast = 999;
               } else if (pPast <= 0) {
                   pPast = 1;
               }
               channel.setFilterParam(pPast);
           }
           else if (channel.getFilterType().equals(ChatChannel.FILTER_NONE)) {
               channel.setFilterParam(0);
           }
           String retView = PAGE_LIST_ROOMS;

           if (directEdit)
               retView = PAGE_ENTER_ROOM;
           else
               retView = PAGE_LIST_ROOMS;

           // If the hidden values contain valid ISO dates set them
           Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
           String startISODate = params.get(HIDDEN_START_ISO_DATE);
           String endISODate = params.get(HIDDEN_END_ISO_DATE);
           if(StringUtils.isEmpty(startISODate) || DateFormatterUtil.isValidISODate(startISODate)){
                channel.setStartDate(DateFormatterUtil.parseISODate(startISODate));
           }

           if(StringUtils.isEmpty(endISODate) || DateFormatterUtil.isValidISODate(endISODate)){
                channel.setEndDate(DateFormatterUtil.parseISODate(endISODate));
           }

           if (validateChannel(channel))
               try {
                   getChatManager().updateChannel(channel, true);

                   if (dChannel != null && dChannel.getChatChannel().getId().equals(channel.getId())) {
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
	  else
	  {
		  return "";
	  }
   }
   
   /**
    * Sets the current room to null
    * @return String goes to the edit room view
    */
   public String processActionEditRoomCancel()
   {
	  DecoratedChatChannel dChannel = getCurrentChannelEdit();
      boolean directEdit = dChannel != null ? dChannel.isDirectEdit() : false;
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
   
   public void setCurrentChatChannelId(String channelId) {
       ChatChannel newChannel = getChatManager().getChatChannel(channelId);
       if (newChannel != null)
	   setCurrentChannel(new DecoratedChatChannel(this, newChannel));
   }

   /**
    * gets the current channel
    * @return ChatChannel
    */
   public DecoratedChatChannel getCurrentChannel()
   {
	   if (currentChannel == null) {
		   // reset to the default channel
		   setupTool();
	   }
	   
	   return currentChannel;
   }
   
   /**
    * Implements a change of the chat room.
    * @param channel
    */
   public void setCurrentChannel(DecoratedChatChannel channel)
   {

      // if changing to the same channel, nothing to do
      // this is a fairly expensive operation, so it's worth optimizing out

      if (this.currentChannel != null && channel != null &&
	   this.currentChannel.getChatChannel().getId().equals(
	       channel.getChatChannel().getId())) {
    	  return;
      }
      
      this.currentChannel = channel;
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
            tmpCurrent = new DecoratedChatMessage(this, message, chatManager);
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
         log.debug("Can't get tool property for synoptic chat.  Using default option");
      }
      try {
         dso.setItems(Integer.parseInt(placement.getPlacementConfig().getProperty(PARAM_ITEMS)));
      } catch (NumberFormatException e) {
         dso.setItems(DEFAULT_ITEMS);
         log.debug("Can't get tool property for synoptic chat.  Using default option");
      }
      try {
         dso.setChars(Integer.parseInt(placement.getPlacementConfig().getProperty(PARAM_LENGTH)));
      } catch (NumberFormatException e) {
         dso.setChars(DEFAULT_LENGTH);
         log.debug("Can't get tool property for synoptic chat.  Using default option");
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
   public List<SelectItem> getChatRoomsSelectItems()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      
      for (ChatChannel channel : getSiteChannels()) {
         items.add(createSelect(channel.getId(), channel.getTitle()));
      }
      
      return items;
   }
   
   
   /**
    * gets the tool decorated channels 
    * @return
    */
   public List<DecoratedChatChannel> getChatChannels()
   {
      List<DecoratedChatChannel> items = new ArrayList<DecoratedChatChannel>();
      
      for (ChatChannel channel : getSiteChannels()) {
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
      DecoratedChatChannel dChannel = getCurrentChannel();
      if (dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_ALL)) {
         result = MESSAGEOPTIONS_ALL_MESSAGES;
      }
      else if (dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) {
         result = MESSAGEOPTIONS_MESSAGES_BY_NUMBER;
      }
      else if (dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
         result = MESSAGEOPTIONS_MESSAGES_BY_DATE;
      }
      else if (dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) {
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
	   DecoratedChatChannel dChannel = getCurrentChannel();
	   return (!getCanRenderMessageOptions() &&
         dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_ALL)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_ALL_MESSAGES);
   }

   public boolean getCanRenderDateMessages() {
	  DecoratedChatChannel dChannel = getCurrentChannel();
      return (!getCanRenderMessageOptions() && 
    	 dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_TIME)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_MESSAGES_BY_DATE);
   }
   
   public boolean getCanRenderNumberMessages() {
	  DecoratedChatChannel dChannel = getCurrentChannel();
      return (!getCanRenderMessageOptions() && 
         dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_MESSAGES_BY_NUMBER);
   }
   
   public boolean getCanRenderNoMessages() {
	  DecoratedChatChannel dChannel = getCurrentChannel();
      return (!getCanRenderMessageOptions() && 
         dChannel != null && dChannel.getChatChannel().getFilterType().equals(ChatChannel.FILTER_NONE)) ||
         (getCanRenderMessageOptions() && messageOptions == MESSAGEOPTIONS_NO_MESSAGES);
   }
   
   public List<SelectItem> getMessageOptionsList() {
	   List<SelectItem> messageOptions = new ArrayList<SelectItem>();
	   DecoratedChatChannel dChannel = getCurrentChannel();
	   int numberParam = dChannel != null ? dChannel.getChatChannel().getNumberParam() : 0;
	   int timeParam = dChannel != null ? dChannel.getChatChannel().getTimeParam() : 0;
	   
	   if(getCanRenderMessageOptions()){

		   // Commenting out the original here for now in case this is ever truly implemented.  Instead, display the true maximum limit
		   // SelectItem item1 = new SelectItem(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES), getMessageFromBundle("allMessages"));
		   SelectItem item1 = new SelectItem(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES), getCustomOptionText(ChatChannel.FILTER_BY_NUMBER, getChatManager().getMessagesMax()));
		   SelectItem item2 = new SelectItem(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_NUMBER), getCustomOptionText(ChatChannel.FILTER_BY_NUMBER, numberParam));
		   SelectItem item3 = new SelectItem(Integer.toString(MESSAGEOPTIONS_MESSAGES_BY_DATE), getCustomOptionText(ChatChannel.FILTER_BY_TIME, timeParam));
		   SelectItem item4 = new SelectItem(Integer.toString(MESSAGEOPTIONS_NO_MESSAGES), getCustomOptionText(ChatChannel.FILTER_NONE, 0));

		   messageOptions.add(item1);
		   messageOptions.add(item2);
		   messageOptions.add(item3);
		   messageOptions.add(item4);
	   }else{
		   String filter = dChannel != null ? dChannel.getChatChannel().getFilterType() : "";
		   SelectItem item = null;
		   if(filter.equals(ChatChannel.FILTER_ALL)){
			   // Commenting out the original here for now in case this is ever truly implemented.  Instead, display the true maximum limit
			   // item = new SelectItem(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES), getMessageFromBundle("allMessages"));
			   item = new SelectItem(Integer.toString(MESSAGEOPTIONS_ALL_MESSAGES), getCustomOptionText(ChatChannel.FILTER_BY_NUMBER, getChatManager().getMessagesMax()));
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
	  DecoratedChatChannel dChannel = getCurrentChannel();
      return dChannel != null? dChannel.getChatChannel().isEnableUserOverride() : false;
   }
   
   protected String getCustomOptionValue(String filterType) {
      int val = MESSAGEOPTIONS_MESSAGES_BY_DATE;
      if (filterType.equals(ChatChannel.FILTER_BY_TIME)) {
         val = MESSAGEOPTIONS_MESSAGES_BY_DATE;
      }
      else if (filterType.equals(ChatChannel.FILTER_BY_NUMBER)) {
         val = MESSAGEOPTIONS_MESSAGES_BY_NUMBER;
      }
      else if (filterType.equals(ChatChannel.FILTER_NONE)) {
         val = MESSAGEOPTIONS_NO_MESSAGES;
      }
      return Integer.toString(val);
   }
   
   protected String getCustomOptionText(String filterType, int filterParam) {
      String result = getPastXDaysText(filterParam);
      
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

   public List<DecoratedChatMessage> getRoomMessages() {
       // NOTE: make sure this uses the same method to find the date as #getRoomMessagesCount() below
       Date xDaysOld = null;
       int maxMessages = 0;
       DecoratedChatChannel dChannel = getCurrentChannel();

       if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_DATE) {
           int x = dChannel != null? dChannel.getChatChannel().getTimeParam():0;
           xDaysOld = getChatManager().calculateDateByOffset(x);
           maxMessages = getChatManager().getMessagesMax();
       }
       else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_NUMBER) {
           int x = dChannel != null?dChannel.getChatChannel().getNumberParam():0;
           maxMessages = x;
       }
       else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_ALL_MESSAGES) {
           maxMessages = getChatManager().getMessagesMax();
       }
       else if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_NO_MESSAGES) {
           maxMessages = 0;
       }
       EventTrackingService ets = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
       if (ets != null && dChannel != null && dChannel.getChatChannel() != null) {
           ets.post(ets.newEvent("chat.read", dChannel.getChatChannel().getReference(), false));
       }
       return getMessages(getContext(), xDaysOld, maxMessages, true);
   }

   /**
    * Finds the current messages count for the current room
    * @return the count of the total messages for this chatroom
    */
   public int getRoomMessagesCount() {
       // NOTE: make sure this uses the same method to find the date as #getRoomMessages() above
       Date xDaysOld = null;
       DecoratedChatChannel dChannel = getCurrentChannel();

       if (Integer.parseInt(getMessageOptions()) == MESSAGEOPTIONS_MESSAGES_BY_DATE) {
           int x = dChannel != null? dChannel.getChatChannel().getTimeParam():0;
           xDaysOld = getChatManager().calculateDateByOffset(x);
       }
       return getMessagesCount(getContext(), xDaysOld);
   }

   public List<DecoratedChatMessage> getSynopticMessages()
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
    * @return the set of all messages in this channel, limited to a max of 100 for paging
    */
   protected List<DecoratedChatMessage> getMessages(String context, Date limitDate, int numMessages, boolean sortAsc)
   {
      if (numMessages > getChatManager().getMessagesMax()) {
          numMessages = getChatManager().getMessagesMax();
      }
      List<ChatMessage> messages = new ArrayList<ChatMessage>();
      try {
         ChatChannel channel = (currentChannel==null) ? null : currentChannel.getChatChannel();
         messages = getChatManager().getChannelMessages(channel, context, limitDate, 0, numMessages, sortAsc);
      }
      catch (PermissionException e) {
         setErrorMessage(PERMISSION_ERROR, new String[] {ChatFunctions.CHAT_FUNCTION_READ});
      }
      
      List<DecoratedChatMessage> decoratedMessages = new ArrayList<DecoratedChatMessage>();
      
      for (ChatMessage message : messages) {         
         DecoratedChatMessage decoratedMessage = new DecoratedChatMessage(this, message, chatManager);
         decoratedMessages.add(decoratedMessage);
      }
      return decoratedMessages;
   }

   /**
    * @return the number of items in this channel, mostly for paging
    */
   protected int getMessagesCount(String context, Date limitDate) {
       ChatChannel channel = (currentChannel==null) ? null : currentChannel.getChatChannel();
       return getChatManager().getChannelMessagesCount(channel, context, limitDate);
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
      return getChatManager().getCanDeleteAnyMessage(channel.getContext());
   }
   
   public boolean getCanEditChannel(ChatChannel channel)
   {
      return getChatManager().getCanEdit(channel);
   }
   
   public boolean getCanCreateChannel()
   {
      return getChatManager().getCanCreateChannel(getContext());
   }

   public boolean getCanRead(ChatChannel channel)
   {
      return getChatManager().getCanReadMessage(channel);
   }
   
   public boolean getCanPost()
   {
      return (getCurrentChannel() == null) ? false : getChatManager().getCanPostMessage(getCurrentChannel().getChatChannel());
   }
   
   public boolean getMaintainer()
   {
      return (getChatManager() == null) ? false : getChatManager().isMaintainer(getContext());
   }
   
   public String getMessageOwnerDisplayName(ChatMessage message)
   {
      User sender = null;
      try {
         sender = UserDirectoryService.getUser(message.getOwner());
      } catch(UserNotDefinedException e) {
          // Expected some users will get deleted.
         log.debug("Failed to find user for ID: "+ message.getOwner(), e);
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

   public String getMessagesShownTotalText() {
       return getMessageFromBundle("messages_shown_total", new Object[]{"*SHOWN*","*TOTAL*"});
   }

   public String getUnreadedMessagesText() {
       return getMessageFromBundle("messages_unreaded", new Object[]{"*UNREADED*"});
   }

   public String getViewingChatRoomText() {
      String title = null;
      DecoratedChatChannel dChannel = getCurrentChannel();
      if (dChannel != null && dChannel.getChatChannel() != null)
      {
         title = dChannel.getChatChannel().getTitle();
      }
      return getMessageFromBundle("viewingChatRoomText", new Object[]{title});
   }
   
   private void setErrorMessage(String errorMsg, Object[] extras)
   {
      setErrorMessage(null, errorMsg, extras);
   }
   
   private void setErrorMessage(String field, String errorMsg, Object[] extras)
   {
     log.debug("setErrorMessage(String " + errorMsg + ")");
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
   protected List<ChatChannel> getSiteChannels() {
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

   public String getMessageFromBundle(String key) {
      if (toolBundle == null) {
         String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
         toolBundle = new ResourceLoader(bundle);
      }
      return toolBundle.getString(key);
   }

   public String getToolContext() {
      return toolContext;
   }

   public void setToolContext(String toolContext) {
      this.toolContext = toolContext;
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

   //SAK-19700 method to get name of tool so it can be rendered with the option link, for screenreaders
   public String getToolTitle() {
	   return toolManager.getCurrentPlacement().getTitle();
   }
   
   //SAK-19700 renders a complete Options link with an additional span link for accessiblity
   public String getAccessibleOptionsLink() {
	   StringBuilder sb = new StringBuilder();
	   sb.append(getMessageFromBundle("manage_tool"));
	   sb.append("<span class=\"skip\">");
	   sb.append(getToolTitle());
	   sb.append("</span>");
	   return sb.toString();
   }
   
}
