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

public class ForumsTopicEntityProviderImpl extends BaseEntityProvider implements CoreEntityProvider, Outputable, Resolvable, AutoRegisterEntityProvider, ActionsExecutable, Describeable {
	
	private static final Log LOG = LogFactory.getLog(ForumsTopicEntityProviderImpl.class);

	public final static String ENTITY_PREFIX = "forums-topic";

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		
		Topic topic = null;
		try {
			topic = forumManager.getTopicById(Long.valueOf(id));
		} catch (Exception e) {
			LOG.error("Failed to get topic with id '" + id +"'",e);
		}
		return (topic != null);
	}

	public Object getEntity(EntityReference ref) {
		
		String userId = developerHelperService.getCurrentUserId();
		
		if(userId == null) {
			throw new EntityException("You must be logged in to retrieve topics.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		Long topicId = -1L;
		
		try {
			topicId = Long.parseLong(ref.getId());
		} catch(NumberFormatException nfe) {
			throw new EntityException("The topic id must be an integer.","",HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// This call gets the attachments for the messages but not the topic. Unexpected, yes. Cool, not.
		Topic fatTopic = forumManager.getTopicByIdWithMessagesAndAttachments(topicId);
		
		String siteId = forumManager.getContextForTopicById(topicId);
		
        checkSiteAndToolAccess(siteId);
		
		if(!uiPermissionsManager.isRead(topicId,((DiscussionTopic)fatTopic).getDraft(),false,userId,forumManager.getContextForTopicById(topicId))) {
			throw new EntityException("You are not authorised to read this topic.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		SparseTopic sparseTopic = new SparseTopic(fatTopic);
		
		// Setup the total and read message counts on the topic
		List<Long> topicIds = new ArrayList<Long>();
		topicIds.add(fatTopic.getId());
		
		List<Object[]> totalCounts = forumManager.getMessageCountsForMainPage(topicIds);
		if(totalCounts.size() > 0) {
			sparseTopic.setTotalMessages((Integer) totalCounts.get(0)[1]);
		} else {
			sparseTopic.setTotalMessages(0);
		}
		
		List<Object[]> readCounts = forumManager.getReadMessageCountsForMainPage(topicIds);
		if(readCounts.size() > 0) {
			sparseTopic.setReadMessages((Integer) readCounts.get(0)[1]);
		} else {
			sparseTopic.setReadMessages(0);
		}
		
		List<SparseMessage> messages = new ArrayList<SparseMessage>();
		for(Message fatMessage : (List<Message>) fatTopic.getMessages()) {
			SparseMessage sparseMessage = new SparseMessage(fatMessage,/* readStatus = */ false,/* addAttachments = */ true,developerHelperService.getServerURL());
			messages.add(sparseMessage);
		}
		
		List<SparseThread> threads = new MessageUtils().getThreadsWithCounts(messages, forumManager, userId);
		
		sparseTopic.setThreads(threads);
		
		return sparseTopic;
	}

	public String[] getHandledOutputFormats() {
		return new String[] {Formats.JSON,Formats.XML};
	}
}
