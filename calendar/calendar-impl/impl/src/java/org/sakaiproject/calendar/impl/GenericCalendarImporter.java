/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.calendar.impl;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.sakaiproject.calendar.impl.readers.CSVReader;
import org.sakaiproject.calendar.impl.readers.MeetingMakerReader;
import org.sakaiproject.calendar.impl.readers.OutlookReader;
import org.sakaiproject.calendar.impl.readers.Reader;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarImporterService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.entity.ResourcePropertiesEdit;
import org.sakaiproject.service.legacy.time.Time;
import org.sakaiproject.service.legacy.time.TimeRange;
import org.sakaiproject.service.legacy.time.TimeService;
import org.sakaiproject.util.text.FormattedText;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class provides common importing functionality after a lower-level
 * reader has taken care of the peculiarities of a given import format.
 */
public class GenericCalendarImporter implements CalendarImporterService
{
	public static final String LOCATION_PROPERTY_NAME = "Location";
	public static final String LOCATION_DEFAULT_COLUMN_HEADER = "Location";

	public static final String ITEM_TYPE_PROPERTY_NAME = "ItemType";
	public static final String ITEM_TYPE_DEFAULT_COLUMN_HEADER = "Type";

	public static final String FREQUENCY_PROPERTY_NAME = "Frequency";
	public static final String FREQUENCY_DEFAULT_COLUMN_HEADER = "Frequency";

	public static final String END_TIME_PROPERTY_NAME = "EndTime";
	public static final String END_TIME_DEFAULT_COLUMN_HEADER = "Ends";

	public static final String DURATION_PROPERTY_NAME = "Duration";
	public static final String DURATION_DEFAULT_COLUMN_HEADER = "Duration";

	public static final String START_TIME_PROPERTY_NAME = "StartTime";
	public static final String START_TIME_DEFAULT_COLUMN_HEADER = "Start";

	public static final String DATE_PROPERTY_NAME = "Date";
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
	
	// Map of readers for various formats.  Keyed by import type.
	private final Map readerMap = new HashMap();
		
	public static final DateFormat TIME_FORMATTER = new SimpleDateFormat("hh:mm a");
	public static final DateFormat TIME_FORMATTER_WITH_SECONDS = new SimpleDateFormat("hh:mm:ss a");
	static final DateFormat time24HourFormatter = new SimpleDateFormat("HH:mm");
	static final DateFormat time24HourFormatterWithSeconds = new SimpleDateFormat("HH:mm:ss");
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yy");
	
	// These are injected at runtime by Spring.
	private CalendarService calendarService = null;
	private TimeService timeService = null;
	private Logger logger = null;
	
	/*
	 * This class is used as a "prototype" event that may be added to
	 * a real calendar.  We emulate enough of a calendar event to hold
	 * all the information necessary to create a real event.
	 */
	public class PrototypeEvent implements CalendarEventEdit
	{
		private RecurrenceRule recurrenceRule;
		private Map fields;
		private String location;
		private String type;
		private String description;
		private String displayName;
		private TimeRange timeRange;
		private int lineNumber;
		
