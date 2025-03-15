/**
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.login.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * A SAML logout handler that integrates with Sakai session management.
 * This performs a logout of Sakai by invalidating the Sakai session.
 */
@Slf4j
public class SakaiLogoutHandler implements LogoutHandler {

    @Setter private UsageSessionService usageSessionService;
    @Setter private SessionManager sessionManager;

    @Getter @Setter private boolean invalidateSakaiSession = true;

    /**
     * Requires the request to be passed in.
     *
     * @param request        from which to obtain the HTTP session (cannot be null)
     * @param response       not used
     * @param authentication not used
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (invalidateSakaiSession) {
            Session session = sessionManager.getCurrentSession();

            if (session != null) {
                log.debug("SAML logout invalidating Sakai session: {}", session.getId());
                usageSessionService.logout();
            }
        }
    }
}