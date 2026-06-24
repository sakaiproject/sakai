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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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
  private final LtiBearerSessionSupport sessionSupport = new LtiBearerSessionSupport();

  @Autowired
  public LtiBearerTokenInterceptor(
      @Qualifier("org.sakaiproject.lti.api.SakaiAccessTokenService") SakaiAccessTokenService sakaiAccessTokenService,
      @Qualifier("org.sakaiproject.tool.api.SessionManager") SessionManager sessionManager) {
    this.sakaiAccessTokenService = sakaiAccessTokenService;
    this.sessionManager = sessionManager;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer")) {
      return true;
    }

    if (!sakaiAccessTokenService.isLtiBearerWebApiEnabled()) {
      log.warn("LTI Bearer access to webapi is disabled ({}=false)", SakaiAccessTokenService.PROPERTY_WEBAPI_ENABLED);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      try {
        response.getWriter().write("{\"error\":\"webapi_disabled\",\"message\":\"LTI Bearer access to webapi is disabled\"}");
      } catch (Exception ex) {
        log.debug("Could not write 403 body", ex);
      }
      return false;
    }

    log.info(STAR);
    log.info(BANNER);
    log.info("*** LTI Bearer Token Interceptor — preHandle START ***");
    log.info(BANNER);
    log.info("*** method={} uri={}", request.getMethod(), request.getRequestURI());
    log.info("*** Authorization header present (Bearer), length={}", authorization.length());
    log.info(STAR);

    try {
      String jws = sakaiAccessTokenService.extractBearerToken(authorization);
      log.info("*** extracted JWS from Bearer, jwsLength={}", jws != null ? jws.length() : 0);

      SakaiAccessToken sat = sakaiAccessTokenService.validateToken(jws);
      log.info("*** SAT validated toolId={} scope={} expires={}", sat.tool_id, sat.scope, sat.expires);

      sessionSupport.establishMiniSession(sessionManager, request, sat);

      log.info(STAR);
      log.info(BANNER);
      log.info("*** LTI Bearer Token Interceptor — mini-session ESTABLISHED ***");
      log.info("*** sessionUserId={}{}", LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX, sat.tool_id);
      log.info(BANNER);
      log.info(STAR);

      return true;
    } catch (SakaiAccessTokenException e) {
      log.info(STAR);
      log.info(BANNER);
      log.info("*** LTI Bearer Token Interceptor — REJECTED ***");
      log.info("*** errorKey={} message={}", e.getErrorKey(), e.getMessage());
      log.info(BANNER);
      log.info(STAR);
      log.warn("LTI Bearer token rejected: {} ({})", e.getMessage(), e.getErrorKey());
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

    log.info(STAR);
    log.info(BANNER);
    log.info("*** LTI Bearer Token Interceptor — afterCompletion CLEANUP ***");
    log.info("*** method={} uri={} status={}", request.getMethod(), request.getRequestURI(), response.getStatus());
    if (ex != null) {
      log.info("*** request exception: {}", ex.toString());
    }
    log.info(BANNER);
    log.info(STAR);

    sessionSupport.restoreSession(sessionManager, request);
  }
}
