/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

package org.sakaiproject.grading.api.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.sakaiproject.grading.api.GradingEventStatus;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A log of grading activity.  A GradingEvent should be saved any time a grade
 * record is added or modified.  GradingEvents should be added when the entered
 * value of a course grade record is added or modified, but not when the
 * autocalculated value changes.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Entity
@Table(name = "GB_GRADING_EVENT_T", indexes = {
    @Index(name = "GB_GRADING_EVENT_T_STU_OBJ_ID", columnList = "STUDENT_ID, GRADABLE_OBJECT_ID"),
    @Index(name = "GB_GRADING_EVENT_T_DATE_OBJ_ID", columnList = "DATE_GRADED, GRADABLE_OBJECT_ID") }
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class GradingEvent implements PersistableEntity<Long>, Comparable<Object>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "GRADER_ID", nullable = false)
    @EqualsAndHashCode.Include
    private String graderId;

    @Column(name = "STUDENT_ID", nullable = false)
    @EqualsAndHashCode.Include
    private String studentId;

    @ManyToOne
    @JoinColumn(name = "GRADABLE_OBJECT_ID", nullable = false)
    private GradableObject gradableObject;

    @Column(name = "GRADE")
    private String grade;

    @Column(name = "DATE_GRADED", nullable = false)
    @EqualsAndHashCode.Include
    private Date dateGraded;

    @Column(name = "IS_EXCLUDED")
    private GradingEventStatus status;

    public GradingEvent() {
        this.dateGraded = new Date();
    }

    public GradingEvent(GradableObject gradableObject, String graderId, String studentId, Object grade) {

        this.gradableObject = gradableObject;
        this.graderId = graderId;
        this.studentId = studentId;
        if (grade != null) {
            this.grade = grade.toString();
        }
        this.dateGraded = new Date();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o) {
        return this.dateGraded.compareTo(((GradingEvent)o).dateGraded);
    }
}



