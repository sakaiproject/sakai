/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import java.math.BigDecimal;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.sakaiproject.grading.api.GradingConstants;

import lombok.Getter;
import lombok.Setter;

/**
 * An AssignmentGradeRecord is a grade record that can be associated with an
 * GradebookAssignment.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Entity
@DiscriminatorValue("1")
@Getter
@Setter
public class AssignmentGradeRecord extends AbstractGradeRecord implements Cloneable {

    @Transient
    private String letterEarned;

    @Transient
    private Double percentEarned;

    @Transient
    private boolean userAbleToView;

    @Column(name = "IS_EXCLUDED_FROM_GRADE")
    private Boolean excludedFromGrade = Boolean.FALSE;

    @Transient
    private transient BigDecimal earnedWeightedPercentage;

    @Transient
    private transient BigDecimal overallWeight;

    @Transient
    private transient Boolean isDropped = Boolean.FALSE;

    // used for drop highest/lowest score functionality
    @Transient
    private Boolean droppedFromGrade = Boolean.FALSE;

    public static Comparator<AssignmentGradeRecord> numericComparator;

    static{
        numericComparator = new Comparator<AssignmentGradeRecord>() {

            @Override
            public int compare(AssignmentGradeRecord agr1, AssignmentGradeRecord agr2) {

                if (agr1 == null && agr2 == null) {
                    return 0;
                }
                if (agr1 == null) {
                    return -1;
                }
                if (agr2 == null) {
                    return 1;
                }
                Double agr1Points = agr1.getGradeAsPercentage(); // Use percent in case the category is equal weight
                Double agr2Points = agr2.getGradeAsPercentage();

                if (agr1Points == null && agr2Points == null) {
                    return 0;
                }
                if (agr1Points == null && agr2Points != null) {
                    return -1;
                }
                if (agr1Points != null && agr2Points == null) {
                    return 1;
                }
                try {
                    return agr1Points.compareTo(agr2Points);
                } catch (NumberFormatException e) {
                    return agr1Points.compareTo(agr2Points); // if not number, default to calcComparator functionality
                }
            }
        };
    }

    public AssignmentGradeRecord() { }

    /**
     * The graderId and dateRecorded properties will be set explicitly by the
     * grade manager before the database is updated.
     * @param assignment The assignment this grade record is associated with
     * @param studentId The student id for whom this grade record belongs
     * @param grade The grade, or points earned
     */
    public AssignmentGradeRecord(GradebookAssignment assignment, String studentId, Double grade) {

        super();
        this.gradableObject = assignment;
        this.studentId = studentId;
        this.pointsEarned = grade;
    }

    public static Comparator<AssignmentGradeRecord> calcComparator;

    static {
        calcComparator = new Comparator<AssignmentGradeRecord>() {
            @Override
            public int compare(AssignmentGradeRecord agr1, AssignmentGradeRecord agr2) {
                if(agr1 == null && agr2 == null) {
                    return 0;
                }
                if(agr1 == null) {
                    return -1;
                }
                if(agr2 == null) {
                    return 1;
                }
                Double agr1Points = agr1.getPointsEarned();
                Double agr2Points = agr2.getPointsEarned();

                if (agr1Points == null && agr2Points == null) {
                    return 0;
                }
                if (agr1Points == null && agr2Points != null) {
                    return -1;
                }
                if (agr1Points != null && agr2Points == null) {
                    return 1;
                }
                return agr1Points.compareTo(agr2Points);
            }
        };
    }

    /**
     * Returns null if the points earned is null.  Otherwise, returns earned / points possible * 100.
     *
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#getGradeAsPercentage()
     */
    @Override
    public Double getGradeAsPercentage() {

        if (pointsEarned == null) {
            return null;
        }
        BigDecimal bdPointsEarned = new BigDecimal(pointsEarned.toString());
        BigDecimal bdPossible = new BigDecimal(((GradebookAssignment)getGradableObject()).getPointsPossible().toString());
        BigDecimal bdPercent = bdPointsEarned.divide(bdPossible, GradingConstants.MATH_CONTEXT).multiply(new BigDecimal("100"));
        return Double.valueOf(bdPercent.doubleValue());
    }

    /**
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
     */
    @Override
    public boolean isCourseGradeRecord() {
        return false;
    }

    public GradebookAssignment getAssignment() {
        return (GradebookAssignment) this.getGradableObject();
    }

    @Override
    public AssignmentGradeRecord clone() {

        AssignmentGradeRecord agr = new AssignmentGradeRecord();
        agr.setDateRecorded(dateRecorded);
        agr.setGradableObject(gradableObject);
        agr.setGraderId(graderId);
        agr.setLetterEarned(letterEarned);
        agr.setPointsEarned(pointsEarned);
        agr.setPercentEarned(percentEarned);
        agr.setStudentId(studentId);
        return agr;
    }
}



