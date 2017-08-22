/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.chat.entity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.jmx.JmxConfigurator;
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
import org.sakaiproject.portal.api.PortalChatPermittedHelper;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides all the RESTful targets for the portal chat code in chat.js. Clustering
 * is catered for using a JGroups channel.
 *
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
@Slf4j
public final class PCServiceEntityProvider extends AbstractEntityProvider implements Receiver, EntityProvider, Createable, Inputable, Outputable, ActionsExecutable, AutoRegisterEntityProvider, Describeable {

	private final static ResourceLoader rb = new ResourceLoader("portal-chat");

	private final static String WEBRTC_SERVER_REGEX = "^(turn|stun):(([^:]*):([^@]*)@){0,1}([^:@]*(:[0-9]{1,5}){0,1})$";
	
	public final static String ENTITY_PREFIX = "portal-chat";

    /* messageMap keys */
    private static final String VIDEO = "video";
    private static final String PLAIN = "plain";
    private static final String CONNECTION = "connection";
    private static final String SITE = "site";
	
    private boolean showSiteUsers = true;
    
    private int pollInterval = 5000;

    private boolean isVideoEnabled = false;

    private final List<PortalVideoServer> iceServers = new ArrayList<PortalVideoServer>();

    private PortalChatPermittedHelper portalChatPermittedHelper;
	public void setPortalChatPermittedHelper(PortalChatPermittedHelper portalChatPermittedHelper) {
		this.portalChatPermittedHelper = portalChatPermittedHelper;
	}

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

    private ProfileConnectionsLogic profileConnectionsLogic = null;
    public void setProfileConnectionsLogic(ProfileConnectionsLogic profileConnectionsLogic) {
        this.profileConnectionsLogic = profileConnectionsLogic;
    }

    private ProfileMessagingLogic profileMessagingLogic = null;
    public void setProfileMessagingLogic(ProfileMessagingLogic profileMessagingLogic) {
        this.profileMessagingLogic = profileMessagingLogic;
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
	private final Map<String, Map<String, Map<String, List<UserMessage>>>> messageMap
        = new HashMap<String, Map<String, Map<String, List<UserMessage>>>>();
	
    /*
     *  A mapping of timestamps onto the user id that sent the heartbeat. The initial capacity should be set
     *  to the number of app servers in your cluster times the max number of threads per app server. This is
     *  configurable in sakai.properties as portalchat.heartbeatmap.size.
     */
	private Map<String,UserMessage> heartbeatMap;

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
        
        pollInterval = serverConfigurationService.getInt("portal.chat.pollInterval", 5000);

        showSiteUsers = serverConfigurationService.getBoolean("portal.chat.showSiteUsers", true);
        
        isVideoEnabled = serverConfigurationService.getBoolean("portal.chat.video", true);

        if (isVideoEnabled) {
            String [] servers = serverConfigurationService.getStrings("portal.chat.video.servers");
            if (servers == null) {
                servers = new String[]{"stun:stun.l.google.com:19302"};
            }
            for (String server : servers) {
                iceServers.add(new PortalVideoServer(server));
            }
        }

        try {
            String channelId = serverConfigurationService.getString("portalchat.cluster.channel");
            if (channelId != null && !channelId.equals("")) {
                // Pick up the config file from sakai home if it exists
                File jgroupsConfig = new File(serverConfigurationService.getSakaiHomePath() + File.separator + "jgroups-config.xml");
                JChannel channel;
                if (jgroupsConfig.exists()) {
                    log.debug("Using jgroups config file: {}", jgroupsConfig.getAbsolutePath());
                    clusterChannel = channel = new JChannel(jgroupsConfig);
                } else {
                    log.debug("No jgroups config file. Using jgroup defaults.");
                    clusterChannel = channel = new JChannel();
                }

                log.debug("JGROUPS PROTOCOL: {}", clusterChannel.getProtocolStack().printProtocolSpecAsXML());

                clusterChannel.setReceiver(this);
                clusterChannel.connect(channelId);
                // We don't want a copy of our JGroups messages sent back to us
                clusterChannel.setDiscardOwnMessages(true);
                JmxConfigurator.registerChannel(channel, ManagementFactory.getPlatformMBeanServer(), "DefaultDomain:name=JGroups");
                clustered = true;

                log.info("Portal chat is connected on JGroups channel '" + channelId + "'"); 
            } else {
                log.info("No 'portalchat.cluster.channel' specified in sakai.properties. JGroups will not be used and chat messages will not be replicated."); 
            }
        } catch (Exception e) {
            log.error("Error creating JGroups channel. Chat messages will now NOT BE KEPT IN SYNC", e);
        }
        
        int heartbeatMapSize = serverConfigurationService.getInt("portalchat.heartbeatmap.size",1000);
        heartbeatMap = new ConcurrentHashMap<String,UserMessage>(heartbeatMapSize,0.75F,64);
    }
    
