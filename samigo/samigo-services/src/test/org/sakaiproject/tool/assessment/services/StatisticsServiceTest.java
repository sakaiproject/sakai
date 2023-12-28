/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.memory.mock.MemoryService;
import org.sakaiproject.tool.assessment.business.entity.ItemStatistics;
import org.sakaiproject.tool.assessment.business.entity.QuestionPoolStatistics;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.StatisticsFacadeQueries;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;

public class StatisticsServiceTest {


    private StatisticsService statisticsService;
    private StatisticsFacadeQueries statisticsFacadeQueries;
    private QuestionPoolService questionPoolService;
    private MemoryService memoryService;

    // TODO: Additional test ideas:
    // - Test calculated questions
    // - Test hot spot items
    // - Test ignored item types are ignores

    @Before
    public void setUp() {
        GradingService gradingService = new GradingService();
        questionPoolService = mock(QuestionPoolService.class);
        statisticsFacadeQueries = mock(StatisticsFacadeQueries.class);
        memoryService = new MemoryService();

        statisticsService = spy(new StatisticsService(gradingService, memoryService,
                questionPoolService, statisticsFacadeQueries));

        doReturn(Collections.emptySet()).when(questionPoolService).getAllItemHashes(any());
    }

    @Test
    public void testTrueFalseItem() {
        long itemId = 0L;
        long correctAnswerId = 0L;
        long incorrectAnswerId = 1L;

        PublishedItemData item = item(itemId, TypeIfc.TRUE_FALSE);

        Set<PublishedAnswer> itemAnswers = Set.of(
                // Correct answer
                answer(correctAnswerId, true),
                // Incorrect answer
                answer(incorrectAnswerId, false)
        );

        // Each gradingData represents one submission
        Set<ItemGradingData> gradingData = Set.of(
                gradingData(0L, correctAnswerId),
                gradingData(1L, correctAnswerId),
                gradingData(2L, incorrectAnswerId),
                gradingData(3L, incorrectAnswerId),
                gradingData(4L, incorrectAnswerId),
                gradingData(5L, correctAnswerId),
                gradingData(6L, null),
                gradingData(7L, null),
                gradingData(8L, correctAnswerId),
                gradingData(9L, correctAnswerId)
        );

        List<PublishedItemData> items = Collections.singletonList(item);
        Map<Long, Set<ItemGradingData>> gradingDataMap = Map.of(itemId, gradingData);
        Map<Long, Set<PublishedAnswer>> answerMap = Map.of(itemId, itemAnswers);

        stubData(items, answerMap, gradingDataMap);

        QuestionPoolStatistics poolStatistics = statisticsService.getQuestionPoolStatistics(0L);
        ItemStatistics itemStatistics = poolStatistics.getAggregatedItemStatistics();

        assertEquals(Long.valueOf(8), itemStatistics.getAttemptedResponses());
        assertEquals(Long.valueOf(5), itemStatistics.getCorrectResponses());
        assertEquals(Long.valueOf(3), itemStatistics.getIncorrectResponses());
        assertEquals(Long.valueOf(2), itemStatistics.getBlankResponses());
        assertEquals(Integer.valueOf(50), itemStatistics.getDifficulty());
    }

    @Test
    public void testMultipleChoiceSingleSelectionSingleCorrectItem() {
        long itemId = 0L;

        PublishedItemData item = item(itemId, TypeIfc.MULTIPLE_CHOICE);

        Set<PublishedAnswer> itemAnswers = Set.of(
                // Incorrect selection
                answer(0L, false),
                // Correct selection
                answer(1L, true),
                // Incorrect selections
                answer(2L, false),
                answer(3L, false)
        );

        // Each gradingData represents one submission
        Set<ItemGradingData> gradingData = Set.of(
                gradingData(0L, 0L),
                gradingData(1L, 0L),
                gradingData(2L, 1L),
                gradingData(3L, 1L),
                gradingData(4L, 1L),
                gradingData(5L, 1L),
                gradingData(6L, 1L),
                gradingData(7L, 1L),
                gradingData(8L, 2L),
                gradingData(9L, 3L),
                gradingData(10L, null),
                gradingData(11L, null),
                gradingData(12L, null),
                gradingData(13L, 3L)
        );

        // Incorrect MCSC questions have one ItemGradingData that references an answer that is not correct
        List<PublishedItemData> items = Collections.singletonList(item);
        Map<Long, Set<ItemGradingData>> gradingDataMap = Map.of(itemId, gradingData);
        Map<Long, Set<PublishedAnswer>> answerMap = Map.of(itemId, itemAnswers);

        stubData(items, answerMap, gradingDataMap);

        QuestionPoolStatistics poolStatistics = statisticsService.getQuestionPoolStatistics(0L);
        ItemStatistics itemStatistics = poolStatistics.getAggregatedItemStatistics();

        assertEquals(Long.valueOf(11), itemStatistics.getAttemptedResponses());
        assertEquals(Long.valueOf(6), itemStatistics.getCorrectResponses());
        assertEquals(Long.valueOf(5), itemStatistics.getIncorrectResponses());
        assertEquals(Long.valueOf(3), itemStatistics.getBlankResponses());
        assertEquals(Integer.valueOf(57), itemStatistics.getDifficulty());
    }

