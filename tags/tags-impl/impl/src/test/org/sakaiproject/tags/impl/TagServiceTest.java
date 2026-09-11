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
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;
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
        TagCollection collection = new TagCollection(null, name, "Description", "ignored", 1L,
            name + "-source", "External description", "ignored", 1L, false, false, 3L, 4L);
        String id = service.createTagCollection(collection);
        return service.getTagCollection(id).get();
    }

    private Tag tag(TagCollection collection, String label) {
        Tag proposed = new Tag("ignored", collection.getTagCollectionId(), label, "Description", "ignored", 1L,
            "ignored", 1L, label + "-external", "Alternative", true, 5L, false, 6L,
            "parent", "hierarchy", "type", "data", null);
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
        assertEquals(5L, tag.getExternalCreationDate());
        assertEquals("data", tag.getData());
        assertNotEquals("ignored", tag.getTagId());
        assertEquals("creator", collection.getCreatedBy());
        verify(events).post(argThat(event -> event.getEvent().equals("tags.new.tag")));
    }

    @Test
    public void preservesAssignedCollectionIdsAndRejectsDuplicates() {
        String id = UUID.randomUUID().toString();
        TagCollection collection = new TagCollection(id, "Assigned", null, null, 0L, null, null, null, 0L, false, false, 0L, 0L);
        assertEquals(id, service.createTagCollection(collection));
        collection.setName("Must not replace");
        assertThrows(RuntimeException.class, () -> service.createTagCollection(collection));
        assertEquals("Assigned", service.getTagCollection(id).get().getName());
    }

    @Test
    public void readsLegacyNullMetadataAsZeroAndFalse() {
        TagCollection collection = collection("Null metadata");
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO tagservice_tag(tagid,tagcollectionid,taglabel) VALUES(?,?,?)", id, collection.getTagCollectionId(), "Legacy");
        Tag tag = service.getTag(id).get();
        assertEquals(0L, tag.getCreationDate());
        assertEquals(0L, tag.getExternalCreationDate());
        assertFalse(tag.getExternalCreation());
        assertFalse(tag.getExternalUpdate());
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
        assertEquals(100L, service.getTag(tag.getTagId()).get().getLastUpdateDateInExternalSystem());
        assertEquals(100L, service.getTagCollection(collection.getTagCollectionId()).get().getLastSynchronizationDate());
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
    public void unchangedCollectionDoesNotChangeAuditOrPostEvent() {
        TagCollection original = collection("Unchanged collection");
        clearInvocations(events);
        service.updateTagCollection(original);
        assertEquals(original.getLastModificationDate(), service.getTagCollection(original.getTagCollectionId()).get().getLastModificationDate());
        verify(events, never()).post(any());
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
        service.saveTagAssociation("item", tag.getTagId());
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
        service.saveTagAssociation("item", tag.getTagId());
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
    public void updatesSelectedAssociationsAndCreatesNewLabels() {
        TagCollection collection = collection("Associations");
        Tag tag = tag(collection, "Existing");
        service.updateTagAssociations(collection.getTagCollectionId(), "item", Arrays.asList(tag.getTagId(), "New label"), true);
        assertEquals(2, service.getAssociatedTagsForItem(collection.getTagCollectionId(), "item").size());
        service.updateTagAssociations(collection.getTagCollectionId(), "item", Collections.singletonList(tag.getTagId()), true);
        assertEquals(Collections.singletonList(tag.getTagId()), service.getTagAssociationIds(collection.getTagCollectionId(), "item"));
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
    public void editingReturnedObjectsDoesNotPersistWithoutUpdate() {
        TagCollection collection = collection("Detached");
        Tag tag = tag(collection, "Original");
        clearInvocations(events);
        new TransactionTemplate(transactionManager).execute(status -> {
            service.getTag(tag.getTagId()).get().setTagLabel("Unsaved");
            service.getTagCollection(collection.getTagCollectionId()).get().setName("Unsaved");
            return null;
        });
        assertEquals("Original", service.getTag(tag.getTagId()).get().getTagLabel());
        assertEquals("Detached", service.getTagCollection(collection.getTagCollectionId()).get().getName());
        verify(events, never()).post(any());
    }

    @Test
    public void rejectsTagsInMissingCollectionsWithoutEvent() {
        Tag proposed = new Tag();
        proposed.setTagCollectionId(UUID.randomUUID().toString());
        proposed.setTagLabel("Orphan");
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
        service.saveTagAssociation("item", tag.getTagId());
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
