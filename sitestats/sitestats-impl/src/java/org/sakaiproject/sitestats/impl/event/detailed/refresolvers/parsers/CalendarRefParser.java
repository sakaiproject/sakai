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
