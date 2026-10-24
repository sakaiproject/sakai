package org.sakaiproject.user.impl;

import org.junit.Test;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.junit.BeforeClass;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.sakaiproject.user.api.UserEdit;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.util.WebDavEvidence;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import java.util.concurrent.atomic.AtomicInteger;
import org.sakaiproject.user.impl.UserAuthnComponent;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;

import java.util.UUID;

public class AuthenticationCacheLegacyRecordTest
        extends SakaiKernelTestBase {
    
        @BeforeClass
        public static void startKernel() throws Exception {
                oneTimeSetup("AuthenticationCacheTest");
        }

        @Test
        public void legacyCacheRecordCompatibility() throws Exception {
            AuthenticationManager authManager = 
                getService(AuthenticationManager.class);

            assertNotNull(authManager);

            AuthenticationCache authCache = 
                getService(AuthenticationCache.class);

            assertNotNull(authCache);

            CacheManager manager = (CacheManager) getService(
                "org.sakaiproject.ignite.SakaiCacheManager"
            );

            assertNotNull(manager);
            
            Cache records = manager.getCache(
                "org.sakaiproject.user.api.AuthenticationManager"
            );
            assertNotNull(authCache);
            assertNotNull(manager);

            assertNotNull(records);


            UserDirectoryService users = getService(
                UserDirectoryService.class
            );

            String eid = "legacy-cache-" + UUID.randomUUID();
            String password = "TestPassword123!";
              
            User user = users.addUser(
                null,
                eid,
                "Cache",
                "Tester",
                eid + "@example.org",
                password,
                null,
                null
            );

            try {
                // after writing the real UserDirectoryService, we use
                Authentication historicalAuth = 
                    new org.sakaiproject.util.Authentication(
                        user.getId(),
                        user.getEid()
                    );

                byte[] salt = new byte[8];
                new SecureRandom().nextBytes(salt);

                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(salt);

                digest.update(password.getBytes(StandardCharsets.UTF_8));

                
                byte[] hash = digest.digest();
                byte[] encodedPassword = 
                    new byte[salt.length + hash.length];

                System.arraycopy(
                    salt,
                    0,
                    encodedPassword,
                    0,
                    salt.length
                );
                
                System.arraycopy(
                    hash,
                    0,
                    encodedPassword,
                    salt.length,
                    hash.length
                );
                assertEquals(28, encodedPassword.length);



                AuthenticationCache.AuthenticationRecord legacyRecord= new AuthenticationCache.AuthenticationRecord(
                    encodedPassword,
                    historicalAuth
                );

                records.put(user.getEid(), legacyRecord);
            
                AtomicInteger verificationCount = new AtomicInteger(0);
                UserDirectoryService original = users;


                UserAuthnComponent component = (UserAuthnComponent) authManager;

                UserDirectoryService counting = 
                    (UserDirectoryService) Proxy.newProxyInstance(
                        UserDirectoryService.class.getClassLoader(),
                        new Class<?>[] { UserDirectoryService.class },
                        (proxy, method, args) -> {

                        if (method.getName().equals("authenticate")
                            && args != null && args.length == 2){
                                verificationCount.incrementAndGet();
                        } 
                        try {
                            return method.invoke(original, args);
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    }
                );
                component.setUserDirectoryService(counting);

                try {
                    // when we call  this below we expect to refuse historical cache hit
                    Authentication result = authManager.authenticate(
                        new WebDavEvidence(user.getEid(), password, null)
                    );
                    assertEquals(1, verificationCount.get());
                    assertEquals(user.getId(), result.getUid());


                    Cache.ValueWrapper wrapper = records.get(user.getEid());

                    assertNotNull(wrapper);
                    Object stored = wrapper.get();
                    assertNotNull(stored);
                    AuthenticationCache.AuthenticationRecord refreshed =
                            (AuthenticationCache.AuthenticationRecord) stored;

                    assertEquals(48, refreshed.encodedPassword.length);
                } finally {
                    component.setUserDirectoryService(original);
                }
            } finally {
                SecurityService security = getService(
                    SecurityService.class
                );

                assertNotNull(security);

                String userReference = users.userReference(
                        user.getId()
                );

                SecurityAdvisor advisor = (actingUserId, function, reference) -> {
                    if (
                        (UserDirectoryService.SECURE_UPDATE_USER_ANY
                            .equals(function) || 
                        UserDirectoryService.SECURE_REMOVE_USER
                            .equals(function)
                        ) && 
                         userReference.equals(reference)
                    ) {
                         return SecurityAdvisor.SecurityAdvice.ALLOWED;
                        }
                        return SecurityAdvisor.SecurityAdvice.PASS;
                    };
                    security.pushAdvisor(advisor);
                    try {
                        try {

                            UserEdit edit = users.editUser(user.getId());
                            users.removeUser(edit);
                        } finally {
                            security.popAdvisor(advisor);
                        }
                    } finally {
                        authCache.removeAuthentification(eid);
                    }
        }
    }

}