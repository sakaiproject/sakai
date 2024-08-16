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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.ContentReviewResult;
import org.sakaiproject.assignment.api.AssignmentTransferBean;
import org.sakaiproject.assignment.api.SubmissionTransferBean;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.util.comparator.UserSortNameComparator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class SubmitterSubmissionComparator extends BaseComparator implements Comparator<SubmitterSubmission> {

    private Map<String, Integer> crSubmissionScoreMap = new HashMap<>();
    private boolean anonymous = false;

    /**
     * implementing the compare function
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The compare result. 1 is o1 < o2; -1 otherwise
     */
    public int compare(SubmitterSubmission ss1, SubmitterSubmission ss2) {

        int result = -1;

        if (criteria == null) {
            criteria = AssignmentConstants.SORTED_BY_DEFAULT;
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_CONTENTREVIEW)) {
            if (ss1 == null || ss2 == null) {
                result = 1;
            } else {
                SubmissionTransferBean s1 = ss1.getSubmission();
                SubmissionTransferBean s2 = ss2.getSubmission();

                if (s1 == null) {
                    result = -1;
                } else if (s2 == null) {
                    result = 1;
                } else {
                    // Avoid expensive calls below if possible
                    Integer score1 = crSubmissionScoreMap.get(s1.getId());
                    Integer score2 = crSubmissionScoreMap.get(s2.getId());

                    if (score1 == null) {
                      score1 = getContentReviewResultScore(assignmentService.getContentReviewResults(s1.getId()));
                      crSubmissionScoreMap.put(s1.getId(), score1);
                    }
                    if (score2 == null) {
                      score2 = getContentReviewResultScore(assignmentService.getContentReviewResults(s2.getId()));
                      crSubmissionScoreMap.put(s2.getId(), score2);
                    }

                    if (score1 == null && score2 == null) {
                        result = 0;
                    } else if (score1 == null) {
                        result = -1;
                    } else if (score2 == null) {
                        result = 1;
                    } else {
                        result = score1 == score2 ? 0 : (score1 > score2 ? 1 : -1);
                    }
                }
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_LASTNAME)) {
            // sorted by the submitters sort name

            if (ss1 == null || ss2 == null || (ss1.getUser() == null && ss1.getGroup() == null) || (ss2.getUser() == null && ss2.getGroup() == null)) {
                result = 1;
            } else if (anonymous) {
                String anon1 = ss1.getSubmission().getId();
                String anon2 = ss2.getSubmission().getId();
                result = compareString(anon1, anon2);
            } else if (ss1.getUser() != null && ss2.getUser() != null) {
                result = new UserSortNameComparator().compare(ss1.getUser(), ss2.getUser());
            } else {
                String lName1 = ss1.getUser() == null ? ss1.getGroup().getTitle() : ss1.getUser().getSortName();
                String lName2 = ss2.getUser() == null ? ss2.getGroup().getTitle() : ss2.getUser().getSortName();
                result = compareString(lName1, lName2);
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME)) {
            // sorted by submission time

            if (ss1 == null || ss2 == null) {
                result = -1;
            } else {
                SubmissionTransferBean s1 = ss1.getSubmission();
                SubmissionTransferBean s2 = ss2.getSubmission();


                if (s1 == null || s1.getDateSubmitted() == null) {
                    result = -1;
                } else if (s2 == null || s2.getDateSubmitted() == null) {
                    result = 1;
                } else if (s1.getDateSubmitted().isBefore(s2.getDateSubmitted())) {
                    result = -1;
                } else {
                    result = 1;
                }
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_STATUS)) {
            // sort by submission status

            String status1 = "";
            String status2 = "";

            if (ss1 == null) {
                status1 = rb.getString("listsub.nosub");
            } else {
                SubmissionTransferBean s1 = ss1.getSubmission();
                if (s1 == null) {
                    status1 = rb.getString("listsub.nosub");
                } else {
                    status1 = assignmentService.getSubmissionStatus(s1.getId(), false);
                }
            }

            if (ss2 == null) {
                status2 = rb.getString("listsub.nosub");
            } else {
                SubmissionTransferBean s2 = ss2.getSubmission();
                if (s2 == null) {
                    status2 = rb.getString("listsub.nosub");
                } else {
                    status2 = assignmentService.getSubmissionStatus(s2.getId(), false);
                }
            }

            result = compareString(status1, status2);
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_GRADE)) {
            // sort by submission status

            if (ss1 == null || ss2 == null) {
                result = -1;
            } else {
                SubmissionTransferBean s1 = ss1.getSubmission();
                SubmissionTransferBean s2 = ss2.getSubmission();

                //sort by submission grade
                if (s1 == null) {
                    result = -1;
                } else if (s2 == null) {
                    result = 1;
                } else {
                    String grade1 = s1.getGrade();
                    String grade2 = s2.getGrade();
                    if (grade1 == null) {
                        grade1 = "";
                    }
                    if (grade2 == null) {
                        grade2 = "";
                    }

                    AssignmentTransferBean a1 = s1.getAssignment();
                    AssignmentTransferBean a2 = s2.getAssignment();

                    // if scale is points
                    if ((a1.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE)
                            && ((a2.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE))) {
                        if (StringUtils.isBlank(grade1)) {
                            result = -1;
                        } else if (StringUtils.isBlank(grade2)) {
                            result = 1;
                        } else {
                            result = compareDouble(grade1, grade2);
                        }
                    } else {
                        result = compareString(grade1, grade2);
                    }
                }
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_RELEASED)) {
            // sort by submission status

            if (ss1 == null || ss2 == null) {
                result = -1;
            } else {
                SubmissionTransferBean s1 = ss1.getSubmission();
                SubmissionTransferBean s2 = ss2.getSubmission();

                if (s1 == null) {
                    result = -1;
                } else if (s2 == null) {
                    result = 1;
                } else {
                    // sort by submission released
                    String released1 = (s1.getGradeReleased()).toString();
                    String released2 = (s2.getGradeReleased()).toString();

                    result = compareString(released1, released2);
                }
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_ESTIMATE)) {
            result = compareEstimate(ss1.getTimeSpent(), ss2.getTimeSpent());
        }

        /*
        // sort ascending or descending
        if (!Boolean.valueOf(m_asc)) {
            result = -result;
        }
        */
        return result;
    }

    private int getContentReviewResultScore(List<ContentReviewResult> resultList) {

        if (CollectionUtils.isEmpty(resultList)) {
            return -1;
        }

        // Find the highest score in all of the possible submissions
        int score = -99;

        for (ContentReviewResult crr : resultList) {
            if (score <= -2 && crr.isPending()) {
                score = -2;
            } else if (score <= -1 && Objects.equals(crr.getReviewReport(), "Error")) {
                // Yes, "Error" appears to be magic throughout the review code
                // Error should appear before pending
                score = -1;
            } else if (crr.getReviewScore() > score) {
                score = crr.getReviewScore();
            }
        }

        return score;
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
}
