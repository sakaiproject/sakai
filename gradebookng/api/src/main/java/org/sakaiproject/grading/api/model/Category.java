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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

// TODO: Check this against SAK-46484. I cut this code before that patch.

@Entity
@Table(name = "GB_CATEGORY_T", indexes = @Index(name = "GB_CATEGORY_GB_IDX", columnList = "GRADEBOOK_ID"))
@Getter @Setter
public class Category implements PersistableEntity<Long>, Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "gb_category_id_sequence")
    @SequenceGenerator(name = "gb_category_id_sequence", sequenceName = "GB_CATEGORY_S")
    private Long id;

    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    @ManyToOne
    @JoinColumn(name = "GRADEBOOK_ID", nullable = false)
    private Gradebook gradebook;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "DROP_LOWEST")
    private Integer dropLowest = 0;

    @Column(name = "REMOVED")
    private Boolean removed = Boolean.FALSE;

    @Column(name = "IS_EXTRA_CREDIT")
    private Boolean extraCredit = Boolean.FALSE;

    @Column(name = "IS_EQUAL_WEIGHT_ASSNS")
    private Boolean equalWeightAssignments = Boolean.FALSE;

    @Column(name = "IS_UNWEIGHTED")
    private Boolean unweighted = Boolean.FALSE;

    @Column(name = "CATEGORY_ORDER")
    private Integer categoryOrder = 0;

    @Column(name = "ENFORCE_POINT_WEIGHTING")
    private Boolean enforcePointWeighting = Boolean.FALSE;

    @Column(name = "DROP_HIGHEST")
    private Integer dropHighest = 0;

    @Column(name = "KEEP_HIGHEST")
    private Integer keepHighest = 0;

    @Transient
    private Double averageScore; // average scores that students got for this category
								 //
    @Transient
    private List<GradebookAssignment> assignmentList = new ArrayList<>();;

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
