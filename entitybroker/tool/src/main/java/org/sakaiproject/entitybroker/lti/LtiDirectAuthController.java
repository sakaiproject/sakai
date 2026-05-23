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

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.lti.api.LtiBearerSessions;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Diagnostics for LTI Bearer (SAT) authentication on Entity Broker ({@code /direct}).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LtiDirectAuthController {

    private final SessionManager sessionManager;

    /**
     * Call with {@code Authorization: Bearer &lt;SAT&gt;}. Returns token and session details when valid.
     */
    @GetMapping(value = "/lti/bearer-probe", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> bearerProbe() {

        Session session = checkSakaiSession();
        SakaiAccessToken sat = LtiBearerSessions.getSakaiAccessToken(session);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", Boolean.TRUE);
        result.put("sessionId", session.getId());
        result.put("sessionUserId", session.getUserId());
        result.put("ltiAuthenticated", sat != null);

        if (sat != null) {
            result.put("toolId", sat.tool_id);
            result.put("scope", sat.scope);
            result.put("expires", sat.expires);
            if (StringUtils.isNotBlank(sat.site_id)) {
                result.put("siteId", sat.site_id);
            }
        }

        log.debug("LTI bearer probe on /direct toolId={} sessionUserId={}",
                sat != null ? sat.tool_id : null, session.getUserId());
        return result;
    }

    private Session checkSakaiSession() {
        Session session = sessionManager.getCurrentSession();
        if (session == null || StringUtils.isBlank(session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sakai user session is invalid");
        }
        return session;
    }
}
