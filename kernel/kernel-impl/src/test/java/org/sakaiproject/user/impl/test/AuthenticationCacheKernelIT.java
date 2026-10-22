package org.sakaiproject.user.impl.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.impl.AuthenticationCache;
import org.sakaiproject.util.WebDavEvidence;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.sql.ResultSet;
import org.sakaiproject.user.impl.PasswordService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import static org.junit.Assert.*;
import java.util.List;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.impl.DbUserService;
import org.sakaiproject.user.impl.UserAuthnComponent;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;

public class AuthenticationCacheKernelIT
        extends SakaiKernelTestBase {

    private AuthenticationManager authManager;
    private AuthenticationCache authCache;
    private UserDirectoryService users;

    private String eid;
    private String userId;

    private static final String PASSWORD = "TestPassword123!";
    private static final String NEW_PASSWORD = "NewPassword456!";
    private static final String BAD_PASSWORD = "WrongPassword999!";
    private static final String LEGACY_PASSWORD = "admin";

    private static final String LEGACY_HASH =
        "MD5-SALT-SHA256:W2vRdA==:"
      + "8QkvjZDLkqy5RoQUkRfOTG+C2FEhuq4sQyNxP7XKCvg=";


    @BeforeClass
    public static void startKernel() throws Exception {
        oneTimeSetup("AuthenticationCacheTest");
    }

    @Before
    public void setup() throws Exception {
        authManager = (AuthenticationManager)
                getService(AuthenticationManager.class.getName());

        authCache = (AuthenticationCache)
                getService(AuthenticationCache.class.getName());

        users = (UserDirectoryService)
                getService(UserDirectoryService.class.getName());

        assertNotNull(authManager);
        assertNotNull(authCache);
        assertNotNull(users);

        eid = "cachetest-" +
                UUID.randomUUID().toString().substring(0, 8);

        User user = users.addUser(
                null,
                eid,
                "Cache",
                "Tester",
                eid + "@example.org",
                PASSWORD,
                null,
                null
        );

        userId = user.getId();
    }
    @After
    public void cleanup() throws Exception {

        if (eid != null) {
            authCache.removeAuthentification(eid);
        }

        if (userId != null) {
            withUserWritePermission(() -> {
                UserEdit edit = users.editUser(userId);
                users.removeUser(edit);
            });
        }
    }

    private Authentication login(String password)
            throws AuthenticationException {
        return authManager.authenticate(
                new WebDavEvidence(eid, password, null)
        );
    }

    private void expectRejected(String password) {
        try {
            login(password);
            fail("Authentication must be rejected");
        } catch (AuthenticationException expected) {
            // Expected
        }
    }

    @Test
    public void cacheHit() throws Exception {
        final  UserDirectoryService original = users;
            UserAuthnComponent component = (UserAuthnComponent) authManager;

        try {
            // place proxy-n for testing.
            // count calls to authenticate().
            // repeat 3 times login.


            AtomicInteger verificationCount = new AtomicInteger(0);

            UserDirectoryService counting =
                (UserDirectoryService) Proxy.newProxyInstance(
                    UserDirectoryService.class.getClassLoader(),
                    new Class<?>[] { UserDirectoryService.class },
                    (proxy, method, args) -> {

                        if (method.getName().equals("authenticate")
                                && args != null && args.length == 2) {
                            verificationCount.incrementAndGet();
                        }

                        try {
                            return method.invoke(original, args);
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    }
                );
                try {
                    // connect proxy-n.
                    component.setUserDirectoryService(counting);
                    authCache.removeAuthentification(eid);
                    assertNull(authCache.getAuthentication(eid, PASSWORD));
                    
                    // we do three authentications.
                    login(PASSWORD);
                    login(PASSWORD);
                    login(PASSWORD);

                    // we wait for one verification.
                    assertEquals(1, verificationCount.get());

                } finally {
                    // return original service.
                    component.setUserDirectoryService(original);
                }
            
            // after three hits assertEquals(1, verificationCount.get());

        } finally {
            // return original service.
            component.setUserDirectoryService(original);
        }

        Authentication first = login(PASSWORD);
        assertEquals(eid, first.getEid());

        Authentication cached =
                authCache.getAuthentication(eid, PASSWORD);

        assertNotNull("Expected cached authentication", cached);
        assertEquals(eid, cached.getEid());

        Authentication second = login(PASSWORD);
        assertEquals(eid, second.getEid());
    }

    @Test
    public void incorrectPassword() throws Exception {
        login(PASSWORD);

        expectRejected(BAD_PASSWORD);

        // A wrong password must never return a valid identity.
        expectRejected(BAD_PASSWORD);
    }

    @Test
    public void cachedFailure() throws Exception {
        expectRejected(BAD_PASSWORD);

        try {
            authCache.getAuthentication(eid, BAD_PASSWORD);
            fail("Expected cached failure");
        } catch (AuthenticationException expected) {
            // Negative cache entry was found.
        }

        expectRejected(BAD_PASSWORD);
    }

    @Test
    public void recordReplacement() throws Exception {

        Authentication initial = login(PASSWORD);
        assertEquals(eid, initial.getEid());

        withUserWritePermission(() -> {
            UserEdit edit = users.editUser(userId);
            edit.setPassword(NEW_PASSWORD);
            users.commitEdit(edit);
        });

        expectRejected(PASSWORD);

        Authentication updated = login(NEW_PASSWORD);
        assertEquals(eid, updated.getEid());

        expectRejected(PASSWORD);
    }




@Test
public void legacyRecordCompatibility() throws Exception {

    PasswordService passwordService = new PasswordService();

    assertTrue(
        "Historical password fixture is invalid",
        passwordService.check(LEGACY_PASSWORD, LEGACY_HASH)
    );

    // Use the SAME SQL service as the real Sakai kernel.
    SqlService sql = getService(SqlService.class);
    assertNotNull("Sakai SqlService missing", sql);

    assertTrue(
        "Unable to write historical password hash",
        sql.dbWrite(
            "UPDATE SAKAI_USER SET PW = ? WHERE USER_ID = ?",
            new Object[] { LEGACY_HASH, userId }
        )
    );

    // Confirm the actual Sakai SQL service sees the hash.
    List<?> rows = sql.dbRead(
        "SELECT PW FROM SAKAI_USER WHERE USER_ID = ?",
        new Object[] { userId },
        null
    );

    assertEquals("Expected exactly one user", 1, rows.size());
    assertEquals(LEGACY_HASH, rows.get(0));

    // Clear the authentication cache.
    authCache.removeAuthentification(eid);

    // Clear the actual user-directory call cache.
    CacheManager manager = (CacheManager)
        getService("org.sakaiproject.ignite.SakaiCacheManager");

    assertNotNull(manager);

    Cache callCache = manager.getCache(
        "org.sakaiproject.user.api.UserDirectoryService.callCache"
    );

    assertNotNull(callCache);

    String ref = users.userReference(userId);
    callCache.evict(ref);

    assertNull(
        "User cache entry survived eviction",
        callCache.get(ref)
    );

    // Clear thread-local state associated with this user.
    ThreadLocalManager threadLocals =
        getService(ThreadLocalManager.class);

    assertNotNull(threadLocals);
    threadLocals.set(ref, null);

    // Clear the database user ID/EID mapping cache.
    if (users instanceof DbUserService) {
        ((DbUserService) users).getIdEidCache().clear();
    }

    // Reload the record from the real UserDirectoryService.
    UserEdit reloaded = (UserEdit) users.getUserByEid(eid);

    assertEquals(userId, reloaded.getId());
    assertEquals(eid, reloaded.getEid());
    // Diagnose stale password data explicitly.
    assertFalse(
        "UserDirectoryService still uses the original password",
        reloaded.checkPassword(PASSWORD)
    );

    assertTrue(
        "UserDirectoryService did not load the legacy password",
        reloaded.checkPassword(LEGACY_PASSWORD)
    );

    // Authenticate using the actual Sakai kernel.
    Authentication first = login(LEGACY_PASSWORD);

    assertNotNull(first);
    assertEquals(eid, first.getEid());

    // Wrong password must be rejected.
    expectRejected("IncorrectLegacyPassword!");

    // Repeated authentication must still succeed.
    Authentication second = login(LEGACY_PASSWORD);

    assertEquals(eid, second.getEid());
}


    private void withUserWritePermission(
            ThrowingOperation operation) throws Exception {

        SecurityService security =
                getService(SecurityService.class);

        String userReference = users.userReference(userId);

        SecurityAdvisor advisor = (actingUserId, function, reference) -> {
            boolean permittedOperation =
                    UserDirectoryService.SECURE_UPDATE_USER_ANY.equals(function)
                    || UserDirectoryService.SECURE_REMOVE_USER.equals(function);

            if (permittedOperation && userReference.equals(reference)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }

            return SecurityAdvisor.SecurityAdvice.PASS;
        };

        security.pushAdvisor(advisor);

        try {
            operation.run();
        } finally {
            security.popAdvisor(advisor);
        }
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }

}