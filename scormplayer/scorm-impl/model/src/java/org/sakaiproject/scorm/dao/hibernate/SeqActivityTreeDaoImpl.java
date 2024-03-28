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
import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.SeqActivityTree;

import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.SeqActivityTreeSnapshot;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

@Slf4j
public class SeqActivityTreeDaoImpl extends HibernateDaoSupport implements SeqActivityTreeDao
{
	@Override
	public ISeqActivityTree find(long contentPackageId, String userId)
	{
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery("from " + SeqActivityTree.class.getName() + " where contentPackageId=:cpid and mLearnerID=:lid")
				.setParameter("cpid", contentPackageId)
				.setParameter("lid", userId)
				.getResultList();

		log.info("SeqActivityTreeDAO::find: records: {}", r.size());

		if (r.isEmpty())
		{
			return null;
		}

		return (ISeqActivityTree) r.get(0);
	}

	public SeqActivityTreeSnapshot findSnapshot(String courseId, String userId)
	{
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery("from " + SeqActivityTreeSnapshot.class.getName() + " where mCourseID=:cid and mLearnerID=:lid")
				.setParameter("cid", courseId)
				.setParameter("lid", userId)
				.getResultList();

		log.info("SeqActivityTreeDAO::findSnapshot: records: {}", r.size());

		if (r.isEmpty())
		{
			return null;
		}

		return (SeqActivityTreeSnapshot) r.get(0);
	}

	public List<SeqActivityTreeSnapshot> findUserSnapshots(String userId)
	{
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery("from " + SeqActivityTreeSnapshot.class.getName() + " where mLearnerID=:lid")
				.setParameter("lid", userId)
				.getResultList();

		log.info("SeqActivityTreeDAO::findUserSnapshots: records: {}", r.size());

		return r;
	}

	@Override
	public void save(ISeqActivityTree tree)
	{
		getHibernateTemplate().saveOrUpdate(tree);
	}
}
