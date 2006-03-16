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
package org.sakaiproject.calendar.api.cover;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.ImportException;
import org.sakaiproject.service.framework.component.cover.ComponentManager;

/**
* <p>CalendarService is a static Cover for the {@link org.sakaiproject.calendar.api.CalendarImporterService};
* see that interface for usage details.</p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public class CalendarImporterService
{
	/** Comma separated value import type */
	public static final String CSV_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.CSV_IMPORT;
	
	/** MeetingMaker import type */
	public static final String MEETINGMAKER_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.MEETINGMAKER_IMPORT;
	
	/** Outlook import type */
	public static final String OUTLOOK_IMPORT = org.sakaiproject.calendar.api.CalendarImporterService.OUTLOOK_IMPORT;
	
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.calendar.api.CalendarImporterService getInstance()
	{
		if (ComponentManager.CACHE_SINGLETONS)
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
		org.sakaiproject.calendar.api.CalendarImporterService service = getInstance();
		
		if ( service != null )
		{
			return service.doImport(importType, importStream, columnMapping, customFieldPropertyNames);
		}
		else
		{
			return null; 
		}
	}

}
