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

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.tags.api.MissingUuidException;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Null.NULL;

public class TagStorageTest extends BaseStorageTest {

    @Before
    public void setUp() {
        super.setUp();


    }

    @Test
    public void testCreateFailsIfOwningCollectionMissing() {
        final List<TagCollection> all = tagCollectionStorage.getAll();
        assertThat("Test assumes the tag collection repository is empty", all, empty());

        final TagCollection tagCollection = new TagCollection();
        tagCollection.setTagCollectionId("tagCollectionId");
        tagCollection.setName("tagCollectionName");
        final Tag tag = newTag("1", tagCollection);

        // Can't use a ExpectedException rule b/c we need to run additional verify()s after the exeception
        // occurs
        try {
            tagStorage.createTag(tag);
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getMessage(), startsWith("Failure in database action: Create a tag"));
            // some credit to http://stackoverflow.com/a/39221730
            assertThat(e.getCause(), allOf(instanceOf(SQLException.class),
                    hasProperty("message", startsWith("integrity constraint violation"))));
        }

        verify(tagsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(any(Event.class));

    }

    @Test
    public void testCreate() throws InterruptedException {
        final List<Tag> all = tagStorage.getAll();
        assertThat("Test assumes the tag repository is empty", all, empty());

        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tag = newTag("1", tagCollection);

        // have to control ID creation so we can verify event contents
        final String specifiedTagId = UUID.randomUUID().toString();
        tagStorage.setIdProvider(() -> specifiedTagId);

        when(tagTimestampProvider.get()).thenReturn(tag.getCreationDate());

        final Event event = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.new.tag", "/tags/"+specifiedTagId, true)).thenReturn(event);

        final String generatedTagId = tagStorage.createTag(tag);

        //sanity check
        assertThat(generatedTagId, equalTo(specifiedTagId));

        tag.setTagId(generatedTagId);
        final Tag readBackTag = tagStorage.getForId(generatedTagId).get();

        //sanity check
        assertNotSame("Test did not appear to actually hit the database", tag, readBackTag);

        assertThat(readBackTag, samePropertyValuesAs(tag));
        verify(tagTimestampProvider, times(1)).get(); // be sure it's using the exact same time for both update and create
        verify(tagsEventTrackingService, times(1)).post(event);
    }

