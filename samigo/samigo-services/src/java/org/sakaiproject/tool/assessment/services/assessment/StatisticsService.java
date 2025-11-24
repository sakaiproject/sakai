/*
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.assessment.business.entity.ItemStatistics;
import org.sakaiproject.tool.assessment.business.entity.QuestionPoolStatistics;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc.TypeId;
import org.sakaiproject.tool.assessment.facade.StatisticsFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticsService {


    public static final String QP_STATISTICS_CACHE_NAME = StatisticsService.class.getPackageName() + "." + QuestionPoolStatistics.CACHE_NAME;

    private static final String LOG_ANSWER_IS_CORRECT_IS_NULL = "Null value for isCorrect on answer with id {}";
    private static final String LOG_GRADING_DATA_IS_CORRECT_IS_NULL = "Null value for isCorrect on item grading data with id {}";
    private static final String LOG_GRADING_DATA_ANSWER_NOT_FOUND = "Could not find PublishedAnswer with id {} referenced in ItemGradingData with id {}";

    private static final String HOT_SPOT_ITEM_BLANK_VALUE = "undefined";

    private GradingService gradingService;

    @Autowired
    private MemoryService memoryService;

    private QuestionPoolService questionPoolService;

    private StatisticsFacadeQueriesAPI statisticsFacadeQueries;

    private Cache<String, QuestionPoolStatistics> questionPoolStatisticsCache;


    public StatisticsService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        statisticsFacadeQueries = PersistenceService.getInstance().getStatisticsFacadeQueries();

        gradingService = new GradingService();
        questionPoolService = new QuestionPoolService();

        init();
    }

    public StatisticsService(GradingService gradingService, MemoryService memoryService,
            QuestionPoolService questionPoolService, StatisticsFacadeQueriesAPI statisticsFacadeQueries) {

        this.gradingService = gradingService;
        this.memoryService = memoryService;
        this.questionPoolService = questionPoolService;
        this.statisticsFacadeQueries = statisticsFacadeQueries;

        init();
    }

    @PostConstruct
    private void init() {
        questionPoolStatisticsCache = memoryService.getCache(QP_STATISTICS_CACHE_NAME);
    }

    public QuestionPoolStatistics getQuestionPoolStatistics(@NonNull Long questionPoolId) {
        // Check the cache for statistics of this pool and return it if present
        QuestionPoolStatistics cachedStatistics = questionPoolStatisticsCache.get(questionPoolId.toString());
        if (cachedStatistics != null) {
            log.debug("Returning cached statistics of question pool with id {}", questionPoolId);
            return cachedStatistics;
        }

        Long qpItemCount = questionPoolService.getItemCount(questionPoolId);
        if (qpItemCount == null) {
            log.warn("Question pool with id {} does not exist", questionPoolId);
            return QuestionPoolStatistics.builder().build();
        }

        Long subpoolCount = questionPoolService.getSubPoolCount(questionPoolId);

        List<PublishedItemData> itemCopies = getItemCopies(questionPoolId);

        Set<Long> itemCopyIds = itemCopies.stream().map(PublishedItemData::getItemId).collect(Collectors.toSet());

        long useCount = getUsageCount(itemCopies);

        Map<Long, Set<PublishedAnswer>> publishedAnswerMap = statisticsFacadeQueries.getPublishedAnswerMap(itemCopyIds);

        Map<Long, Set<ItemGradingData>> gradingDataMap = statisticsFacadeQueries.getGradingDataMap(itemCopyIds);

        List<ItemStatistics> itemCopyStatistics = new ArrayList<>(itemCopies.size());
        for (PublishedItemData itemCopy : itemCopies) {
            Long itemCopyId = itemCopy.getItemId();
            Set<ItemGradingData> gradingData = gradingDataMap.getOrDefault(itemCopyId, Collections.emptySet());
            Set<PublishedAnswer> publishedAnswers = publishedAnswerMap.getOrDefault(itemCopyId, Collections.emptySet());
            ItemStatistics itemStatistics = getItemStatistics(itemCopy, gradingData, publishedAnswers);

            if (itemStatistics != null) {
                itemCopyStatistics.add(itemStatistics);
            }
        }

        ItemStatistics aggregatedItemStatistics = aggregateItemStatistics(itemCopyStatistics);

        QuestionPoolStatistics calculatedStatistics = QuestionPoolStatistics.builder()
                .questionCount(qpItemCount)
                .subpoolCount(subpoolCount)
                .usageCount(useCount)
                .aggregatedItemStatistics(aggregatedItemStatistics)
                .build();

        // Cache the question pool statistics
        questionPoolStatisticsCache.put(questionPoolId.toString(), calculatedStatistics);

        return calculatedStatistics;
    }

    public long getUsageCount(@NonNull Collection<PublishedItemData> items) {
        return items.stream()
                .map(PublishedItemData::getSection)
                .map(SectionDataIfc::getAssessment)
                .map(AssessmentIfc::getAssessmentId)
                .distinct()
                .count();
    }

    private ItemStatistics getItemStatistics(ItemDataIfc item, Set<ItemGradingData> gradingData, Set<PublishedAnswer> answers) {
        Long itemType = item.getTypeId();
        if (itemType == null || !TypeId.isValidId(itemType.longValue())) {
            log.warn("Can not create TypeId from type id {}", itemType);
            return null;
        }

        switch (TypeId.getInstance(itemType)) {
            case TRUE_FALSE_ID:
            case MULTIPLE_CHOICE_ID:
                return getItemStatisticsForItemWithOneCorrectAnswer(gradingData, answers);
            case MULTIPLE_CORRECT_ID:
            case MULTIPLE_CORRECT_SINGLE_SELECTION_ID:
                return getItemStatisticsForItemWithMultipleCorrectAnswers(gradingData, answers);
            case FILL_IN_BLANK_ID:
            case FILL_IN_NUMERIC_ID:
                return getItemStatisticsForFillInItem(item, gradingData, answers);
            case MATCHING_ID:
                return getItemStatisticsForMatchingItem(gradingData, answers);
            case EXTENDED_MATCHING_ITEMS_ID:
                return getItemStatisticsForExtendedMatchingItem(item, gradingData, answers);
            case CALCULATED_QUESTION_ID:
                return getItemStatisticsForCalculatedQuestion(item, gradingData);
            case IMAGEMAP_QUESTION_ID:
                return getItemStatisticsForHotSpotItem(item, gradingData);
            case ESSAY_QUESTION_ID:
            case FILE_UPLOAD_ID:
            case AUDIO_RECORDING_ID:
            case MATRIX_CHOICES_SURVEY_ID:
            case MULTIPLE_CHOICE_SURVEY_ID:
                log.debug("Ignored type with id {}", itemType);
                return ItemStatistics.builder().build();
            default:
                log.warn("Unhandled type with id {}", itemType);
                return ItemStatistics.builder().build();
        }
    }

    // Item is considered correct if one of the answers is correct
    private ItemStatistics getItemStatisticsForItemWithOneCorrectAnswer(Set<ItemGradingData> gradingData, Set<PublishedAnswer> answers) {
        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity()));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (ItemGradingData itemGradingData : gradingData) {
            Long selectedAnswerId = itemGradingData.getPublishedAnswerId();
            if (selectedAnswerId == null) {
                blankResponses++;
                continue;
            }

            PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
            if (selectedAnswer == null) {
                log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, selectedAnswerId, itemGradingData.getItemGradingId());
                continue;
            }

            Boolean answerCorrect = selectedAnswer.getIsCorrect();
            if (answerCorrect == null) {
                log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, selectedAnswer.getId());
                continue;
            }

            if (answerCorrect) {
                correctResponses++;
            } else {
                incorrectResponses++;
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    // Item is considered correct, if all the selected answers are correct and the
    // number of selected answers is correct
    private ItemStatistics getItemStatisticsForFillInItem(ItemDataIfc item, Set<ItemGradingData> gradingData,
            Set<PublishedAnswer> answers) {

        Map<Long, Set<ItemGradingData>> itemgradingDataByAssessmentGradingId = gradingData.stream()
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId, Collectors.toSet()));

        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity()));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Set<ItemGradingData> submissionItemGradingData : itemgradingDataByAssessmentGradingId.values()) {
            int presentAnswerCount = submissionItemGradingData.size();
            int correctAnswers = 0;
            int incorrectAnswers = 0;
            int blankAnswers = 0;

            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long answerId = itemGradingData.getPublishedAnswerId();
                if (answerId == null) {
                    // With a blank answer there should only one ItemGradingData per submission
                    // But to be safe, let's break out of the loop to avoid double counting
                    blankResponses++;
                    break;
                }

                PublishedAnswer answer = answerMap.get(answerId);
                if (answer == null) {
                    log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, answerId, itemGradingData.getItemGradingId());
                    continue;
                }

                // The the text that the student entered
                String answerText = itemGradingData.getAnswerText();
                if (StringUtils.isBlank(answerText)) {
                    blankAnswers++;
                    continue;
                }

                Long typeId = item.getTypeId();

                // Check the result using the GradingServe, switching between FIB and FIN
                if ((TypeIfc.FILL_IN_BLANK.equals(typeId)
                                && gradingService.getFIBResult(itemGradingData, Collections.emptyMap(), item, answerMap))
                        || (TypeIfc.FILL_IN_NUMERIC.equals(typeId)
                                && gradingService.getFINResult(itemGradingData, item, answerMap))) {
                    correctAnswers++;
                    continue;
                } else {
                    incorrectAnswers++;
                    continue;
                }
            }

            // Even if all selected answers are correct, we also need to compare the count
            // to know that all correct answers were selected
            if (correctAnswers == presentAnswerCount) {
                correctResponses++;
            } else if (blankAnswers == presentAnswerCount) {
                blankResponses++;
            } else if (incorrectAnswers != 0) {
                incorrectResponses++;
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    private ItemStatistics getItemStatisticsForMatchingItem(Set<ItemGradingData> gradingData, Set<PublishedAnswer> answers) {
        Map<Long, Set<ItemGradingData>> itemgradingDataByAssessmentGradingId = gradingData.stream()
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId, Collectors.toSet()));

        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity()));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Set<ItemGradingData> submissionItemGradingData : itemgradingDataByAssessmentGradingId.values()) {
            int presentAnswerCount = submissionItemGradingData.size();
            int correctAnswers = 0;
            int blankAnswers = 0;

            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long answerId = itemGradingData.getPublishedAnswerId();
                if (answerId == null) {
                    // With a blank answer there should only one ItemGradingData per submission
                    // But to be safe, let's break out of the loop to avoid double counting
                    blankAnswers++;
                    continue;
                }

                PublishedAnswer answer = answerMap.get(answerId);
                if (answer == null) {
                    log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, answerId, itemGradingData.getItemGradingId());
                    continue;
                }

                PublishedAnswer selectedAnswer = answerMap.get(answerId);
                if (selectedAnswer == null) {
                    log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, answerId, itemGradingData.getItemGradingId());
                    continue;
                }

                Boolean answerCorrect = selectedAnswer.getIsCorrect();
                if (answerCorrect == null) {
                    log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, answerId);
                    continue;
                }

                if (answerCorrect) {
                    correctAnswers++;
                }
            }

            // Even if all selected answers are correct, we also need to compare the count
            // to know that
            // all correct answers were selected
            if (correctAnswers == presentAnswerCount) {
                correctResponses++;
            } else if (blankAnswers == presentAnswerCount) {
                blankResponses++;
            } else {
                incorrectResponses++;
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    // Item is considered correct, if all the selected answers are correct and the
    // number of selected answers is correct
    private ItemStatistics getItemStatisticsForItemWithMultipleCorrectAnswers(Set<ItemGradingData> gradingData, Set<PublishedAnswer> answers) {
        Map<Long, Set<ItemGradingData>> itemgradingDataByAssessmentGradingId = gradingData.stream()
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId, Collectors.toSet()));

        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity()));

        long correctAnswerCount = answers.stream()
                .map(PublishedAnswer::getIsCorrect)
                .filter(Boolean.TRUE::equals)
                .count();

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Set<ItemGradingData> submissionItemGradingData : itemgradingDataByAssessmentGradingId.values()) {
            int selectedAnswerCount = submissionItemGradingData.size();
            Boolean hasIncorrectAnswer = null;

            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long selectedAnswerId = itemGradingData.getPublishedAnswerId();
                if (selectedAnswerId == null) {
                    // With a blank answer there should only one ItemGradingData per submission
                    // But to be safe, let's break out of the loop to avoid double counting
                    blankResponses++;
                    break;
                }

                PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
                if (selectedAnswer == null) {
                    log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, selectedAnswerId, itemGradingData.getItemGradingId());
                    continue;
                }

                Boolean answerCorrect = selectedAnswer.getIsCorrect();
                if (answerCorrect == null) {
                    log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, answerCorrect);
                    continue;
                }

                if (!answerCorrect) {
                    // Found incorrect answer, we can count it and break te loop
                    hasIncorrectAnswer = true;
                    incorrectResponses++;
                    break;
                } else if (hasIncorrectAnswer == null) {
                    hasIncorrectAnswer = false;
                }
            }

            // Even if all selected answers are correct, we also need to compare the count
            // to know that all correct answers were selected
            if (Boolean.FALSE.equals(hasIncorrectAnswer) && selectedAnswerCount == correctAnswerCount) {
                correctResponses++;
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    private ItemStatistics getItemStatisticsForExtendedMatchingItem(ItemDataIfc item, Set<ItemGradingData> gradingData,
            Set<PublishedAnswer> answers) {

        Map<Long, Map<Long, Set<ItemGradingData>>> groupedGradingData = gradingData.stream()
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId,
                        Collectors.groupingBy(ItemGradingData::getPublishedItemTextId, Collectors.toSet())));

        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity()));

        Map<Long, ItemTextIfc> itemTextMap = item.getItemTextSet().stream()
                .collect(Collectors.toMap(ItemTextIfc::getId, Function.identity()));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Map<Long, Set<ItemGradingData>> submissionItemGradingData : groupedGradingData.values()) {
            long requiredOptionsCount = 0;
            int correctOptions = 0;
            boolean isBlank = false;

            // One option of an EMI item can have multiple answers, so we need to iterate
            // through the grading data by item text
            submissionLoop:
            for (Map.Entry<Long, Set<ItemGradingData>> submissionGradingDataEntry : submissionItemGradingData.entrySet()) {
                Long itemTextId = submissionGradingDataEntry.getKey();
                ItemTextIfc itemText = itemTextMap.get(itemTextId);

                Set<ItemGradingData> itemTextGradingData = submissionGradingDataEntry.getValue();

                requiredOptionsCount++;

                Integer requiredAnswerCount = itemText.getRequiredOptionsCount();
                if (requiredAnswerCount == null) {
                    log.warn("requiredOptionsCount is null on ItemText with id {}", itemTextId);
                    continue;
                }

                int correctAnswers = 0;
                int incorrectAnswers = 0;

                for (ItemGradingData optionGradingData : itemTextGradingData) {
                    Long selectedAnswerId = optionGradingData.getPublishedAnswerId();
                    if (selectedAnswerId == null) {
                        // With a blank answer there should only one ItemGradingData per submission
                        // But to be safe, let's break out of the loop to avoid double counting
                        blankResponses++;
                        isBlank = true;
                        break submissionLoop;
                    }

                    PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
                    if (selectedAnswer == null) {
                        log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND,
                                selectedAnswerId, optionGradingData.getItemGradingId());
                        continue;
                    }

                    Boolean answerCorrect = selectedAnswer.getIsCorrect();
                    if (answerCorrect == null) {
                        log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, selectedAnswerId);
                        continue;
                    }

                    if (answerCorrect) {
                        correctAnswers++;
                    } else {
                        incorrectAnswers++;
                    }
                }

                if (incorrectAnswers <= 0 && correctAnswers >= requiredAnswerCount) {
                    correctOptions++;
                }
            }

            if (!isBlank) {
                if (correctOptions == requiredOptionsCount) {
                    correctResponses++;
                } else {
                    incorrectResponses++;
                }
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    private ItemStatistics getItemStatisticsForCalculatedQuestion(ItemDataIfc item, Set<ItemGradingData> gradingData) {
        Map<Long, Set<ItemGradingData>> itemGradingDataByAssessmentGradingId = gradingData.stream()
                .sorted(Comparator.comparing(ItemGradingData::getPublishedAnswerId))
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId, Collectors.toCollection(LinkedHashSet::new)));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Set<ItemGradingData> submissionItemGradingData : itemGradingDataByAssessmentGradingId.values()) {
            long correctAnswers = 0;
            long blankAnswers = 0;

            int answerSequence = 0;
            Long previousAnswerId = null;
            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long currentAnswerId = itemGradingData.getPublishedAnswerId();

                // This is the way to check if an item submission is empty but for calculated
                // questions the answerId is populated, so the usual case will be a blank answer text
                if (currentAnswerId == null || StringUtils.isBlank(itemGradingData.getAnswerText())) {
                    blankAnswers++;
                    continue;
                }

                if (!Objects.equals(previousAnswerId, currentAnswerId)) {
                    answerSequence++;
                    previousAnswerId = currentAnswerId;
                }

                // Populate answerMap
                Map<Integer, String> answerMap = new HashMap<>();
                gradingService.extractCalcQAnswersArray(answerMap, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                        item, itemGradingData.getAssessmentGradingId(), itemGradingData.getAgentId());

                if (gradingService.getCalcQResult(itemGradingData, item, answerMap, answerSequence)) {
                    correctAnswers++;
                }
            }

            int maxAnswerSize = submissionItemGradingData.size();
            if (maxAnswerSize == correctAnswers) {
                correctResponses++;
            } else if (maxAnswerSize == blankAnswers) {
                blankResponses++;
            } else {
                incorrectResponses++;
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    private ItemStatistics getItemStatisticsForHotSpotItem(ItemDataIfc item, Set<ItemGradingData> gradingData) {
        Map<Long, Set<ItemGradingData>> itemGradingDataByAssessmentGradingId = gradingData.stream()
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId, Collectors.toSet()));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Set<ItemGradingData> submissionItemGradingData : itemGradingDataByAssessmentGradingId.values()) {
            long correctAnswers = 0;
            long blankAnswers = 0;

            int answerSequence = 0;
            Long previousAnswerId = null;
            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long currentAnswerId = itemGradingData.getPublishedAnswerId();

                // A blank hot spot item submission will have the coordinates in the answer text set to undefined
                String answerText = itemGradingData.getAnswerText();
                if (StringUtils.contains(answerText, HOT_SPOT_ITEM_BLANK_VALUE) || StringUtils.isBlank(answerText)) {
                    blankAnswers++;
                    continue;
                }

                if (!Objects.equals(previousAnswerId, currentAnswerId)) {
                    answerSequence++;
                    previousAnswerId = currentAnswerId;
                }

                Boolean isCorrect = itemGradingData.getIsCorrect();
                if (isCorrect == null) {
                    log.warn(LOG_GRADING_DATA_IS_CORRECT_IS_NULL, itemGradingData.getItemGradingId());
                }

                if (isCorrect) {
                    correctAnswers++;
                }
            }

            int maxAnswerSize = submissionItemGradingData.size();
            if (maxAnswerSize == correctAnswers) {
                correctResponses++;
            } else if (maxAnswerSize == blankAnswers) {
                blankResponses++;
            } else {
                incorrectResponses++;
            }
        }

        long attemptedResponses = correctResponses + incorrectResponses;

        return ItemStatistics.builder()
                .attemptedResponses(attemptedResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .blankResponses(blankResponses)
                .calcDifficulty()
                .build();
    }

    private ItemStatistics aggregateItemStatistics(Collection<ItemStatistics> itemsStatistics) {
        if (itemsStatistics.isEmpty()) {
            return ItemStatistics.builder().build();
        }

        // If only one item is present return it right as is
        if (itemsStatistics.size() == 1) {
            return itemsStatistics.iterator().next();
        }

        long totalAttemptedResponses = 0;
        long totalCorrectResponses = 0;
        long totalIncorrectResponses = 0;
        long totalBlankResponses = 0;

        for (ItemStatistics itemStatistics : itemsStatistics) {
            Long attemptedResponses = itemStatistics.getAttemptedResponses();
            if (attemptedResponses != null) {
                totalAttemptedResponses += attemptedResponses;
            }

            Long correctResponses = itemStatistics.getCorrectResponses();
            if (correctResponses != null) {
                totalCorrectResponses += correctResponses;
            }

            Long incorrectResponses = itemStatistics.getIncorrectResponses();
            if (incorrectResponses != null) {
                totalIncorrectResponses += incorrectResponses;
            }

            Long blankResponses = itemStatistics.getBlankResponses();
            if (blankResponses != null) {
                totalBlankResponses += blankResponses;
            }
        }

        return ItemStatistics.builder()
                .attemptedResponses(totalAttemptedResponses)
                .correctResponses(totalCorrectResponses)
                .incorrectResponses(totalIncorrectResponses)
                .blankResponses(totalBlankResponses)
                .calcDifficulty()
                .build();
    }

    private List<PublishedItemData> getItemCopies(Long questionPoolId) {
        Set<String> itemHashes = questionPoolService.getAllItemHashes(questionPoolId);

        return statisticsFacadeQueries.getItemDataByHashes(itemHashes);
    }
}
