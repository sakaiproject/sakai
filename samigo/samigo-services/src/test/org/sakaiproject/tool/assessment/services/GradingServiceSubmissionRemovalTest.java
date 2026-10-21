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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
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
    @Autowired private SecurityService securityService;
    @Autowired private SiteService siteService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private GradingService gradingService;
    @Autowired private PersistenceService persistenceService;
    @Autowired private PersistenceHelper persistenceHelper;
    @Autowired private RubricsService rubricsService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ApplicationContext applicationContext;

    private final List<Long> submissionIds = new ArrayList<>();
    private final String siteId = "removal-site";
    private Long assessmentId;
    private Long authorizationId;
    private ResourceProperties siteProperties;

    @BeforeClass
    public static void initializeLocators() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void setUp() throws Exception {
        reset(rubricsService, toolManager, userDirectoryService, securityService, siteService, sessionManager, serverConfigurationService);
        ComponentManager.loadComponent(SecurityService.class, securityService);
        ComponentManager.loadComponent(SiteService.class, siteService);
        ComponentManager.loadComponent(SessionManager.class, sessionManager);
        ComponentManager.loadComponent(ServerConfigurationService.class, serverConfigurationService);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        when(siteService.siteReference(siteId)).thenReturn("/site/" + siteId);
        when(siteService.siteReference("!admin")).thenReturn("/site/!admin");
        when(securityService.unlock(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, "/site/" + siteId)).thenReturn(true);
        Site site = mock(Site.class);
        siteProperties = mock(ResourceProperties.class);
        when(site.getProperties()).thenReturn(siteProperties);
        when(siteService.getSite(siteId)).thenReturn(site);
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
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Session session = sessionFactory.getCurrentSession();
            Date now = Date.from(Instant.now());
            PublishedAssessmentData assessment = new PublishedAssessmentData();
            assessment.setTitle("Submission removal");
            assessment.setStatus(1);
            assessment.setCreatedBy("instructor");
            assessment.setCreatedDate(now);
            assessment.setLastModifiedBy("instructor");
            assessment.setLastModifiedDate(now);
            session.persist(assessment);
            assessmentId = assessment.getPublishedAssessmentId();
            AuthorizationData authorization = new AuthorizationData(siteId, "OWN_PUBLISHED_ASSESSMENT",
                    assessmentId.toString(), now, null, "instructor", now, true);
            session.persist(authorization);
            authorizationId = authorization.getSurrogateKey();
        });
    }

    @After
    public void removeFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Session session = sessionFactory.getCurrentSession();
            submissionIds.forEach(id -> session.remove(session.get(AssessmentGradingData.class, id)));
            session.remove(session.get(AuthorizationData.class, authorizationId));
            session.remove(session.get(PublishedAssessmentData.class, assessmentId));
        });
    }

    @Test
    public void cleanupFailureLeavesSubmissionAvailableForRetry() {
        AssessmentGradingData submission = createSubmission();
        IllegalStateException failure = new IllegalStateException("Rubric cleanup unavailable");
        when(rubricsService.deleteEvaluationsForToolAndEvaluatedItems(
                RubricsConstants.RBCS_TOOL_SAMIGO, Set.of(submission.getAssessmentGradingId() + ".200"), siteId))
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
    public void failedSubmissionPersistenceIsReportedAndLeavesAttemptAvailableForRetry() {
        AssessmentGradingData submission = createSubmission();
        int retryCount = persistenceHelper.getRetryCount();
        // Exercise the real DAO's false-save path with an exhausted retry budget.
        persistenceHelper.setRetryCount(0);
        try {
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> gradingService.removeAssessmentGradingData(submission));
            assertTrue(failure.getMessage().contains(submission.getAssessmentGradingId().toString()));
        } finally {
            persistenceHelper.setRetryCount(retryCount);
        }
        verify(rubricsService).deleteEvaluationsForToolAndEvaluatedItems(
                RubricsConstants.RBCS_TOOL_SAMIGO, Set.of(submission.getAssessmentGradingId() + ".200"), siteId);
        AssessmentGradingData retry = gradingService.load(submission.getAssessmentGradingId().toString());
        assertEquals(Integer.valueOf(1), retry.getStatus());
        assertTrue(retry.getForGrade());

        gradingService.removeAssessmentGradingData(retry);
        assertEquals(Integer.valueOf(AssessmentGradingData.REMOVED),
                gradingService.load(submission.getAssessmentGradingId().toString()).getStatus());
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

    @Test
    public void rubricsEvaluatorPermissionDoesNotAuthorizeSubmissionRemoval() {
        AssessmentGradingData submission = createSubmission();
        when(securityService.unlock(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EVALUATOR, "/site/" + siteId)).thenReturn(true);
        assertRemovalDenied(submission);
    }

    @Test
    public void ownAssessmentGraderMustOwnAssessmentAndNeedsNoRubricsPermission() {
        AssessmentGradingData submission = createSubmission();
        when(securityService.unlock(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_OWN, "/site/" + siteId)).thenReturn(true);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("another-grader");
        assertRemovalDenied(submission);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        gradingService.removeAssessmentGradingData(submission);
        assertEquals(Integer.valueOf(AssessmentGradingData.REMOVED),
                gradingService.load(submission.getAssessmentGradingId().toString()).getStatus());
    }

    @Test
    public void wrongSiteAndAnonymousUsersCannotRemoveSubmission() {
        AssessmentGradingData submission = createSubmission();
        when(toolManager.getCurrentPlacement().getContext()).thenReturn("other-site");
        when(siteService.siteReference("other-site")).thenReturn("/site/other-site");
        when(securityService.unlock(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, "/site/other-site")).thenReturn(true);
        assertRemovalDenied(submission);
        when(toolManager.getCurrentPlacement().getContext()).thenReturn(siteId);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(null);
        assertRemovalDenied(submission);
    }

    @Test
    public void siteAndGlobalRemovalRestrictionsApplyBeforeRubricCleanup() {
        AssessmentGradingData submission = createSubmission();
        String restriction = "samigo.removeSubmission.restricted";
        when(serverConfigurationService.getBoolean(restriction, false)).thenReturn(true);
        assertRemovalDenied(submission);
        when(serverConfigurationService.getBoolean(restriction, false)).thenReturn(false);
        when(siteProperties.getProperty(restriction)).thenReturn("true");
        assertRemovalDenied(submission);
        when(securityService.unlock("site.upd", "/site/!admin")).thenReturn(true);
        gradingService.removeAssessmentGradingData(submission);
        assertEquals(Integer.valueOf(AssessmentGradingData.REMOVED),
                gradingService.load(submission.getAssessmentGradingId().toString()).getStatus());
    }

    private void assertRemovalDenied(AssessmentGradingData submission) {
        assertThrows(SecurityException.class, () -> gradingService.removeAssessmentGradingData(submission));
        verifyNoInteractions(rubricsService);
        AssessmentGradingData unchanged = gradingService.load(submission.getAssessmentGradingId().toString());
        assertEquals(Integer.valueOf(1), unchanged.getStatus());
        assertTrue(unchanged.getForGrade());
    }

    private AssessmentGradingData createSubmission() {
        Long id = new TransactionTemplate(transactionManager).execute(status -> {
            Session session = sessionFactory.getCurrentSession();
            AssessmentGradingData submission = new AssessmentGradingData();
            submission.setPublishedAssessmentId(assessmentId);
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
