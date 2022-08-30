package org.sakaiproject.timesheet.api;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "TIMESHEET_ENTRY", indexes = {
        @Index(name = "IDX_TIMESHEETENTRY_REF_USER", columnList = "REFERENCE, USER_ID")
})
public class TimeSheetEntry implements PersistableEntity<Long> {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "timesheet_sequence")
    @SequenceGenerator(name = "timesheet_sequence", sequenceName = "TIMESHEET_S")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name="REFERENCE", length = 255, nullable = false)
    private String reference;

    @Column(name = "USER_ID", length = 99)
    private String userId;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "START_TIME", nullable = false)
    private Instant startTime;

    @Column(name = "DURATION", length = 255)
    private String duration;

    @Column(name = "TEXT_COMMENT", length = 4000)
    private String comment;
}
