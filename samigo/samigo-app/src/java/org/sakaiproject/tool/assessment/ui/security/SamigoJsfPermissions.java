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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import jakarta.servlet.ServletRequest;

/** Shares an immutable permission snapshot across dispatch and JSF phases of one request. */
public final class SamigoJsfPermissions {
    private static final String ATTRIBUTE = SamigoJsfPermissions.class.getName();

    private SamigoJsfPermissions() {
    }

    public static Set<String> get(ServletRequest request, String userId, String siteId,
            Supplier<Map<String, Boolean>> refreshPermissions) {
        Object cached = request.getAttribute(ATTRIBUTE);
        if (cached instanceof Snapshot snapshot && Objects.equals(userId, snapshot.userId())
                && Objects.equals(siteId, snapshot.siteId())) {
            return snapshot.permissions();
        }
        String suffix = "_" + siteId;
        Set<String> permissions = refreshPermissions.get().entrySet().stream()
            .filter(entry -> entry.getKey().endsWith(suffix) && Boolean.TRUE.equals(entry.getValue()))
            .map(entry -> entry.getKey().substring(0, entry.getKey().length() - suffix.length()))
            .collect(Collectors.toUnmodifiableSet());
        request.setAttribute(ATTRIBUTE, new Snapshot(userId, siteId, permissions));
        return permissions;
    }

    private record Snapshot(String userId, String siteId, Set<String> permissions) {
    }
}
