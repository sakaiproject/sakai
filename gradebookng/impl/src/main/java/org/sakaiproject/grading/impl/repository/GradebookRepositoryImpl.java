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
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.repository.GradebookRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradebookRepositoryImpl extends SpringCrudRepositoryImpl<Gradebook, Long>  implements GradebookRepository {

    @Transactional(readOnly = true)
    public Optional<Gradebook> findByUid(String uid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Gradebook> query = cb.createQuery(Gradebook.class);
        Root<Gradebook> gradebook = query.from(Gradebook.class);
        query.where(cb.equal(gradebook.get("uid"), uid));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional
    public int deleteByUid(String uid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Gradebook> delete = cb.createCriteriaDelete(Gradebook.class);
        Root<Gradebook> gradebook = delete.from(Gradebook.class);
        delete.where(cb.equal(gradebook.get("uid"), uid));
        return session.createQuery(delete).executeUpdate();
    }
}
