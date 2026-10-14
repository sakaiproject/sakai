/**
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.InvalidCategoryException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.*;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.*;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService.TotalScoreCancellationException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = SamigoCancellationTestConfiguration.class)
public class PublishedAssessmentServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired private PublishedAssessmentService service;
    @Autowired private PersistenceService persistenceService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private org.sakaiproject.grading.api.GradingService gradebookService;
    @Autowired private SiteService siteService;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ApplicationContext applicationContext;

    private final String siteId = "cancellation-site";
    private PublishedAssessmentFacade assessment;
    private Long itemId;
    private CategoryDefinition category;
    private Site site;

    @BeforeClass
    public static void initializeLocators() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void setup() throws Exception {
        reset(gradebookService, siteService, toolManager, userDirectoryService);
        ComponentManager.loadComponent("agentHelper", applicationContext.getBean("agentHelper"));
        ComponentManager.loadComponent("gradebookServiceHelper", applicationContext.getBean("gradebookServiceHelper"));
        ComponentManager.loadComponent(org.sakaiproject.grading.api.GradingService.class,
                applicationContext.getBean(org.sakaiproject.grading.api.GradingService.class));
        ComponentManager.loadComponent("PersistenceService", persistenceService);
        ComponentManager.loadComponent(SiteService.class, siteService);
        ComponentManager.loadComponent(ToolManager.class, toolManager);
        ComponentManager.loadComponent(UserDirectoryService.class, userDirectoryService);
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn(siteId);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        User user = mock(User.class);
        when(user.getId()).thenReturn("instructor");
        when(userDirectoryService.getCurrentUser()).thenReturn(user);
        site = mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(site.getGroups()).thenReturn(Collections.emptyList());
        when(siteService.getSite(siteId)).thenReturn(site);
        category = category(10L, 0, 0, 2, false);
        when(gradebookService.getCategoryDefinitions(siteId, siteId)).thenReturn(List.of(category));
        when(gradebookService.getGradebookUidByExternalId(anyString())).thenReturn(Collections.emptyList());
        assessment = createAssessment(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString(), 10L);
        itemId = service.preparePublishedItemHash(assessment).keySet().stream().min(Long::compareTo).get();
    }

    @Test
    public void eachKeepDropRuleRejectsBeforeAnyPersistenceWrite() {
        for (int mode = 0; mode < 3; mode++) {
            category.setKeepHighest(mode == 0 ? 2 : 0);
            category.setDropLowest(mode == 1 ? 1 : 0);
            category.setDropHighest(mode == 2 ? 1 : 0);
            assertFalse(service.isTotalScoreCancellationAllowed(assessment));
            assertRejectedWithoutChanges();
        }
    }

    @Test
    public void publicCancellationRechecksCategoryAfterEligibilityQuery() {
        category.setKeepHighest(0);
        category.setDropKeepEnabled(false);
        assertTrue(service.isTotalScoreCancellationAllowed(assessment));
        category.setKeepHighest(2);
        category.setDropKeepEnabled(true);
        assertRejectedWithoutChanges();
    }

    @Test
    public void loadedFacadeCannotHideItsPersistedCategory() {
        assertNull(assessment.getCategoryId());
        assertEquals(category.getId(), data().getCategoryId());
        assertRejectedWithoutChanges();
    }

    @Test
    public void exportSettingsAreReloadedAfterEligibilityQuery() {
        data().getEvaluationModel().setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
        sessionFactory.getCurrentSession().flush();
        assertTrue(service.isTotalScoreCancellationAllowed(assessment));
        data().getEvaluationModel().setToGradeBook(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
        sessionFactory.getCurrentSession().flush();
        assertRejectedWithoutChanges();
    }

    @Test
    public void configuredCategoryBlocksBeforeExternalItemCreation() {
        assertFalse(gradebookService.isExternalAssignmentDefined(siteId, assessment.getId()));
        assertRejectedWithoutChanges();
    }

    @Test
    public void linkedCategoryReassignmentCannotBypassRestriction() {
        PublishedAssessmentData data = data();
        data.setCategoryId(null);
        sessionFactory.getCurrentSession().flush();
        assessment = reload();
        Assignment linked = new Assignment();
        linked.setCategoryId(category.getId());
        when(gradebookService.getGradebookUidByExternalId(assessment.getId())).thenReturn(List.of(siteId));
        when(gradebookService.getExternalAssignment(siteId, assessment.getId())).thenReturn(linked);
        assertRejectedWithoutChanges();
    }

    @Test
    public void oneRestrictedGroupBlocksAllTargets() {
        configureGroupExport();
        assertTrue(service.isTotalScoreCancellationAllowed(assessment));
        when(gradebookService.getCategoryDefinitions("group-two", siteId)).thenReturn(List.of(category(20L, 0, 0, 2, false)));
        assertRejectedWithoutChanges();
    }

    @Test
    public void linkedItemsInOtherSitesAreExcluded() {
        category.setKeepHighest(0);
        category.setDropKeepEnabled(false);
        when(gradebookService.getGradebookUidByExternalId(assessment.getId())).thenReturn(List.of("other-site"));
        assertTrue(service.isTotalScoreCancellationAllowed(assessment));
        verify(gradebookService, never()).getCategoryDefinitions(eq("other-site"), anyString());
    }

    @Test
    public void missingExpectedCategoryFailsClosed() {
        when(gradebookService.getCategoryDefinitions(siteId, siteId)).thenReturn(Collections.emptyList());
        assertRejectedWithoutChanges();
    }

    @Test
    public void unavailableGradebookFailsClosed() {
        when(gradebookService.getCategoryDefinitions(siteId, siteId)).thenThrow(new IllegalStateException("unavailable"));
        assertRejectedWithoutChanges();
    }

    @Test
    public void legacyPreparationRejectsPendingReductionBeforePointsChange() {
        ItemDataIfc item = service.preparePublishedItemHash(assessment).get(itemId);
        item.setCancellation(ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL);
        sessionFactory.getCurrentSession().flush();
        List<Object> before = snapshot();
        assertThrows(TotalScoreCancellationException.class, () -> service.preparePublishedItemCancellation(reload()));
        assertEquals(before, snapshot());
    }

    @Test
    public void redistributionRetainsTotalAndExistingPointAllocation() {
        PublishedAssessmentFacade updated = service.cancelPublishedItem(assessment.getId(), itemId.toString(), ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL);
        assertEquals(3D, updated.getTotalScore(), 0D);
        Map<Long, ItemDataIfc> items = service.preparePublishedItemHash(reload());
        assertEquals(Integer.valueOf(ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED), items.get(itemId).getCancellation());
        for (ItemDataIfc item : items.values()) {
            assertEquals(item.getItemId().equals(itemId) ? 0D : 1.5D, item.getScore(), 0D);
        }
    }

    @Test
    public void equalWeightCategoryAllowsReduction() {
        category.setEqualWeight(true);
        assertAllowedReduction();
    }

    @Test
    public void ordinaryCategoryAllowsReduction() {
        category.setKeepHighest(0);
        category.setDropKeepEnabled(false);
        assertAllowedReduction();
    }

    @Test
    public void uncategorizedAssessmentAllowsReduction() {
        data().setCategoryId(null);
        sessionFactory.getCurrentSession().flush();
        assessment = reload();
        assertAllowedReduction();
    }

    @Test
    public void noExportAllowsReduction() {
        data().getEvaluationModel().setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
        sessionFactory.getCurrentSession().flush();
        assessment = reload();
        assertAllowedReduction();
        verify(gradebookService, never()).getCategoryDefinitions(anyString(), anyString());
    }

    @Test
    public void selectedItemExportAllowsReductionWithoutChangingTargetDenominator() {
        data().getEvaluationModel().setToGradeBook(EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString());
        sessionFactory.getCurrentSession().flush();
        assessment = reload();
        assertAllowedReduction();
        verify(gradebookService, never()).updateExternalAssessment(anyString(), anyString(), any(), any(), anyString(), any(), any(), any(), any());
    }

    @Test
    public void wrongItemOrModeCannotWrite() {
        List<Object> before = snapshot();
        assertThrows(IllegalArgumentException.class, () -> service.cancelPublishedItem(assessment.getId(), Long.toString(Long.MAX_VALUE), ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL));
        assertThrows(IllegalArgumentException.class, () -> service.cancelPublishedItem(assessment.getId(), itemId.toString(), 0));
        assertEquals(before, snapshot());
    }

    @Test
    public void questionFromAnotherAssessmentCannotWrite() {
        PublishedAssessmentFacade other = createAssessment(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString(), null);
        Long otherItem = service.preparePublishedItemHash(other).keySet().iterator().next();
        List<Object> before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> service.cancelPublishedItem(assessment.getId(), otherItem.toString(), ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL));
        assertEquals(before, snapshot());
        assertEquals(3D, service.getPublishedAssessment(other.getId()).getTotalScore(), 0D);
    }

    @Test
    public void wrongToolSiteCannotWrite() {
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn("other-site");
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        assertRejectedWithoutChanges();
    }

    @Test
    public void lateCategoryRejectionDoesNotDisableGradebookExport() {
        when(gradebookService.isExternalAssignmentDefined(siteId, assessment.getId())).thenReturn(true);
        doThrow(new InvalidCategoryException("category changed")).when(gradebookService)
                .updateExternalAssessment(anyString(), anyString(), any(), any(), anyString(), any(), any(), any(), any());
        assertThrows(InvalidCategoryException.class, () -> service.updateGradebook(data()));
        assertEquals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString(), data().getEvaluationModel().getToGradeBook());
    }

    @Test
    public void singleExportMapsConfiguredAndUncategorizedItemsForCreateAndUpdate() {
        for (Long categoryId : Arrays.asList(10L, null, -1L)) {
            PublishedAssessmentData current = data();
            current.setCategoryId(categoryId);
            when(gradebookService.isExternalAssignmentDefined(siteId, assessment.getId())).thenReturn(false);
            clearInvocations(gradebookService);
            service.updateGradebook(current);
            verify(gradebookService).addExternalAssessment(eq(siteId), eq(siteId), eq(assessment.getId()),
                    isNull(), eq(current.getTitle()), eq(3D), isNull(), eq("sakai.samigo"), isNull(), eq(false),
                    eq(categoryId == null || categoryId == -1L ? null : categoryId), anyString());
            verify(gradebookService, never()).updateExternalAssessment(anyString(), anyString(), any(), any(),
                    anyString(), any(), any(), any(), any());

            when(gradebookService.isExternalAssignmentDefined(siteId, assessment.getId())).thenReturn(true);
            clearInvocations(gradebookService);
            service.updateGradebook(current);
            verify(gradebookService).updateExternalAssessment(eq(siteId), eq(assessment.getId()), isNull(),
                    isNull(), eq(current.getTitle()), eq(categoryId == null ? -1L : categoryId), eq(3D), isNull(), isNull());
            verify(gradebookService, never()).addExternalAssessment(anyString(), anyString(), anyString(), any(),
                    anyString(), any(), any(), anyString(), any(), any(), any(), anyString());
        }
    }

    @Test
    public void groupExportUsesEditedMetadataAndUpdatesAllSelectedTargetsAfterCreation() {
        configureGroupExport();
        PublishedAssessmentData current = data();
        PublishedMetaData selection = (PublishedMetaData) current.getAssessmentMetaDataSet().stream()
                .filter(metadata -> AssessmentMetaDataIfc.CATEGORY_LIST.equals(((PublishedMetaData) metadata).getLabel()))
                .findFirst().get();
        selection.setEntry("30,20,-1");
        assertEquals("10,20,-1", current.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CATEGORY_LIST));
        when(gradebookService.buildCategoryGradebookMap(anyList(), eq("30,20,-1"), eq(siteId)))
                .thenReturn(Map.of("group-one", "30", "group-two", "20", "group-three", "-1"));
        when(gradebookService.isExternalAssignmentDefined("group-one", assessment.getId())).thenReturn(true);

        service.updateGradebook(current);

        verify(gradebookService).buildCategoryGradebookMap(argThat(groups ->
                new HashSet<>(groups).equals(Set.of("group-one", "group-two", "group-three"))), eq("30,20,-1"), eq(siteId));
        verify(gradebookService).addExternalAssessment(eq("group-two"), eq(siteId), eq(assessment.getId()),
                isNull(), eq(current.getTitle()), eq(3D), isNull(), eq("sakai.samigo"), isNull(), eq(false), eq(20L), anyString());
        verify(gradebookService).addExternalAssessment(eq("group-three"), eq(siteId), eq(assessment.getId()),
                isNull(), eq(current.getTitle()), eq(3D), isNull(), eq("sakai.samigo"), isNull(), eq(false), isNull(), anyString());
        verify(gradebookService).updateExternalAssessment(eq("group-one"), eq(assessment.getId()), isNull(),
                isNull(), eq(current.getTitle()), eq(30L), eq(3D), isNull(), isNull());
        for (String created : List.of("group-two", "group-three")) {
            verify(gradebookService).updateExternalAssessment(eq(created), eq(assessment.getId()), isNull(),
                    isNull(), eq(current.getTitle()), isNull(), eq(3D), isNull(), isNull());
        }
    }

    @Test
    public void missingConfiguredGroupCategoryCannotBecomeUncategorizedPermission() {
        configureGroupExport();
        when(gradebookService.getCategoryDefinitions("group-two", siteId)).thenReturn(Collections.emptyList());
        when(gradebookService.buildCategoryGradebookMap(anyList(), eq("10,20,-1"), eq(siteId)))
                .thenReturn(Map.of("group-one", "10", "group-two", "-1", "group-three", "-1"));
        assertRejectedWithoutChanges();
    }

    private void assertRejectedWithoutChanges() {
        List<Object> before = snapshot();
        assertThrows(TotalScoreCancellationException.class,
                () -> service.cancelPublishedItem(assessment.getId(), itemId.toString(), ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL));
        assertEquals(before, snapshot());
        assertTrue(mockingDetails(gradebookService).getInvocations().stream()
                .noneMatch(invocation -> invocation.getMethod().getName().startsWith("updateExternalAssessment")));
    }

    private void assertAllowedReduction() {
        assertTrue(service.isTotalScoreCancellationAllowed(assessment));
        PublishedAssessmentFacade updated = service.cancelPublishedItem(assessment.getId(), itemId.toString(), ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL);
        assertEquals(2D, updated.getTotalScore(), 0D);
        ItemDataIfc item = service.preparePublishedItemHash(reload()).get(itemId);
        assertEquals(Integer.valueOf(ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED), item.getCancellation());
        assertEquals(0D, item.getScore(), 0D);
    }

    private List<Object> snapshot() {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        PublishedAssessmentFacade current = reload();
        List<Object> values = new ArrayList<>();
        values.add(current.getTotalScore());
        service.preparePublishedItemHash(current).values().stream().sorted((a, b) -> a.getItemId().compareTo(b.getItemId())).forEach(item -> {
            Collections.addAll(values, item.getItemId(), item.getCancellation(), item.getScore(), item.getDiscount(), item.getHash());
            for (ItemTextIfc text : item.getItemTextSet()) {
                for (AnswerIfc answer : text.getAnswerSet()) {
                    Collections.addAll(values, answer.getScore(), answer.getDiscount());
                }
            }
        });
        List<AssessmentGradingData> assessmentGrades = new GradingService().getAllAssessmentGradingData(current.getPublishedAssessmentId());
        for (AssessmentGradingData grade : assessmentGrades) {
            Collections.addAll(values, grade.getFinalScore(), grade.getTotalAutoScore(), grade.getTotalOverrideScore(), grade.getStatus(), grade.getForGrade());
        }
        for (ItemGradingData grade : sessionFactory.getCurrentSession().createQuery("from ItemGradingData order by itemGradingId", ItemGradingData.class).list()) {
            Collections.addAll(values, grade.getAutoScore(), grade.getOverrideScore(), grade.getPublishedAnswerId());
        }
        return values;
    }

    private PublishedAssessmentFacade reload() {
        return service.getPublishedAssessment(assessment.getId());
    }

    private PublishedAssessmentData data() {
        return sessionFactory.getCurrentSession().get(PublishedAssessmentData.class, assessment.getPublishedAssessmentId());
    }

    private CategoryDefinition category(Long id, int dropHighest, int dropLowest, int keepHighest, boolean equalWeight) {
        CategoryDefinition definition = new CategoryDefinition();
        definition.setId(id);
        definition.setDropHighest(dropHighest);
        definition.setDropLowest(dropLowest);
        definition.setKeepHighest(keepHighest);
        definition.setDropKeepEnabled(dropHighest > 0 || dropLowest > 0 || keepHighest > 0);
        definition.setEqualWeight(equalWeight);
        return definition;
    }

    private void configureGroupExport() {
        Group first = mock(Group.class);
        when(first.getId()).thenReturn("group-one");
        when(first.getTitle()).thenReturn("One");
        when(first.getContainingSite()).thenReturn(site);
        when(siteService.findGroup("group-one")).thenReturn(first);
        Group second = mock(Group.class);
        when(second.getId()).thenReturn("group-two");
        when(second.getTitle()).thenReturn("Two");
        when(second.getContainingSite()).thenReturn(site);
        when(siteService.findGroup("group-two")).thenReturn(second);
        Group third = mock(Group.class);
        when(third.getId()).thenReturn("group-three");
        when(third.getTitle()).thenReturn("Three");
        when(third.getContainingSite()).thenReturn(site);
        when(siteService.findGroup("group-three")).thenReturn(third);
        when(site.getGroups()).thenReturn(List.of(first, second, third));
        when(site.getGroup("group-one")).thenReturn(first);
        when(site.getGroup("group-two")).thenReturn(second);
        when(site.getGroup("group-three")).thenReturn(third);
        ownerAuthorization("group-one", "TAKE_PUBLISHED_ASSESSMENT", assessment.getId());
        ownerAuthorization("group-two", "TAKE_PUBLISHED_ASSESSMENT", assessment.getId());
        ownerAuthorization("group-three", "TAKE_PUBLISHED_ASSESSMENT", assessment.getId());
        PublishedAssessmentData data = data();
        data.getAssessmentAccessControl().setReleaseTo(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);
        data.getAssessmentMetaDataSet().add(new PublishedMetaData(data, AssessmentMetaDataIfc.CATEGORY_LIST, "10,20,-1"));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assessment = reload();
        when(gradebookService.isGradebookGroupEnabled(siteId)).thenReturn(true);
        when(gradebookService.buildCategoryGradebookMap(anyList(), eq("10,20,-1"), eq(siteId)))
                .thenReturn(Map.of("group-one", "10", "group-two", "20", "group-three", "-1"));
        when(gradebookService.getCategoryDefinitions("group-one", siteId)).thenReturn(List.of(category(10L, 0, 0, 0, false)));
        when(gradebookService.getCategoryDefinitions("group-two", siteId)).thenReturn(List.of(category(20L, 0, 0, 0, false)));
        when(gradebookService.getCategoryDefinitions("group-three", siteId)).thenReturn(Collections.emptyList());
    }

    private void ownerAuthorization(String agent, String function, String id) {
        Date now = Date.from(Instant.now());
        sessionFactory.getCurrentSession().persist(new AuthorizationData(agent, function, id, now, null, "instructor", now, true));
    }

    private PublishedAssessmentFacade createAssessment(String export, Long categoryId) {
        Date now = Date.from(Instant.now());
        PublishedAssessmentData data = new PublishedAssessmentData();
        data.setTitle("Three point quiz");
        data.setStatus(1);
        data.setCreatedBy("instructor");
        data.setCreatedDate(now);
        data.setLastModifiedBy("instructor");
        data.setLastModifiedDate(now);
        data.setCategoryId(categoryId);
        data.setAssessmentMetaDataSet(new HashSet<>());
        PublishedEvaluationModel evaluation = new PublishedEvaluationModel();
        evaluation.setAssessment(data);
        evaluation.setToGradeBook(export);
        evaluation.setScoringType(EvaluationModelIfc.HIGHEST_SCORE);
        data.setEvaluationModel(evaluation);
        PublishedAccessControl access = new PublishedAccessControl();
        access.setAssessment(data);
        access.setReleaseTo(siteId);
        data.setAssessmentAccessControl(access);
        PublishedSectionData section = new PublishedSectionData(0, 1, "Part 1", "", 1L, 1, "instructor", now, "instructor", now);
        section.setAssessment(data);
        section.setSectionMetaDataSet(new HashSet<>());
        Set<PublishedItemData> items = new HashSet<>();
        for (int number = 1; number <= 3; number++) {
            PublishedItemData item = new PublishedItemData();
            item.setSection(section);
            item.setSequence(number);
            item.setTypeId(TypeIfc.TRUE_FALSE);
            item.setScore(1D);
            item.setDiscount(0D);
            item.setStatus(1);
            item.setCreatedBy("instructor");
            item.setCreatedDate(now);
            item.setLastModifiedBy("instructor");
            item.setLastModifiedDate(now);
            item.setCancellation(ItemDataIfc.ITEM_NOT_CANCELED);
            item.setIsFixed(true);
            item.setIsExtraCredit(false);
            item.setHash("question-" + number);
            item.setItemMetaDataSet(new HashSet<>());
            item.setItemAttachmentSet(new HashSet<>());
            PublishedItemText text = new PublishedItemText(item, 1L, "Question " + number, new HashSet<>());
            text.getAnswerSet().add(new PublishedAnswer(text, "True", 1L, "A", true, "", 1D, 100D, 0D));
            item.setItemTextSet(new HashSet<>(Set.of(text)));
            items.add(item);
        }
        section.setItemSet(items);
        data.setSectionSet(new HashSet<>(Set.of(section)));
        sessionFactory.getCurrentSession().persist(data);
        sessionFactory.getCurrentSession().flush();
        ownerAuthorization(siteId, "OWN_PUBLISHED_ASSESSMENT", data.getPublishedAssessmentId().toString());
        ownerAuthorization(siteId, "TAKE_PUBLISHED_ASSESSMENT", data.getPublishedAssessmentId().toString());
        AssessmentGradingData grade = new AssessmentGradingData();
        grade.setPublishedAssessmentId(data.getPublishedAssessmentId());
        grade.setAgentId("student");
        grade.setIsLate(false);
        grade.setForGrade(true);
        grade.setFinalScore(2D);
        grade.setTotalAutoScore(2D);
        grade.setTotalOverrideScore(0D);
        grade.setStatus(1);
        grade.setHasAutoSubmissionRun(false);
        sessionFactory.getCurrentSession().persist(grade);
        sessionFactory.getCurrentSession().flush();
        for (PublishedItemData item : items) {
            ItemTextIfc text = (ItemTextIfc) item.getItemTextSet().iterator().next();
            AnswerIfc answer = text.getAnswerSet().iterator().next();
            ItemGradingData response = new ItemGradingData();
            response.setAssessmentGradingId(grade.getAssessmentGradingId());
            response.setPublishedItemId(item.getItemId());
            response.setPublishedItemTextId(text.getId());
            response.setPublishedAnswerId(answer.getId());
            response.setAgentId("student");
            response.setAutoScore(1D);
            response.setOverrideScore(0D);
            sessionFactory.getCurrentSession().persist(response);
        }
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        return service.getPublishedAssessment(data.getPublishedAssessmentId().toString());
    }
}
