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

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.accountvalidator.api.model.ValidationAccount;
import org.sakaiproject.accountvalidator.api.repository.ValidationAccountRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.time.Instant;
import org.sakaiproject.accountvalidator.api.model.PasswordReset;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ValidationAccountRepository using Spring's repository pattern.
 * Extends SpringCrudRepositoryImpl for standard CRUD operations and implements
 * custom query methods using Hibernate Criteria API.
 */
@Slf4j
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

        String id = org.sakaiproject.entitybroker.EntityReference.getIdFromRef(userId);
        String reference = org.sakaiproject.user.api.UserDirectoryService.REFERENCE_ROOT + "/" + id;
        query.select(root)
                .where(root.get("userId").in(id, reference));

        List<ValidationAccount> results = sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
        if (results.size() > 1) {
            log.warn("Multiple validation accounts match user {}", id);
        }
        return results.stream()
                .filter(account -> userId.equals(account.getUserId()))
                .findFirst()
                .or(() -> results.stream().findFirst());
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
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean consumePasswordReset(String token, Instant earliestSent, Instant received) {
        return sessionFactory.getCurrentSession().createMutationQuery(
                "update PasswordReset set consumedAt = :received, version = version + 1 "
                + "where token = :token and consumedAt is null and createdAt >= :earliestSent")
                .setParameter("received", received)
                .setParameter("token", token)
                .setParameter("earliestSent", earliestSent)
                .executeUpdate() == 1;
    }

    @Override
    public Optional<PasswordReset> findPasswordResetByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return sessionFactory.getCurrentSession().createQuery(
                "from PasswordReset where token = :token", PasswordReset.class)
                .setParameter("token", token).uniqueResultOptional();
    }

    @Override
    public Optional<PasswordReset> findPasswordResetByUserId(String userId) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().find(PasswordReset.class, userId));
    }

    @Override
    public PasswordReset lockPasswordReset(String userId) {
        return sessionFactory.getCurrentSession().find(PasswordReset.class, userId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public PasswordReset savePasswordReset(PasswordReset reset) {
        PasswordReset managed = sessionFactory.getCurrentSession().merge(reset);
        sessionFactory.getCurrentSession().flush();
        return managed;
    }

}
