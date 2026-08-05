/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.impl.persistence;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftSiteSynchronizationRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MicrosoftSiteSynchronizationRepositoryImpl extends BasicSerializableRepository<SiteSynchronization, String> implements MicrosoftSiteSynchronizationRepository {

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public List<SiteSynchronization> findAll(){
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.orderBy(cb.asc(root.get("status")));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}
	
	@Override
	public List<SiteSynchronization> findAllEnabled() {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.where(cb.equal(root.get("disabled"), false));
		cq.orderBy(cb.asc(root.get("status")));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}
	
	@Override
	public Optional<SiteSynchronization> findById(String id) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.where(cb.equal(root.get("id"), id));
		return Optional.ofNullable(sessionFactory.getCurrentSession().createQuery(cq).uniqueResult());
	}
	
	@Override
	public Optional<SiteSynchronization> findBySiteTeam(String siteId, String teamId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.where(
			cb.equal(root.get("siteId"), siteId),
			cb.equal(root.get("teamId"), teamId)
		);
		return Optional.ofNullable(sessionFactory.getCurrentSession().createQuery(cq).uniqueResult());
	}
	
	@Override
	public List<SiteSynchronization> findBySite(String siteId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.where(cb.equal(root.get("siteId"), siteId));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}
	
	@Override
	public List<String> findBySiteIdList(List<String> siteIds) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.select(root.get("siteId"));
		cq.where(root.get("siteId").in(siteIds));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}
	
	@Override
	public List<SiteSynchronization> findByTeam(String teamId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.where(cb.equal(root.get("teamId"), teamId));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}

	@Override
	public List<SiteSynchronization> findByDate(ZonedDateTime fromDate, ZonedDateTime toDate) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSynchronization> cq = cb.createQuery(SiteSynchronization.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		if (Objects.nonNull(fromDate) && Objects.nonNull(toDate)) {
			cq.where(cb.between(root.get("syncDateFrom"), fromDate, toDate));
		} else {
			cq.orderBy(cb.asc(root.get("status")));
		}
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}

	@Override
	public long countSiteSynchronizationsByTeamId(String teamId, boolean forced) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<SiteSynchronization> root = cq.from(SiteSynchronization.class);
		cq.select(cb.countDistinct(root.get("id")));
		if (forced) {
			cq.where(
				cb.equal(root.get("teamId"), teamId),
				cb.equal(root.get("forced"), true)
			);
		} else {
			cq.where(cb.equal(root.get("teamId"), teamId));
		}
		return sessionFactory.getCurrentSession().createQuery(cq).uniqueResult();
	}
	
	@Override
	public Integer deleteSiteSynchronizationsById(List<String> ids) {
		Session session = sessionFactory.getCurrentSession();

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<SiteSynchronization> delete = cb.createCriteriaDelete(SiteSynchronization.class);
		Root<SiteSynchronization> siteSynchronization = delete.from(SiteSynchronization.class);
		delete.where(siteSynchronization.get("id").in(ids));
		
		return session.createQuery(delete).executeUpdate();
	}
}
