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

import static org.sakaiproject.rubrics.api.RubricsConstants.RBCS_CONFIG;
import static org.sakaiproject.rubrics.api.RubricsConstants.RBCS_MULTIPLE_OPTIONS_CONFIG;
import static org.sakaiproject.rubrics.api.RubricsConstants.RBCS_PREFIX;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionOutcomeTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.Criterion;
import org.sakaiproject.rubrics.api.model.CriterionOutcome;
import org.sakaiproject.rubrics.api.model.EvaluatedItemOwnerType;
import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;
import org.sakaiproject.rubrics.api.model.Rating;
import org.sakaiproject.rubrics.api.model.ReturnedCriterionOutcome;
import org.sakaiproject.rubrics.api.model.ReturnedEvaluation;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.CriterionRepository;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RatingRepository;
import org.sakaiproject.rubrics.api.repository.ReturnedEvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Transactional
public class RubricsServiceImpl implements RubricsService, EntityProducer, EntityTransferrer {

    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL);

    private static Predicate<ToolItemRubricAssociation> canEdit;
    private static Predicate<ToolItemRubricAssociation> canEvaluate;
    private static Predicate<ToolItemRubricAssociation> isCreator;

    private AssignmentService assignmentService;
    private AuthzGroupService authzGroupService;
    private CriterionRepository criterionRepository;
    private EntityManager entityManager;
    private EvaluationRepository evaluationRepository;
    private EventTrackingService eventTrackingService;
    private FormattedText formattedText;
    private FunctionManager functionManager;
    private GradingService gradingService;
    @Setter private PersistenceService assessmentPersistenceService;
    @Setter private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueriesAPI;
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

        canEdit = tira -> isCurrentUserEditor(tira.getRubric().getOwnerId());
        canEvaluate = tira -> isEvaluator(tira.getRubric().getOwnerId());
        isCreator = tira -> tira.getCreatorId().equalsIgnoreCase(sessionManager.getCurrentSessionUserId());

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, true);
        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_EDITOR, true);
        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, true);
        functionManager.registerFunction(RubricsConstants.RBCS_PERMISSIONS_MANAGER_VIEW, true);

        setPublishedAssessmentFacadeQueriesAPI(assessmentPersistenceService.getPublishedAssessmentFacadeQueries());
    }

    @Override
    public RubricTransferBean createDefaultRubric(String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit rubrics");
        }

        Rubric rubric = new Rubric();
        rubric.setOwnerId(siteId);
        rubric.setCreatorId(currentUserId);
        rubric.setTitle(resourceLoader.getString("default_rubric_title"));

        Criterion crit1 = new Criterion();
        crit1.setTitle(resourceLoader.getString("default_criterion1_title"));
        crit1.setRubric(rubric);

        Rating c1Rating1 = new Rating();
        c1Rating1.setTitle(resourceLoader.getString("default_c1_r1_title"));
        c1Rating1.setPoints(0D);
        c1Rating1.setCriterion(crit1);

        Rating c1Rating2 = new Rating();
        c1Rating2.setTitle(resourceLoader.getString("default_c1_r2_title"));
        c1Rating2.setPoints(1D);
        c1Rating2.setCriterion(crit1);

        Rating c1Rating3 = new Rating();
        c1Rating3.setTitle(resourceLoader.getString("default_c1_r3_title"));
        c1Rating3.setPoints(2D);
        c1Rating3.setCriterion(crit1);

        List<Rating> c1Ratings = crit1.getRatings();
        c1Ratings.add(c1Rating1);
        c1Ratings.add(c1Rating2);
        c1Ratings.add(c1Rating3);

        Criterion crit2 = new Criterion();
        crit2.setTitle(resourceLoader.getString("default_criterion2_title"));
        crit2.setRubric(rubric);

        Rating c2Rating1 = new Rating();
        c2Rating1.setTitle(resourceLoader.getString("default_c2_r1_title"));
        c2Rating1.setPoints(0D);
        c2Rating1.setCriterion(crit2);

        Rating c2Rating2 = new Rating();
        c2Rating2.setTitle(resourceLoader.getString("default_c2_r2_title"));
        c2Rating2.setPoints(5D);
        c2Rating2.setCriterion(crit2);

        Rating c2Rating3 = new Rating();
        c2Rating3.setTitle(resourceLoader.getString("default_c2_r3_title"));
        c2Rating3.setPoints(10D);
        c2Rating3.setCriterion(crit2);

        Rating c2Rating4 = new Rating();
        c2Rating4.setTitle(resourceLoader.getString("default_c2_r4_title"));
        c2Rating4.setPoints(15D);
        c2Rating4.setCriterion(crit2);

        Rating c2Rating5 = new Rating();
        c2Rating5.setTitle(resourceLoader.getString("default_c2_r5_title"));
        c2Rating5.setPoints(20D);
        c2Rating5.setCriterion(crit2);

        List<Rating> c2Ratings = crit2.getRatings();
        c2Ratings.add(c2Rating1);
        c2Ratings.add(c2Rating2);
        c2Ratings.add(c2Rating3);
        c2Ratings.add(c2Rating4);
        c2Ratings.add(c2Rating5);

        List<Criterion> criteria = rubric.getCriteria();
        criteria.add(crit1);
        criteria.add(crit2);

        Instant now = Instant.now();
        rubric.setCreated(now);
        rubric.setModified(now);

        Rubric savedRubric = rubricRepository.save(updateRubricMaxPoints(rubric));
        return decorateRubricBean(new RubricTransferBean(savedRubric));
    }

    public RubricTransferBean copyRubricToSite(Long rubricId, String toSiteId) {

        if (!isCurrentUserEditor(toSiteId)) {
            throw new SecurityException("You need to be a rubrics editor to get a site's rubrics");
        }

        return rubricRepository.findById(rubricId).map(source -> {

            Rubric copy = source.clone(toSiteId);
            Instant now = Instant.now();
            copy.setCreated(now);
            copy.setModified(now);
            copy.setCreatorId(sessionManager.getCurrentSessionUserId());
            copy.setTitle(copy.getTitle() + " " + resourceLoader.getString("copy"));
            return decorateRubricBean(new RubricTransferBean(rubricRepository.save(copy)));
        }).orElseThrow(() -> new IllegalArgumentException("No rubric with id: " + rubricId));
    }

    @Transactional(readOnly = true)
    public List<RubricTransferBean> getRubricsForSite(String siteId) {

        if (!isCurrentUserEditor(siteId)) {
            throw new SecurityException("You need to be an editor to get a site's rubrics");
        }

        return rubricRepository.findByOwnerId(siteId).stream()
            .filter(r -> r.getAdhoc() == null || !r.getAdhoc())
            .map(r -> decorateRubricBean(new RubricTransferBean(r))).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RubricTransferBean> getSharedRubrics() {

        return rubricRepository.findByShared(true).stream()
            .map(r -> decorateRubricBean(new RubricTransferBean(r))).collect(Collectors.toList());
    }

    public boolean deleteRubric(Long rubricId) {

        Optional<Rubric> optRubric = rubricRepository.findById(rubricId);

        if (!optRubric.isPresent()) {
            return false;
        }

        Rubric rubric = optRubric.get();

        if (!isCurrentUserEditor(rubric.getOwnerId())) {
            log.warn("The current user {} needs to be an editor in site {} to get the site's rubrics", sessionManager.getCurrentSessionUserId(), rubric.getOwnerId());
            return false;
        }

        if (associationRepository.findByRubricId(rubricId).stream()
                .anyMatch(ass -> !evaluationRepository.findByAssociationId(ass.getId()).isEmpty())) {
            log.warn("Rubric {} cannot be deleted. It has evaluations against it", rubricId);
            return false;
        }

        // SAK-42944 removing the soft-deleted associations
        rubric.getAssociations().forEach(ass -> evaluationRepository.deleteByToolItemRubricAssociation_Id(ass.getId()));

        rubricRepository.delete(rubric);
        return true;
    }

    private RubricTransferBean decorateRubricBean(RubricTransferBean bean) {

        bean.setFormattedCreatedDate(userTimeService.dateTimeFormat(bean.getCreated(), FormatStyle.MEDIUM, FormatStyle.SHORT));
        bean.setFormattedModifiedDate(userTimeService.dateTimeFormat(bean.getModified(), FormatStyle.MEDIUM, FormatStyle.SHORT));
        if (StringUtils.isNotBlank(bean.getCreatorId())) {
            try {
                bean.setCreatorDisplayName(userDirectoryService.getUser(bean.getCreatorId()).getDisplayName());
            } catch (UserNotDefinedException undfe) {
                log.warn("Failed to set the creatorDisplayName on rubric bean: {}", undfe.toString());
            }
        }
        if (StringUtils.isNotBlank(bean.getOwnerId())) {
            try {
                bean.setSiteTitle(siteService.getSite(bean.getOwnerId()).getTitle());
            } catch (IdUnusedException iue) {
                log.warn("Failed to set the siteTitle on rubric bean: {}", iue.toString());
            }
        }
        for (CriterionTransferBean criterion : bean.getCriteria()) {
            for (RatingTransferBean rating : criterion.getRatings()) {
                if (bean.getWeighted()){
                    rating.setWeightedPoints(rating.getPoints() * (criterion.getWeight() / 100D));
                } else {
                    rating.setWeightedPoints(rating.getPoints());
                }
            }
        }
        return bean;
    }

    public CriterionTransferBean copyCriterion(Long rubricId, Long sourceId) {

        Criterion criterion = criterionRepository.findById(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("No source criterion with id " + sourceId));

        Rubric rubric = rubricRepository.findById(rubricId)
            .orElseThrow(() -> new IllegalArgumentException("No rubric with id " + rubricId));

        List<Long> criterionIds = rubric.getCriteria().stream().map(Criterion::getId).collect(Collectors.toList());

        Criterion clone = criterion.clone();
        clone.setRubric(rubric);
        rubric.getCriteria().add(clone);
        clone.setTitle(clone.getTitle() + " " + resourceLoader.getString("copy"));

        Rubric savedRubric = rubricRepository.save(updateRubricMaxPoints(rubric));
        Criterion newCriterion = savedRubric.getCriteria().stream()
                .filter(c -> !criterionIds.contains(c.getId()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("copy criterion failed to create a new criterion, rubric: [" + rubricId + "], criterion: [" +sourceId + "]"));
        return new CriterionTransferBean(newCriterion);
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

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit criteria");
        }

        return rubricRepository.findById(rubricId).map(rubric -> {

            List<Long> criterionIds = rubric.getCriteria().stream().map(Criterion::getId).collect(Collectors.toList());

            Criterion criterion = new Criterion();
            criterion.setRubric(rubric);
            criterion.setTitle(resourceLoader.getString("default_criterion_title"));

            Rating rating1 = new Rating();
            rating1.setTitle(resourceLoader.getString("default_c1_r1_title"));
            rating1.setPoints(0D);
            rating1.setCriterion(criterion);

            Rating rating2 = new Rating();
            rating2.setTitle(resourceLoader.getString("default_c1_r2_title"));
            rating2.setPoints(1D);
            rating2.setCriterion(criterion);

            Rating rating3 = new Rating();
            rating3.setTitle(resourceLoader.getString("default_c1_r3_title"));
            rating3.setPoints(2D);
            rating3.setCriterion(criterion);

            List<Rating> ratings = criterion.getRatings();
            ratings.add(rating1);
            ratings.add(rating2);
            ratings.add(rating3);

            rubric.getCriteria().add(criterion);
            Rubric savedRubric = rubricRepository.save(updateRubricMaxPoints(rubric));

            Criterion newCriterion = savedRubric.getCriteria().stream()
                    .filter(c -> !criterionIds.contains(c.getId()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("create criterion failed to create a new criterion, siteId: [" + siteId + "], rubric: [" + rubricId + "]"));

            CriterionTransferBean bean = new CriterionTransferBean(newCriterion);
            bean.setNew(true);
            return bean;
        });
    }

    public Optional<CriterionTransferBean> createDefaultEmptyCriterion(String siteId, Long rubricId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit criteria");
        }

        return rubricRepository.findById(rubricId).map(rubric -> {
            List<Long> criterionIds = rubric.getCriteria().stream().map(Criterion::getId).collect(Collectors.toList());

            Criterion criterion = new Criterion();
            criterion.setRubric(rubric);
            criterion.setTitle(resourceLoader.getString("default_empty_criterion_title"));
            rubric.getCriteria().add(criterion);
            Rubric savedRubric = rubricRepository.save(rubric);

            Criterion newCriterion = savedRubric.getCriteria().stream()
                    .filter(c -> !criterionIds.contains(c.getId()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("default criterion create failed, siteId: [" + siteId + "], rubric: [" + rubricId + "]"));

            CriterionTransferBean bean = new CriterionTransferBean(newCriterion);
            bean.setNew(true);
            return bean;
        });
    }

    public Optional<RatingTransferBean> createDefaultRating(String siteId, Long rubricId, Long criterionId, int position) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit ratings");
        }

        Rubric rubric = rubricRepository.findById(rubricId)
            .orElseThrow(() -> new IllegalArgumentException("No rubric with id " + rubricId));

        Optional<RatingTransferBean> bean = criterionRepository.findById(criterionId).map(criterion -> {

            Rating rating = new Rating();
            rating.setTitle(resourceLoader.getString("default_rating_title"));
            rating.setPoints(0D);
            rating.setCriterion(criterion);

            criterion.getRatings().add(position, rating);

            criterion = criterionRepository.save(criterion);

            return new RatingTransferBean(criterion.getRatings().get(position));
        });

        rubricRepository.save(updateRubricMaxPoints(rubric));

        return bean;
    }

    public RubricTransferBean saveRubric(RubricTransferBean bean) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(bean.getOwnerId())) {
            throw new SecurityException("You must be a rubrics editor to create/edit rubrics");
        }

        Rubric rubric;
        if (bean.getId() == null) {
            rubric = new Rubric();
            rubric.setCriteria(bean.getCriteria().stream().map(c -> {
                Criterion criterion = new Criterion();
                criterion.setTitle(c.getTitle());
                criterion.setRubric(rubric);
                criterion.setDescription(c.getDescription());
                criterion.setWeight(c.getWeight());
                criterion.setRatings(c.getRatings().stream()
                        .map(r -> new Rating(null, r.getTitle(), r.getDescription(), r.getPoints(), criterion))
                        .collect(Collectors.toList()));
                return criterion;
            }).collect(Collectors.toList()));
        } else if (bean.getAdhoc()) {
            rubric = rubricRepository.getById(bean.getId());
            List<Criterion> oldCriterion = new ArrayList<>();

            // update existing criteria
            rubric.getCriteria().forEach(c -> bean.getCriteria().stream()
                    .filter(bc -> bc.getId() != null)
                    .filter(bc -> bc.getId().equals(c.getId()))
                    .findAny()
                    .ifPresentOrElse(bc -> {
                        c.setTitle(bc.getTitle());
                        c.setDescription(bc.getDescription());
                        c.setWeight(bc.getWeight());
                        c.getRatings().forEach(r -> bc.getRatings().stream()
                                .filter(br -> br.getId().equals(r.getId()))
                                .findAny()
                                .ifPresent(br -> {
                                    r.setTitle(br.getTitle());
                                    r.setDescription(br.getDescription());
                                    r.setPoints(br.getPoints());
                                }));
                    }, () -> oldCriterion.add(c))
            );

            // remove old criterion
            rubric.getCriteria().removeAll(oldCriterion);

            //add new
            List<Criterion> newCriterion = new ArrayList<>();
            newCriterion = bean.getCriteria().stream()
                    .filter(bc -> bc.getId() == null)
                    .map(c -> {
                        Criterion criterion = new Criterion();
                        criterion.setTitle(c.getTitle());
                        criterion.setRubric(rubric);
                        criterion.setDescription(c.getDescription());
                        criterion.setWeight(c.getWeight());
                        criterion.setRatings(c.getRatings().stream()
                                .map(r -> new Rating(null, r.getTitle(), r.getDescription(), r.getPoints(), criterion))
                                .collect(Collectors.toList()));
                        return criterion;
                    }).collect(Collectors.toList());
            rubric.getCriteria().addAll(newCriterion);
        } else {
            rubric = rubricRepository.getById(bean.getId());
            rubric.getCriteria().forEach(c -> bean.getCriteria().stream()
                    .filter(bc -> bc.getId().equals(c.getId()))
                    .findAny()
                    .ifPresent(bc -> {
                        c.setTitle(bc.getTitle());
                        c.setDescription(bc.getDescription());
                        c.setWeight(bc.getWeight());
                        c.getRatings().forEach(r -> bc.getRatings().stream()
                                .filter(br -> br.getId().equals(r.getId()))
                                .findAny()
                                .ifPresent(br -> {
                                    r.setTitle(br.getTitle());
                                    r.setDescription(br.getDescription());
                                    r.setPoints(br.getPoints());
                                }));
                    }));
        }
        rubric.setTitle(bean.getTitle());
        rubric.setWeighted(bean.getWeighted());
        rubric.setCreated(bean.getCreated());
        rubric.setModified(bean.getModified());
        rubric.setOwnerId(bean.getOwnerId());
        rubric.setCreatorId(bean.getCreatorId());
        rubric.setShared(bean.getShared());
        rubric.setDraft(bean.getDraft());
        rubric.setMaxPoints(bean.getMaxPoints());
        rubric.setAdhoc(bean.getAdhoc());

        return new RubricTransferBean(rubricRepository.save(updateRubricMaxPoints(rubric)));
    }

    public CriterionTransferBean updateCriterion(CriterionTransferBean bean, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit criteria");
        }

        if (bean.getId() != null) {
            // we can use getById since a bean with an id should exist
            Criterion criterion = criterionRepository.getById(bean.getId());
            criterion.setTitle(bean.getTitle());
            criterion.setDescription(bean.getDescription());
            criterion.setWeight(bean.getWeight());

            // update ratings from the bean
            List<Rating> ratings = criterion.getRatings();
            bean.getRatings()
                    .forEach(rb -> ratings.stream()
                            .filter(r -> r.getId().equals(rb.getId()))
                            .findAny()
                            .ifPresent(r -> {
                                r.setTitle(rb.getTitle());
                                r.setPoints(rb.getPoints());
                                r.setDescription(rb.getDescription());
                            }));
            return new CriterionTransferBean(criterionRepository.save(criterion));
        }

        return bean;
    }

    public void deleteCriterion(Long rubricId, Long criterionId, String siteId) {

        if (!isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to delete criteria");
        }

        Rubric rubric = rubricRepository.findById(rubricId)
            .orElseThrow(() -> new IllegalArgumentException("No rubric for id " + rubricId));

        rubric.getCriteria().removeIf(c -> c.getId().equals(criterionId));

        rubricRepository.save(updateRubricMaxPoints(rubric));
    }

    public RatingTransferBean updateRating(RatingTransferBean bean, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId) || !isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit ratings");
        }

        if (bean.getId() != null) {
            Rating rating = ratingRepository.getById(bean.getId());
            rating.setTitle(bean.getTitle());
            rating.setDescription(bean.getDescription());
            rating.setPoints(bean.getPoints());

            // persist the changes to the rating first as it is needed to create updated transfer bean
            Rating updatedRating = ratingRepository.save(rating);

            // since the rating points may have changed a rubric update may be needed
            rubricRepository.save(updateRubricMaxPoints(updatedRating.getCriterion().getRubric()));

            return new RatingTransferBean(updatedRating);
        }
        return bean;
    }

    public CriterionTransferBean deleteRating(Long ratingId, Long criterionId, String siteId, Long rubricId) {

        if (!isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to create/edit ratings");
        }

        CriterionTransferBean bean = criterionRepository.findById(criterionId).map(criterion -> {

            criterion.getRatings().removeIf(r -> r.getId().equals(ratingId));
            return new CriterionTransferBean(criterionRepository.save(criterion));
        }).orElseThrow(() -> new IllegalArgumentException("Could not remove rating for criterion [" + criterionId + "]"));

        log.debug("ratings: {}", bean.getRatings());

        Rubric rubric = rubricRepository
                .findById(rubricId)
                .orElseThrow(() ->new IllegalArgumentException("No rubric for id [" + rubricId + "]"));

        log.debug("Rubric max points before update: {}", rubric.getMaxPoints());
        rubric = rubricRepository.save(updateRubricMaxPoints(rubric));
        log.debug("Rubric max points after update:: {}", rubric.getMaxPoints());

        return bean;
    }

    @Transactional(readOnly = true)
    public Optional<RubricTransferBean> getRubric(Long rubricId) {

        return rubricRepository.findById(rubricId).map(rubric -> {

            String currentUserId = userDirectoryService.getCurrentUser().getId();
            if (rubric.getShared()
                || isCurrentUserEditor(rubric.getOwnerId())
                || isEvaluee(rubric.getOwnerId())
                || rubric.getCreatorId().equalsIgnoreCase(currentUserId)) {
                return decorateRubricBean(new RubricTransferBean(rubric));
            } else {
                return null;
            }
        });
    }

    @Transactional(readOnly = true)
    public Optional<CriterionTransferBean> getCriterion(Long criterionId, String siteId) {

        if (!isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to get criteria");
        }

        return criterionRepository.findById(criterionId).map(CriterionTransferBean::new);
    }

    @Transactional(readOnly = true)
    public Optional<AssociationTransferBean> getAssociationForToolAndItem(String toolId, String itemId, String siteId) {

        return associationRepository.findByToolIdAndItemId(toolId, itemId).map(AssociationTransferBean::new);
    }

    @Transactional(readOnly = true)
    public Optional<EvaluationTransferBean> getEvaluation(Long evaluationId, String siteId) {

        return evaluationRepository.findById(evaluationId).map(e -> {

                String currentUserId = sessionManager.getCurrentSessionUserId();
                if (isEvaluator(siteId)
                    || (e.getStatus() == EvaluationStatus.RETURNED && e.getEvaluatedItemOwnerId().equals(currentUserId))) {
                    return new EvaluationTransferBean(e);
                } else {
                    return null;
                }
            });
    }

    @Transactional(readOnly = true)
    public Optional<EvaluationTransferBean> getEvaluationForToolAndItemAndEvaluatedItemAndOwnerId(String toolId, String itemId, String evaluatedItemId, String evaluatedItemOwnerId, String siteId, boolean isPeer) {

        ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
            .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));
        Optional<Evaluation> optEval = null;
        if (evaluatedItemOwnerId == null) {
            optEval = evaluationRepository.findByAssociationIdAndEvaluatedItemId(association.getId(), evaluatedItemId);
        } else {
            optEval = evaluationRepository.findByAssociationIdAndEvaluatedItemIdAndOwner(association.getId(), evaluatedItemId, evaluatedItemOwnerId);
        }

        return optEval.map(eval -> {
            if (canViewEvaluation(eval, siteId, isPeer)) {
                return new EvaluationTransferBean(eval);
            } else {
                return null;
            }
        });

	}

    public boolean deleteEvaluationForToolAndItemAndEvaluatedItemId(String toolId, String itemId, String evaluatedItemId, String siteId) {

        ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
            .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));

        int count = evaluationRepository.deleteByAssociationIdAndEvaluatedItemId(association.getId(), evaluatedItemId);

        return count == 1;
    }

    @Transactional(readOnly = true)
    public List<EvaluationTransferBean> getEvaluationsForToolAndItem(String toolId, String itemId, String siteId) {

        ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
            .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));

        List<Evaluation> evaluations = evaluationRepository.findByAssociationId(association.getId());
        List<String> userIds = evaluations.stream().map(Evaluation::getEvaluatedItemOwnerId).collect(Collectors.toList());
        Map<String, User> userMap = userDirectoryService.getUsers(userIds).stream().collect(Collectors.toMap(Entity::getId, u -> u));
        return evaluationRepository.findByAssociationId(association.getId())
            .stream()
            .filter(eval -> canViewEvaluation(eval, siteId, false))
            .map(e -> {

                EvaluationTransferBean bean = new EvaluationTransferBean(e);
                bean.setSortName(userMap.get(bean.getEvaluatedItemOwnerId()).getSortName());
                return bean;
            })
            .collect(Collectors.toList());
    }


    public EvaluationTransferBean saveEvaluation(EvaluationTransferBean evaluationBean, String siteId) {

        if (!canGrade(evaluationBean, siteId)) {
            throw new SecurityException("You must be an evaluator to evaluate rubrics");
        }

        Evaluation evaluation;
        List<Long> newOutcomesCriterionIds = new ArrayList<>();
        if (evaluationBean.getId() != null) {
            evaluation = evaluationRepository.getById(evaluationBean.getId());

            List<CriterionOutcome> outcomes = evaluation.getCriterionOutcomes();
            List<Long> outcomeIds = outcomes.stream().map(CriterionOutcome::getCriterionId).collect(Collectors.toList());

            for (CriterionOutcomeTransferBean outcomeBean : evaluationBean.getCriterionOutcomes()) {
                Long beanCriterionId = outcomeBean.getCriterionId();
                if (beanCriterionId == null) {
                    // add
                    CriterionOutcome outcome = new CriterionOutcome();
                    outcome.setCriterionId(outcomeBean.getCriterionId());
                    outcome.setPoints(outcomeBean.getPoints());
                    outcome.setComments(outcomeBean.getComments());
                    outcome.setPointsAdjusted(outcomeBean.getPointsAdjusted());
                    outcome.setSelectedRatingId(outcomeBean.getSelectedRatingId());
                    outcomes.add(outcome);
                } else if (outcomeIds.contains(beanCriterionId)) {
                    outcomes.stream().filter(i -> i.getCriterionId().equals(beanCriterionId)).findAny().ifPresent(o -> {
                        // update
                        o.setPoints(outcomeBean.getPoints());
                        o.setComments(outcomeBean.getComments());
                        o.setPointsAdjusted(outcomeBean.getPointsAdjusted());
                        o.setSelectedRatingId(outcomeBean.getSelectedRatingId());
                    });
                    // criterion processed so remove it from the list
                    outcomeIds.remove(beanCriterionId);
                } else {
                    log.info("An outcome with id: [{}], was not in the original list but now it appears (dynamic rubric)", beanCriterionId);

                    CriterionOutcome outcome = new CriterionOutcome();
                    outcome.setCriterionId(beanCriterionId);
                    outcome.setPoints(outcomeBean.getPoints());
                    outcome.setComments(outcomeBean.getComments());
                    outcome.setPointsAdjusted(outcomeBean.getPointsAdjusted());
                    outcome.setSelectedRatingId(outcomeBean.getSelectedRatingId());
                    outcomes.add(outcome);
                    newOutcomesCriterionIds.add(beanCriterionId);
                }
            }
            // outcomeIds should be empty, if not the db contained outcomes not reported in the ui so remove them
            outcomes.removeIf(o -> outcomeIds.contains(o.getCriterionId()));
        } else {
            evaluation = new Evaluation();
            evaluation.getCriterionOutcomes().addAll(evaluationBean.getCriterionOutcomes().stream().map(o -> {
                CriterionOutcome outcome = new CriterionOutcome();
                outcome.setCriterionId(o.getCriterionId());
                outcome.setPoints(o.getPoints());
                outcome.setComments(o.getComments());
                outcome.setPointsAdjusted(o.getPointsAdjusted());
                outcome.setSelectedRatingId(o.getSelectedRatingId());
                return outcome;
            }).collect(Collectors.toList()));
        }

        // only set these once
        if (StringUtils.isBlank(evaluation.getCreatorId())) evaluation.setCreatorId(userDirectoryService.getCurrentUser().getId());
        if (evaluation.getCreated() == null) evaluation.setCreated(Instant.now());
        if (StringUtils.isBlank(evaluation.getOwnerId())) evaluation.setOwnerId(siteId);
        if (evaluation.getAssociationId() == null) evaluation.setAssociationId(evaluationBean.getAssociationId());

        // set these on each save
        evaluation.setEvaluatorId(evaluationBean.getEvaluatorId());
        evaluation.setEvaluatedItemId(evaluationBean.getEvaluatedItemId());
        evaluation.setEvaluatedItemOwnerId(evaluationBean.getEvaluatedItemOwnerId());
        evaluation.setOverallComment(evaluationBean.getOverallComment());
        evaluation.setStatus(evaluationBean.getStatus());
        evaluation.setEvaluatedItemOwnerType(evaluationBean.getEvaluatedItemOwnerType());
        evaluation.setModified(Instant.now());

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);

        // If this evaluation has been returned, back it up.
        if (savedEvaluation.getStatus() == EvaluationStatus.RETURNED) {

            ReturnedEvaluation returnedEvaluation = returnedEvaluationRepository.findByOriginalEvaluationId(evaluation.getId())
                .map(re -> {
                    re.setOverallComment(savedEvaluation.getOverallComment());
                    Map<Long, CriterionOutcome> outcomes = savedEvaluation.getCriterionOutcomes().stream()
                            .collect(Collectors.toMap(CriterionOutcome::getCriterionId, co -> co));
                    re.getCriterionOutcomes().removeIf(o -> outcomes.get(o.getCriterionId()) == null);
                    re.getCriterionOutcomes().forEach(rco -> {
                        CriterionOutcome o = outcomes.get(rco.getCriterionId());
                        rco.setSelectedRatingId(o.getSelectedRatingId());
                        rco.setPointsAdjusted(o.getPointsAdjusted());
                        rco.setPoints(o.getPoints());
                        rco.setComments(o.getComments());
                    });
                    if (!newOutcomesCriterionIds.isEmpty()) {
                        savedEvaluation.getCriterionOutcomes().stream().filter(o -> newOutcomesCriterionIds.contains(o.getCriterionId())).findAny().ifPresent(o -> {
                            ReturnedCriterionOutcome rco = new ReturnedCriterionOutcome(o);
                            re.getCriterionOutcomes().add(rco);
                        });
                    }
                    return re;
                }).orElseGet(() -> {
                    ReturnedEvaluation re = new ReturnedEvaluation();
                    re.setOverallComment(savedEvaluation.getOverallComment());
                    re.setOriginalEvaluationId(savedEvaluation.getId());
                    List<ReturnedCriterionOutcome> criterionOutcomes = re.getCriterionOutcomes();
                    savedEvaluation.getCriterionOutcomes().forEach(co -> {
                        ReturnedCriterionOutcome rco = new ReturnedCriterionOutcome(co);
                        criterionOutcomes.add(rco);
                    });
                    return re;
                });
            returnedEvaluationRepository.save(returnedEvaluation);
        }
        return new EvaluationTransferBean(savedEvaluation);
    }

    public EvaluationTransferBean cancelDraftEvaluation(Long draftEvaluationId) {

        Evaluation evaluation = evaluationRepository.findById(draftEvaluationId)
            .orElseThrow(() -> new IllegalArgumentException("No evaluation for id " + draftEvaluationId));

        if (evaluation.getStatus() != EvaluationStatus.DRAFT) {
            log.info("{} is not a draft evaluation. Returning it as is.", draftEvaluationId);
            // This is not a draft. It's not cancellable.
            return new EvaluationTransferBean(evaluation);
        }

        Optional<ReturnedEvaluation> optReturnedEvaluation = returnedEvaluationRepository.findByOriginalEvaluationId(draftEvaluationId);
        if (optReturnedEvaluation.isPresent()) {
            ReturnedEvaluation returnedEvaluation = optReturnedEvaluation.get();
            evaluation.setOverallComment(returnedEvaluation.getOverallComment());
            Map<Long, ReturnedCriterionOutcome> returnedOutcomes = returnedEvaluation.getCriterionOutcomes().stream()
                    .collect(Collectors.toMap(ReturnedCriterionOutcome::getCriterionId, rco -> rco));
            evaluation.getCriterionOutcomes().forEach(co -> {
                ReturnedCriterionOutcome rco = returnedOutcomes.get(co.getCriterionId());
                co.setSelectedRatingId(rco.getSelectedRatingId());
                co.setPointsAdjusted(rco.getPointsAdjusted());
                co.setPoints(rco.getPoints());
                co.setComments(rco.getComments());
            });
            evaluation.setStatus(EvaluationStatus.RETURNED);
            return new EvaluationTransferBean(evaluationRepository.save(evaluation));
        } else {
            evaluationRepository.deleteById(draftEvaluationId);
            return new EvaluationTransferBean(new Evaluation());
        }
    }

    public boolean hasAssociatedRubric(String tool, String id) {

        if (StringUtils.isBlank(id)) return false;

        // S2U-34 This is bypassing the permission check as it's needed for peer review
        return getRubricAssociation(tool, id).isPresent();
    }

    public Optional<ToolItemRubricAssociation> saveRubricAssociation(String toolId, String toolItemId, final Map<String, String> params) {
        return saveRubricAssociation(toolId, toolItemId, params, null);
    }

    public Optional<ToolItemRubricAssociation> saveRubricAssociation(String toolId, String toolItemId, final Map<String, String> params, String siteId) {

        if (StringUtils.isNotBlank(toolId) && StringUtils.isNotBlank(toolItemId) && !CollectionUtils.isEmpty(params)) {

            String optionRubricId = Optional.ofNullable(params.get(RubricsConstants.RBCS_LIST)).orElse(StringUtils.EMPTY);
            final String optionRubricAssociate = Optional.ofNullable(params.get(RubricsConstants.RBCS_ASSOCIATE)).orElse(StringUtils.EMPTY);
            final Optional<ToolItemRubricAssociation> existingAssociation = getRubricAssociationCheckPermission(toolId, toolItemId);

            Long requestedRubricId = NumberUtils.toLong(optionRubricId);

            // S2U-5 dynamic rubrics with criterions created at evaluation time
            if (RubricsConstants.RBCS_TOOL_SAMIGO.equals(toolId) && requestedRubricId == 0 && StringUtils.equals(optionRubricAssociate, "2") && siteId != null) {
                Rubric dynamicRubric = rubricRepository.findAdhocByTitle(siteId).stream().filter(Objects::nonNull).findFirst().orElse(null);
                if (dynamicRubric == null) {
                    dynamicRubric = new Rubric();
                    String currentUserId = sessionManager.getCurrentSessionUserId();
                    dynamicRubric.setOwnerId(siteId);
                    dynamicRubric.setCreatorId(currentUserId);
                    Instant now = Instant.now();
                    dynamicRubric.setCreated(now);
                    dynamicRubric.setModified(now);
                    dynamicRubric.setAdhoc(true);
                    dynamicRubric.setTitle(toolItemId);
                    dynamicRubric = rubricRepository.save(dynamicRubric);
                }
                requestedRubricId = dynamicRubric.getId();
                optionRubricId = String.valueOf(requestedRubricId);
            }

            if (existingAssociation.isPresent()) {
                final ToolItemRubricAssociation association = existingAssociation.get();
                final Rubric existingRubric = association.getRubric();
                final boolean isSameRubric = StringUtils.equals(optionRubricId, existingRubric.getId().toString());

                if (StringUtils.equals(optionRubricAssociate, "1") || StringUtils.equals(optionRubricAssociate, "2")) {
                    if (isSameRubric) {
                        // We're updating an existing association, not the rubric though.
                        association.setParameters(setConfigurationParameters(params, association.getParameters()));
                        association.setModified(LocalDateTime.now());
                        return Optional.of(associationRepository.save(association));
                    } else {
                        // Rubrics switching, deactivate the current association
                        association.setActive(false);

                        Optional<ToolItemRubricAssociation> optionalExistingAssociation = findAssociationByItemIdAndRubricId(toolItemId, requestedRubricId);

                        if (optionalExistingAssociation.isPresent()) {
                            // if there's already an association for the requested rubric, reuse it
                            optionalExistingAssociation.get().setActive(true);
                        } else {
                            // if there is no association for the requested rubric create a new association
                            Optional<ToolItemRubricAssociation> newAssociation = createToolItemRubricAssociation(toolId, toolItemId, params, requestedRubricId);
                            if (newAssociation.isPresent()) {
                                return Optional.of(associationRepository.save(newAssociation.get()));
                            }
                        }
                    }
                } else if (requestedRubricId.equals(0L)){
                    //deactivating an association without making a new one
                    association.setActive(false);
                    associationRepository.save(association);
                    return Optional.empty();
                }
            } else {
                // if existingAssociation is not present, it could just mean that it was deactivated previously
                // the specific getRubricAssociation impl that we used earlier to load it will ignore deactivated ones.
                Optional<ToolItemRubricAssociation> optionalExistingAssociation = findAssociationByItemIdAndRubricId(toolItemId, requestedRubricId);    // this will include inactive [soft-deleted] ones
                if (optionalExistingAssociation.isPresent()) {  // if there's already an old association for the requested rubric that was deactivated previously, reuse it
                    optionalExistingAssociation.get().setActive(true);
                    return Optional.of(associationRepository.save(optionalExistingAssociation.get()));
                }
                Optional<ToolItemRubricAssociation> newAssociation = createToolItemRubricAssociation(toolId, toolItemId, params, requestedRubricId);
                if (newAssociation.isPresent()) {
                    return Optional.of(associationRepository.save(newAssociation.get()));
                }
            }
        }
        log.debug("associating rubric failed returning none for tool [{}], item [{}]", toolId, toolItemId);
        return Optional.empty();
    }

    private Optional<ToolItemRubricAssociation> createToolItemRubricAssociation(String toolId, String toolItemId, Map<String, String> params, Long requestedRubricId) {
        log.debug("Creating new association for rubric [{}], tool [{}], item[{}]", requestedRubricId, toolId, toolItemId);
        Optional<Rubric> rubric = rubricRepository.findById(requestedRubricId);
        if (rubric.isPresent()) {
            ToolItemRubricAssociation newAssociation = new ToolItemRubricAssociation();
            newAssociation.setRubric(rubric.get());
            newAssociation.setToolId(toolId);
            newAssociation.setItemId(toolItemId);
            newAssociation.setActive(true);
            LocalDateTime now = LocalDateTime.now();
            newAssociation.setCreated(now);
            newAssociation.setModified(now);
            newAssociation.setCreatorId(userDirectoryService.getCurrentUser().getId());
            newAssociation.setParameters(setConfigurationParameters(params, Collections.emptyMap()));
            return Optional.of(newAssociation);
        }
        log.warn("Requested rubric [{}] not found when attempting to create a new association", requestedRubricId);
        return Optional.empty();
    }

    private Optional<ToolItemRubricAssociation> findAssociationByItemIdAndRubricId(String toolItemId, Long rubricId) {

        return associationRepository.findByItemIdAndRubricId(toolItemId, rubricId).map(assoc -> {

            String siteId = assoc.getRubric().getOwnerId();

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

    private Map<String, Integer> setConfigurationParameters(Map<String, String> params, Map<String, Integer> oldParams) {

        Map<String, Integer> merged = new HashMap<>();

        //Get the parameters
        params.forEach((k, v) -> {
            if (k.startsWith(RBCS_CONFIG)) {
                merged.put(StringUtils.remove(k, RBCS_CONFIG), Integer.valueOf(v));
            } else if (k.startsWith(RBCS_MULTIPLE_OPTIONS_CONFIG)) {
                merged.put(StringUtils.remove(k, RBCS_MULTIPLE_OPTIONS_CONFIG), Integer.valueOf(v));
            } else if (!k.startsWith(RubricsConstants.RBCS_LIST)) {//duplicate or migrate
                merged.put(k, Integer.valueOf(v));
            }
        });

        oldParams.keySet().stream()
                .filter(name -> !(params.containsKey(RBCS_CONFIG + name)) && !(params.containsKey(RBCS_MULTIPLE_OPTIONS_CONFIG + name)) && !(params.containsKey(RubricsConstants.RBCS_ASSOCIATE)))
                .forEach(name -> merged.put(name, 0));
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
        return associationRepository.findByToolIdAndItemId(toolId, associatedToolItemId);
    }

    @Transactional(readOnly = true)
    private Optional<ToolItemRubricAssociation> getRubricAssociationCheckPermission(String toolId, String associatedToolItemId) {
        return getRubricAssociation(toolId, associatedToolItemId).filter(canEdit.or(isCreator).or(canEvaluate));
    }

    @Transactional(readOnly = true)
    public String getRubricEvaluationObjectId(String itemId, String userId, String toolId, String siteId) {

        Optional<ToolItemRubricAssociation> optAssociation = associationRepository.findByToolIdAndItemId(toolId, itemId);
        if (optAssociation.isEmpty()) {
            return null;
        }

        Optional<Evaluation> optEvaluation = evaluationRepository.findByAssociationIdAndUserId(optAssociation.get().getId(), userId);

        if (optEvaluation.isPresent() && canViewEvaluation(optEvaluation.get(), siteId, false)) {
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

            if (securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteService.siteReference(assoc.getRubric().getOwnerId()))) {
                try {
                    evaluationRepository.deleteByToolItemRubricAssociation_Id(assoc.getId());
                    associationRepository.delete(assoc);
                } catch (Exception e) {
                    log.warn("Error deleting rubric association for id {} : {}", itemId, e.toString());
                }
            }
        });
    }

    public void softDeleteRubricAssociationsByItemIdPrefix(String itemId, String toolId) {

        associationRepository.findByItemIdPrefix(toolId, itemId).forEach(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, 0);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error soft deleting rubric association for item id prefix {} : {}", itemId, e.toString());
            }
        });
    }

    public void restoreRubricAssociation(String toolId, String itemId) {

        associationRepository.findByToolIdAndItemId(toolId, itemId).ifPresent(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, 0);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error restoring rubric association for item id {} : {}", itemId, e.toString());
            }
        });
    }

    public void restoreRubricAssociationsByItemIdPrefix(String itemId, String toolId) {

        associationRepository.findByItemIdPrefix(toolId, itemId).forEach(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, 0);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error restoring rubric association for item id prefix {} : {}", itemId, e.toString());
            }
        });
    }

    private void deleteRubricEvaluationsForAssociation(Long associationId) {
        evaluationRepository.deleteByToolItemRubricAssociation_Id(associationId);
    }

    public void softDeleteRubricAssociation(String toolId, String id) {

        getRubricAssociationCheckPermission(toolId, id).ifPresent(assoc -> {

            try {
                assoc.getParameters().put(RubricsConstants.RBCS_SOFT_DELETED, 1);
                associationRepository.save(assoc);
            } catch (Exception e) {
                log.warn("Error soft deleting rubric association for tool {} and id {} : {}", toolId, id, e.toString());
            }
        });
    }

    public void deleteRubricAssociation(String tool, String id) {

        try {
            getRubricAssociationCheckPermission(tool, id).ifPresent(assoc -> {

                evaluationRepository.deleteByToolItemRubricAssociation_Id(assoc.getId());
                associationRepository.delete(assoc);
            });
        } catch (Exception e) {
            log.warn("Error deleting rubric association for tool {} and id {} : {}", tool, id, e.toString());
        }
    }

    public String createContextualFilename(RubricTransferBean rubric, String toolId, String itemId, String evaluatedItemId, String siteId) {

        String filename = StringUtils.trimToEmpty(rubric.getTitle()).replace(".", "_");
        Optional<Evaluation> optEvaluation = Optional.empty();

        if (toolId != null && itemId != null && evaluatedItemId != null) {
            ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
                    .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));
            optEvaluation = evaluationRepository.findByAssociationIdAndEvaluatedItemId(association.getId(), evaluatedItemId);
        }

        if (optEvaluation.isPresent() && canViewEvaluation(optEvaluation.get(), siteId, false)) {
            Evaluation eval = optEvaluation.get();
            String studentName = "";
            try {
                studentName = userDirectoryService.getUser(eval.getEvaluatedItemOwnerId()).getSortName();
            } catch (UserNotDefinedException ex) {
                log.error("No user for id {} : {}", eval.getEvaluatedItemOwnerId(), ex.toString());
            }
            filename = filename + '_' + studentName;
        }
        return filename;
    }

    @Transactional(readOnly = true)
    public byte[] createPdf(String siteId, Long rubricId, String toolId, String itemId, String evaluatedItemId)
            throws IOException {

        Rubric rubric = rubricRepository.findById(rubricId)
                .orElseThrow(() -> new IllegalArgumentException("No rubric for id " + rubricId));

        if (!isEvaluator(siteId) && !isEvaluee(siteId) && !rubric.getShared()) {
            throw new SecurityException("You must be either an evaluator or evaluee to create PDFs");
        }

        Optional<Evaluation> optEvaluation = Optional.empty();
        if (toolId != null && itemId != null && evaluatedItemId != null) {
            ToolItemRubricAssociation association = associationRepository.findByToolIdAndItemId(toolId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("No association for toolId " + toolId + " and itemId " + itemId));
            optEvaluation = evaluationRepository.findByAssociationIdAndEvaluatedItemId(association.getId(), evaluatedItemId);
        }

        // Count points
        double points = 0;
        String studentName = "";
        String itemName = "";
        boolean showEvaluated = optEvaluation.isPresent() && canViewEvaluation(optEvaluation.get(), siteId, false);

        if (showEvaluated) {
            Evaluation eval = optEvaluation.get();
            points = eval.getCriterionOutcomes().stream().mapToDouble(CriterionOutcome::getPoints).sum();
            try {
                studentName = userDirectoryService.getUser(eval.getEvaluatedItemOwnerId()).getDisplayName();
            } catch (UserNotDefinedException ex) {
                log.error("No user for id {} : {}", eval.getEvaluatedItemOwnerId(), ex.toString());
            }
            itemName = getAssociatedName(eval, siteId).get();
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
        if (StringUtils.isNotBlank(itemName)){
            paragraph.add(resourceLoader.getFormattedMessage("export_rubric_association", itemName));
            paragraph.add(Chunk.NEWLINE);
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
            Optional<Double> adjustedScoreCell = optEvaluation.isPresent() ? getAdjustedScore(cri, optEvaluation.get()) : Optional.empty();
            Optional<String> criterionComment = optEvaluation.isPresent() ? getCriterionComment(cri, optEvaluation.get()) : Optional.empty();
            PdfPTable criterionTable = new PdfPTable(cri.getRatings().size() + 1 + (criterionComment.isEmpty() ? 0 : 1) + (adjustedScoreCell.isEmpty() ? 0 : 1));
            Paragraph criterionParagraph = new Paragraph();
            criterionParagraph.setFont(BOLD_FONT);
            boolean isCriterionGroup = cri.getRatings().isEmpty();

            if (showEvaluated && !isCriterionGroup) {
                Optional<Double> evaluatedPoints = this.getCriterionPoints(cri, optEvaluation.get());

                //Get CriteriumOutcome as Optional for current criterium as an optional by matching associated criterion ids
                Optional<CriterionOutcome> optCriterionOutcome = optEvaluation.get().getCriterionOutcomes().stream()
                    .filter(outcome -> cri.getId().equals(outcome.getCriterionId())).findAny();

                if (evaluatedPoints.isPresent()) {
                    if (optCriterionOutcome.isPresent() && optCriterionOutcome.get().getPointsAdjusted()) {

                        //Get points of the selected rating (not altered) by maching selected rating id's and getting points from matched rating
                        Double selectedRatingOriginalPoints = optCriterionOutcome
                                .flatMap(outcome -> cri.getRatings().stream()
                                        .filter(rating -> rating.getId().equals(outcome.getSelectedRatingId())).findAny())
                                .map(Rating::getPoints)
                                .orElse(0D);

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
                String weightString = "";
                if(rubric.getWeighted()){
                    weightString = "(" + BigDecimal.valueOf(rating.getPoints() * (cri.getWeight()/100.0D)).setScale(2, RoundingMode.HALF_UP) + ") ";
                }
                String ratingPoints = resourceLoader.getFormattedMessage("export_rubrics_points", rating.getTitle(), weightString + rating.getPoints());
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
                        }
                    }
                }
                newCell.addElement(ratingsParagraph);
                criterionTable.addCell(newCell);
            }
            if(adjustedScoreCell.isPresent()){
                PdfPCell newCell = new PdfPCell();
                newCell.setBackgroundColor(Color.GRAY);
                Paragraph adjustedParagraph = new Paragraph(resourceLoader.getFormattedMessage("export_adjusted", adjustedScoreCell.get().toString()), BOLD_FONT);
                newCell.addElement(adjustedParagraph);
                criterionTable.addCell(newCell);
            }
            if(criterionComment.isPresent()){
                PdfPCell newCell = new PdfPCell();
                newCell.setBackgroundColor(Color.GRAY);
                Paragraph commentHeader = new Paragraph(resourceLoader.getFormattedMessage("export_comments", ""), BOLD_FONT);
                newCell.addElement(commentHeader);
                Paragraph commentParagraph = new Paragraph(Jsoup.parse(criterionComment.get()).text(), NORMAL_FONT);
                newCell.addElement(commentParagraph);
                criterionTable.addCell(newCell);
            }

            criterionTable.completeRow();
            document.add(criterionTable);
        }

        document.close();
        return out.toByteArray();
    }

    private Optional<Double> getAdjustedScore(Criterion criterion, Evaluation evaluation){
        if(criterion==null || evaluation==null) {
            return Optional.empty();
        }
        for(CriterionOutcome outcome: evaluation.getCriterionOutcomes()){
            if(outcome.getPointsAdjusted() && outcome.getCriterionId().equals(criterion.getId())){
                return Optional.of(outcome.getPoints());
            }
        }
        return Optional.empty();
    }

    private Optional<String> getCriterionComment(Criterion criterion, Evaluation evaluation){
        if(criterion==null || evaluation==null){
            return Optional.empty();
        }
        for(CriterionOutcome outcome: evaluation.getCriterionOutcomes()){
            if(outcome.getCriterionId().equals(criterion.getId()) && StringUtils.isNotBlank(outcome.getComments())){
                return Optional.of(outcome.getComments());
            }
        }
        return Optional.empty();
    }

    private Optional<String> getAssociatedName(Evaluation evaluation, String siteId){
        if(evaluation==null || StringUtils.isEmpty(siteId)){
            return Optional.empty();
        }
        Optional<ToolItemRubricAssociation> optAssociation = associationRepository.findById(evaluation.getAssociationId());
        if(optAssociation.isPresent()){
            ToolItemRubricAssociation association = optAssociation.get();
            try {
                if(association.getToolId().equals(AssignmentConstants.TOOL_ID)){
                    return Optional.of(assignmentService.getAssignment(association.getItemId()).getTitle());
                } else if (association.getToolId().equals(RubricsConstants.RBCS_TOOL_GRADEBOOKNG)){
                    String gradebookUid = gradingService.getGradebookUidByAssignmentById(siteId, Long.valueOf(association.getItemId()));
                    return Optional.of(gradingService.getAssignment(gradebookUid, siteId, Long.valueOf(association.getItemId())).getName());
                } else if (association.getToolId().equals(RubricsConstants.RBCS_TOOL_SAMIGO)){
                    String[] idParts = association.getItemId().split("\\.");
                    return Optional.of(publishedAssessmentFacadeQueriesAPI.getPublishedAssessment(Long.valueOf(idParts[1])).getTitle());
                }
            } catch (Exception e){  //on any kind of exception, just leave the item name out.
                log.debug("Could not get graded item name for tool {} and item id {}", association.getToolId(), association.getItemId());
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Map<String, String>> getEntityMap(String fromContext) {

        return rubricRepository.findByOwnerId(fromContext).stream()
            .map(r -> Map.of("id", r.getId().toString(), "title", r.getTitle())).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {

        Map<String, String> traversalMap = new HashMap<>();
        rubricRepository.findByOwnerId(fromContext).forEach(rubric -> {

            if (CollectionUtils.isEmpty(ids) || ids.contains(rubric.getId().toString())) {

                try {
                    Rubric clone = rubric.clone(toContext);
                    clone.setCreated(Instant.now());
                    clone.setModified(Instant.now());
                    clone = rubricRepository.save(clone);
                    traversalMap.put(RBCS_PREFIX + rubric.getId(), RBCS_PREFIX + clone.getId());
                } catch (Exception e) {
                    log.warn("Failed to clone rubric into new site: {}", e.toString());
                }
            }
        });
        return traversalMap;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

        if (cleanup) {
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
                if (key.startsWith(RBCS_PREFIX)) {
                    try {
                        //2 for each, get its active associations
                        Long rubricId = NumberUtils.toLong(key.substring(RBCS_PREFIX.length()));
                        associationRepository.findByRubricId(rubricId).stream()
                                .filter(ToolItemRubricAssociation::getActive)
                                .forEach(association -> {

                            String tool = association.getToolId();
                            String itemId = association.getItemId();
                            String newRubricId = transversalMap.get(RBCS_PREFIX + rubricId).substring(RBCS_PREFIX.length());
                            String newItemId = null;
                            //3 association type
                            if (AssignmentConstants.TOOL_ID.equals(tool)) {
                                //3a if assignments
                                log.debug("Handling Rubrics association transfer for Assignment entry " + itemId);
                                if(transversalMap.get("assignment/"+itemId) != null) {
                                    newItemId = transversalMap.get("assignment/"+itemId).substring("assignment/".length());
                                }
                            } else if (RubricsConstants.RBCS_TOOL_SAMIGO.equals(tool)) {
                                //3b if samigo
                                if(itemId.startsWith(RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX)) {
                                    log.debug("Skipping published item {}", itemId);
                                }
                                log.debug("Handling Rubrics association transfer for Samigo entry " + itemId);
                                if(transversalMap.get("sam_item/"+itemId) != null) {
                                    newItemId = transversalMap.get("sam_item/"+itemId).substring("sam_item/".length());
                                }
                            } else if (RubricsConstants.RBCS_TOOL_FORUMS.equals(tool)) {
                                //3c if forums
                                newItemId = itemId.substring(0, 4);
                                String strippedId = itemId.substring(4);//every forum prefix have this size
                                log.debug("Handling Rubrics association transfer for Forums entry " + strippedId);
                                if(RubricsConstants.RBCS_FORUM_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum/"+strippedId) != null) {
                                    newItemId += transversalMap.get("forum/"+strippedId).substring("forum/".length());
                                } else if(RubricsConstants.RBCS_TOPIC_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum_topic/"+strippedId) != null) {
                                    newItemId += transversalMap.get("forum_topic/"+strippedId).substring("forum_topic/".length());
                                } else {
                                    log.debug("Not found updated id for item {}", itemId);
                                }
                            } else if (RubricsConstants.RBCS_TOOL_GRADEBOOKNG.equals(tool)) {
                                //3d if gradebook
                                log.debug("Handling Rubrics association transfer for Gradebook entry " + itemId);
                                if(transversalMap.get("gb/"+itemId) != null) {
                                    newItemId = transversalMap.get("gb/"+itemId).substring("gb/".length());
                                }
                            } else {
                                log.warn("Unhandled tool for Rubrics transfer between sites");
                            }

                            //4 save new association
                            if (StringUtils.isNoneBlank(newItemId, newRubricId)) {
                                Map<String, String> params = association.getFormattedAssociation();
                                params.put(RubricsConstants.RBCS_LIST, newRubricId);
                                log.debug("Create association for the new rubric [{}] and new item [{}] in the tool [{}]", newRubricId, newItemId, tool);
                                saveRubricAssociation(tool, newItemId, params);
                            } else {
                                log.warn("Cannot create association with blank rubric id [{}] or item [{}]", newRubricId, newItemId);
                            }
                        });
                    } catch (Exception ex) {
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

    public void deleteSiteRubrics(String siteId) {

        if (!isCurrentUserEditor(siteId)) {
            throw new SecurityException("You must be a rubrics editor to delete a site's rubrics");
        }

        evaluationRepository.deleteByOwnerId(siteId);
        rubricRepository.deleteByOwnerId(siteId);
    }

    private Optional<Double> getCriterionPoints(Criterion cri, Evaluation evaluation) {

        if (evaluation == null) return Optional.empty();

        double points = 0;
        List<Rating> ratingList = cri.getRatings();
        for (Rating rating : ratingList) {
            for (CriterionOutcome outcome : evaluation.getCriterionOutcomes()) {
                if (cri.getId().equals(outcome.getCriterionId())
                        && rating.getId().equals(outcome.getSelectedRatingId())) {
                    points = points + outcome.getPoints();
                }
            }
        }

        return Optional.of(points);
    }

    private boolean isCurrentUserEditor(String siteId) {

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

    private boolean canViewEvaluation(Evaluation eval, String siteId, boolean isPeer) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (isEvaluator(siteId)) {
            return true;
        }

        if (isEvaluee(siteId)) {
            if (eval.getEvaluatedItemOwnerType() == EvaluatedItemOwnerType.USER) {
                if (currentUserId.equals(eval.getEvaluatedItemOwnerId())) {//evals made to my submission
                    return true;
                }
                if (isPeer && currentUserId.equals(eval.getEvaluatedItemId())) {//self or peer evals assigned to me
                    return true;
                }
            }
            if (eval.getEvaluatedItemOwnerType() == EvaluatedItemOwnerType.GROUP) {
                if (authzGroupService.getUserRole(currentUserId, "/site/" + siteId + "/group/" + eval.getEvaluatedItemOwnerId()) != null) {//evals made to my group submission
                    return true;
                }
                if (isPeer && authzGroupService.getUserRole(currentUserId, "/site/" + siteId + "/group/" + eval.getEvaluatedItemId()) != null) {//peer evals assigned to my group
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canGrade(EvaluationTransferBean eval, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (isEvaluator(siteId)) {
            return true;
        }

        if (eval.isPeerOrSelf()) {
            if (eval.getEvaluatedItemOwnerType() == EvaluatedItemOwnerType.USER && currentUserId.equals(eval.getEvaluatedItemId())) {//self or peer evals assigned to me
                return true;
            }
            if (eval.getEvaluatedItemOwnerType() == EvaluatedItemOwnerType.GROUP) {//peer evals assigned to my group
                return authzGroupService.getUserRole(currentUserId, "/site/" + siteId + "/group/" + eval.getEvaluatedItemId()) != null;
            }
        }

        return false;
    }

    private Rubric updateRubricMaxPoints(Rubric rubric) {

        Double maxPoints = rubric.getCriteria().stream().filter(c -> c.getRatings().size() > 0).reduce(0D, (m, c) -> {

            // Get the highest possible score for this criterion
            Double highest
                = c.getRatings().stream().reduce(0D, (h, r) -> r.getPoints() > h ? r.getPoints() : h, Double::sum);

            // Add it to the total, checking if it is weighted first
            if (rubric.getWeighted() && highest > 0) {
                return m + (highest * (c.getWeight() / 100D));
            } else {
                return m + highest;
            }
        }, Double::sum);
        rubric.setMaxPoints(maxPoints);
        return rubric;
    }

    public List<ToolItemRubricAssociation> getRubricAssociationsByRubricAndTool(Long rubricId, String toolId) {
        return associationRepository.findByRubricIdAndToolId(rubricId, toolId);
    }

}
