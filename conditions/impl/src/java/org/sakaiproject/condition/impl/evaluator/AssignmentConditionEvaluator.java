/**
 * Copyright (c) 2023 The Apereo Foundation
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
package org.sakaiproject.condition.impl.evaluator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentConditionEvaluator extends BaseConditionEvaluator {


    @Autowired
    private AssignmentService assignmentService;


    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        Assignment assignment = getAssignment(condition.getItemId());

        if (assignment != null && NumberUtils.isParsable(condition.getArgument())) {
            String submitterId = assignmentService.getSubmitterIdForAssignment(assignment, userId);
            List<Double> submissionScores = assignmentService.getSubmissions(assignment).stream()
                    // Filter by user
                    .filter(submission -> submission.getSubmitters().stream()
                            .filter(submissionSubmitter -> StringUtils.equals(submissionSubmitter.getSubmitter(), submitterId))
                            .findAny()
                            .isPresent())
                    // Filter by grades that have been released to the student
                    .filter(AssignmentSubmission::getGradeReleased)
                    // Map to grade
                    .map(submission -> assignmentService.getGradeForSubmitter(submission, submitterId))
                    // Filter by valid values
                    .filter(NumberUtils::isParsable)
                    // Map to double
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());

            if (submissionScores.isEmpty()) {
                log.debug("No graded submission present => false");

                return false;
            }

            BigDecimal highestScore = BigDecimal.valueOf(submissionScores.stream().sorted(Comparator.reverseOrder()).findFirst().get());
            BigDecimal conditionScore = new BigDecimal(condition.getArgument());

            return super.evaluateScore(highestScore, condition.getOperator(), conditionScore);
        }

        return false;
    }

    private Assignment getAssignment(String assignmentId) {
        Assignment assignment;

        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException | PermissionException e) {
            assignment = null;
        }

        return assignment;
    }
}
