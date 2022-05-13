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

package org.sakaiproject.rubrics.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.jsoup.Jsoup;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;
import org.sakaiproject.rubrics.api.model.EvaluatedItemOwnerType;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.Criterion;
import org.sakaiproject.rubrics.api.model.CriterionOutcome;
import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;
import org.sakaiproject.rubrics.api.model.ReturnedCriterionOutcome;
import org.sakaiproject.rubrics.api.model.ReturnedEvaluation;
import org.sakaiproject.rubrics.api.model.Rating;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.CriterionRepository;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RatingRepository;
import org.sakaiproject.rubrics.api.repository.ReturnedEvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Transactional
public class RubricsServiceImpl implements RubricsService, EntityProducer, EntityTransferrer {

    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL);

    private AuthzGroupService authzGroupService;
    private CriterionRepository criterionRepository;
    private EntityManager entityManager;
    private EvaluationRepository evaluationRepository;
    private EventTrackingService eventTrackingService;
    private FormattedText formattedText;
    private FunctionManager functionManager;
    private RatingRepository ratingRepository;
    private ResourceLoader resourceLoader;
    private ReturnedEvaluationRepository returnedEvaluationRepository;
    private RubricRepository rubricRepository;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private AssociationRepository associationRepository;
    private ToolManager toolManager;
    private UserDirectoryService userDirectoryService;
    private UserTimeService userTimeService;

    public void init() {

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, true);
        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_EDITOR, true);
        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, true);
    }

    public RubricTransferBean createDefaultRubric(String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit rubrics");
        }

        Rubric rubric = new Rubric();
        rubric.setOwnerId(siteId);
        rubric.setCreatorId(currentUserId);
        rubric.setTitle(resourceLoader.getString("default_rubric_title"));

        Criterion crit1 = new Criterion();
        crit1.setTitle(resourceLoader.getString("default_criterion1_title"));
        crit1.setOwnerId(siteId);

        Rating c1Rating1 = new Rating();
        c1Rating1.setTitle(resourceLoader.getString("default_c1_r1_title"));
        c1Rating1.setPoints(0D);

        Rating c1Rating2 = new Rating();
        c1Rating2.setTitle(resourceLoader.getString("default_c1_r2_title"));
        c1Rating2.setPoints(1D);

        Rating c1Rating3 = new Rating();
        c1Rating3.setTitle(resourceLoader.getString("default_c1_r3_title"));
        c1Rating3.setPoints(2D);

        List<Rating> c1Ratings = new ArrayList<>();
        c1Ratings.add(c1Rating1);
        c1Ratings.add(c1Rating2);
        c1Ratings.add(c1Rating3);
        crit1.setRatings(c1Ratings);

        Criterion crit2 = new Criterion();
        crit2.setTitle(resourceLoader.getString("default_criterion2_title"));
        crit2.setOwnerId(siteId);

        Rating c2Rating1 = new Rating();
        c2Rating1.setTitle(resourceLoader.getString("default_c2_r1_title"));
        c2Rating1.setPoints(0D);

        Rating c2Rating2 = new Rating();
        c2Rating2.setTitle(resourceLoader.getString("default_c2_r2_title"));
        c2Rating2.setPoints(5D);

        Rating c2Rating3 = new Rating();
        c2Rating3.setTitle(resourceLoader.getString("default_c2_r3_title"));
        c2Rating3.setPoints(10D);

        Rating c2Rating4 = new Rating();
        c2Rating4.setTitle(resourceLoader.getString("default_c2_r4_title"));
        c2Rating4.setPoints(15D);

        Rating c2Rating5 = new Rating();
        c2Rating5.setTitle(resourceLoader.getString("default_c2_r5_title"));
        c2Rating5.setPoints(20D);

        List<Rating> c2Ratings = new ArrayList<>();
        c2Ratings.add(c2Rating1);
        c2Ratings.add(c2Rating2);
        c2Ratings.add(c2Rating3);
        c2Ratings.add(c2Rating4);
        c2Ratings.add(c2Rating5);
        crit2.setRatings(c2Ratings);

        List<Criterion> criteria = new ArrayList<>();
        criteria.add(crit1);
        criteria.add(crit2);
        rubric.setCriteria(criteria);

        Instant now = Instant.now();
        rubric.setCreated(now);
        rubric.setModified(now);

        rubric = rubricRepository.save(rubric);
        return decorateRubricBean(RubricTransferBean.of(rubric), rubric);
    }

    public RubricTransferBean copyRubricToSite(Long rubricId, String toSiteId) {

        if (!isEditor(toSiteId)) {
            throw new SecurityException("You need to be a rubrics editor to get a site's rubrics");
        }

        return rubricRepository.findById(rubricId).map(source -> {

            Rubric copy = source.clone(toSiteId);
            Instant now = Instant.now();
            copy.setCreated(now);
            copy.setModified(now);
            copy.setCreatorId(sessionManager.getCurrentSessionUserId());
            copy.setTitle(copy.getTitle() + " " + resourceLoader.getString("copy"));
            return decorateRubricBean(RubricTransferBean.of(rubricRepository.save(copy)), copy);
        }).orElseThrow(() -> new IllegalArgumentException("No rubric with id " + rubricId));
    }

    @Transactional(readOnly = true)
    public List<RubricTransferBean> getRubricsForSite(String siteId) {

        if (!isEditor(siteId)) {
            throw new SecurityException("You need to be an editor to get a site's rubrics");
        }

        return rubricRepository.findByOwnerId(siteId).stream()
            .map(r -> decorateRubricBean(RubricTransferBean.of(r), r)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RubricTransferBean> getSharedRubrics() {

        return rubricRepository.findByShared(true).stream()
            .map(r -> decorateRubricBean(RubricTransferBean.of(r), r)).collect(Collectors.toList());
    }

    public void deleteRubric(Long rubricId) {

        // SAK-42944 removing the soft-deleted associations
        associationRepository.findByRubricId(rubricId).forEach(ass -> {
            evaluationRepository.deleteByToolItemRubricAssociation_Id(ass.getId());
        });

        associationRepository.deleteByRubricId(rubricId);

        rubricRepository.deleteById(rubricId);
    }

    private RubricTransferBean decorateRubricBean(RubricTransferBean bean, Rubric rubric) {

        bean.formattedCreatedDate = userTimeService.dateTimeFormat(bean.created, FormatStyle.MEDIUM, FormatStyle.SHORT);
        bean.formattedModifiedDate = userTimeService.dateTimeFormat(bean.modified, FormatStyle.MEDIUM, FormatStyle.SHORT);
        try {
            bean.creatorDisplayName = userDirectoryService.getUser(bean.creatorId).getDisplayName();
            bean.siteTitle = siteService.getSite(bean.ownerId).getTitle();
        } catch (Exception e) {
            log.error("Failed to set the creatorDisplayName or the siteTitle", e);
        }
        //bean.locked = rubric.getAssociations().size() > 0;
        return bean;
    }

    public CriterionTransferBean copyCriterion(Long rubricId, Long sourceId) {

        Criterion criterion = criterionRepository.findById(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("No source criterion with id " + sourceId));

        Rubric rubric = rubricRepository.findById(rubricId)
            .orElseThrow(() -> new IllegalArgumentException("No rubric with id " + rubricId));

        Criterion newCriterion = criterion.clone();
        newCriterion.setTitle(newCriterion.getTitle() + " " + resourceLoader.getString("copy"));
        newCriterion = criterionRepository.save(newCriterion);
        rubric.getCriteria().add(newCriterion);
        rubricRepository.save(rubric);
        return CriterionTransferBean.of(newCriterion);
    }

    public RatingTransferBean copyRating(Long sourceId) {
        return null;
    }

    public void sortRubricCriteria(Long rubricId, List<Long> sortedCriterionIds) {

        rubricRepository.findById(rubricId).ifPresent(rubric -> {

            Map<Long, Criterion> current = rubric.getCriteria().stream().collect(Collectors.toMap(Criterion::getId, c -> c));
            List<Criterion> sorted = sortedCriterionIds.stream().map(current::get).collect(Collectors.toList());
            rubric.getCriteria().clear();
            rubric.getCriteria().addAll(sorted);
            rubricRepository.save(rubric);
        });
    }

    public void sortCriterionRatings(Long criterionId, List<Long> sortedRatingIds) {

        criterionRepository.findById(criterionId).ifPresent(criterion -> {

            Map<Long, Rating> current = criterion.getRatings().stream().collect(Collectors.toMap(Rating::getId, r -> r));
            List<Rating> sorted = sortedRatingIds.stream().map(current::get).collect(Collectors.toList());
            criterion.getRatings().clear();
            criterion.getRatings().addAll(sorted);
            criterionRepository.save(criterion);
        });
    }

    public Optional<CriterionTransferBean> createDefaultCriterion(String siteId, Long rubricId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit criteria");
        }

        return rubricRepository.findById(rubricId).map(rubric -> {

            Criterion criterion = new Criterion();
            criterion.setOwnerId(siteId);
            criterion.setTitle(resourceLoader.getString("default_criterion_title"));

            Rating rating1 = new Rating();
            rating1.setTitle(resourceLoader.getString("default_c1_r1_title"));
            rating1.setPoints(0D);

            Rating rating2 = new Rating();
            rating2.setTitle(resourceLoader.getString("default_c1_r2_title"));
            rating2.setPoints(1D);

            Rating rating3 = new Rating();
            rating3.setTitle(resourceLoader.getString("default_c1_r3_title"));
            rating3.setPoints(2D);

            List<Rating> ratings = new ArrayList<>();
            ratings.add(rating1);
            ratings.add(rating2);
            ratings.add(rating3);

            criterion.setRatings(ratings);

            //criterion.setRubric(rubric);
            int length = rubric.getCriteria().size();
            rubric.getCriteria().add(criterion);
            rubric = rubricRepository.save(rubric);

            CriterionTransferBean bean = CriterionTransferBean.of(rubric.getCriteria().get(length));
            bean.isNew = true;
            return bean;
        });
    }

    public Optional<CriterionTransferBean> createDefaultEmptyCriterion(String siteId, Long rubricId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit criteria");
        }

        return rubricRepository.findById(rubricId).map(rubric -> {

            Criterion criterion = new Criterion();
            criterion.setOwnerId(siteId);
            criterion.setTitle(resourceLoader.getString("default_empty_criterion_title"));

            //criterion.setRubric(rubric);
            int length = rubric.getCriteria().size();
            rubric.getCriteria().add(criterion);
            rubric = rubricRepository.save(rubric);

            return CriterionTransferBean.of(rubric.getCriteria().get(length));
        });
    }

    public Optional<RatingTransferBean> createDefaultRating(String siteId, Long criterionId, int position) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit ratings");
        }

        return criterionRepository.findById(criterionId).map(criterion -> {

            Rating rating = new Rating();
            rating.setTitle(resourceLoader.getString("default_rating_title"));
            rating.setPoints(0D);
            //rating.setCriterion(criterion);

            int length = criterion.getRatings().size();
            criterion.getRatings().add(position, rating);

            criterion = criterionRepository.save(criterion);

            return RatingTransferBean.of(criterion.getRatings().get(position));
        });
    }

    public RubricTransferBean saveRubric(RubricTransferBean bean) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(bean.ownerId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit rubrics");
        }

        return RubricTransferBean.of(rubricRepository.save(bean.toRubric()));
    }

    public CriterionTransferBean saveCriterion(CriterionTransferBean bean, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit criteria");
        }

        return CriterionTransferBean.of(criterionRepository.save(bean.toCriterion()));
    }

    public void deleteCriterion(Long rubricId, Long criterionId, String siteId) {

        if (!isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to delete criteria");
        }

        Rubric rubric = rubricRepository.findById(rubricId)
            .orElseThrow(() -> new IllegalArgumentException("No rubric for id " + rubricId));

        rubric.getCriteria().removeIf(c -> c.getId().equals(criterionId));

        rubricRepository.save(rubric);
    }

    public RatingTransferBean saveRating(RatingTransferBean bean, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit ratings");
        }

        return RatingTransferBean.of(ratingRepository.save(bean.toRating()));
    }

    public CriterionTransferBean deleteRating(Long ratingId, Long criterionId, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (!isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit ratings");
        }

        return criterionRepository.findById(criterionId).map(criterion -> {

            criterion.getRatings().removeIf(r -> r.getId().equals(ratingId));
            return CriterionTransferBean.of(criterionRepository.save(criterion));
        }).orElseThrow(() -> new IllegalArgumentException());
    }

    @Transactional(readOnly = true)
    public Optional<RubricTransferBean> getRubric(Long rubricId) {

        return rubricRepository.findById(rubricId).map(rubric -> {

            String currentUserId = userDirectoryService.getCurrentUser().getId();

            if (rubric.getShared()
                || isEditor(rubric.getOwnerId())
                || isEvaluee(rubric.getOwnerId())
                || rubric.getCreatorId().equalsIgnoreCase(currentUserId)) {
                return decorateRubricBean(RubricTransferBean.of(rubric), rubric);
            } else {
                return null;
            }
        });
    }

    @Transactional(readOnly = true)
    public Optional<CriterionTransferBean> getCriterion(Long criterionId, String siteId) {

        if (!isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to get criteria");
        }

        return criterionRepository.findById(criterionId).map(CriterionTransferBean::of);
    }

    @Transactional(readOnly = true)
    public Optional<AssociationTransferBean> getAssociationForToolAndItem(String toolId, String itemId, String siteId) {

        return associationRepository.findByToolIdAndItemId(toolId, itemId).map(AssociationTransferBean::of);
    }

    @Transactional(readOnly = true)
    public Optional<EvaluationTransferBean> getEvaluation(Long evaluationId, String siteId) {

        return evaluationRepository.findById(evaluationId).map(e -> {

                String currentUserId = sessionManager.getCurrentSessionUserId();
                if (isEvaluator(siteId)
                    || (e.getStatus() == EvaluationStatus.RETURNED && e.getEvaluatedItemOwnerId().equals(currentUserId))) {
                    return EvaluationTransferBean.of(e);
                } else {
                    return null;
                }
            });
    }

    @Transactional(readOnly = true)
    public Optional<EvaluationTransferBean> getEvaluationForToolAndItemAndEvaluatedItemId(String toolId, String itemId, String evaluatedItemId, String siteId) {

        ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
            .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));

        return evaluationRepository.findByAssociationIdAndEvaluatedItemId(association.getId(), evaluatedItemId)
            .map(eval -> {

                if (canViewEvaluation(eval, siteId)) {
                    return EvaluationTransferBean.of(eval);
                } else {
                    return null;
                }
            });
    }

    public EvaluationTransferBean saveEvaluation(EvaluationTransferBean evaluationBean, String siteId) {

        if (!isEvaluator(siteId)) {
            throw new SecurityException("You must be an evaluator to evaluate rubrics");
        }

        if (evaluationBean.isNew) {
            evaluationBean.creatorId = userDirectoryService.getCurrentUser().getId();
            evaluationBean.created = Instant.now();
        }

        evaluationBean.modified = Instant.now();

        Evaluation evaluation = evaluationRepository.save(evaluationBean.toEvaluation());

        // If this evaluation has been returned, back it up.
        if (evaluation.getStatus() == EvaluationStatus.RETURNED) {

            ReturnedEvaluation returnedEvaluation
                = returnedEvaluationRepository.findByOriginalEvaluationId(evaluation.getId())
                .map(re -> {

                    re.setOverallComment(evaluation.getOverallComment());
                    Map<Long, CriterionOutcome> outcomes
                        = evaluation.getCriterionOutcomes().stream()
                            .collect(Collectors.toMap(co -> co.getCriterionId(), co -> co));
                    re.getCriterionOutcomes().forEach(rco -> rco.assign(outcomes.get(rco.getCriterionId())));
                    return re;
                }).orElseGet(() -> {

                    ReturnedEvaluation re = new ReturnedEvaluation();
                    re.setOverallComment(evaluation.getOverallComment());
                    re.setOriginalEvaluationId(evaluation.getId());
                    List<ReturnedCriterionOutcome> criterionOutcomes = new ArrayList<>();
                    evaluation.getCriterionOutcomes().forEach(co -> {

                        ReturnedCriterionOutcome rco = new ReturnedCriterionOutcome();
                        criterionOutcomes.add(rco.assign(co));
                    });
                    re.setCriterionOutcomes(criterionOutcomes);
                    return re;
                });
            returnedEvaluationRepository.save(returnedEvaluation);
        }

        return EvaluationTransferBean.of(evaluation);
    }

    public EvaluationTransferBean cancelDraftEvaluation(Long draftEvaluationId) {

        Evaluation evaluation = evaluationRepository.findById(draftEvaluationId)
            .orElseThrow(() -> new IllegalArgumentException("No evaluation for id " + draftEvaluationId));

        if (evaluation.getStatus() != EvaluationStatus.DRAFT) {
            log.info("{} is not a draft evaluation. Returning it as is.", draftEvaluationId);
            // This is not a draft. It's not cancellable.
            return EvaluationTransferBean.of(evaluation);
        }

        Optional<ReturnedEvaluation> optReturnedEvaluation
            = returnedEvaluationRepository.findByOriginalEvaluationId(draftEvaluationId);
        if (optReturnedEvaluation.isPresent()) {
            ReturnedEvaluation returnedEvaluation = optReturnedEvaluation.get();
            evaluation.setOverallComment(returnedEvaluation.getOverallComment());
            Map<Long, ReturnedCriterionOutcome> returnedOutcomes
                = returnedEvaluation.getCriterionOutcomes().stream()
                    .collect(Collectors.toMap(rco -> rco.getCriterionId(), rco -> rco));
            evaluation.getCriterionOutcomes().forEach(co ->co.assign(returnedOutcomes.get(co.getCriterionId())));
            evaluation.setStatus(EvaluationStatus.RETURNED);
            return EvaluationTransferBean.of(evaluationRepository.save(evaluation));
        } else {
            evaluationRepository.deleteById(draftEvaluationId);
            return EvaluationTransferBean.of(new Evaluation());
        }
    }

    public boolean hasAssociatedRubric(String tool, String id) {
        return hasAssociatedRubric(tool, id, toolManager.getCurrentPlacement().getContext());
    }

    public boolean hasAssociatedRubric(String tool, String id, String siteId ) {

        if (StringUtils.isBlank(id)) return false;

        return getRubricAssociation(tool, id).isPresent();
    }

    public Optional<ToolItemRubricAssociation> saveRubricAssociation(String toolId, String toolItemId, final Map<String, String> params) {

        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("No association params supplied");
        }

        String siteId = toolManager.getCurrentPlacement().getContext();

        String associationId = null;
        String created = "";
        String owner = "";
        String ownerType = "";
        String creatorId = "";
        String oldRubricId = null;
        Map <String,Boolean> oldParams = new HashMap<>();

        try {
            Optional<ToolItemRubricAssociation> optAssociation = getRubricAssociation(toolId, toolItemId);
            ToolItemRubricAssociation association = null;
            if (optAssociation.isPresent()) {
                association = optAssociation.get();
                created = association.getCreated().toString();
                owner = association.getSiteId();
                creatorId = association.getCreatorId();
                oldParams = association.getParameters();
                oldRubricId = association.getRubricId().toString();
            }

            String rubricId = params.get(RubricsConstants.RBCS_LIST);

            boolean rubricSwitch = oldRubricId == null ? false : !StringUtils.equals(rubricId, oldRubricId);

            String nowTime = LocalDateTime.now().toString();
            if (params.get(RubricsConstants.RBCS_ASSOCIATE).equals("1")) {

                if (!optAssociation.isPresent() || rubricSwitch) {
                    // No existing association or we're switching rubrics

                    if (rubricSwitch) {
                        // We're switching, deactivate the current association
                        setAssociationActive(association, false, toolId);
                    }

                    // See if there's already an association for the requested rubric and reuse that.
                    Optional<ToolItemRubricAssociation> optionalExisting = findAssociationByItemIdAndRubricId(toolItemId, Long.parseLong(rubricId), toolId, null);
                    if (optionalExisting.isPresent()) {
                        return Optional.of(setAssociationActive(optionalExisting.get(), true, toolId));
                    } else {
                        ToolItemRubricAssociation newAssociation = new ToolItemRubricAssociation();
                        newAssociation.setToolId(toolId);
                        newAssociation.setItemId(toolItemId);
                        newAssociation.setRubricId(Long.parseLong(rubricId));
                        newAssociation.setActive(true);
                        LocalDateTime now = LocalDateTime.now();
                        newAssociation.setCreated(now);
                        newAssociation.setModified(now);
                        newAssociation.setSiteId(siteId);
                        newAssociation.setCreatorId(userDirectoryService.getCurrentUser().getId());
                        newAssociation.setParameters(setConfigurationParameters(params, oldParams));

                        return Optional.of(associationRepository.save(newAssociation));
                    }
                } else {
                    // We're updating an existing association, not the rubric though.
                    association.setParameters(setConfigurationParameters(params, oldParams));
                    association.setModified(LocalDateTime.now());
                    return Optional.of(associationRepository.save(association));
                }
            } else {
                if (optAssociation.isPresent()) {
                    setAssociationActive(association, false, toolId);
                }
                return Optional.empty();
            }
        } catch (Exception e) {

            log.error("Failed to save association", e);
            return Optional.empty();
        }
    }

    private Optional<ToolItemRubricAssociation> findAssociationByItemIdAndRubricId(String toolItemId, Long rubricId, String toolId, String siteId) {

        return associationRepository.findByItemIdAndRubricId(toolItemId, rubricId).map(assoc -> {

            String currentUserId = userDirectoryService.getCurrentUser().getId();

            if (securityService.unlock(currentUserId, RubricsConstants.RBCS_PERMISSIONS_EDITOR, "/site/" + siteId)
                || assoc.getCreatorId().equalsIgnoreCase(currentUserId)) {
                return assoc;
            } else {
                return null;
            }
        });
    }

    /**
     * Prepare the association params in json format
     * @param params the full list of rubrics params coming from the component
     * @return
     */

    private Map<String, Boolean> setConfigurationParameters(Map<String, String> params, Map<String,Boolean> oldParams ){

        Map<String, Boolean> merged = new HashMap<>();

        //Get the parameters
        params.entrySet().forEach(entry -> {

            String name = entry.getKey();
            if (name.startsWith(RubricsConstants.RBCS_CONFIG)) {
                Boolean value = Boolean.FALSE;
                if ((entry.getValue() != null) && (entry.getValue().equals("1"))) {
                    value = Boolean.TRUE;
                }
                merged.put(name.substring(12), value);
            }
        });

        for (String name : oldParams.keySet()) {
            if (!(params.containsKey(RubricsConstants.RBCS_CONFIG + name))) {
                merged.put(name, Boolean.FALSE);
            }
        }
        return merged;
    }

    /**
     * Returns the ToolItemRubricAssociation resource for the given tool and associated item ID, wrapped as an Optional.
     * @param toolId the tool id, something like "sakai.assignment"
     * @param associatedToolItemId the id of the associated element within the tool
     * @return
     */
    @Transactional(readOnly = true)
    public Optional<ToolItemRubricAssociation> getRubricAssociation(String toolId, String associatedToolItemId) {

        return associationRepository.findByToolIdAndItemId(toolId, associatedToolItemId).map(assoc -> {

            String currentUserId = userDirectoryService.getCurrentUser().getId();

            if (isEditor(assoc.getSiteId()) || assoc.getCreatorId().equalsIgnoreCase(currentUserId)) {
                return assoc;
            } else {
                return null;
            }
        });
    }

    @Transactional(readOnly = true)
    public String getRubricEvaluationObjectId(String itemId, String userId, String toolId, String siteId) {

        ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
            .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));

        Optional<Evaluation> optEvaluation = evaluationRepository.findByAssociationIdAndUserId(association.getId(), userId);

        if (optEvaluation.isPresent() && canViewEvaluation(optEvaluation.get(), siteId)) {
            return optEvaluation.get().getEvaluatedItemId();
        } else {
            return null;
        }
    }

    /**
     * Delete all the rubric associations starting with itemId.
     * @param itemId The formatted item id.
     */
    public void deleteRubricAssociationsByItemIdPrefix(String itemId, String toolId) {

        associationRepository.findByItemIdPrefix(toolId, itemId).forEach(assoc -> {

            if (securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteService.siteReference(assoc.getSiteId()))) {
                try {
                    evaluationRepository.deleteByToolItemRubricAssociation_Id(assoc.getId());
                } catch (Exception e) {
                    log.warn("Error deleting rubric association for id {} : {}", itemId, e.toString());
                }
            }
        });
    }

    public ToolItemRubricAssociation setAssociationActive(ToolItemRubricAssociation association, boolean active, String toolId) {

        association.setActive(active);
        return associationRepository.save(association);
    }

    public void softDeleteRubricAssociationsByItemIdPrefix(String itemId, String toolId) {

        associationRepository.findByItemIdPrefix(toolId, itemId).forEach(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, true);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error soft deleting rubric association for item id prefix {} : {}", itemId, e.toString());
            }
        });
    }

    public void restoreRubricAssociation(String toolId, String itemId) {

        associationRepository.findByToolIdAndItemId(toolId, itemId).ifPresent(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, false);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error restoring rubric association for item id {} : {}", itemId, e.toString());
            }
        });
    }

    public void restoreRubricAssociationsByItemIdPrefix(String itemId, String toolId) {

        associationRepository.findByItemIdPrefix(toolId, itemId).forEach(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, false);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error soft deleting rubric association for item id prefix {} : {}", itemId, e.toString());
            }
        });
    }

    private void deleteRubricEvaluationsForAssociation(Long associationId, String tool){
        evaluationRepository.deleteByToolItemRubricAssociation_Id(associationId);
    }

    public void softDeleteRubricAssociation(String toolId, String id){

        getRubricAssociation(toolId, id).ifPresent(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, true);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error soft deleting rubric association for tool {} and id {} : {}", toolId, id, e.toString());
            }
        });
    }

    public void deleteRubricAssociation(String tool, String id) {

        try {
            Optional<ToolItemRubricAssociation> optAssociation = getRubricAssociation(tool, id);
            getRubricAssociation(tool, id).ifPresent(assoc -> {
                evaluationRepository.deleteByToolItemRubricAssociation_Id(assoc.getId());
                associationRepository.delete(assoc);
            });
        } catch (Exception e) {
            log.warn("Error deleting rubric association for tool {} and id {} : {}", tool, id, e.toString());
        }
    }

    @Transactional(readOnly = true)
    public byte[] createPdf(String siteId, Long rubricId, String toolId, String itemId, String evaluatedItemId)
            throws IOException {

        if (!isEvaluator(siteId) && !isEvaluee(siteId)) {
            throw new SecurityException("You must be either an evaluator or evaluee to create PDFs");
        }

        Rubric rubric = rubricRepository.findById(rubricId)
            .orElseThrow(() -> new IllegalArgumentException("No rubric for id " + rubricId));

        Optional<Evaluation> optEvaluation = Optional.empty();
        if (toolId != null && itemId != null && evaluatedItemId != null) {
            ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));
            optEvaluation
                = evaluationRepository.findByAssociationIdAndEvaluatedItemId(association.getId(), evaluatedItemId);
        }

        // Count points
        Double points = Double.valueOf(0);
        String studentName = "";
        boolean showEvaluated = optEvaluation.isPresent() && canViewEvaluation(optEvaluation.get(), siteId);

        if (showEvaluated) {

            Evaluation eval = optEvaluation.get();
            points = eval.getCriterionOutcomes().stream().mapToDouble(x -> x.getPoints()).sum();
            try {
                studentName = userDirectoryService.getUser(eval.getEvaluatedItemOwnerId()).getDisplayName();
            } catch (UserNotDefinedException ex) {
                log.error("No user for id {} : {}", eval.getEvaluatedItemOwnerId(), ex.toString());
            }
        }

        // Create pdf document
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        PdfPTable table = new PdfPTable(1);
        PdfPCell header = new PdfPCell();

        Paragraph paragraph = new Paragraph(resourceLoader.getFormattedMessage("export_rubric_title", rubric.getTitle() + "\n"), BOLD_FONT);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        try {
            String siteTitle = siteService.getSite(rubric.getOwnerId()).getTitle();
            paragraph.add(resourceLoader.getFormattedMessage("export_rubric_site", siteTitle));
            paragraph.add(Chunk.NEWLINE);
        } catch (IdUnusedException ex) {
            log.error("No site for id {}", rubric.getOwnerId());
        }
        if (StringUtils.isNotBlank(studentName)) {
            paragraph.add(resourceLoader.getFormattedMessage("export_rubric_student", studentName));
            paragraph.add(Chunk.NEWLINE);
        }
        String exportDate = resourceLoader.getFormattedMessage("export_rubric_date", DateFormat.getDateInstance(DateFormat.LONG, resourceLoader.getLocale()).format(new Date()) + "\n");
        paragraph.add(exportDate);
        header.setBackgroundColor(Color.LIGHT_GRAY);

        if (optEvaluation.isPresent()) {
            paragraph.add(resourceLoader.getFormattedMessage("export_total_points", points));
            paragraph.add(Chunk.NEWLINE);
        }
        paragraph.add(Chunk.NEWLINE);
        header.addElement(paragraph);
        table.addCell(header);
        table.completeRow();
        document.add(table);

        for (Criterion cri : rubric.getCriteria()) {
            PdfPCell criterionCell = new PdfPCell();
            PdfPTable criterionTable = new PdfPTable(cri.getRatings().size() + 1);
            Paragraph criterionParagraph = new Paragraph();
            criterionParagraph.setFont(BOLD_FONT);
            boolean isCriterionGroup = cri.getRatings().size() <= 0;

            if (showEvaluated && !isCriterionGroup) {
                Optional<Double> evaluatedPoints = this.getCriterionPoints(cri, optEvaluation);

                //Get CriteriumOutcome as Optional for current criterium as an optional by matching associated criterion ids
                Optional<CriterionOutcome> optCriterionOutcome = optEvaluation.get().getCriterionOutcomes().stream()
                    .filter(outcome -> cri.getId().equals(outcome.getCriterionId())).findFirst();

                if (evaluatedPoints.isPresent()) {
                    if (optCriterionOutcome.get().getPointsAdjusted()) {

                        //Get points of the selected rating (not altered) by maching selected rating id's and getting points from matched rating
                        Double selectedRatingOriginalPoints = optCriterionOutcome.flatMap(outcome -> {
                            return cri.getRatings().stream().filter(rating -> rating.getId().equals(outcome.getSelectedRatingId())).findAny();
                        }).map(rating -> rating.getPoints()).orElse(0D);

                        //Instrucor adjusted the rating point value on evaluation
                        //Chunk for the points we want to display as original
                        Chunk originalPoints = new Chunk(selectedRatingOriginalPoints.toString());
                        originalPoints.getFont().setStyle(Font.STRIKETHRU);
                        //Construct Phrase containing both point values
                        Phrase pointsPhrase = new Phrase();
                        pointsPhrase.add(originalPoints);
                        pointsPhrase.add(" ");
                        pointsPhrase.add(evaluatedPoints.get().toString());
                        //Split message from resourceloader where we want to inset the poit values a Phrase
                        String message;
                        if (rubric.getWeighted()) {
                            message = resourceLoader.getFormattedMessage("export_rubrics_weight", cri.getTitle(), "{1}", cri.getWeight());
                        } else {
                            message = resourceLoader.getFormattedMessage("export_rubrics_points", cri.getTitle());
                        }
                        //Map strings to phrases, insert the point value phrase and add all to the criterions Paragraph
                        criterionParagraph.add(new Phrase(StringUtils.substringBefore(message, "{1}")));
                        criterionParagraph.add(pointsPhrase);
                        criterionParagraph.add(new Phrase(StringUtils.substringAfter(message, "{1}")));
                    } else {
                        if (rubric.getWeighted()) {
                            criterionParagraph.add(resourceLoader.getFormattedMessage("export_rubrics_weight", cri.getTitle(), evaluatedPoints.get().toString(), cri.getWeight()));
                        } else {
                            criterionParagraph.add(resourceLoader.getFormattedMessage("export_rubrics_points", cri.getTitle(), evaluatedPoints.get().toString()));
                        }
                    }
                }
            } else {
                //A rubric that is not graded (PDF export from within rubrics tool) or a criterion group
                //Just display title of criterion without points
                if (rubric.getWeighted() && !isCriterionGroup) {
                    criterionParagraph.add(String.format("%s (%s%%)", cri.getTitle(), cri.getWeight()));
                } else {
                    criterionParagraph.add(cri.getTitle());
                }
                if (isCriterionGroup) {
                    criterionCell.setBackgroundColor(Color.LIGHT_GRAY);
                }
            }
            criterionParagraph.setFont(BOLD_FONT);
            if (StringUtils.isNotBlank(cri.getDescription())) {
                criterionParagraph.add(Chunk.NEWLINE);
                criterionParagraph.add(new Paragraph(formattedText.stripHtmlFromText(cri.getDescription(), true), NORMAL_FONT));
            }
            criterionCell.addElement(criterionParagraph);

            criterionTable.addCell(criterionCell);
            for (Rating rating : cri.getRatings()) {
                Paragraph ratingsParagraph = new Paragraph("", BOLD_FONT);
                String ratingPoints = resourceLoader.getFormattedMessage("export_rubrics_points", rating.getTitle(), rating.getPoints());
                ratingsParagraph.add(ratingPoints);
                ratingsParagraph.add(Chunk.NEWLINE);
                Paragraph ratingsDesc = new Paragraph("", NORMAL_FONT);

                if (StringUtils.isNotEmpty(rating.getDescription())) {
                    ratingsDesc.add(rating.getDescription() + "\n");
                }
                ratingsParagraph.add(ratingsDesc);

                PdfPCell newCell = new PdfPCell();
                if (optEvaluation.isPresent()) {
                    for (CriterionOutcome outcome : optEvaluation.get().getCriterionOutcomes()) {
                        if (cri.getId().equals(outcome.getCriterionId()) && rating.getId().equals(outcome.getSelectedRatingId())) {
                            newCell.setBackgroundColor(Color.LIGHT_GRAY);
                            if (outcome.getComments() != null && !outcome.getComments().isEmpty()) {
                                ratingsParagraph.add(Chunk.NEWLINE);
                                ratingsParagraph.add(resourceLoader.getFormattedMessage("export_comments", Jsoup.parse(outcome.getComments()).text() + "\n"));
                            }
                        }
                    }
                }
                newCell.addElement(ratingsParagraph);
                criterionTable.addCell(newCell);
            }

            criterionTable.completeRow();
            document.add(criterionTable);
        }

        document.close();
        return out.toByteArray();
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {

        Map<String, String> traversalMap = new HashMap<>();
        rubricRepository.findByOwnerId(fromContext).forEach(rubric -> {

            try {
                Rubric clone = rubric.clone(toContext);
                clone = rubricRepository.save(clone);
                traversalMap.put(RubricsConstants.RBCS_PREFIX + rubric.getId(), RubricsConstants.RBCS_PREFIX + clone.getId());
            } catch (Exception e) {
                log.error("Failed to clone rubric into new site", e);
            }
        });
        return traversalMap;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

        if (cleanup){
            deleteSiteRubrics(toContext);
        }
        return transferCopyEntities(fromContext, toContext, ids, null);
    }

    @Override
    public String[] myToolIds() {
        return new String[] { RubricsConstants.RBCS_TOOL };
    }

    @Override
    public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {

        if (transversalMap != null && !transversalMap.isEmpty()) {
            for (Map.Entry<String, String> entry : transversalMap.entrySet()) {
                String key = entry.getKey();
                //1 get all the rubrics from map
                if (key.startsWith(RubricsConstants.RBCS_PREFIX)) {
                    try {
                        //2 for each, get its associations
                        Long rubricId = Long.parseLong(key.substring(RubricsConstants.RBCS_PREFIX.length()));
                        associationRepository.findByRubricId(rubricId).forEach(association -> {

                            //2b get association params
                            Map<String,Boolean> originalParams = association.getParameters();

                            String tool = association.getToolId();
                            String itemId = association.getItemId();
                            String newItemId = null;
                            //3 association type
                            if (RubricsConstants.RBCS_TOOL_ASSIGNMENT.equals(tool)){
                                //3a if assignments
                                log.debug("Handling Rubrics association transfer for Assignment entry " + itemId);
                                if(transversalMap.get("assignment/"+itemId) != null){
                                    newItemId = transversalMap.get("assignment/"+itemId).substring("assignment/".length());
                                }
                            } else if (RubricsConstants.RBCS_TOOL_SAMIGO.equals(tool)){
                                //3b if samigo
                                if(itemId.startsWith(RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX)){
                                    log.debug("Skipping published item {}", itemId);
                                }
                                log.debug("Handling Rubrics association transfer for Samigo entry " + itemId);
                                if(transversalMap.get("sam_item/"+itemId) != null){
                                    newItemId = transversalMap.get("sam_item/"+itemId).substring("sam_item/".length());
                                }
                            } else if (RubricsConstants.RBCS_TOOL_FORUMS.equals(tool)){
                                //3c if forums
                                newItemId = itemId.substring(0, 4);
                                String strippedId = itemId.substring(4);//every forum prefix have this size
                                log.debug("Handling Rubrics association transfer for Forums entry " + strippedId);
                                if(RubricsConstants.RBCS_FORUM_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum/"+strippedId) != null){
                                    newItemId += transversalMap.get("forum/"+strippedId).substring("forum/".length());
                                } else if(RubricsConstants.RBCS_TOPIC_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum_topic/"+strippedId) != null){
                                    newItemId += transversalMap.get("forum_topic/"+strippedId).substring("forum_topic/".length());
                                } else {
                                    log.debug("Not found updated id for item {}", itemId);
                                }
                            } else if (RubricsConstants.RBCS_TOOL_GRADEBOOKNG.equals(tool)){
                                //3d if gradebook
                                log.debug("Handling Rubrics association transfer for Gradebook entry " + itemId);
                                if(transversalMap.get("gb/"+itemId) != null){
                                    newItemId = transversalMap.get("gb/"+itemId).substring("gb/".length());
                                }
                            } else {
                                log.warn("Unhandled tool for Rubrics transfer between sites");
                            }

                            //4 save new association
                            if (newItemId != null){
                                try {
                                    ToolItemRubricAssociation newAssociation = new ToolItemRubricAssociation();
                                    newAssociation.setToolId(tool);
                                    newAssociation.setItemId(newItemId);
                                    Long newRubricId = Long.parseLong(entry.getValue().substring(RubricsConstants.RBCS_PREFIX.length()));
                                    newAssociation.setRubricId(newRubricId);
                                    newAssociation.setParameters(originalParams);
                                    associationRepository.save(newAssociation);
                                } catch (Exception exc){
                                    log.error("Error while trying to save new association with item it {} : {}", newItemId, exc.toString());
                                }
                            }
                        });
                    } catch (Exception ex){
                        log.error("Error while trying to update association for Rubric {} : {}", key, ex.toString());
                    }
                }
            }
        }
    }

    @Override
    public boolean parseEntityReference(String reference, Reference ref) {
        return reference.startsWith(REFERENCE_ROOT);
    }
    protected List<ToolItemRubricAssociation> getRubricAssociationByRubric(Long rubricId, String toSite) {
        return associationRepository.findByRubricId(rubricId);
    }

    public void deleteSiteRubrics(String siteId) {

        if (!isEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to delete a site's rubrics");
        }

        associationRepository.deleteBySiteId(siteId);
        rubricRepository.deleteByOwnerId(siteId);
    }

    private Optional<Double> getCriterionPoints(Criterion cri, Optional<Evaluation> optEvaluation) {

        if (!optEvaluation.isPresent()) return Optional.empty();

        Double points = Double.valueOf(0);
        List<Rating> ratingList = cri.getRatings();
        for (Rating rating : ratingList) {
            for (CriterionOutcome outcome : optEvaluation.get().getCriterionOutcomes()) {
                if (cri.getId().equals(outcome.getCriterionId())
                        && rating.getId().equals(outcome.getSelectedRatingId())) {
                    points = points + outcome.getPoints();
                }
            }
        }

        return Optional.of(points);
    }

    private boolean isEditor(String siteId) {

        return securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteService.siteReference(siteId));
    }

    private boolean isEvaluator(String siteId) {

        String siteRef = siteService.siteReference(siteId);
        return securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, siteRef);
    }

    private boolean isEvaluee(String siteId) {

        String siteRef = siteService.siteReference(siteId);
        return securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, siteRef);
    }

    private boolean canViewEvaluation(Evaluation eval, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (isEvaluator(siteId)) {
            return true;
        }

        if (isEvaluee(siteId)) {
            if (eval.getEvaluatedItemOwnerType() == EvaluatedItemOwnerType.USER && currentUserId.equals(eval.getEvaluatedItemOwnerId())) {
                return true;
            }
            if (eval.getEvaluatedItemOwnerType() == EvaluatedItemOwnerType.GROUP) {
                return authzGroupService.getUserRole(currentUserId, eval.getEvaluatedItemOwnerId()) != null;
            }
        }

        return false;
    }
}
