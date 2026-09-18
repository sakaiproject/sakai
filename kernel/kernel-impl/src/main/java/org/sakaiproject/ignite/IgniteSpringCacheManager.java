/**
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
package org.sakaiproject.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.spring.SpringCacheManager;
import org.apache.ignite.configuration.IgniteConfiguration;

import lombok.Setter;

public class IgniteSpringCacheManager extends SpringCacheManager {

    @Setter private Ignite sakaiIgnite;
    @Setter private IgniteConfiguration igniteConfiguration;

    @Override
    public IgniteConfiguration getConfiguration() {
        return igniteConfiguration;
    }

    public void init() {
        // this configuration is required so that SpringCacheManager looks up the existing
        // ignite instance that was started by Hibernate
        setConfiguration(null);
        setConfigurationPath(null);
        setIgniteInstanceName(igniteConfiguration.getIgniteInstanceName());

        // SpringCacheManager normally only attaches to the ignite instance when it
        // receives a ContextRefreshedEvent, which Spring fires after every singleton
        // bean (including other beans' own init-methods) has already been created.
        // sakaiIgnite is a required dependency of this bean, so EagerIgniteSpringBean
        // has already started the real ignite instance by the time we get here -
        // attach to it now instead of waiting, so getCache() is safe to call from
        // other beans' eager init-methods.
        onApplicationEvent(null);
    }
}
