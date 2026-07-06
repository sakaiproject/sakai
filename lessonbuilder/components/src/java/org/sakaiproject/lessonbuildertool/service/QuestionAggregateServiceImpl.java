/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageProperty;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.api.QuestionAggregateCalculator;
import org.sakaiproject.lessonbuildertool.api.QuestionAggregateService;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

/**
 * Shared inline-question gradebook item: settings live in lesson_builder_properties
 * (per-site keys), membership is the questionAggregate item attribute, and scores
 * are always recomputed from lesson_builder_q_responses — never incremented — so
 * re-answers, regrades, and overrides can't drift. Registered as a
 * ScheduledInvocationCommand so a final pass runs after the item's due date.
 */
@Slf4j
public class QuestionAggregateServiceImpl implements QuestionAggregateService, ScheduledInvocationCommand {

    private static final String PROP_TITLE = "question-aggregate-title";
    private static final String PROP_MODE = "question-aggregate-mode";
    private static final String PROP_ESSAY = "question-aggregate-essay";
    private static final String PROP_ITEM_POINTS = "question-aggregate-item-points";
    private static final String PROP_POINTS = "question-aggregate-points";
    private static final String PROP_DUE = "question-aggregate-due";
    private static final String PROP_AUTOINCLUDE = "question-aggregate-autoinclude";

    /** component id used for the delayed final-pass invocation; must match the bean id */
    public static final String BEAN_ID = "org.sakaiproject.lessonbuildertool.api.QuestionAggregateService";

    @Setter private SimplePageToolDao simplePageToolDao;
    @Setter private GradingService gradingService;
    @Setter private ScheduledInvocationManager scheduledInvocationManager;

    public void init() {
        log.info("init()");
    }

    // ---- site properties ----

    private String getProperty(String base, String siteId) {
        SimplePageProperty prop = simplePageToolDao.findProperty(base + " " + siteId);
        return prop == null ? null : prop.getValue();
    }

    private void setProperty(String base, String siteId, String value) {
        String attribute = base + " " + siteId;
        SimplePageProperty prop = simplePageToolDao.findProperty(attribute);
        if (prop == null) {
            if (StringUtils.isBlank(value)) {
                return;
            }
            prop = simplePageToolDao.makeProperty(attribute, value);
            simplePageToolDao.quickSaveItem(prop);
        } else {
            prop.setValue(value == null ? "" : value);
            simplePageToolDao.quickUpdate(prop);
        }
    }

    @Override
    public boolean isEnabled(String siteId) {
        return StringUtils.isNotBlank(getTitle(siteId));
    }

    @Override
    public String getTitle(String siteId) {
        return getProperty(PROP_TITLE, siteId);
    }

    @Override
    public String getMode(String siteId) {
        String mode = getProperty(PROP_MODE, siteId);
        return QuestionAggregateCalculator.MODE_CORRECTNESS.equals(mode)
                ? mode : QuestionAggregateCalculator.MODE_PARTICIPATION;
    }

    @Override
    public String getEssayMode(String siteId) {
        String essayMode = getProperty(PROP_ESSAY, siteId);
        return QuestionAggregateCalculator.ESSAY_CARRY_GRADE.equals(essayMode)
                ? essayMode : QuestionAggregateCalculator.ESSAY_ANY_ANSWER;
    }

    @Override
    public Double getPerQuestionPoints(String siteId) {
        return parsePositive(getProperty(PROP_ITEM_POINTS, siteId));
    }

    @Override
    public boolean isAutoInclude(String siteId) {
        return "true".equals(getProperty(PROP_AUTOINCLUDE, siteId));
    }

    @Override
    public Double getFixedPoints(String siteId) {
        return parsePositive(getProperty(PROP_POINTS, siteId));
    }

    private Double parsePositive(String value) {
        return QuestionAggregateCalculator.parsePositivePoints(value);
    }

