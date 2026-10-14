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

package org.sakaiproject.tool.assessment.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.*;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.*;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(classes = SamigoCancellationTestConfiguration.class)
public class ItemCancellationTest extends AbstractTransactionalJUnit4SpringContextTests {


    private static final double SCORE_MAX_DELTA = 0.0;


    private long nextId;
    @Autowired private PublishedAssessmentService publishedAssessmentService;
    @Autowired private PersistenceService persistenceService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private SiteService siteService;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ApplicationContext applicationContext;

    @BeforeClass
    public static void initializeLocators() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void setUp() throws Exception {
        nextId = 0;
        reset(siteService, toolManager, userDirectoryService);
        ComponentManager.loadComponent("agentHelper", applicationContext.getBean("agentHelper"));
        ComponentManager.loadComponent("gradebookServiceHelper", applicationContext.getBean("gradebookServiceHelper"));
        ComponentManager.loadComponent(org.sakaiproject.grading.api.GradingService.class,
                applicationContext.getBean(org.sakaiproject.grading.api.GradingService.class));
        ComponentManager.loadComponent("PersistenceService", persistenceService);
        ComponentManager.loadComponent(SiteService.class, siteService);
        ComponentManager.loadComponent(ToolManager.class, toolManager);
        ComponentManager.loadComponent(UserDirectoryService.class, userDirectoryService);
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn("cancellation-site");
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        User user = mock(User.class);
        when(user.getId()).thenReturn("instructor");
        when(userDirectoryService.getCurrentUser()).thenReturn(user);
        Site site = mock(Site.class);
        when(site.getGroups()).thenReturn(new ArrayList<>());
        when(siteService.getSite("cancellation-site")).thenReturn(site);
    }

