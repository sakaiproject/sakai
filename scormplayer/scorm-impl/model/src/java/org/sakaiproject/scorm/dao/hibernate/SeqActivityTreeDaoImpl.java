/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.dao.hibernate;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.SeqActivityTree;

import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.SeqActivityTreeSnapshot;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class SeqActivityTreeDaoImpl implements SeqActivityTreeDao
{
	@Setter private SessionFactory sessionFactory;

	@Override
	public ISeqActivityTree find(long contentPackageId, String userId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SeqActivityTree> query = cb.createQuery(SeqActivityTree.class);
		Root<SeqActivityTree> root = query.from(SeqActivityTree.class);
		query.select(root).where(cb.equal(root.get("contentPackageId"), contentPackageId), cb.equal(root.get("mLearnerID"), userId));
		List<SeqActivityTree> result = session.createQuery(query).getResultList();
		log.debug("Activity trees found: {}", result.size());
		return result.isEmpty() ? null : result.get(0);
	}

	public SeqActivityTreeSnapshot findSnapshot(String courseId, String userId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SeqActivityTreeSnapshot> query = cb.createQuery(SeqActivityTreeSnapshot.class);
		Root<SeqActivityTreeSnapshot> root = query.from(SeqActivityTreeSnapshot.class);
		query.select(root).where(cb.equal(root.get("mCourseID"), courseId), cb.equal(root.get("mLearnerID"), userId));
		List<SeqActivityTreeSnapshot> result = session.createQuery(query).getResultList();
		log.debug("Activity trees found: {}", result.size());
		return result.isEmpty() ? null : result.get(0);
	}

	public List<SeqActivityTreeSnapshot> findUserSnapshots(String userId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SeqActivityTreeSnapshot> query = cb.createQuery(SeqActivityTreeSnapshot.class);
		Root<SeqActivityTreeSnapshot> root = query.from(SeqActivityTreeSnapshot.class);
		query.select(root).where(cb.equal(root.get("mLearnerID"), userId));
		List<SeqActivityTreeSnapshot> result = session.createQuery(query).getResultList();
		log.debug("Activity trees found: {}", result.size());
		return result;
	}

	@Override
	public void save(ISeqActivityTree tree)
	{
		sessionFactory.getCurrentSession().merge(tree);
	}
}
