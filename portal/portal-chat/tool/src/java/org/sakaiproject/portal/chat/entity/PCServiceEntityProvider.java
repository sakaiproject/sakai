package org.sakaiproject.portal.chat.entity;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

/**
 * Provides all the RESTful targets for the portal chat code in chat.js. Clustering
 * is catered for using a JGroups channel.
 *
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class PCServiceEntityProvider extends AbstractEntityProvider implements Receiver, EntityProvider, Createable, Inputable, Outputable, ActionsExecutable, AutoRegisterEntityProvider, Describeable {

	protected final Logger logger = Logger.getLogger(getClass());
	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("portal-chat");

	public final static String ENTITY_PREFIX = "portal-chat";

    /* JGROUPS MESSAGE PREFIXES */

    /* Heartbeat messages start with this */
    private final String HEARTBEAT_PREAMBLE = "heartbeat:";
    /* Message messages start with this */
    private final String MESSAGE_PREAMBLE = "message:";
    /* Clear messages start with this */
    private final String CLEAR_PREAMBLE = "clear:";

    /* SAK-20565. Gets set to false if Profile2 isn't available */
    private boolean connectionsAvailable = true;

    /* SAK-20565. We now use reflection to call the profile connection methods */
    private Object profileServiceObject = null;
    private Method getConnectionsForUserMethod = null;
    private Method getUuidMethod = null;
    private Method setProfileMethod = null;
    private Method setPrivacyMethod = null;
    private Method setPreferencesMethod = null;
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	private EmailService emailService;
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	private PresenceService presenceService;
	public void setPresenceService(PresenceService presenceService) {
		this.presenceService = presenceService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private DeveloperHelperService developerService = null;
	public void setDeveloperService(DeveloperHelperService developerService) {
		this.developerService = developerService;
	}
	
    /* A mapping of a list of messages onto the user id they are intended for */
	private Map<String, List<UserMessage>> messageMap = new HashMap<String,List<UserMessage>>();
	
    /*
     *  A mapping of timestamps onto the user id that sent the heartbeat. The initial capacity should be set
     *  to the number of app servers in your cluster times the max number of threads per app server. This is
     *  configurable in sakai.properties as portalchat.heartbeatmap.size.
     */
	private Map<String,Date> heartbeatMap;

    /* JGroups channel for keeping the above maps in sync across nodes in a Sakai cluster */
    private Channel clusterChannel = null;
    private boolean clustered = false;

    private String portalUrl;

    private String service;

    private String serverName;

    public void init() {
    	
        service = serverConfigurationService.getString("ui.service","Sakai");

        portalUrl = serverConfigurationService.getServerUrl() + "/portal";

        serverName = serverConfigurationService.getServerName();

        try {
            String channelId = serverConfigurationService.getString("portalchat.cluster.channel");
            if(channelId != null && !channelId.equals("")) {
            	// Pick up the config file from sakai home if it exists
            	File jgroupsConfig = new File(serverConfigurationService.getSakaiHomePath() + File.separator + "jgroups-config.xml");
            	if(jgroupsConfig.exists()) {
            		if(logger.isDebugEnabled()) {
            			logger.debug("Using jgroups config file: " + jgroupsConfig.getAbsolutePath());
            		}
            		clusterChannel = new JChannel(jgroupsConfig);
            	} else {
            		if(logger.isDebugEnabled()) {
            			logger.debug("No jgroups config file. Using jgroup defaults.");
            		}
            		clusterChannel = new JChannel();
            	}
            	
            	if(logger.isDebugEnabled()) {
            		logger.debug("JGROUPS PROTOCOL: " + clusterChannel.getProtocolStack().printProtocolSpecAsXML());
            	}
            	
                clusterChannel.setReceiver(this);
                clusterChannel.connect(channelId);
                // We don't want a copy of our JGroups messages sent back to us
                clusterChannel.setDiscardOwnMessages(true);
                clustered = true;

                logger.info("Portal chat is connected on JGroups channel '" + channelId + "'"); 
            } else {
                logger.info("No 'portalchat.cluster.channel' specified in sakai.properties. JGroups will not be used and chat messages will not be replicated."); 
            }
        } catch (Exception e) {
            logger.error("Error creating JGroups channel. Chat messages will now NOT BE KEPT IN SYNC", e);
        }
        
        int heartbeatMapSize = serverConfigurationService.getInt("portalchat.heartbeatmap.size",1000);
        heartbeatMap = new ConcurrentHashMap<String,Date>(heartbeatMapSize,0.75F,64);

        // SAK-20565. Get handles on the profile2 connections methods if available. If not, unset the connectionsAvailable flag.
        ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
        profileServiceObject = componentManager.get("org.sakaiproject.profile2.service.ProfileService");
            
        if(profileServiceObject != null) {
            try {
                getConnectionsForUserMethod = profileServiceObject.getClass().getMethod("getConnectionsForUser",new Class[] {String.class});
                try {
                    Class personClass = Class.forName("org.sakaiproject.profile2.model.Person");
                    try {
                        getUuidMethod = personClass.getMethod("getUuid",null);
                    } catch(Exception e) {
                        logger.warn("Failed to set getUuidMethod");
                    }
                    try {
                        Class clazz = Class.forName("org.sakaiproject.profile2.model.UserProfile");
                        setProfileMethod = personClass.getMethod("setProfile",new Class[] {clazz});
                    } catch(Exception e) {
                        logger.warn("Failed to set setProfileMethod");
                    }
                    try {
                        Class clazz = Class.forName("org.sakaiproject.profile2.model.ProfilePrivacy");
                        setPrivacyMethod = personClass.getMethod("setPrivacy",new Class[] {clazz});
                    } catch(Exception e) {
                        logger.warn("Failed to set setPrivacyMethod");
                    }
                    try {
                        Class clazz = Class.forName("org.sakaiproject.profile2.model.ProfilePreferences");
                        setPreferencesMethod = personClass.getMethod("setPreferences",new Class[] {clazz});
                    } catch(Exception e) {
                        logger.warn("Failed to set setPreferencesMethod");
                    }
                } catch(Exception e) {
                    logger.error("Failed to find Person class. Connections will NOT be available in portal chat.",e);
                    connectionsAvailable = false;
                }
            } catch(Exception e) {
                logger.warn("Failed to set getConnectionsForUserMethod. Connections will NOT be available in portal chat.");
                connectionsAvailable = false;
            }
        } else {
            logger.warn("Failed to find ProfileService interface. Connections will NOT be available in portal chat.");
            connectionsAvailable = false;
        }
    }
    
    public void destroy() {
    	
    	if(logger.isDebugEnabled()) logger.debug("DESTROY!!!!!");
    	
    	if(clusterChannel != null && clusterChannel.isConnected()) {
    		// This calls disconnect() first
    		clusterChannel.close();
    	}
    }

    /**
     * Uses reflection to call Profile2's connections method.
     *
     * @returns A list of Person instances cunningly disguised as lowly Objects
     */
    private List<Object> getConnectionsForUser(String uuid) {
        List<Object> connections = new ArrayList<Object>();

        if(connectionsAvailable == false) {
            return connections;
        }

        try {
            connections = (List<Object>) getConnectionsForUserMethod.invoke(profileServiceObject,new Object[] {uuid});
        } catch(Exception e) {
            logger.error("Failed to invoke the getConnectionsForUser method. Returning an empty connections list ...", e);
        }

        return connections;
    }
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public String[] getHandledOutputFormats() {
	    return new String[] { Formats.TXT ,Formats.JSON};
	}

	public Object getSampleEntity() {
		return new UserMessage();
	}

    /**
     * New messages come in here. The recipient is indicated by the parameter 'to'.
     */
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if(anon.equals(currentUser)) {
			throw new SecurityException("You must be logged in to use this service");
		}
		
		String to = (String) params.get("to");
		if(to == null) throw new IllegalArgumentException("You must supply a recipient");
		
		if(to.equals(currentUser.getId())) {
			throw new IllegalArgumentException("You can't chat with yourself");
		}
		
		Date now = new Date();
		Date lastHeartbeat = null;
		
		lastHeartbeat = heartbeatMap.get(to);
		
		if(lastHeartbeat == null) return "OFFLINE";
			
		if((now.getTime() - lastHeartbeat.getTime()) >= 5000L)
			return "OFFLINE";
		
		String message = (String) params.get("message");
		if(message == null) throw new IllegalArgumentException("You must supply a message");
		
		// Sanitise the message. XSS attacks. Unescape single quotes. They are valid.
		message = StringEscapeUtils.escapeHtml4(
							StringEscapeUtils.escapeEcmaScript(message)).replaceAll("\\\\'","'");
		
		//message = message.replaceAll("\\\\'","'");

        addMessageToMap(new UserMessage(currentUser.getId(), to, message));
			
        if(clustered) {
            try {
                Message msg = new Message(null, null, MESSAGE_PREAMBLE + currentUser.getId() + ":" + to + ":" + message);
                clusterChannel.send(msg);
            } catch (Exception e) {
                logger.error("Error sending JGroups message", e);
            }
        }
		
		return "success";
	}

	public String[] getHandledInputFormats() {
	    return new String[] { Formats.HTML };
	}
	
	public class UserMessage {
		
		public String from;
        public String to;
		public String content;
		public long timestamp;
		
		private UserMessage() {
		}

        private UserMessage(String from, String to, String content) {
            this.to = to;
			this.from = from;
			this.content = content;
			this.timestamp = (new Date()).getTime();
		}
	}
	
	public class PortalChatUser {
		
		public String id;
		public String displayName;
		
		public PortalChatUser(String id,String displayName) {
			this.id = id;
			this.displayName = displayName;
		}
	}

    /**
     * The JS client calls this to grab the latest data in one call. Connections, latest messages, online users
     * and present users (in a site) are all returned in one lump of JSON. If the online parameter is supplied and
     * true, a heartbeat is stamped for the sender as well.
     */
	@EntityCustomAction(action = "latestData", viewKey = EntityView.VIEW_SHOW)
	public Map<String,Object> handleLatestData(EntityReference ref, Map<String,Object> params) {
		
		if(logger.isDebugEnabled()) logger.debug("handleLatestData");

		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if(anon.equals(currentUser)) {
			return new HashMap<String,Object>(0);
		}
		
		String online = (String) params.get("online");
		
		if(logger.isDebugEnabled()) logger.debug("online: " + online);
		
		if(online != null && "true".equals(online)) {
			
			if(logger.isDebugEnabled()) logger.debug(currentUser.getEid() + " is online. Stamping their heartbeat ...");
			
			heartbeatMap.put(currentUser.getId(),new Date());

            if(clustered) {
            	
            	if(logger.isDebugEnabled()) logger.debug("We are clustered. Propagating heartbeat ...");
            	
                Message msg = new Message(null, null, HEARTBEAT_PREAMBLE + currentUser.getId());
                try {
                    clusterChannel.send(msg);
                    if(logger.isDebugEnabled()) logger.debug("Heartbeat message sent.");
                } catch (Exception e) {
                    logger.error("Error sending JGroups heartbeat message", e);
                }
            }
		}
		else {
			
			if(logger.isDebugEnabled()) logger.debug(currentUser.getEid() + " is offline. Removing them from the message map ...");
			synchronized(messageMap) {
				messageMap.remove(currentUser.getId());
			}

            sendClearMessage(currentUser.getId());
			
			if(logger.isDebugEnabled()) logger.debug(currentUser.getEid() + " is offline. Returning an empty data map ...");
			
			return new HashMap<String,Object>(0);
		}

		List<PortalChatUser> presentUsers = new ArrayList<PortalChatUser>();

		String siteId = (String) params.get("siteId");
		
		if(logger.isDebugEnabled()) logger.debug("Site ID: " +  siteId);

        if(siteId != null && siteId.length() > 0) {
			// A site id has been specified, so we refresh our presence at the 
			// location and retrieve the present users
			String location = siteId + "-presence";
			presenceService.setPresence(location);
			List<User> presentSakaiUsers = presenceService.getPresentUsers(siteId + "-presence");
			presentSakaiUsers.remove(currentUser);
			for(User user : presentSakaiUsers) {
				presentUsers.add(new PortalChatUser(user.getId(), user.getDisplayName()));
			}
        }
		
		List<Object> connections = getConnectionsForUser(currentUser.getId());
		
		List<String> onlineConnections = new ArrayList<String>(connections.size());
		
		Date now = new Date();
		
		for(Object personObject : connections) {

            String uuid = null;

            try {
                uuid = (String) getUuidMethod.invoke(personObject,null);
                
                // Null all the person stuff to reduce the download size
                if(setProfileMethod != null) {
                    setProfileMethod.invoke(personObject,new Object[] {null});
                }
                if(setPrivacyMethod != null) {
                    setPrivacyMethod.invoke(personObject,new Object[] {null});
                }
                if(setPreferencesMethod != null) {
                    setPreferencesMethod.invoke(personObject,new Object[] {null});
                }
                
            } catch(Exception e) {
                logger.error("Failed to invoke getUuid on a Person instance. Skipping this person ...",e);
                continue;
            }
			
			Date lastHeartbeat = null;
			
			lastHeartbeat = heartbeatMap.get(uuid);
			
			if(lastHeartbeat == null) continue;
			
			if((now.getTime() - lastHeartbeat.getTime()) < 5000L) {
				onlineConnections.add(uuid);
			}
		}
		
		List<UserMessage> messages = new ArrayList<UserMessage>();
		
		String currentUserId = currentUser.getId();
		
		synchronized(messageMap) {
			if(messageMap.containsKey(currentUserId)) {
				// Grab the user's messages
				messages = messageMap.get(currentUserId);
				// Now we can reset the replicated map.
				messageMap.remove(currentUserId);
			}

            sendClearMessage(currentUserId);
		}

		
		Map<String,Object> data = new HashMap<String,Object>(4);
		
		data.put("connections", connections);
		data.put("messages", messages);
		data.put("online", onlineConnections);
		data.put("presentUsers", presentUsers);
		data.put("connectionsAvailable", connectionsAvailable);
		
		return data;
	}

    private void sendClearMessage(String userId) {
        if(clustered) {
            try {
            	
            	if(logger.isDebugEnabled()) logger.debug("Sending messagMap clear message for " + userId + " ...");
            	
                Message msg = new Message(null, null, CLEAR_PREAMBLE + userId);
                clusterChannel.send(msg);
            } catch (Exception e) {
                logger.error("Error sending JGroups clear message", e);
            }
        }
    }
	
	@EntityCustomAction(action = "ping", viewKey = EntityView.VIEW_SHOW)
	public String handlePing(EntityReference ref)
	{
		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if(anon.equals(currentUser)) {
			throw new SecurityException("You must be logged in to use this service");
		}
		
		String userId = ref.getId();
		
		try {
			String email = userDirectoryService.getUser(userId).getEmail();
            new EmailSender(email, rb.getFormattedMessage("email.subject", new String[]{service}), rb.getFormattedMessage("email.body", new String[]{currentUser.getDisplayName(), service, portalUrl}));
		}
		catch(Exception e) {
			throw new EntityException("Failed to send email",userId);
		}
		
		return "success";
	}

    /**
     * Implements a threadsafe addition to the message map
     */
    private void addMessageToMap(UserMessage m) {
        synchronized (messageMap) {
            List<UserMessage> current = messageMap.get(m.to);

            if (current != null) {
                List<UserMessage> copy = new ArrayList<UserMessage>(current.size());
                copy.addAll(current);
                copy.add(m);
                messageMap.put(m.to, copy);
            } else {
                messageMap.put(m.to, Arrays.asList(m));
            }
        }   
    }

	private class EmailSender implements Runnable {
		private Thread runner;

		private String email;
		private String subject;
		private String message;

		public EmailSender(String email, String subject, String message)
		{
			this.email = email;
			this.subject = subject;
			this.message = message;
			runner = new Thread(this, "PC EmailSender thread");
			runner.start();
		}

		public synchronized void run() {
			try {
				final List<String> additionalHeaders = new ArrayList<String>();
				additionalHeaders.add("Content-Type: text/plain; charset=ISO-8859-1");

				final String emailFromAddress = "\"" + service + "\" <no-reply@" + serverName + ">";
				emailService.send(emailFromAddress, email, subject, message, email, null, additionalHeaders);
			} catch (Exception e) {
                logger.error("sendEmail() failed for email: " + email,e);
			}
		}
	}
	
    /**
     * JGroups message listener.
     */
    public void receive(Message msg) {
        Object o = msg.getObject();
        if (o instanceof String) {
            String message = (String) o;
            if (message.startsWith(HEARTBEAT_PREAMBLE)) {
                String onlineUserId = message.substring(HEARTBEAT_PREAMBLE.length());
                heartbeatMap.put(onlineUserId, new Date());
            } else if (message.startsWith(MESSAGE_PREAMBLE)) {
                Address address = clusterChannel.getAddress();
                String[] parts = message.split(":");
                String from = parts[1];
                String to = parts[2];
                String m = parts[3];
                addMessageToMap(new UserMessage(from, to, m));
            } else if (message.startsWith(CLEAR_PREAMBLE)) {
                String userId = message.substring(CLEAR_PREAMBLE.length());
                synchronized (messageMap) {
                    messageMap.remove(userId);
                }
            }
        }
    }
	
	public void getState(OutputStream arg0) throws Exception {
	}

	public void setState(InputStream arg0) throws Exception {
	}

	public void block() {
	}

	public void suspect(Address arg0) {
	}

	public void unblock() {
	}

	public void viewAccepted(View arg0) {
	}
}
