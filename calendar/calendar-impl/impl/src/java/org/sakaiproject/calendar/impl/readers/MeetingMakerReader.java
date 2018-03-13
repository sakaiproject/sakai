/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.impl.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class parses an import file from MeetingMaker.
 */
public class MeetingMakerReader extends Reader
{
   private ResourceLoader rb = new ResourceLoader("calendar");
   private Map<String, String> defaultHeaderMap = getDefaultColumnMap();
   
	private static final String CONTACT_SECTION_HEADER = "Contacts";
	private static final String TODO_SECTION_HEADER = "Todos";
	private static final String EVENT_SECTION_HEADER = "Events";

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
	public String importStreamFromDelimitedFile(
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
		
		// tzid of calendar
		return null;

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#filterEvents(java.util.List, java.lang.String[], String)
	 */
	public List filterEvents(List events, String[] customFieldNames, String tzid) throws ImportException
	{
		ZoneId dstZoneId = ZoneId.of(getTimeService().getLocalTimeZone().getID());		
		ZoneId srcZoneId;
		if (tzid != null) {
			srcZoneId = ZoneId.of(tzid);
		} else {
			srcZoneId = dstZoneId;
		}
		
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
			Date startDate = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER));
			Integer durationInMinutes = (Integer)eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER));
			
			if (startTime == null ) {
				Integer line = Integer.valueOf(lineNumber);
				String msg = (String)rb.getFormattedMessage("err_no_stime_on", new Object[]{line});
				throw new ImportException( msg );
			}
			if (startDate == null) {
	            Integer line = Integer.valueOf(lineNumber);
				String msg = (String)rb.getFormattedMessage("err_no_start", new Object[]{line});
				throw new ImportException( msg );
			}
			if (durationInMinutes == null) {
				Integer line = Integer.valueOf(lineNumber);
				String msg = (String)rb.getFormattedMessage("err_no_dur", new Object[]{line});
				throw new ImportException( msg );
			}
			
			// Raw date + raw time
			Instant startInstant = startDate.toInstant().plusMillis(startTime.getTime());

			// Raw + calendar/owner TZ's offset
			ZonedDateTime srcZonedDateTime = startInstant.atZone(srcZoneId);
			long millis = startInstant.plusMillis(srcZonedDateTime.getOffset().getTotalSeconds() * 1000).toEpochMilli();
			
			// Duration of event
			Duration gapMinutes = Duration.ofMinutes(durationInMinutes);
			
			// Time Service will ajust to current user's TZ
			eventProperties.put(GenericCalendarImporter.ACTUAL_TIMERANGE,
				getTimeService().newTimeRange(millis, gapMinutes.toMillis()));
					
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

		columnHeaderMap.put(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.TITLE_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DESCRIPTION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DATE_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.START_TIME_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DURATION_PROPERTY_NAME);
		columnHeaderMap.put(GenericCalendarImporter.LOCATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.LOCATION_PROPERTY_NAME);
		
		return columnHeaderMap;
	}
}
