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
package org.sakaiproject.component.app.syllabus;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;

/**
 * A syllabus item contains information relating to a syllabus and an order
 * within a particular context (site).
 */

@Data
@EqualsAndHashCode(of = "syllabusId")
@ToString(of = {"syllabusId", "syllabusItem", "asset", "position", "title", "view", "status", "emailNotification", "lockId"})
public class SyllabusDataImpl implements SyllabusData, Comparable {
    private Long syllabusId;
    private String asset;
    private Integer position;
    private Integer lockId;
    private String title;
    private String view;
    private String status;
    private String emailNotification;
    private Set<SyllabusAttachment> attachments = new TreeSet<>();
    private Date startDate;
    private Date endDate;
    private Boolean linkCalendar = Boolean.FALSE;
    private String calendarEventIdStartDate;
    private String calendarEventIdEndDate;
    private SyllabusItem syllabusItem;

    public int compareTo(Object obj) {
        if (this.equals(obj)) return 0;
        if (this.position == null) return -1;
        return this.position.compareTo(((SyllabusData) obj).getPosition());
    }
}
