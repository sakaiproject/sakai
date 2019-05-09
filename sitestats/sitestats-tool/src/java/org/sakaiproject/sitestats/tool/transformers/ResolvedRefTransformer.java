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

import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.calendar.CalendarEntryData;
import org.sakaiproject.sitestats.api.event.detailed.forums.MsgForumsData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.LessonsData;
import org.sakaiproject.sitestats.api.event.detailed.polls.PollData;
import org.sakaiproject.sitestats.api.event.detailed.wiki.PageData;
import org.sakaiproject.sitestats.api.event.detailed.news.FeedData;
import org.sakaiproject.sitestats.api.event.detailed.announcements.AnnouncementData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.AssignmentsData;
import org.sakaiproject.sitestats.api.event.detailed.content.ContentData;
import org.sakaiproject.sitestats.api.event.detailed.podcasts.PodcastData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.SamigoData;
import org.sakaiproject.sitestats.api.event.detailed.web.WebData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class ResolvedRefTransformer
{
	private static final ResourceLoader MSG = new ResourceLoader("Messages");

	/**
	 * Transforms ResolvedEventData for presentation to the user
	 * @param resolved the data
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(ResolvedEventData resolved)
	{
		if (resolved instanceof CalendarEntryData)
		{
			return CalendarResolvedRefTransformer.transform((CalendarEntryData) resolved, MSG);
		}
		else if (resolved instanceof MsgForumsData)
		{
			return ForumsResolvedRefTransformer.transform((MsgForumsData) resolved, MSG);
		}
		else if (resolved instanceof LessonsData)
		{
			return LessonsResolvedRefTransformer.transform((LessonsData) resolved, MSG);
		}
		else if (resolved instanceof PollData)
		{
			return PollsResolvedRefTransformer.transform((PollData) resolved, MSG);
		}
		else if (resolved instanceof PageData)
		{
			return WikiResolvedRefTransformer.transform((PageData) resolved, MSG);
		}
		else if (resolved instanceof FeedData)
		{
			return NewsResolvedRefTransformer.transform((FeedData) resolved, MSG);
		}
		else if (resolved instanceof AnnouncementData)
		{
			return AnncResolvedRefTransformer.transform((AnnouncementData) resolved, MSG);
		}
		else if (resolved instanceof AssignmentsData)
		{
			return AsnResolvedRefTransformer.transform((AssignmentsData) resolved, MSG);
		}
		else if (resolved instanceof PodcastData)
		{
			return PodcastResolvedRefTransformer.transform((PodcastData) resolved, MSG);
		}
		else if (resolved instanceof ContentData)
		{
			return ContentResolvedRefTransformer.transform((ContentData) resolved, MSG);
		}
		else if (resolved instanceof WebData)
		{
			return WebResolvedRefTransformer.transform((WebData) resolved, MSG);
		}
		else if (resolved instanceof SamigoData)
		{
			return SamigoResolvedRefTransformer.transform((SamigoData) resolved, MSG);
		}
		else if (resolved instanceof ResolvedEventData.NoDetails)
		{
			return Collections.singletonList(EventDetail.newText(MSG.getString("de_info"), MSG.getString("de_nodetails")));
		}
		else if (resolved instanceof ResolvedEventData.PermissionError)
		{
			return Collections.singletonList(EventDetail.newText(MSG.getString("de_error"), MSG.getString("de_noperms")));
		}

		// ResolvedEventData.Error or other error state
		return Collections.singletonList(EventDetail.newText(MSG.getString("de_error"), MSG.getString("de_nodata")));
	}
}
