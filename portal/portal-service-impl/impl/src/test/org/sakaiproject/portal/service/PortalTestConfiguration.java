/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.portal.service;

import javax.annotation.Resource;

import static org.mockito.Mockito.*;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.user.api.PreferencesService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.Getter;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class PortalTestConfiguration extends SakaiTestConfiguration {

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.portal")
    @Getter
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name = "org.sakaiproject.tool.api.ActiveToolManager")
    public ActiveToolManager activeToolManager() {
        return mock(ActiveToolManager.class);
    }

    @Bean(name = "org.sakaiproject.alias.api.AliasService")
    public AliasService aliasService() {
        return mock(AliasService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.thread_local.api.ThreadLocalManager")
    public ThreadLocalManager threadLocalManager() {
        return mock(ThreadLocalManager.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService mock = mock(ServerConfigurationService.class);
        when(mock.getInt("portal.max.recent.sites", PortalServiceImpl.DEFAULT_MAX_RECENT_SITES))
            .thenReturn(PortalServiceImpl.DEFAULT_MAX_RECENT_SITES);
        return mock;
    }
}
