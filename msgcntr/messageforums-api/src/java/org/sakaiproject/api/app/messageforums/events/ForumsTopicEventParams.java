/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums.events;

import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;

/**
 * The parameters (type and LRS statement) for a Forums topic event to be posted to the Event Tracking Service
 * @author plukasew
 */
public class ForumsTopicEventParams
{
	public final TopicEvent event;
	public final LRS_Statement lrsStatement;

	public ForumsTopicEventParams(TopicEvent event, LRS_Statement lrsStatement)
	{
		this.event = event;
		this.lrsStatement = lrsStatement;
	}

	public enum TopicEvent
	{
		ADD(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, true),
		READ(DiscussionForumService.EVENT_FORUMS_TOPIC_READ, false),
		REMOVE(DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE, true),
		REVISE(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, true);

		public final String type;
		public final boolean modification;

		TopicEvent(String type, boolean modification)
		{
			this.type = type;
			this.modification = modification;
		}
	}
}
