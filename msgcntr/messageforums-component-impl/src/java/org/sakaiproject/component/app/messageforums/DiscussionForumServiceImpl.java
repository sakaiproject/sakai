/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/DiscussionForumServiceImpl.java $
 * $Id: DiscussionForumServiceImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.tool.api.SessionManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.cover.LinkMigrationHelper;

@Slf4j
public class DiscussionForumServiceImpl  implements DiscussionForumService, EntityTransferrer, EntityTransferrerRefMigrator
{
	private static final String MESSAGEFORUM = "messageforum";
	private static final String DISCUSSION_FORUM = "discussion_forum";
	private static final String DISCUSSION_TOPIC = "discussion_topic";
	private static final String DISCUSSION_FORUM_TITLE = "category";
	private static final String DISCUSSION_FORUM_DESC = "body";
	private static final String DISCUSSION_FORUM_SHORT_DESC = "summary";
	private static final String TOPIC_TITLE = "subject";
	private static final String DRAFT = "draft";
	private static final String LOCKED = "locked";
	private static final String MODERATED = "moderated";
	private static final String POST_FIRST = "post_first";
	private static final String SORT_INDEX = "sort_index";
	private static final String PROPERTIES = "properties";
	private static final String PROPERTY = "property";
	private static final String TOPIC_SHORT_DESC = "Classic:bboardForums_description";
	private static final String TOPIC_LONG_DESC = "Classic:bboardForums_content";
	private static final String NAME = "name";
	private static final String ENCODE = "enc";
	private static final String BASE64 = "BASE64";
	private static final String VALUE = "value";
	private static final String ATTACHMENT = "attachment";
	private static final String ATTACH_ID = "relative-url";
	private static final String PERMISSIONS = "permissions";
	private static final String PERMISSION = "permission";
	private static final String PERMISSION_TYPE = "permission_type";
	private static final String PERMISSION_NAME = "permission_name";
	private static final String PERMISSION_LEVEL_NAME = "permission_level_name";
	private static final String CUSTOM_PERMISSIONS = "permission_levels";
	
	private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
	private static final String VERSION_ATTR = "version";

