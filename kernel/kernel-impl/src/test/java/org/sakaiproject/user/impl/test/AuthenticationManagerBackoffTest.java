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
package org.sakaiproject.user.impl.test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.ExpiryPolicy;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.impl.PasswordLoginBackoff;
import org.sakaiproject.user.impl.AuthenticationCache;
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.WebDavEvidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/** Exercises the real authenticator, user storage, credential cache and shared backoff. */
public class AuthenticationManagerBackoffTest extends SakaiKernelTestBase {
    private AuthenticationManager authenticationManager;
    private PasswordLoginBackoff backoff;
    private UserDirectoryService users;
    private User user;
    private Instant now;
    private static final String PASSWORD = "guest-password";

    @BeforeClass
    public static void startKernel() throws Exception {
        oneTimeSetup("password_login_backoff");
    }

    @Before
    public void setUp() throws Exception {
        authenticationManager = getService(AuthenticationManager.class);
        backoff = getService(PasswordLoginBackoff.class);
        users = getService(UserDirectoryService.class);
        user = users.addUser(null, "guest-" + UUID.randomUUID(), "Guest", "User",
                "guest@example.com", PASSWORD, null, null);
        now = Instant.now();
        advanceSeconds(0);
    }

    @After
    public void restoreClock() {
        backoff.setClock(Clock.systemUTC());
    }

    @Test
    public void xmlCacheConfigurationPreservesAtomicBackoffAndConfiguredExpiry() {
        Ignite ignite = (Ignite) getService("org.sakaiproject.ignite.SakaiIgnite");
        CacheConfiguration<?, ?> configuration = ignite.cache(PasswordLoginBackoff.class.getName())
                .getConfiguration(CacheConfiguration.class);
        assertEquals(CacheAtomicityMode.ATOMIC, configuration.getAtomicityMode());
        assertEquals(CacheMode.REPLICATED, configuration.getCacheMode());
        assertEquals(CacheWriteSynchronizationMode.FULL_SYNC, configuration.getWriteSynchronizationMode());
        assertFalse(configuration.isReadFromBackup());
        ExpiryPolicy expiry = configuration.getExpiryPolicyFactory().create();
        // The test overrides the normal 900-second window, exercising XML property resolution.
        assertEquals(TimeUnit.SECONDS, expiry.getExpiryForUpdate().getTimeUnit());
        assertEquals(1200, expiry.getExpiryForUpdate().getDurationAmount());
        assertNull(expiry.getExpiryForAccess());
    }

    @Test
    public void pausesAfterFiveGuessesAndRejectedRequestsDoNotExtendPause() throws Exception {
        failPasswords(user.getEid(), 5);
        assertRejected(user.getEid(), PASSWORD);
        advanceSeconds(1);
        assertRejected(user.getEid(), PASSWORD);
        advanceSeconds(1);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
    }

    @Test
    public void delaysIncreaseAndStopAtThirtySeconds() throws Exception {
        failPasswords(user.getEid(), 5);
        for (int delay : new int[] {2, 4, 8, 16, 30, 30}) {
            advanceSeconds(delay - 1);
            assertRejected(user.getEid(), PASSWORD);
            advanceSeconds(1);
            assertRejected(user.getEid(), "wrong-at-" + now);
        }
        advanceSeconds(30);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
    }

    @Test
    public void successfulAndCachedLoginsClearHistory() throws Exception {
        failPasswords(user.getEid(), 4);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
        assertEquals(user.getId(), webDavLogin(user.getEid(), PASSWORD));
        // This second WebDAV success uses the existing credential cache.
        assertEquals(user.getId(), webDavLogin(user.getEid(), PASSWORD));
        failPasswords(user.getEid(), 4);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
    }

    @Test
    public void quietPeriodExpiresHistory() throws Exception {
        failPasswords(user.getEid(), 5);
        advanceSeconds(1);
        assertRejected(user.getEid(), PASSWORD);
        int windowSeconds = getService(ServerConfigurationService.class).getInt("login.password.backoff.windowSeconds", 900);
        advanceSeconds(windowSeconds - 1);
        failPasswords(user.getEid(), 4);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
    }

    @Test
    public void caseAndCleanedIdentifierVariantsShareAnAccountLimit() throws Exception {
        failPasswords(user.getEid(), 4);
        assertRejected(" <" + user.getEid().toUpperCase(Locale.ROOT) + "> ", "wrong");
        assertRejected(user.getEid(), PASSWORD);
        advanceSeconds(2);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
    }

    @Test
    public void browserAndWebDavShareTheSameAllowance() throws Exception {
        failPasswords(user.getEid(), 4);
        assertWebDavRejected(user.getEid(), "wrong-webdav-password");
        assertRejected(user.getEid(), PASSWORD);
        assertWebDavRejected(user.getEid(), PASSWORD);
        advanceSeconds(2);
        assertEquals(user.getId(), login(user.getEid(), PASSWORD));
    }

