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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * An AttendanceStatus is a wrapper around the Status enum type defining meta information on individual Statuses.
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Entity(name = "AttendanceStatus")
@Table(name = "ATTENDANCE_STATUS_T")
public class AttendanceStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter @Setter
    @Id
    @GenericGenerator(name = "ATTENDANCE_STATUS_GEN", strategy = "native",
            parameters = @Parameter(name = "sequence", value = "ATTENDANCE_STATUS_S"))
    @GeneratedValue(generator = "ATTENDANCE_STATUS_GEN")
    @Column(name = "A_STATUS_ID", nullable = false, updatable = false)
    private                 Long                    id;

    @Getter @Setter
    @Column(name = "IS_ACTIVE")
    private                 Boolean                 isActive;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private                 Status                  status;

    @Getter @Setter
    @Column(name = "SORT_ORDER")
    private                 int                     sortOrder;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "A_SITE_ID", nullable = false)
    private                 AttendanceSite          attendanceSite;

    // Create a copy constructor
    public AttendanceStatus(AttendanceStatus attendanceStatus) {
        this.isActive       = attendanceStatus.getIsActive();
        this.status         = attendanceStatus.getStatus();
        this.sortOrder      = attendanceStatus.getSortOrder();
        this.attendanceSite = attendanceStatus.getAttendanceSite();
    }

}
