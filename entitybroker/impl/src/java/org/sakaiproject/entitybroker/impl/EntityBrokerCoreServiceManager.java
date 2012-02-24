/**
 * $Id$
 * $URL$
 * EntityBrokerCoreServiceManager.java - entity-broker - Jan 14, 2009 5:59:42 PM - azeckoski
 **********************************************************************************
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.impl;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.util.access.EntityViewAccessProviderManagerImpl;
import org.sakaiproject.entitybroker.util.access.HttpServletAccessProviderManagerImpl;
import org.sakaiproject.entitybroker.util.core.EntityPropertiesServiceSimple;
import org.sakaiproject.entitybroker.util.core.EntityProviderMethodStoreImpl;
import org.sakaiproject.entitybroker.util.request.RequestGetterImpl;
import org.sakaiproject.entitybroker.util.request.RequestStorageImpl;
import org.sakaiproject.entitybroker.util.spring.EntityPropertiesServiceSpringImpl;

/**
 * This allows easy startup of the core entitybroker services in a way which avoids the developer
 * having to know anything about it, anyone who wants to startup the entitybroker core would create
 * an instance of this class
 * Note that the {@link DeveloperHelperService} has to be started separately
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("deprecation")
public class EntityBrokerCoreServiceManager {

    private static volatile EntityBrokerCoreServiceManager instance;
    public static EntityBrokerCoreServiceManager getInstance() {
        if (instance == null) {
            instance = new EntityBrokerCoreServiceManager();
        }
        return instance;
    }
    public static void setInstance(EntityBrokerCoreServiceManager sm) {
        instance = sm;
    }

    private EntityBrokerDao dao;
    private HttpServletAccessProviderManagerImpl httpServletAccessProviderManager;

    private RequestStorageImpl requestStorage;
    private RequestGetterImpl requestGetter;
    private EntityProviderMethodStoreImpl entityProviderMethodStore;
    private EntityPropertiesService entityPropertiesService;
    private EntityProviderManagerImpl entityProviderManager;
    private EntityBrokerManagerImpl entityBrokerManager;
    private EntityViewAccessProviderManagerImpl entityViewAccessProviderManager;
    private EntityBrokerImpl entityBroker;

    private EntityMetaPropertiesService entityMetaPropertiesService;
    private EntityTaggingService entityTaggingService;

    /**
     * Create the core services,
     * they can be accessed using the getters on this class
     */
    public EntityBrokerCoreServiceManager() {
        this(null, false);
    }

    /**
     * Create the core services,
     * set the optional dao so that the storage parts of EB work,
     * enable the classes that depend on spring
     * @param dao (optional) the dao which is used to write storage, without this the 
     * internal entityMetaPropertiesService and entityTaggingService will be null
     * @param useSpringBasedServices if true then spring impls will be used (must have spring classes in path)
     */
    public EntityBrokerCoreServiceManager(EntityBrokerDao dao, boolean useSpringBasedServices) {
        this.dao = dao;
        this.useSpringBasedServices = useSpringBasedServices;
        init();
        setInstance(this);
    }

    private boolean useSpringBasedServices = false;
    /**
     * WARNING: If you use the non-empty constructors to make this object then do not run this,
     * it has already been run and should not be run a second time <br/>
     * Startup all the Core services for the EB system,
     * this can only be run after this is constructed with a full constructor or 
     * the {@link #setEntityBrokerManager(EntityBrokerManager)} method has been called
     * (i.e. all the required services are set)
     */
    public void init() {
        // initialize all the parts
        this.requestGetter = new RequestGetterImpl();
        if (this.useSpringBasedServices) {
            this.entityPropertiesService = new EntityPropertiesServiceSpringImpl();
        } else {
            this.entityPropertiesService = new EntityPropertiesServiceSimple();
        }
        this.httpServletAccessProviderManager = new HttpServletAccessProviderManagerImpl();
        this.entityViewAccessProviderManager = new EntityViewAccessProviderManagerImpl();
        this.entityProviderMethodStore = new EntityProviderMethodStoreImpl();
        this.requestStorage = new RequestStorageImpl(requestGetter);
        this.entityProviderManager = new EntityProviderManagerImpl(requestStorage, requestGetter, entityPropertiesService, entityProviderMethodStore);
        this.entityBrokerManager = new EntityBrokerManagerImpl(entityProviderManager, entityPropertiesService, entityViewAccessProviderManager);
        this.entityBroker = new EntityBrokerImpl(entityProviderManager, entityBrokerManager, requestStorage);

        // optional DB dependent pieces
        if (dao != null) {
            this.entityMetaPropertiesService = new EntityMetaPropertiesService();
            this.entityMetaPropertiesService.setDao(dao);
            this.entityMetaPropertiesService.setEntityBrokerManager(entityBrokerManager);
            this.entityMetaPropertiesService.setEntityProviderManager(entityProviderManager);

            this.entityTaggingService = new EntityTaggingService();
            this.entityTaggingService.setDao(dao);
            this.entityTaggingService.setEntityBrokerManager(entityBrokerManager);
            this.entityTaggingService.setEntityProviderManager(entityProviderManager);
        }
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
        // cleanup everything
        setInstance(null);
        this.entityTaggingService = null;
        this.entityMetaPropertiesService = null;
        this.entityBroker = null;
        this.entityBrokerManager = null;
        this.entityProviderManager = null;
        this.requestStorage.reset();
        this.requestStorage = null;
        this.entityProviderMethodStore = null;
        this.entityViewAccessProviderManager = null;
        this.httpServletAccessProviderManager = null;
        this.entityPropertiesService = null;
        this.requestGetter.destroy();
        this.requestGetter = null;
        // DAO shutdown?
        this.dao = null;
    }

    // GETTERS

    public EntityBrokerDao getDao() {
        return dao;
    }

    public EntityBroker getEntityBroker() {
        return entityBroker;
    }

    public HttpServletAccessProviderManager getHttpServletAccessProviderManager() {
        return httpServletAccessProviderManager;
    }

    public RequestStorageWrite getRequestStorage() {
        return requestStorage;
    }

    public RequestGetterWrite getRequestGetter() {
        return requestGetter;
    }

    public EntityProviderMethodStore getEntityProviderMethodStore() {
        return entityProviderMethodStore;
    }

    public EntityPropertiesService getEntityPropertiesService() {
        return entityPropertiesService;
    }

    public EntityProviderManager getEntityProviderManager() {
        return entityProviderManager;
    }

    public EntityBrokerManager getEntityBrokerManager() {
        return entityBrokerManager;
    }

    public EntityViewAccessProviderManager getEntityViewAccessProviderManager() {
        return entityViewAccessProviderManager;
    }

    public EntityMetaPropertiesService getEntityMetaPropertiesService() {
        return entityMetaPropertiesService;
    }

    public EntityTaggingService getEntityTaggingService() {
        return entityTaggingService;
    }


    // setters to allow setting provided services

    public void setDao(EntityBrokerDao dao) {
        this.dao = dao;
    }

    public void setEntityMetaPropertiesService(EntityMetaPropertiesService entityMetaPropertiesService) {
        this.entityMetaPropertiesService = entityMetaPropertiesService;
    }

    public void setEntityTaggingService(EntityTaggingService entityTaggingService) {
        this.entityTaggingService = entityTaggingService;
    }

}
