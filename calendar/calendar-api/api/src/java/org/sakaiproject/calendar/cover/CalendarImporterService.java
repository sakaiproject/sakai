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

package org.sakaiproject.calendar.cover;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.ImportException;
import org.sakaiproject.component.cover.ComponentManager;

/**
* <p>CalendarService is a static Cover for the {@link org.sakaiproject.calendar.api.CalendarImporterService};
* see that interface for usage details.</p>
*/
public class CalendarImporterService
{
	/** Comma separated value import type */
	public static final String CSV_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.CSV_IMPORT;
	
	/** MeetingMaker import type */
	public static final String MEETINGMAKER_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.MEETINGMAKER_IMPORT;
	
	/** Outlook import type */
	public static final String OUTLOOK_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.OUTLOOK_IMPORT;

	/** ical import type */
	public static final String ICALENDAR_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.ICALENDAR_IMPORT;
	
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.calendar.api.CalendarImporterService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.calendar.api.CalendarImporterService) ComponentManager.get(org.sakaiproject.calendar.api.CalendarImporterService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.calendar.api.CalendarImporterService) ComponentManager.get(org.sakaiproject.calendar.api.CalendarImporterService.class);
		}
	}
	private static org.sakaiproject.calendar.api.CalendarImporterService m_instance = null;



	/* (non-Javadoc)
	 * @see org.sakaiproject.service.legacy.calendar#getDefaultColumnMap(java.lang.String)
	 */
	public static Map getDefaultColumnMap(String importType) throws ImportException
	{
		org.sakaiproject.calendar.api.CalendarImporterService service = getInstance();
		
		if ( service != null )
		{
			return service.getDefaultColumnMap(importType);
		}
		else
		{
			return null; 
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.service.legacy.calendar#doImport(java.lang.String, java.io.InputStream, java.util.Map, java.lang.String[])
	 */
	public static List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames) throws ImportException
	{
		return doImport(importType, importStream, columnMapping, customFieldPropertyNames, null);
	}
	
	public static List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames, String userTzid) throws ImportException
	{
		org.sakaiproject.calendar.api.CalendarImporterService service = getInstance();
		
		if ( service != null )
		{
			return service.doImport(importType, importStream, columnMapping, customFieldPropertyNames, userTzid);
		}
		else
		{
			return null; 
		}
	}

}
