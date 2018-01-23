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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarImporterService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.calendar.impl.readers.CSVReader;
import org.sakaiproject.calendar.impl.readers.MeetingMakerReader;
import org.sakaiproject.calendar.impl.readers.OutlookReader;
import org.sakaiproject.calendar.impl.readers.IcalendarReader;
import org.sakaiproject.calendar.impl.readers.Reader;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

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

	// Injected Property Names - These properties are synthesized during the
	// translation process.
	public static final String ACTUAL_TIMERANGE = "ActualStartTime";

	// Map of readers for various formats. Keyed by import type.
	private final Map<String, Class<? extends Reader>> readerMap = new HashMap<>();
	
	protected Map<String, String> columnMap = null;

	private DateFormat timeFormatter()
	{
		DateFormat rv = new SimpleDateFormat("hh:mm a");
		rv.setLenient(false);
		return rv;
	}

	private DateFormat timeFormatterWithSeconds()
	{
		return new SimpleDateFormat("hh:mm:ss a");
	}

	private DateFormat time24HourFormatter()
	{
		DateFormat rv = new SimpleDateFormat("HH:mm");
		rv.setLenient(false);
		return rv;
	}

	private DateFormat time24HourFormatterWithSeconds()
	{
		DateFormat rv = new SimpleDateFormat("HH:mm:ss");
		rv.setLenient(false);
		return rv;
	}

	private static ResourceLoader rb = new ResourceLoader("calendar");

	// These are injected at runtime by Spring.
	private CalendarService calendarService = null;

	private TimeService timeService = null;

	/*
	 * This class is used as a "prototype" event that may be added to a real calendar. We emulate enough of a calendar event to hold all the information necessary to create a real event.
	 */
	public class PrototypeEvent implements CalendarEventEdit
	{
		private RecurrenceRule recurrenceRule;
		
		private RecurrenceRule exclusionRule;

		private Map fields;

		private String location;

		private String type;

		private String description;

		private String displayName;

		private TimeRange timeRange;

		private int lineNumber;
		
		private String creator;

		/**
		 * Default constructor
		 */
		public PrototypeEvent()
		{
			fields = new HashMap();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getRange()
		 */
		public TimeRange getRange()
		{
			return this.timeRange;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getDisplayName()
		 */
		public String getDisplayName()
		{
			return this.displayName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getDescription()
		 */
		public String getDescription()
		{
			return FormattedText.convertFormattedTextToPlaintext(description);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getType()
		 */
		public String getType()
		{
			return this.type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getLocation()
		 */
		public String getLocation()
		{
			return this.location;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getField(java.lang.String)
		 */
		public String getField(String fieldName)
		{
			return (String) this.fields.get(fieldName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getCalendarReference()
		 */
		public String getCalendarReference()
		{
			// Stub routine only
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getRecurrenceRule()
		 */
		public RecurrenceRule getRecurrenceRule()
		{
			// Stub routine only
			return this.recurrenceRule;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getExclusionRule()
		 */
		public RecurrenceRule getExclusionRule()
		{
			// Stub routine only
			return this.exclusionRule;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Resource#getUrl()
		 */
		public String getUrl()
		{
			// Stub routine only
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Resource#getReference()
		 */
		public String getReference()
		{
			// Stub routine only
			return null;
		}

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Resource#getId()
		 */
		public String getId()
		{
			// Stub routine only
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Resource#getProperties()
		 */
		public ResourceProperties getProperties()
		{
			// Stub routine only
			return null;
		}

		/**
		 * @inheritDoc
		 */
		public String getSiteName()
		{
			// Stub routine only
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Resource#toXml(org.w3c.dom.Document, java.util.Stack)
		 */
		public Element toXml(Document arg0, Stack arg1)
		{
			// Stub routine only
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o)
		{
			// Stub routine only
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainer#getAttachments()
		 */
		public List getAttachments()
		{
			// Stub routine only
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setRange(org.sakaiproject.service.legacy.time.TimeRange)
		 */
		public void setRange(TimeRange timeRange)
		{
			this.timeRange = timeRange;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setDisplayName(java.lang.String)
		 */
		public void setDisplayName(String displayName)
		{
			this.displayName = displayName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setDescription(java.lang.String)
		 */
		public void setDescription(String description)
		{
			this.description = FormattedText.convertPlaintextToFormattedText(description);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setType(java.lang.String)
		 */
		public void setType(String type)
		{
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setLocation(java.lang.String)
		 */
		public void setLocation(String location)
		{
			this.location = location;
		}

		/**
		* Returns true if current user is the event's owner/creator
		* @return boolean true or false
		*/
		public boolean isUserOwner()
      {
			// Stub routine only
			return true;

      }

		/**
		* Gets the event creator (userid), if any (cover for PROP_CREATOR).
		* @return The event's creator property.
		*/
		public String getCreator()
		{
			// Stub routine only
			return null;

		} // getCreator

		/**
		* Set the event creator (cover for PROP_CREATOR) to current user
		*/
		public void setCreator()
		{
			// Stub routine only

		} // setCreator
		
		public void setCreator(String creator)
		{
			this.creator = creator;
		}

		/**
		* Gets the event modifier (userid), if any (cover for PROP_MODIFIED_BY).
		* @return The event's modified-by property.
		*/
		public String getModifiedBy()
		{
			// Stub routine only
			return null;

		} // getModifiedBy

		/**
		* Set the event modifier (cover for PROP_MODIFIED_BY) to current user
		*/
		public void setModifiedBy()
		{
			// Stub routine only

		} // setModifiedBy

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setField(java.lang.String, java.lang.String)
		 */
		public void setField(String key, String value)
		{
			this.fields.put(key, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setRecurrenceRule(org.sakaiproject.calendar.api.RecurrenceRule)
		 */
		public void setRecurrenceRule(RecurrenceRule recurrenceRule)
		{
			this.recurrenceRule = recurrenceRule;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setExclusionRule(org.sakaiproject.calendar.api.ExclusionRule)
		 */
		public void setExclusionRule(RecurrenceRule exclusionRule)
		{
			this.exclusionRule = exclusionRule;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Edit#isActiveEdit()
		 */
		public boolean isActiveEdit()
		{
			// Stub routine only
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.Edit#getPropertiesEdit()
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			// Stub routine only
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#addAttachment(org.sakaiproject.service.legacy.entity.Reference)
		 */
		public void addAttachment(Reference arg0)
		{
			// Stub routine only
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#removeAttachment(org.sakaiproject.service.legacy.entity.Reference)
		 */
		public void removeAttachment(Reference arg0)
		{
			// Stub routine only
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#replaceAttachments(org.sakaiproject.service.legacy.entity.ReferenceVector)
		 */
		public void replaceAttachments(List arg0)
		{
			// Stub routine only
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#clearAttachments()
		 */
		public void clearAttachments()
		{
			// Stub routine only
		}

		/**
		 * Get the start date formatted for display.
		 */
		public String getDisplayStartDate()
		{
			return this.timeRange.firstTime().toStringLocalDate();
		}

		/**
		 * Get the start time formatted for display.
		 */
		public String getDisplayStartTime()
		{
			return this.timeRange.firstTime().toStringLocalTime();
		}

		/**
		 * Get the end time of the event formatted for display. This handles the fact that events that end at a given time actually end about a minute earlier.
		 */
		public String getDisplayEndTime()
		{
			// We store event time ranges as slightly less than the end time.
			// Make a new time range that is inclusive, just to show the users.

			Time endTime = getTimeService().newTime(this.getRange().lastTime().getTime() + (60 * 1000));

			return endTime.toStringLocalTime();
		}

		/**
		 * Get the line number on which this event occurs.
		 */
		public int getLineNumber()
		{
			return lineNumber;
		}

		/**
		 * Set the line number on which this event occurs.
		 * 
		 * @param i
		 */
		public void setLineNumber(int i)
		{
			lineNumber = i;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDescriptionFormatted(String description)
		{
			this.description = description;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getDescriptionFormatted()
		{
			return description;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getGroups()
		 */
		public Collection getGroups()
		{
			// TODO Auto-generated method stub
			return new Vector();
		}

		public Collection getGroupObjects()
		{
			return new Vector();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getAccess()
		 */
		public EventAccess getAccess()
		{
			return CalendarEvent.EventAccess.SITE;
		}

		public String getGroupRangeForDisplay(Calendar calendar)
		{
			return null;
		}

		public void clearGroupAccess() throws PermissionException
		{
		}

		public void setGroupAccess(Collection groups, boolean own) throws PermissionException
		{
		}

	}

	/**
	 * Constructor to set up a few of the formatters.
	 */
	public GenericCalendarImporter()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.service.legacy.calendar#doImport(java.lang.String, java.io.InputStream, java.util.Map, java.lang.String[])
	 */
	public List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames)
			throws ImportException
	{
		return doImport(importType, importStream, columnMapping, customFieldPropertyNames, null);
	}
	

	public List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames, String userTzid)
			throws ImportException
	{
		final List rowList = new ArrayList();
		final Reader scheduleImport;

		try
		{
			scheduleImport = (Reader) ((Class) readerMap.get(importType)).newInstance();

			// Set the timeservice in the reader.
			scheduleImport.setTimeService(getTimeService());
		}

		catch (InstantiationException e1)
		{
			String msg = (String)rb.getFormattedMessage("err_import", 
                                                      new Object[]{importType});
			throw new ImportException( msg );
		}
		catch (IllegalAccessException e1)
		{
			String msg = (String)rb.getFormattedMessage("err_import", 
                                                      new Object[]{importType});
			throw new ImportException( msg );
		}

		if (scheduleImport == null)
		{
			throw new ImportException(rb.getString("err_import_unknown"));
		}

		// If no column mapping has been specified, use the default.
		if (columnMapping != null)
		{
			scheduleImport.setColumnHeaderToAtributeMapping(columnMapping);
		}
		
		columnMap = scheduleImport.getDefaultColumnMap();
		
		// Read in the file.
		String calendarTzid = scheduleImport.importStreamFromDelimitedFile(importStream, new Reader.ReaderImportRowHandler()
		{
			String frequencyColumn = columnMap.get(FREQUENCY_DEFAULT_COLUMN_HEADER);
			String startTimeColumn = columnMap.get(START_TIME_DEFAULT_COLUMN_HEADER);
			String endTimeColumn = columnMap.get(END_TIME_DEFAULT_COLUMN_HEADER);
			String durationTimeColumn = columnMap.get(DURATION_DEFAULT_COLUMN_HEADER);
			String dateColumn = columnMap.get(DATE_DEFAULT_COLUMN_HEADER);
			String endsColumn = columnMap.get(ENDS_DEFAULT_COLUMN_HEADER);
			String intervalColumn = columnMap.get(INTERVAL_DEFAULT_COLUMN_HEADER);
			String repeatColumn = columnMap.get(REPEAT_DEFAULT_COLUMN_HEADER);
			
			// This is the callback that is called for each row.
			public void handleRow(Iterator columnIterator) throws ImportException
			{
				final Map eventProperties = new HashMap();

				// Add all the properties to the map
				while (columnIterator.hasNext())
				{
					Reader.ReaderImportCell column = (Reader.ReaderImportCell) columnIterator.next();

					String value = column.getCellValue().trim();
					Object mapCellValue = null;

					// First handle any empy columns.
					if (value.length() == 0)
					{
						mapCellValue = null;
					}
					else
					{
						if (frequencyColumn != null && frequencyColumn.equals(column.getColumnHeader()))
						{
							mapCellValue = column.getCellValue();
						}
						else if (endTimeColumn != null && endTimeColumn.equals(column.getColumnHeader())
								|| (startTimeColumn != null && startTimeColumn.equals(column.getColumnHeader())))
						{
							boolean success = false;

							try
							{
								mapCellValue = timeFormatter().parse(value);
								success = true;
							}

							catch (ParseException e)
							{
								// Try another format
							}

							if (!success)
							{
								try
								{
									mapCellValue = timeFormatterWithSeconds().parse(value);
									success = true;
								}

								catch (ParseException e)
								{
									// Try another format
								}
							}

							if (!success)
							{
								try
								{
									mapCellValue = time24HourFormatter().parse(value);
									success = true;
								}

								catch (ParseException e)
								{
									// Try another format
								}
							}

							if (!success)
							{
								try
								{
									mapCellValue = time24HourFormatterWithSeconds().parse(value);
									success = true;
								}

								catch (ParseException e)
								{
									// Give up, we've run out of possible formats.
                           String msg = (String)rb.getFormattedMessage(
                                                   "err_time", 
                                                   new Object[]{Integer.valueOf(column.getLineNumber()),
                                                                column.getColumnHeader()});
                           throw new ImportException( msg );
								}
							}
						}
						else if (durationTimeColumn != null && durationTimeColumn.equals(column.getColumnHeader()))
						{
                     String timeFormatErrorString = (String)rb.getFormattedMessage(
                                                   "err_time", 
                                                   new Object[]{Integer.valueOf(column.getLineNumber()),
                                                                column.getColumnHeader()});

							String parts[] = value.split(":");

							if (parts.length == 1)
							{
								// Convert to minutes to get into one property field.
								try
								{
									mapCellValue = Integer.valueOf(Integer.parseInt(parts[0]));
								}
								catch (NumberFormatException ex)
								{
									throw new ImportException(timeFormatErrorString);
								}
							}
							else if (parts.length == 2)
							{
								// Convert to hours:minutes to get into one property field.
								try
								{
									mapCellValue = Integer.valueOf(Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]));
								}
								catch (NumberFormatException ex)
								{
									throw new ImportException(timeFormatErrorString);
								}
							}
							else
							{
								// Not a legal format of mm or hh:mm
								throw new ImportException(timeFormatErrorString);
							}
						}
						else if (dateColumn != null && dateColumn.equals(column.getColumnHeader())
								|| (endsColumn != null && endsColumn.equals(column.getColumnHeader())))
						{
                     DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT, rb.getLocale() );
                     df.setLenient(false);
							try
							{
								mapCellValue = df.parse(value);
							}
							catch (ParseException e)
							{
                        String msg = (String)rb.getFormattedMessage("err_date", 
                                                                    new Object[]{Integer.valueOf(column.getLineNumber()),
                                                                                 column.getColumnHeader()});
                        throw new ImportException( msg );
							}
						}
						else if (intervalColumn != null && intervalColumn.equals(column.getColumnHeader())
								|| repeatColumn != null && repeatColumn.equals(column.getColumnHeader()))
						{
							try
							{
								mapCellValue = Integer.valueOf(column.getCellValue());
							}
							catch (NumberFormatException ex)
							{
                        String msg = (String)rb.getFormattedMessage("err_interval", 
                                                                    new Object[]{Integer.valueOf(column.getLineNumber()),
                                                                                 column.getColumnHeader()});
                        throw new ImportException( msg );
							}
						}
						else if (ITEM_TYPE_PROPERTY_NAME.equals(column.getColumnHeader())){
							String cellValue = column.getCellValue();
							if (cellValue!=null){
								CalendarEventType.getEventTypeFromImportType(cellValue);
							}
							else { 
								mapCellValue = cellValue; 
							}
						}
						else
						{
							// Just a string...
							mapCellValue = column.getCellValue();
						}
					}

					// Store in the map for later reference.
					eventProperties.put(column.getColumnHeader(), mapCellValue);
				}

				// Add the map of properties for this row to the list of rows.
				rowList.add(eventProperties);
			}
		});

		// Calendar time zone remains over user time zone
		String tzid = calendarTzid==null ? userTzid:calendarTzid;
		
		return getPrototypeEvents(scheduleImport.filterEvents(rowList, customFieldPropertyNames, tzid), customFieldPropertyNames);
	}

	/**
	 * Interprets the list of maps created by doImport()
	 */
	protected List getPrototypeEvents(List rowList, String[] customFieldPropertyNames) throws ImportException
	{
		Iterator it = rowList.iterator();
		List eventList = new ArrayList();
		int lineNumber = 1;

		while (it.hasNext())
		{
			Map eventProperties = (Map) it.next();
			RecurrenceRule recurrenceRule = null;
			PrototypeEvent prototypeEvent = new PrototypeEvent();

			prototypeEvent.setDescription((String) eventProperties.get(columnMap.get(DESCRIPTION_DEFAULT_COLUMN_HEADER)));
			prototypeEvent.setDisplayName((String) eventProperties.get(columnMap.get(TITLE_DEFAULT_COLUMN_HEADER)));
			prototypeEvent.setLocation((String) eventProperties.get(columnMap.get(LOCATION_DEFAULT_COLUMN_HEADER)));
			prototypeEvent.setType((String) eventProperties.get(ITEM_TYPE_PROPERTY_NAME));

			if (prototypeEvent.getType() == null || prototypeEvent.getType().length() == 0)
			{
				prototypeEvent.setType("Activity");
			}

			// The time range has been calculated in the reader, based on
			// whatever time fields are available in the particular import format.
			// This range has been placed in the ACTUAL_TIMERANGE property.

			TimeRange timeRange = (TimeRange) eventProperties.get(GenericCalendarImporter.ACTUAL_TIMERANGE);

			if (timeRange == null)
			{
            String msg = (String)rb.getFormattedMessage("err_notime", 
                                                        new Object[]{Integer.valueOf(lineNumber)});
            throw new ImportException( msg );
			}

			// The start/end times were calculated during the import process.
			prototypeEvent.setRange(timeRange);

			// Do custom fields, if any.
			if (customFieldPropertyNames != null)
			{
				for (int i = 0; i < customFieldPropertyNames.length; i++)
				{
					prototypeEvent.setField(customFieldPropertyNames[i], (String) eventProperties.get(customFieldPropertyNames[i]));
				}
			}

			// See if this is a recurring event
			String frequencyString = (String) eventProperties.get(columnMap.get(FREQUENCY_DEFAULT_COLUMN_HEADER));

			if (frequencyString != null)
			{
				Integer interval = (Integer) eventProperties.get(columnMap.get(INTERVAL_DEFAULT_COLUMN_HEADER));
				Integer count = (Integer) eventProperties.get(columnMap.get(REPEAT_DEFAULT_COLUMN_HEADER));
				Date until = (Date) eventProperties.get(columnMap.get(ENDS_DEFAULT_COLUMN_HEADER));

				if (count != null && until != null)
				{
               String msg = (String)rb.getFormattedMessage("err_datebad", 
                                                           new Object[]{Integer.valueOf(lineNumber)});
               throw new ImportException( msg );
				}

				if (interval == null && count == null && until == null)
				{
					recurrenceRule = getCalendarService().newRecurrence(frequencyString);
				}
				else if (until == null && interval != null && count != null)
				{
					recurrenceRule = getCalendarService().newRecurrence(frequencyString, interval.intValue(), count.intValue());
				}
				else if (until == null && interval != null && count == null)
				{
					recurrenceRule = getCalendarService().newRecurrence(frequencyString, interval.intValue());
				}
				else if (until != null && interval != null && count == null)
				{
					Time untilTime = getTimeService().newTime(until.getTime());

					recurrenceRule = getCalendarService().newRecurrence(frequencyString, interval.intValue(), untilTime);
				}

				// See if we were able to successfully create a recurrence rule.
				if (recurrenceRule == null)
				{
               String msg = (String)rb.getFormattedMessage("err_freqbad", 
                                                           new Object[]{Integer.valueOf(lineNumber)});
               throw new ImportException( msg );
				}

				prototypeEvent.setRecurrenceRule(recurrenceRule);
			}
			prototypeEvent.setLineNumber(lineNumber);
			eventList.add(prototypeEvent);
			lineNumber++;
		}

		return eventList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.tool.calendar.schedimport.importers.Importer#getDefaultColumnMap(java.lang.String)
	 */
	public Map<String, String> getDefaultColumnMap(String importType) throws ImportException
	{
		try
		{
			Reader scheduleImport = readerMap.get(importType).newInstance();

			if (scheduleImport != null)
			{
				return scheduleImport.getDefaultColumnMap();
			}
		}

		catch (InstantiationException | IllegalAccessException e1)
		{
			String msg = rb.getFormattedMessage("err_import", importType);
			throw new ImportException( msg );
		}

		// No map exists if we get here.
		return null;
	}

	/**
	 * Getter for injected service
	 */
	public CalendarService getCalendarService()
	{
		return calendarService;
	}

	/**
	 * Getter for injected service
	 */
	public TimeService getTimeService()
	{
		return timeService;
	}

	/**
	 * Setter for injected service
	 * 
	 * @param service
	 */
	public void setCalendarService(CalendarService service)
	{
		calendarService = service;
	}

	/**
	 * Setter for injected service
	 * 
	 * @param service
	 */
	public void setTimeService(TimeService service)
	{
		timeService = service;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// Add our readers. This might be done from a
			// config file in future versions.
			readerMap.put(OUTLOOK_IMPORT, OutlookReader.class);
			readerMap.put(MEETINGMAKER_IMPORT, MeetingMakerReader.class);
			readerMap.put(CSV_IMPORT, CSVReader.class);
			readerMap.put(ICALENDAR_IMPORT, IcalendarReader.class);
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}
}
