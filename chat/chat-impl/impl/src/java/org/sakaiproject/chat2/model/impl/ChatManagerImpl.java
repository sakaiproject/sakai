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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.chat2.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.RoomObserver;
import org.sakaiproject.chat2.model.ChatFunctions;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
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
   
   /** the clients listening to the various rooms */
   protected Map<String,List<RoomObserver>> roomListeners = new HashMap<String,List<RoomObserver>>();
   
   private EntityManager entityManager;
   
   private ChatChannel defaultChannelSettings;
   
   
   static Comparator<ChatMessage> channelComparatorAsc = new Comparator<ChatMessage>() {
      public int compare(ChatMessage o1, ChatMessage o2) {
             return (o1.getMessageDate().compareTo(o2.getMessageDate()));
      }
   };  
   
   static Comparator<ChatMessage> channelComparatorDesc = new Comparator<ChatMessage>() {
      public int compare(ChatMessage o1, ChatMessage o2) {
             return -1 * (o1.getMessageDate().compareTo(o2.getMessageDate()));
      }
   };     
   
   /**
    * Called on after the startup of the singleton.  This sets the global
    * list of functions which will have permission managed by sakai
    * @throws Exception
    */
   protected void init() throws Exception
   {
      logger.info("init()");
      
      try {
         
         EventTrackingService.addObserver(this);
         
         // register functions
         if(FunctionManager.getRegisteredFunctions(ChatFunctions.CHAT_FUNCTION_PREFIX).size() == 0) {
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_READ);
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_NEW);
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_ANY);
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_OWN);
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL);
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL);
            FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL);
         }
         
      }
      catch (Exception e) {
         logger.warn("Error with ChatManager.init()", e);
      }
      
   }
   
   /**
    * Destroy
    */
   public void destroy()
   {
      EventTrackingService.deleteObserver(this);

      logger.info("destroy()");
   }

   /**
    * {@inheritDoc}
    */
   public ChatChannel createNewChannel(String context, String title, boolean placementDefaultChannel, boolean checkAuthz, String placement) throws PermissionException {
      if (checkAuthz)
         checkPermission(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL, context);
      
      ChatChannel channel = new ChatChannel(getDefaultChannelSettings());
      
      channel.setCreationDate(new Date());
      channel.setContext(context);
      channel.setTitle(title);
      channel.setPlacementDefaultChannel(placementDefaultChannel);
      if (placementDefaultChannel) {
    	  channel.setPlacement(placement);  
      }
         
      return channel;
   }


   /**
    * {@inheritDoc}
    */
   public void updateChannel(ChatChannel channel, boolean checkAuthz) throws PermissionException {

	  if (channel == null)
		  return;
	  
      if (checkAuthz)
         checkPermission(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL, channel.getContext());
      
      getHibernateTemplate().saveOrUpdate(channel);
   }


   /**
    * {@inheritDoc}
    */
   public void deleteChannel(ChatChannel channel) throws PermissionException {

	  if (channel == null)
		   return;
	   
      checkPermission(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL, channel.getContext());
      getHibernateTemplate().delete(channel);
      
      sendDeleteChannel(channel);
   }


   /**
    * {@inheritDoc}
    */
   public ChatChannel getChatChannel(String chatChannelId) {
      return (ChatChannel)getHibernateTemplate().get(
                              ChatChannel.class, chatChannelId);
   }
   
   /**
    * {@inheritDoc}
    */
   public Date calculateDateByOffset(int offset) {
      Calendar tmpDate = Calendar.getInstance();
      tmpDate.set(Calendar.DAY_OF_MONTH, tmpDate.get(Calendar.DAY_OF_MONTH)-offset);
      return new Date(tmpDate.getTimeInMillis());
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public int countChannelMessages(ChatChannel channel) {
      Criteria c = this.getSession().createCriteria(ChatMessage.class);
      if (channel != null) {
         c.add(Expression.eq("chatChannel", channel));      
      }
      List messages = c.list();
      return messages.size();
   }

   /**
    * {@inheritDoc}
    */
   public List<ChatMessage> getChannelMessages(ChatChannel channel, String context, Date date, int items, boolean sortAsc) throws PermissionException {
      if (channel == null) {
         List<ChatMessage> allMessages = new ArrayList<ChatMessage>();
         List<ChatChannel> channels = getContextChannels(context, true);
         for (Iterator<ChatChannel> i = channels.iterator(); i.hasNext();) {
            ChatChannel tmpChannel = i.next();
            allMessages.addAll(getChannelMessages(tmpChannel, date, items, sortAsc));
         }
         
         if (sortAsc)
            Collections.sort(allMessages, channelComparatorAsc);
         else
            Collections.sort(allMessages, channelComparatorDesc);
         return allMessages;
      }
      else
         return getChannelMessages(channel, date, items, sortAsc);
   }


   /**
    * 
    * @see getChannelMessages
    */
   @SuppressWarnings("unchecked")
protected List<ChatMessage> getChannelMessages(ChatChannel channel, Date date, int items, boolean sortAsc) throws PermissionException 
   {
	  if (channel == null) {
		  return new ArrayList<ChatMessage>();
	  }
	  
      checkPermission(ChatFunctions.CHAT_FUNCTION_READ, channel.getContext());
      int localItems = items;
      Date localDate = date;

      Criteria c = this.getSession().createCriteria(ChatMessage.class);
      
      // Find out which values to use.
      // If the settings of the channel have more strict values then the passed info, use them instead.
      if (channel.getFilterType().equals(ChatChannel.FILTER_BY_NUMBER) || 
            channel.getFilterType().equals(ChatChannel.FILTER_NONE)) {
         if (localItems < 0) localItems = Integer.MAX_VALUE;
         if (!channel.isEnableUserOverride()) {
            localItems = Math.min(localItems, channel.getFilterParam());
         }
      }
      else if (channel.getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
         int days = channel.getFilterParam();
         Date tmpDate = calculateDateByOffset(days);
         if (!channel.isEnableUserOverride()) {
            localDate = tmpDate;
         }
      }
      
      c.add(Expression.eq("chatChannel", channel));      
      
      if (localDate != null) {
         c.add(Expression.ge("messageDate", localDate));
      }
      
      //Always sort desc so we get the newest messages
      // reorder after we get the final list
      c.addOrder(Order.desc("messageDate"));
      
      List<ChatMessage> messages = new ArrayList<ChatMessage>();
      
      if (localItems != 0) {
         if (localItems > 0) {
            c.setMaxResults(localItems);
         }
         messages = c.list();
      }
      //Reorder the list
      if (sortAsc)
         Collections.sort(messages, channelComparatorAsc);
      else
         Collections.sort(messages, channelComparatorDesc);
      
      return messages;
      
   }

   /**
    * {@inheritDoc}
    */
   public ChatMessage createNewMessage(ChatChannel channel, String owner) throws PermissionException {
      
	  if (channel == null) {
		  throw new IllegalArgumentException("Must specify a channel");
	  }
	  
	  // We don't support posting by anonymous users
	  if (owner == null) {
		  throw new PermissionException(null, ChatFunctions.CHAT_FUNCTION_NEW, channel.getContext());
	  }
	  
	  checkPermission(ChatFunctions.CHAT_FUNCTION_NEW, channel.getContext());
      
      ChatMessage message = new ChatMessage();
      
      message.setChatChannel(channel);
      message.setOwner(owner);
      message.setMessageDate(new Date());
      
      return message;
   }
   
   /**
    * {@inheritDoc}
    */
   public void updateMessage(ChatMessage message)
   {
      getHibernateTemplate().saveOrUpdate(message);
   }
   
   /**
    * {@inheritDoc}
    */
   public void migrateMessage(String sql, Object[] values) {
      
	 
      //String statement = "insert into CHAT2_MESSAGE (MESSAGE_ID, CHANNEL_ID, OWNER, MESSAGE_DATE, BODY, migratedMessageId) " +
      //   "select ?, ?, ?, ?, ?, ? from dual where not exists " +  
      //   "(select * from CHAT2_MESSAGE m2 where m2.migratedMessageId=?)";
      
	  try {
		  
		  String messageId = (String) values[0];
		  String channelId = (String) values[1];
		  String owner     = (String) values[2];
		  Date messageDate = (Date) values[3];
		  String body      = (String) values[4];
		  String migratedId= (String) values[5];
		  
		  logger.debug("migrate message: "+messageId+", "+channelId);

		  
	      ChatMessage message = getMigratedMessage(messageId);
	      
		  
		  if (owner == null) {
			  logger.warn("can't migrate message, owner is null. messageId: ["+messageId
					  +"] channelId: ["+channelId+"]");
			  return;
		  }
	      
	      if(message == null && body != null && !body.equals("")) {
	    	  
	    	  ChatChannel channel = getChatChannel(channelId);
	    	  
	    	  message = new ChatMessage();
	    	  
	    	  message.setId(messageId);
	    	  message.setChatChannel(channel);
	    	  message.setOwner(owner);
	    	  message.setMessageDate(messageDate);
	    	  message.setBody(FormattedText.convertPlaintextToFormattedText(body));
	    	  message.setMigratedMessageId(migratedId);
	    	  
	    	  
	    	  getHibernateTemplate().save(message);
	    	  
	      }
	      
	  } catch (Exception e) {
		  
		 logger.error("migrateMessage: "+e);
		  
	  }
      
     
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean getCanDelete(ChatMessage message) {
    	
       if (message == null)
    	   return false;
       
       String context = message.getChatChannel().getContext();
       
       boolean canDeleteAny = can(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, context);
       boolean canDeleteOwn = can(ChatFunctions.CHAT_FUNCTION_DELETE_OWN, context);
       boolean isOwner = SessionManager.getCurrentSessionUserId() != null ?
    		   SessionManager.getCurrentSessionUserId().equals(message.getOwner()) : false;
       
       boolean canDelete = canDeleteAny;
       
       if(canDeleteOwn && isOwner)
          canDelete = true;
       
       return canDelete;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean getCanDeleteAnyMessage(String context) {
       return can(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, context);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean getCanDelete(ChatChannel channel)
    {
        return channel == null ? false : can(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL, channel.getContext());
     }
     
    /**
     * {@inheritDoc}
     */
     public boolean getCanEdit(ChatChannel channel)
     {
        return channel == null ? false : can(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL, channel.getContext());
     }
      
     /**
      * {@inheritDoc}
      */
     public boolean getCanCreateChannel(String context)
     {
        return can(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL, context);
     }
  
     /**
      * {@inheritDoc}
      */
     public boolean getCanReadMessage(ChatChannel channel)
     {
        return channel == null ? false : can(ChatFunctions.CHAT_FUNCTION_READ, channel.getContext());
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean getCanPostMessage(ChatChannel channel)
     {
    	 // We don't currently support posting messages by anonymous users
    	 if (SessionManager.getCurrentSessionUserId() == null)
    		return false;
    	
        return channel == null ? false : can(ChatFunctions.CHAT_FUNCTION_NEW, channel.getContext());
     }
   
 
   /**
    * delete a Chat Message
    * @param ChatMessage the message to delete
    */
   public void deleteMessage(ChatMessage message) throws PermissionException 
   {
	  if(message==null) return;
      if(!getCanDelete(message))
         checkPermission(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, message.getChatChannel().getContext());
      
      getHibernateTemplate().delete(message);
      
      sendDeleteMessage(message);
   }

   /**
    * {@inheritDoc}
    */
   public void deleteChannelMessages(ChatChannel channel) throws PermissionException {
	   
	  if (channel == null)
		  return;
	  
      if (!getCanDeleteAnyMessage(channel.getContext()))
         checkPermission(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, channel.getContext());
      
      channel = getChatChannel(channel.getId());
      channel.getMessages().size();
      channel.getMessages().clear();
      updateChannel(channel, false);
      
      sendDeleteChannelMessages(channel);
   }

   /**
    * {@inheritDoc}
    */
   public ChatMessage getMessage(String chatMessageId) {
      return (ChatMessage)getHibernateTemplate().get(
                              ChatMessage.class, chatMessageId);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   protected ChatMessage getMigratedMessage(String migratedMessageId) {
      List<ChatMessage> messages = getHibernateTemplate().findByNamedQuery("findMigratedMessage", migratedMessageId);
      ChatMessage message = null;
      if (messages.size() > 0)
         message = messages.get(0);
      return message;
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public List<ChatChannel> getContextChannels(String context, boolean lazy) {
      List<ChatChannel> channels = getHibernateTemplate().findByNamedQuery("findChannelsInContext", context);
      if (!lazy) {
         for (Iterator<ChatChannel> i = channels.iterator(); i.hasNext();) {
            ChatChannel channel = i.next();
            channel.getMessages().size();
         }
      }
      return channels;
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public List<ChatChannel> getContextChannels(String context, String defaultNewTitle, String placement) {
      List<ChatChannel> channels = getHibernateTemplate().findByNamedQuery("findChannelsInContext", context);
      
      if(channels.size() == 0) {
         try {
            ChatChannel channel = createNewChannel(context, defaultNewTitle, true, false, placement);
            getHibernateTemplate().save(channel);
            channels.add(channel);
         }
         catch (PermissionException e) {
            logger.debug("Ignoring exception since it shouldn't be thrown here as we're not checking");
         }
         
      }
      
      return channels;
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public ChatChannel getDefaultChannel(String contextId, String placement) {
      List<ChatChannel> channels = getHibernateTemplate().findByNamedQuery("findDefaultChannelsInContext", new Object[] {contextId, placement});
      if (channels.size() == 0) {
         channels = getContextChannels(contextId, "", placement);
      }
      if (channels.size() >= 1)
         return (ChatChannel)channels.get(0);
      
      return null;
   }
   
   /**
    * {@inheritDoc}
    */
   public void addRoomListener(RoomObserver observer, String roomId)
   {
      List<RoomObserver> roomObservers;
      synchronized(roomListeners) {
         if(roomListeners.get(roomId) == null)
            roomListeners.put(roomId, new ArrayList<RoomObserver>());
         roomObservers = roomListeners.get(roomId);
      }
      synchronized(roomObservers) {
         roomObservers.add(observer);
      }
      
      if (logger.isDebugEnabled()) {
          logger.debug("after add roomObservers " + roomObservers);
       }

   }
   
   /**
    * {@inheritDoc}
    */
   public void removeRoomListener(RoomObserver observer, String roomId)
   {
      
      if(roomListeners.get(roomId) != null) {
         List<RoomObserver> roomObservers = roomListeners.get(roomId);
         
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
         
         if (logger.isDebugEnabled()) {
             logger.debug("after remove roomObservers " + roomObservers);
          }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(ChatMessage message) {
      ChatMessageTxSync txSync = new ChatMessageTxSync(message);

      getHibernateTemplate().flush();
      txSync.afterCompletion(ChatMessageTxSync.STATUS_COMMITTED);
   }
   
   /**
    * {@inheritDoc}
    */
   public void sendDeleteMessage(ChatMessage message) {
      ChatMessageDeleteTxSync txSync = new ChatMessageDeleteTxSync(message);

      if (TransactionSynchronizationManager.isSynchronizationActive()) {
         TransactionSynchronizationManager.registerSynchronization(txSync);
      }
      else {
         txSync.afterCompletion(ChatMessageDeleteTxSync.STATUS_COMMITTED);
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
    * {@inheritDoc}
    */
   public void sendDeleteChannelMessages(ChatChannel channel) {
      ChatChannelMessagesDeleteTxSync txSync = new ChatChannelMessagesDeleteTxSync(channel);

      if (TransactionSynchronizationManager.isSynchronizationActive()) {
         TransactionSynchronizationManager.registerSynchronization(txSync);
      }
      else {
         txSync.afterCompletion(ChatChannelMessagesDeleteTxSync.STATUS_COMMITTED);
      }
   }
   
   /**
    * This helps to send out the message when the record is placed in the database
    * @author andersjb
    *
    */
   private class ChatMessageDeleteTxSync extends TransactionSynchronizationAdapter {
      private ChatMessage message;
      
      public ChatMessageDeleteTxSync(ChatMessage message) {
         this.message = message;
      }

      public void afterCompletion(int status) {
         Event event = null;
         String function = ChatFunctions.CHAT_FUNCTION_DELETE_ANY;
         if (message.getOwner().equals(SessionManager.getCurrentSessionUserId()))
         {
            // own or any
            function = ChatFunctions.CHAT_FUNCTION_DELETE_OWN;
         }
         
         
         event = EventTrackingService.newEvent(function, 
               message.getReference(), false);

         if (event != null)
            EventTrackingService.post(event);
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
         event = EventTrackingService.newEvent(ChatFunctions.CHAT_FUNCTION_NEW, 
                  message.getReference(), false);

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
         event = EventTrackingService.newEvent(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL, 
                  channel.getReference(), false);

         if (event != null)
            EventTrackingService.post(event);
      }
   }
   
   /**
    * This helps to send out the message when the messages are all deleted
    * @author andersjb
    *
    */
   private class ChatChannelMessagesDeleteTxSync extends TransactionSynchronizationAdapter {
      private ChatChannel channel;

      public ChatChannelMessagesDeleteTxSync(ChatChannel channel) {
         this.channel = channel;
      }

      public void afterCompletion(int status) {
         Event event = null;
         event = EventTrackingService.newEvent(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, 
                  channel.getReference(), false);

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
   @SuppressWarnings("unchecked")
   public void update(Observable o, Object arg) {
      if (arg instanceof Event) {
         Event event = (Event)arg;
         
         Reference ref = getEntityManager().newReference(event.getResource());
         
         if (event.getEvent().equals(ChatFunctions.CHAT_FUNCTION_NEW)) {
            
            //String[] messageParams = event.getResource().split(":");
            
            ArrayList<RoomObserver> observers = (ArrayList<RoomObserver>) roomListeners.get(ref.getContainer());
            
	    // originally we did the iteration inside synchronized.
	    // however that turns out to hold the lock too long
	    // a shallow copy of an arraylist shouldn't be bad.
	    // we currently call removeRoom from receivedMessage in
	    // some cases, so it can't be locked or we will deadlock
            if(observers != null) {
               synchronized(observers) {
		   observers = (ArrayList)observers.clone();
	       }
	       for(Iterator<RoomObserver> i = observers.iterator(); i.hasNext(); ) {
		   RoomObserver observer = i.next();
                   
		   observer.receivedMessage(ref.getContainer(), ref.getId());
               }
            }
            
            
         } else if (event.getEvent().equals(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL)) {
            //String chatChannelId = event.getResource();
            
            ArrayList<RoomObserver> observers = (ArrayList<RoomObserver>) roomListeners.get(ref.getId());
            
            if(observers != null) {
               synchronized(observers) {
		   observers = (ArrayList)observers.clone();
	       }
	       for(Iterator<RoomObserver> i = observers.iterator(); i.hasNext(); ) {
		   RoomObserver observer = i.next();
                   
		   observer.roomDeleted(ref.getId());
	       }
            }
         }
      }
   }
   
   /**
    * Resets the passed context's default channel
    *
    */
   protected void resetPlacementDefaultChannel(String context, String placement) {
      Session session = null;
      Connection conn = null;
      PreparedStatement statement = null;
      
     
      String query="update CHAT2_CHANNEL c set c.placementDefaultChannel=?, c.PLACEMENT_ID=? " +
      		"WHERE c.context=? and c.PLACEMENT_ID=?";
      
      try{
        session = getSession();
        conn = session.connection();
        
        statement = conn.prepareStatement(query);
        statement.setBoolean(1, false);
        statement.setString(2, null);
        statement.setString(3, context);
        statement.setString(4, placement);
        statement.executeUpdate();
      }
      catch(Exception e){
        logger.warn(e.getMessage());
      }
      finally{
         if (statement != null) {
            //ensure the statement is closed
            try {
               statement.close();
            } 
            catch (Exception e) {
               if (logger.isDebugEnabled()) {
                  logger.debug(e);
               }
            }
         }
        try{
          if (conn !=null) conn.close();
        }
        catch(Exception ex){
          logger.warn(ex.getMessage());
        }
      }
   }
   
   /**
    * {@inheritDoc}
    */   
   public void makeDefaultContextChannel(ChatChannel channel, String placement) {
      //reset context's defaults
      if (isMaintainer(channel.getContext())) {
         try {
            resetPlacementDefaultChannel(channel.getContext(), placement);
         
            //set new one as default
            channel.setPlacementDefaultChannel(true);
            channel.setPlacement(placement);
            updateChannel(channel, false);
         }
         catch (PermissionException e) {
            logger.debug("Ignoring PermissionException since it is unchecked here.");
         }
      }
   }
   
   protected void checkPermission(String function, String context) throws PermissionException {
      
      if (!SecurityService.unlock(function, SiteService.siteReference(context)))
      {
         String user = SessionManager.getCurrentSessionUserId();
         throw new PermissionException(user, function, context);
      }
   }

   protected boolean can(String function, String context) {      
      return SecurityService.unlock(function, SiteService.siteReference(context));
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMaintainer(String context) {
      return SecurityService.unlock(SiteService.SECURE_UPDATE_SITE, SiteService.siteReference(context));
   }
   

   protected String getSummaryFromHeader(ChatMessage item) throws UserNotDefinedException
   {
      String body = item.getBody();
      if ( body.length() > 50 ) body = body.substring(1,49);
      User user = UserDirectoryService.getUser(item.getOwner());
      Time messageTime = TimeService.newTime(item.getMessageDate().getTime());
      String newText = body + ", " + user.getDisplayName() + ", " + messageTime.toStringLocalFull();
      return newText;
   }


   /**********************************************************************************************************************************************************************************************************************************************************
    * getSummary implementation
    *********************************************************************************************************************************************************************************************************************************************************/
   public Map<String,String> getSummary(String channel, int items, int days)
         throws IdUsedException, IdInvalidException, PermissionException
   {
      long startTime = System.currentTimeMillis() - (days * 24l * 60l * 60l * 1000l);

      List<ChatMessage> messages = getChannelMessages(getChatChannel(channel), new Date(startTime), items, true);
      
      Iterator<ChatMessage> iMsg = messages.iterator();
      Time pubDate = null;
      String summaryText = null;
      Map<String,String> m = new HashMap<String,String>();
      while (iMsg.hasNext()) {
         ChatMessage item  = iMsg.next();
         //MessageHeader header = item.getHeader();
         Time newTime = TimeService.newTime(item.getMessageDate().getTime());
         if ( pubDate == null || newTime.before(pubDate) ) pubDate = newTime;
         try {
            String newText = getSummaryFromHeader(item);
            if ( summaryText == null ) {
               summaryText = newText;
            } else {
               summaryText = summaryText + "<br>\r\n" + newText;
            }
         }
         catch (UserNotDefinedException e) {
            logger.warn("Skipping the chat message for user: " + item.getOwner() + " since they cannot be found");
         }
      }
      if ( pubDate != null ) {
         m.put(Summary.PROP_PUBDATE, pubDate.toStringRFC822Local());
      }
      if ( summaryText != null ) {
         m.put(Summary.PROP_DESCRIPTION, summaryText);
         return m;
      }
      return null;
   }


   /**
    * @return the entityManager
    */
   public EntityManager getEntityManager() {
      return entityManager;
   }

   /**
    * @param entityManager the entityManager to set
    */
   public void setEntityManager(EntityManager entityManager) {
      this.entityManager = entityManager;
   }
   
   /**
    * Access the partial URL that forms the root of resource URLs.
    * 
    * @param relative
    *        if true, form within the access path only (i.e. starting with /msg)
    * @return the partial URL that forms the root of resource URLs.
    */
   protected String getAccessPoint(boolean relative)
   {
      return (relative ? "" : ServerConfigurationService.getAccessUrl()) + REFERENCE_ROOT;
   } // getAccessPoint


   /**
    * {@inheritDoc}
    */
   public String[] summarizableToolIds() {
      String[] toolIds = { CHAT_TOOL_ID };
      return toolIds;
   }
      
   /**
    * {@inheritDoc}
    */
   public String getSummarizableReference(String siteId, String toolIdentifier) {
      //I think this should just return null so we get all channels.
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getLabel() {
      return CHAT;
   }

   /**
    * {@inheritDoc}
    */
   public ChatChannel getDefaultChannelSettings() {
      return defaultChannelSettings;
   }
   
   /**
    * {@inheritDoc}
    */
   public void setDefaultChannelSettings(ChatChannel defaultChannelSettings) {
      this.defaultChannelSettings = defaultChannelSettings;
   }

}
