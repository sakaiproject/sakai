/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
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
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.sql.DataSource;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.junit.Test;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PreferencesServiceCacheTest {

    private static final int EXPIRY_SECONDS = 12;

    @Test
    public void savesFreshPreferencesAfterTheOriginalTimestampDeadline() throws Exception {
        String name = "preferences-cache-" + UUID.randomUUID();
        try (Ignite ignite = Ignition.start(igniteConfiguration(name));
                AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("cache-test", Map.of(
                    "test.ignite.name", name,
                    "hibernate.connection.url", "jdbc:hsqldb:mem:" + name,
                    "hibernate.hbm2ddl.auto", "create-drop")));
            context.register(CacheTestConfiguration.class);
            context.refresh();
            when(context.getBean(SecurityService.class).unlock(anyString(), anyString())).thenReturn(true);
            PreferencesService service = context.getBean(PreferencesService.class);
            JdbcTemplate jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            String userId = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO SAKAI_PREFERENCES(PREFERENCES_ID,XML,VERSION) VALUES(?,?,?)", userId,
                    "<preferences id='" + userId + "'><properties><property name='value' value='old'/></properties></preferences>", 0L);

            // Establish the table timestamp before this user's query enters the cache.
            assertTrue(service.applyEditWithAutoCommit("bootstrap-" + UUID.randomUUID(),
                    edit -> edit.getPropertiesEdit().addProperty("bootstrap", "yes")));
            long timestampCreated = System.nanoTime();
            Thread.sleep(4000);
            PreferencesEdit first = service.edit(userId);
            first.getPropertiesEdit().addProperty("value", "new");
            service.commit(first);
            assertEquals(Long.valueOf(1), version(jdbc, userId));

            // CreatedExpiryPolicy updates retain the original deadline. Query results are newer.
            while (System.nanoTime() - timestampCreated < TimeUnit.SECONDS.toNanos(EXPIRY_SECONDS) + TimeUnit.MILLISECONDS.toNanos(500)) {
                Thread.sleep(100);
            }
            assertTrue("The cached query must survive the original timestamp deadline",
                    ignite.cache("default-query-results-region").size() > 0);
            assertEquals(1, ignite.cluster().nodes().size());
            for (int attempt = 1; attempt <= 2; attempt++) {
                assertEquals("new", service.getPreferences(userId).getProperties().getProperty("value"));
                PreferencesEdit edit = service.edit(userId);
                edit.getPropertiesEdit().addProperty("attempt", Integer.toString(attempt));
                service.commit(edit);
                assertEquals(Long.valueOf(attempt + 1), version(jdbc, userId));
            }
        }
    }

    private Long version(JdbcTemplate jdbc, String userId) {
        return jdbc.queryForObject("SELECT VERSION FROM SAKAI_PREFERENCES WHERE PREFERENCES_ID=?", Long.class, userId);
    }

    private IgniteConfiguration igniteConfiguration(String name) {
        TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(List.of("127.0.0.1:48573"));
        return new IgniteConfiguration().setIgniteInstanceName(name).setConsistentId(name).setLocalHost("127.0.0.1")
                .setDiscoverySpi(new TcpDiscoverySpi().setLocalAddress("127.0.0.1").setLocalPort(48573).setLocalPortRange(0).setIpFinder(finder))
                .setCommunicationSpi(new TcpCommunicationSpi().setLocalAddress("127.0.0.1").setLocalPort(48673).setLocalPortRange(0))
                .setClientConnectorConfiguration(null).setConnectorConfiguration(null)
                .setWorkDirectory(Path.of("target", name).toAbsolutePath().toString())
                .setMetricsLogFrequency(0).setPublicThreadPoolSize(2).setSystemThreadPoolSize(2)
                .setStripedPoolSize(2).setServiceThreadPoolSize(2).setDataStreamerThreadPoolSize(1)
                .setQueryThreadPoolSize(2).setAsyncCallbackPoolSize(2).setRebalanceThreadPoolSize(1)
                .setDataStorageConfiguration(new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                        new DataRegionConfiguration().setInitialSize(32L * 1024 * 1024).setMaxSize(128L * 1024 * 1024)))
                .setCacheConfiguration(hibernateCaches());
    }

    @SuppressWarnings("unchecked")
    private CacheConfiguration<?, ?>[] hibernateCaches() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(beans).loadBeanDefinitions("classpath:/WEB-INF/ignite-components.xml");
        List<CacheConfiguration<?, ?>> caches = (List<CacheConfiguration<?, ?>>) beans.getBean("org.sakaiproject.ignite.cache.list.hibernate");
        for (CacheConfiguration<?, ?> cache : caches) {
            cache.setDataRegionName(null);
            // Preserve the production choice of eternal versus finite expiry, shortening only finite deadlines.
            if (!cache.getExpiryPolicyFactory().create().getExpiryForCreation().isEternal()) {
                cache.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new javax.cache.expiry.Duration(TimeUnit.SECONDS, EXPIRY_SECONDS)));
            }
        }
        return caches.toArray(new CacheConfiguration<?, ?>[0]);
    }

    @Configuration
    public static class CacheTestConfiguration extends PreferencesServiceTestConfiguration {
        @Override
        @Bean
        public Properties hibernateProperties() {
            Properties properties = super.hibernateProperties();
            properties.setProperty("hibernate.cache.use_second_level_cache", "true");
            properties.setProperty("hibernate.cache.use_query_cache", "true");
            properties.setProperty("hibernate.cache.region.factory_class", "org.apache.ignite.cache.hibernate.HibernateRegionFactory");
            properties.setProperty("org.apache.ignite.hibernate.ignite_instance_name", environment.getRequiredProperty("test.ignite.name"));
            return properties;
        }
    }
}
