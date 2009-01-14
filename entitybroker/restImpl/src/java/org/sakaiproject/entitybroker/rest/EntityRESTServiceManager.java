/**
 * $Id$
 * $URL$
 * TestManager.java - entity-broker - Jul 23, 2008 6:27:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
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

package org.sakaiproject.entitybroker.rest;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;


/**
 * This creates all the needed services (as if it were the component manager),
 * this will let us create the objects we need without too much confusion and ensure
 * we are using the same ones <br/>
 * If this is used then the services should not be created in some other way but should be
 * initiated here and then exported from this class only
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("deprecation")
public class EntityRESTServiceManager {

    private static EntityRESTServiceManager instance;
    public static EntityRESTServiceManager getInstance() {
        if (instance == null) {
            instance = new EntityRESTServiceManager();
        }
        return instance;
    }
    public static void setInstance(EntityRESTServiceManager ersm) {
        instance = ersm;
    }

    // services we need
    private RequestStorageWrite requestStorage;
    private RequestGetter requestGetter;
    private EntityPropertiesService entityPropertiesService;
    private EntityBrokerManager entityBrokerManager;
    private EntityProviderManager entityProviderManager;
    private EntityProviderMethodStore entityProviderMethodStore;
    private HttpServletAccessProviderManager httpServletAccessProviderManager;
    private EntityViewAccessProviderManager entityViewAccessProviderManager;

    // services we are starting up
    private EntityActionsManager entityActionsManager;
    private EntityDescriptionManager entityDescriptionManager;
    private EntityEncodingManager entityEncodingManager;
    private EntityRedirectsManager entityRedirectsManager;
    private EntityHandlerImpl entityRequestHandler;
    private EntityBatchHandler entityBatchHandler;
    private EntityRESTProviderBase entityRESTProvider;

    protected EntityRESTServiceManager() { }

    public EntityRESTServiceManager(RequestStorageWrite requestStorage, RequestGetter requestGetter,
            EntityPropertiesService entityPropertiesService,
            EntityBrokerManager entityBrokerManager, EntityProviderManager entityProviderManager,
            EntityProviderMethodStore entityProviderMethodStore,
            HttpServletAccessProviderManager httpServletAccessProviderManager,
            EntityViewAccessProviderManager entityViewAccessProviderManager) {
        super();
        this.requestStorage = requestStorage;
        this.requestGetter = requestGetter;
        this.entityPropertiesService = entityPropertiesService;
        this.entityBrokerManager = entityBrokerManager;
        this.entityProviderManager = entityProviderManager;
        this.entityProviderMethodStore = entityProviderMethodStore;
        this.httpServletAccessProviderManager = httpServletAccessProviderManager;
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
    }

    public void init() {
        if (this.requestGetter == null
                || this.requestStorage == null
                || this.entityPropertiesService == null
                || this.entityBrokerManager == null
                || this.entityProviderManager == null
                || this.entityProviderMethodStore == null
                || this.httpServletAccessProviderManager == null
                || this.entityViewAccessProviderManager == null) {
            throw new IllegalArgumentException("Main services must all be set and non-null!");
        }
        // initialize all the parts
        entityActionsManager = new EntityActionsManager(entityProviderMethodStore);
        entityRedirectsManager = new EntityRedirectsManager(entityProviderMethodStore, requestStorage);

        entityDescriptionManager = new EntityDescriptionManager(entityViewAccessProviderManager,
                httpServletAccessProviderManager, entityProviderManager, entityPropertiesService,
                entityBrokerManager, entityProviderMethodStore);
        entityEncodingManager = new EntityEncodingManager(entityProviderManager, entityBrokerManager);
        entityBatchHandler = new EntityBatchHandler(entityBrokerManager, entityEncodingManager);

        entityRequestHandler = new EntityHandlerImpl(entityProviderManager,
                entityBrokerManager, entityEncodingManager, entityDescriptionManager,
                entityViewAccessProviderManager, requestGetter, entityActionsManager,
                entityRedirectsManager, entityBatchHandler, requestStorage);
        entityRequestHandler.setAccessProviderManager( httpServletAccessProviderManager );

        entityRESTProvider = new EntityRESTProviderBase(entityBrokerManager, 
                entityActionsManager, entityEncodingManager, entityRequestHandler);

        setInstance(this);
    }

    public void destroy() {
        // cleanup everything
        setInstance(null);
        this.entityRESTProvider.destroy();
        this.entityRESTProvider = null;
        this.entityRequestHandler = null;
        this.entityBatchHandler = null;
        this.entityEncodingManager = null;
        this.entityDescriptionManager.destroy();
        this.entityDescriptionManager = null;
        this.entityRedirectsManager = null;
        this.entityActionsManager = null;
    }

    public EntityActionsManager getEntityActionsManager() {
        return entityActionsManager;
    }
    
    public void setEntityActionsManager(EntityActionsManager entityActionsManager) {
        this.entityActionsManager = entityActionsManager;
    }
    
    public EntityDescriptionManager getEntityDescriptionManager() {
        return entityDescriptionManager;
    }
    
    public void setEntityDescriptionManager(EntityDescriptionManager entityDescriptionManager) {
        this.entityDescriptionManager = entityDescriptionManager;
    }
    
    public EntityEncodingManager getEntityEncodingManager() {
        return entityEncodingManager;
    }
    
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
    }
    
    public EntityRedirectsManager getEntityRedirectsManager() {
        return entityRedirectsManager;
    }
    
    public void setEntityRedirectsManager(EntityRedirectsManager entityRedirectsManager) {
        this.entityRedirectsManager = entityRedirectsManager;
    }
    
    public EntityHandlerImpl getEntityRequestHandler() {
        return entityRequestHandler;
    }
    
    public void setEntityRequestHandler(EntityHandlerImpl entityRequestHandler) {
        this.entityRequestHandler = entityRequestHandler;
    }
    
    public EntityBatchHandler getEntityBatchHandler() {
        return entityBatchHandler;
    }
    
    public void setEntityBatchHandler(EntityBatchHandler entityBatchHandler) {
        this.entityBatchHandler = entityBatchHandler;
    }
    
    public EntityRESTProviderBase getEntityRESTProvider() {
        return entityRESTProvider;
    }
    
    public void setEntityRESTProvider(EntityRESTProviderBase entityRESTProvider) {
        this.entityRESTProvider = entityRESTProvider;
    }

}
