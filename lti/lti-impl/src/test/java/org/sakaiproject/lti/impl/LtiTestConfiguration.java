/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.lti.impl;

import static org.mockito.Mockito.mock;

import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.lti.impl.testutil.MapBackedCacheManager;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
@EnableTransactionManagement
public class LtiTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("ltiHibernateMappings")
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Bean(name = "org.sakaiproject.ignite.SakaiCacheManager")
    public CacheManager cacheManager() {
        return new MapBackedCacheManager();
    }

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService PreferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.profile2.api.ProfileService")
    public ProfileService profileService() {
        return mock(ProfileService.class);
    }

    @Bean(name = "org.sakaiproject.util.ResourceLoader.ltiservice")
    public ResourceLoader resourceLoader() {
        return mock(ResourceLoader.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return mock(SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }
}
