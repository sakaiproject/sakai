/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.entitybroker.lti;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.lti.api.LtiBearerSessionConstants;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.lti.util.LtiBearerSessionSupport;
import org.sakaiproject.tool.api.SessionManager;
import org.tsugi.jackson.JacksonUtil;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * When {@code Authorization: Bearer <SAT>} is present, validate the Sakai access token and
 * establish a minimal Sakai session for the request. No scope-to-function checks yet.
 */
@Slf4j
public class LtiBearerDirectInterceptor implements HandlerInterceptor {

    private final SakaiAccessTokenService sakaiAccessTokenService;
    private final SessionManager sessionManager;
    private final LtiBearerSessionSupport sessionSupport = new LtiBearerSessionSupport();

    public LtiBearerDirectInterceptor(SakaiAccessTokenService sakaiAccessTokenService,
            SessionManager sessionManager) {
        this.sakaiAccessTokenService = sakaiAccessTokenService;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer")) {
            return true;
        }

        if (!sakaiAccessTokenService.isLtiBearerDirectEnabled()) {
            log.warn("LTI Bearer access to /direct is disabled ({}=false)",
                    SakaiAccessTokenService.PROPERTY_DIRECT_ENABLED);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                response.getWriter().write(
                        "{\"error\":\"direct_disabled\",\"message\":\"LTI Bearer access to /direct is disabled\"}");
            } catch (Exception ex) {
                log.debug("Could not write 403 body", ex);
            }
            return false;
        }

        try {
            String jws = sakaiAccessTokenService.extractBearerToken(authorization);
            SakaiAccessToken sat = sakaiAccessTokenService.validateToken(jws);
            sessionSupport.establishMiniSession(sessionManager, request, sat);
            log.debug("LTI bearer mini-session established for toolId={} uri={}", sat.tool_id, request.getRequestURI());
            return true;
        } catch (SakaiAccessTokenException e) {
            log.warn("LTI Bearer token rejected on /direct: {} ({})", e.getMessage(), e.getErrorKey());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                Map<String, String> body = new HashMap<>();
                body.put("error", e.getErrorKey());
                body.put("message", e.getMessage());
                String json = JacksonUtil.toString(body);
                if (json != null) {
                    response.getWriter().write(json);
                }
            } catch (Exception ex) {
                log.debug("Could not write 401 body", ex);
            }
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        if (request.getAttribute(LtiBearerSessionConstants.REQUEST_ATTR_LTI_SESSION) == null) {
            return;
        }
        sessionSupport.restoreSession(sessionManager, request);
    }
}
