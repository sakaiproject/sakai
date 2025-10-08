/**
 * $Id$
 * $URL$
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */
package org.sakaiproject.accountvalidator.impl.repository;

import org.sakaiproject.accountvalidator.api.model.ValidationAccount;
import org.sakaiproject.accountvalidator.api.repository.ValidationAccountRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ValidationAccountRepository using Spring's repository pattern.
 * Extends SpringCrudRepositoryImpl for standard CRUD operations and implements
 * custom query methods using Hibernate Criteria API.
 */
@Transactional
public class ValidationAccountRepositoryImpl extends SpringCrudRepositoryImpl<ValidationAccount, Long> implements ValidationAccountRepository {

	@Override
	public Optional<ValidationAccount> findByValidationToken(String validationToken) {
		if (validationToken == null) return Optional.empty();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<ValidationAccount> query = cb.createQuery(ValidationAccount.class);
        Root<ValidationAccount> root = query.from(ValidationAccount.class);

        query.select(root)
                .where(cb.equal(root.get("validationToken"), validationToken));

        ValidationAccount result = sessionFactory.getCurrentSession()
            .createQuery(query)
            .uniqueResult();

		return Optional.ofNullable(result);
	}

	@Override
	public Optional<ValidationAccount> findByUserId(String userId) {
		if (userId == null) return Optional.empty();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<ValidationAccount> query = cb.createQuery(ValidationAccount.class);
        Root<ValidationAccount> root = query.from(ValidationAccount.class);

        query.select(root)
                .where(cb.equal(root.get("userId"), userId));

        ValidationAccount result = sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();
		return Optional.ofNullable(result);
	}

	@Override
	public List<ValidationAccount> findByStatus(Integer status) {
		if (status == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ValidationAccount> query = cb.createQuery(ValidationAccount.class);
		Root<ValidationAccount> root = query.from(ValidationAccount.class);

		query.select(root)
				.where(cb.equal(root.get("status"), status));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}
}
