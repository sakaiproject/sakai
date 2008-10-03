/**
 * $Id$
 * $URL$
 * EntityBrokerImpl.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.BrowseEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.sakaiproject.entitybroker.util.EntityResponse;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;

/**
 * The default implementation of the EntityBroker interface
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityBrokerImpl implements EntityBroker, PropertiesProvider {

    private static Log log = LogFactory.getLog(EntityBrokerImpl.class);

    private EntityActionsManager entityActionsManager;
    public void setEntityActionsManager(EntityActionsManager entityActionsManager) {
        this.entityActionsManager = entityActionsManager;
    }

    private EntityProviderManager entityProviderManager;
    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    private EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    private EntityEncodingManager entityEncodingManager;
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityMetaPropertiesService entityMetaPropertiesService;
    public void setEntityMetaPropertiesService(
            EntityMetaPropertiesService entityMetaPropertiesService) {
        this.entityMetaPropertiesService = entityMetaPropertiesService;
    }

    private EntityTaggingService entityTaggingService;
    public void setEntityTaggingService(EntityTaggingService entityTaggingService) {
        this.entityTaggingService = entityTaggingService;
    }

    private EntityHandlerImpl entityHandler;
    public void setEntityRequestHandler(EntityHandlerImpl entityHandler) {
        this.entityHandler = entityHandler;
    }

    private EntityManager entityManager;
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Must be the implementation
     */
    private RequestStorageImpl requestStorage;
    public void setRequestStorage(RequestStorageImpl requestStorage) {
        this.requestStorage = requestStorage;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#entityExists(java.lang.String)
     */
    public boolean entityExists(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        boolean exists = entityBrokerManager.entityExists(ref);
        return exists;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getEntityURL(java.lang.String)
     */
    public String getEntityURL(String reference) {
        String URL = entityBrokerManager.getEntityURL(reference, null, null);
        return URL;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getEntityURL(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getEntityURL(String reference, String viewKey, String extension) {
        String URL = entityBrokerManager.getEntityURL(reference, viewKey, extension);
        return URL;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getEntityView(java.lang.String, java.lang.String, java.lang.String)
     */
    public EntityView getEntityView(String reference, String viewKey, String extension) {
        EntityReference ref = parseReference(reference);
        EntityView ev = entityBrokerManager.makeEntityView(ref, viewKey, extension);
        return ev;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#isPrefixRegistered(java.lang.String)
     */
    public boolean isPrefixRegistered(String prefix) {
        boolean registered = false;
        if (entityProviderManager.getProviderByPrefix(prefix) != null) {
            registered = true;
        }
        return registered;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getRegisteredPrefixes()
     */
    public Set<String> getRegisteredPrefixes() {
        Set<String> prefixes = entityProviderManager.getRegisteredPrefixes();
        return prefixes;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#parseReference(java.lang.String)
     */
    public EntityReference parseReference(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        return ref;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#fireEvent(java.lang.String, java.lang.String)
     */
    public void fireEvent(String eventName, String reference) {
        if (eventName == null || "".equals(eventName)) {
            throw new IllegalArgumentException("Cannot fire event if name is null or empty");
        }
        if (reference == null || "".equals(reference)) {
            throw new IllegalArgumentException("Cannot fire event if reference is null or empty");
        }
        String refName = reference;
        try {
            // parse the reference string to validate it and remove any extra bits
            EntityReference ref = entityBrokerManager.parseReference(reference);
            if (ref != null) {
                refName = ref.toString();
            } else {
                // fallback to simple parsing
                refName = new EntityReference(reference).toString();
            }
        } catch (Exception e) {
            refName = reference;
            log.warn("Invalid reference ("+reference+") for eventName ("+eventName+"), could not parse the reference correctly, continuing to create event with original reference");
        }
        // had to take out the exists check because it makes firing events for removing entities very annoying -AZ
        Event event = eventTrackingService.newEvent(eventName, refName, true,
                NotificationService.PREF_IMMEDIATE);
        eventTrackingService.post(event);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#fireEntityRequest(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.lang.Object)
     */
    public EntityResponse fireEntityRequest(String reference, String viewKey, String format,
            Map<String, String> params, Object entity) {
        return entityHandler.fireEntityRequestInternal(reference, viewKey, format, params, entity);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#fetchEntity(java.lang.String)
     */
    public Object fetchEntity(String reference) {
        Object entity = null;
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            // not handled in EB so attempt to parse out a prefix and try to get entity from the legacy system
            try {
                // cannot test this in a meaningful way so the tests are designed to not get here -AZ
                entity = entityManager.newReference(reference).getEntity();
            } catch (Exception e) {
                log.warn("Failed to look up reference '" + reference
                        + "' to an entity in legacy entity system", e);
            }
        } else {
            // this is a registered prefix
            entity = entityBrokerManager.fetchEntity(ref);
        }
        return entity;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getEntity(java.lang.String)
     */
    public EntityData getEntity(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        return entityBrokerManager.getEntityData(ref);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#fetchEntities(java.lang.String, org.sakaiproject.entitybroker.entityprovider.search.Search, java.util.Map)
     */
    public List<?> fetchEntities(String prefix, Search search, Map<String, Object> params) {
        EntityReference ref = new EntityReference(prefix, "");
        List<?> l = null;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<String, Object>(); }
            l = entityBrokerManager.fetchEntities(ref, search, params);
        } finally {
            requestStorage.reset();
        }
        return l;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getEntities(java.lang.String, org.sakaiproject.entitybroker.entityprovider.search.Search, java.util.Map)
     */
    public List<EntityData> getEntities(String prefix, Search search, Map<String, Object> params) {
        EntityReference ref = new EntityReference(prefix, "");
        List<EntityData> data = null;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<String, Object>(); }
            data = entityBrokerManager.getEntitiesData(ref, search, params);
        } finally {
            requestStorage.reset();
        }
        return data;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#browseEntities(java.lang.String, org.sakaiproject.entitybroker.entityprovider.search.Search, java.lang.String, java.lang.String, java.util.Map)
     */
    public List<EntityData> browseEntities(String prefix, Search search,
            String userReference, String associatedReference, String parentReference, Map<String, Object> params) {
        EntityReference parentRef = null;
        if (parentReference != null) {
            parentRef = entityBrokerManager.parseReference(parentReference);
        }
        List<EntityData> data = null;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<String, Object>(); }
            data = entityBrokerManager.browseEntities(prefix, search, userReference, associatedReference, parentRef, params);
        } finally {
            requestStorage.reset();
        }
        return data;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBroker#getBrowseableEntities(java.lang.String)
     */
    public List<BrowseEntity> getBrowseableEntities(String parentPrefix) {
        List<BrowseEntity> l = entityBrokerManager.getBrowseableEntities(parentPrefix);
        return l;
    }


    public void formatAndOutputEntity(String reference, String format, List<?> entities,
            OutputStream output, Map<String, Object> params) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
        }
        try {
            requestStorage.setRequestValues(params);
            // convert entities to entity data list
            List<EntityData> data = entityBrokerManager.convertToEntityData(entities, ref);
            if (params == null) { params = new HashMap<String, Object>(); }
            entityEncodingManager.formatAndOutputEntity(ref, format, data, output, params);
        } finally {
            requestStorage.reset();
        }
    }

    public Object translateInputToEntity(String reference, String format, InputStream input,
            Map<String, Object> params) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
        }
        Object entity = null;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<String, Object>(); }
            entity = entityEncodingManager.translateInputToEntity(ref, format, input, params);
        } finally {
            requestStorage.reset();
        }
        return entity;
    }

    public ActionReturn executeCustomAction(String reference, String action,
            Map<String, Object> params, OutputStream outputStream) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            throw new IllegalArgumentException("Invalid entity reference, no provider found to handle this ref: " + reference);
        }
        ActionsExecutable actionProvider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), ActionsExecutable.class);
        if (actionProvider == null) {
            throw new IllegalArgumentException("The provider for prefix ("+ref.getPrefix()+") cannot handle custom actions");
        }
        ActionReturn ar = null;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<String, Object>(); }
            ar = entityActionsManager.handleCustomActionExecution(actionProvider, ref, action, params, outputStream, null, null);
            // populate the entity data
            if (ar != null) {
                if (ar.entitiesList != null) {
                    entityBrokerManager.populateEntityData(ar.entitiesList);
                } else if (ar.entityData != null) {
                    entityBrokerManager.populateEntityData( new EntityData[] {ar.entityData} );
                }
            }
        } finally {
            requestStorage.reset();
        }
        return ar;
    }


    // PROPERTIES

    public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
            boolean exactMatch) {
        return entityMetaPropertiesService.findEntityRefs(prefixes, name, searchValue, exactMatch);
    }

    public Map<String, String> getProperties(String reference) {
        return entityMetaPropertiesService.getProperties(reference);
    }

    public String getPropertyValue(String reference, String name) {
        return entityMetaPropertiesService.getPropertyValue(reference, name);
    }

    public void setPropertyValue(String reference, String name, String value) {
        entityMetaPropertiesService.setPropertyValue(reference, name, value);
    }


    // TAGS


    public List<EntityData> findEntitesByTags(String[] tags, String[] prefixes,
            boolean matchAll, Search search, Map<String, Object> params) {
        requestStorage.setRequestValues(params);
        List<EntityData> results = entityTaggingService.findEntitesByTags(tags, prefixes, matchAll, search);
        requestStorage.reset();
        return results;
    }

    public List<String> getTagsForEntity(String reference) {
        return entityTaggingService.getTagsForEntity(reference);
    }

    public void removeTagsFromEntity(String reference, String[] tags) {
        entityTaggingService.removeTagsFromEntity(reference, tags);
    }

    public void addTagsToEntity(String reference, String[] tags) {
        entityTaggingService.addTagsToEntity(reference, tags);
    }

    public void setTagsForEntity(String reference, String[] tags) {
        entityTaggingService.setTagsForEntity(reference, tags);
    }

    /**
     * @deprecated use {@link #getTagsForEntity(String)}
     */
    public Set<String> getTags(String reference) {
        return new HashSet<String>( entityTaggingService.getTagsForEntity(reference) );
    }

    /**
     * @deprecated use {@link #setTagsForEntity(String, String[])}
     */
    public void setTags(String reference, String[] tags) {
        entityTaggingService.setTagsForEntity(reference, tags);
    }

    /**
     * @deprecated use {@link #findEntitesByTags(String[], String[], boolean, Search)}
     */
    public List<String> findEntityRefsByTags(String[] tags) {
        ArrayList<String> refs = new ArrayList<String>();
        List<EntityData> results = entityTaggingService.findEntitesByTags(tags, null, false, null);
        for (EntityData entitySearchResult : results) {
            refs.add( entitySearchResult.getEntityReference() );
        }
        return refs;
    }

}
