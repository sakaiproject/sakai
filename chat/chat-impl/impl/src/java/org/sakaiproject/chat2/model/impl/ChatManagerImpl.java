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

package org.sakaiproject.chat2.model.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.SimpleUser;
import org.sakaiproject.chat2.model.TransferableChatMessage;
import org.sakaiproject.chat2.model.ChatFunctions;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.chat2.model.DeleteMessage;
import org.sakaiproject.chat2.model.MessageDateString;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 
 * @author andersjb
 *
 */
@Slf4j
public class ChatManagerImpl extends HibernateDaoSupport implements ChatManager, Receiver {

    private int messagesMax = 100;

    @Getter @Setter private ChatChannel defaultChannelSettings;

    @Setter private UserDirectoryService userDirectoryService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private PresenceService presenceService;
    @Setter private SessionManager sessionManager;
    @Setter private UsageSessionService usageSessionService;
    @Setter private FormattedText formattedText;
    @Setter private PreferencesService preferencesService;
    @Setter private SecurityService securityService;
    @Setter private FunctionManager functionManager;
    @Setter private SiteService siteService;
    @Setter private EventTrackingService eventTrackingService;

    /** MAP[SESSION_KEY][CHANNEL_ID] -> List<TransferableChatMessage> */
    private Cache<String, Map<String, List<TransferableChatMessage>>> messageMap;

    /** MAP[CHANNEL_ID][SESSION_ID] -> TransferableChatMessage */
    /** We store the session_id to allow login multiple times with different browsers */
    private Cache<String, Cache<String, TransferableChatMessage>> heartbeatMap;
    
    // Used for fetching user's default language locale
    ResourceLoader rl = new ResourceLoader();
    //stores users timezone
    private Cache<String, String> timezoneCache;

    @Getter private int pollInterval = 5000; //5 sec

    /* JGroups channel for keeping the above maps in sync across nodes in a Sakai cluster */
    private JChannel clusterChannel = null;
    private boolean clustered = false;


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

    // part of HibernateDaoSupport; this is the only context in which it is OK                                             
    // to modify the template configuration                                                                                
    protected void initDao() throws Exception {
        super.initDao();
        getHibernateTemplate().setCacheQueries(true);
        log.info("initDao template " + getHibernateTemplate());
    }

