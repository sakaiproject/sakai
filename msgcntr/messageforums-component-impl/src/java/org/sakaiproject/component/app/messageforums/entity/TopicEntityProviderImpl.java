/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.entity.DecoratedAttachment;
import org.sakaiproject.api.app.messageforums.entity.DecoratedForumInfo;
import org.sakaiproject.api.app.messageforums.entity.DecoratedTopicInfo;
import org.sakaiproject.api.app.messageforums.entity.ForumMessageEntityProvider;
import org.sakaiproject.api.app.messageforums.entity.TopicEntityProvider;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class TopicEntityProviderImpl implements TopicEntityProvider,
AutoRegisterEntityProvider, PropertyProvideable, RESTful, RequestStorable, RequestAware, ActionsExecutable {

	private DiscussionForumManager forumManager;
	private UIPermissionsManager uiPermissionsManager;
	private MessageForumsMessageManager messageManager;
	private MessageForumsTypeManager typeManager;
	private PrivateMessageManager privateMessageManager;
	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.api.app.messagecenter.bundle.Messages");
	public static final String PVTMSG_MODE_DRAFT = "Drafts";
	
	private UserDirectoryService userDirectoryService;	
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private EntityBrokerManager entityBrokerManager;

	public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
		this.entityBrokerManager = entityBrokerManager;
	}
	
	private RequestStorage requestStorage;
	public void setRequestStorage(RequestStorage requestStorage) {
		this.requestStorage = requestStorage;
	}

	private RequestGetter requestGetter;
    public void setRequestGetter(RequestGetter requestGetter){
    	this.requestGetter = requestGetter;
    }
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		Topic topic = null;
		try {
			topic = forumManager.getTopicById(new Long(id));
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return (topic != null);
	}

	public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
			boolean exactMatch) {
		List<String> rv = new ArrayList<String>();

		String forumId = null;
		String userId = null;
		String siteId = null;

		if (ENTITY_PREFIX.equals(prefixes[0])) {

			for (int i = 0; i < name.length; i++) {
				if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
					siteId = searchValue[i];
				else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
					userId = searchValue[i];
				else if ("parentReference".equalsIgnoreCase(name[i])) {
					String[] parts = searchValue[i].split("/");
					forumId = parts[parts.length - 1];
				}
			}
			String siteRef = siteId;
			if(siteRef != null && !siteRef.startsWith("/site/")){
				siteRef = "/site/" + siteRef;
			}
			// TODO: need a way to generate the url with out having siteId in search
			if (forumId != null && userId != null) {
				DiscussionForum forum = forumManager.getForumByIdWithTopics(new Long(forumId));
				List<Topic> topics = forum.getTopics();
				for (int i = 0; i < topics.size(); i++) {
					// TODO: authz is way too basic, someone more hip to message center please improve...
					//This should also allow people with read access to an item to link to it
					if (forumManager.isInstructor(userId, siteRef)
							|| userId.equals(topics.get(i).getCreatedBy()))
						rv.add("/" + ENTITY_PREFIX + "/" + topics.get(i).getId().toString());
				}
			}
			else if (siteId != null && userId != null) {
				List<DiscussionForum> forums = forumManager.getDiscussionForumsByContextId(siteId);
				for (int i = 0; i < forums.size(); i++) {
					List<Topic> topics = forums.get(i).getTopics();
					for (int j = 0; j < topics.size(); j++) {
						// TODO: authz is way too basic, someone more hip to message center please improve...
						//This should also allow people with read access to an item to link to it
						if (forumManager.isInstructor(userId, siteRef)
								|| userId.equals(topics.get(j).getCreatedBy()))
							rv.add("/" + ENTITY_PREFIX + "/" + topics.get(j).getId().toString());
					}
				}
			}
		}

		return rv;
	}

	public Map<String, String> getProperties(String reference) {
		Map<String, String> props = new HashMap<String, String>();
		Topic topic =
			forumManager.getTopicById(new Long(reference.substring(reference.lastIndexOf("/") + 1)));

		props.put("title", topic.getTitle());
		props.put("author", topic.getCreatedBy());
		if (topic.getCreated() != null)
			props.put("date", DateFormat.getInstance().format(topic.getCreated()));
		if (topic.getModified() != null) {
			props.put("modified_by", topic.getModifiedBy());
			props.put("modified_date", DateFormat.getInstance().format(topic.getModified()));
		}
		props.put("short_description", topic.getShortDescription());
		props.put("description", topic.getExtendedDescription());
		if (topic.getModerated() != null)
			props.put("moderated", topic.getModerated().toString());
		props.put("child_provider", ForumMessageEntityProvider.ENTITY_PREFIX);

		return props;
	}

	public String getPropertyValue(String reference, String name) {
		// TODO: don't be so lazy, just get what we need...
		Map<String, String> props = getProperties(reference);
		return props.get(name);
	}

	public void setPropertyValue(String reference, String name, String value) {
		// This does nothing for now... we could all the setting of many published assessment properties
		// here though... if you're feeling jumpy feel free.
	}

	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSampleEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	public Object getEntity(EntityReference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	public List<?> getEntities(EntityReference ref, Search search) {
		String siteId = null;
		String forumId = null;
		boolean privateMessages = false;
		if (! search.isEmpty()) {
			Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE);
            if (locRes != null) {
                ForumSearch fs = findLocationByReference(locRes.getStringValue());
                siteId = fs.siteId;
                forumId = fs.forumId;
                privateMessages = fs.isPrivate;
            }
		}		
		
		String userId = userDirectoryService.getCurrentUser().getId();
		if(userId == null || "".equals(userId)){
			return null;
		}

		List<DecoratedForumInfo> dForums = new ArrayList<DecoratedForumInfo>();


			if(privateMessages && siteId != null){
				
				DecoratedForumInfo dForum = new DecoratedForumInfo(0L, "Messages", new ArrayList<DecoratedAttachment>(), "", "");
				Area area = getPrivateMessageManager().getPrivateMessageArea(siteId);

				if (area != null){    
					List aggregateList = new ArrayList();
					PrivateForum pf = getPrivateMessageManager().initializePrivateMessageArea(area, aggregateList, userId, siteId);
					pf = getPrivateMessageManager().initializationHelper(pf, area, userId);
					List pvtTopics = pf.getTopics();
					Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator
					




					List topicsbyLocalization= new ArrayList();// only three folder supported, if need more, please modifify here

					String local_received=rb.getString("pvt_received");
					String local_sent = rb.getString("pvt_sent");
					String local_deleted= rb.getString("pvt_deleted");

					String current_NAV= rb.getString("pvt_message_nav");

					topicsbyLocalization.add(local_received);
					topicsbyLocalization.add(local_sent);
					topicsbyLocalization.add(local_deleted);

					int countForFolderNum = 0;// only three folder 
					Iterator iterator = pvtTopics.iterator(); 
					for (int indexlittlethanTHREE=0;indexlittlethanTHREE<3;indexlittlethanTHREE++)//Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)//only three times
					{

						PrivateTopic topic = (PrivateTopic) iterator.next();

						if (topic != null)
						{
						    String CurrentTopicTitle= topic.getTitle();//folder name

							/** filter topics by context and type*/                                                    
							if (topic.getTypeUuid() != null
									&& topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
									&& topic.getContextId() != null && !topic.getContextId().equals(getPrivateMessageManager().getContextId())){
								continue;
							}       



							String typeUuid="";  // folder uuid
							if(getLanguage(CurrentTopicTitle).toString().equals(getLanguage(current_NAV).toString()))
							{
								typeUuid = getPrivateMessageTypeFromContext(topicsbyLocalization.get(countForFolderNum).toString());


							}
							else
							{

								typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());

							}
							countForFolderNum++;

							int totalNoMessages = getPrivateMessageManager().findMessageCount(typeUuid, aggregateList);

							int totalUnreadMessages = getPrivateMessageManager().findUnreadMessageCount(typeUuid, aggregateList);

                            // in this context, the topics are the folders in the Messages tool
                            // they will never have attachments
                            List<DecoratedAttachment> attachments = new ArrayList<DecoratedAttachment>();

                            DecoratedTopicInfo dTopicInfo = new DecoratedTopicInfo(topic.getId(), topic.getTitle(), totalUnreadMessages, totalNoMessages, typeUuid, attachments, topic.getShortDescription(), topic.getExtendedDescription());
							
							dForum.addTopic(dTopicInfo);
						}

					}

					while(iterator.hasNext())//add more folder 
					{
						PrivateTopic topic = (PrivateTopic) iterator.next();
						if (topic != null)
						{


							/** filter topics by context and type*/                                                    
							if (topic.getTypeUuid() != null
									&& topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
									&& topic.getContextId() != null && !topic.getContextId().equals(siteId)){
								continue;
							}       

							String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());          

							int totalNoMessages = getPrivateMessageManager().findMessageCount(typeUuid, aggregateList);
							int totalUnreadMessages = getPrivateMessageManager().findUnreadMessageCount(typeUuid,aggregateList);

                            // in this context, the topics are the folders in the Messages tool
                            // they will never have attachments
                            List<DecoratedAttachment> attachments = new ArrayList<DecoratedAttachment>();

                            DecoratedTopicInfo dTopicInfo = new DecoratedTopicInfo(topic.getId(), topic.getTitle(), totalUnreadMessages, totalNoMessages, typeUuid, attachments, topic.getShortDescription(), topic.getExtendedDescription());							
							dForum.addTopic(dTopicInfo);
						}          

					}
				} 

				dForums.add(dForum);
			}else if ((siteId != null && !"".equals(siteId)) || (forumId != null && !"".equals(forumId))) {

				List<DiscussionForum> forums = new ArrayList<DiscussionForum>();
				if(forumId != null && !"".equals(forumId)){
					DiscussionForum forum = forumManager.getForumByIdWithTopicsAttachmentsAndMessages(new Long(forumId));					
					siteId = forumManager.getContextForForumById(forum.getId());
					forums.add(forum);
				}else{
					forums = forumManager.getDiscussionForumsWithTopics(siteId);
				}
				
				// retrieve all of the gradebook items here so we aren't checking repeatedly
				Map<String, Long> gbItemNameToId = new HashMap<String, Long>();
				try {
				    GradebookService gradebookService = (GradebookService)ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
				    List<Assignment> gbItems = gradebookService.getAssignments(siteId);
				    if (gbItems != null) {
				        for (Assignment gbItem : gbItems) {
				            gbItemNameToId.put(gbItem.getName(), gbItem.getId());
				        }
				    }
				} catch (GradebookNotFoundException gnfe) {
				    log.debug("No gradebook exists for site " + siteId + ". No gb item ids will be included.", gnfe);
				} catch (Exception e) {
				    log.debug("Exception attempting to retrieve gradebook information for site " + siteId + ". ", e);
				}

				for (DiscussionForum forum : forums) {
						List<DecoratedAttachment> forumAttachments = decorateAttachments(forum.getAttachments());
					        Long forumOpenDate = null;
					        Long forumCloseDate = null;
					        if (forum.getAvailabilityRestricted()) {
					            forumOpenDate = forum.getOpenDate() != null ? forum.getOpenDate().getTime()/1000 : null; 
					            forumCloseDate = forum.getCloseDate() != null ? forum.getCloseDate().getTime()/1000 : null;
					        }
					        
					        Long forumGbItemId = null;
					        if (forum.getDefaultAssignName() != null && !forum.getDefaultAssignName().isEmpty() &&
					                gbItemNameToId.containsKey(forum.getDefaultAssignName())) {
					            forumGbItemId = gbItemNameToId.get(forum.getDefaultAssignName());
					        }
					             
					        DecoratedForumInfo dForum = new DecoratedForumInfo(forum.getId(), forum.getTitle(), forumAttachments, forum.getShortDescription(), forum.getExtendedDescription(), forum.getLocked(),forum.getDraft(), 
                                    forum.getPostFirst(), forum.getAvailabilityRestricted(), forumOpenDate, forumCloseDate, forum.getDefaultAssignName(), forumGbItemId);
						List<DiscussionTopic> topics = forum.getTopics();
						int viewableTopics = 0;

						for (DiscussionTopic topic : topics) {

								if (forumManager.isInstructor(userId, siteId) || 
										getUiPermissionsManager().isRead(topic.getId(), topic.getDraft(), forum.getDraft(), userId, siteId))
								{
									int unreadMessages = 0;
									int totalMessages = 0;
									if (!topic.getModerated().booleanValue()
											|| (topic.getModerated().booleanValue() && 
													getUiPermissionsManager().isModeratePostings(topic.getId(), forum.getLocked(), forum.getDraft(), topic.getLocked(), topic.getDraft(), userId, siteId))){

										unreadMessages = getMessageManager().findUnreadMessageCountByTopicIdByUserId(topic.getId(), userId);										
									}
									else
									{	
										// b/c topic is moderated and user does not have mod perm, user may only
										// see approved msgs or pending/denied msgs authored by user
										unreadMessages = getMessageManager().findUnreadViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
									}
									totalMessages = getMessageManager().findViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
									
									List<DecoratedAttachment> attachments = decorateAttachments(topic.getAttachments());									
									Long topicOpenDate = null;
									Long topicCloseDate = null;
									if (topic.getAvailabilityRestricted()) {
									    topicOpenDate = topic.getOpenDate() != null ? topic.getOpenDate().getTime()/1000 : null; 
									    topicCloseDate = topic.getCloseDate() != null ? topic.getCloseDate().getTime()/1000 : null;
									}
									
									Long topicGbItemId = null;
									if (topic.getDefaultAssignName() != null && !topic.getDefaultAssignName().isEmpty() &&
											gbItemNameToId.containsKey(topic.getDefaultAssignName())) {
										topicGbItemId = gbItemNameToId.get(topic.getDefaultAssignName());
									}
									
									dForum.addTopic(new DecoratedTopicInfo(topic.getId(), topic.getTitle(), unreadMessages, 
											totalMessages, "", attachments, topic.getShortDescription(), topic.getExtendedDescription(), topic.getLocked(), topic.getDraft(), topic.getPostFirst(), topic.getAvailabilityRestricted(), 
                                            topicOpenDate, topicCloseDate, topic.getDefaultAssignName(), topicGbItemId));
									viewableTopics++;
								}						  
						}
						
						// TODO this is a bit too simplistic but will do for now. better to be more restrictive than less at this point
						// "instructor" type users can view all forums. others may view the forum if they can view at least one topic within the forum
						if (forumManager.isInstructor(userId, siteId) || viewableTopics > 0) {
							dForums.add(dForum);
						}
				}
			}
		

		return dForums;
	}
	
	@EntityCustomAction(action="forum",viewKey=EntityView.VIEW_LIST)
    public List<?> getForum(EntityView view, Map<String, Object> params) {
        String forumId = view.getPathSegment(2);
        if (forumId == null) {
        	forumId = (String) params.get("forumId");
            if (forumId == null) {
                throw new IllegalArgumentException("forumId must be set in order to get the forum info, set in params or in the URL /topic/forum/forumId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/forum/" + forumId));
        return l;
    }
	
	@EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
    public List<?> getForumsInSite(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
        	siteId = (String) params.get("site");
            if (siteId == null) {
                throw new IllegalArgumentException("site must be set in order to get the forum info, set in params or in the URL /topic/site/siteId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/site/" + siteId));
        return l;
    }
	
	@EntityCustomAction(action="private",viewKey=EntityView.VIEW_LIST)
    public List<?> getPrivateTopicsInSite(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
        	siteId = (String) params.get("private");
            if (siteId == null) {
                throw new IllegalArgumentException("siteId must be set in order to get the private topic info, set in params or in the URL /topic/private/siteId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/private/" + siteId));
        return l;
    }
	
	/**
     * Find a site (and optionally group) by reference
     * @param locationReference
     * @return a Site and optional group
     * @throws IllegalArgumentException if they cannot be found for this ref
     */
    public ForumSearch findLocationByReference(String locationReference) {
    	ForumSearch holder = new ForumSearch(locationReference);
        if (locationReference.contains("/forum/")) {
            // group membership
        	EntityReference ref = new EntityReference(locationReference);
        	String forumId = ref.getId();
            holder.forumId = forumId;
        } else if (locationReference.contains("/site/")) {
            // site membership
            EntityReference ref = new EntityReference(locationReference);
            String siteId = ref.getId();            
            holder.siteId = siteId;
        } else if(locationReference.contains("/private/")){
        	EntityReference ref = new EntityReference(locationReference);
            String siteId = ref.getId();            
            holder.siteId = siteId;
            holder.isPrivate = true;
        }else{
            throw new IllegalArgumentException("Do not know how to handle this location reference ("+locationReference+"), only can handle forum, site and private references");
        }
        return holder;
    }
    
    public static class ForumSearch{
    	public String forumId;
    	public String siteId;
    	public boolean isPrivate = false;
    	public String locationReference;
        public ForumSearch(String locationReference) {
            this.locationReference = locationReference;
        }
    }

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public UIPermissionsManager getUiPermissionsManager() {
		return uiPermissionsManager;
	}

	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}

	public MessageForumsMessageManager getMessageManager() {
		return messageManager;
	}

	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	public PrivateMessageManager getPrivateMessageManager() {
		return privateMessageManager;
	}


	public void setPrivateMessageManager(PrivateMessageManager privateMessageManager) {
		this.privateMessageManager = privateMessageManager;
	}
	private String getLanguage(String navName)
	{
		String Tmp= "";
		//getLocale( String userId )
		
		//( userId);//SessionManager.getCurrentSessionUserId() );

		//  List topicsbyLocalization= new ArrayList();// only three folder supported, if need more, please modifify here

		//
		//		  topicsbyLocalization.add(local_received);
		//		  topicsbyLocalization.add(local_sent);
		//		  topicsbyLocalization.add(local_deleted);

		

		if(navName.equals("Received")||navName.equals("Sent")||navName.equals("Deleted"))
		{
			Tmp = "en";
		}
		else if(navName.equals("Recibidos")||navName.equals("Enviados")||navName.equals("Borrados"))
		{

			Tmp ="es";

		}


		else//english language
		{		  
			Tmp="en";		  
		}

		return Tmp;	  
	}

	private String getPrivateMessageTypeFromContext(String navMode){    

		List<String> topicsbyLocalization= new ArrayList<String>();// only three folder supported, if need more, please modifify here


		String local_received=rb.getString("pvt_received");
		String local_sent = rb.getString("pvt_sent");
		String local_deleted= rb.getString("pvt_deleted");
		

		topicsbyLocalization.add(local_received);
		topicsbyLocalization.add(local_sent);
		topicsbyLocalization.add(local_deleted);

		//need to add more dictionary to support more language
		if (((String) topicsbyLocalization.get(0)).equalsIgnoreCase(navMode)||"Recibidos".equalsIgnoreCase(navMode)||"Received".equalsIgnoreCase(navMode)){
			return typeManager.getReceivedPrivateMessageType();
		}
		else if (((String) topicsbyLocalization.get(1)).equalsIgnoreCase(navMode)||"Enviados".equalsIgnoreCase(navMode)||"Sent".equalsIgnoreCase(navMode)){
			return typeManager.getSentPrivateMessageType();
		}
		else if (((String) topicsbyLocalization.get(2)).equalsIgnoreCase(navMode)||"Borrados".equalsIgnoreCase(navMode)||"Deleted".equalsIgnoreCase(navMode)){
			return typeManager.getDeletedPrivateMessageType(); 
		}
		else if (PVTMSG_MODE_DRAFT.equalsIgnoreCase(navMode)){
			return typeManager.getDraftPrivateMessageType();
		}
		else{
			return typeManager.getCustomTopicType(navMode);
		}    	  
	}

	public MessageForumsTypeManager getTypeManager() {
		return typeManager;
	}


	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}
	
    /**
     * Return a list of DecoratedAttachment objects
     * @param attachments List of Attachment objects
     * @return
     */
    private List<DecoratedAttachment> decorateAttachments(List<Attachment> attachments) {
        List<DecoratedAttachment> decoAttachments = new ArrayList<DecoratedAttachment>();
        for(Attachment attachment : attachments){
            DecoratedAttachment da = new DecoratedAttachment();
            da.setName(attachment.getAttachmentName());
            da.setId(attachment.getAttachmentId());
            da.setType(attachment.getAttachmentType());

            Reference ref = entityManager.newReference("/content" + attachment.getAttachmentId());
            String context = entityBrokerManager.getServletContext();
            String url = ServerConfigurationService.getServerUrl() + "/access/" +  ref.getEntity().getReference();
            da.setUrl(url);

            da.setRef(ref.getEntity().getReference());

            decoAttachments.add(da);
        }
        return decoAttachments;
    }


}
