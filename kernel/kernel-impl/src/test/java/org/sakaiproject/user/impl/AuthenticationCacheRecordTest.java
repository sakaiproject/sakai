/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.user.api.Authentication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AuthenticationCacheRecordTest extends SakaiKernelTestBase {

    private static final String IDENTIFIER = "cache-record-user";
    private static final String PASSWORD = "cache-record-password";
    private AuthenticationCache authenticationCache;
    private Cache records;
    private final Authentication authentication = new org.sakaiproject.util.Authentication("cache-user-id", IDENTIFIER);

    @BeforeClass
    public static void beforeClass() throws Exception {
        oneTimeSetup("AuthenticationCacheTest");
    }

    @Before
    public void setUp() {
        authenticationCache = getService(AuthenticationCache.class);
        CacheManager cacheManager = (CacheManager) getService("org.sakaiproject.ignite.SakaiCacheManager");
        records = cacheManager.getCache("org.sakaiproject.user.api.AuthenticationManager");
        records.evict(IDENTIFIER);
    }

    @Test
    public void expiresAfterFiveMinutesWithoutExtendingOnAccess() {
        IgniteCache<?, ?> nativeCache = (IgniteCache<?, ?>) records.getNativeCache();
        CacheConfiguration<?, ?> configuration = nativeCache.getConfiguration(CacheConfiguration.class);
        ExpiryPolicy policy = configuration.getExpiryPolicyFactory().create();
        assertEquals(Duration.FIVE_MINUTES, policy.getExpiryForCreation());
        assertNull(policy.getExpiryForAccess());
        assertNull(policy.getExpiryForUpdate());
        assertEquals(CacheMode.REPLICATED, configuration.getCacheMode());

        CacheManager cacheManager = (CacheManager) getService("org.sakaiproject.ignite.SakaiCacheManager");
        IgniteCache<?, ?> userCache = (IgniteCache<?, ?>) cacheManager
                .getCache("org.sakaiproject.user.api.UserDirectoryService").getNativeCache();
        CacheConfiguration<?, ?> userConfiguration = userCache.getConfiguration(CacheConfiguration.class);
        assertEquals(new Duration(TimeUnit.MINUTES, 120),
                userConfiguration.getExpiryPolicyFactory().create().getExpiryForCreation());
    }

    @Test
    public void storesSaltedSha256Records() throws Exception {
        authenticationCache.putAuthentication(IDENTIFIER, PASSWORD, authentication);
        byte[] encoded = records.get(IDENTIFIER, AuthenticationCache.AuthenticationRecord.class).encodedPassword;
        assertEquals(48, encoded.length);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(encoded, 0, 16);
        assertArrayEquals(digest.digest(PASSWORD.getBytes(StandardCharsets.UTF_8)), Arrays.copyOfRange(encoded, 16, encoded.length));
        assertEquals(IDENTIFIER, authenticationCache.getAuthentication(IDENTIFIER, PASSWORD).getEid());

        authenticationCache.removeAuthentification(IDENTIFIER);
        authenticationCache.putAuthentication(IDENTIFIER, PASSWORD, authentication);
        byte[] replacement = records.get(IDENTIFIER, AuthenticationCache.AuthenticationRecord.class).encodedPassword;
        assertFalse(Arrays.equals(Arrays.copyOf(encoded, 16), Arrays.copyOf(replacement, 16)));
        assertEquals(IDENTIFIER, authenticationCache.getAuthentication(IDENTIFIER, PASSWORD).getEid());
    }

    @Test
    public void retainsExistingRecordUntilInvalidated() throws Exception {
        authenticationCache.putAuthentication(IDENTIFIER, PASSWORD, authentication);
        byte[] original = records.get(IDENTIFIER, AuthenticationCache.AuthenticationRecord.class).encodedPassword;
        authenticationCache.putAuthenticationFailure(IDENTIFIER, "another-password");
        assertArrayEquals(original, records.get(IDENTIFIER, AuthenticationCache.AuthenticationRecord.class).encodedPassword);
        assertEquals(IDENTIFIER, authenticationCache.getAuthentication(IDENTIFIER, PASSWORD).getEid());
    }

}
