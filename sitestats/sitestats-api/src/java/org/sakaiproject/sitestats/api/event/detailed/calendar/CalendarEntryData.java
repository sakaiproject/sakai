/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed.calendar;

import java.time.Instant;
import java.util.Date;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.time.api.TimeRange;

/**
 * Data for a calendar entry
 * @author plukasew
 */
public class CalendarEntryData implements ResolvedEventData
{
	public static final String FREQ_ONCE = "Once";

	public final String title;
	public final Instant start, end;
	public final int interval;
	public final String frequencyUnit;

	/**
	 * Constructor
	 * @param title the title of the entry
	 * @param range the date range for the entry
	 * @param interval the interval of the entry
	 * @param frequencyUnit localized description of the frequency unit for the interval (ie. weeks, months)
	 */
	public CalendarEntryData(String title, TimeRange range, int interval, String frequencyUnit)
	{
		this.title = title;
		this.start = new Date(range.firstTime().getTime()).toInstant();
		this.end = new Date(range.lastTime().getTime()).toInstant();
		this.interval = interval;
		this.frequencyUnit = frequencyUnit;
	}
}
