package org.sakaiproject.meetings.api.model;

import javax.persistence.CascadeType;
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
@Table(name = "meeting_properties")
@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MeetingProperty {

    @Id
    @Column(name = "prop_id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "meeting_property_sequence")
    @SequenceGenerator(name = "meeting_property_sequence", sequenceName = "MEETING_PROPERTY_S")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="prop_meeting_id")
    private Meeting meeting;

    @Column(name = "prop_name", length = 255, nullable = false)
    private String name;

    @Column(name = "prop_value", length = 255)
    private String value;

}
