/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.user.impl;

import java.util.function.Supplier;

import jakarta.persistence.OptimisticLockException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.user.api.PreferencesConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Each operation finishes its transaction before an edit is returned or reported as saved. */
public class PreferencesRepository {

    private final SessionFactory sessionFactory;
    private final TransactionTemplate transactions;

    public PreferencesRepository(SessionFactory sessionFactory, PlatformTransactionManager transactionManager) {
        this.sessionFactory = sessionFactory;
        transactions = new TransactionTemplate(transactionManager);
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public PreferencesRecord find(String id) {
        return transactions.execute(status -> sessionFactory.getCurrentSession().find(PreferencesRecord.class, id));
    }

    public PreferencesRecord save(PreferencesRecord record) {
        return write(record, () -> {
            Session session = sessionFactory.getCurrentSession();
            if (record.getVersion() == null) {
                session.persist(record);
                return record;
            }
            return session.merge(record);
        });
    }

    public void remove(PreferencesRecord record) {
        write(record, () -> {
            Session session = sessionFactory.getCurrentSession();
            session.remove(session.merge(record));
            return null;
        });
    }

    private <T> T write(PreferencesRecord record, Supplier<T> operation) {
        // Hibernate may assign the initial version before a failed insert is flushed.
        boolean inserting = record.getVersion() == null;
        try {
            return transactions.execute(status -> operation.get());
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            // Stale merges fail in JPA; conflicts detected at transaction commit are translated by Spring.
            throw new PreferencesConflictException(record.getId(), e);
        } catch (DataIntegrityViolationException e) {
            if (inserting && find(record.getId()) != null) {
                throw new PreferencesConflictException(record.getId(), e);
            }
            throw e;
        }
    }
}
