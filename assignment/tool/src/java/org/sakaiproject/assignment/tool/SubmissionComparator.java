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
package org.sakaiproject.assignment.tool;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentTransferBean;
import org.sakaiproject.assignment.api.SubmissionTransferBean;
import org.sakaiproject.assignment.api.SubmitterTransferBean;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubmissionComparator extends BaseComparator implements Comparator<SubmissionTransferBean> {

    /**
     * implementing the compare function
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The compare result. 1 is o1 < o2; -1 otherwise
     */
    public int compare(SubmissionTransferBean s1, SubmissionTransferBean s2) {
        int result = -1;

        if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_LASTNAME)) {
            // sorted by the submitters sort name
            String n1 = "";
            String n2 = "";

            if (s1.getAssignment().getIsGroup()) {
                try {
                    n1 = siteService.getSite(s1.getContext()).getGroup(s1.getGroupId()).getTitle();
                } catch (Throwable _dfef) {
                }
            } else {
                try {
                    n1 = userDirectoryService.getUser(s1.getSubmitters().toArray(new SubmitterTransferBean[0])[0].getSubmitter()).getSortName();
                } catch (UserNotDefinedException e) {
                    log.warn("Cannot find user id while sorting by last name for submission: {}, {}", s1.getId(), e.toString());
                }
            }
            if (s2.getAssignment().getIsGroup()) {
                try {
                    n2 = siteService.getSite(s2.getContext()).getGroup(s2.getGroupId()).getTitle();
                } catch (Throwable _dfef) { // TODO empty exception block
                }
            } else {
                try {
                    n2 = userDirectoryService.getUser(s2.getSubmitters().toArray(new SubmitterTransferBean[0])[0].getSubmitter()).getSortName();
                } catch (UserNotDefinedException e) {
                    log.warn("Cannot find user id while sorting by last name for submission: {}, {}", s2.getId(), e.toString());
                }
            }

            result = n1.compareTo(n2);
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_SUBMIT_TIME)) {
            // sorted by submission time
            result = compareInstant(s1.getDateSubmitted(), s2.getDateSubmitted());
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_STATUS)) {
            // sort by submission status
            String stat1 = assignmentService.getSubmissionStatus(s1.getId(), false);
            String stat2 = assignmentService.getSubmissionStatus(s2.getId(), false);
            result = compareString(stat1, stat2);
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_GRADE)) {
            // sort by submission grade
            String grade1 = s1.getGrade();
            String grade2 = s2.getGrade();
            if (grade1 == null) {
                grade1 = "";
            }
            if (grade2 == null) {
                grade2 = "";
            }

            // if scale is points
            if (s1.getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE
                    && s2.getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                if ("".equals(grade1)) {
                    result = -1;
                } else if ("".equals(grade2)) {
                    result = 1;
                } else {
                    result = compareDouble(grade1, grade2);
                }
            } else {
                result = compareString(grade1, grade2);
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_GRADE)) {
            // sort by submission grade
            String grade1 = s1.getGrade();
            String grade2 = s2.getGrade();
            if (grade1 == null) {
                grade1 = "";
            }
            if (grade2 == null) {
                grade2 = "";
            }

            // if scale is points
            if (s1.getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE
                    && s2.getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                if ("".equals(grade1)) {
                    result = -1;
                } else if ("".equals(grade2)) {
                    result = 1;
                } else {
                    result = compareDouble(grade1, grade2);
                }
            } else {
                result = compareString(grade1, grade2);
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_MAX_GRADE)) {
            Optional<AssignmentTransferBean> a1 = assignmentService.getAssignmentForSubmission(s1.getId());
            Optional<AssignmentTransferBean> a2 = assignmentService.getAssignmentForSubmission(s2.getId());

            if (a1.isEmpty() || a2.isEmpty()) {
                log.warn("Failed to get assignments for one or both of submissions {} and {}", s1.getId(), s2.getId());
                result = 0;
            } else {
                String maxGrade1 = maxGrade(a1.get().getTypeOfGrade(), a1.get());
                String maxGrade2 = maxGrade(a2.get().getTypeOfGrade(), a2.get());

                try {
                    // do integer comparation inside point grade type
                    int max1 = Integer.parseInt(maxGrade1);
                    int max2 = Integer.parseInt(maxGrade2);
                    result = (max1 < max2) ? -1 : 1;
                } catch (NumberFormatException e) {
                    log.warn("Failed to compare assignment max grades: {}", e.toString());
                    // otherwise do an alpha-compare
                    result = maxGrade1.compareTo(maxGrade2);
                }
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_RELEASED)) {
            // sort by submission released
            String released1 = (Boolean.valueOf(s1.getGradeReleased())).toString();
            String released2 = (Boolean.valueOf(s2.getGradeReleased())).toString();

            result = compareString(released1, released2);
        } else if (criteria.equals(AssignmentConstants.SORTED_SUBMISSION_BY_ASSIGNMENT)) {
            Optional<AssignmentTransferBean> a1 = assignmentService.getAssignmentForSubmission(s1.getId());
            Optional<AssignmentTransferBean> a2 = assignmentService.getAssignmentForSubmission(s2.getId());
            // sort by submission's assignment
            if (a1.isEmpty() || a2.isEmpty()) {
                log.warn("Failed to get assignments for one or both of submissions {} and {}", s1.getId(), s2.getId());
                result = 0;
            } else {
                result = compareString(a1.get().getTitle(), a2.get().getTitle());
            }
        }

        return result;
    }

    /**
     * Compare two strings as double values. Deal with the case when either of the strings cannot be parsed as double value.
     *
     * @param grade1
     * @param grade2
     * @return
     */
    private int compareDouble(String grade1, String grade2) {
        int result;
        try {
            result = Double.valueOf(grade1) > Double.valueOf(grade2) ? 1 : -1;
        } catch (Exception formatException) {
            // in case either grade1 or grade2 cannot be parsed as Double
            result = compareString(grade1, grade2);
            log.warn(this + ":AssignmentComparator compareDouble " + formatException.getMessage());
        }
        return result;
    } // compareDouble

    /**
     * get assignment maximum grade available based on the assignment grade type
     *
     * @param gradeType The int value of grade type
     * @param a         The assignment object
     * @return The max grade String
     */
    private String maxGrade(Assignment.GradeType gradeType, AssignmentTransferBean a) {
        String maxGrade = "";

        if (gradeType == Assignment.GradeType.GRADE_TYPE_NONE) {
            // Grade type not set
            maxGrade = rb.getString("granotset");
        } else if (gradeType == Assignment.GradeType.UNGRADED_GRADE_TYPE) {
            // Ungraded grade type
            maxGrade = rb.getString("gen.nograd");
        } else if (gradeType == Assignment.GradeType.LETTER_GRADE_TYPE) {
            // Letter grade type
            maxGrade = "A";
        } else if (gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
            // Score based grade type
            maxGrade = Integer.toString(a.getMaxGradePoint());
        } else if (gradeType == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
            // Pass/fail grade type
            maxGrade = rb.getString("pass");
        } else if (gradeType == Assignment.GradeType.CHECK_GRADE_TYPE) {
            // Grade type that only requires a check
            maxGrade = rb.getString("check");
        }

        return maxGrade;
    } // maxGrade
}
