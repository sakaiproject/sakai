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
package org.sakaiproject.messaging.impl;

import javax.annotation.Resource;

import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.api.FormattedText;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteMessaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import lombok.Getter;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/messaging-components.xml")
@PropertySource("classpath:/hibernate.properties")
public class UserMessagingServiceTestConfiguration extends SakaiTestConfiguration {

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.usernotifications")
    @Getter
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name = "org.sakaiproject.email.api.DigestService")
    public DigestService digestService() {
        return mock(DigestService.class);
    }

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.emailtemplateservice.api.EmailTemplateService")
    public EmailTemplateService emailTemplateService() {
        return mock(EmailTemplateService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.EventTrackingService")
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }


    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        when(scs.getInt("messaging.threadpool.size", 20)).thenReturn(20);
        return scs;
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        FormattedText ft = mock(FormattedText.class);
        when(ft.processFormattedText(any(String.class), isNull(), isNull())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ft.convertFormattedTextToPlaintext(any(String.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return ft;
    }

    @Bean(name = "org.sakaiproject.ignite.SakaiIgnite")
    public IgniteSpringBean ignite() {
        IgniteSpringBean ignite = mock(IgniteSpringBean.class);
        IgniteCluster cluster = mock(IgniteCluster.class);
        when(cluster.forLocal()).thenReturn(null);
        IgniteMessaging messaging = mock(IgniteMessaging.class);
        when(ignite.message(any())).thenReturn(messaging);
        when(ignite.cluster()).thenReturn(cluster);
        return ignite;
    }

}
