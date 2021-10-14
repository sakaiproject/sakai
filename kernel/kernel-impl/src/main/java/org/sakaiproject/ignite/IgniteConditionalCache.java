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

import org.apache.ignite.configuration.CacheConfiguration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class IgniteConditionalCache {
    private String className;
    private CacheConfiguration cacheConfiguration;

    /**
     * Checks to see if the class stored className exists. Note this works because all of our data classes are in shared.
     * @return true if the class exists and false if it doesn't
     */
    public boolean exists() {
        try {
            Class.forName(className);
            log.info("Conditional cache {} detected for class {}, adding cache", cacheConfiguration.getName(), className);
            return true;
        } catch (ClassNotFoundException e) {
            log.debug("Conditional cache {} not detected for class {}, skipping cache", cacheConfiguration.getName(), className);
            return false;
        }
    }
}
