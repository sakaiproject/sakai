/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.test.emailtemplateservice;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.config.ThymeleafConfig;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Loads the real tool MVC wiring, replacing only its external kernel boundaries. */
@Configuration
@Import(ThymeleafConfig.class)
public class EmailTemplateToolTestConfiguration {

    @Bean(name = "org.sakaiproject.emailtemplateservice.api.EmailTemplateService")
    public EmailTemplateService emailTemplateService() {
        return mock(EmailTemplateService.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public LocaleService localeService() {
        return mock(LocaleService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        SessionManager manager = mock(SessionManager.class);
        Session session = mock(Session.class);
        Map<String, Object> attributes = new HashMap<>();
        when(manager.getCurrentSession()).thenReturn(session);
        when(session.getId()).thenReturn("test-session");
        when(session.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.getArgument(0)));
        doAnswer(call -> {
            attributes.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        ComponentManager.getInstance().loadComponent(SessionManager.class, manager);
        return manager;
    }

    @Bean(name = "org.sakaiproject.thread_local.api.ThreadLocalManager")
    public ThreadLocalManager threadLocalManager() {
        ThreadLocalManager manager = mock(ThreadLocalManager.class);
        ComponentManager.getInstance().loadComponent(ThreadLocalManager.class, manager);
        return manager;
    }
}
