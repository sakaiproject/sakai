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
package org.sakaiproject.component.section.sakai;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.sakaiproject.modi.SharedApplicationContext;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.SectionManager.ExternalIntegrationConfig;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.BaseResourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SectionsTestConfiguration.class)
public class SectionManagerImportTest {

    @Autowired private SectionManager sectionManager;
    @Autowired private SiteService siteService;
    @Autowired private EntityManager entityManager;
    private EntityTransferrer transferrer;
    private Site source;
    private Site destination;
    private List<Group> sourceGroups;
    private List<Group> destinationGroups;
    private static String originalModi;

    @BeforeClass
    public static void initializeServiceLocator() {
        originalModi = System.getProperty("sakai.modi.enabled");
        System.setProperty("sakai.modi.enabled", "true");
        SharedApplicationContext context = GlobalApplicationContext.getContext();
        context.refresh();
    }

    @AfterClass
    public static void closeServiceLocator() {
        ComponentManager.shutdown();
        GlobalApplicationContext.destroyContext();
        if (originalModi == null) {
            System.clearProperty("sakai.modi.enabled");
        } else {
            System.setProperty("sakai.modi.enabled", originalModi);
        }
    }

    @Before
    public void setUp() throws Exception {
        reset(siteService);
        ((SectionManagerImpl) sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_DEFAULT.name());
        transferrer = (EntityTransferrer) sectionManager;
        sourceGroups = new ArrayList<>();
        destinationGroups = new ArrayList<>();
        source = site("source", sourceGroups);
        destination = site("destination", destinationGroups);
        when(siteService.getSite("source")).thenReturn(source);
        when(siteService.getSite("destination")).thenReturn(destination);
        when(siteService.allowAccessSite("source")).thenReturn(true);
        when(siteService.allowUpdateSite("destination")).thenReturn(true);
        when(destination.addGroup()).thenAnswer(invocation -> {
            Group group = group("destination", UUID.randomUUID().toString(), null, null);
            doAnswer(call -> { when(group.getTitle()).thenReturn(call.getArgument(0)); return null; })
                .when(group).setTitle(anyString());
            doAnswer(call -> { when(group.getDescription()).thenReturn(call.getArgument(0)); return null; })
                .when(group).setDescription(anyString());
            destinationGroups.add(group);
            return group;
        });
    }

    @Test
    public void registersSectionsForSiteImport() {
        assertArrayEquals(new String[] { "sakai.sections" }, transferrer.myToolIds());
        verify(entityManager).registerEntityProducer((SectionManagerImpl) sectionManager, "/sections");
    }

    @Test
    public void copiesManualStructureAndPreservesSerializedMeetings() throws Exception {
        Group original = group("source", "lab", "Lab 1", "lab");
        original.getProperties().addProperty(CourseSectionImpl.MAX_ENROLLMENTS, "25");
        original.getProperties().addProperty(CourseSectionImpl.LOCATION, "Room 1,Room 2");
        original.getProperties().addProperty(CourseSectionImpl.START_TIME, "01/01/1970 09:00 EST,01/01/1970 10:00 EST");
        original.getProperties().addProperty(CourseSectionImpl.END_TIME, "01/01/1970 10:00 EST,01/01/1970 11:00 EST");
        original.getProperties().addProperty(CourseSectionImpl.MONDAY, "true,false");
        original.getProperties().addProperty(CourseSectionImpl.TUESDAY, "false,true");
        original.getProperties().addProperty(CourseSectionImpl.EID, "old-enterprise-id");
        original.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, "old-joinable-set");
        sourceGroups.add(original);

        Map<String, String> references = transferrer.transferCopyEntities("source", "destination", null, null);

