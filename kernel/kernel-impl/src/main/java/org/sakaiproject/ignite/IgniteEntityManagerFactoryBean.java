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

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    /**
     * To manage our lifecycle timing, we take our concrete subclass here rather than
     * either the Ignite interface or even the IgniteSpringBean. Effectively, we need to
     * start the grid slightly before the normal Spring behavior to satisfy Hibernate's
     * initialization.
     */
    public void setIgnite(EagerIgniteSpringBean ignite) {
        ignite.ensureStarted();
        getJpaPropertyMap().put("org.apache.ignite.hibernate.ignite_instance_name", ignite.name());
        log.info("Ignite instance name [{}] configured as hibernate cache provider", ignite.name());
    }
}
