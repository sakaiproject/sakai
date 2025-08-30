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
package org.sakaiproject.profile2.impl.test;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;

import static org.mockito.Mockito.mock;

import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.profile2.api.repository.ProfileImageOfficialRepository;
import org.sakaiproject.profile2.impl.repository.ProfileImageOfficialRepositoryImpl;
import org.sakaiproject.profile2.api.repository.ProfileImageUploadedRepository;
import org.sakaiproject.profile2.impl.repository.ProfileImageUploadedRepositoryImpl;
import org.sakaiproject.profile2.api.repository.SocialNetworkingInfoRepository;
import org.sakaiproject.profile2.impl.repository.SocialNetworkingInfoRepositoryImpl;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;

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
public class ProfileServiceTestConfiguration extends SakaiTestConfiguration {

    @Resource(name = "profileHibernateMappings")
    @Getter
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name="org.sakaiproject.profile2.api.repository.ProfileImageOfficialRepository")
    public ProfileImageOfficialRepository profileImageOfficialRepository(SessionFactory sessionFactory) {

        ProfileImageOfficialRepositoryImpl profileImageOfficialRepository = new ProfileImageOfficialRepositoryImpl();
        profileImageOfficialRepository.setSessionFactory(sessionFactory);
        return profileImageOfficialRepository;
    }

    @Bean(name="org.sakaiproject.profile2.api.repository.ProfileImageUploadedRepository")
    public ProfileImageUploadedRepository profileImageUploadedRepository(SessionFactory sessionFactory) {

        ProfileImageUploadedRepositoryImpl profileImageUploadedRepository = new ProfileImageUploadedRepositoryImpl();
        profileImageUploadedRepository.setSessionFactory(sessionFactory);
        return profileImageUploadedRepository;
    }

    /*
    @Bean(name="org.sakaiproject.profile2.api.repository.SocialNetworkingInfoRepository")
    public SocialNetworkingInfoRepository socialNetworkingInfoRepository(SessionFactory sessionFactory) {

        SocialNetworkingInfoRepositoryImpl socialNetworkingInfoRepository = new SocialNetworkingInfoRepositoryImpl();
        socialNetworkingInfoRepository.setSessionFactory(sessionFactory);
        return socialNetworkingInfoRepository;
    }
    */

    @Bean(name = "org.sakaiproject.api.common.edu.person.SakaiPersonManager")
    public SakaiPersonManager sakaiPersonManager() {
        return mock(SakaiPersonManager.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.id.api.IdManager")
    public IdManager idManager() {
        return mock(IdManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.ActivityService")
    public ActivityService activityService() {
        return mock(ActivityService.class);
    }

    @Bean(name = "org.sakaiproject.search.elasticsearch.ElasticSearchIndexBuilder")
    public SearchIndexBuilder searchIndexBuilder() {
        return mock(SearchIndexBuilder.class);
    }
}
