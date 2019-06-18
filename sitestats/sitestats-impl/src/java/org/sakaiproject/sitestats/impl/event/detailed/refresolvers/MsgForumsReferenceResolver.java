/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.sitestats.api.event.detailed.forums.ForumData;
import org.sakaiproject.sitestats.api.event.detailed.forums.MessageData;
import org.sakaiproject.sitestats.api.event.detailed.forums.TopicData;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.MsgForumsRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.MsgForumsRefParser.MsgForumsEventRef;

/**
 *
 * @author plukasew
 */
@Slf4j
public class MsgForumsReferenceResolver
{
	public static final String FORUMS_TOOL_ID = "sakai.forums";
	public static final String MESSAGES_TOOL_ID = "sakai.messages";

	public enum Tool { FORUMS, MESSAGES; }

	public enum HierarchyLevel	{ FORUM, TOPIC, MESSAGE; }

	/**
	 * Resolves a Messages/Forums event reference into meaningful details about the event
	 * @param eventType the event type
	 * @param ref the event reference
	 * @param tips the tips used for parsing the event refs
	 * @param dfMan a MsgForums service class
	 * @param uiPermMan a MsgForums service class
	 * @param broker the entitybroker service
	 * @return one of the MsgForumsData variants, or ResolvedEventData.ERROR
	 */
	public static ResolvedEventData resolveEventReference(final String eventType, final String ref, List<EventParserTip> tips,
			DiscussionForumManager dfMan, UIPermissionsManager uiPermMan, EntityBroker broker)
	{
		Optional<ParsedMsgForumsRef> parsedRefOpt = parse(ref, tips);
		if (!parsedRefOpt.isPresent())
		{
			return ResolvedEventData.ERROR;
		}
		ParsedMsgForumsRef parsedRef = parsedRefOpt.get();

		switch(parsedRef.level)
		{
			case FORUM:
				DiscussionForum forum = dfMan.getForumById(parsedRef.itemId);
				return new ForumData(forum.getTitle());
			case TOPIC:
				Optional<DiscussionTopic> topic = findTopic(parsedRef.itemId, dfMan);
				return topic.isPresent() ? buildTopicData(topic.get(), dfMan, broker, uiPermMan) : ResolvedEventData.ERROR;
			case MESSAGE:
				Message msg = dfMan.getMessageById(parsedRef.itemId);
				if (msg == null)
				{
					log.error("Unable to retrieve message for id: {}", parsedRef.itemId);
					return ResolvedEventData.ERROR;
				}

				// we need the topic (and later the thread or "conversation") in order to provide necessary context around this message,
				// and also to properly support the anonymous forums feature and check forums permissions
				Topic tpc = msg.getTopic(); // lightweight topic, has only the id
				if (tpc == null || tpc.getId() == null)
				{
					log.error("Message {} has no topic.", msg.getId());
					return ResolvedEventData.ERROR;
				}

				Optional<DiscussionTopic> dTopic = findTopic(tpc.getId(), dfMan);
				if (!dTopic.isPresent())
				{
					log.error("Unable to find DiscussionTopic for topic id: {}", tpc.getId());
					return ResolvedEventData.ERROR;
				}

				TopicData td = buildTopicData(dTopic.get(), dfMan, broker, uiPermMan);
				if (td.deleted || !td.userPermittedToReadMsgs)
				{
					// we can't check permissions on the topic because the forum is required,
					// or the current user does not have permissions to read this topic (or the topic/forum is in draft, according to the implementation)
					return new MessageData.MessageDataTopicError(td);
				}

				// get the thread (conversation), if different from message
				MessageData thread = null;
				Long threadId = msg.getThreadId();
				if (threadId != null && !msg.getId().equals(threadId))
				{
					Message threadMsg = dfMan.getMessageById(threadId);
					if (threadMsg != null)
					{
						thread = new MessageData(td, null, threadMsg.getTitle(), threadMsg.getAuthor(), threadMsg.getCreated(), false);
					}
				}

				if ("forums.response".equals(eventType))
				{
					return new MessageData(td, thread, msg.getTitle(), msg.getAuthor(), msg.getCreated(), true);
				}
				return new MessageData(td, thread, msg.getTitle(), msg.getAuthor(), msg.getCreated(), false);
			default:
				log.error("Invalid HierarchyLevel: {} for ref {}", parsedRef.level, ref);
				return ResolvedEventData.ERROR;
		}
	}

