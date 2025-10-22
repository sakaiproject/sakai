/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at:
 *
 * http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.signup.api.model;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.sakaiproject.signup.api.Permission;
import org.sakaiproject.signup.api.SignupMessageTypes;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * This class holds the information for signup meeting/event. This object is
 * mapped directly to the DB storage by Hibernate
 * </p>
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "signup_meetings")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SignupMeeting implements MeetingTypes, SignupMessageTypes, PersistableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "signup_meeting_seq")
    @SequenceGenerator(name = "signup_meeting_seq", sequenceName = "signup_meeting_ID_SEQ")
    @EqualsAndHashCode.Include @Column(name = "id")
    private Long id;

    @Column(name = "recurrence_id")
    private Long recurrenceId;

    @Version @Column(name = "version")
    private int version;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob @Column(name = "description")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "category")
    private String category;

    @Column(name = "creator_user_id", length = 99, nullable = false)
    private String creatorUserId;

    @Column(name = "coordinators_user_Ids", length = 1000)
    private String coordinatorIds;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time", nullable = false)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time", nullable = false)
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "signup_begins")
    private Date signupBegins;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "signup_deadline")
    private Date signupDeadline;

    @Column(name = "canceled")
    private boolean canceled;

    @Column(name = "locked")
    private boolean locked;

    @Column(name = "meeting_type", length = 50, nullable = false)
    private String meetingType;

    @Column(name = "repeat_type", length = 20)
    private String repeatType;

    @Column(name = "allow_waitList")
    private boolean allowWaitList;

    @Column(name = "allow_comment")
    private boolean allowComment;

    @Column(name = "auto_reminder")
    private boolean autoReminder;

    @Column(name = "eid_input_mode")
    private boolean eidInputMode;

    @Column(name = "receive_email_owner")
    private boolean receiveEmailByOwner;

    @Column(name = "default_send_email_by_owner")
    private boolean sendEmailByOwner;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 50)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name = "list_index") @JoinColumn(name = "meeting_id", nullable = false)
    private List<SignupTimeslot> signupTimeSlots = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 50)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name = "list_index") @JoinColumn(name = "meeting_id", nullable = false)
    private List<SignupSite> signupSites = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "signup_attachments", joinColumns = @JoinColumn(name = "meeting_id", nullable = false))
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 50)
    @OrderColumn(name = "list_index")
    private List<SignupAttachment> signupAttachments = new ArrayList<>();

    @Column(name = "allow_attendance")
    private boolean allowAttendance;

    @Column(name = "create_groups")
    private boolean createGroups;

    @Column(name = "maxnumof_slot")
    private Integer maxNumOfSlots;

    @Column(name = "vevent_uuid", length = 36)
    private String uuid;

    @Transient private Permission permission;
    @Transient private String sendEmailToSelectedPeopleOnly;
    @Transient private int repeatNum;// numbers of occurrences
    @Transient private boolean applyToAllRecurMeetings;
    @Transient private String currentSiteId; // For RESTful case to pass siteId for email
    @Transient private Date repeatUntil;
    @Transient private Calendar cal = Calendar.getInstance();
    @Transient private boolean inMultipleCalendarBlocks = false;
    @Transient private VEvent vevent; // ICS VEvent created for this meeting

    public SignupMeeting() {
        // set the meeting UUID only at construction time
        uuid = UUID.randomUUID().toString();
    }

    /**
     * get how many slots allowed for one user to sign in in this meeting
     *
     * @return Integer.
     */
    public Integer getMaxNumOfSlots() {
        if (maxNumOfSlots == null) {
            maxNumOfSlots = 1; //default
        }
        return maxNumOfSlots;
    }

    /**
     * special setter.
     *
     * @param endTime
     *            the end time of the event/meeting
     */
    public void setEndTime(Date endTime) {
        this.endTime = truncateSeconds(endTime);
    }

    /**
     * This method obtains the number of time slots in the event/meeting
     *
     * @return the number of time slots
     */
    public int getNoOfTimeSlots() {
        return (signupTimeSlots == null) ? 0 : signupTimeSlots.size();
    }

    /**
     * special setter
     *
     * @param signupBegins
     *            a time when the signup process starts
     */
    public void setSignupBegins(Date signupBegins) {
        this.signupBegins = truncateSeconds(signupBegins);
    }

    /**
     * special setter
     *
     * @param signupDeadLine
     *            the time when signup process stops
     */
    public void setSignupDeadline(Date signupDeadLine) {
        this.signupDeadline = truncateSeconds(signupDeadLine);
    }

    /**
     * special setter
     *
     * @param startTime
     *            the time when the event/meeting starts
     */
    public void setStartTime(Date startTime) {
        this.startTime = truncateSeconds(startTime);
    }

    /**
     * get the maximum number of the attendees, which is allowed in one timeslot
     *
     * @return the maximum number of the attendees
     */
    public int getMaxNumberOfAttendees() {
        if (signupTimeSlots == null || signupTimeSlots.isEmpty()) {
            return 0;
        }

        return signupTimeSlots.get(0).getMaxNoOfAttendees();
    }

    /**
     * This method will obtain the SignupTimeslot object according to the timeslotId
     *
     * @param timeslotId
     *            a timeslot Id
     * @return a SignupTimeslot object
     */
    public SignupTimeslot getTimeslot(Long timeslotId) {
        if (signupTimeSlots == null) return null;
        for (SignupTimeslot timeslot : signupTimeSlots) {
            Long id = timeslot.getId();
            if (id != null && id.equals(timeslotId)) {
                return timeslot;
            }
        }
        return null;
    }

    /**
     * This method will obtain the number of participants signed
     *
     * @return an int
     */
    public int getParticipantsNum() {
        return signupTimeSlots == null ? 0 : signupTimeSlots.stream()
                .map(t -> t.getAttendees().size())
                .reduce(Integer::sum)
                .orElse(0);
    }

    /**
     * This method will check if the event/meeting is already expired
     *
     * @return true if the event/meeting is expired
     */
    public boolean isMeetingExpired() {
        return new Date().after(endTime);
    }

    /**
     * This method will check if the current time has already passed the signup
     * deadline
     *
     * @return true if the current time has already passed the signup deadline
     */
    public boolean isPassedDeadline() {
        if (signupDeadline == null) return false;
        return new Date().after(signupDeadline);
    }

    /**
     * Test if the event/meeting is cross days
     *
     * @return true if the event/meeting is cross days
     */
    public boolean isMeetingCrossDays() {
        cal.setTime(getStartTime());
        int startingDay = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTime(getEndTime());
        int endingDay = cal.get(Calendar.DAY_OF_YEAR);
        return (startingDay != endingDay);
    }

    /**
     * Test if the event/meeting is started to sign up
     *
     * @return true if the sign-up begin time is before current time.
     */
    public boolean isStartToSignUp() {
        if (signupBegins == null) return true;
        return signupBegins.before(new Date());
    }

    /**
     * Set the second value to zero, precision is in minutes.
     * Otherwise, it can cause a shorter display by one minute
     *
     * @param time
     *            a Date object
     * @return a Date object
     */
    private Date truncateSeconds(Date time) {
        // set second to zero
        if (time == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public boolean isRecurredMeeting() {
        return recurrenceId != null || DAILY.equals(repeatType) || WEEKLY.equals(repeatType) || BIWEEKLY.equals(repeatType) || WEEKDAYS.equals(repeatType);
    }


    public boolean hasSignupAttachments() {
        return this.signupAttachments != null && !this.signupAttachments.isEmpty();
    }

    /**
     * Breaks up the coordinatorIds string into a list object
     * @return a list of coordinator id strings
     */
    public List<String> getCoordinatorIdsList() {

        List<String> coUsers = new ArrayList<>();

        if (StringUtils.isNotBlank(coordinatorIds)) {
            StringTokenizer userIdTokens = new StringTokenizer(coordinatorIds, "|");
            while (userIdTokens.hasMoreTokens()) {
                String uId = userIdTokens.nextToken();
                coUsers.add(uId);
            }
        }
        return coUsers;
    }
}
