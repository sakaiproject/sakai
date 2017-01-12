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
package org.sakaiproject.tags.impl.storage;

import org.junit.Test;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Null.NULL;

public class TagCollectionStorageTest extends BaseStorageTest {

    @Test
    public void testCreate() {

        // have to control ID creation so we can verify event contents
        final String specifiedTagCollectionId = UUID.randomUUID().toString();
        tagCollectionStorage.setIdProvider(() -> specifiedTagCollectionId);

        final Event event = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.new.collection", "/tagcollections/"+specifiedTagCollectionId, true)).thenReturn(event);

        final TagCollection tagCollection = newTagCollection("1");
        final TagCollection readBackTagCollection = saveAndReadBackNewTagCollection(tagCollection);

        //sanity checks
        assertThat(readBackTagCollection, hasProperty("tagCollectionId", equalTo(specifiedTagCollectionId)));
        assertNotSame("Test did not appear to actually hit the database", tagCollection, readBackTagCollection);

        tagCollection.setTagCollectionId(readBackTagCollection.getTagCollectionId());

        assertThat(readBackTagCollection, samePropertyValuesAs(tagCollection));
        verify(tagCollectionTimestampProvider, times(1)).get(); // be sure it's using the exact same time for both update and create
        verify(tagCollectionsEventTrackingService, times(1)).post(event);

    }

    @Test
    public void testCreateEventNotPostedIfTransactionFails() {
        // this will force a SQLException b/c of a missing PK
        tagCollectionStorage.setIdProvider(() -> null);

        try {
            newPersistentTagCollection("1");
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        verify(tagCollectionsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagCollectionsEventTrackingService, never()).post(any(Event.class));
    }

    @Test
    public void testUpdate() {

        final TagCollection tagCollectionInitial = newPersistentTagCollection("1");

        final TagCollection tagCollectionEdit = newTagCollection("2");
        final String tagCollectionId = tagCollectionInitial.getTagCollectionId();
        tagCollectionEdit.setTagCollectionId(tagCollectionId);
        tagCollectionEdit.setCreationDate(tagCollectionInitial.getCreationDate());
        tagCollectionEdit.setLastModificationDate(tagCollectionInitial.getLastModificationDate());

        // sanity check
        assertThat(tagCollectionInitial, not(samePropertyValuesAs(tagCollectionEdit)));

        final long newModDate = now();
        when(tagCollectionTimestampProvider.get()).thenReturn(newModDate);

        final Event event = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.update.collection",
                "/tagcollections/"+tagCollectionId, true)).thenReturn(event);

        tagCollectionStorage.updateTagCollection(tagCollectionEdit);

        final TagCollection readBackTagCollection = tagCollectionStorage.getForId(tagCollectionId).get();
        tagCollectionEdit.setLastModificationDate(newModDate);

