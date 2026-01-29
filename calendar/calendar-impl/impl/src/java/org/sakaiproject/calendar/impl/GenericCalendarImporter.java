/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.impl;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarImporterService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.calendar.impl.readers.CSVReader;
import org.sakaiproject.calendar.impl.readers.IcalendarReader;
import org.sakaiproject.calendar.impl.readers.OutlookReader;
import org.sakaiproject.calendar.impl.readers.Reader;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides common importing functionality after a lower-level reader has taken care of the peculiarities of a given import format.
 */
@Slf4j
public class GenericCalendarImporter implements CalendarImporterService
{
	public static final String LOCATION_PROPERTY_NAME = "Location";
	public static final String LOCATION_DEFAULT_COLUMN_HEADER = "Location";
	public static final String ITEM_TYPE_PROPERTY_NAME = "ItemType";
	public static final String ITEM_TYPE_DEFAULT_COLUMN_HEADER = "Type";
	public static final String FREQUENCY_PROPERTY_NAME = "Frequency";
	public static final String FREQUENCY_DEFAULT_COLUMN_HEADER = "Frequency";
	public static final String END_TIME_PROPERTY_NAME = "EndTime";
	public static final String END_TIME_DEFAULT_COLUMN_HEADER = "EndTime";
	public static final String DURATION_PROPERTY_NAME = "Duration";
	public static final String DURATION_DEFAULT_COLUMN_HEADER = "Duration";
	public static final String START_TIME_PROPERTY_NAME = "Start Time";
	public static final String START_TIME_CSV_PROPERTY_NAME = "Start";
	public static final String START_TIME_DEFAULT_COLUMN_HEADER = "Start";
	public static final String DATE_PROPERTY_NAME = "Start Date";
	public static final String DATE_CSV_PROPERTY_NAME = "Date";
	public static final String DATE_DEFAULT_COLUMN_HEADER = "Date";
	public static final String DESCRIPTION_PROPERTY_NAME = "Description";
	public static final String DESCRIPTION_DEFAULT_COLUMN_HEADER = "Description";
	public static final String TITLE_PROPERTY_NAME = "Title";
	public static final String TITLE_DEFAULT_COLUMN_HEADER = "Title";
	public static final String INTERVAL_PROPERTY_NAME = "Interval";
	public static final String INTERVAL_DEFAULT_COLUMN_HEADER = "Interval";
	public static final String ENDS_PROPERTY_NAME = "Ends";
	public static final String ENDS_DEFAULT_COLUMN_HEADER = "Ends";
	public static final String REPEAT_PROPERTY_NAME = "Repeat";
	public static final String REPEAT_DEFAULT_COLUMN_HEADER = "Repeat";
	public static final String ACTUAL_TIMERANGE = "ActualStartTime";

	@Setter private static ResourceLoader rb = new ResourceLoader("calendar");

	public static DateTimeFormatter timeFormatter() {
		// Case-insensitive parser that accepts AM/PM in English regardless of system locale
		return new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("h:mm[:ss] a")
				.toFormatter(Locale.ENGLISH);
	}

	public static DateTimeFormatter time24HourFormatter() {
		// 24-hour format works the same in all locales
		return DateTimeFormatter.ofPattern("HH:mm[:ss]");
	}

	public static DateTimeFormatter dateISOFormatter() {
		return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(rb.getLocale());
	}

	public static DateTimeFormatter dateMDYFormatter() {
		return DateTimeFormatter.ofPattern("M/d/yyyy");
	}

	@Setter private CalendarService calendarService = null;
	@Setter private FormattedText formattedText;
	@Setter private TimeService timeService = null;

	private final Map<String, Class<? extends Reader>> readerMap = new HashMap<>();
	protected Map<String, String> columnMap = null;

	/**
	 * This class is used as a "prototype" event that may be added to a real calendar.
	 * It emulates enough of a calendar event to hold all the information necessary to create a real event.
	 */
	public class PrototypeEvent implements CalendarEventEdit {
        @Getter
        @Setter
        private RecurrenceRule recurrenceRule;

        @Getter
        @Setter
        private RecurrenceRule exclusionRule;

		private final Map<String, String> fields;

        @Getter
        @Setter
        private String location;

        @Getter
        @Setter
        private String siteId;

		@Getter
		@Setter
        private String eventUrl;

        @Getter
        @Setter
        private String type;

		private String description;

        @Getter
        @Setter
        private String displayName;

        @Setter
        @Getter
        private TimeRange range;

        @Getter
        @Setter
        private int lineNumber;

		@Getter
		@Setter
		private String creator;

		public PrototypeEvent() {
			fields = new HashMap<>();
		}

		@Override
		public String getDescription() {
			return formattedText.convertFormattedTextToPlaintext(description);
		}

		@Override
		public String getField(String fieldName) {
			return this.fields.get(fieldName);
		}

		@Override
		public String getCalendarReference() {
			return null;
		}

		@Override
		public String getUrl() {
			return null;
		}

