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
package org.sakaiproject.lti.api;

import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.tool.api.Session;

/**
 * Helpers for reading LTI Bearer state from a Sakai session.
 */
public final class LtiBearerSessions {

    private LtiBearerSessions() {
    }

    public static SakaiAccessToken getSakaiAccessToken(Session session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(LtiBearerSessionConstants.SESSION_ATTR_SAT);
        if (value instanceof SakaiAccessToken) {
            return (SakaiAccessToken) value;
        }
        return null;
    }
}
