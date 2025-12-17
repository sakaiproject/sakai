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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.CriterionOutcomeTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.Criterion;
import org.sakaiproject.rubrics.api.model.EvaluatedItemOwnerType;
import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;
import org.sakaiproject.rubrics.api.model.Rating;
import org.sakaiproject.rubrics.api.model.ReturnedEvaluation;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.CriterionRepository;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
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
import org.sakaiproject.util.Xml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RubricsTestConfiguration.class})
public class RubricsServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AssociationRepository associationRepository;
    @Autowired private CriterionRepository criterionRepository;
    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private ReturnedEvaluationRepository returnedEvaluationRepository;
    @Autowired private RubricRepository rubricRepository;
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
    String user1DisplayName = "User 1";
    String user1SortName = "1, User";
    User user1User = null;
    String user2 = "user2";
    String user2SortName = "2, User";
    User user2User = null;
    String user3 = "user3";
    User user3User = null;
    String instructor = "instructor";
    User instructorUser = null;
    String teachingAssistant = "TA";
    User teachingAssistantUser = null;
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
        when(user1User.getSortName()).thenReturn(user1SortName);

        user2User = mock(User.class);
        when(user2User.getId()).thenReturn(user2);
        when(user2User.getDisplayName()).thenReturn("User 2");
        when(user2User.getSortName()).thenReturn("2, User");

        user3User = mock(User.class);
        when(user3User.getId()).thenReturn(user3);
        when(user3User.getDisplayName()).thenReturn("3, User");

        instructorUser = mock(User.class);
        when(instructorUser.getId()).thenReturn(instructor);
        when(instructorUser.getDisplayName()).thenReturn("Instructor");

        teachingAssistantUser = mock(User.class);
        when(teachingAssistantUser.getId()).thenReturn(teachingAssistant);
        when(teachingAssistantUser.getDisplayName()).thenReturn("TA");

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

        switchToTeachingAssistant();

        // Should still throw it as the user doesn't have rubrics.editor
        assertThrows(SecurityException.class, () -> rubricsService.saveRubric(rubricBean1));
    }

    @Test
    public void createDefaultRubric() {

        switchToUser1();

        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRubric(siteId));

        switchToTeachingAssistant();

        // Should still throw it as the user doesn't have rubrics.editor
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
        // Expect empty list instead of exception
        List<RubricTransferBean> userRubrics = rubricsService.getRubricsForSite(siteId);
        assertTrue(userRubrics.isEmpty());

        switchToTeachingAssistant();
        // Should also return empty list
        List<RubricTransferBean> taRubrics = rubricsService.getRubricsForSite(siteId);
        assertTrue(taRubrics.isEmpty());

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
        assertEquals(newTitle, rubricBean.getTitle());
    }

    @Test
    public void copyRubric() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        assertEquals(1, rubricsService.getRubricsForSite(siteId).size());

        switchToUser1();
        assertThrows(SecurityException. class, () -> rubricsService.copyRubricToSite(rubricBean.getId(), siteId));

        switchToTeachingAssistant();

        // Should still throw it as the user doesn't have rubrics.editor
        assertThrows(SecurityException.class, () -> rubricsService.copyRubricToSite(rubricBean.getId(), siteId));

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

        // This should fail as we have evaluations
        boolean deletedOk = rubricsService.deleteRubric(rubricBean.getId());

        assertFalse(deletedOk);

        associations = associationRepository.findAll();
        assertEquals(2, associations.size());

        evaluationRepository.findAll().forEach(ev -> evaluationRepository.delete(ev));

        deletedOk = rubricsService.deleteRubric(rubricBean.getId());

        assertTrue(deletedOk);

        assertFalse(rubricsService.getRubric(rubricBean.getId()).isPresent());

        List<Criterion> criteria = criterionRepository.findAll();
        assertEquals(0, criteria.size());

        associations = associationRepository.findAll();
        assertEquals(0, associations.size());
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
        assertEquals(22D, rubricBean.getMaxPoints(), 0);
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

        switchToTeachingAssistant();

        // Should still throw it as the user doesn't have rubrics.editor
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
        assertEquals(24D, rubricBean2.getMaxPoints(), 0);
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
        assertEquals(20D, optRubricBean.get().getMaxPoints(), 0);
    }

    @Test
    public void createDefaultRating() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterionBean = rubricBean.getCriteria().get(0);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRating(siteId, rubricBean.getId(), criterionBean.getId(), 0));

        switchToTeachingAssistant();

        // Should still throw it as the user doesn't have rubrics.editor
        assertThrows(SecurityException.class, () -> rubricsService.createDefaultRating(siteId, rubricBean.getId(), criterionBean.getId(), 0));

        switchToInstructor();

        Optional<RatingTransferBean> optRatingBean = rubricsService.createDefaultRating(siteId, rubricBean.getId(), criterionBean.getId(), 0);
        assertTrue(optRatingBean.isPresent());
        RatingTransferBean ratingBean = optRatingBean.get();
        assertNotNull(ratingBean.getId());
        //assertTrue(ratingBean.title.equals(defaultRatingTitle));
        RubricTransferBean rubricBean2 = rubricsService.getRubricsForSite(siteId).get(0);
        assertEquals(4, rubricBean2.getCriteria().get(0).getRatings().size());
        assertEquals(22D, rubricBean2.getMaxPoints(), 0);
    }

    @Test
    public void deleteRating() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterionBean = rubricBean.getCriteria().get(1);
        int initialRatingsSize = criterionBean.getRatings().size();
        RatingTransferBean ratingBean = criterionBean.getRatings().get(2);

        switchToUser1();
        assertThrows(SecurityException.class, () -> rubricsService.deleteRating(ratingBean.getId(), criterionBean.getId(), siteId, rubricBean.getId()));

        switchToTeachingAssistant();

        // Should still throw it as the user doesn't have rubrics.editor
        assertThrows(SecurityException.class, () -> rubricsService.deleteRating(ratingBean.getId(), criterionBean.getId(), siteId, rubricBean.getId()));

        switchToInstructor();
        rubricsService.deleteRating(ratingBean.getId(), criterionBean.getId(), siteId, rubricBean.getId());
        rubricsService.getCriterion(criterionBean.getId(), siteId).ifPresent(bean -> assertEquals(bean.getRatings().size(), initialRatingsSize - 1));
        RubricTransferBean rubricBean2 = rubricsService.getRubricsForSite(siteId).get(0);
        assertEquals(22D, rubricBean2.getMaxPoints(), 0);
    }

    @Test
    public void maxPoints() {

        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        CriterionTransferBean criterionBean = rubricBean.getCriteria().get(1);
        Optional<RatingTransferBean> ratingBean
            = criterionBean.getRatings().stream().filter(r -> r.getPoints() == 20D).findAny();

        if (ratingBean.isPresent()) {
            rubricsService.deleteRating(ratingBean.get().getId(), criterionBean.getId(), siteId, rubricBean.getId());
        }

        RubricTransferBean rubricBean2 = rubricsService.getRubricsForSite(siteId).get(0);
        assertEquals(17D, rubricBean2.getMaxPoints(), 0);

        rubricBean = rubricsService.createDefaultRubric(siteId);

        Long firstId = rubricBean.getCriteria().get(0).getId();
        rubricsService.deleteCriterion(rubricBean.getId(), firstId, siteId);
        rubricBean2 = rubricsService.getRubricsForSite(siteId).get(1);
        assertEquals(20D, rubricBean2.getMaxPoints(), 0);

        criterionBean = rubricBean2.getCriteria().get(0);
        ratingBean = criterionBean.getRatings().stream().filter(r -> r.getPoints() == 15D).findAny();
        if (ratingBean.isPresent()) {
            ratingBean.get().setPoints(43D);
            rubricsService.updateRating(ratingBean.get(), siteId);
        }
        rubricBean2 = rubricsService.getRubricsForSite(siteId).get(1);
        assertEquals(43D, rubricBean2.getMaxPoints(), 0);

        rubricBean = rubricsService.createDefaultRubric(siteId);
        rubricsService.copyCriterion(rubricBean.getId(), rubricBean.getCriteria().get(1).getId());
        rubricBean2 = rubricsService.getRubricsForSite(siteId).get(2);
        assertEquals(42D, rubricBean2.getMaxPoints(), 0);
    }

    @Test
    public void saveRubricAssociation() {

        switchToInstructor();

        String toolId = "sakai.assignment";
        String toolItemId = "item1";

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        Map<String, String> rbcsParams = Map.of(RubricsConstants.RBCS_ASSOCIATE, "1",
                                                RubricsConstants.RBCS_LIST, rubricBean.getId().toString());

        Optional<ToolItemRubricAssociation> association = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams);
        assertTrue(association.isPresent());
        assertEquals(rubricBean.getId(), association.get().getRubric().getId());

        // test switching associations
        RubricTransferBean rubricBean2 = rubricsService.createDefaultRubric(siteId);
        Map<String, String> rbcsParams2 = new HashMap<>();
        rbcsParams2.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams2.put(RubricsConstants.RBCS_LIST, rubricBean2.getId().toString());
        Optional<ToolItemRubricAssociation> association2 = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams2);
        assertTrue(association2.isPresent());
        assertEquals(rubricBean2.getId(), association2.get().getRubric().getId());

        // remove association
        Map<String, String> rbcsParams3 = new HashMap<>();
        rbcsParams3.put(RubricsConstants.RBCS_ASSOCIATE, "");
        rbcsParams3.put(RubricsConstants.RBCS_LIST, "0");
        Optional<ToolItemRubricAssociation> association3 = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams3);
        assertFalse(association3.isPresent());

        // random text string
        Map<String, String> rbcsParams4 = new HashMap<>();
        rbcsParams4.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams4.put(RubricsConstants.RBCS_LIST, "one");
        Optional<ToolItemRubricAssociation> association4 = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams3);
        assertFalse(association4.isPresent());
    }

    @Test
    public void deleteRubricAssociation() {

        switchToInstructor();

        String toolId = "sakai.assignment";
        String toolItemId = "item1";

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        Map<String, String> rbcsParams = Map.of(RubricsConstants.RBCS_ASSOCIATE, "1",
                                                    RubricsConstants.RBCS_LIST, rubricBean.getId().toString());

        Optional<ToolItemRubricAssociation> optAssociation = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams);
        assertTrue(optAssociation.isPresent());

        assertTrue(rubricsService.getRubricAssociation(toolId, toolItemId).isPresent());

        rubricsService.deleteRubricAssociation(toolId, toolItemId);

        assertFalse(rubricsService.getRubricAssociation(toolId, toolItemId).isPresent());
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
        ToolItemRubricAssociation association = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams).get();

        rubricsService.getRubric(rubricBean.getId()).ifPresent(r -> assertFalse(r.getLocked()));

        EvaluationTransferBean etb = buildEvaluation(association.getId(), rubricBean, toolItemId);
        assertEquals(association.getId(), etb.getAssociationId());

        etb.setStatus(EvaluationStatus.DRAFT);
        etb.setOverallComment(originalComment);

        etb = rubricsService.saveEvaluation(etb, siteId);
        assertEquals(EvaluationStatus.DRAFT, etb.getStatus());
        Optional<ReturnedEvaluation> returnedEvaluation = returnedEvaluationRepository.findByOriginalEvaluationId(etb.getId());
        assertFalse(returnedEvaluation.isPresent());

        switchToUser1();
        Optional<EvaluationTransferBean> optEtb = rubricsService.getEvaluation(etb.getId(), siteId);
        assertFalse(optEtb.isPresent());

        switchToInstructor();
        optEtb = rubricsService.getEvaluation(etb.getId(), siteId);
        assertTrue(optEtb.isPresent());
        rubricsService.getRubric(rubricBean.getId()).ifPresent(r -> assertTrue(r.getLocked()));
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

        List<String> userIds = new ArrayList<>();
        userIds.add(user2);
        List<User> users = new ArrayList<>();
        users.add(user2User);
        when(userDirectoryService.getUsers(userIds)).thenReturn(users);

        List<EvaluationTransferBean> evaluations = rubricsService.getEvaluationsForToolAndItem(toolId, toolItemId, siteId);
        assertEquals(1, evaluations.size());
        assertEquals(user2SortName, evaluations.get(0).getSortName());

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

    @Test
    public void saveEvaluationAsTeachingAssistant() {

        // Only users with a rubrics.editor role can create rubric and save rubric association
        switchToInstructor();

        RubricTransferBean rubricBean = rubricsService.createDefaultRubric(siteId);

        String toolId = "sakai.assignment";
        String toolItemId = "item1";
        String originalComment = "This is the original comment";
        String updatedComment = "This is the updated comment";

        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubricBean.getId().toString());
        ToolItemRubricAssociation association =
                rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams).get();

        // TA can save evaluation
        switchToTeachingAssistant();

        EvaluationTransferBean etb = buildEvaluation(association.getId(), rubricBean, toolItemId);
        assertEquals(association.getId(), etb.getAssociationId());

        etb.setStatus(EvaluationStatus.DRAFT);
        etb.setOverallComment(originalComment);

        etb = rubricsService.saveEvaluation(etb, siteId);
        assertEquals(EvaluationStatus.DRAFT, etb.getStatus());
        Optional<ReturnedEvaluation> returnedEvaluation
                = returnedEvaluationRepository.findByOriginalEvaluationId(etb.getId());
        assertFalse(returnedEvaluation.isPresent());

        switchToUser1();
        Optional<EvaluationTransferBean> optEtb = rubricsService.getEvaluation(etb.getId(), siteId);
        assertFalse(optEtb.isPresent());

        switchToTeachingAssistant();
        optEtb = rubricsService.getEvaluation(etb.getId(), siteId);
        assertTrue(optEtb.isPresent());
        assertNotNull(optEtb.get().getCreatorId());
        assertNotNull(optEtb.get().getCreated());
        assertEquals(association.getId(), optEtb.get().getAssociationId());

        switchToUser2();

        Optional<EvaluationTransferBean> optEtb2 = rubricsService.getEvaluation(etb.getId(), siteId);
        assertFalse(optEtb2.isPresent());

        switchToTeachingAssistant();
        optEtb2 = rubricsService.getEvaluation(etb.getId(), siteId);
        optEtb2.get().setStatus(EvaluationStatus.RETURNED);
        etb = rubricsService.saveEvaluation(optEtb2.get(), siteId);
        assertNotNull(etb.getModified());

        List<String> userIds = new ArrayList<>();
        userIds.add(user2);
        List<User> users = new ArrayList<>();
        users.add(user2User);
        when(userDirectoryService.getUsers(userIds)).thenReturn(users);

        List<EvaluationTransferBean> evaluations = rubricsService.getEvaluationsForToolAndItem(toolId, toolItemId, siteId);
        assertEquals(1, evaluations.size());
        assertEquals(user2SortName, evaluations.get(0).getSortName());

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
        switchToTeachingAssistant();
        optEtb2.get().setStatus(EvaluationStatus.DRAFT);
        optEtb2.get().setOverallComment(updatedComment);
        etb = rubricsService.saveEvaluation(optEtb2.get(), siteId);

        etb = rubricsService.cancelDraftEvaluation(etb.getId());
        assertEquals(originalComment, etb.getOverallComment());
    }

    @Test
    public void contextualFilenameNoEvaluation() {

        switchToInstructor();
        RubricTransferBean rubric = rubricsService.createDefaultRubric(siteId);
        String toolId = "sakai.assignment";
        String toolItem1 = "item1";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubric.getId().toString());
        Optional<ToolItemRubricAssociation> optAssociation1 = rubricsService.saveRubricAssociation(toolId, toolItem1, rbcsParams);
        assertTrue(optAssociation1.isPresent());
        String filename = rubricsService.createContextualFilename(rubric, toolId, toolItem1, null, siteId);
        assertEquals(rubric.getTitle(), filename);
    }

    @Test
    public void contextualFilename() throws UserNotDefinedException {

        switchToInstructor();
        RubricTransferBean rubric = rubricsService.createDefaultRubric(siteId);
        String toolId = "sakai.assignment";
        String toolItem1 = "item1";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubric.getId().toString());
        Optional<ToolItemRubricAssociation> optAssociation1 = rubricsService.saveRubricAssociation(toolId, toolItem1, rbcsParams);
        assertTrue(optAssociation1.isPresent());
        ToolItemRubricAssociation association1 = optAssociation1.get();
        EvaluationTransferBean evaluation1 = buildEvaluation(association1.getId(), rubric, toolItem1);
        evaluation1.setEvaluatedItemOwnerId(user1User.getId());
        rubricsService.saveEvaluation(evaluation1, siteId);
        assertNotNull(evaluation1);
        when(userDirectoryService.getUser("user1")).thenReturn(user1User);
        String filename = rubricsService.createContextualFilename(rubric, toolId, toolItem1, evaluation1.getEvaluatedItemId(), siteId);
        assertEquals(rubric.getTitle() + '_' + user1SortName, filename);
    }

    @Test
    public void concurrentSaveSameEvaluationKeepsSingleRow() throws Exception {
        switchToInstructor();
        RubricTransferBean rubric = rubricsService.createDefaultRubric(siteId);
        String toolId = "sakai.assignment";
        String toolItemId = "item-concurrent";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubric.getId().toString());
        ToolItemRubricAssociation association = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams)
            .orElseThrow(() -> new IllegalStateException("Association not created"));

        EvaluationTransferBean eval1 = buildEvaluation(association.getId(), rubric, toolItemId);
        EvaluationTransferBean eval2 = buildEvaluation(association.getId(), rubric, toolItemId);

        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService exec = Executors.newFixedThreadPool(2);
        Callable<EvaluationTransferBean> task1 = () -> {
            startLatch.await();
            return rubricsService.saveEvaluation(eval1, siteId);
        };
        Callable<EvaluationTransferBean> task2 = () -> {
            startLatch.await();
            return rubricsService.saveEvaluation(eval2, siteId);
        };

        Future<EvaluationTransferBean> f1 = exec.submit(task1);
        Future<EvaluationTransferBean> f2 = exec.submit(task2);
        startLatch.countDown();

        EvaluationTransferBean saved1 = f1.get();
        EvaluationTransferBean saved2 = f2.get();

        assertNotNull(saved1);
        assertNotNull(saved2);
        List<Evaluation> evaluations = evaluationRepository.findByAssociationId(association.getId());
        assertEquals(1, evaluations.size());
        assertTrue(evaluationRepository.findByAssociationIdAndEvaluatedItemIdAndOwner(association.getId(), toolItemId, user2).isPresent());

        exec.shutdownNow();
    }

    @Test
    public void findByAssociationIdAndEvaluatedItemIdHandlesMultipleOwners() {
        switchToInstructor();
        RubricTransferBean rubric = rubricsService.createDefaultRubric(siteId);
        String toolId = "sakai.assignment";
        String toolItemId = "item-duplicates";
        Map<String, String> rbcsParams = new HashMap<>();
        rbcsParams.put(RubricsConstants.RBCS_ASSOCIATE, "1");
        rbcsParams.put(RubricsConstants.RBCS_LIST, rubric.getId().toString());
        ToolItemRubricAssociation association = rubricsService.saveRubricAssociation(toolId, toolItemId, rbcsParams)
            .orElseThrow(() -> new IllegalStateException("Association not created"));

        EvaluationTransferBean evalUser2 = buildEvaluation(association.getId(), rubric, toolItemId);
        EvaluationTransferBean evalUser3 = buildEvaluation(association.getId(), rubric, toolItemId);
        evalUser3.setEvaluatedItemOwnerId(user3);

        rubricsService.saveEvaluation(evalUser2, siteId);
        rubricsService.saveEvaluation(evalUser3, siteId);

        Optional<Evaluation> opt = evaluationRepository.findByAssociationIdAndEvaluatedItemId(association.getId(), toolItemId);
        assertTrue(opt.isPresent());
        assertTrue(Arrays.asList(user2, user3).contains(opt.get().getEvaluatedItemOwnerId()));
        assertEquals(2, evaluationRepository.findByAssociationId(association.getId()).size());
    }

    @Test
    public void archive() {

        switchToInstructor();

        RubricTransferBean rubric1 = rubricsService.createDefaultRubric(siteId);
        String title1 = "Cheese Sandwich";
        rubric1.setTitle(title1);
        String rubric1Criteria1Rating1Description = "This is great";
        rubric1.getCriteria().get(0).getRatings().get(0).setDescription(rubric1Criteria1Rating1Description);
        rubricsService.saveRubric(rubric1);

        String rubric2CriteriaDescription = "Rate those sandwiches";
        RubricTransferBean rubric2 = rubricsService.createDefaultRubric(siteId);
        String title2 = "Egg Sandwich";
        rubric2.setTitle(title2);
        rubric2.getCriteria().get(0).setDescription(rubric2CriteriaDescription);
        rubricsService.saveRubric(rubric2);

        RubricTransferBean rubric3 = rubricsService.createDefaultRubric(siteId);
        String title3 = "Ham Sandwich";
        rubric3.setTitle(title3);
        rubricsService.saveRubric(rubric3);

        RubricTransferBean[] rubrics = new RubricTransferBean[] { rubric1, rubric2, rubric3 };

        Document doc = Xml.createDocument();
        Stack<Element> stack = new Stack<>();

        Element root = doc.createElement("archive");
        doc.appendChild(root);
        root.setAttribute("source", siteId);
        root.setAttribute("xmlns:sakai", ArchiveService.SAKAI_ARCHIVE_NS);
        root.setAttribute("xmlns:CHEF", ArchiveService.SAKAI_ARCHIVE_NS.concat("CHEF"));
        root.setAttribute("xmlns:DAV", ArchiveService.SAKAI_ARCHIVE_NS.concat("DAV"));
        stack.push(root);

        String results = rubricsService.archive(siteId, doc, stack, "", null);

        assertEquals(1, stack.size());

        NodeList rubricsNode = root.getElementsByTagName(rubricsService.getLabel());
        assertEquals(1, rubricsNode.getLength());

        NodeList rubricNodes = ((Element) rubricsNode.item(0)).getElementsByTagName("rubric");
        assertEquals(3, rubricNodes.getLength());

        for (int i = 0; i < rubricNodes.getLength(); i++) {

            RubricTransferBean rubricBean = rubrics[i];
            Element rubricEl = (Element) rubricNodes.item(i);

            assertEquals(rubricBean.getTitle(), rubricEl.getAttribute("title"));
            assertEquals(Double.toString(rubricBean.getMaxPoints()), rubricEl.getAttribute("max-points"));
            assertEquals(Boolean.toString(rubricBean.getWeighted()), rubricEl.getAttribute("weighted"));
            assertEquals(Boolean.toString(rubricBean.getAdhoc()), rubricEl.getAttribute("adhoc"));

            NodeList criteriaNodes = rubricEl.getElementsByTagName("criteria");
            assertEquals(1, criteriaNodes.getLength());

            NodeList criterionNodes = ((Element) criteriaNodes.item(0)).getElementsByTagName("criterion");
            assertEquals(rubricBean.getCriteria().size(), criterionNodes.getLength());

            for (int j = 0; j < criterionNodes.getLength(); j++) {

                CriterionTransferBean criterionBean = rubrics[i].getCriteria().get(j);
                Element criterionEl = (Element) criterionNodes.item(j);

                assertEquals(criterionBean.getTitle(), criterionEl.getAttribute("title"));
                assertEquals(Float.toString(criterionBean.getWeight()), criterionEl.getAttribute("weight"));

                String criterionDescription = criterionBean.getDescription();
                if (StringUtils.isNotBlank(criterionDescription)) {
                    NodeList descriptionNodes = criterionEl.getElementsByTagName("description");
                    assertEquals(1, descriptionNodes.getLength());
                    CDATASection descriptionNode = (CDATASection) descriptionNodes.item(0).getFirstChild();
                    assertNotNull(descriptionNode);
                    assertEquals(criterionDescription, descriptionNode.getNodeValue());
                }

                NodeList ratingsNodes = criterionEl.getElementsByTagName("ratings");
                assertEquals(1, ratingsNodes.getLength());

                NodeList ratingNodes = ((Element) ratingsNodes.item(0)).getElementsByTagName("rating");
                assertEquals(criterionBean.getRatings().size(), ratingNodes.getLength());

                for (int k = 0; k < ratingNodes.getLength(); k++) {

                    RatingTransferBean ratingBean = criterionBean.getRatings().get(k);
                    Element ratingEl = (Element) ratingNodes.item(k);

                    assertEquals(ratingBean.getTitle(), ratingEl.getAttribute("title"));
                    assertEquals(Double.toString(ratingBean.getPoints()), ratingEl.getAttribute("points"));

                    String ratingDescription = ratingBean.getDescription();
                    if (StringUtils.isNotBlank(ratingDescription)) {
                        NodeList ratingDescriptionNodes = ratingEl.getElementsByTagName("description");
                        CDATASection ratingDescriptionNode = (CDATASection) ratingDescriptionNodes.item(0).getFirstChild();
                        assertNotNull(ratingDescriptionNode);
                        assertEquals(ratingDescription, ratingDescriptionNode.getNodeValue());
                    }
                }
            }
        }
    }

    @Test
    public void merge() {

        Document doc = Xml.readDocumentFromStream(this.getClass().getResourceAsStream("/archive/rubrics.xml"));

        Element root = doc.getDocumentElement();

        String fromSite = root.getAttribute("source");
        String toSite = "my-new-site";

        switchToInstructor(toSite);

        Element rubricsElement = doc.createElement("not-rubrics");

        rubricsService.merge(toSite, rubricsElement, "", fromSite, null, null, null);

        assertEquals("Invalid xml document", rubricsService.merge(siteId, rubricsElement, "", fromSite, null, null, null));

        rubricsElement = (Element) root.getElementsByTagName(rubricsService.getLabel()).item(0);

        rubricsService.merge(toSite, rubricsElement, "", fromSite, null, null, null);

        NodeList rubricNodes = rubricsElement.getElementsByTagName("rubric");

        List<Rubric> rubrics = rubricRepository.findByOwnerId(toSite);

        for (int i = 0; i < rubricNodes.getLength(); i++) {
            Element rubricEl = (Element) rubricNodes.item(i);
            String rubricTitle = rubricEl.getAttribute("title");
            Optional<Rubric> optRubric = rubrics.stream().filter(r -> r.getTitle().equals(rubricTitle)).findAny();
            assertTrue(optRubric.isPresent());
            Rubric rubric = optRubric.get();
            //assertEquals(Double.valueOf(rubricEl.getAttribute("max-points")), rubric.getMaxPoints());
            assertEquals(Boolean.valueOf(rubricEl.getAttribute("adhoc")), rubric.getAdhoc());
            assertEquals(Boolean.valueOf(rubricEl.getAttribute("weighted")), rubric.getWeighted());

            NodeList criterionNodes = ((Element) rubricEl.getElementsByTagName("criteria").item(0))
                                        .getElementsByTagName("criterion");
            List<Criterion> criteria = rubric.getCriteria();
            assertEquals(criterionNodes.getLength(), criteria.size());

            for (int j = 0; j < criterionNodes.getLength(); j++) {
                Element criterionEl = (Element) criterionNodes.item(j);
                String criterionTitle = criterionEl.getAttribute("title");
                Optional<Criterion> optCriterion = criteria.stream().filter(c -> c.getTitle().equals(criterionEl.getAttribute("title"))).findAny();
                assertTrue(optCriterion.isPresent());
                Criterion criterion = optCriterion.get();
                NodeList descriptionNodes = criterionEl.getElementsByTagName("description");
                if (descriptionNodes.getLength() == 1) {
                    assertEquals(descriptionNodes.item(0).getFirstChild().getNodeValue(), criterion.getDescription());
                }

                NodeList ratingNodes = ((Element) criterionEl.getElementsByTagName("ratings").item(0))
                                            .getElementsByTagName("rating");
                List<Rating> ratings = criterion.getRatings();
                for (int k = 0; k < ratingNodes.getLength(); k++) {
                    Element ratingEl = (Element) ratingNodes.item(j);
                    String ratingTitle = ratingEl.getAttribute("title");
                    Optional<Rating> optRating = ratings.stream().filter(r -> r.getTitle().equals(ratingEl.getAttribute("title"))).findAny();
                    assertTrue(optRating.isPresent());
                    Rating rating = optRating.get();
                    assertEquals(Double.valueOf(ratingEl.getAttribute("points")), rating.getPoints());

                    NodeList ratingDescriptionNodes = ratingEl.getElementsByTagName("description");
                    if (ratingDescriptionNodes.getLength() == 1) {
                        assertEquals(ratingDescriptionNodes.item(0).getFirstChild().getNodeValue(), rating.getDescription());
                    }
                }
            }
        }

        // Now let's try and merge again. The original rubrics with the same titles should remain and
        // not be replaced or duplicated.
        rubricsService.merge(toSite, rubricsElement, "", fromSite, null, null, null);

        assertEquals(rubrics.size(), rubricRepository.findByOwnerId(toSite).size());

        Set<String> oldTitles = rubrics.stream().map(Rubric::getTitle).collect(Collectors.toSet());

        // Now let's try and merge this set of rubrics. It has one with a different title, but the
        // rest the same, so we should end up with only one rubric being added.
        Document doc2 = Xml.readDocumentFromStream(this.getClass().getResourceAsStream("/archive/rubrics2.xml"));

        Element root2 = doc2.getDocumentElement();

        rubricsElement = (Element) root2.getElementsByTagName(rubricsService.getLabel()).item(0);

        rubricsService.merge(toSite, rubricsElement, "", fromSite, null, null, null);

        String extraTitle = "Smurfs";

        assertEquals(rubrics.size() + 1, rubricRepository.findByOwnerId(toSite).size());

        Set<String> newTitles = rubricRepository.findByOwnerId(toSite)
            .stream().map(Rubric::getTitle).collect(Collectors.toSet());

        assertFalse(oldTitles.contains(extraTitle));
        assertTrue(newTitles.contains(extraTitle));
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
        switchToInstructor(null);
    }

    private void switchToInstructor(String siteId) {

        String ref = siteId != null ? "/site/" + siteId : siteRef;

        if (siteId != null) {
            when(siteService.siteReference(siteId)).thenReturn(ref);
        }

        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, ref)).thenReturn(true);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, ref)).thenReturn(true);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, ref)).thenReturn(false);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(instructor);
        when(userDirectoryService.getCurrentUser()).thenReturn(instructorUser);
        try {
            when(userDirectoryService.getUser(instructor)).thenReturn(instructorUser);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToTeachingAssistant() {

        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteRef)).thenReturn(false);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, siteRef)).thenReturn(true);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUEE, siteRef)).thenReturn(true);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(teachingAssistant);
        when(userDirectoryService.getCurrentUser()).thenReturn(teachingAssistantUser);
        try {
            when(userDirectoryService.getUser(teachingAssistant)).thenReturn(teachingAssistantUser);
        } catch (UserNotDefinedException unde) {
        }
    }
}
