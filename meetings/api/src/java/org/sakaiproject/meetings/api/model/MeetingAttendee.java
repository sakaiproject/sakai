package org.sakaiproject.meetings.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meeting_attendees")
@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MeetingAttendee {

    @Id
    @Column(name = "attendee_id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "meeting_attendee_sequence")
    @SequenceGenerator(name = "meeting_attendee_sequence", sequenceName = "MEETING_ATTENDEE_S")
    private Long id;

    @ManyToOne
    @JoinColumn(name="attendee_meeting_id")
    private Meeting meeting;

    @Column(name = "attendee_type")
    private AttendeeType type;

    @Column(name = "attendee_object_id", length = 255)
    private String objectId;

}
