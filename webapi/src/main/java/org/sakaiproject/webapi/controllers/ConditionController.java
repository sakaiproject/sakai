/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
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
package org.sakaiproject.webapi.controllers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ConditionController extends AbstractSakaiApiController {


    @Autowired
    private ConditionService conditionService;

    @Autowired
    private SecurityService securityService;


    @GetMapping(value = "/sites/{siteId}/conditions/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Condition> getCondition(@PathVariable String siteId, @PathVariable String conditionId) {
        checkSakaiSession();
        checkSite(siteId);

        if (StringUtils.isNotBlank(conditionId)) {
            Condition condition = conditionService.getCondition(conditionId);
            if (condition != null) {
                return ResponseEntity.ok(condition);
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping(value = "/sites/{siteId}/conditions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Condition>> getConditions(@PathVariable String siteId,
                @RequestParam Optional<String> toolId, @RequestParam Optional<String> itemId) {
        checkSakaiSession();
        checkSite(siteId);

        boolean filterByItem = toolId.isPresent() && itemId.isPresent();

        // If only toolId or itemId,not both, are defined, it's a bad request
        if (!filterByItem && (toolId.isPresent() || itemId.isPresent())) {
            return ResponseEntity.badRequest().build();
        }

        List<Condition> conditions = filterByItem
            ? conditionService.getConditionsForItem(siteId, toolId.get(), itemId.get())
            : conditionService.getConditionsForSite(siteId);

        return ResponseEntity.ok(conditions);
    }

    @PostMapping(value = "/sites/{siteId}/conditions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Condition> createCondition(@PathVariable String siteId, @RequestBody Condition condition) {
        Session session = checkSakaiSession();
        Site site = checkSite(siteId);

        if (!canUpdateCondition(session.getUserId(), site.getReference())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (condition != null && StringUtils.isBlank(condition.getId())) {
            Condition savedCondition = conditionService.saveCondition(condition);

            if (savedCondition != null) {
                return ResponseEntity.ok(savedCondition);
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping(value = "/sites/{siteId}/conditions/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<Condition>> createConditions(@PathVariable String siteId, @RequestBody Set<Condition> conditions) {
        Session session = checkSakaiSession();
        Site site = checkSite(siteId);

        if (!canUpdateCondition(session.getUserId(), site.getReference())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        for (Condition condition : conditions) {
            if (!(StringUtils.isBlank(condition.getId()) && StringUtils.equals(siteId, condition.getSiteId()))) {
                return ResponseEntity.badRequest().build();
            }
        }

        Set<Condition> savedConditions = conditions.stream()
                .map(conditionService::saveCondition)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(savedConditions);
    }

    @PutMapping(value = "/sites/{siteId}/conditions/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Condition> updateCondition(@PathVariable String siteId, @PathVariable String conditionId,
            @RequestBody Condition condition) {
        Session session = checkSakaiSession();
        Site site = checkSite(siteId);

        if (!canUpdateCondition(session.getUserId(), site.getReference())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Condition existingCondition = conditionService.getCondition(conditionId);
        log.debug("existingCondition {}", existingCondition);

        if (existingCondition != null && StringUtils.equals(condition.getId(), conditionId)) {
            Condition updatedCondition = conditionService.saveCondition(condition);
            log.debug("updatedCondition {}", updatedCondition);

            if (updatedCondition != null) {
                return ResponseEntity.ok(updatedCondition);
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping(value = "/sites/{siteId}/conditions/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteCondition(@PathVariable String siteId, @PathVariable String conditionId) {
        Session session = checkSakaiSession();
        Site site = checkSite(siteId);

        if (!canUpdateCondition(session.getUserId(), site.getReference())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean success = StringUtils.isNotBlank(conditionId) && conditionService.deleteCondition(conditionId);

        return success ? ResponseEntity.ok(conditionId) : ResponseEntity.badRequest().build();
    }

    private boolean canUpdateCondition(String userId, String siteRef) {
        return securityService.unlock(userId, ConditionService.PERMISSION_UPDATE_CONDITION, siteRef);
    }
}
