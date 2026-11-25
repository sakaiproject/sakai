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

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * An essay with a minimum score gets it only when the answer meets the word count and keywords
 * stored on the published item.
 */
@ContextConfiguration(classes = SamigoCancellationTestConfiguration.class)
public class GradingServiceEssayMinScoreTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final double MIN_SCORE = 2.0;

    @Autowired private GradingService gradingService;
    @Autowired private PersistenceService persistenceService;
    @Autowired private SessionFactory sessionFactory;

    @BeforeClass
    public static void initializeLocators() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void setUp() {
        ComponentManager.loadComponent("PersistenceService", persistenceService);
    }

    @Test
    public void answerMeetingCriteriaGetsMinimumScore() {
        assertEquals(MIN_SCORE, grade("5", "cell, wall", "<p>The cell wall protects the plant.</p>"), 0.0);
    }

    @Test
    public void answerShorterThanWordCountGetsNothing() {
        assertEquals(0.0, grade("5", "cell, wall", "<p>cell wall</p>"), 0.0);
    }

    @Test
    public void answerMissingAKeywordGetsNothing() {
        assertEquals(0.0, grade("5", "cell, wall", "<p>The cell membrane protects the plant.</p>"), 0.0);
    }

    @Test
    public void essayWithoutCriteriaKeepsMinimumScoreForAnyAnswer() {
        assertEquals(MIN_SCORE, grade(null, null, "Hi"), 0.0);
    }

    private double grade(String wordCount, String keywords, String answer) {
        ItemDataIfc item = publishEssay(wordCount, keywords);

        ItemGradingData itemGrading = new ItemGradingData();
        itemGrading.setPublishedItemId(item.getItemId());
        itemGrading.setPublishedItemTextId(item.getItemTextArray().get(0).getId());
        itemGrading.setAnswerText(answer);
        AssessmentGradingData submission = new AssessmentGradingData();
        submission.setAssessmentGradingId(0L);
        submission.setAgentId("student");
        submission.setTotalOverrideScore(0.0);
        submission.setItemGradingSet(new HashSet<>(Set.of(itemGrading)));

        Map<Long, ItemDataIfc> publishedItemHash = new HashMap<>();
        publishedItemHash.put(item.getItemId(), item);
        gradingService.storeGrades(submission, false, null, publishedItemHash, new HashMap<>(), new HashMap<>(), false);
        return itemGrading.getAutoScore();
    }

    // Persists and reloads the essay so its metadata goes through the Hibernate mappings
    private ItemDataIfc publishEssay(String wordCount, String keywords) {
        Date now = Date.from(Instant.now());
        PublishedAssessmentData assessment = new PublishedAssessmentData();
        assessment.setTitle("Essay minimum score");
        assessment.setStatus(1);
        assessment.setCreatedBy("instructor");
        assessment.setCreatedDate(now);
        assessment.setLastModifiedBy("instructor");
        assessment.setLastModifiedDate(now);
        assessment.setAssessmentMetaDataSet(new HashSet<>());
        PublishedEvaluationModel evaluation = new PublishedEvaluationModel();
        evaluation.setAssessment(assessment);
        evaluation.setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
        assessment.setEvaluationModel(evaluation);
        PublishedAccessControl access = new PublishedAccessControl();
        access.setAssessment(assessment);
        assessment.setAssessmentAccessControl(access);
        PublishedSectionData section = new PublishedSectionData(0, 1, "Part", "", 1L, 1, "instructor", now, "instructor", now);
        section.setAssessment(assessment);
        section.setSectionMetaDataSet(new HashSet<>());

        PublishedItemData item = new PublishedItemData();
        item.setSection(section);
        item.setSequence(1);
        item.setTypeId(TypeIfc.ESSAY_QUESTION);
        item.setScore(10.0);
        item.setMinScore(MIN_SCORE);
        item.setDiscount(0D);
        item.setPartialCreditFlag(false);
        item.setIsExtraCredit(false);
        item.setStatus(1);
        item.setCreatedBy("instructor");
        item.setCreatedDate(now);
        item.setLastModifiedBy("instructor");
        item.setLastModifiedDate(now);
        item.setHash("essay");
        item.setItemMetaDataSet(new HashSet<>());
        if (wordCount != null) {
            item.addItemMetaData(ItemMetaDataIfc.MIN_SCORE_WORD_COUNT, wordCount);
        }
        if (keywords != null) {
            item.addItemMetaData(ItemMetaDataIfc.MIN_SCORE_KEYWORDS, keywords);
        }
        item.setItemAttachmentSet(new HashSet<>());
        item.setItemTextSet(new HashSet<>(Set.of(new PublishedItemText(item, 1L, "Describe a plant cell", new HashSet<>()))));
        section.setItemSet(new HashSet<>(Set.of(item)));
        assessment.setSectionSet(new HashSet<>(Set.of(section)));

        sessionFactory.getCurrentSession().persist(assessment);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        return sessionFactory.getCurrentSession().get(PublishedItemData.class, item.getItemId());
    }
}
