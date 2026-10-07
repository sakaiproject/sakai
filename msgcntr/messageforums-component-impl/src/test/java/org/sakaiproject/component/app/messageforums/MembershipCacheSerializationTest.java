/*
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
package org.sakaiproject.component.app.messageforums;

import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.MembershipItemSnapshot;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** The shared Ignite loader cannot see a tool's component-private classes. */
public class MembershipCacheSerializationTest {
    @Test
    public void readsMembershipAndCustomPermissionsWithoutComponentClasses() throws Exception {
        ClassLoader sharedLoader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("org.sakaiproject.component.app.messageforums.")
                        && !name.startsWith("org.sakaiproject.component.app.messageforums.dao.hibernate.")) {
                    throw new ClassNotFoundException("Component-private class: " + name);
                }
                return super.loadClass(name, resolve);
            }
        };
        TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder(false);
        finder.setAddresses(Collections.singletonList("127.0.0.1:49250"));
        IgniteConfiguration configuration = new IgniteConfiguration()
                .setIgniteInstanceName("membership-serialization-test")
                .setClassLoader(sharedLoader).setPeerClassLoadingEnabled(false)
                .setLocalHost("127.0.0.1")
                .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(finder).setLocalPort(49250).setLocalPortRange(0))
                .setWorkDirectory(Files.createTempDirectory("membership-ignite-").toString())
                .setMetricsLogFrequency(0)
                .setPublicThreadPoolSize(2).setSystemThreadPoolSize(2).setStripedPoolSize(2)
                .setQueryThreadPoolSize(2).setServiceThreadPoolSize(2).setDataStreamerThreadPoolSize(2)
                .setDataStorageConfiguration(new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                        new DataRegionConfiguration().setInitialSize(16L * 1024 * 1024).setMaxSize(32L * 1024 * 1024)));
        try (Ignite ignite = Ignition.start(configuration)) {
            IgniteCache<String, Set<MembershipItemSnapshot>> cache = ignite.createCache(
                    new CacheConfiguration<String, Set<MembershipItemSnapshot>>("membership")
                            .setCacheMode(CacheMode.PARTITIONED).setBackups(0));
            PermissionLevelImpl permission = new PermissionLevelImpl();
            permission.setName("Custom level");
            permission.setRead(true);
            permission.setNewTopic(false);
            permission.setIdentifyAnonAuthors(true);
            MembershipItemSnapshot membership = new MembershipItemSnapshot();
            membership.setId(7L);
            membership.setAreaId(19L);
            membership.setPermissionLevelName(permission.getName());
            membership.setPermissionLevel(permission);
            cache.put("site", Collections.singleton(membership));
            MembershipItemSnapshot restored = cache.get("site").iterator().next();
            assertEquals(Long.valueOf(7), restored.getId());
            assertEquals(Long.valueOf(19), restored.getAreaId());
            assertEquals("Custom level", restored.getPermissionLevel().getName());
            assertTrue(restored.getPermissionLevel().getRead());
            assertFalse(restored.getPermissionLevel().getNewTopic());
            assertTrue(restored.getPermissionLevel().getIdentifyAnonAuthors());
        }
    }
}
