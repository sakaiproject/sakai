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

import org.apache.commons.lang3.compare.ComparableUtils;
import org.sakaiproject.condition.api.ConditionEvaluator;
import org.sakaiproject.condition.api.model.ConditionOperator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseConditionEvaluator implements ConditionEvaluator {


    protected boolean evaluateScore(BigDecimal userScore,ConditionOperator conditionOperator, BigDecimal conditionScore) {
        boolean result;

        switch (conditionOperator) {
            case SMALLER_THAN:
                result = ComparableUtils.is(userScore).lessThan(conditionScore);
                break;
            case SMALLER_THAN_OR_EQUAL_TO:
                result = ComparableUtils.is(userScore).lessThanOrEqualTo(conditionScore);
                break;
            case EQUAL_TO:
                result = ComparableUtils.is(userScore).equalTo(conditionScore);
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                result = ComparableUtils.is(userScore).greaterThanOrEqualTo(conditionScore);
                break;
            case GREATER_THAN:
                result = ComparableUtils.is(userScore).greaterThan(conditionScore);
                break;
            default:
                log.error("Invalid operator [{}] to evaluate score", conditionOperator);
                result = false;
                break;
        }

        log.debug("Evaluated score: {} {} {} => {}", userScore, conditionOperator, conditionScore, result);
        return result;
    }
}
