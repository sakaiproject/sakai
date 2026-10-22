/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * An AttendanceRecord for a specific user for a specific AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "AttendanceRecord")
@Table(name = "ATTENDANCE_RECORD_T", uniqueConstraints = {
        @UniqueConstraint(name = "UK_ATTENDANCE_RECORD_USER", columnNames = {"USER_ID", "A_EVENT_ID"})
})
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "ATTENDANCE_RECORD_GEN", strategy = "native",
            parameters = @Parameter(name = "sequence", value = "ATTENDANCE_RECORD_S"))
    @GeneratedValue(generator = "ATTENDANCE_RECORD_GEN")
    @Column(name = "A_RECORD_ID", nullable = false, updatable = false)
    private Long            id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "A_EVENT_ID", nullable = false)
    private AttendanceEvent attendanceEvent;

    @Column(name = "USER_ID", length = 99)
    private String          userID;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private Status          status;

    @Column(name = "RECORD_COMMENT", length = 4000)
    private String          comment;

    @Column(name = "LAST_MODIFIED_BY", nullable = false, length = 99)
    private String          lastModifiedBy;

    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private Instant         lastModifiedDate;

    public AttendanceRecord(AttendanceEvent e, String uId, Status s) {
        this.attendanceEvent    = e;
        this.userID             = uId;
        this.status             = s;
    }

}
