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

package org.sakaiproject.calendar.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.ImportException;

/**
 * Interface for importing various calendar exports into Sakai.
 */
public interface CalendarImporterService
{
	/** Comma separated value import type */
	public static final String CSV_IMPORT = "CSV";
	
	/** MeetingMaker import type */
	public static final String MEETINGMAKER_IMPORT = "MeetingMaker";
	
	/** Outlook import type */
	public static final String OUTLOOK_IMPORT = "Outlook";

	/** icalendar import type */
	public static final String ICALENDAR_IMPORT = "Icalendar";
	
	/**
	 * Get the default column mapping (keys are column headers, values are property names).
	 * @param importType Type such as Outlook, MeetingMaker, etc. defined in the CalendarImporterService interface.
	 * @throws ImportException if the importType can't be found.
	 */
	public Map<String, String> getDefaultColumnMap(String importType)  throws ImportException;
	
	/**
	 * Perform an import given the import type.
	 * @param importType Type such as Outlook, MeetingMaker, etc. defined in the CalendarImporterService interface.
	 * @param importStream Stream of data to be imported
	 * @param columnMapping Map of column headers (keys) to property names (values)
	 * @param customFieldPropertyNames Array of custom properties that we want to import.  null if there are no custom properties.
	 * @return A list of CalendarEvent objects.  These objects are not "real", so their copies
	 * must be copied into CalendarEvents created by the Calendar service.
	 * @throws ImportException
	 */
	public List<CalendarEvent> doImport(String importType, InputStream importStream, Map<String, String> columnMapping, String[] customFieldPropertyNames)
		throws ImportException;

	/**
	 * Perform an import given the import type. (SAK-33451)
	 * @param importType Type such as Outlook, MeetingMaker, etc. defined in the CalendarImporterService interface.
	 * @param importStream Stream of data to be imported
	 * @param columnMapping Map of column headers (keys) to property names (values)
	 * @param customFieldPropertyNames Array of custom properties that we want to import.  null if there are no custom properties.
	 * @param userTzid Id of user's time zone when the user subscribed to calendar.
	 * @return A list of CalendarEvent objects.  These objects are not "real", so their copies
	 * must be copied into CalendarEvents created by the Calendar service.
	 * @throws ImportException
	 */
	public List<CalendarEvent> doImport(String importType, InputStream importStream, Map<String, String> columnMapping,
			String[] customFieldPropertyNames, String userTzid)
			throws ImportException;


}
