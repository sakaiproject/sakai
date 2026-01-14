/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.impl;

import static org.mockito.Mockito.mock;

import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lti.api.SiteEmailPreferenceSetter;
import org.sakaiproject.lti.api.SiteMembershipUpdater;
import org.sakaiproject.lti.api.UserFinderOrCreator;
import org.sakaiproject.lti.api.UserLocaleSetter;
import org.sakaiproject.lti.api.UserPictureSetter;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTestConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class PlusTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("plusHibernateMappings")
    @Getter
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name = "org.sakaiproject.sitestats.api.StatsManager")
    public StatsManager statsManager() {
        return mock(StatsManager.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.UserFinderOrCreator")
    public UserFinderOrCreator userFinderOrCreator() {
        return mock(UserFinderOrCreator.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.UserLocaleSetter")
    public UserLocaleSetter userLocaleSetter() {
        return mock(UserLocaleSetter.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.UserPictureSetter")
    public UserPictureSetter userPictureSetter() {
        return mock(UserPictureSetter.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.SiteEmailPreferenceSetter")
    public SiteEmailPreferenceSetter siteEmailPreferenceSetter() {
        return mock(SiteEmailPreferenceSetter.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.SiteMembershipUpdater")
    public SiteMembershipUpdater siteMembershipUpdater() {
        return mock(SiteMembershipUpdater.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteConfigurationService() {
        return mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.scheduling.api.SchedulingService")
    public SchedulingService schedulingService() {
        return mock(SchedulingService.class);
    }
}