        assertNotSame("Test did not appear to actually hit the database", tagCollectionEdit, readBackTagCollection);
        assertThat(readBackTagCollection, samePropertyValuesAs(tagCollectionEdit));
        verify(tagCollectionsEventTrackingService, times(1)).post(event);
    }

    @Test
    public void testUpdateEventNotPostedIfTransactionFails() {

        // set up the create event to avoid ambiguity in the update event verification
        // at the bottom (if we don't do this, eventservice.post() will be given a null
        // event as a side-effect of the initial tag create, but it's hard to distiguish
        // between that and a mis-implemented update event generater which might generate
        // a null event as a side effect of an error
        final String specifiedTagId = UUID.randomUUID().toString();
        tagCollectionStorage.setIdProvider(() -> specifiedTagId);
        final Event createEvent = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.new.collection",
                "/tagcollections/"+specifiedTagId, true)).thenReturn(createEvent);


        final TagCollection tagCollection = newPersistentTagCollection("1");
        forceErrorOnDBPreparedStatementCreation();

        try {
            tagCollectionStorage.updateTagCollection(tagCollection);
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        verify(tagCollectionsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.collection"))));
    }

    @Test
    public void testUpdateEventNotPostedIfNoFieldsChanged() {

        final String specifiedTagId = UUID.randomUUID().toString();
        tagCollectionStorage.setIdProvider(() -> specifiedTagId);
        final Event createEvent = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.new.collection",
                "/tagcollections/"+specifiedTagId, true)).thenReturn(createEvent);

        final TagCollection initialTagCollection = newPersistentTagCollection("1");
        final TagCollection tagCollectionClone = copyTagCollection(initialTagCollection);

        tagCollectionStorage.updateTagCollection(tagCollectionClone);

        verify(tagCollectionsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.collection"))));
    }

    @Test
    public void testUpdateEventNotPostedIfOnlyNonCriticalFieldsChanged() {

        final String specifiedTagId = UUID.randomUUID().toString();
        tagCollectionStorage.setIdProvider(() -> specifiedTagId);
        final Event createEvent = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.new.collection",
                "/tagcollections/"+specifiedTagId, true)).thenReturn(createEvent);

        final TagCollection initialTagCollection = newPersistentTagCollection("1");
        final TagCollection tagCollectionClone = copyTagCollection(initialTagCollection);
        tagCollectionClone.setLastUpdateDateInExternalSystem(tagCollectionClone.getLastUpdateDateInExternalSystem()+1); // just shouldn't be of interest to event listeners
        tagCollectionClone.setLastModificationDate(tagCollectionClone.getLastModificationDate()+1); // a generated field
        tagCollectionClone.setCreatedBy(tagCollectionClone.getCreatedBy()+"foo"); // a generated field and should be read-only
        tagCollectionClone.setCreationDate(tagCollectionClone.getCreationDate()+1); // a generated field, and should be read-only
        tagCollectionClone.setExternalUpdate(!(tagCollectionClone.getExternalUpdate())); // just shouldn't be of interest to event listeners
        tagCollectionClone.setExternalCreation(!(tagCollectionClone.getExternalCreation())); // just shouldn't be of interest to event listeners
        tagCollectionClone.setLastModifiedBy(tagCollectionClone.getLastModifiedBy()+"foo"); // a generated field
        tagCollectionClone.setLastSynchronizationDate(tagCollectionClone.getLastSynchronizationDate()+1); // just shouldn't be of interest to event listeners

        tagCollectionStorage.updateTagCollection(tagCollectionClone);

        verify(tagCollectionsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.collection"))));
    }

    @Test
    public void testGetForIdReturnsEmptyOptionalIfTagDoesNotExist() {
        final Optional<TagCollection> lookupResult = tagCollectionStorage.getForId("foo");
        assertFalse(lookupResult.isPresent());
    }

    @Test
    public void testUpdateEventNotPostedIfUpdatedTagCollectionDoesNotExist() {

        final TagCollection tagCollection = newTagCollection("1");
        tagCollection.setTagCollectionId(UUID.randomUUID().toString());

        try {
            tagCollectionStorage.updateTagCollection(tagCollection);
            fail("Should have thrown exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getMessage(), startsWith("Can't update a tag collection that doesn't exist"));
        }

        verify(tagCollectionsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.collection"))));
    }

    @Test
    public void testDelete() {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final String tagCollectionId = tagCollection.getTagCollectionId();

        final Event deleteEvent = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.delete.collection",
                "/tagcollections/"+tagCollectionId, true)).thenReturn(deleteEvent);

        tagCollectionStorage.deleteTagCollection(tagCollectionId);

        assertFalse(tagCollectionStorage.getForId(tagCollectionId).isPresent());
        verify(tagCollectionsEventTrackingService).post(deleteEvent);
    }

    @Test
    public void testDeleteCascadesToTags() {
        // create tags in two collections to make sure the cascade targets the correct collection
        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final TagCollection tagCollection2 = newPersistentTagCollection("2");

        final String tagCollection1Id = tagCollection1.getTagCollectionId();
        final String tagCollection2Id = tagCollection2.getTagCollectionId();

        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final Tag tag2Collection1 = newTag("2", tagCollection1);
        final Tag tag3Collection1 = newTag("3", tagCollection1);

        final Tag tag4Collection2 = newTag("4", tagCollection2);
        final Tag tag5Collection2 = newTag("5", tagCollection2);
        final Tag tag6Collection2 = newTag("6", tagCollection2);

        final String tag1Id = tagStorage.createTag(tag1Collection1);
        final String tag2Id = tagStorage.createTag(tag2Collection1);
        final String tag3Id = tagStorage.createTag(tag3Collection1);
        final String tag4Id = tagStorage.createTag(tag4Collection2);
        final String tag5Id = tagStorage.createTag(tag5Collection2);
        final String tag6Id = tagStorage.createTag(tag6Collection2);

        // sanity checks
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(3));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()), hasSize(3));

        final Event deleteEvent = mock(Event.class);
        when(tagCollectionsEventTrackingService.newEvent("tags.delete.collection",
                "/tagcollections/"+tagCollection1Id, true)).thenReturn(deleteEvent);

        tagCollectionStorage.deleteTagCollection(tagCollection1Id);
        assertFalse(tagCollectionStorage.getForId(tagCollection1Id).isPresent());
        assertTrue(tagCollectionStorage.getForId(tagCollection2Id).isPresent());
        assertFalse(tagStorage.getForId(tag1Id).isPresent());
        assertFalse(tagStorage.getForId(tag2Id).isPresent());
        assertFalse(tagStorage.getForId(tag3Id).isPresent());
        assertTrue(tagStorage.getForId(tag4Id).isPresent());
        assertTrue(tagStorage.getForId(tag5Id).isPresent());
        assertTrue(tagStorage.getForId(tag6Id).isPresent());
        verify(tagCollectionsEventTrackingService).post(deleteEvent);
    }

    @Test
    public void testDeleteEventNotPostedIfTransactionFails() {
        forceErrorOnDBPreparedStatementCreation();

        try {
            tagCollectionStorage.deleteTagCollection("foo");
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        verify(tagCollectionsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagCollectionsEventTrackingService, never()).post(any(Event.class));
    }

    @Test
    public void testDeleteEventNotPostedIfNoRecordsAffected() {
        assertFalse(tagCollectionStorage.getForId("foo").isPresent());
        tagCollectionStorage.deleteTagCollection("foo");
        verify(tagCollectionsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagCollectionsEventTrackingService, never()).post(any(Event.class));
    }

    //TODO implement me
//    @Test
//    public void testGetAll() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetForExternalSourceName() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTagCollectionsPaginated() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTotalTagCollections() {
//        fail("implement me");
//    }

}
