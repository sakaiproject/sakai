/**
 * $Id$
 * $URL$
 * TestManager.java - entity-broker - Jul 23, 2008 6:27:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.entitybroker.rest;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;

import lombok.extern.slf4j.Slf4j;


/**
 * This creates all the needed services (as if it were a service manager),
 * this will let us create the services we need without too much confusion and ensure
 * we are using the same ones <br/>
 * If this is used then the services should not be created in some other way but should be
 * initiated here and then exported from this class only
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
@SuppressWarnings("deprecation")
public class EntityBrokerRESTServiceManager {

    private static volatile EntityBrokerRESTServiceManager instance;
    public static EntityBrokerRESTServiceManager getInstance() {
        if (instance == null) {
            instance = new EntityBrokerRESTServiceManager();
        }
        return instance;
    }
    public static void setInstance(EntityBrokerRESTServiceManager ersm) {
        instance = ersm;
    }

    // services we need
    private RequestStorageWrite requestStorage;
    private RequestGetterWrite requestGetter;
    private EntityPropertiesService entityPropertiesService;
    private EntityBrokerManager entityBrokerManager;
    private EntityProviderManager entityProviderManager;
    private EntityProviderMethodStore entityProviderMethodStore;
    private HttpServletAccessProviderManager httpServletAccessProviderManager;
    private EntityViewAccessProviderManager entityViewAccessProviderManager;
    private ExternalIntegrationProvider externalIntegrationProvider;

    // services we are starting up
    private EntityActionsManager entityActionsManager;
    private EntityDescriptionManager entityDescriptionManager;
    private EntityEncodingManager entityEncodingManager;
    private EntityRedirectsManager entityRedirectsManager;
    private EntityHandlerImpl entityRequestHandler;
    private EntityBatchHandler entityBatchHandler;
    private EntityRESTProviderBase entityRESTProvider;

    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
        this.requestStorage = entityBrokerManager.getRequestStorage();
        this.requestGetter = entityBrokerManager.getRequestGetter();
        this.entityPropertiesService = entityBrokerManager.getEntityPropertiesService();
        this.entityProviderManager = entityBrokerManager.getEntityProviderManager();
        this.entityProviderMethodStore = entityBrokerManager.getEntityProviderMethodStore();
        this.entityViewAccessProviderManager = entityBrokerManager.getEntityViewAccessProviderManager();
        this.externalIntegrationProvider = entityBrokerManager.getExternalIntegrationProvider();
    }

    protected EntityBrokerRESTServiceManager() { }

    /**
     * Base constructor
     * @param entityBrokerManager the main entity broker manager service
     */
    public EntityBrokerRESTServiceManager(EntityBrokerManager entityBrokerManager) {
        this(entityBrokerManager, null);
    }

    /**
     * Full constructor
     * @param entityBrokerManager the main entity broker manager service
     * @param httpServletAccessProviderManager (optional)
     */
    public EntityBrokerRESTServiceManager(EntityBrokerManager entityBrokerManager,
            HttpServletAccessProviderManager httpServletAccessProviderManager) {
        super();
        if (entityBrokerManager == null) {
            throw new IllegalArgumentException("entityBrokerManager cannot be null");
        }
        setEntityBrokerManager(entityBrokerManager);
        this.httpServletAccessProviderManager = httpServletAccessProviderManager;
        init();
    }

    /**
     * WARNING: If you use the non-empty constructors to make this object then do not run this,
     * it has already been run and should not be run a second time <br/>
     * Startup all the REST services for the EB system,
     * this can only be run after this is constructed with a full constructor or 
     * the {@link #setEntityBrokerManager(EntityBrokerManager)} method has been called
     * (i.e. all the required services are set)
     */
    public void init() {
        log.info("EntityBrokerRESTServiceManager: init()");
        if (this.entityBrokerManager == null
                || this.requestGetter == null
                || this.requestStorage == null
                || this.entityPropertiesService == null
                || this.entityProviderManager == null
                || this.entityProviderMethodStore == null
                || this.externalIntegrationProvider == null
                || this.entityViewAccessProviderManager == null) {
            throw new IllegalArgumentException("Main services must all be set and non-null!");
        }
        // initialize all the parts
        entityActionsManager = new EntityActionsManager(entityProviderMethodStore);
        entityRedirectsManager = new EntityRedirectsManager(entityBrokerManager, 
                entityProviderMethodStore, requestStorage);

        entityDescriptionManager = new EntityDescriptionManager(entityViewAccessProviderManager,
                httpServletAccessProviderManager, entityProviderManager, entityPropertiesService,
                entityBrokerManager, entityProviderMethodStore);
        entityEncodingManager = new EntityEncodingManager(entityProviderManager, entityBrokerManager);
        entityBatchHandler = new EntityBatchHandler(entityBrokerManager, entityEncodingManager, externalIntegrationProvider);

        entityRequestHandler = new EntityHandlerImpl(entityProviderManager,
                entityBrokerManager, entityEncodingManager, entityDescriptionManager,
                entityViewAccessProviderManager, requestGetter, entityActionsManager,
                entityRedirectsManager, entityBatchHandler, requestStorage);
        entityRequestHandler.setAccessProviderManager( httpServletAccessProviderManager );

        entityRESTProvider = new EntityRESTProviderBase(entityBrokerManager, 
                entityActionsManager, entityEncodingManager, entityRequestHandler);

        setInstance(this);
    }

    /**
     * Shutdown the services
     * (just calls over to destroy)
     */
    public void shutdown() {
        destroy();
    }

    /**
     * Shuts down all services and cleans up
     */
    public void destroy() {
        log.info("EntityBrokerRESTServiceManager: destroy()");
        // cleanup everything
        setInstance(null);
        this.entityRESTProvider.destroy();
        this.entityRESTProvider = null;
        this.entityRequestHandler = null;
        this.entityBatchHandler.destroy();
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
    
    public EntityDescriptionManager getEntityDescriptionManager() {
        return entityDescriptionManager;
    }
    
    public EntityEncodingManager getEntityEncodingManager() {
        return entityEncodingManager;
    }
    
    public EntityRedirectsManager getEntityRedirectsManager() {
        return entityRedirectsManager;
    }
    
    public EntityHandlerImpl getEntityRequestHandler() {
        return entityRequestHandler;
    }
    
    public EntityBatchHandler getEntityBatchHandler() {
        return entityBatchHandler;
    }
    
    public EntityRESTProviderBase getEntityRESTProvider() {
        return entityRESTProvider;
    }

}
