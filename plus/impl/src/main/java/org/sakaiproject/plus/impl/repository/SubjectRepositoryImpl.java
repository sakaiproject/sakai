/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.impl.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.repository.SubjectRepository;
import org.sakaiproject.plus.api.repository.ContextRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubjectRepositoryImpl extends SpringCrudRepositoryImpl<Subject, String>  implements SubjectRepository {

    @Autowired private ContextRepository contextRepository;

    @Transactional(readOnly = true)
    @Override
    public Subject findBySubjectAndTenant(String subject, Tenant tenant) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Subject> cr = cb.createQuery(Subject.class);
		Root<Subject> root = cr.from(Subject.class);

		Predicate cond = cb.and(
				cb.equal(root.get("subject"), subject),
				cb.equal(root.get("tenant"), tenant)
		);

		CriteriaQuery<Subject> cq = cr.select(root).where(cond);

		Subject result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}

    @Transactional(readOnly = true)
    @Override
    public Subject findByEmailAndTenant(String email, Tenant tenant) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Subject> cr = cb.createQuery(Subject.class);
		Root<Subject> root = cr.from(Subject.class);

		Predicate cond = cb.and(
				cb.equal(root.get("email"), email),
				cb.equal(root.get("tenant"), tenant)
		);

		CriteriaQuery<Subject> cq = cr.select(root).where(cond);

		Subject result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}

	// This uses the one-to-one betwee sakai sites and contexts to work back to the tenant
    // so we get the correct subject for this particular sakaiUserId
    @Transactional(readOnly = true)
    @Override
	public Subject findBySakaiUserIdAndSakaiSiteId(String sakaiUserId, String siteId) {

		Context context = contextRepository.findBySakaiSiteId(siteId);
		if ( context == null ) return null;
		Tenant tenant = context.getTenant();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Subject> cr = cb.createQuery(Subject.class);
		Root<Subject> root = cr.from(Subject.class);

		Predicate cond = cb.and(
				cb.equal(root.get("sakaiUserId"), sakaiUserId),
				cb.equal(root.get("tenant"), tenant)
		);

		// If bollooxed, this might not return a unique result - While it is a bit of a punt,
		// we will use the most recent subject associated with this SakaiUserId
		CriteriaQuery<Subject> cq = cr
			.select(root)
			.where(cond)
			.orderBy(cb.desc(root.get("modifiedAt")))
		;

		List<Subject> results = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.list();

		if ( results == null || results.size() < 1 ) return null;
		Subject first = results.get(0);
		if ( results.size() == 1 ) return first;

		// One way this can get bolloxed is to edit existing tenant data (like deployment_id)
		// after a bunch of subjects have been created.  In SakaiPlus we see that as the same tenant,
		// but the controlling LMS might generate quite different subjects to keep us
		// from tracking a human across deployments...   Sigh.
		Subject second = results.get(1);
		log.warn("Multiple subjects for siteId={} tenant={} userId={} subject1={} at={} subject2={} at={}",
				siteId, tenant.getId(), sakaiUserId,
				first.getSubject(), first.getCreatedAt(),
				second.getSubject(), second.getCreatedAt() );

		return first;
	}

}
