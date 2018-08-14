package org.sakaiproject.sitestats.tool.transformers;

import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.polls.PollData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class PollsResolvedRefTransformer
{
	/**
	 * Transforms PollData for presentation to the user
	 * @param poll the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(PollData poll, ResourceLoader rl)
	{
		return Collections.singletonList(EventDetail.newText(rl.getString("de_polls_poll"), poll.question));
	}
}
