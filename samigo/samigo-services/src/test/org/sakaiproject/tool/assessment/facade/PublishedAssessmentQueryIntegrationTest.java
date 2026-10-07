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

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionMetaData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.authz.integrated.AuthzQueriesFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Exercise public queries against Samigo's real Hibernate mappings. */
@ContextConfiguration(locations = "/spring-samigo-query-integration.xml")
public class PublishedAssessmentQueryIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired private SessionFactory sessionFactory;
    @Autowired private PublishedAssessmentFacadeQueries publishedAssessmentFacadeQueries;
    @Autowired private PublishedItemFacadeQueriesAPI publishedItemFacadeQueries;
    @Autowired private AuthzQueriesFacade authzQueriesFacade;
    @Autowired private AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries;
    private PublishedAssessmentData assessment;
    private PublishedSectionData section;
    private PublishedItemData item;

    @Before
    public void seedAssessment() {
        Session session = sessionFactory.getCurrentSession();
        Date now = new Date();
        assessment = new PublishedAssessmentData();
        assessment.setTitle("Mapped assessment");
        assessment.setStatus(1);
        assessment.setCreatedBy("test");
        assessment.setCreatedDate(now);
        assessment.setLastModifiedBy("test");
        assessment.setLastModifiedDate(now);
        session.persist(assessment);
        section = new PublishedSectionData();
        section.setAssessment(assessment);
        section.setTypeId(1L);
        section.setStatus(1);
        section.setCreatedBy("test");
        section.setCreatedDate(now);
        section.setLastModifiedBy("test");
        section.setLastModifiedDate(now);
        session.persist(section);
        item = new PublishedItemData();
        item.setSection(section);
        item.setTypeId(1L);
        item.setStatus(1);
        item.setCreatedBy("test");
        item.setCreatedDate(now);
        item.setLastModifiedBy("test");
        item.setLastModifiedDate(now);
        session.persist(item);
        session.flush();
    }

    @Test
    public void findsPublishedAssessmentForItem() {
        assertEquals(assessment.getPublishedAssessmentId(), publishedItemFacadeQueries.getPublishedAssessmentId(item.getItemId()));
    }

    @Test
    public void countsManuallyAuthoredItemsWithoutCountingRandomSections() {
        setAuthorType(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE);
        assertEquals(Integer.valueOf(1), publishedAssessmentFacadeQueries.getPublishedItemCountForNonRandomSections(assessment.getPublishedAssessmentId()));
    }

    @Test
    public void recognizesFixedRandomDrawSection() {
        setAuthorType(SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL);
        assertTrue(publishedAssessmentFacadeQueries.isFixedRandomDrawPart(assessment.getPublishedAssessmentId(), section.getSectionId()));
    }

    @Test
    public void recognizesRandomDrawSection() {
        setAuthorType(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL);
        assertTrue(publishedAssessmentFacadeQueries.isRandomDrawPart(assessment.getPublishedAssessmentId(), section.getSectionId()));
    }

    @Test
    public void filtersAuthorizationRecordsByMappedAgentId() {
        AuthorizationData authorization = authorizeDraft("owner", "EDIT_ASSESSMENT");
        assertEquals(authorization.getSurrogateKey(), authzQueriesFacade.getAssessments("owner", "EDIT_ASSESSMENT").get(0).getSurrogateKey());
        assertTrue(authzQueriesFacade.getAssessments("other-site", "EDIT_ASSESSMENT").isEmpty());
    }

    @Test
    public void joinsDraftAssessmentsToTheirAuthorizationRecords() {
        AuthorizationData authorization = authorizeDraft("owner", "EDIT_ASSESSMENT");
        List<AssessmentBaseData> results = authzQueriesFacade.getAssessmentsByAgentAndFunction("owner", "EDIT_ASSESSMENT");
        assertEquals(1, results.size());
        assertEquals(authorization.getQualifierId(), results.get(0).getAssessmentBaseId().toString());
        assertTrue(authzQueriesFacade.getAssessmentsByAgentAndFunction("other-site", "EDIT_ASSESSMENT").isEmpty());
    }

    @Test
    public void listsActivePublishedAssessmentsOnlyForTheirOwningSite() {
        Session session = sessionFactory.getCurrentSession();
        assessment.setLastModifiedBy("anonymous_test");
        PublishedAccessControl access = new PublishedAccessControl();
        access.setAssessmentBase(assessment);
        assessment.setAssessmentAccessControl(access);
        session.persist(access);
        authorizePublishedAssessment();
        session.flush();
        // Kernel testing mode permits the real Samigo helper to resolve an anonymous author.
        boolean previousTestingMode = ComponentManager.testingMode;
        ComponentManager.testingMode = true;
        try {
            SpringBeanLocator.setApplicationContext(applicationContext);
            List<PublishedAssessmentFacade> results = publishedAssessmentFacadeQueries
                    .getBasicInfoOfAllActivePublishedAssessments("title", "owner-site", true);
            assertEquals(1, results.size());
            assertEquals(assessment.getPublishedAssessmentId(), results.get(0).getPublishedAssessmentId());
            assertTrue(publishedAssessmentFacadeQueries
                    .getBasicInfoOfAllActivePublishedAssessments("title", "other-site", true).isEmpty());
        } finally {
            SpringBeanLocator.setApplicationContext(null);
            ComponentManager.shutdown();
            ComponentManager.testingMode = previousTestingMode;
        }
    }

    @Test
    public void countsInProgressAttemptsForTheirOwningSite() {
        seedAttempt(false, AssessmentGradingData.IN_PROGRESS);
        assertEquals(Long.valueOf(1), assessmentGradingFacadeQueries.getInProgressCounts("owner-site")
                .get(assessment.getPublishedAssessmentId()));
        assertTrue(assessmentGradingFacadeQueries.getInProgressCounts("other-site").isEmpty());
    }

    @Test
    public void countsSubmittedStudentsForTheirOwningSite() {
        seedAttempt(true, AssessmentGradingData.SUBMITTED);
        assertEquals(Long.valueOf(1), assessmentGradingFacadeQueries.getSubmittedCounts("owner-site")
                .get(assessment.getPublishedAssessmentId()));
        assertTrue(assessmentGradingFacadeQueries.getSubmittedCounts("other-site").isEmpty());
    }

    @Test
    public void returnsOnlyGradedItemsInTheRequestedSection() {
        assertGradedItemsAreFiltered(1);
    }

    @Test
    public void filtersGradedItemsWhenIdentifiersRequireMultipleInClauses() {
        assertGradedItemsAreFiltered(1001);
    }

    private void assertGradedItemsAreFiltered(int count) {
        Session session = sessionFactory.getCurrentSession();
        seedAttempt(true, AssessmentGradingData.SUBMITTED);
        AssessmentGradingData attempt = session.createQuery("from AssessmentGradingData", AssessmentGradingData.class).getSingleResult();
        Set<Long> expectedIds = new HashSet<>();
        for (int index = 0; index < count; index++) {
            PublishedItemData graded = index == 0 ? item : seedItem();
            PublishedItemText text = new PublishedItemText(graded, 1L, "Question", null);
            session.persist(text);
            ItemGradingData response = new ItemGradingData();
            response.setPublishedItemTextId(text.getId());
            response.setAssessmentGradingId(attempt.getAssessmentGradingId());
            response.setPublishedItemId(graded.getItemId());
            response.setAgentId("student");
            session.persist(response);
            expectedIds.add(graded.getItemId());
        }
        seedItem(); // An ungraded question must never be returned.
        session.flush();

        Set<PublishedItemData> results = assessmentGradingFacadeQueries.getItemSet(assessment.getPublishedAssessmentId(), section.getSectionId());
        Set<Long> actualIds = new HashSet<>();
        for (PublishedItemData result : results) {
            actualIds.add(result.getItemId());
        }
        assertEquals(expectedIds, actualIds);
        assertTrue(assessmentGradingFacadeQueries.getItemSet(assessment.getPublishedAssessmentId(), -1L).isEmpty());
    }

    private PublishedItemData seedItem() {
        PublishedItemData extraItem = new PublishedItemData();
        extraItem.setSection(section);
        extraItem.setTypeId(1L);
        extraItem.setStatus(1);
        extraItem.setCreatedBy("test");
        extraItem.setCreatedDate(new Date());
        extraItem.setLastModifiedBy("test");
        extraItem.setLastModifiedDate(new Date());
        sessionFactory.getCurrentSession().persist(extraItem);
        return extraItem;
    }

    private void seedAttempt(boolean forGrade, Integer status) {
        Session session = sessionFactory.getCurrentSession();
        authorizePublishedAssessment();
        AssessmentGradingData attempt = new AssessmentGradingData();
        attempt.setPublishedAssessmentId(assessment.getPublishedAssessmentId());
        attempt.setAgentId("student");
        attempt.setForGrade(forGrade);
        attempt.setIsLate(false);
        attempt.setStatus(status);
        attempt.setSubmittedDate(new Date());
        session.persist(attempt);
        session.flush();
    }

    private void authorizePublishedAssessment() {
        AuthorizationData authorization = new AuthorizationData();
        authorization.setAgentIdString("owner-site");
        authorization.setFunctionId("OWN_PUBLISHED_ASSESSMENT");
        authorization.setQualifierId(assessment.getPublishedAssessmentId().toString());
        authorization.setLastModifiedBy("test");
        authorization.setLastModifiedDate(new Date());
        sessionFactory.getCurrentSession().persist(authorization);
    }

    private AuthorizationData authorizeDraft(String agentId, String functionId) {
        Session session = sessionFactory.getCurrentSession();
        AssessmentData draft = new AssessmentData();
        draft.setTitle("Draft assessment");
        draft.setStatus(1);
        draft.setCreatedBy("test");
        draft.setCreatedDate(new Date());
        draft.setLastModifiedBy("test");
        draft.setLastModifiedDate(new Date());
        session.persist(draft);
        AuthorizationData authorization = new AuthorizationData();
        authorization.setAgentIdString(agentId);
        authorization.setFunctionId(functionId);
        authorization.setQualifierId(draft.getAssessmentBaseId().toString());
        authorization.setLastModifiedBy("test");
        authorization.setLastModifiedDate(new Date());
        session.persist(authorization);
        session.flush();
        return authorization;
    }

    private void setAuthorType(Integer authorType) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(new PublishedSectionMetaData(section, SectionDataIfc.AUTHOR_TYPE, authorType.toString()));
        session.flush();
    }
}
