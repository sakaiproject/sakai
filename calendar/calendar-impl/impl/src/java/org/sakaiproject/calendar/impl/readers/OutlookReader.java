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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.impl.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class parses a comma (or other separator other than a double-quote) delimited
 * file.
 */
public class OutlookReader extends CSVReader
{
   private ResourceLoader rb = new ResourceLoader("calendar");
   
	//
	// Commented out lines are present in the import file, but we are
	// currently ignoring them.  They are here for reference/future use.
	//
	public final String SUBJECT_HEADER = "Subject";	
	public final String START_DATE_HEADER = "Start Date";	
	public final String START_TIME_HEADER = "Start Time";	
	public final String END_DATE_HEADER = "End Date";	
	public final String END_TIME_HEADER = "End Time";	
	public final String ALL_DAY_EVENT_HEADER = "All day event";	// FALSE/TRUE
	public final String DESCRIPTION_HEADER = "Description";	
	public final String LOCATION_HEADER = "Location";	

	/**
	 * Default constructor
	 */
	public OutlookReader()
	{
		super();
	}

	/**
	 * Import a CSV file from a stream and callback on each row.
	 * @param stream Stream of CSV (or other delimited data)
	 * @param handler Callback for each row.
	 */
	public void importStreamFromDelimitedFile(
		InputStream stream,
		ReaderImportRowHandler handler) throws ImportException
	{
		BufferedReader bufferedReader = getReader(stream);

		ColumnHeader columnDescriptionArray[] = null;
		
		// Set out delimiter to be a comma.
		setColumnDelimiter(",");

		int lineNumber = 1;

		boolean readDone = false;

		while (!readDone)
		{
			try
			{
				// Prepare the column map on the first line.
				String lineBuffer = bufferedReader.readLine();

				// See if we have exhausted the input
				if (lineBuffer == null)
				{
					break;
				}

				if (columnDescriptionArray == null)
				{
					columnDescriptionArray =
						buildColumnDescriptionArray(
							parseLineFromDelimitedFile(lineBuffer));
					
					// Immediately start the next loop, don't do any more
					// processing.
					lineNumber++;
					continue;
				}
				else
				{
					lineBuffer = lineBuffer.trim();
					
					// If we have continuation lines, concatenate those.
					if ( !lineBuffer.endsWith("\"") || lineBuffer.endsWith(",\"") )
					{
						String lineRead = bufferedReader.readLine();
						
						while ( lineRead != null )
						{
							lineBuffer += "\n" + lineRead;

							if ( lineRead.startsWith("\"") )
							{
								break;
							}
							
							lineRead = bufferedReader.readLine();
						}
					}
					
					handler.handleRow(
						processLine(
							columnDescriptionArray,
							lineNumber,
							parseLineFromDelimitedFile(lineBuffer)));
				}
			}
			catch (IOException e)
			{
				// We'll get an exception when we've exhauster
				readDone = true;
			}

			// If we get this far, increment the line counter.
			lineNumber++;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#filterEvents(java.util.List, java.lang.String[])
	 */
	public List filterEvents(List events, String[] customFieldNames) throws ImportException
	{
		setColumnDelimiter(",");

		Iterator it = events.iterator();
		
		//
		// Convert the date/time fields as they appear in the Outlook import to
		// be a synthesized start/end timerange.
		//
		while ( it.hasNext() )
		{
			Map eventProperties = (Map)it.next();

			Date startTime = (Date) eventProperties.get(GenericCalendarImporter.START_TIME_PROPERTY_NAME);
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
            throw new ImportException( rb.getString("err_no_stime") );
			}
			
			Date endTime = (Date) eventProperties.get(GenericCalendarImporter.END_TIME_PROPERTY_NAME);
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
         else
			{
            throw new ImportException( rb.getString("err_no_etime") );
			}

			Date startDate = (Date) eventProperties.get(GenericCalendarImporter.DATE_PROPERTY_NAME);
         
         // if the source time zone were known, this would be
         // a good place to set it: startCal.setTimeZone()
         GregorianCalendar startCal = new GregorianCalendar();
			if ( startDate != null )
            startCal.setTimeInMillis( startDate.getTime() );
            
         startTimeBreakdown.setYear( startCal.get(Calendar.YEAR) );
         startTimeBreakdown.setMonth( startCal.get(Calendar.MONTH)+1 );
         startTimeBreakdown.setDay( startCal.get(Calendar.DAY_OF_MONTH) );
			
			Date endDate = (Date) eventProperties.get(GenericCalendarImporter.ENDS_PROPERTY_NAME);
         
         // if the source time zone were known, this would be
         // a good place to set it: startCal.setTimeZone()
         GregorianCalendar endCal = new GregorianCalendar();
			if ( endDate != null )
            endCal.setTimeInMillis( endDate.getTime() );
			
         endTimeBreakdown.setYear( endCal.get(Calendar.YEAR) );
         endTimeBreakdown.setMonth( endCal.get(Calendar.MONTH)+1 );
         endTimeBreakdown.setDay( endCal.get(Calendar.DAY_OF_MONTH) );
         
			eventProperties.put(
				GenericCalendarImporter.ACTUAL_TIMERANGE,
				getTimeService().newTimeRange(
               getTimeService().newTimeLocal(startTimeBreakdown),
               getTimeService().newTimeLocal(endTimeBreakdown),
					true,
					false));
		}
		
		return events;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#getDefaultColumnMap()
	 */
	public Map getDefaultColumnMap()
	{
		//
		// Commented out properties are ones that we could set, but are
		// currently not being used.  They might be in the future.
		//
		Map columnHeaderMap = new HashMap();

		columnHeaderMap.put(SUBJECT_HEADER, GenericCalendarImporter.TITLE_PROPERTY_NAME);
		columnHeaderMap.put(DESCRIPTION_HEADER, GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME);
		columnHeaderMap.put(START_DATE_HEADER, GenericCalendarImporter.DATE_PROPERTY_NAME);
		columnHeaderMap.put(START_TIME_HEADER, GenericCalendarImporter.START_TIME_PROPERTY_NAME);
		columnHeaderMap.put(END_TIME_HEADER, GenericCalendarImporter.END_TIME_PROPERTY_NAME);
		columnHeaderMap.put(LOCATION_HEADER, GenericCalendarImporter.LOCATION_PROPERTY_NAME);
		columnHeaderMap.put(END_DATE_HEADER, GenericCalendarImporter.ENDS_PROPERTY_NAME);
		
		// This is one that we use only for conversion.
		columnHeaderMap.put(ALL_DAY_EVENT_HEADER, ALL_DAY_EVENT_HEADER);
		
		return columnHeaderMap;
	}
}
