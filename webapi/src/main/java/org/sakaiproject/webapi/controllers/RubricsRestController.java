/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionOutcomeTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.fge.jsonpatch.JsonPatch;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RubricsRestController extends AbstractSakaiApiController {

    private ObjectMapper jsonMapper;

    public RubricsRestController() {
        jsonMapper = MapperFactory.jsonBuilder()
                .includeEmpty()
                .registerJavaTimeModule()
                .build();
    }

    @Autowired
    private RubricsService rubricsService;

    @GetMapping(value = "/sites/{siteId}/rubrics", produces = MediaType.APPLICATION_JSON_VALUE)
    List<EntityModel<RubricTransferBean>> getRubricsForSite(@PathVariable String siteId) {

        checkSakaiSession();

        return rubricsService.getRubricsForSite(siteId).stream().map(b -> entityModelForRubricBean(b)).collect(Collectors.toList());
    }

    @GetMapping(value = "/rubrics/shared", produces = MediaType.APPLICATION_JSON_VALUE)
    List<EntityModel<RubricTransferBean>> getSharedRubrics() {

        checkSakaiSession();

        return rubricsService.getSharedRubrics().stream().map(b -> entityModelForRubricBean(b)).collect(Collectors.toList());
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/default", produces = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<RubricTransferBean> createDefaultRubric(@PathVariable String siteId) {

        checkSakaiSession();

        return entityModelForRubricBean(rubricsService.createDefaultRubric(siteId));
    }

    //@PreAuthorize("canCopy(#sourceId, 'Rubric')")
    @GetMapping(value = "/sites/{siteId}/rubrics/{sourceId}/copyToSite", produces = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<RubricTransferBean> copyRubricToSite(@PathVariable String siteId, @PathVariable Long sourceId) throws Exception {

        checkSakaiSession();

        return entityModelForRubricBean(rubricsService.copyRubricToSite(sourceId, siteId));
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/title")
    public ResponseEntity setCriterionTitle(@PathVariable String siteId, @PathVariable Long criterionId, @RequestBody String title) throws Exception {

        checkSakaiSession();

        return rubricsService.getCriterion(criterionId, siteId).map(criterion -> {

            criterion.setTitle(title);
            try {
                rubricsService.updateCriterion(criterion, siteId);
                return ResponseEntity.ok().build();
            } catch (SecurityException se) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (Exception e) {
                log.error("Failed to patch rubric", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/adhoc", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RubricTransferBean> updateRubricAdhoc(@PathVariable String siteId, @RequestBody RubricTransferBean bean, @RequestParam(defaultValue = "false") Boolean pointsUpdated) throws Exception {

        if (bean == null) {
            log.warn("updateRubricAdhoc called with null rubric bean (siteId={})", siteId);
            return ResponseEntity.badRequest().build();
        }

        Long rubricId = bean.getId();
        if (rubricId == null) {
            log.warn("updateRubricAdhoc called without rubric id (siteId={})", siteId);
            return ResponseEntity.badRequest().build();
        }
        if (bean.getTitle() == null) {
            log.warn("updateRubricAdhoc called without rubric title (siteId={}, rubricId={})", siteId, rubricId);
            return ResponseEntity.badRequest().build();
        }

        log.debug("Loading existing criteria for rubric {}", rubricId);
        List<CriterionTransferBean> oldCriteria = rubricsService.getRubric(rubricId)
                .map(RubricTransferBean::getCriteria)
                .orElseGet(java.util.Collections::emptyList);
        log.debug("Loaded {} existing criteria", oldCriteria.size());
        // confirm rubric changes on database
        RubricTransferBean saved = rubricsService.saveRubric(bean);
        if (saved == null) {
            log.error("Rubric save returned null for rubric {}", rubricId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        List<CriterionTransferBean> newCriteria = java.util.Optional.ofNullable(saved.getCriteria())
                .orElseGet(java.util.Collections::emptyList);
        log.debug("Rubric now has {} criteria after save", newCriteria.size());
        if (Boolean.TRUE.equals(pointsUpdated)) {
            Map<Long, CriterionTransferBean> oldCriteriaById = oldCriteria.stream()
                    .filter(c -> c.getId() != null)
                    .collect(Collectors.toMap(CriterionTransferBean::getId, c -> c, (existing, replacement) -> existing));
            Map<Long, CriterionTransferBean> newCriteriaById = newCriteria.stream()
                    .filter(c -> c.getId() != null)
                    .collect(Collectors.toMap(CriterionTransferBean::getId, c -> c, (existing, replacement) -> replacement));

            List<CriterionTransferBean> toRemove = oldCriteria.stream()
                    .filter(c -> c.getId() == null || !newCriteriaById.containsKey(c.getId()))
                    .filter(c -> c.getRatings() != null && !c.getRatings().isEmpty())
                    .collect(Collectors.toList());
            List<CriterionTransferBean> toUpdate = newCriteria.stream()
                    .filter(c -> c.getId() != null && oldCriteriaById.containsKey(c.getId()))
                    .filter(c -> c.getRatings() != null && !c.getRatings().isEmpty())
                    .collect(Collectors.toList());

            if (log.isDebugEnabled()) {
                log.debug("Criteria to update: {}", toUpdate.stream().map(CriterionTransferBean::getId).collect(Collectors.toList()));
                log.debug("Criteria to add: {}", newCriteria.stream()
                        .filter(c -> c.getId() == null || !oldCriteriaById.containsKey(c.getId()))
                        .map(CriterionTransferBean::getId)
                        .collect(Collectors.toList()));
                log.debug("Criteria to remove: {}", toRemove.stream().map(CriterionTransferBean::getId).collect(Collectors.toList()));
            }

            List<CriterionOutcomeTransferBean> toAddOutcome = new ArrayList<>();
            java.util.Set<Long> addedIds = new java.util.HashSet<>(newCriteriaById.keySet());
            addedIds.removeAll(oldCriteriaById.keySet());
            for (Long id : addedIds) {
                CriterionOutcomeTransferBean co = new CriterionOutcomeTransferBean();
                co.setCriterionId(id);
                co.setPoints(0.0d);
                toAddOutcome.add(co);
            }
            Map<Long, CriterionTransferBean> updateMap = toUpdate.stream()
                    .filter(c -> c.getId() != null)
                    .collect(Collectors.toMap(CriterionTransferBean::getId, item -> item, (existing, replacement) -> replacement));
            List<Long> removeIds = toRemove.stream()
                    .map(CriterionTransferBean::getId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
                
            // get assessment, itemgradings and rubric evaluations
            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            GradingService gradingService = new GradingService();
            String samigoData = StringUtils.removeStart(bean.getTitle(), "pub.");
            String[] samigoIds = samigoData.split("\\.");
            if (samigoIds.length != 2) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Map<Long, List<ItemGradingData>> itemScores = gradingService.getItemScores(Long.valueOf(samigoIds[0]), Long.valueOf(samigoIds[1]), EvaluationModelIfc.ALL_SCORE.toString(), false);
            PublishedAssessmentIfc publishedAssessment = publishedAssessmentService.getPublishedAssessment(samigoIds[0]);
            if (publishedAssessment == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Map<Long, String> ownerIdByAssessment = new java.util.HashMap<>();
            for (Map.Entry<Long, List<ItemGradingData>> entry : itemScores.entrySet()) {
                List<ItemGradingData> igds = entry.getValue();
                log.debug("For published item {}", entry.getKey());
                for (ItemGradingData igd : igds) {
                    Long assessmentGradingId = igd.getAssessmentGradingId();
                    String ownerId = ownerIdByAssessment.get(assessmentGradingId);
                    if (!ownerIdByAssessment.containsKey(assessmentGradingId)) {
                        org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData assessmentGrading = gradingService.load(String.valueOf(assessmentGradingId), false);
                        ownerId = assessmentGrading != null ? assessmentGrading.getAgentId() : null;
                        ownerIdByAssessment.put(assessmentGradingId, ownerId);
                    }
                    if (ownerId == null) {
                        log.warn("Unable to resolve owner id for assessment grading {}", assessmentGradingId);
                        continue;
                    }
                    Double scoreDifference = igd.getAutoScore();
                    boolean matchesPreviousScore = false;
                    log.debug("Item grading {} - assessment grading {} - autoscore {}", igd.getItemGradingId(), igd.getAssessmentGradingId(), igd.getAutoScore());
                    Optional<EvaluationTransferBean> optBean = rubricsService.getEvaluationForToolAndItemAndEvaluatedItemAndOwnerId("sakai.samigo", "pub."+samigoData, igd.getAssessmentGradingId()+"."+entry.getKey(), ownerId, siteId, false);
                    if (optBean.isPresent()) {
                        EvaluationTransferBean eval = optBean.get();
                        if (igd.getAutoScore() != null && eval.getOverallComment() != null && igd.getAutoScore().equals(Double.valueOf(eval.getOverallComment()))) {
                            matchesPreviousScore = true;
                            log.debug("Previous score matches");
                        }
                        log.debug("Evaluation before changes {}", eval);
                        if (eval.getCriterionOutcomes() == null) {
                            eval.setCriterionOutcomes(new ArrayList<>());
                        }
                        for (CriterionOutcomeTransferBean c : eval.getCriterionOutcomes()) {
                            // update points and apply difference
                            if (updateMap.get(c.getCriterionId()) != null && c.getSelectedRatingId() != null) {
                                Double newPoints = updateMap.get(c.getCriterionId()).getRatings().get(0).getPoints();
                                Double oldPoints = c.getPoints();
                                if (matchesPreviousScore && !nearlyEqual(newPoints, oldPoints)) {
                                    c.setPoints(newPoints);
                                    log.debug("Updated criterion, subtracting old {} and adding new {}", oldPoints, newPoints);
                                    scoreDifference -= oldPoints;
                                    scoreDifference += newPoints;
                                }
                            // subtract points of removed criteria
                            } else if (removeIds.contains(c.getCriterionId()) && c.getSelectedRatingId() != null) {
                                scoreDifference -= c.getPoints();
                                log.debug("Deleted criterion, subtracting {}", c.getPoints());
                            }
                            // deselect criterions as grade has been modified manually since last rubric evaluation
                            if (!matchesPreviousScore) {
                                c.setSelectedRatingId(null);
                            }
                        }
                        // after updating grade, modify list of criterion outcomes
                        eval.getCriterionOutcomes().removeIf(c -> removeIds.contains(c.getCriterionId()));
                        eval.getCriterionOutcomes().addAll(toAddOutcome);
                        log.debug("Evaluation after changes {}", eval);

                        if (scoreDifference < 0) {
                            scoreDifference = 0.0;
                        }
                        log.debug("Score is {} and before it was {}", scoreDifference, igd.getAutoScore());
                        eval.setOverallComment(String.valueOf(scoreDifference));
                        rubricsService.saveEvaluation(eval, siteId);
                        log.debug("Rubric evaluation successfully updated");
                        if (!nearlyEqual(scoreDifference, igd.getAutoScore())) {
                            igd.setAutoScore(scoreDifference);
                            gradingService.updateItemScore(igd, 1, publishedAssessment);// if second value is not 0 it will check gb association and update it if necessary    
                            log.debug("Samigo grading successfully updated");
                        }                
                    }
                }
            }
        }

        return ResponseEntity.ok(saved);
    }

    @PatchMapping(value = "/sites/{siteId}/rubrics/{rubricId}", consumes = "application/json-patch+json")
    public ResponseEntity patchRubric(@PathVariable Long rubricId, @RequestBody JsonPatch patch) throws Exception {

        checkSakaiSession();

        return rubricsService.getRubric(rubricId).map(rubric -> {

            try {
                JsonNode patched = patch.apply(jsonMapper.convertValue(rubric, JsonNode.class));
                RubricTransferBean patchedBean = jsonMapper.treeToValue(patched, RubricTransferBean.class);
                rubricsService.saveRubric(patchedBean);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Failed to patch rubric", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/sites/{siteId}/rubrics/{rubricId}")
    public ResponseEntity deleteRubric(@PathVariable Long rubricId) {

        checkSakaiSession();

        if (rubricsService.deleteRubric(rubricId)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<RubricTransferBean>> getRubric(@PathVariable String siteId, @PathVariable Long rubricId) throws Exception {

        checkSakaiSession();

        return rubricsService.getRubric(rubricId)
            .map(b -> ResponseEntity.ok(entityModelForRubricBean(b)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/default", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> createDefaultCriterion(@PathVariable String siteId, @PathVariable Long rubricId) {

        checkSakaiSession();

        return rubricsService.createDefaultCriterion(siteId, rubricId)
            .map(criterion -> ResponseEntity.ok(entityModelForCriterionBean(criterion)))
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/defaultEmpty", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> createDefaultEmptyCriterion(@PathVariable String siteId, @PathVariable Long rubricId) {

        checkSakaiSession();

        return rubricsService.createDefaultEmptyCriterion(siteId, rubricId)
            .map(criterion -> ResponseEntity.ok(entityModelForCriterionBean(criterion)))
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criterions/{criterionId}")
    public ResponseEntity deleteRubric(@PathVariable String siteId, @PathVariable Long rubricId, @PathVariable Long criterionId) {

        checkSakaiSession();

        rubricsService.deleteCriterion(rubricId, criterionId, siteId);
        return ResponseEntity.ok().build();
	}

    @GetMapping(value = "/sites/{siteId}/rubric-associations/tools/{toolId}/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AssociationTransferBean> getAssociationForToolAndAssignment(@PathVariable String siteId, @PathVariable String toolId, @PathVariable String itemId) throws Exception {

        checkSakaiSession();

        Optional<AssociationTransferBean> optBean = rubricsService.getAssociationForToolAndItem(toolId, itemId, siteId);
        if (optBean.isPresent()) {
            return ResponseEntity.ok(optBean.get());
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping(value = "/sites/{siteId}/rubric-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EvaluationTransferBean> createEvaluation(@PathVariable String siteId, @RequestBody EvaluationTransferBean bean) throws Exception {

        return ResponseEntity.ok(rubricsService.saveEvaluation(bean, siteId));
    }

    @PutMapping(value = "/sites/{siteId}/rubric-evaluations/{evaluationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity saveEvaluation(@PathVariable String siteId, @PathVariable Long evaluationId, @RequestBody EvaluationTransferBean bean) throws Exception {

        checkSakaiSession();

        return ResponseEntity.ok(rubricsService.saveEvaluation(bean, siteId));
    }

    @PatchMapping(value = "/sites/{siteId}/rubric-evaluations/{evaluationId}", consumes = "application/json-patch+json")
    ResponseEntity patchEvaluation(@PathVariable String siteId, @PathVariable Long evaluationId, @RequestBody JsonPatch patch) throws Exception {

        checkSakaiSession();

        return rubricsService.getEvaluation(evaluationId, siteId).map(evaluation -> {

            try {
                JsonNode patched = patch.apply(jsonMapper.convertValue(evaluation, JsonNode.class));
                EvaluationTransferBean patchedBean  = jsonMapper.treeToValue(patched, EvaluationTransferBean.class);
                return ResponseEntity.ok(rubricsService.saveEvaluation(patchedBean, siteId));
            } catch (Exception e) {
                log.error("Failed to patch evaluation", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/sites/{siteId}/rubric-evaluations/{evaluationId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EvaluationTransferBean> cancelEvaluation(@PathVariable String siteId, @PathVariable Long evaluationId) throws Exception {

        checkSakaiSession();

        return ResponseEntity.ok(rubricsService.cancelDraftEvaluation(evaluationId));
    }

    @GetMapping(value = {"/sites/{siteId}/rubric-evaluations/tools/{toolId}/items/{itemId}/evaluations/{evaluatedItemId}/owners/{evaluatedItemOwnerId}",
        "/sites/{siteId}/rubric-evaluations/tools/{toolId}/items/{itemId}/evaluations/{evaluatedItemId}"},	produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EvaluationTransferBean> getEvaluation(@PathVariable String siteId, @PathVariable String toolId, @PathVariable String itemId, @PathVariable String evaluatedItemId, @PathVariable(required = false) String evaluatedItemOwnerId,
        @RequestParam(defaultValue = "false") Boolean isPeer) throws Exception {

        checkSakaiSession();

        Optional<EvaluationTransferBean> optBean = rubricsService.getEvaluationForToolAndItemAndEvaluatedItemAndOwnerId(toolId, itemId, evaluatedItemId, evaluatedItemOwnerId, siteId, isPeer);
        if (optBean.isPresent()) {
            return ResponseEntity.ok(optBean.get());
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping(value = "/sites/{siteId}/rubric-evaluations/tools/{toolId}/items/{itemId}/evaluations/{evaluatedItemId}")
    ResponseEntity deleteEvaluation(@PathVariable String siteId, @PathVariable String toolId, @PathVariable String itemId, @PathVariable String evaluatedItemId) throws Exception {

        checkSakaiSession();

        if (rubricsService.deleteEvaluationForToolAndItemAndEvaluatedItemId(toolId, itemId, evaluatedItemId, siteId)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping(value = "/sites/{siteId}/rubric-evaluations/tools/{toolId}/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<EvaluationTransferBean>> getEvaluationsForItem(@PathVariable String siteId, @PathVariable String toolId, @PathVariable String itemId) throws Exception {

        checkSakaiSession();
        return ResponseEntity.ok(rubricsService.getEvaluationsForToolAndItem(toolId, itemId, siteId));
    }

    @PutMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/sort")
    ResponseEntity sortCriteria(@PathVariable String siteId, @PathVariable Long rubricId, @RequestBody List<Long> sortedIds) {

        checkSakaiSession();

        rubricsService.sortRubricCriteria(rubricId, sortedIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/sort")
    ResponseEntity sortRatings(@PathVariable String siteId, @PathVariable Long criterionId, @RequestBody List<Long> sortedIds) {

        checkSakaiSession();

        rubricsService.sortCriterionRatings(criterionId, sortedIds);
        return ResponseEntity.ok().build();
    }
	
    //@PreAuthorize("canCopy(#sourceId, 'Criterion')")
    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{sourceId}/copy", produces = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<CriterionTransferBean> copyCriterion(@PathVariable Long rubricId, @PathVariable Long sourceId) {

        checkSakaiSession();

        return entityModelForCriterionBean(rubricsService.copyCriterion(rubricId, sourceId));
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{sourceId}/copy", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> copyCriterionPost(@PathVariable String siteId, @PathVariable Long rubricId, @PathVariable Long sourceId) {

        checkSakaiSession();

        return ResponseEntity.ok(entityModelForCriterionBean(rubricsService.copyCriterion(rubricId, sourceId)));
    }

    @PatchMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}",
                    consumes = "application/json-patch+json",
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity patchCriterion(@PathVariable String siteId, @PathVariable Long criterionId, @RequestBody JsonPatch patch) throws Exception {

        checkSakaiSession();

        return rubricsService.getCriterion(criterionId, siteId).map(criterion -> {

            try {
                JsonNode patched = patch.apply(jsonMapper.convertValue(criterion, JsonNode.class));
                CriterionTransferBean patchedBean  = jsonMapper.treeToValue(patched, CriterionTransferBean.class);
                return ResponseEntity.ok(entityModelForCriterionBean(rubricsService.updateCriterion(patchedBean, siteId)));
            } catch (Exception e) {
                log.error("Failed to patch criterion", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/default", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<RatingTransferBean>> createDefaultRating(@PathVariable String siteId, @PathVariable Long rubricId, @PathVariable Long criterionId, @RequestParam Integer position) {

        checkSakaiSession();

        return rubricsService.createDefaultRating(siteId, rubricId, criterionId, position)
            .map(rating -> ResponseEntity.ok(entityModelForRatingBean(rating)))
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    //@PreAuthorize("canCopy(#sourceId, 'Rating')")
    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/{ratingId}")
    ResponseEntity saveRating(@PathVariable String siteId, @PathVariable Long rubricId, @RequestBody RatingTransferBean ratingBean) {

        checkSakaiSession();

        rubricsService.updateRating(ratingBean, siteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/{ratingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> deleteRating(@PathVariable String siteId, @PathVariable Long rubricId, @PathVariable Long criterionId, @PathVariable Long ratingId) throws Exception {

        checkSakaiSession();

        return ResponseEntity.ok(entityModelForCriterionBean(rubricsService.deleteRating(ratingId, criterionId, siteId, rubricId)));
    }

    @ResponseBody
    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable String siteId, @PathVariable Long rubricId,
        @RequestParam(required = false) String toolId, @RequestParam(required = false) String itemId,
        @RequestParam(required = false) String evaluatedItemId) throws Exception {

        checkSakaiSession();

        ContentDisposition contentDisposition = rubricsService.getRubric(rubricId).map(rubric -> {
            String filename = rubricsService.createContextualFilename(rubric, toolId, itemId, evaluatedItemId, siteId);
            filename = StringUtils.isNotBlank(filename) ? filename : "_";
            return ContentDisposition.builder("attachment").filename(String.format("%s.pdf", filename)).build();
        }).orElseThrow(() -> new IllegalArgumentException("No rubric for id " + rubricId));

        return ResponseEntity.ok().headers(h -> h.setContentDisposition(contentDisposition))
                .body(rubricsService.createPdf(siteId, rubricId, toolId, itemId, evaluatedItemId));
    }

    private static boolean nearlyEqual(Double a, Double b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return Math.abs(a - b) < 1e-6;
    }

    private EntityModel<RubricTransferBean> entityModelForRubricBean(RubricTransferBean rubricBean) {

        List<Link> links = new ArrayList<>();
        /*
        links.add(Link.of(topicBean.url, "self"));
        links.add(Link.of(topicBean.url + "/bookmarked", "bookmark"));
        links.add(Link.of(topicBean.url + "/posts/markpostsviewed", "markpostsviewed"));
        if (topicBean.canPin) links.add(Link.of(topicBean.url + "/pinned", "pin"));
        if (topicBean.canPost) links.add(Link.of(topicBean.url + "/posts", "post"));
        if (topicBean.canDelete) links.add(Link.of(topicBean.url, "delete"));
        if (topicBean.canReact) links.add(Link.of(topicBean.url + "/reactions", "react"));
        if (topicBean.canModerate) links.add(Link.of(topicBean.url + "/locked", "lock"));
        if (topicBean.canModerate) links.add(Link.of(topicBean.url + "/hidden", "hide"));
        */
        return EntityModel.of(rubricBean, links);
    }

    private EntityModel<CriterionTransferBean> entityModelForCriterionBean(CriterionTransferBean criterionBean) {

        List<Link> links = new ArrayList<>();
        return EntityModel.of(criterionBean, links);
    }

    private EntityModel<RatingTransferBean> entityModelForRatingBean(RatingTransferBean ratingBean) {

        List<Link> links = new ArrayList<>();
        return EntityModel.of(ratingBean, links);
    }
}
