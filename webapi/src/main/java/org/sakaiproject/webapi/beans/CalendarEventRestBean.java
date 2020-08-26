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

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.util.CalendarUtil;

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
    private String creatorDisplayName;
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
        siteId = ce.getLocation();
        siteTitle = ce.getSiteName();
        creator = ce.getCreator();
        viewText = ce.getDescription();
        title = ce.getDisplayName();
        type = ce.getType();
        TimeRange timeRange = ce.getRange();
        start = timeRange.firstTime().getTime();
        duration = timeRange.duration();
        recurrence = new RecurrenceRuleRestBean(ce.getRecurrenceRule());
        assignmentId = ce.getField(CalendarUtil.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);
        if (assignmentId != null) {
            this.tool = "assignments";
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
