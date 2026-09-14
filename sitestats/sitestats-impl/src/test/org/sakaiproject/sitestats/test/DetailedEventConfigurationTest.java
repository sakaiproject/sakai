/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.util.ReversiblePropertyOverrideConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        SiteStatsTestConfiguration.class,
        DetailedEventConfigurationTest.LegacyPropertyOverrides.class
})
public class DetailedEventConfigurationTest {

    @Autowired private StatsManager statsManager;
    @Autowired private StatsUpdateManager statsUpdateManager;

    @Test
    public void legacyDetailedEventPropertiesConfigureManagers() {
        assertTrue(statsManager.isDisplayDetailedEvents());
        assertTrue(statsUpdateManager.isCollectDetailedEvents());
    }

    @Configuration
    static class LegacyPropertyOverrides {

        @Bean
        public static ReversiblePropertyOverrideConfigurer detailedEventPropertyOverrides() {
            Properties properties = new Properties();
            properties.setProperty(
                    "displayDetailedEvents@org.sakaiproject.sitestats.api.StatsManager.target", "true");
            properties.setProperty(
                    "collectDetailedEvents@org.sakaiproject.sitestats.api.StatsUpdateManager.target", "true");

            ReversiblePropertyOverrideConfigurer configurer = new ReversiblePropertyOverrideConfigurer();
            configurer.setBeanNameAtEnd(true);
            configurer.setBeanNameSeparator("@");
            configurer.setProperties(properties);
            return configurer;
        }
    }
}