    @Test
    public void repeatedCachedFailuresAndUnknownUsersStillConsumeAttempts() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            assertWebDavRejected(user.getEid(), "same-wrong-password");
        }
        String blockedMessage = assertWebDavRejected(user.getEid(), PASSWORD);
        String unknown = "missing-" + UUID.randomUUID();
        assertEquals(blockedMessage, assertWebDavRejected(unknown, "wrong"));
        for (int attempt = 0; attempt < 4; attempt++) {
            assertWebDavRejected(unknown, "wrong");
        }
        assertEquals(blockedMessage, assertWebDavRejected(unknown, "wrong"));
    }

    @Test
    public void pausedRequestsCannotEvictCredentialCacheEntries() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            assertWebDavRejected(user.getEid(), "wrong-" + attempt);
        }
        assertWebDavRejected(user.getEid(), "different-password-during-pause");
        AuthenticationCache credentialCache = getService(AuthenticationCache.class);
        AuthenticationException failure = assertThrows(AuthenticationException.class,
                () -> credentialCache.getAuthentication(user.getEid(), "wrong-4"));
        assertEquals("repeated invalid login", failure.getMessage());
    }

    @Test
    public void unavailableBackoffRejectsPasswordsButDoesNotBlockTrustedSso() throws Exception {
        Ignite ignite = (Ignite) getService("org.sakaiproject.ignite.SakaiIgnite");
        ignite.cache(PasswordLoginBackoff.class.getName()).close();
        try {
            assertRejected(user.getEid(), PASSWORD);
            assertEquals(user.getId(), authenticationManager.authenticate(new ExternalTrustedEvidence(user.getEid())).getUid());
        } finally {
            backoff.init();
        }
    }

    @Test
    public void anotherAccountOnTheSameAddressAndTrustedSsoAreUnaffected() throws Exception {
        failPasswords(user.getEid(), 5);
        assertEquals(user.getId(), authenticationManager.authenticate(new ExternalTrustedEvidence(user.getEid())).getUid());
        User other = users.addUser(null, "other-" + UUID.randomUUID(), "Other", "Guest",
                "other@example.com", PASSWORD, null, null);
        assertEquals(other.getId(), login(other.getEid(), PASSWORD));
        // SSO must not clear or bypass the password policy for a later password request.
        assertRejected(user.getEid(), PASSWORD);
    }

    @Test
    public void localAdministratorHasTheSamePolicy() throws Exception {
        failPasswords("admin", 5);
        assertRejected("admin", "admin");
        assertEquals("admin", authenticationManager.authenticate(new ExternalTrustedEvidence("admin")).getUid());
        advanceSeconds(2);
        assertEquals("admin", login("admin", "admin"));
    }

    @Test
    public void parallelRequestsWithCachedValidCredentialsAreNotThrottled() throws Exception {
        assertEquals(user.getId(), webDavLogin(user.getEid(), PASSWORD));
        ExecutorService executor = Executors.newFixedThreadPool(12);
        try {
            List<Callable<String>> attempts = new ArrayList<>();
            for (int attempt = 0; attempt < 40; attempt++) {
                attempts.add(() -> webDavLogin(user.getEid(), PASSWORD));
            }
            for (Future<String> result : executor.invokeAll(attempts)) {
                assertEquals(user.getId(), result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void concurrentReservationsCannotExceedTheInitialAllowance() throws Exception {
        String key = "concurrent-" + UUID.randomUUID();
        ExecutorService executor = Executors.newFixedThreadPool(12);
        try {
            List<Callable<Boolean>> attempts = new ArrayList<>();
            for (int attempt = 0; attempt < 40; attempt++) {
                attempts.add(() -> {
                    try {
                        backoff.reserve(key);
                        return true;
                    } catch (AuthenticationException e) {
                        return false;
                    }
                });
            }
            int admitted = 0;
            for (Future<Boolean> result : executor.invokeAll(attempts)) {
                if (result.get()) {
                    admitted++;
                }
            }
            assertEquals(5, admitted);
        } finally {
            executor.shutdownNow();
            backoff.clear(key);
        }
    }

    private String login(String identifier, String password) throws AuthenticationException {
        return authenticationManager.authenticate(new IdPwEvidence(identifier, password, "192.0.2.1")).getUid();
    }

    private String webDavLogin(String identifier, String password) throws AuthenticationException {
        return authenticationManager.authenticate(new WebDavEvidence(identifier, password, "192.0.2.1")).getUid();
    }

    private String assertRejected(String identifier, String password) {
        return assertThrows(AuthenticationException.class, () -> login(identifier, password)).getMessage();
    }

    private String assertWebDavRejected(String identifier, String password) {
        return assertThrows(AuthenticationException.class, () -> webDavLogin(identifier, password)).getMessage();
    }

    private void failPasswords(String identifier, int count) throws Exception {
        for (int attempt = 0; attempt < count; attempt++) {
            assertRejected(identifier, "wrong-" + attempt);
        }
    }

    private void advanceSeconds(long seconds) {
        now = now.plusSeconds(seconds);
        backoff.setClock(Clock.fixed(now, ZoneOffset.UTC));
    }
}