		/**
		 * Default constructor
		 */
		public PrototypeEvent()
		{
			fields = new HashMap();
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getRange()
		 */
		public TimeRange getRange()
		{
			return this.timeRange;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getDisplayName()
		 */
		public String getDisplayName()
		{
			return this.displayName;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getDescription()
		 */
		public String getDescription()
		{
			return FormattedText.convertFormattedTextToPlaintext(description);
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getType()
		 */
		public String getType()
		{
			return this.type;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getLocation()
		 */
		public String getLocation()
		{
			return this.location;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getField(java.lang.String)
		 */
		public String getField(String fieldName)
		{
			return (String) this.fields.get(fieldName);
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getCalendarReference()
		 */
		public String getCalendarReference()
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEvent#getRecurrenceRule()
		 */
		public RecurrenceRule getRecurrenceRule()
		{
			// Stub routine only
			return this.recurrenceRule;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.Resource#getUrl()
		 */
		public String getUrl()
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
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

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.Resource#getId()
		 */
		public String getId()
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.Resource#getProperties()
		 */
		public ResourceProperties getProperties()
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.Resource#toXml(org.w3c.dom.Document, java.util.Stack)
		 */
		public Element toXml(Document arg0, Stack arg1)
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o)
		{
			// Stub routine only
			return 0;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainer#getAttachments()
		 */
		public List getAttachments()
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setRange(org.sakaiproject.service.legacy.time.TimeRange)
		 */
		public void setRange(TimeRange timeRange)
		{
			this.timeRange = timeRange;			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setDisplayName(java.lang.String)
		 */
		public void setDisplayName(String displayName)
		{
			this.displayName = displayName;			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setDescription(java.lang.String)
		 */
		public void setDescription(String description)
		{
			this.description = FormattedText.convertPlaintextToFormattedText(description);			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setType(java.lang.String)
		 */
		public void setType(String type)
		{
			this.type = type;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setLocation(java.lang.String)
		 */
		public void setLocation(String location)
		{
			this.location = location;			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setField(java.lang.String, java.lang.String)
		 */
		public void setField(String key, String value)
		{
			this.fields.put(key, value);			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.calendar.api.CalendarEventEdit#setRecurrenceRule(org.sakaiproject.calendar.api.RecurrenceRule)
		 */
		public void setRecurrenceRule(RecurrenceRule recurrenceRule)
		{
			this.recurrenceRule = recurrenceRule;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.Edit#isActiveEdit()
		 */
		public boolean isActiveEdit()
		{
			// Stub routine only
			return false;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.Edit#getPropertiesEdit()
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			// Stub routine only
			return null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#addAttachment(org.sakaiproject.service.legacy.entity.Reference)
		 */
		public void addAttachment(Reference arg0)
		{
			// Stub routine only
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#removeAttachment(org.sakaiproject.service.legacy.entity.Reference)
		 */
		public void removeAttachment(Reference arg0)
		{
			// Stub routine only
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.service.legacy.entity.AttachmentContainerEdit#replaceAttachments(org.sakaiproject.service.legacy.entity.ReferenceVector)
		 */
		public void replaceAttachments(List arg0)
		{
			// Stub routine only
		}

		/* (non-Javadoc)
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
		 * Get the end time of the event formatted for display.  This
		 * handles the fact that events that end at a given time actually end
		 * about a minute earlier.
		 */
		public String getDisplayEndTime()
		{
			// We store event time ranges as slightly less than the end time.
			// Make a new time range that is inclusive, just to show the users.
			
			Time endTime =
				getTimeService().newTime(
					this.getRange().lastTime().getTime() + (60 * 1000));
			
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

	}

	/**
	 * Constructor to set up a few of the formatters.
	 */
	public GenericCalendarImporter()
	{
		super();
		DATE_FORMATTER.setLenient(false);
		TIME_FORMATTER.setLenient(false);
		time24HourFormatter.setLenient(false);
		time24HourFormatterWithSeconds.setLenient(false);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.service.legacy.calendar#doImport(java.lang.String, java.io.InputStream, java.util.Map, java.lang.String[])
	 */
	public List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames)
		throws ImportException
	{
		final List rowList = new ArrayList();		
		final Reader scheduleImport;
		
		try
		{
			scheduleImport =
				(Reader) ((Class) readerMap.get(importType)).newInstance();
				
			// Set the timeservice in the reader.
			scheduleImport.setTimeService(getTimeService());
		}
		
		catch (InstantiationException e1)
		{
			throw new ImportException("Unable to create importer for " + importType);
		}
		catch (IllegalAccessException e1)
		{
			throw new ImportException("Unable to create importer for " + importType);
		}
		
		if ( scheduleImport == null )
		{	
			throw new ImportException("Unknown import type");
		}
		
		// If no column mapping has been specified, use the default.
		if ( columnMapping != null )
		{
			scheduleImport.setColumnHeaderToAtributeMapping(columnMapping);
		}

		// Read in the file.
		scheduleImport
			.importStreamFromDelimitedFile(
				importStream,
				new Reader.ReaderImportRowHandler()
		{
			// This is the callback that is called for each row.
			public void handleRow(Iterator columnIterator)
				throws ImportException
			{
				final Map eventProperties = new HashMap();

				// Add all the properties to the map
				while (columnIterator.hasNext())
				{
					Reader.ReaderImportCell column =
						(Reader.ReaderImportCell) columnIterator.next();

					String value = column.getCellValue().trim();
					Object mapCellValue = null;
					
					// First handle any empy columns.
					if ( value.length() == 0 )
					{
						mapCellValue = null;
					}
					else
					{
						if (FREQUENCY_PROPERTY_NAME
							.equals(column.getPropertyName()))
						{
							mapCellValue = column.getCellValue();
						}
						else
						if (END_TIME_PROPERTY_NAME
							.equals(column.getPropertyName())
							|| START_TIME_PROPERTY_NAME.equals(
								column.getPropertyName()))
						{
							boolean success = false;
							
							try
							{
								mapCellValue = TIME_FORMATTER.parse(value);
								success = true;
							}
							
							catch (ParseException e)
							{
								// Try another format
							}

							if ( !success )
							{
								try
								{
									mapCellValue = TIME_FORMATTER_WITH_SECONDS.parse(value);
									success = true;
								}
							
								catch (ParseException e)
								{
									// Try another format
								}
							}

							
							if ( !success )
							{
								try
								{
									mapCellValue = time24HourFormatter.parse(value);
									success = true;
								}
							
								catch (ParseException e)
								{
									// Try another format
								}
							}

							if ( !success )
							{
								try
								{
									mapCellValue = time24HourFormatterWithSeconds.parse(value);
									success = true;
								}
								
								catch (ParseException e)
								{
									// Give up, we've run out of possible formats.
									throw new ImportException(
										"Illegal time format on row: "
											+ column.getLineNumber()
											+ ", column: "
											+ column.getColumnHeader() + ". Please make the appropriate changes to your template and save it again.");
								}
							}
						}
						else
						if (DURATION_PROPERTY_NAME
							.equals(column.getPropertyName()))
						{
						    String timeFormatErrorString = "Illegal time format on row: "
								+ column.getLineNumber()
								+ ", column: "
								+ column.getColumnHeader() + ". Please make the appropriate changes to your template and save it again.";
						    
						    String parts[] = value.split(":");
	
							if ( parts.length == 1)
							{
								// Convert to minutes to get into one property field.
								try
								{
									mapCellValue = new Integer(Integer.parseInt(parts[0]));
								}
								catch (NumberFormatException ex)
								{
									throw new ImportException(timeFormatErrorString);
								}							    
							}
							else if ( parts.length == 2)
							{
								// Convert to hours:minutes to get into one property field.
								try
								{
									mapCellValue = new Integer(Integer.parseInt(parts[0]) * 60
											+ Integer.parseInt(parts[1]));
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
						else
						if (DATE_PROPERTY_NAME.equals(column.getPropertyName())
							|| ENDS_PROPERTY_NAME.equals(column.getPropertyName()))
						{
							try
							{
								mapCellValue = DATE_FORMATTER.parse(value);
							}
							catch (ParseException e)
							{
								throw new ImportException(
									"Illegal date format on row: "
										+ column.getLineNumber()
										+ ", column: "
										+ column.getColumnHeader() + ". Please make the appropriate changes to your template and save it again.");
							}
						}
						else
						if (INTERVAL_PROPERTY_NAME.equals(column.getPropertyName())
							|| REPEAT_PROPERTY_NAME.equals(
								column.getPropertyName()))
						{
							try
							{
								mapCellValue = new Integer(column.getCellValue());
							}
							catch (NumberFormatException ex)
							{
								throw new ImportException(
									"Illegal interval format on row: "
										+ column.getLineNumber()
										+ ", column: "
										+ column.getColumnHeader() + ". Please make the appropriate changes to your template and save it again.");
							}
						}
						else
						{
							// Just a string...
							mapCellValue = column.getCellValue();
						}
					}

					// Store in the map for later reference.
					eventProperties.put(column.getPropertyName(), mapCellValue);
				}
				
				// Add the map of properties for this row to the list of rows.
				rowList.add(eventProperties);
			}
		});

		return getPrototypeEvents(
			scheduleImport.filterEvents(rowList, customFieldPropertyNames),
			customFieldPropertyNames);
	}
	
	/**
	 * Interprets the list of maps created by doImport()
	 * @param map
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
			
			prototypeEvent.setDescription((String) eventProperties.get(GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME));
			prototypeEvent.setDisplayName((String) eventProperties.get(GenericCalendarImporter.TITLE_PROPERTY_NAME));
			prototypeEvent.setLocation((String) eventProperties.get(GenericCalendarImporter.LOCATION_PROPERTY_NAME));
			prototypeEvent.setType((String) eventProperties.get(GenericCalendarImporter.ITEM_TYPE_PROPERTY_NAME));
			
			if ( prototypeEvent.getType() == null || prototypeEvent.getType().length() == 0)
			{
				prototypeEvent.setType("Activity");
			}
			
			// The time range has been calculated in the reader, based on
			// whatever time fields are available in the particular import format.
			// This range has been placed in the ACTUAL_TIMERANGE property.
			
			TimeRange timeRange = (TimeRange) eventProperties.get(GenericCalendarImporter.ACTUAL_TIMERANGE);
			
			if ( timeRange == null )
			{
				throw new ImportException("A start, end time or the duration was not specified on line #" + lineNumber + ". Please make the appropriate changes to your template and save it again.");
			}
			
			// The start/end times were calculated during the import process.
			prototypeEvent.setRange(timeRange);
			
			// Do custom fields, if any.
			if ( customFieldPropertyNames != null )
			{
				for ( int i=0; i < customFieldPropertyNames.length; i++)
				{
					prototypeEvent.setField(customFieldPropertyNames[i], (String) eventProperties.get(customFieldPropertyNames[i])); 
				}
			}
			
			// See if this is a recurring event
			String frequencyString = (String)eventProperties.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME);
			
			if ( frequencyString != null )
			{
				Integer interval = (Integer)eventProperties.get(GenericCalendarImporter.INTERVAL_PROPERTY_NAME);
				Integer count = (Integer)eventProperties.get(GenericCalendarImporter.REPEAT_PROPERTY_NAME);
				Date until = (Date) eventProperties.get(GenericCalendarImporter.ENDS_PROPERTY_NAME);
				
				if ( count != null && until != null )
				{
					throw new ImportException("Both a count and end date cannot be specified at the same time, error on line #" + lineNumber + ". Please make the appropriate changes to your template and save it again.");
				}

				if ( interval == null && count == null && until == null )
				{
					recurrenceRule = getCalendarService().newRecurrence(frequencyString);
				}
				else
				if ( until == null && interval != null && count != null )
				{
					recurrenceRule = getCalendarService().newRecurrence(frequencyString, interval.intValue(), count.intValue());
				}
				else
				if ( until == null && interval != null && count == null )
				{
					recurrenceRule = getCalendarService().newRecurrence(frequencyString, interval.intValue());
				}
				else
				if ( until != null && interval != null && count == null )
				{
					Time untilTime = getTimeService().newTime(until.getTime());
					
					recurrenceRule = getCalendarService().newRecurrence(frequencyString, interval.intValue(), untilTime);
				}
				
				// See if we were able to successfully create a recurrence rule.
				if ( recurrenceRule == null )
				{
					throw new ImportException("A frequency was specified, but a recurrence rule could not be created due to missing data on line #" + lineNumber + ". Please make the appropriate changes to your template and save it again.");
				} 
				
				prototypeEvent.setRecurrenceRule(recurrenceRule);
			}
			prototypeEvent.setLineNumber(lineNumber);
			eventList.add(prototypeEvent);
			lineNumber++;
		}
		
		return eventList; 
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimport.importers.Importer#getDefaultColumnMap(java.lang.String)
	 */
	public Map getDefaultColumnMap(String importType) throws ImportException
	{
		try
		{
			Reader scheduleImport =
				(Reader) ((Class) readerMap.get(importType)).newInstance();
				
			if ( scheduleImport != null )
			{
				return scheduleImport.getDefaultColumnMap();
			}
		}
		
		catch (InstantiationException e1)
		{
			throw new ImportException("Unable to create importer for " + importType);
		}
		catch (IllegalAccessException e1)
		{
			throw new ImportException("Unable to create importer for " + importType);
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
	 * @param service
	 */
	public void setCalendarService(CalendarService service)
	{
		calendarService = service;
	}

	/**
	 * Setter for injected service
	 * @param service
	 */
	public void setTimeService(TimeService service)
	{
		timeService = service;
	}

	/**
	 * Getter for injected service
	 */
	public Logger getLogger()
	{
		return logger;
	}

	/**
	 * Setter for injected service
	 * @param logger
	 */
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}


	/*******************************************************************************
	* Init and Destroy
	*******************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// Add our readers.  This might be done from a
			// config file in future versions.
			readerMap.put(OUTLOOK_IMPORT, OutlookReader.class);
			readerMap.put(MEETINGMAKER_IMPORT, MeetingMakerReader.class);
			readerMap.put(CSV_IMPORT, CSVReader.class);
		}
		catch (Throwable t)
		{
			if ( getLogger() != null )
			{
				getLogger().warn(this +".init(): ", t);
			} 
		}
	}

	/**
	* Returns to uninitialized state.
	*/
	public void destroy()
	{
		if ( logger != null )
		{
			getLogger().info(this +".destroy()");
		} 
	}
}
