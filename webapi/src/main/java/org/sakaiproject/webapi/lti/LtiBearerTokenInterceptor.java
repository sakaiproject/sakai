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
package org.sakaiproject.webapi.lti;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.lti.api.LtiBearerSessionConstants;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.lti.util.LtiBearerSessionSupport;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * When {@code Authorization: Bearer <SAT>} is present, validate the Sakai access token and
 * establish a minimal Sakai session for the request. No scope-to-function checks yet.
 */
@Slf4j
@Component
public class LtiBearerTokenInterceptor implements HandlerInterceptor {

    private static final String BANNER =
      "****************************************************************";
    private static final String STAR = "************************************************************";

    private final SakaiAccessTokenService sakaiAccessTokenService;
    private final SessionManager sessionManager;
    private final LtiBearerSessionSupport sessionSupport;

    @Autowired
    public LtiBearerTokenInterceptor(
        @Qualifier("org.sakaiproject.lti.api.SakaiAccessTokenService") SakaiAccessTokenService sakaiAccessTokenService,
        @Qualifier("org.sakaiproject.tool.api.SessionManager") SessionManager sessionManager) {

        this.sakaiAccessTokenService = sakaiAccessTokenService;
        this.sessionManager = sessionManager;
        this.sessionSupport = new LtiBearerSessionSupport();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String authorization = request.getHeader("Authorization");
        if (!sakaiAccessTokenService.isValidBearerHeader(authorization)) {
            return true;
        }

        if (!sakaiAccessTokenService.isWebApiEnabled()) {
            log.warn("LTI Bearer access to webapi is disabled ({}=false)", SakaiAccessTokenService.PROPERTY_WEBAPI_ENABLED);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "LTI Bearer access to webapi is disabled");
        }

        log.debug(STAR);
        log.debug(BANNER);
        log.debug("*** LTI Bearer Token Interceptor — preHandle START ***");
        log.debug(BANNER);
        log.debug("*** method={} uri={}", request.getMethod(), request.getRequestURI());
        log.debug("*** Authorization header present (Bearer), length={}", authorization.length());
        log.debug(STAR);

        try {
            String jws = sakaiAccessTokenService.extractBearerToken(authorization);
            log.debug("*** extracted JWS from Bearer, jwsLength={}", jws != null ? jws.length() : 0);

            SakaiAccessToken sat = sakaiAccessTokenService.validateToken(jws);
            log.debug("*** SAT validated toolId={} scope={} expires={}", sat.tool_id, sat.scope, sat.expires);

            sessionSupport.establishMiniSession(sessionManager, request, sat);

            log.debug(STAR);
            log.debug(BANNER);
            log.debug("*** LTI Bearer Token Interceptor — mini-session ESTABLISHED ***");
            log.debug("*** sessionUserId={}{}", LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX, sat.tool_id);
            log.debug(BANNER);
            log.debug(STAR);

            return true;
        } catch (SakaiAccessTokenException e) {
            log.debug(STAR);
            log.debug(BANNER);
            log.debug("*** LTI Bearer Token Interceptor — REJECTED ***");
            log.debug("*** errorKey={} message={}", e.getErrorKey(), e.getMessage());
            log.debug(BANNER);
            log.debug(STAR);
            log.warn("LTI Bearer token rejected: {} ({})", e.getMessage(), e.getErrorKey());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LTI Bearer token rejected");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
        Object handler, Exception ex) {
        if (request.getAttribute(LtiBearerSessionConstants.REQUEST_ATTR_LTI_SESSION) == null) {
            return;
        }

        log.debug(STAR);
        log.debug(BANNER);
        log.debug("*** LTI Bearer Token Interceptor — afterCompletion CLEANUP ***");
        log.debug("*** method={} uri={} status={}", request.getMethod(), request.getRequestURI(), response.getStatus());
        if (ex != null) {
            log.warn("*** request exception: {}", ex.toString());
        }
        log.debug(BANNER);
        log.debug(STAR);

        sessionSupport.restoreSession(sessionManager, request);
    }
}
