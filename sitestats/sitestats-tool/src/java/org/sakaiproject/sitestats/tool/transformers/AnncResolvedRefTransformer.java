package org.sakaiproject.sitestats.tool.transformers;

import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.announcements.AnnouncementData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class AnncResolvedRefTransformer
{
	/**
	 * Transforms AnnouncementData for presentation to the user
	 * @param annc announcement data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(AnnouncementData annc, ResourceLoader rl)
	{
		return Collections.singletonList(EventDetail.newText(rl.getString("de_annc_title"), annc.title));
	}
}
