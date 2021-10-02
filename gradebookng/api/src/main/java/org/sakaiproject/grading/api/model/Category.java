/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

// TODO: Check this against SAK-46484. I cut this code before that patch.

@Entity
@Table(name = "GB_CATEGORY_T", indexes = @Index(name = "GB_CATEGORY_GB_IDX", columnList = "GRADEBOOK_ID"))
@Getter @Setter
public class Category implements PersistableEntity<Long>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "VERSION")
    private Integer version = 1;

    @ManyToOne
    @JoinColumn(name = "GRADEBOOK_ID", nullable = false)
    private Gradebook gradebook;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "DROP_LOWEST")
    private Integer dropLowest = 0;

    @Column(name = "DROP_HIGHEST")
    private Integer dropHighest = 0;

    @Column(name = "KEEP_HIGHEST")
    private Integer keepHighest = 0;

    private Boolean removed = Boolean.FALSE;

    @Transient
    private Double averageTotalPoints; // average total points possible for this category
    @Transient
    private Double averageScore; // average scores that students got for this category
    @Transient
    private Double mean; // mean value of percentage for this category
    @Transient
    private Double totalPointsEarned; // scores that students got for this category
    @Transient
    private Double totalPointsPossible; // total points possible for this category
    @Transient
    private List<GradebookAssignment> assignmentList = new ArrayList<>();;
    @Transient
    private int assignmentCount;
    @Transient
    private Boolean extraCredit = Boolean.FALSE;
    @Transient
    private Boolean unweighted;
    @Transient
    private Boolean equalWeightAssignments = Boolean.FALSE;
    @Transient
    private Integer categoryOrder;
    @Transient
    private Boolean enforcePointWeighting;

    public static final Comparator nameComparator;
    public static final Comparator averageScoreComparator;
    public static final Comparator weightComparator;

    public static String SORT_BY_NAME = "name";
    public static String SORT_BY_AVERAGE_SCORE = "averageScore";
    public static String SORT_BY_WEIGHT = "weight";

    static {
        nameComparator = new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                return ((Category) o1).getName().toLowerCase().compareTo(((Category) o2).getName().toLowerCase());
            }
        };
        averageScoreComparator = new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                final Category one = (Category) o1;
                final Category two = (Category) o2;

                if (one.getAverageScore() == null && two.getAverageScore() == null) {
                    return one.getName().compareTo(two.getName());
                }

                if (one.getAverageScore() == null) {
                    return -1;
                }
                if (two.getAverageScore() == null) {
                    return 1;
                }

                final int comp = (one.getAverageScore().compareTo(two.getAverageScore()));
                if (comp == 0) {
                    return one.getName().compareTo(two.getName());
                } else {
                    return comp;
                }
            }
        };
        weightComparator = new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                final Category one = (Category) o1;
                final Category two = (Category) o2;

                if (one.getWeight() == null && two.getWeight() == null) {
                    return one.getName().compareTo(two.getName());
                }

                if (one.getWeight() == null) {
                    return -1;
                }
                if (two.getWeight() == null) {
                    return 1;
                }

                final int comp = (one.getWeight().compareTo(two.getWeight()));
                if (comp == 0) {
                    return one.getName().compareTo(two.getName());
                } else {
                    return comp;
                }
            }
        };
    }

    /*
     * returns true if this category drops any scores
     */
    public boolean isDropScores() {
        return dropLowest > 0 || dropHighest > 0 || keepHighest > 0;
    }

    public Double getItemValue() {

        if (isAssignmentsEqual()) {
            Double returnVal = 0.0;
            final List assignments = getAssignmentList();
            if (assignments != null) {
                for (final Object obj : assignments) {
                    if (obj instanceof GradebookAssignment) {
                        final GradebookAssignment assignment = (GradebookAssignment) obj;
                        if (!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {// ignore adjustment items
                            returnVal = assignment.getPointsPossible();
                            return returnVal;
                        }
                    }
                }
            }
            // didn't find any, so return 0.0
            return returnVal;
        } else {
            return 0.0;
        }
    }

    public void calculateStatistics(final List<GradebookAssignment> assignmentsWithStats) {

        int numScored = 0;
        int numOfAssignments = 0;
        BigDecimal total = new BigDecimal("0");
        BigDecimal totalPossible = new BigDecimal("0");

        for (final GradebookAssignment assign : assignmentsWithStats) {
            final Double score = assign.getAverageTotal();

            if (assign.getCounted() && !assign.getUngraded() && assign.getPointsPossible() != null
                    && assign.getPointsPossible().doubleValue() > 0.0) {
                if (score != null) {
                    total = total.add(new BigDecimal(score.toString()));
                    if (assign.getPointsPossible() != null && !assign.isExtraCredit()) {
                        totalPossible = totalPossible.add(new BigDecimal(assign.getPointsPossible().toString()));
                        numOfAssignments++;
                    }
                    if (!assign.isExtraCredit()) {
                        numScored++;
                    }
                }
            }
        }

        if (numScored == 0 || numOfAssignments == 0) {
            this.averageScore = null;
            this.averageTotalPoints = null;
            this.mean = null;
            this.totalPointsEarned = null;
            this.totalPointsPossible = null;
        } else {
            final BigDecimal bdNumScored = new BigDecimal(numScored);
            final BigDecimal bdNumAssign = new BigDecimal(numOfAssignments);
            this.averageScore = Double.valueOf(total.divide(bdNumScored, GradingConstants.MATH_CONTEXT).doubleValue());
            this.averageTotalPoints = Double.valueOf(totalPossible.divide(bdNumAssign, GradingConstants.MATH_CONTEXT).doubleValue());
            final BigDecimal value = total.divide(bdNumScored, GradingConstants.MATH_CONTEXT)
                    .divide(new BigDecimal(this.averageTotalPoints.doubleValue()), GradingConstants.MATH_CONTEXT)
                    .multiply(new BigDecimal("100"));
            this.mean = Double.valueOf(value.doubleValue());
        }
    }

    public void calculateStatisticsPerStudent(final List<AssignmentGradeRecord> gradeRecords, final String studentUid) {

        getGradebook().getGradeType();
        int numScored = 0;
        int numOfAssignments = 0;
        BigDecimal total = new BigDecimal("0");
        BigDecimal totalPossible = new BigDecimal("0");

        if (gradeRecords == null) {
            setAverageScore(null);
            setAverageTotalPoints(null);
            setMean(null);
            setTotalPointsEarned(null);
            setTotalPointsPossible(null);
            return;
        }

        for (final AssignmentGradeRecord gradeRecord : gradeRecords) {
            if (gradeRecord != null && gradeRecord.getStudentId().equals(studentUid)) {
                final GradebookAssignment assignment = gradeRecord.getAssignment();
                if (assignment.getCounted() && !assignment.getUngraded() && assignment.getPointsPossible().doubleValue() > 0.0
                        && !gradeRecord.getDroppedFromGrade()) {

                    final Category assignCategory = assignment.getCategory();
                    if (assignCategory != null && assignCategory.getId().equals(this.id)) {
                        final Double score = gradeRecord.getPointsEarned();
                        if (score != null) {
                            final BigDecimal bdScore = new BigDecimal(score.toString());
                            total = total.add(bdScore);
                            if (assignment.getPointsPossible() != null && !assignment.isExtraCredit()) {
                                final BigDecimal bdPointsPossible = new BigDecimal(assignment.getPointsPossible().toString());
                                totalPossible = totalPossible.add(bdPointsPossible);
                                numOfAssignments++;
                            }
                            if (!assignment.isExtraCredit()) {
                                numScored++;
                            }
                        }
                    }
                }
            }
        }

        // if totalPossible is 0, this prevents a division by zero scenario likely from
        // an adjustment item being the only thing graded.
        if (numScored == 0 || numOfAssignments == 0 || totalPossible.doubleValue() == 0) {
            this.averageScore = null;
            this.averageTotalPoints = null;
            this.mean = null;
            this.totalPointsEarned = null;
            this.totalPointsPossible = null;
        } else {
            final BigDecimal bdNumScored = new BigDecimal(numScored);
            final BigDecimal bdNumAssign = new BigDecimal(numOfAssignments);
            this.averageScore = Double.valueOf(total.divide(bdNumScored, GradingConstants.MATH_CONTEXT).doubleValue());
            this.averageTotalPoints = Double.valueOf(totalPossible.divide(bdNumAssign, GradingConstants.MATH_CONTEXT).doubleValue());
            final BigDecimal value = total.divide(bdNumScored, GradingConstants.MATH_CONTEXT)
                .divide((totalPossible.divide(bdNumAssign, GradingConstants.MATH_CONTEXT)), GradingConstants.MATH_CONTEXT)
                .multiply(new BigDecimal("100"));

            this.mean = Double.valueOf(value.doubleValue());
        }
    }

    /*
     * The methods below are used with the GradableObjects because all three are displayed in a dataTable together
     */
    /*
    public boolean getIsCategory() {
        return true;
    }

    public boolean isCourseGrade() {
        return false;
    }

    public boolean isAssignment() {
        return false;
    }
    */

    public boolean isAssignmentsEqual() {

        boolean isEqual = true;
        Double pointsPossible = null;
        final List assignments = getAssignmentList();
        if (assignments == null) {
            return isEqual;
        } else {
            for (final Object obj : assignments) {
                if (obj instanceof GradebookAssignment) {
                    final GradebookAssignment assignment = (GradebookAssignment) obj;
                    if (pointsPossible == null) {
                        if (!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {// ignore adjustment items
                            pointsPossible = assignment.getPointsPossible();
                        }
                    } else {
                        if (assignment.getPointsPossible() != null
                                && !GradebookAssignment.item_type_adjustment.equals(assignment.getItemType()) // ignore adjustment items
                                                                                                                // that are not equal
                                && !pointsPossible.equals(assignment.getPointsPossible()) && !equalWeightAssignments) {
                            isEqual = false;
                            return isEqual;
                        }
                    }
                }
            }
        }
        return isEqual;
    }
}
