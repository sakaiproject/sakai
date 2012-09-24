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

import org.sakaiproject.entitybroker.impl.EntityBrokerImpl;
import org.sakaiproject.entitybroker.impl.EntityBrokerManagerImpl;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;

import org.sakaiproject.entitybroker.mocks.EntityViewAccessProviderManagerMock;
import org.sakaiproject.entitybroker.mocks.HttpServletAccessProviderManagerMock;
import org.sakaiproject.entitybroker.mocks.data.TestData;

import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.rest.EntityActionsManager;
import org.sakaiproject.entitybroker.rest.EntityBatchHandler;
import org.sakaiproject.entitybroker.rest.EntityDescriptionManager;
import org.sakaiproject.entitybroker.rest.EntityEncodingManager;
import org.sakaiproject.entitybroker.rest.EntityHandlerImpl;
import org.sakaiproject.entitybroker.rest.EntityRedirectsManager;

import org.sakaiproject.entitybroker.util.core.EntityPropertiesServiceSimple;
import org.sakaiproject.entitybroker.util.core.EntityProviderMethodStoreImpl;
import org.sakaiproject.entitybroker.util.external.ExternalIntegrationProviderMock;
import org.sakaiproject.entitybroker.util.request.RequestGetterImpl;
import org.sakaiproject.entitybroker.util.request.RequestStorageImpl;


/**
 * This creates all the needed services (as if it were the component manager),
 * this will let us create the objects we need without too much confusion and ensure
 * we are using the same ones
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ServiceTestManager {

    private static volatile ServiceTestManager instance;
    public static ServiceTestManager getInstance() {
        if (instance == null) {
            instance = new ServiceTestManager( new TestData() );
        }
        return instance;
    }
    public static void setInstance(ServiceTestManager sts) {
        instance = sts;
    }

    private EntityBrokerRESTServiceManager entityBrokerRESTServiceManager;
    public EntityBrokerRESTServiceManager getEntityBrokerRESTServiceManager() {
        return entityBrokerRESTServiceManager;
    }

    private HttpServletAccessProviderManagerMock httpServletAccessProviderManager;
    private EntityViewAccessProviderManagerMock entityViewAccessProviderManager;
    private RequestStorageImpl requestStorage;
    private RequestGetterImpl requestGetter;
    private EntityProviderMethodStoreImpl entityProviderMethodStore;
    private EntityPropertiesService entityPropertiesService;
    private EntityProviderManagerImpl entityProviderManager;

    public EntityBrokerManagerImpl entityBrokerManager;
    public EntityBrokerImpl entityBroker;
    public ExternalIntegrationProvider externalIntegrationProvider;

    public EntityActionsManager entityActionsManager;
    public EntityEncodingManager entityEncodingManager;
    public EntityRedirectsManager entityRedirectsManager;
    public EntityBatchHandler entityBatchHandler;
    public EntityDescriptionManager entityDescriptionManager;
    public EntityHandlerImpl entityRequestHandler;
    public EntityRESTProviderBase entityRESTProvider;

    public TestData td;
    public TestData getTestData() {
        return td;
    }

    public ServiceTestManager(TestData td) {
        this.td = td;
        initializeCoreServiceMocks();
        initializeRESTServices();
        setInstance(this);
    }

    public void initializeRESTServices() {
        this.entityBrokerRESTServiceManager = new EntityBrokerRESTServiceManager(this.entityBrokerManager, this.httpServletAccessProviderManager);
        // get out the services from the service manager
        this.entityActionsManager = this.entityBrokerRESTServiceManager.getEntityActionsManager();
        this.entityBatchHandler = this.entityBrokerRESTServiceManager.getEntityBatchHandler();
        this.entityDescriptionManager = this.entityBrokerRESTServiceManager.getEntityDescriptionManager();
        this.entityEncodingManager = this.entityBrokerRESTServiceManager.getEntityEncodingManager();
        this.entityRedirectsManager = this.entityBrokerRESTServiceManager.getEntityRedirectsManager();
        this.entityRequestHandler = this.entityBrokerRESTServiceManager.getEntityRequestHandler();
        this.entityRESTProvider = this.entityBrokerRESTServiceManager.getEntityRESTProvider();
    }

    public void initializeCoreServiceMocks() {
        requestGetter = new RequestGetterImpl();
        externalIntegrationProvider = new ExternalIntegrationProviderMock();
        entityPropertiesService = new EntityPropertiesServiceSimple();
        httpServletAccessProviderManager = new HttpServletAccessProviderManagerMock();
        entityViewAccessProviderManager = new EntityViewAccessProviderManagerMock();
        entityProviderMethodStore = new EntityProviderMethodStoreImpl();

        requestStorage = new RequestStorageImpl(requestGetter);
        entityActionsManager = new EntityActionsManager(entityProviderMethodStore);
        entityRedirectsManager = new EntityRedirectsManager(entityBrokerManager, entityProviderMethodStore, requestStorage);
        entityProviderManager = new EntityProviderManagerImpl(requestStorage, requestGetter, entityPropertiesService, entityProviderMethodStore);

        entityProviderManager.registerEntityProvider(td.entityProvider1);
        entityProviderManager.registerEntityProvider(td.entityProvider1T);
        entityProviderManager.registerEntityProvider(td.entityProvider2);
        entityProviderManager.registerEntityProvider(td.entityProvider3);
        entityProviderManager.registerEntityProvider(td.entityProvider4);
        entityProviderManager.registerEntityProvider(td.entityProvider5);
        entityProviderManager.registerEntityProvider(td.entityProvider6);
        entityProviderManager.registerEntityProvider(td.entityProvider7);
        entityProviderManager.registerEntityProvider(td.entityProvider8);
        entityProviderManager.registerEntityProvider(td.entityProviderA);
        entityProviderManager.registerEntityProvider(td.entityProviderA1);
        entityProviderManager.registerEntityProvider(td.entityProviderA2);
        entityProviderManager.registerEntityProvider(td.entityProviderA3);
        entityProviderManager.registerEntityProvider(td.entityProviderU1);
        entityProviderManager.registerEntityProvider(td.entityProviderU2);
        entityProviderManager.registerEntityProvider(td.entityProviderU3);
        entityProviderManager.registerEntityProvider(td.entityProviderTag);
        entityProviderManager.registerEntityProvider(td.entityProviderB1);
        entityProviderManager.registerEntityProvider(td.entityProviderB2);
        entityProviderManager.registerEntityProvider(td.entityProviderS1);
        // add new providers here

        entityBrokerManager = new EntityBrokerManagerImpl(entityProviderManager, entityPropertiesService, entityViewAccessProviderManager, externalIntegrationProvider);
        entityBroker = new EntityBrokerImpl(entityProviderManager, entityBrokerManager, requestStorage);
    }

}
