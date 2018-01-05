/**
 * $Id$
 * $URL$
 * EntityBrokerImpl.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.BrowseEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.QuerySearch;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchContent;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchResults;
import org.sakaiproject.entitybroker.entityprovider.extension.TagSearchService;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.util.EntityResponse;

/**
 * The default implementation of the EntityBroker interface
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
@Slf4j
public class EntityBrokerImpl implements EntityBroker, PropertiesProvider {

    public void init() {
        // setup the external providers
        if (externalIntegrationProvider != null) {
            SearchProvider searchProvider = externalIntegrationProvider.findService(SearchProvider.class);
            if (searchProvider != null) {
                this.searchProvider = searchProvider;
            }
        }
    }

    /**
     * Empty constructor
     */
    protected EntityBrokerImpl() { }

    /**
     * Minimal constructor
     */
    public EntityBrokerImpl(EntityProviderManager entityProviderManager,
            EntityBrokerManagerImpl entityBrokerManager,
            RequestStorageWrite requestStorageWrite) {
        super();
        this.entityProviderManager = entityProviderManager;
        this.entityBrokerManager = entityBrokerManager;
        this.requestStorage = requestStorageWrite;
    }

    /**
     * Full constructor with optional pieces
     */
    public EntityBrokerImpl(EntityProviderManager entityProviderManager,
            EntityBrokerManagerImpl entityBrokerManager,
            RequestStorageWrite requestStorageWrite,
            PropertiesProvider propertiesProvider,
            TagSearchService tagSearchService) {
        super();
        this.entityProviderManager = entityProviderManager;
        this.entityBrokerManager = entityBrokerManager;
        this.requestStorage = requestStorageWrite;
        this.propertiesProvider = propertiesProvider;
        this.tagSearchService = tagSearchService;
    }

    private EntityProviderManager entityProviderManager;
    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    private EntityBrokerManagerImpl entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManagerImpl entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private RequestStorageWrite requestStorage;
    public void setRequestStorage(RequestStorageWrite requestStorage) {
        this.requestStorage = requestStorage;
    }

    private ExternalIntegrationProvider externalIntegrationProvider;
    public void setExternalIntegrationProvider(
            ExternalIntegrationProvider externalIntegrationProvider) {
        this.externalIntegrationProvider = externalIntegrationProvider;
    }

    // OPTIONAL Data Storage providers
    private PropertiesProvider propertiesProvider;
    public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    private TagSearchService tagSearchService;
    public void setTagSearchService(TagSearchService tagSearchService) {
        this.tagSearchService = tagSearchService;
    }

    private SearchProvider searchProvider;
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#entityExists(java.lang.String)
     */
    public boolean entityExists(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        boolean exists = entityBrokerManager.entityExists(ref);
        return exists;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#getEntityURL(java.lang.String)
     */
    public String getEntityURL(String reference) {
        String URL = entityBrokerManager.getEntityURL(reference, null, null);
        return URL;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#getEntityURL(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getEntityURL(String reference, String viewKey, String extension) {
        String URL = entityBrokerManager.getEntityURL(reference, viewKey, extension);
        return URL;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#getEntityView(java.lang.String, java.lang.String, java.lang.String)
     */
    public EntityView getEntityView(String reference, String viewKey, String extension) {
        EntityReference ref = parseReference(reference);
        EntityView ev = entityBrokerManager.makeEntityView(ref, viewKey, extension);
        return ev;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#isPrefixRegistered(java.lang.String)
     */
    public boolean isPrefixRegistered(String prefix) {
        boolean registered = false;
        if (entityProviderManager.getProviderByPrefix(prefix) != null) {
            registered = true;
        }
        return registered;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#getRegisteredPrefixes()
     */
    public Set<String> getRegisteredPrefixes() {
        Set<String> prefixes = entityProviderManager.getRegisteredPrefixes();
        return prefixes;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#parseReference(java.lang.String)
     */
    public EntityReference parseReference(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        return ref;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#fireEvent(java.lang.String, java.lang.String)
     */
    public void fireEvent(String eventName, String reference) {
        if (eventName == null || "".equals(eventName)) {
            throw new IllegalArgumentException("Cannot fire event if name is null or empty");
        }
        if (reference == null || "".equals(reference)) {
            throw new IllegalArgumentException("Cannot fire event if reference is null or empty");
        }
        if (entityBrokerManager.getExternalIntegrationProvider() != null) {
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
                log.warn("Invalid reference ({}) for eventName ({}), could not parse the reference correctly, continuing to create event with original reference", reference, eventName);
            }
            // had to take out the exists check because it makes firing events for removing entities very annoying -AZ
            entityBrokerManager.getExternalIntegrationProvider().fireEvent(eventName, refName);
        } else {
            log.warn("No external system to handle events: event not fired: {}:{}", eventName, reference);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#fireEntityRequest(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.lang.Object)
     */
    public EntityResponse fireEntityRequest(String reference, String viewKey, String format,
            Map<String, String> params, Object entity) {
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            return entityBrokerManager.getEntityRESTProvider().handleEntityRequest(reference, viewKey, format, params, entity);
        } else {
            throw new UnsupportedOperationException("No provider to handle fireEntityRequest for ("+reference+","+viewKey+","+format+")");
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#fetchEntity(java.lang.String)
     */
    public Object fetchEntity(String reference) {
        Object entity = null;
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            // not handled in EB so attempt to parse out a prefix and try to get entity from the external system
            if (entityBrokerManager.getExternalIntegrationProvider() != null) {
                entityBrokerManager.getExternalIntegrationProvider().fetchEntity(reference);
            }
        } else {
            // this is a registered prefix
            entity = entityBrokerManager.fetchEntity(ref);
        }
        return entity;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#getEntity(java.lang.String)
     */
    public EntityData getEntity(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        return entityBrokerManager.getEntityData(ref);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybus.EntityBroker#fetchEntities(java.lang.String, org.sakaiproject.entitybus.entityprovider.search.Search, java.util.Map)
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
     * @see org.sakaiproject.entitybus.EntityBroker#getEntities(java.lang.String, org.sakaiproject.entitybus.entityprovider.search.Search, java.util.Map)
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
     * @see org.sakaiproject.entitybus.EntityBroker#browseEntities(java.lang.String, org.sakaiproject.entitybus.entityprovider.search.Search, java.lang.String, java.lang.String, java.util.Map)
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
     * @see org.sakaiproject.entitybus.EntityBroker#getBrowseableEntities(java.lang.String)
     */
    public List<BrowseEntity> getBrowseableEntities(String parentPrefix) {
        List<BrowseEntity> l = entityBrokerManager.getBrowseableEntities(parentPrefix);
        return l;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.LearningTrackingProvider#registerStatement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Float)
     */
    public void registerStatement(String prefix, String actorEmail, String verbStr, String objectURI, Boolean resultSuccess, Float resultScaledScore) {
        externalIntegrationProvider.registerStatement(prefix, actorEmail, verbStr, objectURI, resultSuccess, resultScaledScore);
    }


    public void formatAndOutputEntity(String reference, String format, List<?> entities,
            OutputStream output, Map<String, Object> params) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
        }
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            try {
                requestStorage.setRequestValues(params);
                // convert entities to entity data list
                List<EntityData> data = entityBrokerManager.convertToEntityData(entities, ref);
                if (params == null) { params = new HashMap<String, Object>(); }
                entityBrokerManager.getEntityRESTProvider().formatAndOutputEntity(ref, format, data, output, params);
            } finally {
                requestStorage.reset();
            }
        } else {
            throw new UnsupportedOperationException("No provider to handle formatAndOutputEntity for ("+reference+","+format+")");
        }
    }

    public Object translateInputToEntity(String reference, String format, InputStream input,
            Map<String, Object> params) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
        }
        Object entity = null;
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            try {
                requestStorage.setRequestValues(params);
                if (params == null) { params = new HashMap<String, Object>(); }
                entity = entityBrokerManager.getEntityRESTProvider().translateInputToEntity(ref, format, input, params);
            } finally {
                requestStorage.reset();
            }
        } else {
            throw new UnsupportedOperationException("No provider to handle translateInputToEntity for ("+reference+","+format+")");
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
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            try {
                requestStorage.setRequestValues(params);
                if (params == null) { params = new HashMap<String, Object>(0); }
                EntityView view = entityBrokerManager.parseEntityURL(reference);
                ar = entityBrokerManager.getEntityRESTProvider().handleCustomActionExecution(actionProvider, ref, action, params, outputStream, view, null);
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
        } else {
            throw new UnsupportedOperationException("No provider to handle executeCustomAction for ("+reference+","+action+")");
        }
        return ar;
    }


    // PROPERTIES

    public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
            boolean exactMatch) {
        if (propertiesProvider != null) {
            return propertiesProvider.findEntityRefs(prefixes, name, searchValue, exactMatch);
        } else {
            log.warn("No propertiesProvider defined");
            return new ArrayList<String>();
        }
    }

    public Map<String, String> getProperties(String reference) {
        if (propertiesProvider != null) {
            return propertiesProvider.getProperties(reference);
        } else {
            log.warn("No propertiesProvider defined");
            return new HashMap<String, String>(0);
        }
    }

    public String getPropertyValue(String reference, String name) {
        if (propertiesProvider != null) {
            return propertiesProvider.getPropertyValue(reference, name);
        } else {
            log.warn("No propertiesProvider defined");
            return null;
        }
    }

    public void setPropertyValue(String reference, String name, String value) {
        if (propertiesProvider != null) {
            propertiesProvider.setPropertyValue(reference, name, value);
        } else {
            log.warn("No propertiesProvider defined");
        }
    }


    // TAGS

    public List<EntityData> findEntitesByTags(String[] tags, String[] prefixes,
            boolean matchAll, Search search, Map<String, Object> params) {
        if (tagSearchService != null) {
            requestStorage.setRequestValues(params);
            List<EntityData> results = tagSearchService.findEntitesByTags(tags, prefixes, matchAll, search);
            requestStorage.reset();
            return results;
        } else {
            log.warn("No tagSearchService defined");
            return new ArrayList<EntityData>();
        }
    }

    public List<String> getTagsForEntity(String reference) {
        if (tagSearchService != null) {
            return tagSearchService.getTagsForEntity(reference);
        } else {
            log.warn("No tagSearchService defined");
            return new ArrayList<String>();
        }
    }

    public void removeTagsFromEntity(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.removeTagsFromEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    public void addTagsToEntity(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.addTagsToEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    public void setTagsForEntity(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.setTagsForEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    /**
     * @deprecated use {@link #getTagsForEntity(String)}
     */
    public Set<String> getTags(String reference) {
        if (tagSearchService != null) {
            return new HashSet<String>( tagSearchService.getTagsForEntity(reference) );
        } else {
            log.warn("No tagSearchService defined");
            return new HashSet<String>();
        }
    }

    /**
     * @deprecated use {@link #setTagsForEntity(String, String[])}
     */
    public void setTags(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.setTagsForEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    /**
     * @deprecated use {@link #findEntitesByTags(String[], String[], boolean, Search)}
     */
    public List<String> findEntityRefsByTags(String[] tags) {
        if (tagSearchService != null) {
            ArrayList<String> refs = new ArrayList<String>();
            List<EntityData> results = tagSearchService.findEntitesByTags(tags, null, false, null);
            for (EntityData entitySearchResult : results) {
                refs.add( entitySearchResult.getEntityReference() );
            }
            return refs;
        } else {
            log.warn("No tagSearchService defined");
            return new ArrayList<String>();
        }
    }

    // SEARCH methods
    
    public boolean add(String reference, SearchContent content) {
        if (searchProvider != null) {
            return searchProvider.add(reference, content);
        }
        log.warn("No searchProvider defined");
        return false;
    }

    public boolean remove(String reference) {
        if (searchProvider != null) {
            return searchProvider.remove(reference);
        }
        log.warn("No searchProvider defined");
        return false;
    }

    public SearchResults search(QuerySearch query) {
        if (searchProvider != null) {
            return searchProvider.search(query);
        }
        log.warn("No searchProvider defined");
        return null;
    }

    public void resetSearchIndexes(String context) {
        if (searchProvider != null) {
            searchProvider.resetSearchIndexes(context);
        }
    }

}
