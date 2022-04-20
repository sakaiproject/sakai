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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AbstractGradeRecord is the abstract base class for Grade Records, which are
 * records of instructors (or the application, in the case of autocalculated
 * gradebooks) assigning a grade to a student for a particular GradableObject.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Entity
@Table(name = "GB_GRADE_RECORD_T"
    , indexes = @Index(name = "GB_GRADE_RECORD_G_O_IDX", columnList = "GRADABLE_OBJECT_ID")
    , uniqueConstraints = @UniqueConstraint(name = "GB_GRADE_RECORD_STUDENT_ID_IDX", columnNames = { "GRADABLE_OBJECT_ID", "STUDENT_ID" })
)
@DiscriminatorColumn(name = "OBJECT_TYPE_ID")
@DiscriminatorValue("0")
@ToString(onlyExplicitlyIncluded = true)
@Getter @Setter
public abstract class AbstractGradeRecord implements PersistableEntity<Long>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @ToString.Include
    protected Long id;

    @Column(name = "VERSION")
    protected Integer version = 1;

    @Column(name = "STUDENT_ID", nullable = false)
    @ToString.Include
    protected String studentId;

    @Column(name = "GRADER_ID", nullable = false)
    @ToString.Include
    protected String graderId;

    @ManyToOne
    @JoinColumn(name = "GRADABLE_OBJECT_ID", nullable = false)
    protected GradableObject gradableObject;

    @Column(name = "DATE_RECORDED", nullable = false)
    protected Date dateRecorded;

    @Column(name = "POINTS_EARNED")
    protected Double pointsEarned;

    public abstract Double getGradeAsPercentage();

    /**
     * @return Whether this is a course grade record
     */
    public abstract boolean isCourseGradeRecord();
}



