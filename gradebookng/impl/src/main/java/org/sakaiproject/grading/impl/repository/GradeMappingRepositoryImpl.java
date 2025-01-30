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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.repository.GradeMappingRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradeMappingRepositoryImpl extends SpringCrudRepositoryImpl<GradeMapping, Long>  implements GradeMappingRepository {

    @Transactional(readOnly = true)
    public List<GradeMapping> findByGradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradeMapping> query = cb.createQuery(GradeMapping.class);
        Join<GradeMapping, Gradebook> gb = query.from(GradeMapping.class).join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }

}
