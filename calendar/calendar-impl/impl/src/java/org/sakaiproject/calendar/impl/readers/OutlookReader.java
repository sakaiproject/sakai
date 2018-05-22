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

package org.sakaiproject.calendar.impl.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
 * This class parses a comma (or other separator other than a double-quote) delimited
 * file.
 */
public class OutlookReader extends CSVReader
{
	private static ResourceLoader rb = new ResourceLoader("calendar");
   
	private Map<String, String> defaultHeaderMap = getDefaultColumnMap();
	
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
	 * @return tzid of calendar (returns null if it does not exist)
	 */
	public String importStreamFromDelimitedFile(
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
						//we need a string buffer
						StringBuffer sb = new StringBuffer(lineBuffer);
						while ( lineRead != null )
						{
							sb.append("\n" + lineRead);

							if ( lineRead.startsWith("\"") )
							{
								break;
							}
							
							lineRead = bufferedReader.readLine();
						}
						lineBuffer = sb.toString();
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
		
		// tzid of calendar
		return null;

	}
	
	/**
	 * overrides {@link org.sakaiproject.calendar.impl.readers.Reader#getReader(InputStream)}
	 * 
	 * This is because MS Outlook exports in other char set that UTF-8 and it probably   
	 * depends on the Outlook's language 
	 */
	protected BufferedReader getReader(InputStream stream) {
		InputStreamReader inStreamReader = null;
		try {
			inStreamReader = new InputStreamReader(stream, rb.getString("import.outlook.charset"));
			
		} catch (UnsupportedEncodingException e) {
			inStreamReader = new InputStreamReader(stream);
		}
		BufferedReader bufferedReader = new BufferedReader(inStreamReader);
		return bufferedReader;
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
		setColumnDelimiter(",");

		Iterator it = events.iterator();
		
		//
		// Convert the date/time fields as they appear in the Outlook import to
		// be a synthesized start/end timerange.
		//
		while ( it.hasNext() )
		{
			Map eventProperties = (Map)it.next();

			Date startTime = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER));
			Date startDate = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER));
			Date endTime = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.END_TIME_DEFAULT_COLUMN_HEADER));
			Date endDate = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.ENDS_DEFAULT_COLUMN_HEADER));
			
			if (startTime == null ) {
				throw new ImportException(rb.getString("err_no_stime"));
			}
			if (endTime == null ) {
				throw new ImportException(rb.getString("err_no_etime"));
			}
			
			// Raw date + raw time
			Instant startInstant = startTime.toInstant();
			Instant endInstant = endTime.toInstant();
			
			if (startDate != null) {
				startInstant = startInstant.plusMillis(startDate.getTime());
			}
			if (endDate != null) {
				endInstant = endInstant.plusMillis(endDate.getTime());
			}
			
			// Duration of event
			long duration = endInstant.toEpochMilli() - startInstant.toEpochMilli();
			
			// Raw + calendar/owner TZ's offset
			ZonedDateTime srcZonedDateTime = startInstant.atZone(srcZoneId);
			long millis = startInstant.plusMillis(srcZonedDateTime.getOffset().getTotalSeconds() * 1000).toEpochMilli();

			// Time Service will ajust to current user's TZ
			eventProperties.put(GenericCalendarImporter.ACTUAL_TIMERANGE,
				getTimeService().newTimeRange(millis, duration));
			
		}
		
		return events;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#getDefaultColumnMap()
	 */
	public Map<String, String> getDefaultColumnMap()
	{
		Map<String, String> columnHeaderMap = new HashMap<>();

		columnHeaderMap.put(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.subject_header"));
		columnHeaderMap.put(GenericCalendarImporter.DESCRIPTION_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.description_header"));
		columnHeaderMap.put(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.start_date_header"));
		columnHeaderMap.put(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.start_time_header"));
		columnHeaderMap.put(GenericCalendarImporter.ENDS_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.end_date_header"));
		columnHeaderMap.put(GenericCalendarImporter.END_TIME_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.end_time_header"));
		columnHeaderMap.put(GenericCalendarImporter.LOCATION_DEFAULT_COLUMN_HEADER, rb.getString("import.outlook.location_header"));
				
		// This is one that we use only for conversion.
		//columnHeaderMap.put(GenericCalendarImporter.ALL_DAY_EVENT_HEADER, rb.getString("import.outlook.end_date_header"));
		
		return columnHeaderMap;
	}
}