    public void destroy() throws Exception {
    	super.destroy();
    	if (clusterChannel != null && clusterChannel.isConnected()) {
    		// This calls disconnect() first
    		clusterChannel.close();
    	}
    }

    /**
     * Uses reflection to call Profile2's connections method.
     *
     * @returns A list of Person instances cunningly disguised as lowly Objects
     */
    private List<Person> getConnectionsForUser(String uuid) {

        List<Person> connections = new ArrayList<Person>();
        try {
            connections = profileConnectionsLogic.getConnectionsForUser(uuid);
        } catch (NullPointerException npe) {
            // TODO: this needs tracing back into the Profile2 code
            log.error("NPE thrown by profile service. No connections will be returned for '" + uuid + "'");
        }

        List<Person> filteredConnections = new ArrayList<Person>();

        for (Person person : connections) {

            // Only sparsify and add the connection if that person is allowed
            // to use portal chat.
            if (portalChatPermittedHelper.checkChatPermitted(person.getUuid())) {

                // We null all the person stuff to reduce the download size
                try {
                    person.setProfile(null);
                    person.setPrivacy(null);
                    person.setPreferences(null);
                    
                } catch (Exception e) {
                    log.error("Failed to sparsify Person instance. Skipping this person ...",e);
                    continue;
                }

            	filteredConnections.add(person);
            }
        }

        return filteredConnections;
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

        log.debug("createEntity");

		final User currentUser = userDirectoryService.getCurrentUser();
		final User anon = userDirectoryService.getAnonymousUser();
		
		if (anon.equals(currentUser)) {
            log.debug("No current user");
			throw new SecurityException("You must be logged in to use this service");
		}
		
		final String to = (String) params.get("to");
        if (to == null) {
            log.debug("No recipient");
            throw new IllegalArgumentException("You must supply a recipient");
        }
		
		if (to.equals(currentUser.getId())) {
            log.debug("recipient is sender");
			throw new IllegalArgumentException("You can't chat with yourself");
		}

		String message = (String) params.get("message");

		if (message == null) {
			log.debug("no message supplied");
			throw new IllegalArgumentException("You must supply a message");
		}

		String siteId = (String) params.get("siteId");
		log.debug("siteId: {}", siteId);

		boolean isMessageToConnection = "true".equals(params.get("isMessageToConnection"));
		log.debug("isMessageToConnection: {}", isMessageToConnection);

        // Sakai plays the role of signalling server in the WebRTC architecture.
		boolean isVideoSignal = "true".equals(params.get("video"));

		final UserMessage lastHeartbeat = heartbeatMap.get(to);
		
		if (lastHeartbeat == null || ((new Date()).getTime() - lastHeartbeat.timestamp) >= pollInterval) {
            // If this is not a video signal, send a message via the profile's
            // messaging function
            if (!isVideoSignal) {
                profileMessagingLogic.sendNewMessage(to,currentUser.getId(), UUID.randomUUID().toString(), rb.getString("profile_message_subject"), message);
            }
            log.debug("returning OFFLINE ...");
            return "OFFLINE";
        }

		log.debug("message: {}", message);
		log.debug("isVideoSignal: {}", isVideoSignal);

		// Sanitise the message. XSS attacks. Unescape single quotes. They are valid.
		if (!isVideoSignal) { 
			message = StringEscapeUtils.escapeHtml4(
						StringEscapeUtils.escapeEcmaScript(message)).replaceAll("\\\\'", "'");
		}

		final UserMessage userMessage = new UserMessage(currentUser.getId(), to, siteId, message, isVideoSignal, false, isMessageToConnection);

		addMessageToMap(userMessage);
		
        if (clustered) {
            try {
                log.debug("Sending {} message to cluster ...", isVideoSignal ? "video signal " : "");
                Message msg = new Message(null, null, userMessage);
            	clusterChannel.send(msg);
            } catch (Exception e) {
                log.error("Error sending JGroups message", e);
            }
        }
		
		return "success";
	}

