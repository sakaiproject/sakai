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

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.repository.LetterGradePercentMappingRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LetterGradePercentMappingRepositoryImpl extends SpringCrudRepositoryImpl<LetterGradePercentMapping, Long>  implements LetterGradePercentMappingRepository {

    @Transactional(readOnly = true)
    public List<LetterGradePercentMapping> findByMappingType(Integer mappingType) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LetterGradePercentMapping> query = cb.createQuery(LetterGradePercentMapping.class);
        Root<LetterGradePercentMapping> pm = query.from(LetterGradePercentMapping.class);
        query.where(cb.equal(pm.get("mappingType"), mappingType));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<LetterGradePercentMapping> findByGradebookIdAndMappingType(Long gradebookId, Integer mappingType) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LetterGradePercentMapping> query = cb.createQuery(LetterGradePercentMapping.class);
        Root<LetterGradePercentMapping> pm = query.from(LetterGradePercentMapping.class);
        query.where(cb.and(cb.equal(pm.get("gradebookId"), gradebookId), cb.equal(pm.get("mappingType"), mappingType)));
        return session.createQuery(query).uniqueResultOptional();
    }
}
