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

import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.repository.TenantRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

public class TenantRepositoryImpl extends SpringCrudRepositoryImpl<Tenant, String>  implements TenantRepository {

	public Tenant findByIssuerClientIdAndDeploymentId(String issuer, String clientId, String deploymentId)
	{
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Tenant> cr = cb.createQuery(Tenant.class);
		Root<Tenant> root = cr.from(Tenant.class);

		Predicate cond = cb.and(
				cb.equal(root.get("issuer"), issuer),
				cb.equal(root.get("clientId"), clientId),
				cb.equal(root.get("deploymentId"), deploymentId)
		);

		CriteriaQuery<Tenant> cq = cr.select(root).where(cond);

		Tenant result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;

		/*
		// import org.hibernate.criterion.Order;
		// import org.hibernate.criterion.Restrictions;
		// import org.hibernate.criterion.Projections;

		Tenant retval = (Tenant) sessionFactory.getCurrentSession().createCriteria(Tenant.class)
			.add(Restrictions.eq("issuer", issuer))
			.add(Restrictions.eq("clientId", clientId))
			.add(Restrictions.eq("deploymentId", deploymentId))
			.uniqueResult();
		return retval;
		*/
	}

}
