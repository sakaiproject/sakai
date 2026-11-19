/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.emailtemplateservice.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.SecFetchSiteCsrf;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Protects the template administration tool before any template is read or changed. */
@Component
public class EmailTemplateSecurityInterceptor implements HandlerInterceptor {

    @Autowired private SessionManager sessionManager;
    @Autowired private SecurityService securityService;
    @Autowired private LocaleService localeService;
    @Autowired private MessageSource messageSource;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String userId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isNotBlank(userId) && securityService.isSuperUser(userId)
                && !SecFetchSiteCsrf.isUntrustedUnsafeRequest(request)) {
            return true;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            JSONArray errors = new JSONArray();
            errors.add(messageSource.getMessage("GeneralActionError", null, localeService.getLocaleForCurrentSiteAndUser()));
            JSONObject body = new JSONObject();
            body.put("status", "ERROR");
            body.put("errors", errors);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(body.toJSONString());
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }
}
