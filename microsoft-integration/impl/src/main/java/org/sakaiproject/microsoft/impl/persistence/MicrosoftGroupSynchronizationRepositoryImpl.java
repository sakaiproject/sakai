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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftGroupSynchronizationRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MicrosoftGroupSynchronizationRepositoryImpl extends BasicSerializableRepository<GroupSynchronization, String> implements MicrosoftGroupSynchronizationRepository {

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public Optional<GroupSynchronization> findById(String id) {
		GroupSynchronization groupSynchronization = (GroupSynchronization) startCriteriaQuery().add(Restrictions.eq("id", id)).uniqueResult();
		return Optional.ofNullable(groupSynchronization);
	}
	
	@Override
	public Optional<GroupSynchronization> findByGroupChannel(String groupId, String channelId) {
		GroupSynchronization groupSynchronization = (GroupSynchronization) startCriteriaQuery()
				.add(Restrictions.eq("groupId", groupId))
				.add(Restrictions.eq("channelId", channelId))
				.uniqueResult();
		return Optional.ofNullable(groupSynchronization);
	}
	
	@Override
	public List<GroupSynchronization> findBySiteSynchronizationId(String siteSynchronizationId) {
		return (List<GroupSynchronization>)startCriteriaQuery()
		.add(Restrictions.eq("siteSynchronization.id", siteSynchronizationId))
		.list();
	}
	
	@Override
	public List<GroupSynchronization> findByGroup(String groupId) {
		return (List<GroupSynchronization>)startCriteriaQuery()
		.add(Restrictions.eq("groupId", groupId))
		.list();
	}
	
	@Override
	public long countGroupSynchronizationsByChannelId(String channelId) {
		Criteria criteria = startCriteriaQuery()
				.setProjection(Projections.countDistinct("id"))
				.add(Restrictions.eq("channelId", channelId));
		
		return ((Number) criteria.uniqueResult()).longValue();
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
