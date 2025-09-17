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

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
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
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.cover.LinkMigrationHelper;
import org.sakaiproject.util.MergeConfig;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscussionForumServiceImpl implements DiscussionForumService, EntityTransferrer
{
	private static final String CONTENT_GROUP = "/content/group/";
	private static final String ARCHIVING = "archiving ";
	private static final String MESSAGEFORUM = "messageforum";
	private static final String DISCUSSION_FORUM = "discussion_forum";
	private static final String DISCUSSION_TOPIC = "discussion_topic";
	private static final String DISCUSSION_FORUM_TITLE = "category";
	private static final String DISCUSSION_FORUM_DESC = "body";
	private static final String DISCUSSION_FORUM_SHORT_DESC = "summary";

	private static final String MESSAGES = "messages";
	private static final String MESSAGE = "message";
	private static final String MESSAGE_TITLE = "title";
	private static final String MESSAGE_AUTHOR_NAME = "author_name";
	private static final String MESSAGE_CREATED_BY = "created_by";
	private static final String MESSAGE_MODIFIED_BY = "modified_by";
	private static final String MESSAGE_CREATED_DATE = "created_date";
	private static final String MESSAGE_MODIFIED_DATE = "modified_date";
	private static final String MESSAGE_BODY = "body";
	private static final String MESSAGE_IN_REPLY_TO = "in_reply_to";
	private static final String MESSAGE_GRADE_ASSIGNMENT_NAME = "grade_assignment_name";
	private static final String MESSAGE_LABEL = "label";
	private static final String MESSAGE_TYPE_UUID = "type_uuid";
	private static final String MESSAGE_UUID = "uuid";
	private static final String MESSAGE_APPROVED = "approved";
	private static final String MESSAGE_DELETED = "deleted";
	private static final String MESSAGE_HAS_ATTACHMENTS = "has_attachments";
	private static final String MESSAGE_NUM_READER = "num_readers";
	private static final String MESSAGE_THREAD_ID = "thread_id";
	private static final String MESSAGE_THREAD_LAST_POST = "thread_last_post";
	private static final String MESSAGE_DATE_THREAD_LAST_UPDATED = "date_thread_last_updated";
	private static final String MESSAGE_TOPIC = "topic";

	private static final String TOPIC_TITLE = "subject";
	private static final String ID = "id";
	private static final String DRAFT = "draft";
	private static final String LOCKED = "locked";
	private static final String LOCKED_AFTER_CLOSED = "locked_after_closed";
	private static final String MODERATED = "moderated";
	private static final String POST_ANONYMOUS = "anonymous";
	private static final String POST_FIRST = "post_first";
	private static final String SORT_INDEX = "sort_index";
	private static final String PROPERTIES = "properties";
	private static final String PROPERTY = "property";
	private static final String TOPIC_SHORT_DESC = "Classic:bboardForums_description";
	private static final String TOPIC_LONG_DESC = "Classic:bboardForums_content";
	private static final String GRADE_ASSIGNMENT = "grade_assignment";
	private static final String OPEN_DATE = "available_open";
	private static final String CLOSE_DATE = "available_close";
	private static final String AUTO_MARK_THREADS_READ = "auto_mark_threads_read";
	private static final String ALLOW_EMAIL_NOTIFICATIONS = "allow_email_notifications";
	private static final String INCLUDE_CONTENTS_IN_EMAILS = "include_contents_in_emails";
	private static final String REVEAL_IDS_TO_ROLES = "reveal_ids_to_roles";
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
	private static final String DIRECT_TOOL = "/directtool/";

	private static final String ARCHIVE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mmZ"; // ISO8601 with timezone
	private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
	private static final String VERSION_ATTR = "version";

	@Getter
	@Setter
	private MessageForumsForumManager forumManager;
	@Getter
	@Setter
	private AreaManager areaManager;
	@Getter
	@Setter
	private MessageForumsMessageManager messageManager;
	@Getter
	@Setter
	private MessageForumsTypeManager typeManager;
	@Getter
	@Setter
	private DiscussionForumManager dfManager;
	@Getter
	@Setter
	private PermissionLevelManager permissionManager;
	@Setter
	private ContentHostingService contentHostingService;
	@Setter
	private AuthzGroupService authzGroupService;
	@Setter
	private EntityManager entityManager;
	@Setter
	private SessionManager sessionManager;
	@Setter
	private SiteService siteService;
	@Setter
	private ToolManager toolManager;
	@Setter
	private ServerConfigurationService serverConfigurationService;
	@Setter
	private LTIService ltiService;

	private final Base64 base64Encoder = new Base64();

	public void init() throws Exception {
		log.info("init()");
		entityManager.registerEntityProducer(this, REFERENCE_ROOT);
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		StringBuilder results = new StringBuilder();
		try {
			results.append(ARCHIVING).append(getLabel()).append(" context " + Entity.SEPARATOR).append(siteId)
					.append(Entity.SEPARATOR).append(SiteService.MAIN_CONTAINER).append(".\n");
			// start with an element with our very own (service) name
			Element element = doc.createElement(DiscussionForumService.class.getName());
			element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
			((Element) stack.peek()).appendChild(element);
			stack.push(element);

			if (StringUtils.isNotEmpty(siteId)) {
				Area dfArea = areaManager.getAreaByContextIdAndTypeId(siteId, typeManager.getDiscussionForumType());

				if (dfArea != null) {
					Element messageForumElement = doc.createElement(MESSAGEFORUM);

					// APPEND DISCUSSION FORUM ELEMENTS
					int discussionForumCount = appendDiscussionForumElements(siteId, doc, messageForumElement, attachments);

					results.append(ARCHIVING).append(getLabel()).append(": (").append(discussionForumCount)
							.append(") messageforum DF items archived successfully.\n");

					((Element) stack.peek()).appendChild(messageForumElement);
					stack.push(messageForumElement);
				} else {
					results.append(ARCHIVING).append(getLabel()).append(": empty messageforum DF archived.\n");
				}
			}
			stack.pop();

		} catch (DOMException e) {
			log.error(e.getMessage(), e);
		}
		return results.toString();
	}

	private int appendDiscussionForumElements(String siteId, Document doc, Element messageForumElement, List attachments) {
		SimpleDateFormat formatter = new SimpleDateFormat(ARCHIVE_DATE_FORMAT);

		int discussionForumCount = 0;
		List<DiscussionForum> discussionForums = dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(siteId);
		if (CollectionUtils.isNotEmpty(discussionForums)) {
			for (DiscussionForum discussionForum : discussionForums) {
				if (discussionForum != null) {
					discussionForumCount++;
					final Element discussionForumElement = doc.createElement(DISCUSSION_FORUM);
					discussionForumElement.setAttribute(DISCUSSION_FORUM_TITLE, discussionForum.getTitle());
					discussionForumElement.setAttribute(ID, discussionForum.getId().toString());
					discussionForumElement.setAttribute(DRAFT, discussionForum.getDraft().toString());
					discussionForumElement.setAttribute(LOCKED, discussionForum.getLocked().toString());
					discussionForumElement.setAttribute(LOCKED_AFTER_CLOSED, discussionForum.getLockedAfterClosed().toString());
					discussionForumElement.setAttribute(MODERATED, discussionForum.getModerated().toString());
					discussionForumElement.setAttribute(POST_FIRST, discussionForum.getPostFirst().toString());
					discussionForumElement.setAttribute(SORT_INDEX, discussionForum.getSortIndex().toString());
					discussionForumElement.setAttribute(DISCUSSION_FORUM_DESC,
							getEncodedString(discussionForum.getExtendedDescription()));
					discussionForumElement.setAttribute(DISCUSSION_FORUM_SHORT_DESC,
							getEncodedString(discussionForum.getShortDescription()));
					if (discussionForum.getDefaultAssignName() != null) {
						discussionForumElement.setAttribute(GRADE_ASSIGNMENT, discussionForum.getDefaultAssignName());
					}
					if (discussionForum.getAvailabilityRestricted()) {
						if (discussionForum.getOpenDate() != null) {
							discussionForumElement.setAttribute(OPEN_DATE, formatter.format(discussionForum.getOpenDate()));
						}
						if (discussionForum.getCloseDate() != null) {
							discussionForumElement.setAttribute(CLOSE_DATE, formatter.format(discussionForum.getCloseDate()));
						}
					}

					// attachments
					List<Attachment> forumAttachments = discussionForum.getAttachments();
					appendAttachmentElements(doc, discussionForumElement, forumAttachments, attachments);

					// permissions
					Set<DBMembershipItem> forumMembershipItems = discussionForum.getMembershipItemSet();
					appendPermissionsElement(doc, discussionForumElement, forumMembershipItems);

					// APPEND DISCUSSION TOPIC ELEMENTS
					appendDiscussionTopicElements(doc, discussionForum, discussionForumElement, attachments);
					messageForumElement.appendChild(discussionForumElement);
				}
			}
		}
		return discussionForumCount;
	}

	private void appendDiscussionTopicElements(Document doc, DiscussionForum forum, Element discussionForumElement, List attachments) {
		SimpleDateFormat formatter = new SimpleDateFormat(ARCHIVE_DATE_FORMAT);

		List<DiscussionTopic> discussionTopics = dfManager
				.getTopicsByIdWithMessagesMembershipAndAttachments(forum.getId());
		if (CollectionUtils.isNotEmpty(discussionTopics)) {
			for (DiscussionTopic discussionTopic : discussionTopics) {
				final Element discussionTopicElement = doc.createElement(DISCUSSION_TOPIC);
				discussionTopicElement.setAttribute(TOPIC_TITLE, discussionTopic.getTitle());
				discussionTopicElement.setAttribute(ID, discussionTopic.getId().toString());
				discussionTopicElement.setAttribute(DRAFT, discussionTopic.getDraft().toString());
				discussionTopicElement.setAttribute(LOCKED, discussionTopic.getLocked().toString());
				discussionTopicElement.setAttribute(LOCKED_AFTER_CLOSED, discussionTopic.getLockedAfterClosed().toString());
				discussionTopicElement.setAttribute(MODERATED, discussionTopic.getModerated().toString());
				discussionTopicElement.setAttribute(POST_ANONYMOUS, discussionTopic.getPostAnonymous().toString());
				discussionTopicElement.setAttribute(POST_FIRST, discussionTopic.getPostFirst().toString());
				discussionTopicElement.setAttribute(ALLOW_EMAIL_NOTIFICATIONS, discussionTopic.getAllowEmailNotifications().toString());
				discussionTopicElement.setAttribute(INCLUDE_CONTENTS_IN_EMAILS, discussionTopic.getIncludeContentsInEmails().toString());
				discussionTopicElement.setAttribute(REVEAL_IDS_TO_ROLES, discussionTopic.getRevealIDsToRoles().toString());
				discussionTopicElement.setAttribute(AUTO_MARK_THREADS_READ, discussionTopic.getAutoMarkThreadsRead().toString());
				if (discussionTopic.getDefaultAssignName() != null) {
					discussionTopicElement.setAttribute(GRADE_ASSIGNMENT, discussionTopic.getDefaultAssignName());
				}
				if (discussionTopic.getAvailabilityRestricted()) {
					if (discussionTopic.getOpenDate() != null) {
						discussionTopicElement.setAttribute(OPEN_DATE, formatter.format(discussionTopic.getOpenDate()));
					}
					if (discussionTopic.getCloseDate() != null) {
						discussionTopicElement.setAttribute(CLOSE_DATE, formatter.format(discussionTopic.getCloseDate()));
					}
				}
				if (discussionTopic.getSortIndex() != null) {
					discussionTopicElement.setAttribute(SORT_INDEX, discussionTopic.getSortIndex().toString());
				} else {
					discussionTopicElement.setAttribute(SORT_INDEX, StringUtils.EMPTY);
				}
				final Element discussionTopicPropertiesElement = appendDiscussionTopicPropertiesElement(doc,
						discussionTopic);
				discussionTopicElement.appendChild(discussionTopicPropertiesElement);

				// permissions
				final Set<DBMembershipItem> membershipItems = discussionTopic.getMembershipItemSet();
				appendPermissionsElement(doc, discussionTopicElement, membershipItems);

				// attachments
				final List<Attachment> topicAttachments = discussionTopic.getAttachments();
				appendAttachmentElements(doc, discussionTopicElement, topicAttachments, attachments);

				// APPEND MESSAGE ELEMENTS
				Element messagesElement = appendMessagesElements(doc, discussionTopic.getMessages(), null,
						discussionTopicElement, attachments);
				if (messagesElement != null) {
					discussionTopicElement.appendChild(messagesElement);
				}
				discussionForumElement.appendChild(discussionTopicElement);
			}
		}
	}

	private Element appendMessagesElements(Document doc, List<Message> messages, Message parentMessage,
		   Element discussionTopicElement, List attachments) {
		Element messagesElement = null;
		if (CollectionUtils.isNotEmpty(messages)) {
			List<Message> startConversationMessages = null;
			if (parentMessage == null) {
				startConversationMessages = messages.stream().filter(m -> (parentMessage == m.getInReplyTo()))
						.collect(Collectors.toList());
			} else {
				startConversationMessages = messages.stream().filter(m -> (parentMessage.equals(m.getInReplyTo())))
						.collect(Collectors.toList());
			}
			if (CollectionUtils.isNotEmpty(startConversationMessages)) {
				messagesElement = doc.createElement(MESSAGES);
				for (Message startConversationMessage : startConversationMessages) {
					appendMessageElement(doc, messages, discussionTopicElement, messagesElement,
							startConversationMessage, attachments);
				}
			}
		}
		return messagesElement;
	}
	private void appendMessageElement(Document doc, List<Message> messages, Element discussionTopicElement,
			  final Element messagesElement, Message message, List attachments) {
		final Element messageElement = doc.createElement(MESSAGE);
		messageElement.setAttribute(ID, message.getId().toString());
		if (message.getInReplyTo() != null) {
			messageElement.setAttribute(MESSAGE_IN_REPLY_TO, message.getInReplyTo().getId().toString());
		} else {
			messageElement.setAttribute(MESSAGE_IN_REPLY_TO, StringUtils.EMPTY);
		}
		messageElement.setAttribute(MESSAGE_TITLE, message.getTitle());
		messageElement.setAttribute(MESSAGE_AUTHOR_NAME, message.getAuthor());
		messageElement.setAttribute(MESSAGE_CREATED_BY, message.getCreatedBy());
		messageElement.setAttribute(MESSAGE_CREATED_DATE, message.getCreated().toString());
		messageElement.setAttribute(MESSAGE_MODIFIED_BY, message.getModifiedBy());
		messageElement.setAttribute(MESSAGE_MODIFIED_DATE, message.getModified().toString());
		messageElement.setAttribute(DRAFT, message.getDraft().toString());
		messageElement.setAttribute(MESSAGE_GRADE_ASSIGNMENT_NAME, message.getGradeAssignmentName());
		messageElement.setAttribute(MESSAGE_LABEL, message.getLabel());
		messageElement.setAttribute(MESSAGE_TYPE_UUID, message.getTypeUuid());
		messageElement.setAttribute(MESSAGE_UUID, message.getUuid());
		if (message.getApproved() != null) {
			messageElement.setAttribute(MESSAGE_APPROVED, message.getApproved().toString());
		}
		messageElement.setAttribute(MESSAGE_DELETED, message.getDeleted().toString());
		messageElement.setAttribute(MESSAGE_HAS_ATTACHMENTS, message.getHasAttachments().toString());
		if (message.getNumReaders() != null) {
			messageElement.setAttribute(MESSAGE_NUM_READER, message.getNumReaders().toString());
		}
		if (message.getThreadId() != null) {
			messageElement.setAttribute(MESSAGE_THREAD_ID, message.getThreadId().toString());
		} else {
			messageElement.setAttribute(MESSAGE_THREAD_ID, StringUtils.EMPTY);
		}
		if (message.getThreadLastPost() != null) {
			messageElement.setAttribute(MESSAGE_THREAD_LAST_POST, message.getThreadLastPost().toString());
		} else {
			messageElement.setAttribute(MESSAGE_THREAD_LAST_POST, StringUtils.EMPTY);
		}
		if(message.getDateThreadlastUpdated() != null) {
			messageElement.setAttribute(MESSAGE_DATE_THREAD_LAST_UPDATED, message.getDateThreadlastUpdated().toString());
		} else {
			messageElement.setAttribute(MESSAGE_DATE_THREAD_LAST_UPDATED, StringUtils.EMPTY);
		}
		messageElement.setAttribute(MESSAGE_TOPIC, message.getTopic().toString());
		messageElement.setAttribute(MESSAGE_BODY, getEncodedString(message.getBody()));

		// attachments
		final List<Attachment> messageAttachments = message.getAttachments();
		appendAttachmentElements(doc, messageElement, messageAttachments, attachments);

		messages.remove(message);
		Element childMessagesElement = appendMessagesElements(doc, messages, message, discussionTopicElement, attachments);
		if (childMessagesElement != null) {
			messageElement.appendChild(childMessagesElement);
		}
		messagesElement.appendChild(messageElement);
	}

	private Element appendDiscussionTopicPropertiesElement(Document doc, DiscussionTopic discussionTopic) {
		Element discussionTopicShortDescElement = doc.createElement(PROPERTY);
		discussionTopicShortDescElement.setAttribute(NAME, TOPIC_SHORT_DESC);
		discussionTopicShortDescElement.setAttribute(ENCODE, BASE64);
		try {
			String encoded = new String(base64Encoder.encode(discussionTopic.getShortDescription().getBytes()));
			discussionTopicShortDescElement.setAttribute(VALUE, encoded);
		} catch (Exception e) {
			discussionTopicShortDescElement.setAttribute(VALUE, StringUtils.EMPTY);
		}
		Element discussionTopicLongDescElement = doc.createElement(PROPERTY);
		discussionTopicLongDescElement.setAttribute(NAME, TOPIC_LONG_DESC);
		discussionTopicLongDescElement.setAttribute(ENCODE, BASE64);
		try {
			String encoded = new String(base64Encoder.encode(discussionTopic.getExtendedDescription().getBytes()));
			discussionTopicLongDescElement.setAttribute(VALUE, encoded);
		} catch (Exception e) {
			discussionTopicLongDescElement.setAttribute(VALUE, StringUtils.EMPTY);
		}
		final Element discussionTopicPropertiesElement = doc.createElement(PROPERTIES);
		discussionTopicPropertiesElement.appendChild(discussionTopicShortDescElement);
		discussionTopicPropertiesElement.appendChild(discussionTopicLongDescElement);
		return discussionTopicPropertiesElement;
	}

	private void appendAttachmentElements(Document doc, Element parentElement, List<Attachment> itemAttachments, List archiveAttachments) {
		for (Attachment attachment : itemAttachments) {
			// Append to the XML doc
			final Element attachmentElement = doc.createElement(ATTACHMENT);
			String attachId = attachment.getAttachmentId();
			attachmentElement.setAttribute(ATTACH_ID, attachId);
			parentElement.appendChild(attachmentElement);

			// Append to the attachment reference list for archive
			Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getAttachmentId()));
			archiveAttachments.add(ref);
		}
	}

	private void appendPermissionsElement(Document doc, Element parentElement, Set<DBMembershipItem> membershipItems) {
		if (CollectionUtils.isNotEmpty(membershipItems)) {
			final Element permissionsElement = doc.createElement(PERMISSIONS);
			for (DBMembershipItem membershipItem : membershipItems) {
				final Element permissionElement = doc.createElement(PERMISSION);
				permissionElement.setAttribute(PERMISSION_TYPE, membershipItem.getType().toString());
				permissionElement.setAttribute(PERMISSION_NAME, membershipItem.getName());
				permissionElement.setAttribute(PERMISSION_LEVEL_NAME, membershipItem.getPermissionLevelName());

				if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(membershipItem.getPermissionLevelName())) {
					List<String> customPerms = permissionManager.getCustomPermissions();
					if (CollectionUtils.isNotEmpty(customPerms)) {
						Element customPermissions = doc.createElement(CUSTOM_PERMISSIONS);
						for (String name : customPerms) {
							String hasPermission = permissionManager.getCustomPermissionByName(name, membershipItem.getPermissionLevel()).toString();
							customPermissions.setAttribute(name, hasPermission);
						}
						permissionElement.appendChild(customPermissions);
					}
				}
				permissionsElement.appendChild(permissionElement);
			}
			parentElement.appendChild(permissionsElement);
		}
	}

	public String getEntityUrl(Reference ref) {
		if (StringUtils.isNotBlank(ref.getId())) {
			String context = ref.getContext();
			Site site = null;
			try {
				site = siteService.getSite( context );
			} catch (Exception e) {
				log.error("Unable to get entity url, site {} does not exist.", context);
				return null;
			}
			List<SitePage> pages = site.getOrderedPages();
			for (SitePage page : pages) {
				for (ToolConfiguration toolConfiguration : (List<ToolConfiguration>) page.getTools(0)) {
					Tool tool = toolConfiguration.getTool();
					if (tool != null && "sakai.forums".equals(tool.getId())) {
						String placementId = toolConfiguration.getId();
						return serverConfigurationService.getPortalUrl() + DIRECT_TOOL + placementId;
					}
				}
			}
		}
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

	@Override
	public List<Map<String, String>> getEntityMap(String fromContext) {

		return dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(fromContext).stream()
			.map(f -> Map.of("id", f.getId().toString(), "title", f.getTitle())).collect(Collectors.toList());
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options)
	{
		Map<String, String> transversalMap = new HashMap<>();
		
		boolean importOpenCloseDates = serverConfigurationService.getBoolean("msgcntr.forums.import.openCloseDates", true);
		try 
		{
			log.debug("transfer copy mc items by transferCopyEntities");

			// Copy area-level permissions first
			Area fromArea = areaManager.getAreaByContextIdAndTypeId(fromContext, typeManager.getDiscussionForumType());
			Area toArea = areaManager.getDiscussionArea(toContext, false);
			
			if (fromArea != null && toArea != null) {
				Set membershipItemSet = fromArea.getMembershipItemSet();
				List allowedPermNames = getSiteRolesAndGroups(toContext);
				
				if (membershipItemSet != null && !membershipItemSet.isEmpty() && allowedPermNames != null && !allowedPermNames.isEmpty()) {
					Iterator membershipIter = membershipItemSet.iterator();
					while (membershipIter.hasNext()) {
						DBMembershipItem oldItem = (DBMembershipItem)membershipIter.next();
						if(allowedPermNames.contains(oldItem.getName())) {
							DBMembershipItem newItem = getMembershipItemCopy(oldItem);
							if (newItem != null) {
								newItem = permissionManager.saveDBMembershipItem(newItem);
								toArea.addMembershipItem(newItem);
							}
						}
					}
					areaManager.saveArea(toArea);
				}
			}
			
			List<DiscussionForum> fromDfList = dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(fromContext);
			if (CollectionUtils.isNotEmpty(ids)) {
				fromDfList = fromDfList.stream().filter(df -> ids.contains(df.getId().toString())).collect(Collectors.toList());
			}
			List existingForums = dfManager.getDiscussionForumsByContextId(toContext);
			String currentUserId = sessionManager.getCurrentSessionUserId();
			int numExistingForums = existingForums.size();

			if (CollectionUtils.isNotEmpty(fromDfList)) {
				for (DiscussionForum fromForum : fromDfList) {
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
						newForum.setLockedAfterClosed(fromForum.getLockedAfterClosed());
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
										newItem = permissionManager.saveDBMembershipItem(newItem);
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
								Attachment newAttachment = copyAttachment(thisAttach.getAttachmentId(), toContext, null);
								if (newAttachment != null) {
									newForum.addAttachment(newAttachment);
								}
							}
						}   

						//add the gradebook assignment associated with the forum settings
						newForum.setDefaultAssignName(fromForum.getDefaultAssignName());

						// save the forum, since this is copying over a forum, send "false" for parameter otherwise
						//it will create a default forum as well
						Area area = areaManager.getDiscussionArea(toContext, false);
						newForum.setArea(area);

						if (!getImportAsDraft())
						{
							newForum = forumManager.saveDiscussionForum(newForum, newForum.getDraft(), false, currentUserId);
						}
						else
						{
							newForum.setDraft(Boolean.TRUE);
							newForum = forumManager.saveDiscussionForum(newForum, true, false, currentUserId);
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
									String extendedDescription = fromTopic.getExtendedDescription();
									extendedDescription = ltiService.fixLtiLaunchUrls(extendedDescription, fromContext, toContext, transversalMap);
									newTopic.setExtendedDescription(extendedDescription);
								}
								newTopic.setLocked(fromTopic.getLocked());
								newTopic.setLockedAfterClosed(fromTopic.getLockedAfterClosed());
								newTopic.setDraft(fromTopic.getDraft());
								newTopic.setModerated(fromTopic.getModerated());
								newTopic.setPostFirst(fromTopic.getPostFirst());
								newTopic.setSortIndex(fromTopic.getSortIndex());
								newTopic.setAutoMarkThreadsRead(fromTopic.getAutoMarkThreadsRead());
								newTopic.setPostAnonymous(fromTopic.getPostAnonymous());
								newTopic.setAllowEmailNotifications(fromTopic.getAllowEmailNotifications());
								newTopic.setIncludeContentsInEmails(fromTopic.getIncludeContentsInEmails());
								newTopic.setAutoMarkThreadsRead(fromTopic.getAutoMarkThreadsRead());
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
												newItem = permissionManager.saveDBMembershipItem(newItem);
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
										Attachment newAttachment = copyAttachment(thisAttach.getAttachmentId(), toContext, null);
										if (newAttachment != null)
											newTopic.addAttachment(newAttachment);
									}			
								}

								//add the gradebook assignment associated with the topic	
								newTopic.setDefaultAssignName(fromTopic.getDefaultAssignName());

								newTopic = forumManager.saveDiscussionForumTopic(newTopic, newForum.getDraft(), currentUserId, false);
								
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

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, MergeConfig mcx) {

		log.debug("merge archiveContext={} archiveServerUrl={}", mcx.archiveContext, mcx.archiveServerUrl);

		Set<String> discussionTitles = new HashSet<>();
		List<DiscussionForum> discussionForums = dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(siteId);
		if (CollectionUtils.isNotEmpty(discussionForums)) {
			for (DiscussionForum discussionForum : discussionForums) {
				discussionTitles.add(discussionForum.getTitle());
			}
		}

		final StringBuilder results = new StringBuilder();
		if (StringUtils.isNotBlank(siteId)) {
			results.append("merging ").append(getLabel()).append(" context " + Entity.SEPARATOR).append(siteId)
					.append(Entity.SEPARATOR).append(SiteService.MAIN_CONTAINER).append(".\n");
			try {
				final List<Element> elements = getChildElementList(root);
				final List<Element> messageForumElementList = elements.stream()
						.filter(element -> MESSAGEFORUM.equals(element.getTagName())).collect(Collectors.toList());
				if (!messageForumElementList.isEmpty()) {
					mergeMessageForumElements(siteId, fromSiteId, messageForumElementList.get(0), discussionTitles, mcx);
				}
			} catch (Exception e) {
				results.append("merging ").append(getLabel()).append(" failed.\n");
				log.error(e.getMessage(), e);
			}
		}
		return results.toString();
	}

	private void mergeMessageForumElements(final String siteId, final String fromSiteId,
		final Element siteElement, Set<String> discussionTitles, MergeConfig mcx) throws Exception {

		final NodeList messageForumChildNodeList = siteElement.getChildNodes();

		final List<Element> discussionForumElementsList = IntStream.range(0, messageForumChildNodeList.getLength())
				.mapToObj(messageForumChildNodeList::item).filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
				.map(element -> (Element) element).filter(element -> DISCUSSION_FORUM.equals(element.getTagName()))
				.collect(Collectors.toList());

		for (Element discussionForumElement : discussionForumElementsList) {
			String title = discussionForumElement.getAttribute(DISCUSSION_FORUM_TITLE);
			if (discussionTitles.contains(title)) {
				continue;
			}
			mergeDiscussionForumElements(siteId, fromSiteId, discussionForumElement, mcx);
		}
	}

	private void mergeDiscussionForumElements(final String siteId, final String fromSiteId,
			final Element discussionForumElement, MergeConfig mcx) throws Exception {

		final DiscussionForum discussionForum = forumManager.createDiscussionForum();

		discussionForum.setTitle(discussionForumElement.getAttribute(DISCUSSION_FORUM_TITLE));

		final String forumDraft = discussionForumElement.getAttribute(DRAFT);
		if (StringUtils.isNotEmpty(forumDraft)) {
			discussionForum.setDraft(Boolean.valueOf(forumDraft));
		}

		final String forumLocked = discussionForumElement.getAttribute(LOCKED);
		if (StringUtils.isNotEmpty(forumLocked)) {
			discussionForum.setLocked(Boolean.valueOf(forumLocked));
		}

		final String forumLockedAfterClosed = discussionForumElement.getAttribute(LOCKED_AFTER_CLOSED);
		if (StringUtils.isNotEmpty(forumLockedAfterClosed)) {
			discussionForum.setLockedAfterClosed(Boolean.valueOf(forumLockedAfterClosed));
		}

		final String forumModerated = discussionForumElement.getAttribute(MODERATED);
		if (StringUtils.isNotEmpty(forumModerated)) {
			discussionForum.setModerated(Boolean.valueOf(forumModerated));
		} else {
			discussionForum.setModerated(Boolean.FALSE);
		}

		final String forumPostFirst = discussionForumElement.getAttribute(POST_FIRST);
		if (StringUtils.isNotEmpty(forumPostFirst)) {
			discussionForum.setPostFirst(Boolean.valueOf(forumPostFirst));
		} else {
			discussionForum.setPostFirst(Boolean.FALSE);
		}

		final String forumSortIndex = discussionForumElement.getAttribute(SORT_INDEX);
		if (StringUtils.isNotEmpty(forumSortIndex)) {
			try {
				Integer sortIndex = Integer.valueOf(forumSortIndex);
				int numExistingForums = dfManager.getDiscussionForumsByContextId(siteId).size();
				sortIndex += numExistingForums;
				discussionForum.setSortIndex(sortIndex);
			} catch (NumberFormatException nfe) {
				// do nothing b/c invalid
			}
		}

		String extendedDescription = getDecodedString(discussionForumElement.getAttribute(DISCUSSION_FORUM_DESC));
		extendedDescription = ltiService.fixLtiLaunchUrls(extendedDescription, siteId, mcx);
		discussionForum
				.setExtendedDescription(extendedDescription);

		discussionForum.setShortDescription(
				getDecodedString(discussionForumElement.getAttribute(DISCUSSION_FORUM_SHORT_DESC)));

		final Area area = areaManager.getDiscussionArea(siteId);
		discussionForum.setArea(area);

		// Discussion Forum is saved inside this method
		mergeDiscussionForumDetailNodeList(siteId, fromSiteId, discussionForumElement, discussionForum, mcx);
	}

	private void mergeDiscussionForumDetailNodeList(final String siteId, final String fromSiteId,
		final Element discussionForumElement, DiscussionForum discussionForum, MergeConfig mcx) throws Exception {

		final NodeList discussionForumDetailNodeList = discussionForumElement.getChildNodes();
		final List<Element> elements = IntStream.range(0, discussionForumDetailNodeList.getLength())
				.mapToObj(discussionForumDetailNodeList::item).filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
				.map(element -> (Element) element).collect(Collectors.toList());

		final List<Element> attachmentElementList = getAttachmentElementList(elements);
		for (Element attachmentElement : attachmentElementList) {
			final Attachment newAttachment = mergeAttachmentElement(siteId, fromSiteId, mcx,
					attachmentElement);
			if (newAttachment != null) {
				discussionForum.addAttachment(newAttachment);
			}
		}

		final List<Element> permissionsElementList = getPermissionsElements(elements);
		for (Element permissionsElement : permissionsElementList) {
			mergeDiscussionForumPermissionsElement(siteId, discussionForum, permissionsElement);
		}

		DiscussionForum discussionForumReturn = discussionForum;
		// Save the discussion forum before saving discussion topic
		if (!getImportAsDraft()) {
			discussionForumReturn = forumManager.saveDiscussionForum(discussionForum, discussionForum.getDraft());
		} else {
			discussionForumReturn.setDraft(Boolean.TRUE);
			discussionForumReturn = forumManager.saveDiscussionForum(discussionForum, Boolean.TRUE);
		}

		final List<Element> discussionTopicElementList = getDiscussionTopicElementList(elements);
		for (Element discussionTopicElement : discussionTopicElementList) {
			mergeDiscussionTopicElement(siteId, fromSiteId, discussionForumReturn, discussionTopicElement, mcx);
		}
	}

	private List<Element> getDiscussionTopicElementList(List<Element> elements) {
		return elements.stream()
				.filter(e -> DISCUSSION_TOPIC.equals(e.getTagName())).collect(Collectors.toList());
	}

	private List<Element> getPermissionsElements(List<Element> elements) {
		return elements.stream().filter(e -> PERMISSIONS.equals(e.getTagName()))
				.collect(Collectors.toList());
	}

	private void mergeDiscussionForumPermissionsElement(final String siteId, final DiscussionForum discussionForum,
														final Element permissionElement) {
		final Set<DBMembershipItem> membershipItemSet = getMembershipItemSetFromPermissionElement(permissionElement,
				siteId);
		if (CollectionUtils.isNotEmpty(membershipItemSet)) {
			discussionForum.setMembershipItemSet(membershipItemSet);
		}
	}

	private Attachment mergeAttachmentElement(final String siteId, final String fromSiteId,
											  MergeConfig mcx, final Element attachmentElement) {
		String oldAttachId = attachmentElement.getAttribute(ATTACH_ID);
		if (StringUtils.isNotBlank(oldAttachId)) {
			return copyAttachment(oldAttachId, siteId, mcx);
		}
		return null;
	}

	private void mergeDiscussionTopicElement(final String siteId, final String fromSiteId,
		final DiscussionForum discussionForum, final Element discussionTopicElement, MergeConfig mcx) throws Exception {

		DiscussionTopic discussionTopic = forumManager.createDiscussionForumTopic(discussionForum);

		setDiscussionTopicValues(discussionTopicElement, discussionTopic);

		final List<Element> elements = getChildElementList(discussionTopicElement);

		final List<Element> propertiesElementList = elements.stream().filter(e -> PROPERTIES.equals(e.getTagName()))
				.collect(Collectors.toList());
		for (Element propertiesElement : propertiesElementList) {
			mergeDiscussionTopicPropertiesNodes(discussionTopic, propertiesElement, siteId, mcx);
		}

		final List<Element> attachmentElementList = getAttachmentElementList(elements);
		for (Element attachmentElement : attachmentElementList) {
			final Attachment newAttachment = mergeAttachmentElement(siteId, fromSiteId, mcx,
					attachmentElement);
			if (newAttachment != null) {
				discussionTopic.addAttachment(newAttachment);
			}
		}

		final List<Element> permissionsElementList = getPermissionsElements(elements);
		for (Element permissionsElement : permissionsElementList) {
			mergeDiscussionTopicPermissionsElement(siteId, discussionTopic, permissionsElement);
		}

		// Discussion topic have to be saved before its messages
		discussionTopic = forumManager.saveDiscussionForumTopic(discussionTopic, discussionForum.getDraft());

		discussionTopic.setBaseForum(discussionForum);

		// Messages have to be merged after the topic in order to control the ids of the
		// "onReplyTo" attribute
		final List<Element> messagesElementList = getMessagesElementList(elements);
		for (Element messagesElement : messagesElementList) {
			mergeDiscussionTopicMessagesElement(siteId, fromSiteId, mcx, discussionTopic, messagesElement, null);
		}
	}

	private void setDiscussionTopicValues(Element discussionTopicElement, DiscussionTopic discussionTopic) {
		final String topicTitle = discussionTopicElement.getAttribute(TOPIC_TITLE);
		discussionTopic.setTitle(topicTitle);

		final String topicDraft = discussionTopicElement.getAttribute(DRAFT);
		if (StringUtils.isNotEmpty(topicDraft)) {
			discussionTopic.setDraft(Boolean.valueOf(topicDraft));
		}

		final String topicLocked = discussionTopicElement.getAttribute(LOCKED);
		if (StringUtils.isNotEmpty(topicLocked)) {
			discussionTopic.setLocked(Boolean.valueOf(topicLocked));
		}

		final String topicLockedAfterClosed = discussionTopicElement.getAttribute(LOCKED_AFTER_CLOSED);
		if (StringUtils.isNotEmpty(topicLockedAfterClosed)) {
			discussionTopic.setLockedAfterClosed(Boolean.valueOf(topicLockedAfterClosed));
		}

		final String topicPostAnonymous = discussionTopicElement.getAttribute(POST_ANONYMOUS);
		if (StringUtils.isNotEmpty(topicPostAnonymous)) {
			discussionTopic.setPostAnonymous(Boolean.valueOf(topicPostAnonymous));
		}

		final String topicAutoMarkThreadsRead = discussionTopicElement.getAttribute(AUTO_MARK_THREADS_READ);
		if (StringUtils.isNotEmpty(topicAutoMarkThreadsRead)) {
			discussionTopic.setAutoMarkThreadsRead(Boolean.valueOf(topicAutoMarkThreadsRead));
		}

		final String topicAllowEmailNotifications = discussionTopicElement.getAttribute(ALLOW_EMAIL_NOTIFICATIONS);
		if (StringUtils.isNotEmpty(topicAllowEmailNotifications)) {
			discussionTopic.setAllowEmailNotifications(Boolean.valueOf(topicAllowEmailNotifications));
		}

		final String topicIncludeContentsInEmails = discussionTopicElement.getAttribute(INCLUDE_CONTENTS_IN_EMAILS);
		if (StringUtils.isNotEmpty(topicIncludeContentsInEmails)) {
			discussionTopic.setIncludeContentsInEmails(Boolean.valueOf(topicIncludeContentsInEmails));
		}

		final String topicRevealIdsToRoles = discussionTopicElement.getAttribute(REVEAL_IDS_TO_ROLES);
		if (StringUtils.isNotEmpty(topicRevealIdsToRoles)) {
			discussionTopic.setRevealIDsToRoles(Boolean.valueOf(topicRevealIdsToRoles));
		}

		final String topicAvailableCloseDate = discussionTopicElement.getAttribute(CLOSE_DATE);
		if (StringUtils.isNotEmpty(topicAvailableCloseDate)) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(ARCHIVE_DATE_FORMAT);
				discussionTopic.setCloseDate(formatter.parse(topicAvailableCloseDate));
				discussionTopic.setAvailabilityRestricted(Boolean.TRUE);
			} catch (ParseException e) {
				log.debug("ERROR merging topic: Wrong date format or null in close date", e);
			}
		}

		final String topicAvailableOpenDate = discussionTopicElement.getAttribute(OPEN_DATE);
		if (StringUtils.isNotEmpty(topicAvailableOpenDate)) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(ARCHIVE_DATE_FORMAT);
				discussionTopic.setOpenDate(formatter.parse(topicAvailableOpenDate));
				discussionTopic.setAvailabilityRestricted(Boolean.TRUE);
			} catch (ParseException e) {
				log.debug("ERROR merging topic: Wrong date format or null in open date", e);
			}
		}

		final String topicModerated = discussionTopicElement.getAttribute(MODERATED);
		if (StringUtils.isNotEmpty(topicModerated)) {
			discussionTopic.setModerated(Boolean.valueOf(topicModerated));
		} else {
			discussionTopic.setModerated(Boolean.FALSE);
		}

		final String topicPostFirst = discussionTopicElement.getAttribute(POST_FIRST);
		if (StringUtils.isNotEmpty(topicPostFirst)) {
			discussionTopic.setPostFirst(Boolean.valueOf(topicPostFirst));
		} else {
			discussionTopic.setPostFirst(Boolean.FALSE);
		}

		final String sortIndex = discussionTopicElement.getAttribute(SORT_INDEX);
		if (StringUtils.isBlank(sortIndex)) {
			discussionTopic.setSortIndex(null);
		} else {
			try {
				Integer sortIndexAsInt = Integer.valueOf(sortIndex);
				discussionTopic.setSortIndex(sortIndexAsInt);
			} catch (NumberFormatException nfe) {
				discussionTopic.setSortIndex(null);
			}
		}
	}

	private List<Element> getMessagesElementList(List<Element> elements) {
		return elements.stream().filter(e -> MESSAGES.equals(e.getTagName()))
				.collect(Collectors.toList());
	}

	private List<Element> getChildElementList(Element discussionTopicElement) {
		final NodeList discussionTopicChildNodeList = discussionTopicElement.getChildNodes();
		final Stream<Node> discussionTopicChildnodes = IntStream.range(0, discussionTopicChildNodeList.getLength())
				.mapToObj(discussionTopicChildNodeList::item);

		return discussionTopicChildnodes.filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
				.map(element -> (Element) element).collect(Collectors.toList());
	}

	private List<Element> getAttachmentElementList(List<Element> elements) {
		return elements.stream().filter(e -> ATTACHMENT.equals(e.getTagName()))
				.collect(Collectors.toList());
	}

	private void mergeDiscussionTopicPropertiesNodes(final DiscussionTopic discussionTopic, final Element propertiesElement, final String siteId, MergeConfig mcx) {
		final NodeList propertyList = propertiesElement.getChildNodes();
		for (int n = 0; n < propertyList.getLength(); n++) {
			final Node propertyNode = propertyList.item(n);
			if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
				final Element propertyElement = (Element) propertyNode;
				if (propertyElement.getTagName().equals(PROPERTY)) {
					if (TOPIC_SHORT_DESC.equals(propertyElement.getAttribute(NAME))) {
						final String shortDescription = getDescriptionFromPropertyElement(propertyElement);
						discussionTopic.setShortDescription(shortDescription);
					} else if (TOPIC_LONG_DESC.equals(propertyElement.getAttribute(NAME))) {
						String extendedDescription = getDescriptionFromPropertyElement(propertyElement);
						extendedDescription = ltiService.fixLtiLaunchUrls(extendedDescription, siteId, mcx);
						extendedDescription = LinkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, extendedDescription);
						discussionTopic.setExtendedDescription(extendedDescription);
					}
				}
			}
		}
	}

	private void mergeDiscussionTopicPermissionsElement(final String siteId, final DiscussionTopic discussionTopic,
														final Element permissionElement) {
		final Set<DBMembershipItem> membershipItemSet = getMembershipItemSetFromPermissionElement(permissionElement,
				siteId);
		if (CollectionUtils.isNotEmpty(membershipItemSet)) {
			discussionTopic.setMembershipItemSet(membershipItemSet);
		}
	}

	private void mergeDiscussionTopicMessagesElement(final String siteId, final String fromSiteId, MergeConfig mcx, final DiscussionTopic discussionTopic,
													 final Element messagesElement, final String messageIdInReplyTo) throws Exception {
		final NodeList messagesNodeList = messagesElement.getChildNodes();
		for (int m = 0; m < messagesNodeList.getLength(); m++) {
			final Node messageNode = messagesNodeList.item(m);
			final Element messageElement = (Element) messageNode;

			Message message = createMessage(discussionTopic, messageIdInReplyTo, messageElement);

			// Merge messages in reply to this message once the new id is known
			final List<Element> elements = getChildElementList(messageElement);

			final List<Element> attachmentElementList = getAttachmentElementList(elements);

			for (Element attachmentElement : attachmentElementList) {
				final Attachment newAttachment = mergeAttachmentElement(siteId, fromSiteId, mcx,
						attachmentElement);
				if (newAttachment != null) {
					message.addAttachment(newAttachment);
				}
			}

			// Save the message to get the new id
			String messageId = messageManager.saveMessage(message);

			final List<Element> messagesElementList = getMessagesElementList(elements);
			for (Element messagesChildElement : messagesElementList) {
				mergeDiscussionTopicMessagesElement(siteId, fromSiteId, mcx, discussionTopic, messagesChildElement, messageId);
			}
		}
	}

	private Message createMessage(final DiscussionTopic discussionTopic, final String messageIdInReplyTo,
								  final Element messageElement) throws ParseException {
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		final Message message = messageManager.createMessage(messageElement.getAttribute(MESSAGE_TYPE_UUID));

		try {
			message.setId(Long.valueOf(messageElement.getAttribute(ID)));
		} catch (Exception e) {
			log.error("ERROR merging messages: Message with wrong or null id", e);
			throw e;
		}
		message.setTitle(messageElement.getAttribute(MESSAGE_TITLE));
		message.setAuthor(messageElement.getAttribute(MESSAGE_AUTHOR_NAME));
		try {
			message.setDraft(Boolean.valueOf(messageElement.getAttribute(DRAFT)));
		} catch (Exception e) {
			log.error(
					"ERROR merging messages: Wrong date format or null in draft in message with id: " + message.getId(),
					e);
			throw e;
		}
		message.setCreatedBy(messageElement.getAttribute(MESSAGE_CREATED_BY));

		try {
			message.setCreated(formatter.parse(messageElement.getAttribute(MESSAGE_CREATED_DATE)));
		} catch (ParseException e) {
			log.error("ERROR merging messages: Wrong date format or null in created date in message with id: "
					+ message.getId(), e);
			throw e;
		}

		message.setModifiedBy(messageElement.getAttribute(MESSAGE_MODIFIED_BY));
		try {
			message.setModified(formatter.parse(messageElement.getAttribute(MESSAGE_MODIFIED_DATE)));
		} catch (ParseException e) {
			log.error("ERROR merging messages: Wrong date format or null in modified date in message with id: "
					+ message.getId(), e);
			throw e;
		}
		message.setUuid(messageElement.getAttribute(MESSAGE_UUID));
		message.setDeleted(nullSafeToBoolean(messageElement.getAttribute(MESSAGE_DELETED)));
		message.setHasAttachments(nullSafeToBoolean(messageElement.getAttribute(MESSAGE_HAS_ATTACHMENTS)));
		message.setApproved(nullSafeToBoolean(messageElement.getAttribute(MESSAGE_APPROVED)));
		message.setGradeAssignmentName(messageElement.getAttribute(MESSAGE_GRADE_ASSIGNMENT_NAME));
		message.setLabel(messageElement.getAttribute(MESSAGE_LABEL));
		message.setNumReaders(nullSafeToInteger(messageElement.getAttribute(MESSAGE_NUM_READER)));
		message.setThreadId(nullSafeToLong(messageElement.getAttribute(MESSAGE_THREAD_ID)));
		message.setThreadLastPost(nullSafeToLong(messageElement.getAttribute(MESSAGE_THREAD_LAST_POST)));
		final String dateThreadLastUpdated = messageElement.getAttribute(MESSAGE_DATE_THREAD_LAST_UPDATED);
		if (StringUtils.isNotBlank(dateThreadLastUpdated)) {
			try {
				message.setDateThreadlastUpdated(formatter.parse(dateThreadLastUpdated));
			} catch (ParseException e) {
				log.error(
						"ERROR merging messages: Wrong date format or null in thread last updated in message with id: "
								+ message.getId());
				throw e;
			}
		}
		if (StringUtils.isNotBlank(messageIdInReplyTo)) {
			message.setInReplyTo(messageManager.getMessageById(Long.valueOf(messageIdInReplyTo)));
		}
		message.setBody(getDecodedString(messageElement.getAttribute(MESSAGE_BODY)));

		// Set the topic of the message
		message.setTopic(discussionTopic);
		return message;
	}

	private String getDescriptionFromPropertyElement(final Element propertyElement) {
		if (BASE64.equals(propertyElement.getAttribute(ENCODE))) {
			return getDecodedString(propertyElement.getAttribute(VALUE));
		} else {
			return propertyElement.getAttribute(VALUE);
		}
	}

	private String getDecodedString(final String inputString) {
		if (StringUtils.isNotBlank(inputString)) {
			return new String(base64Encoder.decode(inputString.getBytes()), StandardCharsets.UTF_8);
		}
		return StringUtils.EMPTY;
	}

	private String getEncodedString(final String inputString) {
		try {
			return new String(base64Encoder.encode(inputString.getBytes()));
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

	private Long nullSafeToLong(String stringToConvert) {
		Long longToReturn = null;
		if (StringUtils.isNotBlank(stringToConvert)) {
			longToReturn = Long.valueOf(stringToConvert);
		}
		return longToReturn;
	}

	private Boolean nullSafeToBoolean(String stringToConvert) {
		Boolean booleanToReturn = null;
		if (StringUtils.isNotBlank(stringToConvert)) {
			booleanToReturn = Boolean.valueOf(stringToConvert);
		}
		return booleanToReturn;
	}

	private Integer nullSafeToInteger(String stringToConvert) {
		Integer integerToReturn = null;
		if (StringUtils.isNumeric(stringToConvert)) {
			integerToReturn = Integer.valueOf(stringToConvert);
		}
		return integerToReturn;
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

	public Optional<String> getTool() {
		return Optional.of(FORUMS_TOOL_ID);
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

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}
	
	private Attachment copyAttachment(String attachmentId, String toContext, MergeConfig mcx) {
		try {			
			ContentResource attachment = contentHostingService.copyAttachment(attachmentId, toContext, toolManager.getTool("sakai.forums").getTitle(), mcx);

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
								membershipItem = permissionManager.saveDBMembershipItem(membershipItem);
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

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup)
	{	
		Map<String, String> transversalMap = new HashMap<>();
		try
		{
			log.debug("transfer copy mc items by transferCopyEntities with cleanup: {}", cleanup);
			if (cleanup)
			{
				try 
				{
					// Clean up the forums in the site
					List<DiscussionForum> destForums = dfManager.getDiscussionForumsByContextId(toContext);
					if (destForums != null && !destForums.isEmpty())
					{
						for (int currForum = 0; currForum < destForums.size(); currForum++) 
						{
							DiscussionForum dForum = (DiscussionForum) destForums.get(currForum);
							// clean up all messages
							List<DiscussionTopic> topics = dForum.getTopics();
							if (topics != null) {
								for (DiscussionTopic topic : topics) {
									List<Message> messages = dfManager.getTopicByIdWithMessagesAndAttachments(topic.getId()).getMessages();
									if (messages != null) {
										for (Message message : messages) {
											dfManager.deleteMessage(message);
										}
									}
								}
							}
							// Pass the forum object directly, not just the ID
							forumManager.deleteDiscussionForum(dForum);
						}
					}
					
					// Clean up the area-level permissions before copying
					Area toArea = areaManager.getDiscussionArea(toContext, false);
					if (toArea != null) {
						Set membershipItemSet = toArea.getMembershipItemSet();
						if (membershipItemSet != null && !membershipItemSet.isEmpty()) {
							// Clone the set to avoid ConcurrentModificationException
							Set<DBMembershipItem> itemsToRemove = new HashSet<>(membershipItemSet);
							for (DBMembershipItem item : itemsToRemove) {
								toArea.removeMembershipItem(item);
								// Simply remove the item from the area, no need to explicitly delete it
								// The permissionManager doesn't have a deleteDBMembershipItem method
							}
							areaManager.saveArea(toArea);
						}
					}
				}
				catch (Exception e)
				{
					log.warn("could not remove existing forums during copy: {}", e.toString());
				}
			}
			
			// Call the regular transferCopyEntities method to do the copying
			transversalMap.putAll(transferCopyEntities(fromContext, toContext, ids, options));
		}
		catch(Exception e)
		{
			log.error("Forums transferCopyEntities with cleanup failed", e);
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
					
					if(fromForum.getDefaultAssignName()!=null && transversalMap.get("gb/"+fromForum.getDefaultAssignName()) != null){
						fromForum.setDefaultAssignName(transversalMap.get("gb/"+fromForum.getDefaultAssignName()).substring(3));
						updateForum = true;
					}

					if(updateForum){
						//update forum
						fromForum = dfManager.saveForum(fromForum, fromForum.getDraft(), toContext, false, currentUserId);
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

							if(topic.getDefaultAssignName()!=null && transversalMap.get("gb/"+topic.getDefaultAssignName()) != null){
								topic.setDefaultAssignName(transversalMap.get("gb/"+topic.getDefaultAssignName()).substring(3));
								updateTopic = true;
							}

							if(updateTopic){
								//update forum
								dfManager.saveTopic(topic, topic.getDraft(), null, currentUserId);
							}
						}						
					}
				}
			}
		}
	}

	private String replaceAllRefs(String msgBody, Set<Entry<String, String>> entrySet) {
		String msgBodyToReturn = msgBody;
		if (msgBody != null) {
			msgBodyToReturn = LinkMigrationHelper.migrateAllLinks(entrySet, msgBody);
		}
		return msgBodyToReturn;
	}

	private Boolean getImportAsDraft() {
		boolean importAsDraft = serverConfigurationService.getBoolean("import.importAsDraft", true);
		return serverConfigurationService.getBoolean("msgcntr.forums.import.importAsDraft", importAsDraft);
	}

}
