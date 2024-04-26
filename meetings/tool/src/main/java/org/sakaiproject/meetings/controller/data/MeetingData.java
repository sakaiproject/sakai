/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.meetings.controller.data;

import java.io.Serializable;
import java.util.List;

import org.sakaiproject.meetings.api.model.AttendeeType;

import lombok.Data;

@Data
public class MeetingData implements Serializable {

    private static final long serialVersionUID = 3284276542110972341L;

    private String id;
    private String title;
    private String contextTitle;
    private String description;
    private String siteId;
    private String startDate;
    private String endDate;
    private String url;
    private String ownerId;
    private boolean saveToCalendar;
    private NotificationType notificationType;
    private Integer live;
    private String provider;
    private AttendeeType participantOption;
    private List<String> groupSelection;
    private List<ParticipantData> participants;
    
}
