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
 * This class parses a comma (or other separator other than a double-quote) delimited
 * file.
 */
public class CSVReader extends Reader
{
   private ResourceLoader rb = new ResourceLoader("calendar");
   private Map<String, String> defaultHeaderMap = getDefaultColumnMap();
   
	private static final String COMMENT_LINE_PREFIX = "//";
	/** 
	 * This regular expression will split separate separated values, with optionally quoted columns.
	 * Quote characters not used to group columns must be escaped with a backslash.  The __DELIMITER__
	 * token is replaced with the actual character in the form of \x2c where the "2c" part is the
	 * character value, in this case a comma.
	 */
	private static final String CSV_REGEX =
		"__DELIMITER__(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";
		
	private static final String DELIMITER_TOKEN_IN_REGEX = "__DELIMITER__";

	/** Defaults to comma */
	private String columnDelimiter = ",";

	/**
	 * Default constructor
	 */
	public CSVReader()
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
				
				// Skip comment or empty lines, but keep track of the line number.
				if (lineBuffer.startsWith(COMMENT_LINE_PREFIX)
					|| lineBuffer.trim().length() == 0)
				{
					lineNumber++;
					continue;
				}
				
				if (columnDescriptionArray == null)
				{
					columnDescriptionArray =
						buildColumnDescriptionArray(
							parseLineFromDelimitedFile(lineBuffer));

					lineNumber++;
					
					// Immediately start the next loop, don't do any more
					// processing or increment the line counter.
					continue;
				}
				else
				{
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
	 * Form the hex string for the delimiter character(s)
	 */
	private String getHexStringForDelimiter()
	{
		StringBuilder delimiter = new StringBuilder();
		
		for ( int i=0; i < columnDelimiter.length(); i++)
		{
			delimiter.append( "\\" + "x"); 
			delimiter.append( Integer.toHexString(this.columnDelimiter.charAt(i)) );
		}
		
		return delimiter.toString().replaceAll("\\\\","\\\\\\\\");		
	}
	
	/**
	 * Break a line's columns up into a String array.  (One element for each column.)
	 * @param line
	 */
	protected String[] parseLineFromDelimitedFile(String line)
	{
		String[] columnsReadFromFile;
		String pattern = CSV_REGEX;
		
		pattern =
			pattern.replaceAll(
				DELIMITER_TOKEN_IN_REGEX,
				getHexStringForDelimiter());

		columnsReadFromFile = line.trim().split(pattern);
		
		trimLeadingTrailingQuotes(columnsReadFromFile);

		return columnsReadFromFile;
	}

	/**
	 * Set the delimiter
	 * @param string
	 */
	public void setColumnDelimiter(String columnDelimiter)
	{
		this.columnDelimiter = columnDelimiter;
	}
	
	/**
	 * Get the default column map for CSV files.
	 */
	public Map<String, String> getDefaultColumnMap()
	{
		Map<String, String> columnMap = new HashMap<>();

		columnMap.put(GenericCalendarImporter.LOCATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.LOCATION_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.ITEM_TYPE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.ITEM_TYPE_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.FREQUENCY_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.FREQUENCY_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DURATION_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.START_TIME_CSV_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DATE_CSV_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.DESCRIPTION_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.DESCRIPTION_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.TITLE_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.INTERVAL_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.INTERVAL_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.ENDS_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.ENDS_PROPERTY_NAME);
		columnMap.put(GenericCalendarImporter.REPEAT_DEFAULT_COLUMN_HEADER, GenericCalendarImporter.REPEAT_PROPERTY_NAME);
		
		return columnMap;
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
		
		Map augmentedMapping = getDefaultColumnMap();
		
		// Add custom fields.
		if ( customFieldNames != null )
		{
			for ( int i=0; i < customFieldNames.length; i++ )
			{
				augmentedMapping.put(customFieldNames[i], customFieldNames[i]);
			}
		}
		
		// Use the default mappings
		setColumnHeaderToAtributeMapping(augmentedMapping);

		Iterator it = events.iterator();
		
		int lineNumber = 2;	// The headers are (or should be) on line 1.
		
		//
		// Convert the date/time fields as they appear in the Outlook import to
		// be a synthesized start/end timerange.
		//
		while ( it.hasNext() )
		{
			Map eventProperties = (Map)it.next();

			Date startDate = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER));
			Date startTime = (Date) eventProperties.get(defaultHeaderMap.get(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER));
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

}
