/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import org.sakaiproject.grading.api.model.GradebookProperty;
import org.sakaiproject.grading.api.repository.GradebookPropertyRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradebookPropertyRepositoryImpl extends SpringCrudRepositoryImpl<GradebookProperty, Long>  implements GradebookPropertyRepository {

    public Optional<GradebookProperty> findByName(String name) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookProperty> query = cb.createQuery(GradebookProperty.class);
        Root<GradebookProperty> prop = query.from(GradebookProperty.class);
        query.where(cb.equal(prop.get("name"), name));
        return session.createQuery(query).uniqueResultOptional();
    }
}
