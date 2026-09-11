/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tags.impl.job.TagsSyncJob;
import org.sakaiproject.tags.impl.job.TagsExportedXMLSyncJob;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TagServiceTestConfiguration.class)
public class TagServiceTest {
    @Rule public TemporaryFolder files = new TemporaryFolder();
    @Autowired private ServerConfigurationService configuration;
    @Autowired private TagsSyncJob genericImport;
    @Autowired private TagsExportedXMLSyncJob fullImport;
    @Autowired private TagService service;
    @Autowired private SessionManager sessionManager;
    @Autowired private EventTrackingService events;
    @Autowired private DataSource dataSource;
    @Autowired private PlatformTransactionManager transactionManager;
    private JdbcTemplate jdbc;

    @Before
    public void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.update("DELETE FROM tagservice_tagassociation");
        jdbc.update("DELETE FROM tagservice_tag");
        jdbc.update("DELETE FROM tagservice_collection");
        reset(events);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("creator");
        when(events.newEvent(anyString(), anyString(), eq(true))).thenAnswer(invocation -> {
            Event event = mock(Event.class);
            when(event.getEvent()).thenReturn(invocation.getArgument(0));
            when(event.getResource()).thenReturn(invocation.getArgument(1));
            return event;
        });
    }

    private TagCollection collection(String name) {
        TagCollection collection = TagCollection.builder()
            .name(name)
            .description("Description")
            .createdBy("ignored")
            .creationDate(1L)
            .externalSourceName(name + "-source")
            .externalSourceDescription("External description")
            .lastModifiedBy("ignored")
            .lastModificationDate(1L)
            .externalUpdate(false)
            .externalCreation(false)
            .lastSynchronizationDate(3L)
            .lastUpdateDateInExternalSystem(4L)
            .build();
        String id = service.createTagCollection(collection);
        return service.getTagCollection(id).get();
    }

    private Tag tag(TagCollection collection, String label) {
        Tag proposed = Tag.builder()
            .tagId("ignored")
            .tagCollectionId(collection.getTagCollectionId())
            .tagLabel(label)
            .description("Description")
            .createdBy("ignored")
            .creationDate(1L)
            .lastModifiedBy("ignored")
            .lastModificationDate(1L)
            .externalId(label + "-external")
            .alternativeLabels("Alternative")
            .externalCreation(true)
            .externalCreationDate(5L)
            .externalUpdate(false)
            .lastUpdateDateInExternalSystem(6L)
            .parentId("parent")
            .externalHierarchyCode("hierarchy")
            .externalType("type")
            .data("data")
            .build();
        return service.getTag(service.createTag(proposed)).get();
    }

    @Test
    public void createsAndReadsTagsWithExistingColumnsAndAuditMetadata() {
        TagCollection collection = collection("Collection");
        Tag tag = tag(collection, "数学");
        assertEquals(collection.getTagCollectionId(), tag.getTagCollectionId());
        assertEquals("Collection", tag.getCollectionName());
        assertEquals("creator", tag.getCreatedBy());
        assertTrue(tag.getCreationDate() > 1L);
        assertEquals("Alternative", tag.getAlternativeLabels());
        assertEquals("hierarchy", tag.getExternalHierarchyCode());
        assertEquals(Long.valueOf(5L), tag.getExternalCreationDate());
        assertEquals("data", tag.getData());
        assertNotEquals("ignored", tag.getTagId());
        assertEquals("creator", collection.getCreatedBy());
        verify(events).post(argThat(event -> event.getEvent().equals("tags.new.tag")));
    }

    @Test
    public void preservesAssignedCollectionIdsAndRejectsDuplicates() {
        String id = UUID.randomUUID().toString();
        TagCollection collection = TagCollection.builder().tagCollectionId(id).name("Assigned").build();
        assertEquals(id, service.createTagCollection(collection));
        collection.setName("Must not replace");
        assertThrows(RuntimeException.class, () -> service.createTagCollection(collection));
        assertEquals("Assigned", service.getTagCollection(id).get().getName());
    }

    @Test
    public void readsLegacyNullMetadataWithoutCoercion() {
        TagCollection collection = collection("Null metadata");
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO tagservice_tag(tagid,tagcollectionid,taglabel) VALUES(?,?,?)", id, collection.getTagCollectionId(), "Legacy");
        Tag tag = service.getTag(id).get();
        assertNull(tag.getCreationDate());
        assertNull(tag.getExternalCreationDate());
        assertNull(tag.getExternalCreation());
        assertNull(tag.getExternalUpdate());
    }

    private Tag legacyTagWithNullMetadata() {
        String collectionId = UUID.randomUUID().toString();
        String tagId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO tagservice_collection(tagcollectionid,name) VALUES(?,?)", collectionId, "Legacy collection");
        jdbc.update("INSERT INTO tagservice_tag(tagid,tagcollectionid,taglabel) VALUES(?,?,?)", tagId, collectionId, "Legacy tag");
        return service.getTag(tagId).get();
    }

    private void assertUntouchedNullMetadata(Tag tag) {
        jdbc.queryForMap("SELECT creationdate, externalcreationdate, externalcreation, externalupdate "
            + "FROM tagservice_tag WHERE tagid=?", tag.getTagId()).forEach((column, value) -> assertNull(column, value));
        jdbc.queryForMap("SELECT creationdate, externalcreation, externalupdate "
            + "FROM tagservice_collection WHERE tagcollectionid=?", tag.getTagCollectionId())
            .forEach((column, value) -> assertNull(column, value));
    }

    @Test
    public void unchangedUpdatesPreserveDatabaseNulls() {
        Tag tag = legacyTagWithNullMetadata();
        service.updateTag(tag);
        service.updateTagCollection(service.getTagCollection(tag.getTagCollectionId()).get());
        assertUntouchedNullMetadata(tag);
        assertNull(jdbc.queryForObject("SELECT lastupdatedateinexternalsystem FROM tagservice_tag WHERE tagid=?", Long.class, tag.getTagId()));
        jdbc.queryForMap("SELECT lastsynchronizationdate, lastupdatedateinexternalsystem "
            + "FROM tagservice_collection WHERE tagcollectionid=?", tag.getTagCollectionId())
            .forEach((column, value) -> assertNull(column, value));
        assertNotNull(jdbc.queryForObject("SELECT lastmodificationdate FROM tagservice_tag WHERE tagid=?", Long.class, tag.getTagId()));
        verify(events, never()).post(any());
    }

    @Test
    public void contentUpdatesPreserveUnrelatedDatabaseNulls() {
        Tag tag = legacyTagWithNullMetadata();
        service.updateTag(tag.toBuilder().tagLabel("Edited").build());
        service.updateTagCollection(service.getTagCollection(tag.getTagCollectionId()).get()
            .toBuilder().name("Edited collection").build());
        assertUntouchedNullMetadata(tag);
        assertNull(jdbc.queryForObject("SELECT lastupdatedateinexternalsystem FROM tagservice_tag WHERE tagid=?", Long.class, tag.getTagId()));
        jdbc.queryForMap("SELECT lastsynchronizationdate, lastupdatedateinexternalsystem "
            + "FROM tagservice_collection WHERE tagcollectionid=?", tag.getTagCollectionId())
            .forEach((column, value) -> assertNull(column, value));
        assertEquals("Edited", service.getTag(tag.getTagId()).get().getTagLabel());
    }

    @Test
    public void synchronizationUpdatesPreserveUnrelatedDatabaseNulls() {
        Tag tag = legacyTagWithNullMetadata();
        service.updateTag(tag.toBuilder().lastUpdateDateInExternalSystem(100L).build());
        service.updateTagCollection(service.getTagCollection(tag.getTagCollectionId()).get()
            .toBuilder().lastSynchronizationDate(200L).build());
        assertUntouchedNullMetadata(tag);
        assertEquals(Long.valueOf(100L), jdbc.queryForObject("SELECT lastupdatedateinexternalsystem FROM tagservice_tag WHERE tagid=?", Long.class, tag.getTagId()));
        assertEquals(Long.valueOf(200L), jdbc.queryForObject("SELECT lastsynchronizationdate FROM tagservice_collection WHERE tagcollectionid=?", Long.class, tag.getTagCollectionId()));
        assertNull(jdbc.queryForObject("SELECT lastupdatedateinexternalsystem FROM tagservice_collection WHERE tagcollectionid=?", Long.class, tag.getTagCollectionId()));
        verify(events, never()).post(any());
    }

    @Test
    public void explicitlySubmittedZeroAndFalseArePersisted() {
        Tag tag = legacyTagWithNullMetadata();
        service.updateTag(tag.toBuilder().externalCreationDate(0L).externalCreation(false).build());
        service.updateTagCollection(service.getTagCollection(tag.getTagCollectionId()).get()
            .toBuilder().externalCreation(false).build());
        assertEquals(Long.valueOf(0L), jdbc.queryForObject("SELECT externalcreationdate FROM tagservice_tag WHERE tagid=?", Long.class, tag.getTagId()));
        assertEquals(Boolean.FALSE, jdbc.queryForObject("SELECT externalcreation FROM tagservice_tag WHERE tagid=?", Boolean.class, tag.getTagId()));
        assertEquals(Boolean.FALSE, jdbc.queryForObject("SELECT externalcreation FROM tagservice_collection WHERE tagcollectionid=?", Boolean.class, tag.getTagCollectionId()));
        assertNull(service.getTag(tag.getTagId()).get().getExternalUpdate());
        verify(events, never()).post(any());
    }

    @Test
    public void updatesDetachedTagAndPreservesCreationMetadata() {
        Tag original = tag(collection("Update"), "Before");
        Tag edited = service.getTag(original.getTagId()).get();
        edited.setTagLabel("After");
        edited.setCreatedBy("forged");
        edited.setCreationDate(42L);
        assertEquals("Before", service.getTag(original.getTagId()).get().getTagLabel());
        clearInvocations(events);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("editor");
        service.updateTag(edited);
        Tag saved = service.getTag(original.getTagId()).get();
        assertEquals("After", saved.getTagLabel());
        assertEquals("creator", saved.getCreatedBy());
        assertEquals(original.getCreationDate(), saved.getCreationDate());
        assertEquals("editor", saved.getLastModifiedBy());
        verify(events).post(argThat(event -> event.getEvent().equals("tags.update.tag")));
    }

    @Test
    public void unchangedTagStillRefreshesImportTimestampWithoutEvent() {
        Tag original = tag(collection("Unchanged"), "Same");
        jdbc.update("UPDATE tagservice_tag SET lastmodificationdate=1 WHERE tagid=?", original.getTagId());
        clearInvocations(events);
        service.updateTag(service.getTag(original.getTagId()).get());
        assertTrue(service.getTag(original.getTagId()).get().getLastModificationDate() > 1L);
        verify(events, never()).post(any());
    }

    @Test
    public void synchronizationOnlyUpdatesDoNotGenerateEvents() {
        TagCollection collection = collection("Sync");
        Tag tag = tag(collection, "Tag");
        clearInvocations(events);
        tag.setLastUpdateDateInExternalSystem(100L);
        service.updateTag(tag);
        collection.setLastSynchronizationDate(100L);
        service.updateTagCollection(collection);
        assertEquals(Long.valueOf(100L), service.getTag(tag.getTagId()).get().getLastUpdateDateInExternalSystem());
        assertEquals(Long.valueOf(100L), service.getTagCollection(collection.getTagCollectionId()).get().getLastSynchronizationDate());
        verify(events, never()).post(any());
    }

    @Test
    public void updatesCollectionContentAndPreservesCreationMetadata() {
        TagCollection original = collection("Before");
        Tag tag = tag(original, "Tag");
        original.setName("After");
        original.setCreatedBy("forged");
        original.setCreationDate(1L);
        clearInvocations(events);
        service.updateTagCollection(original);
        TagCollection saved = service.getTagCollection(original.getTagCollectionId()).get();
        assertEquals("creator", saved.getCreatedBy());
        assertTrue(saved.getCreationDate() > 1L);
        assertEquals("After", service.getTag(tag.getTagId()).get().getCollectionName());
        verify(events).post(argThat(event -> event.getEvent().equals("tags.update.collection")));
    }

    @Test
    public void unchangedCollectionRefreshesAuditWithoutEvent() {
        TagCollection original = collection("Unchanged collection");
        jdbc.update("UPDATE tagservice_collection SET lastmodificationdate=1 WHERE tagcollectionid=?", original.getTagCollectionId());
        when(sessionManager.getCurrentSessionUserId()).thenReturn("editor");
        clearInvocations(events);
        service.updateTagCollection(original);
        assertTrue(service.getTagCollection(original.getTagCollectionId()).get().getLastModificationDate() > 1L);
        assertEquals("editor", service.getTagCollection(original.getTagCollectionId()).get().getLastModifiedBy());
        verify(events, never()).post(any());
    }

    @Test
    public void movingTagBetweenCollectionsIsPersistedAndGeneratesContentEvent() {
        Tag tag = tag(collection("Source"), "Moved tag");
        TagCollection target = collection("Target");
        clearInvocations(events);
        service.updateTag(tag.toBuilder().tagCollectionId(target.getTagCollectionId()).build());
        Tag saved = service.getTag(tag.getTagId()).get();
        assertEquals(target.getTagCollectionId(), saved.getTagCollectionId());
        assertEquals("Target", saved.getCollectionName());
        verify(events).post(argThat(event -> event.getEvent().equals("tags.update.tag")));
    }

    @Test
    public void findsCollectionsByNameAndExternalSource() {
        TagCollection first = collection("A");
        collection("B");
        assertEquals(first, service.getTagCollectionForName("A").get());
        assertEquals(first, service.getTagCollectionForExternalSourceName("A-source").get());
        assertEquals(2, service.getTotalTagCollections());
        assertEquals("B", service.getTagCollectionsPaginated(2, 1).get(0).getName());
    }

    @Test
    public void queriesTagsWithOrderingPaginationAndCaseInsensitivePrefixes() {
        TagCollection collection = collection("Queries");
        tag(collection, "Beta");
        Tag alpha = tag(collection, "Alpha");
        tag(collection, "Alpine");
        assertEquals(3, service.getTagsInCollection(collection.getTagCollectionId()).size());
        assertEquals("Alpha", service.getTags().get(0).getTagLabel());
        assertEquals("Alpine", service.getTagsPaginatedInCollection(2, 1, collection.getTagCollectionId()).get(0).getTagLabel());
        assertEquals(2, service.getTotalTagsByPrefixInLabel("aL"));
        assertEquals(2, service.getTagsByPrefixInLabel("aL").size());
        assertEquals("Alpine", service.getTagsPaginatedByPrefixInLabel(2, 1, "al").get(0).getTagLabel());
        assertEquals(1, service.getTagsByPartialLabel("lph").size());
        assertEquals(alpha, service.getTagsByExactLabel("Alpha", collection.getTagCollectionId()).get(0));
        assertEquals(alpha, service.getTagForExternalIdAndCollection("Alpha-external", collection.getTagCollectionId()).get());
        assertEquals(3, service.getTotalTagsInCollection(collection.getTagCollectionId()));
    }

    @Test
    public void rejectsInvalidPagination() {
        assertThrows(IllegalArgumentException.class, () -> service.getTagCollectionsPaginated(0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.getTagsPaginatedByPrefixInLabel(1, 0, "a"));
    }

    @Test
    public void missingRecordsReturnEmptyAndDeletionIsQuiet() {
        String id = UUID.randomUUID().toString();
        assertFalse(service.getTag(id).isPresent());
        assertFalse(service.getTagCollection(id).isPresent());
        service.deleteTag(id);
        service.deleteTagCollection(id);
        verify(events, never()).post(any());
    }

    @Test
    public void updatesCannotInsertMissingRecords() {
        Tag tag = tag(collection("Missing"), "Delete me");
        service.deleteTag(tag.getTagId());
        clearInvocations(events);
        assertThrows(RuntimeException.class, () -> service.updateTag(tag));
        assertFalse(service.getTag(tag.getTagId()).isPresent());
        verify(events, never()).post(any());
    }

    @Test
    public void deletesTagAndAssociationsTogether() {
        TagCollection collection = collection("Delete tag");
        Tag tag = tag(collection, "Tag");
        service.associateExistingTag("item", tag.getTagId());
        clearInvocations(events);
        service.deleteTag(tag.getTagId());
        assertFalse(service.getTag(tag.getTagId()).isPresent());
        assertEquals(Integer.valueOf(0), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tagassociation", Integer.class));
        verify(events).post(argThat(event -> event.getEvent().equals("tags.delete.tag")));
    }

    @Test
    public void deletesCollectionTagsAndAssociationsTogether() {
        TagCollection collection = collection("Delete collection");
        Tag tag = tag(collection, "Tag");
        service.associateExistingTag("item", tag.getTagId());
        clearInvocations(events);
        service.deleteTagCollection(collection.getTagCollectionId());
        assertFalse(service.getTagCollection(collection.getTagCollectionId()).isPresent());
        assertFalse(service.getTag(tag.getTagId()).isPresent());
        assertEquals(Integer.valueOf(0), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tagassociation", Integer.class));
        verify(events).post(argThat(event -> event.getEvent().equals("tags.delete.collection")));
        verify(events, times(1)).post(any());
    }

    @Test
    public void expiresOnlyOlderTagsInSelectedCollection() {
        TagCollection collection = collection("Expire");
        Tag old = tag(collection, "Old");
        Tag keep = tag(collection, "Keep");
        Tag other = tag(collection("Other"), "Other");
        jdbc.update("UPDATE tagservice_tag SET lastmodificationdate=10 WHERE tagid=?", old.getTagId());
        jdbc.update("UPDATE tagservice_tag SET lastmodificationdate=20 WHERE tagid=?", keep.getTagId());
        assertEquals(Collections.singletonList(old.getTagId()), service.deleteTagsOlderThanDateFromCollection(collection.getTagCollectionId(), 20L));
        assertTrue(service.getTag(keep.getTagId()).isPresent());
        assertTrue(service.getTag(other.getTagId()).isPresent());
    }

    @Test
    public void deletesByExternalIdWithinItsCollection() {
        TagCollection collection = collection("External delete");
        Tag tag = tag(collection, "Shared");
        Tag other = tag(collection("Other external"), "Shared");
        assertEquals(Collections.singletonList(tag.getTagId()), service.deleteTagFromExternalCollection(tag.getExternalId(), collection.getTagCollectionId()));
        assertTrue(service.getTag(other.getTagId()).isPresent());
    }

    @Test
    public void duplicatesTagsAndTheirAssociations() {
        Tag source = tag(collection("Source"), "Copied");
        TagCollection target = collection("Target");
        List<Tag> copies = service.duplicateTags(target.getTagCollectionId(), true, Collections.singletonList(source.getTagId()), "item");
        assertEquals(1, copies.size());
        assertNotEquals(source.getTagId(), copies.get(0).getTagId());
        assertEquals("Copied", service.getAssociatedTagsForItem(target.getTagCollectionId(), "item").get(0).getTagLabel());
    }

    @Test
    public void associatedTagsAreScopedAndShareThePersistenceContext() {
        TagCollection collection = collection("Selected");
        Tag selected = tag(collection, "Selected tag");
        Tag other = tag(collection("Other"), "Other tag");
        Tag otherItem = tag(collection, "Other item");
        service.associateExistingTag("item", selected.getTagId());
        service.associateExistingTag("item", other.getTagId());
        service.associateExistingTag("other-item", otherItem.getTagId());
        new TransactionTemplate(transactionManager).execute(status -> {
            List<Tag> tags = service.getAssociatedTagsForItem(collection.getTagCollectionId(), "item");
            assertEquals(1, tags.size());
            assertEquals(selected.getTagId(), tags.get(0).getTagId());
            assertEquals("Selected", tags.get(0).getCollectionName());
            assertSame(service.getTag(selected.getTagId()).get(), tags.get(0));
            return null;
        });
        assertEquals("Selected tag", service.getTag(selected.getTagId()).get().getTagLabel());
    }

    @Test
    public void updatesSelectedAssociationsAndCreatesNewLabels() {
        TagCollection collection = collection("Associations");
        Tag tag = tag(collection, "Existing");
        service.updateTagAssociations(collection.getTagCollectionId(), "item", Arrays.asList(tag.getTagId(), "New label"), true);
        assertEquals(2, service.getAssociatedTagsForItem(collection.getTagCollectionId(), "item").size());
        service.updateTagAssociations(collection.getTagCollectionId(), "item", Collections.singletonList(tag.getTagId()), true);
        assertEquals(Collections.singletonList(tag.getTagId()), service.getTagAssociationIds(collection.getTagCollectionId(), "item"));
    }

    @Test
    public void explicitAssociationRejectsMissingIdsWithoutCreatingLabels() {
        assertThrows(org.sakaiproject.tags.api.TagServiceException.class,
            () -> service.associateExistingTag("item", "This is not a tag ID"));
        assertTrue(service.getTags().isEmpty());
        verify(events, never()).post(any());
    }

    @Test
    public void literalLabelsAreNeverInterpretedAsIds() {
        TagCollection collection = collection("Explicit inputs");
        Tag existing = tag(collection, "Existing");
        service.associateExistingTag("existing-item", existing.getTagId());
        String created = service.createAndAssociateTag(collection.getTagCollectionId(), "label-item", existing.getTagId(), true);
        assertNotEquals(existing.getTagId(), created);
        assertEquals(existing.getTagId(), service.getTag(created).get().getTagLabel());
        assertEquals(Collections.singletonList(created), service.getTagAssociationIds(collection.getTagCollectionId(), "label-item"));
    }

    @Test
    public void externalSourceImportCreatesAndUpdatesWithoutReplacingCreationMetadata() throws Exception {
        when(configuration.getSakaiHomePath()).thenReturn(files.getRoot().getAbsolutePath() + "/");
        when(configuration.getString("tags.tagcollectionsfile", "tags/tagcollections.xml")).thenReturn("collections.xml");
        when(configuration.getString("tags.tagsfile", "tags/tags.xml")).thenReturn("tags.xml");
        Path collectionsFile = files.getRoot().toPath().resolve("collections.xml");
        Path tagsFile = files.getRoot().toPath().resolve("tags.xml");
        String collectionsXml = "<TagCollections><TagCollection><Name>Imported</Name><Description>Original</Description>"
            + "<ExternalSourceName>Source</ExternalSourceName><ExternalSourceDescription>Source description</ExternalSourceDescription>"
            + "<DateRevised><Year>2020</Year><Month>01</Month><Day>01</Day></DateRevised></TagCollection></TagCollections>";
        String tagsXml = "<Tags><Tag><TagLabel>First</TagLabel><ExternalId>external</ExternalId>"
            + "<ExternalSourceName>Source</ExternalSourceName><Description>Tag description</Description>"
            + "<DateCreated><Year>2020</Year><Month>01</Month><Day>01</Day></DateCreated>"
            + "<DateRevised><Year>2021</Year><Month>01</Month><Day>01</Day></DateRevised></Tag></Tags>";
        Files.writeString(collectionsFile, collectionsXml);
        Files.writeString(tagsFile, tagsXml);
        genericImport.execute(null);
        TagCollection collection = service.getTagCollectionForExternalSourceName("Source").get();
        Tag original = service.getTagForExternalIdAndCollection("external", collection.getTagCollectionId()).get();
        Files.writeString(collectionsFile, collectionsXml.replace("Imported", "Renamed").replace("Original", "Ignored on update"));
        Files.writeString(tagsFile, tagsXml.replace("First", "Updated").replace("2020", "2022").replace("><", ">\n<"));
        genericImport.execute(null);
        Tag updated = service.getTag(original.getTagId()).get();
        assertEquals("Updated", updated.getTagLabel());
        assertEquals(original.getExternalCreationDate(), updated.getExternalCreationDate());
        assertEquals(original.getCreationDate(), updated.getCreationDate());
        assertEquals(1, service.getTagsInCollection(collection.getTagCollectionId()).size());
        TagCollection saved = service.getTagCollection(collection.getTagCollectionId()).get();
        assertEquals("Renamed", saved.getName());
        assertEquals("Original", saved.getDescription());
        assertNotNull(saved.getLastSynchronizationDate());
    }

    @Test
    public void externalSourceImportSkipsUnknownSourceAndContinues() throws Exception {
        TagCollection collection = collection("Valid");
        when(configuration.getSakaiHomePath()).thenReturn(files.getRoot().getAbsolutePath() + "/");
        when(configuration.getString("tags.tagcollectionsfile", "tags/tagcollections.xml")).thenReturn("collections.xml");
        when(configuration.getString("tags.tagsfile", "tags/tags.xml")).thenReturn("tags.xml");
        Files.writeString(files.getRoot().toPath().resolve("collections.xml"), "<TagCollections/>");
        String row = "<Tag><TagLabel>%s</TagLabel><ExternalId>%s</ExternalId>"
            + "<ExternalSourceName>%s</ExternalSourceName>"
            + "<DateCreated><Year>2020</Year><Month>01</Month><Day>01</Day></DateCreated>"
            + "<DateRevised><Year>2021</Year><Month>01</Month><Day>01</Day></DateRevised></Tag>";
        Files.writeString(files.getRoot().toPath().resolve("tags.xml"), "<Tags>"
            + String.format(row, "Invalid", "invalid", "Unknown-source")
            + String.format(row, "Valid", "valid", collection.getExternalSourceName()) + "</Tags>");

        genericImport.execute(null);

        assertFalse(service.getTagCollectionForExternalSourceName("Unknown-source").isPresent());
        assertEquals(1, service.getTags().size());
        assertEquals("Valid", service.getTagForExternalIdAndCollection("valid", collection.getTagCollectionId()).get().getTagLabel());
        assertNotNull(service.getTagCollection(collection.getTagCollectionId()).get().getLastSynchronizationDate());
    }

    @Test
    public void fullImportUpdatesByIdThenFallsBackToExternalIdAndCreatesMissingTags() throws Exception {
        TagCollection collection = collection("Full import");
        Tag byId = tag(collection, "By ID");
        Tag byExternalId = tag(collection, "By external ID");
        Tag expired = tag(collection, "Expired");
        jdbc.update("UPDATE tagservice_tag SET lastmodificationdate=1 WHERE tagid=?", expired.getTagId());
        when(configuration.getSakaiHomePath()).thenReturn(files.getRoot().getAbsolutePath() + "/");
        when(configuration.getString("tags.fullxmltagsfile", "tags/fullxmltags.xml")).thenReturn("full.xml");
        String row = "<Tag><tagId>%s</tagId><externalId>%s</externalId><tagCollectionId>%s</tagCollectionId>"
            + "<tagLabel>%s</tagLabel><externalCreationDate>99</externalCreationDate></Tag>";
        Files.writeString(files.getRoot().toPath().resolve("full.xml"), "<Tags>"
            + String.format(row, byId.getTagId(), byId.getExternalId(), collection.getTagCollectionId(), "Updated by ID")
            + String.format(row, UUID.randomUUID(), byExternalId.getExternalId(), collection.getTagCollectionId(), "Updated by external ID")
            + String.format(row, "", "new-external", collection.getTagCollectionId(), "New tag") + "</Tags>");
        fullImport.execute(null);
        assertEquals("Updated by ID", service.getTag(byId.getTagId()).get().getTagLabel());
        assertEquals(Long.valueOf(99L), service.getTag(byId.getTagId()).get().getExternalCreationDate());
        assertEquals("Updated by external ID", service.getTag(byExternalId.getTagId()).get().getTagLabel());
        assertEquals(byExternalId.getExternalCreationDate(), service.getTag(byExternalId.getTagId()).get().getExternalCreationDate());
        assertEquals("New tag", service.getTagForExternalIdAndCollection("new-external", collection.getTagCollectionId()).get().getTagLabel());
        assertFalse(service.getTag(expired.getTagId()).isPresent());
        assertEquals(3, service.getTagsInCollection(collection.getTagCollectionId()).size());
    }

    @Test
    public void outerRollbackUndoesTagsCollectionsAndAssociationsAndSuppressesEvents() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        String collectionId = UUID.randomUUID().toString();
        assertThrows(IllegalStateException.class, () -> transaction.execute(status -> {
            service.updateTagAssociations(collectionId, "item", Collections.singletonList("New"), true);
            assertEquals(1, service.getAssociatedTagsForItem(collectionId, "item").size());
            throw new IllegalStateException("Roll back the whole operation");
        }));
        assertFalse(service.getTagCollection(collectionId).isPresent());
        assertTrue(service.getTags().isEmpty());
        assertEquals(Integer.valueOf(0), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tagassociation", Integer.class));
        verify(events, never()).post(any());
    }

    @Test
    public void failedAssociationInsertRollsBackEarlierTagCreation() {
        TagCollection collection = collection("Failed association");
        clearInvocations(events);
        assertThrows(RuntimeException.class, () -> service.updateTagAssociations(collection.getTagCollectionId(), "x".repeat(256), Collections.singletonList("New"), true));
        assertTrue(service.getTagsInCollection(collection.getTagCollectionId()).isEmpty());
        verify(events, never()).post(any());
    }

    @Test
    public void duplicateCollectionNameRollsBackWithoutEvent() {
        collection("Unique");
        clearInvocations(events);
        assertThrows(RuntimeException.class, () -> collection("Unique"));
        assertEquals(1, service.getTagCollections().size());
        verify(events, never()).post(any());
    }

    @Test
    public void serviceAppliesSubmittedEditsToManagedEntitiesAndPostsAfterCommit() {
        TagCollection collection = collection("Original collection");
        Tag tag = tag(collection, "Original");
        clearInvocations(events);
        new TransactionTemplate(transactionManager).execute(status -> {
            Tag managed = service.getTag(tag.getTagId()).get();
            TagCollection managedCollection = service.getTagCollection(collection.getTagCollectionId()).get();
            Tag edits = managed.toBuilder().tagLabel("Updated").createdBy("forged").build();
            TagCollection collectionEdits = managedCollection.toBuilder().name("Updated collection").build();
            assertEquals("Original", managed.getTagLabel());
            service.updateTag(edits);
            service.updateTagCollection(collectionEdits);
            assertEquals("Updated", managed.getTagLabel());
            assertEquals("Updated collection", managedCollection.getName());
            assertEquals("creator", managed.getCreatedBy());
            verify(events, never()).post(any());
            return null;
        });
        assertEquals("Updated", service.getTag(tag.getTagId()).get().getTagLabel());
        assertEquals("Updated collection", service.getTagCollection(collection.getTagCollectionId()).get().getName());
        verify(events).post(argThat(event -> event.getEvent().equals("tags.update.tag")));
        verify(events).post(argThat(event -> event.getEvent().equals("tags.update.collection")));
    }

    @Test
    public void managedUpdatesRollBackWithTheEnclosingTransaction() {
        TagCollection collection = collection("Rollback update");
        Tag tag = tag(collection, "Original");
        clearInvocations(events);
        new TransactionTemplate(transactionManager).execute(status -> {
            service.updateTag(service.getTag(tag.getTagId()).get().toBuilder().tagLabel("Rolled back").build());
            service.updateTagCollection(service.getTagCollection(collection.getTagCollectionId()).get()
                .toBuilder().name("Rolled back").build());
            status.setRollbackOnly();
            return null;
        });
        assertEquals("Original", service.getTag(tag.getTagId()).get().getTagLabel());
        assertEquals("Rollback update", service.getTagCollection(collection.getTagCollectionId()).get().getName());
        assertEquals(tag.getLastModificationDate(), service.getTag(tag.getTagId()).get().getLastModificationDate());
        verify(events, never()).post(any());
    }

    @Test
    public void rejectsTagsInMissingCollectionsWithoutEvent() {
        Tag proposed = Tag.builder().tagCollectionId(UUID.randomUUID().toString()).tagLabel("Orphan").build();
        assertThrows(RuntimeException.class, () -> service.createTag(proposed));
        assertTrue(service.getTags().isEmpty());
        verify(events, never()).post(any());
    }

    @Test
    public void preservesLongTextFields() {
        String text = "Imported metadata ".repeat(300);
        TagCollection collection = collection("Long text");
        collection.setDescription(text);
        collection.setExternalSourceDescription(text);
        service.updateTagCollection(collection);
        Tag tag = tag(collection, "Long text");
        tag.setDescription(text);
        tag.setAlternativeLabels(text);
        tag.setExternalHierarchyCode(text);
        tag.setData(text);
        service.updateTag(tag);
        Tag saved = service.getTag(tag.getTagId()).get();
        assertEquals(text, saved.getDescription());
        assertEquals(text, saved.getAlternativeLabels());
        assertEquals(text, saved.getExternalHierarchyCode());
        assertEquals(text, saved.getData());
        TagCollection savedCollection = service.getTagCollection(collection.getTagCollectionId()).get();
        assertEquals(text, savedCollection.getDescription());
        assertEquals(text, savedCollection.getExternalSourceDescription());
    }

    @Test
    public void rollingBackCollectionDeletionRestoresTagsAndAssociations() {
        TagCollection collection = collection("Rollback delete");
        Tag tag = tag(collection, "Keep");
        service.associateExistingTag("item", tag.getTagId());
        clearInvocations(events);
        new TransactionTemplate(transactionManager).execute(status -> {
            service.deleteTagCollection(collection.getTagCollectionId());
            status.setRollbackOnly();
            return null;
        });
        assertTrue(service.getTagCollection(collection.getTagCollectionId()).isPresent());
        assertEquals(tag.getTagId(), service.getAssociatedTagsForItem(collection.getTagCollectionId(), "item").get(0).getTagId());
        verify(events, never()).post(any());
    }
}
