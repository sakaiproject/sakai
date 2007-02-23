package org.sakaiproject.chat2.model.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.RoomObserver;
import org.sakaiproject.chat2.model.ChatFunctions;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.metaobj.security.AuthorizationFacade;
import org.sakaiproject.metaobj.security.model.AuthZMap;
import org.sakaiproject.metaobj.shared.mgt.IdManager;

import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * 
 * @author andersjb
 *
 */
public class ChatManagerImpl extends HibernateDaoSupport implements ChatManager, Observer {

   protected final transient Log logger = LogFactory.getLog(getClass());
   
   /** the key for sending message events around */
   protected final static String EVENT_CHAT_SEND_MESSAGE = "sakai.chat.message";
   
   protected final static String EVENT_CHAT_DELETE_CHANNEL = "sakai.chat.delete.channel";
   
   /**   The id manager   */
   private IdManager idManager = null;
   
   /**   The tool manager   */
   private ToolManager toolManager;

   /**
    * the sakai class that manages permissions
    */
   private AuthorizationFacade authzManager = null;
   
   /** the clients listening to the various rooms */
   protected Map roomListeners = new HashMap();
   
   
   
   /**
    * Called on after the startup of the singleton.  This sets the global
    * list of functions which will have permission managed by sakai
    * @throws Exception
    */
   protected void init() throws Exception
   {
      logger.info("init()");
      
      //
      EventTrackingService.addObserver(this);
      
      // register functions
      if(FunctionManager.getRegisteredFunctions(ChatFunctions.CHAT_FUNCTION_PREFIX).size() == 0) {
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_READ);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_NEW);
         //FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_MANAGE_TOOL);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_ANY);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_OWN);
      }
   }


   /**
    * {@inheritDoc}
    */
   public ChatChannel createNewChannel(String context, String title) {

      checkAdminTool();
      
      ChatChannel channel = new ChatChannel();
      
      channel.setCreationDate(new Date());
      channel.setContext(context);
      channel.setTitle(title);
      
      return channel;
   }


   /**
    * {@inheritDoc}
    */
   public void updateChannel(ChatChannel channel) {

      checkAdminTool();
      
      getHibernateTemplate().saveOrUpdate(channel);
   }


   /**
    * {@inheritDoc}
    */
   public void deleteChannel(ChatChannel channel) {

      checkAdminTool();
      
      List messages = getHibernateTemplate().findByNamedQuery("getAllChannelMessages", channel);
      
      for(Iterator i = messages.iterator(); i.hasNext(); ) {
         ChatMessage message = (ChatMessage)i.next();
         
         getHibernateTemplate().delete(message);
      }
      getHibernateTemplate().delete(channel);
      
      sendDeleteChannel(channel);
   }


   /**
    * {@inheritDoc}
    */
   public ChatChannel getChatChannel(Id chatChannelId) {
      return (ChatChannel)getHibernateTemplate().get(
                              ChatChannel.class, chatChannelId);
   }


   /**
    * {@inheritDoc}
    */
   public List getChannelMessages(ChatChannel channel)
   {
      checkPermission(ChatFunctions.CHAT_FUNCTION_READ);
      
      return getHibernateTemplate().findByNamedQuery("getAllChannelMessages", channel);
   }


   /**
    * {@inheritDoc}
    */
   public ChatMessage createNewMessage(ChatChannel channel, String owner) {
      
      checkPermission(ChatFunctions.CHAT_FUNCTION_NEW);
      
      ChatMessage message = new ChatMessage();
      
      message.setChatChannel(channel);
      message.setDraft(false);
      message.setPubView(true);
      message.setOwner(owner);
      message.setMessageDate(new Date());
      
      return message;
   }
   /**
    * saves a Chat Message
    * @param ChatMessage the message to update
    */
   public void updateMessage(ChatMessage message)
   {
      getHibernateTemplate().saveOrUpdate(message);
   }
   
   /**
    * tells us if the message can be deleted by the current session user
    */
    public boolean getCanDelete(ChatMessage message, String placementId) {
       boolean canDeleteAny = can(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, placementId);
       boolean canDeleteOwn = can(ChatFunctions.CHAT_FUNCTION_DELETE_OWN, placementId);
       boolean isOwner = SessionManager.getCurrentSessionUserId().equals(message.getOwner());
       
       boolean canDelete = canDeleteAny;
       
       if(canDeleteOwn && isOwner)
          canDelete = true;
       
       return canDelete;
    }
    public boolean getCanDelete(ChatMessage message)
   {
      return getCanDelete(message, toolManager.getCurrentPlacement().getId());
   }
   
   /**
    * delete a Chat Message
    * @param ChatMessage the message to delete
    */
   public void deleteMessage(ChatMessage message)
   {
      if(!getCanDelete(message))
         checkPermission(ChatFunctions.CHAT_FUNCTION_DELETE_ANY);
      
      getHibernateTemplate().delete(message);
   }


   /**
    * {@inheritDoc}
    */
   public ChatMessage getMessage(Id chatMessageId) {
      return (ChatMessage)getHibernateTemplate().get(
                              ChatMessage.class, chatMessageId);
   }
   public ChatMessage getMessage(String chatMessageId) {
      return (ChatMessage)getMessage(getIdManager().getId(chatMessageId));
   }


   /**
    * {@inheritDoc}
    */
   public List getContextChannels(String context, String defaultNewTitle) {
      List channels = getHibernateTemplate().findByNamedQuery("findChannelsInContext", context);
      
      if(channels.size() == 0) {
         ChatChannel channel = createNewChannel(context, defaultNewTitle);
         getHibernateTemplate().save(channel);
         channels.add(channel);
      }
      
      return channels;
   }
   
   

   
   public void addRoomListener(RoomObserver observer, String roomId)
   {
      List roomObservers;
      synchronized(roomListeners) {
         if(roomListeners.get(roomId) == null)
            roomListeners.put(roomId, new ArrayList());
         roomObservers = (List)roomListeners.get(roomId);
      }
      synchronized(roomObservers) {
         roomObservers.add(observer);
      }
   }
   
   public void removeRoomListener(RoomObserver observer, String roomId)
   {
      
      if(roomListeners.get(roomId) != null) {
         List roomObservers = (List)roomListeners.get(roomId);
         
         if(roomObservers != null) {
            synchronized(roomObservers) {
               
               roomObservers.remove(observer);
               if(roomObservers.size() == 0) {
                  
                  synchronized(roomListeners) {
                     roomListeners.remove(roomId);
                  }
                  
               }
               
            }
            
         } // end if(roomObservers != null)
      }
   }
   
   
   
   


   /**
    * {@inheritDoc}
    */
   public void sendMessage(ChatMessage message) {
      ChatMessageTxSync txSync = new ChatMessageTxSync(message);

      if (TransactionSynchronizationManager.isSynchronizationActive()) {
         TransactionSynchronizationManager.registerSynchronization(txSync);
      }
      else {
         txSync.afterCompletion(ChatMessageTxSync.STATUS_COMMITTED);
      }
   }
   
   
   
   


   /**
    * {@inheritDoc}
    */
   public void sendDeleteChannel(ChatChannel channel) {
      ChatChannelDeleteTxSync txSync = new ChatChannelDeleteTxSync(channel);

      if (TransactionSynchronizationManager.isSynchronizationActive()) {
         TransactionSynchronizationManager.registerSynchronization(txSync);
      }
      else {
         txSync.afterCompletion(ChatChannelDeleteTxSync.STATUS_COMMITTED);
      }
   }

   /**
    * This helps to send out the message when the record is placed in the database
    * @author andersjb
    *
    */
   private class ChatMessageTxSync extends TransactionSynchronizationAdapter {
      private ChatMessage message;

      public ChatMessageTxSync(ChatMessage message) {
         this.message = message;
      }

      public void afterCompletion(int status) {
         Event event = null;
         event = EventTrackingService.newEvent(EVENT_CHAT_SEND_MESSAGE, 
                  message.getChatChannel().getId().getValue() + ":" + message.getId().getValue(), false);

         if (event != null)
            EventTrackingService.post(event);
      }
   }

   /**
    * This helps to send out the message when the record is placed in the database
    * @author andersjb
    *
    */
   private class ChatChannelDeleteTxSync extends TransactionSynchronizationAdapter {
      private ChatChannel channel;

      public ChatChannelDeleteTxSync(ChatChannel channel) {
         this.channel = channel;
      }

      public void afterCompletion(int status) {
         Event event = null;
         event = EventTrackingService.newEvent(EVENT_CHAT_DELETE_CHANNEL, 
                  channel.getId().getValue(), false);

         if (event != null)
            EventTrackingService.post(event);
      }
   }

   /**
    * This method is called whenever the observed object is changed. An
    * application calls an <tt>Observable</tt> object's
    * <code>notifyObservers</code> method to have all the object's
    * observers notified of the change.
    * 
    * This operates within its own Thread so normal rules and conditions don't apply
    *
    * @param o   the observable object.
    * @param arg an argument passed to the <code>notifyObservers</code>
    *            method.
    */
   public void update(Observable o, Object arg) {
      if (arg instanceof Event) {
         Event event = (Event)arg;
         if (event.getEvent().equals(EVENT_CHAT_SEND_MESSAGE)) {
            String[] messageParams = event.getResource().split(":");
            
            List observers = (List)roomListeners.get(messageParams[0]);
            
            if(observers != null) {
               synchronized(observers) {
                  for(Iterator i = observers.iterator(); i.hasNext(); ) {
                     RoomObserver observer = (RoomObserver)i.next();
                     
                     observer.receivedMessage(messageParams[0], messageParams[1]);
                  }
               }
            }
            
            
         } else if (event.getEvent().equals(EVENT_CHAT_DELETE_CHANNEL)) {
            String chatChannelId = event.getResource();
            
            List observers = (List)roomListeners.get(chatChannelId);
            
            if(observers != null) {
               synchronized(observers) {
                  for(Iterator i = observers.iterator(); i.hasNext(); ) {
                     RoomObserver observer = (RoomObserver)i.next();
                     
                     observer.roomDeleted(chatChannelId);
                  }
               }
            }
         }
      }
   }
   private void checkAdminTool()
   {
      //checkPermission(ChatFunctions.CHAT_FUNCTION_MANAGE_TOOL);
   }


   public IdManager getIdManager() {
      return idManager;
   }


   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
   }

   public AuthorizationFacade getAuthzManager() {
       return authzManager;
   }

   public void setAuthzManager(AuthorizationFacade authzManager) {
       this.authzManager = authzManager;
   }

   protected void checkPermission(String function) {
       getAuthzManager().checkPermission(function, getIdManager().getId(toolManager.getCurrentPlacement().getId()));
   }


   /**
    * {@inheritDoc}
    */ 
   public Map getAuthorizationsMap() {
       return new AuthZMap(getAuthzManager(), ChatFunctions.CHAT_FUNCTION_PREFIX,
               getIdManager().getId(toolManager.getCurrentPlacement().getId()));
   }

   protected boolean can(String function, String placementId) {
      getAuthzManager().pushAuthzGroups(placementId);
      return new Boolean(getAuthzManager().isAuthorized(function,
               getIdManager().getId(placementId))).booleanValue();
   }

   protected boolean can(String function) {
       return can(function, toolManager.getCurrentPlacement().getId());
   }


   /**
    * {@inheritDoc}
    */
   public boolean isMaintaner() {
       return new Boolean(getAuthzManager().isAuthorized(WorksiteManager.WORKSITE_MAINTAIN,
               getIdManager().getId(toolManager.getCurrentPlacement().getContext()))).booleanValue();
   }


   public ToolManager getToolManager() {
      return toolManager;
   }


   public void setToolManager(ToolManager toolManager) {
      this.toolManager = toolManager;
   }

}