        assertEquals(1, destinationGroups.size());
        Group copy = destinationGroups.get(0);
        assertEquals("Lab 1", copy.getTitle());
        assertEquals("Description for Lab 1", copy.getDescription());
        for (String property : List.of(CourseSectionImpl.CATEGORY, CourseSectionImpl.MAX_ENROLLMENTS,
                CourseSectionImpl.LOCATION, CourseSectionImpl.START_TIME, CourseSectionImpl.END_TIME,
                CourseSectionImpl.MONDAY, CourseSectionImpl.TUESDAY)) {
            assertEquals(original.getProperties().getProperty(property), copy.getProperties().getProperty(property));
        }
        assertNull(copy.getProperties().getProperty(CourseSectionImpl.EID));
        assertNull(copy.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET));
        assertNull(copy.getProviderGroupId());
        verify(copy, never()).insertMember(anyString(), anyString(), anyBoolean(), anyBoolean());
        assertEquals(Map.of(original.getReference(), copy.getReference()), references);
        verify(siteService).save(destination);
        assertEquals("false", destination.getProperties().getProperty(CourseImpl.EXTERNALLY_MAINTAINED));
    }

    @Test
    public void ignoresOrdinaryGroupsAndProvidedSections() throws Exception {
        sourceGroups.add(group("source", "ordinary", "Group", null));
        Group provided = group("source", "provided", "SIS Lab", "lab");
        when(provided.getProviderGroupId()).thenReturn("sis-lab");
        sourceGroups.add(provided);
        assertFalse(((SectionManagerImpl) sectionManager).hasContent("source"));
        assertTrue(copy(false).isEmpty());
        verify(siteService, never()).save(any());
    }

    @Test
    public void exposesManualSectionsAsImportableContent() {
        sourceGroups.add(group("source", "lab", "Lab", "lab"));
        assertTrue(((SectionManagerImpl) sectionManager).hasContent("source"));
        source.getProperties().addProperty(CourseImpl.EXTERNALLY_MAINTAINED, "true");
        assertFalse(((SectionManagerImpl) sectionManager).hasContent("source"));
    }

    @Test
    public void skipsExternallyManagedSourceAndDestination() throws Exception {
        sourceGroups.add(group("source", "lab", "Lab", "lab"));
        source.getProperties().addProperty(CourseImpl.EXTERNALLY_MAINTAINED, "true");
        assertTrue(copy(false).isEmpty());
        source.getProperties().addProperty(CourseImpl.EXTERNALLY_MAINTAINED, "false");
        destination.getProperties().addProperty(CourseImpl.EXTERNALLY_MAINTAINED, "true");
        assertTrue(copy(true).isEmpty());
        verify(destination, never()).addGroup();
        verify(siteService, never()).save(any());
    }

    @Test
    public void respectsMandatoryAndDefaultManagementConfiguration() {
        sourceGroups.add(group("source", "lab", "Lab", "lab"));
        ((SectionManagerImpl) sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_MANDATORY.name());
        assertTrue(copy(false).isEmpty());
        ((SectionManagerImpl) sectionManager).setConfig(ExternalIntegrationConfig.AUTOMATIC_DEFAULT.name());
        destination.getProperties().removeProperty(CourseImpl.EXTERNALLY_MAINTAINED);
        assertTrue(copy(false).isEmpty());
        ((SectionManagerImpl) sectionManager).setConfig(ExternalIntegrationConfig.MANUAL_DEFAULT.name());
        assertEquals(1, copy(false).size());
    }

    @Test
    public void repeatedAndReplaceImportsPreserveExistingSectionsAndMemberships() throws Exception {
        Group original = group("source", "lab", "Lab", "lab");
        Group existing = group("destination", "existing", "Lab", "lab");
        existing.getProperties().addProperty(CourseSectionImpl.MAX_ENROLLMENTS, "10");
        when(existing.getRealmLock()).thenReturn(RealmLockMode.ALL);
        sourceGroups.add(original);
        destinationGroups.add(existing);
        assertEquals(Map.of(original.getReference(), existing.getReference()), copy(false));
        assertEquals(Map.of(original.getReference(), existing.getReference()), copy(true));
        assertEquals("10", existing.getProperties().getProperty(CourseSectionImpl.MAX_ENROLLMENTS));
        verify(destination, never()).addGroup();
        verify(destination, never()).removeGroup(any());
        verify(siteService, never()).save(any());
    }

    @Test
    public void leavesConflictingOrdinaryAndProvidedGroupsUnchanged() throws Exception {
        sourceGroups.add(group("source", "lab", "Lab", "lab"));
        Group existing = group("destination", "existing", "Lab", null);
        destinationGroups.add(existing);
        assertTrue(copy(false).isEmpty());
        existing.getProperties().addProperty(CourseSectionImpl.CATEGORY, "lecture");
        assertTrue(copy(false).isEmpty());
        existing.getProperties().addProperty(CourseSectionImpl.CATEGORY, "lab");
        when(existing.getProviderGroupId()).thenReturn("sis-lab");
        assertTrue(copy(false).isEmpty());
        verify(siteService, never()).save(any());
    }

    @Test
    public void filtersSelectedSectionsAndDoesNotDuplicateRepeatedCopies() {
        Group first = group("source", "first", "Lab 1", "lab");
        Group second = group("source", "second", "Lab 2", "lab");
        sourceGroups.addAll(List.of(first, second));
        Map<String, String> copied = transferrer.transferCopyEntities("source", "destination", List.of(second.getId()), null);
        assertEquals(1, destinationGroups.size());
        assertTrue(copied.containsKey(second.getReference()));
        assertFalse(copied.containsKey(first.getReference()));
        assertEquals(copied, transferrer.transferCopyEntities("source", "destination", List.of(second.getReference()), null));
        assertEquals(1, destinationGroups.size());
    }

    @Test
    public void requiresSourceAccessAndDestinationUpdatePermission() throws Exception {
        when(siteService.allowAccessSite("source")).thenReturn(false);
        assertThrows(SecurityException.class, () -> copy(false));
        when(siteService.allowAccessSite("source")).thenReturn(true);
        when(siteService.allowUpdateSite("destination")).thenReturn(false);
        assertThrows(SecurityException.class, () -> copy(false));
        verify(siteService, never()).save(any());
    }

    @Test
    public void reportsSaveFailuresRatherThanReturningReferences() throws Exception {
        sourceGroups.add(group("source", "lab", "Lab", "lab"));
        doThrow(new PermissionException("user", "site.upd", "/site/destination")).when(siteService).save(destination);
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> copy(false));
        assertTrue(error.getCause() instanceof PermissionException);
    }

    private Map<String, String> copy(boolean cleanup) {
        return transferrer.transferCopyEntities("source", "destination", Collections.emptyList(), Collections.emptyList(), cleanup);
    }

    private Site site(String id, List<Group> groups) {
        Site site = mock(Site.class);
        ResourceProperties properties = new BaseResourceProperties();
        properties.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, "false");
        when(site.getId()).thenReturn(id);
        when(site.getType()).thenReturn("course");
        when(site.getProperties()).thenReturn(properties);
        when(site.getGroups()).thenReturn(groups);
        return site;
    }

    private Group group(String siteId, String id, String title, String category) {
        Group group = mock(Group.class);
        ResourceProperties properties = new BaseResourceProperties();
        if (category != null) {
            properties.addProperty(CourseSectionImpl.CATEGORY, category);
        }
        when(group.getId()).thenReturn(id);
        when(group.getReference()).thenReturn("/site/" + siteId + "/group/" + id);
        when(group.getTitle()).thenReturn(title);
        when(group.getDescription()).thenReturn("Description for " + title);
        when(group.getProperties()).thenReturn(properties);
        return group;
    }
}
