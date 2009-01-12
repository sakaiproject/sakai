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

package org.sakaiproject.entitybroker.impl;

import org.sakaiproject.entitybroker.EntityPropertiesService;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.impl.mocks.FakeServerConfigurationService;
import org.sakaiproject.entitybroker.mocks.EntityViewAccessProviderManagerMock;
import org.sakaiproject.entitybroker.mocks.HttpServletAccessProviderManagerMock;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.util.request.RequestGetterImpl;
import org.sakaiproject.entitybroker.util.request.RequestStorageImpl;
import org.sakaiproject.entitybroker.util.spring.EntityPropertiesServiceSpringImpl;


/**
 * This creates all the needed services (as if it were the component manager),
 * this will let us create the objects we need without too much confusion and ensure
 * we are using the same ones
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
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

    public FakeServerConfigurationService serverConfigurationService;
    public RequestStorageImpl requestStorage;
    public RequestGetterImpl requestGetter;
    public EntityPropertiesService entityPropertiesService;
    public EntityActionsManager entityActionsManager;
    public EntityProviderManagerImpl entityProviderManager;
    public EntityBrokerManager entityBrokerManager;
    public EntityDescriptionManager entityDescriptionManager;
    public EntityEncodingManager entityEncodingManager;
    public EntityRedirectsManager entityRedirectsManager;
    public EntityHandlerImpl entityRequestHandler;
    public HttpServletAccessProviderManagerMock httpServletAccessProviderManager;
    public EntityViewAccessProviderManagerMock entityViewAccessProviderManager;
    public EntityMetaPropertiesService entityMetaPropertiesService;
    public EntityTaggingService entityTaggingService;
    public EntityBatchHandler entityBatchHandler;

    public TestData td;
    public TestData getTestData() {
        return td;
    }

    public ServiceTestManager(TestData td) {
        this(td, null);
    }

    public ServiceTestManager(TestData td, EntityBrokerDao dao) {
        this.td = td;
        // initialize all the parts
        requestGetter = new RequestGetterImpl();
        entityPropertiesService = new EntityPropertiesServiceSpringImpl();
        entityActionsManager = new EntityActionsManager();
        serverConfigurationService = new FakeServerConfigurationService();
        httpServletAccessProviderManager = new HttpServletAccessProviderManagerMock();
        entityViewAccessProviderManager = new EntityViewAccessProviderManagerMock();

        requestStorage = new RequestStorageImpl();
        requestStorage.setRequestGetter(requestGetter);

        entityRedirectsManager = new EntityRedirectsManager();
        entityRedirectsManager.setRequestStorage(requestStorage);

        entityProviderManager = new EntityProviderManagerImpl();
        entityProviderManager.setRequestGetter( requestGetter );
        entityProviderManager.setRequestStorage( requestStorage );
        entityProviderManager.setEntityProperties( entityPropertiesService );
        entityProviderManager.setEntityActionsManager( entityActionsManager );
        entityProviderManager.setEntityRedirectsManager(entityRedirectsManager);

        entityProviderManager.init();

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

        entityBrokerManager = new EntityBrokerManager();
        entityBrokerManager.setEntityProviderManager( entityProviderManager );
        entityBrokerManager.setServerConfigurationService( serverConfigurationService );
        entityBrokerManager.setEntityPropertiesService( entityPropertiesService );
        entityBrokerManager.setEntityViewAccessProviderManager(entityViewAccessProviderManager);

        entityDescriptionManager = new EntityDescriptionManager();
        entityDescriptionManager.setEntityProviderManager( entityProviderManager );
        entityDescriptionManager.setEntityBrokerManager( entityBrokerManager );
        entityDescriptionManager.setEntityProperties( entityPropertiesService );
        entityDescriptionManager.setEntityActionsManager( entityActionsManager );
        entityDescriptionManager.setEntityRedirectsManager(entityRedirectsManager);
        entityDescriptionManager.setEntityViewAccessProviderManager(entityViewAccessProviderManager);
        entityDescriptionManager.setHttpServletAccessProviderManager(httpServletAccessProviderManager);

        entityEncodingManager = new EntityEncodingManager();
        entityEncodingManager.setEntityProviderManager( entityProviderManager );
        entityEncodingManager.setEntityBrokerManager( entityBrokerManager );

        entityBatchHandler = new EntityBatchHandler();
        entityBatchHandler.setEntityBrokerManager(entityBrokerManager);
        entityBatchHandler.setEntityEncodingManager(entityEncodingManager);

        entityRequestHandler = new EntityHandlerImpl();
        entityRequestHandler.setAccessProviderManager( httpServletAccessProviderManager );
        entityRequestHandler.setEntityBrokerManager( entityBrokerManager );
        entityRequestHandler.setEntityDescriptionManager( entityDescriptionManager );
        entityRequestHandler.setEntityEncodingManager( entityEncodingManager );
        entityRequestHandler.setEntityProviderManager( entityProviderManager );
        entityRequestHandler.setEntityViewAccessProviderManager( entityViewAccessProviderManager );
        entityRequestHandler.setRequestGetter( requestGetter );
        entityRequestHandler.setRequestStorage( requestStorage );
        entityRequestHandler.setEntityActionsManager(entityActionsManager);
        entityRequestHandler.setEntityRedirectsManager(entityRedirectsManager);
        entityRequestHandler.setServerConfigurationService(serverConfigurationService);
        entityRequestHandler.setEntityBatchHandler(entityBatchHandler);

        entityMetaPropertiesService = new EntityMetaPropertiesService();
        entityMetaPropertiesService.setDao(dao);
        entityMetaPropertiesService.setEntityBrokerManager(entityBrokerManager);
        entityMetaPropertiesService.setEntityProviderManager(entityProviderManager);

        entityTaggingService = new EntityTaggingService();
        entityTaggingService.setDao(dao);
        entityTaggingService.setEntityBrokerManager(entityBrokerManager);
        entityTaggingService.setEntityProviderManager(entityProviderManager);

        setInstance(this);
    }

}
