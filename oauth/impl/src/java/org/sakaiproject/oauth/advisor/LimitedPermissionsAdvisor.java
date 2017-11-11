/*
 * #%L
 * OAuth Implementation
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.advisor;

import org.sakaiproject.authz.api.SecurityAdvisor;

import java.util.HashSet;
import java.util.Set;

/**
 * Advisor allowing only permissions authorised to a specific consumer.
 *
 * @author Colin Hebert
 */
public class LimitedPermissionsAdvisor implements SecurityAdvisor {

    private Set<String> allowedPermissions;

    public LimitedPermissionsAdvisor(Set<String> permissions) {
        allowedPermissions = new HashSet<String>(permissions);
    }

    @Override
    public SecurityAdvice isAllowed(String userId, String function, String reference) {
        return (allowedPermissions.contains(function)) ? SecurityAdvice.PASS : SecurityAdvice.NOT_ALLOWED;
    }

    @Override
    public String toString() {
        return "Allowed permissions: " + allowedPermissions;
    }
}
