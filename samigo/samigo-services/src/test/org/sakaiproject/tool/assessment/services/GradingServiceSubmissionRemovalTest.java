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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SamigoCancellationTestConfiguration.class)
public class GradingServiceSubmissionRemovalTest {
    @Autowired private GradingService gradingService;
    @Autowired private PersistenceService persistenceService;
    @Autowired private RubricsService rubricsService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ApplicationContext applicationContext;

    private final List<Long> submissionIds = new ArrayList<>();
    private final String siteId = "removal-site";

    @BeforeClass
    public static void initializeLocators() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void setUp() {
        reset(rubricsService, toolManager, userDirectoryService);
        ComponentManager.loadComponent("PersistenceService", persistenceService);
        ComponentManager.loadComponent(RubricsService.class, rubricsService);
        ComponentManager.loadComponent("agentHelper", applicationContext.getBean("agentHelper"));
        ComponentManager.loadComponent(ToolManager.class, toolManager);
        ComponentManager.loadComponent(UserDirectoryService.class, userDirectoryService);
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn(siteId);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        User user = mock(User.class);
        when(user.getId()).thenReturn("instructor");
        when(userDirectoryService.getCurrentUser()).thenReturn(user);
    }

    @After
    public void removeFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Session session = sessionFactory.getCurrentSession();
            submissionIds.forEach(id -> session.remove(session.get(AssessmentGradingData.class, id)));
        });
    }

    @Test
    public void cleanupFailureLeavesSubmissionAvailableForRetry() {
        AssessmentGradingData submission = createSubmission();
        String evaluatedItemId = submission.getAssessmentGradingId() + ".200";
        IllegalStateException failure = new IllegalStateException("Rubric cleanup unavailable");
        when(rubricsService.deleteEvaluationsForToolAndEvaluatedItems(
                RubricsConstants.RBCS_TOOL_SAMIGO, Set.of(evaluatedItemId), siteId))
                .thenThrow(failure).thenReturn(1);

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> gradingService.removeAssessmentGradingData(submission)));
        AssessmentGradingData retry = gradingService.load(submission.getAssessmentGradingId().toString());
        assertEquals(Integer.valueOf(1), retry.getStatus());
        assertTrue(retry.getForGrade());

        gradingService.removeAssessmentGradingData(retry);
        AssessmentGradingData removed = gradingService.load(submission.getAssessmentGradingId().toString());
        assertEquals(Integer.valueOf(AssessmentGradingData.REMOVED), removed.getStatus());
        assertFalse(removed.getForGrade());
    }

    @Test
    public void successfulRemovalLeavesOtherAttemptAvailable() {
        AssessmentGradingData submission = createSubmission();
        AssessmentGradingData remaining = createSubmission();
        gradingService.removeAssessmentGradingData(submission);
        verify(rubricsService).deleteEvaluationsForToolAndEvaluatedItems(
                RubricsConstants.RBCS_TOOL_SAMIGO, Set.of(submission.getAssessmentGradingId() + ".200"), siteId);
        AssessmentGradingData removed = gradingService.load(submission.getAssessmentGradingId().toString());
        assertEquals(Integer.valueOf(AssessmentGradingData.REMOVED), removed.getStatus());
        assertFalse(removed.getForGrade());
        AssessmentGradingData unchanged = gradingService.load(remaining.getAssessmentGradingId().toString());
        assertEquals(Integer.valueOf(1), unchanged.getStatus());
        assertTrue(unchanged.getForGrade());
    }

    private AssessmentGradingData createSubmission() {
        Long id = new TransactionTemplate(transactionManager).execute(status -> {
            Session session = sessionFactory.getCurrentSession();
            AssessmentGradingData submission = new AssessmentGradingData();
            submission.setPublishedAssessmentId(100L);
            submission.setAgentId("student");
            submission.setIsLate(false);
            submission.setForGrade(true);
            submission.setStatus(1);
            session.persist(submission);
            ItemGradingData response = new ItemGradingData();
            response.setAssessmentGradingId(submission.getAssessmentGradingId());
            response.setPublishedItemId(200L);
            response.setPublishedItemTextId(300L);
            response.setAgentId("student");
            session.persist(response);
            return submission.getAssessmentGradingId();
        });
        submissionIds.add(id);
        return gradingService.load(id.toString());
    }
}
