/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.assessment.integration.helper.integrated;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidAnswer1;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.integrated.TagSyncEventHandlerImpl;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.fail;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Relatively "pure" unit tests of {@link TagSyncEventHandlerImpl}. Does not actually test persistence,
 * just the basics of event handling registration and dispatch to service/dao functions in Samigo-proper.
 * Actual persistence operations are tested in various "facade" tests.
 */
public class TagSyncEventHandlerImplTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationEdit notification;
    @Mock
    private TagServiceHelper tagServiceHelper;
    private TagSyncEventHandlerImpl tagSyncEventHandler;
    private Supplier<ItemService> itemServiceSupplier;
    private Supplier<PublishedItemService> publishedItemServiceSupplier;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tagSyncEventHandler = new TagSyncEventHandlerImpl() {
            protected ItemService getItemService() {
                return itemServiceSupplier == null ? super.getItemService() : itemServiceSupplier.get();
            }
            protected PublishedItemService getPublishedItemService() {
                return publishedItemServiceSupplier == null ? super.getPublishedItemService() : publishedItemServiceSupplier.get();
            }
        };
        tagSyncEventHandler.setTagServiceHelper(tagServiceHelper);
        expectEventHandlerRegistration();
    }

    private void expectEventHandlerRegistration() {
        doAnswer(answerVoid(o -> {
            tagSyncEventHandler.registerEventCallbacks(notificationService);
        })).when(tagServiceHelper).registerTagEventHandler(tagSyncEventHandler);
        when(notificationService.addTransientNotification()).thenReturn(notification);

        tagSyncEventHandler.init();

        verify(tagServiceHelper).registerTagEventHandler(tagSyncEventHandler);
        verify(notificationService).addTransientNotification();

        verify(notification).addFunction("tags.update.tag");
        verify(notification).addFunction("tags.delete.tag");
        verify(notification).addFunction("tags.update.collection");
        verify(notification).addFunction("tags.delete.collection");

        verify(notification).setAction(tagSyncEventHandler);
    }

    @After
    public void tearDown() {
        // nothing to do
    }

    @Test
    public void testNotifyWithTagUpdateEvent() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();
        final TagServiceHelper.TagView tag = new TagServiceHelper.TagView("foo", "fooLabel", "fooCollectionId", "fooCollectionName");
        when(tagServiceHelper.findTagById("foo")).thenReturn(Optional.of(tag));

        tagSyncEventHandler.notify(notification, newEvent("tags.update.tag", "/tags/foo"));

        verify(itemServices.getLeft()).updateItemTagBindingsHavingTag(tag);
        verify(itemServices.getRight()).updateItemTagBindingsHavingTag(tag);
    }


    @Test
    public void testNotifyWithOrphanTagUpdateEvent() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();
        when(tagServiceHelper.findTagById("foo")).thenReturn(Optional.empty());

        tagSyncEventHandler.notify(notification, newEvent("tags.update.tag", "/tags/foo"));

        verify(itemServices.getLeft(), never()).updateItemTagBindingsHavingTag(any());
        verify(itemServices.getRight(), never()).updateItemTagBindingsHavingTag(any());
    }

    @Test
    public void testNotifyWithTagDeleteEvent() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();

        tagSyncEventHandler.notify(notification, newEvent("tags.delete.tag", "/tags/foo"));

        verify(itemServices.getLeft()).deleteItemTagBindingsHavingTagId("foo");
        verify(itemServices.getRight()).deleteItemTagBindingsHavingTagId("foo");
    }

    @Test
    public void testNotifyWithTagCollectionUpdateEvent() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();
        final TagServiceHelper.TagCollectionView tagCollection =
                new TagServiceHelper.TagCollectionView("fooCollectionId", "fooCollectionName");
        when(tagServiceHelper.findTagCollectionById("foo")).thenReturn(Optional.of(tagCollection));

        tagSyncEventHandler.notify(notification, newEvent("tags.update.collection", "/tagcollections/foo"));

        verify(itemServices.getLeft()).updateItemTagBindingsHavingTagCollection(tagCollection);
        verify(itemServices.getRight()).updateItemTagBindingsHavingTagCollection(tagCollection);
    }

    @Test
    public void testNotifyWithOrphanTagCollectionUpdateEvent() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();
        when(tagServiceHelper.findTagCollectionById("foo")).thenReturn(Optional.empty());

        tagSyncEventHandler.notify(notification, newEvent("tags.update.collection", "/tagcollections/foo"));

        verify(itemServices.getLeft(), never()).updateItemTagBindingsHavingTagCollection(any());
        verify(itemServices.getRight(), never()).updateItemTagBindingsHavingTagCollection(any());
    }

    @Test
    public void testNotifyWithTagCollectionDeleteEvent() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();

        tagSyncEventHandler.notify(notification, newEvent("tags.delete.collection", "/tagcollections/foo"));

        verify(itemServices.getLeft()).deleteItemTagBindingsHavingTagCollectionId("foo");
        verify(itemServices.getRight()).deleteItemTagBindingsHavingTagCollectionId("foo");
    }

    @Test
    public void testNotifyWithUnexpectedEventExitsQuietly() {
        tagSyncEventHandler.notify(notification, newEvent("foo", "foo"));
    }

    @Test
    public void testNotifyFailureExitsQuietly() {
        final Pair<ItemService, PublishedItemService> itemServices = mockItemServices();

        doThrow(new RuntimeException("oops")).when(itemServices.getKey()).deleteItemTagBindingsHavingTagCollectionId("foo");

        // choice of event types is completely random
        tagSyncEventHandler.notify(notification, newEvent("tags.delete.collection", "/tagcollections/foo"));
    }

    @Test
    public void testDestroy() {
        tagSyncEventHandler.destroy();
        verify(notificationService).removeNotification(notification);
    }


    private Pair<ItemService, PublishedItemService> mockItemServices() {
        return new ImmutablePair<>(mockItemService(),mockPublishedItemService());
    }

    private ItemService mockItemService() {
        final ItemService itemService = mock(ItemService.class);
        itemServiceSupplier = () -> itemService;
        return itemService;
    }

    private PublishedItemService mockPublishedItemService() {
        final PublishedItemService itemService = mock(PublishedItemService.class);
        publishedItemServiceSupplier = () -> itemService;
        return itemService;
    }

    private Event newEvent(String eventKey, String eventingEntityRef) {
        final Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(eventKey);
        when(event.getResource()).thenReturn(eventingEntityRef);
        return event;
    }
}
