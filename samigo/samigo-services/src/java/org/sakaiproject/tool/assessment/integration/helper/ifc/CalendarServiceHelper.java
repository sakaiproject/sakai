package org.sakaiproject.tool.assessment.integration.helper.ifc;
import java.util.*;
import org.sakaiproject.calendar.api.Calendar;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;

import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;


public interface CalendarServiceHelper {
	public static final String DEADLINE_EVENT_TYPE = "Deadline";
	public static final String QUIZ_EVENT_TYPE = "Quiz";

	public String getString(String key, String defaultValue);

	public String calendarReference(String siteId, String container);

	public Calendar getCalendar(String ref) throws IdUnusedException, PermissionException;

	public void removeCalendarEvent(String siteId, String eventId);

	public String addCalendarEvent(String siteId, String title, String desc, long dateTime, List<Group> groupRestrictions, String calendarEventType);

	public void updateAllCalendarEvents(PublishedAssessmentFacade pub, String releaseTo, String[] groupsAuthorized, String dueDateTitlePrefix, boolean addDueDateToCalendar, String eventDesc);
}