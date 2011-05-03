package org.sakaiproject.pcservice.impl.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.service.ProfileService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

public class PCServiceEntityProvider implements EntityProvider, Createable, Inputable, Outputable, ActionsExecutable, AutoRegisterEntityProvider {
	
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
	
	private ProfileService profileService;
	public void setProfileService(ProfileService profileService) {
		this.profileService = profileService;
	}
	
	public final static String ENTITY_PREFIX = "portal-chat";

	protected final Logger logger = Logger.getLogger(getClass());

	private Map<String, List<UserMessage>> messageMap = new HashMap<String,List<UserMessage>>();
	
	private Map<String,Date> heartbeatMap = new ConcurrentHashMap<String,Date>(500,0.75F,32);
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public String[] getHandledOutputFormats() {
	    return new String[] { Formats.TXT ,Formats.JSON};
	}

	@Override
	public Object getSampleEntity() {
		return new UserMessage();
	}

	@Override
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if(anon.equals(currentUser)) {
			throw new SecurityException("You must be logged in to use this service");
		}
		
		String uuid = (String) params.get("uuid");
		if(uuid == null) throw new IllegalArgumentException("You must supply a uuid");
		
		if(uuid.equals(currentUser.getId())) {
			throw new IllegalArgumentException("You can't chat with yourself");
		}
		
		Date now = new Date();
		Date lastHeartbeat = null;
		
		lastHeartbeat = heartbeatMap.get(uuid);
		
		if(lastHeartbeat == null) return "OFFLINE";
			
		if((now.getTime() - lastHeartbeat.getTime()) >= 5000L)
			return "OFFLINE";
		
		String message = (String) params.get("message");
		if(message == null) throw new IllegalArgumentException("You must supply a message");
			
		UserMessage userMessage = new UserMessage(currentUser.getId(),message);
		
		synchronized(messageMap) {
			List<UserMessage> current = messageMap.get(uuid);
		
			if(current != null) {
				List<UserMessage> copy = new ArrayList<UserMessage>(current.size());
				copy.addAll(current);
				copy.add(userMessage);
				messageMap.put(uuid, copy);
			}
			else {
				messageMap.put(uuid, Arrays.asList(userMessage));
			}
		}
		
		return "success";
	}

	@Override
	public String[] getHandledInputFormats() {
	    return new String[] { Formats.HTML };
	}
	
	public class UserMessage {
		
		public String from;
		public String content;
		public long timestamp;
		
		private UserMessage() {
		}

		private UserMessage(String from, String content) {
			this.from = from;
			this.content = content;
			this.timestamp = (new Date()).getTime();
		}
	}

	@EntityCustomAction(action = "latestData", viewKey = EntityView.VIEW_SHOW)
	public Map<String,Object> handleLatestData(EntityReference ref, Map<String,Object> params) {
		
		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if(anon.equals(currentUser))
			throw new SecurityException("You must be logged in to use this service");
		
		String online = (String) params.get("online");
		
		if("true".equals(online)) {
			heartbeatMap.put(currentUser.getId(),new Date());
		}
		else {
			synchronized(messageMap) {
				messageMap.remove(currentUser.getId());
			}
			
			return new HashMap<String,Object>(0);
		}

        List<User> presentUsers = new ArrayList<User>();

		String siteId = (String) params.get("siteId");

        if(siteId != null && siteId.length() > 0) {
            // A site id has been specified, so we add the present users from the presence service
            presentUsers = presenceService.getPresentUsers(siteId + "-presence");
            presentUsers.remove(currentUser);
        }
		
		List<Person> connections =  profileService.getConnectionsForUser(currentUser.getId());
		
		List<String> onlineConnections = new ArrayList<String>(connections.size());
		
		Date now = new Date();
		
		for(Person person : connections) {
			
			Date lastHeartbeat = null;
			
			lastHeartbeat = heartbeatMap.get(person.getUuid());
			
			if(lastHeartbeat == null) continue;
			
			if((now.getTime() - lastHeartbeat.getTime()) < 5000L) {
				onlineConnections.add(person.getUuid());
			}
		}
		
		List<UserMessage> messages = new ArrayList<UserMessage>();
		
		String currentUserId = currentUser.getId();
		
		synchronized(messageMap) {
			if(messageMap.containsKey(currentUserId)) {
				messages = messageMap.get(currentUserId);
				messageMap.remove(currentUserId);
			}
		}
		
		Map<String,Object> data = new HashMap<String,Object>(4);
		
		data.put("connections", connections);
		data.put("messages", messages);
		data.put("online", onlineConnections);
		data.put("presentUsers", presentUsers);
		
		return data;
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
			String portalUrl = serverConfigurationService.getServerUrl() + "/portal";
			new EmailSender(email,"[Sakai Chat] Chat Invitation",currentUser.getDisplayName() + " wants you to come and chat on <a href=\"" + portalUrl + "\">Sakai!</a>");
		}
		catch(Exception e) {
			throw new EntityException("Failed to send email",userId);
		}
		
		return "success";
	}

	private class EmailSender implements Runnable {
		private Thread runner;

		private String email;

		private String subject;

		private String message;

		public final String HTML_END = "\n  </body>\n</html>\n";

		public EmailSender(String email, String subject, String message)
		{
			this.email = email;
			this.subject = subject;
			this.message = message;
			runner = new Thread(this, "PC EmailSender thread");
			runner.start();
		}

		// do it!
		public synchronized void run()
		{
			try
			{
				final List<String> additionalHeaders = new ArrayList<String>();
				additionalHeaders.add("Content-Type: text/html; charset=ISO-8859-1");

				// do it
				final String emailFromAddress = "\""+serverConfigurationService.getString("ui.service") + "\" <no-reply@" + serverConfigurationService.getServerName()+">";
				emailService.send(emailFromAddress, email, subject, formatMessage(subject, message), email, null, additionalHeaders);
			}
			catch (Exception e)
			{
				logger.error("sendEmail() failed for emailuserId: " + email + " : " + e.getClass() + " : " + e.getMessage());
			}
		}

		/** helper methods for formatting the message */
		private String formatMessage(String subject, String message)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(htmlPreamble(subject));
			sb.append(message);
			sb.append(HTML_END);
			return sb.toString();
		}

		private String htmlPreamble(String subject)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
			sb.append("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
			sb.append("<html>\n");
			sb.append("<head><title>");
			sb.append(subject);
			sb.append("</title></head>\n");
			sb.append("<body>\n");

			return sb.toString();
		}
	}
}