    @Override
    public Instant getDueDate(String siteId) {
        String due = getProperty(PROP_DUE, siteId);
        if (StringUtils.isBlank(due)) {
            return null;
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(due));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    // ---- configuration ----

    @Override
    public boolean configure(String siteId, String title, String mode, String essayMode,
            Double perQuestionPoints, Double fixedPoints, Instant dueDate, boolean autoInclude) {
        title = StringUtils.trimToNull(title);
        setProperty(PROP_TITLE, siteId, title == null ? "" : title);
        scheduledInvocationManager.deleteDelayedInvocation(BEAN_ID, siteId);

        if (title == null) {
            // blank title disables the feature and removes the shared gradebook item
            removeExternalItem(siteId);
            return true;
        }

        setProperty(PROP_MODE, siteId, QuestionAggregateCalculator.MODE_CORRECTNESS.equals(mode)
                ? QuestionAggregateCalculator.MODE_CORRECTNESS : QuestionAggregateCalculator.MODE_PARTICIPATION);
        setProperty(PROP_ESSAY, siteId, QuestionAggregateCalculator.ESSAY_CARRY_GRADE.equals(essayMode)
                ? QuestionAggregateCalculator.ESSAY_CARRY_GRADE : QuestionAggregateCalculator.ESSAY_ANY_ANSWER);
        setProperty(PROP_ITEM_POINTS, siteId, perQuestionPoints != null && perQuestionPoints > 0 ? String.valueOf(perQuestionPoints) : "");
        setProperty(PROP_POINTS, siteId, fixedPoints != null && fixedPoints > 0 ? String.valueOf(fixedPoints) : "");
        setProperty(PROP_DUE, siteId, dueDate != null ? String.valueOf(dueDate.toEpochMilli()) : "");
        setProperty(PROP_AUTOINCLUDE, siteId, String.valueOf(autoInclude));

        if (dueDate != null && dueDate.isAfter(Instant.now())) {
            // final pass shortly after the due date so late-arriving grades settle
            scheduledInvocationManager.createDelayedInvocation(dueDate.plusSeconds(60), BEAN_ID, siteId);
        }

        return recompute(siteId);
    }

    @Override
    public void copyConfig(String fromSiteId, String toSiteId) {
        if (!isEnabled(fromSiteId)) {
            return;
        }
        for (String base : new String[] {PROP_TITLE, PROP_MODE, PROP_ESSAY, PROP_ITEM_POINTS,
                PROP_POINTS, PROP_DUE, PROP_AUTOINCLUDE}) {
            String value = getProperty(base, fromSiteId);
            setProperty(base, toSiteId, value == null ? "" : value);
        }
        scheduledInvocationManager.deleteDelayedInvocation(BEAN_ID, toSiteId);
        Instant due = getDueDate(toSiteId);
        if (due != null && due.isAfter(Instant.now())) {
            scheduledInvocationManager.createDelayedInvocation(due.plusSeconds(60), BEAN_ID, toSiteId);
        }
        // creates the destination's gradebook item from the copied membership flags;
        // fails quietly (logged) if the destination has no gradebook yet — the first
        // settings save there will create it
        recompute(toSiteId);
    }

    @Override
    public void setDueDate(String siteId, Instant dueDate) {
        if (!isEnabled(siteId)) {
            return;
        }
        setProperty(PROP_DUE, siteId, dueDate != null ? String.valueOf(dueDate.toEpochMilli()) : "");
        scheduledInvocationManager.deleteDelayedInvocation(BEAN_ID, siteId);
        if (dueDate != null && dueDate.isAfter(Instant.now())) {
            scheduledInvocationManager.createDelayedInvocation(dueDate.plusSeconds(60), BEAN_ID, siteId);
        }
        // refresh the displayed date on the gradebook item without touching scores
        if (gradingService.isExternalAssignmentDefined(siteId, externalId(siteId))) {
            try {
                double max = QuestionAggregateCalculator.maxPoints(countMembers(siteId),
                        getPerQuestionPoints(siteId), getFixedPoints(siteId));
                gradingService.updateExternalAssessment(siteId, externalId(siteId), null, null,
                        getTitle(siteId), null, max, dueDate != null ? Date.from(dueDate) : null, null);
            } catch (Exception e) {
                log.warn("Could not update due date on shared question item in site {}", siteId, e);
            }
        }
    }

    /** whether a question is an effective member: flagged and not group-restricted
     *  (group-restricted questions are barred because out-of-group students could
     *  never earn those shares). The single definition every scoring path shares,
     *  so the item max and the score denominator can never disagree. */
    private boolean isEffectiveMember(SimplePageItem question) {
        return "true".equals(question.getAttribute(MEMBER_ATTRIBUTE))
                && !QuestionAggregateService.isGroupRestricted(question);
    }

    private List<SimplePageItem> memberQuestions(String siteId) {
        List<SimplePageItem> members = new ArrayList<>();
        for (SimplePageItem question : findSiteQuestionItems(siteId)) {
            if (isEffectiveMember(question)) {
                members.add(question);
            }
        }
        return members;
    }

    private int countMembers(String siteId) {
        return memberQuestions(siteId).size();
    }

    @Override
    public int includeAllQuestions(String siteId, long pageId) {
        List<SimplePageItem> questions = pageId > 0
                ? simplePageToolDao.findItemsOnPage(pageId)
                : findSiteQuestionItems(siteId);
        int flagged = 0;
        for (SimplePageItem question : questions) {
            if (question.getType() == SimplePageItem.QUESTION
                    && !QuestionAggregateService.isGroupRestricted(question)
                    && !"true".equals(question.getAttribute(MEMBER_ATTRIBUTE))) {
                question.setAttribute(MEMBER_ATTRIBUTE, "true");
                simplePageToolDao.quickUpdate(question);
                flagged++;
            }
        }
        return flagged;
    }

    // ---- scoring ----

    private String externalId(String siteId) {
        return EXTERNAL_ID_PREFIX + siteId;
    }

    /** Every inline question in the site, minus questions on student-authored or
     *  hidden pages (which can't be answered by the class). One query for the
     *  question ids, then cached item/page loads — not a full page-by-page walk. */
    private List<SimplePageItem> findSiteQuestionItems(String siteId) {
        List<SimplePageItem> questions = new ArrayList<>();
        for (SimplePageItem question : simplePageToolDao.findQuestionItemsInSite(siteId)) {
            SimplePage page = simplePageToolDao.getPage(question.getPageId());
            if (page == null || page.getOwner() != null || page.isHidden()) {
                continue;
            }
            questions.add(question);
        }
        return questions;
    }

    private void removeExternalItem(String siteId) {
        try {
            gradingService.removeExternalAssignment(null, externalId(siteId), LessonBuilderConstants.TOOL_ID);
        } catch (Exception e) {
            log.debug("Nothing to remove for {}", externalId(siteId), e);
        }
    }

    @Override
    public boolean recompute(String siteId) {
        Map<SimplePageItem, List<SimplePageQuestionResponse>> members = new HashMap<>();
        Set<String> allResponders = new HashSet<>();
        for (SimplePageItem question : findSiteQuestionItems(siteId)) {
            List<SimplePageQuestionResponse> responses = simplePageToolDao.findQuestionResponses(question.getId());
            if (responses != null) {
                responses.forEach(r -> allResponders.add(r.getUserId()));
            }
            if (isEffectiveMember(question)) {
                members.put(question, responses);
            }
        }

        if (!isEnabled(siteId) || members.isEmpty()) {
            removeExternalItem(siteId);
            return true;
        }

        String title = getTitle(siteId);
        Double perQuestionPoints = getPerQuestionPoints(siteId);
        Double fixedPoints = getFixedPoints(siteId);
        double max = QuestionAggregateCalculator.maxPoints(members.size(), perQuestionPoints, fixedPoints);
        Instant due = getDueDate(siteId);
        Date dueDate = due != null ? Date.from(due) : null;

        try {
            if (gradingService.isExternalAssignmentDefined(siteId, externalId(siteId))) {
                gradingService.updateExternalAssessment(siteId, externalId(siteId), null, null, title, null, max, dueDate, null);
            } else {
                gradingService.addExternalAssessment(siteId, siteId, externalId(siteId), null, title, max, dueDate,
                        LessonBuilderConstants.TOOL_ID, null, null, null, null);
            }
        } catch (ConflictingAssignmentNameException cane) {
            // an unrelated gradebook item already owns this title
            log.warn("Conflicting gradebook item title '{}' in site {}", title, siteId);
            return false;
        } catch (Exception e) {
            log.warn("Could not register shared question gradebook item in site {}", siteId, e);
            return false;
        }

        Map<String, Double> contributions = QuestionAggregateCalculator.computeContributions(
                members, getMode(siteId), getEssayMode(siteId), allResponders);
        Map<String, String> scores = QuestionAggregateCalculator.toScores(
                contributions, members.size(), perQuestionPoints, fixedPoints);
        if (!scores.isEmpty()) {
            try {
                gradingService.updateExternalAssessmentScoresString(siteId, siteId, externalId(siteId), scores);
            } catch (Exception e) {
                log.warn("Could not push shared question scores in site {}", siteId, e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateScoreForUser(String siteId, String userId) {
        if (!isEnabled(siteId)) {
            return;
        }
        String mode = getMode(siteId);
        String essayMode = getEssayMode(siteId);
        double shares = 0;
        List<SimplePageItem> members = memberQuestions(siteId);
        for (SimplePageItem question : members) {
            SimplePageQuestionResponse response = simplePageToolDao.findQuestionResponse(question.getId(), userId);
            if (response != null) {
                shares += QuestionAggregateCalculator.contribution(question, response, mode, essayMode);
            }
        }
        int memberCount = members.size();
        if (memberCount == 0) {
            return;
        }
        Map<String, String> score = QuestionAggregateCalculator.toScores(
                java.util.Collections.singletonMap(userId, shares), memberCount,
                getPerQuestionPoints(siteId), getFixedPoints(siteId));
        try {
            gradingService.updateExternalAssessmentScore(siteId, siteId, externalId(siteId), userId, score.get(userId));
        } catch (Exception e) {
            log.warn("Could not push shared question score for {} in site {}", userId, siteId, e);
        }
    }

    /** Scheduled final pass after the due date; opaqueContext is the site id. */
    @Override
    public void execute(String opaqueContext) {
        log.info("Post-due-date recompute of the shared question gradebook item for site {}", opaqueContext);
        recompute(opaqueContext);
    }
}
