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
package org.sakaiproject.lti.util;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.lti.api.LtiBearerSessionConstants;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Establishes a minimal Sakai session for an LTI tool Bearer token request (webapi, /direct).
 */
@Slf4j
public class LtiBearerSessionSupport {

    /**
     * @return the new LTI mini-session (also set as current)
     */
    public Session establishMiniSession(SessionManager sessionManager, HttpServletRequest request,
            SakaiAccessToken sat) {

        Session previous = sessionManager.getCurrentSession();
        request.setAttribute(LtiBearerSessionConstants.REQUEST_ATTR_PREVIOUS_SESSION, previous);

        Session ltiSession = sessionManager.startSession();
        String toolPrincipal = LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX + sat.tool_id;
        ltiSession.setUserId(toolPrincipal);
        ltiSession.setUserEid(toolPrincipal);
        ltiSession.setAttribute(LtiBearerSessionConstants.SESSION_ATTR_SAT, sat);
        sessionManager.setCurrentSession(ltiSession);

        request.setAttribute(LtiBearerSessionConstants.REQUEST_ATTR_LTI_SESSION, ltiSession);

        log.debug("LTI bearer mini-session toolId={} sessionId={}", sat.tool_id, ltiSession.getId());
        return ltiSession;
    }

    public void restoreSession(SessionManager sessionManager, HttpServletRequest request) {

        Session ltiSession = (Session) request.getAttribute(LtiBearerSessionConstants.REQUEST_ATTR_LTI_SESSION);
        if (ltiSession != null) {
            try {
                ltiSession.invalidate();
            } catch (IllegalStateException e) {
                log.debug("LTI mini-session already invalidated: {}", e.toString());
            }
        }

        Session previous = (Session) request.getAttribute(LtiBearerSessionConstants.REQUEST_ATTR_PREVIOUS_SESSION);
        if (previous != null) {
            sessionManager.setCurrentSession(previous);
        }
    }
}