    @Test
    public void testMultipleChoiceSingleSelectionMultipleCorrectItem() {
        long itemId = 0L;

        PublishedItemData item = item(itemId, TypeIfc.MULTIPLE_CHOICE);

        Set<PublishedAnswer> itemAnswers = Set.of(
                // Incorrect selection
                answer(0L, false),
                // Correct selection
                answer(1L, true),
                // Incorrect selection
                answer(2L, false),
                // Correct selection
                answer(3L, true)
        );

        // Each gradingData represents one submission
        Set<ItemGradingData> gradingData = Set.of(
                gradingData(0L, 0L),
                gradingData(1L, 0L),
                gradingData(2L, 1L),
                gradingData(3L, 1L),
                gradingData(4L, 1L),
                gradingData(5L, 1L),
                gradingData(6L, 1L),
                gradingData(7L, 1L),
                gradingData(8L, 2L),
                gradingData(9L, 3L),
                gradingData(10L, null),
                gradingData(11L, null),
                gradingData(12L, null),
                gradingData(13L, 3L)
        );

        // Incorrect MCSC questions have one ItemGradingData that references an answer that is not correct
        List<PublishedItemData> items = Collections.singletonList(item);
        Map<Long, Set<ItemGradingData>> gradingDataMap = Map.of(itemId, gradingData);
        Map<Long, Set<PublishedAnswer>> answerMap = Map.of(itemId, itemAnswers);

        stubData(items, answerMap, gradingDataMap);

        QuestionPoolStatistics poolStatistics = statisticsService.getQuestionPoolStatistics(0L);
        ItemStatistics itemStatistics = poolStatistics.getAggregatedItemStatistics();

        assertEquals(Long.valueOf(11), itemStatistics.getAttemptedResponses());
        assertEquals(Long.valueOf(8), itemStatistics.getCorrectResponses());
        assertEquals(Long.valueOf(3), itemStatistics.getIncorrectResponses());
        assertEquals(Long.valueOf(3), itemStatistics.getBlankResponses());
        assertEquals(Integer.valueOf(43), itemStatistics.getDifficulty());
    }

    @Test
    public void testMultipleChoiceMultipleSelectionsItem() {
        long itemId = 0L;

        PublishedItemData item = item(itemId, TypeIfc.MULTIPLE_CORRECT);

        Set<PublishedAnswer> itemAnswers = Set.of(
                // Incorrect selection
                answer(0L, false),
                // Correct selection
                answer(1L, true),
                // Incorrect selections
                answer(2L, true),
                answer(3L, false)
        );

        Set<ItemGradingData> gradingData = Set.of(
                // Correct selections
                gradingData(0L, 1L, 0L),
                gradingData(1L, 2L, 0L),

                // Mixed correct and incorrect
                gradingData(4L, 0L, 1L),
                gradingData(5L, 1L, 1L),
                gradingData(6L, 3L, 1L),

                // Everything selected
                gradingData(7L, 0L, 2L),
                gradingData(8L, 1L, 2L),
                gradingData(9L, 2L, 2L),
                gradingData(9L, 3L, 2L),

                // No selection - blank
                gradingData(10L, null, 3L),

                // Correct selections
                gradingData(11L, 1L, 4L),
                gradingData(12L, 2L, 4L),

                // No selection - blank
                gradingData(13L, null, 5L),

                // One correct - still incorrect
                gradingData(14L, 0L, 6L)
        );

        List<PublishedItemData> items = Collections.singletonList(item);
        Map<Long, Set<ItemGradingData>> gradingDataMap = Map.of(itemId, gradingData);
        Map<Long, Set<PublishedAnswer>> answerMap = Map.of(itemId, itemAnswers);

        stubData(items, answerMap, gradingDataMap);

        QuestionPoolStatistics poolStatistics = statisticsService.getQuestionPoolStatistics(0L);
        ItemStatistics itemStatistics = poolStatistics.getAggregatedItemStatistics();

        assertEquals(Long.valueOf(5), itemStatistics.getAttemptedResponses());
        assertEquals(Long.valueOf(2), itemStatistics.getCorrectResponses());
        assertEquals(Long.valueOf(3), itemStatistics.getIncorrectResponses());
        assertEquals(Long.valueOf(2), itemStatistics.getBlankResponses());
        assertEquals(Integer.valueOf(71), itemStatistics.getDifficulty());
    }

