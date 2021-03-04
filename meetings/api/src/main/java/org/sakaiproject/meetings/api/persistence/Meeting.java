/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.api.persistence;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MEETINGS")
@Getter @Setter
public class Meeting implements PersistableEntity<String> {

    @Id
    @Column(name = "MEETING_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "NAME", length = 255, nullable = false)
    private String name;

    @Lob
    @Column(name = "WELCOME_MESSAGE")
    private String welcomeMessage;

    @Column(name = "HOST_URL", length = 255)
    private String hostUrl;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "ATTENDEE_PW", length = 99, nullable = false)
    private String attendeePassword;

    @Column(name = "MODERATOR_PW", length = 99, nullable = false)
    private String moderatorPassword;

    @Column(name = "OWNER_ID", length = 99, nullable = false)
    private String ownerId = null;

    @Column(name = "START_DATE")
    private Instant startDate;

    @Column(name = "END_DATE")
    private Instant endDate = null;

    @Column(name = "WAIT_FOR_MODERATOR")
    private Boolean waitForModerator = Boolean.FALSE;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants;

    @Column(name = "MULTIPLE_SESSIONS_ALLOWED")
    private Boolean multipleSessionsAllowed = Boolean.FALSE;

    @Column(name = "CALENDAR_EVENT_ID", length = 99)
    private String calendarEventId;

    @Column(name = "PRESENTATION", length = 255)
    private String presentation;

    @Column(name = "DELETED")
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "RECORDING")
    private Boolean recording = Boolean.FALSE;

    @Column(name = "RECORDING_DURATION")
    private Long recordingDuration;

    @ElementCollection
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUE")
    @CollectionTable(name = "MEETING_PROPERTIES", joinColumns = @JoinColumn(name = "MEETING_ID"))
    private Map<String, String> properties = new HashMap<>();

    @Column(name = "VOICE_BRIDGE")
    private Integer voiceBridge;

    @Transient
    private String joinUrl;

    @Transient
    private String ownerDisplayName;

    @Transient
    private Map<String, String> meta = new HashMap<>();
}
