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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class parses a comma (or other separator other than a double-quote) delimited
 * file.
 */
public class OutlookReader extends CSVReader
{
	private static final ResourceLoader rb = new ResourceLoader("calendar");
   
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
		//Detect and exclude all BOM
		BOMInputStream bomIn = new BOMInputStream(stream, false, ByteOrderMark.UTF_8,
				ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE,
				ByteOrderMark.UTF_32BE);

		InputStreamReader inputStream = new InputStreamReader(bomIn);
		BufferedReader bufferedReader = new BufferedReader(inputStream);

		return bufferedReader;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.calendar.schedimportreaders.Reader#filterEvents(java.util.List, java.lang.String[], String)
	 */
	public List<Map<String, Object>> filterEvents(List<Map<String, Object>> events, String[] customFieldNames, ZoneId srcZoneId) throws ImportException
	{
		setColumnDelimiter(",");

		// Convert the date/time fields as they appear in the Outlook import to
		// be a synthesized start/end timerange.
		//
		for (Map<String, Object> event: events)
		{
			LocalTime startTime = (LocalTime) event.get(defaultHeaderMap.get(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER));
			LocalDate startDate = (LocalDate) event.get(defaultHeaderMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER));
			LocalTime endTime = (LocalTime) event.get(defaultHeaderMap.get(GenericCalendarImporter.END_TIME_DEFAULT_COLUMN_HEADER));
			LocalDate endDate = (LocalDate) event.get(defaultHeaderMap.get(GenericCalendarImporter.ENDS_DEFAULT_COLUMN_HEADER));
			
			if (startTime == null ) {
				throw new ImportException(rb.getString("err_no_stime"));
			}
			if (endTime == null ) {
				throw new ImportException(rb.getString("err_no_etime"));
			}
			
			// Raw date + raw time
			Instant startInstant = LocalDateTime.of(startDate, startTime).atZone(srcZoneId).toInstant();
			Instant endInstant = LocalDateTime.of(endDate, endTime).atZone(srcZoneId).toInstant();
			
			// Duration of event
			long duration = endInstant.toEpochMilli() - startInstant.toEpochMilli();

			// Time Service will ajust to current user's TZ
			event.put(GenericCalendarImporter.ACTUAL_TIMERANGE, getTimeService().newTimeRange(startInstant.toEpochMilli(), duration));
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
