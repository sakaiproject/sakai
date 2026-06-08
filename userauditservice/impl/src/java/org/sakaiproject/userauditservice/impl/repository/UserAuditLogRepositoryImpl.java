/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.userauditservice.impl.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
import org.sakaiproject.userauditservice.api.repository.UserAuditLogRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserAuditLogRepositoryImpl extends SpringCrudRepositoryImpl<UserAuditLog, Long> implements UserAuditLogRepository {

	@Override
	@Transactional(readOnly = true)
	public long count(UserAuditLogQuery query) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<UserAuditLog> root = criteria.from(UserAuditLog.class);

		criteria.select(cb.count(root)).where(predicates(query, cb, root).toArray(new Predicate[0]));
		return session.createQuery(criteria).uniqueResult();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserAuditLog> find(UserAuditLogQuery query) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<UserAuditLog> criteria = cb.createQuery(UserAuditLog.class);
		Root<UserAuditLog> root = criteria.from(UserAuditLog.class);

		String sortProperty = query.getSortColumn().getEntityProperty();
		criteria.select(root)
				.where(predicates(query, cb, root).toArray(new Predicate[0]))
				.orderBy(query.isSortAscending() ? cb.asc(root.get(sortProperty)) : cb.desc(root.get(sortProperty)),
						cb.asc(root.get("userId")));

		org.hibernate.query.Query<UserAuditLog> hibernateQuery = session.createQuery(criteria);
		if (query.getOffset() > 0) {
			hibernateQuery.setFirstResult(query.getOffset());
		}
		if (query.getLimit() > UserAuditLogQuery.NO_LIMIT) {
			hibernateQuery.setMaxResults(query.getLimit());
		}
		return hibernateQuery.getResultList();
	}

	@Override
	public int deleteBySiteId(String siteId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<UserAuditLog> delete = cb.createCriteriaDelete(UserAuditLog.class);
		Root<UserAuditLog> root = delete.from(UserAuditLog.class);
		delete.where(cb.equal(root.get("siteId"), siteId));
		return session.createQuery(delete).executeUpdate();
	}

	private List<Predicate> predicates(UserAuditLogQuery query, CriteriaBuilder cb, Root<UserAuditLog> root) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("siteId"), query.getSiteId()));
		if (query.getUserId() != null) {
			predicates.add(cb.equal(root.get("userId"), query.getUserId()));
		}
		if (query.getFromAuditStamp() != null) {
			Date fromAuditStamp = Date.from(query.getFromAuditStamp());
			predicates.add(cb.greaterThanOrEqualTo(root.get("auditStamp"), fromAuditStamp));
		}
		if (query.getToAuditStamp() != null) {
			Date toAuditStamp = Date.from(query.getToAuditStamp());
			predicates.add(cb.lessThan(root.get("auditStamp"), toAuditStamp));
		}
		return predicates;
	}
}
