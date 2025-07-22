/**
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.datemanager.tool;

import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.datemanager.api.DateManagerService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

import static org.mockito.Mockito.when;

/**
 * Test configuration for MainController CSV functionality tests
 */
@Configuration
public class MainControllerTestConfiguration {

    @Bean
    public DateManagerService dateManagerService() {
        return Mockito.mock(DateManagerService.class);
    }

    @Bean
    public SessionManager sessionManager() {
        return Mockito.mock(SessionManager.class);
    }

    @Bean
    public SiteService siteService() {
        return Mockito.mock(SiteService.class);
    }

    @Bean
    public PreferencesService preferencesService() {
        return Mockito.mock(PreferencesService.class);
    }

    @Bean
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService service = Mockito.mock(ServerConfigurationService.class);
        // Default to comma separator for US style
        when(service.getString("csv.separator", ",")).thenReturn(",");
        return service;
    }
}