	public String[] getHandledInputFormats() {
	    return new String[] { Formats.HTML };
	}
	
    /**
     * This is the transfer message used in jGroups sync, if clustered.
     */
	public class UserMessage implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public String from;
	    public String to;
	    public String siteId;
		public String content;
		public long timestamp;
		public boolean video;
		public boolean clear;
		public boolean fromConnection;
		
		private UserMessage() {}

		private UserMessage(String from, boolean clear) {
			this(from, null, null, null, false, clear, false);
		}

		private UserMessage(String from, String content) {
			this(from, null, null, content, false, false, false);
		}

        private UserMessage(String from, String to, String siteId, String content, boolean video, boolean clear, boolean fromConnection) {

            this.to = to;
			this.from = from;
            this.siteId = siteId;
			this.content = content;
			this.timestamp = (new Date()).getTime();
			this.video = video;
			this.clear = clear;
			this.fromConnection = fromConnection;
		}
        
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        	out.writeObject(from);
        	out.writeObject(to);
        	out.writeObject(siteId);
        	out.writeObject(content);
        	out.writeObject(timestamp);
        	out.writeObject(video);
        	out.writeObject(clear);
        	out.writeObject(fromConnection);
        }
        
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        	this.from = (String) in.readObject();
        	this.to = (String) in.readObject();
        	this.siteId = (String) in.readObject();
        	this.content = (String) in.readObject();
        	this.timestamp = (Long) in.readObject();
        	this.video = (Boolean) in.readObject();
        	this.clear = (Boolean) in.readObject();
        	this.fromConnection = (Boolean) in.readObject();
        }
	}

	public class PortalChatUser {
		
		public String id;
		public String displayName;
		public boolean offline = false;
		public String video;
		
		public PortalChatUser(String id, String displayName, boolean offline, String video) {

			this.id = id;
			this.displayName = displayName;
			this.offline = offline;
			this.video = video;
		}
	}

	public class PortalVideoServer {

		public String protocol;
		public String host;
		public String username;
		public String credential;
		
		public PortalVideoServer(String url) {

			if (url.matches(WEBRTC_SERVER_REGEX)) {
				this.protocol = url.replaceFirst(WEBRTC_SERVER_REGEX, "$1");
				this.username = url.replaceFirst(WEBRTC_SERVER_REGEX, "$3");
				this.credential = url.replaceFirst(WEBRTC_SERVER_REGEX, "$4");
				this.host = url.replaceFirst(WEBRTC_SERVER_REGEX, "$5");
			} else {
				log.warn("WebRTC Server doesn't match expected format!!");
			}
		}
	}
	
    /**
     * The JS client calls this to grab the latest data in one call. Connections, latest messages, online users
     * and present users (in a site) are all returned in one lump of JSON. If the online parameter is supplied and
     * true, a heartbeat is stamped for the sender as well.
     */
	@EntityCustomAction(action = "latestData", viewKey = EntityView.VIEW_SHOW)
	public Map<String,Object> handleLatestData(EntityReference ref, Map<String,Object> params) {
		
		log.debug("handleLatestData");

		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if (anon.equals(currentUser)) {
            log.debug("No current user");
			throw new SecurityException("You must be logged in to use this service");
		}
		
		String online = (String) params.get("online");
		String videoAgent = (String) params.get("videoAgent");

		String siteId = (String) params.get("siteId");
		log.debug("siteId: {}", siteId);

		log.debug("online: {}", online);

		if (online != null && "true".equals(online)) {
			
			log.debug("{} is online. Stamping their heartbeat ...", currentUser.getEid());

			UserMessage userMessage = new UserMessage(currentUser.getId(), videoAgent);
			heartbeatMap.put(currentUser.getId(), userMessage);

            if (clustered) {
            	
            	log.debug("We are clustered. Propagating heartbeat ...");
            	
                Message msg = new Message(null, null, userMessage);
                try {
                    clusterChannel.send(msg);
                    log.debug("Heartbeat message sent.");
                } catch (Exception e) {
                    log.error("Error sending JGroups heartbeat message", e);
                }
            }
        } else {
			log.debug("{} is offline. Removing them from the message map ...", currentUser.getEid());

            synchronized (messageMap) {
                messageMap.remove(currentUser.getId());
            }

	        sendClearMessage(currentUser.getId());

			log.debug("{} is offline. Returning an empty data map ...", currentUser.getEid());

            return new HashMap<String,Object>(0);
        }

        List<PortalChatUser> presentUsers = new ArrayList<PortalChatUser>();
		
        if (siteId != null && siteId.length() > 0 && showSiteUsers) {
			// A site id has been specified, so we refresh our presence at the 
			// location and retrieve the present users
			String location = siteId + "-presence";
			presenceService.setPresence(location);
			List<User> presentSakaiUsers = presenceService.getPresentUsers(siteId + "-presence");
			presentSakaiUsers.remove(currentUser);
			for (User user : presentSakaiUsers) {
				UserMessage heartbeat = heartbeatMap.get(user.getId());
				if (heartbeat != null) {
					// Flag this user as offline if they can't access portal chat
					boolean offline = !portalChatPermittedHelper.checkChatPermitted(user.getId());
					presentUsers.add(new PortalChatUser(user.getId(), user.getDisplayName(), offline, heartbeat.content));
				}
				else {
					log.debug("Heartbeat is null so not adding {} to presentUsers", user.getId());
				}
			}
        }
		
		List<Person> connections = getConnectionsForUser(currentUser.getId());
		
		List<PortalChatUser> onlineConnections = new ArrayList<PortalChatUser>(connections.size());
		
		Date now = new Date();
		
		for (Person person : connections) {

            String uuid = person.getUuid();
			
			UserMessage lastHeartbeat = heartbeatMap.get(uuid);
			
			if (lastHeartbeat == null) continue;
			
			if ((now.getTime() - lastHeartbeat.timestamp) < pollInterval) {
				onlineConnections.add(new PortalChatUser(uuid, uuid, false, lastHeartbeat.content));
			}
		}
		
		List<UserMessage> messages = new ArrayList<UserMessage>();
		List<UserMessage> videoMessages = new ArrayList<UserMessage>();

		String currentUserId = currentUser.getId();
		
		synchronized (messageMap) {
			if (messageMap.containsKey(currentUserId)) {
				// Grab the type map for this user
                Map<String, Map<String, List<UserMessage>>> typeMap = messageMap.get(currentUserId);

                // Now pull the plain, video and connection messages for this site
                messages = typeMap.get(PLAIN).get(siteId);
                if (messages != null) {
                    messages.addAll(typeMap.get(PLAIN).get(CONNECTION));
                } else {
                    messages = typeMap.get(PLAIN).get(CONNECTION);
                }

                videoMessages = typeMap.get(VIDEO).get(siteId);
                if (videoMessages != null) {
                    videoMessages.addAll(typeMap.get(VIDEO).get(CONNECTION));
                } else {
                    videoMessages = typeMap.get(VIDEO).get(CONNECTION);
                }

                messageMap.remove(currentUserId);
			}

            sendClearMessage(currentUserId);
		}

		Map<String,Object> data = new HashMap<String,Object>(4);
		
		data.put("connections", connections);
		data.put("messages", messages);
		data.put("videoMessages", videoMessages);
		data.put("online", onlineConnections);
		data.put("showSiteUsers", showSiteUsers);
		data.put("presentUsers", presentUsers);
		data.put("connectionsAvailable", true);
		
		return data;
	}
	
    private void sendClearMessage(String userId) {

        if (clustered) {
            try {
				log.debug("Sending messagMap clear message for {} ...", userId);
            	UserMessage userMessage = new UserMessage(userId, true);
                Message msg = new Message(null, null, userMessage);
                clusterChannel.send(msg);
            } catch (Exception e) {
                log.error("Error sending JGroups clear message", e);
            }
        }
    }

	@EntityCustomAction(action = "ping", viewKey = EntityView.VIEW_SHOW)
	public String handlePing(EntityReference ref) {

		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if (anon.equals(currentUser)) {
            log.debug("No current user");
			throw new SecurityException("You must be logged in to use this service");
		}
		
		String userId = ref.getId();
		
		try {
			String email = userDirectoryService.getUser(userId).getEmail();
            new EmailSender(email, rb.getFormattedMessage("email.subject", new String[]{service}), rb.getFormattedMessage("email.body", new String[]{currentUser.getDisplayName(), service, portalUrl}));
		}
		catch (Exception e) {
			throw new EntityException("Failed to send email",userId);
		}
		
		return "success";
	}

	@EntityCustomAction(action = "servers", viewKey = EntityView.VIEW_SHOW)
	public Map<String,Object> handleServers(EntityReference ref) {

		final User currentUser = userDirectoryService.getCurrentUser();
		final User anon = userDirectoryService.getAnonymousUser();
		
		if (anon.equals(currentUser)) {
            log.debug("No current user");
			throw new SecurityException("You must be logged in to use this service");
		}

		final Map<String,Object> data = new HashMap<String,Object>();
		data.put("iceServers", iceServers);
		return data;
	}
	
    /**
     * Implements a threadsafe addition to the message map
     */
    private void addMessageToMap(UserMessage m) {

        synchronized (messageMap) {

            if (!messageMap.containsKey(m.to)) {
				log.debug("No message map entry for '{}'. Creating new entries ...", m.to);
                Map<String, Map<String, List<UserMessage>>> typeMap = new HashMap<String, Map<String, List<UserMessage>>>();
                typeMap.put(PLAIN, new HashMap<String, List<UserMessage>>());
                typeMap.get(PLAIN).put(m.siteId, new ArrayList<UserMessage>());
                typeMap.get(PLAIN).put(CONNECTION, new ArrayList<UserMessage>());
                typeMap.put(VIDEO, new HashMap<String, List<UserMessage>>());
                typeMap.get(VIDEO).put(m.siteId, new ArrayList<UserMessage>());
                typeMap.get(VIDEO).put(CONNECTION, new ArrayList<UserMessage>());
                messageMap.put(m.to, typeMap);
            }

            if (m.video) {
                log.debug("Message is a video message");
                Map<String, List<UserMessage>> videoMap = messageMap.get(m.to).get(VIDEO);

                if (m.fromConnection) {
                    videoMap.get(CONNECTION).add(m);
                } else if (videoMap.containsKey(m.siteId)) {
                    videoMap.get(m.siteId).add(m);
                } else {
                    videoMap.put(m.siteId, Arrays.asList(m));
                }
            } else {
                log.debug("Message is a plain message");
                Map<String, List<UserMessage>> plainMap = messageMap.get(m.to).get(PLAIN);

                if (m.fromConnection) {
                    plainMap.get(CONNECTION).add(m);
                } else if (plainMap.containsKey(m.siteId)) {
					log.debug("plainMap already contains '{}'", m.siteId);
                    plainMap.get(m.siteId).add(m);
                } else {
					log.debug("plainMap does not contain '{}'. A new list will be mapped", m.siteId);
                    plainMap.put(m.siteId, Arrays.asList(m));
                }
            }
        }
    }

	private class EmailSender implements Runnable {

		private Thread runner;

		private String email;
		private String subject;
		private String message;

		public EmailSender(String email, String subject, String message) {

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

				final String emailFromAddress = "\"" + service + "\" <" + serverConfigurationService.getString("setup.request","no-reply@" + serverName) + ">";
				emailService.send(emailFromAddress, email, subject, message, email, null, additionalHeaders);
			} catch (Exception e) {
                log.error("sendEmail() failed for email: " + email,e);
			}
		}
	}
	
    /**
     * JGroups message listener.
     */
    public void receive(Message msg) {

        Object o = msg.getObject();
        if (o instanceof UserMessage) {
            UserMessage message = (UserMessage) o;
            if (message.to == null) {
            	if (message.clear) {
                    String userId = message.from;
                    synchronized (messageMap) {
                        messageMap.remove(userId);
    				}
            	} else {
            		log.debug("Received heartbeat from cluster ...");
            		heartbeatMap.put(message.from, message);
            	}
            } else  {
				log.debug("Received {} message from cluster ...",  message.video ? "video" : "");
                addMessageToMap(message);
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
