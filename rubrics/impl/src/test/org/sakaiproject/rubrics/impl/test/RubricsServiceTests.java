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

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionOutcomeTransferBean;
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
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.SessionFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RubricsTestConfiguration.class})
public class RubricsServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Resource private AssociationRepository associationRepository;
    @Resource private EvaluationRepository evaluationRepository;
    @Resource private ReturnedEvaluationRepository returnedEvaluationRepository;
    @Resource private RubricsService rubricsService;
    @Resource private SecurityService securityService;
    @Resource private SessionManager sessionManager;
    @Resource private SessionFactory sessionFactory;
    @Resource private SiteService siteService;
    @Resource private ToolManager toolManager;
    @Resource private UserDirectoryService userDirectoryService;

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

    private static ResourceBundle resourceBundle;
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
        rubricBean1.title = "Rubric 1";
        rubricBean1.ownerId = siteId;

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
        assertTrue(rubricBean.id != null);
        assertTrue(rubricBean.title.equals(defaultRubricTitle));
        assertTrue(rubricBean.criteria.size() == 2);
        CriterionTransferBean crit1 = rubricBean.criteria.get(0);
        assertTrue(crit1.title.equals(defaultC1Title));
        assertTrue(crit1.ratings.size() == 3);
        CriterionTransferBean crit2 = rubricBean.criteria.get(1);
        assertTrue(crit2.title.equals(defaultC2Title));
        assertTrue(crit2.ratings.size() == 5);
    }

    @Test
    public void getRubrics() {

        switchToUser1();

        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRubric(siteId));

        switchToInstructor();
        rubricsService.createDefaultRubric(siteId);
        List<RubricTransferBean> rubrics = rubricsService.getRubricsForSite(siteId);
        assertTrue(rubrics.size() == 1);
        assertTrue(rubrics.get(0).criteria.size() == 2);
    }

    @Test
    public void updateRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        String newTitle = "Cheese Sandwich";
        String newDescription = "Sandwiches of the world";
        rubricBean.title = newTitle;
        rubricBean = rubricsService.saveRubric(rubricBean);
        assertTrue(rubricBean.title.equals(newTitle));
    }

    @Test
    public void copyRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        assertTrue(rubricsService.getRubricsForSite(siteId).size() == 1);

        switchToUser1();
        assertThrows(SecurityException. class, () -> rubricsService.copyRubricToSite(rubricBean.id, siteId));
        switchToInstructor();
        rubricsService.copyRubricToSite(rubricBean.id, siteId);
        assertTrue(rubricsService.getRubricsForSite(siteId).size() == 2);
    }

    @Test
    public void deleteRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        String toolId = "sakai.assignment";
        String toolItem1 = "item1";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.id.toString());
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

        rubricsService.deleteRubric(rubricBean.id);

        associations = associationRepository.findAll();
        assertEquals(0, associations.size());

        evaluations = evaluationRepository.findAll();
        assertEquals(0, evaluations.size());

        assertFalse(rubricsService.getRubric(rubricBean.id).isPresent());
    }

    @Test
    public void updateCriteria() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterion1Bean = rubricBean.criteria.get(0);
        String newTitle = "Taste";
        criterion1Bean.title = newTitle;
        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.saveCriterion(criterion1Bean, siteId));
        switchToInstructor();
        CriterionTransferBean newBean = rubricsService.saveCriterion(criterion1Bean, siteId);
        assertTrue(newBean.title.equals(newTitle));
    }

    @Test
    public void createDefaultCriterion() {

        switchToInstructor();
        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.createDefaultCriterion(siteId, rubricBean.id));

        switchToInstructor();

        Optional<CriterionTransferBean> optCriterionBean = rubricsService.createDefaultCriterion(siteId, rubricBean.id);
        assertTrue(optCriterionBean.isPresent());
        CriterionTransferBean criterionBean = optCriterionBean.get();
        assertTrue(criterionBean.id != null);
        assertTrue(criterionBean.title.equals(defaultCriterionTitle));
        assertTrue(criterionBean.ratings.size() == 3);
        assertTrue(rubricsService.getRubricsForSite(siteId).get(0).criteria.size() == 3);
    }

    @Test
    public void deleteCriterion() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        assertEquals(2, rubricBean.criteria.size());

        Long firstId = rubricBean.criteria.get(0).id;
        rubricsService.deleteCriterion(rubricBean.id, firstId, siteId);
        Optional<RubricTransferBean> optRubricBean = rubricsService.getRubric(rubricBean.id);
        assertTrue(optRubricBean.isPresent());
        assertEquals(1, optRubricBean.get().criteria.size());
    }

    @Test
    public void createDefaultRating() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterionBean = rubricBean.criteria.get(0);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRating(siteId, criterionBean.id, 0));

        switchToInstructor();

        Optional<RatingTransferBean> optRatingBean = rubricsService.createDefaultRating(siteId, criterionBean.id, 0);
        assertTrue(optRatingBean.isPresent());
        RatingTransferBean ratingBean = optRatingBean.get();
        assertTrue(ratingBean.id != null);
        //assertTrue(ratingBean.title.equals(defaultRatingTitle));
        assertTrue(rubricsService.getRubricsForSite(siteId).get(0).criteria.get(0).getRatings().size() == 4);
    }

    @Test
    public void deleteRating() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterionBean = rubricBean.criteria.get(1);
        int initialRatingsSize = criterionBean.ratings.size();
        RatingTransferBean ratingBean = criterionBean.ratings.get(2);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.deleteRating(ratingBean.id, criterionBean.id, siteId));

        switchToInstructor();
        rubricsService.deleteRating(ratingBean.id, criterionBean.id, siteId);
        rubricsService.getCriterion(criterionBean.id, siteId).ifPresent(bean -> {
            assertTrue(bean.ratings.size() == initialRatingsSize - 1);
        });
    }

    @Test
    public void saveRubricAssociation() {

        switchToInstructor();

        String toolId = "sakai.assignment";
        String toolItemId = "item1";

        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.id.toString());
        Optional<ToolItemRubricAssociation> association
            = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams);
        assertTrue(association.isPresent());
        assertEquals(rubricBean.id, association.get().getRubricId());
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
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.id.toString());
        ToolItemRubricAssociation association
            = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams).get();

        EvaluationTransferBean etb = buildEvaluation(association.getId(), rubricBean, toolItemId);
        assertEquals(association.getId(), etb.associationId);

        etb.status = EvaluationStatus.DRAFT;
        etb.overallComment = originalComment;
        etb.isNew = true;

        etb = rubricsService.saveEvaluation(etb, siteId);
        assertEquals(EvaluationStatus.DRAFT, etb.status);
        Optional<ReturnedEvaluation> returnedEvaluation
            = returnedEvaluationRepository.findByOriginalEvaluationId(etb.id);
        assertFalse(returnedEvaluation.isPresent());
        rubricBean = rubricsService.createDefaultRubric(siteId);

        switchToUser1();
        Optional<EvaluationTransferBean> optEtb = rubricsService.getEvaluation(etb.id, siteId);
        assertFalse(optEtb.isPresent());

        switchToInstructor();
        optEtb = rubricsService.getEvaluation(etb.id, siteId);
        assertTrue(optEtb.isPresent());
        assertNotNull(optEtb.get().creatorId);
        assertNotNull(optEtb.get().created);
        assertEquals(association.getId(), optEtb.get().associationId);

        switchToUser2();

        Optional<EvaluationTransferBean> optEtb2 = rubricsService.getEvaluation(etb.id, siteId);
        assertFalse(optEtb2.isPresent());

        switchToInstructor();
        optEtb2 = rubricsService.getEvaluation(etb.id, siteId);
        optEtb2.get().status = EvaluationStatus.RETURNED;
        etb = rubricsService.saveEvaluation(optEtb2.get(), siteId);
        assertNotNull(etb.modified);

        returnedEvaluation = returnedEvaluationRepository.findByOriginalEvaluationId(etb.id);
        assertTrue(returnedEvaluation.isPresent());

        // Now the evaluation has been returned, the evaluee, user2, should be able to view it.
        switchToUser2();
        optEtb2 = rubricsService.getEvaluation(etb.id, siteId);
        assertTrue(optEtb2.isPresent());

        switchToUser3();
        Optional<EvaluationTransferBean> none = rubricsService.getEvaluation(etb.id, siteId);
        assertFalse(none.isPresent());

        // Now save it as draft again, so we can test cancel.
        switchToInstructor();
        optEtb2.get().status = EvaluationStatus.DRAFT;
        optEtb2.get().overallComment = updatedComment;
        etb = rubricsService.saveEvaluation(optEtb2.get(), siteId);

        etb = rubricsService.cancelDraftEvaluation(etb.id);
        assertEquals(originalComment, etb.overallComment);
    }

    private EvaluationTransferBean buildEvaluation(Long associationId, RubricTransferBean rubricBean, String toolItemId) {

        EvaluationTransferBean etb = new EvaluationTransferBean();
        etb.associationId = associationId;
        etb.evaluatorId = user1;
        etb.evaluatedItemId = toolItemId;
        etb.evaluatedItemOwnerId = user2;
        etb.evaluatedItemOwnerType = EvaluatedItemOwnerType.USER;

        List<CriterionOutcomeTransferBean> criterionOutcomes = new ArrayList<>();

        rubricBean.criteria.forEach(ctb -> {

            CriterionOutcomeTransferBean cotb = new CriterionOutcomeTransferBean();
            cotb.criterionId = ctb.id;
            cotb.selectedRatingId = ctb.ratings.get(0).id;
            cotb.points = ctb.ratings.get(0).points;
            criterionOutcomes.add(cotb);
        });
        etb.criterionOutcomes = criterionOutcomes;
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
