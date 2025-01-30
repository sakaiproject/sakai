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
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.parameter.Value;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(stream);

			ZoneId calendarZone = null;
			try {
				Component vTimeZone = calendar.getComponent(Component.VTIMEZONE);
				if (vTimeZone != null) {
					Property tzProperty = vTimeZone.getProperty(Property.TZID);
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

			int lineNumber = 1;
			for (Component event : calendar.getComponents(Component.VEVENT)) {
				Property summary = event.getProperty(Property.SUMMARY);
				Property start = event.getProperty(Property.DTSTART);
				Property end = event.getProperty(Property.DTEND);
				Property location = event.getProperty(Property.LOCATION);
				Property description = event.getProperty(Property.DESCRIPTION);

				if (summary == null) {
					log.warn("Defer importing this calendar event as it has no SUMMARY, [{}]", event);
					continue;
				}

				if ( start == null || end == null) {
					log.warn("Defer importing this calendar event as it has no START or END, [{}]", event);
					continue;
				}

				if (start.getParameters().contains(Value.DATE)) {
					// all day event
					Date startDate = Date.from(LocalDate
							.parse(start.getValue(), DateTimeFormatter.BASIC_ISO_DATE)
							.atStartOfDay(calendarZone)
							.toInstant());
					Date endDate = Date.from(LocalDate
							.parse(end.getValue(), DateTimeFormatter.BASIC_ISO_DATE)
							.atTime(LocalTime.MAX)
							.atZone(calendarZone)
							.minusDays(1)
							.toInstant());
					Period period = new Period(new DateTime(startDate), new DateTime(endDate));

					processRow(handler,
							summary.getValue(),
							description != null ? description.getValue() : "",
							columnDescriptionArray,
							location != null ? location.getValue() : "",
							lineNumber,
							period);
					lineNumber++;
				} else {
					DateTime from = new DateTime(Date.from(ZonedDateTime.now().minusMonths(6).toInstant()));
					DateTime to = new DateTime(Date.from(ZonedDateTime.now().plusMonths(12).toInstant()));
					Period range = new Period(from, to);

					PeriodList periods = event.calculateRecurrenceSet(range);
					for (Period period : periods) {
						processRow(handler,
								summary.getValue(),
								description != null ? description.getValue() : "",
								columnDescriptionArray,
								location != null ? location.getValue() : "",
								lineNumber,
								period);
						lineNumber++;
					}
				}
			}
		} catch (Exception e) {
			log.warn("The calendaring stream finished abnormally, {}", e.toString());
		}

		// tzid of calendar (returns null if it does not exist)
		return calendarTzid;
	}

	private void processRow(ReaderImportRowHandler handler, String summary, String description,
							ColumnHeader[] columnDescriptionArray, String location, int lineNumber, Period period) {

		TemporalAmount amount = period.getDuration();
		if (amount.getUnits().stream().anyMatch(TemporalUnit::isDurationEstimated)) {
			log.warn("Estimated duration for event [{}] detected, skipping", summary);
			return;
		}

		String[] columns;
		long durationMinutes = Duration.from(amount).toMinutes();

		// for any event spanning more than a single day we setup a daily frequency using until
		if (durationMinutes > MINUTES_IN_DAY) {
			columns = new String[] {
					summary,
					description,
					GenericCalendarImporter.dateISOFormatter().format(period.getStart().toInstant().atZone(ZoneId.systemDefault())),
					GenericCalendarImporter.timeFormatter().format(period.getStart().toInstant().atZone(ZoneId.systemDefault())),
					Long.toString(MINUTES_IN_DAY),
					location,
					DailyRecurrenceRule.FREQ,
					"1",
					GenericCalendarImporter.dateISOFormatter().format(period.getEnd().toInstant().atZone(ZoneId.systemDefault()))
			};
		} else {
			columns = new String[] {
					summary,
					description,
					GenericCalendarImporter.dateISOFormatter().format(period.getStart().toInstant().atZone(ZoneId.systemDefault())),
					GenericCalendarImporter.timeFormatter().format(period.getStart().toInstant().atZone(ZoneId.systemDefault())),
					durationMinutes < 10 ? "0" + durationMinutes : "" + durationMinutes,
					location,
					"",
					"",
					""
			};
		}

        try {
            handler.handleRow(processLine(columnDescriptionArray, lineNumber, columns));
        } catch (ImportException e) {
            log.warn("Could not import period [{}] for event [{}/{}], {}", period, lineNumber, summary, e.toString());
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
