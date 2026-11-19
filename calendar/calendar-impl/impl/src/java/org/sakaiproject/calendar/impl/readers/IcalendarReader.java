/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.calendar.impl.readers;

import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TemporalAdapter;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.sakaiproject.calendar.impl.DailyRecurrenceRule;
import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.util.ResourceLoader;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class parses an import file from iCalendar.
 */
@Slf4j
public class IcalendarReader extends Reader {
	public static final long MINUTES_IN_DAY = TimeUnit.DAYS.toMinutes(1) - 5;
	private static final ResourceLoader resourceLoader = new ResourceLoader("calendar");

	private final Map<String, String> defaultHeaderMap = getDefaultColumnMap();

	public IcalendarReader() {
		super();
	}

	public String importStreamFromDelimitedFile(InputStream stream, ReaderImportRowHandler handler) {
		String calendarTzid = null;
		String[] descriptionColumns = {
				"Summary",
				"Description",
				"Start Date",
				"Start Time",
				"Duration",
				"Location",
				"Frequency",
				"Interval",
				"Ends"
		};

		trimLeadingTrailingQuotes(descriptionColumns);
		ColumnHeader[] columnDescriptionArray = buildColumnDescriptionArray(descriptionColumns);

		try {
			// enable "relaxed parsing"; read file using LF instead of CRLF
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true); 
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true); 
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true); 
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true); 

			Calendar calendar = new CalendarBuilder().build(stream);

			ZoneId calendarZone = null;
			try {
				Component vTimeZone = calendar.getComponent(Component.VTIMEZONE).orElse(null);
				if (vTimeZone != null) {
					Property tzProperty = vTimeZone.getProperty(Property.TZID).orElse(null);
					if (tzProperty != null) {
						calendarTzid = tzProperty.getValue();
						calendarZone = ZoneId.of(calendarTzid);
						log.debug("Calendar time zone is valid [{}]", calendarZone);
					}
				}
				log.debug("Calendar time zone not found");
			} catch (Exception e) {
				log.warn("Error reading VTIMEZONE component/TZID property: [{}]", e.toString());
				calendarTzid = null;
			}

			if (calendarZone == null) {
				calendarZone = timeService.getLocalTimeZone().toZoneId();
			}

			ZoneId importZone = calendarZone;
			int lineNumber = 1;
			for (Component event : calendar.getComponents(Component.VEVENT)) {
				try {
					Property summary = event.getProperty(Property.SUMMARY).orElse(null);
					DateProperty<?> start = event.<DateProperty<?>>getProperty(Property.DTSTART).orElse(null);
					if (summary == null || start == null) {
						log.warn("Skipping calendar event missing SUMMARY or DTSTART: [{}]", event);
						continue;
					}

					Property description = event.getProperty(Property.DESCRIPTION).orElse(null);
					Property location = event.getProperty(Property.LOCATION).orElse(null);
					ZonedDateTime now = ZonedDateTime.now(importZone);
					Period<?> range;
					if (start.getDate() instanceof LocalDate date) {
						boolean recurring = !event.getProperties(Property.RRULE).isEmpty()
								|| !event.getProperties(Property.RDATE).isEmpty();
						// Preserve imports of one-off all-day events outside the recurrence window.
						// iCal4j determines the event end from DTEND, DURATION, or the one-day default.
						range = recurring
								? new Period<>(now.minusMonths(6).toLocalDate(), now.plusMonths(12).toLocalDate())
								: new Period<>(date, date.plusDays(1));
					} else {
						range = new Period<>(now.minusMonths(6), now.plusMonths(12));
					}

					Set<Period<Temporal>> periods = event.calculateRecurrenceSet(range);
					for (Period<Temporal> period : periods.stream()
							.sorted(Comparator.comparing(period -> TemporalAdapter.toLocalTime(period.getStart(), importZone).toInstant()))
							.toList()) {
						processRow(handler, summary.getValue(), description != null ? description.getValue() : "",
								columnDescriptionArray, location != null ? location.getValue() : "",
								lineNumber++, period, importZone);
					}
				} catch (RuntimeException e) {
					String uid = event.getProperty(Property.UID).map(Property::getValue).orElse("unknown");
					log.warn("Could not import calendar event with UID [{}]", uid, e);
				}
			}
		} catch (Exception e) {
			log.warn("Could not read the calendaring stream", e);
		}

		// tzid of calendar (returns null if it does not exist)
		return calendarTzid;
	}

	private void processRow(ReaderImportRowHandler handler, String summary, String description,
			ColumnHeader[] headers, String location, int lineNumber, Period<Temporal> period, ZoneId zone) {
		boolean allDay = period.getStart() instanceof LocalDate;
		LocalDate date;
		LocalTime time;
		long minutes;
		String frequency = "";
		String until = "";
		if (allDay) {
			LocalDate start = (LocalDate) period.getStart();
			LocalDate end = (LocalDate) period.getEnd();
			if (!end.isAfter(start)) {
				log.warn("Skipping all-day event [{}] with non-positive date range", summary);
				return;
			}
			date = start;
			time = LocalTime.MIDNIGHT;
			minutes = MINUTES_IN_DAY;
			if (end.isAfter(start.plusDays(1))) {
				frequency = DailyRecurrenceRule.FREQ;
				until = GenericCalendarImporter.dateISOFormatter().format(end.minusDays(1));
			}
		} else {
			ZonedDateTime start = TemporalAdapter.toLocalTime(period.getStart(), zone);
			ZonedDateTime end = TemporalAdapter.toLocalTime(period.getEnd(), zone);
			minutes = Duration.between(start.toInstant(), end.toInstant()).toMinutes();
			if (minutes <= 0) {
				log.warn("Skipping timed event [{}] with non-positive duration", summary);
				return;
			}
			date = start.toLocalDate();
			time = start.toLocalTime();
		}

		String[] columns = { summary, description,
				GenericCalendarImporter.dateISOFormatter().format(date),
				GenericCalendarImporter.timeFormatter().format(time), Long.toString(minutes), location,
				frequency, frequency.isEmpty() ? "" : "1", until };
		try {
			handler.handleRow(processLine(headers, lineNumber, columns));
		} catch (ImportException e) {
			log.warn("Could not import calendar event [{}/{}]", lineNumber, summary, e);
		}
	}

	public List<Map<String, Object>> filterEvents(List<Map<String, Object>> events, String[] customFieldNames, ZoneId srcZoneId) throws ImportException {
		//
		// Convert the date/time fields as they appear in the Outlook import to
		// be a synthesized start/end timerange.
		//
		int lineNumber = 1;
		for (Map<String, Object> event : events) {

			LocalTime startTime = (LocalTime) event.get(defaultHeaderMap.get(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER));
			LocalDate startDate = (LocalDate) event.get(defaultHeaderMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER));
			Integer durationInMinutes = (Integer) event.get(defaultHeaderMap.get(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER));
			
			if (startTime == null ) {
				String msg = resourceLoader.getFormattedMessage("err_no_stime_on", lineNumber);
				throw new ImportException( msg );
			}
			if (startDate == null) {
				String msg = resourceLoader.getFormattedMessage("err_no_start", lineNumber);
				throw new ImportException( msg );
			}
			if (durationInMinutes == null) {
				String msg = resourceLoader.getFormattedMessage("err_no_dur", lineNumber);
				throw new ImportException( msg );
			}
			
			// Raw date + raw time
			LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
			Instant startInstant = startDateTime.atZone(srcZoneId).toInstant();

			// Duration of event
			Duration gapMinutes = Duration.ofMinutes(durationInMinutes);
			
			// Time Service will ajust to current user's TZ
			event.put(GenericCalendarImporter.ACTUAL_TIMERANGE, getTimeService().newTimeRange(startInstant.toEpochMilli(), gapMinutes.toMillis()));

			lineNumber++;
		}
		
		return events;
	}

	public Map<String, String> getDefaultColumnMap() {
		Map<String, String> columnHeaderMap = new HashMap<>();

		columnHeaderMap.put(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER, "Summary");
		columnHeaderMap.put(GenericCalendarImporter.DESCRIPTION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DATE_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.START_TIME_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DURATION_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.LOCATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.LOCATION_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.FREQUENCY_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.FREQUENCY_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.INTERVAL_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.INTERVAL_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.ENDS_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.ENDS_PROPERTY_NAME);

		return columnHeaderMap;
	}
}
