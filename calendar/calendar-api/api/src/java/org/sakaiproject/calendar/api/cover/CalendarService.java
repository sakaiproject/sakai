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

import org.sakaiproject.component.cover.ComponentManager;

/**
* <p>CalendarService is a static Cover for the {@link org.sakaiproject.calendar.api.CalendarService CalendarService};
* see that interface for usage details.</p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public class CalendarService
{
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.calendar.api.CalendarService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.calendar.api.CalendarService) ComponentManager.get(org.sakaiproject.calendar.api.CalendarService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.calendar.api.CalendarService) ComponentManager.get(org.sakaiproject.calendar.api.CalendarService.class);
		}
	}
	private static org.sakaiproject.calendar.api.CalendarService m_instance = null;



	public static java.lang.String SERVICE_NAME = org.sakaiproject.calendar.api.CalendarService.SERVICE_NAME;
	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.calendar.api.CalendarService.REFERENCE_ROOT;
	public static java.lang.String EVENT_ADD_CALENDAR = org.sakaiproject.calendar.api.CalendarService.EVENT_ADD_CALENDAR;
	public static java.lang.String EVENT_IMPORT_CALENDAR = org.sakaiproject.calendar.api.CalendarService.EVENT_IMPORT_CALENDAR;
	public static java.lang.String EVENT_REMOVE_CALENDAR = org.sakaiproject.calendar.api.CalendarService.EVENT_REMOVE_CALENDAR;
	public static java.lang.String EVENT_READ_CALENDAR = org.sakaiproject.calendar.api.CalendarService.EVENT_READ_CALENDAR;
	public static java.lang.String EVENT_MODIFY_CALENDAR = org.sakaiproject.calendar.api.CalendarService.EVENT_MODIFY_CALENDAR;
	public static java.lang.String REF_TYPE_CALENDAR = org.sakaiproject.calendar.api.CalendarService.REF_TYPE_CALENDAR;
	public static java.lang.String REF_TYPE_CALENDAR_PDF = org.sakaiproject.calendar.api.CalendarService.REF_TYPE_CALENDAR_PDF;
	public static java.lang.String REF_TYPE_EVENT = org.sakaiproject.calendar.api.CalendarService.REF_TYPE_EVENT;
	public static int MOD_NA = org.sakaiproject.calendar.api.CalendarService.MOD_NA;
	public static int MOD_THIS = org.sakaiproject.calendar.api.CalendarService.MOD_THIS;
	public static int MOD_ALL = org.sakaiproject.calendar.api.CalendarService.MOD_ALL;
	public static int MOD_REST = org.sakaiproject.calendar.api.CalendarService.MOD_REST;
	public static int MOD_PRIOR = org.sakaiproject.calendar.api.CalendarService.MOD_PRIOR;
	public static int UNKNOWN_VIEW = org.sakaiproject.calendar.api.CalendarService.UNKNOWN_VIEW;
	public static int DAY_VIEW = org.sakaiproject.calendar.api.CalendarService.DAY_VIEW;
	public static int WEEK_VIEW = org.sakaiproject.calendar.api.CalendarService.WEEK_VIEW;
	public static int MONTH_VIEW = org.sakaiproject.calendar.api.CalendarService.MONTH_VIEW;
	public static int LIST_VIEW = org.sakaiproject.calendar.api.CalendarService.LIST_VIEW;

	public static java.lang.String calendarReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.calendarReference(param0, param1);
	}

	public static java.lang.String calendarPdfReference(java.lang.String param0, java.lang.String param1,
			int param2, java.util.List param3, java.lang.String param4, java.lang.String param5, org.sakaiproject.time.api.TimeRange param6)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.calendarPdfReference(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.util.List getCalendars()
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.getCalendars();
	}

	public static boolean allowAddCalendar(java.lang.String param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return false;

		return service.allowAddCalendar(param0);
	}

	public static org.sakaiproject.calendar.api.CalendarEdit addCalendar(java.lang.String param0) throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.addCalendar(param0);
	}

	public static boolean allowGetCalendar(java.lang.String param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return false;

		return service.allowGetCalendar(param0);
	}

	public static boolean allowImportCalendar(java.lang.String param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return false;

		return service.allowImportCalendar(param0);
	}

	public static boolean allowUpdateCalendar(java.lang.String param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return false;

		return service.allowUpdateCalendar(param0);
	}

	public static org.sakaiproject.calendar.api.CalendarEdit editCalendar(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.editCalendar(param0);
	}

	public static void commitCalendar(org.sakaiproject.calendar.api.CalendarEdit param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return;

		service.commitCalendar(param0);
	}

	public static void cancelCalendar(org.sakaiproject.calendar.api.CalendarEdit param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return;

		service.cancelCalendar(param0);
	}

	public static void removeCalendar(org.sakaiproject.calendar.api.CalendarEdit param0) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return;

		service.removeCalendar(param0);
	}

	public static boolean allowRemoveCalendar(java.lang.String param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return false;

		return service.allowRemoveCalendar(param0);
	}

	public static java.lang.String eventReference(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.eventReference(param0, param1, param2);
	}

	public static org.sakaiproject.calendar.api.CalendarEventVector getEvents(java.util.List param0, org.sakaiproject.time.api.TimeRange param1)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.getEvents(param0, param1);
	}

	public static org.sakaiproject.calendar.api.RecurrenceRule newRecurrence(java.lang.String param0, int param1, org.sakaiproject.time.api.Time param2)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.newRecurrence(param0, param1, param2);
	}

	public static org.sakaiproject.calendar.api.RecurrenceRule newRecurrence(java.lang.String param0, int param1)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.newRecurrence(param0, param1);
	}

	public static org.sakaiproject.calendar.api.RecurrenceRule newRecurrence(java.lang.String param0, int param1, int param2)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.newRecurrence(param0, param1, param2);
	}

	public static org.sakaiproject.calendar.api.RecurrenceRule newRecurrence(java.lang.String param0)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.newRecurrence(param0);
	}

	public static org.sakaiproject.calendar.api.Calendar getCalendar(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.getCalendar(param0);
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2, java.lang.String param3, java.util.Map param4, java.util.HashMap param5, java.util.Set param6)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.merge(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.lang.String getLabel()
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.getLabel();
	}

	public static java.lang.String archive(java.lang.String param0, org.w3c.dom.Document param1, java.util.Stack param2, java.lang.String param3, java.util.List param4)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return null;

		return service.archive(param0, param1, param2, param3, param4);
	}
	
	public static void importResources(java.lang.String param0, java.lang.String param1, java.util.List param2)
	{
		org.sakaiproject.calendar.api.CalendarService service = getInstance();
		if (service == null)
			return;

		service.importEntities(param0, param1, param2);
	}
}



