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
package org.sakaiproject.condition.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.condition.api.ConditionEvaluator;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.condition.api.exception.UnsupportedToolIdException;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.condition.api.model.ConditionType;
import org.sakaiproject.condition.api.persistence.ConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ConditionServiceImpl implements ConditionService {


    @Autowired
    private ConditionRepository conditionRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FunctionManager functionManager;

    @Setter
    private Map<String, ConditionEvaluator> conditionEvaluators;


    @Override
    public void init() {
        log.info("Initializing Condition Service");

        // Log registered condition evaluators
        if (conditionEvaluators != null) {
            for (Entry<String, ConditionEvaluator> conditionEvaluatorEntry : conditionEvaluators.entrySet()) {
                log.info("Registered condition evaluator [{}] for tool with id [{}]",
                        conditionEvaluatorEntry.getValue().getClass().getName(), conditionEvaluatorEntry.getKey());
            }
        } else {
            log.error("Condition resolvers not set");
        }

        // Register permissions
        functionManager.registerFunction(PERMISSION_UPDATE_CONDITION);
    }

    @Override
    public Condition getCondition(String conditionId) {
        if (StringUtils.isNotBlank(conditionId)) {
            return conditionRepository.findConditionForId(conditionId);
        } else {
            log.error("Can not get condition with invalid id [{}]", conditionId);
            return null;
        }
    }

    @Override
    public List<Condition> getConditionsForSite(String siteId) {
        if (StringUtils.isNotBlank(siteId)) {
            return conditionRepository.findConditionsForSite(siteId);
        } else {
            log.error("Can not get conditions for site with blank id [{}]", siteId);
            return null;
        }
    }

    @Override
    public List<Condition> getConditionsForItem(String siteId, String toolId, String itemId) {
        if (StringUtils.isNoneBlank(siteId, toolId, itemId)) {
            return conditionRepository.findConditionsForItem(siteId, toolId, itemId);
        } else {
            log.error("Can not get conditions for invalid parameters: siteId [{}], toolId [{}] itemId [{}]",
                    siteId, toolId, itemId);
            return null;
        }
    }

    @Override
    public Optional<Condition> getRootConditionForItem(String siteId, String toolId, String itemId) {
        if (StringUtils.isNoneBlank(siteId, toolId, itemId)) {
            return Optional.ofNullable(conditionRepository.findRootConditionForItem(siteId, toolId, itemId));
        } else {
            log.error("Can not get root condition for invalid parameters: siteId [{}], toolId [{}] itemId [{}]",
                    siteId, toolId, itemId);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Condition saveCondition(Condition condition) {
        if (condition != null) {
            String toolId = condition.getToolId();
            Condition originalCondition = Optional.ofNullable(condition.getId())
                    .map(id -> getCondition(id))
                    .orElse(null);

            // Check if toolId is supported
            // For a PARENT condition it can be null
            if (!isToolIdSupported(toolId)
                    && !(condition.getToolId() == null && ConditionType.PARENT.equals(condition.getType()))) {
                throw new UnsupportedToolIdException(toolId);
            }

            if (!isConditionValid(condition)) {
                log.error("Invalid condition can not be saved");
                return originalCondition;
            }

            if (originalCondition != null) {
                if (isConditionUsed(originalCondition)
                        && (!StringUtils.equals(toolId, originalCondition.getToolId())
                                || !StringUtils.equals(condition.getItemId(), originalCondition.getItemId()))) {
                    log.error("Can not update toolId or itemId of condition with id [{}], it is still in use",
                             condition.getId());
                    return originalCondition;
                }

                try {
                    conditionRepository.update(condition);

                    return getCondition(condition.getId());
                } catch (Exception e) {
                    log.error("Updating condition failed: {}", ExceptionUtils.getStackTrace(e));

                    return originalCondition;
                }
            } else {
                if (ConditionType.ROOT.equals(condition.getType())
                        && getRootConditionForItem(condition.getSiteId(), toolId, condition.getItemId()).isPresent()) {
                    log.error("Can not create another root condition for item with siteId [{}], toolId [{}] itemId [{}]",
                        condition.getSiteId(), toolId, condition.getItemId());

                    return null;
                }

                try {
                    return conditionRepository.save(condition);
                } catch (Exception e) {
                    log.error("Saving condition failed: {}", ExceptionUtils.getStackTrace(e));

                    return null;
                }
            }
        } else {
            log.error("Can not save condition as it is null");
            return null;
        }
    }

    @Override
    @Transactional
    public boolean deleteCondition(String conditionId) {
        Condition condition = getCondition(conditionId);

        if (condition != null) {
            conditionRepository.delete(conditionId);
            return true;
        }

        return false;
    }

    @Override
    public boolean isConditionUsed(Condition condition) {
        return condition != null
                && condition.getSubConditions() != null
                && condition.getSubConditions().size() > 0;
    }

    @Override
    public boolean isToolIdSupported(String toolId) {
        return toolId != null && conditionEvaluators.containsKey(toolId)
                || ConditionService.CONDITIONS_TOOL_ID.equals(toolId);
    }

    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        if (condition != null && StringUtils.isNotBlank(userId)) {
            log.debug("Evaluating condition {} for user with id [{}]", condition, userId);

            securityService.pushAdvisor(SecurityAdvisor.ADVISOR_ALLOW_ALL_FUNCTIONS);

            boolean result;
            try {
                result = isParentCondition(condition)
                    ? evaluateSubConditions(condition, userId)
                    : getConditionEvaluator(condition).evaluateCondition(condition, userId);
            } catch(Exception e) {
                result = false;
                log.error("Condition with id [{}] could not be evaluated: {}", condition.getId(),
                        ExceptionUtils.getStackTrace(e));
            } finally {
                securityService.popAdvisor(SecurityAdvisor.ADVISOR_ALLOW_ALL_FUNCTIONS);
            }

            return result;
        } else {
            log.error("Can not evaluate condition due to invalid arguments: condition [{}], userId [{}]",
                    condition, userId);
            return false;
        }
    }

    private boolean evaluateSubConditions(Condition condition, String userId) {
        switch(condition.getOperator()) {
            case AND:
                for (Condition subCondition : condition.getSubConditions()) {
                    if (!evaluateCondition(subCondition, userId)) {
                        return false;
                    }
                }
                return true;
            case OR:
                for (Condition subCondition : condition.getSubConditions()) {
                    if (evaluateCondition(subCondition, userId)) {
                        return true;
                    }
                }
                return condition.getSubConditions().isEmpty();
            default:
                log.error("Unexpected operator [{}] for parent condition");
                return false;
        }
    }

    private boolean isParentCondition(Condition condition) {
        return condition != null && (ConditionType.ROOT.equals(condition.getType())
                        || (ConditionType.PARENT.equals(condition.getType())));
    }

    private ConditionEvaluator getConditionEvaluator(Condition condition) {
        String toolId = condition.getToolId();
        Optional<ConditionEvaluator> conditionEvaluator = Optional.ofNullable(conditionEvaluators.get(toolId));

        return conditionEvaluator.orElseThrow(() -> new UnsupportedToolIdException(toolId));
    }

    private boolean isConditionValid(Condition condition) {
        log.debug("Validating condition: {}", condition);

        ConditionType conditionType = condition.getType();
        String conditionArgument = condition.getArgument();

        if (conditionType == null) {
            return false;
        }

        switch(conditionType) {
            case SCORE:
                boolean scoreParsable = NumberUtils.isParsable(conditionArgument);
                log.debug("Score parsable? {}", scoreParsable);
                return scoreParsable;
            default:
                return true;
        }
    }
}
