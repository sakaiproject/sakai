/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagEventHandler;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.w3c.dom.Element;

@Slf4j
public class TagSyncEventHandlerImpl implements TagEventHandler {

    // For now we're going to see how far we can get without actually having a direct
    // dependency on TagService. These are well-known Event IDs, so even _with_ a direct
    // dep, there's really no harm in inlining them here anyway.
    private static final String TAG_UPDATE_EVENT = "tags.update.tag";
    private static final String TAG_DELETE_EVENT = "tags.delete.tag";
    private static final String TAGCOLLECTION_UPDATE_EVENT = "tags.update.collection";
    private static final String TAGCOLLECTION_DELETE_EVENT = "tags.delete.collection";
    private static final String RESOURCE_REFEREENCE_DELIM_REGEX = "/";

    private TagServiceHelper tagServiceHelper;
    private NotificationEdit notification;
    private NotificationService notificationService;

    public void init() {
        if ( tagServiceHelper == null ) {
            log.warn("No TagServiceHelper assigned. Skipping initialization of this TagSyncEventHandlerImpl");
            return;
        }

        tagServiceHelper.registerTagEventHandler(this);
    }

    public void destroy() {
        if ( notificationService != null && notification != null ) {
            notificationService.removeNotification(notification);
        }
    }

    @Override
    public void registerEventCallbacks(NotificationService notificationService) {

        // register a transient notification for resources
        this.notificationService = notificationService;
        this.notification = notificationService.addTransientNotification();

        // we don't care about create evets
        this.notification.addFunction(TAG_UPDATE_EVENT);
        this.notification.addFunction(TAG_DELETE_EVENT);
        this.notification.addFunction(TAGCOLLECTION_UPDATE_EVENT);
        this.notification.addFunction(TAGCOLLECTION_DELETE_EVENT);

        this.notification.setAction(this);
    }

    @Override
    public void set(Element el) {
        // intentional no-op
    }

    @Override
    public void set(NotificationAction other) {
        // intentional no-op
    }

    @Override
    public NotificationAction getClone() {
        // TODO are we obliged to clone the underlying NotificationEdit, too? Or just a shallow copy like we're currently doing hre?
        final TagSyncEventHandlerImpl clone = new TagSyncEventHandlerImpl();
        clone.tagServiceHelper = tagServiceHelper;
        clone.notification = notification;
        clone.notificationService = notificationService;
        return clone;
    }

    @Override
    public void toXml(Element el) {
        // intentional no-op
    }

    @Override
    public void notify(Notification notification, Event event) {
        try {
            switch (event.getEvent()) {
                case TAG_UPDATE_EVENT:
                    handleTagUpdate(notification, event);
                    return;
                case TAG_DELETE_EVENT:
                    handleTagDelete(notification, event);
                    return;
                case TAGCOLLECTION_UPDATE_EVENT:
                    handleTagCollectionUpdate(notification, event);
                    return;
                case TAGCOLLECTION_DELETE_EVENT:
                    handleTagCollectionDelete(notification, event);
                    return;
                default:
                    noEventHandlerFor(notification, event);
                    return;
            }
        } catch ( Exception e ) {
            // We let reference ID unpack errors hit this catch. This means they'll be logged as errors. Compare
            // to unexpected event codes, which we log as debugs. The distinction is that we really don't care
            // about an unexpected event... end result is the same as if we had never received it, so we keep the
            // logs quiet. But if we get an expected event and the reference structure is unexpected then
            // we probably *do* have a real issue. It's most likely a static code bug, so there wouldn't be much
            // an ops team could do about it, but something is still plainly not working properly.
            eventHandlingFailed(notification, event, e);
            return;
        }
    }

    private void noEventHandlerFor(Notification notification, Event event) {
        // Really shouldn't ever happen ... registerEventCallbacks() filters out events we're not interested in,
        // so would be either a static bug in this class or a change in behavior of Sakai's notification system.
        // Either way, we don't care about the event, so just a debug message.
        log.debug("Unexpected event type: " + event.getEvent());
        return;
    }

    private void eventHandlingFailed(Notification notification, Event event, Exception e) {
        // far as we know, NotificationService does nothing helpful with exceptions raised from
        // handlers, e.g. is no retry protocol. so just log and abandon here.
        log.error("Failure handling event: " + e);
    }

