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
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.Link;
import org.sakaiproject.plus.api.repository.LinkRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class LinkRepositoryImpl extends SpringCrudRepositoryImpl<Link, String>  implements LinkRepository {

	@Transactional(readOnly = true)
        @Override
	public Link findByLinkAndContext(String link, Context context) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Link> cr = cb.createQuery(Link.class);
		Root<Link> root = cr.from(Link.class);

		Predicate cond = cb.and(
				cb.equal(root.get("link"), link),
				cb.equal(root.get("context"), context)
		);

		CriteriaQuery<Link> cq = cr.select(root).where(cond);

		Link result = sessionFactory.getCurrentSession()
				.createQuery(cq)
				.uniqueResult();
		return result;
	}
}
