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
package org.sakaiproject.sitestats.tool.transformers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.forums.ForumData;
import org.sakaiproject.sitestats.api.event.detailed.forums.MessageData;
import org.sakaiproject.sitestats.api.event.detailed.forums.MsgForumsData;
import org.sakaiproject.sitestats.api.event.detailed.forums.TopicData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class ForumsResolvedRefTransformer
{
	/**
	 * Transforms MsgForumsData for presentation to the user
	 * @param resolved the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(MsgForumsData resolved, ResourceLoader rl)
	{
		if (resolved instanceof ForumData)
		{
			// even with a deleted forum, this is always just the forum title and can be presented as-is
			ForumData forum = (ForumData) resolved;
			return Collections.singletonList(EventDetail.newText(rl.getString("de_msgforums_title"), forum.title));
		}
		else if (resolved instanceof TopicData)
		{
			TopicData topic = (TopicData) resolved;
			return Collections.singletonList(topicToRef(topic, rl));
		}
		else if (resolved instanceof MessageData.MessageDataTopicError)
		{
			TopicData topic = ((MessageData.MessageDataTopicError) resolved).topic;
			if (topic.deleted)
			{
				EventDetail ref = EventDetail.newText(rl.getString("de_error"), rl.getString("de_msgforums_msgError_topicDeleted"));
				return Collections.singletonList(ref);
			}
			else if (!topic.userPermittedToReadMsgs)
			{
				EventDetail ref = EventDetail.newText(rl.getString("de_error"), rl.getString("de_msgforums_msgError_perms"));
				return Collections.singletonList(ref);
			}

			return Collections.emptyList();
		}
		else if (resolved instanceof MessageData)
		{
			MessageData msg = (MessageData) resolved;
			List<EventDetail> list = new ArrayList<>(3); // should have at most 3 entries

			TopicData topic = msg.topic;
			list.add(topicToRef(topic, rl));
			if (topic.anon)
			{
				list.add(EventDetail.newText(rl.getString("de_msgforums_conversation"), rl.getString("de_msgforums_conversation_anon")));
				list.add(EventDetail.newText(rl.getString("de_msgforums_message"), rl.getString("de_msgforums_message_anon")));
				return list;
			}

			UserTimeService timeServ = Locator.getFacade().getUserTimeService();
			msg.conversation.ifPresent(c -> ForumsResolvedRefTransformer.addMsgDetails(list, rl.getString("de_msgforums_conversation"), c, timeServ, rl));
			String key = msg.repliedTo ? rl.getString("de_msgforums_message_responded") : rl.getString("de_msgforums_message");
			ForumsResolvedRefTransformer.addMsgDetails(list, key, msg, timeServ, rl);

			return list;
		}

		return Collections.emptyList();
	}

	private static void addMsgDetails(List<EventDetail> list, String key, MessageData msg, UserTimeService timeServ, ResourceLoader rl)
	{
		String date = timeServ.shortLocalizedTimestamp(msg.creationDate, timeServ.getLocalTimeZone(), rl.getLocale());
		list.add(EventDetail.newText(key, rl.getFormattedMessage("de_msgforums_message_template", msg.title, msg.author, date)));
	}

	private static EventDetail topicToRef(TopicData topic, ResourceLoader rl)
	{
		if (topic.deleted)
		{
			return EventDetail.newText(rl.getString("de_msgforums_topic"), rl.getFormattedMessage("de_msgforums_topic_deleted", topic.title));
		}

		String topicPath = rl.getFormattedMessage("de_msgforums_topicPath", topic.forum.map(f -> f.title).orElse(""), topic.title);
		if (topic.entityUrl.isPresent())
		{
			return EventDetail.newLink(rl.getString("de_msgforums_topic"), topicPath, topic.entityUrl.get());
		}

		return EventDetail.newText(rl.getString("de_msgforums_topic"), topicPath);
	}
}
