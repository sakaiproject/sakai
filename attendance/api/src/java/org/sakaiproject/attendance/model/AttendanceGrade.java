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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * The AttendanceGrade earned for the all AttendanceItems
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]au)
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AttendanceGrade")
@Table(name = "ATTENDANCE_GRADE_T")
public class AttendanceGrade implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "ATTENDANCE_GRADE_GEN", strategy = "native",
            parameters = @Parameter(name = "sequence", value = "ATTENDANCE_GRADE_S"))
    @GeneratedValue(generator = "ATTENDANCE_GRADE_GEN")
    @Column(name = "A_GRADE_ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "GRADE")
    private Double grade;

    @Column(name = "USER_ID", length = 99)
    private String userID;

    @Column(name = "OVERRIDE")
    private Boolean override;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "A_SITE_ID", nullable = false)
    private AttendanceSite attendanceSite;

    @Column(name = "LAST_MODIFIED_BY", nullable = false, length = 99)
    private String lastModifiedBy;

    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private Instant lastModifiedDate;

	public AttendanceGrade(AttendanceSite aS, String userId){
		this.attendanceSite = aS;
		this.userID 		= userId;
	}
}
