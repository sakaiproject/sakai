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