    private void handleTagUpdate(Notification notification, Event event) {
        final ItemService itemService = getItemService();
        final PublishedItemService publishedItemService = getPublishedItemService();
        final Optional<TagServiceHelper.TagView> tagOptional = tagServiceHelper.findTagById(tagIdFor(event));
        if ( !(tagOptional.isPresent()) ) {
            // tag disappeared since this event was fired. nothing to do for the 'update'.
            // let's not get fancy and try to coerce this update to a delete. a delete event
            // should be on its way, so we'd be duplicating work if we coerced the update.
            log.debug("Tag for update event has gone missing. Skipping update processing. " + event);
            return;
        }
        // yes, either one of these could fail and leave work un-done. idea is for a healing job to
        // periodically run a full tag-question sync and pick up this type of orphaned update.
        itemService.updateItemTagBindingsHavingTag(tagOptional.get());
        publishedItemService.updateItemTagBindingsHavingTag(tagOptional.get());
    }

    private void handleTagDelete(Notification notification, Event event) {
        final ItemService itemService = getItemService();
        final PublishedItemService publishedItemService = getPublishedItemService();
        final String tagId = tagIdFor(event);
        // yes, either one of these could fail and leave work un-done. idea is for a healing job to
        // periodically run a full tag-question sync and pick up this type of orphaned update.
        itemService.deleteItemTagBindingsHavingTagId(tagId);
        publishedItemService.deleteItemTagBindingsHavingTagId(tagId);
    }

    private void handleTagCollectionUpdate(Notification notification, Event event) {
        final ItemService itemService = getItemService();
        final PublishedItemService publishedItemService = getPublishedItemService();
        final Optional<TagServiceHelper.TagCollectionView> tagCollectionOptional =
                tagServiceHelper.findTagCollectionById(tagIdFor(event));
        if ( !(tagCollectionOptional.isPresent()) ) {
            // tag collection disappeared since this event was fired. nothing to do for the 'update'.
            // let's not get fancy and try to coerce this update to a delete. a delete event
            // should be on its way, so we'd be duplicating work if we coerced the update.
            log.debug("Tag Collection for update event has gone missing. Skipping update processing. " + event);
            return;
        }
        // yes, either one of these could fail and leave work un-done. idea is for a healing job to
        // periodically run a full tag-question sync and pick up this type of orphaned update.
        itemService.updateItemTagBindingsHavingTagCollection(tagCollectionOptional.get());
        publishedItemService.updateItemTagBindingsHavingTagCollection(tagCollectionOptional.get());
    }

    private void handleTagCollectionDelete(Notification notification, Event event) {
        final ItemService itemService = getItemService();
        final PublishedItemService publishedItemService = getPublishedItemService();
        final String tagCollectionId = tagCollectionIdFor(event);
        // yes, either one of these could fail and leave work un-done. idea is for a healing job to
        // periodically run a full tag-question sync and pick up this type of orphaned update.
        itemService.deleteItemTagBindingsHavingTagCollectionId(tagCollectionId);
        publishedItemService.deleteItemTagBindingsHavingTagCollectionId(tagCollectionId);
    }

    // protected to help w/ testing
    protected ItemService getItemService() {
        return new ItemService(); // established Samigo pattern is instantiation on each invoke rather than DI
    }

    // protected to help w/ testing
    protected PublishedItemService getPublishedItemService() {
        return new PublishedItemService(); // established Samigo pattern is instantiation on each invoke rather than DI
    }

    public void setTagServiceHelper(TagServiceHelper tagServiceHelper) {
        this.tagServiceHelper = tagServiceHelper;
    }

    private String tagIdFor(Event event) throws IllegalArgumentException {
        return entityIdFor(event);
    }

    private String tagCollectionIdFor(Event event) throws IllegalArgumentException {
        return entityIdFor(event);
    }

    private String entityIdFor(Event event) throws IllegalArgumentException {
        // Properly this should go through the EntityManager/Entity/Reference APIs to eventually result in a
        // Reference.getId() call. But event handlers certainly exist that *dont* do that
        // (see ResourceReleaseRule). And we're trying to put off building all that machinery for as long as
        // possible since it really just amounts to boilerplate for our needs.
        final String resourceRef = event.getResource();
        if ( resourceRef == null ) {
            throw new IllegalArgumentException("Null resource reference for event: " + event);
        }
        // structure should be:
        //   /tags/<id>
        //   /tagcollections/<id>
        // (which is why we can currently use the same id unpacker for both entity types
        final String[] resourceRefParts = resourceRef.split(RESOURCE_REFEREENCE_DELIM_REGEX);
        try {
            return resourceRefParts[2];
        } catch ( ArrayIndexOutOfBoundsException e ) {
            throw new IllegalArgumentException("Unexpected resource reference structure for event: " + e);
        }
    }
}
