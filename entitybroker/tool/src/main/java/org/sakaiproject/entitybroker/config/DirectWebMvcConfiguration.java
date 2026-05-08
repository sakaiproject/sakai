/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.entitybroker.config;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.controller.DirectController;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.rest.EntityActionsManager;
import org.sakaiproject.entitybroker.rest.EntityBatchHandler;
import org.sakaiproject.entitybroker.rest.EntityDescriptionManager;
import org.sakaiproject.entitybroker.rest.EntityEncodingManager;
import org.sakaiproject.entitybroker.rest.EntityHandlerImpl;
import org.sakaiproject.entitybroker.rest.EntityRESTProviderBase;
import org.sakaiproject.entitybroker.rest.EntityRedirectsManager;
import org.sakaiproject.util.BasicAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring WebMVC configuration for the EntityBroker direct access WAR.
 * EntityBrokerManager is registered in the Sakai component ApplicationContext
 * (parent of this WebApplicationContext) and injected directly via @Autowired.
 */
@Configuration
@ImportResource("/WEB-INF/applicationContext.xml")
@EnableWebMvc
public class DirectWebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private EntityBrokerManager entityBrokerManager;

    @Bean
    public EntityActionsManager entityActionsManager() {
        return new EntityActionsManager(entityBrokerManager.getEntityProviderMethodStore());
    }

    @Bean
    public EntityRedirectsManager entityRedirectsManager() {
        return new EntityRedirectsManager(entityBrokerManager,
                entityBrokerManager.getEntityProviderMethodStore(),
                entityBrokerManager.getRequestStorage());
    }

    @Bean(destroyMethod = "destroy")
    public EntityDescriptionManager entityDescriptionManager() {
        return new EntityDescriptionManager(
                entityBrokerManager.getEntityViewAccessProviderManager(),
                entityBrokerManager.getEntityProviderManager(),
                entityBrokerManager.getEntityPropertiesService(),
                entityBrokerManager,
                entityBrokerManager.getEntityProviderMethodStore());
    }

    @Bean
    public EntityEncodingManager entityEncodingManager() {
        return new EntityEncodingManager(
                entityBrokerManager.getEntityProviderManager(),
                entityBrokerManager);
    }

    @Bean(destroyMethod = "destroy")
    public EntityBatchHandler entityBatchHandler(EntityEncodingManager entityEncodingManager) {
        return new EntityBatchHandler(entityBrokerManager, entityEncodingManager,
                entityBrokerManager.getExternalIntegrationProvider());
    }

    @Bean
    public EntityHandlerImpl entityRequestHandler(
            EntityActionsManager entityActionsManager,
            EntityRedirectsManager entityRedirectsManager,
            EntityEncodingManager entityEncodingManager,
            EntityDescriptionManager entityDescriptionManager,
            EntityBatchHandler entityBatchHandler) {
        EntityHandlerImpl handler = new EntityHandlerImpl(
                entityBrokerManager.getEntityProviderManager(),
                entityBrokerManager,
                entityEncodingManager,
                entityDescriptionManager,
                entityBrokerManager.getEntityViewAccessProviderManager(),
                entityBrokerManager.getRequestGetter(),
                entityActionsManager,
                entityRedirectsManager,
                entityBatchHandler,
                entityBrokerManager.getRequestStorage());
        return handler;
    }

    @Bean(destroyMethod = "destroy")
    public EntityRESTProviderBase entityRESTProvider(
            EntityActionsManager entityActionsManager,
            EntityEncodingManager entityEncodingManager,
            EntityHandlerImpl entityRequestHandler) {
        return new EntityRESTProviderBase(entityBrokerManager, entityActionsManager,
                entityEncodingManager, entityRequestHandler);
    }

    @Bean
    public BasicAuth basicAuth() {
        BasicAuth ba = new BasicAuth();
        ba.init();
        return ba;
    }

    @Bean
    public DirectController directController(EntityRequestHandler entityRequestHandler, BasicAuth basicAuth) {
        return new DirectController(entityRequestHandler, basicAuth);
    }
}
