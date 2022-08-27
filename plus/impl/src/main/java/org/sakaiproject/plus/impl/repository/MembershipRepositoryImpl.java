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
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

import org.sakaiproject.plus.api.model.Membership;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.repository.MembershipRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class MembershipRepositoryImpl extends SpringCrudRepositoryImpl<Membership, Long>  implements MembershipRepository {

	@Transactional(readOnly = true)
	public Membership findBySubjectAndContext(Subject subject, Context context) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Membership> cr = cb.createQuery(Membership.class);
		Root<Membership> root = cr.from(Membership.class);

		Predicate cond = null;
		cond = cb.and(
				cb.equal(root.get("subject"), subject),
				cb.equal(root.get("context"), context)
		);

		CriteriaQuery<Membership> cq = cr.select(root).where(cond);

		Membership result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}

	// TODO: Tell Earle that in this particular area, JPA sucks!
	// TODO: Make sure this f'n does what I think it f'n does given that I think I am f'n forced to do it
	@Transactional
	@Override
	public Membership upsert(Membership entity) {
		if ( entity.getId() != null ) return save(entity);

		// Painfully and inefficiently check for UPSERT
		Membership newEntity = findBySubjectAndContext(entity.getSubject(), entity.getContext());
		if ( newEntity == null ) return save(entity);

		boolean unchanged =
			Objects.equals(entity.getRole(), newEntity.getRole())
			&& Objects.equals(entity.getRoleOverride(), newEntity.getRoleOverride())
		;

		if ( unchanged ) return newEntity;

		// Do the UPDATE variant of UPSERT
		newEntity.setRole(entity.getRole());
		newEntity.setRoleOverride(entity.getRoleOverride());
		return save(newEntity);
	}

	// vim: tabstop=4 noet

}