    /**
     * Called on after the startup of the singleton.  This sets the global
     * list of functions which will have permission managed by sakai
     * @throws Exception
     */
    protected void init() throws Exception
    {
        log.info("init()");

        try {

            // register functions
            if(functionManager.getRegisteredFunctions(ChatFunctions.CHAT_FUNCTION_PREFIX).size() == 0) {
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_READ);
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_NEW);
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_ANY);
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_OWN);
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL);
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_NEW_CHANNEL);
                functionManager.registerFunction(ChatFunctions.CHAT_FUNCTION_EDIT_CHANNEL);
            }

            pollInterval = serverConfigurationService.getInt("chat.pollInterval", 5000);

            messageMap = CacheBuilder.newBuilder()
                    //.recordStats()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .build();
            heartbeatMap = CacheBuilder.newBuilder()
            		//.recordStats()
            		.expireAfterAccess(pollInterval*2, TimeUnit.MILLISECONDS)
            		.build();
            
            timezoneCache = CacheBuilder.newBuilder()
            		.maximumSize(1000)
                    .expireAfterWrite(600, TimeUnit.SECONDS)
                    .build();

            try {
                String channelId = serverConfigurationService.getString("chat.cluster.channel", "");
                
                if (StringUtils.isNotBlank(channelId)) {
                    URL jgroupsConfigURL = null;
                    // Pick up the config file from sakai home if it exists
                    File jgroupsConfig = new File(serverConfigurationService.getSakaiHomePath() + File.separator + "jgroups-chat-config.xml");
                    if (jgroupsConfig.exists()) {
                        log.debug("Using custom jgroups config file: {}", jgroupsConfig.getAbsolutePath());
                        clusterChannel = new JChannel(jgroupsConfig);
                    } else if((jgroupsConfigURL = this.getClass().getClassLoader().getResource("jgroups-config.xml")) != null) { //pick up our default file
                        log.debug("Using default jgroups config file: {}", jgroupsConfigURL);
                        clusterChannel = new JChannel(jgroupsConfigURL);
                    } else {
                        log.debug("No jgroups config file. Using jgroup defaults.");
                        clusterChannel = new JChannel();
                    }

                    log.debug("JGROUPS PROTOCOL: {}", clusterChannel.getProtocolStack().printProtocolSpecAsXML());

                    clusterChannel.setReceiver(this);
                    clusterChannel.connect(channelId);
                    // We don't want a copy of our JGroups messages sent back to us
                    clusterChannel.setDiscardOwnMessages(true);
                    //JmxConfigurator.registerChannel(clusterChannel, ManagementFactory.getPlatformMBeanServer(), "DefaultDomain:name=JGroups");
                    clustered = true;

                    log.info("Chat is connected on JGroups channel '" + channelId + "'"); 
                } else {
                    log.info("No 'chat.cluster.channel' specified in sakai.properties. JGroups will not be used and chat messages will not be replicated."); 
                }
            } catch (Exception e) {
                log.error("Error creating JGroups channel. Chat messages will now NOT BE KEPT IN SYNC", e);
                
                if (clusterChannel != null && clusterChannel.isConnected()) {
                    // This calls disconnect() first
                    clusterChannel.close();
                }
            }

        }
        catch (Exception e) {
            log.warn("Error with ChatManager.init()", e);
        }

    }

    /**
     * Destroy
     */
    public void destroy()
    {
        if (clusterChannel != null && clusterChannel.isConnected()) {
            // This calls disconnect() first
            clusterChannel.close();
        }

        log.info("destroy()");
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

    /* (non-Javadoc)
     * @see org.sakaiproject.chat2.model.ChatManager#getChannelMessages(org.sakaiproject.chat2.model.ChatChannel, java.lang.String, java.util.Date, int, boolean)
     */
    public List<ChatMessage> getChannelMessages(ChatChannel channel, String context, Date date, int start, int max, boolean sortAsc) throws PermissionException {
        if (channel == null) {
            List<ChatMessage> allMessages = new ArrayList<ChatMessage>();
            List<ChatChannel> channels = getContextChannels(context, true);
            for (Iterator<ChatChannel> i = channels.iterator(); i.hasNext();) {
                ChatChannel tmpChannel = i.next();
                allMessages.addAll(getChannelMessages(tmpChannel, date, start, max, sortAsc));
            }

            // Why resorting here? -AZ
            if (sortAsc) {
                Collections.sort(allMessages, channelComparatorAsc);
            } else {
                Collections.sort(allMessages, channelComparatorDesc);
            }
            return allMessages;
        }
        else
            return getChannelMessages(channel, date, start, max, sortAsc);
    }


    /**
     * 
     * @see getChannelMessages
     */
    @SuppressWarnings("unchecked")
    protected List<ChatMessage> getChannelMessages(ChatChannel channel, Date date, int start, int max, boolean sortAsc) throws PermissionException {

        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        if (channel == null || max == 0) {
            // no channel or no items causes nothing to be returned
            return messages;
        }

        checkPermission(ChatFunctions.CHAT_FUNCTION_READ, channel.getContext());
        int localMax = max;
        int localStart = start;
        Date localDate = date;

        // Find out which values to use.
        // If the settings of the channel have more strict values then the passed info, use them instead.
        if (channel.getFilterType().equals(ChatChannel.FILTER_BY_NUMBER) || 
                channel.getFilterType().equals(ChatChannel.FILTER_NONE)) {
            if (!channel.isEnableUserOverride()) {
                localMax = Math.min(localMax, channel.getFilterParam());
            }
        } else if (channel.getFilterType().equals(ChatChannel.FILTER_BY_TIME)) {
            int days = channel.getFilterParam();
            Date tmpDate = calculateDateByOffset(days);
            if (!channel.isEnableUserOverride()) {
                localDate = tmpDate;
            }
        }

        // enforce maximum number of messages returned
        if (localStart < 0) {
            localStart = 0;
        }
        if (localMax < 0 || localMax > messagesMax) {
            localMax = messagesMax;
        }

        Criteria c = this.getSessionFactory().getCurrentSession().createCriteria(ChatMessage.class);
        c.add(Expression.eq("chatChannel", channel));      
        if (localDate != null) {
            c.add(Expression.ge("messageDate", localDate));
        }

        // Always sort desc so we get the newest messages, reorder after we get the final list
        c.addOrder(Order.desc("messageDate"));

        if (localMax != 0) {
            if (localMax > 0) {
                c.setMaxResults(localMax);
            }
            if (localStart > 0) {
                c.setFirstResult(localStart);
            }
            messages = c.list();
        }

        //Reorder the list
        if (sortAsc) {
            Collections.sort(messages, channelComparatorAsc);
        } else {
            Collections.sort(messages, channelComparatorDesc);
        }

        return messages;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.chat2.model.ChatManager#countChannelMessages(org.sakaiproject.chat2.model.ChatChannel)
     */
    public int countChannelMessages(ChatChannel channel) {
        return getChannelMessagesCount(channel, null, null);
        // use getChannelMessagesCount since it is more efficient
//        Criteria c = this.getSession().createCriteria(ChatMessage.class);
//        if (channel != null) {
//            c.add(Expression.eq("chatChannel", channel));      
//        }
//        List<ChatMessage> messages = c.list();
//        return messages.size();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.chat2.model.ChatManager#getChannelMessagesCount(org.sakaiproject.chat2.model.ChatChannel, java.lang.String, java.util.Date)
     */
    public int getChannelMessagesCount(ChatChannel channel, String context, Date date) {
        if (channel == null) {
            // default to the first one
            List<ChatChannel> channels = getContextChannels(context, true);
            if (channels != null && channels.size() > 0) {
                channel = channels.iterator().next();
            }
        }
        int count = 0;
        if (channel != null) {
            Criteria c = this.getSessionFactory().getCurrentSession().createCriteria(ChatMessage.class);
            c.add(Expression.eq("chatChannel", channel));      
            if (date != null) {
                c.add(Expression.ge("messageDate", date));
            }
            c.setProjection(Projections.rowCount());
            count = ((Long) c.uniqueResult()).intValue();
        }
        return count;
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

            log.debug("migrate message: "+messageId+", "+channelId);


            ChatMessage message = getMigratedMessage(messageId);


            if (owner == null) {
                log.warn("can't migrate message, owner is null. messageId: ["+messageId
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
                message.setBody(formattedText.convertPlaintextToFormattedText(body));
                message.setMigratedMessageId(migratedId);


                getHibernateTemplate().save(message);

            }

        } catch (Exception e) {

            log.error("migrateMessage: "+e);

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
        boolean isOwner = sessionManager.getCurrentSessionUserId() != null ?
                sessionManager.getCurrentSessionUserId().equals(message.getOwner()) : false;

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
        if (sessionManager.getCurrentSessionUserId() == null)
            return false;

        boolean allowed = false;
        if (channel != null) {
            allowed = can(ChatFunctions.CHAT_FUNCTION_NEW, channel.getContext());
            if (allowed) {
                // check the dates if they are set (https://jira.sakaiproject.org/browse/SAK-24207)
                Date today = new Date();
                Date start = channel.getStartDate();
                if (start == null) {
                    start = today;
                } else {
                    // fix up the date to shift to be beginning or end of the day (drop any time component)
                    start = DateUtils.truncate(start, Calendar.DATE);
                }
                Date end = channel.getEndDate();
                if (end == null) {
                    end = today;
                } else {
                    // fix up the date to shift to be beginning or end of the day (drop any time component)
                    end = DateUtils.truncate(end, Calendar.DATE);
                    end = DateUtils.addSeconds(end, 86398); // just short of a full day in seconds
                }
                if ( today.before(start) || today.after(end) ) {
                    // today is outside the configured dates so no posting allowed
                    allowed = false;
                }
            }
        }
        return allowed;
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

        if (channel != null) {
            channel.getMessages().size();
            channel.getMessages().clear();
            updateChannel(channel, false);
        }
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
        List<ChatMessage> messages = (List<ChatMessage>) getHibernateTemplate().findByNamedQueryAndNamedParam("findMigratedMessage", "messageId", migratedMessageId);
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
        List<ChatChannel> channels = (List<ChatChannel>) getHibernateTemplate().findByNamedQueryAndNamedParam("findChannelsInContext", "context", context);
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
        List<ChatChannel> channels = (List<ChatChannel>) getHibernateTemplate().findByNamedQueryAndNamedParam("findChannelsInContext", "context", context);

        if(channels.size() == 0) {
            try {
                ChatChannel channel = createNewChannel(context, defaultNewTitle, true, false, placement);
                getHibernateTemplate().save(channel);
                channels.add(channel);
            }
            catch (PermissionException e) {
                log.debug("Ignoring exception since it shouldn't be thrown here as we're not checking");
            }

        }

        return channels;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public ChatChannel getDefaultChannel(String contextId, String placement) {
        List<ChatChannel> channels = (List<ChatChannel>) getHibernateTemplate().findByNamedQueryAndNamedParam("findDefaultChannelsInContext", new String[] {"context", "placement"}, new Object[] {contextId, placement});
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
    public void sendMessage(ChatMessage message) {
        ChatMessageTxSync txSync = new ChatMessageTxSync(message);

        getHibernateTemplate().flush();
        txSync.afterCompletion(ChatMessageTxSync.STATUS_COMMITTED);

        sendToCluster(new TransferableChatMessage(message));
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
        
        sendToCluster(new TransferableChatMessage(TransferableChatMessage.MessageType.REMOVE, message.getId(), message.getChatChannel().getId()));
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
        
        sendToCluster(new TransferableChatMessage(TransferableChatMessage.MessageType.REMOVE, "*", channel.getId()));
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
            if (message.getOwner().equals(sessionManager.getCurrentSessionUserId()))
            {
                // own or any
                function = ChatFunctions.CHAT_FUNCTION_DELETE_OWN;
            }


            event = eventTrackingService.newEvent(function, 
                    message.getReference(), false);

            if (event != null)
                eventTrackingService.post(event);

            addMessageToMap(new TransferableChatMessage(TransferableChatMessage.MessageType.REMOVE, message.getId(), message.getChatChannel().getId()));
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
            event = eventTrackingService.newEvent(ChatFunctions.CHAT_FUNCTION_NEW, 
                    message.getReference(), false);

            if (event != null)
                eventTrackingService.post(event);
            
            addMessageToMap(new TransferableChatMessage(message));

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
            event = eventTrackingService.newEvent(ChatFunctions.CHAT_FUNCTION_DELETE_CHANNEL, 
                    channel.getReference(), false);

            if (event != null)
                eventTrackingService.post(event);
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
            event = eventTrackingService.newEvent(ChatFunctions.CHAT_FUNCTION_DELETE_ANY, 
                    channel.getReference(), false);

            if (event != null)
                eventTrackingService.post(event);

            addMessageToMap(new TransferableChatMessage(TransferableChatMessage.MessageType.REMOVE, "*", channel.getId()));
        }
    }

    /**
     * Resets the passed context's default channel
     *
     */
    protected void resetPlacementDefaultChannel(String context, String placement) {
        Session session = null;

        try {
            session = getSessionFactory().getCurrentSession();
            Query query = session.createSQLQuery("update CHAT2_CHANNEL c set c.placementDefaultChannel = :channel, c.PLACEMENT_ID = NULL WHERE c.context = :context and c.PLACEMENT_ID = :placement");
            query.setBoolean("channel", false);
            query.setString("context", context);
            query.setString("placement", placement);
            query.executeUpdate();
        } catch(Exception e) {
            log.warn(e.getMessage());
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
                log.debug("Ignoring PermissionException since it is unchecked here.");
            }
        }
    }

    protected void checkPermission(String function, String context) throws PermissionException {

        if (!securityService.unlock(function, siteService.siteReference(context)))
        {
            String user = sessionManager.getCurrentSessionUserId();
            throw new PermissionException(user, function, context);
        }
    }

    protected boolean can(String function, String context) {      
        return securityService.unlock(function, siteService.siteReference(context));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMaintainer(String context) {
        return securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteService.siteReference(context));
    }


    protected String getSummaryFromHeader(ChatMessage item) throws UserNotDefinedException
    {
        String body = item.getBody();
        if ( body.length() > 50 ) body = body.substring(1,49);
        User user = userDirectoryService.getUser(item.getOwner());
        
        ZonedDateTime ldt = ZonedDateTime.ofInstant(item.getMessageDate().toInstant(), ZoneId.of(getUserTimeZone()));
        Locale locale = rl.getLocale();
        
        String newText = body + ", " + user.getDisplayName() + ", " + ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale));
        return newText;
    }


    /**********************************************************************************************************************************************************************************************************************************************************
     * getSummary implementation
     *********************************************************************************************************************************************************************************************************************************************************/
    public Map<String,String> getSummary(String channel, int items, int days)
    throws IdUsedException, IdInvalidException, PermissionException
    {
        long startTime = System.currentTimeMillis() - (days * 24l * 60l * 60l * 1000l);

        List<ChatMessage> messages = getChannelMessages(getChatChannel(channel), new Date(startTime), 0, items, true);

        Iterator<ChatMessage> iMsg = messages.iterator();
        ZonedDateTime pubDate = null;
        String summaryText = null;
        Map<String,String> m = new HashMap<String,String>();
        Locale locale = rl.getLocale();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z").withLocale(locale);
        while (iMsg.hasNext()) {
            ChatMessage item  = iMsg.next();
            //MessageHeader header = item.getHeader();
            ZonedDateTime newTime = ZonedDateTime.ofInstant(item.getMessageDate().toInstant(), ZoneId.of(getUserTimeZone()));
            if ( pubDate == null || newTime.isBefore(pubDate) ) pubDate = newTime;
            try {
                String newText = getSummaryFromHeader(item);
                if ( summaryText == null ) {
                    summaryText = newText;
                } else {
                    summaryText = summaryText + "<br>\r\n" + newText;
                }
            }
            catch (UserNotDefinedException e) {
                log.warn("Skipping the chat message for user: " + item.getOwner() + " since they cannot be found");
            }
        }
        if ( pubDate != null ) {
            m.put(Summary.PROP_PUBDATE, pubDate.format(dtf));
        }
        if ( summaryText != null ) {
            m.put(Summary.PROP_DESCRIPTION, summaryText);
            return m;
        }
        return null;
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
        return (relative ? "" : serverConfigurationService.getAccessUrl()) + REFERENCE_ROOT;
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

    public void setMessagesMax(int messagesMax) {
        this.messagesMax = messagesMax;
    }

    public int getMessagesMax() {
        return messagesMax;
    }
    
    //********************************************************************
    /**
     * {@inheritDoc}
     */
    public List<SimpleUser> getPresentUsers(String siteId, String channelId){
        Set<SimpleUser> presentUsers = new HashSet<SimpleUser>();

        if (StringUtils.isNotBlank(siteId)) {

            // refresh our presence at the location and retrieve the present users
            String location = siteId + "-presence";
            presenceService.setPresence(location);

            for(UsageSession us : presenceService.getPresence(siteId + "-presence")){
                //check if still online in the heartbeat map
                if (isOnline(channelId, us.getId())) {
                    TransferableChatMessage tcm = heartbeatMap.getIfPresent(channelId).getIfPresent(us.getId());
                    String sessionUserId = getUserIdFromSessionKey(tcm.getId());
                    
                    String displayName = us.getUserDisplayId();
                    String userId = us.getUserId();
                    try {
                        displayName = userDirectoryService.getUser(us.getUserId()).getDisplayName();
                        //if user stored in heartbeat is different to the presence one
                        if(!userId.equals(sessionUserId)) {
                            userId += ":"+sessionUserId;
                            displayName += " (" + userDirectoryService.getUser(sessionUserId).getDisplayName() + ")";
                        }
                    }catch(Exception e){
                        log.error("Error getting user "+sessionUserId, e);
                    }

                    presentUsers.add(new SimpleUser(userId, formattedText.escapeHtml(displayName, true)));
                }
                else {
                    log.debug("Heartbeat not found for sessionId {}, so not adding to presentUsers", us.getId());
                }
            }
            
        }
        List<SimpleUser> ret = new ArrayList<>(presentUsers);
        ret.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,Object> handleChatData(String siteId, String channelId, String sessionKey) {

        Map<String,Object> data = new HashMap<String,Object>();
        
        if(StringUtils.isNotBlank(sessionKey)) {
            //current user is requesting data -> is online in that channel
            TransferableChatMessage hb = addHeartBeat(channelId, sessionKey);
    
            sendToCluster(hb);
    
            List<SimpleUser> presentUsers = getPresentUsers(siteId, channelId);
    
            ChatChannel channel = null;
    
            if (channelId != null) {
                channel = getChatChannel(channelId);
            } else if (siteId != null) {
                channel = getDefaultChannel(siteId, null);
    }

            List<ChatMessage> messages = new ArrayList<ChatMessage>();
            List<DeleteMessage> delete = new ArrayList<DeleteMessage>();
            //as guava cache is synchronized, maybe this is not necessary
            synchronized (messageMap){
                if (messageMap.getIfPresent(sessionKey) != null) {
                    try {
                        if(messageMap.getIfPresent(sessionKey).get(channelId) != null) {
                            for(TransferableChatMessage tcm : messageMap.getIfPresent(sessionKey).get(channelId)){
                                switch(tcm.getType()){
                                    case CHAT:
                                        messages.add(tcm.toChatMessage(channel));
                                        break;
                                    case REMOVE:
                                        delete.add(new DeleteMessage(tcm.getId(), tcm.getChannelId()));
                                        break;
                                }
                            }
                        }
                    } catch(Exception e){
                        log.error("Error getting messages in channel "+channelId+" for session_key "+sessionKey, e);
    }

                    //clear all messages for this user
                    messageMap.invalidate(sessionKey);
                }
            }
            //sort messages by date
            messages.sort((a, b) -> a.getMessageDate().compareTo(b.getMessageDate()));
    
            //send clear message to jGroups
            sendToCluster(new TransferableChatMessage(TransferableChatMessage.MessageType.CLEAR, sessionKey));        
    
            data.put("messages", messages);
            data.put("deletedMessages", delete);
            data.put("presentUsers", presentUsers);
        }

        return data;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getPollInterval(){
        return pollInterval;
    }
    

    /**
     * {@inheritDoc}
     */
    public String getSessionKey(){
        try {
            UsageSession usageSession = usageSessionService.getSession();
            String sessionId = usageSession.getId();
            //this is different from usageSession.getUserId(), because we want to know both users (real and login as)
            String sessionUser = sessionManager.getCurrentSessionUserId();
    
            return sessionId+":"+sessionUser;
        } catch(Exception e){
            log.error("Error getting current session key", e);
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public MessageDateString getMessageDateString(ChatMessage msg){
        ZonedDateTime ldt = ZonedDateTime.ofInstant(msg.getMessageDate().toInstant(), ZoneId.of(getUserTimeZone()));

        Locale locale = rl.getLocale();

        DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        DateTimeFormatter dtf2 = DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG).withLocale(locale);
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", locale);

        return  new MessageDateString(ldt.format(dtf), ldt.format(dtf2), ldt.format(dtf3));
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUserTimeZone() {
		String userId = sessionManager.getCurrentSessionUserId();
		if (userId == null) { return ZoneId.systemDefault().getId(); }
		
		String elementCacheId = "TZ_"+userId;
		String element = timezoneCache.getIfPresent(elementCacheId);
		if(element != null) {
			return element;
		}
		
		Preferences prefs = preferencesService.getPreferences(userId);
		ResourceProperties tzProps = prefs.getProperties(TimeService.APPLICATION_ID);
		String timeZone = tzProps.getProperty(TimeService.TIMEZONE_KEY);
		
		try {
			if (StringUtils.isNotBlank(timeZone)) {
				ZoneId.of(timeZone);
			}
		} catch(java.time.zone.ZoneRulesException e){
			try {
				//maybe the given zoneId was a shortId (like 'CST')
				timeZone = ZoneId.SHORT_IDS.get(timeZone);
			}catch(Exception ex){
				timeZone = null;
			}
		} catch(java.time.DateTimeException e) {
			timeZone = null;
		}
		
		if(StringUtils.isBlank(timeZone)) {
			timeZone = ZoneId.systemDefault().getId();
		}
		
		timezoneCache.put(elementCacheId, timeZone);
		
		return timeZone;
	}
    

    /**
     * JGroups message listener.
     */
    public void receive(Message msg) {
        Object o = msg.getObject();
        if (o instanceof TransferableChatMessage) {
            TransferableChatMessage message = (TransferableChatMessage) o;

            String id = message.getId();
            String channelId = message.getChannelId();

            switch(message.getType()){
            case CHAT : 
                log.debug("Received message {} from cluster ...", id);
                addMessageToMap(message);
                break;
            case HEARTBEAT :
                log.debug("Received heartbeat {} - {} from cluster ...", id, channelId);
                addHeartBeat(channelId, id);
                break;
            case CLEAR :
                log.debug("Received clear message {} from cluster ...", id);
                //as guava cache is synchronized, maybe this is not necessary
                synchronized (messageMap){
                    messageMap.invalidate(id);
                }
                break;
            case REMOVE :
                log.debug("Received remove message {} from cluster ...", id);
                addMessageToMap(message); 
                break;
            }
        }
    }
    
    //********************************************************************
    // private utility functions
    
    /**
     * Implements a threadsafe addition to the message map
     */
    private void addMessageToMap(TransferableChatMessage msg) {
        String channelId = msg.getChannelId();
        //as guava cache is synchronized, maybe this is not necessary
        synchronized (messageMap){
            //get all users (sessions) present in the channel where the message goes to
            Cache<String, TransferableChatMessage> sessionsInChannel = heartbeatMap.getIfPresent(channelId);
            if(sessionsInChannel != null) {
                for(String sessionId : sessionsInChannel.asMap().keySet()) {
                    TransferableChatMessage tcm = sessionsInChannel.getIfPresent(sessionId);
                    String sessionKey = tcm.getId();
                    try {
                        Map<String, List<TransferableChatMessage>> channelMap = messageMap.get(sessionKey, () -> {
                            return new HashMap<String, List<TransferableChatMessage>>();
                        });
    
                        if(channelMap.get(channelId) == null) {
                            channelMap.put(channelId, new ArrayList<TransferableChatMessage>());
                        }
                        channelMap.get(channelId).add(msg);
    
                        log.debug("Added chat message to channel={}, sessionKey={}", channelId, sessionKey);
                    } catch(Exception e){
                        log.warn("Failed to add chat message to channel={}, sessionKey={}", channelId, sessionKey);
                    }
                }
            }
        }
    }
    
    /**
     * Set/Update the heartbeat for given sessionKey (indexed by channelId and sessionId)
     * @param channelId
     * @param sessionKey
     * @return
     */
    private TransferableChatMessage addHeartBeat(String channelId, String sessionKey){
        TransferableChatMessage ret = null;
        
        String sessionId = getSessionIdFromSessionKey(sessionKey);        

        try {
            ret = TransferableChatMessage.HeartBeat(channelId, sessionKey);
            heartbeatMap.get(channelId, () -> {
                return CacheBuilder.newBuilder()
                        .expireAfterWrite(pollInterval*2, TimeUnit.MILLISECONDS)
                        .build();
            }).put(sessionId, ret);
        } catch(Exception e){
            log.error("Error adding heartbet in channel : "+channelId+" and session_key : "+sessionKey);
        }
        return ret;
    }

    /** Check if given userId is online in the channel.
     * 
     * @param channelId
     * @param userId
     * @return
     */
    private boolean isOnline(String channelId, String sessionId) {
        if(heartbeatMap.getIfPresent(channelId) == null) {
            return false;
        }

        //thanks to the cache auto-expiration system, not updated hearbeats will be automatically removed
        return (heartbeatMap.getIfPresent(channelId).getIfPresent(sessionId) != null);
    }
    
    private void sendToCluster(TransferableChatMessage message){
        if (clustered) {
            try {
                log.debug("Sending message ({}) id:{}, channelId:{} to cluster ...", message.getType(), message.getId(), message.getChannelId());
                Message msg = new Message(null, message);
                clusterChannel.send(msg);
            } catch (Exception e) {
                log.error("Error sending JGroups message", e);
            }
        }
    }


    private String getSessionIdFromSessionKey(String sessionKey){
        return  sessionKey.substring(0, sessionKey.indexOf(":"));
    }
    private String getUserIdFromSessionKey(String sessionKey){
        return sessionKey.substring(sessionKey.indexOf(":") + 1);
    }
    
    //********************************************************************
    // jGroups override functions

    @Override
    public void getState(OutputStream output) throws Exception {
    }

    @Override
    public void setState(InputStream input) throws Exception {
    }

    @Override
    public void block() {
    }

    @Override
    public void suspect(Address arg0) {
    }

    @Override
    public void unblock() {
    }

    @Override
    public void viewAccepted(View arg0) {
    }
}
