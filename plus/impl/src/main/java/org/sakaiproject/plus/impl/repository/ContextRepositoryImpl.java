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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.repository.ContextRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ContextRepositoryImpl extends SpringCrudRepositoryImpl<Context, String>  implements ContextRepository {

	@Transactional(readOnly = true)
    @Override
	public List<Context> findByTenant(Tenant tenant) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Context> cr = cb.createQuery(Context.class);
		Root<Context> root = cr.from(Context.class);

		Predicate cond = cb.equal(root.get("tenant"), tenant);
		CriteriaQuery<Context> cq = cr.select(root).where(cb.equal(root.get("tenant"), tenant));

		List<Context> result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.list();
		return result;
	}

	@Transactional(readOnly = true)
	@Override
	public Context findByContextAndTenant(String context, Tenant tenant) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Context> cr = cb.createQuery(Context.class);
		Root<Context> root = cr.from(Context.class);

		Predicate cond = cb.and(
				cb.equal(root.get("context"), context),
				cb.equal(root.get("tenant"), tenant)
		);

		CriteriaQuery<Context> cq = cr.select(root).where(cond);

		Context result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}

	@Transactional(readOnly = true)
	@Override
	public Context findBySakaiSiteId(String sakaiSiteId) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Context> cr = cb.createQuery(Context.class);
		Root<Context> root = cr.from(Context.class);

		Predicate cond = cb.equal(root.get("sakaiSiteId"), sakaiSiteId);

		CriteriaQuery<Context> cq = cr.select(root).where(cond);

		Context result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}

}
