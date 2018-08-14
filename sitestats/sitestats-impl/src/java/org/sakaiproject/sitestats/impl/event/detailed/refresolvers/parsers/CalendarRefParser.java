package org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers;

import java.util.List;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser.GenericEventRef;

/**
 * Parser for calendar event references.
 *
 * @author plukasew
 */
public class CalendarRefParser
{
	/**
	 * Parses a Calendar event reference
	 * @param eventType the type of event
	 * @param ref the event reference
	 * @param tips tips for parsing the event
	 * @return a GenericEventRef where context = siteId, subContextId is empty, and entity = calendarEventId
	 */
	public static GenericEventRef parse(String eventType, String ref, List<EventParserTip> tips)
	{
		if (!"calendar.read".equals(eventType))
		{
			return GenericRefParser.parse(ref, tips);
		}

		//calendar.read event refs look like this:
		// /calendar/calendar/9e998ef7-1d53-4cf6-97d1-8499439996ee/week
		// /calendar/calendar/9e998ef7-1d53-4cf6-97d1-8499439996ee/week/021a2c82-8d44-437d-b92f-35f1c9b5c6d8
		// /calendar/calendar/9e998ef7-1d53-4cf6-97d1-8499439996ee/day/021a2c82-8d44-437d-b92f-35f1c9b5c6d8
		// /calendar/calendar/9e998ef7-1d53-4cf6-97d1-8499439996ee/description/021a2c82-8d44-437d-b92f-35f1c9b5c6d8
		String[] tokens = ref.split("/");
        String context = tokens[3];
		String entity = tokens.length > 5 ? tokens[5] : "";

		return new GenericEventRef(context, "", entity);
	}
}
