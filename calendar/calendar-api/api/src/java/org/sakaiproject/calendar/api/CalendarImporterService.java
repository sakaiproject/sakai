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
	
	/**
	 * Get the default column mapping (keys are column headers, values are property names).
	 * @param importType Type such as Outlook, MeetingMaker, etc. defined in the CalendarImporterService interface.
	 * @throws ImportException
	 */
	public Map getDefaultColumnMap(String importType)  throws ImportException;
	
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
	public List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames)
		throws ImportException;

}
