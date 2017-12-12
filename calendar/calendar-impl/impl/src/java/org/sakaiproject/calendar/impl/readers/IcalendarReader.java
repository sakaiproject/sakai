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

import java.io.InputStream;
import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class parses an import file from iCalendar.
 */
@Slf4j
public class IcalendarReader extends Reader
{
	private ResourceLoader rb = new ResourceLoader("calendar");
	private Map<String, String> defaultHeaderMap = getDefaultColumnMap();
	
	private static final String TITLE_PROPERTY_NAME = "Summary";
	private static final String CONTACT_SECTION_HEADER = "Contacts";
	private static final String TODO_SECTION_HEADER = "Todos";
	private static final String EVENT_SECTION_HEADER = "Events";

	/**
	 * Default constructor 
	 */
	public IcalendarReader()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.ImportReader#importStreamFromDelimitedFile(java.io.InputStream, org.sakaiproject.tool.calendar.ImportReader.ReaderImportRowHandler)
	 */
	public void importStreamFromDelimitedFile(
		InputStream stream,
		ReaderImportRowHandler handler)
		throws ImportException//, IOException, ParserException
	{
	
		try {

			ColumnHeader columnDescriptionArray[] = null;
			String descriptionColumns[] = {"Summary","Description","Start Date","Start Time","Duration","Location"};

			int lineNumber = 1;
			String durationformat ="";
			String requireValues = "";
			
			// column map stuff
			trimLeadingTrailingQuotes(descriptionColumns);
			columnDescriptionArray = buildColumnDescriptionArray(descriptionColumns);

			// enable "relaxed parsing"; read file using LF instead of CRLF
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true); 
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true); 
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true); 
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true); 

			CalendarBuilder builder = new CalendarBuilder();
			net.fortuna.ical4j.model.Calendar calendar = builder.build(stream);
		
			for (Iterator i = calendar.getComponents("VEVENT").iterator(); i.hasNext();)
			{
				Component component = (Component) i.next();

	
				if ( component.getProperty("SUMMARY") == null )
				{
					log.warn("IcalendarReader: SUMMARY is required; event not imported");
					continue;
				}
				DateTime from = new DateTime(Date.from(ZonedDateTime.now().minusMonths(6).toInstant()));
				DateTime to = new DateTime(Date.from(ZonedDateTime.now().plusMonths(12).toInstant()));
				Period range = new Period(from, to);

				PeriodList list = component.calculateRecurrenceSet(range);
				for (Iterator j = list.iterator(); j.hasNext();) 
				{
					Period period = (Period) j.next();
					Dur duration = period.getDuration();
					int durationminutes = duration.getMinutes();
					int durationhours = duration.getHours();
					//todo investiage ical4j's handling of 'days'

					if (durationminutes < 10)
					{
					durationformat = "0"+durationminutes;
					}
					else
					{
					durationformat = ""+durationminutes;
					}

					if (durationhours != 0)
					{
						durationformat = durationhours+":"+durationformat;
					}
					String description = "";
					if ( component.getProperty("DESCRIPTION") != null) {
						description = component.getProperty("DESCRIPTION").getValue();
					}
					String location = "";
					if (component.getProperty("LOCATION") != null) {
						location = component.getProperty("LOCATION").getValue();
					}
					String columns[]	= 
							{component.getProperty("SUMMARY").getValue(),
							 description,
							 DateFormat.getDateInstance(DateFormat.SHORT, rb.getLocale()).format(period.getStart()),
							 DateFormat.getTimeInstance(DateFormat.SHORT, rb.getLocale()).format(period.getStart()),
							 durationformat,
							 location};

					// Remove trailing/leading quotes from all columns.
					//trimLeadingTrailingQuotes(columns);

					handler.handleRow(
						processLine(
							columnDescriptionArray,
							lineNumber,
							columns));

					lineNumber++;
				}
			} // end for
		
		}
		catch (Exception e)
		{
			log.warn(".importSteamFromDelimitedFile(): ", e);
		}
	} // end importStreamFromDelimitedFile

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#filterEvents(java.util.List, java.lang.String[])
	 */
	public List filterEvents(List events, String[] customFieldNames) throws ImportException
	{
		Iterator it = events.iterator();
		int lineNumber = 1;
		
		//
		// Convert the date/time fields as they appear in the Outlook import to
		// be a synthesized start/end timerange.
		//
		while ( it.hasNext() )
		{
			Map eventProperties = (Map)it.next();

			Date startTime = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER));
			TimeBreakdown startTimeBreakdown = null;
			
			if ( startTime != null )
			{
				// if the source time zone were known, this would be
				// a good place to set it: startCal.setTimeZone()
				GregorianCalendar startCal = new GregorianCalendar();
				startCal.setTimeInMillis( startTime.getTime() );
				startTimeBreakdown = 
						  getTimeService().newTimeBreakdown( 0, 0, 0, 
							  startCal.get(Calendar.HOUR_OF_DAY),
							  startCal.get(Calendar.MINUTE),
							  startCal.get(Calendar.SECOND),
								0 );
			}
			else
			{
				Integer line = Integer.valueOf(lineNumber);
				String msg = (String)rb.getFormattedMessage("err_no_stime_on", 
																		  new Object[]{line});
				throw new ImportException( msg );
			}
			
			Integer durationInMinutes = (Integer)eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER));

			if ( durationInMinutes == null )
			{
				Integer line = Integer.valueOf(lineNumber);
				String msg = (String)rb.getFormattedMessage("err_no_dtime_on", 
																		  new Object[]{line});
				throw new ImportException( msg );
			}
			
			Date endTime =
				new Date(
					startTime.getTime() + (durationInMinutes.longValue() * 60 * 1000) );
					
			TimeBreakdown endTimeBreakdown = null;

			if ( endTime != null )
			{
				// if the source time zone were known, this would be
				// a good place to set it: endCal.setTimeZone()
				GregorianCalendar endCal = new GregorianCalendar();
				endCal.setTimeInMillis( endTime.getTime() );
				endTimeBreakdown = 
						  getTimeService().newTimeBreakdown( 0, 0, 0, 
							  endCal.get(Calendar.HOUR_OF_DAY),
							  endCal.get(Calendar.MINUTE),
							  endCal.get(Calendar.SECOND),
							  0 );
			}

			Date startDate = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER));
			
			// if the source time zone were known, this would be
			// a good place to set it: startCal.setTimeZone()
			GregorianCalendar startCal = new GregorianCalendar();
			if ( startDate != null ) {
				startCal.setTimeInMillis( startDate.getTime() );
			}
			startTimeBreakdown.setYear( startCal.get(Calendar.YEAR) );
			startTimeBreakdown.setMonth( startCal.get(Calendar.MONTH)+1 );
			startTimeBreakdown.setDay( startCal.get(Calendar.DAY_OF_MONTH) );
				
			endTimeBreakdown.setYear( startCal.get(Calendar.YEAR) );
			endTimeBreakdown.setMonth( startCal.get(Calendar.MONTH)+1 );
			endTimeBreakdown.setDay( startCal.get(Calendar.DAY_OF_MONTH) );
			
			eventProperties.put(
				GenericCalendarImporter.ACTUAL_TIMERANGE,
				getTimeService().newTimeRange(
						  getTimeService().newTimeLocal(startTimeBreakdown),
						  getTimeService().newTimeLocal(endTimeBreakdown),
					true,
					false));
					
			lineNumber++;
		}
		
		return events;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#getDefaultColumnMap()
	 */
	public Map<String, String> getDefaultColumnMap()
	{
		Map<String, String> columnHeaderMap = new HashMap<String, String>();

		columnHeaderMap.put(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER, TITLE_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DESCRIPTION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DATE_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.START_TIME_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DURATION_PROPERTY_NAME);
		//columnHeaderMap.put(ITEM_HEADER, GenericCalendarImporter.ITEM_TYPE_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.LOCATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.LOCATION_PROPERTY_NAME);
		
				
		return columnHeaderMap;
	}
}
