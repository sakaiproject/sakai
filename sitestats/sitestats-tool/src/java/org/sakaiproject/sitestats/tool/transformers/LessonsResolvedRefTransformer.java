/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentsSectionItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.ContentLinkItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.EmbeddedItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.GenericItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.LessonsData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.PageData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.TextItemData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 *
 * @author bjones86, plukasew
 */
public class LessonsResolvedRefTransformer
{
	private static final int TRUNCATE_LENGTH = 50;

	/**
	 * Transforms LessonsData for presentation to the user
	 * @param resolved the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(LessonsData resolved, ResourceLoader rl)
	{
		List<EventDetail> eventDetails = new ArrayList<>(3);
		if (resolved instanceof TextItemData)
		{
			TextItemData text = (TextItemData) resolved;
			addEventDetailsText(eventDetails, rl.getString("de_lessons_item_text"), trunc(text.html));
			addEventDetailsText(eventDetails, rl.getString("de_lessons_page"), getPageDisplay(text.parentPage, rl));
		}
		else if (resolved instanceof EmbeddedItemData)
		{
			EmbeddedItemData embed = (EmbeddedItemData) resolved;
			addEventDetailsText(eventDetails, rl.getString("de_lessons_item"), rl.getString("de_lessons_embed"));
			if( !embed.desc.isEmpty() )
			{
				addEventDetailsText( eventDetails, rl.getString("de_lessons_desc"), trunc(embed.desc) );
			}

			addEventDetailsText( eventDetails, rl.getString("de_lessons_page"), getPageDisplay(embed.parentPage, rl) );
		}
		else if (resolved instanceof ContentLinkItemData)
		{
			ContentLinkItemData link = (ContentLinkItemData) resolved;
			addEventDetailsText( eventDetails, rl.getString("de_lessons_link"), link.name);
			addEventDetailsText( eventDetails, rl.getString("de_lessons_page"), getPageDisplay(link.parentPage, rl));
		}
		else if (resolved instanceof GenericItemData)
		{
			GenericItemData item = (GenericItemData) resolved;
			addEventDetailsText( eventDetails, rl.getString("de_lessons_item"), item.title );
			addEventDetailsText( eventDetails, rl.getString("de_lessons_page"), getPageDisplay(item.parentPage, rl));
		}
		else if (resolved instanceof GenericItemData.DeletedItem)
		{
			// we don't know if this was actually a top-level PAGE item or a regular item
			addEventDetailsText(eventDetails, rl.getString("de_lessons_item"), rl.getString("de_lessons_deleted"));
		}
		else if (resolved instanceof PageData)
		{
			PageData page = (PageData) resolved;
			EventDetail pageRef = EventDetail.newText(rl.getString("de_lessons_page"), page.title);
			eventDetails.add(pageRef);
			if (page.pageHierarchy.size() > 1)
			{
				eventDetails.add(EventDetail.newText(rl.getString("de_lessons_hierarchy"), getPageDisplay(page, rl)));
			}
		}
		else if (resolved instanceof PageData.DeletedPage)
		{
			addEventDetailsText(eventDetails, rl.getString("de_lessons_page"), rl.getString("de_lessons_deleted_page"));
		}
		else if (resolved instanceof CommentData)
		{
			CommentData comment = (CommentData) resolved;

			UserTimeService timeServ = Locator.getFacade().getUserTimeService();
			String commentTrunc = StringUtils.trimToEmpty(StringUtils.abbreviate( comment.comment, TRUNCATE_LENGTH));
			if( commentTrunc.isEmpty() )
			{
				commentTrunc = rl.getString("de_lessons_comment_deleted");
			}
			String timePosted = timeServ.shortLocalizedTimestamp(comment.timePosted, rl.getLocale());
			String author = formatAuthor(comment.author);
			String message = rl.getFormattedMessage("de_lessons_comment_template", commentTrunc, author, timePosted);
			addEventDetailsText(eventDetails, rl.getString("de_lessons_comment"), message);
			addEventDetailsText(eventDetails, rl.getString("de_lessons_page"), getPageDisplay(comment.parent, rl));
		}
		else if (resolved instanceof CommentsSectionItemData)
		{
			eventDetails.add(EventDetail.newText(rl.getString("de_lessons_item"), rl.getString("de_lessons_comments")));

			CommentsSectionItemData comments = (CommentsSectionItemData) resolved;
			eventDetails.add(EventDetail.newText(rl.getString("de_lessons_page"), getPageDisplay(comments.parent, rl)));
		}
		else if (resolved instanceof CommentsSectionItemData.ForcedComments)
		{
			eventDetails.add(EventDetail.newText(rl.getString("de_lessons_item"), rl.getString("de_lessons_comments")));
			String note = rl.getString("de_lessons_comments_placeholder");
			EventDetail ref = EventDetail.newText(rl.getString("de_lessons_note"), note);
			eventDetails.add(ref);
		}

		return eventDetails;
	}

	/**
	 * Add a new text EventDetail to the list, provided that both the key and value are not empty/blank/null
	 * @param eventDetails the list to add the EventDetail to
	 * @param key the key to be added
	 * @param value the value to be added
	 */
	private static void addEventDetailsText(List<EventDetail> eventDetails, String key, String value)
	{
		if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
		{
			eventDetails.add(EventDetail.newText(key, value));
		}
	}

	/**
	 * Returns the page title for a top level page, or the full page hierarchy for sub-pages
	 * @param page
	 * @return the page title, or full page hierarchy for sub-pages
	 */
	private static String getPageDisplay(PageData page, ResourceLoader rl)
	{
		return page.pageHierarchy.stream()
				.map(pg -> PageData.DELETED_HIERARCHY_PAGE.equals(pg) ? rl.getString("de_lessons_deleted_page") : pg)
				.collect(Collectors.joining(" > "));
	}

	private static String trunc(String text)
	{
		return StringUtils.abbreviate(text, TRUNCATE_LENGTH);
	}

	private static String formatAuthor(String uuid)
	{
		String siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		return Locator.getFacade().getStatsManager().getUserInfoForDisplay(uuid, siteId);
	}
}
