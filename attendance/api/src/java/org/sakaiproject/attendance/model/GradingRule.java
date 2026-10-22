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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Entity(name = "GradingRule")
@Table(name = "ATTENDANCE_RULE_T", uniqueConstraints = {
        @UniqueConstraint(name = "UK_ATTENDANCE_RULE", columnNames = {"A_SITE_ID", "STATUS", "START_RANGE", "END_RANGE"})
})
public class GradingRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "GRADING_RULE_ID", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "A_SITE_ID", nullable = false)
    private AttendanceSite attendanceSite;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status;

    @Column(name = "START_RANGE", nullable = false)
    private Integer startRange;

    @Column(name = "END_RANGE")
    private Integer endRange;

    @Column(name = "POINTS", nullable = false)
    private Double points;

    @Column(name = "LAST_MODIFIED_BY", nullable = false, length = 99)
    private String lastModifiedBy;

    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private Instant lastModifiedDate;

    public GradingRule(AttendanceSite attendanceSite) {
        this.attendanceSite = attendanceSite;
    }

}
