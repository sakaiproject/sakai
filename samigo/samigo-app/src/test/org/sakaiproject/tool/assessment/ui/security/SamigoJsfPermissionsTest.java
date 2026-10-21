/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_CREATE_ASSESSMENT;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequestWrapper;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class SamigoJsfPermissionsTest {
    @Test
    public void dispatchAndJsfPhasesSharePermissionsButCheckEachView() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        AtomicInteger refreshes = new AtomicInteger();
        Supplier<Map<String, Boolean>> refresh = () -> {
            refreshes.incrementAndGet();
            return Map.of(AUTHZ_GRADE_ASSESSMENT_ANY + "_site", true);
        };
        Set<String> dispatch = SamigoJsfPermissions.get(request, "user", "site", refresh);
        Set<String> restored = SamigoJsfPermissions.get(new HttpServletRequestWrapper(request), "user", "site", refresh);
        Set<String> rendered = SamigoJsfPermissions.get(request, "user", "site", refresh);

        assertSame(dispatch, restored);
        assertSame(dispatch, rendered);
        assertEquals(1, refreshes.get());
        assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/evaluation/totalScores.jsp", restored::contains));
        assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/qti/importAssessment.jsp", rendered::contains));
    }

    @Test
    public void snapshotIsImmutableAndNewRequestSeesRevokedPermissions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put(AUTHZ_CREATE_ASSESSMENT + "_site", true);
        Set<String> snapshot = SamigoJsfPermissions.get(request, "user", "site", () -> permissions);
        permissions.put(AUTHZ_CREATE_ASSESSMENT + "_site", false);

        assertTrue(snapshot.contains(AUTHZ_CREATE_ASSESSMENT));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertSame(snapshot, SamigoJsfPermissions.get(request, "user", "site", () -> permissions));
        Set<String> nextRequest = SamigoJsfPermissions.get(new MockHttpServletRequest(), "user", "site", () -> permissions);
        assertFalse(nextRequest.contains(AUTHZ_CREATE_ASSESSMENT));
    }

    @Test
    public void siteChangeReloadsAndExcludesOtherSitesAndDeniedPermissions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        AtomicInteger refreshes = new AtomicInteger();
        Supplier<Map<String, Boolean>> refresh = () -> {
            refreshes.incrementAndGet();
            return Map.of(AUTHZ_CREATE_ASSESSMENT + "_first", true,
                AUTHZ_CREATE_ASSESSMENT + "_second", false,
                AUTHZ_GRADE_ASSESSMENT_ANY + "_second", true);
        };
        assertEquals(Set.of(AUTHZ_CREATE_ASSESSMENT), SamigoJsfPermissions.get(request, "user", "first", refresh));
        assertEquals(Set.of(AUTHZ_GRADE_ASSESSMENT_ANY), SamigoJsfPermissions.get(request, "user", "second", refresh));
        assertEquals(2, refreshes.get());
    }

    @Test
    public void userChangeReloadsIncludingAnonymousUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SamigoJsfPermissions.get(request, "instructor", "site", () -> Map.of(AUTHZ_CREATE_ASSESSMENT + "_site", true));
        Set<String> student = SamigoJsfPermissions.get(request, "student", "site", Map::of);
        assertTrue(student.isEmpty());
        AtomicInteger refreshes = new AtomicInteger();
        Set<String> anonymous = SamigoJsfPermissions.get(request, null, "site", () -> {
            refreshes.incrementAndGet();
            return Map.of();
        });
        assertTrue(anonymous.isEmpty());
        assertSame(anonymous, SamigoJsfPermissions.get(request, null, "site", Map::of));
        assertEquals(1, refreshes.get());
    }

    @Test
    public void failedRefreshDoesNotCachePermissionsForAnotherSite() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SamigoJsfPermissions.get(request, "user", "first", () -> Map.of(AUTHZ_CREATE_ASSESSMENT + "_first", true));
        assertThrows(IllegalStateException.class, () -> SamigoJsfPermissions.get(request, "user", "second", () -> {
            throw new IllegalStateException("Permission lookup unavailable");
        }));
        assertTrue(SamigoJsfPermissions.get(request, "user", "second", Map::of).isEmpty());
    }
}
