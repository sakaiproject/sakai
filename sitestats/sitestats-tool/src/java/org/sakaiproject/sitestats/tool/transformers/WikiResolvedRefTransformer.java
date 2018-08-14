package org.sakaiproject.sitestats.tool.transformers;

import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.wiki.PageData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class WikiResolvedRefTransformer
{
	/**
	 * Transforms PageData for presentation to the user
	 * @param page the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(PageData page, ResourceLoader rl)
	{
		return Collections.singletonList(EventDetail.newLink(rl.getString("de_wiki_page"), page.name, page.url));
	}
}
