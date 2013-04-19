package org.sakaiproject.tool.messageforums.entityproviders;

import java.util.*;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.tool.messageforums.entityproviders.sparsepojos.*;

/**
 * Provides the forums entity provider. 
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class ForumsForumEntityProviderImpl extends BaseEntityProvider implements CoreEntityProvider, Outputable, Resolvable, AutoRegisterEntityProvider, ActionsExecutable, Describeable {
	
	private static final Log LOG = LogFactory.getLog(ForumsForumEntityProviderImpl.class);

	public final static String ENTITY_PREFIX = "forums-forum";

	@Setter
	private SecurityService securityService;

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		
		DiscussionForum forum = null;
		try {
			forum = forumManager.getForumById(Long.valueOf(id));
		} catch (Exception e) {
			LOG.error("Failed to get forum with id '" + id +"'",e);
		}
		return (forum != null);
	}
	
	/**
	 * This will return a SparseForum populated down to the topics with their
	 * attachments.
	 */
	public Object getEntity(EntityReference ref) {
		
		String userId = developerHelperService.getCurrentUserId();
		
		if(userId == null) {
			throw new EntityException("You must be logged in to retrieve fora.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		Long forumId = -1L;
		
		try {
			forumId = Long.parseLong(ref.getId());
		} catch(NumberFormatException nfe) {
			throw new EntityException("The forum id must be an integer.","",HttpServletResponse.SC_BAD_REQUEST);
		}
		
		String siteId = forumManager.getContextForForumById(forumId);
		
        checkSiteAndToolAccess(siteId);
		
		DiscussionForum fatForum = forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forumId);
		
		if(checkAccess(fatForum,userId)) {
			
			SparseForum sparseForum = new SparseForum(fatForum,developerHelperService);
			
			List<DiscussionTopic> fatTopics = (List<DiscussionTopic>) fatForum.getTopics();
			
			// Gather all the topic ids so we can make the minimum number
			// of calls for the message counts.
			List<Long> topicIds = new ArrayList<Long>();
			for(DiscussionTopic topic : fatTopics) {
				topicIds.add(topic.getId());
			}
				
			List<Object[]> topicTotals = forumManager.getMessageCountsForMainPage(topicIds);
			List<Object[]> topicReadTotals = forumManager.getReadMessageCountsForMainPage(topicIds);
			
			int totalForumMessages = 0;
			for(Object[] topicTotal : topicTotals) {
				totalForumMessages += (Integer) topicTotal[1];
			}
			sparseForum.setTotalMessages(totalForumMessages);
				
			int totalForumReadMessages = 0;
			for(Object[] topicReadTotal : topicReadTotals) {
				totalForumReadMessages += (Integer) topicReadTotal[1];
			}
			sparseForum.setReadMessages(totalForumReadMessages);
			
			// Reduce the fat topics to sparse topics while setting the total and read
			// counts. A SparseTopic will only be created if the currrent user has read access.
			List<SparsestTopic> sparseTopics = new ArrayList<SparsestTopic>();
			for(DiscussionTopic fatTopic : fatTopics) {
				
				// Only add this topic to the list if the current user has read permission
				if( ! uiPermissionsManager.isRead(fatTopic,fatForum,userId,siteId)) {
					// No read permission, skip this topic.
					continue;
				}
				
				SparsestTopic sparseTopic = new SparsestTopic(fatTopic);
				for(Object[] topicTotal : topicTotals) {
					if(topicTotal[0].equals(sparseTopic.getId())) {
						sparseTopic.setTotalMessages((Integer)topicTotal[1]);
					}
				}
				for(Object[] topicReadTotal : topicReadTotals) {
					if(topicReadTotal[0].equals(sparseTopic.getId())) {
						sparseTopic.setReadMessages((Integer)topicReadTotal[1]);
					}
				}
				
				List<SparseAttachment> attachments = new ArrayList<SparseAttachment>();
				for(Attachment attachment : (List<Attachment>) fatTopic.getAttachments()) {
					String url = developerHelperService.getServerURL() + "/access/content" + attachment.getAttachmentId();
					attachments.add(new SparseAttachment(attachment.getAttachmentName(),url));
				}
				sparseTopic.setAttachments(attachments);
				
				sparseTopics.add(sparseTopic);
			}
			
			sparseForum.setTopics(sparseTopics);
			
			return sparseForum;
		} else {
			throw new EntityException("You are not authorised to access this forum.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	/**
	 * This will return a list of SparseForum populated just to forum level, but with counts setup.
	 * 
	 * @param view
	 * @param params
	 * @return
	 */
	@EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
    public List<?> getForumsInSite(EntityView view, Map<String, Object> params) {
		
		String userId = developerHelperService.getCurrentUserId();
		
		if(userId == null) {
			throw new EntityException("You must be logged in to retrieve fora.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
        String siteId = view.getPathSegment(2);
        
        if(siteId == null) {
        	throw new EntityException("Bad request: To get the fora in a site you need a url like '/direct/forum/site/SITEID.json'"
        									,"",HttpServletResponse.SC_BAD_REQUEST);
        }
        
        checkSiteAndToolAccess(siteId);
		
		List<SparsestForum> sparseFora = new ArrayList<SparsestForum>();
		
		List<DiscussionForum> fatFora = forumManager.getDiscussionForumsWithTopics(siteId);
		
		for(DiscussionForum fatForum : fatFora) {
			
			if( ! checkAccess(fatForum,userId)) {
				// TODO: Log this rejected access attempt
				continue;
			}
				
			List<Long> topicIds = new ArrayList<Long>();
			for(Topic topic : (List<Topic>) fatForum.getTopics()) {
				topicIds.add(topic.getId());
			}
			
			List<Object[]> topicTotals = forumManager.getMessageCountsForMainPage(topicIds);
			List<Object[]> topicReadTotals = forumManager.getReadMessageCountsForMainPage(topicIds);
		
			SparsestForum sparseForum = new SparsestForum(fatForum,developerHelperService);
			
			int totalForumMessages = 0;
			for(Object[] topicTotal : topicTotals) {
				totalForumMessages += (Integer) topicTotal[1];
			}
			sparseForum.setTotalMessages(totalForumMessages);
			
			int totalForumReadMessages = 0;
			for(Object[] topicReadTotal : topicReadTotals) {
				totalForumReadMessages += (Integer) topicReadTotal[1];
			}
			sparseForum.setReadMessages(totalForumReadMessages);
		
			sparseFora.add(sparseForum);
		}
		
		return sparseFora;
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.JSON,Formats.XML};
	}
	
	private boolean checkAccess(BaseForum baseForum, String userId) {
		
		if(baseForum instanceof OpenForum) {
			
			// If the supplied user is the super user, return true.
			if(securityService.isSuperUser(userId)) {
				return true;
			}
			
			OpenForum of = (OpenForum) baseForum;
			
			// If this is not a draft and is available, return true.
			if(!of.getDraft() && of.getAvailability()) {
				return true;
			}
			
			// If this is a draft/unavailable forum AND was authored by the current user, return true.
			if((of.getDraft() || !of.getAvailability()) && of.getCreatedBy().equals(userId)) {
				return true;
			}
		}
		else if(baseForum instanceof PrivateForum) {
			PrivateForum pf = (PrivateForum) baseForum;
			// If the current user is the creator, return true.
			if(pf.getCreatedBy().equals(userId)) {
				return true;
			}
		}
		
		return false;
	}
}
