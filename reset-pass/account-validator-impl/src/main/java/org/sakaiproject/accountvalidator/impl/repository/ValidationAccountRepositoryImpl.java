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

import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.repository.ValidationAccountRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

/**
 * Implementation of ValidationAccountRepository using Spring's repository pattern.
 * Extends SpringCrudRepositoryImpl for standard CRUD operations and implements
 * custom query methods using Hibernate Criteria API.
 */
public class ValidationAccountRepositoryImpl extends SpringCrudRepositoryImpl<ValidationAccount, Long> implements ValidationAccountRepository {

	@Override
	public Optional<ValidationAccount> findByValidationToken(String validationToken) {
		if (validationToken == null) {
			return Optional.empty();
		}

		Criteria criteria = startCriteriaQuery();
		criteria.add(Restrictions.eq("validationToken", validationToken));

		ValidationAccount result = (ValidationAccount) criteria.uniqueResult();
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<ValidationAccount> findByUserId(String userId) {
		if (userId == null) {
			return Optional.empty();
		}

		Criteria criteria = startCriteriaQuery();
		criteria.add(Restrictions.eq("userId", userId));

		ValidationAccount result = (ValidationAccount) criteria.uniqueResult();
		return Optional.ofNullable(result);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ValidationAccount> findByStatus(Integer status) {
		if (status == null) {
			return List.of();
		}

		Criteria criteria = startCriteriaQuery();
		criteria.add(Restrictions.eq("status", status));

		return criteria.list();
	}
}
