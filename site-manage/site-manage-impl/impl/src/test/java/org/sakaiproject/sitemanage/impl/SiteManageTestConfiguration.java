/**
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
package org.sakaiproject.sitemanage.impl;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Loads the production import service and notification provider with kernel/infrastructure boundaries. */
final class SiteManageTestConfiguration implements AutoCloseable {
    private final DefaultListableBeanFactory beans = new DefaultListableBeanFactory();

    SiteManageTestConfiguration() {
        new XmlBeanDefinitionReader(beans).loadBeanDefinitions(
            new FileSystemResource("src/webapp/WEB-INF/components.xml"));
        registerBoundary(AuthzGroupService.class);
        registerBoundary(FunctionManager.class);
        registerBoundary(SecurityService.class);
        registerBoundary(ServerConfigurationService.class);
        registerBoundary(ContentHostingService.class);
        registerBoundary(EmailService.class);
        registerBoundary(EmailTemplateService.class);
        registerBoundary(EntityManager.class);
        registerBoundary(DeveloperHelperService.class);
        registerBoundary(EventTrackingService.class);
        registerBoundary(ShortenedUrlService.class);
        registerBoundary(SiteService.class);
        registerBoundary(ThreadLocalManager.class);
        registerBoundary(UserTimeService.class);
        registerBoundary(SessionManager.class);
        registerBoundary(ToolManager.class);
        registerBoundary(PreferencesService.class);
        registerBoundary(UserDirectoryService.class);
        registerBoundary(LinkMigrationHelper.class);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        beans.registerSingleton("org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager", transactions);
    }

    <T> T getBean(Class<T> type) {
        return beans.getBean(type.getName(), type);
    }

    private <T> void registerBoundary(Class<T> type) {
        beans.registerSingleton(type.getName(), mock(type));
    }

    @Override
    public void close() {
        beans.destroySingletons();
    }
}
