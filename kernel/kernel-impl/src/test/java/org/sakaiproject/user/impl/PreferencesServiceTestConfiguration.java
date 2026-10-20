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
package org.sakaiproject.user.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import jakarta.annotation.Resource;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import lombok.Getter;

import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/preferences-components.xml")
@PropertySource("classpath:/hibernate.properties")
public class PreferencesServiceTestConfiguration extends SakaiTestConfiguration {

    @Bean(destroyMethod = "close")
    public Ignite preferencesCache() {
        IgniteConfiguration configuration = new IgniteConfiguration();
        configuration.setIgniteInstanceName("preferences-service-test");
        configuration.setWorkDirectory(Path.of("target", "preferences-ignite").toAbsolutePath().toString());
        configuration.setLocalHost("127.0.0.1");
        configuration.setDiscoverySpi(new TcpDiscoverySpi()
                .setIpFinder(new TcpDiscoveryVmIpFinder().setAddresses(List.of("127.0.0.1:47550")))
                .setLocalPort(47550).setLocalPortRange(0));
        configuration.setDataStorageConfiguration(new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                new DataRegionConfiguration().setInitialSize(16 * 1024 * 1024).setMaxSize(64 * 1024 * 1024)));
        configuration.setCacheConfiguration(new CacheConfiguration<>("default-query-results-region"),
                new CacheConfiguration<>("default-update-timestamps-region"));
        return Ignition.start(configuration);
    }

    @Override
    @Bean
    public Properties hibernateProperties() {
        Properties properties = super.hibernateProperties();
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.apache.ignite.cache.hibernate.HibernateRegionFactory");
        properties.setProperty("org.apache.ignite.hibernate.ignite_instance_name", preferencesCache().name());
        return properties;
    }

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.preferences")
    @Getter
    private AdditionalHibernateMappings additionalHibernateMappings;
}
