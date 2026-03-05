/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.user.api.User;

/**
 * A complete gradebook export payload for integrations.
 */
@Data
public class GradebookMatrixRestBean {

    private String siteId;
    private String gradebookUid;
    private Long exportedAt;
    private GradebookMatrixSettingsRestBean settings;
    private List<GradebookMatrixCategoryRestBean> categories = new ArrayList<>();
    private List<GradebookMatrixColumnRestBean> columns = new ArrayList<>();
    private List<GradebookMatrixStudentRestBean> students = new ArrayList<>();

    @Data
    public static class GradebookMatrixSettingsRestBean {
        private Integer gradeType;
        private Integer categoryType;
        private String gradeScale;
        private Boolean displayReleasedGradeItemsToStudents;
        private Boolean courseGradeDisplayed;
        private Boolean coursePointsDisplayed;
        private Boolean courseAverageDisplayed;
        private Boolean courseLetterGradeDisplayed;

        public GradebookMatrixSettingsRestBean() {}

        public GradebookMatrixSettingsRestBean(GradebookInformation gradebookInformation) {
            if (gradebookInformation == null) {
                return;
            }

            gradeType = gradebookInformation.getGradeType();
            categoryType = gradebookInformation.getCategoryType();
            gradeScale = gradebookInformation.getGradeScale();
            displayReleasedGradeItemsToStudents = gradebookInformation.getDisplayReleasedGradeItemsToStudents();
            courseGradeDisplayed = gradebookInformation.getCourseGradeDisplayed();
            coursePointsDisplayed = gradebookInformation.getCoursePointsDisplayed();
            courseAverageDisplayed = gradebookInformation.getCourseAverageDisplayed();
            courseLetterGradeDisplayed = gradebookInformation.getCourseLetterGradeDisplayed();
        }
    }

    @Data
    public static class GradebookMatrixCategoryRestBean {
        private Long id;
        private String name;
        private Double weight;
        private Integer dropLowest;
        private Integer dropHighest;
        private Integer keepHighest;
        private Boolean extraCredit;
        private Boolean equalWeight;
        private Integer categoryOrder;
        private Boolean dropKeepEnabled;

        public GradebookMatrixCategoryRestBean(CategoryDefinition categoryDefinition) {
            id = categoryDefinition.getId();
            name = categoryDefinition.getName();
            weight = categoryDefinition.getWeight();
            dropLowest = categoryDefinition.getDropLowest();
            dropHighest = categoryDefinition.getDropHighest();
            keepHighest = categoryDefinition.getKeepHighest();
            extraCredit = categoryDefinition.getExtraCredit();
            equalWeight = categoryDefinition.getEqualWeight();
            categoryOrder = categoryDefinition.getCategoryOrder();
            dropKeepEnabled = categoryDefinition.getDropKeepEnabled();
        }
    }

    @Data
    public static class GradebookMatrixColumnRestBean {
        private Long id;
        private String name;
        private Double points;
        private Double weight;
        private Long categoryId;
        private String categoryName;
        private Date dueDate;
        private Boolean released;
        private Boolean counted;
        private Boolean ungraded;
        private Boolean externallyMaintained;
        private String externalId;
        private String externalAppName;

        public GradebookMatrixColumnRestBean(Assignment assignment) {
            id = assignment.getId();
            name = assignment.getName();
            points = assignment.getPoints();
            weight = assignment.getWeight();
            categoryId = assignment.getCategoryId();
            categoryName = assignment.getCategoryName();
            dueDate = assignment.getDueDate();
            released = assignment.getReleased();
            counted = assignment.getCounted();
            ungraded = assignment.getUngraded();
            externallyMaintained = assignment.getExternallyMaintained();
            externalId = assignment.getExternalId();
            externalAppName = assignment.getExternalAppName();
        }
    }

    @Data
    public static class GradebookMatrixStudentRestBean {
        private String userId;
        private String userEid;
        private String userDisplayId;
        private String userDisplayName;
        private String firstName;
        private String lastName;
        private GradebookMatrixCourseGradeRestBean courseGrade;
        private Map<String, GradebookMatrixStudentGradeRestBean> grades = new LinkedHashMap<>();

        public GradebookMatrixStudentRestBean() {}

        public GradebookMatrixStudentRestBean(User user) {
            userId = user.getId();
            userEid = user.getEid();
            userDisplayId = user.getDisplayId();
            userDisplayName = user.getDisplayName();
            firstName = user.getFirstName();
            lastName = user.getLastName();
        }
    }

    @Data
    public static class GradebookMatrixCourseGradeRestBean {
        private String enteredGrade;
        private String calculatedGrade;
        private String mappedGrade;
        private String displayGrade;
        private Double pointsEarned;
        private Double totalPointsPossible;
        private Date dateRecorded;

        public GradebookMatrixCourseGradeRestBean(CourseGradeTransferBean courseGradeTransferBean) {
            enteredGrade = courseGradeTransferBean.getEnteredGrade();
            calculatedGrade = courseGradeTransferBean.getCalculatedGrade();
            mappedGrade = courseGradeTransferBean.getMappedGrade();
            displayGrade = courseGradeTransferBean.getDisplayGrade();
            pointsEarned = courseGradeTransferBean.getPointsEarned();
            totalPointsPossible = courseGradeTransferBean.getTotalPointsPossible();
            dateRecorded = courseGradeTransferBean.getDateRecorded();
        }
    }

    @Data
    public static class GradebookMatrixStudentGradeRestBean {
        private String grade;
        private String gradeComment;
        private Integer gradeEntryType;
        private Boolean gradeReleased;
        private Boolean excused;
        private Date dateRecorded;

        public GradebookMatrixStudentGradeRestBean(GradeDefinition gradeDefinition, boolean includeComments) {
            grade = gradeDefinition.getGrade();
            gradeComment = includeComments ? gradeDefinition.getGradeComment() : null;
            gradeEntryType = gradeDefinition.getGradeEntryType();
            gradeReleased = gradeDefinition.isGradeReleased();
            excused = gradeDefinition.isExcused();
            dateRecorded = gradeDefinition.getDateRecorded();
        }
    }
}
