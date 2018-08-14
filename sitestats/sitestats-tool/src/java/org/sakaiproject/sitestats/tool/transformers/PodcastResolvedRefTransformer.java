package org.sakaiproject.sitestats.tool.transformers;

import java.util.ArrayList;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.podcasts.PodcastData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class PodcastResolvedRefTransformer
{
	/**
	 * Transforms PodcastData for presentation to the user
	 * @param data the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(PodcastData data, ResourceLoader rl)
	{
		List<EventDetail> details = new ArrayList<>(2);

		UserTimeService uts = Locator.getFacade().getUserTimeService();

		details.add(EventDetail.newLink(rl.getString("de_podcasts_title"), data.title, data.parentUrl));
		details.add(EventDetail.newText(rl.getString("de_podcasts_datetime"), uts.shortLocalizedTimestamp(data.publishTime, rl.getLocale())));

		return details;
	}
}
