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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.sakaiproject.attendance.util.AttendanceConstants;

/**
 * An AttendanceSite represents all the Attendance related data for a specific Sakai Site.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude="attendanceStatuses")
@Entity(name = "AttendanceSite")
@Table(
    name = "ATTENDANCE_SITE_T",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ATT_SITE_SITE_ID", columnNames = "SITE_ID")
    }
)
public class AttendanceSite implements Serializable {
	private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "ATTENDANCE_SITE_GEN", strategy = "native",
            parameters = @Parameter(name = "sequence", value = "ATTENDANCE_SITE_S"))
    @GeneratedValue(generator = "ATTENDANCE_SITE_GEN")
    @Column(name = "A_SITE_ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "SITE_ID", length = 99)
    private String siteID;

    @Enumerated(EnumType.STRING)
    @Column(name = "DEFAULT_STATUS", length = 20)
    private Status defaultStatus;

    @Column(name = "MAXIMUM_GRADE")
    private Double maximumGrade;

    @Column(name = "IS_GRADE_SHOWN")
    private Boolean isGradeShown;

    @Column(name = "SEND_TO_GRADEBOOK")
    private Boolean sendToGradebook;

    @Column(name = "GRADING_METHOD")
    private Integer gradingMethod;

    @Column(name = "GRADEBOOK_ITEM_NAME")
    private String gradebookItemName;

    @Column(name = "SHOW_COMMENTS")
    private Boolean showCommentsToStudents;

    @Column(name = "SYNC")
    private Boolean isSyncing;

    @Column(name = "SYNC_TIME")
    private Instant syncTime;

    @OneToMany(mappedBy = "attendanceSite", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private Set<AttendanceStatus> attendanceStatuses = new HashSet<>(0);

	public AttendanceSite(String siteID){
		this.siteID = siteID;
		this.defaultStatus = Status.UNKNOWN;
		this.isGradeShown = false;
		this.sendToGradebook = false;
		this.gradingMethod = 0;
		this.gradebookItemName = AttendanceConstants.GRADEBOOK_ITEM_NAME;
		this.showCommentsToStudents = false;
		this.isSyncing = false;
	}

	public Boolean getSendToGradebook() {
		if(this.sendToGradebook == null) {
			return false;
		}

		return this.sendToGradebook;
	}

	public Boolean getIsSyncing() {
		if(this.isSyncing == null) {
			return false;
		}

		return this.isSyncing;
	}

	public Boolean getUseAutoGrading() {
        return this.gradingMethod != null;
    }

}
