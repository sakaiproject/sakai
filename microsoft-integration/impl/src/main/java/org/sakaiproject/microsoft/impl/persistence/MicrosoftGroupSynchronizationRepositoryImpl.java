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


import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftGroupSynchronizationRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MicrosoftGroupSynchronizationRepositoryImpl extends BasicSerializableRepository<GroupSynchronization, String> implements MicrosoftGroupSynchronizationRepository {

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public Optional<GroupSynchronization> findById(String id) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<GroupSynchronization> cq = cb.createQuery(GroupSynchronization.class);
		Root<GroupSynchronization> root = cq.from(GroupSynchronization.class);
		cq.where(cb.equal(root.get("id"), id));
		return Optional.ofNullable(sessionFactory.getCurrentSession().createQuery(cq).uniqueResult());
	}
	
	@Override
	public Optional<GroupSynchronization> findByGroupChannel(String groupId, String channelId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<GroupSynchronization> cq = cb.createQuery(GroupSynchronization.class);
		Root<GroupSynchronization> root = cq.from(GroupSynchronization.class);
		cq.where(
			cb.equal(root.get("groupId"), groupId),
			cb.equal(root.get("channelId"), channelId)
		);
		return Optional.ofNullable(sessionFactory.getCurrentSession().createQuery(cq).uniqueResult());
	}
	
	@Override
	public List<GroupSynchronization> findBySiteSynchronizationId(String siteSynchronizationId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<GroupSynchronization> cq = cb.createQuery(GroupSynchronization.class);
		Root<GroupSynchronization> root = cq.from(GroupSynchronization.class);
		cq.where(cb.equal(root.get("siteSynchronization").get("id"), siteSynchronizationId));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}
	
	@Override
	public List<GroupSynchronization> findByGroup(String groupId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<GroupSynchronization> cq = cb.createQuery(GroupSynchronization.class);
		Root<GroupSynchronization> root = cq.from(GroupSynchronization.class);
		cq.where(cb.equal(root.get("groupId"), groupId));
		return sessionFactory.getCurrentSession().createQuery(cq).list();
	}
	
	@Override
	public long countGroupSynchronizationsByChannelId(String channelId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<GroupSynchronization> root = cq.from(GroupSynchronization.class);
		cq.select(cb.countDistinct(root.get("id")));
		cq.where(cb.equal(root.get("channelId"), channelId));
		return sessionFactory.getCurrentSession().createQuery(cq).uniqueResult();
	}
	
	@Override
	public Integer deleteBySiteSynchronizationId(String siteSynchronizationId) {
		Session session = sessionFactory.getCurrentSession();

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<GroupSynchronization> delete = cb.createCriteriaDelete(GroupSynchronization.class);
		Root<GroupSynchronization> groupSynchronization = delete.from(GroupSynchronization.class);
		delete.where(cb.equal(groupSynchronization.get("siteSynchronization").get("id"), siteSynchronizationId));
		
		return session.createQuery(delete).executeUpdate();
	}
}