	private static Optional<ParsedMsgForumsRef> parse(String eventRef, List<EventParserTip> tips)
	{
		MsgForumsEventRef ref = MsgForumsRefParser.parse(eventRef, tips);
		try
		{
			long itemId = Long.valueOf(ref.entityId);
			if (itemId < 1)
			{
				return Optional.empty();
			}
			Tool tool = Tool.valueOf(ref.tool.toUpperCase(Locale.ROOT));
			HierarchyLevel level = HierarchyLevel.valueOf(ref.subContextId.toUpperCase(Locale.ROOT));
			return Optional.of(new ParsedMsgForumsRef(itemId, ref.contextId, tool, level, ref.userId));
		}
		catch (IllegalArgumentException e)
		{
			// this is thrown if any of the valueOf() calls above fail, in which case the ref is malformed and cannot be resolved
			log.warn("Unable to parse, ref is malformed: {}", eventRef, e) ;
			return Optional.empty();
		}
	}

	private static TopicData buildTopicData(DiscussionTopic fullTopic, DiscussionForumManager dfMan, EntityBroker broker, UIPermissionsManager uiPermMan)
	{
		String linkUrl = null;
		boolean userCanRead = false;
		ForumData forumData = null;

		boolean isDeleted = fullTopic.getOpenForum() == null;

		if (!isDeleted)
		{
			// try to get the discussion topic and forum and build an entity link
			DiscussionForum forum = dfMan.getForumById(fullTopic.getOpenForum().getId());
			if (forum != null)
			{
				linkUrl = StringUtils.trimToNull(buildTopicUrl(fullTopic, broker));
				userCanRead = uiPermMan.isRead(fullTopic, forum); // uiPermMan.isRead() is slow but that's the best we have right now since the service permissions impl appears incomplete
				forumData = new ForumData(forum.getTitle());
			}
		}

		return new TopicData(forumData, fullTopic.getTitle(), linkUrl, isDeleted, fullTopic.getPostAnonymous(), userCanRead);
	}

	private static String buildTopicUrl(Topic t, EntityBroker broker)
	{
		EntityData edata = broker.getEntity("/forum_topic/" + t.getId());
		return edata != null ? edata.getEntityURL() : "";
	}

	// note that this only finds DiscussonTopic and may not work with Messages events (which we do not resolve by default)
	private static Optional<DiscussionTopic> findTopic(Long topicId, DiscussionForumManager dfMan)
	{
		// this call gets a fully-populated discussiontopic object
		Topic t = dfMan.getTopicByIdWithAttachments(topicId); // retrieves deleted topics with a small performance cost (an extra join)
		return (t instanceof DiscussionTopic) ? Optional.of((DiscussionTopic) t) : Optional.empty();
	}

	private static class ParsedMsgForumsRef
	{
		public final long itemId;
		public final String siteId;
		public final Tool tool;
		public final HierarchyLevel level;
		public final String userId;

		/**
		 * Data parsed out of a MsgForums event reference string
		 * @param itemId the item id
		 * @param siteId the site id
		 * @param tool the tool (Messages or Forums)
		 * @param level the hierarcy level for the item referenced (Forum, Topic, or Message)
		 * @param userId the user id
		 */
		public ParsedMsgForumsRef(long itemId, String siteId, Tool tool, HierarchyLevel level, String userId)
		{
			this.itemId = itemId;
			this.siteId = siteId;
			this.tool = tool;
			this.level = level;
			this.userId = userId;
		}
	}
}
