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

package org.sakaiproject.grading.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Represents the settings for the gradebook
 *
 */
@Data
public class GradebookInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String selectedGradingScaleUid;

    /**
     * The ID of the GradeMapping that should be used for this gradebook
     */
    private String selectedGradeMappingId;

    /**
     * The list of GradeMappings defined for this gradebook but as a DTO representation
     */
    private List<GradeMappingDefinition> gradeMappings;

    /**
     * The grading schema map currently in use for the this gradebook. For example A+ = 100 etc.
     */
    private Map<String, Double> selectedGradingScaleBottomPercents = new HashMap<>();

    private Boolean displayReleasedGradeItemsToStudents;

    private GradeType gradeType = GradeType.POINTS;

    private Integer categoryType;

    private List<CategoryDefinition> categories;

    /**
     * The name of the grading scale, e.g. Pass / Not Pass
     */
    private String gradeScale;

    /**
     * Is the course grade to be shown at all?
     */
    private Boolean courseGradeDisplayed;

    /**
     * If the course grade is displayed, should the letter grade be displayed?
     */
    private Boolean courseLetterGradeDisplayed;

    /**
     * If the course grade is displayed, should the total points be displayed?
     */
    private Boolean coursePointsDisplayed;

    /**
     * If the course grade is displayed, should the percentage be displayed?
     */
    private Boolean courseAverageDisplayed;

    /**
     * Are assignment stats to be shown to students?
     */
    private Boolean assignmentStatsDisplayed;

    /**
     * Are course grade stats to be shown to students?
     */
    private Boolean courseGradeStatsDisplayed;


    /**
     * Are students allowed to compare grades with classmates?
     */
    private Boolean allowStudentsToCompareGrades;


    /**
     * Are student names displayed when comparing grades with classmates?
     */
    private Boolean comparingDisplayStudentNames;

    /**
     * Are student surnames displayed when comparing grades with classmates?
     */
    private Boolean comparingDisplayStudentSurnames;

    /**
     * Are teacher comments displayed when comparing grades with classmates?
     */
    private Boolean comparingDisplayTeacherComments;
  
    /**
     * Include grades that doesn't count when comparing grades with classmates?
     */
    private Boolean comparingIncludeAllGrades;

    /**
     * Randomize displayed data order when comparing grades with classmates?
     */
    private Boolean comparingRandomizeDisplayedData;

}
