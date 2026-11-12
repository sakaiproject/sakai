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
package org.sakaiproject.tool.assessment.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.authz.AuthzQueriesFacade;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(classes = SamigoImportTestConfiguration.class)
public class AssessmentFacadeQueriesAPITest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired @Qualifier("assessmentImportService") private AssessmentFacadeQueriesAPI service;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private AuthzQueriesFacade authorizations;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private ServerConfigurationService serverConfigurationService;

    @BeforeClass
    public static void initializeLocators() {
        ComponentManager.testingMode = true;
    }

    @Before
    public void setup() {
        ComponentManager.loadComponent(SiteService.class, siteService);
        ComponentManager.loadComponent(UserDirectoryService.class, userDirectoryService);
        ComponentManager.loadComponent(ServerConfigurationService.class, serverConfigurationService);
    }

    @Test
    public void copyingAssessmentsDropsSourceLessonsReferenceAndPreservesGroupSettings() throws Exception {
        String from = "import-source";
        String to = "import-destination";
        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        Group sourceGroup = mock(Group.class);
        Group destinationGroup = mock(Group.class);
        ResourceProperties sourceProperties = new BaseResourcePropertiesEdit();
        sourceProperties.addProperty("lessonbuilder_ref", "/lessonbuilder/source/item/123");
        sourceProperties.addProperty("sections_category", "lecture");
        sourceProperties.addProperty("sections_location", "Room 52946");
        ResourceProperties destinationProperties = new BaseResourcePropertiesEdit();
        when(siteService.getSite(from)).thenReturn(sourceSite);
        when(siteService.getSite(to)).thenReturn(destinationSite);
        when(sourceSite.getGroups()).thenReturn(List.of(sourceGroup));
        when(sourceSite.getGroup("source-group")).thenReturn(sourceGroup);
        when(sourceGroup.getId()).thenReturn("source-group");
        when(sourceGroup.getTitle()).thenReturn("Imported group");
        when(sourceGroup.getProperties()).thenReturn(sourceProperties);
        when(destinationSite.getGroups()).thenReturn(Collections.emptyList());
        when(destinationSite.addGroup()).thenReturn(destinationGroup);
        when(destinationGroup.getId()).thenReturn("destination-group");
        when(destinationGroup.getProperties()).thenReturn(destinationProperties);

        Date now = Date.from(Instant.now());
        AssessmentData source = new AssessmentData(0L, "Import regression", "", "", null,
            1L, 0, 0, 0, 1, "instructor", now, "instructor", now);
        source.setSectionSet(new HashSet<>());
        source.setAssessmentMetaDataSet(new HashSet<>());
        source.setSecuredIPAddressSet(new HashSet<>());
        source.setAssessmentAttachmentSet(new HashSet<>());
        AssessmentAccessControl access = new AssessmentAccessControl();
        access.setAssessmentBase(source);
        access.setReleaseTo(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);
        source.setAssessmentAccessControl(access);
        sessionFactory.getCurrentSession().persist(source);
        authorizations.createAuthorization(from, "EDIT_ASSESSMENT", source.getAssessmentId().toString());
        authorizations.createAuthorization("source-group", "TAKE_ASSESSMENT", source.getAssessmentId().toString());
        sessionFactory.getCurrentSession().flush();

        service.copyAllAssessments(from, to, List.of(source.getAssessmentId().toString()), new HashMap<>());

        List<AssessmentData> copies = service.getAllActiveAssessmentsByAgent(to);
        assertEquals(1, copies.size());
        assertNull(destinationProperties.getProperty("lessonbuilder_ref"));
        assertEquals("lecture", destinationProperties.getProperty("sections_category"));
        assertEquals("Room 52946", destinationProperties.getProperty("sections_location"));
        assertEquals("/lessonbuilder/source/item/123", sourceProperties.getProperty("lessonbuilder_ref"));
        List<AuthorizationData> copiedAuthorizations = authorizations.getAuthorizationByAgentAndFunction(
            "destination-group", "TAKE_ASSESSMENT");
        assertEquals(1, copiedAuthorizations.size());
        assertEquals(copies.get(0).getAssessmentId().toString(), copiedAuthorizations.get(0).getQualifierId());
        verify(siteService).save(destinationSite);
    }
}
