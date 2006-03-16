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

package org.sakaiproject.calendar.impl.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.service.legacy.time.TimeBreakdown;

/**
 * This class parses a comma (or other separator other than a double-quote) delimited
 * file.
 */
public class OutlookReader extends CSVReader
{
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
					if ( !lineBuffer.endsWith("\"") )
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
				startTimeBreakdown = getTimeService().newTime(startTime.getTime()).breakdownLocal();
			}
			
			Date endTime = (Date) eventProperties.get(GenericCalendarImporter.END_TIME_PROPERTY_NAME);
			TimeBreakdown endTimeBreakdown = null;

			if ( endTime != null )
			{
				endTimeBreakdown = getTimeService().newTime(endTime.getTime()).breakdownLocal();
			}

			Date startDate = (Date) eventProperties.get(GenericCalendarImporter.DATE_PROPERTY_NAME);
			TimeBreakdown startDateBreakdown = null;
			
			if ( startDate != null )
			{
				startDateBreakdown = getTimeService().newTime(startDate.getTime()).breakdownLocal();
			}
			
			Date endDate = (Date) eventProperties.get(GenericCalendarImporter.ENDS_PROPERTY_NAME);
			TimeBreakdown endDateBreakdown = null;
			
			if ( endDate != null )
			{
				endDateBreakdown = getTimeService().newTime(endDate.getTime()).breakdownLocal();
			}
			
			// Check for bad input.  This is likely an incorrect file format.
			if ( startDateBreakdown == null || endDateBreakdown == null )
			{
				throw new ImportException("Unable to determine start/end times for an event. Please check that this is the correct file format.");				
			}
			
			GregorianCalendar startCal =
				getTimeService().getCalendar(
					getTimeService().getLocalTimeZone(),
					startDateBreakdown.getYear(),
					startDateBreakdown.getMonth() - 1,
					startDateBreakdown.getDay(),
					startTimeBreakdown.getHour(),
					startTimeBreakdown.getMin(),
					startTimeBreakdown.getSec(),
					0);

			GregorianCalendar endCal =
				getTimeService().getCalendar(
					getTimeService().getLocalTimeZone(),
					endDateBreakdown.getYear(),
					endDateBreakdown.getMonth() - 1,
					endDateBreakdown.getDay(),
					endTimeBreakdown.getHour(),
					endTimeBreakdown.getMin(),
					endTimeBreakdown.getSec(),
					0);

			// Include the start time, but not the end time.
			eventProperties.put(
				GenericCalendarImporter.ACTUAL_TIMERANGE,
				getTimeService().newTimeRange(
					getTimeService().newTime(startCal.getTimeInMillis()),
					getTimeService().newTime(endCal.getTimeInMillis()),
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
