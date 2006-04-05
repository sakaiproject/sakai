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
import org.sakaiproject.time.api.TimeBreakdown;

/**
 * This class parses an import file from MeetingMaker.
 */
public class MeetingMakerReader extends Reader
{
	private static final String CONTACT_SECTION_HEADER = "Contacts";
	private static final String TODO_SECTION_HEADER = "Todos";
	private static final String EVENT_SECTION_HEADER = "Events";

	public static final String TITLE_HEADER = "Title";	
	public static final String LOCATION_HEADER = "Location";
	public static final String DATE_HEADER = "Date";	
	public static final  String START_TIME_HEADER = "Start Time";
	public static final  String DURATION_HEADER = "Duration";	
	public static final  String AGENDA_NOTES_HEADER = "Agenda/Notes";

	/**
	 * Default constructor 
	 */
	public MeetingMakerReader()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.ImportReader#importStreamFromDelimitedFile(java.io.InputStream, org.sakaiproject.tool.calendar.ImportReader.ReaderImportRowHandler)
	 */
	public void importStreamFromDelimitedFile(
		InputStream stream,
		ReaderImportRowHandler handler)
		throws ImportException
	{
		boolean inEventSection = false;
		boolean alreadySawEventSection = false;

		BufferedReader bufferedReader = getReader(stream);

		ColumnHeader columnDescriptionArray[] = null;

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

				// See if we're in the "Event" section of the import file.
				if (EVENT_SECTION_HEADER.equals(lineBuffer.trim()))
				{
					inEventSection = true;
					alreadySawEventSection = true;
					lineNumber++;
					continue;
				}
				else
				if (TODO_SECTION_HEADER.equals(lineBuffer.trim()))
				{
					inEventSection = false;
					lineNumber++;
					continue;
				}
				else
				if (CONTACT_SECTION_HEADER.equals(lineBuffer.trim()))
				{
					inEventSection = false;
					lineNumber++;
					continue;
				}
				else
				if ( lineBuffer.toString().startsWith("Time Zone:") )
				{
					// Ignore the timezone line.
					lineNumber++;
					continue;
				}

				// If we leave the event section and see another non-event section header,
				// then stop reading the stream since there is only one event section.
				if (alreadySawEventSection && !inEventSection)
				{
					readDone = true;
					continue;
				}

				if (inEventSection)
				{
					if (columnDescriptionArray == null)
					{
						String[] columns = lineBuffer.split("\t");

						trimLeadingTrailingQuotes(columns);

						columnDescriptionArray =
							buildColumnDescriptionArray(columns);

						// Immediately start the next loop.
						lineNumber++;
						continue;
					}
					else
					{
						// Empty lines are preserved at some points, like in quoted string
						// descriptions, but at this point, just skip over them.
						if (lineBuffer.trim().length() == 0 )
						{
							lineNumber++;
							continue;
						}

						String[] columns = lineBuffer.split("\t");

						// If the last column starts with a double-quote, then keep
						// concatentating lines until we see a line that ends
						// with double-quotes.
						String endingColumnValue = columns[columns.length - 1].trim();
						
						if (endingColumnValue.startsWith("\"")
							&& (!endingColumnValue.endsWith("\"")
								|| endingColumnValue.length() == 1))
						{
							String continuationLineBuffer =
								bufferedReader.readLine();

							// See if we have exhausted the input
							while ( continuationLineBuffer != null )
							{
								columns[columns.length - 1] =
									columns[columns.length
										- 1]
										+ "\n"
										+ continuationLineBuffer;
										
								// Break out when we hit the end of the quoted string.
								if ( continuationLineBuffer.trim().endsWith("\"") )
								{
									break;
								}

								continuationLineBuffer = bufferedReader.readLine();
							}
						}

						// Remove trailing/leading quotes from all columns.
						trimLeadingTrailingQuotes(columns);

						handler.handleRow(
							processLine(
								columnDescriptionArray,
								lineNumber,
								columns));
					}
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
		Iterator it = events.iterator();
		int lineNumber = 1;
		
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
			else
			{
				throw new ImportException("No start time specified on line #" + lineNumber);
			}
			
			Integer durationInMinutes = (Integer)eventProperties.get(GenericCalendarImporter.DURATION_PROPERTY_NAME);

			if ( durationInMinutes == null )
			{
				throw new ImportException("No duration time specified on line #" + lineNumber);
			}
			
			Date endTime =
				new Date(
					startTime.getTime() + (durationInMinutes.longValue() * 60 * 1000) );
					
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
					startDateBreakdown.getYear(),
					startDateBreakdown.getMonth() - 1,
					startDateBreakdown.getDay(),
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
					
			lineNumber++;
		}
		
		return events;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#getDefaultColumnMap()
	 */
	public Map getDefaultColumnMap()
	{
		Map columnHeaderMap = new HashMap();

		columnHeaderMap.put(TITLE_HEADER, GenericCalendarImporter.TITLE_PROPERTY_NAME);
		columnHeaderMap.put(AGENDA_NOTES_HEADER, GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME);
		columnHeaderMap.put(DATE_HEADER, GenericCalendarImporter.DATE_PROPERTY_NAME);
		columnHeaderMap.put(START_TIME_HEADER, GenericCalendarImporter.START_TIME_PROPERTY_NAME);
		columnHeaderMap.put(DURATION_HEADER, GenericCalendarImporter.DURATION_PROPERTY_NAME);
		columnHeaderMap.put(LOCATION_HEADER, GenericCalendarImporter.LOCATION_PROPERTY_NAME);
		
		return columnHeaderMap;
	}
}
