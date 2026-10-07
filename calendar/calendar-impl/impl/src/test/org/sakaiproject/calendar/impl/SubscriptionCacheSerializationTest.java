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
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.junit.Test;
import org.sakaiproject.calendar.api.ExternalCalendarSubscriptionSnapshot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** The shared Ignite loader cannot see a tool's component-private classes. */
public class SubscriptionCacheSerializationTest {
    @Test
    public void readsSubscriptionWithoutComponentClasses() throws Exception {
        IgniteConfiguration configuration = SubscriptionCacheReceiver.configuration("subscription-sender-test", 49251);
        try (Ignite ignite = Ignition.start(configuration)) {
            IgniteCache<String, ExternalCalendarSubscriptionSnapshot> cache = ignite.createCache(
                    new CacheConfiguration<String, ExternalCalendarSubscriptionSnapshot>("subscription")
                            .setCacheMode(CacheMode.REPLICATED));
            org.sakaiproject.ignite.IgniteSpringCacheManager manager = new org.sakaiproject.ignite.IgniteSpringCacheManager();
            manager.setSakaiIgnite(ignite);
            manager.setIgniteConfiguration(configuration);
            manager.init();
            SubscriptionCache subscriptions = new SubscriptionCache(
                    manager.getCache("subscription"), java.time.Clock.systemUTC());
            ExternalCalendarSubscriptionSnapshot snapshot = new ExternalCalendarSubscriptionSnapshot();
            snapshot.subscriptionUrl = "https://example.com/calendar.ics";
            snapshot.calendarName = "External calendar";
            snapshot.ok = true;
            snapshot.refreshed = java.time.Instant.now();
            ExternalCalendarSubscriptionSnapshot.EventSnapshot event = new ExternalCalendarSubscriptionSnapshot.EventSnapshot();
            event.displayName = "Weekly class";
            event.rangeStartMillis = 1791381600000L;
            event.rangeEndMillis = 1791385200000L;
            WeeklyRecurrenceRule recurrence = new WeeklyRecurrenceRule(2, 7);
            event.recurrenceFrequency = recurrence.getFrequency();
            event.recurrenceInterval = recurrence.getInterval();
            event.recurrenceCount = recurrence.getCount();
            event.recurrenceUntilMillis = recurrence.getUntil() == null ? null : recurrence.getUntil().getTime();
            snapshot.events.add(event);
            ExternalCalendarSubscriptionSnapshot.EventSnapshot untilEvent = new ExternalCalendarSubscriptionSnapshot.EventSnapshot();
            untilEvent.recurrenceFrequency = "day";
            untilEvent.recurrenceInterval = 1;
            untilEvent.recurrenceUntilMillis = 1791800000000L;
            snapshot.events.add(untilEvent);
            snapshot.events.add(new ExternalCalendarSubscriptionSnapshot.EventSnapshot());
            subscriptions.put(snapshot);
            String privateClasses = java.nio.file.Path.of(SubscriptionCache.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize().toString();
            String receiverClasspath = java.util.Arrays.stream(System.getProperty("java.class.path").split(java.io.File.pathSeparator))
                    .filter(entry -> !java.nio.file.Path.of(entry).toAbsolutePath().normalize().toString().equals(privateClasses))
                    .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
            java.util.List<String> command = new java.util.ArrayList<>();
            command.add(java.nio.file.Path.of(System.getProperty("java.home"), "bin", "java").toString());
            command.addAll(java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                    .filter(argument -> argument.startsWith("--add-opens=") || argument.startsWith("--add-exports="))
                    .toList());
            command.add("-cp");
            command.add(receiverClasspath);
            command.add(SubscriptionCacheReceiver.class.getName());
            java.nio.file.Path receiverLog = Files.createTempFile("subscription-receiver-", ".log");
            Process receiver = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(receiverLog.toFile()).start();
            try {
                assertTrue("Receiving JVM timed out", receiver.waitFor(30, java.util.concurrent.TimeUnit.SECONDS));
                assertEquals(Files.readString(receiverLog), 0, receiver.exitValue());
            } finally {
                receiver.destroyForcibly();
            }
        }
    }
}
