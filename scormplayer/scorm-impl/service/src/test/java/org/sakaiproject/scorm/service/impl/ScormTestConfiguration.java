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
package org.sakaiproject.scorm.service.impl;

import static org.mockito.Mockito.mock;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring test context for {@link ScormEntityProducer}. Collaborators are supplied as mock beans and the
 * producer is wired exactly as it is in production (spring-scorm-services.xml), without a database.
 */
@Configuration
public class ScormTestConfiguration {

    @Bean(name = "org.sakaiproject.scorm.service.api.ScormContentService")
    public ScormContentService scormContentService() {
        return mock(ScormContentService.class);
    }

    @Bean(name = "org.sakaiproject.scorm.service.api.ScormResourceService")
    public ScormResourceService scormResourceService() {
        return mock(ScormResourceService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.scorm.dao.api.ContentPackageDao")
    public ContentPackageDao contentPackageDao() {
        return mock(ContentPackageDao.class);
    }

    @Bean(name = "org.sakaiproject.scorm.dao.api.ContentPackageManifestDao")
    public ContentPackageManifestDao contentPackageManifestDao() {
        return mock(ContentPackageManifestDao.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        return mock(ServerConfigurationService.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.entity.api.EntityManager")
    public EntityManager entityManager() {
        return mock(EntityManager.class);
    }

    @Bean
    public ScormEntityProducer scormEntityProducer(ScormContentService scormContentService,
            ScormResourceService scormResourceService, ContentHostingService contentHostingService,
            ContentPackageDao contentPackageDao, ContentPackageManifestDao contentPackageManifestDao,
            SecurityService securityService, ServerConfigurationService serverConfigurationService,
            GradingService gradingService, EntityManager entityManager) {
        ScormEntityProducer producer = new ScormEntityProducer();
        producer.setScormContentService(scormContentService);
        producer.setScormResourceService(scormResourceService);
        producer.setContentHostingService(contentHostingService);
        producer.setContentPackageDao(contentPackageDao);
        producer.setContentPackageManifestDao(contentPackageManifestDao);
        producer.setSecurityService(securityService);
        producer.setServerConfigurationService(serverConfigurationService);
        producer.setGradingService(gradingService);
        producer.setEntityManager(entityManager);
        return producer;
    }
}
