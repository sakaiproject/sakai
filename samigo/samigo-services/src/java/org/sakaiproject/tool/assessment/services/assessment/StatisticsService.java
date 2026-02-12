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
import java.util.EnumMap;
import java.util.EnumSet;
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
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
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

    public enum SubmissionOutcome {
        CORRECT,
        INCORRECT,
        BLANK,
        NOT_APPLICABLE
    }

    public enum QuestionTypeCapability {
        SUBMISSION_OUTCOME,
        TOTAL_SCORES_TALLY,
        DETAILED_STATISTICS,
        DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
        ANSWER_STATISTICS,
        SCORE_STATISTICS,
        SURVEY
    }

    private static final EnumMap<TypeId, EnumSet<QuestionTypeCapability>> QUESTION_TYPE_CAPABILITIES = buildQuestionTypeCapabilities();

    private static EnumMap<TypeId, EnumSet<QuestionTypeCapability>> buildQuestionTypeCapabilities() {
        EnumMap<TypeId, EnumSet<QuestionTypeCapability>> capabilities = new EnumMap<>(TypeId.class);

        registerTypeCapabilities(capabilities, TypeId.TRUE_FALSE_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.MULTIPLE_CHOICE_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.MULTIPLE_CORRECT_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.MULTIPLE_CORRECT_SINGLE_SELECTION_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.FILL_IN_BLANK_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.FILL_IN_NUMERIC_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.MATCHING_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.EXTENDED_MATCHING_ITEMS_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.CALCULATED_QUESTION_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.IMAGEMAP_QUESTION_ID,
                QuestionTypeCapability.SUBMISSION_OUTCOME,
                QuestionTypeCapability.TOTAL_SCORES_TALLY,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.MULTIPLE_CHOICE_SURVEY_ID,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS,
                QuestionTypeCapability.SURVEY);
        registerTypeCapabilities(capabilities, TypeId.MATRIX_CHOICES_SURVEY_ID,
                QuestionTypeCapability.DETAILED_STATISTICS,
                QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS,
                QuestionTypeCapability.ANSWER_STATISTICS,
                QuestionTypeCapability.SURVEY);
        registerTypeCapabilities(capabilities, TypeId.ESSAY_QUESTION_ID, QuestionTypeCapability.SCORE_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.FILE_UPLOAD_ID, QuestionTypeCapability.SCORE_STATISTICS);
        registerTypeCapabilities(capabilities, TypeId.AUDIO_RECORDING_ID, QuestionTypeCapability.SCORE_STATISTICS);

        return capabilities;
    }

    private static void registerTypeCapabilities(EnumMap<TypeId, EnumSet<QuestionTypeCapability>> capabilities,
            TypeId typeId, QuestionTypeCapability... questionTypeCapabilities) {
        capabilities.put(typeId, EnumSet.copyOf(List.of(questionTypeCapabilities)));
    }

    public static boolean hasQuestionTypeCapability(Long typeId, QuestionTypeCapability capability) {
        return hasQuestionTypeCapability(toTypeId(typeId), capability);
    }

    public static boolean hasQuestionTypeCapability(String typeId, QuestionTypeCapability capability) {
        return hasQuestionTypeCapability(toTypeId(typeId), capability);
    }

    private static boolean hasQuestionTypeCapability(TypeId typeId, QuestionTypeCapability capability) {
        if (typeId == null || capability == null) {
            return false;
        }

        EnumSet<QuestionTypeCapability> capabilities = QUESTION_TYPE_CAPABILITIES.get(typeId);
        return capabilities != null && capabilities.contains(capability);
    }

    public static Set<QuestionTypeCapability> getQuestionTypeCapabilities(Long typeId) {
        TypeId resolvedTypeId = toTypeId(typeId);
        if (resolvedTypeId == null) {
            return Collections.emptySet();
        }

        EnumSet<QuestionTypeCapability> capabilities = QUESTION_TYPE_CAPABILITIES.get(resolvedTypeId);
        if (capabilities == null || capabilities.isEmpty()) {
            return Collections.emptySet();
        }
        return EnumSet.copyOf(capabilities);
    }

    public static boolean supportsSubmissionOutcome(Long typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.SUBMISSION_OUTCOME);
    }

    public static boolean supportsTotalScoresTally(Long typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.TOTAL_SCORES_TALLY);
    }

    public static boolean includesInDetailedStatistics(String typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.DETAILED_STATISTICS);
    }

    public static boolean showsIndividualAnswersInDetailedStatistics(String typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.DETAILED_STATISTICS_INDIVIDUAL_ANSWERS);
    }

    public static boolean supportsAnswerStatistics(String typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.ANSWER_STATISTICS);
    }

    public static boolean supportsScoreStatistics(String typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.SCORE_STATISTICS);
    }

    public static boolean isSurveyQuestionType(String typeId) {
        return hasQuestionTypeCapability(typeId, QuestionTypeCapability.SURVEY);
    }

    private static TypeId toTypeId(Long typeId) {
        if (typeId == null || !TypeId.isValidId(typeId.longValue())) {
            return null;
        }
        return TypeId.getInstance(typeId.longValue());
    }

    private static TypeId toTypeId(String typeId) {
        if (StringUtils.isBlank(typeId)) {
            return null;
        }

        long parsedTypeId;
        try {
            parsedTypeId = Long.parseLong(typeId);
        } catch (NumberFormatException e) {
            return null;
        }

        if (!TypeId.isValidId(parsedTypeId)) {
            return null;
        }
        return TypeId.getInstance(parsedTypeId);
    }


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

    public SubmissionOutcome classifySubmission(ItemDataIfc item, Collection<ItemGradingData> submissionGradingData,
            Map<Long, ? extends AnswerIfc> answerMap) {
        Long itemType = item == null ? null : item.getTypeId();
        if (!supportsSubmissionOutcome(itemType)) {
            return SubmissionOutcome.NOT_APPLICABLE;
        }

        Set<ItemGradingData> submissionSet = submissionGradingData == null ? Collections.emptySet() : submissionGradingData
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (submissionSet.isEmpty()) {
            return SubmissionOutcome.BLANK;
        }

        Set<PublishedAnswer> publishedAnswers = toPublishedAnswerSet(item, submissionSet, answerMap);
        ItemStatistics itemStatistics;
        TypeId resolvedTypeId = toTypeId(itemType);
        if (resolvedTypeId == null) {
            return SubmissionOutcome.NOT_APPLICABLE;
        }

        switch (resolvedTypeId) {
            case TRUE_FALSE_ID:
            case MULTIPLE_CHOICE_ID:
                itemStatistics = getItemStatisticsForItemWithOneCorrectAnswer(submissionSet, publishedAnswers);
                break;
            case MULTIPLE_CORRECT_ID:
                itemStatistics = getItemStatisticsForItemWithMultipleCorrectAnswers(submissionSet, publishedAnswers);
                break;
            case MULTIPLE_CORRECT_SINGLE_SELECTION_ID:
                itemStatistics = getItemStatisticsForMultipleCorrectSingleSelectionItem(submissionSet, publishedAnswers);
                break;
            case FILL_IN_BLANK_ID:
            case FILL_IN_NUMERIC_ID:
                itemStatistics = getItemStatisticsForFillInItem(item, submissionSet, publishedAnswers);
                break;
            case MATCHING_ID:
                itemStatistics = getItemStatisticsForMatchingItem(submissionSet, publishedAnswers);
                break;
            case EXTENDED_MATCHING_ITEMS_ID:
                itemStatistics = getItemStatisticsForExtendedMatchingItem(item, submissionSet, publishedAnswers);
                break;
            case CALCULATED_QUESTION_ID:
                itemStatistics = getItemStatisticsForCalculatedQuestion(item, submissionSet);
                break;
            case IMAGEMAP_QUESTION_ID:
                itemStatistics = getItemStatisticsForHotSpotItem(item, submissionSet);
                break;
            default:
                return SubmissionOutcome.NOT_APPLICABLE;
        }

        return toSubmissionOutcome(itemStatistics);
    }

    private SubmissionOutcome toSubmissionOutcome(ItemStatistics itemStatistics) {
        if (itemStatistics == null) {
            return SubmissionOutcome.NOT_APPLICABLE;
        }

        long incorrectResponses = itemStatistics.getIncorrectResponses() == null ? 0 : itemStatistics.getIncorrectResponses();
        if (incorrectResponses > 0) {
            return SubmissionOutcome.INCORRECT;
        }

        long correctResponses = itemStatistics.getCorrectResponses() == null ? 0 : itemStatistics.getCorrectResponses();
        if (correctResponses > 0) {
            return SubmissionOutcome.CORRECT;
        }

        long blankResponses = itemStatistics.getBlankResponses() == null ? 0 : itemStatistics.getBlankResponses();
        if (blankResponses > 0) {
            return SubmissionOutcome.BLANK;
        }

        return SubmissionOutcome.NOT_APPLICABLE;
    }

    private Set<PublishedAnswer> toPublishedAnswerSet(ItemDataIfc item, Set<ItemGradingData> submissionSet,
            Map<Long, ? extends AnswerIfc> answerMap) {
        Map<Long, PublishedAnswer> publishedAnswers = new HashMap<>();
        Long itemId = item == null ? null : item.getItemId();
        Set<Long> selectedAnswerIds = submissionSet.stream()
                .map(ItemGradingData::getPublishedAnswerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<AnswerIfc> itemAnswers = getItemAnswers(item);
        boolean hasItemAnswers = !itemAnswers.isEmpty();

        for (AnswerIfc answer : itemAnswers) {
            PublishedAnswer publishedAnswer = toPublishedAnswer(answer);
            if (publishedAnswer != null && publishedAnswer.getId() != null) {
                publishedAnswers.put(publishedAnswer.getId(), publishedAnswer);
            }
        }

        if (answerMap != null) {
            if (hasItemAnswers) {
                for (Long answerId : selectedAnswerIds) {
                    AnswerIfc answer = answerMap.get(answerId);
                    PublishedAnswer publishedAnswer = toPublishedAnswer(answer);
                    if (publishedAnswer != null && publishedAnswer.getId() != null) {
                        publishedAnswers.put(publishedAnswer.getId(), publishedAnswer);
                    }
                }
            } else {
                for (AnswerIfc answer : answerMap.values()) {
                    if (answer == null || answer.getId() == null) {
                        continue;
                    }
                    boolean sameItem = answer.getItem() != null && itemId != null
                            && itemId.equals(answer.getItem().getItemId());
                    if (!sameItem && !selectedAnswerIds.contains(answer.getId())) {
                        continue;
                    }
                    PublishedAnswer publishedAnswer = toPublishedAnswer(answer);
                    if (publishedAnswer != null) {
                        publishedAnswers.put(publishedAnswer.getId(), publishedAnswer);
                    }
                }
            }
        }

        return new LinkedHashSet<>(publishedAnswers.values());
    }

    private List<AnswerIfc> getItemAnswers(ItemDataIfc item) {
        List<AnswerIfc> answers = new ArrayList<>();
        if (item == null || item.getItemTextSet() == null) {
            return answers;
        }

        for (ItemTextIfc itemText : item.getItemTextSet()) {
            if (itemText == null || itemText.getAnswerSet() == null) {
                continue;
            }
            for (Object answerObject : itemText.getAnswerSet()) {
                AnswerIfc answer = (AnswerIfc) answerObject;
                if (answer != null) {
                    answers.add(answer);
                }
            }
        }
        return answers;
    }

    private PublishedAnswer toPublishedAnswer(AnswerIfc answer) {
        if (answer == null) {
            return null;
        }

        if (answer instanceof PublishedAnswer) {
            return (PublishedAnswer) answer;
        }

        PublishedAnswer publishedAnswer = new PublishedAnswer();
        publishedAnswer.setId(answer.getId());
        publishedAnswer.setIsCorrect(answer.getIsCorrect());
        publishedAnswer.setScore(answer.getScore());
        return publishedAnswer;
    }

    private ItemStatistics getItemStatistics(ItemDataIfc item, Set<ItemGradingData> gradingData, Set<PublishedAnswer> answers) {
        Long itemType = item == null ? null : item.getTypeId();
        if (itemType == null || !TypeId.isValidId(itemType.longValue())) {
            log.warn("Can not create TypeId from type id {}", itemType);
            return null;
        }

        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity(), (existing, replacement) -> existing, HashMap::new));

        Map<Long, List<ItemGradingData>> gradingBySubmission = groupGradingDataBySubmission(gradingData);
        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (List<ItemGradingData> submissionGradingData : gradingBySubmission.values()) {
            SubmissionOutcome submissionOutcome = classifySubmission(item, submissionGradingData, answerMap);
            if (submissionOutcome == SubmissionOutcome.CORRECT) {
                correctResponses++;
            } else if (submissionOutcome == SubmissionOutcome.INCORRECT) {
                incorrectResponses++;
            } else if (submissionOutcome == SubmissionOutcome.BLANK) {
                blankResponses++;
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

    private Map<Long, List<ItemGradingData>> groupGradingDataBySubmission(Set<ItemGradingData> gradingData) {
        Map<Long, List<ItemGradingData>> grouped = new HashMap<>();
        long syntheticKey = Long.MIN_VALUE;
        for (ItemGradingData itemGradingData : gradingData) {
            if (itemGradingData == null) {
                continue;
            }

            Long assessmentGradingId = itemGradingData.getAssessmentGradingId();
            if (assessmentGradingId == null) {
                Long itemGradingId = itemGradingData.getItemGradingId();
                assessmentGradingId = itemGradingId == null ? syntheticKey++ : -Math.abs(itemGradingId);
            }

            grouped.computeIfAbsent(assessmentGradingId, key -> new ArrayList<>()).add(itemGradingData);
        }
        return grouped;
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

            if (isSelectedAnswerCorrect(itemGradingData, answerMap)) {
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

    // Item is considered correct if the single selected answer is correct.
    // This method is dedicated to MULTIPLE_CORRECT_SINGLE_SELECTION to keep
    // per-submission classification stable even with legacy/anomalous grading rows.
    private ItemStatistics getItemStatisticsForMultipleCorrectSingleSelectionItem(Set<ItemGradingData> gradingData, Set<PublishedAnswer> answers) {
        Map<Long, Set<ItemGradingData>> itemgradingDataByAssessmentGradingId = gradingData.stream()
                .collect(Collectors.groupingBy(ItemGradingData::getAssessmentGradingId, Collectors.toSet()));

        Map<Long, PublishedAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(PublishedAnswer::getId, Function.identity()));

        long correctResponses = 0;
        long incorrectResponses = 0;
        long blankResponses = 0;

        for (Set<ItemGradingData> submissionItemGradingData : itemgradingDataByAssessmentGradingId.values()) {
            boolean hasAnsweredOption = false;
            boolean hasCorrectAnswer = false;
            boolean hasIncorrectAnswer = false;
            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                if (!isMcssResponsePresent(itemGradingData)) {
                    continue;
                }

                hasAnsweredOption = true;
                if (isMcssSelectionCorrect(itemGradingData, answerMap)) {
                    hasCorrectAnswer = true;
                } else {
                    hasIncorrectAnswer = true;
                }

                if (hasIncorrectAnswer) {
                    break;
                }
            }

            if (!hasAnsweredOption) {
                blankResponses++;
                continue;
            }

            if (hasCorrectAnswer && !hasIncorrectAnswer) {
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

    private boolean isMcssResponsePresent(ItemGradingData itemGradingData) {
        if (itemGradingData.getPublishedAnswerId() != null) {
            return true;
        }

        if (StringUtils.isNotBlank(itemGradingData.getAnswerText())) {
            return true;
        }

        if (itemGradingData.getIsCorrect() != null) {
            return true;
        }

        Double autoScore = itemGradingData.getAutoScore();
        return autoScore != null && Double.compare(autoScore, 0d) != 0;
    }

    private boolean isMcssSelectionCorrect(ItemGradingData itemGradingData, Map<Long, PublishedAnswer> answerMap) {
        if (itemGradingData.getIsCorrect() != null) {
            return itemGradingData.getIsCorrect();
        }

        Double autoScore = itemGradingData.getAutoScore();
        if (autoScore != null) {
            return autoScore > 0;
        }

        Long selectedAnswerId = itemGradingData.getPublishedAnswerId();
        if (selectedAnswerId == null) {
            return false;
        }

        PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
        if (selectedAnswer != null && selectedAnswer.getIsCorrect() != null) {
            return selectedAnswer.getIsCorrect();
        }

        if (selectedAnswer == null) {
            log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, selectedAnswerId, itemGradingData.getItemGradingId());
        } else {
            log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, selectedAnswer.getId());
        }

        if (selectedAnswer != null && selectedAnswer.getScore() != null) {
            return selectedAnswer.getScore() > 0;
        }

        return false;
    }

    private boolean isSelectedAnswerCorrect(ItemGradingData itemGradingData, Map<Long, PublishedAnswer> answerMap) {
        Long selectedAnswerId = itemGradingData.getPublishedAnswerId();
        if (selectedAnswerId == null) {
            return false;
        }

        PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
        if (selectedAnswer != null && selectedAnswer.getIsCorrect() != null) {
            return selectedAnswer.getIsCorrect();
        }

        if (selectedAnswer == null) {
            log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, selectedAnswerId, itemGradingData.getItemGradingId());
        } else {
            log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, selectedAnswer.getId());
        }

        if (itemGradingData.getIsCorrect() != null) {
            return itemGradingData.getIsCorrect();
        }

        Double autoScore = itemGradingData.getAutoScore();
        if (autoScore != null) {
            return autoScore > 0;
        }

        if (selectedAnswer != null && selectedAnswer.getScore() != null) {
            return selectedAnswer.getScore() > 0;
        }

        return false;
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
            boolean hasNullAnswer = false;

            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long answerId = itemGradingData.getPublishedAnswerId();
                if (answerId == null) {
                    // With a blank answer there should only one ItemGradingData per submission
                    // But to be safe, let's break out of the loop to avoid double counting
                    hasNullAnswer = true;
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

            if (hasNullAnswer) {
                blankResponses++;
                continue;
            }

            // Even if all selected answers are correct, we also need to compare the count
            // to know that all correct answers were selected
            if (blankAnswers == presentAnswerCount) {
                blankResponses++;
            } else if (correctAnswers == presentAnswerCount) {
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
            boolean hasIncorrectAnswer = false;
            boolean hasBlankAnswer = false;

            for (ItemGradingData itemGradingData : submissionItemGradingData) {
                Long selectedAnswerId = itemGradingData.getPublishedAnswerId();
                if (selectedAnswerId == null) {
                    // With a blank answer there should only one ItemGradingData per submission
                    // But to be safe, let's break out of the loop to avoid double counting
                    blankResponses++;
                    hasBlankAnswer = true;
                    break;
                }

                PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
                if (selectedAnswer == null) {
                    log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND, selectedAnswerId, itemGradingData.getItemGradingId());
                    hasIncorrectAnswer = true;
                    break;
                }

                Boolean answerCorrect = selectedAnswer.getIsCorrect();
                if (answerCorrect == null) {
                    log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, selectedAnswer.getId());
                    hasIncorrectAnswer = true;
                    break;
                }

                if (!answerCorrect) {
                    hasIncorrectAnswer = true;
                    break;
                }
            }

            if (hasBlankAnswer) {
                continue;
            }

            // Even if all selected answers are correct, we also need to compare the count
            // to know that all correct answers were selected
            if (!hasIncorrectAnswer && selectedAnswerCount == correctAnswerCount) {
                correctResponses++;
            } else if (selectedAnswerCount > 0) {
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
            boolean hasAnsweredOption = false;
            boolean hasIncorrectOption = false;

            // One option of an EMI item can have multiple answers, so we need to iterate
            // through the grading data by item text
            for (Map.Entry<Long, Set<ItemGradingData>> submissionGradingDataEntry : submissionItemGradingData.entrySet()) {
                Long itemTextId = submissionGradingDataEntry.getKey();
                Set<ItemGradingData> itemTextGradingData = submissionGradingDataEntry.getValue();
                requiredOptionsCount++;

                ItemTextIfc itemText = itemTextMap.get(itemTextId);
                if (itemText == null) {
                    log.warn("Could not find ItemText with id {} for EMI grading data", itemTextId);
                    if (itemTextGradingData.stream().anyMatch(option -> option.getPublishedAnswerId() != null)) {
                        hasAnsweredOption = true;
                    }
                    hasIncorrectOption = true;
                    continue;
                }

                Integer requiredAnswerCount = itemText.getRequiredOptionsCount();
                if (requiredAnswerCount == null) {
                    log.warn("requiredOptionsCount is null on ItemText with id {}", itemTextId);
                    hasIncorrectOption = true;
                    continue;
                }

                int correctAnswers = 0;
                int incorrectAnswers = 0;
                boolean hasBlankAnswer = false;

                for (ItemGradingData optionGradingData : itemTextGradingData) {
                    Long selectedAnswerId = optionGradingData.getPublishedAnswerId();
                    if (selectedAnswerId == null) {
                        hasBlankAnswer = true;
                        continue;
                    }
                    hasAnsweredOption = true;

                    PublishedAnswer selectedAnswer = answerMap.get(selectedAnswerId);
                    if (selectedAnswer == null) {
                        log.warn(LOG_GRADING_DATA_ANSWER_NOT_FOUND,
                                selectedAnswerId, optionGradingData.getItemGradingId());
                        incorrectAnswers++;
                        continue;
                    }

                    Boolean answerCorrect = selectedAnswer.getIsCorrect();
                    if (answerCorrect == null) {
                        log.warn(LOG_ANSWER_IS_CORRECT_IS_NULL, selectedAnswerId);
                        incorrectAnswers++;
                        continue;
                    }

                    if (answerCorrect) {
                        correctAnswers++;
                    } else {
                        incorrectAnswers++;
                    }
                }

                if (!hasBlankAnswer && incorrectAnswers <= 0 && correctAnswers >= requiredAnswerCount) {
                    correctOptions++;
                } else {
                    hasIncorrectOption = true;
                }
            }

            if (!hasAnsweredOption) {
                blankResponses++;
                continue;
            }

            if (!hasIncorrectOption && correctOptions == requiredOptionsCount) {
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

    private ItemStatistics getItemStatisticsForCalculatedQuestion(ItemDataIfc item, Set<ItemGradingData> gradingData) {
        Map<Long, Set<ItemGradingData>> itemGradingDataByAssessmentGradingId = gradingData.stream()
                .sorted(Comparator.comparing(ItemGradingData::getPublishedAnswerId, Comparator.nullsLast(Long::compareTo)))
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
            long attemptedAnswers = maxAnswerSize - blankAnswers;
            if (attemptedAnswers <= 0) {
                blankResponses++;
            } else if (attemptedAnswers == correctAnswers) {
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

                if (Boolean.TRUE.equals(isCorrect)) {
                    correctAnswers++;
                }
            }

            int maxAnswerSize = submissionItemGradingData.size();
            long attemptedAnswers = maxAnswerSize - blankAnswers;
            if (attemptedAnswers <= 0) {
                blankResponses++;
            } else if (attemptedAnswers == correctAnswers) {
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