		@Override
		public String getReference() {
			return null;
		}

		@Override
		public String getReference(String rootProperty) {
			return getReference();
		}

		@Override
		public String getUrl(String rootProperty) {
			return getUrl();
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getSiteName() {
			return null;
		}

		@Override
		public int compareTo(Object o) {
			return 0;
		}

		@Override
		public List<Reference> getAttachments() {
			return null;
		}

        @Override
        public Calendar getCalendar() {
            return null;
        }

        @Override
		public void setDescription(String description) {
			this.description = formattedText.convertPlaintextToFormattedText(description);
		}

		/**
		 * Returns true if current user is the event's owner/creator
		 * @return boolean true or false
		 */
		@Override
		public boolean isUserOwner() {
			return true;
		}

		/**
		 * Set the event creator (cover for PROP_CREATOR) to current user
		 */
		@Override
		public void setCreator() {
		}

		/**
		 * Gets the event modifier (userid), if any (cover for PROP_MODIFIED_BY).
		 * @return The event's modified-by property.
		 */
		@Override
		public String getModifiedBy() {
			return null;
		}

		/**
		 * Set the event modifier (cover for PROP_MODIFIED_BY) to current user
		 */
		@Override
		public void setModifiedBy() {
		}

		@Override
		public void setField(String key, String value) {
			this.fields.put(key, value);
		}

		@Override
		public boolean isActiveEdit() {
			return false;
		}

		@Override
		public ResourcePropertiesEdit getPropertiesEdit() {
			return null;
		}

		@Override
		public void addAttachment(Reference arg0) {
		}

		@Override
		public void removeAttachment(Reference arg0) {
		}

		@Override
		public void replaceAttachments(List<Reference> arg0) {
		}

		@Override
		public void clearAttachments() {
		}

		/**
		 * Get the start date formatted for display.
		 */
		public String getDisplayStartDate() {
			return this.range.firstTime().toStringLocalDate();
		}

		/**
		 * Get the start time formatted for display.
		 */
		public String getDisplayStartTime() {
			return this.range.firstTime().toStringLocalTime();
		}

		/**
		 * Get the end time of the event formatted for display. This handles the fact that events that end at a given time actually end about a minute earlier.
		 */
		public String getDisplayEndTime() {
			// We store event time ranges as slightly less than the end time.
			// Make a new time range that is inclusive, just to show the users.

			Time endTime = timeService.newTime(this.getRange().lastTime().getTime() + (60 * 1000));

			return endTime.toStringLocalTime();
		}

		@Override
		public void setDescriptionFormatted(String description) {
			this.description = description;
		}

		@Override
		public String getDescriptionFormatted() {
			return description;
		}

		@Override
		public Collection<String> getGroups() {
			return Collections.emptyList();
		}

		@Override
		public Collection<Group> getGroupObjects() {
			return Collections.emptyList();
		}

		@Override
		public EventAccess getAccess() {
			return CalendarEvent.EventAccess.SITE;
		}

		@Override
		public String getGroupRangeForDisplay(Calendar calendar) {
			return null;
		}

		@Override
		public void clearGroupAccess() throws PermissionException {
		}

		@Override
		public void setGroupAccess(Collection<Group> groups, boolean own) throws PermissionException {
		}
	}

	public GenericCalendarImporter() {
		super();
	}

	@Override
	public List<? extends CalendarEvent> doImport(String importType, InputStream importStream, Map<String, String> columnMapping, String[] customFieldPropertyNames) throws ImportException {
		return doImport(importType, importStream, columnMapping, customFieldPropertyNames, null);
	}

