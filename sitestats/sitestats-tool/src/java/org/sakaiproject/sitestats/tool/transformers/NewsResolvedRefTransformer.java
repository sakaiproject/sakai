/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sakaiproject.sitestats.tool.transformers;

import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.news.FeedData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class NewsResolvedRefTransformer
{
	/**
	 * Transforms FeedData for presentation to the user
	 * @param feed the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(FeedData feed, ResourceLoader rl)
	{
		return Collections.singletonList(EventDetail.newText(rl.getString("de_news_feed"), feed.title));
	}
}
