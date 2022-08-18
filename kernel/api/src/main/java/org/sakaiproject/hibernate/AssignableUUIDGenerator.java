/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.hibernate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.sakaiproject.component.api.ServerConfigurationService;

import lombok.Setter;

public class AssignableUUIDGenerator extends UUIDGenerator implements IdentifierGenerator, Configurable {

    public static final String HIBERNATE_ASSIGNABLE_ID_CLASSES = "hibernate.assignable.id.classes";

    @Setter
    private static ServerConfigurationService serverConfigurationService;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(type, params, serviceRegistry);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        if (serverConfigurationService != null) {
            String[] classes = serverConfigurationService.getStrings(HIBERNATE_ASSIGNABLE_ID_CLASSES);
            if (classes != null) {
                String entityName = object.getClass().getName();
                if (Arrays.asList(classes).contains(entityName)) {
                    final Serializable id = session.getEntityPersister(entityName, object).getIdentifier(object, session);
                    if (id != null) return id;
                }
            }
        }
        return super.generate(session, object);
    }
}
