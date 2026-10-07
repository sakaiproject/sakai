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
package org.sakaiproject.calendar.impl;

import java.nio.file.Files;
import java.util.Collections;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

/** Receives cache data in a JVM whose classpath excludes the Calendar implementation JAR. */
public class SubscriptionCacheReceiver {
    public static void main(String[] args) throws Exception {
        try (Ignite receiver = Ignition.start(configuration("subscription-receiver-test", 49252))) {
            try {
                Class.forName("org.sakaiproject.calendar.impl.SubscriptionCache");
                throw new AssertionError("Receiving JVM still contains the private Calendar component");
            } catch (ClassNotFoundException expected) {
                // The shared API must be sufficient to deserialize the subscription.
            }
            org.sakaiproject.calendar.api.ExternalCalendarSubscriptionSnapshot snapshot =
                    (org.sakaiproject.calendar.api.ExternalCalendarSubscriptionSnapshot)
                            receiver.cache("subscription").get("https://example.com/calendar.ics");
            if (!"External calendar".equals(snapshot.calendarName)) {
                throw new AssertionError("Calendar name was not preserved");
            }
            org.sakaiproject.calendar.api.ExternalCalendarSubscriptionSnapshot.EventSnapshot event = snapshot.events.get(0);
            if (!"Weekly class".equals(event.displayName) || event.rangeStartMillis != 1791381600000L) {
                throw new AssertionError("Event title or start time was not preserved");
            }
            if (!"week".equals(event.recurrenceFrequency) || event.recurrenceInterval != 2 || event.recurrenceCount != 7) {
                throw new AssertionError("Count-limited recurrence settings were not preserved");
            }
            if (!"day".equals(snapshot.events.get(1).recurrenceFrequency)
                    || !Long.valueOf(1791800000000L).equals(snapshot.events.get(1).recurrenceUntilMillis)) {
                throw new AssertionError("Date-limited recurrence settings were not preserved");
            }
            if (snapshot.events.get(2).recurrenceFrequency != null) {
                throw new AssertionError("A non-recurring event acquired a recurrence");
            }
        }
    }

    public static IgniteConfiguration configuration(String name, int port) throws Exception {
        TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder(false);
        finder.setAddresses(Collections.singletonList("127.0.0.1:49251"));
        return new IgniteConfiguration().setIgniteInstanceName(name).setPeerClassLoadingEnabled(false)
                .setLocalHost("127.0.0.1")
                .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(finder).setLocalPort(port).setLocalPortRange(0))
                .setWorkDirectory(Files.createTempDirectory("subscription-ignite-").toString())
                .setMetricsLogFrequency(0)
                .setPublicThreadPoolSize(2).setSystemThreadPoolSize(2).setStripedPoolSize(2)
                .setQueryThreadPoolSize(2).setServiceThreadPoolSize(2).setDataStreamerThreadPoolSize(2)
                .setDataStorageConfiguration(new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                        new DataRegionConfiguration().setInitialSize(16L * 1024 * 1024).setMaxSize(32L * 1024 * 1024)));
    }
}
