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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

import org.sakaiproject.plus.api.model.Score;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.repository.ScoreRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ScoreRepositoryImpl extends SpringCrudRepositoryImpl<Score, String>  implements ScoreRepository {

	// We are mostly loading individual Scores to Update them if they exist
	@Transactional
    @Override
	public Score findBySubjectAndColumn(Subject subject, Long gradeBookColumn)
	{
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Score> cr = cb.createQuery(Score.class);
		Root<Score> root = cr.from(Score.class);

		Predicate cond = cb.and(cb.equal(root.get("subject"), subject), cb.equal(root.get("gradeBookColumnId"), gradeBookColumn));

		CriteriaQuery<Score> cq = cr.select(root).where(cond);

		Score result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}

	// https://www.baeldung.com/hibernate-criteria-queries
	// https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/criteria-delete.html
    @Override
	public Integer deleteBySubjectAndColumn(Subject subject, Long gradeBookColumn)
	{
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaDelete<Score> cd = cb.createCriteriaDelete(Score.class);
		Root<Score> root = cd.from(Score.class);

		Predicate cond = cb.and(cb.equal(root.get("subject"), subject), cb.equal(root.get("gradeBookColumnId"), gradeBookColumn));

		cd.where(cond);

		 Integer count = sessionFactory.getCurrentSession()
				.createQuery(cd)
				.executeUpdate();
		return count;
	}
}