	private MessageForumsForumManager forumManager;
	private AreaManager areaManager;
	private MessageForumsMessageManager messageManager;
	private MessageForumsTypeManager typeManager;
	private DiscussionForumManager dfManager;
	private PermissionLevelManager permissionManager;
	private ContentHostingService contentHostingService;
	private AuthzGroupService authzGroupService;
	private EntityManager entityManager;
	@Setter private SessionManager sessionManager;
	private SiteService siteService;
	private ToolManager toolManager;
	
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void init() throws Exception
	{
      log.info("init()");
		entityManager.registerEntityProducer(this, REFERENCE_ROOT);	
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		Base64 base64Encoder = new Base64();
		StringBuilder results = new StringBuilder();

		try { 	
			int forumCount = 0;
			results.append("archiving ").append(getLabel()).append(" context " + Entity.SEPARATOR).append(siteId)
					.append(Entity.SEPARATOR).append(SiteService.MAIN_CONTAINER).append(".\n");
			// start with an element with our very own (service) name
			Element element = doc.createElement(DiscussionForumService.class.getName());
			element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
			((Element) stack.peek()).appendChild(element);
			stack.push(element);

			if (siteId != null && siteId.trim().length() > 0) {
				Area dfArea = areaManager.getAreaByContextIdAndTypeId(siteId, typeManager.getDiscussionForumType());

				if (dfArea != null)
				{
					Element dfElement = doc.createElement(MESSAGEFORUM);

					//List forums = dfManager.getDiscussionForumsByContextId(siteId);
					List forums = dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(siteId);
					
					if (forums != null && !forums.isEmpty())
					{
						Iterator forumsIter = forums.iterator();
						while (forumsIter.hasNext())
						{
							DiscussionForum forum = (DiscussionForum)forumsIter.next();

							if (forum != null)
							{
								forumCount++;
								Element df_data = doc.createElement(DISCUSSION_FORUM);
								df_data.setAttribute(DISCUSSION_FORUM_TITLE, forum.getTitle());
								df_data.setAttribute(DRAFT, forum.getDraft().toString());
								df_data.setAttribute(LOCKED, forum.getLocked().toString());
								df_data.setAttribute(MODERATED, forum.getModerated().toString());
								df_data.setAttribute(SORT_INDEX, forum.getSortIndex().toString());


								try {
									String encoded = new String(base64Encoder.encode(forum.getExtendedDescription().getBytes()));
									df_data.setAttribute(DISCUSSION_FORUM_DESC, encoded);
								}
								catch(Exception e) {
									//log.warn("Encode DF Extended Desc - " + e);
									df_data.setAttribute(DISCUSSION_FORUM_DESC, "");
								}

								try {
									String encoded = new String(base64Encoder.encode(forum.getShortDescription().getBytes()));
									df_data.setAttribute(DISCUSSION_FORUM_SHORT_DESC, encoded);
								}
								catch(Exception e) {
									//log.warn("Encode DF Short Desc - " + e);
									df_data.setAttribute(DISCUSSION_FORUM_SHORT_DESC, "");
								}

								List atts = forumManager.getForumById(true, forum.getId()).getAttachments();
								for (int i = 0; i < atts.size(); i++)
								{
									Element forum_attachment = doc.createElement(ATTACHMENT);
									String attachId = ((Attachment)atts.get(i)).getAttachmentId();
									
									forum_attachment.setAttribute(ATTACH_ID, attachId);
									df_data.appendChild(forum_attachment);
								}
								
								Set forumMembershipItems = forum.getMembershipItemSet();
								if (forumMembershipItems != null && forumMembershipItems.size() > 0) {
									Element forum_permissions = doc.createElement(PERMISSIONS);
									Iterator membershipIter = forumMembershipItems.iterator();
									while (membershipIter.hasNext()) {
										DBMembershipItem membershipItem = (DBMembershipItem) membershipIter.next();
										Element permission = doc.createElement(PERMISSION);
										permission.setAttribute(PERMISSION_TYPE, membershipItem.getType().toString());
										permission.setAttribute(PERMISSION_NAME, membershipItem.getName());
										permission.setAttribute(PERMISSION_LEVEL_NAME, membershipItem.getPermissionLevelName());
										
										if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(membershipItem.getPermissionLevelName())){
											List customPerms = permissionManager.getCustomPermissions();
											if (customPerms != null && customPerms.size() > 0) {
												Element customPermissions = doc.createElement(CUSTOM_PERMISSIONS);
												for (int i = 0; i < customPerms.size(); i++) {
													String name = (String)customPerms.get(i);
													String hasPermission = permissionManager.getCustomPermissionByName(name, membershipItem.getPermissionLevel()).toString();
													customPermissions.setAttribute(name, hasPermission);				
												}
												permission.appendChild(customPermissions);
											}
									    }
																			
										forum_permissions.appendChild(permission);
									}
									df_data.appendChild(forum_permissions);
								}

								List topicList = dfManager.getTopicsByIdWithMessagesMembershipAndAttachments(forum.getId());
								if (topicList != null && topicList.size() > 0) {
									Iterator topicIter = topicList.iterator();
									while (topicIter.hasNext()) {
										DiscussionTopic topic = (DiscussionTopic) topicIter.next();
										Element topic_data = doc.createElement(DISCUSSION_TOPIC);
										topic_data.setAttribute(TOPIC_TITLE, topic.getTitle());
										topic_data.setAttribute(DRAFT, topic.getDraft().toString());
										topic_data.setAttribute(LOCKED, topic.getLocked().toString());
										topic_data.setAttribute(MODERATED, topic.getModerated().toString());
										if (topic.getSortIndex() != null) {
											topic_data.setAttribute(SORT_INDEX, topic.getSortIndex().toString());
										} else {
											topic_data.setAttribute(SORT_INDEX, "");
										}
										Element topic_properties = doc.createElement(PROPERTIES);
										Element topic_short_desc = doc.createElement(PROPERTY);

										try {
											String encoded = new String(base64Encoder.encode(topic.getShortDescription().getBytes()));
											topic_short_desc.setAttribute(NAME, TOPIC_SHORT_DESC);
											topic_short_desc.setAttribute(ENCODE, BASE64);
											topic_short_desc.setAttribute(VALUE, encoded);
										} catch(Exception e) {
											//log.warn("Encode Topic Short Desc - " + e);
											topic_short_desc.setAttribute(NAME, TOPIC_SHORT_DESC);
											topic_short_desc.setAttribute(ENCODE, BASE64);
											topic_short_desc.setAttribute(VALUE, "");
										}

										topic_properties.appendChild(topic_short_desc);

										Element topic_long_desc = doc.createElement(PROPERTY);

										try {
											String encoded = new String(base64Encoder.encode(topic.getExtendedDescription().getBytes()));
											topic_long_desc.setAttribute(NAME, TOPIC_LONG_DESC);
											topic_long_desc.setAttribute(ENCODE, BASE64);
											topic_long_desc.setAttribute(VALUE, encoded);
										} catch(Exception e) {
											//log.warn("Encode Topic Ext Desc - " + e);
											topic_long_desc.setAttribute(NAME, TOPIC_LONG_DESC);
											topic_long_desc.setAttribute(ENCODE, BASE64);
											topic_long_desc.setAttribute(VALUE, "");
										}


										topic_properties.appendChild(topic_long_desc);

										topic_data.appendChild(topic_properties);

										// permissions
										Set topicMembershipItems = topic.getMembershipItemSet();
										if (topicMembershipItems != null && topicMembershipItems.size() > 0) {
											Element topic_permissions = doc.createElement(PERMISSIONS);
											Iterator topicMembershipIter = topicMembershipItems.iterator();
											while (topicMembershipIter.hasNext()) {
												DBMembershipItem membershipItem = (DBMembershipItem) topicMembershipIter.next();
												Element permission = doc.createElement(PERMISSION);
												permission.setAttribute(PERMISSION_TYPE, membershipItem.getType().toString());
												permission.setAttribute(PERMISSION_NAME, membershipItem.getName());
												permission.setAttribute(PERMISSION_LEVEL_NAME, membershipItem.getPermissionLevelName());
												topic_permissions.appendChild(permission);
												
												if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(membershipItem.getPermissionLevelName())){
													List customPerms = permissionManager.getCustomPermissions();
													if (customPerms != null && customPerms.size() > 0) {
														Element customPermissions = doc.createElement(CUSTOM_PERMISSIONS);
														for (int i = 0; i < customPerms.size(); i++) {
															String name = (String)customPerms.get(i);
															String hasPermission = permissionManager.getCustomPermissionByName(name, membershipItem.getPermissionLevel()).toString();
															customPermissions.setAttribute(name, hasPermission);				
														}
														permission.appendChild(customPermissions);
													}
											    }
											}
											topic_data.appendChild(topic_permissions);
										}
										
										List topicAtts = forumManager.getTopicByIdWithAttachments(topic.getId()).getAttachments();
										for (int j = 0; j < topicAtts.size(); j++)
										{
											Element topic_attachment = doc.createElement(ATTACHMENT);
											String attachId = ((Attachment)topicAtts.get(j)).getAttachmentId();


											topic_attachment.setAttribute(ATTACH_ID, attachId);
											topic_data.appendChild(topic_attachment);
										}

										df_data.appendChild(topic_data);
									}
								}

								dfElement.appendChild(df_data);
							}
						}
					}
					results.append("archiving ").append(getLabel()).append(": (").append(forumCount)
							.append(") messageforum DF items archived successfully.\n");
					
					((Element) stack.peek()).appendChild(dfElement);
					stack.push(dfElement);
				}
				else
				{
					results.append("archiving ").append(getLabel()).append(": empty messageforum DF archived.\n");
				}

			}
			stack.pop();

		}
		catch (DOMException e)
		{
			log.error(e.getMessage(), e);
		}
		return results.toString();
	}

	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel()
	{
		return "messageforum";
	}

	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.messagecenter", "sakai.forums" };
		return toolIds;
	}

	public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
	{
		transferCopyEntitiesRefMigrator(fromContext, toContext, resourceIds);
	}

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List resourceIds)
	{
		Map<String, String> transversalMap = new HashMap<>();
		
		boolean importOpenCloseDates = ServerConfigurationService.getBoolean("msgcntr.forums.import.openCloseDates", true);
		try 
		{
			log.debug("transfer copy mc items by transferCopyEntities");

			//List fromDfList = dfManager.getDiscussionForumsByContextId(fromContext);
			List fromDfList = dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(fromContext);
			List existingForums = dfManager.getDiscussionForumsByContextId(toContext);
			String currentUserId = sessionManager.getCurrentSessionUserId();
			int numExistingForums = existingForums.size();

			if (fromDfList != null && !fromDfList.isEmpty()) {
				for (int currForum = 0; currForum < fromDfList.size(); currForum++) {
					DiscussionForum fromForum = (DiscussionForum)fromDfList.get(currForum);
					Long fromForumId = fromForum.getId();

					DiscussionForum newForum = forumManager.createDiscussionForum();
					if(newForum != null){
					
						newForum.setTitle(fromForum.getTitle());

						if (fromForum.getShortDescription() != null && fromForum.getShortDescription().length() > 0) {
							newForum.setShortDescription(fromForum.getShortDescription());
						}

						if (fromForum.getExtendedDescription() != null && fromForum.getExtendedDescription().length() > 0) {
							newForum.setExtendedDescription(fromForum.getExtendedDescription());
						}

						newForum.setDraft(fromForum.getDraft());
						newForum.setLocked(fromForum.getLocked());
						newForum.setModerated(fromForum.getModerated());
						newForum.setPostFirst(fromForum.getPostFirst());
						newForum.setAutoMarkThreadsRead(fromForum.getAutoMarkThreadsRead());
						if(importOpenCloseDates){
							newForum.setOpenDate(fromForum.getOpenDate());
							newForum.setCloseDate(fromForum.getCloseDate());
							newForum.setAvailability(fromForum.getAvailability());
							newForum.setAvailabilityRestricted(fromForum.getAvailabilityRestricted());
						}

						// set the forum order. any existing forums will be first
						// if the "from" forum has a 0 sort index, there is no sort order
						Integer fromSortIndex = fromForum.getSortIndex();
						if (fromSortIndex != null && fromSortIndex > 0) {
							newForum.setSortIndex(fromForum.getSortIndex() + numExistingForums);
						}

						// get permissions for "from" site
						Set membershipItemSet = fromForum.getMembershipItemSet();
						List allowedPermNames = this.getSiteRolesAndGroups(toContext);

						if (membershipItemSet != null && !membershipItemSet.isEmpty() && allowedPermNames != null && !allowedPermNames.isEmpty()) {
							Iterator membershipIter = membershipItemSet.iterator();
							while (membershipIter.hasNext()) {
								DBMembershipItem oldItem = (DBMembershipItem)membershipIter.next();
								if(allowedPermNames.contains(oldItem.getName())) {

									DBMembershipItem newItem = getMembershipItemCopy(oldItem);
									if (newItem != null) {
										permissionManager.saveDBMembershipItem(newItem);
										newForum.addMembershipItem(newItem);
									}
								}
							}
						}

						// get/add the forum's attachments
						List fromAttach = forumManager.getForumById(true, fromForumId).getAttachments();
						if (fromAttach != null && !fromAttach.isEmpty()) {
							for (int currAttach=0; currAttach < fromAttach.size(); currAttach++) {                   			
								Attachment thisAttach = (Attachment)fromAttach.get(currAttach);
								Attachment newAttachment = copyAttachment(thisAttach.getAttachmentId(), toContext);
								if (newAttachment != null) {
									newForum.addAttachment(newAttachment);
								}
							}
						}   

						// get/add the gradebook assignment associated with the forum settings
						GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
						ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
						String gradebookUid;
						// if this code is called from a quartz job, like SIS, then getCurrentPlacement() will return null.
						// so just use the fromContext which gives the site id.
						if (toolManager.getCurrentPlacement() != null)
						{
							gradebookUid = toolManager.getCurrentPlacement().getContext();
						}
						else
						{
							gradebookUid = fromContext;
						}

						if (gradebookService.isGradebookDefined(gradebookUid))
						{
							String fromAssignmentTitle = fromForum.getDefaultAssignName();
							if (gradebookService.isAssignmentDefined(gradebookUid, fromAssignmentTitle))
							{
								newForum.setDefaultAssignName(fromAssignmentTitle);
							}
						}

						// save the forum, since this is copying over a forum, send "false" for parameter otherwise
						//it will create a default forum as well
						Area area = areaManager.getDiscussionArea(toContext, false);
						newForum.setArea(area);

						if (!getImportAsDraft())
						{
							forumManager.saveDiscussionForum(newForum, newForum.getDraft(), false, currentUserId);
						}
						else
						{
							newForum.setDraft(Boolean.TRUE);
							forumManager.saveDiscussionForum(newForum, true, false, currentUserId);
						}
						
						//add the ref's for the old and new forum
						transversalMap.put("forum/" + fromForumId, "forum/" + newForum.getId());

						// get/add the topics
						List topicList = dfManager.getTopicsByIdWithMessagesMembershipAndAttachments(fromForumId);
						if (topicList != null && !topicList.isEmpty()) {
							for (int currTopic = 0; currTopic < topicList.size(); currTopic++) {
								DiscussionTopic fromTopic = (DiscussionTopic)topicList.get(currTopic);
								Long fromTopicId = fromTopic.getId();

								DiscussionTopic newTopic = forumManager.createDiscussionForumTopic(newForum);

								newTopic.setTitle(fromTopic.getTitle());
								if (fromTopic.getShortDescription() != null && fromTopic.getShortDescription().length() > 0) {
									newTopic.setShortDescription(fromTopic.getShortDescription());
								}
								if (fromTopic.getExtendedDescription() != null && fromTopic.getExtendedDescription().length() > 0) {
									newTopic.setExtendedDescription(fromTopic.getExtendedDescription());
								}
								newTopic.setLocked(fromTopic.getLocked());
								newTopic.setDraft(fromTopic.getDraft());
								newTopic.setModerated(fromTopic.getModerated());
								newTopic.setPostFirst(fromTopic.getPostFirst());
								newTopic.setSortIndex(fromTopic.getSortIndex());
								newTopic.setAutoMarkThreadsRead(fromTopic.getAutoMarkThreadsRead());
								newTopic.setPostAnonymous(fromTopic.getPostAnonymous());
								newTopic.setRevealIDsToRoles(fromTopic.getRevealIDsToRoles());
								if(importOpenCloseDates){
									newTopic.setOpenDate(fromTopic.getOpenDate());
									newTopic.setCloseDate(fromTopic.getCloseDate());
									newTopic.setAvailability(fromTopic.getAvailability());
									newTopic.setAvailabilityRestricted(fromTopic.getAvailabilityRestricted());
								}

								// Get/set the topic's permissions
								Set topicMembershipItemSet = fromTopic.getMembershipItemSet();

								if (topicMembershipItemSet != null && !topicMembershipItemSet.isEmpty() && allowedPermNames != null && !allowedPermNames.isEmpty()) {
									Iterator membershipIter = topicMembershipItemSet.iterator();
									while (membershipIter.hasNext()) {
										DBMembershipItem oldItem = (DBMembershipItem)membershipIter.next();
										if(allowedPermNames.contains(oldItem.getName())) {
											DBMembershipItem newItem = getMembershipItemCopy(oldItem);
											if (newItem != null) {
												permissionManager.saveDBMembershipItem(newItem);
												newTopic.addMembershipItem(newItem);
											}
										}
									}
								}
								// Add the attachments
								List fromTopicAttach = forumManager.getTopicByIdWithAttachments(fromTopicId).getAttachments();
								if (fromTopicAttach != null && !fromTopicAttach.isEmpty()) {
									for (int topicAttach=0; topicAttach < fromTopicAttach.size(); topicAttach++) {                   			
										Attachment thisAttach = (Attachment)fromTopicAttach.get(topicAttach);
										Attachment newAttachment = copyAttachment(thisAttach.getAttachmentId(), toContext);
										if (newAttachment != null)
											newTopic.addAttachment(newAttachment);
									}			
								}

								// get/add the gradebook assignment associated with the topic	
								if (gradebookService.isGradebookDefined(gradebookUid))
								{
									String fromAssignmentTitle = fromTopic.getDefaultAssignName();
									if (gradebookService.isAssignmentDefined(gradebookUid, fromAssignmentTitle))
									{
										newTopic.setDefaultAssignName(fromAssignmentTitle);
									}
								}

								forumManager.saveDiscussionForumTopic(newTopic, newForum.getDraft(), currentUserId, false);
								
								//add the ref's for the old and new topic
								transversalMap.put("forum_topic/" + fromTopicId, "forum_topic/" + newTopic.getId());
							}
						}
					}	
				}
			}			
		}

		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return transversalMap;
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		List existingForums = dfManager.getDiscussionForumsByContextId(siteId);
		int numExistingForums = existingForums.size();
		
		Base64 base64Encoder = new Base64();
		StringBuilder results = new StringBuilder();
		if (siteId != null && siteId.trim().length() > 0)
		{
			try
			{
				NodeList allChildrenNodes = root.getChildNodes();
				int length = allChildrenNodes.getLength();
				for (int i = 0; i < length; i++)
				{
					Node siteNode = allChildrenNodes.item(i);
					if (siteNode.getNodeType() == Node.ELEMENT_NODE)
					{
						Element siteElement = (Element) siteNode;
						if (siteElement.getTagName().equals(MESSAGEFORUM))
						{
							NodeList allForumNodes = siteElement.getChildNodes();
							int lengthForum = allForumNodes.getLength();
							for (int j = 0; j < lengthForum; j++)
							{
								Node child1 = allForumNodes.item(j);
								if (child1.getNodeType() == Node.ELEMENT_NODE)
								{
									Element forumElement = (Element) child1;
									if (forumElement.getTagName().equals(DISCUSSION_FORUM))
									{
										DiscussionForum dfForum = forumManager.createDiscussionForum();

										String forumTitle = forumElement.getAttribute(DISCUSSION_FORUM_TITLE);
										dfForum.setTitle(forumTitle);

										String forumDraft = forumElement.getAttribute(DRAFT);
										if(forumDraft != null && forumDraft.length() >0)
											dfForum.setDraft(Boolean.valueOf(forumDraft));

										String forumLocked = forumElement.getAttribute(LOCKED);
										if(forumLocked != null && forumLocked.length() >0)
											dfForum.setLocked(Boolean.valueOf(forumLocked));
										
										String forumModerated = forumElement.getAttribute(MODERATED);
										if(forumModerated != null && forumModerated.length() >0)
										{
											dfForum.setModerated(Boolean.valueOf(forumModerated));
										}
										else
										{
											dfForum.setModerated(Boolean.FALSE);
										}
										
										String forumPostFirst = forumElement.getAttribute(POST_FIRST);
										if(forumPostFirst != null && forumPostFirst.length() >0)
										{
											dfForum.setPostFirst(Boolean.valueOf(forumPostFirst));
										}
										else
										{
											dfForum.setPostFirst(Boolean.FALSE);
										}
										
										String forumSortIndex = forumElement.getAttribute(SORT_INDEX);
										if(forumSortIndex != null && forumSortIndex.length() > 0) {
											try {
												Integer sortIndex = Integer.valueOf(forumSortIndex);
												sortIndex = sortIndex + numExistingForums;
												dfForum.setSortIndex(sortIndex);
											} catch (NumberFormatException nfe) {
												// do nothing b/c invalid
											}
										}

										String forumDesc = forumElement.getAttribute(DISCUSSION_FORUM_DESC);
										String trimBody = null;
										if(forumDesc != null && forumDesc.length() >0)
										{
											trimBody = trimToNull(forumDesc);
											if (trimBody != null && trimBody.length() >0)
											{
												byte[] decoded = base64Encoder.decode(trimBody.getBytes());
												trimBody = new String(decoded, "UTF-8");
											}
										}
										if(trimBody != null)
										{
											dfForum.setExtendedDescription(trimBody);
										}

										String forumShortDesc = forumElement.getAttribute(DISCUSSION_FORUM_SHORT_DESC);
										String trimSummary = null;
										if(forumShortDesc != null && forumShortDesc.length() >0)
										{
											trimSummary = trimToNull(forumShortDesc);
											if (trimSummary != null && trimSummary.length() >0)
											{
												byte[] decoded = base64Encoder.decode(trimSummary.getBytes());
												trimSummary = new String(decoded, "UTF-8");
											}
										}
										if(trimSummary != null)
										{
											dfForum.setShortDescription(trimSummary);
										}

										NodeList forumDetailNodes = forumElement.getChildNodes();
										boolean hasTopic = false;
										for(int k=0; k<forumDetailNodes.getLength(); k++)
										{
											Node forumChild = forumDetailNodes.item(k);
											if(forumChild.getNodeType() == Node.ELEMENT_NODE)
											{
												Element forumChildElement = (Element) forumChild;

												if (forumChildElement.getTagName().equals(ATTACHMENT)) {
													String oldAttachId = forumChildElement.getAttribute(ATTACH_ID);
													if (oldAttachId != null && oldAttachId.trim().length() > 0) {	                			
														String oldUrl = oldAttachId;
														if (oldUrl.startsWith("/content/attachment/"))
														{
															String newUrl = (String) attachmentNames.get(oldUrl);
															if (newUrl != null)
															{
																oldAttachId = Validator.escapeQuestionMark(newUrl);
															}
														}
														else if (oldUrl.startsWith("/content/group/" + fromSiteId + "/"))
														{
															String newUrl = "/content/group/" + siteId
																	+ oldUrl.substring(15 + fromSiteId.length());
															oldAttachId = Validator.escapeQuestionMark(newUrl);
														}
														Attachment newAttachment = copyAttachment(oldAttachId, siteId);
														if (newAttachment != null)	
															dfForum.addAttachment(newAttachment);																	
													}			
												}
												// PERMISSIONS
												else if(forumChildElement.getTagName().equals(PERMISSIONS)) {
													Set membershipItemSet = getMembershipItemSetFromPermissionElement(forumChildElement, siteId);
													if (membershipItemSet != null && membershipItemSet.size() > 0) {
														Iterator membershipIter = membershipItemSet.iterator();
														while (membershipIter.hasNext()) {
															DBMembershipItem oldItem = (DBMembershipItem)membershipIter.next();

																DBMembershipItem newItem = getMembershipItemCopy(oldItem);
																if (newItem != null) {
																	permissionManager.saveDBMembershipItem(newItem);
																	dfForum.addMembershipItem(newItem);
																}
	
														}
													}
												}

												else if(forumChildElement.getTagName().equals(DISCUSSION_TOPIC))
												{
													DiscussionTopic dfTopic = forumManager.createDiscussionForumTopic(dfForum);

													String topicTitle = forumChildElement.getAttribute(TOPIC_TITLE);
													dfTopic.setTitle(topicTitle);

													String topicDraft = forumChildElement.getAttribute(DRAFT);
													if(topicDraft != null && topicDraft.length() >0)
														dfTopic.setDraft(Boolean.valueOf(topicDraft));

													String topicLocked = forumChildElement.getAttribute(LOCKED);
													if(topicLocked != null && topicLocked.length() >0)
														dfTopic.setLocked(Boolean.valueOf(topicLocked));
													
													String topicModerated = forumChildElement.getAttribute(MODERATED);
													if(topicModerated != null && topicModerated.length() >0)
														dfTopic.setModerated(Boolean.valueOf(topicModerated));
													else
														dfTopic.setModerated(Boolean.FALSE);
													
													String topicPostFirst = forumChildElement.getAttribute(POST_FIRST);
													if(topicPostFirst != null && topicPostFirst.length() >0)
														dfTopic.setPostFirst(Boolean.valueOf(topicPostFirst));
													else
														dfTopic.setPostFirst(Boolean.FALSE);
													
													String sortIndex = forumChildElement.getAttribute(SORT_INDEX);
													if (sortIndex != null) {
														try {
															Integer sortIndexAsInt = Integer.valueOf(sortIndex);
															dfTopic.setSortIndex(sortIndexAsInt);
														} catch (NumberFormatException nfe) {
															dfTopic.setSortIndex(null);
														}
													}

													NodeList topicPropertiesNodes = forumChildElement.getChildNodes();
													for(int m=0; m<topicPropertiesNodes.getLength(); m++)
													{
														Node propertiesNode = topicPropertiesNodes.item(m);
														if(propertiesNode.getNodeType() == Node.ELEMENT_NODE)
														{
															Element propertiesElement = (Element)propertiesNode;
															if(propertiesElement.getTagName().equals(PROPERTIES))
															{
																NodeList propertyList = propertiesElement.getChildNodes();
																for(int n=0; n<propertyList.getLength(); n++)
																{
																	Node propertyNode = propertyList.item(n);
																	if(propertyNode.getNodeType() == Node.ELEMENT_NODE)
																	{
																		Element propertyElement = (Element)propertyNode;
																		if(propertyElement.getTagName().equals(PROPERTY))
																		{
																			if(TOPIC_SHORT_DESC.equals(propertyElement.getAttribute(NAME)))
																			{
																				if(BASE64.equals(propertyElement.getAttribute(ENCODE)))
																				{
																					String topicDesc = propertyElement.getAttribute(VALUE);
																					String trimDesc = null;
																					if(topicDesc != null && topicDesc.length() >0)
																					{
																						trimDesc = trimToNull(topicDesc);
																						if (trimDesc != null && trimDesc.length() >0)
																						{
																							byte[] decoded = base64Encoder.decode(trimDesc.getBytes());
																							trimDesc = new String(decoded, "UTF-8");
																						}
																					}
																					if(trimDesc != null)
																					{
																						dfTopic.setShortDescription(trimDesc);
																					}
																				}
																				else
																					dfTopic.setShortDescription(propertyElement.getAttribute(VALUE));
																			}
																			if(TOPIC_LONG_DESC.equals(propertyElement.getAttribute(NAME)))
																			{

																				if(BASE64.equals(propertyElement.getAttribute(ENCODE)))
																				{
																					String topicDesc = propertyElement.getAttribute(VALUE);
																					String trimDesc = null;
																					if(topicDesc != null && topicDesc.length() >0)
																					{
																						trimDesc = trimToNull(topicDesc);
																						if (trimDesc != null && trimDesc.length() >0)
																						{
																							byte[] decoded = base64Encoder.decode(trimDesc.getBytes());
																							trimDesc = new String(decoded, "UTF-8");
																						}
																					}
																					if(trimDesc != null)
																					{
																						dfTopic.setExtendedDescription(trimDesc);
																					}
																				}
																				else
																					dfTopic.setExtendedDescription(propertyElement.getAttribute(VALUE));                  											
																			}
																		}
																	}
																}
															}
															else if (propertiesElement.getTagName().equals(ATTACHMENT))
															{
																String oldAttachId = propertiesElement.getAttribute(ATTACH_ID);
																if (oldAttachId != null && oldAttachId.trim().length() > 0) {	
																	String oldUrl = oldAttachId;
																	if (oldUrl.startsWith("/content/attachment/"))
																	{
																		String newUrl = (String) attachmentNames.get(oldUrl);
																		if (newUrl != null)
																		{
																			oldAttachId = Validator.escapeQuestionMark(newUrl);
																		}
																	}
																	else if (oldUrl.startsWith("/content/group/" + fromSiteId + "/"))
																	{
																		String newUrl = "/content/group/" + siteId
																				+ oldUrl.substring(15 + fromSiteId.length());
																		oldAttachId = Validator.escapeQuestionMark(newUrl);
																	}
																	Attachment newAttachment = copyAttachment(oldAttachId, siteId);
																	if (newAttachment != null) {
																		dfTopic.addAttachment(newAttachment);
																	}
																}				
															}

															else if (propertiesElement.getTagName().equals(PERMISSIONS)) {
																Set membershipItemSet = getMembershipItemSetFromPermissionElement(propertiesElement, siteId);
																if (membershipItemSet != null && membershipItemSet.size() > 0) {
																	Iterator membershipIter = membershipItemSet.iterator();
																	while (membershipIter.hasNext()) {
																		DBMembershipItem oldItem = (DBMembershipItem)membershipIter.next();
																		DBMembershipItem newItem = getMembershipItemCopy(oldItem);
																		if (newItem != null) {
																			permissionManager.saveDBMembershipItem(newItem);
																			dfTopic.addMembershipItem(newItem);
																		}

																	}
																}
															}
														}
													}                  				

													if(!hasTopic)
													{
														Area area = areaManager.getDiscussionArea(siteId);
														dfForum.setArea(area);
														if (!getImportAsDraft())
														{
															forumManager.saveDiscussionForum(dfForum, dfForum.getDraft());
														}
														else
														{
															dfForum.setDraft(Boolean.valueOf("true"));
															forumManager.saveDiscussionForum(dfForum, true);
														}
													}
													hasTopic = true;

													forumManager.saveDiscussionForumTopic(dfTopic, dfForum.getDraft());
												}                  			
											}
										}

										if(!hasTopic)
										{
											Area area = areaManager.getDiscussionArea(siteId);
											dfForum.setArea(area);
											if (!getImportAsDraft())
											{
												forumManager.saveDiscussionForum(dfForum, dfForum.getDraft());
											}
											else
											{
												dfForum.setDraft(Boolean.valueOf("true"));
												forumManager.saveDiscussionForum(dfForum, true);
											}
										}
									}
								} 
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				results.append("merging ").append(getLabel()).append(" failed.\n");
			}

		}
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// /syllabus/siteid/syllabusid
			String[] parts = split(reference, Entity.SEPARATOR);

			String subType = null;
			String context = null;
			String id = null;
			String container = null;

			if (parts.length > 2)
			{
				// the site/context
				context = parts[2];

				// the id
				if (parts.length > 3)
				{
					id = parts[3];
				}
			}

			ref.set(SERVICE_NAME, subType, id, container, context);

			return true;
		}

		return false;
	}

	public boolean willArchiveMerge()
	{
		return true;
	}

	protected String[] split(String source, String splitter)
	{
		// hold the results as we find them
		List<String> rv = new ArrayList<>();
		int last = 0;
		int next;
		do
		{
			// find next splitter in source
			next = source.indexOf(splitter, last);
			if (next != -1)
			{
				// isolate from last thru before next
				rv.add(source.substring(last, next));
				last = next + splitter.length();
			}
		}
		while (next != -1);
		if (last < source.length())
		{
			rv.add(source.substring(last, source.length()));
		}

		// convert to array
		return (String[]) rv.toArray(new String[rv.size()]);

	} // split

	public MessageForumsForumManager getForumManager()
	{
		return forumManager;
	}

	public void setForumManager(MessageForumsForumManager forumManager)
	{
		this.forumManager = forumManager;
	}

	public AreaManager getAreaManager()
	{
		return areaManager;
	}

	public void setAreaManager(AreaManager areaManager)
	{
		this.areaManager = areaManager;
	}

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}
	
	private Attachment copyAttachment(String attachmentId, String toContext) {
		try {			
			ContentResource oldAttachment = contentHostingService.getResource(attachmentId);
			ContentResource attachment = contentHostingService.addAttachmentResource(
				oldAttachment.getProperties().getProperty(
						ResourceProperties.PROP_DISPLAY_NAME), toContext, toolManager.getTool(
						"sakai.forums").getTitle(), oldAttachment.getContentType(),
						oldAttachment.getContent(), oldAttachment.getProperties());
			Attachment thisDFAttach = dfManager.createDFAttachment(
				attachment.getId(), 
				attachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			return thisDFAttach;
		} catch (IdUnusedException iue) {
			log.error("Error with attachment id: " + attachmentId);
			log.error(iue.getMessage(), iue);
		}
		catch (Exception e) {
			log.error("Error with attachment id: " + attachmentId);
			log.error(e.getMessage(), e);
		}

		return null;
	}
	
	private Set getMembershipItemSetFromPermissionElement(Element permissionsElement, String siteId) {
		Set membershipItemSet = new HashSet();
		List allowedPermNames = getSiteRolesAndGroups(siteId);
		List allowedPermLevels = permissionManager.getOrderedPermissionLevelNames();
		
		// add the custom level, as well
		if (allowedPermLevels != null && !allowedPermLevels.contains(PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM)) {
			allowedPermLevels.add(PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM);
		}

		NodeList permissionsNodes = permissionsElement.getChildNodes();
		for (int m=0; m < permissionsNodes.getLength(); m++)
		{
			Node permissionsNode = permissionsNodes.item(m);
			if (permissionsNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element permissionElement = (Element)permissionsNode;
				if(permissionElement.getTagName().equals(PERMISSION)) {
					try {
						if (permissionElement.getAttribute(PERMISSION_NAME) != null && permissionElement.getAttribute(PERMISSION_LEVEL_NAME) != null &&
								permissionElement.getAttribute(PERMISSION_TYPE) != null) {
							String permissionName = permissionElement.getAttribute(PERMISSION_NAME);
							String permissionLevelName = permissionElement.getAttribute(PERMISSION_LEVEL_NAME);
							if (allowedPermNames != null && allowedPermLevels != null && allowedPermNames.contains(permissionName) && allowedPermLevels.contains(permissionLevelName))
							{

								Integer permissionType = Integer.valueOf(permissionElement.getAttribute(PERMISSION_TYPE));
								DBMembershipItem membershipItem = permissionManager.createDBMembershipItem(permissionName, permissionLevelName, permissionType);

								if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(membershipItem.getPermissionLevelName())){
									NodeList customPermNodes = permissionElement.getChildNodes();
									for (int l=0; l < customPermNodes.getLength(); l++)
									{
										Node customPermNode = customPermNodes.item(l);
										if (customPermNode.getNodeType() == Node.ELEMENT_NODE)
										{
											Element customPermElement = (Element)customPermNode;
											if (customPermElement.getTagName().equals(CUSTOM_PERMISSIONS)) {
												PermissionsMask mask = new PermissionsMask();
												List customPermList = permissionManager.getCustomPermissions();
												for (int c=0; c < customPermList.size(); c++) {
													String customPermName = (String) customPermList.get(c);
													Boolean hasPermission = Boolean.valueOf(customPermElement.getAttribute(customPermName));
													mask.put(customPermName, hasPermission);
												}

												PermissionLevel level = permissionManager.createPermissionLevel(membershipItem.getPermissionLevelName(), typeManager.getCustomLevelType(), mask);
												membershipItem.setPermissionLevel(level);
											}
										}
									}
								}
								// save DBMembershipItem here to get an id so we can add to the set
								permissionManager.saveDBMembershipItem(membershipItem);
								membershipItemSet.add(membershipItem);
							}
						}

					} catch (NumberFormatException nfe) {
						log.error(nfe.getMessage());
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		return membershipItemSet;
	}
	
	private List getSiteRolesAndGroups(String contextId) {
		// get the roles in the site
		AuthzGroup realm;
		List rolesAndGroups = new ArrayList();
		try
		{      
			realm = authzGroupService.getAuthzGroup("/site/" + contextId);
			Set roleSet = realm.getRoles();

			if (roleSet != null && roleSet.size() > 0)
			{
				Iterator roleIter = roleSet.iterator();
				while (roleIter.hasNext())
				{
					Role role = (Role) roleIter.next();
					if (role != null) 
					{
						rolesAndGroups.add(role.getId());
					}
				}
			}
			
			// get any groups/sections in site
			Site currentSite = siteService.getSite(contextId); 
			  Collection groups = currentSite.getGroups();
			  for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
		      {
		        Group currentGroup = (Group) groupIterator.next(); 
		        rolesAndGroups.add(currentGroup.getTitle());
		      }
		} catch (GroupNotDefinedException e) {
			// TODO Auto-generated catch block
			log.error("GroupNotDefinedException retrieving site's roles and groups", e);
		} catch (Exception e) {
			log.error("Exception retrieving site's roles and groups", e);
		}
		
		return rolesAndGroups;
	}
	
	private DBMembershipItem getMembershipItemCopy(DBMembershipItem itemToCopy) {
		DBMembershipItem newItem = permissionManager.createDBMembershipItem(itemToCopy.getName(), itemToCopy.getPermissionLevelName(), 
				itemToCopy.getType());
		PermissionLevel oldPermLevel = itemToCopy.getPermissionLevel();
		if (newItem.getPermissionLevelName().equals(PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM)) {
			PermissionsMask mask = new PermissionsMask();
			List customPermList = permissionManager.getCustomPermissions();
			for (int c=0; c < customPermList.size(); c++) {
				String customPermName = (String) customPermList.get(c);
				Boolean hasPermission = permissionManager.getCustomPermissionByName(customPermName, oldPermLevel);
				mask.put(customPermName, hasPermission);
			}

			PermissionLevel level = permissionManager.createPermissionLevel(newItem.getPermissionLevelName(), typeManager.getCustomLevelType(), mask);
			newItem.setPermissionLevel(level);
		}
		return newItem;
	}

	public MessageForumsMessageManager getMessageManager()
	{
		return messageManager;
	}

	public void setMessageManager(MessageForumsMessageManager messageManager)
	{
		this.messageManager = messageManager;
	}

	public MessageForumsTypeManager getTypeManager()
	{
		return typeManager;
	}

	public void setTypeManager(MessageForumsTypeManager typeManager)
	{
		this.typeManager = typeManager;
	}

	public DiscussionForumManager getDfManager() {
		return dfManager;
	}

	public void setDfManager(DiscussionForumManager dfManager) {
		this.dfManager = dfManager;
	}
	
	public void setPermissionManager(PermissionLevelManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	
	public PermissionLevelManager getPermissionManager() {
		return permissionManager;
	}
	
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{
		transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
	}

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		Map<String, String> transversalMap = new HashMap<>();
		try
		{
			if(cleanup == true)
			{
				try 
				{
					List existingForums = dfManager.getDiscussionForumsByContextId(toContext);
				
					if (existingForums != null && !existingForums.isEmpty()) 
					{
						for (int currForum = 0; currForum < existingForums.size(); currForum++) 
						{
							DiscussionForum fromForum = (DiscussionForum)existingForums.get(currForum);
						
							forumManager.deleteDiscussionForum(fromForum);
						}
					}
				}
				catch(Exception e)
				{
					log.debug ("Remove Forums from Site Import failed" + e);
				}
			}
			transversalMap.putAll(transferCopyEntitiesRefMigrator(fromContext, toContext, ids));
		}
		catch(Exception e)
		{
			log.debug ("Forums transferCopyEntities failed" + e);
		}
		
		return transversalMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap){
		if(transversalMap != null && transversalMap.size() > 0){
			Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();

			List existingForums = dfManager.getDiscussionForumsByContextId(toContext);
			String currentUserId = sessionManager.getCurrentSessionUserId();

			if (existingForums != null && !existingForums.isEmpty()) 
			{
				for (int currForum = 0; currForum < existingForums.size(); currForum++) 
				{
					boolean updateForum = false;
					DiscussionForum fromForum = (DiscussionForum)existingForums.get(currForum);
					
					//check long Desc:
					String fLongDesc = fromForum.getExtendedDescription();
					if(fLongDesc != null){
						fLongDesc = replaceAllRefs(fLongDesc, entrySet);
						if(!fLongDesc.equals(fromForum.getExtendedDescription())){
							fromForum.setExtendedDescription(fLongDesc);
							updateForum = true;
						}
					}
					
					if(updateForum){
						//update forum
						dfManager.saveForum(fromForum, fromForum.getDraft(), toContext, false, currentUserId);
					}
					
					List topics = fromForum.getTopics();
					if(topics != null && !topics.isEmpty()){
						//check topics too:
						for(int currTopic = 0; currTopic < topics.size(); currTopic++){
							boolean updateTopic = false;
							DiscussionTopic topic = dfManager.getTopicById(((DiscussionTopic) topics.get(currTopic)).getId());

							//check long Desc:
							String tLongDesc = topic.getExtendedDescription();
							if(tLongDesc != null){
								tLongDesc = replaceAllRefs(tLongDesc, entrySet);
								if(!tLongDesc.equals(topic.getExtendedDescription())){
									topic.setExtendedDescription(tLongDesc);
									updateTopic = true;
								}
							}

							if(updateTopic){
								//update forum
								dfManager.saveTopic(topic, topic.getDraft(), false, currentUserId);
							}
						}						
					}
				}
			}
		}
	}
	
	private String replaceAllRefs(String msgBody, Set<Entry<String, String>> entrySet){
		if(msgBody != null){
			msgBody = LinkMigrationHelper.migrateAllLinks(entrySet, msgBody);
			}	
		return msgBody;		
	}

	private Boolean getImportAsDraft() {
		boolean importAsDraft = ServerConfigurationService.getBoolean("import.importAsDraft", true);
		return ServerConfigurationService.getBoolean("msgcntr.forums.import.importAsDraft", importAsDraft);
	}

}
