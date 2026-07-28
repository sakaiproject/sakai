/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SakaiCsrfTokens {

    public static final String REQUEST_PARAMETER = "sakai_csrf_token";

    private final SessionManager sessionManager;

    public String currentToken() {
        Session session = sessionManager.getCurrentSession();
        Object token = session == null ? null
                : session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
        return token == null ? null : token.toString();
    }

    public boolean matches(String submittedToken) {
        String expectedToken = currentToken();
        return StringUtils.isNotBlank(expectedToken)
                && StringUtils.equals(expectedToken, submittedToken);
    }
}