    @Test
    public void testMatchingItem() {
        long itemId = 0L;

        PublishedItemData item = item(itemId, TypeIfc.MATCHING);

        // Matching items duplicate answers for each item text
        // -> 3 options 9 answers

        // What do those animals eat?
        // Dog -> Homework
        // Cat -> Fish
        // Dinosaur -> You
        Set<PublishedAnswer> itemAnswers = Set.of(
                // Dog ->
                answer(0L, true),  // Homework
                answer(1L, false), // Fish
                answer(2L, false), // You
                // Cat ->
                answer(3L, false), // Homework
                answer(4L, true),  // Fish
                answer(5L, false), // You
                // Dinosaur ->
                answer(6L, false), // Homework
                answer(7L, false), // Fish
                answer(8L, true)   // You
        );

        Set<ItemGradingData> gradingData = Set.of(
                // All correct - correct
                gradingData(0L, 0L, 0L),
                gradingData(1L, 4L, 0L),
                gradingData(2L, 8L, 0L),

                // All matched to homework - incorrect
                gradingData(3L, 0L, 1L),
                gradingData(4L, 3L, 1L),
                gradingData(5L, 6L, 1L),

                // 2 correct, 1 blank - incorrect
                gradingData(6L, 0L, 2L),
                gradingData(7L, null, 2L),
                gradingData(8L, 8L, 2L),

                // All correct - correct
                gradingData(9L, 0L, 3L),
                gradingData(10L, 4L, 3L),
                gradingData(11L, 8L, 3L),

                // All incorrect - incorrect
                gradingData(12L, 1L, 4L),
                gradingData(13L, 5L, 4L),
                gradingData(14L, 7L, 4L),

                // All blank - blank
                gradingData(15L, null, 5L),
                gradingData(16L, null, 5L),
                gradingData(17L, null, 5L),

                // All blank - blank
                gradingData(18L, null, 6L),
                gradingData(19L, null, 6L),
                gradingData(20L, null, 6L),

                // 1 Correct, 2 incorrect - incorrect
                gradingData(21L, 0L, 7L),
                gradingData(22L, 3L, 7L),
                gradingData(23L, 7L, 7L),

                // All correct - correct
                gradingData(24L, 0L, 8L),
                gradingData(25L, 4L, 8L),
                gradingData(26L, 8L, 8L)
        );

        List<PublishedItemData> items = Collections.singletonList(item);
        Map<Long, Set<ItemGradingData>> gradingDataMap = Map.of(itemId, gradingData);
        Map<Long, Set<PublishedAnswer>> answerMap = Map.of(itemId, itemAnswers);

        stubData(items, answerMap, gradingDataMap);

        QuestionPoolStatistics poolStatistics = statisticsService.getQuestionPoolStatistics(0L);
        ItemStatistics itemStatistics = poolStatistics.getAggregatedItemStatistics();

        assertEquals(Long.valueOf(7), itemStatistics.getAttemptedResponses());
        assertEquals(Long.valueOf(3), itemStatistics.getCorrectResponses());
        assertEquals(Long.valueOf(4), itemStatistics.getIncorrectResponses());
        assertEquals(Long.valueOf(2), itemStatistics.getBlankResponses());
        assertEquals(Integer.valueOf(67), itemStatistics.getDifficulty());
    }

