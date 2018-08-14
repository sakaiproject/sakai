package org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers;

import java.util.List;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser.GenericEventRef;

/**
 *
 * @author plukasew
 */
public class PodcastRefParser
{
	protected static final String RESOURCE = "resourceId";

	/**
	 * Parses a podcast event reference
	 * @param ref the event reference
	 * @param tips tips for parsing the reference
	 * @return a GenericEventRef where context = siteId, subContext is empty, and entity = the resource id
	 */
	public static GenericEventRef parse(String ref, List<EventParserTip> tips)
	{
		/* Formats:
		/content/group/<siteID>/Podcasts/<fileName>
		/content/group/<siteID>/Podcasts/<userID>//content/group/<siteID>/Podcasts/<fileName> (podcast.add)
		*/

		// first the generic parts
		GenericEventRef genRef = GenericRefParser.parse(ref, tips);

		// now the custom parts
		String resourceId = genRef.entityId;
		for (EventParserTip tip : tips)
		{
			if (RESOURCE.equals(tip.getFor()))
			{
				String res = GenericRefParser.parseTip(ref, tip);
				if (!res.isEmpty())
				{
					resourceId = res;
				}
			}
		}

		return new GenericRefParser.GenericEventRef(genRef.contextId, "", resourceId);
	}
}
