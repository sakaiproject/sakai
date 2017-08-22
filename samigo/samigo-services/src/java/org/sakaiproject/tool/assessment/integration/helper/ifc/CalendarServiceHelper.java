/**
 * Copyright (c) 2005-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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