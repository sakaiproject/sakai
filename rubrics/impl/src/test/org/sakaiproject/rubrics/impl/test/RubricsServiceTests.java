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
package org.sakaiproject.rubrics.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.CriterionOutcomeTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.EvaluatedItemOwnerType;
import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;
import org.sakaiproject.rubrics.api.model.ReturnedEvaluation;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.ReturnedEvaluationRepository;
import org.sakaiproject.rubrics.impl.RubricsServiceImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RubricsTestConfiguration.class})
public class RubricsServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AssociationRepository associationRepository;
    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private ReturnedEvaluationRepository returnedEvaluationRepository;
    @Autowired private RubricsService rubricsService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private SiteService siteService;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;

    String siteId = "playpen";
    String siteTitle = "Playpen";
    String siteRef = "/site/" + siteId;
    Site site = null;
    String user1 = "user1";
    User user1User = null;
    String user2 = "user2";
    User user2User = null;
    String user3 = "user3";
    User user3User = null;
    String instructor = "instructor";
    User instructorUser = null;
    RubricTransferBean rubricBean1 = null;
    String defaultRubricTitle = "New Rubric";
    String defaultC1Title = "Criterion 1";
    String defaultC1R1Title = "Inadequate";
    String defaultC1R2Title = "Meets expectations";
    String defaultC1R3Title = "Exceeds expectations";
    String defaultC2Title = "Criterion 2";
    String defaultC2R1Title = "Inadequate";
    String defaultC2R2Title = "Poor";
    String defaultC2R3Title = "Fair";
    String defaultC2R4Title = "Good";
    String defaultC2R5Title = "Exceptional";
    String defaultCriterionTitle = "New Criterion";
    String defaultRatingTitle = "New Rating";

    private ResourceLoader resourceLoader;

    @Before
    public void setup() {

        reset(securityService);
        reset(userDirectoryService);

        user1User = mock(User.class);
        when(user1User.getId()).thenReturn(user1);
        when(user1User.getDisplayName()).thenReturn("User 1");

        user2User = mock(User.class);
        when(user2User.getId()).thenReturn(user2);
        when(user2User.getDisplayName()).thenReturn("User 2");

        user3User = mock(User.class);
        when(user3User.getId()).thenReturn(user3);
        when(user3User.getDisplayName()).thenReturn("User 3");

        instructorUser = mock(User.class);
        when(instructorUser.getId()).thenReturn(instructor);
        when(instructorUser.getDisplayName()).thenReturn("Instructor");

        rubricBean1 = new RubricTransferBean();
        rubricBean1.setSiteTitle("Rubric 1");
        rubricBean1.setOwnerId(siteId);

        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        when(resourceLoader.getString("default_rubric_title")).thenReturn(defaultRubricTitle);
        when(resourceLoader.getString("default_criterion1_title")).thenReturn(defaultC1Title);
        when(resourceLoader.getString("default_c1_r1_title")).thenReturn(defaultC1R1Title);
        when(resourceLoader.getString("default_c1_r2_title")).thenReturn(defaultC1R2Title);
        when(resourceLoader.getString("default_c1_r3_title")).thenReturn(defaultC1R3Title);
        when(resourceLoader.getString("default_criterion2_title")).thenReturn(defaultC2Title);
        when(resourceLoader.getString("default_c2_r1_title")).thenReturn(defaultC2R1Title);
        when(resourceLoader.getString("default_c2_r2_title")).thenReturn(defaultC2R2Title);
        when(resourceLoader.getString("default_c2_r3_title")).thenReturn(defaultC2R3Title);
        when(resourceLoader.getString("default_c2_r4_title")).thenReturn(defaultC2R4Title);
        when(resourceLoader.getString("default_c2_r5_title")).thenReturn(defaultC2R5Title);
        when(resourceLoader.getString("default_criterion_title")).thenReturn(defaultCriterionTitle);
        when(resourceLoader.getString("default_rating_title")).thenReturn(defaultRatingTitle);
        ((RubricsServiceImpl) AopTestUtils.getTargetObject(rubricsService)).setResourceLoader(resourceLoader);

        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn(siteId);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);

        site = mock(Site.class);
        when(site.getTitle()).thenReturn(siteTitle);

        try {
            when(siteService.getSite(siteId)).thenReturn(site);
            when(siteService.siteReference(siteId)).thenReturn(siteRef);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveRubricWithoutUserOrPermission() {

        // Should throw it as there's no current user
        assertThrows(SecurityException.class, () -> rubricsService.saveRubric(rubricBean1));

        switchToUser1();

        // Should still throw it as the user doesn't have rubrics.editor
        assertThrows(SecurityException.class, () -> rubricsService.saveRubric(rubricBean1));
    }

    @Test
    public void createDefaultRubric() {

        switchToUser1();

        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRubric(siteId));

        switchToInstructor();
        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        assertNotNull(rubricBean.getId());
        assertEquals(rubricBean.getTitle(), defaultRubricTitle);
        assertEquals(2, rubricBean.getCriteria().size());
        CriterionTransferBean crit1 = rubricBean.getCriteria().get(0);
        assertEquals(crit1.getTitle(), defaultC1Title);
        assertEquals(3, crit1.getRatings().size());
        CriterionTransferBean crit2 = rubricBean.getCriteria().get(1);
        assertEquals(crit2.getTitle(), defaultC2Title);
        assertEquals(5, crit2.getRatings().size());
    }

    @Test
    public void getRubrics() {

        switchToUser1();

        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRubric(siteId));

        switchToInstructor();
        rubricsService.createDefaultRubric(siteId);
        List<RubricTransferBean> rubrics = rubricsService.getRubricsForSite(siteId);
        assertEquals(1, rubrics.size());
        assertEquals(2, rubrics.get(0).getCriteria().size());
    }

    @Test
    public void updateRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        String newTitle = "Cheese Sandwich";
        String newDescription = "Sandwiches of the world";
        rubricBean.setTitle(newTitle);
        rubricBean = rubricsService.saveRubric(rubricBean);
        assertEquals(rubricBean.getTitle(), newTitle);
    }

    @Test
    public void copyRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        assertEquals(1, rubricsService.getRubricsForSite(siteId).size());

        switchToUser1();
        assertThrows(SecurityException. class, () -> rubricsService.copyRubricToSite(rubricBean.getId(), siteId));
        switchToInstructor();
        rubricsService.copyRubricToSite(rubricBean.getId(), siteId);
        assertEquals(2, rubricsService.getRubricsForSite(siteId).size());
    }

    @Test
    public void deleteRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        String toolId = "sakai.assignment";
        String toolItem1 = "item1";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.getId().toString());
        Optional<ToolItemRubricAssociation> optAssociation1
            = rubricsService.saveRubricAssociation(toolId, toolItem1, rbcsParams);
        assertTrue(optAssociation1.isPresent());
        ToolItemRubricAssociation association1 = optAssociation1.get();

        String toolItem2 = "item2";
        Optional<ToolItemRubricAssociation> optAssociation2
            = rubricsService.saveRubricAssociation(toolId, toolItem2, rbcsParams);
        assertTrue(optAssociation2.isPresent());
        ToolItemRubricAssociation association2 = optAssociation2.get();

        List<ToolItemRubricAssociation> associations = associationRepository.findAll();
        assertEquals(2, associations.size());

        EvaluationTransferBean etb1 = buildEvaluation(association1.getId(), rubricBean, toolItem1);
        etb1 = rubricsService.saveEvaluation(etb1, siteId);
        EvaluationTransferBean etb2 = buildEvaluation(association2.getId(), rubricBean, toolItem2);
        etb2 = rubricsService.saveEvaluation(etb2, siteId);

        List<Evaluation> evaluations = evaluationRepository.findAll();
        assertEquals(2, evaluations.size());

        rubricsService.deleteRubric(rubricBean.getId());

        associations = associationRepository.findAll();
        assertEquals(0, associations.size());

        evaluations = evaluationRepository.findAll();
        assertEquals(0, evaluations.size());

        assertFalse(rubricsService.getRubric(rubricBean.getId()).isPresent());
    }

    @Test
    public void deleteSiteRubrics() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        String toolId = "sakai.assignment";
        String toolItem1 = "item1";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.getId().toString());
        Optional<ToolItemRubricAssociation> optAssociation1
                = rubricsService.saveRubricAssociation(toolId, toolItem1, rbcsParams);
        assertTrue(optAssociation1.isPresent());
        ToolItemRubricAssociation association1 = optAssociation1.get();

        String toolItem2 = "item2";
        Optional<ToolItemRubricAssociation> optAssociation2
                = rubricsService.saveRubricAssociation(toolId, toolItem2, rbcsParams);
        assertTrue(optAssociation2.isPresent());
        ToolItemRubricAssociation association2 = optAssociation2.get();

        assertEquals(2, associationRepository.findAll().size());

        EvaluationTransferBean etb1 = buildEvaluation(association1.getId(), rubricBean, toolItem1);
        etb1 = rubricsService.saveEvaluation(etb1, siteId);
        EvaluationTransferBean etb2 = buildEvaluation(association2.getId(), rubricBean, toolItem2);
        etb2 = rubricsService.saveEvaluation(etb2, siteId);

        assertEquals(2, evaluationRepository.findAll().size());

        rubricsService.deleteSiteRubrics(siteId);

        assertEquals(0, associationRepository.findAll().size());
        assertEquals(0, evaluationRepository.findAll().size());
        assertEquals(0, rubricsService.getRubricsForSite(siteId).size());
    }

    @Test
    public void updateCriteria() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterion1Bean = rubricBean.getCriteria().get(0);
        String newTitle = "Taste";
        criterion1Bean.setTitle(newTitle);
        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.updateCriterion(criterion1Bean, siteId));
        switchToInstructor();
        CriterionTransferBean newBean = rubricsService.updateCriterion(criterion1Bean, siteId);
        assertEquals(newBean.getTitle(), newTitle);
    }

    @Test
    public void createDefaultCriterion() {

        switchToInstructor();
        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.createDefaultCriterion(siteId, rubricBean.getId()));

        switchToInstructor();

        Optional<CriterionTransferBean> optCriterionBean = rubricsService.createDefaultCriterion(siteId, rubricBean.getId());
        assertTrue(optCriterionBean.isPresent());
        CriterionTransferBean criterionBean = optCriterionBean.get();
        assertNotNull(criterionBean.getId());
        assertEquals(criterionBean.getTitle(), defaultCriterionTitle);
        assertEquals(3, criterionBean.getRatings().size());

        RubricTransferBean rubricBean2 = rubricsService.getRubricsForSite(siteId).get(0);
        assertEquals(3, rubricBean2.getCriteria().size());
    }

    @Test
    public void deleteCriterion() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        assertEquals(2, rubricBean.getCriteria().size());

        Long firstId = rubricBean.getCriteria().get(0).getId();
        rubricsService.deleteCriterion(rubricBean.getId(), firstId, siteId);
        Optional<RubricTransferBean> optRubricBean = rubricsService.getRubric(rubricBean.getId());
        assertTrue(optRubricBean.isPresent());
        assertEquals(1, optRubricBean.get().getCriteria().size());
    }

    @Test
    public void createDefaultRating() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterionBean = rubricBean.getCriteria().get(0);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRating(siteId, criterionBean.getId(), 0));

        switchToInstructor();

        Optional<RatingTransferBean> optRatingBean = rubricsService.createDefaultRating(siteId, criterionBean.getId(), 0);
        assertTrue(optRatingBean.isPresent());
        RatingTransferBean ratingBean = optRatingBean.get();
        assertNotNull(ratingBean.getId());
        //assertTrue(ratingBean.title.equals(defaultRatingTitle));
        RubricTransferBean rubricBean2 = rubricsService.getRubricsForSite(siteId).get(0);
        assertEquals(4, rubricBean2.getCriteria().get(0).getRatings().size());
    }

    @Test
    public void deleteRating() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterionBean = rubricBean.getCriteria().get(1);
        int initialRatingsSize = criterionBean.getRatings().size();
        RatingTransferBean ratingBean = criterionBean.getRatings().get(2);

        System.out.println(ratingBean.getPoints());

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.deleteRating(ratingBean.getId(), criterionBean.getId(), siteId));

        switchToInstructor();
        rubricsService.deleteRating(ratingBean.getId(), criterionBean.getId(), siteId);
        rubricsService.getCriterion(criterionBean.getId(), siteId).ifPresent(bean -> assertEquals(bean.getRatings().size(), initialRatingsSize - 1));
        RubricTransferBean rubricBean2 = rubricsService.getRubricsForSite(siteId).get(0);

    }

    @Test
    public void saveRubricAssociation() {

        switchToInstructor();

        String toolId = "sakai.assignment";
        String toolItemId = "item1";

        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.getId().toString());
        Optional<ToolItemRubricAssociation> association = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams);
        assertTrue(association.isPresent());
        assertEquals(rubricBean.getId(), association.get().getRubric().getId());
    }

    @Test
    public void saveEvaluation() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        String toolId = "sakai.assignment";
        String toolItemId = "item1";
        String originalComment = "This is the original comment";
        String updatedComment = "This is the updated comment";

        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.getId().toString());
        ToolItemRubricAssociation association
            = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams).get();

        EvaluationTransferBean etb = buildEvaluation(association.getId(), rubricBean, toolItemId);
        assertEquals(association.getId(), etb.getAssociationId());

        etb.setStatus(EvaluationStatus.DRAFT);
        etb.setOverallComment(originalComment);
        etb.setNew(true);

        etb = rubricsService.saveEvaluation(etb, siteId);
        assertEquals(EvaluationStatus.DRAFT, etb.getStatus());
        Optional<ReturnedEvaluation> returnedEvaluation
            = returnedEvaluationRepository.findByOriginalEvaluationId(etb.getId());
        assertFalse(returnedEvaluation.isPresent());
        rubricBean = rubricsService.createDefaultRubric(siteId);

        switchToUser1();
        Optional<EvaluationTransferBean> optEtb = rubricsService.getEvaluation(etb.getId(), siteId);
        assertFalse(optEtb.isPresent());

        switchToInstructor();
        optEtb = rubricsService.getEvaluation(etb.getId(), siteId);
        assertTrue(optEtb.isPresent());
        assertNotNull(optEtb.get().getCreatorId());
        assertNotNull(optEtb.get().getCreated());
        assertEquals(association.getId(), optEtb.get().getAssociationId());

        switchToUser2();

        Optional<EvaluationTransferBean> optEtb2 = rubricsService.getEvaluation(etb.getId(), siteId);
        assertFalse(optEtb2.isPresent());

        switchToInstructor();
        optEtb2 = rubricsService.getEvaluation(etb.getId(), siteId);
        optEtb2.get().setStatus(EvaluationStatus.RETURNED);
        etb = rubricsService.saveEvaluation(optEtb2.get(), siteId);
        assertNotNull(etb.getModified());

        returnedEvaluation = returnedEvaluationRepository.findByOriginalEvaluationId(etb.getId());
        assertTrue(returnedEvaluation.isPresent());

        // Now the evaluation has been returned, the evaluee, user2, should be able to view it.
        switchToUser2();
        optEtb2 = rubricsService.getEvaluation(etb.getId(), siteId);
        assertTrue(optEtb2.isPresent());

        switchToUser3();
        Optional<EvaluationTransferBean> none = rubricsService.getEvaluation(etb.getId(), siteId);
        assertFalse(none.isPresent());

        // Now save it as draft again, so we can test cancel.
        switchToInstructor();
        optEtb2.get().setStatus(EvaluationStatus.DRAFT);
        optEtb2.get().setOverallComment(updatedComment);
        etb = rubricsService.saveEvaluation(optEtb2.get(), siteId);

        etb = rubricsService.cancelDraftEvaluation(etb.getId());
        assertEquals(originalComment, etb.getOverallComment());
    }

    private EvaluationTransferBean buildEvaluation(Long associationId, RubricTransferBean rubricBean, String toolItemId) {

        EvaluationTransferBean etb = new EvaluationTransferBean();
        etb.setAssociationId(associationId);
        etb.setEvaluatorId(user1);
        etb.setEvaluatedItemId(toolItemId);
        etb.setEvaluatedItemOwnerId(user2);
        etb.setEvaluatedItemOwnerType(EvaluatedItemOwnerType.USER);

        List<CriterionOutcomeTransferBean> criterionOutcomes = new ArrayList<>();

        rubricBean.getCriteria().forEach(ctb -> {
            CriterionOutcomeTransferBean cotb = new CriterionOutcomeTransferBean();
            cotb.setCriterionId(ctb.getId());
            cotb.setSelectedRatingId(ctb.getRatings().get(0).getId());
            cotb.setPoints(ctb.getRatings().get(0).getPoints());
            criterionOutcomes.add(cotb);
        });
        etb.getCriterionOutcomes().addAll(criterionOutcomes);
        return etb;
    }

    private void setupStudentPermissions() {
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteRef)).thenReturn(false);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, siteRef)).thenReturn(false);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, siteRef)).thenReturn(true);
    }

    private void switchToUser1() {

        setupStudentPermissions();

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        when(userDirectoryService.getCurrentUser()).thenReturn(user1User);
        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser2() {

        setupStudentPermissions();

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        when(userDirectoryService.getCurrentUser()).thenReturn(user2User);
        try {
            when(userDirectoryService.getUser(user2)).thenReturn(user2User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser3() {

        setupStudentPermissions();

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user3);
        when(userDirectoryService.getCurrentUser()).thenReturn(user3User);
        try {
            when(userDirectoryService.getUser(user3)).thenReturn(user3User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToInstructor() {

        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteRef)).thenReturn(true);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, siteRef)).thenReturn(true);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, siteRef)).thenReturn(false);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(instructor);
        when(userDirectoryService.getCurrentUser()).thenReturn(instructorUser);
        try {
            when(userDirectoryService.getUser(instructor)).thenReturn(instructorUser);
        } catch (UserNotDefinedException unde) {
        }
    }
}