    @Test
    public void testTotalCancellation() {
        // Array index of the item that is going to be cancelled
        int cancelItemIndex = 0;

        // Items that we use as input for the cancellation
        ItemDataIfc[] testItems = {
            createItem(0L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL),
            createItem(1L, 0, 2.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(2L, 0, 3.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 8.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        // Reference cancelled items - what we are going to compare to
        ItemDataIfc[] cancelledTestItems = {
            createItem(0L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED),
            createItem(1L, 0, 2.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(2L, 0, 3.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 8.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        assertItemCancellation(testItems, cancelledTestItems, cancelItemIndex);
    }

    @Test
    public void testDistributedCancellation() {
        // Array index of the item that is going to be cancelled
        int cancelItemIndex = 0;

        // Items that we use as input for the cancellation
        ItemDataIfc[] testItems = {
            createItem(0L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL),
            createItem(1L, 0, 2.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(2L, 0, 3.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 8.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        // Reference cancelled items - what we are going to compare to
        ItemDataIfc[] cancelledTestItems = {
            createItem(0L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED),
            createItem(1L, 0, 3.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(2L, 0, 4.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 7.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 9.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        assertItemCancellation(testItems, cancelledTestItems, cancelItemIndex);
    }

    @Test
    public void testMixedCancellation() {
        // Multiple cancellations at the same time don't occur at the moment, but it should work

        // Items that we use as input for the cancellation
        ItemDataIfc[] testItems = {
            createItem(0L, 0, 4.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL),
            createItem(1L, 0, 2.0, 1, 1, ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL),
            createItem(2L, 0, 3.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 8.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        // Reference cancelled items - what we are going to compare to
        ItemDataIfc[] cancelledTestItems = {
            createItem(0L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED),
            createItem(1L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED),
            createItem(2L, 0, 4.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 7.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 9.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        assertItemCancellation(testItems, cancelledTestItems, 0);
        assertItemCancellation(testItems, cancelledTestItems, 1);
    }

    @Test
    public void testRepeatedCancellation() {
        // Cancellation with cancelled item present

        // Items that we use as input for the cancellation
        ItemDataIfc[] testItems = {
            createItem(0L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED),
            createItem(1L, 0, 4.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL),
            createItem(2L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        // First reference cancelled items
        ItemDataIfc[] cancelledTestItems1 = {
            createItem(0L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED),
            createItem(1L, 0, 0.0, 1, 1, ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED),
            createItem(2L, 0, 9.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        assertItemCancellation(testItems, cancelledTestItems1, 1);
    }

    @Test
    public void testNoCancellation() {
        // Not cancelling items is does not happen, but we can test it anyway, to further validate the integrity

        // Items that we use as input for the cancellation
        ItemDataIfc[] testItems = {
            createItem(0L, 0, 4.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(1L, 0, 2.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(2L, 0, 3.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(3L, 0, 6.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(4L, 0, 5.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
            createItem(5L, 0, 8.0, 1, 1, ItemDataIfc.ITEM_NOT_CANCELED),
        };

        ItemDataIfc[] cancelledTestItems = SerializationUtils.clone(testItems);

        assertItemCancellation(testItems, cancelledTestItems, 0);
    }

    private void assertItemCancellation(ItemDataIfc[] testItems, ItemDataIfc[] cancelledTestItems, int cancelItemIndex) {
        PublishedAssessmentFacade assessment = persistAssessment(testItems);
        publishedAssessmentService.preparePublishedItemCancellation(assessment);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        List<ItemDataIfc> savedItemList = new ArrayList<>(publishedAssessmentService.preparePublishedItemHash(
                publishedAssessmentService.getPublishedAssessment(assessment.getId())).values());
        savedItemList.sort((first, second) -> first.getSequence().compareTo(second.getSequence()));
        ItemDataIfc[] savedItems = savedItemList.toArray(new ItemDataIfc[0]);
        Assert.assertEquals(cancelledTestItems.length, savedItems.length);
        Assert.assertEquals(cancelledTestItems[cancelItemIndex].getCancellation(), savedItems[cancelItemIndex].getCancellation());
        for (int i = 0; i < testItems.length; i++) {
            Assert.assertEquals(cancelledTestItems[i].getCancellation(), savedItems[i].getCancellation());
            assertItemScoreEquals(cancelledTestItems[i], savedItems[i]);
            assertItemAnswerScoresEqual(cancelledTestItems[i], savedItems[i]);
        }
    }

    private PublishedAssessmentFacade persistAssessment(ItemDataIfc[] input) {
        Date now = Date.from(Instant.now());
        PublishedAssessmentData data = new PublishedAssessmentData();
        data.setTitle("Cancellation allocation fixture");
        data.setStatus(1);
        data.setCreatedBy("instructor");
        data.setCreatedDate(now);
        data.setLastModifiedBy("instructor");
        data.setLastModifiedDate(now);
        data.setAssessmentMetaDataSet(new HashSet<>());
        PublishedEvaluationModel evaluation = new PublishedEvaluationModel();
        evaluation.setAssessment(data);
        evaluation.setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
        data.setEvaluationModel(evaluation);
        PublishedAccessControl access = new PublishedAccessControl();
        access.setAssessment(data);
        access.setReleaseTo("cancellation-site");
        data.setAssessmentAccessControl(access);
        PublishedSectionData section = new PublishedSectionData(0, 1, "Part", "", 1L, 1, "instructor", now, "instructor", now);
        section.setAssessment(data);
        section.setSectionMetaDataSet(new HashSet<>());
        Set<PublishedItemData> items = new HashSet<>();
        for (int index = 0; index < input.length; index++) {
            ItemDataIfc source = input[index];
            PublishedItemData item = new PublishedItemData();
            item.setSection(section);
            item.setSequence(index);
            item.setTypeId(TypeIfc.TRUE_FALSE);
            item.setScore(source.getScore());
            item.setDiscount(0D);
            item.setCancellation(source.getCancellation());
            item.setIsFixed(true);
            item.setIsExtraCredit(false);
            item.setStatus(1);
            item.setCreatedBy("instructor");
            item.setCreatedDate(now);
            item.setLastModifiedBy("instructor");
            item.setLastModifiedDate(now);
            item.setHash("question-" + index);
            item.setItemMetaDataSet(new HashSet<>());
            item.setItemAttachmentSet(new HashSet<>());
            Set<PublishedItemText> texts = new HashSet<>();
            for (ItemTextIfc originalText : source.getItemTextArray()) {
                PublishedItemText text = new PublishedItemText(item, originalText.getSequence(), "Question", new HashSet<>());
                for (AnswerIfc originalAnswer : originalText.getAnswerArray()) {
                    text.getAnswerSet().add(new PublishedAnswer(text, "True", originalAnswer.getSequence(), "A", true, "",
                            originalAnswer.getScore(), 100D, 0D));
                }
                texts.add(text);
            }
            item.setItemTextSet(texts);
            items.add(item);
        }
        section.setItemSet(items);
        data.setSectionSet(new HashSet<>(Set.of(section)));
        sessionFactory.getCurrentSession().persist(data);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().persist(new AuthorizationData("cancellation-site", "OWN_PUBLISHED_ASSESSMENT",
                data.getPublishedAssessmentId().toString(), now, null, "instructor", now, true));
        sessionFactory.getCurrentSession().persist(new AuthorizationData("cancellation-site", "TAKE_PUBLISHED_ASSESSMENT",
                data.getPublishedAssessmentId().toString(), now, null, "instructor", now, true));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        return publishedAssessmentService.getPublishedAssessment(data.getPublishedAssessmentId().toString());
    }

    private void assertItemScoreEquals(ItemDataIfc item1, ItemDataIfc item2) {
        Assert.assertEquals(item1.getScore(), item2.getScore(), SCORE_MAX_DELTA);
    }

    private void assertItemAnswerScoresEqual(ItemDataIfc item1, ItemDataIfc item2) {
        double[] item1Scores = item1.getItemTextArray().stream()
                .flatMap(itemText -> itemText.getAnswerArray().stream())
                .mapToDouble(answer -> answer.getScore())
                .toArray();
        double[] item2Scores = item2.getItemTextArray().stream()
                .flatMap(itemText -> itemText.getAnswerArray().stream())
                .mapToDouble(answer -> answer.getScore())
                .toArray();

        Assert.assertArrayEquals(item1Scores, item2Scores, SCORE_MAX_DELTA);
    }

    private ItemDataIfc createItem(long id, int sequence, Double score, int itemTextCount, int answerCount, int cancellation) {
        ItemDataIfc item = new ItemData();
        item.setItemId(id);
        item.setScore(score);
        item.setCancellation(cancellation);
        item.setSequence(sequence);

        ArrayList<ItemTextIfc> itemTextList = new ArrayList<>();
        for (int i = 0; i < itemTextCount; i++) {
            itemTextList.add(createItemText((long) i, score, answerCount));
        }
        item.setItemTextSet(itemTextList.stream().collect(Collectors.toSet()));

        return item;
    }

    private ItemTextIfc createItemText(Long sequence, Double score, int answerCount) {
        ItemTextIfc itemText = new ItemText();
        itemText.setId(nextId++);
        itemText.setSequence(sequence);

        List<AnswerIfc> answerList = new ArrayList<>();
        for (int i = 0; i < answerCount; i++) {
            answerList.add(createAnswer((long) i, score));
        }
        itemText.setAnswerSet(answerList.stream().collect(Collectors.toSet()));

        return itemText;
    }

    private AnswerIfc createAnswer(Long sequence, Double score) {
        AnswerIfc answer = new Answer();
        answer.setItem(new ItemData());
        answer.setId(nextId++);
        answer.setSequence(sequence);
        answer.setScore(score);

        return answer;
    }
}
