/**
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
package org.sakaiproject.springframework.orm.hibernate;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;

import lombok.Setter;

/**
 * Exposes the native Hibernate {@link SessionFactory} behind a Hibernate-backed JPA {@link EntityManagerFactory},
 * for the benefit of non-JPA code that still depends on the classic Hibernate SessionFactory API.
 * <p>
 * Replaces Spring's {@code org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean}, which was deprecated
 * in Spring 4.3.12 in favor of {@link EntityManagerFactory#unwrap(Class)} and removed entirely in Spring 6.
 */
public class HibernateSessionFactoryBean implements FactoryBean<SessionFactory> {

    @Setter private EntityManagerFactory entityManagerFactory;

    @Override
    public SessionFactory getObject() {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    @Override
    public Class<?> getObjectType() {
        return SessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