	@Override
	public List<? extends CalendarEvent> doImport(String importType, InputStream importStream, Map<String, String> columnMapping, String[] customFieldPropertyNames, String userTzid) throws ImportException {
		final List<Map<String, Object>> rowList;
		final Reader scheduleImport;

		try {
			scheduleImport = readerMap.get(importType).getDeclaredConstructor().newInstance();
			scheduleImport.setTimeService(timeService);
		} catch (InstantiationException | IllegalAccessException e1) {
			String msg = rb.getFormattedMessage("err_import", importType);
			throw new ImportException(msg);
		} catch (InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

		if (scheduleImport == null) {
			throw new ImportException(rb.getString("err_import_unknown"));
		}

		// If no column mapping has been specified, use the default.
		if (columnMapping != null) {
			scheduleImport.setColumnHeaderToAtributeMapping(columnMapping);
		}
		
		columnMap = scheduleImport.getDefaultColumnMap();
		
		// Read in the file.
		GenericImportRowHandler handler = new GenericImportRowHandler(columnMap, rb);
		String calendarTzid = scheduleImport.importStreamFromDelimitedFile(importStream, handler);
		rowList = handler.getRowList();

		// Calendar time zone remains over user time zone
		String tzid = calendarTzid==null ? userTzid:calendarTzid;
		ZoneId srcZoneId;
		if (tzid != null) {
			srcZoneId = ZoneId.of(tzid);
		} else {
			srcZoneId = ZoneId.of(timeService.getLocalTimeZone().getID());
		}
		return getPrototypeEvents(scheduleImport.filterEvents(rowList, customFieldPropertyNames, srcZoneId), customFieldPropertyNames);
	}

	/**
	 * Interprets the list of maps created by doImport()
	 */
	protected List<? extends CalendarEvent> getPrototypeEvents(List<Map<String, Object>> rowList, String[] customFieldPropertyNames) throws ImportException {
		List<PrototypeEvent> eventList = new ArrayList<>();
		int lineNumber = 1;

		for (Map<String, Object> eventProperties : rowList) {
			RecurrenceRule recurrenceRule = null;
			PrototypeEvent prototypeEvent = new PrototypeEvent();

			prototypeEvent.setDescription((String) eventProperties.get(columnMap.get(DESCRIPTION_DEFAULT_COLUMN_HEADER)));
			prototypeEvent.setDisplayName((String) eventProperties.get(columnMap.get(TITLE_DEFAULT_COLUMN_HEADER)));
			prototypeEvent.setLocation((String) eventProperties.get(columnMap.get(LOCATION_DEFAULT_COLUMN_HEADER)));
			prototypeEvent.setType((String) eventProperties.get(ITEM_TYPE_DEFAULT_COLUMN_HEADER));

			if (prototypeEvent.getType() == null || prototypeEvent.getType().isEmpty()) {
				prototypeEvent.setType("Activity");
			}

			// The time range has been calculated in the reader, based on
			// whatever time fields are available in the particular import format.
			// This range has been placed in the ACTUAL_TIMERANGE property.

			TimeRange timeRange = (TimeRange) eventProperties.get(GenericCalendarImporter.ACTUAL_TIMERANGE);

			if (timeRange == null) {
            	String msg = rb.getFormattedMessage("err_notime", Integer.valueOf(lineNumber));
            	throw new ImportException( msg );
			}

			// The start/end times were calculated during the import process.
			prototypeEvent.setRange(timeRange);

			// Do custom fields, if any.
			if (customFieldPropertyNames != null) {
                for (String customFieldPropertyName : customFieldPropertyNames) {
                    prototypeEvent.setField(customFieldPropertyName, (String) eventProperties.get(customFieldPropertyName));
                }
			}

			// See if this is a recurring event
			String frequencyString = (String) eventProperties.get(columnMap.get(FREQUENCY_DEFAULT_COLUMN_HEADER));

			if (frequencyString != null) {
				Integer interval = (Integer) eventProperties.get(columnMap.get(INTERVAL_DEFAULT_COLUMN_HEADER));
				Integer count = (Integer) eventProperties.get(columnMap.get(REPEAT_DEFAULT_COLUMN_HEADER));
				LocalDate until = (LocalDate) eventProperties.get(columnMap.get(ENDS_DEFAULT_COLUMN_HEADER));

				if (count != null && until != null) {
					String msg = rb.getFormattedMessage("err_datebad", Integer.valueOf(lineNumber));
					throw new ImportException(msg);
				}

				if (interval == null && count == null && until == null) {
					recurrenceRule = calendarService.newRecurrence(frequencyString);
				} else if (until == null && interval != null && count != null) {
					recurrenceRule = calendarService.newRecurrence(frequencyString, interval, count);
				} else if (until == null && interval != null && count == null) {
					recurrenceRule = calendarService.newRecurrence(frequencyString, interval);
				} else if (until != null && interval != null && count == null) {
					Time untilTime = timeService.newTime(until.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
					recurrenceRule = calendarService.newRecurrence(frequencyString, interval, untilTime);
				}

				// See if we were able to successfully create a recurrence rule.
				if (recurrenceRule == null) {
					String msg = rb.getFormattedMessage("err_freqbad", Integer.valueOf(lineNumber));
					throw new ImportException(msg);
				}

				prototypeEvent.setRecurrenceRule(recurrenceRule);
			}
			prototypeEvent.setLineNumber(lineNumber);
			eventList.add(prototypeEvent);
			lineNumber++;
		}

		return eventList;
	}

	@Override
	public Map<String, String> getDefaultColumnMap(String importType) throws ImportException {
		try {
			Reader scheduleImport = readerMap.get(importType).getDeclaredConstructor().newInstance();
            return scheduleImport.getDefaultColumnMap();
        } catch (InstantiationException | IllegalAccessException e1) {
			String msg = rb.getFormattedMessage("err_import", importType);
			throw new ImportException(msg);
		} catch (InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void init() {
		try {
			// Add our readers. This might be done from a
			// config file in future versions.
			readerMap.put(OUTLOOK_IMPORT, OutlookReader.class);
			readerMap.put(CSV_IMPORT, CSVReader.class);
			readerMap.put(ICALENDAR_IMPORT, IcalendarReader.class);
		} catch (Exception e) {
			log.warn("could not initialized readers, {}", e.toString());
		}
	}

	public void destroy() {
		log.info("destroy()");
	}

}
