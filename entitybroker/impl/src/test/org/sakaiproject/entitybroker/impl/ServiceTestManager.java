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

package org.sakaiproject.entitybroker.impl;

import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.util.core.EntityProviderMethodStoreImpl;
import org.sakaiproject.entitybroker.util.external.ExternalIntegrationProviderMock;


/**
 * This creates all the needed services (as if it were the component manager),
 * this will let us create the objects we need without too much confusion and ensure
 * we are using the same ones
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("deprecation")
public class ServiceTestManager {

    private static ServiceTestManager instance;
    public static ServiceTestManager getInstance() {
        if (instance == null) {
            instance = new ServiceTestManager( new TestData(), null );
        }
        return instance;
    }
    public static void setInstance(ServiceTestManager sts) {
        instance = sts;
    }

    private EntityBrokerCoreServiceManager entityBrokerCoreServiceManager;
    public EntityBrokerCoreServiceManager getEntityBrokerCoreServiceManager() {
        return entityBrokerCoreServiceManager;
    }

    public RequestStorageWrite requestStorage;
    public RequestGetterWrite requestGetter;
    public EntityProviderMethodStoreImpl entityProviderMethodStore;
    public EntityPropertiesService entityPropertiesService;
    public EntityProviderManagerImpl entityProviderManager;
    public EntityBrokerManagerImpl entityBrokerManager;
    public HttpServletAccessProviderManager httpServletAccessProviderManager;
    public EntityViewAccessProviderManager entityViewAccessProviderManager;
    public EntityMetaPropertiesService entityMetaPropertiesService;
    public EntityTaggingService entityTaggingService;
    public ExternalIntegrationProvider externalIntegrationProvider;

    public TestData td;
    public TestData getTestData() {
        return td;
    }

    public ServiceTestManager(TestData td) {
        this(td, null);
    }

    public ServiceTestManager(TestData td, EntityBrokerDao dao) {
        this.td = td;
        this.externalIntegrationProvider = new ExternalIntegrationProviderMock();
        this.entityBrokerCoreServiceManager = new EntityBrokerCoreServiceManager(dao, true);

        // init the variables for the getters
        this.requestGetter = this.entityBrokerCoreServiceManager.getRequestGetter();
        this.entityPropertiesService = this.entityBrokerCoreServiceManager.getEntityPropertiesService();
        this.httpServletAccessProviderManager = this.entityBrokerCoreServiceManager.getHttpServletAccessProviderManager();
        this.entityViewAccessProviderManager = this.entityBrokerCoreServiceManager.getEntityViewAccessProviderManager();
        this.entityProviderMethodStore = (EntityProviderMethodStoreImpl) this.entityBrokerCoreServiceManager.getEntityProviderMethodStore();
        this.requestStorage = this.entityBrokerCoreServiceManager.getRequestStorage();
        this.entityProviderManager = (EntityProviderManagerImpl) this.entityBrokerCoreServiceManager.getEntityProviderManager();
        this.entityBrokerManager = (EntityBrokerManagerImpl) this.entityBrokerCoreServiceManager.getEntityBrokerManager();
        this.entityBrokerManager.setExternalIntegrationProvider(this.externalIntegrationProvider);
        this.entityMetaPropertiesService = this.entityBrokerCoreServiceManager.getEntityMetaPropertiesService();
        this.entityTaggingService = this.entityBrokerCoreServiceManager.getEntityTaggingService();

        initTestProviders(this.entityProviderManager, this.td);
        setInstance(this);
    }

    public void initTestProviders(EntityProviderManager entityProviderManager, TestData td) {
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
    }
}
