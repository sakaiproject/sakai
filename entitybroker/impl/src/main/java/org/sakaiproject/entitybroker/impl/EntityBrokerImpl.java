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

import lombok.Setter;
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

    protected EntityBrokerImpl() { }

    public EntityBrokerImpl(EntityProviderManager entityProviderManager,
            EntityBrokerManagerImpl entityBrokerManager,
            RequestStorageWrite requestStorageWrite) {
        super();
        this.entityProviderManager = entityProviderManager;
        this.entityBrokerManager = entityBrokerManager;
        this.requestStorage = requestStorageWrite;
    }

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

    @Setter private EntityProviderManager entityProviderManager;
    @Setter private EntityBrokerManagerImpl entityBrokerManager;
    @Setter private RequestStorageWrite requestStorage;
    @Setter private ExternalIntegrationProvider externalIntegrationProvider;
    @Setter private PropertiesProvider propertiesProvider; // OPTIONAL Data Storage providers
    @Setter private TagSearchService tagSearchService;
    @Setter private SearchProvider searchProvider;

    @Override
    public boolean entityExists(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        boolean exists = entityBrokerManager.entityExists(ref);
        return exists;
    }

    @Override
    public String getEntityURL(String reference) {
        return entityBrokerManager.getEntityURL(reference, null, null);
    }

    @Override
    public String getEntityURL(String reference, String viewKey, String extension) {
        return entityBrokerManager.getEntityURL(reference, viewKey, extension);
    }

    @Override
    public EntityView getEntityView(String reference, String viewKey, String extension) {
        EntityReference ref = parseReference(reference);
        return entityBrokerManager.makeEntityView(ref, viewKey, extension);
    }

    @Override
    public boolean isPrefixRegistered(String prefix) {
        return entityProviderManager.getProviderByPrefix(prefix) != null;
    }

    @Override
    public Set<String> getRegisteredPrefixes() {
        return entityProviderManager.getRegisteredPrefixes();
    }

    @Override
    public EntityReference parseReference(String reference) {
        return entityBrokerManager.parseReference(reference);
    }

    @Override
    public void fireEvent(String eventName, String reference) {
        if (eventName == null || eventName.isEmpty()) {
            throw new IllegalArgumentException("Cannot fire event if name is null or empty");
        }
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException("Cannot fire event if reference is null or empty");
        }
        if (entityBrokerManager.getExternalIntegrationProvider() != null) {
            String refName;
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

    @Override
    public EntityResponse fireEntityRequest(String reference, String viewKey, String format, Map<String, String> params, Object entity) {
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            return entityBrokerManager.getEntityRESTProvider().handleEntityRequest(reference, viewKey, format, params, entity);
        } else {
            throw new UnsupportedOperationException("No provider to handle fireEntityRequest for ("+reference+","+viewKey+","+format+")");
        }
    }

    @Override
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

    @Override
    public EntityData getEntity(String reference) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        return entityBrokerManager.getEntityData(ref);
    }

    @Override
    public List<?> fetchEntities(String prefix, Search search, Map<String, Object> params) {
        EntityReference ref = new EntityReference(prefix, "");
        List<?> l;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<>(); }
            l = entityBrokerManager.fetchEntities(ref, search, params);
        } finally {
            requestStorage.reset();
        }
        return l;
    }

    @Override
    public List<EntityData> getEntities(String prefix, Search search, Map<String, Object> params) {
        EntityReference ref = new EntityReference(prefix, "");
        List<EntityData> data;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<>(); }
            data = entityBrokerManager.getEntitiesData(ref, search, params);
        } finally {
            requestStorage.reset();
        }
        return data;
    }

    @Override
    public List<EntityData> browseEntities(String prefix, Search search,
            String userReference, String associatedReference, String parentReference, Map<String, Object> params) {
        EntityReference parentRef = null;
        if (parentReference != null) {
            parentRef = entityBrokerManager.parseReference(parentReference);
        }
        List<EntityData> data;
        try {
            requestStorage.setRequestValues(params);
            if (params == null) { params = new HashMap<>(); }
            data = entityBrokerManager.browseEntities(prefix, search, userReference, associatedReference, parentRef, params);
        } finally {
            requestStorage.reset();
        }
        return data;
    }

    @Override
    public List<BrowseEntity> getBrowseableEntities(String parentPrefix) {
        return entityBrokerManager.getBrowseableEntities(parentPrefix);
    }


    @Override
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
                if (params == null) params = new HashMap<>();
                entityBrokerManager.getEntityRESTProvider().formatAndOutputEntity(ref, format, data, output, params);
            } finally {
                requestStorage.reset();
            }
        } else {
            throw new UnsupportedOperationException("No provider to handle formatAndOutputEntity for ("+reference+","+format+")");
        }
    }

    @Override
    public Object translateInputToEntity(String reference, String format, InputStream input,
            Map<String, Object> params) {
        EntityReference ref = entityBrokerManager.parseReference(reference);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
        }
        Object entity;
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            try {
                requestStorage.setRequestValues(params);
                if (params == null) { params = new HashMap<>(); }
                entity = entityBrokerManager.getEntityRESTProvider().translateInputToEntity(ref, format, input, params);
            } finally {
                requestStorage.reset();
            }
        } else {
            throw new UnsupportedOperationException("No provider to handle translateInputToEntity for ("+reference+","+format+")");
        }
        return entity;
    }

    @Override
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
        ActionReturn ar;
        if (entityBrokerManager.getEntityRESTProvider() != null) {
            try {
                requestStorage.setRequestValues(params);
                if (params == null) { params = new HashMap<>(0); }
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

    @Override
    public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
            boolean exactMatch) {
        if (propertiesProvider != null) {
            return propertiesProvider.findEntityRefs(prefixes, name, searchValue, exactMatch);
        } else {
            log.warn("No propertiesProvider defined");
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, String> getProperties(String reference) {
        if (propertiesProvider != null) {
            return propertiesProvider.getProperties(reference);
        } else {
            log.warn("No propertiesProvider defined");
            return new HashMap<>(0);
        }
    }

    @Override
    public String getPropertyValue(String reference, String name) {
        if (propertiesProvider != null) {
            return propertiesProvider.getPropertyValue(reference, name);
        } else {
            log.warn("No propertiesProvider defined");
            return null;
        }
    }

    @Override
    public void setPropertyValue(String reference, String name, String value) {
        if (propertiesProvider != null) {
            propertiesProvider.setPropertyValue(reference, name, value);
        } else {
            log.warn("No propertiesProvider defined");
        }
    }

    @Override
    public List<EntityData> findEntitesByTags(String[] tags, String[] prefixes,
            boolean matchAll, Search search, Map<String, Object> params) {
        if (tagSearchService != null) {
            requestStorage.setRequestValues(params);
            List<EntityData> results = tagSearchService.findEntitesByTags(tags, prefixes, matchAll, search);
            requestStorage.reset();
            return results;
        } else {
            log.warn("No tagSearchService defined");
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getTagsForEntity(String reference) {
        if (tagSearchService != null) {
            return tagSearchService.getTagsForEntity(reference);
        } else {
            log.warn("No tagSearchService defined");
            return new ArrayList<>();
        }
    }

    @Override
    public void removeTagsFromEntity(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.removeTagsFromEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    @Override
    public void addTagsToEntity(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.addTagsToEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    @Override
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
    @Deprecated
    public Set<String> getTags(String reference) {
        if (tagSearchService != null) {
            return new HashSet<>(tagSearchService.getTagsForEntity(reference));
        } else {
            log.warn("No tagSearchService defined");
            return new HashSet<>();
        }
    }

    /**
     * @deprecated use {@link #setTagsForEntity(String, String[])}
     */
    @Deprecated
    public void setTags(String reference, String[] tags) {
        if (tagSearchService != null) {
            tagSearchService.setTagsForEntity(reference, tags);
        } else {
            log.warn("No tagSearchService defined");
        }
    }

    @Deprecated
    @Override
    public List<String> findEntityRefsByTags(String[] tags) {
        if (tagSearchService != null) {
            ArrayList<String> refs = new ArrayList<>();
            List<EntityData> results = tagSearchService.findEntitesByTags(tags, null, false, null);
            for (EntityData entitySearchResult : results) {
                refs.add( entitySearchResult.getEntityReference() );
            }
            return refs;
        } else {
            log.warn("No tagSearchService defined");
            return new ArrayList<>();
        }
    }

    @Override
    public boolean add(String reference, SearchContent content) {
        if (searchProvider != null) {
            return searchProvider.add(reference, content);
        }
        log.warn("No searchProvider defined");
        return false;
    }

    @Override
    public boolean remove(String reference) {
        if (searchProvider != null) {
            return searchProvider.remove(reference);
        }
        log.warn("No searchProvider defined");
        return false;
    }

    @Override
    public SearchResults search(QuerySearch query) {
        if (searchProvider != null) {
            return searchProvider.search(query);
        }
        log.warn("No searchProvider defined");
        return null;
    }

    @Override
    public void resetSearchIndexes(String context) {
        if (searchProvider != null) {
            searchProvider.resetSearchIndexes(context);
        }
    }

}
