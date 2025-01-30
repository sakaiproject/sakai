/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
