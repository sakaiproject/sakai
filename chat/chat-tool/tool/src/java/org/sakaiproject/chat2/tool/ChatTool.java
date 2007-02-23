package org.sakaiproject.chat2.tool;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

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
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.DirectRefreshDelivery;
import org.sakaiproject.util.PresenceObservingCourier;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;

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
 * TODO:
 *    permissions
 *    manage tool
 *       manage rooms
 *          add
 *          delete
 *       initial view setting
 *    export room (archive)
 *    import room (merge)
 *    write message
 *    filter messages
 * 
 * @author andersjb
 *
 */
public class ChatTool implements RoomObserver, PresenceObserver {

   /** Our logger. */
   private static Log logger = LogFactory.getLog(ChatTool.class);
   
   /*  */
   private String IFRAME_ROOM_USERS = "Presence";
   
   /* various pages that we can go to within the tool */
   private String PAGE_SELECT_A_ROOM = "selectRoom";
   private String PAGE_EDIT_A_ROOM = "editRoom";
   private String PAGE_MANAGE_TOOL = "toolOptions";
   private String PAGE_ENTER_ROOM = "room";
   private String PAGE_ROOM_USERS = "roomUsers";
   private String PAGE_ROOM_MONITOR = "roomMonitor";
   private String PAGE_ROOM_CONTROL = "roomControl";
   private String PAGE_ROOM_ACTIONS = "roomActions";
   private String PAGE_EDIT_ROOM = "editRoom";
   private String PAGE_DELETE_ROOM_CONFIRM = "deleteRoomConfirm";
   private String PAGE_DELETE_MESSAGE_CONFIRM = "deleteMessagesConfirm";
   
   
   private String PRESENCE_PREPEND = "chat_room_";
   
   /* All the managers */
   /**   The work-horse of chat   */
   private ChatManager chatManager;
   
   /**   The tool manager   */
   private ToolManager toolManager;
   
   /**   The id manager   */
   private IdManager idManager;
   
   /** Constructor discovered injected CourierService. */
   protected CourierService m_courierService = null;
   
   
   /* All the private variables */
   /** The current channel the user is in */
   private ChatChannel currentChannel = null;
   
   /** The next Channel to move into */
   private String nextChatChannel = null;
   
   /** The location where the new message text goes */
   private String newMessageText = "";
   
   /** display the time (1), date(2), both(3), or neither(0) */
   private int viewOptions = 1;
   
   /** The id of the session. needed for adding messages to the courier because that runs in the notification thread */
   private String sessionId = "";
   
   /** The id of the placement of this sakai tool.  the jsf tool bean needs this for passing to the delivery  */
   private String placementId = "";
   
   /** Mapping the color of each message */
   private ColorMapper colorMapper = new ColorMapper();
   
   /** the worksite the tool is in */
   private Site worksite = null;
   
   
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
      
      String initialView = placement.getPlacementConfig().getProperty(ChatManager.TOOL_INITIAL_VIEW_SETTING);
      
      if(initialView == null || initialView.equals("") || initialView.equals(ChatManager.INITIAL_VIEW_SELECT_ROOM)) {
         if(initialView == null || initialView.equals(""))
            placement.getPlacementConfig().setProperty(ChatManager.TOOL_INITIAL_VIEW_SETTING, ChatManager.INITIAL_VIEW_SELECT_ROOM);

         List rooms = getToolChannels();
         
         if(rooms.size() == 1)
            setCurrentChannel((ChatChannel)rooms.get(0));
         
      } else {
         setCurrentChannel(getChatManager().getChatChannel(idManager.getId(initialView)));
      }
      
      // if there is no room selected to enter then go to select a room
      if(currentChannel == null)
         url = PAGE_SELECT_A_ROOM;
         
      
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      
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
      if(presenceChannelObserver == null)
         return new ArrayList();
      
      presenceChannelObserver.updatePresence();
      
      // put into context a list of sessions with chat presence
      String location = presenceChannelObserver.getLocation();

