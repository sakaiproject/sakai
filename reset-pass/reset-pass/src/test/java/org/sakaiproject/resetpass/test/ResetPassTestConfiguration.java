/**********************************************************************************
 *
 * Copyright (c) 2026 The Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.resetpass.test;

import static org.mockito.Mockito.mock;

import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.resetpass.config.ThymeleafConfig;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
@Import(ThymeleafConfig.class)
public class ResetPassTestConfiguration {

    @Bean
    public ToolManager toolManager() {
        return mock(ToolManager.class);
    }

    @Bean
    public ServerConfigurationService serverConfigurationService() {
        return mock(ServerConfigurationService.class);
    }

    @Bean
    public LocaleService localeService() {
        return mock(LocaleService.class);
    }

    @Bean
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean
    public AccountValidationService accountValidationService() {
        return mock(AccountValidationService.class);
    }

    @Bean
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean
    @Primary
    public MessageSource testMessageSource() {
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("Messages");
        messages.setFallbackToSystemLocale(false);
        return messages;
    }
}
