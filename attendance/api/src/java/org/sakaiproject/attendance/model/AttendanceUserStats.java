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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "AttendanceUserStats")
@Table(name = "ATTENDANCE_USER_STATS_T")
public class AttendanceUserStats extends AttendanceStats {
    private static final    long            serialVersionUID    = 1L;

    @Id
    @GenericGenerator(name = "ATTENDANCE_USER_STATS_GEN", strategy = "native",
            parameters = @Parameter(name = "sequence", value = "ATTENDANCE_USER_STATS_S"))
    @GeneratedValue(generator = "ATTENDANCE_USER_STATS_GEN")
    @Column(name = "A_USER_STATS_ID", nullable = false, updatable = false)
    private                 Long            id;

    @Column(name = "USER_ID", length = 99)
    private                 String          userID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "A_SITE_ID", nullable = false)
    private                 AttendanceSite  attendanceSite;

    public AttendanceUserStats(String userID, AttendanceSite attendanceSite) {
        this.userID = userID;
        this.attendanceSite = attendanceSite;
    }

}
