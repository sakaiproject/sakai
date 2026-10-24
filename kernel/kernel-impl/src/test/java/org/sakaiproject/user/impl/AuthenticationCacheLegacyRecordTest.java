package org.sakaiproject.user.impl;

import org.junit.Test;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.junit.BeforeClass;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
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

           MemoryService memory = getService(MemoryService.class);
           Cache<String, AuthenticationCache.AuthenticationRecord> records =
            memory.getCache("org.sakaiproject.user.api.AuthenticationManager");
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

                AuthenticationCache.AuthenticationRecord before = 
                    records.get(eid);

                assertNotNull(before);

                assertEquals(28, before.encodedPassword.length);
            

                // when we call  this below we expect to refuse historical cache hit
                Authentication result = authManager.authenticate(
                    new WebDavEvidence(user.getEid(), password, null)
                );
                assertEquals(user.getId(), result.getUid());

                AuthenticationCache.AuthenticationRecord stored
                    = records.get(user.getEid());


                assertEquals(48, stored.encodedPassword.length);

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