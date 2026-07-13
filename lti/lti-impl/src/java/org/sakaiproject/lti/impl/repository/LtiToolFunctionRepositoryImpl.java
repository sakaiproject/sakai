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
package org.sakaiproject.lti.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.model.LtiToolFunction;
import org.sakaiproject.lti.api.repository.LtiToolFunctionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiToolFunctionRepositoryImpl extends SpringCrudRepositoryImpl<LtiToolFunction, Long> implements LtiToolFunctionRepository {

    @Transactional(readOnly = true)
    public List<LtiToolFunction> findByTool_Id(Long toolId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiToolFunction> query = cb.createQuery(LtiToolFunction.class);
        Root<LtiToolFunction> function = query.from(LtiToolFunction.class);
        Join<LtiToolFunction, LtiTool> tool = function.join("tool");
        query.where(cb.equal(tool.get("id"), toolId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<LtiToolFunction> findByTool_IdAndFunctionName(Long toolId, String functionName) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiToolFunction> query = cb.createQuery(LtiToolFunction.class);
        Root<LtiToolFunction> function = query.from(LtiToolFunction.class);
        Join<LtiToolFunction, LtiTool> tool = function.join("tool");
        query.where(cb.and(cb.equal(tool.get("id"), toolId),
                           cb.equal(function.get("functionName"), functionName)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional
    public int deleteByTool_Id(Long toolId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<LtiToolFunction> delete = cb.createCriteriaDelete(LtiToolFunction.class);
        Root<LtiToolFunction> function = delete.from(LtiToolFunction.class);
        delete.where(cb.equal(function.get("tool").get("id"), toolId));
        return session.createQuery(delete).executeUpdate();
    }
}
