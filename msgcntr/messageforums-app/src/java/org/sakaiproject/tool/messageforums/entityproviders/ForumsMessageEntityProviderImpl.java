package org.sakaiproject.tool.messageforums.entityproviders;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.tool.messageforums.entityproviders.sparsepojos.*;
import org.sakaiproject.tool.messageforums.entityproviders.utils.MessageUtils;

public class ForumsMessageEntityProviderImpl extends BaseEntityProvider implements CoreEntityProvider, Outputable, Resolvable, AutoRegisterEntityProvider, ActionsExecutable, Describeable {
	
	private static final Log LOG = LogFactory.getLog(ForumsMessageEntityProviderImpl.class);

	public final static String ENTITY_PREFIX = "forums-message";

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		
		Message message = null;
		try {
			message = forumManager.getMessageById(Long.valueOf(id));
		} catch (Exception e) {
			LOG.error("Failed to get message with id '" + id +"'",e);
		}
		return (message != null);
	}

	/**
	 * Implements the functionality for getting a particular message and its replies.
	 */
	public Object getEntity(EntityReference ref) {
		
		String userId = developerHelperService.getCurrentUserId();
		
		if(userId == null) {
			throw new EntityException("You must be logged in to retrieve messages.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		Long messageId = -1L;
		
		try {
			messageId = Long.parseLong(ref.getId());
		} catch(NumberFormatException nfe) {
			throw new EntityException("The thread id must be an integer.","",HttpServletResponse.SC_BAD_REQUEST);
		}
		
		Message fatMessage = forumManager.getMessageById(messageId);
		
		Topic fatTopic = forumManager.getTopicByIdWithMessagesAndAttachments(fatMessage.getTopic().getId());
		
		String siteId = forumManager.getContextForTopicById(fatTopic.getId());
        checkSiteAndToolAccess(siteId);
		
		// This sets the attachments on the message.We have to do this as
        // getMessageById doesn't populate the attachments.
		setAttachments(fatMessage,fatTopic.getMessages());
		
		if(!uiPermissionsManager.isRead(fatTopic.getId(),((DiscussionTopic)fatTopic).getDraft(),false,userId,forumManager.getContextForTopicById(fatTopic.getId()))) {
			throw new EntityException("You are not authorised to read this message.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		List<SparseMessage> messages = new ArrayList<SparseMessage>();
		
		for(Message fm : (List<Message>) fatTopic.getMessages()) {
			messages.add(new SparseMessage(fm,/* readStatus =*/ false,/* addAttachments =*/ true, developerHelperService.getServerURL()));
		}
		
		SparseMessage sparseThread = new SparseMessage(fatMessage,false,/* readStatus =*/ true,developerHelperService.getServerURL());
		
		new MessageUtils().attachReplies(sparseThread,messages, forumManager, userId);
		
		return sparseThread;
	}

	public String[] getHandledOutputFormats() {
		return new String[] {Formats.JSON,Formats.XML};
	}
	
	/**
	 * This is a dirty hack to set the attachments on the message. There doesn't seem
	 * to be an api for getting a single message with all attachments. If you try and retrieve
	 * them after, hibernate, wonderful framework that it is, throws a lazy exception.
	 * 
	 * @param unPopulatedMessage The message we want to set attachments on
	 * @param populatedMessages The list of populated messages retrieved from the forum manager
	 */
	private void setAttachments(Message unPopulatedMessage, List<Message> populatedMessages) {
		
		for(Message populatedMessage : populatedMessages) {
			if(populatedMessage.getId().equals(unPopulatedMessage.getId())
					&& populatedMessage.getHasAttachments()) {
				unPopulatedMessage.setAttachments(populatedMessage.getAttachments());
				break;
			}
		}
	}
}