    @Test
    public void testCreateEventNotPostedIfTransactionFails() {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tagInitial = newTag("1", tagCollection);

        when(tagTimestampProvider.get()).thenReturn(tagInitial.getCreationDate());

        // this will force a SQLException b/c of a missing PK
        tagStorage.setIdProvider(() -> null);

        try {
            final String tagId = tagStorage.createTag(tagInitial);
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        verify(tagsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(any(Event.class));
    }

    @Test
    public void testUpdate() {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tagInitial = newTag("1", tagCollection);

        when(tagTimestampProvider.get()).thenReturn(tagInitial.getCreationDate());

        final String tagId = tagStorage.createTag(tagInitial);

        final Tag tagEdit = newTag("2", tagCollection);
        tagEdit.setTagId(tagId);
        tagEdit.setCreationDate(tagInitial.getCreationDate());
        tagEdit.setLastModificationDate(tagInitial.getLastModificationDate());

        // sanity check
        assertThat(tagInitial, not(samePropertyValuesAs(tagEdit)));

        final long newModDate = now();
        when(tagTimestampProvider.get()).thenReturn(newModDate);

        final Event event = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.update.tag", "/tags/"+tagId, true)).thenReturn(event);

        tagStorage.updateTag(tagEdit);

        final Tag readBackTag = tagStorage.getForId(tagId).get();
        tagEdit.setLastModificationDate(newModDate);

        assertNotSame("Test did not appear to actually hit the database", tagEdit, readBackTag);
        assertThat(readBackTag, samePropertyValuesAs(tagEdit));
        verify(tagsEventTrackingService, times(1)).post(event);
    }

    @Test
    public void testUpdateEventNotPostedIfTransactionFails() {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tagInitial = newTag("1", tagCollection);

        when(tagTimestampProvider.get()).thenReturn(tagInitial.getCreationDate());
        final String specifiedTagId = UUID.randomUUID().toString();
        tagStorage.setIdProvider(() -> specifiedTagId);

        // set up the create event to avoid ambiguity in the update event verification
        // at the bottom (if we don't do this, eventservice.post() will be given a null
        // event as a side-effect of the initial tag create, but it's hard to distiguish
        // between that and a mis-implemented update event generater which might generate
        // a null event as a side effect of an error
        final Event createEvent = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.new.tag", "/tags/"+specifiedTagId, true)).thenReturn(createEvent);

        final String tagId = tagStorage.createTag(tagInitial);

        final Tag tagEdit = newTag("2", tagCollection);
        tagEdit.setTagId(tagId);
        // this will force a SQLException b/c of a missing FK
        tagEdit.setTagCollectionId(null);

        try {
            tagStorage.updateTag(tagEdit);
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        verify(tagsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.tag"))));
    }

    @Test
    public void testUpdateEventNotPostedIfNoFieldsChanged() throws MissingUuidException {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tagInitial = newTag("1", tagCollection);

        when(tagTimestampProvider.get()).thenReturn(tagInitial.getCreationDate());
        final String specifiedTagId = UUID.randomUUID().toString();
        tagStorage.setIdProvider(() -> specifiedTagId);

        final Event createEvent = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.new.tag", "/tags/"+specifiedTagId, true)).thenReturn(createEvent);

        final String tagId = tagStorage.createTag(tagInitial);
        final Tag readBackTag = tagStorage.getForId(tagId).get();
        final Tag tagClone = copyTag(readBackTag);

        tagStorage.updateTag(tagClone);

        verify(tagsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.tag"))));
    }

    @Test
    public void testUpdateEventNotPostedIfOnlyNonCriticalFieldsChanged() throws MissingUuidException {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tagInitial = newTag("1", tagCollection);

        when(tagTimestampProvider.get()).thenReturn(tagInitial.getCreationDate());
        final String specifiedTagId = UUID.randomUUID().toString();
        tagStorage.setIdProvider(() -> specifiedTagId);

        final Event createEvent = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.new.tag", "/tags/"+specifiedTagId, true)).thenReturn(createEvent);

        final String tagId = tagStorage.createTag(tagInitial);
        final Tag readBackTag = tagStorage.getForId(tagId).get();
        final Tag tagClone = copyTag(readBackTag);
        tagClone.setLastModificationDate(tagClone.getLastModificationDate()+1); // a generated field
        tagClone.setLastModifiedBy(tagClone.getLastModifiedBy()+"foo"); // a generated field
        tagClone.setCreationDate(tagClone.getCreationDate()+1); // a generated field, and should be read-only
        tagClone.setCreatedBy(tagClone.getCreatedBy()+"foo"); // a generated field and should be read-only
        tagClone.setCollectionName(tagClone.getCollectionName()+"foo"); // this is a transient field
        tagClone.setExternalUpdate(!(tagClone.getExternalUpdate())); // just shouldn't be of interest to event listeners
        tagClone.setExternalCreation(!(tagClone.getExternalCreation())); // just shouldn't be of interest to event listeners
        tagClone.setExternalCreationDate(tagClone.getExternalCreationDate()+1); // just shouldn't be of interest to event listeners, and should be read-only
        tagClone.setLastUpdateDateInExternalSystem(tagClone.getLastUpdateDateInExternalSystem()+1); // just shouldn't be of interest to event listeners
        tagStorage.updateTag(tagClone);

        verify(tagsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.tag"))));
    }

    @Test
    public void testUpdateEventNotPostedIfUpdatedTagDoesNotExist() {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tag = newTag("1", tagCollection);
        tag.setTagId(UUID.randomUUID().toString());

        try {
            tagStorage.updateTag(tag);
            fail("Should have thrown exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getMessage(), startsWith("Can't update a tag that doesn't exist"));
        }

        verify(tagsEventTrackingService, never())
                .post(argThatMatchesAnyOf(NULL,
                        argHasProperty("event", equalTo("tags.update.tag"))));
    }

    @Test
    public void testDelete() {
        final TagCollection tagCollection = newPersistentTagCollection("1");
        final Tag tagInitial = newTag("1", tagCollection);

        when(tagTimestampProvider.get()).thenReturn(tagInitial.getCreationDate());
        final String tagId = tagStorage.createTag(tagInitial);

        // sanity check
        assertNotNull(tagStorage.getForId(tagId));

        final Event event = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.delete.tag", "/tags/"+tagId, true)).thenReturn(event);

        tagStorage.deleteTag(tagId);

        assertFalse(tagStorage.getForId(tagId).isPresent());
        verify(tagsEventTrackingService, times(1)).post(event);
    }

    @Test
    public void testDeleteEventNotPostedIfTransactionFails() {
        forceErrorOnDBPreparedStatementCreation();

        try {
            tagStorage.deleteTag("foo");
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        verify(tagsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(any(Event.class));
    }

    @Test
    public void testDeleteEventNotPostedIfNoRecordsAffected() {
        assertFalse(tagStorage.getForId("foo").isPresent());
        tagStorage.deleteTag("foo");
        verify(tagsEventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(any(Event.class));
    }

    @Test
    public void testDeleteTagsOlderThanDateFromCollection() {
        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final TagCollection tagCollection2 = newPersistentTagCollection("2");

        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final Tag tag2Collection1 = newTag("2", tagCollection1);
        final Tag tag3Collection1 = newTag("3", tagCollection1);

        final Tag tag4Collection2 = newTag("4", tagCollection2);
        final Tag tag5Collection2 = newTag("5", tagCollection2);
        final Tag tag6Collection2 = newTag("6", tagCollection2);

        when(tagTimestampProvider.get()).thenReturn(tag1Collection1.getCreationDate());

        final String tag1Id = tagStorage.createTag(tag1Collection1);
        final String tag2Id = tagStorage.createTag(tag2Collection1);
        final String tag3Id = tagStorage.createTag(tag3Collection1);
        final String tag4Id = tagStorage.createTag(tag4Collection2);
        final String tag5Id = tagStorage.createTag(tag5Collection2);
        final String tag6Id = tagStorage.createTag(tag6Collection2);

        // sanity checks
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(3));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()), hasSize(3));

        // bump the timestamp provider so mod timestamps are later than create timestamps
        final long modTimestamp = tag1Collection1.getCreationDate() + 1;
        when(tagTimestampProvider.get()).thenReturn(modTimestamp);

        final Tag tag2Collection1ReadBack = tagStorage.getForId(tag2Id).get();
        tag2Collection1ReadBack.setTagLabel(tag2Collection1ReadBack.getTagLabel()+"foo");
        tagStorage.updateTag(tag2Collection1ReadBack);

        final Tag tag5Collection1ReadBack = tagStorage.getForId(tag5Id).get();
        tag5Collection1ReadBack.setTagLabel(tag5Collection1ReadBack.getTagLabel()+"foo");
        tagStorage.updateTag(tag5Collection1ReadBack);

        final Event tag1DeleteEvent = mock(Event.class);
        final Event tag3DeleteEvent = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.delete.tag", "/tags/"+tag1Id, true)).thenReturn(tag1DeleteEvent);
        when(tagsEventTrackingService.newEvent("tags.delete.tag", "/tags/"+tag3Id, true)).thenReturn(tag3DeleteEvent);

        tagStorage.deleteTagsOlderThanDateFromCollection(tagCollection1.getTagCollectionId(), modTimestamp);

        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(1));
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()).get(0),
                hasProperty("tagId", equalTo(tag2Id)));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()), hasSize(3));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()),
                containsInAnyOrder(
                        hasProperty("tagId", equalTo(tag4Id)),
                        hasProperty("tagId", equalTo(tag5Id)),
                        hasProperty("tagId", equalTo(tag6Id))));
        verify(tagsEventTrackingService).post(tag3DeleteEvent);
        verify(tagsEventTrackingService).post(tag1DeleteEvent);
    }

