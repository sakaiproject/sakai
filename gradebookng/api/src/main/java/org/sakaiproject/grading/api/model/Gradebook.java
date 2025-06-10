/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.springframework.data.PersistableEntity;

// TODO: Check this against SAK-46484. I cut this code before that patch.

/**
 * A Gradebook is the top-level object in the Sakai Gradebook tool.
 */
@Entity
@Table(name = "GB_GRADEBOOK_T")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Gradebook implements PersistableEntity<Long>, Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "gb_gradebook_id_sequence")
    @SequenceGenerator(name = "gb_gradebook_id_sequence", sequenceName = "GB_GRADEBOOK_S")
    @ToString.Include
    private Long id;

    @Column(name = "GRADEBOOK_UID", unique = true, nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private String uid;

    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    @Column(name = "NAME", nullable = false)
    @ToString.Include
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SELECTED_GRADE_MAPPING_ID")
    private GradeMapping selectedGradeMapping;

    @OneToMany(mappedBy = "gradebook", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<GradeMapping> gradeMappings = new HashSet<>();

    @Column(name = "ASSIGNMENTS_DISPLAYED", nullable = false)
    private Boolean assignmentsDisplayed = Boolean.FALSE;

    @Column(name = "COURSE_GRADE_DISPLAYED", nullable = false)
    private Boolean courseGradeDisplayed = Boolean.FALSE;

    @Column(name = "COURSE_LETTER_GRADE_DISPLAYED", nullable = false)
    private Boolean courseLetterGradeDisplayed = Boolean.TRUE;

    @Column(name = "COURSE_POINTS_DISPLAYED", nullable = false)
    private Boolean coursePointsDisplayed = Boolean.FALSE;

    @Column(name = "TOTAL_POINTS_DISPLAYED", nullable = false)
    private Boolean totalPointsDisplayed = Boolean.FALSE;

    @Column(name = "COURSE_AVERAGE_DISPLAYED", nullable = false)
    private Boolean courseAverageDisplayed = Boolean.TRUE;

    @Column(name = "ALL_ASSIGNMENTS_ENTERED", nullable = false)
    private Boolean allAssignmentsEntered = Boolean.FALSE;

    @Column(name = "LOCKED", nullable = false)
    private Boolean locked = Boolean.FALSE;

    @Column(name = "GRADE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private GradeType gradeType = GradeType.POINTS;

    @Column(name = "CATEGORY_TYPE", nullable = false)
    private Integer categoryType = GradingConstants.CATEGORY_TYPE_NO_CATEGORY;

    @Column(name = "IS_EQUAL_WEIGHT_CATS")
    private Boolean equalWeightCategories = Boolean.FALSE;

    @Column(name = "IS_SCALED_EXTRA_CREDIT")
    private Boolean scaledExtraCredit = Boolean.FALSE;

    @Column(name = "DO_SHOW_MEAN")
    private Boolean showMean = Boolean.FALSE;

    @Column(name = "DO_SHOW_MEDIAN")
    private Boolean showMedian = Boolean.FALSE;

    @Column(name = "DO_SHOW_MODE")
    private Boolean showMode = Boolean.FALSE;

    @Column(name = "DO_SHOW_RANK")
    private Boolean showRank = Boolean.FALSE;

    @Column(name = "DO_SHOW_ITEM_STATS")
    private Boolean showItemStatistics = Boolean.FALSE;

    @Column(name = "DO_SHOW_STATISTICS_CHART")
    private Boolean showStatisticsChart = Boolean.FALSE;

    @Column(name = "ASSIGNMENT_STATS_DISPLAYED", nullable = false)
    private Boolean assignmentStatsDisplayed = Boolean.FALSE;

    @Column(name = "COURSE_GRADE_STATS_DISPLAYED", nullable = false)
    private Boolean courseGradeStatsDisplayed = Boolean.FALSE;

    @Column(name = "ALLOW_COMPARE_GRADES", nullable = false)
    private Boolean allowStudentsToCompareGrades = Boolean.FALSE;

    @Column(name = "COMPARING_DISPLAY_FIRSTNAMES", nullable = false)
    private Boolean comparingDisplayStudentNames = Boolean.FALSE;

    @Column(name = "COMPARING_DISPLAY_SURNAMES", nullable = false)
    private  Boolean comparingDisplayStudentSurnames = Boolean.FALSE;

    @Column(name = "COMPARING_DISPLAY_COMMENTS", nullable = false)
    private Boolean comparingDisplayTeacherComments = Boolean.FALSE;

    @Column(name = "COMPARING_DISPLAY_ALLITEMS", nullable = false)
    private Boolean comparingIncludeAllGrades = Boolean.FALSE;

    @Column(name = "COMPARING_RANDOMIZEDATA", nullable = false)
    private Boolean comparingRandomizeDisplayedData = Boolean.FALSE;
}
