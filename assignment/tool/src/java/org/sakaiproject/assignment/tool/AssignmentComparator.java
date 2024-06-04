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


import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentTransferBean;
import org.sakaiproject.assignment.api.SubmissionTransferBean;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.comparator.UserSortNameComparator;

import java.time.Instant;
import java.util.Comparator;
import java.util.function.Predicate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AssignmentComparator extends BaseComparator implements Comparator<AssignmentTransferBean> {

    /**
     * caculate the range string for an assignment
     */
    private String getAssignmentRange(AssignmentTransferBean a) {
        String rv = "";
        if (a.getTypeOfAccess().equals(Assignment.Access.SITE)) {
            // site assignment
            rv = rb.getString("range.allgroups");
        } else {
            try {
                Site site = siteService.getSite(a.getContext());
                for (String s : a.getGroups()) {
                    // announcement by group
                    Group group = site.getGroup(s);
                    if (group != null)
                        rv = rv.concat(group.getTitle());
                }
            } catch (IdUnusedException iue) {
                log.warn("Could not get site: {}, {}", a.getContext(), iue.getMessage());
            }
        }

        return rv;

    } // getAssignmentRange

    /**
     * implementing the compare function
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The compare result. 1 is o1 < o2; -1 otherwise
     */
    public int compare(AssignmentTransferBean a1, AssignmentTransferBean a2) {
        int result = -1;

        if (criteria == null) {
            criteria = AssignmentConstants.SORTED_BY_DEFAULT;
        }

        /** *********** for sorting assignments ****************** */
        if (criteria.equals(AssignmentConstants.SORTED_BY_DEFAULT)) {
            int s1 = a1.getPosition();
            int s2 = a2.getPosition();

            if (s1 == s2) {
                // we either have 2 assignments with no existing postion_order or a numbering error, so sort by duedate
                // sorted by the assignment due date
                Instant t1 = a1.getDueDate();
                Instant t2 = a1.getDueDate();

                if (t1 == null) {
                    result = -1;
                } else if (t2 == null) {
                    result = 1;
                } else {
                    if (t1.equals(t2)) {
                        t1 = a1.getDateCreated();
                        t2 = a2.getDateCreated();
                    }
                    if (t1.isBefore(t2)) {
                        result = 1;
                    } else {
                        result = -1;
                    }
                }
            } else if (s1 == 0 && s2 > 0) // order has not been set on this object, so put it at the bottom of the list
            {
                result = 1;
            } else if (s2 == 0 && s1 > 0) // making sure assignments with no position_order stay at the bottom
            {
                result = -1;
            } else // 2 legitimate postion orders
            {
                result = (s1 < s2) ? -1 : 1;
            }
        }
        if (criteria.equals(AssignmentConstants.SORTED_BY_TITLE)) {
            result = compareString(a1.getTitle(), a2.getTitle());
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_SECTION)) {
            result = compareString(a1.getSection(), a2.getSection());
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_DUEDATE)) {
            result = compareInstant(a1.getDueDate(), a2.getDueDate());
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_SOFT_REMOVED_DATE)) {
            result = compareInstant(a1.getSoftRemovedDate(), a2.getSoftRemovedDate());
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_MODIFIEDDATE)) {
            result = compareInstant(a1.getDateModified(), a2.getDateModified());
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_MODIFIEDUSER)) {
            try {
                User u1 = userDirectoryService.getUser(a1.getModifier());
                User u2 = userDirectoryService.getUser(a2.getModifier());
                result = new UserSortNameComparator().compare(u1, u2);
            } catch (UserNotDefinedException e) {
                log.error("Could not get user {} or {}: {}", a1.getModifier(), a2.getModifier(), e.toString());
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_OPENDATE)) {
            result = compareInstant(a1.getOpenDate(), a2.getOpenDate());
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_ASSIGNMENT_STATUS)) {

            if (assignmentService.allowAddAssignment(a1.getContext())) {
                // comparing assignment status
                String s1 = assignmentService.getAssignmentStatus(a1.getId());
                String s2 = assignmentService.getAssignmentStatus(a2.getId());
                result = compareString(s1, s2);
            } else {
                // comparing submission status
                SubmissionTransferBean as1 = findAssignmentSubmission(a1);
                SubmissionTransferBean as2 = findAssignmentSubmission(a2);
                String s1 = assignmentService.getSubmissionStatus(as1.getId(), false);
                String s2 = assignmentService.getSubmissionStatus(as2.getId(), false);
                result = as1 == null ? 1 : as2 == null ? -1 : compareString(s1, s2);
            }
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_NUM_SUBMISSIONS)) {
            // sort by numbers of submissions
            String assignment1reference = AssignmentReferenceReckoner.reckoner().assignment(a1).reckon().getReference();
            int subNum1 = a1.getDraft() ? -1 : assignmentService.countSubmissions(assignment1reference, null);

            String assignment2reference = AssignmentReferenceReckoner.reckoner().assignment(a2).reckon().getReference();
            int subNum2 = a2.getDraft() ? -1 : assignmentService.countSubmissions(assignment2reference, null);

            result = (subNum1 > subNum2) ? 1 : -1;

        } else if (criteria.equals(AssignmentConstants.SORTED_BY_NUM_UNGRADED)) {
            // sort by numbers of ungraded submissions

            Predicate<SubmissionTransferBean> subFilter = s -> s.getDateSubmitted() != null && !s.getGraded();
            long ungraded1 = assignmentService.getSubmissions(a1.getId()).stream().filter(subFilter).count();
            long ungraded2 = assignmentService.getSubmissions(a2.getId()).stream().filter(subFilter).count();
            result = (ungraded1 > ungraded2) ? 1 : -1;

        /*} else if (criteria.equals(AssignmentConstants.SORTED_BY_GRADE) || criteria.equals(AssignmentConstants.SORTED_BY_SUBMISSION_STATUS)) {
            SubmissionTransferBean submission1 = getSubmission(a1.getId(), null, "compare", null);
            String grade1 = " ";
            if (submission1 != null && submission1.getGraded() && submission1.getGradeReleased()) {
                grade1 = submission1.getGrade();
            }

            SubmissionTransferBean submission2 = getSubmission(a2.getId(), null, "compare", null);
            String grade2 = " ";
            if (submission2 != null && submission2.getGraded() && submission2.getGradeReleased()) {
                grade2 = submission2.getGrade();
            }

            result = compareString(grade1, grade2);
        */
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_MAX_GRADE)) {
            String maxGrade1 = maxGrade(a1.getTypeOfGrade(), a1);
            String maxGrade2 = maxGrade(a2.getTypeOfGrade(), a2);

            try {
                // do integer comparation inside point grade type
                int max1 = Integer.parseInt(maxGrade1);
                int max2 = Integer.parseInt(maxGrade2);
                result = (max1 < max2) ? -1 : 1;
            } catch (NumberFormatException e) {
                // otherwise do an alpha-compare
                result = compareString(maxGrade1, maxGrade2);
            }
        }
        // group related sorting
        else if (criteria.equals(AssignmentConstants.SORTED_BY_FOR)) {
            // sorted by the public view attribute
            String factor1 = getAssignmentRange(a1);
            String factor2 = getAssignmentRange(a2);
            result = compareString(factor1, factor2);
        } else if (criteria.equals(AssignmentConstants.SORTED_BY_ESTIMATE)) {
            result = compareEstimate(a1.getEstimate(), a2.getEstimate());
        }

        return result;
    }

    /**
     * returns SubmissionTransferBean object for given assignment by current user
     *
     * @param a
     * @return
     */
    protected SubmissionTransferBean findAssignmentSubmission(AssignmentTransferBean a) {
        User user = userDirectoryService.getCurrentUser();
        try {
            return assignmentService.getSubmission(a.getId(), user);
        } catch (PermissionException e) {
            log.warn("Could not access submission for user: {}, {}", user.getId(), e.getMessage());
        }
        return null;
    }

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
