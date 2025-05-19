/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.calendar.api.CalendarConstants;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.time.api.TimeRange;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarEventRestBean {

    private String id;
    private String siteId;
    private String siteTitle;
    private String creator;
    private String viewText;
    private String title;
    private String tool;
    private String type;
    private String assignmentId;
    private long start;
    private long duration;
    private List<AttachmentRestBean> attachments;
    private RecurrenceRuleRestBean recurrence;
    private String url;

    public CalendarEventRestBean(CalendarEvent ce, ContentHostingService chs) {

        id = ce.getId();
        siteId = ce.getSiteId();
        siteTitle = ce.getSiteName();
        creator = ce.getCreator();
        viewText = ce.getDescription();
        title = ce.getDisplayName();
        type = ce.getType();
        TimeRange timeRange = ce.getRange();
        start = timeRange.firstTime().getTime();
        duration = timeRange.duration();
        recurrence = new RecurrenceRuleRestBean(ce.getRecurrenceRule());
        assignmentId = ce.getField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);

        // Set the tool property based on the event type or assignmentId
        if (StringUtils.isNotBlank(assignmentId)) {
            this.tool = "assignments";
        } else {
            // Map calendar event types to appropriate icon types based on raw (non-localized) event type strings
            // These are the values defined in CalendarEventType.java
            switch (type) {
                case "Deadline":
                    this.tool = "deadline";
                    break;
                case "Meeting", "Class section - Small Group", "Workshop":
                    this.tool = "users";
                    break;
                case "Exam":
                    this.tool = "gradebook";
                    break;
                case "Quiz", "Formative Assessment":
                    this.tool = "quizzes";
                    break;
                case "Special event", "Academic Calendar":
                    this.tool = "pin";
                    break;
                case "Multidisciplinary Conference", "Class section - Discussion":
                    this.tool = "forums";
                    break;
                case "Class session", "Class section - Lecture":
                    this.tool = "teacher";
                    break;
                case "Class section - Lab", "Computer Session":
                    this.tool = "cog";
                    break;
                case "Tutorial":
                    this.tool = "lightbulb";
                    break;
                case "Web Assignment":
                    this.tool = "assignments";
                    break;
                case "Submission Date", "Activity":
                    this.tool = "file";
                    break;
                case "Cancellation":
                    this.tool = "delete";
                    break;
                default:
                    this.tool = "bell";
                    break;
            }
        }

        attachments = ce.getAttachments().stream().map(ref -> {

            try {
                return new AttachmentRestBean(chs.getResource(ref.getId()));
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
    }
}
