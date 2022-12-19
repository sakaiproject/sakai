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

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.tool.api.SessionManager;
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
import com.fasterxml.jackson.databind.json.JsonMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.fge.jsonpatch.JsonPatch;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RubricsRestController extends AbstractSakaiApiController {

    @Autowired
    private RubricsService rubricsService;

    @Autowired
    private SessionManager sessionManager;

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

    @GetMapping(value = "/sites/{siteId}/rubrics/default", produces = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<RubricTransferBean> getDefaultRubric(@PathVariable String siteId) {

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

    @PatchMapping(value = "/sites/{siteId}/rubrics/{rubricId}", consumes = "application/json-patch+json")
    public ResponseEntity patchRubric(@PathVariable Long rubricId, @RequestBody JsonPatch patch) throws Exception {

        checkSakaiSession();

        return rubricsService.getRubric(rubricId).map(rubric -> {

            ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

            try {
                JsonNode patched = patch.apply(objectMapper.convertValue(rubric, JsonNode.class));
                RubricTransferBean patchedBean  = objectMapper.treeToValue(patched, RubricTransferBean.class);
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

        rubricsService.deleteRubric(rubricId);
        return ResponseEntity.ok().build();
	}

    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<RubricTransferBean>> getRubric(@PathVariable String siteId, @PathVariable Long rubricId) throws Exception {

        checkSakaiSession();

        return rubricsService.getRubric(rubricId)
            .map(b -> ResponseEntity.ok(entityModelForRubricBean(b)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/default", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> getDefaultCriterion(@PathVariable String siteId, @PathVariable Long rubricId) {

        checkSakaiSession();

        return rubricsService.createDefaultCriterion(siteId, rubricId)
            .map(criterion -> ResponseEntity.ok(entityModelForCriterionBean(criterion)))
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/defaultEmpty", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> getDefaultEmptyCriterion(@PathVariable String siteId, @PathVariable Long rubricId) {

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
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/sites/{siteId}/rubric-evaluations/{evaluationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EvaluationTransferBean> getEvaluation(@PathVariable String siteId, @PathVariable Long evaluationId) throws Exception {

        checkSakaiSession();

        Optional<EvaluationTransferBean> optBean = rubricsService.getEvaluation(evaluationId, siteId);
        if (optBean.isPresent()) {
            return ResponseEntity.ok(optBean.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/sites/{siteId}/rubric-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EvaluationTransferBean> createEvaluation(@PathVariable String siteId, @RequestBody EvaluationTransferBean bean) throws Exception {

        bean.setNew(true);
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

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                JsonNode patched = patch.apply(objectMapper.convertValue(evaluation, JsonNode.class));
                EvaluationTransferBean patchedBean  = objectMapper.treeToValue(patched, EvaluationTransferBean.class);
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

    @GetMapping(value = "/sites/{siteId}/rubric-evaluations/tools/{toolId}/items/{itemId}/evaluations/{evaluatedItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EvaluationTransferBean> getEvaluation(@PathVariable String siteId, @PathVariable String toolId, @PathVariable String itemId, @PathVariable String evaluatedItemId) throws Exception {

        checkSakaiSession();

        Optional<EvaluationTransferBean> optBean = rubricsService.getEvaluationForToolAndItemAndEvaluatedItemId(toolId, itemId, evaluatedItemId, siteId);
        if (optBean.isPresent()) {
            return ResponseEntity.ok(optBean.get());
        } else {
            return ResponseEntity.notFound().build();
        }
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

    @PatchMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}",
                    consumes = "application/json-patch+json",
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity patchCriterion(@PathVariable String siteId, @PathVariable Long criterionId, @RequestBody JsonPatch patch) throws Exception {

        checkSakaiSession();

        return rubricsService.getCriterion(criterionId, siteId).map(criterion -> {

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                JsonNode patched = patch.apply(objectMapper.convertValue(criterion, JsonNode.class));
                CriterionTransferBean patchedBean  = objectMapper.treeToValue(patched, CriterionTransferBean.class);
                return ResponseEntity.ok(entityModelForCriterionBean(rubricsService.updateCriterion(patchedBean, siteId)));
            } catch (Exception e) {
                log.error("Failed to patch criterion", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/default", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<RatingTransferBean>> getDefaultRating(@PathVariable String siteId, @PathVariable Long criterionId, @RequestParam Integer position) {

        checkSakaiSession();

        return rubricsService.createDefaultRating(siteId, criterionId, position)
            .map(rating -> ResponseEntity.ok(entityModelForRatingBean(rating)))
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    //@PreAuthorize("canCopy(#sourceId, 'Rating')")
    @PostMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/{ratingId}")
    ResponseEntity saveRating(@PathVariable String siteId, @RequestBody RatingTransferBean ratingBean) {

        checkSakaiSession();

        rubricsService.updateRating(ratingBean, siteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/sites/{siteId}/rubrics/{rubricId}/criteria/{criterionId}/ratings/{ratingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<CriterionTransferBean>> deleteRating(@PathVariable String siteId, @PathVariable Long criterionId, @PathVariable Long ratingId) throws Exception {

        checkSakaiSession();

        return ResponseEntity.ok(entityModelForCriterionBean(rubricsService.deleteRating(ratingId, criterionId, siteId)));
    }

    @ResponseBody
    @GetMapping(value = "/sites/{siteId}/rubrics/{rubricId}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable String siteId, @PathVariable Long rubricId,
        @RequestParam(required = false) String toolId, @RequestParam(required = false) String itemId,
        @RequestParam(required = false) String evaluatedItemId) throws Exception {

        checkSakaiSession();

        ContentDisposition contentDisposition = rubricsService.getRubric(rubricId).map(rubric -> {
            String filename = StringUtils.trimToEmpty(rubric.getTitle()).replace(".", "_");
            filename = StringUtils.isNotBlank(filename) ? filename : "_";
            return ContentDisposition.builder("attachment").filename(String.format("%s.pdf", filename)).build();
        }).orElseThrow(() -> new IllegalArgumentException("No rubric for id " + rubricId));

        return ResponseEntity.ok().headers(h -> h.setContentDisposition(contentDisposition))
                .body(rubricsService.createPdf(siteId, rubricId, toolId, itemId, evaluatedItemId));
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
