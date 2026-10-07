/*
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
package org.sakaiproject.tool.assessment.facade;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = SamigoPersistenceTestConfiguration.class)
public class AssessmentPersistenceTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired private SessionFactory sessionFactory;
    @Autowired private AssessmentFacadeQueriesAPI assessmentFacadeQueries;
    @Autowired private PersistenceService persistenceService;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private RubricsService rubricsService;
    @Autowired private AssessmentGradingFacadeQueries assessmentGradingFacadeQueries;

    @Test
    public void importsDraftWithItsNewIdAuthorizationAndMetadata() throws Exception {
        Session session = sessionFactory.getCurrentSession();
        AssessmentData source = new AssessmentData();
        source.setTitle("Imported draft");
        source.setStatus(1);
        source.setCreatedBy("instructor");
        source.setLastModifiedBy("instructor");
        source.setCreatedDate(new Date());
        source.setLastModifiedDate(new Date());
        AssessmentAccessControl access = new AssessmentAccessControl();
        access.setAssessmentBase(source);
        access.setReleaseTo(AssessmentAccessControl.ANONYMOUS_USERS);
        source.setAssessmentAccessControl(access);
        session.persist(source);
        AuthorizationData authorization = new AuthorizationData();
        authorization.setAgentIdString("source-site");
        authorization.setFunctionId("EDIT_ASSESSMENT");
        authorization.setQualifierId(source.getAssessmentId().toString());
        authorization.setLastModifiedBy("instructor");
        authorization.setLastModifiedDate(new Date());
        session.persist(authorization);
        session.flush();
        Long sourceId = source.getAssessmentId();
        session.clear();

        Site sourceSite = mock(Site.class);
        Site targetSite = mock(Site.class);
        when(siteService.getSite("source-site")).thenReturn(sourceSite);
        when(siteService.getSite("target-site")).thenReturn(targetSite);
        when(rubricsService.getRubricsForSite("source-site")).thenReturn(Collections.emptyList());
        User instructor = mock(User.class);
        when(instructor.getId()).thenReturn("instructor");
        when(userDirectoryService.getCurrentUser()).thenReturn(instructor);

        boolean previousTestingMode = ComponentManager.testingMode;
        ComponentManager.testingMode = true;
        try {
            ComponentManager.loadComponent(SiteService.class, siteService);
            ComponentManager.loadComponent(UserDirectoryService.class, userDirectoryService);
            ComponentManager.loadComponent("PersistenceService", persistenceService);
            SpringBeanLocator.setApplicationContext(applicationContext);
            Map<String, String> references = new HashMap<>();
            assessmentFacadeQueries.copyAllAssessments("source-site", "target-site", Collections.emptyList(), references);
            session.clear();
            AssessmentData imported = (AssessmentData) assessmentFacadeQueries.getAllActiveAssessmentsByAgent("target-site").get(0);
            assertNotEquals(sourceId, imported.getAssessmentId());
            assertEquals("Imported draft", imported.getTitle());
            assertEquals("true", imported.getAssessmentMetaDataByLabel("markForReview_isInstructorEditable"));
            assertEquals("sam_core/" + imported.getAssessmentId(), references.get("sam_core/" + sourceId));
            assertEquals(1, assessmentFacadeQueries.getAllActiveAssessmentsByAgent("source-site").size());
        } finally {
            SpringBeanLocator.setApplicationContext(null);
            ComponentManager.shutdown();
            ComponentManager.testingMode = previousTestingMode;
        }
    }

    @Test
    public void marksEligibleAttemptsAsProcessedByAutomaticSubmission() {
        Session session = sessionFactory.getCurrentSession();
        Date past = new Date(System.currentTimeMillis() - 86400000L);
        PublishedAssessmentData published = new PublishedAssessmentData();
        published.setTitle("Past due assessment");
        published.setStatus(1);
        published.setCreatedBy("instructor");
        published.setLastModifiedBy("instructor");
        published.setCreatedDate(past);
        published.setLastModifiedDate(past);
        PublishedAccessControl access = new PublishedAccessControl();
        access.setAssessmentBase(published);
        access.setDueDate(past);
        access.setLateHandling(2);
        access.setAutoSubmit(1);
        published.setAssessmentAccessControl(access);
        session.persist(published);
        AssessmentGradingData attempt = new AssessmentGradingData();
        attempt.setPublishedAssessmentId(published.getPublishedAssessmentId());
        attempt.setAgentId("student");
        attempt.setForGrade(true);
        attempt.setIsLate(false);
        attempt.setStatus(AssessmentGradingData.SUBMITTED);
        attempt.setAttemptDate(past);
        attempt.setHasAutoSubmissionRun(false);
        session.persist(attempt);
        session.flush();
        Long attemptId = attempt.getAssessmentGradingId();
        session.clear();

        boolean previousTestingMode = ComponentManager.testingMode;
        ComponentManager.testingMode = true;
        try {
            ComponentManager.loadComponent("PersistenceService", persistenceService);
            SpringBeanLocator.setApplicationContext(applicationContext);
            assertEquals(0, assessmentGradingFacadeQueries.autoSubmitAssessments());
            session.flush();
            session.clear();
            assertTrue(session.get(AssessmentGradingData.class, attemptId).getHasAutoSubmissionRun());
        } finally {
            SpringBeanLocator.setApplicationContext(null);
            ComponentManager.shutdown();
            ComponentManager.testingMode = previousTestingMode;
        }
    }

}