    @Test
    public void testDeleteTagsOlderThanDateFromCollectionEventNotPostedIfNoRecordsAffected() {
        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final Tag tag2Collection1 = newTag("2", tagCollection1);
        final Tag tag3Collection1 = newTag("3", tagCollection1);

        final long createDate = tag1Collection1.getCreationDate();
        when(tagTimestampProvider.get()).thenReturn(createDate);

        final String tag1Id = tagStorage.createTag(tag1Collection1);
        final String tag2Id = tagStorage.createTag(tag2Collection1);
        final String tag3Id = tagStorage.createTag(tag3Collection1);


        tagStorage.deleteTagsOlderThanDateFromCollection(tagCollection1.getTagCollectionId(), createDate);

        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(3));
        assertTrue(tagStorage.getForId(tag1Id).isPresent());
        assertTrue(tagStorage.getForId(tag2Id).isPresent());
        assertTrue(tagStorage.getForId(tag3Id).isPresent());
        verify(tagsEventTrackingService, never()).newEvent(eq("tags.delete.tag"), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(argThatMatchesAnyOf(
                argHasProperty("event", equalTo("tags.delete.tag"))));

    }

    @Test
    public void testDeleteTagsOlderThanDateFromCollectionEventNotPostedIfTransactionFails() {
        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final Tag tag2Collection1 = newTag("2", tagCollection1);
        final Tag tag3Collection1 = newTag("3", tagCollection1);

        final long createDate = tag1Collection1.getCreationDate();
        when(tagTimestampProvider.get()).thenReturn(createDate);

        final String tag1Id = tagStorage.createTag(tag1Collection1);
        final String tag2Id = tagStorage.createTag(tag2Collection1);
        final String tag3Id = tagStorage.createTag(tag3Collection1);

        forceErrorOnDBPreparedStatementCreation();

        try {
            tagStorage.deleteTagsOlderThanDateFromCollection(tagCollection1.getTagCollectionId(), createDate);
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        stopForceErrorOnDBPreparedStatementCreation(); // so we do our read-backs
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(3));
        assertTrue(tagStorage.getForId(tag1Id).isPresent());
        assertTrue(tagStorage.getForId(tag2Id).isPresent());
        assertTrue(tagStorage.getForId(tag3Id).isPresent());
        verify(tagsEventTrackingService, never()).newEvent(eq("tags.delete.tag"), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(argThatMatchesAnyOf(
                argHasProperty("event", equalTo("tags.delete.tag"))));
    }

    @Test
    public void testDeleteTagFromExternalCollection() {
        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final TagCollection tagCollection2 = newPersistentTagCollection("2");

        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final Tag tag2Collection1 = newTag("2", tagCollection1);

        final Tag tag3Collection2 = newTag("3", tagCollection2);
        final Tag tag4Collection2 = newTag("4", tagCollection2);

        // external ID duplication across collections theoretically not happen in practice, but
        // there's no guarantee that won't happen, and doing it here lets us directly verify that
        // the delete operation is indeed qualified by collection ID
        tag3Collection2.setExternalId(tag1Collection1.getExternalId());

        when(tagTimestampProvider.get()).thenReturn(tag1Collection1.getCreationDate());

        final String tag1Id = tagStorage.createTag(tag1Collection1);
        final String tag2Id = tagStorage.createTag(tag2Collection1);
        final String tag3Id = tagStorage.createTag(tag3Collection2);
        final String tag4Id = tagStorage.createTag(tag4Collection2);

        // sanity checks
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(2));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()), hasSize(2));

        final Event tag1DeleteEvent = mock(Event.class);
        when(tagsEventTrackingService.newEvent("tags.delete.tag", "/tags/"+tag1Id, true)).thenReturn(tag1DeleteEvent);

        tagStorage.deleteTagFromExternalCollection(tag1Collection1.getExternalId(), tagCollection1.getTagCollectionId());
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()), hasSize(1));
        assertThat(tagStorage.getAllInCollection(tagCollection1.getTagCollectionId()).get(0),
                hasProperty("tagId", equalTo(tag2Id)));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()), hasSize(2));
        assertThat(tagStorage.getAllInCollection(tagCollection2.getTagCollectionId()),
                containsInAnyOrder(
                        hasProperty("tagId", equalTo(tag3Id)),
                        hasProperty("tagId", equalTo(tag4Id))));
        verify(tagsEventTrackingService).post(tag1DeleteEvent);
    }

    @Test
    public void testDeleteTagFromExternalCollectionEventNotPostedIfNoRecordsAffected() {
        // slightly more elaborate that strictly necessary, but creating a fixture which
        // should not be affected by the delete lets us more directly verify that the
        // delete is limited by the correct predicate.
        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final String tag1Id = tagStorage.createTag(tag1Collection1);

        // tag collection ID manip is slightly different here b/c the generated tag collection
        // ID probably maxes out the column width, so appending "foo" to it, might have
        // no effect depending on how the underlying rdbms handles string overflows
        tagStorage.deleteTagFromExternalCollection(tag1Collection1.getExternalId()+"foo",
                tagCollection1.getTagCollectionId().substring(0, 10));

        assertTrue(tagStorage.getForId(tag1Id).isPresent());
        verify(tagsEventTrackingService, never()).newEvent(eq("tags.delete.tag"), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(argThatMatchesAnyOf(
                argHasProperty("event", equalTo("tags.delete.tag"))));
    }

    @Test
    public void testDeleteTagFromExternalCollectionEventNotPostedIfTransactionFails() {

        final TagCollection tagCollection1 = newPersistentTagCollection("1");
        final Tag tag1Collection1 = newTag("1", tagCollection1);
        final String tag1Id = tagStorage.createTag(tag1Collection1);


        forceErrorOnDBPreparedStatementCreation();

        try {
            tagStorage.deleteTagFromExternalCollection(tag1Collection1.getExternalId()+"foo",
                    tagCollection1.getTagCollectionId().substring(0, 10));
            fail("Should have thrown an exception");
        } catch ( RuntimeException e ) {
            assertThat(e.getCause(), instanceOf(SQLException.class)); // sanity check
        }

        stopForceErrorOnDBPreparedStatementCreation();
        assertTrue(tagStorage.getForId(tag1Id).isPresent());
        verify(tagsEventTrackingService, never()).newEvent(eq("tags.delete.tag"), anyString(), anyBoolean());
        verify(tagsEventTrackingService, never()).post(argThatMatchesAnyOf(
                argHasProperty("event", equalTo("tags.delete.tag"))));
    }

    //TODO implement me
//    @Test
//    public void testAll() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetAllInCollection() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTagsByExactLabel() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTagsByPartialLabel() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTagsByPrefixInLabel() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetForExternalIdAndCollection() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTagsPaginatedInCollection() {
//        fail("implememnt me");
//    }

    //TODO implement me
//    @Test
//    public void testGetTotalTagsInCollection() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testCreateRollsBackTransactionOnFailure() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testUpdateRollsBackTransactionOnFailure() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testDeleteRollsBackTransactionOnFailure() {
//        fail("implement me");
//    }

    //TODO implement me
//    @Test
//    public void testCleansUpPreparedStatements() {
//        fail("implement me");
//    }

    @Test
    public void testGetForIdReturnsEmptyOptionalIfTagDoesNotExist() {
        final Optional<Tag> lookupResult = tagStorage.getForId("foo");
        assertFalse(lookupResult.isPresent());
    }

    //TODO implement me
//    @Test
//    public void collectionNameLookupUsesAJoinNotASeparateQuery() {
//        fail("this is really just a reminder to change the impl - probably dont need to test it as an impl detail");
//    }

    //TODO implement me
//    @Test
//    public void protectsAgainstTagsWithDuplicateLabelsWithinACollection() {
//        fail("this is largely just a reminder to fix a bug unrelated to the primary event testing task at hand");
//    }

}
