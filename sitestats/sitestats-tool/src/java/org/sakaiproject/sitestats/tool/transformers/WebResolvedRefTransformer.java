package org.sakaiproject.sitestats.tool.transformers;

import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.web.WebData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class WebResolvedRefTransformer
{
	/**
	 * Transforms WebData for presentation to the user
	 * @param data the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(WebData data, ResourceLoader rl)
	{
		String page = data.pageName;
		if (data.toolName.isPresent())
		{
			page += " - " + data.toolName.get();
		}

		return Collections.singletonList(EventDetail.newLink(rl.getString("de_web_page"), page, data.url));
	}
}