    @Test
    public void testExtendedMatchingItem() {
        // Match the following animals to their corresponding categories
        // Categories:
        // A: Mammal
        // B: Bird
        // C: Fish
        // D: Reptile
        // E: Arachnid

        // Lion -> A
        // Eagle -> B
        // Salmon -> C
        // Dolphin -> A
        // Turtle -> D
        // Spider -> E
        // Spiderpig -> AE

        long lionItemTextId = 0L;
        long eagleItemTextId = 1L;
        long salmonItemTextId = 2L;
        long dolphinItemTextId = 3L;
        long turtleItemTextId = 4L;
        long spiderItemTextId = 5L;
        long spiderpigItemTextId = 6L;

        Set<ItemTextIfc> itemTexts = Set.of(
            itemText(lionItemTextId, 1),
            itemText(eagleItemTextId, 1),
            itemText(salmonItemTextId, 1),
            itemText(dolphinItemTextId, 1),
            itemText(turtleItemTextId, 1),
            itemText(spiderItemTextId, 1),
            itemText(spiderpigItemTextId, 2)
        );

        long itemId = 0L;
        PublishedItemData item = item(itemId, TypeIfc.EXTENDED_MATCHING_ITEMS, itemTexts);

        Set<PublishedAnswer> itemAnswers = Set.of(
                // Lion ->
                answer(0L, true),   // A
                answer(1L, false),  // B
                answer(3L, false),  // C
                answer(4L, false),  // D
                answer(5L, false),  // E
                // Eagle ->
                answer(6L, false),  // A
                answer(7L, true),   // B
                answer(8L, false),  // C
                answer(9L, false),  // D
                answer(10L, false), // E
                // Salmon ->
                answer(11L, false), // A
                answer(12L, false), // B
                answer(13L, true),  // C
                answer(14L, false), // D
                answer(15L, false), // E
                // Dolphin ->
                answer(16L, true),  // A
                answer(17L, false), // B
                answer(18L, false), // C
                answer(19L, false), // D
                answer(20L, false), // E
                // Turtle ->
                answer(21L, false), // A
                answer(22L, false), // B
                answer(23L, false), // C
                answer(24L, true),  // D
                answer(25L, false), // E
                // Spider ->
                answer(26L, false), // A
                answer(27L, false), // B
                answer(28L, false), // C
                answer(29L, false), // D
                answer(30L, true),  // E
                // Spiderpig ->
                answer(31L, true),  // A
                answer(32L, false), // B
                answer(33L, false), // C
                answer(34L, false), // D
                answer(35L, true)   // E
        );

        Set<ItemGradingData> gradingData = Set.of(
                // All correct - correct
                gradingData(0L, 0L, 0L, lionItemTextId),
                gradingData(1L, 7L, 0L, eagleItemTextId),
                gradingData(2L, 13L, 0L, salmonItemTextId),
                gradingData(3L, 16L, 0L, dolphinItemTextId),
                gradingData(4L, 24L, 0L, turtleItemTextId),
                gradingData(5L, 30L, 0L, spiderItemTextId),
                gradingData(6L, 31L, 0L, spiderpigItemTextId),
                gradingData(7L, 35L, 0L, spiderpigItemTextId),

                // All matched to homework - incorrect
                gradingData(8L, 0L, 1L, lionItemTextId),
                gradingData(9L, 6L, 1L, eagleItemTextId),
                gradingData(10L, 11L, 1L, salmonItemTextId),
                gradingData(11L, 16L, 1L, dolphinItemTextId),
                gradingData(12L, 21L, 1L, turtleItemTextId),
                gradingData(13L, 26L, 1L, spiderItemTextId),
                gradingData(14L, 31L, 1L, spiderpigItemTextId),

                // All correct, but Spiderpig is only matched with A - incorrect
                gradingData(15L, 0L, 2L, lionItemTextId),
                gradingData(16L, 7L, 2L, eagleItemTextId),
                gradingData(17L, 13L, 2L, salmonItemTextId),
                gradingData(18L, 16L, 2L, dolphinItemTextId),
                gradingData(19L, 24L, 2L, turtleItemTextId),
                gradingData(20L, 30L, 2L, spiderItemTextId),
                gradingData(21L, 31L, 2L, spiderpigItemTextId),

                // All incorrect - incorrect
                gradingData(22L, 1L, 3L, lionItemTextId),
                gradingData(23L, 8L, 3L, eagleItemTextId),
                gradingData(24L, 12L, 3L, salmonItemTextId),
                gradingData(25L, 17L, 3L, dolphinItemTextId),
                gradingData(26L, 22L, 3L, turtleItemTextId),
                gradingData(27L, 26L, 3L, spiderItemTextId),
                gradingData(28L, 32L, 3L, spiderpigItemTextId),
                gradingData(29L, 34L, 3L, spiderpigItemTextId),

                // All blank - blank
                gradingData(30L, null, 4L, lionItemTextId),

                // All blank - blank
                gradingData(31L, null, 5L, lionItemTextId),

                // First two swapped - incorrect
                gradingData(32L, 1L, 6L, eagleItemTextId),
                gradingData(33L, 6L, 6L, lionItemTextId),
                gradingData(34L, 13L, 6L, salmonItemTextId),
                gradingData(35L, 16L, 6L, dolphinItemTextId),
                gradingData(36L, 24L, 6L, turtleItemTextId),
                gradingData(37L, 30L, 6L, spiderItemTextId),
                gradingData(38L, 31L, 6L, spiderpigItemTextId),
                gradingData(39L, 35L, 6L, spiderpigItemTextId)
        );

        List<PublishedItemData> items = Collections.singletonList(item);
        Map<Long, Set<ItemGradingData>> gradingDataMap = Map.of(itemId, gradingData);
        Map<Long, Set<PublishedAnswer>> answerMap = Map.of(itemId, itemAnswers);

        stubData(items, answerMap, gradingDataMap);

        QuestionPoolStatistics poolStatistics = statisticsService.getQuestionPoolStatistics(0L);
        ItemStatistics itemStatistics = poolStatistics.getAggregatedItemStatistics();

        assertEquals(Long.valueOf(5), itemStatistics.getAttemptedResponses());
        assertEquals(Long.valueOf(1), itemStatistics.getCorrectResponses());
        assertEquals(Long.valueOf(4), itemStatistics.getIncorrectResponses());
        assertEquals(Long.valueOf(2), itemStatistics.getBlankResponses());
        assertEquals(Integer.valueOf(86), itemStatistics.getDifficulty());
    }

