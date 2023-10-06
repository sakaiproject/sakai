/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.beans.factory.annotation.Autowired;

public class AssociationRepositoryImpl extends SpringCrudRepositoryImpl<ToolItemRubricAssociation, Long> implements AssociationRepository {

    @Autowired
    private RubricRepository rubricRepository;

    public Optional<ToolItemRubricAssociation> findByToolIdAndItemId(String toolId, String itemId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ToolItemRubricAssociation> query = cb.createQuery(ToolItemRubricAssociation.class);
        Root<ToolItemRubricAssociation> ass = query.from(ToolItemRubricAssociation.class);
        query.where(cb.and(cb.equal(ass.get("toolId"), toolId),
                            cb.equal(ass.get("itemId"), itemId),
                            cb.equal(ass.get("active"), Boolean.TRUE)));

        return session.createQuery(query).uniqueResultOptional();
    }

    public Optional<ToolItemRubricAssociation> findByItemIdAndRubricId(String itemId, Long rubricId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ToolItemRubricAssociation> query = cb.createQuery(ToolItemRubricAssociation.class);
        Root<ToolItemRubricAssociation> ass = query.from(ToolItemRubricAssociation.class);
        query.where(cb.and(cb.equal(ass.get("itemId"), itemId),
                            cb.equal(ass.get("rubric"), rubricId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    public List<ToolItemRubricAssociation> findByRubricId(Long rubricId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ToolItemRubricAssociation> query = cb.createQuery(ToolItemRubricAssociation.class);
        Root<ToolItemRubricAssociation> ass = query.from(ToolItemRubricAssociation.class);
        query.where(cb.equal(ass.get("rubric"), rubricId));

        return session.createQuery(query).list();
    }
	
    public List<ToolItemRubricAssociation> findByItemIdPrefix(String toolId, String itemId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ToolItemRubricAssociation> query = cb.createQuery(ToolItemRubricAssociation.class);
        Root<ToolItemRubricAssociation> ass = query.from(ToolItemRubricAssociation.class);
        query.where(cb.and(cb.equal(ass.get("toolId"), toolId),
                            cb.like(ass.get("itemId"), itemId + "%")));

        return session.createQuery(query).list();
    }

    public ToolItemRubricAssociation save(ToolItemRubricAssociation assoc) {

        Rubric rubric = assoc.getRubric();
        List<ToolItemRubricAssociation> associations = rubric.getAssociations();
        int index = associations.indexOf(assoc);

        if (index != -1) {
            associations.set(index, assoc);
        } else {
            associations.add(assoc);
        }

        rubric = rubricRepository.save(rubric);
        if (index != -1) {
            return rubric.getAssociations().get(index);
        } else {
            return rubric.getAssociations().get(rubric.getAssociations().size() - 1);
        }
    }

    public void delete(ToolItemRubricAssociation assoc) {

        Rubric rubric = assoc.getRubric();
        rubric.getAssociations().remove(assoc);
        rubricRepository.save(rubric);
    }
}
