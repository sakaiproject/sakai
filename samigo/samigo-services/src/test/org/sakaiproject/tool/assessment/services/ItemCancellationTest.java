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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;


public class ItemCancellationTest {


    private static final double SCORE_MAX_DELTA = 0.0;


    private static AtomicLong atomicId;
    private static PublishedAssessmentService publishedAssessmentService;


    @Before
    public void setUp() {
        publishedAssessmentService = mock(PublishedAssessmentService.class);
        doCallRealMethod().when(publishedAssessmentService).preparePublishedItemCancellation(any());
        doCallRealMethod().when(publishedAssessmentService).preparePublishedItemHash(any());

        atomicId = new AtomicLong();
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
        // The method getAssessment is called when cancellation is done, we can ignore it
        when(publishedAssessmentService.getAssessment(anyString())).thenReturn(new AssessmentFacade());

        // Create a mock of publishedAssessment and add one section with out testItems
        PublishedAssessmentFacade publishedAssessment = mock(PublishedAssessmentFacade.class);
        List<SectionDataIfc> sectionList = new ArrayList<>();
        sectionList.add(createSection(SerializationUtils.clone(testItems)));
        when(publishedAssessment.getSectionArray()).thenReturn((ArrayList<SectionDataIfc>) sectionList);

        // Get testItems from stubbed saveItem method
        List<ItemDataIfc> savedItemList = new ArrayList<>();
        doAnswer(invocation -> {
            ItemDataIfc item = invocation.getArgument(0);
            savedItemList.add(item);
            return null;
        }).when(publishedAssessmentService).saveItem(any());

        // Call cancellation process
        publishedAssessmentService.preparePublishedItemCancellation(publishedAssessment);

        // Convert "saved" items to array
        ItemDataIfc[] savedItems = savedItemList.toArray(new ItemDataIfc[savedItemList.size()]);

        // Test if we have same number of items
        Assert.assertEquals(cancelledTestItems.length, savedItems.length);

        // Test if item-cancellation has been set
        Assert.assertEquals(cancelledTestItems[cancelItemIndex].getCancellation(), savedItems[cancelItemIndex].getCancellation());

        // Test if items have the same scores
        for (int i = 0; i < testItems.length; i++) {
            assertItemScoreEquals(cancelledTestItems[i], savedItems[i]);
        }

        // Test if items have the same scores set for the answers
        for (int i = 0; i < testItems.length; i++) {
            assertItemAnswerScoresEqual(cancelledTestItems[i], savedItems[i]);
        }

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

    private SectionDataIfc createSection(ItemDataIfc[] items) {
        SectionDataIfc section = mock(SectionData.class);

        ArrayList<ItemDataIfc> itemList = new ArrayList<>(Arrays.asList(items));

        when(section.getItemArray()).thenReturn(itemList);

        return section;
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
        itemText.setId(atomicId.getAndIncrement());
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
        answer.setItem(mock(ItemData.class));
        answer.setId(atomicId.getAndIncrement());
        answer.setSequence(sequence);
        answer.setScore(score);

        return answer;
    }
}
