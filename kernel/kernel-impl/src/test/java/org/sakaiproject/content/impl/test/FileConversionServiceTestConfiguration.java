/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.content.impl.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.SessionFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.content.api.persistence.FileConversionQueueItem;
import org.sakaiproject.content.api.persistence.FileConversionServiceRepository;
import org.sakaiproject.content.impl.FileConversionServiceImpl;
import org.sakaiproject.content.impl.persistence.FileConversionServiceRepositoryImpl;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/hibernate.properties")
public class FileConversionServiceTestConfiguration extends SakaiTestConfiguration {

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.fileconversionservice")
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {

        Class[] annotatedClasses = new Class[] {FileConversionQueueItem.class};
        AdditionalHibernateMappings mappings = new AdditionalHibernateMappingsImpl();
        mappings.setAnnotatedClasses(annotatedClasses);
        return mappings;
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {

        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        when(scs.getBoolean("fileconversion.submit.enabled", false)).thenReturn(true);
        when(scs.getStringList("fileconversion.fromtypes", FileConversionService.DEFAULT_TYPES)).thenReturn(FileConversionService.DEFAULT_TYPES);
        when(scs.getString("fileconversion.converterurl", "http://localhost:9980")).thenReturn("http://localhost:9980");
        when(scs.getInt("fileconversion.workerthreads", 5)).thenReturn(5);
        when(scs.getInt("fileconversion.queueintervalminutes", 1)).thenReturn(1);
        when(scs.getInt("fileconversion.pausemillis", 1000)).thenReturn(1000);
        return scs;
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {

        ContentHostingService chs = mock(ContentHostingService.class);
        return chs;
    }

    @Bean(name = "org.sakaiproject.scheduling.api.SchedulingService")
    public SchedulingService schedulingService() {
        return mock(SchedulingService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.persistence.FileConversionServiceRepository")
    public FileConversionServiceRepository fileConversionServiceRepository(SessionFactory sessionFactory) {

        FileConversionServiceRepositoryImpl repository = new FileConversionServiceRepositoryImpl();
        repository.setSessionFactory(sessionFactory);
        return repository;
    }

    @Bean
    public FileConversionService fileConversionService(ServerConfigurationService scs, FileConversionServiceRepository repository) {

        FileConversionServiceImpl fcs = new FileConversionServiceImpl();
        fcs.setServerConfigurationService(scs);
        fcs.setRepository(repository);
        fcs.init();
        return fcs;
    }
}