      // get the current presence list (User objects) for this page
      List users = presenceChannelObserver.getPresentUsers();
      
      return users;
   }
   

   //********************************************************************
   // Interface Implementations
   
   /**
    * {@inheritDoc}
    * in the context of the event manager thread
    */
   public void receivedMessage(String roomId, Object message)
   {
      if(currentChannel != null && currentChannel.getId().getValue().equals(roomId)) {
         m_courierService.deliver(new ChatDelivery(sessionId+roomId, "Monitor", message, placementId, false, getChatManager()));
      }
   }

   /**
    * {@inheritDoc}
    */
   public void roomDeleted(String roomId)
   {
      if(currentChannel != null && currentChannel.getId().getValue().equals(roomId)) {
         setCurrentChannel(null);
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
      if(presenceChannelObserver.getPresentUsers().size() == 0) {
         presenceChannelObserver.endObservation();
         getChatManager().removeRoomListener(this, currentChannel.getId().getValue());
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
    * When the user selects a new room to go into
    * @return String next page
    */
   public String processActionChangeChannel()
   {
      clearToolVars();
      
      ChatChannel nextChannel = null;
      for(Iterator i = getToolChannels().iterator(); i.hasNext(); ) {
         ChatChannel channel = (ChatChannel)i.next();
         if(channel.getId().getValue().equals(nextChatChannel)) {
            nextChannel = channel;
            break;
         }
      }
      if(nextChannel == null) {
         selectedRoomNotAvailable = true;
         return PAGE_SELECT_A_ROOM;
      }
      setCurrentChannel(nextChannel);
      
      return PAGE_ENTER_ROOM;
      
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
   
   public String processActionAddRoom()
   {
      currentChannel = getChatManager().createNewChannel(getToolString(), "");
      return "";
   }
   
   public String processActionSubmitMessage()
   {
      ChatMessage message = getChatManager().createNewMessage(
            getCurrentChannel(), SessionManager.getCurrentSessionUserId());
      message.setBody(newMessageText);
      newMessageText = "";
      getChatManager().updateMessage(message);
      getChatManager().sendMessage(message);
      return PAGE_ROOM_CONTROL;
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
   public String processActionEnterRoom(ChatChannel chatChannel)
   {
      setCurrentChannel(chatChannel);
      return PAGE_ENTER_ROOM;
   }
   
   /**
    * Sets the current room and goes to edit that room
    * @param chatChannel
    * @return String goes to the edit room view
    */
   public String processActionEditRoom(ChatChannel chatChannel)
   {
      setCurrentChannel(chatChannel);
      return PAGE_EDIT_ROOM;
   }
   
   /**
    * Sets the current room and goes to confirm deleting the room
    * @param chatChannel
    * @return String goes to the delete room confirmation page
    */
   public String processActionDeleteRoomConfirm(ChatChannel chatChannel)
   {
      setCurrentChannel(chatChannel);
      return "deleteRoomConfirm";
   }
   
   
   /**
    * deletes the current room and all it's messages
    * @return String goes to the select a room page
    */
   public String processActionDeleteRoom()
   {
      getChatManager().deleteChannel(currentChannel);
      setCurrentChannel(null);
      return PAGE_SELECT_A_ROOM;
   }
   
   
   /**
    * cancels the deletion of a room via the confirmation
    * @return String goes to the select a room page
    */
   public String processActionDeleteRoomCancel()
   {
      setCurrentChannel(null);
      return PAGE_MANAGE_TOOL;
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
      return true;
   }
   
   
   /**
    * gets the channel id of the current channel.  If there isn't one then 
    * give a blank string
    * @return ChatManager
    */
   public String getCurrentChatChannelId() {
      if(currentChannel == null)
         return "";
      return currentChannel.getId().getValue();
   }
   
   /**
    * sets the new channel.  it gets stored until the user clicks processActionChangeChannel
    * @param newChatChannelId
    */
   public void setCurrentChatId(String newChatChannelId) {
      this.nextChatChannel = newChatChannelId;
   }
   
   /**
    * gets the current channel
    * @return ChatChannel
    */
   public ChatChannel getCurrentChannel()
   {
      return currentChannel;
   }
   
   /**
    * Implements a change of the chat room.  It removes presense from the prior room,
    *  adds observation of the new room, and then becomes present in the new room
    * @param channel
    */
   public void setCurrentChannel(ChatChannel channel)
   {
      if(presenceChannelObserver != null) {
         presenceChannelObserver.removePresence();
         presenceChannelObserver.endObservation();
         getChatManager().removeRoomListener(this, channel.getId().getValue());
      }
      presenceChannelObserver = null;
      
      this.currentChannel = channel;

      if(channel != null) {
         // place a presence observer on this tool.
         presenceChannelObserver = new PresenceObserverHelper(this,
                  channel.getId().getValue());
         
         getChatManager().addRoomListener(this, channel.getId().getValue());
         
         presenceChannelObserver.updatePresence();
      }
   }
   
   /**
    * This creates select items out of the channels available to the tool
    * @return List of SelectItem
    */
   public List getChotRoomsSelectItems()
   {
      List items = new ArrayList();
      
      for(Iterator i = getToolChannels().iterator(); i.hasNext(); ) {
         ChatChannel channel = (ChatChannel)i.next();
         items.add(createSelect(channel.getId().getValue(), channel.getTitle()));
      }
      
      return items;
   }
   
   
   /**
    * gets the tool decorated channels 
    * @return
    */
   public List getChatChannels()
   {
      List items = new ArrayList();
      
      for(Iterator i = getToolChannels().iterator(); i.hasNext(); ) {
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
   
   public String getMsgDateCutoff() {
      return "";
   }
   public void setMsgDateCutoff(String d) {
      
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

   public IdManager getIdManager() {
      return idManager;
   }

   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
   }

   public ColorMapper getColorMapper() {
      return colorMapper;
   }

   public void setColorMapper(ColorMapper colorMapper) {
      this.colorMapper = colorMapper;
   }
   
   public boolean getDisplayDate()
   {
      return true;
   }
   
   public boolean getDisplayTime()
   {
      return true;
   }
   
   public boolean getSoundAlert()
   {
      return true;
   }
   public List getRoomMessages()
   {
      List messages = getChatManager().getChannelMessages(currentChannel);
      
      List decoratedMessages = new ArrayList();
      
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
   
   public boolean getMaintainer()
   {
      return getChatManager().isMaintaner();
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
   /*
    * 
    * 
    * 
      String skin = null;
      try {
         skin = getCurrentSite().getSkin();
      }
      catch (NullPointerException npe) {
         //Couldn't find the site, just use default skin
      }
      if (skin == null || skin.length() == 0) {
         skin = ServerConfigurationService.getString("skin.default");
      }
      String skinRepo = ServerConfigurationService.getString("skin.repo");
      Element uri = new Element("uri");
      uri.setText(skinRepo + "/tool_base.css");
      css.addContent(uri);
      uri = new Element("uri");
      uri.setText(skinRepo + "/" + skin + "/tool.css");
    */

   
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
    * Gets the id of the tool we are in
    * @return Id
    */
   protected Id getToolId() {
      return getIdManager().getId(getToolString());
   }
   
   
   /**
    * gets the channels in this tool
    * @return List of ChatChannel
    */
   protected List getToolChannels() {
      return getChatManager().getContextChannels(getToolString(), getMessageFromBundle("default_new_channel_title"));
   }
   
   
   /**
    * gets the number of channels in this tool
    * @return int
    */
   public int getToolChannelCount() {
      return getChatManager().getContextChannels(getToolString(), getMessageFromBundle("default_new_channel_title")).size();
   }
   
   
   
   //********************************************************************
   // Common Utilities
   
   private ResourceLoader toolBundle;

   public Object createSelect(Object id, String description) {
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
   
   
}
