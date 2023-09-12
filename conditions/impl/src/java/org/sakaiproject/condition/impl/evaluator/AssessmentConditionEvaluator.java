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

import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;

public class AssessmentConditionEvaluator extends BaseConditionEvaluator {


    private GradingService gradingService = new GradingService();


    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        AssessmentGradingData assessmentGradingData = gradingService.getHighestAssessmentGrading(condition.getItemId(), userId);

        if (assessmentGradingData != null) {
            BigDecimal highestScore = BigDecimal.valueOf(assessmentGradingData.getFinalScore());
            BigDecimal conditionScore = new BigDecimal(condition.getArgument());

            return super.evaluateScore(highestScore, condition.getOperator(), conditionScore);
        }

        return false;
    }
}
