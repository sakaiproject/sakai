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

package org.sakaiproject.user.impl.test;

import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.impl.AuthenticationCache;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.WebDavEvidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class AuthenticationManagerTest extends SakaiKernelTestBase {

    private static final String PASSWORD = "authentication-test-password";
    private static final String ADDRESS = "192.0.2.10";
    private AuthenticationManager authenticationManager;
    private AuthenticationCache authenticationCache;
    private UserDirectoryService userDirectoryService;
    private User user;

    @BeforeClass
    public static void beforeClass() throws Exception {
        oneTimeSetup("AuthenticationCacheTest");
    }

    @Before
    public void setUp() throws Exception {
        authenticationManager = getService(AuthenticationManager.class);
        authenticationCache = getService(AuthenticationCache.class);
        userDirectoryService = getService(UserDirectoryService.class);
        String eid = "auth-" + UUID.randomUUID();
        user = userDirectoryService.addUser(null, eid, "First", "Last", "auth@example.test", PASSWORD, null, null);
    }

    @Test
    public void ordinaryAuthenticationDoesNotPopulateCache() throws Exception {
        Authentication result = authenticationManager.authenticate(new IdPwEvidence(user.getEid(), PASSWORD, ADDRESS));
        assertEquals(user.getId(), result.getUid());
        assertNull(authenticationCache.getAuthentication(user.getEid(), PASSWORD));

        assertThrows(AuthenticationException.class, () ->
                authenticationManager.authenticate(new IdPwEvidence(user.getEid(), "incorrect", ADDRESS)));
        assertNull(authenticationCache.getAuthentication(user.getEid(), "incorrect"));
    }

    @Test
    public void ordinaryAuthenticationIgnoresCachedSuccess() throws Exception {
        Authentication result = authenticationManager.authenticate(new WebDavEvidence(user.getEid(), PASSWORD, ADDRESS));
        authenticationCache.removeAuthentification(user.getEid());
        authenticationCache.putAuthentication(user.getEid(), "incorrect", result);

        assertThrows(AuthenticationException.class, () ->
                authenticationManager.authenticate(new IdPwEvidence(user.getEid(), "incorrect", ADDRESS)));
    }

    @Test
    public void ordinaryAuthenticationIgnoresCachedFailure() throws Exception {
        authenticationCache.putAuthenticationFailure(user.getEid(), PASSWORD);
        Authentication result = authenticationManager.authenticate(new IdPwEvidence(user.getEid(), PASSWORD, ADDRESS));
        assertEquals(user.getId(), result.getUid());
    }

    @Test
    public void webDavChecksRequestAddressOnCacheHits() throws Exception {
        setUserProperty("ip-whitelist", ADDRESS);
        Authentication result = authenticationManager.authenticate(new WebDavEvidence(user.getEid(), PASSWORD, ADDRESS));
        assertEquals(user.getId(), result.getUid());
        assertNotNull(authenticationCache.getAuthentication(user.getEid(), PASSWORD));

        assertThrows(AuthenticationException.class, () -> authenticationManager.authenticate(
                new WebDavEvidence(user.getEid(), PASSWORD, "192.0.2.11")));
        assertEquals(user.getId(), authenticationManager.authenticate(
                new WebDavEvidence(user.getEid(), PASSWORD, ADDRESS)).getUid());
    }

    @Test
    public void webDavChecksDisabledAccountOnCacheHits() throws Exception {
        Authentication result = authenticationManager.authenticate(new WebDavEvidence(user.getEid(), PASSWORD, ADDRESS));
        setUserProperty("disabled", "true");
        // Supply a cache hit independently of the account-change invalidation observer.
        authenticationCache.removeAuthentification(user.getEid());
        authenticationCache.putAuthentication(user.getEid(), PASSWORD, result);
        assertNotNull(authenticationCache.getAuthentication(user.getEid(), PASSWORD));

        assertThrows(AuthenticationException.class, () ->
                authenticationManager.authenticate(new WebDavEvidence(user.getEid(), PASSWORD, ADDRESS)));
    }

    @Test
    public void webDavDiscardsCachedAuthenticationForMissingUser() throws Exception {
        authenticationCache.putAuthentication(user.getEid(), PASSWORD,
                new org.sakaiproject.util.Authentication("missing-user", user.getEid()));
        assertThrows(AuthenticationException.class, () ->
                authenticationManager.authenticate(new WebDavEvidence(user.getEid(), PASSWORD, ADDRESS)));
        assertNull(authenticationCache.getAuthentication(user.getEid(), PASSWORD));
    }

    private void setUserProperty(String name, String value) throws Exception {
        Session session = getService(SessionManager.class).getCurrentSession();
        String previousUserId = session.getUserId();
        session.setUserId("admin");
        try {
            UserEdit edit = userDirectoryService.editUser(user.getId());
            edit.getProperties().addProperty(name, value);
            userDirectoryService.commitEdit(edit);
        } finally {
            session.setUserId(previousUserId);
        }
    }
}
