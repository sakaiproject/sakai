/**
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
package org.sakaiproject.login.springframework;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.login.tool.SkinnableLogin;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;

/** Complete Sakai container login after Spring has validated the SAML response. */
@Slf4j
public class SakaiSamlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthenticationManager authenticationManager;
    private final UsageSessionService usageSessionService;
    private final SessionManager sessionManager;
    private final ServerConfigurationService configuration;
    private final String principalAttribute;

    public SakaiSamlAuthenticationSuccessHandler(AuthenticationManager authenticationManager,
            UsageSessionService usageSessionService, SessionManager sessionManager,
            ServerConfigurationService configuration, String principalAttribute) {
        this.authenticationManager = authenticationManager;
        this.usageSessionService = usageSessionService;
        this.sessionManager = sessionManager;
        this.configuration = configuration;
        this.principalAttribute = principalAttribute;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        String eid = principal.getName();
        if (StringUtils.hasText(principalAttribute)) {
            List<Object> values = principal.getAttribute(principalAttribute);
            // Missing or ambiguous mappings must not fall back to a different identity.
            eid = values != null && values.size() == 1 && values.get(0) instanceof String
                    ? (String) values.get(0) : null;
        }
        Session session = sessionManager.getCurrentSession();
        if (StringUtils.hasText(eid) && session != null) {
            try {
                org.sakaiproject.user.api.Authentication sakaiAuthentication =
                        authenticationManager.authenticate(new ExternalTrustedEvidence(eid));
                if (usageSessionService.login(sakaiAuthentication.getUid(), sakaiAuthentication.getEid(),
                        request.getRemoteAddr(), request.getHeader("user-agent"), UsageSessionService.EVENT_LOGIN_CONTAINER)) {
                    String target = (String) session.getAttribute(Tool.HELPER_DONE_URL);
                    session.removeAttribute(Tool.HELPER_MESSAGE);
                    session.removeAttribute(Tool.HELPER_DONE_URL);
                    session.setAttribute(SkinnableLogin.ATTR_CONTAINER_SUCCESS, SkinnableLogin.ATTR_CONTAINER_SUCCESS);
                    response.sendRedirect(response.encodeRedirectURL(
                            StringUtils.hasText(target) ? target : configuration.getPortalUrl()));
                    return;
                }
            } catch (AuthenticationException exception) {
                log.warn("SAML identity could not authenticate with Sakai", exception);
            }
        }
        // Do not leave an authenticated Spring session after Sakai rejects the identity.
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        if (session != null) {
            session.setAttribute(SkinnableLogin.ATTR_CONTAINER_CHECKED, SkinnableLogin.ATTR_CONTAINER_CHECKED);
        }
        response.sendRedirect(configuration.getPortalUrl() + "/xlogin");
    }
}
