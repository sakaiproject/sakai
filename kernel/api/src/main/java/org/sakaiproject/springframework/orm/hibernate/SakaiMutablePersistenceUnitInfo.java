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
package org.sakaiproject.springframework.orm.hibernate;

import jakarta.persistence.spi.ClassTransformer;

import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public class SakaiMutablePersistenceUnitInfo extends MutablePersistenceUnitInfo {
    @Override
    public ClassLoader getNewTempClassLoader() {
        return new SimpleThrowawayClassLoader(this.getClassLoader());
    }

    /**
     * No real {@link org.springframework.instrument.classloading.LoadTimeWeaver} is configured, since
     * hibernate.enable_lazy_load_no_trans is set to use proxy-based lazy loading instead of bytecode
     * transformation. Hibernate still registers a class transformer during bootstrap regardless, so this
     * is a no-op rather than the superclass's default of throwing UnsupportedOperationException.
     */
    @Override
    public void addTransformer(ClassTransformer classTransformer) {
    }
}
