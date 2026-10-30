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

package org.sakaiproject.webapi.security;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.exception.MissingSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/** Authentication and ordinary site access are mandatory unless explicitly declared otherwise. */
@Component
public class WebApiAccessInterceptor implements HandlerInterceptor {

    private final SessionManager sessionManager;
    private final SiteService siteService;

    @Autowired
    public WebApiAccessInterceptor(
            @Qualifier("org.sakaiproject.tool.api.SessionManager") SessionManager sessionManager,
            SiteService siteService) {
        this.sessionManager = sessionManager;
        this.siteService = siteService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        if (method.hasMethodAnnotation(PublicApi.class)) {
            return true;
        }

        Session session;
        try {
            session = sessionManager.getCurrentSession();
        } catch (IllegalStateException e) {
            throw new MissingSessionException(e);
        }
        if (session == null || StringUtils.isBlank(session.getUserId())) {
            throw new MissingSessionException();
        }

        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof Map) {
            Object siteId = ((Map<?, ?>) variables).get("siteId");
            if (siteId instanceof String) {
                try {
                    siteService.getSiteVisit((String) siteId);
                } catch (IdUnusedException e) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                } catch (PermissionException e) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            }
        }
        return true;
    }
}
