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
package org.sakaiproject.sitestats.tool.transformers;

import java.util.ArrayList;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.calendar.CalendarEntryData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class CalendarResolvedRefTransformer
{
	/**
	 * Transforms CalendarEntryData for presentation to the user
	 * @param calEntry the data
	 * @param msg resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(CalendarEntryData calEntry, ResourceLoader msg)
	{
		List<EventDetail> details = new ArrayList<>(4);
		details.add(EventDetail.newText(msg.getString("de_calendar_title"), calEntry.title));

		UserTimeService uts = Locator.getFacade().getUserTimeService();
		String start = uts.shortLocalizedTimestamp(calEntry.start, msg.getLocale());
		String end = uts.shortLocalizedTimestamp(calEntry.end, msg.getLocale());
		details.add(EventDetail.newText(msg.getString("de_calendar_duration"),
				msg.getFormattedMessage("de_calendar_duration_range", start, end)));

		String occurs = msg.getFormattedMessage("de_calendar_freq", String.valueOf(calEntry.interval), calEntry.frequencyUnit);
		if (CalendarEntryData.FREQ_ONCE.equals(calEntry.frequencyUnit))
		{
			occurs = msg.getFormattedMessage("de_calendar_freq_once");
		}

		details.add(EventDetail.newText(msg.getFormattedMessage("de_calendar_occurs"), occurs));
		return details;
	}
}