    private void stubData(List<PublishedItemData> items, Map<Long, Set<PublishedAnswer>> answerMap,
            Map<Long, Set<ItemGradingData>> gradingDataMap) {

        stubData(items, answerMap, gradingDataMap, 1L);
    }

    private void stubData(List<PublishedItemData> items, Map<Long, Set<PublishedAnswer>> answerMap,
            Map<Long, Set<ItemGradingData>> gradingDataMap, Long usageCount) {

        stubItems(items);
        stubUsageCount(items, usageCount);
        stubAnswerMap(answerMap);
        stubGradingDataMap(gradingDataMap);
    }

    private void stubItems(List<PublishedItemData> items) {
        doReturn(items).when(statisticsFacadeQueries).getItemDataByHashes(any());
    }

    private void stubUsageCount(List<PublishedItemData> items, Long usageCount){
        doReturn(usageCount).when(statisticsService).getUsageCount(items);
    }

    private void stubAnswerMap(Map<Long, Set<PublishedAnswer>> answerMap) {
        doReturn(answerMap).when(statisticsFacadeQueries).getPublishedAnswerMap(any());
    }

    private void stubGradingDataMap(Map<Long, Set<ItemGradingData>> gradingDataMap) {
        doReturn(gradingDataMap).when(statisticsFacadeQueries).getGradingDataMap(any());
    }

    private PublishedItemData item (Long id, Long typeId) {
        return item(id, typeId, null);
    }

    private PublishedItemData item (Long id, Long typeId, Set<ItemTextIfc> itemTextSet) {
        PublishedItemData item = new PublishedItemData();
        item.setItemId(id);
        item.setTypeId(typeId);
        item.setItemTextSet(itemTextSet);

        return item;
    }

    private ItemGradingData gradingData(Long id, Long answerId) {
        return gradingData(id, answerId, null);
    }

    private ItemGradingData gradingData(Long id, Long answerId, Long assessmentGradingId) {
        return gradingData(id, answerId, assessmentGradingId, null);
    }

    private ItemGradingData gradingData(Long id, Long answerId, Long assessmentGradingId, Long itemTextId) {
        ItemGradingData itemGradingData = new ItemGradingData();
        itemGradingData.setItemGradingId(id);
        itemGradingData.setPublishedAnswerId(answerId);
        itemGradingData.setAssessmentGradingId(assessmentGradingId);
        itemGradingData.setPublishedItemTextId(itemTextId);

        return itemGradingData;
    }

    private PublishedAnswer answer(Long id, boolean correct) {
        return answer(id, correct, null, null, null);
    }

    private PublishedAnswer answer(Long id, boolean correct, Long sequence, String text, String label) {
        PublishedAnswer answer = new PublishedAnswer();
        answer.setId(id);
        answer.setIsCorrect(correct);
        answer.setSequence(sequence);
        answer.setText(text);

        return answer;
    }

    private PublishedItemText itemText(Long id, Integer requiredOptionsCount) {
        return itemText(id, requiredOptionsCount, null, null);
    }

    private PublishedItemText itemText(Long id, Integer requiredOptionsCount, Long sequence, String text) {
        PublishedItemText itemText = new PublishedItemText();
        itemText.setId(id);
        itemText.setRequiredOptionsCount(requiredOptionsCount);
        itemText.setSequence(sequence);
        itemText.setText(text);

        return itemText;
    }
}
