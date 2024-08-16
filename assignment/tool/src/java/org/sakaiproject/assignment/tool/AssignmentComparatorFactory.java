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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.assignment.api.AssignmentConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class AssignmentComparatorFactory {

    @Autowired
    private ApplicationContext applicationContext;

    private List<String> assignmentComparatorCriteria = List.of
        (
            AssignmentConstants.SORTED_BY_DEFAULT,
            AssignmentConstants.SORTED_BY_TITLE,
            AssignmentConstants.SORTED_BY_SECTION,
            AssignmentConstants.SORTED_BY_DUEDATE,
            AssignmentConstants.SORTED_BY_SOFT_REMOVED_DATE,
            AssignmentConstants.SORTED_BY_MODIFIEDDATE,
            AssignmentConstants.SORTED_BY_MODIFIEDUSER,
            AssignmentConstants.SORTED_BY_OPENDATE,
            AssignmentConstants.SORTED_BY_ASSIGNMENT_STATUS,
            AssignmentConstants.SORTED_BY_NUM_SUBMISSIONS,
            AssignmentConstants.SORTED_BY_NUM_UNGRADED,
            AssignmentConstants.SORTED_BY_GRADE,
            AssignmentConstants.SORTED_BY_SUBMISSION_STATUS,
            AssignmentConstants.SORTED_BY_MAX_GRADE,
            AssignmentConstants.SORTED_BY_FOR,
            AssignmentConstants.SORTED_BY_ESTIMATE
        );

    private List<String> submitterSubmissionComparatorCriteria = List.of
        (
            AssignmentConstants.SORTED_GRADE_SUBMISSION_CONTENTREVIEW,
            AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_LASTNAME,
            AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME,
            AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_STATUS,
            AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_GRADE,
            AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_RELEASED,
            AssignmentConstants.SORTED_GRADE_SUBMISSION_BY_ESTIMATE
        );

    private List<String> submissionComparatorCriteria = List.of
        (
            AssignmentConstants.SORTED_SUBMISSION_BY_LASTNAME,
            AssignmentConstants.SORTED_SUBMISSION_BY_SUBMIT_TIME,
            AssignmentConstants.SORTED_SUBMISSION_BY_STATUS,
            AssignmentConstants.SORTED_SUBMISSION_BY_GRADE,
            AssignmentConstants.SORTED_SUBMISSION_BY_GRADE,
            AssignmentConstants.SORTED_SUBMISSION_BY_MAX_GRADE,
            AssignmentConstants.SORTED_SUBMISSION_BY_RELEASED,
            AssignmentConstants.SORTED_SUBMISSION_BY_ASSIGNMENT
        );

    public Comparator getComparator(String criteria, boolean anonymous, boolean ascending) {

        if (criteria == null) {
            criteria = AssignmentConstants.SORTED_BY_DEFAULT;
        }

        Comparator comparator = null;

        if (assignmentComparatorCriteria.contains(criteria)) {
            AssignmentComparator aComparator = applicationContext.getBean("org.sakaiproject.assignment.tool.AssignmentComparator", AssignmentComparator.class);
            aComparator.setCriteria(criteria);
            comparator = aComparator;
        } else if (submitterSubmissionComparatorCriteria.contains(criteria)) {
            SubmitterSubmissionComparator ssComparator = applicationContext.getBean("org.sakaiproject.assignment.tool.SubmitterSubmissionComparator", SubmitterSubmissionComparator.class);
            ssComparator.setCriteria(criteria);
            ssComparator.setAnonymous(anonymous);
            comparator = ssComparator;
        } else if (submissionComparatorCriteria.contains(criteria)) {
            SubmissionComparator sComparator = applicationContext.getBean("org.sakaiproject.assignment.tool.SubmissionComparator", SubmissionComparator.class);
            sComparator.setCriteria(criteria);
            comparator = sComparator;
        }

        if (comparator != null) {
            return ascending ? comparator : Collections.reverseOrder(comparator);
        }

        return null;
    }
}
