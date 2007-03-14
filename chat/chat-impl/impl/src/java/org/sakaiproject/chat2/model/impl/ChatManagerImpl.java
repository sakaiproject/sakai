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
 
package org.sakaiproject.chat2.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

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
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.StringUtil;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author andersjb
 *
 */
public class ChatManagerImpl extends HibernateDaoSupport implements ChatManager, Observer { //, EntityContentProducer {

   protected final transient Log logger = LogFactory.getLog(getClass());
   
   /** the key for sending message events around */
   protected final static String EVENT_CHAT_SEND_MESSAGE = "sakai.chat.message";
   
   protected final static String EVENT_CHAT_DELETE_CHANNEL = "sakai.chat.delete.channel";
   
   private static final String CHANNEL_PROP = "channel";
   
   private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
   private static final String VERSION_ATTR = "version";
   
   private static final String CHAT = "chat";
   private static final String SYNOPTIC_TOOL = "synoptic_tool";
   private static final String NAME = "name";
   private static final String VALUE = "value";
   
   private static final String PROPERTIES = "properties";
   private static final String PROPERTY = "property";
   
   /**   The tool manager   */
   private ToolManager toolManager;
   
   /** the clients listening to the various rooms */
   protected Map roomListeners = new HashMap();
   
   private EntityManager entityManager;
   
   
   
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
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_ANY);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_OWN);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL);
         FunctionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL);
      }
      
      try {
         getEntityManager().registerEntityProducer(this, Entity.SEPARATOR + REFERENCE_ROOT);
      }
      catch (Exception e) {
         logger.warn("Error registering Chat Entity Producer", e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public ChatChannel createNewChannel(String context, String title, boolean contextDefaultChannel, boolean checkAuthz) throws PermissionException {
      if (checkAuthz)
         checkPermission(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL);
      
      ChatChannel channel = new ChatChannel();
      
      channel.setCreationDate(new Date());
      channel.setContext(context);
      channel.setTitle(title);
      channel.setContextDefaultChannel(contextDefaultChannel);
      
      return channel;
   }


   /**
    * {@inheritDoc}
    */
   public void updateChannel(ChatChannel channel, boolean checkAuthz) throws PermissionException {

      if (checkAuthz)
         checkPermission(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL);
      
      getHibernateTemplate().saveOrUpdate(channel);
   }


   /**
    * {@inheritDoc}
    */
   public void deleteChannel(ChatChannel channel) throws PermissionException {

      checkPermission(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL);
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
   
   
   public Date calculateDateByOffset(int offset) {
      Calendar tmpDate = Calendar.getInstance();
      tmpDate.set(Calendar.DAY_OF_MONTH, tmpDate.get(Calendar.DAY_OF_MONTH)-offset);
      return new Date(tmpDate.getTimeInMillis());
   }

   /**
    * {@inheritDoc}
    */
   public List<ChatMessage> getChannelMessages(ChatChannel channel, String context, Date date, int items, boolean sortAsc) throws PermissionException {
      if (channel == null) {
         List<ChatMessage> allMessages = new ArrayList<ChatMessage>();
         List channels = getContextChannels(context);
         for (Iterator i = channels.iterator(); i.hasNext();) {
            ChatChannel tmpChannel = (ChatChannel) i.next();
            allMessages.addAll(getChannelMessages(tmpChannel, date, items, sortAsc));
         }
         
         
         Comparator channelComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                   return ((ChatMessage)o1).getMessageDate().compareTo(((ChatMessage)o2).getMessageDate());
            }
         };         
         
         Collections.sort(allMessages, channelComparator);
         return allMessages;
      }
      else
         return getChannelMessages(channel, date, items, sortAsc);
   }


   /**
    * 
    * @see getChannelMessages
    */
   protected List<ChatMessage> getChannelMessages(ChatChannel channel, Date date, int items, boolean sortAsc) throws PermissionException 
   {
      checkPermission(ChatFunctions.CHAT_FUNCTION_READ);
      int localItems = items;
      Date localDate = date;

      Criteria c = this.getSession().createCriteria(ChatMessage.class);
      
      // Find out which values to use.
      // If the settings of the channel have more strict values then the passed info, use them instead.
      if (channel.getFilterType().equals(ChatChannel.FILTER_BY_NUMBER)) {
         if (localItems == 0) localItems = Integer.MAX_VALUE;
         localItems = Math.min(localItems, channel.getFilterParam());
      }
      else if (channel.getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
         int days = channel.getFilterParam();
         Date tmpDate = calculateDateByOffset(days);
         long minDate = (localDate == null) ? tmpDate.getTime() : Math.min(localDate.getTime(), tmpDate.getTime());
         localDate = new Date(minDate);
      }
      
      if (channel != null) {
         c.add(Expression.eq("chatChannel", channel));      
      }
      if (localDate != null) {
         c.add(Expression.ge("messageDate", localDate));
      }
      
      if (!sortAsc) {
         c.addOrder(Order.desc("messageDate"));
      }
      else {
         c.addOrder(Order.asc("messageDate"));
      }
      
      if (localItems > 0)
         c.setMaxResults(localItems);
      
      return c.list();
      
   }

   /**
    * {@inheritDoc}
    */
   public ChatMessage createNewMessage(ChatChannel channel, String owner) throws PermissionException {
      
      checkPermission(ChatFunctions.CHAT_FUNCTION_NEW);
      
      ChatMessage message = new ChatMessage();
      
      message.setChatChannel(channel);
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
       ToolConfiguration toolConfig = SiteService.findTool(placementId);
       String context = toolConfig.getContext();
       boolean canDeleteAny = can(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, context);
       boolean canDeleteOwn = can(ChatFunctions.CHAT_FUNCTION_DELETE_OWN, context);
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
     * tells us if the channel can be deleted by the current session user
     */
     public boolean getCanDelete(ChatChannel channel, String placementId) {
        ToolConfiguration toolConfig = SiteService.findTool(placementId);
        String context = toolConfig.getContext();
        boolean canDelete = can(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL, context);
        
        return canDelete;
     }
     public boolean getCanDelete(ChatChannel channel)
    {
       return getCanDelete(channel, toolManager.getCurrentPlacement().getId());
    }
     
     protected boolean getCanEdit(ChatChannel channel, String placementId) {
        ToolConfiguration toolConfig = SiteService.findTool(placementId);
        String context = toolConfig.getContext();
        boolean canDelete = can(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL, context);
        
        return canDelete;
     }
     
     public boolean getCanEdit(ChatChannel channel)
     {
        return getCanEdit(channel, toolManager.getCurrentPlacement().getId());
     }
     
     protected boolean getCanCreateChannel(String placementId) {
        ToolConfiguration toolConfig = SiteService.findTool(placementId);
        String context = toolConfig.getContext();
        boolean canDelete = can(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL, context);
        
        return canDelete;
     }
     
     public boolean getCanCreateChannel()
     {
        return getCanCreateChannel(toolManager.getCurrentPlacement().getId());
     }
     
     protected boolean getCanReadMessage(ChatChannel channel, String placementId) {
        ToolConfiguration toolConfig = SiteService.findTool(placementId);
        String context = toolConfig.getContext();
        boolean canDelete = can(ChatFunctions.CHAT_FUNCTION_READ, context);
        
        return canDelete;
     }
     
     public boolean getCanReadMessage(ChatChannel channel)
     {
        return getCanReadMessage(channel, toolManager.getCurrentPlacement().getId());
     }
   
   /**
    * delete a Chat Message
    * @param ChatMessage the message to delete
    */
   public void deleteMessage(ChatMessage message) throws PermissionException 
   {
      if(!getCanDelete(message))
         checkPermission(ChatFunctions.CHAT_FUNCTION_DELETE_ANY);
      
      getHibernateTemplate().delete(message);
   }


   /**
    * {@inheritDoc}
    */
   public ChatMessage getMessage(String chatMessageId) {
      return (ChatMessage)getHibernateTemplate().get(
                              ChatMessage.class, chatMessageId);
   }


   protected List getContextChannels(String context) {
      List channels = getHibernateTemplate().findByNamedQuery("findChannelsInContext", context);
      return channels;
   }
   
   /**
    * {@inheritDoc}
    */
   public List getContextChannels(String context, String defaultNewTitle) {
      List channels = getHibernateTemplate().findByNamedQuery("findChannelsInContext", context);
      
      if(channels.size() == 0) {
         try {
            ChatChannel channel = createNewChannel(context, defaultNewTitle, true, false);
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
   public ChatChannel getDefaultChannel(String contextId) {
      List channels = getHibernateTemplate().findByNamedQuery("findDefaultChannelsInContext", contextId);
      if (channels.size() >= 1)
         return (ChatChannel)channels.get(0);
      
      return null;
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
                  message.getChatChannel().getId() + ":" + message.getId(), false);

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
                  channel.getId(), false);

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
   
   /**
    * Resets the passed context's default channel
    *
    */
   protected void resetContextDefaultChannel(String context) {
      Session session = null;
      Connection conn = null;
      ResultSet rs = null;
      try{
        session = getSessionFactory().openSession();
        conn = session.connection();
        String query="update CHAT2_CHANNEL c set c.contextDefaultChannel=false WHERE c.context=?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, context);
        statement.executeUpdate();
      }
      catch(Exception e){
        logger.warn(e.getMessage());
      }
      finally{
        try{
          if (session !=null) session.close();
          if (conn !=null) conn.close();
          if (rs !=null) rs.close();
        }
        catch(Exception ex){
          logger.warn(ex.getMessage());
        }
      }
   }
   
   public void makeDefaultContextChannel(ChatChannel channel) {
      //reset context's defaults
      if (isMaintainer()) {
         try {
            resetContextDefaultChannel(channel.getContext());
         
            //set new one as default
            channel.setContextDefaultChannel(true);
            updateChannel(channel, false);
         }
         catch (PermissionException e) {
            logger.debug("Ignoring PermissionException since it is unchecked here.");
         }
      }
   }
   
   /**
    * This turns the site id into a realm  (/site/<siteId>)
    * @return siteId
    */
   private String getContextSiteId(String context)
   {
     //LOG.debug("getContextSiteId()");
     return ("/site/" + context);
   }

   protected void checkPermission(String function) throws PermissionException {
      String context = toolManager.getCurrentPlacement().getContext();
      if (!SecurityService.unlock(function,getContextSiteId(context)))
      {
         String user = SessionManager.getCurrentSessionUserId();
         throw new PermissionException(user, function, context);
      }
   }

   protected boolean can(String function, String context) {      
      return SecurityService.unlock(function, getContextSiteId(context));
   }

   protected boolean can(String function) {
       return can(function, toolManager.getCurrentPlacement().getContext());
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMaintainer() {
      return SecurityService.unlock("site.upd",
            getContextSiteId(toolManager.getCurrentPlacement().getContext())); 
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
   public Map getSummary(String channel, int items, int days)
         throws IdUsedException, IdInvalidException, PermissionException
   {
      long startTime = System.currentTimeMillis() - (days * 24l * 60l * 60l * 1000l);

      List messages = getChannelMessages(getChatChannel(channel), new Date(startTime), items, true);
      
      Iterator iMsg = messages.iterator();
      Time pubDate = null;
      String summaryText = null;
      Map m = new HashMap();
      while (iMsg.hasNext()) {
         ChatMessage item  = (ChatMessage) iMsg.next();
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


   public ToolManager getToolManager() {
      return toolManager;
   }


   public void setToolManager(ToolManager toolManager) {
      this.toolManager = toolManager;
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
   public String getSummarizableReference(String siteId) {
      //I think this should just return null so we get all channels.
      String channel = null;
      return channel;
   }

   
   
   /**
    * {@inheritDoc}
    */
   public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
   {
      //prepare the buffer for the results log
      StringBuffer results = new StringBuffer();
      int channelCount = 0;

      try 
      {
         // start with an element with our very own (service) name         
         Element element = doc.createElement(serviceName());
         element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
         ((Element) stack.peek()).appendChild(element);
         stack.push(element);

         Element chat = doc.createElement(CHAT);
         List channelList = getContextChannels(siteId);
         if (channelList != null && !channelList.isEmpty()) 
         {
            Iterator channelIterator = channelList.iterator();
            while (channelIterator.hasNext()) 
            {
               ChatChannel channel = (ChatChannel)channelIterator.next();
               Element channelElement = channel.toXml(doc, stack);
               chat.appendChild(channelElement);
               channelCount++;
            }
            results.append("archiving " + getLabel() + ": (" + channelCount + ") channels archived successfully.\n");
            
         } 
         else 
         {
            results.append("archiving " + getLabel()
                  + ": empty chat room archived.\n");
         }
         
         // archive the chat synoptic tool options
         archiveSynopticOptions(siteId, doc, chat);

         ((Element) stack.peek()).appendChild(chat);
         stack.push(chat);

         stack.pop();
      }
      catch (Exception any)
      {
         logger.warn("archive: exception archiving service: " + serviceName());
      }

      stack.pop();

      return results.toString();
   }

   /**
    * {@inheritDoc}
    */
   public Entity getEntity(Reference ref)
   {
      // we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

      Entity rv = null;

      try
      {
         // if this is a channel
         if (REF_TYPE_CHANNEL.equals(ref.getSubType()))
         {
            rv = getChatChannel(ref.getReference());
         }
/*
         // otherwise a message
         else if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
         {
            rv = getMessage(ref);
         }
*/
         // else try {throw new Exception();} catch (Exception e) {M_log.warn("getResource(): unknown message ref subtype: " + m_subType + " in ref: " + m_reference, e);}
         else
            logger.warn("getEntity(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
      }
      catch (NullPointerException e)
      {
         logger.warn("getEntity(): " + e);
      }

      return rv;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference, java.lang.String)
    */
   public Collection getEntityAuthzGroups(Reference ref, String userId) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
    */
   public String getEntityDescription(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
    */
   public ResourceProperties getEntityResourceProperties(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
    */
   public String getEntityUrl(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
    */
   public HttpAccess getHttpAccess() {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getLabel() {
      return "chat";
   }

   /**
    * {@inheritDoc}
    */
   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
         Set userListAllowImport)
   {
      logger.debug("trying to merge chat");

      // buffer for the results log
      StringBuffer results = new StringBuffer();

      int count = 0;

      if (siteId != null && siteId.trim().length() > 0)
      {
         try
         {
            NodeList allChildrenNodes = root.getChildNodes();
            int length = allChildrenNodes.getLength();
            for (int i = 0; i < length; i++)
            {
               count++;
               Node siteNode = allChildrenNodes.item(i);
               if (siteNode.getNodeType() == Node.ELEMENT_NODE)
               {
                  Element chatElement = (Element) siteNode;
                  if (chatElement.getTagName().equals(CHAT))
                  {
                     Site site = SiteService.getSite(siteId);
                     if (site.getToolForCommonId(CHAT_TOOL_ID) != null) {
   
                        // add the chat rooms and synoptic tool options                
                        NodeList chatNodes = chatElement.getChildNodes();
                        int lengthChatNodes = chatNodes.getLength();
                        for (int cn = 0; cn < lengthChatNodes; cn++)
                        {
                           Node chatNode = chatNodes.item(cn);
                           if (chatNode.getNodeType() == Node.ELEMENT_NODE)
                           {
                              Element channelElement = (Element) chatNode;
                              if (channelElement.getTagName().equals(CHANNEL_PROP)) {
                                 ChatChannel channel = ChatChannel.xmlToChatChannel(channelElement, siteId);
                                 //save the channel
                                 updateChannel(channel, false);
                              }
                              
                              else if (channelElement.getTagName().equals(SYNOPTIC_TOOL)) 
                              {
                                 ToolConfiguration synTool = site.getToolForCommonId("sakai.synoptic.chat");
                                 Properties synProps = synTool.getPlacementConfig();

                                 NodeList synPropNodes = channelElement.getChildNodes();
                                 for (int props = 0; props < synPropNodes.getLength(); props++)
                                 {
                                    Node propsNode = synPropNodes.item(props);
                                    if (propsNode.getNodeType() == Node.ELEMENT_NODE)
                                    {
                                       Element synPropEl = (Element) propsNode;
                                       if (synPropEl.getTagName().equals(PROPERTIES))
                                       {
                                          NodeList synProperties = synPropEl.getChildNodes();
                                          for (int p = 0; p < synProperties.getLength(); p++)
                                          {
                                             Node propertyNode = synProperties.item(p);
                                             if (propertyNode.getNodeType() == Node.ELEMENT_NODE)
                                             {
                                                Element propEl = (Element) propertyNode;
                                                if (propEl.getTagName().equals(PROPERTY))
                                                {
                                                   String propName = propEl.getAttribute(NAME);
                                                   String propValue = propEl.getAttribute(VALUE);
                                                   
                                                   if (propName != null && propName.length() > 0 && propValue != null && propValue.length() > 0)
                                                   {
                                                      synProps.setProperty(propName, propValue);
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }        
                        }
                        SiteService.save(site);
                     }
                  }
               }
            }

            results.append("merging chat " + siteId + " (" + count
                  + ") chat items.\n");
         }
         catch (DOMException e)
         {
            logger.error(e.getMessage(), e);
            results.append("merging " + getLabel()
                  + " failed during xml parsing.\n");
         }
         catch (Exception e)
         {
            logger.error(e.getMessage(), e);
            results.append("merging " + getLabel() + " failed.\n");
         }
      }

      return results.toString();

   } // merge

   /**
    * {@inheritDoc}
    */
   public boolean parseEntityReference(String reference, Reference ref)
   {
      if (reference.startsWith(REFERENCE_ROOT))
      {
         String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

         String id = null;
         String subType = null;
         String context = null;
         String container = null;

         // the first part will be null, then next the service, the third will be "msg" or "channel"
         if (parts.length > 2)
         {
            subType = parts[2];
            if (REF_TYPE_CHANNEL.equals(subType))
            {
               // next is the context id
               if (parts.length > 3)
               {
                  context = parts[3];

                  // next is the channel id
                  if (parts.length > 4)
                  {
                     id = parts[4];
                  }
               }
            }
            else if (REF_TYPE_MESSAGE.equals(subType))
            {
               // next three parts are context, channel (container) and mesage id
               if (parts.length > 5)
               {
                  context = parts[3];
                  container = parts[4];
                  id = parts[5];
               }
            }
            else
               logger.warn("parse(): unknown message subtype: " + subType + " in ref: " + reference);
         }

         ref.set(APPLICATION_ID, subType, id, container, context);

         return true;
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean willArchiveMerge()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String[] myToolIds()
   {
      String[] toolIds = { CHAT_TOOL_ID };
      return toolIds;
   }

   /**
    * {@inheritDoc}
    */
   public void transferCopyEntities(String fromContext, String toContext, List ids) 
   {
      try
      {           
         // retrieve all of the chat rooms
         List channels = getContextChannels(fromContext);
         if (channels != null && !channels.isEmpty()) 
         {
            Iterator channelIterator = channels.iterator();
            while (channelIterator.hasNext()) 
            {
               ChatChannel oldChannel = (ChatChannel)channelIterator.next();
               ChatChannel newChannel = createNewChannel(toContext, oldChannel.getTitle(), false, false);
               newChannel.setDescription(oldChannel.getDescription());
               newChannel.setFilterType(oldChannel.getFilterType());
               newChannel.setFilterParam(oldChannel.getFilterParam());
               newChannel.setContextDefaultChannel(oldChannel.isContextDefaultChannel());
               try {
                  updateChannel(newChannel, false);
               } 
               catch (Exception e) 
               {
                  logger.warn("Exception while creating channel: " + newChannel.getTitle() + ": " + e);
               }

            }
         }
         
         transferSynopticOptions(fromContext, toContext);    
      }

      catch (Exception any)
      {
         logger.warn(".transferCopyEntities(): exception in handling " + serviceName() + " : ", any);
      }
   }
   
   /**
    * Import the synoptic tool options from another site
    * 
    * @param fromContext
    * @param toContext
    */
   protected void transferSynopticOptions(String fromContext, String toContext)
   {
      try 
      {
         // transfer the synoptic tool options
         Site fromSite = SiteService.getSite(fromContext);
         ToolConfiguration fromSynTool = fromSite.getToolForCommonId("sakai.synoptic." + getLabel());
         Properties fromSynProp = fromSynTool.getPlacementConfig();

         Site toSite = SiteService.getSite(toContext);
         ToolConfiguration toSynTool = toSite.getToolForCommonId("sakai.synoptic." + getLabel());
         Properties toSynProp = toSynTool.getPlacementConfig();

         if (fromSynProp != null && !fromSynProp.isEmpty()) 
         {
            Set synPropSet = fromSynProp.keySet();
            Iterator propIter = synPropSet.iterator();
            while (propIter.hasNext())
            {
               String propName = ((String)propIter.next());
               String propValue = fromSynProp.getProperty(propName);
               if (propValue != null && propValue.length() > 0)
               {
                  toSynProp.setProperty(propName, propValue);
               }
            }

            SiteService.save(toSite);
         }
      }
      catch (PermissionException pe)
      {
         logger.warn("PermissionException transferring synoptic options for " + serviceName() + ':', pe);
      }
      catch (IdUnusedException e)
      {
         logger.warn("Channel " + fromContext + " cannot be found. ");
      }
      catch (Exception e)
      {
         logger.warn("transferSynopticOptions(): exception in handling " + serviceName() + " : ", e);
      }
   }
   
   /**
    * try to add synoptic options for this tool to the archive, if they exist
    * @param siteId
    * @param doc
    * @param element
    */
   public void archiveSynopticOptions(String siteId, Document doc, Element element)
   {
      try
      {
         // archive the synoptic tool options
         Site site = SiteService.getSite(siteId);
         ToolConfiguration synTool = site.getToolForCommonId("sakai.synoptic." + getLabel());
         Properties synProp = synTool.getPlacementConfig();
         if (synProp != null && synProp.size() > 0) {
            Element synElement = doc.createElement(SYNOPTIC_TOOL);
            Element synProps = doc.createElement(PROPERTIES);

            Set synPropSet = synProp.keySet();
            Iterator propIter = synPropSet.iterator();
            while (propIter.hasNext())
            {
               String propName = (String)propIter.next();
               Element synPropEl = doc.createElement(PROPERTY);
               synPropEl.setAttribute(NAME, propName);
               synPropEl.setAttribute(VALUE, synProp.getProperty(propName));
               synProps.appendChild(synPropEl);
            }

            synElement.appendChild(synProps);
            element.appendChild(synElement);
         }
      }
      catch (Exception e)
      {
         logger.warn("archive: exception archiving synoptic options for service: " + serviceName());
      }
   }
   
   private boolean inputIsValidInteger(String val)
   {
      try {  
         Integer.parseInt(val);
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }
   
   protected String serviceName() {
      return ChatManager.class.getName();
   }

}
