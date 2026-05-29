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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.sakaiproject.lti.api.LtiBearerSessionConstants;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class LtiDirectAuthControllerTest {

    private static final long TOOL_ID = 7L;

    @Mock private SessionManager sessionManager;
    @Mock private Session session;

    private LtiDirectAuthController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LtiDirectAuthController(sessionManager);
    }

    @Test
    public void bearerProbeReturnsTokenDetails() {
        SakaiAccessToken sat = new SakaiAccessToken();
        sat.tool_id = TOOL_ID;
        sat.expires = Long.valueOf(9999999999L);
        sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);

        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getId()).thenReturn("session-abc");
        when(session.getUserId()).thenReturn(LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX + TOOL_ID);
        when(session.getAttribute(LtiBearerSessionConstants.SESSION_ATTR_SAT)).thenReturn(sat);

        Map<String, Object> result = controller.bearerProbe();

        assertEquals(Boolean.TRUE, result.get("ok"));
        assertEquals("sess", result.get("sessionIdPrefix"));
        assertFalse(result.containsKey("sessionId"));
        assertEquals(LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX + TOOL_ID, result.get("sessionUserId"));
        assertEquals(Boolean.TRUE, result.get("ltiAuthenticated"));
        assertEquals(TOOL_ID, result.get("toolId"));
        assertTrue(result.get("scope").toString().contains(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
        assertEquals(sat.expires, result.get("expires"));
    }
}
