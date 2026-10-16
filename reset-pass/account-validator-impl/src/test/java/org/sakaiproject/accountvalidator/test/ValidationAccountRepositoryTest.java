/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.test;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.runner.RunWith;
import org.sakaiproject.accountvalidator.api.model.PasswordReset;
import org.sakaiproject.accountvalidator.api.repository.ValidationAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Verifies committed token consumption against the real Hibernate mappings and repository. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AccountValidationTestConfiguration.class)
public class ValidationAccountRepositoryTest {
    @Autowired private ValidationAccountRepository repository;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private PlatformTransactionManager transactionManager;

    private void cleanUpResets() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                sessionFactory.getCurrentSession().createMutationQuery("delete from PasswordReset").executeUpdate());
    }

    private PasswordReset pendingReset(Instant sent) {
        PasswordReset account = new PasswordReset();
        account.setUserId(UUID.randomUUID().toString());
        account.setToken(UUID.randomUUID().toString());
        account.setCreatedAt(sent);
        return repository.savePasswordReset(account);
    }

    @Test
    public void concurrentConsumersCanCommitOnlyOneConsumption() throws Exception {
        Instant now = Instant.now();
        PasswordReset account = pendingReset(now);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            java.util.concurrent.Callable<Boolean> consume = () -> {
                start.await();
                return repository.consumePasswordReset(account.getToken(), now.minusSeconds(3600), now);
            };
            Future<Boolean> first = executor.submit(consume);
            Future<Boolean> second = executor.submit(consume);
            start.countDown();
            List<Boolean> results = List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
            assertNotNull(repository.findPasswordResetByToken(account.getToken()).orElseThrow().getConsumedAt());
            assertFalse(repository.consumePasswordReset(account.getToken(), now.minusSeconds(3600), now));
        } finally {
            cleanUpResets();
        }
    }

    @Test
    public void expiredTokensCannotBeConsumed() {
        Instant now = Instant.now();
        PasswordReset account = pendingReset(now.minusSeconds(3601));
        try {
            assertFalse(repository.consumePasswordReset(account.getToken(), now.minusSeconds(3600), now));
        } finally {
            cleanUpResets();
        }
    }

    @Test
    public void aStaleQuotaWriteCannotReactivateAConsumedToken() {
        Instant now = Instant.now();
        PasswordReset stale = pendingReset(now);
        assertTrue(repository.consumePasswordReset(stale.getToken(), now.minusSeconds(3600), now));
        stale.getRequests().add(now);
        try {
            repository.savePasswordReset(stale);
            fail("An outdated quota update must not overwrite consumed state");
        } catch (org.springframework.dao.OptimisticLockingFailureException | jakarta.persistence.OptimisticLockException expected) {
            assertNotNull(repository.findPasswordResetByToken(stale.getToken()).orElseThrow().getConsumedAt());
        } finally {
            cleanUpResets();
        }
    }
}
