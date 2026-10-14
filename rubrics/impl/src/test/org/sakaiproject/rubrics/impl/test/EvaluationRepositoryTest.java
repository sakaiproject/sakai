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

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RubricsTestConfiguration.class)
public class EvaluationRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AssociationRepository associationRepository;
    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private RubricRepository rubricRepository;
    @Autowired private SessionFactory sessionFactory;

    @Test
    public void findFirstByAssociationIdAndOwnerIdLimitsResultsAndIncludesInactiveAssociations() {

        Rubric rubric = new Rubric();
        rubric.setTitle("Graded rubric");
        rubric.setOwnerId("site1");
        rubric.setCreatorId("instructor");
        rubric = rubricRepository.save(rubric);

        ToolItemRubricAssociation association = new ToolItemRubricAssociation();
        association.setRubric(rubric);
        association.setToolId("sakai.assignment");
        association.setItemId("assignment1");
        association.setActive(false);
        association = associationRepository.save(association);
        Long associationId = association.getId();

        saveEvaluation(associationId, "site2", "other-site-submission");
        saveEvaluation(associationId + 1, "site1", "other-association-submission");
        for (int i = 0; i < 20; i++) {
            saveEvaluation(associationId, "site1", "submission-" + i);
        }
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Statistics statistics = sessionFactory.getStatistics();
        boolean statisticsEnabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        long loadedBefore = statistics.getEntityLoadCount();
        try {
            Optional<Evaluation> result = evaluationRepository.findFirstByAssociationIdAndOwnerId(associationId, "site1");

            assertTrue(result.isPresent());
            assertEquals(associationId, result.get().getAssociationId());
            assertEquals("site1", result.get().getOwnerId());
            assertEquals(1L, statistics.getEntityLoadCount() - loadedBefore);
            assertFalse(evaluationRepository.findFirstByAssociationIdAndOwnerId(associationId, "missing-site").isPresent());
            assertFalse(evaluationRepository.findFirstByAssociationIdAndOwnerId(associationId + 2, "site1").isPresent());
        } finally {
            statistics.setStatisticsEnabled(statisticsEnabled);
        }
    }

    private void saveEvaluation(Long associationId, String siteId, String itemId) {

        Evaluation evaluation = new Evaluation();
        evaluation.setAssociationId(associationId);
        evaluation.setOwnerId(siteId);
        evaluation.setEvaluatorId("instructor");
        evaluation.setEvaluatedItemId(itemId);
        evaluation.setEvaluatedItemOwnerId("student");
        evaluationRepository.save(evaluation);
    }
}
